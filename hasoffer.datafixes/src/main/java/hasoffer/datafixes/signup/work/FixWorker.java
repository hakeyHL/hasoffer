package hasoffer.datafixes.signup.work;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.mongo.UserSignLog;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.system.IAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

public class FixWorker {
    private static final Logger logger = LoggerFactory.getLogger(FixWorker.class);

    @Resource
    private MongoDbManager mongoDbManager;

    @Resource
    private IAppService appService;

    public void runTask() {
        List<UserSignLog> signLogList = mongoDbManager.query(UserSignLog.class, null);
        logger.info("find sign log size():{}", signLogList.size());
        Map<Long, List<Long>> userSignMap = new HashMap<>();
        for (UserSignLog log : signLogList) {
            List<Long> signList = userSignMap.get(log.getUserId());
            if (signList == null) {
                signList = new ArrayList<>();
                userSignMap.put(log.getUserId(), signList);
            }
            signList.add(log.getSignDate());
        }

        for (Map.Entry<Long, List<Long>> x : userSignMap.entrySet()) {
            Long key = x.getKey();
            List<Long> signList = x.getValue();
            Collections.sort(signList, new Comparator<Long>() {

                /*
                 * int compare(Student o1, Student o2) 返回一个基本类型的整型，
                 * 返回负数表示：o1 小于o2，
                 * 返回0 表示：o1和o2相等，
                 * 返回正数表示：o1大于o2。
                 */
                public int compare(Long o1, Long o2) {

                    //按照学生的年龄进行升序排列
                    if (o1 > o2) {
                        return 1;
                    }
                    if (o1.longValue() == o2.longValue()) {
                        return 0;
                    }
                    return -1;
                }
            });

            // 计算签到用户的实际签到天数及连续性
            UrmUser user = appService.getUserById(key);
            if (user != null) {
                calSignUpCoin(user, signList);
                appService.updateUserInfo(user);
            }
        }

    }

    private void calSignUpCoin(UrmUser user, List<Long> signTimes) {
        //Long userId = user.getKey();
        //List<Long> signTimes = user.getValue();
        long signCoin = 0L;
        Long lastSignTime = 0L;
        int conSignNum = 0;
        user.setMaxConSignNum(1);
        for (Long signTime : signTimes) {
            long x = (signTime + TimeUtils.MILLISECONDS_OF_1_HOUR * 8) / TimeUtils.MILLISECONDS_OF_1_DAY - (lastSignTime + TimeUtils.MILLISECONDS_OF_1_HOUR * 8) / TimeUtils.MILLISECONDS_OF_1_DAY;
            lastSignTime = signTime;
            user.setLastSignTime(lastSignTime);
            if (x < 1) {
                continue;
            } else if (x == 1) {
                conSignNum++;
            } else if (x > 1) {
                if (user.getMaxConSignNum() < conSignNum) {
                    user.setMaxConSignNum(conSignNum);
                }
                conSignNum = 1;
            }
            logger.info("user:{}, Sign time:{}, conSignUp:{}, Day jet lag:{}", user.getId(), new Date(signTime), conSignNum, x);
            signCoin += getThisCoin(conSignNum);

        }
        user.setSignCoin(signCoin);
        user.setConSignNum(conSignNum);
    }

    private Long getThisCoin(int conSignNum) {
        if (conSignNum == 1) {
            return 10L;
        } else if (conSignNum == 2) {
            return 15L;
        } else if (conSignNum == 3) {
            return 20L;
        } else if (conSignNum == 4) {
            return 25L;
        } else if (conSignNum == 5) {
            return 30L;
        } else if (conSignNum == 6) {
            return 35L;
        } else if (conSignNum >= 7) {
            return 40L;
        }
        return 0L;
    }


}

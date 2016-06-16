package hasoffer.core.admin.impl;

import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IAdminCountService;
import hasoffer.core.persistence.dbm.HibernateDao;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.UrmDeviceRequestLog;
import hasoffer.core.persistence.po.urm.UrmBrand;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.persistence.po.urm.UrmVersions;
import hasoffer.core.user.IDeviceService;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2016/4/7.
 */
@Service
@Transactional
public class AdminCountServiceImpl implements IAdminCountService {

    private final String STS_ALIVE_INSERT_PRE = "insert ignore into stsalive";
    private final String STS_ALIVE_INSERT_BODY = " (deviceId,wakeupTime,ratioTime,osVersion,brand,eCommerce,assistIsActive,assistIsFirst,showIcon,clickIcon,clickShop,marketChannel,Campaign,ADset) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    @Resource
    IDataBaseManager dbm;
    @Resource
    IDeviceService deviceService;
    @Resource
    HibernateDao dao;
    @Resource
    private JdbcTemplate jdbcTemplate;
    private Logger logger = LoggerFactory.logger(AdminCountServiceImpl.class);


    @Override
    public PageableResult<Map<String, Object>> findStsAlive(Map<String, String> params, int page, int size) {
        String baseDate = params.get("baseDate");
        String marketChannelString = params.get("marketChannel");
        String sort = params.get("sort");
        Integer deviceNum = Integer.parseInt(params.get("deviceNum"));
        Integer ratioNum = Integer.parseInt(params.get("ratioNum"));

        MarketChannel marketChannel = null;
        if (!StringUtils.isEqual("0", marketChannelString)) {
            if (!StringUtils.isEmpty(marketChannelString)) {
                marketChannel = MarketChannel.valueOf(marketChannelString);
            }
        }

        StringBuilder sql = new StringBuilder("select date_format(u.createTime, '%Y-%m-%d') as date, count(s.wakeupTime) as alives, ");
        sql.append(" count(s.wakeupTime)").append("/").append(deviceNum).append(" *100").append(" as alivesPercent, ");
        sql.append(" count(s.ratioTime) as ratios, ");
        sql.append(" count(s.ratioTime)").append("/").append(ratioNum).append(" *100").append(" as ratioPercent ");
        sql.append(" from stsAlive").append(baseDate.replaceAll("-", "").substring(0, 6)).append(" s, urmDevice u where s.deviceId = u.id and date_format(s.wakeupTime, '%Y-%m-%d') = ? and  s.eCommerce != ''");
        if (marketChannel != null) {
            sql.append(" and s.marketChannel = " + marketChannel);
        }
        sql.append(" group by date");
        sql = orderClause(sort, sql);

        logger.info(sql.toString());
        return dao.findPageOfMapBySql(sql.toString(), page, size, baseDate);
    }

    @Override
    public int totalAliveDevice(String baseDate) {
        String sql = "select ifnull(sum(newAlive),0) as num from stsDayAlive t where date_format(t.date, '%Y-%m-%d') = ? ";
        BigDecimal num = dao.findUniqueBySql(sql, baseDate);
        return num.intValue();
    }

    @Override
    public int totalRatioDevice(String baseDate) {
        String sql = "select ifnull(sum(clickShopNew),0) as num from stsDayAlive t where date_format(t.date, '%Y-%m-%d') = ?";
        BigDecimal num = dao.findUniqueBySql(sql, baseDate);
        return num.intValue();
    }

    private StringBuilder orderClause(String sort, StringBuilder query) {
        if (!StringUtils.isEmpty(sort)) {
            if ("0".equals(sort)) {
                query.append(" order by date desc");
            } else if ("1".equals(sort)) {
                query.append(" order by date asc");
            } else {
                query.append(" order by id asc");
            }
        }

        return query;
    }

    @Override
    public List<String> findTables() {
        return dao.findBySql("SHOW TABLES LIKE 'stsalive20%';");
    }

    @Override
    public List<Map<String, Object>> getWakeupTime(String alive) {
        return dao.findMapBySql("select * from " + alive + " order by wakeupTime desc limit 1");
    }


    @Override
    public List<Map<String, Object>> findStsDayAlive() {
        return dao.findMapBySql("select * from StsDayAlive order by date desc limit 1");
    }

    @Override
    @Transactional
    public List<String> getAliveTables() {
        return dao.findBySql("SHOW TABLES LIKE 'stsalive20%';");
    }

    @Override
    @Transactional
    public List<Map<String, Object>> findDataMapsBySQL(String sql) {
        return dao.findMapBySql(sql);
    }

    private void updateStsAlive(String date, String deviceId, String wakeupTime, String fieldName, String fieldVal) {
        String sql = "update stsalive" + date + " SET " + fieldName + "=? WHERE wakeupTime=? and deviceId=?";
//        dao.updateBySql(sql, fieldVal, wakeupTime, deviceId);
        List<Object[]> datas = new ArrayList<Object[]>();
        datas.add(new Object[]{fieldVal, wakeupTime, deviceId});
        batchUpdate(sql, datas);
    }

    @Override
    public void cntFromAliveToDayAlive(Date logsMaxCreateTime) {
        while (true) {
            String dateStr = TimeUtils.parse(logsMaxCreateTime, "yyyyMM");
            String date = TimeUtils.parse(logsMaxCreateTime, "yyyy-MM-dd");

            List<UrmBrand> UrmBrandList = dbm.query("select t from UrmBrand t order by t.id asc");
            List<UrmVersions> UrmVersionslist = dbm.query("select t from UrmVersions t order by t.id asc");

            //如果没有这一天的组合条件的记录,则插入;如果有这条记录,则更新统计字段
            String sql = "insert into StsDayAlive (date,newAlive,eCommerceNew,assistIsFirst,showIconNew," +
                    "clickIconNew,clickShopNew,osVersion,brand,marketChannel,updateTime) values " +
                    "(?,?,?,?,?,?,?,?,?,?,?)" +
                    "on duplicate key update newAlive=values(newAlive),eCommerceNew=values(ecommerceNew)," +
                    "assistIsFirst=values(assistIsFirst),showIconNew=values(showIconNew)," +
                    "clickIconNew=values(clickIconNew),clickShopNew=values(clickShopNew);";

            List<Object[]> data = new ArrayList<Object[]>();

            for (UrmBrand brand : UrmBrandList) {
                for (UrmVersions version : UrmVersionslist) {
                    for (MarketChannel channel : MarketChannel.values()) {
                        String brandName = brand.getName();
                        String versionName = version.getName();

                        int newAlive = selectCntByDateField(dateStr, date, "wakeupTime", brandName, versionName, channel);
                        int ecommerceNew = selectCntByDateField(dateStr, date, "eCommerce", brandName, versionName, channel);
                        int assistIsFirst = selectCntByDateField(dateStr, date, "assistIsFirst", brandName, versionName, channel);
                        int showIcon = selectCntByDateField(dateStr, date, "showIcon", brandName, versionName, channel);
                        int clickIcon = selectCntByDateField(dateStr, date, "clickIcon", brandName, versionName, channel);
                        int clickShop = selectCntByDateField(dateStr, date, "clickShop", brandName, versionName, channel);

                        if (newAlive == 0 && ecommerceNew == 0 && assistIsFirst == 0 && showIcon == 0 && showIcon == 0 && clickIcon == 0 && clickShop == 0) {
                            continue;
                        }

                        logger.info("newAlive:" + newAlive + " ecommerceNew:" + ecommerceNew + " assistIsFirst" + assistIsFirst + " showIcon" + showIcon + " clickIcon" + clickIcon + " clickShop" + clickShop);

                        Object[] obj = new Object[11];
                        obj[0] = date;
                        obj[1] = newAlive;
                        obj[2] = ecommerceNew;
                        obj[3] = assistIsFirst;
                        obj[4] = showIcon;
                        obj[5] = clickIcon;
                        obj[6] = clickShop;
                        obj[7] = versionName;
                        obj[8] = brandName;
                        obj[9] = channel.toString();
                        obj[10] = TimeUtils.nowDate();

                        data.add(obj);

                        if (data.size() % 2000 == 0) {
                            int[] batchSize = jdbcTemplate.batchUpdate(sql, data);
                            logger.info("batch size :" + batchSize.length);
                            data.clear();
                        }
                    }
                }
            }
            int[] leftBatchSize = jdbcTemplate.batchUpdate(sql, data);
            logger.info("left batch size " + leftBatchSize.length);

            logsMaxCreateTime = TimeUtils.addDay(logsMaxCreateTime, 1);
            if (logsMaxCreateTime.getTime() > TimeUtils.nowDate().getTime()) {
                break;
            }
        }
    }


    @Transactional
    private int selectCntByDateField(String date, String wakeupTime, String fieldName, String brand, String version, MarketChannel channel) {

        String sql = "select id from stsAlive" + date + " WHERE wakeupTime=? and " + fieldName + "=1 and brand=? and osVersion=? and marketChannel=?";

        if (fieldName.equals("wakeupTime")) {
            sql = "select id from stsAlive" + date + " WHERE wakeupTime=? and brand=? and osVersion=? and marketChannel=?";
        }

        if (fieldName.equals("eCommerce")) {
            sql = "select id from stsAlive" + date + " WHERE wakeupTime=? and eCommerce<>'' and brand=? and osVersion=? and marketChannel=?";
        }
        int cnt = dao.countBySql(sql, wakeupTime, brand, version, channel.toString());

        return cnt;
    }

    @Override
    public void cntFromAliveUpdateDayAlive(Date logsMaxCreateTime) {
        while (true) {
            Date yestodayDate = TimeUtils.addDay(logsMaxCreateTime, -1);
            String yestoday = TimeUtils.parse(yestodayDate, "yyyy-MM-dd");
            String today = TimeUtils.parse(logsMaxCreateTime, "yyyy-MM-dd");

            List<Object[]> data = new ArrayList<Object[]>();
            String sql = "update StsDayAlive set allAlive=?,eCommerceALl=?,assistNotFirst=?,showIconAll=?,clickIconALl=?,clickShopAll=? where " +
                    "date=? and brand=? and osVersion=? and marketChannel=?";

            List<UrmBrand> UrmBrandList = dbm.query("select t from UrmBrand t order by t.id asc");
            List<UrmVersions> UrmVersionslist = dbm.query("select t from UrmVersions t order by t.id asc");
            for (UrmBrand brand : UrmBrandList) {
                for (UrmVersions version : UrmVersionslist) {
                    for (MarketChannel channel : MarketChannel.values()) {
                        String brandName = brand.getName();
                        String versionName = version.getName();

                        BigDecimal allAlive = selectSumCntByDateField(yestoday, "newAlive", brandName, versionName, channel);
                        BigDecimal ecommerceAll = selectSumCntByDateField(yestoday, "eCommerceNew", brandName, versionName, channel);
                        BigDecimal assistNotFirst = selectSumCntByDateField(yestoday, "assistIsFirst", brandName, versionName, channel);
                        BigDecimal showIconAll = selectSumCntByDateField(yestoday, "showIconNew", brandName, versionName, channel);
                        BigDecimal clickIconAll = selectSumCntByDateField(yestoday, "clickIconNew", brandName, versionName, channel);
                        BigDecimal clickShopAll = selectSumCntByDateField(yestoday, "clickShopNew", brandName, versionName, channel);

                        Object[] obj = new Object[10];
                        obj[0] = today;
                        obj[1] = brandName;
                        obj[2] = versionName;
                        obj[3] = channel;
                        obj[4] = allAlive;
                        obj[5] = ecommerceAll;
                        obj[6] = assistNotFirst;
                        obj[7] = showIconAll;
                        obj[8] = clickIconAll;
                        obj[9] = clickShopAll;

                        data.add(obj);
                        if (data.size() % 2000 == 0) {
                            int[] batchSize = jdbcTemplate.batchUpdate(sql, data);
                            logger.info("batch size :" + batchSize.length);
                            data.clear();
                        }

                    }
                }
            }

            int[] leftBatchSize = jdbcTemplate.batchUpdate(sql, data);
            logger.info("left batch size :" + leftBatchSize.length);

            logsMaxCreateTime = TimeUtils.addDay(logsMaxCreateTime, 1);
            if (logsMaxCreateTime.getTime() > TimeUtils.nowDate().getTime()) {
                break;
            }
        }
    }

    private BigDecimal selectSumCntByDateField(String date, String fieldName, String brand, String version, MarketChannel channel) {
        String sql = "select ifnull(sum(" + fieldName + "),0) as sum from stsDayAlive WHERE date <= ? and brand=? and osVersion=? and marketChannel=?";
        List<BigDecimal> cntResult = dao.findBySql(sql, date, brand, version, channel.toString());
        return cntResult.get(0);
    }

    @Transactional
    @Deprecated
    private void batchUpdate(String sql, List<Object[]> data) {
        int[] batchSize = jdbcTemplate.batchUpdate(sql, data);
        logger.info("batchSize : " + batchSize.length);
        data.clear();
    }

    @Deprecated
    @Override
    public void deviceRequestLogsToAlive(Date logsMaxCreateTime) {

        Pattern pattern_wakeup = Pattern.compile(".*action=wakeUp.*");

        while (true) {
            Date endDate = TimeUtils.addDay(logsMaxCreateTime, 1);
            String dateStr = TimeUtils.parse(endDate, "yyyyMM");

            PageableResult<UrmDeviceRequestLog> logs = deviceService.findDeviceLogsByRequestUri("/app/dot", logsMaxCreateTime, endDate, 1, Integer.MAX_VALUE);
            List<UrmDeviceRequestLog> urmDeviceRequestLogList = logs.getData();

            logger.info(TimeUtils.parse(logsMaxCreateTime, "yyyy-MM-dd") + " total Size from mongodb : " + logs.getData().size());

            String sql = STS_ALIVE_INSERT_PRE + dateStr + STS_ALIVE_INSERT_BODY;
            List<Object[]> data = new ArrayList<Object[]>();

            for (UrmDeviceRequestLog urmDeviceRequestLog : urmDeviceRequestLogList) {
                String query = urmDeviceRequestLog.getQuery();
                String deviceId = urmDeviceRequestLog.getDeviceId();
                Date date = urmDeviceRequestLog.getCreateTime();
                if (!StringUtils.isEmpty(deviceId) && !StringUtils.isEmpty(query)) {
                    String wakeupDate = TimeUtils.parse(date, "yyyy-MM-dd");
                    //假设存活的设备当天发出的第一条信息一定是存活信息,而不是其他信息
                    Matcher matcher = pattern_wakeup.matcher(query);
                    boolean deviceIsAlive = matcher.matches();
                    if (deviceIsAlive) {
                        UrmDevice device = deviceService.findDevice(deviceId);

                        if (device == null) {
                            continue;
                        }

                        MarketChannel marketChannel = device.getMarketChannel();

                        if (device != null) {
                            Object[] obj = new Object[14];
                            obj[0] = deviceId;
                            obj[1] = wakeupDate;
                            obj[2] = null;
                            obj[3] = device.getOsVersion();
                            obj[4] = device.getBrand();
                            obj[5] = device.getShopApp();
                            obj[6] = 0;
                            obj[7] = 0;
                            obj[8] = 0;
                            obj[9] = 0;
                            obj[10] = 0;
                            obj[11] = marketChannel == null ? "" : marketChannel.toString();
                            obj[12] = "";
                            obj[13] = "";
                            data.add(obj);

                            batchUpdate(sql, data);

//                            if (data.size() % 2000 == 0) {

//                                int[] batchSize = jdbcTemplate.batchUpdate(sql, data);
//                                data.clear();
//                                batchUpdate(sql, data);
//                            }
                        }


                    }
                }
            }
//            int[] leftSize = jdbcTemplate.batchUpdate(sql, data);
//            logger.info("left batch size  : " + leftSize.length);
//            batchUpdate(sql, data);

            if (endDate.getTime() > TimeUtils.nowDate().getTime()) {
                break;
            } else {
                logsMaxCreateTime = TimeUtils.addDay(logsMaxCreateTime, 1);
            }

        }
    }

    @Deprecated
    @Override
    public void deviceRequestLogsAliveUpdate(Date logsMaxCreateTime) {
        Pattern pattern_wakeup = Pattern.compile(".*action=wakeUp.*");
        Pattern pattern_assist_first = Pattern.compile(".*action=autoModifyAccessSuccess.*");
        Pattern pattern_show_icon = Pattern.compile(".*action=showIcon.*");
        Pattern pattern_shop = Pattern.compile(".*action=shop.*");
        Pattern pattern_click = Pattern.compile(".*action=priceList.*");

        while (true) {
            Date endDate = TimeUtils.addDay(logsMaxCreateTime, 1);
            String dateStr = TimeUtils.parse(endDate, "yyyyMM");
            PageableResult<UrmDeviceRequestLog> logs = deviceService.findDeviceLogsByRequestUri("/app/dot", logsMaxCreateTime, endDate, 1, Integer.MAX_VALUE);
            List<UrmDeviceRequestLog> urmDeviceRequestLogList = logs.getData();
            logger.info(TimeUtils.parse(logsMaxCreateTime, "yyyy-MM-dd") + " size from mongodb : " + urmDeviceRequestLogList.size());
            for (UrmDeviceRequestLog urmDeviceRequestLog : urmDeviceRequestLogList) {
                String query = urmDeviceRequestLog.getQuery();
                String deviceId = urmDeviceRequestLog.getDeviceId();
                Date date = urmDeviceRequestLog.getCreateTime();
                if (!StringUtils.isEmpty(deviceId)) {
                    String wakeup_date = TimeUtils.parse(date, "yyyy-MM-dd");
                    Matcher matcher = pattern_wakeup.matcher(query);

                    boolean deviceIsAlive = matcher.matches();

                    if (deviceIsAlive) {
                        continue;
                    }

                    matcher = pattern_assist_first.matcher(query);
                    boolean deviceIsFirstbind = matcher.matches();

                    //辅助功能打开设备（首次/非首次）
                    if (deviceIsFirstbind) {
                        updateStsAlive(dateStr, deviceId, wakeup_date, "assistIsActive", "1");
                        updateStsAlive(dateStr, deviceId, wakeup_date, "assistIsFirst", "1");
                        continue;
                    }

                    // 展示比价icon设备（老/新）
                    matcher = pattern_show_icon.matcher(query);
                    boolean show_icon = matcher.matches();
                    if (show_icon) {
                        updateStsAlive(dateStr, deviceId, wakeup_date, "showIcon", "1");
                        continue;
                    }

                    //点击比价icon设备
                    matcher = pattern_show_icon.matcher(query);
                    boolean click_icon = matcher.matches();
                    if (click_icon) {
                        updateStsAlive(dateStr, deviceId, wakeup_date, "clickIcon", "1");
                        continue;
                    }

                    //点击Shop设备
                    matcher = pattern_shop.matcher(query);
                    boolean do_shop = matcher.matches();
                    if (do_shop) {
                        updateStsAlive(dateStr, deviceId, wakeup_date, "clickShop", "1");
                        continue;
                    }

                    //比价时间
                    matcher = pattern_click.matcher(query);
                    boolean do_ratio = matcher.matches();
                    if (do_ratio) {
                        updateStsAlive(dateStr, deviceId, wakeup_date, "ratioTime", TimeUtils.parse(date, "yyyy-MM-dd"));
                        continue;
                    }

                }
            }
            if (endDate.getTime() > TimeUtils.nowDate().getTime()) {
                break;
            } else {
                logsMaxCreateTime = TimeUtils.addDay(logsMaxCreateTime, 1);
            }
        }
    }
}

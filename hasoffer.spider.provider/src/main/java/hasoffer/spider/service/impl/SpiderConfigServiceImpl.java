package hasoffer.spider.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.exception.WebSiteException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderConfig;
import hasoffer.spider.service.ISpiderConfigService;
import hasoffer.spider.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component("spiderConfigService")
public class SpiderConfigServiceImpl implements ISpiderConfigService {

    private final Logger logger = LoggerFactory.getLogger(ISpiderConfigService.class);

    @Override
    public List<Website> findSupportWebSite(PageType pageType) {
        List<Website> resultList = new ArrayList<>();
        String sql = "select website,apply from spider_config where page_type=? and apply='Y'";
        Connection conn = JdbcUtil.getConnection();
        if (null != conn) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, pageType.toString());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    Website websiteTemp = Website.valueOfString(rs.getString("website"));
                    resultList.add(websiteTemp);
                }
            } catch (SQLException e) {
                logger.error(" query db error !" + e.getMessage());
                throw new RuntimeException(e);
            } catch (WebSiteException e) {
                throw new IllegalArgumentException(e);
            } finally {
                JdbcUtil.releaseConnections(rs, stmt, conn);
            }
            return resultList;
        } else {
            logger.error(" Connection db error !");
            return resultList;
        }
    }

    @Override
    public SpiderConfig findByWebsite(Website website, PageType pageType) {
        if (null != website) {
            SpiderConfig spiderConfig = null;
            String sql = "select id, website,thread_num, time_out,  retry_times, interval_times, task_level, page_type, apply from spider_config where website=? and page_type=?";
            Connection conn = JdbcUtil.getConnection();
            if (null != conn) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, website.toString());
                    stmt.setString(2, pageType.toString());
                    rs = stmt.executeQuery();
                    while (rs.next() && rs.isFirst()) {
                        long rId = rs.getLong("id");
                        Website websiteTemp = Website.valueOfString(rs.getString("website"));
                        int threadNum = rs.getInt("thread_num");
                        int timeOut = rs.getInt("time_out");
                        int retryTimes = rs.getInt("retry_times");
                        int intervalTimes = rs.getInt("interval_times");
                        TaskLevel taskLevel = TaskLevel.valueOfString(rs.getString("task_level"));
                        boolean b = char2Boolean(rs.getString("apply"));
                        spiderConfig = new SpiderConfig(websiteTemp, threadNum, timeOut, retryTimes, intervalTimes, taskLevel, b);
                        spiderConfig.setId(rId);
                    }
                } catch (SQLException e) {
                    logger.error(" query db error !" + e.getMessage());
                    throw new RuntimeException(e);
                } catch (WebSiteException e) {
                    throw new IllegalArgumentException(e);
                } finally {
                    JdbcUtil.releaseConnections(rs, stmt, conn);
                }
                return spiderConfig;
            } else {
                logger.error(" Connection db error !");
                return null;
            }
        }
        return null;
    }

    @Override
    public List<SpiderConfig> findByPageType(PageType pageType, String apply) {
        List<SpiderConfig> resultList = new ArrayList<>();
        String sql = "select id, website,thread_num, time_out,  retry_times, interval_times, task_level, page_type, class_name, apply from spider_config where page_type=? and apply=?";
        Connection conn = JdbcUtil.getConnection();
        if (null != conn) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, pageType.toString());
                stmt.setString(2, apply);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    long rId = rs.getLong("id");
                    Website websiteTemp = Website.valueOfString(rs.getString("website"));
                    int threadNum = rs.getInt("thread_num");
                    int timeOut = rs.getInt("time_out");
                    int retryTimes = rs.getInt("retry_times");
                    int intervalTimes = rs.getInt("interval_times");
                    TaskLevel taskLevel = TaskLevel.valueOfString(rs.getString("task_level"));
                    String className = rs.getString("class_name");
                    boolean b = char2Boolean(rs.getString("apply"));
                    SpiderConfig spiderConfig = new SpiderConfig(rId, websiteTemp, threadNum, timeOut, retryTimes, intervalTimes, taskLevel, className, b);
                    resultList.add(spiderConfig);
                }
            } catch (SQLException e) {
                logger.error(" query db error !" + e.getMessage());
                throw new RuntimeException(e);
            } catch (WebSiteException e) {
                throw new IllegalArgumentException(e);
            } finally {
                JdbcUtil.releaseConnections(rs, stmt, conn);
            }
            return resultList;
        } else {
            logger.error(" Connection db error !");
            return resultList;
        }
    }

    private boolean char2Boolean(String chars) {
        if (StringUtils.isEmpty(chars)) {
            return true;
        }
        if ("Y".equals(chars.toUpperCase())) {
            return true;
        } else {
            return false;
        }
    }
}

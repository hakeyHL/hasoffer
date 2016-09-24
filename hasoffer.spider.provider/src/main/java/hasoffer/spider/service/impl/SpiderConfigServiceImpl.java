package hasoffer.spider.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.exception.WebSiteException;
import hasoffer.base.model.Website;
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

@Component("spiderConfigService")
public class SpiderConfigServiceImpl implements ISpiderConfigService {

    private final Logger logger = LoggerFactory.getLogger(ISpiderConfigService.class);


    @Override
    public SpiderConfig findByWebsite(Website website, PageType pageType) {
        if (null != website) {
            SpiderConfig spiderConfig = null;
            String sql = "select id, website,thread_num, time_out,  retry_times, interval_times,task_level from spider_config where website=? and page_type=?";
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
                        spiderConfig = new SpiderConfig(websiteTemp, threadNum, timeOut, retryTimes, intervalTimes, taskLevel);
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

}

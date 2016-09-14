package hasoffer.spider.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class JdbcUtil {
    private static final Logger logger = LoggerFactory.getLogger(JdbcUtil.class);
    private static String driverClassName = null;
    private static String url = null;
    private static String username = null;
    private static String password = null;
    private static Properties props = new Properties();

    static {
        try {
            props.load(JdbcUtil.class.getResourceAsStream("/db/jdbc.properties"));
        } catch (IOException e) {
            logger.error("#ERROR# :系统加载jdbc.properties配置文件异常，请检查！", e);
            throw new RuntimeException(e);
        }
        driverClassName = (props.getProperty("hasoffer.jdbc.driverClassName"));
        url = (props.getProperty("hasoffer.jdbc.url"));
        username = (props.getProperty("hasoffer.jdbc.username"));
        password = (props.getProperty("hasoffer.jdbc.password"));

        //注册驱动类
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            logger.error("#ERROR# :加载数据库驱动异常，请检查！", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个数据库连接
     *
     * @return 一个数据库连接
     */
    public static Connection getConnection() {
        Connection conn = null;
        //创建数据库连接
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            logger.error("#ERROR# :创建数据库连接发生异常，请检查！", e);
            throw new RuntimeException(e);
        }
        return conn;
    }

    /**
     * 在一个数据库连接上执行一个静态SQL语句查询
     *
     * @param conn            数据库连接
     * @param staticSql 静态SQL语句字符串
     * @return 返回查询结果集ResultSet对象
     */
    public static ResultSet executeQuery(Connection conn, String staticSql) {
        ResultSet rs = null;
        try {
            //创建执行SQL的对象
            Statement stmt = conn.createStatement();
            //执行SQL，并获取返回结果
            rs = stmt.executeQuery(staticSql);
        } catch (SQLException e) {
            logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + staticSql, e);
            throw new RuntimeException(e);
        }
        return rs;
    }

    /* public static void executeSQL(Connection conn, String staticSql, Map<String, Object> params) {
        try {
            //创建执行SQL的对象
            PreparedStatement stmt = conn.prepareStatement(staticSql);
            //执行SQL，并获取返回结果
            stmt.execute(staticSql);
        } catch (SQLException e) {
            logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + staticSql, e);
        }
    }*/

    /**
     * 在一个数据库连接上执行一个静态SQL语句
     *
     * @param conn            数据库连接
     * @param staticSql 静态SQL语句字符串
     */
    public static void executeSQL(Connection conn, String staticSql) {
        try {
            //创建执行SQL的对象
            Statement stmt = conn.createStatement();
            //执行SQL，并获取返回结果
            stmt.execute(staticSql);
        } catch (SQLException e) {
            logger.error("#ERROR# :执行SQL语句出错，请检查！\n" + staticSql, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 在一个数据库连接上执行一批静态SQL语句
     *
     * @param conn        数据库连接
     * @param sqlList 静态SQL语句字符串集合
     */
    public static void executeBatchSQL(Connection conn, List<String> sqlList) {
        try {
            //创建执行SQL的对象
            Statement stmt = conn.createStatement();
            for (String sql : sqlList) {
                stmt.addBatch(sql);
            }
            //执行SQL，并获取返回结果
            stmt.executeBatch();
        } catch (SQLException e) {
            logger.error("#ERROR# :执行批量SQL语句出错，请检查！", e);
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn == null)
            return;
        try {
            if (!conn.isClosed()) {
                //关闭数据库连接
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("#ERROR# :关闭数据库连接发生异常，请检查！", e);
            throw new RuntimeException(e);
        }
    }

    public static void releaseConnections(ResultSet rs, Statement stmt, Connection conn) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("#ERROR# :关闭ResultSet发生异常，请检查！", e);
                throw new RuntimeException(e);
            }
        }

        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("#ERROR# :关闭Statement发生异常，请检查！", e);
                throw new RuntimeException(e);
            }
        }

        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("#ERROR# :关闭Connection发生异常，请检查！", e);
                throw new RuntimeException(e);
            }
        }
    }
}

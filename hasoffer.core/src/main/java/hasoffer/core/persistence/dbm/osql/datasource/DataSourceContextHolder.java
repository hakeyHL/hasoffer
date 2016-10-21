package hasoffer.core.persistence.dbm.osql.datasource;

/**
 * Created by chevy on 2016/10/19.
 */
public class DataSourceContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    /**
     * Description: 获取数据源类型
     */
    public static String getDataSourceType() {
        return contextHolder.get();
    }

    /**
     * Description: 设置数据源类型
     */
    public static void setDataSourceType(String dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    /**
     * Description: 清除数据源类型
     */
    public static void clearDataSourceType() {
        contextHolder.remove();
    }
}
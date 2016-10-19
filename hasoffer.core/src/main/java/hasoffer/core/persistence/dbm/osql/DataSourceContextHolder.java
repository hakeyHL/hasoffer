package hasoffer.core.persistence.dbm.osql;

/**
 * Created by chevy on 2016/10/19.
 */
public class DataSourceContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    /**
     * @param
     * @return String
     * @throws
     * @Description: 获取数据源类型
     */
    public static String getDataSourceType() {
        return contextHolder.get();
    }

    /**
     * @param dataSourceType 数据库类型
     * @return void
     * @throws
     * @Description: 设置数据源类型
     */
    public static void setDataSourceType(String dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    /**
     * @param
     * @return void
     * @throws
     * @Description: 清除数据源类型
     */
    public static void clearDataSourceType() {
        contextHolder.remove();
    }
}
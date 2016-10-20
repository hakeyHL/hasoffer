package hasoffer.core.persistence.dbm.osql.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * Created by chevy on 2016/10/19.
 */

public class DataSourceAspect implements MethodBeforeAdvice, AfterReturningAdvice {

    private Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

    @Override
    public void afterReturning(Object returnValue, Method method,
                               Object[] args, Object target) throws Throwable {
        DataSource ds = method.getAnnotation(DataSource.class);

        if (ds == null) {
            return;
        }

        DataSourceContextHolder.clearDataSourceType();
    }

    @Override
    public void before(Method method, Object[] args, Object target)
            throws Throwable {

        DataSource ds = method.getAnnotation(DataSource.class);

        if (ds == null) {
            return;
        }

        logger.error(String.format("method : %s/%s, datasource : %s", method.getDeclaringClass().getName(), method.getName(), ds.value()));

        if (ds.value() == DataSourceType.Slave) {
            DataSourceContextHolder.setDataSourceType("slave");
        } else {
            DataSourceContextHolder.setDataSourceType("master");
        }
    }

}

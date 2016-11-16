package hasoffer.core.persistence.dbm.osql;

import hasoffer.base.config.AppConfig;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.BeanUtil;
import hasoffer.core.persistence.dbm.osql.exception.OSqlException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by glx on 2015/5/20.
 */

public abstract class AbstractHibernate4DataBaseManager implements IDataBaseManager {
    private static final Pattern PLACE_HOLDER = Pattern.compile("\\?");
    private static final Pattern ORDER = Pattern.compile(" order +by +.*$", Pattern.CASE_INSENSITIVE);
    private static Logger logger = LoggerFactory.getLogger(AbstractHibernate4DataBaseManager.class);
    private static Pattern ORDER_BY = Pattern.compile("\\s+order\\s+by\\s+");

    private static String getCountSql(String sql) {
        String countSql =
                "select count(*) " + sql.substring(StringUtils.indexOfIgnoreCase(sql, "from"));

        int index = -1;
        String temp = countSql.toLowerCase().replace('\t', ' ');
        Matcher matcher = ORDER_BY.matcher(temp);
        if (matcher != null && matcher.find()) {
            index = matcher.start();
        }

        if (index > -1) {
            countSql = countSql.substring(0, index);
        }

        return countSql;
    }

    public <ID extends Serializable, T extends Identifiable<ID>> T get(Class<T> tClass, ID id) {
        return this.getHibernate4Template().get(tClass, id);
    }

    public <ID extends Serializable, T extends Identifiable<ID>> void createIfNoExist(final T t) {
        if (t.getId() == null) {
            throw new OSqlException("must set id");
        }

        getHibernate4Template().execute(new HibernateCallback<Object>() {
            public Object doInHibernate(Session session) throws HibernateException {
                Map<String, Object> poMap = objectToMap(t);
                String tableName = t.getClass().getSimpleName().toLowerCase();

                StringBuffer columns = new StringBuffer("(");
                StringBuffer values = new StringBuffer("");

                String where = " where not exists (select * from " + tableName + " where id=" + getValue(t.getId()) + ")";

                int i = 0;
                for (Map.Entry<String, Object> entry : poMap.entrySet()) {
                    columns.append("`" + entry.getKey() + "`");
                    values.append(getValue(entry.getValue()));
                    if (i != poMap.entrySet().size() - 1) {
                        columns.append(",");
                        values.append(",");
                    }
                    i++;
                }

                columns.append(")");
                values.append("");

                String sql = "insert into " + tableName + columns + "select " + values + " from DUAL" + where + ";";
//                logger.debug(sql);
                Query query = session.createSQLQuery(sql);
                query.executeUpdate();
                return null;
            }
        });
    }

    private String getValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value.equals(true)) {
            return "1";
        }

        if (value.equals(false)) {
            return "0";
        }

        if (value instanceof Character) {
            return "'" + value + "'";
        }

        if (value instanceof BigDecimal) {
            return "'" + ((BigDecimal) value).toPlainString() + "'";
        }

        if (value instanceof Date) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value) + "'";
        }

        if (value instanceof java.sql.Date) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.sql.Date) value) + "'";
        }

        if (value instanceof Calendar) {
            Calendar calendar = (Calendar) value;
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((calendar.getTime())) + "'";
        }


        if (value instanceof java.sql.Timestamp) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.sql.Timestamp) value) + "'";
        }

        return "'" + value.toString() + "'";
    }

    private Map<String, Object> objectToMap(Object obj) {
        try {
            return BeanUtil.objectToMap(obj);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return new HashMap<String, Object>();
    }

    public <ID extends Serializable, T extends Identifiable<ID>> ID create(T t) {
        Serializable id = getHibernate4Template().save(t);
        if (id == null) {
            return t.getId();
        } else {
            t.setId((ID) id);
            return (ID) id;
        }
    }

    @Override
    public <T> int batchSave(final List<T> array) {
        return (Integer) getHibernate4Template().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException {
//                        Transaction tx = session.beginTransaction();
                        for (int i = 0; i < array.size(); i++) {
                            session.save(array.get(i));
                            //强制提交
                            if ((i + 1) % AppConfig.BATCH_MAX_ROW == 0) {
                                session.flush();
//                                session.clear();
                            }
                        }
//                        tx.commit();
                        session.close();
                        return array.size();
                    }
                }
        );
    }

    @Override
    public int batchDelete(final String jpaSql, final String[] ids) {
        return (Integer) getHibernate4Template().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(jpaSql);
                query.setParameterList("ids", ids);
                int i = query.executeUpdate();
                session.flush();
//                session.clear();
                session.close();
                return i;
            }
        });
    }

    public <T> T querySingle(String jpaSql) {
        return this.querySingle(jpaSql, null);
    }

    public <T> T querySingle(final String jpaSql, final List params) {
        return this.getHibernate4Template().execute(new HibernateCallback<T>() {
            public T doInHibernate(org.hibernate.Session session) throws HibernateException {
                Query query = session.createQuery(jpaSql);
                if (params != null) {
                    int i = 0;
                    for (Object obj : params) {
                        query.setParameter("" + i, obj);
                        i++;
                    }
                }

                return (T) query.uniqueResult();
            }
        });
    }

    public <T> List<T> query(String jpaSql) {
        return this.query(jpaSql, null);
    }

    public <T> List<T> query(final String jpaSql, final List params) {
        return (List<T>) this.query(jpaSql, 1, Integer.MAX_VALUE, params);
    }

    public <T> List<T> query(String jpaSql, int pageNumber, int pageSize) {
        return this.query(jpaSql, pageNumber, pageSize, null);
    }

    public <T> List<T> query(final String jpaSql, final int pageNumber, final int pageSize, final List params) {
        return this.getHibernate4Template().execute(new HibernateCallback<List<T>>() {
            public List<T> doInHibernate(org.hibernate.Session session) throws HibernateException {
                Query query = session.createQuery(jpaSql);
                query.setFirstResult(pageNumber * pageSize - pageSize);
                query.setMaxResults(pageSize);
                if (params != null) {
                    int i = 0;
                    for (Object obj : params) {
                        query.setParameter("" + i, obj);
                        i++;
                    }
                }

                return query.list();
            }
        });
    }

    public <T> PageableResult<T> queryPage(String jpaSql, int pageNumber, int pageSize) {
        List<T> results = this.query(jpaSql, pageNumber, pageSize);

//        String countSql = "select count(*) " + jpaSql.substring(StringUtils.indexOfIgnoreCase(jpaSql, "from"));
        String countSql = getCountSql(jpaSql);

        long count = this.querySingle(countSql);
        return new PageableResult<T>(results, count, pageNumber, pageSize);
    }

    public <T> PageableResult<T> queryPage(String jpaSql, int pageNumber, int pageSize, List params) {
        List<T> results = this.query(jpaSql, pageNumber, pageSize, params);

//        String countSql = "select count(*) " + jpaSql.substring(StringUtils.indexOfIgnoreCase(jpaSql, "from"));
        String countSql = getCountSql(jpaSql);

        long count = this.querySingle(countSql, params);
        return new PageableResult<T>(results, count, pageNumber, pageSize);
    }

    public <ID extends Serializable, T extends Identifiable<ID>> void update(final Updater<ID, T> updater) {
        final Map<String, Object> params = updater.getParameter();
        if (params == null || params.isEmpty()) {
            return;
        }
        StringBuffer set = new StringBuffer();
        final List<Object> listParams = new LinkedList<Object>();
        int position = 0;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            listParams.add(entry.getValue());

            if (position == params.entrySet().size() - 1) {
                set.append(" t." + entry.getKey() + "=?" + (position));
            } else {
                set.append(" t." + entry.getKey() + "=?" + (position) + ",");
            }

            position++;
        }

        listParams.add(updater.getId());
        final String finalSet = set.toString();
        final int finalPosition = position;
        this.getHibernate4Template().execute(new HibernateCallback<Void>() {
            public Void doInHibernate(org.hibernate.Session session) throws HibernateException {
                Query query = session.createQuery(
                        "update " + updater.getPoClass().getName() + " t set" + finalSet + " where t.id=?" + (finalPosition));
                int i = 0;
                for (Object obj : listParams) {
                    query.setParameter("" + i, obj);
                    i++;
                }
                logger.debug(query.toString());
                query.executeUpdate();

                return null;
            }
        });

        this.getHibernate4Template().clear();
    }

    public <ID extends Serializable, T extends Identifiable<ID>> void delete(Class<T> tClass, ID id) {
        this.getHibernate4Template().delete(this.get(tClass, id));
    }

    @Override
    public List queryByIds(final String jpaSql, final String[] ids) {
        return (List) getHibernate4Template().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(jpaSql);
                query.setParameterList("ids", ids);
                return query.list();
            }
        });
    }

    @Override
    public List queryBySql(final String queryString, final Map<String, Object> paramsMap) {
        logger.info("queryBySql sql == " + queryString);
        logger.info("paramsMap = " + ReflectionToStringBuilder.toString(paramsMap, ToStringStyle.MULTI_LINE_STYLE));
        return (List) this.getHibernate4Template().execute(new HibernateCallback() {
            public Object doInHibernate(final Session session) throws HibernateException {
                Query query = session.createSQLQuery(queryString).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
                if (paramsMap != null) {
                    for (String key : paramsMap.keySet()) {
                        Object obj = paramsMap.get(key);
                        if (obj instanceof Collection<?>) {
                            query.setParameterList(key, (Collection<?>) obj);
                        } else if (obj instanceof Object[]) {
                            query.setParameterList(key, (Object[]) obj);
                        } else {
                            query.setParameter(key, obj);
                        }
                    }
                }
                return query.list();
            }
        });
    }

    protected abstract HibernateTemplate getHibernate4Template();

    @Override
    public void update(final List<T> array) {
        getHibernate4Template().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException {
                        session.update(array.get(0));
                        session.flush();
//                        session.clear();
                        // why close? zwd.
                        session.close();
                        return null;
                    }
                });
    }

    @Override
    public void update(final Object t) {
        getHibernate4Template().getSessionFactory().getCurrentSession().update(t);
    }

    @Override
    public void saveOrUpdate(Object it) {
        getHibernate4Template().getSessionFactory().getCurrentSession().saveOrUpdate(it);
    }

    @Override
    public void deleteBySQL(final String sql) {
        exeSQL(sql);
    }

    @Override
    public Integer deleteBySql(final String sql, final Object... values) {

        return getHibernate4Template().execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query q = buildQuery(session, true, sql, values);
                int result = q.executeUpdate();
                session.flush(); //清理缓存，执行批量插入
                session.clear();//清空缓存中的 对象
                return result;
            }
        });
    }

    @Override
    public void updateBySQL(final String sql) {
        exeSQL(sql);
    }

    @Override
    public void exeSQL(final String sql) {
        getHibernate4Template().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createQuery(sql);
                query.executeUpdate();
                session.flush();
                session.close();
                return null;
            }
        });
    }

    @Override
    public PageableResult<Map<String, Object>> findPageOfMapBySql(final String sql, final int page, final int pageSize, final Object... params) {
        int totalCount = countBySql(sql, params);
        List<Map<String, Object>> items = findListOfMapBySql(sql, page, pageSize, params);
        return new PageableResult<>(items, totalCount, page, pageSize);
    }

    @Override
    public List<Map<String, Object>> findListOfMapBySql(final String sql, final int page, final int pageSize, final Object... params) {
        return getHibernate4Template().execute(new HibernateCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInHibernate(Session session) throws HibernateException {
                Query query = buildQuery(session, true, sql, params).setFirstResult((page - 1) * pageSize).setMaxResults(pageSize);
                query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
                List<Map<String, Object>> items = query.list();
                session.flush();
                session.close();
                return items;
            }
        });
    }

    @Override
    public int countBySql(String sql, Object... params) {
        sql = ORDER.matcher(sql).replaceFirst("");
        sql = "Select Count(*) From (" + sql + ") t";
        Number nmb = this.findUniqueBySql(sql, params);
        return nmb.intValue();
    }

    @Override
    public <T> T findUniqueBySql(final String sql, final Object... parmas) {

        return getHibernate4Template().execute(new HibernateCallback<T>() {
            @Override
            public T doInHibernate(Session session) throws HibernateException {
                Query query = buildQuery(session, true, sql, parmas);
                T ret = (T) query.uniqueResult();
                session.flush();
                session.close();
                return ret;
            }
        });
    }

    private Query buildQuery(Session session, boolean nativeSql, String ql, Map<String, Object> values) {
        Query query = null;
        if (nativeSql) {
            query = session.createSQLQuery(ql);
        } else {
            query = session.createQuery(ql);
        }

        for (String name : values.keySet()) {
            setParameter(query, name, values.get(name));
        }
        return query;
    }

    private Query buildQuery(Session session, boolean isSql, String ql, Object[] values) {
        int paramCnt = 0;
        if (values.length > 0) {// replace ? to :param{i}
            Matcher m = PLACE_HOLDER.matcher(ql);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, ":param" + paramCnt);
                paramCnt++;
            }
            m.appendTail(sb);
            if (paramCnt != values.length) {
                throw new HibernateException("Params' count is: " + paramCnt + " but expected to be: " + values.length);
            }
            ql = sb.toString();
        }
        Query query;
        if (isSql) {
            query = session.createSQLQuery(ql);
        } else {
            query = session.createQuery(ql);
        }

        for (int i = 0; i < values.length; i++) {
            setParameter(query, "param" + i, values[i]);
        }
        return query;
    }

    private void setParameter(Query query, String name, Object value) {
        if (value instanceof Collection) {
            query.setParameterList(name, (Collection<?>) value);
        } else if (value instanceof Object[]) {
            query.setParameterList(name, (Object[]) value);
        } else {
            query.setParameter(name, value);
        }
    }
}

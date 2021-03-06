package hasoffer.core.persistence.dbm.osql;

import hasoffer.base.model.PageableResult;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by glx on 2015/5/20.
 */
public interface IDataBaseManager {

    <ID extends Serializable, T extends Identifiable<ID>> T get(Class<T> tClass, ID id);

    <ID extends Serializable, T extends Identifiable<ID>> ID create(T t);

    <T> int batchSave(final List<T> array);

    <ID extends Serializable, T extends Identifiable<ID>> void createIfNoExist(final T t);

    int batchDelete(String jpaSql, String[] ids);

    <T> T querySingle(String jpaSql);

    <T> T querySingle(String jpaSql, List params);

    <T> List<T> query(String jpaSql);

    <T> List<T> query(String jpaSql, List params);

    <T> List<T> query(String jpaSql, int pageNumber, int pageSize);

    <T> List<T> query(String jpaSql, int pageNumber, int pageSize, List params);

    <T> PageableResult<T> queryPage(String jpaSql, int pageNumber, int pageSize);

    <T> PageableResult<T> queryPage(String jpaSql, int pageNumber, int pageSize, List params);

    <ID extends Serializable, T extends Identifiable<ID>> void update(Updater<ID, T> updater);

    <ID extends Serializable, T extends Identifiable<ID>> void delete(Class<T> tClass, ID id);

    List queryByIds(String jpaSql, String[] ids);

    List queryBySql(final String queryString, final Map<String, Object> paramsMap);

    void update(final List<T> array);

    void update(Object t);

    void saveOrUpdate(Object it);

    void deleteBySQL(final String sql);

    Integer deleteBySql(final String sql, final Object... values);

    void updateBySQL(final String sql);

    void exeSQL(final String sql);

    PageableResult<Map<String, Object>> findPageOfMapBySql(String sql, int page, int pageSize, Object... values);

    List<Map<String, Object>> findListOfMapBySql(String sql, int page, int pageSize, Object... params);

    int countBySql(String sql, Object... params);

    <T> T findUniqueBySql(String sql, Object... params);
}

package hasoffer.core.app;

import hasoffer.core.app.vo.mobile.KeyWordsVo;

import java.util.List;

/**
 * Created by hs on 2017年01月05日.
 * Time 17:18
 */
public interface MobileService {
    public List<KeyWordsVo> getKeyWordsListFromRepo(KeyWordsVo keyWordsVo, int page, int pageSize);
}

package hasoffer.core.admin;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.app.AppDeal;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


/**
 * Created by lihongde on 2016/6/21 14:02
 */
public interface IDealService {

    public PageableResult<AppDeal> findDealList(int page, int size);

    public Map<String, Object> importExcelFile(MultipartFile multipartFile, String realPath) throws Exception;
}

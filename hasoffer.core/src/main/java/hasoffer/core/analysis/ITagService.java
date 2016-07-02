package hasoffer.core.analysis;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.match.TagBrand;
import hasoffer.core.persistence.po.match.TagModel;

import java.util.List;

/**
 * Created by chevy on 2016/7/1.
 */
public interface ITagService {

    TagBrand createTagBrand(String brand);

    TagModel createTagModel(String model);

    PageableResult<TagBrand> getPagedBrandTags(int page, int size);

    List<TagBrand> listBrandTags(int page, int size);

    PageableResult<TagModel> getPagedModelTags(int page, int size);

    List<TagModel> listModelTags(int page, int size);

    void buildWordDicts();

    void loadWordDicts();
}

package hasoffer.core.analysis;

import hasoffer.core.persistence.po.match.TagBrand;
import hasoffer.core.persistence.po.match.TagModel;

/**
 * Created by chevy on 2016/7/1.
 */
public interface ITagService {

    TagBrand createTagBrand(String brand);

    TagModel createTagModel(String model);
}

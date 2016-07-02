package hasoffer.core.analysis.impl;

import hasoffer.core.analysis.ITagService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.match.TagBrand;
import hasoffer.core.persistence.po.match.TagModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created on 2016/7/1.
 */
@Service
public class TagServiceImpl implements ITagService {

    @Resource
    IDataBaseManager dbm;

    @Override
    @Transactional
    public TagBrand createTagBrand(String brand) {
        TagBrand tagBrand = new TagBrand(brand, "", 0);
        dbm.create(tagBrand);
        return tagBrand;
    }

    @Override
    @Transactional
    public TagModel createTagModel(String model) {
        TagModel tagModel = new TagModel(model, "", 0);
        dbm.create(tagModel);
        return tagModel;
    }
}

package hasoffer.core.analysis.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.analysis.ITagService;
import hasoffer.core.analysis.LingHelper;
import hasoffer.core.bo.match.TagType;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.match.TagBrand;
import hasoffer.core.persistence.po.match.TagModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created on 2016/7/1.
 */
@Service
public class TagServiceImpl implements ITagService {
    private static String Q_TAG_BRAND = "select t from TagBrand t";
    private static String Q_TAG_MODEL = "select t from TagModel t";
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    @Override
    public void loadWordDicts() {
        // todo 从缓存、序列化中查询词库是否存在，如果存在则直接加载
        // 如果不存在，则读取数据库进行创建
        buildWordDicts();
    }

    @Override
    public void buildWordDicts() {
        // 清除词典内容
        logger.info("clear dictionary...");
        LingHelper.clearDict();

        final int START_PAGE = 1, PAGE_SIZE = 1000;

        long total_page = 0;
        int page = 1;

        // build brand dict
        logger.info("build brand dict...");
        PageableResult<TagBrand> pagedTagBrands = getPagedBrandTags(START_PAGE, PAGE_SIZE);
        total_page = pagedTagBrands.getTotalPage();
        page = 1;
        List<TagBrand> tagBrands = pagedTagBrands.getData();
        while (page <= total_page) {
            if (page > 1) {
                tagBrands = listBrandTags(page, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(tagBrands)) {
                for (TagBrand tagBrand : tagBrands) {
                    // todo 处理别名情况
                    LingHelper.addToDict(tagBrand.getTag(), TagType.BRAND, tagBrand.getScore());
                }
            }

            page++;
        }
        logger.info("build brand dict..OK.");

        logger.info("build model dict...");
        // build model dict
        PageableResult<TagModel> pagedTagModels = getPagedModelTags(START_PAGE, PAGE_SIZE);
        total_page = pagedTagModels.getTotalPage();
        page = 1;
        List<TagModel> tagModels = pagedTagModels.getData();
        while (page <= total_page) {
            if (page > 1) {
                tagModels = listModelTags(page, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(tagModels)) {
                for (TagModel tagModel : tagModels) {
                    // todo 处理别名情况
                    LingHelper.addToDict(tagModel.getTag(), TagType.BRAND, tagModel.getScore());
                }
            }

            page++;
        }

        logger.info("build model dict..OK.");

        int dictSize = LingHelper.getDictSize();
        logger.info("dict size = " + dictSize);
    }

    @Override
    public List<TagBrand> listBrandTags(int page, int size) {
        return dbm.query(Q_TAG_BRAND, page, size);
    }

    @Override
    public List<TagModel> listModelTags(int page, int size) {
        return dbm.query(Q_TAG_MODEL, page, size);
    }

    @Override
    public PageableResult<TagBrand> getPagedBrandTags(int page, int size) {
        return dbm.queryPage(Q_TAG_BRAND, page, size);
    }

    @Override
    public PageableResult<TagModel> getPagedModelTags(int page, int size) {
        return dbm.queryPage(Q_TAG_MODEL, page, size);
    }

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

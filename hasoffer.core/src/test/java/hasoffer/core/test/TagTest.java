package hasoffer.core.test;

import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.match.HasTag;
import hasoffer.core.bo.match.ITag;
import hasoffer.core.bo.match.SkuValType;
import hasoffer.core.bo.match.TitleStruct;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.match.TagBrand;
import hasoffer.core.persistence.po.match.TagCategory;
import hasoffer.core.persistence.po.match.TagSkuVal;
import jodd.io.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/6/15
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class TagTest {

    @Resource
    IDataBaseManager dbm;

    @Test
    public void test() {
        List<TagCategory> tagCategories = dbm.query("select t from TagCategory t");
        List<TagSkuVal> tagSkuVals = dbm.query("select t from TagSkuVal t");
        List<TagBrand> tagBrands = dbm.query("select t from TagBrand t");

        Map<String, TagCategory> cateTagMap = new HashMap<String, TagCategory>();
        Map<String, TagSkuVal> skuValTagMap = new HashMap<String, TagSkuVal>();
        Map<String, TagBrand> brandTagMap = new HashMap<String, TagBrand>();

        fill(cateTagMap, tagCategories);
        fill(skuValTagMap, tagSkuVals);
        fill(brandTagMap, tagBrands);

        System.out.println(tagCategories.size() + "\t" + cateTagMap.size());
        System.out.println(tagSkuVals.size() + "\t" + skuValTagMap.size());
        System.out.println(tagBrands.size() + "\t" + brandTagMap.size());

        String title = "BQ S40- Black & Grey 5 Inch HD IPS Screen, 1.3 Ghz Quad Core, Android Kitkat, 1GB RAM, 3G Mobile Phone";

        TitleStruct ts = new TitleStruct(title);

        getStructInfo(ts, cateTagMap, skuValTagMap, brandTagMap);

        System.out.println(ts.getTitle());
    }

    private void getStructInfo(TitleStruct ts,
                               Map<String, TagCategory> cateTagMap,
                               Map<String, TagSkuVal> skuValTagMap,
                               Map<String, TagBrand> brandTagMap) {

        String title = ts.getTitle().toLowerCase();

        for (Map.Entry<String, TagCategory> kv : cateTagMap.entrySet()) {
            if (title.contains(kv.getKey())) {
                ts.getCateTag().add(kv.getKey());
            }
        }

        for (Map.Entry<String, TagSkuVal> kv : skuValTagMap.entrySet()) {
            if (title.contains(kv.getKey())) {
                if (kv.getValue().getSkuValType() == SkuValType.COLOR) {
                    ts.getColorTag().add(kv.getKey());
                } else {
                    ts.getSizeTag().add(kv.getKey());
                }
            }
        }

        for (Map.Entry<String, TagBrand> kv : brandTagMap.entrySet()) {
            if (title.contains(kv.getKey())) {
                ts.getBrandTag().add(kv.getKey());
            }
        }
    }

    private void fill(Map tagMap, List tags) {

        for (Object o : tags) {

            ITag iTag = (ITag) o;

            tagMap.put(iTag.getTag(), o);

            if (!StringUtils.isEmpty(iTag.getAlias())) {
                String[] alias = iTag.getAlias().split(",");
                for (String ali : alias) {
                    tagMap.put(ali, o);
                }
            }
        }

    }

    @Test
    public void importSkuValTags() throws Exception {
        String filePath = "d:/TMP/tags/skuattrtag.txt";

        String[] lines = FileUtil.readLines(new File(filePath));

        List<TagSkuVal> tags = new ArrayList<TagSkuVal>();

        for (String line : lines) {
            HasTag tag = cleanBySplit(line);

            tags.add(new TagSkuVal(tag.getTag(), tag.getAlias(), 4));
        }

        dbm.batchSave(tags);
    }

    @Test
    public void importBrandTags() throws Exception {
        String filePath = "d:/TMP/tags/brand.txt";

        String[] lines = FileUtil.readLines(new File(filePath));

        List<TagBrand> tags = new ArrayList<TagBrand>();

        for (String line : lines) {
            HasTag tag = cleanBySplit(line);

            tags.add(new TagBrand(tag.getTag(), tag.getAlias(), 4));
        }

        dbm.batchSave(tags);
    }

    @Test
    public void importCategoryTags() throws Exception {
        String filePath = "d:/TMP/tags/catetag.txt";

        String[] lines = FileUtil.readLines(new File(filePath));

        List<TagCategory> tags = new ArrayList<TagCategory>();

        for (String line : lines) {
            HasTag tag = cleanBySplit(line);

            tags.add(new TagCategory(tag.getTag(), tag.getAlias(), 10));
        }

        dbm.batchSave(tags);
    }

    private HasTag cleanBySplit(String line) {

        String[] tags = line.split(",");

        String keyword = tags[0].trim().toLowerCase();

        HasTag ht = new HasTag();
        ht.setTag(keyword);

        int len = tags.length;
        if (len > 1) {
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i < len; i++) {
                String tag = tags[i].trim().toLowerCase();
                if (!StringUtils.isEmpty(tag)) {
                    sb.append(tag).append(",");
                }
            }

            if (sb.length() > 0) {
                if (sb.charAt(sb.length() - 1) == ',') {
                    sb.delete(sb.lastIndexOf(","), sb.length());
                }

                ht.setAlias(sb.toString());
            }
        }

        return ht;
    }

}

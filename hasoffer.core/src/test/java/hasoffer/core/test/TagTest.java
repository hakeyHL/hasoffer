package hasoffer.core.test;

import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.match.HasTag;
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
import java.util.List;

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

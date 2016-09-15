package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.match.TagBrand;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.product.solr.ProductIndex2ServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chevy on 2015/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ProductSearchTest {


    @Resource
    ProductIndex2ServiceImpl productIndex2Service;
    @Resource
    IDataBaseManager dbm;

    @Test
    public void importTags() {

        Set<String> tagSet = new HashSet<>();

        long count = 0;

        final String Q_TagBrand = "select t from TagBrand t";
        final String Q_PtmCategory = "select t from PtmCategory t";
        // TagBrand - tag
        List<TagBrand> brands = dbm.query(Q_TagBrand);
        for (TagBrand brand : brands) {
            add2set(tagSet, brand.getTag());
            count++;
        }

        // PtmCategory - name,keyword
        List<PtmCategory> cates = dbm.query(Q_PtmCategory);
        for (PtmCategory cate : cates) {
            add2set(tagSet, cate.getName());
            count++;
        }

        StringBuilder sb = new StringBuilder();

        for (String tag : tagSet) {
            sb.append(tag).append("\n");
        }

        print(tagSet.size() + ":" + count);

        try {
            File file = hasoffer.base.utils.FileUtils.createFile("d:/tmp/spelling.txt", true);
            FileUtils.writeStringToFile(file, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // PtmProduct - brand,model,tag
    }

    private void add2set(Set<String> tagSet, String tag) {
        if (StringUtils.isEmpty(tag) || tag.length() <= 1 || StringUtils.isEmpty(tag.trim())) {
            return;
        }

        tag = tag.trim();

        String[] ts = tag.split("\\s");

        if (ts.length > 1) {
            for (String t : ts) {
                add2set(tagSet, t);
            }
        } else {
            tagSet.add(tag);
        }

        tagSet.add(tag);
    }

    @Test
    public void testSpellCheck() {
        String brand = "aple";
        Map<String, List<String>> strs = productIndex2Service.spellCheck(brand);
        for (Map.Entry<String, List<String>> str : strs.entrySet()) {
            System.out.println("input : " + str.getKey() + "\nout : ");
            for (String s : str.getValue()) {
                System.out.print(s + "\t");
            }
            System.out.println("\t");
        }
    }

    @Test
    public void testSearchKeyword() {
        String keyword = "iphone 6s";

        SearchCriteria sc = new SearchCriteria();
        sc.setKeyword(keyword);

        PageableResult<ProductModel2> productModels = productIndex2Service.searchProducts(sc);

        List<ProductModel2> pms = productModels.getData();

        for (ProductModel2 pm : pms) {
            print(pm.getId() + "\t" + pm.getTitle());
        }
    }

    private void print(String str) {
        System.out.println(str);
    }
}

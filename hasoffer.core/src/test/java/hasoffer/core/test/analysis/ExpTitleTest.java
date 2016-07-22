package hasoffer.core.test.analysis;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.IProductService;
import jodd.io.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by chevy on 2016/7/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ExpTitleTest {

    private static final String Q_CATE = "SELECT t FROM PtmCategory t WHERE t.level <= 3 ORDER BY t.level";

    private static final String Q_PRODUCT = "SELECT COUNT(t.id) FROM PtmProduct t WHERE t.categoryId=?0";

    final Map<Long, PtmCategory> cateMap1 = new HashMap<Long, PtmCategory>();
    final Map<Long, PtmCategory> cateMap2 = new HashMap<Long, PtmCategory>();
    final Map<Long, PtmCategory> cateMap3 = new HashMap<Long, PtmCategory>();
    final List<PtmCategory> cates = new ArrayList<PtmCategory>();
    final List<Long> cateIds = new ArrayList<Long>();

    @Resource
    IDataBaseManager dbm;
    @Resource
    IProductService productService;

    @Test
    public void getI2() {
        initCateMap();

        String fileDir = "E:/data-match/exp/";

        File file1;
        try {
            file1 = createFile(fileDir + "nocate_titles", true);
        } catch (Exception e) {
            System.out.println("error in create file");
            return;
        }

        int page = 1, PAGE_SIZE = 2000;

        PageableResult<PtmProduct> pagedPros = productService.listPagedProducts(page, PAGE_SIZE);
        List<PtmProduct> products = pagedPros.getData();
        long totalPage = pagedPros.getTotalPage();

        while (page <= totalPage) {
            if (page > 1) {
                pagedPros = productService.listPagedProducts(page, PAGE_SIZE);
//                break;
            }

            products = pagedPros.getData();

            if (ArrayUtils.hasObjs(products)) {
                for (PtmProduct o : products) {
                    if (StringUtils.isEmpty(o.getTitle()) || cateIds.contains(o.getCategoryId())) {
                        System.out.println(String.format("[%s].[%d]...CONTINUE...", o.getTitle(), o.getCategoryId()));
                        continue;
                    }

                    try {
                        FileUtil.appendString(file1, StringUtils.filterAndTrim(o.getTitle(), Arrays.asList("\n")) + "\n");
                    } catch (IOException e) {
                        System.out.println(String.format("error[IO ERROR] in exp to file[%s].[%d]", o.getTitle(), o.getCategoryId()));
                    }
                }
            }

            page++;
        }

        System.out.println("all finished.");
    }

    @Test
    public void f() {
        String t = "xxx \n xll \n";
        System.out.println(StringUtils.filterAndTrim(t, Arrays.asList("\n")));
    }

    @Test
    public void getI() {
        initCateMap();

        String fileDir = "E:/data-match/exp/";

        File file1 = null;
        try {
            file1 = createFile(fileDir + "file2_1", true);
        } catch (Exception e) {
            System.out.println("error in create file");
            return;
        }

        int len = cates.size();
        for (int i = 0; i < len; i++) {
            PtmCategory cate = cates.get(i);

            System.out.println(String.format("exp No.[%d] cate[%d] to files", i, cate.getId()));

            List<PtmProduct> products = productService.listProducts(cate.getId(), 1, Integer.MAX_VALUE);

            for (PtmProduct o : products) {
                if (StringUtils.isEmpty(o.getTitle())) {
                    return;
                }

                try {
                    FileUtil.appendString(file1, StringUtils.filterAndTrim(o.getCategoryId() + " " + o.getTitle(), Arrays.asList("\n")) + "\n");
                } catch (IOException e) {
                    System.out.println(String.format("error[IO ERROR] in exp to file[%s].[%d]", o.getTitle(), o.getCategoryId()));
                }
            }
        }

        System.out.println("all finished.");
    }

    private File createFile(String filePath, boolean delIfExists) throws IOException {
        File file1 = new File(filePath);
        if (file1.exists()) {
            if (delIfExists) {
                file1.delete();
            }
        }
        file1.createNewFile();
        return file1;
    }

    public void initCateMap() {
        List<PtmCategory> categories = dbm.query(Q_CATE);

        for (PtmCategory category : categories) {
            Map<Long, PtmCategory> cateMap = null;
            int level = category.getLevel();
            switch (level) {
                case 1:
                    cateMap = cateMap1;
                    break;
                case 2:
                    if (!cateMap1.containsKey(category.getParentId())) {
                        continue;
                    }
                    cateMap = cateMap2;
                    break;
                case 3:
                    if (!cateMap2.containsKey(category.getParentId())) {
                        continue;
                    }
                    cateMap = cateMap3;
                    break;
                default:
                    break;
            }
            if (cateMap != null) {
                if (category.getLevel() == 3) {
                    long count = dbm.querySingle(Q_PRODUCT, Arrays.asList(category.getId()));
                    if (count == 0) {
                        continue;
                    }
                }
                cateMap.put(category.getId(), category);
                cates.add(category);
                cateIds.add(category.getId());
            }
        }

        showMap(cateMap1);
        showMap(cateMap2);
        showMap(cateMap3);
    }

    private void showMap(Map<Long, PtmCategory> cateMap) {
        System.out.println("... show category map ...");
        for (Map.Entry<Long, PtmCategory> cate : cateMap.entrySet()) {
//            long count = dbm.querySingle(Q_PRODUCT, Arrays.asList(cate.getValue().getId()));
//            System.out.println(cate.getKey() + "\t" + cate.getValue().getName() + "\t" + count);
            System.out.println(cate.getKey() + "\t" + cate.getValue().getName());
        }
        System.out.println(".........................");
    }
}

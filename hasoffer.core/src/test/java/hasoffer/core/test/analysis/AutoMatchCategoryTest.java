package hasoffer.core.test.analysis;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.IProductService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import jodd.io.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chevy on 2016/7/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class AutoMatchCategoryTest {

    private final static String Q_CATE = "SELECT t FROM PtmCategory t WHERE t.level <= 3 ORDER BY t.level";

    private final static String Q_PRODUCT = "SELECT COUNT(t.id) FROM PtmProduct t WHERE t.categoryId=?0";
    final Map<Long, PtmCategory> cateMap1 = new HashMap<Long, PtmCategory>();
    final Map<Long, PtmCategory> cateMap2 = new HashMap<Long, PtmCategory>();
    final Map<Long, PtmCategory> cateMap3 = new HashMap<Long, PtmCategory>();
    final List<PtmCategory> cates = new ArrayList<PtmCategory>();
    @Resource
    IDataBaseManager dbm;
    @Resource
    IProductService productService;

    @Test
    public void getI() {
        initCateMap();

        File file1 = null, file2 = null;
        try {
            file1 = createFile("E:/data-match/exp/file3", true);
        } catch (Exception e) {
            System.out.println("error in create file");
            return;
        }
        final File f1 = file1;
        final File f2 = file2;

        final CurrentCategory cc = new CurrentCategory();

        IList list = new IList<PtmProduct>() {
            @Override
            public PageableResult<PtmProduct> getData(int page) {
                return productService.listPagedProducts(cc.getCateId(), page, 1000);
            }

            @Override
            public boolean isRunForever() {
                return false;
            }

            @Override
            public void setRunForever(boolean runForever) {

            }
        };

        ListAndProcessTask2<PtmProduct> listAndProcessTask2 = new ListAndProcessTask2<PtmProduct>(
                list,
                new IProcess<PtmProduct>() {
                    @Override
                    public void process(PtmProduct o) {
                        if (StringUtils.isEmpty(o.getTitle())) {
                            return;
                        }

                        try {
                            FileUtil.appendString(f1, o.getTitle() + "\n");
                            FileUtil.appendString(f2, o.getCategoryId() + "\n");
                        } catch (IOException e) {
                            System.out.println(String.format("error[IO ERROR] in exp to file[%s].[%d]", o.getTitle(), o.getCategoryId()));
                        } /*catch (NullPointerException e){
                            System.out.println(String.format("error[NULL] in exp to file[%s].[%d]", o.getTitle(), o.getCategoryId()));
                        }*/
                    }
                }
        );

        int count = 0;
        for (PtmCategory cate : cates) {

            cc.setCateId(cate.getId());

            System.out.println(String.format("exp cate[%d] to files", cate.getId()));
            listAndProcessTask2.go();

            count++;
            /*if (count >= 10) {
                break;
            }*/
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
//                if (category.getLevel() == 3) {
//                    long count = dbm.querySingle(Q_PRODUCT, Arrays.asList(category.getId()));
//                    if (count == 0) {
//                        continue;
//                    }
//                }
                cateMap.put(category.getId(), category);
                cates.add(category);
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

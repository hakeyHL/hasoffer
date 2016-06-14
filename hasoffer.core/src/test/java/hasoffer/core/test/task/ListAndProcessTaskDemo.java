package hasoffer.core.test.task;

import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmTitleWordStat;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.task.ListAndProcessTask;
import hasoffer.core.task.worker.IProcess;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Date : 2016/5/3
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ListAndProcessTaskDemo {

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ICmpSkuService cmpSkuService;
    private Logger logger = LoggerFactory.getLogger(ListAndProcessTaskDemo.class);

    @Test
    public void f() {
        String sql = "select t from PtmCmpSku t";

        ListAndProcessTask task = new ListAndProcessTask(dbm);
        task.go(sql, new IProcess<PtmCmpSku>() {
            @Override
            public void process(PtmCmpSku cmpSku) {
                cmpSkuService.createPtmCmpSkuIndexToMongo(cmpSku);

                String title = cmpSku.getSkuTitle();
                if (StringUtils.isEmpty(title) || title.contains("null")) {
                    title = cmpSku.getTitle();
                    if (StringUtils.isEmpty(title)) {
                        return;
                    }
                }

                String[] words = StringUtils.getCleanWords(title);
                for (String word : words) {
                    word = word.trim();
                    if (StringUtils.isEmpty(word)) {
                        continue;
                    }
                    updateWordCount(word);
                }
            }
        });
    }

    //    synchronized void updateWordCount(String word) {
    void updateWordCount(String word) {
        PtmTitleWordStat wordStat = mdm.queryOne(PtmTitleWordStat.class, word);
        if (wordStat == null) {
            wordStat = new PtmTitleWordStat(word, 1);
            mdm.save(wordStat);
        } else {
            logger.debug(word + "\t" + wordStat.getCount());
            Update update = new Update();
            update.inc("count", 1);
            mdm.update(PtmTitleWordStat.class, word, update);
        }
    }
}

package hasoffer.spider.test;

import hasoffer.spider.model.SpiderConfig;
import hasoffer.spider.service.ISpiderConfigService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-resources.xml")
public class SpiderConfigServiceTest {

    @Resource
    ISpiderConfigService spiderConfigService;

    @Test
    public void testFindById() {
        SpiderConfig spiderConfig = spiderConfigService.findById(1L);
        Assert.assertNotNull(spiderConfig);
    }
}
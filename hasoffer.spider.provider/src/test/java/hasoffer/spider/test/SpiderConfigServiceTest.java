package hasoffer.spider.test;

import hasoffer.dubbo.spider.task.api.ISpiderConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-resources.xml")
public class SpiderConfigServiceTest {

    ISpiderConfigService spiderConfigService;

    @Test
    public void testFindById() {
        //SpiderConfig spiderConfig = spiderConfigService.findById(1L);
        //Assert.assertNotNull(spiderConfig);
    }
}
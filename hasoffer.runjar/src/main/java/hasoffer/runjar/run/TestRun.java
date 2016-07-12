package hasoffer.runjar.run;

import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.IProductService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by chevy on 2016/7/12.
 */

public class TestRun {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-beans.xml");

        IProductService productService = ctx.getBean(IProductService.class);

        PtmProduct product = productService.getProduct(10L);
        System.out.println(product);
    }

}

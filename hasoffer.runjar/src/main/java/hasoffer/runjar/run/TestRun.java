package hasoffer.runjar.run;

import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.IProductService;
import hasoffer.runjar.util.SpringUtils;

/**
 * Created by chevy on 2016/7/12.
 */

public class TestRun {

    public static void main(String[] args) {
        IProductService productService = SpringUtils.getBean(IProductService.class);//ctx.getBean(IProductService.class);

        PtmProduct product = productService.getProduct(10L);
        System.out.println(product.getTitle());
    }

}

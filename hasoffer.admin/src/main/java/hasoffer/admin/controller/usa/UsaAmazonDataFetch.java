package hasoffer.admin.controller.usa;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.fetch.sites.amazon.UsaAmazonSummaryProductProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * Created by Administrator on 2016/9/2.
 */
@Controller
@RequestMapping(value = "/useamazondatafetch")
public class UsaAmazonDataFetch {

    //useamazondatafetch/start
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public String start(HttpServletRequest request) throws HttpFetchException, ContentParseException {

        String separator = File.separator;

        File file = new File("home" + separator + "hasoffer" + separator + "uploads" + separator + "2016" + separator + "08" + separator + "30");

        boolean mkdir = file.mkdir();

        System.out.println("mkdie " + mkdir);

        ISummaryProductProcessor summaryProductProcessor = new UsaAmazonSummaryProductProcessor();

        OriFetchedProduct oriFetchedProduct = summaryProductProcessor.getSummaryProductByUrl("https://www.amazon.com/gp/product/B00TKFDKMQ");

        System.out.println(oriFetchedProduct.getTitle());
        System.out.println(oriFetchedProduct.getImageUrl());
        System.out.println(oriFetchedProduct.getPrice());

        return "ok";
    }

}

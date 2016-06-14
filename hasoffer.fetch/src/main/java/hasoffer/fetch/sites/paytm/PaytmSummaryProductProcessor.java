package hasoffer.fetch.sites.paytm;

import com.google.gson.Gson;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.FetchedProduct;
import hasoffer.fetch.sites.paytm.model.FetchedProductHelper;
import org.htmlcleaner.TagNode;

/**
 * Created on 2016/2/29.
 */
public class PaytmSummaryProductProcessor implements ISummaryProductProcessor {

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException {

        FetchedProduct fetchedProduct = new FetchedProduct();

        String[] subStrs1 = url.split("\\?");
        url = subStrs1[0];
        //将url更新为返回json的url
        String jsonUrl = url.replace("paytm", "catalog.paytm").replace("shop", "v1");
        TagNode root = HtmlUtils.getUrlRootTagNode(jsonUrl);
        String json = root.getText().toString();

        Gson gson = new Gson();

        FetchedProductHelper summaryProductHelper = gson.fromJson(json, FetchedProductHelper.class);
        float price = Float.parseFloat(summaryProductHelper.getOffer_price().trim());

        fetchedProduct.setImageUrl(summaryProductHelper.getImage_url());
        fetchedProduct.setPrice(price);
        fetchedProduct.setProductStatus(summaryProductHelper.getInstock() == "true" ? ProductStatus.ONSALE : ProductStatus.OUTSTOCK);
        fetchedProduct.setTitle(summaryProductHelper.getName());
        fetchedProduct.setSourceSid(summaryProductHelper.getParent_id());
        fetchedProduct.setWebsite(WebsiteHelper.getWebSite(url));
        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.PAYTM);

        return fetchedProduct;
    }

    private float getPriceByPriceString(String priceString) {

        String[] subStrs1 = priceString.split("Rs");
        String priceStr = subStrs1[1].replace(",", "").replace(" ", "");
        float price = Float.parseFloat(priceStr);

        return price;
    }
}

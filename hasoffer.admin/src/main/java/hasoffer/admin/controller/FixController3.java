package hasoffer.admin.controller;

import com.alibaba.fastjson.JSONObject;
import hasoffer.base.exception.ContentParseException;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.ptm.PtmImage2;
import hasoffer.core.product.IProductService;
import org.htmlcleaner.TagNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

import static hasoffer.base.utils.HtmlUtils.getSubNodesByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 *
 */
@Controller
@RequestMapping(value = "/fix3")
public class FixController3 {

    public static final String WEBSITE_DX_URL_PREFIEX = "http://www.dx.com/";
    @Resource
    IProductService productService;

    @RequestMapping(value = "/getimages", method = RequestMethod.GET)
    @ResponseBody
    public String fetch() {

        String[] urlArray = new String[]{
                "http://www.dx.com/c/hobbies-toys-899/rc-airplanes-quadcopters-805?pageSize=200&page=1",
                "http://www.dx.com/c/hobbies-toys-899/rc-airplanes-quadcopters-805?pageSize=200&page=2",
                "http://www.dx.com/c/hobbies-toys-899/rc-airplanes-quadcopters-805?pageSize=200&page=3",
        };

        int count = 1;
        for (String url : urlArray) {
            try {
                TagNode pageRoot = HtmlUtils.getUrlRootTagNode(url);

                List<TagNode> productNodeList = getSubNodesByXPath(pageRoot, "//ul[@class='productList subList']/li");

                for (TagNode productNode : productNodeList) {
                    try {
                        TagNode urlNode = getSubNodeByXPath(productNode, "//div[@class='photo']/a", new ContentParseException("url node not found"));
                        String productUrl = WEBSITE_DX_URL_PREFIEX + urlNode.getAttributeByName("href");

                        System.out.println(count++ + " : " + productUrl);
                        fetchOne(productUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "ok";
    }

    public void fetchOne(String productUrl) throws Exception {
        TagNode productInfoPageRoot = HtmlUtils.getUrlRootTagNode(productUrl);
        String urlHtml = HtmlUtils.getUrlHtml(productUrl);

        Huiji huiji = new Huiji();
        huiji.setUrl(productUrl);

        TagNode imagebrotherNode = getSubNodeByXPath(productInfoPageRoot, "//div[@id='midPicBox']", new ContentParseException("image brother node not found"));

        TagNode parentNode = imagebrotherNode.getParent();

        List<TagNode> imageListNode = getSubNodesByXPath(parentNode, "//div[@class='small_photo']/div/ul");

        imageListNode = getSubNodesByXPath(imageListNode.get(0), "/li");

        for (TagNode imageNode : imageListNode) {

            imageNode = getSubNodeByXPath(imageNode, "/a", new ContentParseException("image node not found"));

            String imageUrlString = imageNode.getAttributeByName("rel");

            JSONObject json = JSONObject.parseObject(imageUrlString);

            String simageUrl = json.getString("sImg");
            String mimageUrl = StringUtils.filterAndTrim(simageUrl, Arrays.asList("_small"));

            Map<String, String> imageMap = new HashMap<>();

            imageMap.put("simg", "http:" + simageUrl);
            imageMap.put("mimg", "http:" + mimageUrl);
            imageMap.put("bimg", "http:" + mimageUrl);

            huiji.getImageList().add(imageMap);
        }

        for (Object obj : huiji.getImageList()) {
            Map<String, String> image = (Map<String, String>) obj;

            PtmImage2 image2 = new PtmImage2(huiji.getUrl(), image.get("bimg"), image.get("mimg"), image.get("simg"));
            productService.saveImage222(image2);
        }

    }

    class Huiji {
        private String id;
        private String url;
        private List<Object> imageList = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<Object> getImageList() {
            return imageList;
        }

        public void setImageList(List<Object> imageList) {
            this.imageList = imageList;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "Huiji{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", imageList=" + imageList +
                    '}';
        }
    }
}
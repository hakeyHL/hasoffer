package hasoffer.admin.controller.usa;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.fetch.sites.amazon.UsaAmazonSummaryProductProcessor;
import hasoffer.fetch.sites.amazon.ext.model.UsaAmazonData;
import jodd.io.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/9/2.
 */
@Controller
@RequestMapping(value = "/useamazondatafetch")
public class UsaAmazonDataFetch {

    //useamazondatafetch/start
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public String start(HttpServletRequest request) throws HttpFetchException, ContentParseException, IOException {

        String[] urlArrays = {
                "https://www.amazon.com/Aluminum-Protective-Resistant-Shockproof-Military/dp/B015KW9QKW/ref=sr_1_26?ie=UTF8&qid=1472890169&sr=8-26&keywords=man+3c",
                "https://www.amazon.com/3C-Mall-Cartoon-Passport-Organizer/dp/B01508K0U6/ref=sr_1_34?ie=UTF8&qid=1472890265&sr=8-34&keywords=man+3c",
                "https://www.amazon.com/Dane-Elec-USB-flash-drive-Multi-pack/dp/B00KNRWTB8/ref=sr_1_29?ie=UTF8&qid=1472890661&sr=8-29&keywords=free+shipping",
                "https://www.amazon.com/Aquazone-Premium-Swimming-Goggles-Protection/dp/B01F9N62P4/ref=pd_sim_229_5?ie=UTF8&refRID=FRWXNXMKN7PAYTAZCKBX",
                "https://www.amazon.com/Cressi-Leonardo-Computer-computer-included/dp/B00I556YZ2",
                "https://www.amazon.com/Bullet-Wrist-Guard-Adult-Medium/dp/B001UE9DRQ/ref=sr_1_1738?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978784&sr=1-1738&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
        };

        String path = request.getSession().getServletContext().getRealPath("/");

        System.out.println(path);

            File file = new File(path + "/Uploads", "/2016/08/30");

        File sqlFile = new File(path + "sql.txt");

//        已经创建成功
//        boolean mkdir = file.mkdirs();
//        System.out.println(mkdir);

        for (String url : urlArrays) {

            StringBuilder stringBuilder = new StringBuilder();

            UsaAmazonSummaryProductProcessor usaAmazonSummaryProductProcessor = new UsaAmazonSummaryProductProcessor();

            UsaAmazonData usaAmazonData = null;

            try {
                usaAmazonData = usaAmazonSummaryProductProcessor.getSummaryProductByUrl(url);
            } catch (Exception e) {
                System.out.println("error for " + url);
                    e.printStackTrace();
                continue;
            }

            if (usaAmazonData.getPrice() == 0.0) {
                continue;
            }
            if (usaAmazonData.getDisPrice() == -1.0) {
                continue;
            }

            System.out.println(usaAmazonData.getTitle());
            System.out.println(usaAmazonData.getImageUrl());
            System.out.println(usaAmazonData.getPrice());
            System.out.println(usaAmazonData.getDisPrice());
            System.out.println(usaAmazonData.getLink());

            String fileName = StringUtils.filterAndTrim(UUID.randomUUID().toString(), Arrays.asList("-")) + ".jpg";

            HttpUtils.getImage(usaAmazonData.getImageUrl(), new File(file, fileName));

            stringBuilder.append("insert into cb_goods (sell_type,link,title,cover,price,discount_price) values (\"");

                stringBuilder.append("k99\",\"" + usaAmazonData.getLink() + "\",\"" + usaAmazonData.getTitle() + "\",\"Uploads/2016/08/30/" + fileName + "\"," + usaAmazonData.getPrice() + "," + usaAmazonData.getDisPrice() + ");");

            System.out.println(stringBuilder.toString());

            FileUtil.appendString(sqlFile, stringBuilder.toString() + "\n");

                try {
                        System.out.println("sleeping");
                        TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }

        }


        return "ok";
    }

}

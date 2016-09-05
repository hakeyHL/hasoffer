package hasoffer.admin.controller.usa;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.fetch.sites.amazon.UsaAmazonSummaryProductProcessor;
import jodd.io.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

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
                "https://www.amazon.com/1byone-Miles-Super-Antenna-Performance/dp/B00RFLXAMW/ref=gbph_tit_m-4_0782_3cf2e53d?smid=ATVPDKIKX0DER&pf_rd_p=2564520782&pf_rd_s=merchandised-search-4&pf_rd_t=101&pf_rd_i=11601506011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=XQ40QZN0ZZBPPQVGS3XY",
                "https://www.amazon.com/Symphony-Spirito-Mid-tier-Earbuds-Inline/dp/B01DPRY9WY/ref=lp_12270394011_1_1?srs=12270394011&ie=UTF8&qid=1472990542&sr=8-1",
                "https://www.amazon.com/Tripp-Lite-Portable-Protector-SK120USB/dp/B00EYCEZLA/ref=gbph_tit_m-4_0782_13911eee?smid=ATVPDKIKX0DER&pf_rd_p=2564520782&pf_rd_s=merchandised-search-4&pf_rd_t=101&pf_rd_i=11601506011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=XQ40QZN0ZZBPPQVGS3XY",
//                "https://www.amazon.com/Titus-Symphony-Mid-tier-Earbuds-Inline/dp/B01DPRY9RE/ref=lp_12273809011_1_1?srs=12273809011&ie=UTF8&qid=1472990666&sr=8-1",
//                "https://www.amazon.com/Edimax-EW-7811Un-150Mbps-Raspberry-Supports/dp/B003MTTJOY/ref=sr_1_1?s=tv&ie=UTF8&qid=1472990727&sr=1-1&refinements=p_75%3A50-%2Cp_36%3A600-2000%2Cp_72%3A1248879011",
//                "https://www.amazon.com/E422VLE-E472VLE-E552VLE-E320i-A0-Televisions/dp/B00BTFRB4S/ref=sr_1_2?s=tv&ie=UTF8&qid=1472990727&sr=1-2&refinements=p_75%3A50-%2Cp_36%3A600-2000%2Cp_72%3A1248879011",
//                "https://www.amazon.com/LensPen-DSLR-Camera-Cleaning-NDSLRK-1/dp/B0081ER9KG/ref=sr_1_11?s=tv&ie=UTF8&qid=1472990727&sr=1-11&refinements=p_75%3A50-%2Cp_36%3A600-2000%2Cp_72%3A1248879011",
//                "https://www.amazon.com/AmazonBasics-Holster-Camera-Case-Cameras/dp/B008MWBY6W/ref=gbph_tit_m-5_7622_1a0574b3?smid=ATVPDKIKX0DER&pf_rd_p=2548627622&pf_rd_s=merchandised-search-5&pf_rd_t=101&pf_rd_i=761198&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=XHQZ20382E50NFA981QM",
//                "https://www.amazon.com/Pictek-Binoculars-Telescope-Finishing-Sightseeing/dp/B01AWGCLKS/ref=gbph_tit_m-5_7622_06a832fc?smid=A913ZGO5BGAGD&pf_rd_p=2548627622&pf_rd_s=merchandised-search-5&pf_rd_t=101&pf_rd_i=761198&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=XHQZ20382E50NFA981QM",
//                "https://www.amazon.com/Apexel-Foldable-Shutter-Monopod-Fisheye/dp/B00D3F3O8Y/ref=lp_12271258011_1_2?srs=12271258011&ie=UTF8&qid=1472991302&sr=8-2",
//                "https://www.amazon.com/gp/product/B00PMMBBSG/ref=s9_acsd_hps_bw_c_x_5?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=merchandised-search-11&pf_rd_r=XHQZ20382E50NFA981QM&pf_rd_t=101&pf_rd_p=2172929282&pf_rd_i=761198",
//                "https://www.amazon.com/SanDisk-Cruzer-Frustration-Free-Packaging--SDCZ36-064G-AFFP/dp/B007JR5304/ref=lp_12280234011_1_1?srs=12280234011&ie=UTF8&qid=1472991508&sr=8-1",
//                "https://www.amazon.com/SanDisk-Cruzer-Flash-Black--SDCZ51-016G-B35/dp/B004I5A9NQ/ref=lp_12280234011_1_6?srs=12280234011&ie=UTF8&qid=1472991508&sr=8-6",
//                "https://www.amazon.com/Mpow-Bluetooth-Receiver-Streambot-Hands-Free/dp/B00MJMV0GU/ref=gbph_tit_m-3_2322_daae0604?smid=A1E57UY6AMKO25&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/RAVPower-Certified-Lifespan-Lightning-iPhone7/dp/B01G3NL5S8/ref=gbph_tit_m-3_2322_41614b82?smid=A2KUZVNQ9LP7N9&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Mpow-Transmitter-Wireless-Bluetooth-Hands-Free/dp/B00KIMX4EY/ref=gbph_tit_m-3_2322_d9387dee?smid=A1E57UY6AMKO25&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Mobility%C2%AE-Magnetic-Car-Mount-Clip/dp/B01FIMVJIG/ref=gbph_tit_m-3_2322_2ec2c4dc?smid=A14D1BKUJMZKVB&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Cricket-Wireless-Complete-Starter-Pack/dp/B0136NJTLS/ref=gbph_tit_m-3_2322_bb177643?smid=ATVPDKIKX0DER&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/AOFU-Lightning-iPhone-Braided-Charging/dp/B01IGSDEXC/ref=gbph_tit_m-3_2322_420e178f?smid=A1GFV74X9A4UXK&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Mengo-Magna-Snap-Magnetic-Smartphones-Samsung/dp/B00UZBMUK2/ref=gbph_tit_m-3_2322_3c067ae2?smid=A23I86G8ZNJ60H&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/YINENN-Wireless-Bluetooth-Neckband-Smartphones/dp/B00SORGJ7Y/ref=gbph_tit_m-3_2322_d7b8392e?smid=A2FR5F4RUUQQ1T&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Vomercy-Earbuds-Headphones-Microphones-Magnetic/dp/B01IR9JVE6/ref=gbph_tit_m-3_2322_8d8fe777?smid=AN4R3K7NSDY40&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Noot-Premium-Earphones-Android-Smartphone/dp/B00Q7DFW8O/ref=gbph_tit_m-3_2322_5f2a3dca?smid=ATVPDKIKX0DER&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/iXCC-ElementII-Lightning-Charge-iPhone/dp/B00LAG4HN4/ref=gbph_tit_m-3_2322_9d894fed?smid=A2691ZBIKXAV7P&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Galaxy-Spigen%C2%AE-Hybrid-KickStand-Crystal/dp/B01JK2SPWS/ref=gbph_tit_m-3_2322_1b162e4d?smid=A2SFKRF5TPZMT5&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Bestoss-10000mAh-Portable-Lightning-USB-Charged/dp/B01FJV835S/ref=gbph_tit_m-3_2322_40ccc749?smid=A2IRUVGMRMTNFW&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/12000mAh-Solar-SunPower-Highest-Efficiency-Portable/dp/B01B2WSENE/ref=gbph_tit_m-3_2322_834efe1b?smid=A142BGUZC6UH73&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Airsspu-Earphones-Resolution-Headphones-Microphone/dp/B019T37MGI/ref=gbph_tit_m-3_2322_fc108d4e?smid=ARDBPD8BFHWSP&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/iPhone-Charger-Mengo-Certified-Tangle-Free/dp/B01725VFCC/ref=gbph_tit_m-3_2322_04b9e17b?smid=A23I86G8ZNJ60H&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Protector-PRODUCTS%C2%AE-Premium-Crystal-Tempered/dp/B0157E2OOS/ref=gbph_tit_m-3_2322_e695e107?smid=ATVPDKIKX0DER&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/AOFU-Lightning-Charging-Aluminum-Connector/dp/B01IGQB3PU/ref=gbph_tit_m-3_2322_71c6a5c8?smid=A1GFV74X9A4UXK&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Mengo-Slim-Snap-Aluminum-Magnetic-Samsung/dp/B01DL1VEYU/ref=gbph_tit_m-3_2322_40aa3580?smid=A23I86G8ZNJ60H&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Certified-iXCC-Charger-Lightning-Devices/dp/B013JC3MZU/ref=gbph_tit_m-3_2322_9bcae462?smid=A2691ZBIKXAV7P&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/dp/B017BYWP5K/ref=gbph_tit_m-3_2322_b8525fb7?smid=A23I86G8ZNJ60H&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Trianium-Protanium-Shock-Absorption-Protection-Protective/dp/B01JHFRHG8/ref=gbph_tit_m-3_2322_a636ca34?smid=A3FZ4CWF6UZ2CV&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/iPhone-Charger-Mengo-Certified-Lighting/dp/B00ZJ7A9EG/ref=gbph_tit_m-3_2322_9bc600f8?smid=A23I86G8ZNJ60H&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Bluetooth-Selfie-Stick-Selfiestick-Built/dp/B011358N8Q/ref=gbph_tit_m-3_2322_107b970f?smid=A2691ZBIKXAV7P&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/iPhone-Charger-PRODUCTS-Certified-Lightning/dp/B00MBWONEY/ref=gbph_tit_m-3_2322_f07afe97?smid=ATVPDKIKX0DER&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/EZOPower-3-Pack-Certified-Lightning-iPhone/dp/B01GFV6YUW/ref=gbph_tit_m-3_2322_4cdd435c?smid=A3E5K6GEFSI075&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Galaxy-S5-Case-Snap-Packaging/dp/B00H4O0FCI/ref=gbph_tit_m-3_2322_c77e81ea?smid=ATVPDKIKX0DER&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/MOTA-800mAh-Credit-Card-Sized-Portable/dp/B00GJ0XYF8/ref=gbph_tit_m-3_2322_ffe41b5c?smid=ATVPDKIKX0DER&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Anker-5-Pack-PowerLine-Micro-USB/dp/B015XPL2JE/ref=gbph_tit_m-3_2322_3e470a10?smid=A294P4X9EWVXLJ&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Bluetooth-Headphones-TaoTronics-Cancelling-Sweatproof/dp/B017GQ6KI6/ref=gbph_tit_m-3_2322_dff6f838?smid=A2KUZVNQ9LP7N9&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/CardNinja-Ultra-slim-Adhesive-Credit-Smartphones/dp/B00CMC44EC/ref=gbph_tit_m-3_2322_99a2a5fc?smid=ATVPDKIKX0DER&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/SoundPEATS-Bluetooth-Headphones-Microphone-Sweatproof/dp/B01ABOZL58/ref=gbph_tit_m-3_2322_872db5bd?smid=A2NTRQSLKX00J5&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",
//                "https://www.amazon.com/Smartphone-iKross-Universal-Windshield-Dashboard/dp/B00I5URYME/ref=gbph_tit_m-3_2322_0b6e96a4?smid=A3E5K6GEFSI075&pf_rd_p=2549132322&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=2447856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=28K6HYE5DFDZGX00D0WR",

        };

        String path = request.getSession().getServletContext().getRealPath("/");

        System.out.println(path);

        File file = new File(path + "/uploads", "/2016/08/30");

        File sqlFile = new File(path + "sql.txt");

//        已经创建成功
//        boolean mkdir = file.mkdirs();
//        System.out.println(mkdir);

        for (String url : urlArrays) {

            StringBuilder stringBuilder = new StringBuilder();

            ISummaryProductProcessor summaryProductProcessor = new UsaAmazonSummaryProductProcessor();

            OriFetchedProduct oriFetchedProduct = summaryProductProcessor.getSummaryProductByUrl(url);

            System.out.println(oriFetchedProduct.getTitle());
            System.out.println(oriFetchedProduct.getImageUrl());
            System.out.println(oriFetchedProduct.getPrice());

            String fileName = StringUtils.filterAndTrim(UUID.randomUUID().toString(), Arrays.asList("-"));

            HttpUtils.getImage(oriFetchedProduct.getImageUrl(), new File(file, fileName));

            stringBuilder.append("insert into cb_goods (link,title,cover,price) values (");

            stringBuilder.append(url + "," + oriFetchedProduct.getTitle() + "," + fileName + "," + oriFetchedProduct.getPrice() + ");");

            System.out.println(stringBuilder.toString());

            FileUtil.appendString(sqlFile, stringBuilder.toString());
        }


        return "ok";
    }

}

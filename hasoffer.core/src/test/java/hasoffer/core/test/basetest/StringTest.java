package hasoffer.core.test.basetest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.mongo.MobileCateDescription;
import hasoffer.fetch.helper.WebsiteHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2016/5/16.
 */
public class StringTest {

    @Test
    public void testSubString(){

        String str = "afeajfeojafeafeaf";

        String str1 = str.substring(str.indexOf('e'));

        String str2 = str1.substring(0, str1.indexOf('o'));

        System.out.println(str2);

    }

    @Test
    public void testStringIndex() {
        String html = "        <div style=\"position: relative\">\n" +
                "          <div style=\"z-index: 3; padding: 30px 0px 40px 0px;background: #FAFBFC;\" id=\"web-footerMount\"><!-- react-empty: 1 --></div>\n" +
                "        </div>\n" +
                "        <script>window.__myx_deviceType__ = 'desktop';</script>\n" +
                "        <script type=\"text/javascript\" src=\"https://apis.google.com/js/platform.js\"></script>\n" +
                "        <script>\n" +
                "          window.fbAsyncInit = function() {\n" +
                "            FB.init({\n" +
                "              appId      : '182424375109898',\n" +
                "              xfbml      : true,\n" +
                "              version    : 'v2.5'\n" +
                "            });\n" +
                "          };\n" +
                "          (function(d, s, id){\n" +
                "             var js, fjs = d.getElementsByTagName(s)[0];\n" +
                "             if (d.getElementById(id)) {return;}\n" +
                "             js = d.createElement(s); js.id = id;\n" +
                "             js.src = \"//connect.facebook.net/en_US/sdk.js\";\n" +
                "             fjs.parentNode.insertBefore(js, fjs);\n" +
                "           }(document, 'script', 'facebook-jssdk'));\n" +
                "        </script>\n" +
                "        <script>\n" +
                "          window.__myx_seo__ = [[{\"name\":\"Nike Shoes\",\"linkUrl\":\"nike-shoes\"},{\"name\":\"Adidas Shoes\",\"linkUrl\":\"adidas-shoes\"},{\"name\":\"Casual Shoes\",\"linkUrl\":\"casual-s\n" +
                "hoes\"},{\"name\":\"Sports Shoes\",\"linkUrl\":\"sports-shoes\"},{\"name\":\"Fastrack Watches\",\"linkUrl\":\"fastrack-watches\"},{\"name\":\"Ethnic Wear\",\"linkUrl\":\"ethnic-wear\"},{\"name\":\"\n" +
                "Woodland Shoes\",\"linkUrl\":\"woodland-shoes\"},{\"name\":\"Puma Shoes\",\"linkUrl\":\"puma-shoes\"},{\"name\":\"Accessories\",\"linkUrl\":\"accessories\"},{\"name\":\"Anarkali Suits\",\"linkUrl\n" +
                "\":\"anarkali-suit\"},{\"name\":\"Running Shoes\",\"linkUrl\":\"running-shoes\"},{\"name\":\"Reebok\",\"linkUrl\":\"reebok\"},{\"name\":\"Formal Wear\",\"linkUrl\":\"formal-wear\"},{\"name\":\"CAT\",\"\n" +
                "linkUrl\":\"cat\"},{\"name\":\"Jewellery\",\"linkUrl\":\"jewellery\"}],[{\"name\":\"Tops\",\"linkUrl\":\"tops\"},{\"name\":\"Shirts\",\"linkUrl\":\"shirts\"},{\"name\":\"Jackets\",\"linkUrl\":\"jackets\"}\n" +
                ",{\"name\":\"Kurtis\",\"linkUrl\":\"kurtis\"},{\"name\":\"Shoes\",\"linkUrl\":\"shoes\"},{\"name\":\"Tunics\",\"linkUrl\":\"tunics\"},{\"name\":\"Dresses\",\"linkUrl\":\"women-dresses\"},{\"name\":\"Watch\n" +
                "es\",\"linkUrl\":\"watches\"},{\"name\":\"Saree\",\"linkUrl\":\"saree\"},{\"name\":\"Kurtas\",\"linkUrl\":\"kurtas\"},{\"name\":\"Bags\",\"linkUrl\":\"bags\"},{\"name\":\"T-shirts\",\"linkUrl\":\"tshirts\"}\n" +
                ",{\"name\":\"Designer Sarees\",\"linkUrl\":\"designer-saree\"},{\"name\":\"Sunglasses\",\"linkUrl\":\"sunglasses\"},{\"name\":\"Jeans\",\"linkUrl\":\"jeans\"},{\"name\":\"Trousers\",\"linkUrl\":\"trou\n" +
                "sers\"},{\"name\":\"Winter Wear\",\"linkUrl\":\"winter-wear\"}],[{\"name\":\"Online Shopping\",\"linkUrl\":\"\"},{\"name\":\"Nike\",\"linkUrl\":\"nike\"},{\"name\":\"Puma\",\"linkUrl\":\"puma\"},{\"name\"\n" +
                ":\"Adidas\",\"linkUrl\":\"adidas\"},{\"name\":\"Fila\",\"linkUrl\":\"fila\"},{\"name\":\"Lee\",\"linkUrl\":\"lee\"},{\"name\":\"United Colors of Benetton\",\"linkUrl\":\"united-colors-of-benetton\"},\n" +
                "{\"name\":\"Wrangler\",\"linkUrl\":\"wrangler\"},{\"name\":\"Fastrack\",\"linkUrl\":\"fastrack\"},{\"name\":\"Woodland\",\"linkUrl\":\"woodland\"},{\"name\":\"Vans\",\"linkUrl\":\"vans\"},{\"name\":\"Levi\n" +
                "s\",\"linkUrl\":\"levis\"},{\"name\":\"Tommy Hilfiger\",\"linkUrl\":\"tommy-hilfiger\"},{\"name\":\"Peter England\",\"linkUrl\":\"peter-england\"}]];\n" +
                "          window.__myx_navigationData__ = {\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"T-Shirts\",\"url\":\"/men-tshirts?src=tNav\"}},{\"\n" +
                "children\":[],\"props\":{\"style\":\"\",\"title\":\"Casual Shirts\",\"url\":\"/men-casual-shirts-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Formal Shirts\",\"url\":\"/me\n" +
                "n-formal-shirts-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sweaters & Sweatshirts\",\"url\":\"/sweaters-and-sweatshirts-men-menu?src=tNav\"}},{\"children\":[]\n" +
                ",\"props\":{\"style\":\"\",\"title\":\"Jackets\",\"url\":\"/men-jackets-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Blazers & Coats\",\"url\":\"/blazers-and-coats-men-me\n" +
                "nu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Suits\",\"url\":\"/men-suits?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Topwear\",\"url\":\"/men-topwear?src=tNav\"}},{\"\n" +
                "children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Jeans\",\"url\":\"/men-jeans-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Casual Trousers\",\"url\":\"/men-casual-t\n" +
                "rousers-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Formal Trousers\",\"url\":\"/men-formal-trousers-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"ti\n" +
                "tle\":\"Shorts\",\"url\":\"/mens-shorts-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Track Pants\",\"url\":\"/men-trackpants-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\n" +
                "\"title\":\"Bottomwear\",\"url\":\"/men-bottomwear?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Active T-Shirts\",\"url\":\"/men-sports-tshirts-menu?src=tNa\n" +
                "v\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Track Pants & Shorts\",\"url\":\"/sports-tracks-shorts-men-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Jack\n" +
                "ets & Sweatshirts\",\"url\":\"/men-sports-jackets-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Swimwear\",\"url\":\"/men-swimwear-menu?src=tNav\"}},{\"children\":[],\"props\":\n" +
                "{\"style\":\"\",\"title\":\"Smart Wearables\",\"url\":\"/smart-wearables-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Accessories\",\"url\":\"/sports?f=categorie\n" +
                "s%3ABackpacks%2CBag%2CDuffel%2520Bag%2CGloves%2CHeadband%2CHeadphones%2CSports%2520Accessories%2CSunglasses%2CSwimwear%2520Accessories%2CWater%2520Bottle%2CWristbands%3A\n" +
                "%3Agender%3Amen%2Cmen%2520women\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Equipment\",\"url\":\"/sports?f=categories%3ABadminton%2520Racquets%2CBadminton%2520Shu\n" +
                "ttlecocks%2CBasketball%2CCricket%2520Bats%2CFootball%2CFootballs%2CTable%2520Tennis%2520Bats%2CTable%2520Tennis%2520Kits%2CTennis%2520Balls%2CTennis%2520Kits%2CTennis%25\n" +
                "20Racquets\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Shoes\",\"url\":\"/men-sports-shoes?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Sports & Active Wear\",\"url\":\"/\n" +
                "sports?userQuery=true&f=categories%3AJackets%2CShorts%2CSports%2520Accessories%2CSports%2520Shoes%2CSweatshirts%2CSwimwear%2520Accessories%2CTrack%2520Pants%2CTshirts%3A\n" +
                "%3Agender%3Amen%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women\"}},{\"chil\n" +
                "dren\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Kurtas & Kurta Sets\",\"url\":\"/mens-kurtas-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sherwanis\",\"url\"\n" +
                ":\"/men-sherwani-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Nehru Jackets\",\"url\":\"/nehru-jacket?userQuery=true&f=gender%3Amen%2Cmen%2520women&src=tNav\"}\n" +
                "}],\"props\":{\"style\":\"\",\"title\":\"Indian & Festive Wear\",\"url\":\"/mens-kurtas-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Plus Size\",\"url\":\"/men-plus-size-\n" +
                "menu?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Briefs & Trunks\",\"url\":\"/briefs-and-trunks-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\n" +
                "\"title\":\"Boxers\",\"url\":\"/men-boxers-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Vests\",\"url\":\"/men-innerwear-vests-menu?src=tNav\"}},{\"children\":[],\"prop\n" +
                "s\":{\"style\":\"\",\"title\":\"Sleepwear & Loungewear\",\"url\":\"/sleep-and-lounge-wear-men-menu?sort=popularity?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Thermals\",\n" +
                "\"url\":\"/thermals-men-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Innerwear & Sleepwear\",\"url\":\"/men-inner-sleepwear-menu?src=tNav\"}},{\"children\":[{\"children\":[],\"prop\n" +
                "s\":{\"style\":\"\",\"title\":\"Casual Shoes\",\"url\":\"/men-casual-shoes?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Shoes\",\"url\":\"/men-sports-shoes?src=tNav\"}}\n" +
                ",{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Formal Shoes\",\"url\":\"/men-formal-shoes?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sandals & Floaters\",\"url\":\"/m\n" +
                "en-sandals-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Flip Flops\",\"url\":\"/men-flip-flops-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"S\n" +
                "ocks\",\"url\":\"/men-socks-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Footwear\",\"url\":\"/men-footwear?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Watches & W\n" +
                "earables\",\"url\":\"/men-watches-wearables-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sunglasses & Frames\",\"url\":\"/men-sunglasses-frames-menu?src=tNav\"}},\n" +
                "{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Bags & Backpacks\",\"url\":\"/men-bags-and-backpacks-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Luggage & Troll\n" +
                "eys\",\"url\":\"/luggage-and-trolley-bags-menu?f=gender%3Amen%2Cmen%2520women?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Personal Care & Grooming\",\"url\":\"/men-p\n" +
                "ersonal-care-menu?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Wallets\",\"url\":\"/men-wallets-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"B\n" +
                "elts\",\"url\":\"/men-belts-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Ties, Cufflinks & Pocket Squares\",\"url\":\"/men-ties-cufflinks-psquares-menu\"}},{\"children\":[],\n" +
                "\"props\":{\"style\":\"\",\"title\":\"Accessory Gift Sets\",\"url\":\"/men-accessory-gift-set-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Headphones\",\"url\":\"/headphones-menu\"\n" +
                "}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Helmets\",\"url\":\"/helmet-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Caps & Hats\",\"url\":\"/men-caps-hats-menu\"}},{\"c\n" +
                "hildren\":[],\"props\":{\"style\":\"\",\"title\":\"Mufflers, Scarves & Gloves\",\"url\":\"/men-gloves-mufflers-scarves-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Phone Cases\"\n" +
                ",\"url\":\"/mobile-case-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Travel Accessories\",\"url\":\"/men-travel-accessory-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"tit\n" +
                "le\":\"Rings & Wristwear\",\"url\":\"/men-jewellery-menu\"}}],\"props\":{\"style\":\"\",\"title\":\"Fashion Accessories\",\"url\":\"/men-accessories\"}}],\"props\":{\"meta\":\"{\\\"dock\\\":\\\"true\\\",\n" +
                "\\\"template\\\":\\\"fashion\\\",\\\"template_config\\\":{\\\"position\\\":3,\\\"color\\\":\\\"#ee5f73\\\",\\\"icon\\\":\\\"http://myntra.myntassets.com/assets/premium/temp/Men.png\\\"}}\",\"style\":\"\",\"t\n" +
                "itle\":\"Men\",\"url\":\"/shop/men\"}},{\"children\":[{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Kurtas & Suits\",\"url\":\"/kurtas-and-suits-menu?src=tNav\"}},{\"childre\n" +
                "n\":[],\"props\":{\"style\":\"\",\"title\":\"Kurtis, Tunics & Tops\",\"url\":\"/kurtis-tunics-tops-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Leggings, Salwars, Chur\n" +
                "idars\",\"url\":\"/leggings-churidar-salwar-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Skirts & Palazzos\",\"url\":\"fusion-skirts-trousers-menu\"}},{\"children\"\n" +
                ":[],\"props\":{\"style\":\"\",\"title\":\"Sarees & Blouses\",\"url\":\"/sarees-and-blouses-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Dress Material\",\"url\":\"/dress-\n" +
                "material?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Lehenga Choli\",\"url\":\"/lehenga-choli?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Dupattas & \n" +
                "Shawls\",\"url\":\"/dupatta-shawl-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Jackets & Waistcoats\",\"url\":\"/ethnic-jacket-waistcoat-menu?src=tNav\"}}],\"props\n" +
                "\":{\"style\":\"\",\"title\":\"Indian & Fusion Wear\",\"url\":\"/women-indian-fusion-wear-menu?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Dresses & Jumpsui\n" +
                "ts\",\"url\":\"/dresses-and-jumpsuits-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Tops, T-Shirts & Shirts\",\"url\":\"/tops-tees-menu?src=tNav\"}},{\"children\":[]\n" +
                ",\"props\":{\"style\":\"\",\"title\":\"Jeans & Jeggings\",\"url\":\"/women-jeans-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Trousers & Capris\",\"url\":\"/women-western\n" +
                "-bottomwear-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Shorts & Skirts\",\"url\":\"/western-skirts-shorts-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\n" +
                "\"\",\"title\":\"Shrugs\",\"url\":\"/women-shrug?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sweaters & Sweatshirts\",\"url\":\"/sweaters-and-sweatshirts-women-menu?src=t\n" +
                "Nav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Jackets & Waistcoats\",\"url\":\"/western-jackets-waistcoats-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"\n" +
                "Coats & Blazers\",\"url\":\"/blazers-and-coats-women-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Western Wear\",\"url\":\"/women-western-wear-menu?src=tNav\"}},{\"children\":[{\"\n" +
                "children\":[],\"props\":{\"style\":\"\",\"title\":\"Bras & Lingerie Sets\",\"url\":\"/bras-and-sets-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Briefs\",\"url\":\"/briefs\n" +
                "-women-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Shapewear\",\"url\":\"/shapewear-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sleepwear &\n" +
                " Loungewear\",\"url\":\"/sleep-and-lounge-wear-women-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Swimwear\",\"url\":\"/swimwear-women-menu?src=tNav\"}},{\"childre\n" +
                "n\":[],\"props\":{\"style\":\"\",\"title\":\"Camisoles & Thermals\",\"url\":\"/camisoles-and-thermals-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Lingerie & Sleepwear\",\"url\":\"/wome\n" +
                "n-lingerie-sleepwear?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Flats & Casual Shoes\",\"url\":\"/flats-and-casual-shoes-menu?src=tNav\"}},{\"childre\n" +
                "n\":[],\"props\":{\"style\":\"\",\"title\":\"Heels\",\"url\":\"/women-heels?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Shoes & Floaters\",\"url\":\"/sports-footwear-wo\n" +
                "men-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Footwear\",\"url\":\"/women-footwear?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Clothing\",\"url\":\n" +
                "\"/women-sports-allclothing-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Footwear\",\"url\":\"/women-sports-shoes-menu?src=tNav\"}},{\"children\":[],\"props\":{\"st\n" +
                "yle\":\"\",\"title\":\"Accessories\",\"url\":\"/sports?f=categories%3ABackpacks%2CBag%2CDuffel%2520Bag%2CGloves%2CHeadband%2CHeadphones%2CSports%2520Accessories%2CSunglasses%2CSwi\n" +
                "mwear%2520Accessories%2CWater%2520Bottle%2CWristbands%3A%3Agender%3Amen%2520women%2Cwomen\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Equipment\",\"url\":\"/sports\n" +
                "?f=categories%3ABadminton%2520Racquets%2CBadminton%2520Shuttlecocks%2CBasketball%2CCricket%2520Bats%2CFootball%2CFootballs%2CTable%2520Tennis%2520Bats%2CTable%2520Tennis\n" +
                "%2520Kits%2CTennis%2520Balls%2CTennis%2520Kits%2CTennis%2520Racquets\"}}],\"props\":{\"style\":\"\",\"title\":\"Sports & Active Wear\",\"url\":\"/sports?userQuery=true&f=categories%3A\n" +
                "Socks%2CSports%2520Shoes%2CTshirts%2CWristbands%3A%3Agender%3Amen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cmen%2520women%2Cwomen&src=tNav\"}},{\"childre\n" +
                "n\":[],\"props\":{\"style\":\"\",\"title\":\"Handbags, Bags & Wallets\",\"url\":\"/handbags-and-bags-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Watches & Wearables\",\n" +
                "\"url\":\"/women-watches-wearables-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sunglasses & Frames\",\"url\":\"/sunglasses-and-frames-women-menu?src=tNav\"}},{\"children\"\n" +
                ":[],\"props\":{\"style\":\"\",\"title\":\"Luggage & Trolleys\",\"url\":\"/luggage-and-trolley-bags-menu?f=gender%3Amen%2520women%2Cwomen?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\n" +
                "\",\"title\":\"Cosmetics & Personal Care\",\"url\":\"/women-personal-care?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Belts\",\"url\":\"/women-belts-menu?sr\n" +
                "c=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Scarves, Stoles & Gloves\",\"url\":\"/women-gloves-mufflers-scarves-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\"\n" +
                ",\"title\":\"Caps & Hats\",\"url\":\"/women-caps-hats-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Hair Accessories\",\"url\":\"/women-hair-accessory-menu?src=tNav\"\n" +
                "}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Socks\",\"url\":\"/women-socks-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Headphones\",\"url\":\"/headphones-men\n" +
                "u?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Travel Accessories\",\"url\":\"/women-travel-accessory-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\n" +
                "\"Phone Cases\",\"url\":\"/mobile-case-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Fashion Accessories\",\"url\":\"/women-accessories?src=tNav\"}},{\"children\":[{\"children\":[],\"\n" +
                "props\":{\"style\":\"\",\"title\":\"Boutique & Fashion Jewellery\",\"url\":\"/jewellery-women-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Precious Jewellery\",\"url\":\n" +
                "\"/precious-jewellery?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Jewellery\",\"url\":\"/jewellery-women-menu?src=tNav\"}}],\"props\":{\"meta\":\"{\\\"dock\\\":\\\"true\\\",\\\"template\\\":\\\"fa\n" +
                "shion\\\",\\\"template_config\\\":{\\\"position\\\":3,\\\"color\\\":\\\"#fb56c1\\\",\\\"icon\\\":\\\"http://myntra.myntassets.com/assets/premium/temp/women_2.png\\\"}}\",\"style\":\"\",\"title\":\"Women\"\n" +
                ",\"url\":\"/shop/women\"}},{\"children\":[{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"T-Shirts\",\"url\":\"/boys-tshirts-menu?src=tNav\"}},{\"children\":[],\"props\":{\"sty\n" +
                "le\":\"\",\"title\":\"Shirts\",\"url\":\"/boys-shirts-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Jeans & Trousers\",\"url\":\"/boys-jeans-trousers-menu?src=tNav\"}},{\n" +
                "\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Shorts & Dungarees\",\"url\":\"/boys-shorts-dungarees-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Track Pants & P\n" +
                "yjamas\",\"url\":\"/boys-trackpants-pyjamas-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Clothing Sets\",\"url\":\"/boys-clothing-sets-menu?src=tNav\"}},{\"childre\n" +
                "n\":[],\"props\":{\"style\":\"\",\"title\":\"Indian Wear\",\"url\":\"/boys-indianwear-menu?f=gender%3Aboys%2Cboys%2520girls?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Swe\n" +
                "aters, Sweatshirts & Jackets\",\"url\":\"/boys-sweaters-jackets-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Rompers & Sleepwear\",\"url\":\"/boys-inner-sleepwea\n" +
                "r-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Boys Clothing\",\"url\":\"/boys-clothing-wsite?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Dresses\"\n" +
                ",\"url\":\"/girls-dresses-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Tops & T-Shirts\",\"url\":\"/girls-tops-tshirts-menu?src=tNav\"}},{\"children\":[],\"props\":{\n" +
                "\"style\":\"\",\"title\":\"Clothing Sets\",\"url\":\"/girls-clothing-set-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Indian Wear\",\"url\":\"/girls-indianwear-menu?src\n" +
                "=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Skirts, Shorts & Jumpsuits\",\"url\":\"/girls-skirts-shorts-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\n" +
                "\"Tights & Leggings\",\"url\":\"/girls-leggigns-tights-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Jeans, Trousers & Capris\",\"url\":\"/girls-jeans-trousers-cap\n" +
                "ris-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Track Pants\",\"url\":\"/girls-trackpants-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sweat\n" +
                "ers, Sweatshirts & Jackets\",\"url\":\"/girls-sweaters-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Rompers & Sleepwear\",\"url\":\"/girls-inner-sleepwear-menu?s\n" +
                "rc=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Girls Clothing\",\"url\":\"/girls-clothing-wsite?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Casual Shoes\",\n" +
                "\"url\":\"/boys-casual-shoes-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Shoes\",\"url\":\"/boys-sports-shoes-menu?src=tNav\"}},{\"children\":[],\"props\":{\"\n" +
                "style\":\"\",\"title\":\"Sandals & Flip flops\",\"url\":\"/boys-flip-flops-sandals-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Boys Footwear\",\"url\":\"/boys-footwear?src=tNav\"}},\n" +
                "{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Flats & Casual Shoes\",\"url\":\"/girls-flats-casual-shoes-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Heels\n" +
                "\",\"url\":\"/girls-heels-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sports Shoes\",\"url\":\"/girls-sports-shoes-menu?src=tNav\"}},{\"children\":[],\"props\":{\"sty\n" +
                "le\":\"\",\"title\":\"Sandals & Flip flops\",\"url\":\"/girls-flip-flops-sandals-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Girls Footwear\",\"url\":\"/girls-footwear?src=tNav\"}},\n" +
                "{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Bags & Backpacks\",\"url\":\"/kids-accessories-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Watches\"\n" +
                ",\"url\":\"/kids-watches-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Jewellery & Hair Accessories\",\"url\":\"/kids-jewellery-hair-accessories-menu?src=tNav\"}}\n" +
                ",{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sunglasses & Frames\",\"url\":\"/kids-sunglasses-frames-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Kids Accessories\",\"url\":\"\n" +
                "/Kids-Accessories?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Mothercare\",\"url\":\"/mothercare-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\n" +
                "\"Gini and Jony\",\"url\":\"/gini-and-jony-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"The Children's Place\",\"url\":\"/the-childrens-place\"}},{\"children\":[],\"props\":{\"s\n" +
                "tyle\":\"\",\"title\":\"United Colors of Benetton\",\"url\":\"/united-colors-of-benetton-kids-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"YK\",\"url\":\"/yk-menu\"}},{\"children\n" +
                "\":[],\"props\":{\"style\":\"\",\"title\":\"Allen Solly Junior\",\"url\":\"/allen-solly-junior-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Mango Kids\",\"url\":\"/mango-kids-menu\"\n" +
                "}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Marks & Spencer\",\"url\":\"/marks-&-spencer-kids-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Tommy Hilfiger\",\"url\":\"/\n" +
                "tommy-hilfiger-kids-menu\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"People\",\"url\":\"/people-kids-menu\"}}],\"props\":{\"style\":\"\",\"title\":\"Brands\",\"url\":\"/kids\"}}],\"props\n" +
                "\":{\"meta\":\"{\\\"dock\\\":\\\"true\\\",\\\"template\\\":\\\"fashion\\\",\\\"template_config\\\":{\\\"position\\\":3,\\\"color\\\":\\\"#f26a10\\\",\\\"icon\\\":\\\"http://myntra.myntassets.com/assets/premium/t\n" +
                "emp/kids.png\\\"}}\",\"style\":\"\",\"title\":\"Kids\",\"url\":\"/shop/kids\"}},{\"children\":[{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Bedsheets\",\"url\":\"/home-furnishing\n" +
                "-bedsheets-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Bedding Sets\",\"url\":\"/home-furnishing-bedding-sets-menu?src=tNav\"}},{\"children\":[],\"props\":{\"styl\n" +
                "e\":\"\",\"title\":\"Blankets Quilts and Dohars\",\"url\":\"/home-furnishing-blankets-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Pillows & Pillow Covers\",\"url\":\"\n" +
                "/home-furnishing-pillows-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Bed Linen & Furnishing\",\"url\":\"/home-furnishing-bed-linen-menu?src=tNav\"}},{\"children\":[{\"childre\n" +
                "n\":[],\"props\":{\"style\":\"\",\"title\":\"Bath Towels\",\"url\":\"/home-furnishing-bath-towels-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Hand and Face Towels\",\"u\n" +
                "rl\":\"/home-furnishing-hand-face-towels-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Beach Towels\",\"url\":\"/home-furnishing-beach-towels-menu?src=tNav\"}},{\n" +
                "\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Towels Set\",\"url\":\"/home-furnishing-towel-set-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Bath Rugs\",\"url\":\"/\n" +
                "home-furnishing-bath-rugs-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Bath Robes\",\"url\":\"/home-furnishing-bath-robes-menu?src=tNav\"}}],\"props\":{\"style\":\n" +
                "\"\",\"title\":\"Bath\",\"url\":\"/home-furnishing-bath-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Curtains \",\"url\":\"/home-furnishing-curtains-menu?src=tNav\"}},\n" +
                "{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Cushions & Cushion Covers\",\"url\":\"/home-furnishing-cushions-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Door\n" +
                " Mats\",\"url\":\"/home-furnishing-doormats-menu?src=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Table Covers\",\"url\":\"/home-furnishing-table-covers-menu\n" +
                "?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Table Runners, Mats & Napkins\",\"url\":\"/home-furnishing-table-napkins-menu?src=tNav\"}},{\"children\":[],\"props\":{\"s\n" +
                "tyle\":\"\",\"title\":\"Kitchen\",\"url\":\"/home-furnishing-kitchen-menu?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Kitchen & Table\",\"url\":\"/home-furnishing-kitchen-table-menu?src\n" +
                "=tNav\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Vases\",\"url\":\"/home-decor-vases-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Showpieces\n" +
                "\",\"url\":\"/home-decor-showpieces-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Wall Décor\",\"url\":\"/home-decor-wall-decor-menu?src=tNav\"}},{\"children\":[],\"p\n" +
                "rops\":{\"style\":\"\",\"title\":\"Candles & Candle Holders\",\"url\":\"/home-decor-candles-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Wall Decals & Stickers\",\"url\n" +
                "\":\"/home-furnishing-decals-stickers\"}}],\"props\":{\"style\":\"\",\"title\":\"Home Décor\",\"url\":\"/home-decor-menu?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Home Gif\n" +
                "t sets\",\"url\":\"/home-gift-sets\"}},{\"children\":[{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Bombay Dyeing\",\"url\":\"/home-furnishing-menu?src=tNav&f=brands%3ABOMBAY%2520DYE\n" +
                "ING\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Spaces\",\"url\":\"/home-furnishing-menu?src=tNav&f=brands%3ASPACES\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Portico\n" +
                " New York\",\"url\":\"/portico-new-york?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Swayam\",\"url\":\"/home-furnishing-menu?src=tNav&f=brands%3ASWAYAM\"}},{\"children\n" +
                "\":[],\"props\":{\"style\":\"\",\"title\":\"Raymond Home\",\"url\":\"/raymond-home?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Trident\",\"url\":\"/trident?src=tNav\"}},{\"child\n" +
                "ren\":[],\"props\":{\"style\":\"\",\"title\":\"Cortina\",\"url\":\"/cortina?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Athome by Nilkamal\",\"url\":\"/athome-by-nilkamal?src=\n" +
                "tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"WELHOME\",\"url\":\"/welhome?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Tangerine\",\"url\":\"/tangerine?src=tNa\n" +
                "v\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"Sej by Nisha Gupta\",\"url\":\"/sej-by-nisha-gupta?src=tNav\"}},{\"children\":[],\"props\":{\"style\":\"\",\"title\":\"House This\",\"url\"\n" +
                ":\"/house-this?src=tNav\"}}],\"props\":{\"style\":\"\",\"title\":\"Brands\",\"url\":\"/home-furnishing?src=tNav\"}}],\"props\":{\"meta\":\"{\\\"dock\\\":\\\"true\\\",\\\"template\\\":\\\"fashion\\\",\\\"templ\n" +
                "ate_config\\\":{\\\"position\\\":3,\\\"color\\\":\\\"#f2c210\\\",\\\"icon\\\":\\\"http://myntra.myntassets.com/assets/premium/temp/Men.png\\\"}}\",\"style\":\"\",\"title\":\"Home & Living\",\"url\":\"/ho\n" +
                "me-furnishing-menu?src=tNav&sort=new\"}}],\"props\":{\"title\":\"web.v0.top\"}};\n" +
                "        </script>\n" +
                "        <script src=\"http://myntra.myntassets.com/web/assets/js/main.4357a8728b41ed769a04.js.gz\"></script>\n" +
                "      </body>\n" +
                "    </html>";

        String[] subStr = html.split("window.__myx_seo__ = \\[\\[");
        System.out.println(subStr.length);
    }

    @Test
    public void testUrlDecode() {
        String url = "https%3A%2F%2Fwww.tatacliq.com%2Felectronics-accessories-covers-cases%2Fc-msh1222101%3Fq%3D%253Aprice-asc%253AisLuxuryProduct%253Afalse%253AisLuxuryProduct%253Afalse%253Aprice%253A%25E2%2582%25B90-%25E2%2582%25B999999999%26isFacet%3Dtrue%26facetValue%3D%25E2%2582%25B90-%25E2%2582%25B999999999";
        System.out.println(URLDecoder.decode(URLDecoder.decode(url)));
    }

    @Test
    public void testGetWebsite() {

        String aPackage = WebsiteHelper.getPackage(Website.UNKNOWN) == null ? "" : WebsiteHelper.getPackage(Website.UNKNOWN);

        String url = "https://www.mcdelivery.co.in/?utm_source=Hasof&utm_medium=cpc&utm_campaign=Hasof_cpo";
    }

    @Test
    public void testABTest() {
        String test = "%3B152479990235431";
        test = URLDecoder.decode(test);
        System.out.println(test);
    }

    @Test
    public void testYmd() {
        for (TaskLevel taskLevel : TaskLevel.values()) {
            System.out.println(taskLevel);
        }
    }

    @Test
    public void testMD5Url() {

        String url = "https://www.flipkart.com/honor-7-fantasy-silver-16-gb/p/itmebagjdxughuzh?pid=MOBEBAGJF5XGY4Y9";

        String md5Url = HexDigestUtil.md5(url);

        System.out.println(md5Url);
//        bd0a428c5ba4057f67a9fe6d88c7caff
//        bd0a428c5ba4057f67a9fe6d88c7caff
    }

    @Test
    public void testJson() {
        String json = "{\"sImg\":\"//img.dxcdn.com/productimages/sku_344792_2_smal.jpg\", \"mImg\":\"//img.dxcdn.com/productimages/sku_344792_2.jpg\", \"bImg\":\"//img.dxcdn.com/productimages/sku_344792_2.jpg\"}";

        JSONObject object = JSONObject.parseObject(json);

        String sImg = (String) object.get("sImg");
        String mImg = (String) object.get("mImg");
        String bImg = (String) object.get("bImg");
    }

    @Test
    public void beanTest() throws InvocationTargetException, IllegalAccessException {

        MobileCateDescription mobileCateDescription = new MobileCateDescription();

        Map<String, String> specMap = new LinkedHashMap<>();
        Map<String, String> newSpecMap = new LinkedHashMap<>();


        //General
        specMap.put("Launch Date", "1");
        specMap.put("Brand", "2");
        specMap.put("Model", "wdq");
        specMap.put("Operating System", "asdasdw");
        specMap.put("Custom UI", "189hd9");
        specMap.put("SIM Slot(s)", "*(@HH&@!");

        //Design
        specMap.put("Dimensions", "shfwihfewf");
        specMap.put("Weight", "!()@JF)!JF");
        specMap.put("Build Material", "N!*(H(!BV(!");

        //Display
        specMap.put("Screen Size", "*(h91hd1ndiaw");
        specMap.put("Screen Resolution", "N*(!H(*!V!#");
        specMap.put("Pixel Density", "afn89023nf2");

        //Performance
        specMap.put("Chipset", "afn923hf29fn");
        specMap.put("Processor", "123123");
        specMap.put("Architecture", "(!GBV(!BV");
        specMap.put("Graphics", "aohwf");
        specMap.put("RAM", "121eh198h9");

        //Storage
        specMap.put("Internal Memory", "acm8932fn92");
        specMap.put("Expandable Memory", ",.,lmoca");
        specMap.put("USB OTG Support", ",.?");

        //Camera
        specMap.put("MAIN CAMERA Resolution", "1231");
        specMap.put("MAIN CAMERA Sensor", "123123");
        specMap.put("MAIN CAMERA Autofocus", "13123");
        specMap.put("MAIN CAMERA Aperture", "sfsdf");
        specMap.put("MAIN CAMERA Optical Image Stabilisation", "asfaf");
        specMap.put("MAIN CAMERA Flash", "adfafd");
        specMap.put("MAIN CAMERA Image Resolution", "adfadfa");
        specMap.put("MAIN CAMERA Camera Features", "1231d1d1");
        specMap.put("MAIN CAMERA Video Recording", "12d1d21d");
        specMap.put("FRONT CAMERA Resolution", "1d12d1");
        specMap.put("FRONT CAMERA Sensor", "1d1d1");
        specMap.put("FRONT CAMERA Autofocus", "d12d12d1");

        //Battery
        specMap.put("Capacity", "1d21d1");
        specMap.put("Type", "1d12d1");
        specMap.put("User Replaceable", "1d1d21");
        specMap.put("Quick Charging", "1d1d21");

        //Network&Connectivity
        specMap.put("SIM Size", "1d1d12d");
        specMap.put("Network Support", "13c4f5f");
        specMap.put("VoLTE", "2f2f424f");
        specMap.put("SIM 1", "f3f2f4");
        specMap.put("SIM 2", "f24f2f");
        specMap.put("Bluetooth", "f2424");
        specMap.put("GPS", "f2f423");
        specMap.put("NFC", "f24f2");
        specMap.put("USB Connectivity", "2f42f4");

        //Multimedia
        specMap.put("FM Radio", "f2f423");
        specMap.put("Loudspeaker", "2f42f4");
        specMap.put("Audio Jack", "2f42f2");

        //Special Features
        specMap.put("Fingerprint Sensor", "2f2f");
        specMap.put("Fingerprint Sensor Position", "2f2342");
        specMap.put("Other Sensors", "f2f423f3");

        for (Map.Entry<String, String> entry : specMap.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            key = key.replaceAll(" ", "_").replace("(s)", "").toLowerCase();

            newSpecMap.put(key, value);
        }

        BeanUtils.populate(mobileCateDescription, newSpecMap);

        System.out.println(mobileCateDescription);

    }

    @Test
    public void testDiscount() {
        float newPrice = 54090;
        float oriOrice = 54169;

        int i = (int) ((1 - newPrice / oriOrice) * 100);

        System.out.println(i);
    }

    @Test
    public void testFloatToInt() {

        float price = 1231.123f;

        System.out.println((int) price);

    }

    @Test
    public void testuuid() {

        String url = "https://www.amazon.com/gp/product/B000MXKMG2/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B000MXKMG2&linkCode=as2&tag=ascsubtag-20&linkId=0d86b0ec42f12cc0ed8f9e9c58965ade";

        url = url.replace("/gp/product/", "/dp/");
        String[] urlParamArray = url.split("/dp/");
        String sourceIdString = urlParamArray[1];
        url = "https://www.amazon.com/dp/" + sourceIdString.substring(0, sourceIdString.indexOf("/")) + "/";

        System.out.println(url);
    }

    @Test
    public void test() {

        String json = "{\n" +
                "    \"records\": [\n" +
                "        {\n" +
                "            \"domain\": \"hotel.elong.com \\n\\n\",\n" +
                "            \"VIP\": \"211.151.110.32\",\n" +
                "            \"aos_node\": 144\n" +
                "        },\n" +
                "        {\n" +
                "            \"domain\": \"hotel.elong.com \\n\\n\",\n" +
                "            \"VIP\": \"123.59.30.10\",\n" +
                "            \"aos_node\": 156\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        JSONObject jsonObject = JSONObject.parseObject(json);

        JSONArray jsonArray = jsonObject.getJSONArray("records");

        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject jsonObject1 = jsonArray.getJSONObject(i);

            String domain = jsonObject1.getString("domain");
            String vipCode = jsonObject1.getString("VIP");
            int aos_code = jsonObject1.getIntValue("aos_node");

            System.out.println(domain + "_" + vipCode + "_" + aos_code);
            System.out.println();

        }

    }

    @Test
    public void test12() {

        String oldUrl = "http://www.amazon.in/gp/offer-listing/B01BK92AMK";

        String newUrl = oldUrl.replace("gp/offer-listing", "dp");

        System.out.println(oldUrl);
        System.out.println(newUrl);

    }

    @Test
    public void test11() {

        String a = "&nbsp;";

        String s = StringUtils.filterAndTrim(a, Arrays.asList("&nbsp;"));

        System.out.println(s);

    }

    @Test
    public void test10() {

        String url = "{\"Transfer Speed\\\":\\\"Read 18.62 MB/sec, Write 4.02 MB/sec\\\",\\\"Part Number\\\":\\\"Cruzer Blade 8gb\\\",\\\"Color\\\":\\\"Multicolor\\\",\\\"Dimensions\\\":\\\"7.4 mm x 17.6 mm x 41.5 mm\\\",\\\"Encryption\\\":\\\"128-bit AES Encryption\\\",\\\"Weight\\\":\\\"2.50 g\\\",\\\"Brand\\\":\\\"SanDisk\\\",\\\"USB on the go\\\":\\\"No\\\",\\\"Form Factor\\\":\\\"USB Flash Drive\\\",\\\"Type\\\":\\\"Utility Pendrive\\\",\\\"Model\\\":\\\"SDCZ50-008G-I35\\\",\\\"Features\\\":\\\"<p>Ultra Portable, Small Size</p> <p>Feather light</p> <p>Smart and stylish</p>\\\",\\\"Case Material\\\":\\\"Plastic\\\",\\\"Capacity (GB)\\\":\\\"8 GB\\\",\\\"Interface\\\":\\\"USB 2.0\\\"}";

        url = StringEscapeUtils.unescapeHtml(url);

        System.out.println(url);

    }

    @Test
    public void test9() {

        String phone = "035";

        String[] split = phone.split(",");

        System.out.println(split);

    }

    @Test
    public void testStr1() {

        String str = "http%3A%2F%2Fwww.ebay.in%2Fitm%2FMOTO-E3-POWER-16GB-2GB-RAM-8MP-2MP-4G-LTE-3500-MAH-PHONE-BLACK-%2F252594992971%3Fhash=item3acfd5b74b%3Ag%3AQ4oAAOSwal5YCJBG%26aff_source=dgm";

        str = StringUtils.urlDecode(str);

        System.out.println(str);

    }

    @Test
    public void testStr2() {

        String str = "Layer'r Shot Compact Explode And Impact Body Spray (Pack Of 2) Combo Set (Set of 2)";

        String str1 = HexDigestUtil.md5(StringUtils.getCleanChars(str));

        System.out.println(str1);

    }


    @Test
    public void testStr3() {

        String str = "Adraxx SM401098 Digital Speedometer (NA X6)";
        Website website = Website.FLIPKART;

        System.out.println(HexDigestUtil.md5(website.name() + StringUtils.getCleanChars(str)));
    }

    @Test
    public void testStr4() {

        String str = "545";

        System.out.println(str.toLowerCase());

    }


    @Test
    public void md5Str() {
        String str = "Luckie1985";

        String s = HexDigestUtil.md5(str);

        System.out.println(s);
    }

    @Test
    public void test5() {
        Long aLong = new Long(TimeUtils.now());
        int i = aLong.intValue();
        int i1 = i % 30;
        System.out.println();
    }

    @Test
    public void test6() {
        Long aLong = new Long(TimeUtils.now());
        long i = aLong.longValue();
        long i1 = i % 30;
        System.out.println();
    }

    @Test
    public void test7() {

        String url = "http://www.amazon.in/XOLO-Q1000s-Plus-Xolo-White/dp/B00PUCPQGQ";

        String[] substr = url.split("/dp/");

        String result = "http://www.amazon.in/gp/offer-listing/" + substr[1];
        System.out.println(substr[0]);
        System.out.println(substr[1]);
        System.out.println(result);
    }

    @Test
    public void test8() {

        Pattern pattern = Pattern.compile("[0-9,+,-]*");

        String phone = "0351-+5486468";

        Matcher matcher = pattern.matcher(phone);

        System.out.println(matcher.matches());

    }
}

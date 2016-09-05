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
                "https://www.amazon.com/MJ-Soffe-Mens-Running-Short/dp/B003AU5W5K/ref=pd_sim_193_5?ie=UTF8&refRID=23W5ZFXAB184416YVDGT",
                "https://www.amazon.com/Time-Mens-Pace-Running-Short/dp/B008PFJ9EU/ref=pd_sim_200_2?ie=UTF8&refRID=G79RY4CFTFTFHNN5D7W3",
                "https://www.amazon.com/Depot-Unisex-Cotton-Herringbone-Fedora/dp/B01J6JUNCK/ref=pd_sim_193_4?ie=UTF8&refRID=7GHD8QJ091X7HJK0KY7D",
                "https://www.amazon.com/City-Hunter-Cotton-Trilby-Fedora/dp/B008N24O4A/ref=pd_sim_193_2?ie=UTF8&refRID=NKN156W0Z0F800TQV4VW",
                "https://www.amazon.com/City-Hunter-Cotton-Trilby-Fedora/dp/B008N24O4A/ref=pd_sim_193_3?ie=UTF8&refRID=98XSDYX1A05QT8V997JW",
                "https://www.amazon.com/Solid-Band-Summer-Straw-Fedora/dp/B01DHRIIVA/ref=pd_sim_193_5?ie=UTF8&refRID=6KP48GH67MPP2QRGFNR8",
                "https://www.amazon.com/JONES-Mens-Button-Dress-Shirts/dp/B01ABTB1VQ/ref=pd_sim_193_4?ie=UTF8&refRID=Q78XRMNVTFD7YJAF2GCG",
                "https://www.amazon.com/Shirts-Sleeve-Formal-Casual-CL6299/dp/B013G3I81A/ref=pd_sim_193_4?ie=UTF8&refRID=MV2CBJE9AMFJ8WC0BD2V",
                "https://www.amazon.com/localmode-Cotton-Sleeve-Plaid-Button/dp/B01EWMSWH4/ref=pd_sbs_193_2?ie=UTF8&refRID=7WYX71B1ZW6C0XB1TYK7",
                "https://www.amazon.com/Zicac-Mercerized-Cotton-Button-Shirts/dp/B019MTJYBA/ref=pd_sim_193_5?ie=UTF8&refRID=DYBY8KX2P70C5ZNWQ7RJ",
                "https://www.amazon.com/iPretty-Fashion-Contrast-Sleeve-Casual/dp/B01AJUDULQ/ref=pd_sim_193_4?ie=UTF8&refRID=YN0QJVVW4QYS4CBP6CD5",
                "https://www.amazon.com/PAUL-JONES-Sleeves-Casual-CL5248-49/dp/B00RNSNJH8/ref=pd_sim_193_4?ie=UTF8&refRID=A1SPAZ3RCX410RKBQQ1Q",
                "https://www.amazon.com/localmode-Cotton-Sleeve-Plaid-Button/dp/B01EWMSWH4/ref=pd_sim_193_3?ie=UTF8&refRID=DA51Y59QE029TZM0RJ5H",
                "https://www.amazon.com/Hanes-mens-Beefy-T®-Tall-518T/dp/B00KNWZMOE/ref=pd_sim_200_1?ie=UTF8&refRID=9X1ZZ1Z4KS67D55R3XKS",
                "https://www.amazon.com/gp/product/B00HT09WNK/ref=s9_acsd_al_bw_c_x_1?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=merchandised-search-8&pf_rd_r=85ZM36WCTZGSY1GYMMP6&pf_rd_t=101&pf_rd_p=2598495662&pf_rd_i=2476517011",
                "https://www.amazon.com/Seventies-Jersey-Burnout-V-Neck-T-Shirt/dp/B01BE679O8/ref=pd_sim_193_4?ie=UTF8&refRID=6QQ4TA28PNSE20K8A7CA",
                "https://www.amazon.com/Danzi-V-neck-Sports-Outdoor-T-shirt/dp/B01DUQRZQM/ref=pd_sim_193_4?ie=UTF8&refRID=BP8K627NTJTD3MVXTSPV",
                "https://www.amazon.com/Casual-Sleeve-Tri-Blend-Fitted-Cotton/dp/B01BN0NLQK/ref=pd_sim_193_3?ie=UTF8&refRID=EYK68HYZ3G5X389MXQHR",
                "https://www.amazon.com/Canvas-Mens-Triblend-V-Neck-T-Shirt/dp/B006ZS9B0G/ref=pd_sim_193_5?ie=UTF8&refRID=88ZNADAC8EDDCB9Z1KTE",
                "https://www.amazon.com/Levis-Mens-Harris-Baseball-Shirt/dp/B00V0G8SW0/ref=pd_sim_193_2?ie=UTF8&refRID=KNQ1SF4XMBJYMTCFD9XQ",
                "https://www.amazon.com/Levis-Mens-Shawger-Graphic-T-Shirt/dp/B01CO06X2C/ref=pd_sim_193_2?ie=UTF8&refRID=5ZNFSNZB4T2Z6DW4HG37",
                "https://www.amazon.com/Levis-Mens-General-Graphic-T-Shirt/dp/B01CO06SEU/ref=pd_sbs_193_4?ie=UTF8&refRID=FN39JYABW9AZGN0FS39G",
                "https://www.amazon.com/Southpole-Active-Fleece-Jogger-Pockets/dp/B013VU6P5O/ref=pd_sim_sbs_193_1?ie=UTF8&refRID=758YPWGRXTCK66ZEHJ41",
                "https://www.amazon.com/Southpole-Jogger-Active-Tricot-Fabric/dp/B00QV99HCQ/ref=pd_sbs_193_3?ie=UTF8&refRID=1CGH28F8TPGVS92251WZ",
                "https://www.amazon.com/Southpole-Active-Fleece-Jogger-Pockets/dp/B013VU6P5O/ref=cts_ap_2_vtp",
                "https://www.amazon.com/Starbay-Sunville-Shoes/dp/B00CYI5E8E/ref=cts_sh_2_vtp",
                "https://www.amazon.com/BERTELLI-Ultra-Cusioned-Flipflop-Sandal/dp/B01C3C0ZEI/ref=pd_sim_309_2?ie=UTF8&refRID=ZPNYKANM043Q1DF9KNJF",
                "https://www.amazon.com/Bertelli-New-Sandals-Bright-Bi-layered/dp/B00KAGVYYU/ref=pd_sim_309_4?ie=UTF8&refRID=VD9AMVX4VP6XG59TCSQP",
                "https://www.amazon.com/Bertelli-New-Classic-Sandal-Colors/dp/B00KAHVPZC/ref=pd_sim_309_2?ie=UTF8&refRID=2EHF51AKKH9PKTV07A5C",
                "https://www.amazon.com/Hanes-Mens-4-Pack-Assorted-A-Shirt/dp/B00LVJ8JPW/ref=pd_sbs_193_2?ie=UTF8&refRID=HS0JQCHMFZSANESDFFX2",
                "https://www.amazon.com/Andrew-Scott-Sleeveless-Muscle-Shirts/dp/B01HXB05DK/ref=pd_sbs_193_5?ie=UTF8&refRID=HS0JQCHMFZSANESDFFX2",
                "https://www.amazon.com/Kanu-Surf-Mens-Voyage-Trunks/dp/B015VLNDVU/ref=pd_sim_193_4?ie=UTF8&refRID=RFTY6N92C1WX02PWAKG1",
                "https://www.amazon.com/HEAD-Mens-Jackpot-Spacedye-Short/dp/B01AH12CKW/ref=pd_sim_193_2?ie=UTF8&refRID=HHWNFQG05K4ETAJP72S6",
                "https://www.amazon.com/HEAD-Mens-Efficient-Textured-Short/dp/B01AANW5MC/ref=pd_sim_193_2?ie=UTF8&refRID=PB6M6Z2QDJB4X913CFGV",
                "https://www.amazon.com/HEAD-Mens-Game-Performance-Short/dp/B01AH12CFC/ref=pd_sbs_193_5?ie=UTF8&refRID=8FVC9YY8XGF4B7C34FSC",
                "https://www.amazon.com/HEAD-Mens-Spacedye-Hypertek-Crew/dp/B01A63QVC6/ref=pd_sbs_193_3?ie=UTF8&refRID=95V81TFSDY2P2T5MWBAB",
                "https://www.amazon.com/Bentibo-Burgundy-Casual-Sleeve-Button/dp/B01FU8TLOC/ref=pd_rhf_dp_s_cp_3?ie=UTF8&pd_rd_i=B01FU8TLOC&pd_rd_r=3BT7GMYYWMSGPW7QYMQ5&pd_rd_w=OkVdy&pd_rd_wg=AyiPH&refRID=3BT7GMYYWMSGPW7QYMQ5",
                "https://www.amazon.com/Coofandy-Collar-Sleeve-Casual-Shirts/dp/B01DNAI5FU/ref=pd_sim_193_4?ie=UTF8&refRID=XEXYGMSRCCJTA7R2D0CT",
                "https://www.amazon.com/Eurosocks-Cool-Ghost-Black-X-Large/dp/B003L5CBFS/ref=sr_1_2522?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976432&sr=1-2522&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/ExOfficio-Give-n-Go-Brief-Hemlock-Large/dp/B00ZI5L1I2/ref=sr_1_4?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976873&sr=1-4&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Bell-7071185-Darth-Multisport-Helmet/dp/B018NKIWKI/ref=sr_1_12?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976873&sr=1-12&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Zippo-12-Hour-Warmer-Realtree-Camouflage/dp/B013HLGUDE/ref=sr_1_14?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976873&sr=1-14&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Sawyer-Products-SP714-Maxi-DEET-Repellent/dp/B005SO8JRO/ref=sr_1_16?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976873&sr=1-16&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/NeoSport-Neoprene-Snorkel-Low-top-Socks/dp/B00S0WMQU2/ref=sr_1_22?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976873&sr=1-22&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Pearl-Izumi-Womens-Elite-Gloves/dp/B00LXUFGP0/ref=sr_1_35?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976970&sr=1-35&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Serfas-Thorn-Proof-Schrader-20-Inch/dp/B007YNB71C/ref=sr_1_58?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976994&sr=1-58&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Zippo-Warmer-Harley-Davidson-Handwarmer/dp/B00JCPDB5A/ref=sr_1_74?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977006&sr=1-74&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Da-Vinci-Single-Cotton-Hammock/dp/B00GMT1UD4/ref=sr_1_130?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977032&sr=1-130&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/CamelBak-Forge-Travel-Ghost-12-Ounce/dp/B00TRCUF0W/ref=sr_1_170?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977056&sr=1-170&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Bell-Adult-Reflex-Helmet-Titanium/dp/B00TS3FWR6/ref=sr_1_210?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977067&sr=1-210&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/InStep-Weather-Shield-Single-Stroller/dp/B0043471HY/ref=sr_1_297?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977101&sr=1-297&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Zippo-Chinese-Fusion-Pocket-Lighter/dp/B012IHALTM/ref=sr_1_164?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977326&sr=1-164&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Princeton-Tec-Headlamp-Lumens-Green/dp/B0056TLVI0/ref=sr_1_305?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977379&sr=1-305&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Dakine-Womens-Alina-Travel-Dakota/dp/B016ZLZV74/ref=sr_1_346?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977404&sr=1-346&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Defeet-Aireator-Spotty-Socks-X-Large/dp/B00JHKZWI4/ref=sr_1_351?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977404&sr=1-351&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Bridgedale-Coolfusion-Speed-Trail-Socks/dp/B00HFKA9WW/ref=sr_1_467?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977455&sr=1-467&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Mad-Bomber-Supplex-Brown-Medium/dp/B0060YELV4/ref=sr_1_535?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977490&sr=1-535&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/4ZA-Womens-Cirrus-Saddle-Length/dp/B00GZMKHY6/ref=sr_1_609?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977521&sr=1-609&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Spyder-Boys-Fire-Black-Volcano/dp/B00S8NXIVY/ref=sr_1_637?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977534&sr=1-637&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Girls-Explore-Stripe-Medium/dp/B01CQ48TLO/ref=sr_1_713?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977582&sr=1-713&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Pearl-Izumi-Softshell-Glove-X-Small/dp/B00KJGMYSQ/ref=sr_1_721?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977599&sr=1-721&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Canari-Boulder-Jacket-Breakaway-Large/dp/B012Z41IMC/ref=sr_1_722?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977599&sr=1-722&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Avenir-Classic-Cloth-Handlebar-Tape/dp/B003BDKLZM/ref=sr_1_723?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977599&sr=1-723&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Lezyne-Femto-Drive-LED-Silver/dp/B008R5OLX2/ref=sr_1_724?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977599&sr=1-724&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Henschel-Mens-Bucket-Khaki-Large/dp/B016P1ZXS6/ref=sr_1_726?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977599&sr=1-726&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/LOLE-Womens-Large-Mandarino-Stripe/dp/B00KWKDBEA/ref=sr_1_727?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977599&sr=1-727&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Cressi-Leonardo-Computer-computer-included/dp/B00I556YZ2",
                "https://www.amazon.com/Life-Wander-Boots-Crusher-Cobalt/dp/B01594AIS0/ref=sr_1_739?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977599&sr=1-739&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Royal-Robbins-Mission-Lagoon-Large/dp/B00RW5JJU8/ref=sr_1_747?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-747&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Royal-Robbins-Womens-Shiva-Pullover/dp/B00LC8SMDG/ref=sr_1_748?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-748&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/ExOfficio-Flannel-Sleeve-Coffee-Medium/dp/B00RQWPHVW/ref=sr_1_749?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-749&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Crusher-Slate-Green-Small/dp/B00TVJQK3C/ref=sr_1_751?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-751&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Columbia-Womens-Sleeve-Hibiscus-Medium/dp/B00DQYU3WM/ref=sr_1_752?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-752&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/AV-35-Standard-Schrader-Valve-1-75-1-90-Inch/dp/B00DBPK3AI/ref=sr_1_754?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-754&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Dakine-Stashable-Cinchpack-Camo-19-Liter/dp/B00Q7N9EZQ/ref=sr_1_755?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-755&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Crusher-Mountain-T-Shirt-X-Small/dp/B00EA7LZ8U/ref=sr_1_756?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-756&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Optic-Edge-Frame-Sunglasses-Orange/dp/B006QFRDEO/ref=sr_1_757?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-757&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Womens-Azura-Tankini-Nights-X-Small/dp/B00WLHX75U/ref=sr_1_758?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-758&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Lorpen-Work-Coolmax-Socks-Medium/dp/B002JT0M44/ref=sr_1_760?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-760&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Mucky-Nutz-Bender-Fender-X-Large/dp/B009P28R3G/ref=sr_1_761?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-761&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Buffalo-Jackson-Trading-Co-Cottonwood/dp/B00TE1BTZG/ref=sr_1_764?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-764&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Womens-Argentina-Tankini-Wallflower-X-Small/dp/B0183MBSE8/ref=sr_1_765?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977693&sr=1-765&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Aeroskin-Polypropylene-Shorts-Stripes-Pocket/dp/B003TW5HA4/ref=sr_1_772?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977795&sr=1-772&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Oakley-Mens-Beanie-Copper-Canyon/dp/B00TS48IEY/ref=sr_1_778?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977795&sr=1-778&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Zippo-Hendrix-Pocket-Lighter-Street/dp/B019U5X5AW/ref=sr_1_779?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977795&sr=1-779&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Texsport-Aluminum-Percolator-Outdoor-Camping/dp/B001DZQYJW/ref=sr_1_782?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977795&sr=1-782&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Craft-Shield-Finger-Waterproof-Flumino/dp/B00XC860OW/ref=sr_1_789?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977795&sr=1-789&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/FU-R-Headwear-Striped-Fleece-Beanie/dp/B00UC5NXPM/ref=sr_1_791?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977795&sr=1-791&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/NP-Surf-Jigsaw-Rashguard-X-Large/dp/B00GXFYY9Y/ref=sr_1_800?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977861&sr=1-800&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Light-Fire-Replacement-FireSteel-FireKnife/dp/B00H4YBCPC/ref=sr_1_802?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977861&sr=1-802&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Hyperflex-ACCESS-3mm-Round-Boot/dp/B00IMJA6PU/ref=sr_1_803?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977861&sr=1-803&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Crusher-Range-Nantucket-Small/dp/B00M0DITX0/ref=sr_1_809?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977861&sr=1-809&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Mountain-Khakis-Shoreline-Sleeve-X-Large/dp/B00LUXXF12/ref=sr_1_805?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977861&sr=1-805&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Ibex-Outdoor-Clothing-Mosaic-Kohlrabi/dp/B011YQ8CZ8/ref=sr_1_819?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977896&sr=1-819&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Mountain-Khakis-Genevieve-Classic-Regular/dp/B00S5P7LJA/ref=sr_1_820?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977896&sr=1-820&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/KAVU-Womens-Skort-Terrain-Medium/dp/B0113P9XD0/ref=sr_1_824?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977896&sr=1-824&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Merrell-Womens-Placket-Bourbon-Equinox/dp/B00I3Q2IA8/ref=sr_1_823?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977896&sr=1-823&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/KAVU-Mens-Shirt-Blaze-Small/dp/B00XK453ZU/ref=sr_1_825?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977896&sr=1-825&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Womens-Crusher-Jackie-T-Shirt/dp/B00EA7KKW2/ref=sr_1_829?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977896&sr=1-829&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Places-Motorcycle-Crusher-Medium/dp/B01593ZR3M/ref=sr_1_827?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977896&sr=1-827&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Eurosocks-0122-DryStat-Mountaineering-X-Large/dp/B0089HSVB6/ref=sr_1_845?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977947&sr=1-845&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Dakine-Jax-Beanie-Burgundy-Size/dp/B00I84JV0K/ref=sr_1_855?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977947&sr=1-855&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Womens-Night-Black-Medium/dp/B00LV4VDSC/ref=sr_1_857?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977947&sr=1-857&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Mountain-Khakis-Rendezvous-Micro-XX-Large/dp/B00S0Z4B5C/ref=sr_1_861?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977947&sr=1-861&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Unplug-Rainbow-X-Large-Copper/dp/B01599KQ5A/ref=sr_1_863?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472977947&sr=1-863&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Dakine-Mens-Cutter-Beanie-Charcoal/dp/B00W5WXA7G/ref=sr_1_890?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978008&sr=1-890&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-Sleeve-Newbury-Simplify-Night/dp/B00TVJRG8U/ref=sr_1_897?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978008&sr=1-897&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Mountain-Khakis-Flannel-Engine-Medium/dp/B00KLXON6I/ref=sr_1_899?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978008&sr=1-899&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Selle-Royal-Womens-Moderate-Saddle/dp/B00GBV287U/ref=sr_1_900?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978008&sr=1-900&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/ExOfficio-Bugsaway-Borders-Short-Slate/dp/B00TB0I2MI/ref=sr_1_901?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978008&sr=1-901&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Oakley-Mainline-Beanie-Copper-Canyon/dp/B00TS47BGA/ref=sr_1_904?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978008&sr=1-904&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/KAVU-Klear-Above-Shirt-X-Large/dp/B016BTIBAO/ref=sr_1_916?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978047&sr=1-916&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Canari-Essential-Jersey-Medium-Green/dp/B00UYZ0C4A/ref=sr_1_918?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978047&sr=1-918&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Canari-Avalanche-Sleeve-Jersey-X-Large/dp/B012Z48U00/ref=sr_1_926?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978047&sr=1-926&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Liquid-Logic-Square-Bottle-28-Ounce/dp/B001G7QST8/ref=sr_1_945?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978082&sr=1-945&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Craghoppers-Interactive-Jacket-X-Large-Pepper/dp/B00D9LALZG/ref=sr_1_949?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978082&sr=1-949&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Screamer-Pinline-Beanie-Charcoal-Orange/dp/B00LL4Y1H2/ref=sr_1_966?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978098&sr=1-966&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Stansport-Vinyl-Poncho-Olive-52x80-Inch/dp/B0067MOVBO/ref=sr_1_962?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978098&sr=1-962&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Black-Diamond-Headlamp-Coral-Pink/dp/B00PRQ0E8K/ref=sr_1_971?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978098&sr=1-971&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Nemo-2968-Equipment-Espri-Pawprint/dp/B0035ENP3C/ref=sr_1_974?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978098&sr=1-974&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Body-Glove-Five-Finger-Small/dp/B004E95GAC/ref=sr_1_983?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978098&sr=1-983&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Columbia-Silver-Ridge-Plaid-Sleeve/dp/B00L1R6HQ2/ref=sr_1_990?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978147&sr=1-990&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Royal-Robbins-Vista-Sleeve-XX-Large/dp/B010161TMU/ref=sr_1_996?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978147&sr=1-996&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Sector-Junior-Pursuit-Protective-X-Large/dp/B00KM5IA00/ref=sr_1_1027?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978173&sr=1-1027&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/ICEBREAKER-Sierra-Gloves-Infinity-X-Large/dp/B00KXU0PCA/ref=sr_1_1055?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978208&sr=1-1055&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Pearl-Izumi-Journey-Shadow-Small/dp/B00F1JU87U/ref=sr_1_1066?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978230&sr=1-1066&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Industries-Lightweight-Mountain-Sleeve-X-Small/dp/B00XATD758/ref=sr_1_1084?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978245&sr=1-1084&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/DECKY-Neon-Jeep-Cap-Yellow/dp/B00L534TY4/ref=sr_1_1123?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978271&sr=1-1123&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/HippyTree-Homebrew-Small-Heather-Charcoal/dp/B018AAMDT2/ref=sr_1_1170?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978297&sr=1-1170&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/PZ-Racing-CR5-3S-Al7075-Coating/dp/B00EP6JZ42/ref=sr_1_1183?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978316&sr=1-1183&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Heat-Factory-Deluxe-Balaclava-Pockets/dp/B0049HU2HG/ref=sr_1_1221?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978333&sr=1-1221&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Dakine-Kids-Tracker-Gloves-Large/dp/B00ZRQH29O/ref=sr_1_1231?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978368&sr=1-1231&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Celtek-Mens-Meltdown-Face-Black/dp/B00UL1NRMG/ref=sr_1_1247?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978368&sr=1-1247&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Juniper-Taslon-Folding-Bill-Khaki/dp/B00KU3FXKO/ref=sr_1_1297?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978422&sr=1-1297&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/BONEShieldz-Tarmac-Combo-Protective-Youth/dp/B00AEBI8XQ/ref=sr_1_1371?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978475&sr=1-1371&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Manzella-Womens-Ranger-Gloves-Infinity/dp/B00EV66FRQ/ref=sr_1_1418?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978498&sr=1-1418&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Aervoe-Surveyor-Safety-Florida-Yellow/dp/B008APX3NS/ref=sr_1_1433?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978498&sr=1-1433&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Oakley-Mens-Rockslide-Beanie-Aurora/dp/B00TS48M4A/ref=sr_1_1511?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978600&sr=1-1511&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Eurosocks-Marathon-Supreme-Black-Large/dp/B000NA5HWS/ref=sr_1_1625?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978683&sr=1-1625&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-good-Cloud-White-Medium/dp/B00M0CKDUS/ref=sr_1_1637?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978714&sr=1-1637&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Life-good-Cloud-White-Large/dp/B00M15YRBK/ref=sr_1_1638?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978714&sr=1-1638&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Screamer-Mens-Brandon-Beanie-Black/dp/B00FMIY88U/ref=sr_1_1678?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978737&sr=1-1678&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Savant-Shorts-Sleeve-Black-Small/dp/B00LNCHGRE/ref=sr_1_1702?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978751&sr=1-1702&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Bullet-Wrist-Guard-Adult-Medium/dp/B001UE9DRQ/ref=sr_1_1738?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978784&sr=1-1738&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Cobrabraid-BMG-Machine-Gun-Bullet/dp/B00CR0PXGC/ref=sr_1_1917?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978902&sr=1-1917&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/MaxxDry-Studs-Black-Large-9-12/dp/B003A0LOOS/ref=sr_1_1964?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978947&sr=1-1964&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Outdoor-Foldable-Utensil-Stainless-Multi-function/dp/B017HQ207A/ref=sr_1_1980?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978971&sr=1-1980&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Stage-Graphic-Facemask-Diamond-Grill/dp/B00ME7JHXI/ref=sr_1_2029?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472978995&sr=1-2029&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Clockwork-Gears-Snake-Cycling-T-Shirt/dp/B00GZEZCL2/ref=sr_1_2056?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979054&sr=1-2056&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Victorinox-Classic-Chocolate-Clam-Multi-Tool/dp/B00JVYYL42/ref=sr_1_2116?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979106&sr=1-2116&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Canari-Cyclewear-Womens-Evolution-Breakaway/dp/B00H8LSFC4/ref=sr_1_2122?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979106&sr=1-2122&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Great-Eastern-Entertainment-Durarara-Messenger/dp/B007AAN7GM/ref=sr_1_2192?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979178&sr=1-2192&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Innate-Portal-Travel-Pouch-Charcoal/dp/B004XZ2ZSS/ref=sr_1_2237?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979225&sr=1-2237&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Highlander-Outdoor-Micro-Towel-Olive/dp/B008QW51KS/ref=sr_1_2259?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979238&sr=1-2259&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Skateboards-Baseball-Skateboard-32-Inch-14-5-Inch/dp/B00EFOKEJY/ref=sr_1_2323?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979277&sr=1-2323&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/ZtuntZ-Skateboards-Skateboard-32-Inch-14-5-Inch/dp/B00EFOC51E/ref=sr_1_2324?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979277&sr=1-2324&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Rip-Curl-Sleeve-Guard-Shirt/dp/B00CZ8SYSA/ref=sr_1_2345?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472979303&sr=1-2345&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000%2Cp_72%3A1248957011",
                "https://www.amazon.com/Shortsleeve-T-Shirt-Under-Armour-Midnight/dp/B00783KT9Y/ref=sr_1_1?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979526&sr=1-1&refinements=p_36%3A600-2000",
                "https://www.amazon.com/Under-Armour-V-Neck-T-Shirt-X-Large/dp/B00KWK1O30/ref=sr_1_2?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979526&sr=1-2&refinements=p_36%3A600-2000",
                "https://www.amazon.com/TM-R34-BO_X-Large-Tesla-Coldgear-Compression-Baselayer/dp/B01833X868/ref=sr_1_4?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979526&sr=1-4&refinements=p_36%3A600-2000",
                "https://www.amazon.com/TM-R15-GRKZ_X-Large-Tesla-Compression-Baselayer-Sleeveless/dp/B01ET2XHYK/ref=sr_1_7?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979526&sr=1-7&refinements=p_36%3A600-2000",
                "https://www.amazon.com/DRI-EQUIP-Moisture-Wicking-Athletic-T-Shirt-Black-L/dp/B01AQR07KY/ref=sr_1_10?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979526&sr=1-10&refinements=p_36%3A600-2000",
                "https://www.amazon.com/DRI-EQUIP-Moisture-Wicking-Athletic-Shirt-Large-Black/dp/B00LWHURDK/ref=sr_1_13?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979526&sr=1-13&refinements=p_36%3A600-2000",
                "https://www.amazon.com/Dri-Tek-Moisture-Wicking-Athletic-T-Shirt/dp/B00WARVEMO/ref=sr_1_15?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979526&sr=1-15&refinements=p_36%3A600-2000",
                "https://www.amazon.com/Russell-Athletic-Mens-Pocket-XX-Large/dp/B007JPMSEA/ref=sr_1_19?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979604&sr=1-19&refinements=p_36%3A600-2000",
                "https://www.amazon.com/Athletic-Dri-Fit-Lightweight-Workout-Running/dp/B01GWAKIQW/ref=sr_1_38?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979619&sr=1-38&refinements=p_36%3A600-2000",
                "https://www.amazon.com/Level-Rib-Knit-Sublimated-Muscle-X-Large/dp/B01A4C85SC/ref=sr_1_53?s=sports-and-fitness-clothing&ie=UTF8&qid=1472979639&sr=1-53&refinements=p_36%3A600-2000",
                "https://www.amazon.com/Survival-Emergencies-Waterproof-Compact-Comprehensive/dp/B015PJIVJM/ref=gbph_img_m-2_4942_01dc66e1?smid=A20NH3IT2Z71WT&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=G18B80YAKQKF6YZKM7TX",
                "https://www.amazon.com/VicTsing-Waterproof-Hands-free-Headlight-Rechargeable/dp/B01BXS901W/ref=gbph_img_m-2_4942_f8036f20?smid=A14L9DIA3NK9B0&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=G18B80YAKQKF6YZKM7TX",
                "https://www.amazon.com/Venture-Pal-Ultralight-Lightweight-Waterproof/dp/B01HBA8TWC/ref=gbph_img_m-2_4942_dec5b1f2?smid=A1VLG17X286KN7&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=FHKJ89YQ22GM0F3SQZZZ",
                "https://www.amazon.com/Active-Research%C2%AE-Headlight-Flashlight-Headlamp/dp/B01E80XYIM/ref=gbph_img_m-2_4942_8a7b8c6b?smid=A3OOXJ4HXOTX9Q&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=FHKJ89YQ22GM0F3SQZZZ",
                "https://www.amazon.com/Aodor-Running-Belt-Waist-Pack/dp/B01IN15JVM/ref=gbph_img_m-2_4942_8acad4c2?smid=A3S74LVEEWYLSN&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=FHKJ89YQ22GM0F3SQZZZ",
                "https://www.amazon.com/Skyline-Socks-Oregon-Orange-Black/dp/B01D08FO0O/ref=gbph_img_m-2_4942_ebb0c30e?smid=ATVPDKIKX0DER&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=FHKJ89YQ22GM0F3SQZZZ",
                "https://www.amazon.com/Bike-Saddle-Bag-Evecase-Strap/dp/B01AX9XOQO/ref=gbph_img_m-2_4942_52038bed?smid=A3E5K6GEFSI075&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=FHKJ89YQ22GM0F3SQZZZ",
                "https://www.amazon.com/Antishock-OuTera-Adjustable-Ultralight-Telescopic/dp/B01ACT4MTI/ref=gbph_img_m-2_4942_7f276ceb?smid=A2H38VIKKDKMJM&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=FHKJ89YQ22GM0F3SQZZZ",
                "https://www.amazon.com/Liveup-SPORTS-Support-Sporting-Varvious/dp/B01H3ANDLW/ref=gbph_img_m-2_4942_4d80293c?smid=A3W4OSAX1VMRR3&pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=FHKJ89YQ22GM0F3SQZZZ",
                "https://www.amazon.com/dp/B01IJ50P2A/ref=gbph_img_m-2_4942_8a32a966?pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=BAE4RS5VCZRGE555N9NZ",
                "https://www.amazon.com/dp/B01ECSCSK0/ref=gbph_img_m-2_4942_bfe3fd5d?pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=BAE4RS5VCZRGE555N9NZ",
                "https://www.amazon.com/dp/B00EAKCGRG/ref=gbph_img_m-2_4942_c4de3f91?pf_rd_p=2530804942&pf_rd_s=merchandised-search-2&pf_rd_t=101&pf_rd_i=9927316011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=BAE4RS5VCZRGE555N9NZ",
                "https://www.amazon.com/dp/B01EWIKXUW?psc=1",
                "https://www.amazon.com/dp/B016X939EU?psc=1",
                "https://www.amazon.com/Bright-Sales-Strip-Lantern-Pack/dp/B00BNAMGOY/ref=sr_1_8?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472971500&sr=1-8&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Ryder-Radian-Front-White-Light/dp/B00REGYVAS/ref=sr_1_13?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472971500&sr=1-13&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/M-Wave-Alloy-Water-Bottle-Yellow/dp/B004XSBVY4/ref=sr_1_57?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472971550&sr=1-57&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Ultimate-Survival-Technologies-Whistle-Bracelet/dp/B00B0D2F1S/ref=sr_1_101?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472971633&sr=1-101&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Sector-Patches-Leather-Brown-Medium/dp/B009ZCG3BO/ref=sr_1_221?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472972404&sr=1-221&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/M-Wave-Alloy-Water-Bottle-Cage/dp/B004XSBVVC/ref=sr_1_234?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472972404&sr=1-234&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Avenir-Urban-Watt-Headlight-1-LED/dp/B0047IJ9IU/ref=sr_1_309?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472972471&sr=1-309&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Ibera-Bicycle-iPhone-Mount-4-Inch/dp/B00AF3O3W8/ref=sr_1_359?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472972578&sr=1-359&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Tour-France-Limited-Collectors-Bottle/dp/B003TJQLGQ/ref=sr_1_382?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472972596&sr=1-382&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Tour-France-Limited-Bottle-25-Ounce/dp/B008CN1CRW/ref=sr_1_437?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472972726&sr=1-437&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Velo-ErgoGel-Handlebar-Grips-Combo/dp/B007Y5GTTA/ref=sr_1_444?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472972726&sr=1-444&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/BOTTLES-CB1037-Flowers-Foldable-Multicolor/dp/B00A27YI4K/ref=sr_1_550?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973185&sr=1-550&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/BOTTLES-CB1032-Hummingbird-Foldable-Multicolor/dp/B00A27YI3Q/ref=sr_1_555?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973217&sr=1-555&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Stansport-Stainless-Double-Bottle-14-Ounce/dp/B004Z0Z0QK/ref=sr_1_589?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973295&sr=1-589&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Global-Vision-Eyewear-Sunglasses-Gray/dp/B00L4E77SO/ref=sr_1_598?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973295&sr=1-598&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Electra-Bicycle-Co-598605-Saddle/dp/B0071ZD8KG/ref=sr_1_644?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973401&sr=1-644&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Ultimate-Survival-Technologies-4-Inch-Orange/dp/B00B0D2E64/ref=sr_1_745?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973701&sr=1-745&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Stansport-180-Backpackers-IsoButane-Stove/dp/B001B8UY0G/ref=sr_1_829?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973894&sr=1-829&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Liberty-Bottle-Boston-Medium-Almond/dp/B01A0N7AK4/ref=sr_1_835?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472973894&sr=1-835&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Equinox-Hellbender-Map-Case-Medium/dp/B002WJBVY6/ref=sr_1_883?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974108&sr=1-883&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",

                "https://www.amazon.com/Serfas-Womens-Chromoly-Performance-Saddle/dp/B00LW69ID6/ref=sr_1_925?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974201&sr=1-925&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Moneysworth-Best-Professional-Triangular-Cleaning/dp/B01AJNAJLM/ref=sr_1_1037?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974278&sr=1-1037&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Ventura-420035-RD-Bicycle-Bell-Red/dp/B01A0G4NRE/ref=sr_1_1039?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974278&sr=1-1039&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/RavX-Comfy-Lock-Handlebar-Grip/dp/B0060Z195E/ref=sr_1_1053?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974278&sr=1-1053&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Lifeline-Person-Emergency-Survival-Blanket/dp/B00GBMUHK4/ref=sr_1_1099?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974332&sr=1-1099&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Red-Rock-Outdoor-Gear-Function/dp/B00J0VBL76/ref=sr_1_1139?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974378&sr=1-1139&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Crazy-Stuff-Tiger-Cable-Lock/dp/B005Q6UZYO/ref=sr_1_1170?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974395&sr=1-1170&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Gotta-Bandage-Monsters-Bandages-Combo/dp/B016QOJZ4K/ref=sr_1_1174?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974395&sr=1-1174&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Life-good-Womens-Everyday-Stormy/dp/B00TVJTZSE/ref=sr_1_1204?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974446&sr=1-1204&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Life-good-Jakes-Night-Black/dp/B00M0VA78M/ref=sr_1_1207?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974446&sr=1-1207&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/American-Expedition-STMG-330-Collage-Multi-Color/dp/B00UZ083DQ/ref=sr_1_1322?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974517&sr=1-1322&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Chaos-Chinook-Micro-Fleece-Earband/dp/B003YXZ6K4/ref=sr_1_1352?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974530&sr=1-1352&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/True-Utility-Compact-Midilite-Torch/dp/B000ZMB1NI/ref=sr_1_1371?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974544&sr=1-1371&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Level-Six-Flux-GMA-FLUX-BK-XS-Glove/dp/B002VBXSRS/ref=sr_1_1446?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974684&sr=1-1446&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Light-Fire-Replacement-FireSteel-FireKnife/dp/B00H4YBCPC/ref=sr_1_1659?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974809&sr=1-1659&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Seirus-Innovation-6820-Neofleece-Adjustable/dp/B0018BJTO8/ref=sr_1_1666?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974809&sr=1-1666&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Sector-Momentum-Elbow-Protective-Medium/dp/B00KM5MPT2/ref=sr_1_1694?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472974973&sr=1-1694&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Calcutta-Ponchos-Rainsuit-Clear-size/dp/B005LL89CY/ref=sr_1_1734?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976015&sr=1-1734&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Decko-OutDoor-30300-Camping-Lantern/dp/B00KV87QYO/ref=sr_1_1778?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976054&sr=1-1778&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/KeyGear-Mini-Personal-Alarm-Purple/dp/B00YFPQM8U/ref=sr_1_1779?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976054&sr=1-1779&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/M-Wave-Reflective-Lock-Cover-Silver/dp/B00L8M074I/ref=sr_1_1786?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976054&sr=1-1786&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/NIKW4-Womens-Cable-Pants-Large/dp/B00M2HO8H0/ref=sr_1_1850?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976115&sr=1-1850&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Lucky-Bums-Dynamo-Flashlight-Green/dp/B009PXJUEU/ref=sr_1_1868?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976115&sr=1-1868&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Coleman-2000016435-Utensil-Set/dp/B001TSC6MM/ref=sr_1_1899?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976162&sr=1-1899&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Peak-Performance-Folding-Camping-Stool/dp/B0143ENURS/ref=sr_1_1915?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976162&sr=1-1915&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Krown-KRHEL-WHT-Helmet-White/dp/B004UOL8E4/ref=sr_1_1987?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976202&sr=1-1987&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Super-B-Three-Way-Torx-Wrench/dp/B00HFC0NY4/ref=sr_1_2324?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976324&sr=1-2324&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/GSI-Outdoors-Glacier-Stainless-Flask/dp/B001LF3IHK/ref=sr_1_2386?m=ATVPDKIKX0DER&s=sporting-goods&ie=UTF8&qid=1472976360&sr=1-2386&refinements=p_6%3AATVPDKIKX0DER%2Cp_36%3A600-2000",
                "https://www.amazon.com/Allegra-Stylish-Sleeve-Solid-Sweater/dp/B00901HFFI/ref=sr_1_44?s=apparel&ie=UTF8&qid=1473043912&sr=1-44&nodeID=1044442&refinements=p_36%3A200-1998",
                "https://www.amazon.com/Ohoo-Mens-Casual-V-Neck-DCV010-BLACK-L/dp/B01CWRQQRY/ref=sr_1_3?s=apparel&ie=UTF8&qid=1473044060&sr=1-3&nodeID=1044442&refinements=p_36%3A200-1998",
                "https://www.amazon.com/Ohoo-Mens-Slim-Casual-V-Neck/dp/B01CWS7EPG/ref=pd_sbs_193_1?ie=UTF8&refRID=GEDPYFQFCGKSY36CR2Q3",
                "https://www.amazon.com/Allegra-Turtle-Neck-Slim-T-Shirt/dp/B00NWDWNZ2/ref=pd_sim_193_3?ie=UTF8&refRID=TFTJ8FNNV2DCJZM6DPMY",
                "https://www.amazon.com/Alberto-Cardinali-Solid-V-Neck-Sweater/dp/B016TYJXVM/ref=pd_sim_193_4?ie=UTF8&refRID=GY0S35CXAQKVFED98SS9",
                "https://www.amazon.com/Combed-Cotton-Design-Casual-Turtleneck/dp/B001VAKRI8/ref=pd_sim_193_5?ie=UTF8&refRID=09Q4G0V82J7ND4EQRZG7",
                "https://www.amazon.com/Super-Light-Weight-Pullover-Medusa/dp/B01BHCWU2U/ref=sr_1_53?s=apparel&ie=UTF8&qid=1473045035&sr=1-53&nodeID=1044442&refinements=p_36%3A200-1998",
                "https://www.amazon.com/Guide-Gear-Turtleneck/dp/B0040AT2LO/ref=pd_sim_193_3?ie=UTF8&refRID=SZ8TBH8A03Y398W4EE0G",
                "https://www.amazon.com/UNRESTRAINED-Hipster-Longline-Hooded-Zipper/dp/B01ER83J3K/ref=pd_rhf_dp_s_cp_2?ie=UTF8&pd_rd_i=B01ER83J3K&pd_rd_r=F46HC5C48FVED204QP8F&pd_rd_w=22vJh&pd_rd_wg=4wW92&refRID=F46HC5C48FVED204QP8F",
                "https://www.amazon.com/Henley-Sleeve-Button-Cotton-Casual/dp/B01JPFUAPK/ref=pd_d0_nib_v2_cwb_ap_3?ie=UTF8&refRID=Y5SBHK9CQD4Q6C1QH89W",
                "https://www.amazon.com/Ardorrs-100-Cotton-PullOver-Hoodie/dp/B01JMQB8US/ref=pd_d0_nib_v2_cwb_ap_2?ie=UTF8&refRID=Y5SBHK9CQD4Q6C1QH89W",
                "https://www.amazon.com/Ardorr-Mens-100-Cotton-Sweater/dp/B01JMIV9ZU/ref=pd_d0_recs_v2_cwb_ap_1?ie=UTF8&refRID=C7VG2D08JJDYV4DEP7BZ",
                "https://www.amazon.com/Zicac-Designed-Casual-Skinny-Waistcoat/dp/B00GROHJI4/ref=pd_sim_193_4?ie=UTF8&refRID=YAP5V1AY4Q81Y51NCX6C",
                "https://www.amazon.com/Coofandy-V-neck-Sleeveless-Jacket-Business/dp/B018FVI4SU/ref=pd_sim_193_2?ie=UTF8&refRID=Y0GD2T7KYKV3JHTNGRYS",
                "https://www.amazon.com/TopTie-Unisex-Fashion-College-Necktie/dp/B00I9567FU/ref=pd_sim_193_1?ie=UTF8&refRID=S8RV3D3SNH1XW3GRC4HM",
                "https://www.amazon.com/dp/B01D7R9N96?psc=1",
                "https://www.amazon.com/Cambridge-Members-Mens-Basic-Tie/dp/B0140U05WI/ref=pd_sim_sbs_193_4?ie=UTF8&refRID=89V9FBM9PY5WX67SKQQY",
                "https://www.amazon.com/dp/B01EZMTO5A?psc=1",
                "https://www.amazon.com/dp/B01IVYB49W?psc=1&smid=A3V7IZ43JBJ920",
                "https://www.amazon.com/Hanton-Casual-Button-Long-Sleeve-Shirts/dp/B01FESFV5Q/ref=pd_rhf_dp_s_cp_3?ie=UTF8&pd_rd_i=B01FESFV5Q&pd_rd_r=GEZBACHMPEEFBT1F26W3&pd_rd_w=eaWSb&pd_rd_wg=DjaWF&refRID=GEZBACHMPEEFBT1F26W3",
                "https://www.amazon.com/Hanton-Classic-Western-Button-Sleeve/dp/B01IRXKONO/ref=pd_sim_193_3?ie=UTF8&refRID=RG203XQA2J00FS5NK4X0",
                "https://www.amazon.com/Hanes-Ultimate-Socks-White-10-13/dp/B00FBD2G1C/ref=sr_1_5?s=apparel&ie=UTF8&qid=1473048220&sr=1-5&nodeID=7147441011&keywords=socks",
                "https://www.amazon.com/dp/B015QA0KYE?psc=1",
                "https://www.amazon.com/dp/B00LBL4JYU?psc=1",
                "https://www.amazon.com/Carhartt-Mens-Arctic-Heavy-Socks/dp/B0051U15IU/ref=cts_ap_1_vtp",
                "https://www.amazon.com/Carhartt-Mens-Extremes-Arctic-Socks/dp/B00408936U/ref=pd_sim_193_1?ie=UTF8&refRID=PBDQ3895D6GGFQZSS2FF",
                "https://www.amazon.com/Carhartt-Triple-Blend-Thermal-Socks/dp/B0051U15IK/ref=pd_sim_193_3?ie=UTF8&refRID=H1WTWDPV0MZ9RBP05AN8",
                "https://www.amazon.com/dp/B01G395OLG?psc=1",
                "https://www.amazon.com/Spikerking-Show-Cotton-Deodorant-Socks/dp/B01DBS693O/ref=cts_ap_1_vtp",
                "https://www.amazon.com/dp/B01KWVDNXM?psc=1",
                "https://www.amazon.com/PACKGOUT-PAIRS-Breathable-Comfortable-Running/dp/B019NZ0ZZC/ref=pd_sim_sbs_193_1?ie=UTF8&refRID=1VQ780ME1NE43VCW77VX",
                "https://www.amazon.com/SDBING-Unisex-Fingers-Separate-Assorted/dp/B00UTAM1XA/ref=pd_sbs_468_5?ie=UTF8&psc=1&refRID=ZD42ERTQ00TSN9RC9D5S",
                "https://www.amazon.com/RSG-Hosiery-Womens-Shorties-Length/dp/B00IRJKJ34/ref=pd_sim_193_3?ie=UTF8&refRID=SFKMCKZM52BPB4KFW98X",
                "https://www.amazon.com/Foot-Traffic-Striped-Toe-Socks/dp/B014RHY2RI/ref=pd_sim_193_5?ie=UTF8&refRID=VSK8RTDAQ2SZ75VFD05J",
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

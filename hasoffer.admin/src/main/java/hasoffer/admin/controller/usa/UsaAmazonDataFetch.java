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
                "https://www.amazon.com/Titus-Symphony-Mid-tier-Earbuds-Inline/dp/B01DPRY9RE/ref=lp_12273809011_1_1?srs=12273809011&ie=UTF8&qid=1472990666&sr=8-1",
                "https://www.amazon.com/gp/product/B00ZELEKIS?tag=slickdeals&ascsubtag=d41c014e71ac11e6ab8b82b003bfdcdb0INT&ref_=sr_1_3&m=A2RM3U1ORVPROE&s=merchant-items&qid=1472780053&sr=1-3&keywords=gopro&sa-no-redirect=1&pldnSite=1",
                "https://www.amazon.com/Dove-Care-Extra-Fresh-Ounce/dp/B00PCURCYA/ref=sr_1_12_a_it?ie=UTF8&qid=1472889867&sr=8-12&keywords=man",
                "https://www.amazon.com/Blocking-Passport-Protectors-Protection-perfectly/dp/B01F1AGD2C/ref=sr_1_11?ie=UTF8&qid=1472889917&sr=8-11&keywords=man+3c",
                "https://www.amazon.com/iPhone-LoHi-Ultimate-Lightweight-Durable/dp/B00S93RMLU/ref=sr_1_12?ie=UTF8&qid=1472889917&sr=8-12&keywords=man+3c",
                "https://www.amazon.com/Aluminum-Protective-Resistant-Shockproof-Military/dp/B015KW9QKW/ref=sr_1_26?ie=UTF8&qid=1472890169&sr=8-26&keywords=man+3c",
                "https://www.amazon.com/3C-Mall-Cartoon-Passport-Organizer/dp/B01508K0U6/ref=sr_1_34?ie=UTF8&qid=1472890265&sr=8-34&keywords=man+3c",
                "https://www.amazon.com/BlueCosto-Passport-Wallets-Covers-Holders/dp/B01G818A42/ref=pd_sim_sbs_229_4?ie=UTF8&refRID=JS6E90N48XCXZ4YQXZ93",
                "https://www.amazon.com/Outrip-Blocking-Leather-Passport-Holder/dp/B01GYC4KLC/ref=pd_sim_198_1?ie=UTF8&refRID=X260QPQJ8W39FJNZ07PE",
                "https://www.amazon.com/dp/B01E0BDJ20?psc=1",
                "https://www.amazon.com/Professional-Cocktail-Shaker-Pourers-BARVIVO/dp/B01FDSZ7JW/ref=pd_sim_198_3?ie=UTF8&psc=1&refRID=F7CRWKKWYWY286PP2NWD",
                "https://www.amazon.com/dp/B014WSP9OW?psc=1",
                "https://www.amazon.com/Waterproof-Bluetooth-Handsfree-Smartphone-Pedometer/dp/B00PQD1WGC/ref=sr_1_3?ie=UTF8&qid=1472890492&sr=8-3&keywords=free+shipping",
                "https://www.amazon.com/LEMFO-WristWatch-U8-Smartphones-Blackberry/dp/B00JQ8MCFI/ref=pd_sim_107_3?ie=UTF8&psc=1&refRID=55GMM740Y0W3EBCVRDK8",
                "https://www.amazon.com/dp/B01820AXUQ?psc=1",
                "https://www.amazon.com/Travelambo-Travel-Passport-Holders-Sleeves/dp/B01JRT6LTS/ref=pd_sim_198_5?ie=UTF8&refRID=MKW1E3ARXPWXB57TG3QG",
                "https://www.amazon.com/SHIPPING-Nicotine-Free-Cigarettes-Flavors-Smooth/dp/B00TMC3JOG/ref=sr_1_5_a_it?ie=UTF8&qid=1472890508&sr=8-5&keywords=free+shipping",
                "https://www.amazon.com/streer®-Extended-Charging-Charger-iPhone/dp/B014SZ6C8Q/ref=sr_1_8?ie=UTF8&qid=1472890508&sr=8-8&keywords=free+shipping",
                "https://www.amazon.com/Portable-Adjustable-Vent-Mount-Holder/dp/B00J9Z0L42/ref=sr_1_23?ie=UTF8&qid=1472890635&sr=8-23&keywords=free+shipping",
                "https://www.amazon.com/Lightning-Cables-Charger-iPhone-PleBluWte/dp/B01IEP4MOW/ref=pd_sim_sbs_107_2?ie=UTF8&psc=1&refRID=15606G8DKA4A9K1GV372",
                "https://www.amazon.com/Lightning-Charger-3-Meter-Adapters-iPhone/dp/B01IN329RW/ref=pd_sim_sbs_107_3?ie=UTF8&psc=1&refRID=53C7SDYYGFKKGKR0N29N",
                "https://www.amazon.com/dp/B01GIMELOO?psc=1",
                "https://www.amazon.com/dp/B01DFQLQ5S?psc=1",
                "https://www.amazon.com/dp/B00OSLDFTK?psc=1",
                "https://www.amazon.com/dp/B01KAIWAVS?psc=1",
                "https://www.amazon.com/Wooku-Mobile-Steering-Samsung-Cellphones/dp/B00ISQI54G/ref=pd_sim_107_1?ie=UTF8&psc=1&refRID=DWFRNCET5A9J1DRR8NS7",
                "https://www.amazon.com/Universal-compatible-Samsung-Blackberry-smartphones/dp/B00UIA67B8/ref=pd_sim_107_2?ie=UTF8&psc=1&refRID=K86AMVC28AE7CYQB4D7X",
                "https://www.amazon.com/FlyStone-Univeral-Windshield-Samsung-Accommodates/dp/B00JUHQEVI/ref=pd_sim_107_5?ie=UTF8&psc=1&refRID=QD5QHDP4XS1ZA7FN26E8",
                "https://www.amazon.com/dp/B01HJ8E11E?psc=1",
                "https://www.amazon.com/dp/B01FJGGCRY?psc=1",
                "https://www.amazon.com/dp/B01G37X1JU?psc=1",
                "https://www.amazon.com/dp/B01H7LV1YS?psc=1",
                "https://www.amazon.com/Simpac-Fabric-Passport-Holder-Blocking/dp/B01IXHGRQC/ref=pd_sim_200_3?ie=UTF8&refRID=3Z40Z6HYHTZHXY2FNNWJ",
                "https://www.amazon.com/dp/B01HEWU0ME?psc=1",
                "https://www.amazon.com/Invisible-Bluetooth-Hands-free-Smartphone-VONRO/dp/B018E06GBO/ref=sr_1_24?ie=UTF8&qid=1472890661&sr=8-24&keywords=free+shipping",
                "https://www.amazon.com/Invisible-Bluetooth-Novpeak-Headphones-Earphones/dp/B018JL5C90/ref=pd_sim_107_2?ie=UTF8&psc=1&refRID=22H7JCDWEEQXN733M932",
                "https://www.amazon.com/Bluetooth-Invisible-Microphone-Hands-free-Smartphone/dp/B01H5CBC1G/ref=pd_sim_107_3?ie=UTF8&psc=1&refRID=G1PY7JDAWB15YPPN3VT8",
                "https://www.amazon.com/LSoug-Wireless-Bluetooth-Headphones-Ear/dp/B01EAC1SI6/ref=pd_sim_107_2?ie=UTF8&psc=1&refRID=GQE5FSMVJZZMXRJYNMB0",
                "https://www.amazon.com/LSoug-Shockproof-Resistant-Protective-Samsung/dp/B01DQKSURK/ref=pd_sim_200_4?ie=UTF8&psc=1&refRID=4PR1TS933G5TG8KCAJDQ",
                "https://www.amazon.com/dp/B01CQU8ESG?psc=1",
                "https://www.amazon.com/dp/B01ADY57PK?psc=1",
                "https://www.amazon.com/ArmorSuit-MilitaryShield-Protector-Anti-Bubble-Replacements/dp/B00UH3L82Y/ref=pd_sim_107_2?ie=UTF8&psc=1&refRID=TVFK4XNJFMHCRY6YP8RJ",
                "https://www.amazon.com/dp/B01HV9XSHY?psc=1",
                "https://www.amazon.com/dp/B00WSBBV5W?psc=1",
                "https://www.amazon.com/Yosou-Lightning-Braided-Tangle-Free-Charging/dp/B01I4LU8CQ/ref=pd_sim_sbs_107_3?ie=UTF8&psc=1&refRID=SSX8DZGKHP05NR022XQY",
                "https://www.amazon.com/dp/B00KGGJPYA?psc=1",
                "https://www.amazon.com/Dane-Elec-USB-flash-drive-Multi-pack/dp/B00KNRWTB8/ref=sr_1_29?ie=UTF8&qid=1472890661&sr=8-29&keywords=free+shipping",
                "https://www.amazon.com/Shoe-Deodorizer-Spray-Miracle-Forest/dp/B01FNFUPWE/ref=pd_sim_198_2?ie=UTF8&refRID=M6VXYXA6D73HEA34BHZ4",
                "https://www.amazon.com/dp/B01FZNCWFM?psc=1",
                "https://www.amazon.com/dp/B00B0N281A?psc=1",
                "https://www.amazon.com/Dane-Elec-Drive-Aqua-Capless-ZMP-32G-CA-A1-R/dp/B003BVTH7M/ref=pd_sim_sbs_147_3?ie=UTF8&psc=1&refRID=22QYNA68EH1FBW3X8JVE",
                "https://www.amazon.com/SanDisk-Cruzer-Frustration-Free-Packaging-SDCZ60-032G-AFFP/dp/B008AF380Q/ref=pd_sim_sbs_147_3?ie=UTF8&psc=1&refRID=ZH2G7FM9YRN2YWMKK4HN",
                "https://www.amazon.com/SanDisk-Flash-Cruzer-Glide-DCZ60-016G-B35/dp/B007YX9O9O/ref=pd_sim_147_1?ie=UTF8&psc=1&refRID=CCHV8E84JJECK94G92TB",
                "https://www.amazon.com/dp/B01HUMVA8G?psc=1",
                "https://www.amazon.com/SanDisk-Cruzer-Frustration-Free-Packaging-SDCZ36-016G-AFFP/dp/B007JR5368/ref=pd_sim_147_3?ie=UTF8&psc=1&refRID=1PY927Z6P2J640HD7Q2M",
                "https://www.amazon.com/dp/B00YP05B4G?psc=1",
                "https://www.amazon.com/Kingston-Digital-DataTraveler-DTSE9H-16GBZET/dp/B00DYQYITG/ref=pd_sim_sbs_147_2?ie=UTF8&psc=1&refRID=A0TDQ7VCT2ZXNZFVYYM0",
                "https://www.amazon.com/Ultra-Thin-Semi-transparent-Lightweight-Absolutely-Bulkiness/dp/B00UVSNWYC/ref=pd_sim_107_5?ie=UTF8&refRID=ZCKBB9T84EW56HBNHWCX",
                "https://www.amazon.com/Gembonics-Premium-Charger-Charging-Connector/dp/B00USB1D20/ref=pd_sim_107_4?ie=UTF8&psc=1&refRID=EPC6WJGK30M2456XPSM2",
                "https://www.amazon.com/Gembonics-Charging-Connector-Lightning-Portable/dp/B00WRQGQJ4/ref=pd_sim_107_4?ie=UTF8&psc=1&refRID=DQ6GZ51F76QPCAFG87MS",
                "https://www.amazon.com/dp/B01AM53PSG?psc=1",
                "https://www.amazon.com/dp/B01IHO29QI?psc=1",
                "https://www.amazon.com/dp/B00OT797VI?psc=1",
                "https://www.amazon.com/dp/B01IEQNRZG?psc=1",
                "https://www.amazon.com/iPhone-6s-Screen-Protector-Protection/dp/B018HJGDB0/ref=pd_sim_107_5?ie=UTF8&psc=1&refRID=5EBRQ321QB46XFZK6HWA",
                "https://www.amazon.com/dp/B01DUHC6J2?psc=1",
                "https://www.amazon.com/dp/B01IVL8EMU?psc=1",
                "https://www.amazon.com/iPhone-Anti-scratch-Protection-Layered-Protective/dp/B00UZEN0LM/ref=pd_sim_sbs_107_2?ie=UTF8&psc=1&refRID=T5PQZP33E6X0QYD9NE0E",
                "https://www.amazon.com/dp/B01GAQCBZE?psc=1",
                "https://www.amazon.com/Zizo-Tempered-Protector-Included-Kickstand/dp/B01G603AL8/ref=pd_sim_sbs_107_2?ie=UTF8&psc=1&refRID=WTH6BSQ99X8VN294VXBD",
                "https://www.amazon.com/dp/B01G8KCAIA?psc=1",
                "https://www.amazon.com/iPhone-TUCCH®-Leather-Carry-All-Devices/dp/B01J9PSWP6/ref=pd_sim_sbs_107_3?ie=UTF8&psc=1&refRID=0GNAE15BA9F0P7FE6GQF",
                "https://www.amazon.com/Aquazone-Premium-Swimming-Goggles-Protection/dp/B01F9N62P4/ref=pd_sim_229_5?ie=UTF8&refRID=FRWXNXMKN7PAYTAZCKBX",
                "https://www.amazon.com/dp/B01DIQWQ6I?psc=1",
                "https://www.amazon.com/dp/B01D0F1168?psc=1",
                "https://www.amazon.com/dp/B01I3B158I?psc=1",
                "https://www.amazon.com/dp/B01G7FC2RU?psc=1",
                "https://www.amazon.com/UMBERBOX-Stainless-Bottle-Double-Insulated/dp/B01FL57CHM/ref=pd_rhf_dp_s_cp_4?ie=UTF8&pd_rd_i=B01FL57CHM&pd_rd_r=5D3H0PJ7FH4VDQ0PJPRK&pd_rd_w=BfjK7&pd_rd_wg=OYrnK&refRID=5D3H0PJ7FH4VDQ0PJPRK",
                "https://www.amazon.com/TR623-Performance-Short-Single-Lightweight/dp/B00F2XDB3I/ref=sr_1_66?ie=UTF8&qid=1472893123&sr=8-66-spons&keywords=free+shipping&psc=1",
                "https://www.amazon.com/MJ-Soffe-Mens-Running-Short/dp/B003AU5W5K/ref=pd_sim_193_5?ie=UTF8&refRID=23W5ZFXAB184416YVDGT",
                "https://www.amazon.com/Baleaf-Quick-Dry-Lightweight-Running-Shorts/dp/B01FJPVVIK/ref=pd_sim_193_3?ie=UTF8&refRID=QTDR3D8Y2X6G80V9AX9D",
                "https://www.amazon.com/Time-Mens-Pace-Running-Short/dp/B008PFJ9EU/ref=pd_sim_200_2?ie=UTF8&refRID=G79RY4CFTFTFHNN5D7W3",
                "https://www.amazon.com/Baleaf-Workout-Running-Pocket-Shorts/dp/B01B1FK83G/ref=pd_sim_200_5?ie=UTF8&refRID=A3DZK4VMGRN3YGMMJ6D2",
                "https://www.amazon.com/TCK-Retro-Stripe-Tube-Socks/dp/B00PRKKVH0/ref=pd_rhf_dp_s_cp_2?ie=UTF8&pd_rd_i=B00PRKKVH0&pd_rd_r=7888HD6JZJYXDWY8C2GJ&pd_rd_w=UccbT&pd_rd_wg=CYMiY&refRID=7888HD6JZJYXDWY8C2GJ",
                "https://www.amazon.com/Wigwam-Mens-King-Classic-Sport/dp/B00HG757GW/ref=pd_sim_200_5?ie=UTF8&refRID=VAVMZ4PH3GATTWZF5993",
                "https://www.amazon.com/dp/B00TBOD364?psc=1",
                "https://www.amazon.com/Funky-Socks-Colorful-Patterned-Casual/dp/B00KO1876E/ref=pd_d0_recs_v2_cwb_ap_4?ie=UTF8&refRID=35ZQXHMYKWGCXQ46Y0X8",
                "https://www.amazon.com/Mens-Stripe-Pattern-Honeycomb-Socks/dp/B00QN6ZA5A/ref=pd_sim_193_2?ie=UTF8&refRID=TZWW5H9YDX2Y8ZBV7XFY",
                "https://www.amazon.com/dp/B01ETK1M76?psc=1",
                "https://www.amazon.com/Sock-Habit-Patterned-Colorful-Shamrock/dp/B01E4C5EWS/ref=pd_sim_193_3?ie=UTF8&psc=1&refRID=NNP2VPJT1DX6ESA65PGF",
                "https://www.amazon.com/Sale-Bonison-Quality-Lemonade-Sparkling-Beverages/dp/B00XDAKB7G/ref=sr_1_59?ie=UTF8&qid=1472893156&sr=8-59&keywords=free+shipping",
                "https://www.amazon.com/Sporty-Infuser-Lemonade-Sparkling-Beverages/dp/B017FSDLD2/ref=pd_sim_468_3?ie=UTF8&refRID=0820RMQ9SWW65FV9Z6NP",
                "https://www.amazon.com/Bonison-Stylish-Infuser-Watertight-Carrying/dp/B014968DAE/ref=pd_sim_sbs_468_3?ie=UTF8&refRID=7WFP2MPQ3JCJP45SYKPB",
                "https://www.amazon.com/dp/B00XMK7YPO?psc=1",
                "https://www.amazon.com/OPTIONS-Infuser-Bottle-BPA-Free-Infusion/dp/B00WUPXNMA/ref=pd_sim_468_4?ie=UTF8&refRID=YDMKJ2FVN3Y3S3V7YXZ4",
                "https://www.amazon.com/dp/B01E7FC38A?psc=1",
                "https://www.amazon.com/Simply4Travel-Blocking-Passport-Documents-Unmatched/dp/B01EZA17IO/ref=pd_sim_468_4?ie=UTF8&refRID=9W97Q4H5FKR366HPATGD",
                "https://www.amazon.com/dp/B00YPI6Y7G?psc=1",
                "https://www.amazon.com/Travel-Wallet-RFID-Blocking-Secure-Passport/dp/B00YJ5D9IM/ref=pd_sim_sbs_200_1?ie=UTF8&refRID=0YRMVBCFP9CCHW66JCHG",
                "https://www.amazon.com/dp/B01JRT6OV8?psc=1",
                "https://www.amazon.com/SE-All-Weather-Emergency-Magnesium-Everything/dp/B0010O748Q/ref=sr_1_75?ie=UTF8&qid=1472893626&sr=8-75&keywords=free+shipping",
                "https://www.amazon.com/SE-CC4580-MilitaryLensatic-Prismatic-Sighting/dp/B001ID4ZY0/ref=pd_sim_469_3?ie=UTF8&psc=1&refRID=WMEKM0R7DJZWGAANAMRZ",
                "https://www.amazon.com/SE-KHK6320-Outdoor-Tanto-Starter/dp/B00178CS4K/ref=pd_sim_469_1?ie=UTF8&psc=1&refRID=4XJECZFPXRM30DD7W554",
                "https://www.amazon.com/WOWOWO-Tactical-Outdoor-Camping-Survivor/dp/B01BNHTA2M/ref=pd_sim_200_4?ie=UTF8&psc=1&refRID=QR4EKB5PT2K2D4BK93RF",
                "https://www.amazon.com/dp/B007WAZDVM?psc=1",
                "https://www.amazon.com/A2S-Paracord-Bracelet-K2-Peak-Emergency/dp/B01AV77QSA/ref=pd_sim_200_3?ie=UTF8&refRID=QR4EKB5PT2K2D4BK93RF",
                "https://www.amazon.com/dp/B00OK5K8XU?psc=1",
                "https://www.amazon.com/Attmu-Outdoor-Survival-Paracord-Bracelet/dp/B016ZA1U0W/ref=pd_sim_468_5?ie=UTF8&refRID=K3BBMBDMXCS8PXBD1BHY",
                "https://www.amazon.com/dp/B01E2JOPMS?psc=1",
                "https://www.amazon.com/dp/B00J2ZFVGW?psc=1",
                "https://www.amazon.com/Strengthener-Adjustable-Resistance-Exerciser-Hand-muscle/dp/B01GNU3E80/ref=pd_sim_200_1?ie=UTF8&psc=1&refRID=MTSKEBA2WN5DEVHBNK9K",
                "https://www.amazon.com/dp/B017IAK1CG?psc=1",
                "https://www.amazon.com/dp/B01FDM93GQ?psc=1",
                "https://www.amazon.com/dp/B00ZFLZ3JM?psc=1",
                "https://www.amazon.com/Alpine-Swiss-Genuine-Leather-Flip-out/dp/B00MAVN0R2/ref=sr_1_3?s=apparel&ie=UTF8&qid=1472984383&sr=1-3&nodeID=7147441011&keywords=free+shipping",
                "https://www.amazon.com/Alpine-Swiss-Leather-Wallets-Models/dp/B0167WMZ70/ref=pd_sim_193_2?ie=UTF8&refRID=R9C025VVXP88QCH00R27",
                "https://www.amazon.com/dp/B01EM7SDNM?psc=1",
                "https://www.amazon.com/dp/B00R6PK09I?psc=1",
                "https://www.amazon.com/dp/B019UZ7I2S?psc=1",
                "https://www.amazon.com/Credit-YOUNA-Blocking-Genuine-Leather/dp/B019JT7TC4/ref=pd_sim_193_2?ie=UTF8&refRID=AY9X7SHX2SFVTP6ZWMPC",
                "https://www.amazon.com/Wallet-Soye-Blocking-Vintage-Leather/dp/B01FW731UQ/ref=pd_sim_193_5?ie=UTF8&refRID=ZS2PK30ENYQZSXKCT164",
                "https://www.amazon.com/Credit-YOUNA-Blocking-Genuine-Leather/dp/B019JT7TC4/ref=pd_sim_193_1?ie=UTF8&refRID=MZYXMYJQS59GVG41VXVC",
                "https://www.amazon.com/dp/B01GTQXKAU?psc=1",
                "https://www.amazon.com/Wallet-Passport-Carrying-Valuables-Hiding/dp/B01AJRCHT0/ref=pd_sim_193_1?ie=UTF8&refRID=X2AXACK794XSG1B4D0A5",
                "https://www.amazon.com/Alpine-Swiss-Dress-Reversible-Leather/dp/B01BO7WWEE/ref=pd_sim_193_2?ie=UTF8&refRID=M56T2ADEN241921ZRPN7",
                "https://www.amazon.com/Sportoli-Classic-Stitched-Genuine-Leather/dp/B00WH23P9M/ref=pd_sim_193_2?ie=UTF8&refRID=XJJZRNS52EA759PYQCN5",
                "https://www.amazon.com/dp/B0155YI5KC?psc=1",
                "https://www.amazon.com/Sportoli-Classic-Genuine-Leather-Uniform/dp/B01FGFBIAO/ref=pd_sim_sbs_193_3?ie=UTF8&refRID=THSQX5WF94WTHZ2ZTSYW",
                "https://www.amazon.com/Sportoli-Classic-Stitched-Genuine-Leather/dp/B01HQEXEEG/ref=pd_sim_193_3?ie=UTF8&refRID=TYQWWT13QD0K9PWDTVDC",
                "https://www.amazon.com/CTM®-Mens-Leather-Basic-Dress/dp/B006L6QBQE/ref=pd_sbs_193_4?ie=UTF8&refRID=96GRS28BHT2TD3ZWSQN9",
                "https://www.amazon.com/Moda-Raza-Genuine-Leather-Casual/dp/B00PMXVJ9G/ref=cts_ap_2_vtp",
                "https://www.amazon.com/Depot-Unisex-Cotton-Herringbone-Fedora/dp/B01J6JUNCK/ref=pd_sim_193_4?ie=UTF8&refRID=7GHD8QJ091X7HJK0KY7D",
                "https://www.amazon.com/City-Hunter-Cotton-Trilby-Fedora/dp/B008N24O4A/ref=pd_sim_193_2?ie=UTF8&refRID=NKN156W0Z0F800TQV4VW",
                "https://www.amazon.com/HDE-Pinstripe-Houndstooth-Stingy-Gangster/dp/B00OTRODCQ/ref=pd_sim_193_4?ie=UTF8&refRID=1EKM07TYG9XVKNAWCPWG",
                "https://www.amazon.com/Premium-Multi-Color-Stitch-Fedora/dp/B014UL1ZYY/ref=pd_sim_193_3?ie=UTF8&refRID=21KPX45P4C4MC58BRQQN",
                "https://www.amazon.com/City-Hunter-Cotton-Trilby-Fedora/dp/B008N24O4A/ref=pd_sim_193_3?ie=UTF8&refRID=98XSDYX1A05QT8V997JW",
                "https://www.amazon.com/Solid-Band-Summer-Straw-Fedora/dp/B01DHRIIVA/ref=pd_sim_193_5?ie=UTF8&refRID=6KP48GH67MPP2QRGFNR8",
                "https://www.amazon.com/Alexander-Mens-Solid-WHITE-Tie/dp/B000E9ZR6Y/ref=pd_sim_193_4?ie=UTF8&psc=1&refRID=21KPX45P4C4MC58BRQQN",
                "https://www.amazon.com/dp/B01GXZPR2Q?psc=1",
                "https://www.amazon.com/Coxeer®-Mens-Paisley-Necktie-Hanky/dp/B00ZHUE11M/ref=pd_sim_sbs_193_3?ie=UTF8&refRID=WD0PNAM9QHYGBZTQEZFJ",
                "https://www.amazon.com/Stylefad-Solid-Plaid-Hanky-Cufflink/dp/B00Y0XFP2Q/ref=pd_sim_193_1?ie=UTF8&refRID=A3AXY5JVK1P6N3X4Y392",
                "https://www.amazon.com/Bundle-Monster-Fashion-Necktie-Cufflinks/dp/B00YI8QI5Q/ref=pd_sim_193_5?ie=UTF8&refRID=KCRXDDNGW84DBQQFZKGJ",
                "https://www.amazon.com/Business-Cufflinks-Pocket-Zakka-Republic/dp/B017SN4KUM/ref=pd_sim_193_4?ie=UTF8&refRID=5WJ7ZC9108ZPW2V51WC2",
                "https://www.amazon.com/Bundle-Monster-Matching-Fashion-Accessories/dp/B017O910LW/ref=pd_sim_193_5?ie=UTF8&refRID=12K3W9G9712C7420BJWD",
                "https://www.amazon.com/KissTies-Necktie-Paisley-Wedding-Holiday/dp/B01GDH7O7A/ref=pd_sim_sbs_193_2?ie=UTF8&refRID=P1A5E30S17MB16TK0S4G",
                "https://www.amazon.com/Allbebe-Fashion-Jacquard-Microfiber-Necktie/dp/B019P1OYKG/ref=pd_sim_193_5?ie=UTF8&refRID=J1J3WKY5DC38AHEXJV3V",
                "https://www.amazon.com/MINDENG-Stripes-Necktie-Classic-Striped/dp/B00U7N7Z7Q/ref=pd_sim_193_3?ie=UTF8&refRID=M2MS8ZSSDP7N1GQE3ADE",
                "https://www.amazon.com/MINDENG-Classic-Striped-Streak-Necktie/dp/B00XICF8BS/ref=pd_sim_193_1?ie=UTF8&refRID=CNF5XJ96HMJP3GJ89N73",
                "https://www.amazon.com/MINDENG-Tartan-Business-Neckties-Leisure/dp/B00U7NA2N0/ref=pd_sim_193_4?ie=UTF8&refRID=FQV5G16K5SNPB9G25XMH",
                "https://www.amazon.com/dp/B00DU901J8?psc=1",
                "https://www.amazon.com/Classic-Black-Striped-Jacquard-Necktie/dp/B00U7N8YV2/ref=cts_ap_1_vtp",
                "https://www.amazon.com/Classic-Black-Striped-Jacquard-Necktie/dp/B00U7N8YV2/ref=cts_ap_1_vtp",
                "https://www.amazon.com/dp/B00VNEAWNC?psc=1",
                "https://www.amazon.com/Marquis-Mens-Solid-Dress-Shirt/dp/B01ES3VE4K/ref=pd_sim_193_4?ie=UTF8&refRID=J1J3WKY5DC38AHEXJV3V",
                "https://www.amazon.com/Mens-Dress-Shirt-Reversible-Available/dp/B01HFU6I5S/ref=pd_sim_193_5?ie=UTF8&refRID=V3H659CSY45Y31732AB2",
                "https://www.amazon.com/JONES-Mens-Button-Dress-Shirts/dp/B01ABTB1VQ/ref=pd_sim_193_4?ie=UTF8&refRID=Q78XRMNVTFD7YJAF2GCG",
                "https://www.amazon.com/Shirts-Sleeve-Formal-Casual-CL6299/dp/B013G3I81A/ref=pd_sim_193_4?ie=UTF8&refRID=MV2CBJE9AMFJ8WC0BD2V",
                "https://www.amazon.com/PAUL-JONES-Sleeves-Casual-CL5248-49/dp/B00RNSNJH8/ref=pd_sim_193_2?ie=UTF8&refRID=2Z6MFN1Y7D6P19PYK53W",
                "https://www.amazon.com/localmode-Cotton-Sleeve-Plaid-Button/dp/B01EWMSWH4/ref=pd_sbs_193_2?ie=UTF8&refRID=7WYX71B1ZW6C0XB1TYK7",
                "https://www.amazon.com/Zicac-Mercerized-Cotton-Button-Shirts/dp/B019MTJYBA/ref=pd_sim_193_5?ie=UTF8&refRID=DYBY8KX2P70C5ZNWQ7RJ",
                "https://www.amazon.com/iPretty-Fashion-Contrast-Sleeve-Casual/dp/B01AJUDULQ/ref=pd_sim_193_4?ie=UTF8&refRID=YN0QJVVW4QYS4CBP6CD5",
                "https://www.amazon.com/PAUL-JONES-Sleeves-Casual-CL5248-49/dp/B00RNSNJH8/ref=pd_sim_193_4?ie=UTF8&refRID=A1SPAZ3RCX410RKBQQ1Q",
                "https://www.amazon.com/localmode-Cotton-Sleeve-Plaid-Button/dp/B01EWMSWH4/ref=pd_sim_193_3?ie=UTF8&refRID=DA51Y59QE029TZM0RJ5H",
                "https://www.amazon.com/HDE-Solid-Color-Y-Back-Suspenders/dp/B00VMCLU0O/ref=pd_sim_193_5?ie=UTF8&refRID=21KPX45P4C4MC58BRQQN",
                "https://www.amazon.com/HDE-Suspenders-Adjustable-Elastic-Y-Shape/dp/B00997K7RQ/ref=pd_sim_193_4?ie=UTF8&refRID=0W7Z0FXNZTMM1HVKA9B2",
                "https://www.amazon.com/Womens-Suspenders-Adjustable-Various-Designs/dp/B01BE61SA4/ref=pd_sim_193_2?ie=UTF8&refRID=Z7DXV4WDHCKA8BM6N1RJ",
                "https://www.amazon.com/Dockers-Mens-Inch-Plaid-Suspenders/dp/B00TF0R1BC/ref=pd_sim_193_2?ie=UTF8&refRID=81Q06HJRN9XFNE9SN06K",
                "https://www.amazon.com/Dockers-Mens-Inch-Plaid-Suspenders/dp/B00TF0R1BC/rhttps://www.amazon.com/Custom-LeatherCraft-110USA-Elastic-Suspender/dp/B0000DD6C2/ref=pd_sim_193_3?ie=UTF8&psc=1&refRID=KJQQDEZXQFVPYBX7QNPCef=pd_sim_193_2?ie=UTF8&refRID=81Q06HJRN9XFNE9SN06K",
                "https://www.amazon.com/United-States-America-White-Suspender/dp/B00AMRR5WM/ref=pd_sim_469_3?ie=UTF8&psc=1&refRID=8FNSFMFQH64BKEJSNP2V",
                "https://www.amazon.com/FBT-FLAG-White-Blue-American-Flag/dp/B003ZKRFDW/ref=pd_sim_193_5?ie=UTF8&psc=1&refRID=95V7TPPMWJXRM2NQPTWQ",
                "https://www.amazon.com/HDE-Adjustable-Polyester-Tuxedo-Wedding/dp/B0099RISJ0/ref=pd_sim_193_5?ie=UTF8&refRID=0W7Z0FXNZTMM1HVKA9B2",
                "https://www.amazon.com/dp/B01GAC3AC6?psc=1",
                "https://www.amazon.com/Ysiop-Polyester-Pre-tie-Adjustable-Bowties/dp/B01GC04RVE/ref=pd_sim_193_1?ie=UTF8&refRID=JZPGZ1ASK0A5F7MBS2M6",
                "https://www.amazon.com/dp/B01GFAHIZI?psc=1",
                "https://www.amazon.com/DESMIIT-Mens-Mesh-Pocket-Short/dp/B00LLKJFBI/ref=pd_sim_193_2?ie=UTF8&refRID=8RZTMQTS3Q5Z6KZ84EB5",
                "https://www.amazon.com/SEOBEAN-Sports-Running-Training-Short/dp/B00GSMIL1E/ref=pd_sim_193_2?ie=UTF8&refRID=XNEF7T53SVS3CM8Z28AZ",
                "https://www.amazon.com/Linemoon-Cotton-Bottoms-Fashion-Simple/dp/B013NRE70A/ref=pd_sim_193_4?ie=UTF8&refRID=JFWDF2M4TEQ1QACEFVZY",
                "https://www.amazon.com/SEOBEAN-Cotton-Trunk-Lounge-Colors/dp/B00JKT2USM/ref=pd_sbs_193_4?ie=UTF8&refRID=XCNRE8CPA0XHJCQBKRKF",
                "https://www.amazon.com/SEOBEAN-Trunk-Boxer-Brief-Underwear/dp/B00GSQF9AG/ref=pd_sim_193_4?ie=UTF8&refRID=GW2BZDGZXXFSNJYYP17N",
                "https://www.amazon.com/SEOBEAN-Hipster-Panties-Trunk-Underwear/dp/B008ZPW7V2/ref=pd_sim_193_5?ie=UTF8&refRID=AQ3SA8CJVT4K68K8ASSZ",
                "https://www.amazon.com/SEOBEAN-Low-Rise-Trunk-Stripe-Underwear/dp/B00JKTFPZW/ref=pd_sim_193_5?ie=UTF8&refRID=RHX8RBH8W9CADQMSRWVK",
                "https://www.amazon.com/SEOBEAN-Low-Rise-Trunk-Boxer-Underwear/dp/B00FMY3T6G/ref=pd_sim_193_3?ie=UTF8&refRID=3Y1BB9D0B9P4X5Y78JJT",
                "https://www.amazon.com/Shlax-Wing-Necktie-Orange-Floral/dp/B00QAFT4I8/ref=pd_rhf_dp_s_cp_2?ie=UTF8&pd_rd_i=B00QAFT4I8&pd_rd_r=BPR47ND1T9NE45V5XP7D&pd_rd_w=SMFQ1&pd_rd_wg=QYsfX&refRID=BPR47ND1T9NE45V5XP7D",
                "https://www.amazon.com/Shlax-Wing-Checked-Purple-Neckties/dp/B00NCXH5TG/ref=pd_sim_193_4?ie=UTF8&refRID=NC32QXNQFNPHDGNS1AKW",
                "https://www.amazon.com/Shlax-Wing-Necktie-Multicolor-Floral/dp/B00QAGMQUK/ref=pd_sim_193_5?ie=UTF8&refRID=XDSKPND0D8Q4ADVE2N51",
                "https://www.amazon.com/Hanes-Beefy-T-T-Shirt-518T-Charcoal/dp/B00WO290HG/ref=sr_1_16?s=apparel&ie=UTF8&qid=1472998320&sr=1-16&nodeID=7147441011&keywords=T",
                "https://www.amazon.com/Hanes-mens-Beefy-T®-Tall-518T/dp/B00KNWZMOE/ref=pd_sim_200_1?ie=UTF8&refRID=9X1ZZ1Z4KS67D55R3XKS",
                "https://www.amazon.com/Hanes-518T-Mens-Beefy-T-T-Shirt/dp/B00R1MZB7C/ref=pd_sim_193_2?ie=UTF8&psc=1&refRID=TF2TX3JPRDSPFG0X7N0Y",
                "https://www.amazon.com/gp/product/B00HT09WNK/ref=s9_acsd_al_bw_c_x_1?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=merchandised-search-8&pf_rd_r=85ZM36WCTZGSY1GYMMP6&pf_rd_t=101&pf_rd_p=2598495662&pf_rd_i=2476517011",
                "https://www.amazon.com/Levis-Harper-Pocket-V-Neck-T-Shirt/dp/B00V0G8SMK/ref=pd_sim_193_1?ie=UTF8&refRID=DD0K79JPXJYQXD932SXZ",
                "https://www.amazon.com/Seventies-Jersey-Burnout-V-Neck-T-Shirt/dp/B01BE679O8/ref=pd_sim_193_4?ie=UTF8&refRID=6QQ4TA28PNSE20K8A7CA",
                "https://www.amazon.com/Danzi-V-neck-Sports-Outdoor-T-shirt/dp/B01DUQRZQM/ref=pd_sim_193_4?ie=UTF8&refRID=BP8K627NTJTD3MVXTSPV",
                "https://www.amazon.com/Casual-Sleeve-Tri-Blend-Fitted-Cotton/dp/B01BN0NLQK/ref=pd_sim_193_3?ie=UTF8&refRID=EYK68HYZ3G5X389MXQHR",
                "https://www.amazon.com/Yoga-Clothing-You-Blend-V-neck/dp/B00K0PCJJK/ref=pd_sim_193_1?ie=UTF8&refRID=DCRYXWPFXDNN2WKWHBAK",
                "https://www.amazon.com/Canvas-Mens-Triblend-V-Neck-T-Shirt/dp/B006ZS9B0G/ref=pd_sim_193_5?ie=UTF8&refRID=88ZNADAC8EDDCB9Z1KTE",
                "https://www.amazon.com/Bella-3415C-S-Sleeve-V-Neck-Triblend/dp/B007C3HGFK/ref=pd_sbs_468_5?ie=UTF8&psc=1&refRID=KJ0BZ7THX99QT2XJNH3M",
                "https://www.amazon.com/gp/product/B00V0G8SMK/ref=s9_acsd_al_bw_c_x_4?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=merchandised-search-8&pf_rd_r=ZK3JS3CSQ0J4BEVKZTVD&pf_rd_t=101&pf_rd_p=2598495662&pf_rd_i=2476517011",
                "https://www.amazon.com/Levis-Mens-Marble-Henley-Shirt/dp/B00IVJ0UW0/ref=pd_sim_193_3?ie=UTF8&refRID=WKRDQEMTJHE3VG6S705F",
                "https://www.amazon.com/Levis-Mens-Harris-Baseball-Shirt/dp/B00V0G8SW0/ref=pd_sim_193_2?ie=UTF8&refRID=KNQ1SF4XMBJYMTCFD9XQ",
                "https://www.amazon.com/Levis-Mens-Shawger-Graphic-T-Shirt/dp/B01CO06X2C/ref=pd_sim_193_2?ie=UTF8&refRID=5ZNFSNZB4T2Z6DW4HG37",
                "https://www.amazon.com/Levis-Mens-General-Graphic-T-Shirt/dp/B01CO06SEU/ref=pd_sbs_193_4?ie=UTF8&refRID=FN39JYABW9AZGN0FS39G",
                "https://www.amazon.com/Akademiks-Mens-Azuki-Black-6X-LARGE/dp/B01IQ0LK1S/ref=lp_1045558_1_5?s=apparel&ie=UTF8&qid=1472998832&sr=1-5&nodeID=1045558",
                "https://www.amazon.com/Akademiks-A36WB01-Mens-Clark-Pant/dp/B01IQ0IJMQ/ref=pd_sim_sbs_193_2?ie=UTF8&refRID=7CSPQ4EV90JKY1653XKB",
                "https://www.amazon.com/Southpole-Jogger-Pants-Fleece-Pockets/dp/B01FRIPXEC/ref=pd_d0_recs_v2_cwb_ap_2?ie=UTF8&refRID=KJP7SGMEHX32RY6W1HNP",
                "https://www.amazon.com/Southpole-Active-Fleece-Jogger-Pockets/dp/B013VU6P5O/ref=pd_sim_sbs_193_1?ie=UTF8&refRID=758YPWGRXTCK66ZEHJ41",
                "https://www.amazon.com/Southpole-Jogger-Active-Tricot-Fabric/dp/B00QV99HCQ/ref=pd_sbs_193_3?ie=UTF8&refRID=1CGH28F8TPGVS92251WZ",
                "https://www.amazon.com/Southpole-Active-Fleece-Jogger-Pockets/dp/B013VU6P5O/ref=cts_ap_2_vtp",
                "https://www.amazon.com/Rider-Mens-Shape-Flip-White/dp/B0198JE87E/ref=sr_1_42?s=apparel&ie=UTF8&qid=1472998950&sr=1-42&nodeID=14564574011&refinements=p_36%3A2661612011&psc=1",
                "https://www.amazon.com/Starbay-Sunville-Shoes/dp/B00CYI5E8E/ref=cts_sh_2_vtp",
                "https://www.amazon.com/Khombu-Mens-Sandal-Flip-Flops/dp/B01CGQCT72/ref=pd_sim_309_4?ie=UTF8&refRID=XKYAZZSTW2D77FZNB8QG",
                "https://www.amazon.com/BERTELLI-Ultra-Cusioned-Flipflop-Sandal/dp/B01C3C0ZEI/ref=pd_sim_309_2?ie=UTF8&refRID=ZPNYKANM043Q1DF9KNJF",
                "https://www.amazon.com/Bertelli-New-Sandals-Bright-Bi-layered/dp/B00KAGVYYU/ref=pd_sim_309_4?ie=UTF8&refRID=VD9AMVX4VP6XG59TCSQP",
                "https://www.amazon.com/Bertelli-New-Classic-Sandal-Colors/dp/B00KAHVPZC/ref=pd_sim_309_2?ie=UTF8&refRID=2EHF51AKKH9PKTV07A5C",
                "https://www.amazon.com/Undershirt-Sleeveless-Workout-Sports-Slimming/dp/B01IKCHIGI/ref=pd_sim_193_4?ie=UTF8&refRID=RGBS3E5VFZCRKBV80NAR",
                "https://www.amazon.com/Hanes-Label-3-Pack-Comfort-Assortedashirt/dp/B01BLUM7SA/ref=pd_sim_193_5?ie=UTF8&refRID=49XCJ1VWEYR5W7JX876W",
                "https://www.amazon.com/Hanes-Mens-4-Pack-Assorted-A-Shirt/dp/B00LVJ8JPW/ref=pd_sbs_193_2?ie=UTF8&refRID=HS0JQCHMFZSANESDFFX2",
                "https://www.amazon.com/Andrew-Scott-Sleeveless-Muscle-Shirts/dp/B01HXB05DK/ref=pd_sbs_193_5?ie=UTF8&refRID=HS0JQCHMFZSANESDFFX2",
                "https://www.amazon.com/Dimore-Classic-Undershirt-Athletic-Sleeveless/dp/B01F89HHTE/ref=pd_sim_193_1?ie=UTF8&refRID=DY21MEJW8JR74R6EJDHS",
                "https://www.amazon.com/ALiberSoul-Coconut-Boardshorts-Tropical-Swimming/dp/B01EUX7BRW/ref=pd_rhf_dp_s_cp_3?ie=UTF8&pd_rd_i=B01EUX7BRW&pd_rd_r=E0HV1G9DVXCGVPQMX82B&pd_rd_w=IwyQM&pd_rd_wg=MDEd0&refRID=E0HV1G9DVXCGVPQMX82B",
                "https://www.amazon.com/ALiberSoul-Coconut-Tropical-Design-Boardshorts/dp/B01FBPFPW6/ref=pd_sim_193_2?ie=UTF8&refRID=WKQQE926YKPD7BA1JSA6",
                "https://www.amazon.com/ALiberSoul-Style-Coconut-Print-Boardshorts/dp/B01G3UPMAI/ref=pd_sim_193_3?ie=UTF8&refRID=JWDJ9K718FKQ3583BB9M",
                "https://www.amazon.com/Kanu-Surf-Mens-Voyage-Trunks/dp/B015VLNDVU/ref=pd_sim_193_4?ie=UTF8&refRID=RFTY6N92C1WX02PWAKG1",
                "https://www.amazon.com/Kanu-Surf-Mens-Miles-Trunk/dp/B00FMMTQ6U/ref=pd_sim_193_4?ie=UTF8&refRID=V30YTZH66FMBJFP1JRS8",
                "https://www.amazon.com/Kanu-Surf-Mens-Legacy-Trunk/dp/B00FMMTUIO/ref=pd_sim_193_3?ie=UTF8&refRID=HE6R2TZWBGX49RSHZWFD",
                "https://www.amazon.com/Kanu-Surf-Mens-Ultimate-Trunk/dp/B00S9X3K52/ref=pd_sim_193_4?ie=UTF8&refRID=4HJGXFEN5W0C0JEKTKF2",
                "https://www.amazon.com/HEAD-Mens-Jackpot-Spacedye-Short/dp/B01AH12CKW/ref=pd_sim_193_2?ie=UTF8&refRID=HHWNFQG05K4ETAJP72S6",
                "https://www.amazon.com/HEAD-Mens-Efficient-Textured-Short/dp/B01AANW5MC/ref=pd_sim_193_2?ie=UTF8&refRID=PB6M6Z2QDJB4X913CFGV",
                "https://www.amazon.com/HEAD-Mens-Game-Performance-Short/dp/B01AH12CFC/ref=pd_sbs_193_5?ie=UTF8&refRID=8FVC9YY8XGF4B7C34FSC",
                "https://www.amazon.com/HEAD-Mens-Break-Point-Short/dp/B00Q81S7YG/ref=pd_sbs_193_1?ie=UTF8&refRID=D6D6E92X7VN8QCV4KVM4",
                "https://www.amazon.com/HEAD-Space-Hypertek-Performance-T-Shirt/dp/B00SO6TZ44/ref=pd_sim_193_2?ie=UTF8&refRID=0W5SD9RMAP7VTYS19603",
                "https://www.amazon.com/HEAD-Mens-Spacedye-Hypertek-Crew/dp/B01A63QVC6/ref=pd_sbs_193_3?ie=UTF8&refRID=95V81TFSDY2P2T5MWBAB",
                "https://www.amazon.com/Mada-Shirts-Checkered-Sleeve-Shirt/dp/B01F83Y28O/ref=pd_sim_193_1?ie=UTF8&refRID=V92N5WYSXFW98RFG7GQD",
                "https://www.amazon.com/Casual-Shirts-Sleeve-Dress-Shirt/dp/B01DIXUO92/ref=pd_sim_193_2?ie=UTF8&refRID=TQENFR26PWT8EK6HDGDN",
                "https://www.amazon.com/TUNEVUSE-Casual-Shirts-Stripes-Sleeves/dp/B01H1MQOU4/ref=pd_rhf_dp_s_cp_2?ie=UTF8&pd_rd_i=B01H1MQOU4&pd_rd_r=1SPC6X3RPKWGPX5FSSX2&pd_rd_w=Ci7bg&pd_rd_wg=h1SLx&refRID=1SPC6X3RPKWGPX5FSSX2",
                "https://www.amazon.com/Bentibo-Burgundy-Casual-Sleeve-Button/dp/B01FU8TLOC/ref=pd_rhf_dp_s_cp_3?ie=UTF8&pd_rd_i=B01FU8TLOC&pd_rd_r=3BT7GMYYWMSGPW7QYMQ5&pd_rd_w=OkVdy&pd_rd_wg=AyiPH&refRID=3BT7GMYYWMSGPW7QYMQ5",
                "https://www.amazon.com/Casual-Shirts-Stripes-Short-Sleeve/dp/B01GRTKPGG/ref=pd_sim_193_5?ie=UTF8&refRID=PA22DZ2RRTA35A0AFS5H",
                "https://www.amazon.com/Coofandy-Collar-Sleeve-Casual-Shirts/dp/B01DNAI5FU/ref=pd_sim_193_4?ie=UTF8&refRID=XEXYGMSRCCJTA7R2D0CT",
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

            UsaAmazonSummaryProductProcessor usaAmazonSummaryProductProcessor = new UsaAmazonSummaryProductProcessor();

            UsaAmazonData usaAmazonData = null;

            try {
                usaAmazonData = usaAmazonSummaryProductProcessor.getSummaryProductByUrl(url);
            } catch (Exception e) {
                System.out.println("error for " + url);
                continue;
            }


            System.out.println(usaAmazonData.getTitle());
            System.out.println(usaAmazonData.getImageUrl());
            System.out.println(usaAmazonData.getPrice());
            System.out.println(usaAmazonData.getDisPrice());

            String fileName = StringUtils.filterAndTrim(UUID.randomUUID().toString(), Arrays.asList("-")) + ".jpg";

            HttpUtils.getImage(usaAmazonData.getImageUrl(), new File(file, fileName));

            stringBuilder.append("insert into cb_goods (link,title,cover,price,discount_price) values (\"");

            stringBuilder.append(url + "\",\"" + usaAmazonData.getTitle() + "\",\"uploads/2016/08/30/" + fileName + "\"," + usaAmazonData.getPrice() + "," + usaAmazonData.getDisPrice() + ");");

            System.out.println(stringBuilder.toString());

            FileUtil.appendString(sqlFile, stringBuilder.toString() + "\n");
        }


        return "ok";
    }

}

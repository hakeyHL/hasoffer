package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.PushVo;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;
import hasoffer.core.bo.push.*;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.push.IPushService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value = "/push")
public class PushController {
    static Map<Website, String> packageMap = new HashMap<Website, String>();

    static {
        packageMap.put(Website.SNAPDEAL, "com.snapdeal.main");
        packageMap.put(Website.FLIPKART, "com.flipkart.android");
        packageMap.put(Website.AMAZON, "in.amazon.mShop.android.shopping");
        packageMap.put(Website.PAYTM, "net.one97.paytm");
        packageMap.put(Website.EBAY, "com.ebay.mobile");
        packageMap.put(Website.SHOPCLUES, "com.shopclues");
        packageMap.put(Website.INFIBEAM, "com.infibeam.infibeamapp");
        packageMap.put(Website.MYNTRA, "com.myntra.android");
        packageMap.put(Website.JABONG, "com.jabong.android");
        packageMap.put(Website.VOONIK, "com.voonik.android");
    }

    @Resource
    IPushService pushService;
    private Logger logger = LoggerFactory.getLogger(PushController.class);

    @RequestMapping(value = "/pushIndex")
    public ModelAndView PushIndex() {
        ModelAndView mav = new ModelAndView("push/push");
        List<MarketChannel> channles = pushService.getAllMarketChannels();
        List<Website> websites = new ArrayList<>();
        Class classzz = Website.class;
        for (Object o : classzz.getEnumConstants()) {
            websites.add((Website) o);
        }
        //获得所有APP版本
        List<String> versions = pushService.getAllAppVersions();
        mav.addObject("channels", channles);
        mav.addObject("websites", websites);
        mav.addObject("versions", versions);
        return mav;
    }

    @RequestMapping(value = "/pushMessage")
    public ModelAndView PushMessage(PushVo pushVol) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("errorCode", "00000");
        mv.addObject("msg", "ok");
        //1.先按版本推
        //2.推安装了选定app的用户
        //3.推哪些渠道的用户
        //4.数量控制
        try {
            List<String> gcmTokens = new ArrayList<String>();
            AppPushMessage message = new AppPushMessage(
                    new AppMsgDisplay(pushVol.getOutline(), pushVol.getTitle(), pushVol.getContent()),
                    new AppMsgClick(AppMsgClickType.valueOf(pushVol.getMessageType()), pushVol.getValue(), packageMap.get(pushVol.getWebsite()))
            );
            AppPushBo pushBo = new AppPushBo("5x1", "15:10", message);
            //安装了指定app的、指定数量、指定包名、指定类型、指定id推送
            List<UrmDevice> urmDevices = pushService.getGcmTokens(pushVol.getVersion());
            for (UrmDevice urmDevice : urmDevices) {
                String shopApps = urmDevice.getShopApp();
                String[] split = shopApps.split(",");
                for (String str : split) {
                    if (urmDevice.getMarketChannel() != null) {
                        if (str.equals(pushVol.getWebsite()[0]) && urmDevice.getMarketChannel().name().equals(pushVol.getChannel())) {
                            if (gcmTokens.size() < pushVol.getNumber() && !StringUtils.isEmpty(urmDevice.getGcmToken())) {
                                gcmTokens.add(urmDevice.getGcmToken());
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            int i = 0;
            for (String gcmToken : gcmTokens) {
                System.out.println("____  " + i + "  ____");
                pushService.push(gcmToken, pushBo);
                i++;
            }
        } catch (Exception e) {
            mv.addObject("msg", e.getMessage());
            return mv;
        }
        return mv;
    }
}

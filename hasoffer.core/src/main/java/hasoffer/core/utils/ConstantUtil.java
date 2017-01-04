package hasoffer.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hs on 2016年11月30日D
 * Time 17:18
 */
public class ConstantUtil {
    public static final String API_ERRORCODE_SUCCESS = "00000";
    public static final String API_ERRORCODE_FAILED_LOGIC = "10000";
    public static final String API_NAME_ERRORCODE = "errorCode";
    public static final String API_NAME_MSG = "msg";
    public static final String API_NAME_MSG_SUCCESS = "success";
    public static final String API_NAME_DATA = "data";
    //变量名定义规则
    //哪个服务的,操作者,哪个对象.操作类型
    public static final long API_ONE_BILLION_NUMBER = 1000000000;
    public static final String SOLR_DEFAULT_VALUE_NOTEMPTY_FIELD = "-|-";
    public static final String SOLR_DEFAULT_MULTIVALUEDVALUE_FIELD_SPLIT = "HASOFFER";
    public static final String API_DEALS_ = "API_DEALS_";//deal列表缓存的key前缀
    //    public static final String API_FILTER_PARAMS_ = "API_FILTER_PARAMS_";//缓存关键字以及类目id和级别下的筛选参数列表的key
    public static final String API_SOLR_PTMSTDSKU_CATEGORY_SEARCH = "API_SOLR_PTMSTDSKU_CATEGORY_SEARCH_";
    public static final String API_PREFIX_CACAHE_CMP_CMPLIST_ = "API_PREFIX_CACAHE_CMP_CMPLIST_";

    public static final String API_PREFIX_CACAHE_PTMSTDSKU_ = "API_PREFIX_CACAHE_PTMSTDSKU_";
    public static final String API_PREFIX_CACAHE_PTMPRODUCT_ = "API_PREFIX_CACAHE_PTMPRODUCT_";
    public static final String API_PREFIX_CACAHE_PTMSTDPRICE_ = "API_PREFIX_CACAHE_PTMSTDPRICE_";
    public static final String API_PREFIX_CACAHE_PTMSKU_ = "API_PREFIX_CACAHE_PTMSKU_";
    public static final Map<String, String> API_CATEGORY_FILTER_PARAMS_MAP = new HashMap<>();
    public static final Map<String, String> API_PTMSTDSKU_PARAM_MEAN_MAP = new HashMap<>();

    static {
        //下面为General属性---------------------------------------------------------------------------------------------
        //格式是这样的 February 3, 2016 (Official)
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Launch_Date", "Launch Date");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Brand", "Brand");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Model", "Model");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Operating_System", "Operating System");
        //只要,前面的
        API_CATEGORY_FILTER_PARAMS_MAP.put("SIM_Slot", "SIM Slot(s)");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("SIM_Size", "SIM Size");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Network_Support", "Network Support");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Fingerprint_Sensor", "Fingerprint Sensor");


        //下面为Design属性----------------------------------------------------------------------------------------------
        //147 grams 只要值,注意下单位
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Weight", "Weight");

        //下面为Display属性---------------------------------------------------------------------------------------------
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Screen_Resolution", "Screen Resolution");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryScreenSize", "Screen Size");
        //267 ppi  只要值,注意下单位
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Pixel_Density", "Pixel Density");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Touch_Screen", "Touch Screen");
        //73.42 % 只要值
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Screen_to_Body_Ratio", "Screen to Body Ratio");

        //下面为Performance属性-----------------------------------------------------------------------------------------
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Processor", "Processor");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Graphics", "Graphics");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryRam", "RAM");

        //下面为Storage属性---------------------------------------------------------------------------------------------
//        API_CATEGORY_FILTER_PARAMS_MAP.put("queryInternalMemory", "Internal Memory");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Expandable_Memory", "Expandable Memory");


        //关于Camera属性的存储,之前是根据值判断的,不靠谱
        //判断大name 如果是 Main Camera 其Resolution 为Primary　Camera 否则为Front Camera  --->Secondary Camera

        //下面为摄像头属性----------------------------------------------------------------------------------------------
        API_CATEGORY_FILTER_PARAMS_MAP.put("querySecondaryCamera", "Secondary Camera");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryPrimaryCamera", "Primary Camera");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Sensor", "Sensor");
        //能与不能 value有且不为No 即为能
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Autofocus", "Autofocus");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("secondaryAutofocus", "secondaryAutofocus");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Aperture", "Aperture");
        //能与不能 No为不能
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Flash", "Flash");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("SecondaryFlash", "SecondaryFlash");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Image_Resolution", "Image Resolution");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Camera_Features", "Camera Features");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Video_Recording", "Video Recording");

        //下面为Battery 属性--------------------------------------------------------------------------------------------
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryBatteryCapacity", "Battery Capacity");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Type", "Type");
        //能与不能
//        API_CATEGORY_FILTER_PARAMS_MAP.put("User_Replaceable", "User Replaceable");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Quick_Charging", "Quick Charging");

        //下面为Network & Connectivity----------------------------------------------------------------------------------
//        API_CATEGORY_FILTER_PARAMS_MAP.put("VoLTE", "VoLTE");
        //支不支持
        API_CATEGORY_FILTER_PARAMS_MAP.put("WiFi", "Wi-Fi");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Bluetooth", "Bluetooth");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("GPS", "GPS");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("NFC", "NFC");
//        API_CATEGORY_FILTER_PARAMS_MAP.put("GPS", "GPS");

        //Multimedia 属性-----------------------------------------------------------------------------------------------
//        API_CATEGORY_FILTER_PARAMS_MAP.put("FM_Radio", "FM Radio");//能与不能
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Loudspeaker", "Loudspeaker");//能与不能
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Audio_Jack", "Audio Jack");


        //Special Features属性------------------------------------------------------------------------------------------
//        API_CATEGORY_FILTER_PARAMS_MAP.put("Other_Sensors", "Other Sensors");


        //=====================================分界线=============================================

//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("General SIM Slot(s)", "Dual SIM phones allow you to use 2 SIMs/number in the same phone.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Screen Size", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Processor", "	Processor acts as the brain of the device and manages tasks such as opening an app. More number of cores allows you to smoothly execute multiple tasks /apps such as playing movie, checking email, etc. simultaneously.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Internal Memory", "More internal storage allows installation of more apps as well as ability to save songs, images, videos, etc.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Expandable Memory", "	An external microSD card supplements the internal storage and can be used for saving songs, images, videos, etc.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Resolution", "A camera of higher resolution takes more detailed photos, which will look sharp on a large screen as well.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Battery Capacity", "	Higher battery capacity generally means longer battery life, but it also depends upon other factors like phone`s screen, processor, OS, optimisations by the brand and personal usage.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Operating System", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Launch Date", "");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Brand", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Model", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Custom UI", "The brand’s own interface that runs on top of Android OS, and offers certain additional or different features.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("General SIM Size", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Fingerprint Sensor", "It allows you to secure your phone by using your fingerprints to unlock the smartphone");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Quick Charging", "Allows you to charge your phone faster (up to 50% in half an hour).Usually works with the phone’s official charger.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Dimensions", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Weight", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Build Material", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Colours", "");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Screen Resolution", "In phone of the same screen size,higher  resolution indicates a sharper display.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Pixel Density", "High pixel density indicates a sharper display.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Display Type", "In-Plane Switching Liquid Crystal Display (IPS LCD) offers more sharpness, shows more realistic looking images, displays more natural colours and perform better in bright sunlight compared to LCD displays.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Touch Screen", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Screen to Body Ratio", "Screen to body ratio tells how much of the phone size is covered by the screen; the higher the better");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Chipset", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Architecture", "The processors are either 32-bit or 64-bit,more the bits, faster the processor, although the apps and OS should also be compatible to utilize the potential of more bits.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Graphics", "Graphics processor enables smooth processing of 3D games& HD videos. It also shares the load of processor and thus conserves battery life.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("RAM", "More RAM results in faster loading and smoother running of multiple apps simultaneously.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("USB OTG Support", "With USB On-The-Go, your phone can read a USB flash drive.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Sensor", "Sensor of the camera determines how much light it uses to create an image. Bigger sensors generally produce better photo quality.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Autofocus	", "With Autofocus, the phone’s camera is able to lock the focus on the subject automatically.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Aperture", "	perture indicates the lens opening for letting in the light; lower aperture number means larger opening and thus better images.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Optical Image Stabilisation", "Optical Image Stabilisation compensates for hand shakes while shooting pictures or recording videos to offer stable results.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Flash", "To capture photos/video in low-light environments, flash helps in illuminating the scene, Dual-colour LED flash has two colour tones-white and amber, offering better colour accuracy.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Image Resolution", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Settings", "Depending upon the light in the surroundings, you can increase or decrease the exposure to get better images. ISO control lets you adjust the ISO levels. More the ISO, more effective the camera would be in capturing images in low light, although it would result in more grain.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Shooting Modes", "HDR stands for high dynamic range and it results in better quality by capturing three different images at different exposures and then combining them together.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERA Camera Features", "Using Digital Zoom, the camera zooms in on the subject using software (not a physical lens), but the image quality gets reduced.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("MAIN CAMERAVideo Recording", "HD / Full HD / 4K video looks extremely sharp & clear on a compatible TV / monitor. More frame rate (30 - 120 frames per sec) results in smoother videos.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("FRONT CAMERA Resolution", "A camera of higher resolution takes more detailed photos, which will look sharp on a large screen as well.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("FRONT CAMERA Sensor", "Sensor of the camera determines how much light it uses to create an image. Bigger sensors generally produce better photo quality.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("FRONT CAMERA Autofocus", "With Autofocus, the phone’s camera is able to lock the focus on the subject automatically.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("FRONT CAMERA Flash	", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Type", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("User Replaceable", "A device whose battery can be removed and swapped. Useful if you plan to buy a spare battery.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Quick Charging", "Allows you to charge your phone faster (up to 50% in half an hour). Usually works with the phone`s official charger.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("SIM Size", "Hybrid SIM slot can be used either as a nano-SIM slot or a memory card slot.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Network Support", "4G connection provides very fast internet access up to 100 Mbps. Along with voice calls and messages, 3G enables fast internet access of up to 42.2 Mbps, allowing live TV, multiplayer gaming, high speed downloads, etc. With 2G, you can get all cellular features, but basic internet capabilities.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("VoLTE", "Voice over LTE offers better call quality, along with the ability to use voice and data at the same time on the 4G network.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("SIM 1", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("SIM 2", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Wi-Fi", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Wi-Fi Features", "It allows you to share data between two devices using Wi-Fi, With Wi-Fi hotspot, you can turn your phone into a Wi-Fi network and connect other devices to it.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Bluetooth", "It allows your phone to share files, photos, videos, etc. wirelessly with other Bluetooth compatible devices like laptops, tables, phones, etc.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("GPS", "GPS allows you to see your current location and find direction to your destination using apps like Google maps.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("NFC", "NFC enables faster pairing of two devices and secure data sharing between them. It can also enable features such as one-tap payment,etc.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("USB Connectivity", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("FM Radio", "FM Radio allows you to listen to music, news, etc. from radio stations, even when you are travelling.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Loudspeaker", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Audio Jack", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Fingerprint Sensor", "It allows you to secure your phone by using your fingerprints to unlock the smartphone.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Fingerprint Sensor Position", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Other Sensors", "");
    }
}

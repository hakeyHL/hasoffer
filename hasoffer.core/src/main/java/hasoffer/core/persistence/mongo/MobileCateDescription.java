package hasoffer.core.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created on 2016/10/24.
 */
@Document(collection = "MobileCateDescription")
public class MobileCateDescription {

    @Id
    private long id;//ptmproductId

    private String launch_date;
    private String brand;
    private String model;
    private String operating_system;
    private String custom_ui;
    private String sim_slot;
    private String dimensions;
    private String weight;
    private String build_material;
    private String screen_size;
    private String screen_resolution;
    private String pixel_density;
    private String chipset;
    private String processor;
    private String architecture;
    private String graphics;
    private String ram;
    private String internal_memory;
    private String expandable_memory;
    private String usb_otg_support;
    private String main_camera_resolution;
    private String main_camera_sensor;
    private String main_camera_autofocus;
    private String main_camera_aperture;
    private String main_camera_optical_image_stabilisation;
    private String main_camera_flash;
    private String main_camera_image_resolution;
    private String main_camera_camera_features;
    private String main_camera_video_recording;
    private String front_camera_resolution;
    private String front_camera_sensor;
    private String front_camera_autofocus;
    private String capacity;
    private String type;
    private String user_replaceable;
    private String quick_charging;
    private String sim_size;
    private String network_support;
    private String volte;
    private String sim_1;
    private String sim_2;
    private String bluetooth;
    private String gps;
    private String nfc;
    private String usb_connectivity;
    private String fm_radio;
    private String loudspeaker;
    private String audio_jack;
    private String fingerprint_sensor;
    private String fingerprint_sensor_position;
    private String other_sensors;

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getAudio_jack() {
        return audio_jack;
    }

    public void setAudio_jack(String audio_jack) {
        this.audio_jack = audio_jack;
    }

    public String getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(String bluetooth) {
        this.bluetooth = bluetooth;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBuild_material() {
        return build_material;
    }

    public void setBuild_material(String build_material) {
        this.build_material = build_material;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getChipset() {
        return chipset;
    }

    public void setChipset(String chipset) {
        this.chipset = chipset;
    }

    public String getCustom_ui() {
        return custom_ui;
    }

    public void setCustom_ui(String custom_ui) {
        this.custom_ui = custom_ui;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getExpandable_memory() {
        return expandable_memory;
    }

    public void setExpandable_memory(String expandable_memory) {
        this.expandable_memory = expandable_memory;
    }

    public String getFingerprint_sensor() {
        return fingerprint_sensor;
    }

    public void setFingerprint_sensor(String fingerprint_sensor) {
        this.fingerprint_sensor = fingerprint_sensor;
    }

    public String getFingerprint_sensor_position() {
        return fingerprint_sensor_position;
    }

    public void setFingerprint_sensor_position(String fingerprint_sensor_position) {
        this.fingerprint_sensor_position = fingerprint_sensor_position;
    }

    public String getFm_radio() {
        return fm_radio;
    }

    public void setFm_radio(String fm_radio) {
        this.fm_radio = fm_radio;
    }

    public String getFront_camera_autofocus() {
        return front_camera_autofocus;
    }

    public void setFront_camera_autofocus(String front_camera_autofocus) {
        this.front_camera_autofocus = front_camera_autofocus;
    }

    public String getFront_camera_resolution() {
        return front_camera_resolution;
    }

    public void setFront_camera_resolution(String front_camera_resolution) {
        this.front_camera_resolution = front_camera_resolution;
    }

    public String getFront_camera_sensor() {
        return front_camera_sensor;
    }

    public void setFront_camera_sensor(String front_camera_sensor) {
        this.front_camera_sensor = front_camera_sensor;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public String getGraphics() {
        return graphics;
    }

    public void setGraphics(String graphics) {
        this.graphics = graphics;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInternal_memory() {
        return internal_memory;
    }

    public void setInternal_memory(String internal_memory) {
        this.internal_memory = internal_memory;
    }

    public String getLaunch_date() {
        return launch_date;
    }

    public void setLaunch_date(String launch_date) {
        this.launch_date = launch_date;
    }

    public String getLoudspeaker() {
        return loudspeaker;
    }

    public void setLoudspeaker(String loudspeaker) {
        this.loudspeaker = loudspeaker;
    }

    public String getMain_camera_aperture() {
        return main_camera_aperture;
    }

    public void setMain_camera_aperture(String main_camera_aperture) {
        this.main_camera_aperture = main_camera_aperture;
    }

    public String getMain_camera_autofocus() {
        return main_camera_autofocus;
    }

    public void setMain_camera_autofocus(String main_camera_autofocus) {
        this.main_camera_autofocus = main_camera_autofocus;
    }

    public String getMain_camera_camera_features() {
        return main_camera_camera_features;
    }

    public void setMain_camera_camera_features(String main_camera_camera_features) {
        this.main_camera_camera_features = main_camera_camera_features;
    }

    public String getMain_camera_flash() {
        return main_camera_flash;
    }

    public void setMain_camera_flash(String main_camera_flash) {
        this.main_camera_flash = main_camera_flash;
    }

    public String getMain_camera_image_resolution() {
        return main_camera_image_resolution;
    }

    public void setMain_camera_image_resolution(String main_camera_image_resolution) {
        this.main_camera_image_resolution = main_camera_image_resolution;
    }

    public String getMain_camera_optical_image_stabilisation() {
        return main_camera_optical_image_stabilisation;
    }

    public void setMain_camera_optical_image_stabilisation(String main_camera_optical_image_stabilisation) {
        this.main_camera_optical_image_stabilisation = main_camera_optical_image_stabilisation;
    }

    public String getMain_camera_resolution() {
        return main_camera_resolution;
    }

    public void setMain_camera_resolution(String main_camera_resolution) {
        this.main_camera_resolution = main_camera_resolution;
    }

    public String getMain_camera_sensor() {
        return main_camera_sensor;
    }

    public void setMain_camera_sensor(String main_camera_sensor) {
        this.main_camera_sensor = main_camera_sensor;
    }

    public String getMain_camera_video_recording() {
        return main_camera_video_recording;
    }

    public void setMain_camera_video_recording(String main_camera_video_recording) {
        this.main_camera_video_recording = main_camera_video_recording;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getNetwork_support() {
        return network_support;
    }

    public void setNetwork_support(String network_support) {
        this.network_support = network_support;
    }

    public String getNfc() {
        return nfc;
    }

    public void setNfc(String nfc) {
        this.nfc = nfc;
    }

    public String getOperating_system() {
        return operating_system;
    }

    public void setOperating_system(String operating_system) {
        this.operating_system = operating_system;
    }

    public String getOther_sensors() {
        return other_sensors;
    }

    public void setOther_sensors(String other_sensors) {
        this.other_sensors = other_sensors;
    }

    public String getPixel_density() {
        return pixel_density;
    }

    public void setPixel_density(String pixel_density) {
        this.pixel_density = pixel_density;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getQuick_charging() {
        return quick_charging;
    }

    public void setQuick_charging(String quick_charging) {
        this.quick_charging = quick_charging;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getScreen_resolution() {
        return screen_resolution;
    }

    public void setScreen_resolution(String screen_resolution) {
        this.screen_resolution = screen_resolution;
    }

    public String getScreen_size() {
        return screen_size;
    }

    public void setScreen_size(String screen_size) {
        this.screen_size = screen_size;
    }

    public String getSim_1() {
        return sim_1;
    }

    public void setSim_1(String sim_1) {
        this.sim_1 = sim_1;
    }

    public String getSim_2() {
        return sim_2;
    }

    public void setSim_2(String sim_2) {
        this.sim_2 = sim_2;
    }

    public String getSim_size() {
        return sim_size;
    }

    public void setSim_size(String sim_size) {
        this.sim_size = sim_size;
    }

    public String getSim_slot() {
        return sim_slot;
    }

    public void setSim_slot(String sim_slot) {
        this.sim_slot = sim_slot;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsb_connectivity() {
        return usb_connectivity;
    }

    public void setUsb_connectivity(String usb_connectivity) {
        this.usb_connectivity = usb_connectivity;
    }

    public String getUsb_otg_support() {
        return usb_otg_support;
    }

    public void setUsb_otg_support(String usb_otg_support) {
        this.usb_otg_support = usb_otg_support;
    }

    public String getUser_replaceable() {
        return user_replaceable;
    }

    public void setUser_replaceable(String user_replaceable) {
        this.user_replaceable = user_replaceable;
    }

    public String getVolte() {
        return volte;
    }

    public void setVolte(String volte) {
        this.volte = volte;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "MobileCateDescription{" +
                "architecture='" + architecture + '\'' +
                ", id=" + id +
                ", launch_date='" + launch_date + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", operating_system='" + operating_system + '\'' +
                ", custom_ui='" + custom_ui + '\'' +
                ", sim_slot='" + sim_slot + '\'' +
                ", dimensions='" + dimensions + '\'' +
                ", weight='" + weight + '\'' +
                ", build_material='" + build_material + '\'' +
                ", screen_size='" + screen_size + '\'' +
                ", screen_resolution='" + screen_resolution + '\'' +
                ", pixel_density='" + pixel_density + '\'' +
                ", chipset='" + chipset + '\'' +
                ", processor='" + processor + '\'' +
                ", graphics='" + graphics + '\'' +
                ", ram='" + ram + '\'' +
                ", internal_memory='" + internal_memory + '\'' +
                ", expandable_memory='" + expandable_memory + '\'' +
                ", usb_otg_support='" + usb_otg_support + '\'' +
                ", main_camera_resolution='" + main_camera_resolution + '\'' +
                ", main_camera_sensor='" + main_camera_sensor + '\'' +
                ", main_camera_autofocus='" + main_camera_autofocus + '\'' +
                ", main_camera_aperture='" + main_camera_aperture + '\'' +
                ", main_camera_optical_image_stabilisation='" + main_camera_optical_image_stabilisation + '\'' +
                ", main_camera_flash='" + main_camera_flash + '\'' +
                ", main_camera_image_resolution='" + main_camera_image_resolution + '\'' +
                ", main_camera_camera_features='" + main_camera_camera_features + '\'' +
                ", main_camera_video_recording='" + main_camera_video_recording + '\'' +
                ", front_camera_resolution='" + front_camera_resolution + '\'' +
                ", front_camera_sensor='" + front_camera_sensor + '\'' +
                ", front_camera_autofocus='" + front_camera_autofocus + '\'' +
                ", capacity='" + capacity + '\'' +
                ", type='" + type + '\'' +
                ", user_replaceable='" + user_replaceable + '\'' +
                ", quick_charging='" + quick_charging + '\'' +
                ", sim_size='" + sim_size + '\'' +
                ", network_support='" + network_support + '\'' +
                ", volte='" + volte + '\'' +
                ", sim_1='" + sim_1 + '\'' +
                ", sim_2='" + sim_2 + '\'' +
                ", bluetooth='" + bluetooth + '\'' +
                ", gps='" + gps + '\'' +
                ", nfc='" + nfc + '\'' +
                ", usb_connectivity='" + usb_connectivity + '\'' +
                ", fm_radio='" + fm_radio + '\'' +
                ", loudspeaker='" + loudspeaker + '\'' +
                ", audio_jack='" + audio_jack + '\'' +
                ", fingerprint_sensor='" + fingerprint_sensor + '\'' +
                ", fingerprint_sensor_position='" + fingerprint_sensor_position + '\'' +
                ", other_sensors='" + other_sensors + '\'' +
                '}';
    }
}

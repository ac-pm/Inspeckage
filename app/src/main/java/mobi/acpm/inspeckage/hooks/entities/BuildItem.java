package mobi.acpm.inspeckage.hooks.entities;

/**
 * Created by acpm on 29/04/17.
 */

public class BuildItem {

    public String type;
    public String name;
    public String value;
    public String newValue;
    public boolean enable;

    public BuildItem(String type, String name, String value, String newValue, boolean enable){
        this.type = type;
        this.name = name;
        this.value = value;
        this.newValue = newValue;
        this.enable = enable;
    }
    /**
    private String board;
    private String release;
    private String brand;
    private String cpu_abi;
    private String cpu_abi2;
    private String device_key;
    private String display_key;
    private String fingerprint_key;
    private String hardware_key;
    private String host_key;
    private String id_key;
    private String manufacturer_key;
    private String model_key;
    private String product_key;
    private String radio_key;
    private String tags_key;
    private long time_key;
    private String type_key;
    private String user_key;
    private String codename_key;
    private String incremental_key;
    private String release_key;
    private String sdk_key;
    private int sdk_int_key;

    private String new_board;
    private String new_release;
    private String new_brand;
    private String new_cpu_abi;
    private String new_cpu_abi2;
    private String new_device_key;
    private String new_display_key;
    private String new_fingerprint_key;
    private String new_hardware_key;
    private String new_host_key;
    private String new_id_key;
    private String new_manufacturer_key;
    private String new_model_key;
    private String new_product_key;
    private String new_radio_key;
    private String new_tags_key;
    private long new_time_key;
    private String new_type_key;
    private String new_user_key;
    private String new_codename_key;
    private String new_incremental_key;
    private String new_release_key;
    private String new_sdk_key;
    private int new_sdk_int_key;

    public boolean enable_board;
    public boolean enable_release;
    public boolean enable_brand;
    public boolean enable_cpu_abi;
    public boolean enable_cpu_abi2;
    public boolean enable_device_key;
    public boolean enable_display_key;
    public boolean enable_fingerprint_key;
    public boolean enable_hardware_key;
    public boolean enable_host_key;
    public boolean enable_id_key;
    public boolean enable_manufacturer_key;
    public boolean enable_model_key;
    public boolean enable_product_key;
    public boolean enable_radio_key;
    public boolean enable_tags_key;
    public boolean enable_time_key;
    public boolean enable_type_key;
    public boolean enable_user_key;
    public boolean enable_codename_key;
    public boolean enable_incremental_key;
    public boolean enable_release_key;
    public boolean enable_sdk_key;
    public boolean enable_sdk_int_key;

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
        setNew_board(board);
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
        setNew_release(release);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
        setNew_brand(brand);
    }

    public String getCpu_abi() {
        return cpu_abi;
    }

    public void setCpu_abi(String cpu_abi) {
        this.cpu_abi = cpu_abi;
        setNew_cpu_abi(cpu_abi);
    }

    public String getCpu_abi2() {
        return cpu_abi2;
    }

    public void setCpu_abi2(String cpu_abi2) {
        this.cpu_abi2 = cpu_abi2;
        setNew_cpu_abi2(cpu_abi2);
    }

    public String getDevice_key() {
        return device_key;
    }

    public void setDevice_key(String device_key) {
        this.device_key = device_key;
        setNew_device_key(device_key);
    }

    public String getDisplay_key() {
        return display_key;
    }

    public void setDisplay_key(String display_key) {
        this.display_key = display_key;
        setNew_display_key(display_key);
    }

    public String getFingerprint_key() {
        return fingerprint_key;
    }

    public void setFingerprint_key(String fingerprint_key) {
        this.fingerprint_key = fingerprint_key;
        setNew_fingerprint_key(fingerprint_key);
    }

    public String getHardware_key() {
        return hardware_key;
    }

    public void setHardware_key(String hardware_key) {
        this.hardware_key = hardware_key;
        setNew_hardware_key(hardware_key);
    }

    public String getHost_key() {
        return host_key;
    }

    public void setHost_key(String host_key) {
        this.host_key = host_key;
        setNew_host_key(host_key);
    }

    public String getId_key() {
        return id_key;
    }

    public void setId_key(String id_key) {
        this.id_key = id_key;
        setNew_id_key(id_key);
    }

    public String getManufacturer_key() {
        return manufacturer_key;
    }

    public void setManufacturer_key(String manufacturer_key) {
        this.manufacturer_key = manufacturer_key;
        setNew_manufacturer_key(manufacturer_key);
    }

    public String getModel_key() {
        return model_key;
    }

    public void setModel_key(String model_key) {
        this.model_key = model_key;
        setNew_model_key(model_key);
    }

    public String getProduct_key() {
        return product_key;
    }

    public void setProduct_key(String product_key) {
        this.product_key = product_key;
        setNew_product_key(product_key);
    }

    public String getRadio_key() {
        return radio_key;
    }

    public void setRadio_key(String radio_key) {
        this.radio_key = radio_key;
        setNew_radio_key(radio_key);
    }

    public String getTags_key() {
        return tags_key;
    }

    public void setTags_key(String tags_key) {
        this.tags_key = tags_key;
        setNew_tags_key(tags_key);
    }

    public long getTime_key() {
        return time_key;
    }

    public void setTime_key(long time_key) {
        this.time_key = time_key;
        setNew_time_key(time_key);
    }

    public String getType_key() {
        return type_key;
    }

    public void setType_key(String type_key) {
        this.type_key = type_key;
        setNew_type_key(type_key);
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
        setNew_user_key(user_key);
    }

    public String getCodename_key() {
        return codename_key;
    }

    public void setCodename_key(String codename_key) {
        this.codename_key = codename_key;
        setNew_codename_key(codename_key);
    }

    public String getIncremental_key() {
        return incremental_key;
    }

    public void setIncremental_key(String incremental_key) {
        this.incremental_key = incremental_key;
        setNew_incremental_key(incremental_key);
    }

    public String getRelease_key() {
        return release_key;
    }

    public void setRelease_key(String release_key) {
        this.release_key = release_key;
        setNew_release_key(release_key);
    }

    public String getSdk_key() {
        return sdk_key;
    }

    public void setSdk_key(String sdk_key) {
        this.sdk_key = sdk_key;
        setNew_sdk_key(sdk_key);
    }

    public int getSdk_int_key() {
        return sdk_int_key;
    }

    public void setSdk_int_key(int sdk_int_key) {
        this.sdk_int_key = sdk_int_key;
        setNew_sdk_int_key(sdk_int_key);
    }


    public String getNew_board() {
        return new_board;
    }

    public void setNew_board(String new_board) {
        this.new_board = new_board;
    }

    public String getNew_release() {
        return new_release;
    }

    public void setNew_release(String new_release) {
        this.new_release = new_release;
    }

    public String getNew_brand() {
        return new_brand;
    }

    public void setNew_brand(String new_brand) {
        this.new_brand = new_brand;
    }

    public String getNew_cpu_abi() {
        return new_cpu_abi;
    }

    public void setNew_cpu_abi(String new_cpu_abi) {
        this.new_cpu_abi = new_cpu_abi;
    }

    public String getNew_cpu_abi2() {
        return new_cpu_abi2;
    }

    public void setNew_cpu_abi2(String new_cpu_abi2) {
        this.new_cpu_abi2 = new_cpu_abi2;
    }

    public String getNew_device_key() {
        return new_device_key;
    }

    public void setNew_device_key(String new_device_key) {
        this.new_device_key = new_device_key;
    }

    public String getNew_display_key() {
        return new_display_key;
    }

    public void setNew_display_key(String new_display_key) {
        this.new_display_key = new_display_key;
    }

    public String getNew_fingerprint_key() {
        return new_fingerprint_key;
    }

    public void setNew_fingerprint_key(String new_fingerprint_key) {
        this.new_fingerprint_key = new_fingerprint_key;
    }

    public String getNew_hardware_key() {
        return new_hardware_key;
    }

    public void setNew_hardware_key(String new_hardware_key) {
        this.new_hardware_key = new_hardware_key;
    }

    public String getNew_host_key() {
        return new_host_key;
    }

    public void setNew_host_key(String new_host_key) {
        this.new_host_key = new_host_key;
    }

    public String getNew_id_key() {
        return new_id_key;
    }

    public void setNew_id_key(String new_id_key) {
        this.new_id_key = new_id_key;
    }

    public String getNew_manufacturer_key() {
        return new_manufacturer_key;
    }

    public void setNew_manufacturer_key(String new_manufacturer_key) {
        this.new_manufacturer_key = new_manufacturer_key;
    }

    public String getNew_model_key() {
        return new_model_key;
    }

    public void setNew_model_key(String new_model_key) {
        this.new_model_key = new_model_key;
    }

    public String getNew_product_key() {
        return new_product_key;
    }

    public void setNew_product_key(String new_product_key) {
        this.new_product_key = new_product_key;
    }

    public String getNew_radio_key() {
        return new_radio_key;
    }

    public void setNew_radio_key(String new_radio_key) {
        this.new_radio_key = new_radio_key;
    }

    public String getNew_tags_key() {
        return new_tags_key;
    }

    public void setNew_tags_key(String new_tags_key) {
        this.new_tags_key = new_tags_key;
    }

    public long getNew_time_key() {
        return new_time_key;
    }

    public void setNew_time_key(long new_time_key) {
        this.new_time_key = new_time_key;
    }

    public String getNew_type_key() {
        return new_type_key;
    }

    public void setNew_type_key(String new_type_key) {
        this.new_type_key = new_type_key;
    }

    public String getNew_user_key() {
        return new_user_key;
    }

    public void setNew_user_key(String new_user_key) {
        this.new_user_key = new_user_key;
    }

    public String getNew_codename_key() {
        return new_codename_key;
    }

    public void setNew_codename_key(String new_codename_key) {
        this.new_codename_key = new_codename_key;
    }

    public String getNew_incremental_key() {
        return new_incremental_key;
    }

    public void setNew_incremental_key(String new_incremental_key) {
        this.new_incremental_key = new_incremental_key;
    }

    public String getNew_release_key() {
        return new_release_key;
    }

    public void setNew_release_key(String new_release_key) {
        this.new_release_key = new_release_key;
    }

    public String getNew_sdk_key() {
        return new_sdk_key;
    }

    public void setNew_sdk_key(String new_sdk_key) {
        this.new_sdk_key = new_sdk_key;
    }

    public int getNew_sdk_int_key() {
        return new_sdk_int_key;
    }

    public void setNew_sdk_int_key(int new_sdk_int_key) {
        this.new_sdk_int_key = new_sdk_int_key;
    }

    public boolean isEnable_board() {
        return enable_board;
    }

    public void setEnable_board(boolean enable_board) {
        this.enable_board = enable_board;
    }

    public boolean isEnable_release() {
        return enable_release;
    }

    public void setEnable_release(boolean enable_release) {
        this.enable_release = enable_release;
    }

    public boolean isEnable_brand() {
        return enable_brand;
    }

    public void setEnable_brand(boolean enable_brand) {
        this.enable_brand = enable_brand;
    }

    public boolean isEnable_cpu_abi() {
        return enable_cpu_abi;
    }

    public void setEnable_cpu_abi(boolean enable_cpu_abi) {
        this.enable_cpu_abi = enable_cpu_abi;
    }

    public boolean isEnable_cpu_abi2() {
        return enable_cpu_abi2;
    }

    public void setEnable_cpu_abi2(boolean enable_cpu_abi2) {
        this.enable_cpu_abi2 = enable_cpu_abi2;
    }

    public boolean isEnable_device_key() {
        return enable_device_key;
    }

    public void setEnable_device_key(boolean enable_device_key) {
        this.enable_device_key = enable_device_key;
    }

    public boolean isEnable_display_key() {
        return enable_display_key;
    }

    public void setEnable_display_key(boolean enable_display_key) {
        this.enable_display_key = enable_display_key;
    }

    public boolean isEnable_fingerprint_key() {
        return enable_fingerprint_key;
    }

    public void setEnable_fingerprint_key(boolean enable_fingerprint_key) {
        this.enable_fingerprint_key = enable_fingerprint_key;
    }

    public boolean isEnable_hardware_key() {
        return enable_hardware_key;
    }

    public void setEnable_hardware_key(boolean enable_hardware_key) {
        this.enable_hardware_key = enable_hardware_key;
    }

    public boolean isEnable_host_key() {
        return enable_host_key;
    }

    public void setEnable_host_key(boolean enable_host_key) {
        this.enable_host_key = enable_host_key;
    }

    public boolean isEnable_id_key() {
        return enable_id_key;
    }

    public void setEnable_id_key(boolean enable_id_key) {
        this.enable_id_key = enable_id_key;
    }

    public boolean isEnable_manufacturer_key() {
        return enable_manufacturer_key;
    }

    public void setEnable_manufacturer_key(boolean enable_manufacturer_key) {
        this.enable_manufacturer_key = enable_manufacturer_key;
    }

    public boolean isEnable_model_key() {
        return enable_model_key;
    }

    public void setEnable_model_key(boolean enable_model_key) {
        this.enable_model_key = enable_model_key;
    }

    public boolean isEnable_product_key() {
        return enable_product_key;
    }

    public void setEnable_product_key(boolean enable_product_key) {
        this.enable_product_key = enable_product_key;
    }

    public boolean isEnable_radio_key() {
        return enable_radio_key;
    }

    public void setEnable_radio_key(boolean enable_radio_key) {
        this.enable_radio_key = enable_radio_key;
    }

    public boolean isEnable_tags_key() {
        return enable_tags_key;
    }

    public void setEnable_tags_key(boolean enable_tags_key) {
        this.enable_tags_key = enable_tags_key;
    }

    public boolean isEnable_time_key() {
        return enable_time_key;
    }

    public void setEnable_time_key(boolean enable_time_key) {
        this.enable_time_key = enable_time_key;
    }

    public boolean isEnable_type_key() {
        return enable_type_key;
    }

    public void setEnable_type_key(boolean enable_type_key) {
        this.enable_type_key = enable_type_key;
    }

    public boolean isEnable_user_key() {
        return enable_user_key;
    }

    public void setEnable_user_key(boolean enable_user_key) {
        this.enable_user_key = enable_user_key;
    }

    public boolean isEnable_codename_key() {
        return enable_codename_key;
    }

    public void setEnable_codename_key(boolean enable_codename_key) {
        this.enable_codename_key = enable_codename_key;
    }

    public boolean isEnable_incremental_key() {
        return enable_incremental_key;
    }

    public void setEnable_incremental_key(boolean enable_incremental_key) {
        this.enable_incremental_key = enable_incremental_key;
    }

    public boolean isEnable_release_key() {
        return enable_release_key;
    }

    public void setEnable_release_key(boolean enable_release_key) {
        this.enable_release_key = enable_release_key;
    }

    public boolean isEnable_sdk_key() {
        return enable_sdk_key;
    }

    public void setEnable_sdk_key(boolean enable_sdk_key) {
        this.enable_sdk_key = enable_sdk_key;
    }

    public boolean isEnable_sdk_int_key() {
        return enable_sdk_int_key;
    }

    public void setEnable_sdk_int_key(boolean enable_sdk_int_key) {
        this.enable_sdk_int_key = enable_sdk_int_key;
    }

**/
}

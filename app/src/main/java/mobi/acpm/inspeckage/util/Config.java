package mobi.acpm.inspeckage.util;

/**
 * Created by acpm on 19/11/15.
 */
public class Config {

    //Paths
    public static final String P_INSPECKAGE_PATH = "/data/data/mobi.acpm.inspeckage";
    public static final String P_SHARED_PATH = "/shared_prefs/";
    public static final String P_ROOT = "/Inspeckage";

    public static final String P_LOG = "/log";
    public static final String P_PREFS = "/prefs";
    public static final String P_CRYPTO = "/crypto";
    public static final String P_SQLITE = "/sqlite";
    public static final String P_HASH = "/hash";
    public static final String P_PACKAGE_DETAIL = "/package_detail";
    public static final String P_CLIPB = "/clipb";
    public static final String P_IPC = "/ipc";
    public static final String P_WEBVIEW = "/webview";
    public static final String P_FILESYSTEM = "/filesystem";
    public static final String P_MISC = "/miscellaneous";
    public static final String P_SERIALIZATION = "/serialization";
    public static final String P_HTTP = "/http";
    public static final String P_USERHOOKS = "/user_hooks";
    public static final String P_APP_STRUCT = "/struct";
    public static final String P_REPLACEMENT = "/replacement";
    public static final String PREFS_BKP = "/prefs_bkp/";

    //Shared Preferences
    public static final String SP_PACKAGE = "package";
    public static final String SP_APP_NAME = "app_name";
    public static final String SP_APP_ICON_BASE64 = "app_icon_base64";
    public static final String SP_PROCESS_NAME = "process";
    public static final String SP_APP_VERSION = "app_version";
    public static final String SP_DEBUGGABLE = "isDebuggable";
    public static final String SP_ALLOW_BACKUP = "allowBackup";
    public static final String SP_APK_DIR = "apk_dir";
    public static final String SP_UID = "uid";
    public static final String SP_GIDS = "gids";
    public static final String SP_DATA_DIR = "path";
    public static final String SP_DATA_DIR_TREE = "tree";
    public static final String SP_APP_IS_RUNNING = "isRunning";
    public static final String SP_APP_PID = "pid";

    public static final String SP_SERVER_STARTED = "server_started";
    public static final String SP_SERVER_INTERFACES = "server_interfaces";
    public static final String SP_SERVER_HOST = "server_host";
    public static final String SP_SERVER_PORT = "server_port";
    public static final String SP_SERVER_IP = "server_ip";

    public static final String SP_WSOCKET_PORT = "wsocket_port";

    public static final String SP_REQ_PERMISSIONS = "req_permissions";
    public static final String SP_APP_PERMISSIONS = "app_permissions";

    public static final String SP_EXP_ACTIVITIES = "exported_act";
    public static final String SP_N_EXP_ACTIVITIES = "non_exported_act";

    public static final String SP_EXP_SERVICES = "exported_services";
    public static final String SP_N_EXP_SERVICES = "non_exported_services";

    public static final String SP_EXP_BROADCAST = "exported_broadcast";
    public static final String SP_N_EXP_BROADCAST = "non_exported_broadcast";

    public static final String SP_EXP_PROVIDER = "exported_provider";
    public static final String SP_N_EXP_PROVIDER = "non_exported_provider";

    public static final String SP_SHARED_LIB = "shared_libraries";

    public static final String SP_REPLACE_SP = "prefs_replace";

    public static final String SP_SWITCH_OUA = "switch"; //Only-User-Apps
    public static final String SP_PROXY_HOST = "host";
    public static final String SP_PROXY_PORT = "port";
    public static final String SP_SWITCH_PROXY = "switch_proxy";
    public static final String SP_FLAG_SECURE = "flag_secure";

    public static final String SP_UNPINNING = "sslunpinning";
    public static final String SP_EXPORTED = "exported";

    public static final String SP_HAS_W_PERMISSION = "write_permission";

    public static final String SP_USER_HOOKS = "user_hooks";
    public static final String SP_USER_REPLACES = "user_param_replaces";
    public static final String SP_USER_RETURN_REPLACES = "user_return_replaces";

    public static final String SP_TAB_ENABLE_SHAREDP = "enable_sharedp";
    public static final String SP_TAB_ENABLE_SERIALIZATION = "enable_serialization";
    public static final String SP_TAB_ENABLE_CRYPTO = "enable_crypto";
    public static final String SP_TAB_ENABLE_HASH = "enable_hash";
    public static final String SP_TAB_ENABLE_SQLITE = "enable_sqlite";
    public static final String SP_TAB_ENABLE_HTTP = "enable_http";
    public static final String SP_TAB_ENABLE_FS = "enable_fs";
    public static final String SP_TAB_ENABLE_MISC = "enable_misc";
    public static final String SP_TAB_ENABLE_WV = "enable_webview";
    public static final String SP_TAB_ENABLE_IPC = "enable_ipc";
    public static final String SP_TAB_ENABLE_PHOOKS = "enable_plus_hooks";

    public static final String SP_USER_PASS = "login_pass";
    public static final String SP_SWITCH_AUTH = "switch_auth";

    public static final String KEYPAIR_ALIAS = "alias";

    public static final String SP_FINGERPRINT_HOOKS = "fingerprint_hooks";
    public static final String SP_ADS_ID = "ads_id";

    public static final String SP_GEOLOCATION = "geoloc";
    public static final String SP_GEOLOCATION_SW = "geoloc_switch";
}

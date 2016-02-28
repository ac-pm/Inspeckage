package mobi.acpm.inspeckage.util;

/**
 * Created by acpm on 19/11/15.
 */
public class Config {

    //Paths
    public static String P_ROOT = "/Inspeckage";

    public static String P_LOG = "/log";
    public static String P_PREFS = "/prefs";
    public static String P_CRYPTO = "/crypto";
    public static String P_SQLITE = "/sqlite";
    public static String P_HASH = "/hash";
    public static String P_PACKAGE_DETAIL = "/package_detail";
    public static String P_CLIPB = "/clipb";
    public static String P_IPC = "/ipc";
    public static String P_WEBVIEW = "/webview";
    public static String P_FILESYSTEM = "/filesystem";
    public static String P_MISC = "/miscellaneous";
    public static String P_SERIALIZATION = "/serialization";
    public static String P_HTTP = "/http";
    public static String PREFS_BKP = "/prefs_bkp/";

    //Shared Preferences
    public static String SP_PACKAGE = "package";
    public static String SP_APP_NAME = "app_name";
    public static String SP_PROCESS_NAME = "process";
    public static String SP_APP_VERSION = "app_version";
    public static String SP_DEBUGGABLE = "isDebuggable";
    public static String SP_APK_DIR = "apk_dir";
    public static String SP_UID = "uid";
    public static String SP_GIDS = "gids";
    public static String SP_DATA_DIR = "path";
    public static String SP_DATA_DIR_TREE = "tree";
    public static String SP_APP_IS_RUNNING = "isRunning";

    public static String SP_SERVER_STARTED = "server_started";
    public static String SP_SERVER_PORT = "server_port";

    public static String SP_REQ_PERMISSIONS = "req_permissions";
    public static String SP_APP_PERMISSIONS = "app_permissions";

    public static String SP_EXP_ACTIVITIES = "exported_act";
    public static String SP_N_EXP_ACTIVITIES = "non_exported_act";

    public static String SP_EXP_SERVICES = "exported_services";
    public static String SP_N_EXP_SERVICES = "non_exported_services";

    public static String SP_EXP_BROADCAST = "exported_broadcast";
    public static String SP_N_EXP_BROADCAST = "non_exported_broadcast";

    public static String SP_EXP_PROVIDER = "exported_provider";
    public static String SP_N_EXP_PROVIDER = "non_exported_provider";

    public static String SP_SHARED_LIB = "shared_libraries";

    public static String SP_REPLACE_SP = "prefs_replace";

    public static String SP_SWITCH_OUA = "switch"; //Only-User-Apps
    public static String SP_PROXY_HOST = "host";
    public static String SP_PROXY_PORT = "port";
    public static String SP_SWITCH_PROXY = "switch_proxy";
    public static String SP_FLAG_SECURE = "flag_secure";

    public static String SP_UNPINNING = "sslunpinning";
    public static String SP_EXPORTED = "exported";

    public static String SP_HAS_W_PERMISSION = "write_permission";
}

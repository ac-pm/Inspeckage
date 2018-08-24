package mobi.acpm.inspeckage;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.hooks.ClipboardHook;
import mobi.acpm.inspeckage.hooks.CryptoHook;
import mobi.acpm.inspeckage.hooks.FileSystemHook;
import mobi.acpm.inspeckage.hooks.FingerprintHook;
import mobi.acpm.inspeckage.hooks.FlagSecureHook;
import mobi.acpm.inspeckage.hooks.HashHook;
import mobi.acpm.inspeckage.hooks.HttpHook;
import mobi.acpm.inspeckage.hooks.IPCHook;
import mobi.acpm.inspeckage.hooks.MiscHook;
import mobi.acpm.inspeckage.hooks.ProxyHook;
import mobi.acpm.inspeckage.hooks.SQLiteHook;
import mobi.acpm.inspeckage.hooks.SSLPinningHook;
import mobi.acpm.inspeckage.hooks.SerializationHook;
import mobi.acpm.inspeckage.hooks.SharedPrefsHook;
import mobi.acpm.inspeckage.hooks.UIHook;
import mobi.acpm.inspeckage.hooks.UserHooks;
import mobi.acpm.inspeckage.hooks.WebViewHook;
import mobi.acpm.inspeckage.hooks.entities.LocationHook;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.FileType;
import mobi.acpm.inspeckage.util.FileUtil;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 16/11/15.
 */
public class Module extends XC_MethodHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String PREFS = "InspeckagePrefs";
    public static final String TAG = "Inspeckage_Module:";
    public static final String ERROR = "Inspeckage_Error";
    public static XSharedPreferences sPrefs;

    public static final String MY_PACKAGE_NAME = Module.class.getPackage().getName();

    public void initZygote(StartupParam startupParam) throws Throwable {
        sPrefs = new XSharedPreferences(MY_PACKAGE_NAME, PREFS);
        sPrefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        sPrefs.reload();

        //check if this module is enable
        if (loadPackageParam.packageName.equals("mobi.acpm.inspeckage")) {
            findAndHookMethod("mobi.acpm.inspeckage.webserver.WebServer", loadPackageParam.classLoader, "isModuleEnabled", XC_MethodReplacement.returnConstant(true));

            //workaround to bypass MODE_PRIVATE of shared_prefs
            findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "notifyListeners",
                    "android.app.SharedPreferencesImpl.MemoryCommitResult", new XC_MethodHook() {

                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            //workaround to bypass the concurrency (io)
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    Context context = (Context) AndroidAppHelper.currentApplication();
                                    FileUtil.fixSharedPreference(context);
                                }
                            }, 1000);
                        }
                    });
        }

        if (loadPackageParam.packageName.equals("mobi.acpm.inspeckage"))
            return;

        if (!loadPackageParam.packageName.equals(sPrefs.getString("package", "")))
            return;

        //inspeckage needs access to the files
        File folder = new File(sPrefs.getString(Config.SP_DATA_DIR, null));
        folder.setExecutable(true, false);

        findAndHookMethod("android.util.Log", loadPackageParam.classLoader, "i",
                String.class, String.class, new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0] == "Xposed") {

                            String log = (String) param.args[1];
                            FileType ft = null;
                            if (log.contains(SharedPrefsHook.TAG)) { //5
                                ft = FileType.PREFS;
                            } else if (log.contains(CryptoHook.TAG)) { //2
                                ft = FileType.CRYPTO;
                            } else if (log.contains(HashHook.TAG)) { //3
                                ft = FileType.HASH;
                            } else if (log.contains(SQLiteHook.TAG)) { //6
                                ft = FileType.SQLITE;
                            } else if (log.contains(ClipboardHook.TAG)) { //1
                                ft = FileType.CLIPB;
                            } else if (log.contains(IPCHook.TAG)) { //4
                                ft = FileType.IPC;
                            } else if (log.contains(WebViewHook.TAG)) { //8
                                ft = FileType.WEBVIEW;
                            } else if (log.contains(FileSystemHook.TAG)) { //9
                                ft = FileType.FILESYSTEM;
                            } else if (log.contains(MiscHook.TAG)) { //10
                                ft = FileType.MISC;
                            } else if (log.contains(SerializationHook.TAG)) { //10
                                ft = FileType.SERIALIZATION;
                            } else if (log.contains(HttpHook.TAG)) { //10
                                ft = FileType.HTTP;
                            } else if (log.contains(UserHooks.TAG)) { //10
                                ft = FileType.USERHOOKS;
                            }

                            if (ft != null) {
                                FileUtil.writeToFile(sPrefs, log, ft, "");
                            }
                        }
                    }
                });

        UIHook.initAllHooks(loadPackageParam);

        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_HTTP,true)) {
            HttpHook.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_MISC,true)) {
            MiscHook.initAllHooks(loadPackageParam);
            ClipboardHook.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_WV,true)) {
            WebViewHook.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_CRYPTO,true)) {
            CryptoHook.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_FS,true)) {
            FileSystemHook.initAllHooks(loadPackageParam);
        }
        FlagSecureHook.initAllHooks(loadPackageParam);
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_HASH,true)) {
            HashHook.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_IPC,true)) {
            IPCHook.initAllHooks(loadPackageParam);
        }
        ProxyHook.initAllHooks(loadPackageParam);// --
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_SHAREDP,true)) {
            SharedPrefsHook.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_SQLITE,true)) {
            SQLiteHook.initAllHooks(loadPackageParam);
        }
        SSLPinningHook.initAllHooks(loadPackageParam);// --
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_SERIALIZATION,true)) {
            SerializationHook.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_TAB_ENABLE_PHOOKS,true)) {
            UserHooks.initAllHooks(loadPackageParam);
        }
        if(sPrefs.getBoolean(Config.SP_GEOLOCATION_SW,false)) {
            LocationHook.initAllHooks(loadPackageParam);
        }
        FingerprintHook.initAllHooks(loadPackageParam);

        //DexUtil.saveClassesWithMethodsJson(loadPackageParam, sPrefs);
    }

    public static void logError(Error e){
        XposedBridge.log(Module.ERROR + " " + e.getMessage());
    }
}

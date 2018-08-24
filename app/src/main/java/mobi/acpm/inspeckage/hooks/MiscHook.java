package mobi.acpm.inspeckage.hooks;

import android.net.Uri;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 29/12/15.
 */
public class MiscHook extends XC_MethodHook {

    //Miscelllaneous
    public static final String TAG = "Inspeckage_Misc:";

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {


        findAndHookMethod(Uri.class, "parse", String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(TAG + "URI: " + param.args[0] + "");
            }
        });

        try {
            Class<?> classBuildVersion = XposedHelpers.findClass("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info", loadPackageParam.classLoader);
            findAndHookMethod(classBuildVersion, "getId", new XC_MethodHook() {

                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args!=null && param.args.length>0) {
                        XposedBridge.log(TAG + "AdvertisingID: " + param.args[0] + "");
                    }
                }

                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args!=null && param.args.length>0) {
                        XposedBridge.log(TAG + "AdvertisingID before: " + param.args[0] + "");
                    }
                }
            });
        }catch (XposedHelpers.ClassNotFoundError ex) {}
    }
}
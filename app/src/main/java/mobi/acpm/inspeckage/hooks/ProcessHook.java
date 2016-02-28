package mobi.acpm.inspeckage.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 14/01/16.
 */
public class ProcessHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Process:";

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        /**
         final Class<?> build = XposedHelpers.findClass("android.os.Process", loadPackageParam.classLoader);

         hookMethod(XposedHelpers.findMethodBestMatch(build, "start", String.class, String.class, int.class,
         int.class, int[].class, int.class, int.class, int.class,
         String.class, String.class, String.class, String.class, String[].class), new XC_MethodHook() {
         protected void afterHookedMethod(MethodHookParam param) throws Throwable {

         int uid = (Integer) param.args[2];

         if (uid == 10118) {
         int debugFlags = (Integer) param.args[5];
         param.args[5] = (debugFlags | 0x1);
         XposedBridge.log(TAG + "debugFlags: " + String.valueOf(param.args[5]));
         }
         }
         });**/


        try {
            findAndHookMethod("android.os.Process", loadPackageParam.classLoader, "start",
                    String.class, String.class, int.class, int.class, int[].class, int.class, int.class, int.class,
                    String.class, String.class, String.class, String.class, String[].class, new XC_MethodHook() {

                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            int uid = (Integer) param.args[2];
                            if (uid == 10066) {
                                int debugFlags = (Integer) param.args[5];
                                param.args[5] = (debugFlags | 0x1);
                                XposedBridge.log(TAG + "debugFlags: " + String.valueOf(param.args[5]));
                            }
                        }
                    }

            );
        }catch (Error e){
            XposedBridge.log("ERROR_PROCESS: "+e.getMessage());
        }
    }
}

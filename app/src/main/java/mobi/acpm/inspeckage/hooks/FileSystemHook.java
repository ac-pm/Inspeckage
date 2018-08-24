package mobi.acpm.inspeckage.hooks;

import android.content.ContextWrapper;

import java.io.File;
import java.net.URI;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 27/11/15.
 */
public class FileSystemHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_FileSystem:";

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {


        findAndHookMethod(ContextWrapper.class, "openFileOutput", String.class, "int", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                String name = (String) param.args[0];
                int mode = (int) param.args[1];

                if (name.contains("Inspeckage")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {

                    String m;
                    switch (mode) {

                        case android.content.Context.MODE_PRIVATE:
                            m = "MODE_PRIVATE";
                            break;
                        //case android.content.Context.MODE_WORLD_WRITEABLE:
                          //  m = "MODE_WORLD_WRITEABLE";
                            //break;
                        case android.content.Context.MODE_APPEND:
                            m = "MODE_APPEND";
                            break;
                        default:
                            m = "?";
                    }

                    XposedBridge.log(TAG + "openFileOutput("+name+", "+m+")");
                }
            }
        });

        findAndHookConstructor(File.class, String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                String str = (String) param.args[0];
                if (str.contains("Inspeckage")) {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                } else {
                    XposedBridge.log(TAG + "R/W [new File(String)]: " + str);
                }
            }
        });


        findAndHookConstructor(File.class, String.class, String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                String dir = (String) param.args[0];
                String fileName = (String) param.args[1];
                if(dir != null) {
                    if (dir.contains("Inspeckage") || fileName.contains("Inspeckage")) {
                        XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } else {
                        XposedBridge.log(TAG + "R/W Dir: " + dir + " File: " + fileName);
                    }
                }
            }
        });


        findAndHookConstructor(File.class, File.class, String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                File fileDir = (File) param.args[0];
                String fileName = (String) param.args[1];
                if(fileDir != null) {
                    if (fileDir.getAbsolutePath().contains("Inspeckage") || fileName.contains("Inspeckage")) {
                        XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } else {
                        XposedBridge.log(TAG + "R/W Dir: " + fileDir.getAbsolutePath() + " File: " + fileName);
                    }
                }
            }
        });

        findAndHookConstructor(File.class, URI.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                URI uri = (URI) param.args[0];
                if(uri!=null) {
                    if (uri.toString().contains("Inspeckage")) {
                        XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } else {
                        XposedBridge.log(TAG + "R/W [new File(URI)]: " + uri.toString());
                    }
                }
            }
        });
    }
}

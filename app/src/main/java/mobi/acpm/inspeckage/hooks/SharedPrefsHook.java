package mobi.acpm.inspeckage.hooks;

import android.content.ContextWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.FileType;
import mobi.acpm.inspeckage.util.FileUtil;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * Created by acpm on 17/11/15.
 */
public class SharedPrefsHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Prefs:";
    static StringBuffer sb = null;
    private static XSharedPreferences sPrefs;
    public static String putFileName = "";

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        loadPrefs();

        findAndHookMethod(ContextWrapper.class, "getSharedPreferences",
                String.class, "int", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int modeId = (Integer) param.args[1];
                        String mode = "MODE_PRIVATE";
                        if(modeId == 1){
                            mode = "MODE_PRIVATE";
                        }else if(modeId == 2){
                            mode = "MODE_WORLD_WRITEABLE";
                        }else if(modeId > 2){
                            mode = "APPEND or MULTI_PROCESS";
                        }
                        sb = new StringBuffer();
                        //sb.append("PUT[" + (String) param.args[0] + ".xml , "+mode+"]");
                        putFileName = "PUT[" + (String) param.args[0] + ".xml , "+mode+"]";
                    }
                });

        //SharedPreferences é interface, o xposed só trabalha com class
        findAndHookConstructor("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, File.class, "int", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                File mFile = (File) param.args[0];
                String text = "";
                if (mFile.exists() && mFile.canRead()) {
                    FileInputStream f = new FileInputStream(mFile);
                    FileChannel ch = f.getChannel();
                    MappedByteBuffer mbb = ch.map(FileChannel.MapMode.READ_ONLY, 0L, ch.size());

                    while (mbb.hasRemaining()) {
                        String charsetName = "UTF-8";
                        CharBuffer cb = Charset.forName(charsetName).decode(mbb);
                        text = cb.toString();
                    }
                }

                FileUtil.writeToFile(sPrefs, text, FileType.PREFS_BKP, mFile.getName());
            }

        });


        findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "commit",
                new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (sb.toString().length() > 0)
                            XposedBridge.log(TAG + "" + sb.toString().substring(0, sb.length() - 1) + "");

                        sb = new StringBuffer();
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "apply",
                new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (sb.toString().length() > 0)
                            XposedBridge.log(TAG + "" + sb.toString().substring(0, sb.length() - 1) + "");

                        sb = new StringBuffer();
                    }
                });

        //***********GET************//

        findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getString",
                String.class, String.class, new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        File f = (File) getObjectField(param.thisObject, "mFile");

                        XposedBridge.log(TAG + "GET[" + f.getName() + "] String(" + (String) param.args[0] + " , " + (String) param.getResult() + ")");

                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getStringSet",
                String.class, Set.class, new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Set<String> set = (Set) param.getResult();
                        StringBuffer sb = new StringBuffer();
                        if (set != null && set.size() > 0)
                            for (String x : set) {
                                sb.append(x + "\n");
                            }
                        File f = (File) getObjectField(param.thisObject, "mFile");
                        XposedBridge.log(TAG + "GET[" + f.getName() + "] StringSet(" + (String) param.args[0] + ")= " + sb.toString() + ")");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getBoolean",
                String.class, "boolean", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File f = (File) getObjectField(param.thisObject, "mFile");

                        Module.sPrefs.reload();
                        String[] strReplace = Module.sPrefs.getString("prefs_replace", "").split(",");
                        String key = (String) param.args[0];

                        if (key.equals(strReplace[0])) {
                            param.setResult(strReplace[1]);
                        }

                        XposedBridge.log(TAG + "GET[" + f.getName() + "] Boolean(" + param.args[0] + " , " + String.valueOf(param.getResult()) + ")");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getFloat",
                String.class, "float", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File f = (File) getObjectField(param.thisObject, "mFile");
                        XposedBridge.log(TAG + "GET[" + f.getName() + "] Float(" + (String) param.args[0] + " , " + Float.toString((float) param.getResult()) + ")");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getInt",
                String.class, "int", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File f = (File) getObjectField(param.thisObject, "mFile");
                        XposedBridge.log(TAG + "GET[" + f.getName() + "] Int(" + (String) param.args[0] + " , " + Integer.toString((int) param.getResult()) + ")");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "getLong",
                String.class, "long", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File f = (File) getObjectField(param.thisObject, "mFile");
                        XposedBridge.log(TAG + "GET[" + f.getName() + "] Long(" + (String) param.args[0] + " , " + Long.toString((Long) param.getResult()) + ")");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl", loadPackageParam.classLoader, "contains",
                String.class, new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File f = (File) getObjectField(param.thisObject, "mFile");
                        XposedBridge.log(TAG + "CONTAINS[" + f.getName() + "](" + (String) param.args[0] + " , " + Boolean.toString((boolean) param.getResult()) + ")");
                    }
                });

        //********PUT*********//

        findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putString",
                String.class, String.class, new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        sb.append(putFileName+" String(" + (String) param.args[0] + "," + (String) param.args[1] + "),");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putBoolean",
                String.class, "boolean", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        sb.append(putFileName+" Boolean(" + (String) param.args[0] + "," + String.valueOf(param.args[1]) + "),");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putInt",
                String.class, "int", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        sb.append(putFileName+" Int(" + (String) param.args[0] + "," + Integer.toString((Integer) param.args[1]) + "),");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putLong",
                String.class, "long", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        sb.append(putFileName+" Long(" + (String) param.args[0] + "," + Long.toString((Long) param.args[1]) + "),");
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl.EditorImpl", loadPackageParam.classLoader, "putFloat",
                String.class, "float", new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        sb.append(putFileName+" Float(" + (String) param.args[0] + "," + Float.toString((Float) param.args[1]) + "),");
                    }
                });
    }
}


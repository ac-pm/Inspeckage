package mobi.acpm.inspeckage.hooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.Replacement;
import mobi.acpm.inspeckage.util.Util;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by acpm on 19/04/16.
 */
public class UserHooks extends XC_MethodHook {

    public static final String TAG = "Inspeckage_UserHooks:";
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();//new Gson();
    private static XSharedPreferences sPrefs;
    private static XC_LoadPackage.LoadPackageParam lpp;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        loadPrefs();
        lpp = loadPackageParam;
        String json = "{\"hookJson\": " + sPrefs.getString(Config.SP_USER_HOOKS, "") + "}";
        try {

            if(!json.trim().equals("{\"hookJson\":}")) {
                HookList hookList = gson.fromJson(json, HookList.class);
                for (HookItem hookItem : hookList.hookJson) {
                    if(hookItem.state) {
                        hook(hookItem, loadPackageParam.classLoader);
                    }
                }
            }
        } catch (JsonSyntaxException ex) { }
    }

    static void hook(HookItem item, ClassLoader classLoader) {
        try {
            Class<?> hookClass = findClass(item.className, classLoader);

            if (hookClass != null) {

                if (item.method != null && !item.method.equals("")) {
                    for (Method method : hookClass.getDeclaredMethods()) {
                        if (method.getName().equals(item.method) && !Modifier.isAbstract(method.getModifiers())) {
                            XposedBridge.hookMethod(method, methodHook);
                        }
                    }
                } else {
                    for (Method method : hookClass.getDeclaredMethods()) {
                        if(!Modifier.isAbstract(method.getModifiers())) {
                            XposedBridge.hookMethod(method, methodHook);
                        }
                    }
                }

                if (item.constructor) {
                    for (Constructor<?> constructor : hookClass.getDeclaredConstructors()) {
                        XposedBridge.hookMethod(constructor, methodHook);
                    }
                }

            } else {
                log(TAG + "class not found.");
            }
        } catch (Error e) {
            Module.logError(e);
        }
    }

    static XC_MethodHook methodHook = new XC_MethodHook() {

        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            loadPrefs();
            Replacement.parameterReplace(param, sPrefs);
        }

        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            loadPrefs();
            Replacement.resultReplace(param, sPrefs);
            parseParam(param);
        }
    };

    static void parseParam(XC_MethodHook.MethodHookParam param) {
        try {
            JSONObject hookData = new JSONObject();
            hookData.put("class", param.method.getDeclaringClass().getName());

            if(param.method!=null)
                hookData.put("method", param.method.getName());

            JSONArray args = new JSONArray();

            if(param.args!=null) {
                for (Object object : (Object[]) param.args) {

                    if (object != null) {
                        if(object.getClass().equals(byte[].class)){
                            String result = Util.byteArrayToString((byte[])object);
                            args.put(gson.toJson(result));
                        }else{
                            args.put(gson.toJson(object));
                        }
                    }
                }
                hookData.put("args", args);
            }

            if(param.getResult()!=null){
                String result = "";
                if(param.getResult().getClass().equals(byte[].class)){
                    result = Util.byteArrayToString((byte[])param.getResult());
                    hookData.put("result", gson.toJson(result));
                }else{
                    hookData.put("result", gson.toJson(param.getResult()));
                }

            }

            log(TAG + hookData.toString());

        } catch (Exception e) {
            e.getMessage();
        }
    }
}

class HookList {
    public List<HookItem> hookJson;
}

class HookItem {
    protected int id;
    protected String className;
    protected String method;
    protected boolean constructor;
    protected boolean state;
}

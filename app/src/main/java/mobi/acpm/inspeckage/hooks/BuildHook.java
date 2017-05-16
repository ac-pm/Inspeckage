package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.hooks.entities.BuildItem;
import mobi.acpm.inspeckage.hooks.entities.BuildList;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 19/04/17.
 */

public class BuildHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_DeviceData: ";
    private static XSharedPreferences sPrefs;
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        loadPrefs();

        try{
            findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();

                String json = sPrefs.getString("build_hooks", "");
                Class<?> classBuild = XposedHelpers.findClass("android.os.Build", loadPackageParam.classLoader);
                Class<?> classBuildVersion = XposedHelpers.findClass("android.os.Build.VERSION", loadPackageParam.classLoader);
                try {
                    BuildList buildList = gson.fromJson(json, BuildList.class);
                    for (BuildItem buildItem : buildList.buildItems) {
                        if (buildItem.enable) {
                            if (buildItem.type.equals("BUILD")) {
                                XposedHelpers.setStaticObjectField(classBuild, buildItem.name, buildItem.newValue);
                            } else {
                                XposedHelpers.setStaticObjectField(classBuildVersion, buildItem.name, buildItem.newValue);
                            }
                        }
                    }
                } catch (JsonSyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        }catch (NoSuchMethodError e) {
            XposedBridge.log(TAG + "couldn't hook method");
        }
    }
}

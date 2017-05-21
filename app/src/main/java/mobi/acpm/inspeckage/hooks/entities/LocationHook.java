package mobi.acpm.inspeckage.hooks.entities;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 21/05/17.
 */

public class LocationHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Location: ";
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        try {
            Class<?> location = XposedHelpers.findClass("android.location.Location", loadPackageParam.classLoader);

            findAndHookMethod(location, "getLatitude", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    super.afterHookedMethod(param);

                    loadPrefs();
                    String geolocation = sPrefs.getString("geoloc", "");
                    if (!geolocation.equals("") && geolocation.contains(",")) {
                        final String[] latlng = geolocation.split(",");
                        param.setResult(Double.valueOf(latlng[0]));
                    }
                }
            });

            findAndHookMethod(location, "getLongitude", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    super.afterHookedMethod(param);
                    loadPrefs();
                    String geolocation = sPrefs.getString("geoloc", "");
                    if (!geolocation.equals("") && geolocation.contains(",")) {
                        final String[] latlng = geolocation.split(",");
                        param.setResult(Double.valueOf(latlng[1]));
                    }
                }
            });

        } catch (XposedHelpers.ClassNotFoundError ex) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

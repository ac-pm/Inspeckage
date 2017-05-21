package mobi.acpm.inspeckage.hooks;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.hooks.entities.FingerprintItem;
import mobi.acpm.inspeckage.hooks.entities.FingerprintList;

/**
 * Created by acpm on 19/04/17.
 */

public class FingerprintHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_DeviceData: ";
    private static XSharedPreferences sPrefs;
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        loadPrefs();

        try {

            loadPrefs();

            String json = sPrefs.getString("fingerprint_hooks", "");
            Class<?> classBuild = XposedHelpers.findClass("android.os.Build", loadPackageParam.classLoader);
            Class<?> classBuildVersion = XposedHelpers.findClass("android.os.Build.VERSION", loadPackageParam.classLoader);

            try {
                FingerprintList fingerprintList = gson.fromJson(json, FingerprintList.class);

                for (FingerprintItem fingerprintItem : fingerprintList.fingerprintItems) {
                    if (fingerprintItem.enable) {

                        if (fingerprintItem.type.equals("BUILD")) {
                            XposedHelpers.setStaticObjectField(classBuild, fingerprintItem.name, fingerprintItem.newValue);
                        } else if (fingerprintItem.type.equals("VERSION")) {
                            XposedHelpers.setStaticObjectField(classBuildVersion, fingerprintItem.name, fingerprintItem.newValue);
                        } else if (fingerprintItem.type.equals("TelephonyManager")) {

                            try {
                                switch (fingerprintItem.name) {
                                    case "IMEI":
                                        HookTelephony("android.telephony.TelephonyManager", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                        HookTelephony("com.android.internal.telephony.PhoneSubInfo", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                        HookTelephony("com.android.internal.telephony.PhoneProxy", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                        if (Build.VERSION.SDK_INT < 22) {
                                            HookTelephony("com.android.internal.telephony.gsm.GSMPhone", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                        }
                                    case "IMSI":
                                        HookTelephony("android.telephony.TelephonyManager", loadPackageParam, "getSubscriberId", fingerprintItem.newValue);
                                    case "PhoneNumber":
                                        HookTelephony("android.telephony.TelephonyManager", loadPackageParam, "getLine1Number", fingerprintItem.newValue);
                                    case "SimSerial":
                                        HookTelephony("android.telephony.TelephonyManager", loadPackageParam, "getSimSerialNumber", fingerprintItem.newValue);
                                    case "CarrierCode":
                                        HookTelephony("android.telephony.TelephonyManager", loadPackageParam, "getNetworkOperator", fingerprintItem.newValue);
                                    case "Carrier":
                                        HookTelephony("android.telephony.TelephonyManager", loadPackageParam, "getNetworkOperatorName", fingerprintItem.newValue);
                                }
                            } catch (Exception ex) {
                                XposedBridge.log(TAG + fingerprintItem.name + ex.getMessage());
                            }

                        } else if (fingerprintItem.type.equals("Advertising")) {
                            try {
                                HookTelephony("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info", loadPackageParam, "getId", fingerprintItem.newValue);
                            }catch (XposedHelpers.ClassNotFoundError ex) {}
                        }
                    }
                }
            } catch (JsonSyntaxException ex) {
                XposedBridge.log(TAG + ex.getMessage());
            }catch (NoSuchMethodError ex) {
                XposedBridge.log(TAG + ex.getMessage());
            }
        } catch (XposedHelpers.ClassNotFoundError ex) {

            XposedBridge.log(TAG + ex.getMessage());
        }
    }

    private static void HookTelephony(String hookClass, XC_LoadPackage.LoadPackageParam loadPkgParam, String methodName, final String value) {
        try {
            XposedHelpers.findAndHookMethod(hookClass, loadPkgParam.classLoader, methodName, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(value);
                }

            });
        } catch (Exception e) {
            XposedBridge.log(TAG + methodName + " ERROR: " + e.getMessage());
        }
    }
}

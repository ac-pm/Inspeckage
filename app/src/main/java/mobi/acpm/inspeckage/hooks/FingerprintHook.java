package mobi.acpm.inspeckage.hooks;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.hooks.entities.FingerprintItem;
import mobi.acpm.inspeckage.hooks.entities.FingerprintList;
import mobi.acpm.inspeckage.util.Util;

import static de.robv.android.xposed.XposedBridge.log;

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
                if (fingerprintList != null && fingerprintList.fingerprintItems != null && fingerprintList.fingerprintItems.size() > 0) {
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
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                            HookFingerprintItem("com.android.internal.telephony.PhoneSubInfo", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                            HookFingerprintItem("com.android.internal.telephony.PhoneProxy", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                            if (Build.VERSION.SDK_INT < 22) {
                                                HookFingerprintItem("com.android.internal.telephony.gsm.GSMPhone", loadPackageParam, "getDeviceId", fingerprintItem.newValue);
                                            }
                                        case "IMSI":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getSubscriberId", fingerprintItem.newValue);
                                        case "PhoneNumber":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getLine1Number", fingerprintItem.newValue);
                                        case "SimSerial":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getSimSerialNumber", fingerprintItem.newValue);
                                        case "CarrierCode":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getNetworkOperator", fingerprintItem.newValue);
                                        case "Carrier":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getNetworkOperatorName", fingerprintItem.newValue);
                                        case "SimCountry":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getSimCountryIso", fingerprintItem.newValue);
                                        case "NetworkCountry":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getNetworkCountryIso", fingerprintItem.newValue);
                                        case "SimSerialNumber":
                                            HookFingerprintItem("android.telephony.TelephonyManager", loadPackageParam, "getSimSerialNumber", fingerprintItem.newValue);
                                    }
                                } catch (Exception ex) {
                                    log(TAG + fingerprintItem.name + ex.getMessage());
                                }

                            } else if (fingerprintItem.type.equals("Advertising")) {
                                try {
                                    HookFingerprintItem("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info", loadPackageParam, "getId", fingerprintItem.newValue);
                                } catch (XposedHelpers.ClassNotFoundError ex) {
                                }
                            } else if (fingerprintItem.type.equals("Wi-Fi")) {
                                try {

                                    switch (fingerprintItem.name) {
                                        case "BSSID":
                                            HookFingerprintItem("android.net.wifi.WifiInfo", loadPackageParam, "getBSSID", fingerprintItem.newValue);
                                            break;
                                        case "SSID":
                                            HookFingerprintItem("android.net.wifi.WifiInfo", loadPackageParam, "getSSID", fingerprintItem.newValue);
                                            break;
                                        case "IP": {
                                            int value = 0;
                                            try {
                                                value = Util.inetAddressToInt(InetAddress.getByName(fingerprintItem.newValue));
                                            } catch (UnknownHostException e) {
                                                e.printStackTrace();
                                            }
                                            HookFingerprintItem("android.net.wifi.WifiInfo", loadPackageParam, "getIpAddress", value);
                                            break;
                                        }
                                        case "Android": {
                                            byte[] mac = Util.macAddressToByteArr(fingerprintItem.newValue);
                                            HookFingerprintItem("java.net.NetworkInterface", loadPackageParam, "getHardwareAddress", mac);
                                            break;
                                        }

                                    }
                                } catch (XposedHelpers.ClassNotFoundError ex) {
                                }
                            } else if (fingerprintItem.type.equals("Wi-Fi")) {

                            }
                        }
                    }
                }
            } catch (JsonSyntaxException ex) {
                log(TAG + ex.getMessage());
            }catch (NoSuchMethodError ex) {
                log(TAG + ex.getMessage());
            }
        } catch (XposedHelpers.ClassNotFoundError ex) {

            log(TAG + ex.getMessage());
        }
    }

    private static void HookFingerprintItem(String hookClass, XC_LoadPackage.LoadPackageParam loadPkgParam, String methodName, final Object value) {
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
            log(TAG + methodName + " ERROR: " + e.getMessage());
        }
    }
}

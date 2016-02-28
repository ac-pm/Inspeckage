package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.receivers.InspeckageReceiver;

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

        findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Activity appCompatActivity = (Activity) param.thisObject;

                Context context = (Context) appCompatActivity.getApplicationContext();
                context.registerReceiver(new InspeckageReceiver(param.thisObject),
                        new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_FILTER"));

            }
        });

        findAndHookMethod(Fragment.class, "onCreate", Bundle.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Fragment fragment = (Fragment) param.thisObject;

                Context context = (Context) fragment.getActivity().getApplicationContext();
                context.registerReceiver(new InspeckageReceiver(param.thisObject),
                        new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_FILTER"));

            }
        });

        findAndHookMethod(Activity.class, "finish", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                android.os.Process.killProcess(android.os.Process.myPid());

            }
        });





/**
        findAndHookMethod("com.squareup.okhttp.internal.http.HttpsURLConnectionImpl", loadPackageParam.classLoader, "getInputStream", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                HttpsURLConnection urlConn = (HttpsURLConnection) param.thisObject;

                if (urlConn != null) {
                    StringBuilder sb = new StringBuilder();
                    Map<String, List<String>> properties = urlConn.getRequestProperties();
                    if (properties != null && properties.size() > 0) {
                        Collection<List<String>> coll = properties.values();
                        if (coll != null && coll.size() > 0) {
                            for (List<String> ls : coll) {
                                for (String s : ls) {
                                    sb.append(s);
                                }
                            }
                        }
                    }

                    XposedBridge.log(TAG + "HTTPS REQUEST: method=" + urlConn.getRequestMethod() + " " +
                            "URL=" + urlConn.getURL().toString() + " " +
                            "Params=" + sb.toString());
                }

            }
        });
**/
        /**
        Class<?> ahc = findClass("com.loopj.android.http.AsyncHttpClient", loadPackageParam.classLoader);
        findMethodBestMatch(ahc, "post", Context.class, String.class, "RequestParams", new XC_MethodHook() {
            //findAndHookMethod("com.loopj.android.http.AsyncHttpClient",loadPackageParam.classLoader, "post",Context.class, String.class, "RequestParams",new XC_MethodHook() {

                protected void afterHookedMethod (MethodHookParam param)throws Throwable {

                    XposedBridge.log(TAG + "URL: " + param.args[1] + "");
                }
            });**/
        }
}
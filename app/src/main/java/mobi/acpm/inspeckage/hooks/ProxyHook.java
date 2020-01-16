package mobi.acpm.inspeckage.hooks;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URI;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static mobi.acpm.inspeckage.util.WebViewHttpProxy.setProxy;

/**
 * Created by acpm on 21/11/15.
 */
public class ProxyHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Proxy:";
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {


        XC_MethodHook ProxySelectorHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                loadPrefs();

                if (sPrefs.getBoolean("switch_proxy", false)) {

                    System.setProperty("proxyHost", sPrefs.getString("host", null));
                    System.setProperty("proxyPort", sPrefs.getString("port", null));

                    System.setProperty("http.proxyHost", sPrefs.getString("host", null));
                    System.setProperty("http.proxyPort", sPrefs.getString("port", null));

                    System.setProperty("https.proxyHost", sPrefs.getString("host", null));
                    System.setProperty("https.proxyPort", sPrefs.getString("port", null));

                    System.setProperty("socksProxyHost", sPrefs.getString("host", null));
                    System.setProperty("socksProxyPort", sPrefs.getString("port", null));


                    URI uri = (URI) param.args[0];

                    XposedBridge.log(TAG + " [P:" + sPrefs.getString("host", null) + ":" + sPrefs.getString("port", null) + "] - URI = " + uri);
                }
            }
        };

        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                findAndHookMethod("java.net.ProxySelectorImpl", loadPackageParam.classLoader, "select", URI.class, ProxySelectorHook);
            } else {
                findAndHookMethod("sun.net.spi.DefaultProxySelector", loadPackageParam.classLoader, "select", URI.class, ProxySelectorHook);
            }
        } catch (Error e) {
            Module.logError(e);
        }

        hookAllConstructors(XposedHelpers.findClass("org.apache.http.impl.client.DefaultHttpClient", loadPackageParam.classLoader), new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                loadPrefs();
                if (sPrefs.getBoolean("switch_proxy", false)) {
                    String proxyHost = sPrefs.getString("host", null);
                    int proxyPort;
                    try {
                        proxyPort = Integer.parseInt(sPrefs.getString("port", null));
                    } catch (NumberFormatException ex) {
                        proxyPort = -1;
                    }

                    DefaultHttpClient httpClient = (DefaultHttpClient) param.thisObject;
                    HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
            }
        });

        try {
            hookWebView(loadPackageParam.classLoader, loadPackageParam.packageName);
        }catch (Throwable exception) {
        }
    }

    private static Class findClass(String className, ClassLoader classLoader) {
        try {
            return XposedHelpers.findClass(className, classLoader);
        } catch (Throwable exception) {
        }
        return null;
    }

    private static void hookWebView(final ClassLoader classLoader, final String packageName) {
        final String[] webviewList = {
                "android.webkit.WebView", // android webview
                "com.tencent.smtt.sdk.WebView",  // tencent x5
                "com.uc.webview.export.WebView", // UC
                WebView.class.toString()
        };

        LRUCache<String, Boolean> hookedClassLoader = new LRUCache<>(10000);

        for (int i = 0; i < webviewList.length; i++) {
            final String className = webviewList[i];
            final Class cla = findClass(className, classLoader);
            if(cla == null){
                continue;
            }
            String key = cla.getName() + "@" + cla.getClassLoader().hashCode();
            boolean hooked;
            if (hookedClassLoader.get(key) == null) {
                hookedClassLoader.put(key, true);
                hooked = true;
            }else{
                hooked = false;
            }
            if (cla != null && hooked) {
                XposedBridge.log(packageName + " hook " + className + "@" + cla.getClassLoader().getClass().getName() + ":" + cla.getClassLoader().hashCode());
                XC_MethodHook WebserviceProxyHook = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        loadPrefs();
                        if (sPrefs.getBoolean("switch_proxy", false)) {
                            WebView wv = (WebView) param.thisObject;
                            setProxy(wv, sPrefs.getString("host", null), Integer.parseInt(sPrefs.getString("port", null)), "android.app.Application");
                        }
                    }
                };

                XposedHelpers.findAndHookConstructor(cla, Context.class, WebserviceProxyHook);
                XposedHelpers.findAndHookConstructor(cla, Context.class, AttributeSet.class, WebserviceProxyHook);
                XposedHelpers.findAndHookConstructor(cla, Context.class, AttributeSet.class, int.class, WebserviceProxyHook);
                XposedHelpers.findAndHookConstructor(cla, Context.class, AttributeSet.class, int.class, int.class, WebserviceProxyHook);
                XposedHelpers.findAndHookConstructor(cla, Context.class, AttributeSet.class, int.class, boolean.class, WebserviceProxyHook);
            }
        }
    }
}

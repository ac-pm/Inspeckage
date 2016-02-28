package mobi.acpm.inspeckage.hooks;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 24/11/15.
 */
public class WebViewHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_WebView:";

    static StringBuilder sb = null;

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        //Injects the supplied Java object into this WebView.
        //http://developer.android.com/intl/pt-br/reference/android/webkit/WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)
        findAndHookMethod(WebView.class, "addJavascriptInterface",
                Object.class, String.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String objName = (String) param.args[1];
                        XposedBridge.log(TAG + "addJavascriptInterface(Object, " + objName + ");");
                    }
                });

        findAndHookMethod(WebView.class, "loadUrl",
                String.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        sb = new StringBuilder();
                        WebView wv = (WebView) param.thisObject;
                        sb.append("Load URL: " + param.args[0]);

                        sb.append(checkSettings(wv));

                        XposedBridge.log(TAG + sb.toString());
                    }
                });

        findAndHookMethod(WebView.class, "loadData",
                String.class, String.class, String.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        sb = new StringBuilder();
                        WebView wv = (WebView) param.thisObject;

                        sb.append("Load Data: " + param.args[0]);

                        sb.append(checkSettings(wv));

                        XposedBridge.log(TAG + sb.toString());

                    }
                });

        findAndHookMethod(WebView.class, "setWebChromeClient", WebChromeClient.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                XposedBridge.log(TAG + "Client: WebChrome");
            }
        });

        findAndHookMethod(WebView.class, "setWebViewClient", WebViewClient.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                //XposedBridge.log(TAG + "Client: WebView");
            }
        });

        findAndHookMethod(WebView.class, "setWebContentsDebuggingEnabled", "boolean", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                boolean value = (boolean) param.args[0];
                XposedBridge.log(TAG + "Web Contents Debugging Enabled: " + String.valueOf(value));
            }
        });

    }

    static String checkSettings(WebView wv) {

        String r = "</br>";
        //javascript
        if (wv.getSettings().getJavaScriptEnabled()) {
            r = r + " -- JavaScript: Enable</br>";
        } else {
            r = r + " -- JavaScript: Disable</br>";
        }
        //PluginState
        if (wv.getSettings().getPluginState() == WebSettings.PluginState.OFF) {
            r = r + " -- Plugin State: OFF</br>";
        } else {
            r = r + " -- Plugin State: ON</br>";
        }
        //AllowFileAccess
        if (wv.getSettings().getAllowFileAccess()) {
            r = r + " -- Allow File Access: Enable</br>";
        } else {
            r = r + " -- Allow File Access: Disable</br>";
        }
        return r;
    }
}

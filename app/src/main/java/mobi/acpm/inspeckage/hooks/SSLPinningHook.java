package mobi.acpm.inspeckage.hooks;

import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.Module;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * Created by acpm on 25/11/15.
 *
 * Code from SSLUnpinning project https://github.com/ac-pm/SSLUnpinning_Xposed
 */
public class SSLPinningHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_SSLPinning:";
    private static XSharedPreferences sPrefs;

    public static void loadPrefs() {
        sPrefs = new XSharedPreferences(Module.class.getPackage().getName(), Module.PREFS);
        sPrefs.makeWorldReadable();
    }

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        // --- Java Secure Socket Extension (JSSE) ---

        //TrustManagerFactory.getTrustManagers >> EmptyTrustManager
        findAndHookMethod("javax.net.ssl.TrustManagerFactory", loadPackageParam.classLoader, "getTrustManagers", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();
                if (sPrefs.getBoolean("sslunpinning", false)) {
                    TrustManager[] tms = EmptyTrustManager.getInstance();
                    param.setResult(tms);
                }
            }
        });

        //SSLContext.init >> (null,EmptyTrustManager,null)
        findAndHookMethod("javax.net.ssl.SSLContext", loadPackageParam.classLoader, "init", KeyManager[].class, TrustManager[].class, SecureRandom.class, new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();
                if (sPrefs.getBoolean("sslunpinning", false)) {
                    param.args[0] = null;
                    param.args[1] = EmptyTrustManager.getInstance();
                    param.args[2] = null;
                }
            }
        });

        //HttpsURLConnection.setSSLSocketFactory >> new SSLSocketFactory
        findAndHookMethod("javax.net.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setSSLSocketFactory", javax.net.ssl.SSLSocketFactory.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();
                if (sPrefs.getBoolean("sslunpinning", false)) {
                    param.args[0] = newInstance(javax.net.ssl.SSLSocketFactory.class);
                }
            }
        });

        // --- APACHE ---
        try {
            final Class<?> httpsURLConnection = findClass("org.apache.http.conn.ssl.HttpsURLConnection", loadPackageParam.classLoader);
            if (httpsURLConnection != null) {

                //HttpsURLConnection.setDefaultHostnameVerifier >> SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
                try {

                    findAndHookMethod(httpsURLConnection, "setDefaultHostnameVerifier",
                            HostnameVerifier.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    loadPrefs();
                                    if (sPrefs.getBoolean("sslunpinning", false)) {
                                        param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                                    }
                                }
                            });

                } catch (Error e) {
                    Module.logError(e);
                }

                //HttpsURLConnection.setHostnameVerifier >> SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
                try {
                    findAndHookMethod("org.apache.http.conn.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setHostnameVerifier", HostnameVerifier.class,
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    loadPrefs();
                                    if (sPrefs.getBoolean("sslunpinning", false)) {
                                        param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                                    }
                                }
                            });
                } catch (Error e) {
                    Module.logError(e);
                }

                //SSLSocketFactory.getSocketFactory >> new SSLSocketFactory
                try {
                    findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "getSocketFactory", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            loadPrefs();
                            if (sPrefs.getBoolean("sslunpinning", false)) {
                                param.setResult((SSLSocketFactory) newInstance(SSLSocketFactory.class));
                            }
                        }
                    });
                } catch (Error e) {
                    Module.logError(e);
                }

                //SSLSocketFactory(...) >> SSLSocketFactory(...){ new EmptyTrustManager()}
                try {
                    Class<?> sslSocketFactory = findClass("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader);
                    findAndHookConstructor(sslSocketFactory, String.class, KeyStore.class, String.class, KeyStore.class,
                            SecureRandom.class, HostNameResolver.class, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                    loadPrefs();
                                    if (sPrefs.getBoolean("sslunpinning", false)) {
                                        String algorithm = (String) param.args[0];
                                        KeyStore keystore = (KeyStore) param.args[1];
                                        String keystorePassword = (String) param.args[2];
                                        SecureRandom random = (SecureRandom) param.args[4];

                                        KeyManager[] keymanagers = null;
                                        TrustManager[] trustmanagers;

                                        if (keystore != null) {
                                            keymanagers = (KeyManager[]) callStaticMethod(SSLSocketFactory.class, "createKeyManagers", keystore, keystorePassword);
                                        }

                                        trustmanagers = new TrustManager[]{new EmptyTrustManager()};

                                        setObjectField(param.thisObject, "sslcontext", SSLContext.getInstance(algorithm));
                                        callMethod(getObjectField(param.thisObject, "sslcontext"), "init", keymanagers, trustmanagers, random);
                                        setObjectField(param.thisObject, "socketfactory", callMethod(getObjectField(param.thisObject, "sslcontext"), "getSocketFactory"));
                                    }
                                }

                            });
                } catch (Error e) {
                    Module.logError(e);
                }

                //SSLSocketFactory.isSecure >> true
                try {
                    findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "isSecure", Socket.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            loadPrefs();
                            if (sPrefs.getBoolean("sslunpinning", false)) {
                                param.setResult(true);
                            }
                        }
                    });
                } catch (Error e) {
                    Module.logError(e);
                }
            }
        } catch (Error e) {
            Module.logError(e);
        }

        ///OKHTTP
        try {
            findAndHookMethod("okhttp3.CertificatePinner", loadPackageParam.classLoader, "findMatchingPins", String.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    loadPrefs();
                    if (sPrefs.getBoolean("sslunpinning", false)) {
                        param.args[0] = "";
                    }
                }
            });
        } catch (Error e) {
            Module.logError(e);
        }
    }
}

class EmptyTrustManager implements X509TrustManager {

    private static TrustManager[] emptyTM = null;

    public static TrustManager[] getInstance() {
        if (emptyTM == null) {
            emptyTM = new TrustManager[1];
            emptyTM[0] = new EmptyTrustManager();
        }
        return emptyTM;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

package mobi.acpm.inspeckage.webserver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Html;
import android.util.Log;

import org.java_websocket.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.security.auth.x500.X500Principal;

import fi.iki.elonen.NanoHTTPD;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.hooks.CryptoHook;
import mobi.acpm.inspeckage.hooks.FileSystemHook;
import mobi.acpm.inspeckage.hooks.HashHook;
import mobi.acpm.inspeckage.hooks.HttpHook;
import mobi.acpm.inspeckage.hooks.IPCHook;
import mobi.acpm.inspeckage.hooks.MiscHook;
import mobi.acpm.inspeckage.hooks.SQLiteHook;
import mobi.acpm.inspeckage.hooks.SerializationHook;
import mobi.acpm.inspeckage.hooks.SharedPrefsHook;
import mobi.acpm.inspeckage.hooks.UserHooks;
import mobi.acpm.inspeckage.hooks.WebViewHook;
import mobi.acpm.inspeckage.log.LogService;
import mobi.acpm.inspeckage.receivers.InspeckageWebReceiver;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.FileUtil;
import mobi.acpm.inspeckage.util.Fingerprint;
import mobi.acpm.inspeckage.util.PackageDetail;
import mobi.acpm.inspeckage.util.Util;

import static mobi.acpm.inspeckage.util.FileType.APP_STRUCT;
import static mobi.acpm.inspeckage.util.FileType.CRYPTO;
import static mobi.acpm.inspeckage.util.FileType.FILESYSTEM;
import static mobi.acpm.inspeckage.util.FileType.HASH;
import static mobi.acpm.inspeckage.util.FileType.HTTP;
import static mobi.acpm.inspeckage.util.FileType.IPC;
import static mobi.acpm.inspeckage.util.FileType.MISC;
import static mobi.acpm.inspeckage.util.FileType.PREFS;
import static mobi.acpm.inspeckage.util.FileType.SERIALIZATION;
import static mobi.acpm.inspeckage.util.FileType.SQLITE;
import static mobi.acpm.inspeckage.util.FileType.USERHOOKS;
import static mobi.acpm.inspeckage.util.FileType.WEBVIEW;

/**
 * Created by acpm on 16/11/15.
 */
public class WebServer extends fi.iki.elonen.NanoHTTPD {

    private Context mContext;
    private SharedPreferences mPrefs;
    private KeyStore keyStore;

    public WebServer(String host, int port, Context context) throws IOException {
        super(host,port);
        mContext = context;
        mPrefs = mContext.getSharedPreferences(Module.PREFS, mContext.MODE_PRIVATE);

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            Enumeration<String> aliases = keyStore.aliases();
            List<String> keyAliases = new ArrayList<>();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }

            //use uuid as an alias, that way each installation has your own alias
            if(mPrefs.getString(Config.KEYPAIR_ALIAS,"").equals("")) {
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString(Config.KEYPAIR_ALIAS, UUID.randomUUID().toString());
                edit.apply();
            }

            String alias = mPrefs.getString(Config.KEYPAIR_ALIAS,"");

            boolean genNewKey = true;
            for (String key : keyAliases) {
                if(key.equals(alias)){
                    genNewKey = false;
                }
            }
            if(genNewKey) {
                KeyPair keyPair = generateKeys(alias);
                keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
            }
        }
        catch(Exception e) {
            Log.e("Error",e.getMessage());
        }

        if(mPrefs.getBoolean(Config.SP_SWITCH_AUTH, false)) {

            KeyManagerFactory keyManagerFactory = null;
            try {
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//
                keyManagerFactory.init(keyStore, "".toCharArray());
            } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
                e.printStackTrace();
            }

            makeSecure(NanoHTTPD.makeSSLSocketFactory(keyStore, keyManagerFactory), null);
        }
        mContext.registerReceiver(new InspeckageWebReceiver(mContext), new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_WEB"));

        start(10000);
    }

    public KeyPair generateKeys(String alias) {
        KeyPair keyPair = null;
        try {

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA","AndroidKeyStore");

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 1);

            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {

                KeyGenParameterSpec spec= new KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_SIGN|KeyProperties.PURPOSE_VERIFY)
                        .setCertificateSubject(new X500Principal("CN=Inspeckage, OU=ACPM, O=ACPM, C=BR"))
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .setCertificateNotBefore(start.getTime())
                        .setCertificateNotAfter(end.getTime())
                        .setKeyValidityStart(start.getTime())
                        .setKeyValidityEnd(end.getTime())
                        .setKeySize(2048)
                        .setCertificateSerialNumber(BigInteger.valueOf(1))
                        .build();

                keyGen.initialize(spec);
            }else {

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=Inspeckage, OU=ACPM, O=ACPM, C=BR"))
                        .setSerialNumber(BigInteger.valueOf(12345))
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();


                keyGen.initialize(spec);
            }

            keyPair = keyGen.generateKeyPair();
        } catch(GeneralSecurityException e) {
            Log.d("Inspeckage_Exception: ",e.getMessage());
        }
        return keyPair;
    }

    private Response ok(String type, String html, String cacheTime) {

        Response response = newFixedLengthResponse(Response.Status.OK, type, html);
        response.addHeader("Cache-Control", "public");
        response.addHeader("Cache-Control", "max-age="+cacheTime);
        return response;
    }

    private Response ok(String type, String html) {
        return newFixedLengthResponse(Response.Status.OK, type, html);
    }

    private Response ok(String html) {
        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, html);
    }

    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();

        if(mPrefs.getBoolean(Config.SP_SWITCH_AUTH, false)) {
            Map<String, String> headers = session.getHeaders();

            String authorization = headers.get("authorization");
            String base64 = "";
            if (authorization != null) {
                base64 = authorization.substring(6);
            }

            boolean logged = false;
            if (base64.equals(Base64.encodeBytes(mPrefs.getString(Config.SP_USER_PASS, "").getBytes()))) {
                logged = true;
            }

            if (!logged) {
                Response res = newFixedLengthResponse(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_HTML, "Denied!");
                res.addHeader("WWW-Authenticate", "Basic realm=\"Server\"");
                res.addHeader("Content-Length", "0");
                return res;
            }
        }

        //add ip and port to proxy fields
        if (mPrefs.getString(Config.SP_PROXY_HOST, "").equals("")) {
            String ip = session.getHeaders().get("http-client-ip");
            if (ip != null && !ip.trim().equals("")) {

                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString(Config.SP_PROXY_HOST, ip);
                edit.putString(Config.SP_PROXY_PORT, "4443");
                edit.apply();
            }
        }

        Map<String, String> parms = session.getParms();
        String type = parms.get("type");
        String html = new String();


        if (uri.equals("/")) {
            if (type != null) {
                switch (type) {
                    case "startWS":
                        return startWS(parms);
                    case "stopWS":
                        return stopWS();
                    case "filetree":
                        return fileTreeHtml();
                    case "checkapp":
                        return checkApp();
                    case "downloadfile":
                        return downloadFile(parms);
                    case "screenshot":
                        return takeScreenshot();
                    case "setarp":
                        return setArp(parms);
                    case "downapk":
                        return downloadApk();
                    case "downall":
                        return downloadAll();
                    case "finishapp":
                        finishApp();
                        return ok("OK");
                    case "restartapp":
                        finishApp();
                        startApp();
                        return ok("OK");
                    case "startapp":
                        startApp();
                        return ok("OK");
                    case "start":
                        return startComponent(parms);
                    case "file":
                        html = fileHtml(parms);
                        break;
                    case Config.SP_EXPORTED:
                        html = spExported(parms);
                        break;
                    case "flagsec":
                        html = flagSecure(parms);
                        break;
                    case "proxy":
                        html = proxy(parms);
                        break;
                    case "switchproxy":
                        html = switchProxy(parms);
                        break;
                    case "sslunpinning":
                        html = sslUnpinning(parms);
                        break;
                    case "adduserhooks":
                        return addUserHooks(parms);
                    case "addparamreplaces":
                        return addUserReplaces(parms);
                    case "addreturnreplaces":
                        return addUserReturnReplaces(parms);
                    case "getuserhooks":
                        return getUserHooks();
                    case "getparamreplaces":
                        return getUserReplaces();
                    case "getreturnreplaces":
                        return getUserReturnReplaces();
                    case "getbuild":
                        return getBuild();
                    case "addbuild":
                        return addBuild(parms);
                    case "deleteLogs":
                        return clearHooksLog(parms);
                    case "enableTab":
                        html = tabsCheckbox(parms);
                        break;
                    case "clipboard":
                        return addToClipboard(parms);
                    case "location":
                        return addLocation(parms);
                    case "geolocationSwitch":
                        return geoLocSwitch(parms);
                    case "resetfingerprint":
                        return resetFingerprint();
                }
            } else {
                html = setDefaultOptions();
            }

        } else if (uri.equals("/index.html")) {

            html = FileUtil.readHtmlFile(mContext, uri);

        } else if (uri.equals("/logcat.html")) {

            html = FileUtil.readHtmlFile(mContext, uri);
            html = html.replace("#ip_ws#", mPrefs.getString(Config.SP_SERVER_IP, "127.0.0.1"));
            html = html.replace("#port_ws#", String.valueOf(mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887)));
            return ok(html);
        } else if (uri.contains("/content/")) {

            html = FileUtil.readHtmlFile(mContext, uri);

            if (uri.contains("location.html")) {
                html = html.replace("#savedLoc#", mPrefs.getString(Config.SP_GEOLOCATION, ""));

                if (mPrefs.getBoolean(Config.SP_GEOLOCATION_SW, false)) {
                    html = html.replace("#switchLoc#", "<input type='checkbox' name='savedLoc' data-size='mini' checked>");
                }else {
                    html = html.replace("#switchLoc#", "<input type='checkbox' name='savedLoc' data-size='mini' unchecked>");
                }
            }

        } else if (uri.equals("/struct")) {

            String json = FileUtil.readFromFile(mPrefs, APP_STRUCT);//readHtmlFile(mContext, uri);
            return ok("text/json", json);

        } else {

            String fname = FileUtil.readHtmlFile(mContext, uri);

            if (uri.contains(".css")) {
                return ok("text/css", fname, "86400");
            }
            if (uri.contains(".js")) {
                return ok("text/javascript", fname, "86400");
            }
            if (uri.contains(".png")) {
                try {
                    InputStream f = mContext.getAssets().open("HTMLFiles" + uri);
                    return newChunkedResponse(Response.Status.OK, "image/png", f);
                    //return new Response(Response.Status.OK, "image/png", f, f.available());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (uri.contains(".ico")) {
                return ok("image/vnd.microsoft.icon", fname);
            }
            if (uri.contains(".eot")) {
                return ok("application/vnd.ms-fontobject", fname);
            }
            if (uri.contains(".svg")) {
                return ok("image/svg+xml", fname);
            }
            if (uri.contains(".ttf")) {
                return ok("application/x-font-ttf", fname);
            }
            if (uri.contains(".woff")) {
                return ok("application/font-woff", fname);
            }
            if (uri.contains(".woff2")) {
                return ok("font/woff2", fname);
            }
            return ok(fname);
        }

        if (mPrefs.getBoolean(Config.SP_EXPORTED, false)) {
            PackageDetail pd = new PackageDetail(mContext, mPrefs.getString(Config.SP_PACKAGE, ""));
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString(Config.SP_EXP_ACTIVITIES, pd.getExportedActivities());
            edit.putString(Config.SP_N_EXP_ACTIVITIES, pd.getNonExportedActivities());
            edit.apply();
        }


        if (!mPrefs.getString(Config.SP_DATA_DIR_TREE, "").equals("")) {
            html = html.replace("#filetree#", mPrefs.getString(Config.SP_DATA_DIR_TREE, ""));
        }

        String moduleEnable = "true";
        if (!isModuleEnabled()) {
            moduleEnable = "<font style=\"color:red; background:yellow;\">false</font>";
        }
        html = html.replace("#moduleEnable#", moduleEnable);
        html = replaceHtmlVariables(html);

        //Inspeckage version
        PackageInfo pInfo;
        try {
            pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            html = html.replace("#inspeckageVersion#", version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return ok(html);
    }

    private String setDefaultOptions() {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean(Config.SP_APP_IS_RUNNING, false);
        edit.putString(Config.SP_DATA_DIR_TREE, "");
        edit.apply();

        isRunning();
        fileTree();

        return FileUtil.readHtmlFile(mContext, "/index.html");
    }

    private String tabsCheckbox(Map<String, String> parms) {
        String tab = parms.get("tab");
        if (tab != null) {
            String state = parms.get("value");
            SharedPreferences.Editor edit = mPrefs.edit();

            switch (tab){
                case "shared":
                    edit.putBoolean(Config.SP_TAB_ENABLE_SHAREDP, Boolean.valueOf(state));
                    break;
                case "serialization":
                    edit.putBoolean(Config.SP_TAB_ENABLE_SERIALIZATION, Boolean.valueOf(state));
                    break;
                case "crypto":
                    edit.putBoolean(Config.SP_TAB_ENABLE_CRYPTO, Boolean.valueOf(state));
                    break;
                case "hash":
                    edit.putBoolean(Config.SP_TAB_ENABLE_HASH, Boolean.valueOf(state));
                    break;
                case "sqlite":
                    edit.putBoolean(Config.SP_TAB_ENABLE_SQLITE, Boolean.valueOf(state));
                    break;
                case "http":
                    edit.putBoolean(Config.SP_TAB_ENABLE_HTTP, Boolean.valueOf(state));
                    break;
                case "filesystem":
                    edit.putBoolean(Config.SP_TAB_ENABLE_FS, Boolean.valueOf(state));
                    break;
                case "misc":
                    edit.putBoolean(Config.SP_TAB_ENABLE_MISC, Boolean.valueOf(state));
                    break;
                case "webview":
                    edit.putBoolean(Config.SP_TAB_ENABLE_WV, Boolean.valueOf(state));
                    break;
                case "ipc":
                    edit.putBoolean(Config.SP_TAB_ENABLE_IPC, Boolean.valueOf(state));
                    break;
                case "phooks":
                    edit.putBoolean(Config.SP_TAB_ENABLE_PHOOKS, Boolean.valueOf(state));
                    break;

            }
            edit.apply();

        }
        return "#tab_scheckbox#";
    }

    private String sslUnpinning(Map<String, String> parms) {
        String ssl_switch = parms.get("sslswitch");
        if (ssl_switch != null) {
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putBoolean(Config.SP_UNPINNING, Boolean.valueOf(ssl_switch));
            edit.apply();
            if (Boolean.valueOf(ssl_switch))
                Util.showNotification(mContext, "Disable SSL");
        }
        return "#sslunpinning#";
    }

    private String switchProxy(Map<String, String> parms) {

        String pswitch = parms.get("value");
        if (pswitch != null) {

            String host = mPrefs.getString(Config.SP_PROXY_HOST, "");
            String port = mPrefs.getString(Config.SP_PROXY_PORT, "");

            SharedPreferences.Editor edit = mPrefs.edit();
            if (Boolean.valueOf(pswitch) && host.length() > 1 && port.length() > 0) {
                edit.putBoolean(Config.SP_SWITCH_PROXY, true);
                Util.showNotification(mContext, "Proxy Enable");
            } else {
                edit.putBoolean(Config.SP_SWITCH_PROXY, false);
            }
            edit.apply();
        }
        return "#proxy#";
    }

    private String proxy(Map<String, String> parms) {
        String host = parms.get("host");
        String port = parms.get("port");

        if (host != null && port != null && Util.isInt(port)) {

            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString(Config.SP_PROXY_PORT, port);
            edit.putString(Config.SP_PROXY_HOST, host);
            edit.apply();
            Util.showNotification(mContext, "Save Proxy: " + host + ":" + port);
        }
        return "#proxy#";
    }

    private String flagSecure(Map<String, String> parms) {
        String fs_switch = parms.get("fsswitch");
        if (fs_switch != null) {
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putBoolean(Config.SP_FLAG_SECURE, Boolean.valueOf(fs_switch));
            edit.apply();
            if (Boolean.valueOf(fs_switch))
                Util.showNotification(mContext, "Disable all FLAG_SECURE");
        }
        return "#flags#";
    }

    private String spExported(Map<String, String> parms) {
        String value = parms.get("value");
        if (value != null) {
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putBoolean(Config.SP_EXPORTED, Boolean.valueOf(value));
            edit.apply();
            if (Boolean.valueOf(value))
                Util.showNotification(mContext, "Export all activities");
        }
        return "#exported#";
    }

    private String fileHtml(Map<String, String> parms) {
        String value = parms.get("value");

        int count = 0;

        String c = parms.get("count");
        if (c == null || c.equals("")) {
            c = "0";
        }
        count = Integer.valueOf(c);

        if (value != null && !value.trim().equals("")) {
            return hooksContent(value, count);
        }
        return "";
    }

    private Response startComponent(Map<String, String> parms) {
        String component = parms.get("component");

        if (component.equals("activity")) {

            String activity = parms.get("activity");
            String action = parms.get("action");
            String category = parms.get("category");
            String data_uri = parms.get("datauri");
            String extra = parms.get("extra");
            String flags = parms.get("flags");
            String mimetype = parms.get("mimetype");

            startActivity(activity, action, category, data_uri, extra, flags, mimetype);

        } else if (component.equals("service")) {

        } else if (component.equals("broadcast")) {

        } else if (component.equals("provider")) {

            String uri_provider = parms.get("uri");
            return queryProvider(uri_provider);
        }

        return ok("");
    }

    private Response setArp(Map<String, String> parms) {
        String ip = parms.get("ip");
        String mac = parms.get("mac");
        Util.setARPEntry(ip, mac);
        Util.showNotification(mContext, "arp -s " + ip + " " + mac + "");

        return ok("OK");
    }

    private Response addToClipboard(Map<String, String> parms) {
        String value = parms.get("value");

        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_WEB");
        intent.putExtra("package", mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("value", value);
        intent.putExtra("action", "clipboard");
        mContext.sendBroadcast(intent, null);

        return ok("OK");
    }

    private Response downloadFile(Map<String, String> parms) {
        String path = parms.get("value");
        return downloadFileRoot(path);
    }

    private Response checkApp() {
        String isRunning = "App is running: true";
        if (!mPrefs.getBoolean(Config.SP_APP_IS_RUNNING, false)) {
            isRunning = "App is running: <font style=\"color:red; background:yellow;\">false</font>";
        }
        return ok(isRunning);
    }

    private Response fileTreeHtml() {
        String tree = mPrefs.getString(Config.SP_DATA_DIR_TREE, "");
        if (tree.equals("")) {
            tree = "<p class=\"text-danger\">The app is running?</p>";
        }
        return ok(tree);
    }

    private Response startWS(Map<String, String> parms) {
        String selected = parms.get("selected");

        Intent i = new Intent(mContext, LogService.class);
        i.putExtra("filter",selected);
        i.putExtra("port", mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887));

        mContext.startService(i);
        return ok("OK");
    }

    private Response stopWS() {
        mContext.stopService(new Intent(mContext, LogService.class));
        return ok("OK");
    }

    private Response addUserHooks(Map<String, String> parms) {

        String json = parms.get("jhooks");
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(Config.SP_USER_HOOKS, json);
        edit.apply();

        return ok("OK");
    }

    private Response addUserReplaces(Map<String, String> parms) {

        String json = parms.get("data");
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(Config.SP_USER_REPLACES, json);
        edit.apply();

        return ok("OK");
    }

    private Response addUserReturnReplaces(Map<String, String> parms) {

        String json = parms.get("data");
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(Config.SP_USER_RETURN_REPLACES, json);
        edit.apply();

        return ok("OK");
    }

    private Response getUserHooks() {

        String json = mPrefs.getString(Config.SP_USER_HOOKS,"");
        return ok("text/json", json);
    }

    private Response getUserReplaces() {

        String json = mPrefs.getString(Config.SP_USER_REPLACES,"");
        return ok("text/json", json);
    }

    private Response getUserReturnReplaces() {

        String json = mPrefs.getString(Config.SP_USER_RETURN_REPLACES,"");
        return ok("text/json", json);
    }

    private Response getBuild() {
        if(mPrefs.getString(Config.SP_FINGERPRINT_HOOKS,"").equals("")) {
            Fingerprint.getInstance(mContext).load();
        }

        String json = mPrefs.getString(Config.SP_FINGERPRINT_HOOKS,"");
        json = json.replace("{\"fingerprintItems\":[{","[{");
        json = json.replace("\"}]}","\"}]");
        return ok("text/json", json);
    }

    private Response resetFingerprint() {

        Fingerprint.getInstance(mContext).load();

        String json = mPrefs.getString(Config.SP_FINGERPRINT_HOOKS, "");
        json = json.replace("{\"fingerprintItems\":[{", "[{");
        json = json.replace("\"}]}", "\"}]");
        return ok("text/json", json);
    }

    private Response addBuild(Map<String, String> parms) {

        String json = parms.get("build");
        json = "{\"fingerprintItems\":"+json+"}";
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(Config.SP_FINGERPRINT_HOOKS, json);
        edit.apply();

        return ok("OK");
    }

    private Response clearHooksLog(Map<String, String> parms) {

        String hook = parms.get("value");

        String appPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!mPrefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
            appPath = mPrefs.getString(Config.SP_DATA_DIR, "");
        }

        String path = "";
        switch (hook) {
            case "userhooks":
                path = Config.P_USERHOOKS;
                break;
            case "misc":
                path = Config.P_MISC;
                break;
            case "webview":
                path = Config.P_WEBVIEW;
                break;
            case "http":
                path = Config.P_HTTP;
                break;
            case "fs":
                path = Config.P_FILESYSTEM;
                break;
            case "ipc":
                path = Config.P_IPC;
                break;
            case "sqlite":
                path = Config.P_SQLITE;
                break;
            case "hash":
                path = Config.P_HASH;
                break;
            case "crypto":
                path = Config.P_CRYPTO;
                break;
            case "serialization":
                path = Config.P_SERIALIZATION;
                break;
            case "prefs":
                path = Config.P_PREFS;
                break;
        }

        File root = new File(appPath + Config.P_ROOT + path);
        FileUtil.deleteFile(root);
        return ok("ok");
    }

    private String replaceHtmlVariables(String html) {
        html = html.replace("#proxy#", htmlProxy());
        html = html.replace("#flags#", flagSecureCheckbox());
        html = html.replace("#sslunpinning#", SSLUnpinningCheckbox());
        html = html.replace("#exported#", exportedCheckbox());
        html = html.replace("#tab_scheckbox#", tabsCheckbox());
        html = html.replace("#exported_act#", htmlExportedActivities());
        html = html.replace("#activities_list#", htmlActivityList());
        html = html.replace("#exported_provider#", htmlExportedProviders());
        html = html.replace("#non_exported_provider#", htmlNonExportedProviders());
        html = html.replace("#exported_services#", htmlExportedServices());
        html = html.replace("#exported_broadcast#", htmlExportedBroadcasts());

        html = html.replace("#appName#", mPrefs.getString(Config.SP_APP_NAME, "AppName"));

        String icon = "<img src=\"data:image/png;base64, "+mPrefs.getString(Config.SP_APP_ICON_BASE64, "AppIcon")+"\" width=\"80\" height=\"80\" />";
        html = html.replace("#appIcon#", icon);
        html = html.replace("#appVersion#", mPrefs.getString(Config.SP_APP_VERSION, "Version"));
        html = html.replace("#uid#", mPrefs.getString(Config.SP_UID, "uid"));
        html = html.replace("#gids#", mPrefs.getString(Config.SP_GIDS, "GIDs"));
        html = html.replace("#package#", mPrefs.getString(Config.SP_PACKAGE, "package"));
        html = html.replace("#data_dir#", mPrefs.getString(Config.SP_DATA_DIR, "Data Path"));
        html = html.replace("#isdebuggable#", mPrefs.getString(Config.SP_DEBUGGABLE, "?"));
        html = html.replace("#allowbackup#", mPrefs.getString(Config.SP_ALLOW_BACKUP, "?"));

        html = html.replace("#non_exported_act#", mPrefs.getString(Config.SP_N_EXP_ACTIVITIES, "Non Exported Activities").replace("\n", "</br>"));
        html = html.replace("#non_exported_services#", mPrefs.getString(Config.SP_N_EXP_SERVICES, "Services").replace("\n", "</br>"));
        html = html.replace("#non_exported_broadcast#", mPrefs.getString(Config.SP_N_EXP_BROADCAST, "Broadcast Receiver").replace("\n", "</br>"));
        html = html.replace("#req_permissions#", mPrefs.getString(Config.SP_REQ_PERMISSIONS, "Permissions").replace("\n", "</br>"));
        html = html.replace("#app_permissions#", mPrefs.getString(Config.SP_APP_PERMISSIONS, "Permissions").replace("\n", "</br>"));
        html = html.replace("#shared_libraries#", mPrefs.getString(Config.SP_SHARED_LIB, "Shared Libraries").replace("\n", "</br>"));
        return html;
    }

    private Response addLocation(Map<String, String> parms) {

        String loc = parms.get("geolocation");
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(Config.SP_GEOLOCATION, loc);
        edit.apply();
        return ok("OK");
    }

    private Response getLocation() {
        String loc = mPrefs.getString(Config.SP_GEOLOCATION,"");
        return ok(loc);
    }

    private Response geoLocSwitch(Map<String, String> parms) {
        String geo_switch = parms.get("geolocationSwitch");
        if (geo_switch != null) {
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putBoolean(Config.SP_GEOLOCATION_SW, Boolean.valueOf(geo_switch));
            edit.apply();
            if (Boolean.valueOf(geo_switch)) {
                Util.showNotification(mContext, "Geolocation ON");
            }
        }
        return ok("OK");
    }
    //HTML

    public String htmlNonExportedProviders() {
        String act = mPrefs.getString(Config.SP_N_EXP_PROVIDER, "Non Exported Providers");
        String[] providers = act.split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String provider : providers) {
            if (!provider.trim().equals("")) {

                if (provider.contains("GRANT:")) {
                    String[] actInfo = provider.split("GRANT:");
                    String info = "<a data-toggle=\"collapse\" href=\"#collapsenprovider" + i + "\" aria-expanded=\"false\" aria-controls=\"collapsenprovider" + i + "\">" + actInfo[0] + "</a>" +
                            "<div class=\"collapse\" id=\"collapsenprovider" + i + "\"><div class=\"well\">Grant URI Permission: " + actInfo[1].replace("|", "</br>") + "</div></div>";
                    i++;
                    sb.append(info + "</br>");
                } else {
                    sb.append(provider + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedProviders() {
        String act = mPrefs.getString(Config.SP_EXP_PROVIDER, "Exported Providers");
        String[] providers = act.split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String provider : providers) {
            if (!provider.trim().equals("")) {

                if (provider.contains("GRANT:")) {
                    String[] actInfo = provider.split("GRANT:");
                    String info = "<a data-toggle=\"collapse\" href=\"#collapseprovider" + i + "\" aria-expanded=\"false\" aria-controls=\"collapseprovider" + i + "\">" + actInfo[0] + "</a>" +
                            "<div class=\"collapse\" id=\"collapseprovider" + i + "\"><div class=\"well\">Grant URI Permission: " + actInfo[1].replace("|", "</br>") + "</div></div>";
                    i++;
                    sb.append(info + "</br>");
                } else {
                    sb.append(provider + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedBroadcasts() {
        String act = mPrefs.getString(Config.SP_EXP_BROADCAST, "Exported Broadcasts");
        String[] broadcasts = act.split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String broadcast : broadcasts) {
            if (!broadcast.trim().equals("")) {

                if (broadcast.contains("PERM:")) {
                    String[] actInfo = broadcast.split("PERM:");
                    String info = "<a data-toggle=\"collapse\" href=\"#collapsebroadcast" + i + "\" aria-expanded=\"false\" aria-controls=\"collapsebroadcast" + i + "\">" + actInfo[0] + "</a>" +
                            "<div class=\"collapse\" id=\"collapsebroadcast" + i + "\"><div class=\"well\">PERMISSION: " + actInfo[1] + "</div></div></br>";
                    i++;
                    sb.append(info);
                } else {
                    sb.append(broadcast + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedServices() {
        String act = mPrefs.getString(Config.SP_EXP_SERVICES, "Exported Services");
        String[] services = act.split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String service : services) {
            if (!service.trim().equals("")) {

                if (service.contains("PERM:")) {
                    String[] actInfo = service.split("PERM:");
                    String info = "<a data-toggle=\"collapse\" href=\"#collapseservice" + i + "\" aria-expanded=\"false\" aria-controls=\"collapseservice" + i + "\">" + actInfo[0] + "</a>" +
                            "<div class=\"collapse\" id=\"collapseservice" + i + "\"><div class=\"well\">PERMISSION: " + actInfo[1] + "</div></div></br>";
                    i++;
                    sb.append(info);
                } else {
                    sb.append(service + "</br>");
                }
            }
        }
        return sb.toString();
    }

    public String htmlExportedActivities() {
        String act = mPrefs.getString(Config.SP_EXP_ACTIVITIES, "Exported Activities");
        String[] activities = act.split("\n");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String activity : activities) {
            if (!activity.trim().equals("")) {

                if (activity.contains("PERM:")) {
                    String[] actInfo = activity.split("PERM:");

                    String info = "<a data-toggle=\"collapse\" href=\"#collapseActivity" + i + "\" aria-expanded=\"false\" aria-controls=\"collapseActivity" + i + "\">" + actInfo[0] + "</a>" +
                            "<div class=\"collapse\" id=\"collapseActivity" + i + "\"><div class=\"well\">PERMISSION: " + actInfo[1] + "</div></div>";
                    i++;
                    sb.append(info + "</br>");
                } else {
                    sb.append(activity + "</br>");
                }
            }
        }

        return sb.toString();
    }

    public String htmlActivityList() {

        String act = mPrefs.getString(Config.SP_EXP_ACTIVITIES, "Exported Activities");
        String[] activities = act.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String activity : activities) {
            if (!activity.trim().equals("")) {
                if (activity.contains("PERM:")) {
                    String[] actInfo = activity.split("PERM:");
                    sb.append("<li><a href=\"#\" onclick=\"selectAct('" + actInfo[0].trim() + "');\">" + actInfo[0].trim() + "</a></li>");
                } else {
                    sb.append("<li><a href=\"#\" onclick=\"selectAct('" + activity + "');\">" + activity + "</a></li>");
                }
            }
        }

        String nact = mPrefs.getString(Config.SP_N_EXP_ACTIVITIES, "N Exported Activities");
        String[] nactivities = nact.split("\n");

        if(nactivities.length > 0){
            sb.append("<li role='separator' class='divider'></li>");
        }
        String disabled = "";
        if(!mPrefs.getBoolean(Config.SP_APP_IS_RUNNING,false)){
            disabled = "class='disabled'";
        }

        for (String activity : nactivities) {
            if (!activity.trim().equals("")) {

                if (activity.contains("PERM:")) {
                    String[] actInfo = activity.split("PERM:");
                    sb.append("<li "+disabled+"><a href=\"#\" onclick=\"selectAct('" + actInfo[0].trim() + "');\">N " + actInfo[0].trim() + "</a></li>");
                } else {
                    sb.append("<li "+disabled+"><a href=\"#\" onclick=\"selectAct('" + activity + "');\">N " + activity + "</a></li>");
                }
            }
        }

        return sb.toString();
    }

    public String htmlProxy() {
        //Proxy
        String host = mPrefs.getString(Config.SP_PROXY_HOST, "");
        String port = mPrefs.getString(Config.SP_PROXY_PORT, "");
        Boolean sw = mPrefs.getBoolean(Config.SP_SWITCH_PROXY, false);

        String flag_s = "<input type='checkbox' name='switch_proxy' data-size='mini' unchecked>";
        if (sw) {
            flag_s = "<input type='checkbox' name='switch_proxy' data-size='mini' checked>";
        }

        return "<input type='text' class='form-control input-sm' id='host' value='" + host + "' placeholder='192.168.1.337'>" +
                "<input type='text' class='form-control input-sm' id='port' value='" + port + "' placeholder='8081'>" + flag_s;
    }

    public String htmlPrefsAccordion() {
        Map<String, String> prefsFiles = FileUtil.readMultiFile(mPrefs, Config.PREFS_BKP);
        String prefs_files = "";
        int i = 0;
        for (Map.Entry<String, String> e : prefsFiles.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();

            i++;
            prefs_files += "<div class='panel panel-default'><div class='panel-heading' role='tab' id='heading" + i + "'>" +
                    "<h4 class='panel-title'><a role='button' data-toggle='collapse' data-parent='#accordion' href='#collapse" + i + "' " +
                    "aria-expanded='true' aria-controls='collapse" + i + "'> " + k + " </a> </h4> </div> <div id='collapse" + i + "' " +
                    "class='panel-collapse collapse in' role='tabpanel' aria-labelledby='heading" + i + "'> " +
                    "<div class='panel-body'><textarea rows='"+countLines(v)+"' style=\"border:none;width:100%\" readonly>" +v + "</textarea></div> </div> </div>";
        }

        return prefs_files;
    }
    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length+1;
    }

    //CONFIG

    public String flagSecureCheckbox() {
        String flag_s = "<input type='checkbox' name='flag_sec' data-size='mini' unchecked>";

        boolean fs = mPrefs.getBoolean(Config.SP_FLAG_SECURE, false);
        if (fs) {
            flag_s = "<input type='checkbox' name='flag_sec' data-size='mini' checked>";
        }
        return flag_s;
    }

    public String SSLUnpinningCheckbox() {
        String flag_s = "<input type='checkbox' name='ssl_uncheck' data-size='mini' unchecked>";
        boolean fs = mPrefs.getBoolean(Config.SP_UNPINNING, false);
        if (fs) {
            flag_s = "<input type='checkbox' name='ssl_uncheck' data-size='mini' checked>";
        }
        return flag_s;
    }

    public String exportedCheckbox() {
        String flag_s = "<input type='checkbox' name='exported' data-size='mini' unchecked>";

        boolean fs = mPrefs.getBoolean(Config.SP_EXPORTED, false);
        if (fs) {
            flag_s = "<input type='checkbox' name='exported' data-size='mini' checked>";
        }
        return flag_s;
    }

    public String tabsCheckbox() {
        String shared = "<input type='checkbox' name='shared' data-size='mini' checked> Shared Preferences</br>";
        String serialization = "<input type='checkbox' name='serialization' data-size='mini' checked> Serialization</br>";
        String crypto = "<input type='checkbox' name='crypto' data-size='mini' checked> Crypto</br>";
        String hash = "<input type='checkbox' name='hash' data-size='mini' checked> Hash</br>";
        String sqlite = "<input type='checkbox' name='sqlite' data-size='mini' checked> SQLite</br>";
        String http = "<input type='checkbox' name='http' data-size='mini' checked> HTTP</br>";
        String filesystem = "<input type='checkbox' name='filesystem' data-size='mini' checked> File System</br>";
        String misc = "<input type='checkbox' name='misc' data-size='mini' checked> Misc.</br>";
        String webview = "<input type='checkbox' name='webview' data-size='mini' checked> WebView</br>";
        String ipc = "<input type='checkbox' name='ipc' data-size='mini' checked> IPC</br>";
        String phooks = "<input type='checkbox' name='phooks' data-size='mini' checked> + Hooks</br>";

        StringBuilder sb = new StringBuilder();

        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_SHAREDP, true)) {
            shared = "<input type='checkbox' name='shared' data-size='mini' unchecked>  Shared Preferences</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_SERIALIZATION, true)) {
            serialization = "<input type='checkbox' name='serialization' data-size='mini' unchecked> Serialization</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_CRYPTO, true)) {
            crypto = "<input type='checkbox' name='crypto' data-size='mini' unchecked> Crypto</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_HASH, true)) {
            hash = "<input type='checkbox' name='hash' data-size='mini' unchecked> Hash</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_SQLITE, true)) {
            sqlite = "<input type='checkbox' name='sqlite' data-size='mini' unchecked> SQLite</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_HTTP, true)) {
            http = "<input type='checkbox' name='http' data-size='mini' unchecked> HTTP</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_FS, true)) {
            filesystem = "<input type='checkbox' name='filesystem' data-size='mini' unchecked> File System</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_MISC, true)) {
            misc = "<input type='checkbox' name='misc' data-size='mini' unchecked> Misc.</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_WV, true)) {
            webview = "<input type='checkbox' name='webview' data-size='mini' unchecked> WebView</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_IPC, true)) {
            ipc = "<input type='checkbox' name='ipc' data-size='mini' unchecked> IPC</br>";
        }
        if (!mPrefs.getBoolean(Config.SP_TAB_ENABLE_PHOOKS, true)) {
            phooks = "<input type='checkbox' name='phooks' data-size='mini' unchecked> + Hooks</br>";
        }
        return sb.append("<div class=\"col-md-6\" style=\"line-height:200%;\">").append(shared).append(serialization).append(crypto).append(hash).append(sqlite).append(http).append("</div><div class=\"col-md-6\" style=\"line-height:200%;\">")
                .append(filesystem).append(misc).append(webview).append(ipc).append(phooks).append("</div>").toString();
    }


    //ACTIONS

    public Response takeScreenshot() {
        int time = (int) Calendar.getInstance().getTimeInMillis();
        String fileName = String.valueOf(time) + ".png";

        Util.takeScreenshot(fileName);

        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String absolutePath = sdcardPath + Config.P_ROOT + "/"+ fileName;

        try {
            FileInputStream f = new FileInputStream(absolutePath);
            File file = new File(absolutePath);
            Response res = newChunkedResponse(Response.Status.OK, "image/png", f);//new Response(Response.Status.OK, "image/png", f, (int) file.length());
            res.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            return res;
        } catch (FileNotFoundException e) {
            //response with some alert
            e.printStackTrace();
        }
        return null;
    }


    public Response downloadFileRoot(String path) {

        String filename = path.substring(path.lastIndexOf("/") + 1);

        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        if(new File(sdcardPath + Config.P_ROOT ).exists() && new File("/storage/emulated/legacy").exists()){
            sdcardPath = "/storage/emulated/legacy";
        }

        String absolutePath = sdcardPath + Config.P_ROOT + "/" + filename;
        Util.copyFileRoot(path, absolutePath);
        try {
            FileInputStream f = new FileInputStream(absolutePath);
            File file = new File(absolutePath);
            Response res = newChunkedResponse(Response.Status.OK, "application/octet-stream", f);//new Response(Response.Status.OK, "application/octet-stream", f, (int) file.length());
            res.addHeader("Content-Disposition", "attachment;filename=" + filename);

            file.delete();
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Response downloadAll() {

        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            int time = (int) Calendar.getInstance().getTimeInMillis();
            String zipName = mPrefs.getString(Config.SP_PACKAGE, "") + "-" + String.valueOf(time) + ".zip";

            String fullZipPath = sdcardPath + "/" + Config.P_ROOT + "/" + zipName;
            String fullPath = mPrefs.getString(Config.SP_DATA_DIR, "") + Config.P_ROOT;

            if (mPrefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                fullPath = sdcardPath + "/" + Config.P_ROOT+ "/"+ mPrefs.getString(Config.SP_PACKAGE,"");
            }

            FileUtil.zipFolder(fullPath, fullZipPath);

            FileInputStream f = new FileInputStream(fullZipPath);
            File file = new File(fullZipPath);
            Response res = newChunkedResponse(Response.Status.OK, "application/zip", f);//new Response(Response.Status.OK, "application/zip", f, (int) file.length());
            res.addHeader("Content-Disposition", "attachment;filename=" + file.getName());

            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Response downloadApk() {
        String absolutePath = mPrefs.getString(Config.SP_APK_DIR, "");
        try {
            FileInputStream f = new FileInputStream(absolutePath);
            File file = new File(absolutePath);
            Response res = newChunkedResponse(Response.Status.OK, "application/vnd.android.package-archive", f);//new Response(Response.Status.OK, "application/vnd.android.package-archive", f, (int) file.length());
            String appName = mPrefs.getString(Config.SP_PACKAGE, "") + ".apk";
            res.addHeader("Content-Disposition", "attachment;filename=" + appName);
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void startApp() {

        PackageDetail pd = new PackageDetail(mContext, mPrefs.getString(Config.SP_PACKAGE, ""));
        Intent i = pd.getLaunchIntent();
        mContext.startActivity(i);
    }

    public void finishApp() {

        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra("package", mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "finish");
        mContext.sendBroadcast(intent, null);
    }

    public void fileTree() {

        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra("package", mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "fileTree");
        mContext.sendBroadcast(intent, null);
    }

    public void isRunning() {

        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra("package", mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "checkApp");
        mContext.sendBroadcast(intent, null);
    }

    //COMPONENTS

    public void startActivity(String activity, String action, String category, String data_uri, String extra, String flags, String mimetype) {

        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra("package", mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "startAct");

        intent.putExtra("activity", activity);
        intent.putExtra("intent_action", action);
        intent.putExtra("data_uri", data_uri);
        intent.putExtra("extra", extra);
        intent.putExtra("flags", flags);
        intent.putExtra("mimetype", mimetype);
        intent.putExtra("category", category);

        if(mPrefs.getBoolean(Config.SP_APP_IS_RUNNING,false)){
            mContext.sendBroadcast(intent, null);
        }else {
            Intent i = new Intent();
            i.setClassName(mPrefs.getString(Config.SP_PACKAGE, ""), activity);

            //FLAGS
            if(!flags.trim().equals("")) {
                Field[] fields = Intent.class.getFields();
                for (Field f : fields) {

                    try {
                        Object value = f.get(i);

                        if (flags.trim().contains(f.getName())) {
                            i.addFlags((int) value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            //DATA_URI
            if(!data_uri.trim().equals("")){
                Uri u = Uri.parse(data_uri);
                i.setData(u);
            }

            if(!category.trim().equals("")){
                i.addCategory(category);
            }

            if(!mimetype.trim().equals("")){
                i.normalizeMimeType(mimetype);
            }

            if(!extra.trim().equals("")){

                String[] extras = new String[]{extra};
                if(extra.contains(";")){
                    extras = extra.split(";");
                }

                for(String e : extras){
                    String[] values = e.split(",");

                    if(values.length==3){

                        if(values[0].trim().toLowerCase().equals("string")){
                            i.putExtra(values[1],values[2]);
                        }

                        if(values[0].trim().toLowerCase().equals("boolean")){
                            i.putExtra(values[1],Boolean.valueOf(values[2]));
                        }

                        if(values[0].trim().toLowerCase().equals("int")){
                            i.putExtra(values[1], Integer.valueOf(values[2]));
                        }

                        if(values[0].trim().toLowerCase().equals("float")){
                            i.putExtra(values[1],Float.valueOf(values[2]));
                        }

                        if(values[0].trim().toLowerCase().equals("double")){
                            i.putExtra(values[1],Double.valueOf(values[2]));
                        }
                    }
                }

            }

            mContext.startActivity(i);
        }
    }

    public Response queryProvider(String uri) {

        Intent intent = new Intent("mobi.acpm.inspeckage.INSPECKAGE_FILTER");
        intent.putExtra("package", mPrefs.getString(Config.SP_PACKAGE, ""));
        intent.putExtra("action", "query");
        intent.putExtra("uri", uri);
        mContext.sendBroadcast(intent, null);

        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "OK");
    }

    //HOOKS TABS

    public String hooksContent(String type, int count) {

        String html = "";
        int countTmp = 0;
        switch (type) {
            case "serialization": {
                html = FileUtil.readFromFile(mPrefs, SERIALIZATION).replace(SerializationHook.TAG, "");

                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {


                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }

                        if (x[i].length() > 170) {
                            String len170 = x[i].substring(0, 135);
                            String rest = x[i].substring(135);
                            x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + len170 +
                                    "<div class=\"collapse\"><div class=\"breakWord\">" + rest + "</div></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                            continue;
                        }
                        x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i] + "</br>";

                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i]);
                    }

                    //need load from here for callapse work correctaly
                    String script = "<script>$(document).ready(function() {" +
                            "$('a').on('click', function(e) {" +
                            "                    e.preventDefault();" +
                            "                    var $this = $(this);" +
                            "                    var $collapse = $this.closest('.collapse-group').find('.collapse');" +
                            "                    $collapse.collapse('toggle');" +
                            "                });" +
                            "});</script>";
                    sb.append(script);

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "fs": {
                html = FileUtil.readFromFile(mPrefs, FILESYSTEM).replace(FileSystemHook.TAG, "");

                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {
                        x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i];

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }
                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i] + "</br>");
                    }

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "misc": {
                html = FileUtil.readFromFile(mPrefs, MISC).replace(MiscHook.TAG, "");
                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {
                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"breakWord\"><span class=\"label label-info\"> " + (i + 1) + "</span>  " + x[i] + "</div>";
                        } else {
                            x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";
                        }

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }
                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i]);
                    }

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "http": {
                html = FileUtil.readFromFile(mPrefs, HTTP).replace(HttpHook.TAG, "");
                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {


                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"breakWord\"><span class=\"label label-info\">" + (i + 1) + "</span> " + x[i] + "</div>";
                        } else {

                            String color = "label-info";
                            if (x[i].contains("Possible pinning")) {
                                color = "label-danger";
                            }

                            x[i] = "<span class=\"label " + color + "\">" + (i + 1) + "</span> " + Html.escapeHtml(x[i]) + "</br>";
                        }
                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }

                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i]);
                    }

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "wv": {
                html = FileUtil.readFromFile(mPrefs, WEBVIEW).replace(WebViewHook.TAG, "");
                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {
                        if (x[i].contains("addJavascriptInterface(Object, ")) {

                            x[i] = "<a href=\"#\" role=\"button\" class=\"btn popovers\" data-toggle=\"popover\" " +
                                    "title=\"\" data-content=\"" + "Injects the supplied Java object into this WebView. " +
                                    "The object is injected into the JavaScript context of the main frame, " +
                                    "using the supplied name. This allows the Java object's methods to " +
                                    "be accessed from JavaScript. <a href='http://developer.android.com/intl/pt-br/reference/android/webkit/WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)' target='_blank' title='link'> read more.</a>\">" + x[i] + " </a>";
                        } else {
                            x[i] = x[i];
                        }

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }
                    }
                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (String aX : x) {
                        sb.append(aX + "</br>");
                    }

                    String script = "<script>$(document).ready(function() {" +
                            "$('[data-toggle=popover]').popover({html:true})" +
                            "});</script>";

                    sb.append(script);

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "ipc": {
                html = FileUtil.readFromFile(mPrefs, IPC).replace(IPCHook.TAG, "");
                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {
                        x[i] = "<span class=\"label label-default\">" + (i + 1) + "</span>   " + x[i];

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }
                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i] + "</br>");
                    }

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "crypto": {
                html = FileUtil.readFromFile(mPrefs, CRYPTO).replace(CryptoHook.TAG, "");
                if(!html.equals("")) {
                    String[] x = html.split("</br>");

                    for (int i = 0; i < x.length; i++) {

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }

                        if (x[i].length() > 170) {
                            String len170 = x[i].substring(0, 135);
                            String rest = x[i].substring(135);
                            x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + len170 +
                                    "<div class=\"collapse\"><p class=\"breakWord\">" + rest + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                            continue;
                        }
                        x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";


                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i]);
                    }

                    //need load from here for callapse work correctaly
                    String script = "<script>$(document).ready(function() {" +
                            "$('a').on('click', function(e) {" +
                            "                    e.preventDefault();" +
                            "                    var $this = $(this);" +
                            "                    var $collapse = $this.closest('.collapse-group').find('.collapse');" +
                            "                    $collapse.collapse('toggle');" +
                            "                });" +
                            "});</script>";
                    sb.append(script);

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "prefs": {
                html = FileUtil.readFromFile(mPrefs, PREFS).replace(SharedPrefsHook.TAG, "");
                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {

                        String color = "danger";
                        if (x[i].contains("GET[")) {
                            color = "info";
                        } else if (x[i].contains("CONTAINS[")) {
                            color = "warning";
                        } else if (x[i].contains("PUT[")) {
                            color = "danger";
                        }

                        if (x[i].length() > 170) {
                            x[i] = "<tr><td><div class=\"breakWord\"><span class=\"label label-" + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</div></td></tr>";
                        } else {
                            x[i] = "<tr><td><span class=\"label label-" + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br></td></tr>";
                        }
                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }
                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();
                    String tableBefore = "<table class=\"table\"><tbody>";
                    String tableAfter = "</tbody></table>";
                    //sb.append(tableBefore);
                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i]);
                    }
                    //sb.append(tableAfter);
                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "hash": {

                html = FileUtil.readFromFile(mPrefs, HASH).replace(HashHook.TAG, "");
                if(!html.equals("")) {
                    String[] x = html.split("</br>");

                    for (int i = 0; i < x.length; i++) {

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }

                        if (x[i].length() > 170) {
                            String len170 = x[i].substring(0, 135);
                            String rest = x[i].substring(135);
                            x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + len170 +
                                    "<div class=\"collapse\"><p class=\"breakWord\">" + rest + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                            continue;
                        }
                        x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";


                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i]);
                    }

                    //need load from here for callapse work correctaly
                    String script = "<script>$(document).ready(function() {" +
                            "$('a').on('click', function(e) {" +
                            "                    e.preventDefault();" +
                            "                    var $this = $(this);" +
                            "                    var $collapse = $this.closest('.collapse-group').find('.collapse');" +
                            "                    $collapse.collapse('toggle');" +
                            "                });" +
                            "});</script>";
                    sb.append(script);

                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
            case "sqlite": {
                html = FileUtil.readFromFile(mPrefs, SQLITE).replace(SQLiteHook.TAG, "");

                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {

                        String color;
                        if (x[i].contains("INSERT INTO")) {
                            color = "label-info";
                        } else if (x[i].contains("UPDATE")) {
                            color = "label-warning";
                        } else if (x[i].contains("execSQL(")) {
                            color = "label-danger";
                        } else if (x[i].contains("SELECT")) {
                            color = "label-success";
                            x[i].replace("\n", "</br>");
                        } else {
                            color = "label-default";
                        }

                        if (x[i].length() > 170) {
                            x[i] = "<div class=\"breakWord\"><span class=\"label " + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</div>";
                        } else {
                            x[i] = "<span class=\"label " + color + "\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i]) + "</br>";
                        }

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }
                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i]);
                    }

                    if (count == -1) {
                        html = sb.toString().replace("</br></br>", "</br>");
                    } else {
                        html = "" + countTmp;
                    }
                }

                break;
            }
            case "userhooks": {
                html = FileUtil.readFromFile(mPrefs, USERHOOKS).replace(UserHooks.TAG, "");

                if(!html.equals("")) {
                    String[] x = html.split("</br>");
                    for (int i = 0; i < x.length; i++) {

                        if (x[i].length() > 470) {
                            String len300 = x[i].substring(0, 400);
                            String rest = x[i].substring(400);

                            x[i] = "<div class=\"collapse-group\"> <div class=\"breakWord\"><span class=\"label label-default\">" + (i + 1) + "</span>   " + len300 + "</div>" +
                                    "<div class=\"collapse\"><p class=\"breakWord\">" + rest + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";

                        } else {
                            x[i] = "<span class=\"label label-default\">" + (i + 1) + "</span>   " + x[i];
                        }

                        if ((i + 1) > count) {
                            countTmp = (i + 1);
                        } else {
                            countTmp = count;
                        }
                    }

                    List<String> ls = Arrays.asList(x);

                    Collections.reverse(ls);
                    x = (String[]) ls.toArray();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < x.length; i++) {
                        if (i < 500)
                            sb.append(x[i] + "</br>");
                    }
                    //need load from here for callapse work correctaly
                    String script = "<script>$(document).ready(function() {" +
                            "$('a').on('click', function(e) {" +
                            "                    e.preventDefault();" +
                            "                    var $this = $(this);" +
                            "                    var $collapse = $this.closest('.collapse-group').find('.collapse');" +
                            "                    $collapse.collapse('toggle');" +
                            "                });" +
                            "});</script>";
                    sb.append(script);
                    if (count == -1) {
                        html = sb.toString();
                    } else {
                        html = "" + countTmp;
                    }
                }
                break;
            }
        }

        if (type.equals("pfiles")) {
            html = htmlPrefsAccordion();
        }

        return html;
    }

    //TEST MODULE ENABLED

    public static boolean isModuleEnabled() {
        return false;
    }
}
package mobi.acpm.inspeckage.webserver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import mobi.acpm.inspeckage.hooks.WebViewHook;
import mobi.acpm.inspeckage.log.LogService;
import mobi.acpm.inspeckage.receivers.InspeckageWebReceiver;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.FileUtil;
import mobi.acpm.inspeckage.util.PackageDetail;
import mobi.acpm.inspeckage.util.Util;

import static mobi.acpm.inspeckage.util.FileType.CRYPTO;
import static mobi.acpm.inspeckage.util.FileType.FILESYSTEM;
import static mobi.acpm.inspeckage.util.FileType.HASH;
import static mobi.acpm.inspeckage.util.FileType.HTTP;
import static mobi.acpm.inspeckage.util.FileType.IPC;
import static mobi.acpm.inspeckage.util.FileType.MISC;
import static mobi.acpm.inspeckage.util.FileType.PREFS;
import static mobi.acpm.inspeckage.util.FileType.SERIALIZATION;
import static mobi.acpm.inspeckage.util.FileType.SQLITE;
import static mobi.acpm.inspeckage.util.FileType.WEBVIEW;

/**
 * Created by acpm on 16/11/15.
 */
public class WebServer extends NanoHTTPD {

    private Context mContext;
    private SharedPreferences mPrefs;

    public WebServer(int port, Context context) throws IOException {
        super(port);
        mContext = context;
        mPrefs = mContext.getSharedPreferences(Module.PREFS, mContext.MODE_WORLD_READABLE);

        mContext.registerReceiver(new InspeckageWebReceiver(mContext),
                new IntentFilter("mobi.acpm.inspeckage.INSPECKAGE_WEB"));

        start();
    }

    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();

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

        String html = "";

        if (uri.equals("/")) {

            if (type != null) {

                if (type.equals("startWS")) {

                    String selected = parms.get("selected");

                    Intent i = new Intent(mContext, LogService.class);
                    i.putExtra("filter",selected);
                    i.putExtra("port", mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887));

                    mContext.startService(i);

                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "OK");

                } else if (type.equals("stopWS")) {

                    mContext.stopService(new Intent(mContext, LogService.class));

                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "OK");

                } else if (type.equals("filetree")) {

                    String tree = mPrefs.getString(Config.SP_DATA_DIR_TREE, "");
                    if (tree.equals("")) {
                        tree = "<p class=\"text-danger\">The app is running?</p>";
                    }
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, tree);

                } else if (type.equals("checkapp")) {

                    String isRunning = "App is running: true";
                    if (!mPrefs.getBoolean(Config.SP_APP_IS_RUNNING, false)) {
                        isRunning = "App is running: <font style=\"color:red; background:yellow;\">false</font>";
                    }
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, isRunning);

                } else if (type.equals("downloadfile")) {

                    String path = parms.get("value");
                    return downloadFileRoot(path);

                } else if (type.equals("screenshot")) {


                    return takeScreenshot();

                } else if (type.equals("setarp")) {

                    String ip = parms.get("ip");
                    String mac = parms.get("mac");
                    Util.setARPEntry(ip, mac);
                    Util.showNotification(mContext, "arp -s " + ip + " " + mac + "");

                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "OK");

                } else if (type.equals("downapk")) {

                    return downloadApk();

                } else if (type.equals("downall")) {

                    return downloadAll();

                } else if (type.equals("finishapp")) {
                    finishApp();
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "OK");
                } else if (type.equals("restartapp")) {

                    finishApp();

                    startApp();

                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "OK");

                } else if (type.equals("startapp")) {

                    startApp();

                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "OK");

                } else if (type.equals("start")) {

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

                    return newFixedLengthResponse("");

                } else if (type.equals("file")) {

                    String value = parms.get("value");

                    if (value != null && !value.trim().equals(""))
                        html = hooksContent(value);

                } else if (type.equals(Config.SP_EXPORTED)) {

                    String value = parms.get("value");
                    if (value != null) {
                        SharedPreferences.Editor edit = mPrefs.edit();
                        edit.putBoolean(Config.SP_EXPORTED, Boolean.valueOf(value));
                        edit.apply();
                        if (Boolean.valueOf(value))
                            Util.showNotification(mContext, "Export all activities");
                    }
                    html = "#exported#";

                } else if (type.equals("flagsec")) {

                    String fs_switch = parms.get("fsswitch");
                    if (fs_switch != null) {
                        SharedPreferences.Editor edit = mPrefs.edit();
                        edit.putBoolean(Config.SP_FLAG_SECURE, Boolean.valueOf(fs_switch));
                        edit.apply();
                        if (Boolean.valueOf(fs_switch))
                            Util.showNotification(mContext, "Disable all FLAG_SECURE");
                    }
                    html = "#flags#";

                } else if (type.equals("proxy")) {
                    html = "#proxy#";

                    String host = parms.get("host");
                    String port = parms.get("port");

                    if (host != null && port != null && Util.isInt(port)) {

                        SharedPreferences.Editor edit = mPrefs.edit();
                        edit.putString(Config.SP_PROXY_PORT, port);
                        edit.putString(Config.SP_PROXY_HOST, host);
                        edit.apply();
                        Util.showNotification(mContext, "Save Proxy: " + host + ":" + port);
                    }

                } else if (type.equals("switchproxy")) {
                    html = "#proxy#";

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

                } else if (type.equals("sslunpinning")) {
                    String ssl_switch = parms.get("sslswitch");
                    if (ssl_switch != null) {
                        SharedPreferences.Editor edit = mPrefs.edit();
                        edit.putBoolean(Config.SP_UNPINNING, Boolean.valueOf(ssl_switch));
                        edit.apply();
                        if (Boolean.valueOf(ssl_switch))
                            Util.showNotification(mContext, "Disable SSL");
                    }
                    html = "#sslunpinning#";
                }
            } else {

                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putBoolean(Config.SP_APP_IS_RUNNING, false);
                edit.putString(Config.SP_DATA_DIR_TREE, "");
                edit.apply();

                isRunning();
                fileTree();

                html = FileUtil.readHtmlFile(mContext, "/index.html");
            }

        } else if (uri.equals("/index.html")) {

            html = FileUtil.readHtmlFile(mContext, uri);

        } else if (uri.equals("/logcat.html")) {

            html = FileUtil.readHtmlFile(mContext, uri);
            html = html.replace("#ip_ws#", mPrefs.getString(Config.SP_SERVER_IP, "127.0.0.1"));
            html = html.replace("#port_ws#", String.valueOf(mPrefs.getInt(Config.SP_WSOCKET_PORT, 8887)));
            return newFixedLengthResponse(html);
        } else {

            String fname = FileUtil.readHtmlFile(mContext, uri);

            if (uri.contains(".css")) {
                return newFixedLengthResponse(Response.Status.OK, "text/css", fname);
            }
            if (uri.contains(".js")) {
                return newFixedLengthResponse(Response.Status.OK, "text/javascript", fname);
            }
            if (uri.contains(".png")) {
                try {
                    InputStream f = mContext.getAssets().open("HTMLFiles" + uri);

                    return new Response(Response.Status.OK, "image/png", f, f.available());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (uri.contains(".ico")) {
                return newFixedLengthResponse(Response.Status.OK, "image/vnd.microsoft.icon", fname);
            }
            if (uri.contains(".eot")) {
                return newFixedLengthResponse(Response.Status.OK, "application/vnd.ms-fontobject", fname);
            }
            if (uri.contains(".svg")) {
                return newFixedLengthResponse(Response.Status.OK, "image/svg+xml", fname);
            }
            if (uri.contains(".ttf")) {
                return newFixedLengthResponse(Response.Status.OK, "application/x-font-ttf", fname);
            }
            if (uri.contains(".woff")) {
                return newFixedLengthResponse(Response.Status.OK, "font/x-woff", fname);
            }
            if (uri.contains(".woff2")) {
                return newFixedLengthResponse(Response.Status.OK, "font/woff2", fname);
            }
            return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, fname);
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
        html = html.replace("#proxy#", htmlProxy());
        html = html.replace("#flags#", flagSecureCheckbox());
        html = html.replace("#sslunpinning#", SSLUnpinningCheckbox());
        html = html.replace("#exported#", exportedCheckbox());
        html = html.replace("#exported_act#", htmlExportedActivities());
        html = html.replace("#activities_list#", htmlActivityList());
        html = html.replace("#exported_provider#", htmlExportedProviders());
        html = html.replace("#non_exported_provider#", htmlNonExportedProviders());
        html = html.replace("#exported_services#", htmlExportedServices());
        html = html.replace("#exported_broadcast#", htmlExportedBroadcasts());

        html = html.replace("#appName#", mPrefs.getString(Config.SP_APP_NAME, "AppName"));
        html = html.replace("#appVersion#", mPrefs.getString(Config.SP_APP_VERSION, "Version"));
        html = html.replace("#uid#", mPrefs.getString(Config.SP_UID, "uid"));
        html = html.replace("#gids#", mPrefs.getString(Config.SP_GIDS, "GIDs"));
        html = html.replace("#package#", mPrefs.getString(Config.SP_PACKAGE, "package"));
        html = html.replace("#data_dir#", mPrefs.getString(Config.SP_DATA_DIR, "Data Path"));
        html = html.replace("#isdebuggable#", mPrefs.getString(Config.SP_DEBUGGABLE, "?"));

        html = html.replace("#non_exported_act#", mPrefs.getString(Config.SP_N_EXP_ACTIVITIES, "Non Exported Activities").replace("\n", "</br>"));
        html = html.replace("#non_exported_services#", mPrefs.getString(Config.SP_N_EXP_SERVICES, "Services").replace("\n", "</br>"));
        html = html.replace("#non_exported_broadcast#", mPrefs.getString(Config.SP_N_EXP_BROADCAST, "Broadcast Receiver").replace("\n", "</br>"));
        html = html.replace("#req_permissions#", mPrefs.getString(Config.SP_REQ_PERMISSIONS, "Permissions").replace("\n", "</br>"));
        html = html.replace("#app_permissions#", mPrefs.getString(Config.SP_APP_PERMISSIONS, "Permissions").replace("\n", "</br>"));
        html = html.replace("#shared_libraries#", mPrefs.getString(Config.SP_SHARED_LIB, "Shared Libraries").replace("\n", "</br>"));

        //Inspeckage version
        PackageInfo pInfo;
        try {
            pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            html = html.replace("#inspeckageVersion#", version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return newFixedLengthResponse(html);
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
                    "<div class='panel-body'><xmp>" + v + "</xmp></div> </div> </div>";
        }

        return prefs_files;
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
            Response res = new Response(Response.Status.OK, "image/png", f, (int) file.length());
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

        if(new File("/storage/emulated/legacy").exists()){
            sdcardPath = "/storage/emulated/legacy";
        }

        String absolutePath = sdcardPath + Config.P_ROOT + "/" + filename;
        Util.copyFileRoot(path, absolutePath);
        try {
            FileInputStream f = new FileInputStream(absolutePath);
            File file = new File(absolutePath);
            Response res = new Response(Response.Status.OK, "application/octet-stream", f, (int) file.length());
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
            Response res = new Response(Response.Status.OK, "application/zip", f, (int) file.length());
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
            Response res = new Response(Response.Status.OK, "application/vnd.android.package-archive", f, (int) file.length());
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

    public String hooksContent(String type) {

        String html = "";

        switch (type) {
            case "serialization": {
                html = FileUtil.readFromFile(mPrefs, SERIALIZATION).replace(SerializationHook.TAG, "");
                String[] x = html.split("</br>");
                for (int i = 0; i < x.length; i++) {


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
                    if (i < 1000)
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
                html = sb.toString();

                break;
            }
            case "fs": {
                html = FileUtil.readFromFile(mPrefs, FILESYSTEM).replace(FileSystemHook.TAG, "");
                String[] x = html.split("</br>");
                for (int i = 0; i < x.length; i++) {
                    x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i];
                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 1000)
                        sb.append(x[i] + "</br>");
                }

                html = sb.toString();
                break;
            }
            case "misc": {
                html = FileUtil.readFromFile(mPrefs, MISC).replace(MiscHook.TAG, "");

                String[] x = html.split("</br>");
                for (int i = 0; i < x.length; i++) {
                    if (x[i].length() > 170) {
                        x[i] = "<div class=\"breakWord\"><span class=\"label label-info\"> " + (i + 1) + "</span>  " + x[i]+ "</div>";
                    }else {
                        x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i]+"</br>";
                    }
                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 1000)
                        sb.append(x[i]);
                }

                html = sb.toString();

                break;
            }
            case "http": {
                html = FileUtil.readFromFile(mPrefs, HTTP).replace(HttpHook.TAG, "");

                String[] x = html.split("</br>");
                for (int i = 0; i < x.length; i++) {



                    if (x[i].length() > 170) {
                        x[i] = "<div class=\"breakWord\"><span class=\"label label-info\">" + (i + 1) + "</span> " + x[i]+ "</div>";
                    }else {

                        String color = "label-info";
                        if(x[i].contains("Possible pinning")){
                            color = "label-danger";
                        }

                        x[i] = "<span class=\"label "+color+"\">" + (i + 1) + "</span> " + x[i] +"</br>";
                    }


                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 1000)
                        sb.append(x[i]);
                }

                html = sb.toString();

                break;
            }
            case "wv": {
                html = FileUtil.readFromFile(mPrefs, WEBVIEW).replace(WebViewHook.TAG, "");

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

                html = sb.toString();

                break;
            }
            case "ipc": {
                html = FileUtil.readFromFile(mPrefs, IPC).replace(IPCHook.TAG, "");

                String[] x = html.split("</br>");
                for (int i = 0; i < x.length; i++) {
                    x[i] = "<span class=\"label label-default\">" + (i + 1) + "</span>   " + x[i];
                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 1000)
                        sb.append(x[i] + "</br>");
                }

                html = sb.toString();

                break;
            }
            case "crypto": {
                html = FileUtil.readFromFile(mPrefs, CRYPTO).replace(CryptoHook.TAG, "");

                String[] x = html.split("</br>");

                for (int i = 0; i < x.length; i++) {


                    if (x[i].length() > 170) {
                        String len170 = x[i].substring(0, 135);
                        String rest = x[i].substring(135);
                        x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + len170 +
                                "<div class=\"collapse\"><p class=\"breakWord\">" + rest + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                        continue;
                    }
                    x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i] + "</br>";
                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 1000)
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
                html = sb.toString();

                break;
            }
            case "prefs": {
                html = FileUtil.readFromFile(mPrefs, PREFS).replace(SharedPrefsHook.TAG, "");

                String[] x = html.split("</br>");
                for (int i = 0; i < x.length; i++) {

                    String color = "label-danger";
                    if (x[i].contains("GET[")) {
                        color = "label-info";
                    } else if (x[i].contains("CONTAINS[")) {
                        color = "label-warning";
                    } else if (x[i].contains("PUT[")) {
                        color = "label-danger";
                    }

                    if (x[i].length() > 170) {
                        x[i] = "<div class=\"breakWord\"><span class=\"label "+color+"\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i])+"</div>";
                    }else{
                        x[i] = "<span class=\"label "+color+"\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i])+"</br>";
                    }

                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 2000)
                        sb.append(x[i]);
                }

                html = sb.toString();

                break;
            }
            case "hash": {

                html = FileUtil.readFromFile(mPrefs, HASH).replace(HashHook.TAG, "");
                String[] x = html.split("</br>");

                for (int i = 0; i < x.length; i++) {

                    if (x[i].length() > 170) {
                        String len170 = x[i].substring(0, 135);
                        String rest = x[i].substring(135);
                        x[i] = "<div class=\"collapse-group\"> <span class=\"label label-info\">" + (i + 1) + "</span>  " + len170 +
                                "<div class=\"collapse\"><p class=\"breakWord\">" + rest + "</p></div><a class=\"a\" href=\"#\"> &raquo;</a></div>";
                        continue;
                    }
                    x[i] = "<span class=\"label label-info\">" + (i + 1) + "</span>   " + x[i] + "</br>";
                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 1000)
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
                html = sb.toString();


                break;
            }
            case "sqlite": {
                html = FileUtil.readFromFile(mPrefs, SQLITE).replace(SQLiteHook.TAG, "");

                String[] x = html.split("</br>");
                for (int i = 0; i < x.length; i++) {

                    String color;
                    if (x[i].contains("INSERT INTO")) {
                        color = "label-info";
                    } else if (x[i].contains("UPDATE")) {
                        color = "label-warning";
                    } else if (x[i].contains("execSQL(")) {
                        color = "label-danger";
                    }else if (x[i].contains("SELECT")) {
                        color = "label-success";
                        x[i].replace("\n", "</br>");
                    }else{
                        color = "label-default";
                    }

                    if (x[i].length() > 170) {
                        x[i] = "<div class=\"breakWord\"><span class=\"label "+color+"\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i])+"</div>";
                    }else{
                        x[i] = "<span class=\"label "+color+"\">" + (i + 1) + "</span>   " + Html.escapeHtml(x[i])+"</br>";
                    }
                }

                List<String> ls = Arrays.asList(x);

                Collections.reverse(ls);
                x = (String[]) ls.toArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < x.length; i++) {
                    if (i < 1000)
                        sb.append(x[i]);
                }

                html = sb.toString().replace("</br></br>", "</br>");

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
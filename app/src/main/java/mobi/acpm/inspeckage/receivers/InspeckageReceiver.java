package mobi.acpm.inspeckage.receivers;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebView;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedBridge;
import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Util;

/**
 * Created by acpm on 17/01/16.
 */
public class InspeckageReceiver extends BroadcastReceiver {


    private Activity activity = null;

    public InspeckageReceiver(Object obj) {
        if (obj instanceof Activity) {
            this.activity = (Activity) obj;
        } else if (obj instanceof Fragment) {
            activity = ((Fragment) obj).getActivity();
        }else{
            XposedBridge.log(Module.ERROR + " >>>> Receiver");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (activity != null) {
            String pkg = intent.getExtras().getString("package");

            if (pkg.equals(activity.getPackageName())) {


                //PackageDetail pd = new PackageDetail(activity.getApplicationContext(),pkg);
                //pd.extractInfoToFile();

                String action = intent.getExtras().getString("action");

                if (action.equals("finish")) {

                    activity.finish();

                } else if (action.equals("query")) {

                    try {
                        Uri uri = Uri.parse(intent.getExtras().getString("uri"));
                        activity.managedQuery(uri, null, null, null, null);
                    } catch (Exception e) {
                        XposedBridge.log("InspeckageReceiver - query - " + e.getMessage());
                    }

                } else if (action.equals("startAct")) {


                    String act = intent.getExtras().getString("activity");
                    String flags = intent.getExtras().getString("flags");
                    String intent_action = intent.getExtras().getString("intent_action");
                    String uri = intent.getExtras().getString("data_uri");
                    String category = intent.getExtras().getString("category");
                    String mimetype = intent.getExtras().getString("mimetype");
                    String extras = intent.getExtras().getString("extra");

                    Intent i = new Intent();

                    //ACTIVITY
                    i.setClassName(activity.getApplicationContext(), act);

                    //ACTION
                    if (!intent_action.trim().equals("")) {
                        i.setAction(intent_action);
                    }

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
                    if(!uri.trim().equals("")){
                        Uri u = Uri.parse(uri);
                        i.setData(u);
                    }

                    if(!category.trim().equals("")){
                        i.addCategory(category);
                    }

                    if(!mimetype.trim().equals("")){
                        i.normalizeMimeType(mimetype);
                    }

                    if(!extras.trim().equals("")){

                        String[] extra = new String[]{extras};
                        if(extras.contains(";")){
                            extra = extras.split(";");
                        }

                        for(String e : extra){
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

                    activity.startActivity(i);

                } else if (action.equals("fileTree")) {

                    String tree = Util.FileTree(activity.getApplicationInfo().dataDir, "");

                    Intent i = new Intent("mobi.acpm.inspeckage.INSPECKAGE_WEB");
                    i.putExtra("action", "fileTree");
                    float m = (float) tree.length() / 3;
                    String sub1 = tree.substring(0, (int) m);
                    String sub2 = tree.substring((int) m, tree.length());
                    //talvez tenha que dividir pq a arvore pode ficar muito grande para ser enviada via intent
                    i.putExtra("tree", tree);
                    activity.sendBroadcast(i, null);
                    Util.sb = new StringBuilder();

                } else if (action.equals("checkApp")) {

                    Intent i = new Intent("mobi.acpm.inspeckage.INSPECKAGE_WEB");
                    i.putExtra("action", "checkApp");
                    i.putExtra("isRunning", true);

                    int pid = android.os.Process.myPid();
                    i.putExtra("PID",pid);

                    activity.sendBroadcast(i, null);

                } else if (action.equals("webviewSetDebug")) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        WebView.setWebContentsDebuggingEnabled(true);
                    }

                } else if (action.equals("clipboard")) {
                    String value = intent.getExtras().getString("value");

                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData
                            .newPlainText("simple text", value);
                    clipboard.setPrimaryClip(clip);
                }
            }
        }
    }
}

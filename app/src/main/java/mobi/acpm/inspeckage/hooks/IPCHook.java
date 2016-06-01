package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 24/11/15.
 */
public class IPCHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_IPC:";

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        findAndHookMethod(ContextWrapper.class, "startActivities",
                Intent[].class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent[] it = (Intent[]) param.args[0];
                        StringBuffer sb = new StringBuffer();
                        for(Intent i : it){
                            sb.append(i+",");
                        }
                        XposedBridge.log(TAG + "startActivities: "+sb.toString().substring(0,sb.length()-1));
                    }
                });

        findAndHookMethod(ContextWrapper.class, "startService",
                Intent.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[0];
                        XposedBridge.log(TAG + "startService: "+intent);
                    }
                });

        findAndHookMethod(ContextWrapper.class, "startActivity",
                Intent.class, Bundle.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[0];
                        XposedBridge.log(TAG + "startActivity: "+intent);
                    }
                });

        //findAndHookMethod(ContextWrapper.class, "startActivity",
        findAndHookMethod(Activity.class, "startActivity",
                Intent.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[0];
                        XposedBridge.log(TAG + "startActivity: "+intent);
                    }
                });

        findAndHookMethod(ContextWrapper.class, "sendBroadcast",
                Intent.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[0];
                        if(intent !=null && !intent.getAction().contains("mobi.acpm.inspeckage")) {
                            XposedBridge.log(TAG + "sendBroadcast: " + intent);
                        }
                    }
                });

        findAndHookMethod(ContextWrapper.class, "sendBroadcast",
                Intent.class, String.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[0];
                        if(intent !=null && !intent.getAction().contains("mobi.acpm.inspeckage")) {
                            XposedBridge.log(TAG + "sendBroadcast: " + intent);
                        }
                    }
                });

        findAndHookMethod(ContextWrapper.class, "registerReceiver",
                BroadcastReceiver.class, IntentFilter.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        IntentFilter intentFilter = (IntentFilter) param.args[1];
                        StringBuffer sb = new StringBuffer();
                        sb.append("Actions: ");
                        for(int i=0; i<intentFilter.countActions(); i++){
                            sb.append(intentFilter.getAction(i)+",");
                        }
                        if(!sb.toString().contains("mobi.acpm.inspeckage")) {
                            XposedBridge.log(TAG + "registerReceiver: " + sb.toString().substring(0, sb.length() - 1));
                        }
                    }
                });

        findAndHookMethod(ContextWrapper.class, "registerReceiver",
                BroadcastReceiver.class, IntentFilter.class, String.class, Handler.class, new XC_MethodHook() {

                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        IntentFilter intentFilter = (IntentFilter) param.args[1];
                        StringBuffer sb = new StringBuffer();
                        sb.append("Actions: ");
                        for(int i=0; i<intentFilter.countActions(); i++){
                            sb.append(intentFilter.getAction(i)+",");
                        }

                        if(param.args[2] != null){
                        sb.append(" Permissions: "+param.args[2]);
                        }

                        if(!sb.toString().contains("mobi.acpm.inspeckage")) {
                            XposedBridge.log(TAG + "registerReceiver: " + sb.toString().substring(0, sb.length() - 1));
                        }
                    }
                });
    }
}

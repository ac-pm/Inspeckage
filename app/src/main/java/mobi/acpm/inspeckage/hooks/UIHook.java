package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.receivers.InspeckageReceiver;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 17/07/16.
 */
public class UIHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_GUI:";

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

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

        //findAndHookMethod(Activity.class, "finish", new XC_MethodHook() {

            //protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                //android.os.Process.killProcess(android.os.Process.myPid());

            //}
        //});
    }
}

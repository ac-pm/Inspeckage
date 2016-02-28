package mobi.acpm.inspeckage.hooks;

import android.content.ClipData;
import android.content.ClipboardManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 25/11/15.
 */
public class ClipboardHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Clipboard:";

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        findAndHookMethod(ClipboardManager.class, "setPrimaryClip", ClipData.class, new XC_MethodHook() {

            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                ClipData cd = (ClipData) param.args[0];
                StringBuilder sb = new StringBuilder();
                if (cd != null && cd.getItemCount() > 0) {

                    for (int i = 0; i < cd.getItemCount(); i++) {
                        ClipData.Item item = cd.getItemAt(i);
                        sb.append(item.getText());
                    }
                }
                XposedBridge.log(MiscHook.TAG + "Copied to the clipboard: " + sb.toString() + "");

            }
        });
    }
}

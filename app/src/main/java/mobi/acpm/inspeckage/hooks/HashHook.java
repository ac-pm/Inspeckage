package mobi.acpm.inspeckage.hooks;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.util.Util;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 21/11/15.
 */
public class HashHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Hash:";
    private static StringBuffer sb;

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        findAndHookMethod(MessageDigest.class, "getInstance", String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                sb = new StringBuffer();
                sb.append("Algorithm(" +param.args[0]+") [");
            }
        });

        findAndHookMethod(MessageDigest.class, "update", byte[].class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                sb.append("" + Util.byteArrayToString((byte[]) param.args[0])+" : ");
            }

        });

        findAndHookMethod(MessageDigest.class, "update", byte[].class, "int", "int", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                sb.append("" + Util.byteArrayToString((byte[]) param.args[0])+" : ");
            }

        });

        findAndHookMethod(MessageDigest.class, "update", ByteBuffer.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                ByteBuffer bb = (ByteBuffer) param.args[0];

                sb.append("" + Util.byteArrayToString(bb.array()) + " : ");
            }
        });

        //the computed one way hash value
        findAndHookMethod(MessageDigest.class, "digest", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                sb.append(Util.toHexString((byte[]) param.getResult())+"]");

                XposedBridge.log(TAG + sb.toString());
                sb = new StringBuffer();
            }
        });

        findAndHookMethod(MessageDigest.class, "digest", byte[].class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                //XposedBridge.log(TAG + "digest2 = " + Util.byteArrayToString((byte[]) param.args[0]));

                //sb.append(" : " + Util.toHexString((byte[]) param.getResult())+"]");

                //XposedBridge.log(TAG + sb.toString());
                //sb = new StringBuffer();
            }
        });

        findAndHookMethod(MessageDigest.class, "digest", byte[].class, "int", "int", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                //sb.append(" : " + (Integer) param.getResult()+"]");
                //XposedBridge.log(TAG + sb.toString());
                //sb = new StringBuffer();
            }
        });
    }
}

package mobi.acpm.inspeckage.hooks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import mobi.acpm.inspeckage.util.Util;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 17/01/16.
 */
public class SerializationHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_Serialization:";

    static String f = "";
    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {


        findAndHookConstructor(FileInputStream.class, File.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                File file = (File) param.args[0];
                if (file != null) {
                    ///
                    if (!file.getPath().contains("inspeckage") && (file.getPath().contains("data/data/")
                            || file.getPath().contains("storage/emulated/") || file.getPath().contains("data/media/"))) {

                        f = file.getPath();
                    }
                }

            }
        });

        findAndHookMethod(ObjectInputStream.class, "readObject", new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Object paramObject = param.getResult();
                StringBuilder sb = new StringBuilder();

                if (paramObject != null) {

                    String name = paramObject.getClass().getCanonicalName();
                    if(name != null) {
                        if (name.length() > 5 && name.substring(0, 5).contains("java.") || name.substring(0, 5).contains("byte")) {
                            //do nothing
                        } else {

                            sb.append("Read Object[" + name + "] HEX = ");

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();

                            try {
                                ObjectOutput out = new ObjectOutputStream(bos);
                                out.writeObject(paramObject);
                                byte[] yourBytes = bos.toByteArray();
                                String hex = Util.toHexString(yourBytes);

                                sb.append(hex);
                                XposedBridge.log(TAG + "Possible Path [" + f + "] " + sb.toString());
                            } catch (NullPointerException e) {
                                //
                            } catch (IOException i) {
                                i.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }
}

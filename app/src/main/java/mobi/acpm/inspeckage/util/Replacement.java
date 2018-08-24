package mobi.acpm.inspeckage.util;

import com.google.gson.Gson;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;

/**
 * Created by acpm on 30/10/16.
 */

public class Replacement {

    private static Gson gson = new Gson();
    public static final String TAG = "Inspeckage_Replacement:";

    public static boolean parameterReplace(XC_MethodHook.MethodHookParam param, XSharedPreferences sPrefs) {

        String user_json = sPrefs.getString(Config.SP_USER_REPLACES, "");
        if (!user_json.trim().equals("")) {
            String json = "{\"replaceParamItems\": " + sPrefs.getString(Config.SP_USER_REPLACES, "") + "}";
            ReplaceParamList replaceList = gson.fromJson(json, ReplaceParamList.class);

            for (ReplaceParamItem item : replaceList.replaceParamItems) {

                if (item.position > 0 && item.state) {

                    if (item.classMethod.equalsIgnoreCase(param.method.getDeclaringClass().getName() + "." + param.method.getName())) {

                        int p = item.position - 1;
                        if (param.args[p] != null) {
                            if (item.paramType.equals("boolean")) {
                                if (param.args[p] instanceof Boolean) {
                                    param.args[p] = Boolean.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("String") && param.args[p] instanceof String) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (item.paramMatch.equals(param.args[p])) {
                                        param.args[p] = item.paramNewValue;
                                    }else if(((String)param.args[p]).contains((String)item.paramMatch)){
                                        ((String)param.args[p]).replace((String)item.paramMatch,(String)item.paramNewValue);
                                    }
                                } else {
                                    param.args[p] = item.paramNewValue;
                                }

                            } else if (item.paramType.equals("int") && param.args[p] instanceof Integer) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Integer.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("ByteArray") && param.args[p].getClass().equals(byte[].class)) {
                                String v = Util.byteArrayToString((byte[]) param.args[p]);

                                /**
                                String originalValue = v;

                                if(v.contains("{\"body")) {
                                    XposedBridge.log(UserHooks.TAG+" PROJETOX REQUEST" + originalValue);
                                }else if(v.contains("{")){
                                    XposedBridge.log(UserHooks.TAG+" PROJETOX RESPONSE" + originalValue);
                                }
                                **/
                                if (item.paramMatch != null && !item.paramMatch.toString().trim().equals("")) {

                                    if(v.contains(item.paramMatch.toString())){
                                        //XposedBridge.log(UserHooks.TAG+" PROJETOX - OLD - "+originalValue);
                                        String newValue = v.replace(item.paramMatch.toString(),item.paramNewValue.toString());
                                        param.args[p] = newValue.getBytes();
                                        //XposedBridge.log(UserHooks.TAG+" PROJETOX - NEW - "+newValue);
                                    }
                                } else {
                                    param.args[p] = item.paramNewValue.toString().getBytes();
                                }
                            } else if (item.paramType.equals("short") && param.args[p] instanceof Short) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Short.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("long") && param.args[p] instanceof Long) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Long.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("float") && param.args[p] instanceof Float) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Float.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("double") && param.args[p] instanceof Double) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Double.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("charArray") && param.args[p] instanceof char[]) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (item.paramMatch.toString().toCharArray().equals(param.args[p])) {
                                        param.args[p] = item.paramNewValue.toString().toCharArray();
                                    }
                                } else {
                                    param.args[p] = item.paramNewValue.toString().toCharArray();
                                }
                            }

                        }
                        //construtor
                    } else if (item.classMethod.equalsIgnoreCase(param.method.getDeclaringClass().getName() + "." + param.method.getDeclaringClass().getSimpleName())) {
                        int p = item.position - 1;
                        if (param.args[p] != null) {
                            if (item.paramType.equals("boolean")) {
                                if (param.args[p] instanceof Boolean) {
                                    param.args[p] = Boolean.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("String") && param.args[p] instanceof String) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (item.paramMatch.equals(param.args[p])) {
                                        param.args[p] = item.paramNewValue;
                                    }

                                } else {
                                    param.args[p] = item.paramNewValue;
                                }

                            } else if (item.paramType.equals("int") && param.args[p] instanceof Integer) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Integer.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Integer.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("ByteArray") && param.args[p].getClass().equals(byte[].class)) {
                                String v = Util.byteArrayToString((byte[]) param.args[p]);

                                if (item.paramMatch != null && !item.paramMatch.toString().trim().equals("")) {
                                    if (v.equals(item.paramMatch.toString())) {
                                        param.args[p] = v.getBytes();
                                    }
                                } else {
                                    param.args[p] = item.paramNewValue.toString().getBytes();
                                }
                            } else if (item.paramType.equals("short") && param.args[p] instanceof Short) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Short.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Short.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("long") && param.args[p] instanceof Long) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Long.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Long.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("float") && param.args[p] instanceof Float) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Float.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Float.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("double") && param.args[p] instanceof Double) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (Double.valueOf(item.paramMatch.toString()).equals(param.args[p])) {
                                        param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                    }
                                } else {
                                    param.args[p] = Double.valueOf(item.paramNewValue.toString());
                                }
                            } else if (item.paramType.equals("charArray") && param.args[p] instanceof char[]) {
                                if (item.paramMatch != null && item.paramMatch.toString().trim() != "") {
                                    if (item.paramMatch.toString().toCharArray().equals(param.args[p])) {
                                        param.args[p] = item.paramNewValue.toString().toCharArray();
                                    }
                                } else {
                                    param.args[p] = item.paramNewValue.toString().toCharArray();
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean resultReplace(XC_MethodHook.MethodHookParam param, XSharedPreferences sPrefs) {

        String user_json = sPrefs.getString(Config.SP_USER_RETURN_REPLACES, "");
        if (!user_json.trim().equals("")) {

            String json = "{\"replaceReturnItems\": " + sPrefs.getString(Config.SP_USER_RETURN_REPLACES, "") + "}";
            ReplaceReturnList replaceList = gson.fromJson(json, ReplaceReturnList.class);

            for (ReplaceReturnItem item : replaceList.replaceReturnItems) {

                if (item.state && item.classMethod.equalsIgnoreCase(param.method.getDeclaringClass().getName() + "." + param.method.getName())) {

                    if (item.returnNewValue != null && !item.returnNewValue.equals("void")) {
                        if (item.returnType.equals("boolean") && param.getResult() instanceof Boolean) {
                            param.setResult(Boolean.valueOf(item.returnNewValue.toString()));
                        } else if (item.returnType.equals("String") && param.getResult() instanceof String) {

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                if (item.returnMatch.equals(param.getResult())) {
                                    param.setResult(item.returnNewValue);
                                }
                            } else {
                                param.setResult(item.returnNewValue);
                            }

                        } else if (item.returnType.equals("int") && param.getResult() instanceof Integer) {

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                if (Integer.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                                    param.setResult(Integer.valueOf(item.returnNewValue.toString()));
                                }
                            } else {
                                param.setResult(Integer.valueOf(item.returnNewValue.toString()));
                            }

                        } else if (item.returnType.equals("ByteArray") && param.getResult().getClass().equals(byte[].class)) {
                            String v = Util.byteArrayToString((byte[]) param.getResult());

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                param.setResult(v.getBytes());
                            } else {
                                param.setResult(item.returnNewValue.toString().getBytes());
                            }
                        } else if (item.returnType.equals("short") && param.getResult() instanceof Short) {

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                if (Short.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                                    param.setResult(Short.valueOf(item.returnNewValue.toString()));
                                }
                            } else {
                                param.setResult(Short.valueOf(item.returnNewValue.toString()));
                            }
                        } else if (item.returnType.equals("long") && param.getResult() instanceof Long) {

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                if (Long.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                                    param.setResult(Long.valueOf(item.returnNewValue.toString()));
                                }
                            } else {
                                param.setResult(Long.valueOf(item.returnNewValue.toString()));
                            }
                        } else if (item.returnType.equals("float") && param.getResult() instanceof Float) {

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                if (Float.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                                    param.setResult(Float.valueOf(item.returnNewValue.toString()));
                                }
                            } else {
                                param.setResult(Float.valueOf(item.returnNewValue.toString()));
                            }
                        } else if (item.returnType.equals("double") && param.getResult() instanceof Double) {

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                if (Double.valueOf(item.returnNewValue.toString()).equals(param.getResult())) {
                                    param.setResult(Double.valueOf(item.returnNewValue.toString()));
                                }
                            } else {
                                param.setResult(Double.valueOf(item.returnNewValue.toString()));
                            }
                        } else if (item.returnType.equals("charArray") && param.getResult() instanceof char[]) {

                            if (item.returnMatch != null && item.returnMatch.toString().trim() != "") {
                                if (item.returnNewValue.toString().toCharArray().equals(param.getResult())) {
                                    param.setResult(item.returnNewValue.toString().toCharArray());
                                }
                            } else {
                                param.setResult(item.returnNewValue.toString().toCharArray());
                            }

                        }
                    }
                }
            }
        }
        return true;
    }
}

class ReplaceParamItem{
    protected int id;
    protected String classMethod;
    protected int position;
    protected String paramType;
    protected Object paramMatch;
    protected Object paramNewValue;
    protected boolean state;
}
class ReplaceParamList{
    protected List<ReplaceParamItem> replaceParamItems;
}

class ReplaceReturnItem{
    protected int id;
    protected String classMethod;
    protected Object returnType;
    protected Object returnNewValue;
    protected Object returnMatch;
    protected boolean state;
}

class ReplaceReturnList{
    protected List<ReplaceReturnItem> replaceReturnItems;
}
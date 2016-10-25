package mobi.acpm.inspeckage.util;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.text.TextUtils.isDigitsOnly;

/**
 * Created by acpm on 19/09/16.
 */
public class DexUtil {


    public static Map<String, ArrayList<String>> getClassesWithMethods(XC_LoadPackage.LoadPackageParam loadPackageParam, String packageName) throws Throwable {

        Map<String, ArrayList<String>> classes = new HashMap<String, ArrayList<String>>();

        if (!packageName.trim().equals("") && loadPackageParam.appInfo.sourceDir.contains(packageName)) {

            DexFile dexFile = new DexFile(loadPackageParam.appInfo.sourceDir);

            Enumeration<String> classNames = dexFile.entries();

            while (classNames.hasMoreElements()) {
                final String className = classNames.nextElement();

                //if (!packageName.trim().equals("") && className.contains(packageName)) {

                boolean subMethod = false;
                if (className.contains("$")) {
                    String v = className.split("\\$")[1];
                    if (isDigitsOnly(v)) {
                        subMethod = true;
                    }
                }

                if (!subMethod && !className.contains(".R$")) {

                    try {
                        final Class cls = Class.forName(className, false, loadPackageParam.classLoader);

                        if (cls != null && cls.getDeclaredMethods().length > 0) {
                            ArrayList<String> methods = new ArrayList<>();
                            for (final Method method : cls.getDeclaredMethods()) {
                                if (!Modifier.isAbstract(method.getModifiers()) && !methods.contains(method.getName())) {
                                    methods.add(method.getName());
                                }
                            }
                            classes.put(className, methods);
                        }
                    } catch (NoClassDefFoundError ex) {
                        Log.e("Error", ex.getMessage());
                    } catch (ClassNotFoundException ex) {
                        Log.e("Error", ex.getMessage());
                    }

                }
                //}
            }
        }
        return classes;
    }

    public static void saveClassesWithMethodsJson(XC_LoadPackage.LoadPackageParam loadPackageParam, SharedPreferences prefs) throws Throwable {

        String packageName = prefs.getString("package", "");
        Map<String, ArrayList<String>> classes = DexUtil.getClassesWithMethods(loadPackageParam, packageName);

        //RAIZ
        ClassMethod root = new ClassMethod();
        root.setID("p_" + packageName);
        root.setName(packageName);

        int c_id = 0;
        for (String classNameComplete : classes.keySet()) {

            if (classNameComplete.contains(packageName)) {
                c_id++;
                String pack_name = classNameComplete.substring(0, classNameComplete.lastIndexOf("."));
                String class_name = classNameComplete.substring(classNameComplete.lastIndexOf(".") + 1);

                //pacote
                ClassMethod package_class = new ClassMethod();
                package_class.setID(pack_name);
                package_class.setName(pack_name);

                if (!root.contains(package_class)) {
                    root.getClassMethods().add(package_class);
                }

                //classe
                ClassMethod class_leaf = new ClassMethod();
                class_leaf.setID(classNameComplete);
                class_leaf.setName(class_name);

                //adiciona metodos nas folhas(ultimas classes)
                ArrayList<String> methods = classes.get(classNameComplete);
                int m_id = 0;
                for (String method : methods) {
                    m_id++;
                    ClassMethod m = new ClassMethod();
                    m.setID("m_" + c_id+"_"+m_id);//
                    m.setName(method);
                    m.setIcon("jstree-file");
                    if (!class_leaf.contains(m)) {
                        class_leaf.getClassMethods().add(m);
                    }
                }

                package_class.getClassMethods().add(class_leaf);
                root.update(package_class);

            } else {

                /**String name = classNameComplete.substring(0,classNameComplete.lastIndexOf("."));

                 ClassMethod cx = new ClassMethod();
                 cx.setID(name);
                 cx.setName(name);

                 if(!array.contains(cx)){
                 //c.getClassMethods().add(cx);
                 }**/
            }
        }

        Gson gson = new GsonBuilder().create();
        JsonElement jsonElement = gson.toJsonTree(root);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        FileUtil.writeToFile(prefs, jsonObject.toString(), FileType.APP_STRUCT, "");
    }


    public static class ClassMethod {
        private String id;
        private String text;
        private String icon;

        private List<ClassMethod> children = new ArrayList<ClassMethod>();

        public String getID() {
            return id;
        }

        public void setID(String id) {
            this.id = id;
        }

        public String getName() {
            return text;
        }

        public void setName(String name) {
            this.text = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }


        public List<ClassMethod> getClassMethods() {
            return children;
        }

        public void setClassMethods(List<ClassMethod> children) {
            this.children = children;
        }

        public boolean contains(ClassMethod cm) {
            boolean x = false;

            for (ClassMethod c : getClassMethods()) {
                if (c.getID().equals(cm.getID())) {
                    x = true;
                }
            }

            return x;
        }

        //se ja existir a classe entao pega os metodos da nova e atualiza a que ja existe
        public boolean update(ClassMethod cm) {
            boolean x = false;

            for (ClassMethod c : getClassMethods()) {
                if (c.getID().equals(cm.getID())) {
                    for (ClassMethod cm2 : cm.getClassMethods()) {
                        if (!c.contains(cm2)) {
                            c.getClassMethods().add(cm2);
                        }
                    }
                    x = true;
                }
            }


            return x;
        }
    }
}

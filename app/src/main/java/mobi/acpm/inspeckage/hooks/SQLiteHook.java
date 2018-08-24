package mobi.acpm.inspeckage.hooks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Base64;

import java.io.File;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by acpm on 21/11/15.
 */
public class SQLiteHook extends XC_MethodHook {

    public static final String TAG = "Inspeckage_SQLite:";

    public static void initAllHooks(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        findAndHookMethod(SQLiteDatabase.class, "execSQL", String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                XposedBridge.log(TAG + "execSQL(" + param.args[0] + ")");

            }

        });

        findAndHookMethod(SQLiteDatabase.class, "execSQL", String.class, Object[].class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Object[] obj = (Object[]) param.args[1];
                int obj_c = 0;
                if (obj != null && obj.length > 0) {
                    obj_c = obj.length;
                }

                XposedBridge.log(TAG + "execSQL(" + param.args[0] + ") with " + String.valueOf(obj_c) + " args.");
            }

        });

        findAndHookMethod(SQLiteDatabase.class, "update", String.class, ContentValues.class,
                String.class, String[].class, new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        SQLiteDatabase sqlitedb = (SQLiteDatabase) param.thisObject;


                        ContentValues contentValues = (ContentValues) param.args[1];
                        StringBuffer sb = new StringBuffer();

                        Set<Map.Entry<String, Object>> s = contentValues.valueSet();
                        for (Map.Entry<String, Object> entry : s) {
                            sb.append(entry.getKey() + "=" + String.valueOf(entry.getValue()) + ",");
                        }

                        StringBuffer sbuff = new StringBuffer();
                        if (param.args[3] != null) {
                            for (String str : (String[]) param.args[3]) {
                                sbuff.append(str + ",");
                            }
                        }

                        String set = "";
                        if (sb.toString().length() > 1) {
                            set = sb.toString().substring(0, sb.length() - 1);
                        }

                        String whereArgs = "";
                        if (sbuff.toString().length() > 1) {
                            whereArgs = sbuff.toString().substring(0, sbuff.length() - 1);
                        }

                        XposedBridge.log(TAG + "\nUPDATE " + param.args[0] + " SET " + set + "" +
                                " WHERE " + param.args[2] + "" + whereArgs);
                    }
                });

        //1 INSERT INTO students VALUES(grade=Teste2,name=Teste1)

        findAndHookMethod(SQLiteDatabase.class, "insert", String.class, String.class, ContentValues.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                SQLiteDatabase sqlitedb = (SQLiteDatabase) param.thisObject;

                ContentValues contentValues = (ContentValues) param.args[2];
                StringBuffer sb = new StringBuffer();

                for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
                    sb.append(entry.getKey() + "=" + String.valueOf(entry.getValue()) + ",");
                }
                XposedBridge.log(TAG + "INSERT INTO " + param.args[0] + " VALUES(" + sb.toString().substring(0, sb.length() - 1) + ")");
            }
        });

        findAndHookMethod(Activity.class, "managedQuery", Uri.class, String[].class, String.class, String[].class, String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Uri uri = (Uri) param.args[0];

                StringBuffer projection = new StringBuffer();
                if (param.args[1] != null) {
                    for (String str : (String[]) param.args[1]) {
                        projection.append(str + ",");
                    }
                }

                String selection = "";
                if (param.args[2] != null) {
                    selection = " WHERE " + (String) param.args[2] + " = ";
                }

                StringBuffer selectionArgs = new StringBuffer();
                if (param.args[3] != null) {
                    for (String str : (String[]) param.args[3]) {
                        selectionArgs.append(str + ",");
                    }
                }

                String sortOrder = "";
                if (param.args[4] != null) {
                    sortOrder = " ORDER BY " + (String) param.args[4];
                }

                String projec = "";
                if (projection.toString().equals("")) {
                    projec = "*";//projection.append("*");
                } else {
                    projec = projection.toString().substring(0, projection.length() - 1);
                }

                Cursor cursor = (Cursor) param.getResult();

                StringBuffer result = new StringBuffer();

                if (cursor != null)
                    if (cursor.moveToFirst()) {
                        do {
                            int x = cursor.getColumnCount();
                            StringBuffer sb = new StringBuffer();
                            for (int i = 0; i < x; i++) {

                                if (cursor.getType(i) == Cursor.FIELD_TYPE_BLOB) {
                                    String blob = Base64.encodeToString(cursor.getBlob(i), Base64.NO_WRAP);
                                    sb.append(cursor.getColumnName(i) + "=" + blob + ",");
                                } else {
                                    sb.append(cursor.getColumnName(i) + "=" + cursor.getString(i) + ",");
                                }
                            }
                            result.append(sb.toString().substring(0, sb.length() - 1) + "\n");

                        } while (cursor.moveToNext());
                    }

                XposedBridge.log(TAG + "SELECT " + projec + " FROM " + uri.getAuthority() + uri.getPath() +
                        selection + selectionArgs.toString() + sortOrder + "\n   [" + result.toString() + "]");
            }
        });

        //query
        findAndHookMethod(SQLiteDatabase.class, "query", String.class, String[].class, String.class,
                String[].class, String.class, String.class, String.class, String.class, new XC_MethodHook() {

                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                        String table = (String) param.args[0];
                        String[] columns = (String[]) param.args[1];
                        String having = (String) param.args[5];
                        String limit = (String) param.args[6];

                        StringBuffer csb = new StringBuffer();
                        if (param.args[1] != null) {
                            for (String str : (String[]) param.args[1]) {
                                csb.append(str + ",");
                            }
                        }

                        String selection = "";
                        if (param.args[2] != null) {
                            selection = " WHERE " + (String) param.args[2] + " = ";
                        }

                        StringBuffer selectionArgs = new StringBuffer();
                        if (param.args[3] != null) {
                            for (String str : (String[]) param.args[3]) {
                                selectionArgs.append(str + ",");
                            }
                        }

                        String groupBy = "";
                        if (param.args[4] != null) {
                            groupBy = " GROUP BY " + (String) param.args[4];
                        }

                        String sortOrder = "";
                        if (param.args[6] != null) {
                            sortOrder = " ORDER BY " + (String) param.args[6];
                        }

                        if (csb.toString().equals("")) {
                            csb.append("*");
                        }

                        Cursor cursor = (Cursor) param.getResult();

                        StringBuffer result = new StringBuffer();

                        if (cursor != null)
                            if (cursor.moveToFirst()) {
                                do {
                                    int x = cursor.getColumnCount();
                                    StringBuffer sb = new StringBuffer();
                                    for (int i = 0; i < x; i++) {
                                        if(cursor.getType(i) == Cursor.FIELD_TYPE_BLOB){
                                            String blob = Base64.encodeToString(cursor.getBlob(i), Base64.NO_WRAP);
                                            sb.append(cursor.getColumnName(i) + "=" + blob + ",");
                                        }else {
                                            sb.append(cursor.getColumnName(i) + "=" + cursor.getString(i) + ",");
                                        }
                                    }
                                    result.append(sb.toString().substring(0, sb.length() - 1) + "\n");

                                } while (cursor.moveToNext());
                            }

                        XposedBridge.log(TAG + "SELECT " + csb.toString() + " FROM " + table +
                                selection + selectionArgs.toString() + sortOrder + "\n" + result.toString() + "");
                    }
                });


        findAndHookMethod(ContextWrapper.class, "getDatabasePath", String.class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                XposedBridge.log(TAG + "[Context] getDatabasePath(" + param.args[0] + ")");

            }

        });

        ///SQLCipher

        try {
            findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader,
                    "execSQL", String.class, new XC_MethodHook() {

                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            XposedBridge.log(TAG + "[SQLCipher] execSQL(" + param.args[0] + ")");

                        }

                    });

            findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader,
                    "execSQL", String.class, Object[].class, new XC_MethodHook() {

                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            Object[] obj = (Object[]) param.args[1];
                            int obj_c = 0;
                            if (obj != null && obj.length > 0) {
                                obj_c = obj.length;
                            }
                            XposedBridge.log(TAG + "[SQLCipher] execSQL(" + param.args[0] + ") with " + String.valueOf(obj_c) + " args.");
                        }

                    });

            findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader,
                    "openOrCreateDatabase", File.class, String.class, "net.sqlcipher.database.SQLiteDatabase.CursorFactory", new XC_MethodHook() {

                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            File f = (File) param.args[0];
                            String passwd = (String) param.args[1];
                            XposedBridge.log(TAG + "[SQLCipher] Open or Create:" + f.getName() + " with password: " + passwd);

                        }

                    });


            findAndHookMethod("net.sqlcipher.database.SQLiteDatabase", loadPackageParam.classLoader,
                    "rawQuery", String.class, String[].class, new XC_MethodHook() {

                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            String[] obj = (String[]) param.args[1];
                            int obj_c = 0;
                            if (obj != null && obj.length > 0) {
                                obj_c = obj.length;
                            }
                            XposedBridge.log(TAG + "[SQLCipher] rawQuery(" + param.args[0] + ") with " + String.valueOf(obj_c) + " args.");
                        }

                    });
        } catch (XposedHelpers.ClassNotFoundError e) {
        } catch (NoSuchMethodError e) {
        }
    }
}

package mobi.acpm.inspeckage.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mobi.acpm.inspeckage.Module;

/**
 * Created by acpm on 29/11/15.
 */
public class FileUtil {

    public static void fixSharedPreference(Context context) {

        File folder = new File(Config.P_INSPECKAGE_PATH);
        folder.setExecutable(true, false);

        String mPrefFile = Config.P_INSPECKAGE_PATH + Config.P_SHARED_PATH + Module.PREFS + ".xml";
        (new File(mPrefFile)).setReadable(true, false);
    }

    public static void writeToFile(SharedPreferences prefs, String data, FileType ft, String name) {

        try {

            String absolutePath;

            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION,false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath()+Config.P_ROOT+"/"+prefs.getString(Config.SP_PACKAGE,"");
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null)+Config.P_ROOT;
            }
            boolean append = true;
            if (ft != null) {
                switch (ft) {
                    case SERIALIZATION:
                        absolutePath += Config.P_SERIALIZATION; //1
                        data = data + "</br>";
                        break;
                    case CLIPB:
                        absolutePath += Config.P_CLIPB; //1
                        data = data + "</br>";
                        break;
                    case HASH:
                        absolutePath += Config.P_HASH; //3
                        data = data + "</br>";
                        break;
                    case CRYPTO:
                        absolutePath += Config.P_CRYPTO; //2
                        data = data + "</br>";
                        break;
                    case IPC:
                        absolutePath += Config.P_IPC; //4
                        data = data + "</br>";
                        break;
                    case PREFS:
                        absolutePath += Config.P_PREFS; //5
                        data = data + "</br>";
                        break;
                    case PREFS_BKP:
                        absolutePath += Config.PREFS_BKP + name;
                        File conf = new File(absolutePath);
                        if (conf.exists()) {
                            conf.delete();
                        }
                        break;
                    case LOG:
                        absolutePath += Config.P_LOG;
                        break;
                    case PACKAGE:
                        absolutePath += Config.P_PACKAGE_DETAIL;
                        break;
                    case SQLITE:
                        absolutePath += Config.P_SQLITE; //6
                        data = data + "</br>";
                        break;
                    case WEBVIEW:
                        absolutePath += Config.P_WEBVIEW; //8
                        data = data + "</br>";
                        break;
                    case FILESYSTEM:
                        absolutePath += Config.P_FILESYSTEM; //9
                        data = data + "</br>";
                        break;
                    case MISC:
                        absolutePath += Config.P_MISC; //10
                        data = data + "</br>";
                        break;
                    case HTTP:
                        absolutePath += Config.P_HTTP; //10
                        data = data + "</br>";
                        break;
                    case USERHOOKS:
                        absolutePath += Config.P_USERHOOKS;
                        data = data + "</br>";
                        break;
                    case APP_STRUCT:
                        absolutePath += Config.P_APP_STRUCT;
                        append = false;
                        break;
                    case REPLACEMENT:
                        absolutePath += Config.P_REPLACEMENT;
                        break;
                    default:
                }

                File file = new File(absolutePath);

                if (!file.exists()) {

                    File path = new File(String.valueOf(file.getParentFile()));
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);

                    path.mkdirs();
                    path.setReadable(true, false);
                    path.setExecutable(true, false);
                    path.setWritable(true, false);

                    file.createNewFile();

                    file.setReadable(true, false);
                    file.setExecutable(true, false);
                    file.setWritable(true, false);

                }

                FileOutputStream fOut = new FileOutputStream(file, append);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

                myOutWriter.write(data);
                myOutWriter.close();
                fOut.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(SharedPreferences prefs, FileType ft) {

        String text = "";
        try {

            String absolutePath;

            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath()+Config.P_ROOT+"/"+prefs.getString(Config.SP_PACKAGE,"");
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null)+Config.P_ROOT;
            }

            switch (ft) {
                case SERIALIZATION:
                    absolutePath += Config.P_SERIALIZATION; //1
                    break;
                case CLIPB:
                    absolutePath += Config.P_CLIPB; //1
                    break;
                case CRYPTO:
                    absolutePath += Config.P_CRYPTO; //2
                    break;
                case PREFS:
                    absolutePath += Config.P_PREFS; //5
                    break;
                case HASH:
                    absolutePath += Config.P_HASH; //3
                    break;
                case IPC:
                    absolutePath += Config.P_IPC; //4
                    break;
                case LOG:
                    absolutePath += Config.P_LOG;
                    break;
                case PACKAGE:
                    absolutePath += Config.P_PACKAGE_DETAIL;
                    break;
                case SQLITE:
                    absolutePath += Config.P_SQLITE; //6
                    break;
                case WEBVIEW:
                    absolutePath += Config.P_WEBVIEW; //8
                    break;
                case FILESYSTEM:
                    absolutePath += Config.P_FILESYSTEM; //9
                    break;
                case MISC:
                    absolutePath += Config.P_MISC; //10
                    break;
                case HTTP:
                    absolutePath += Config.P_HTTP; //10
                    break;
                case USERHOOKS:
                    absolutePath += Config.P_USERHOOKS; //10
                    break;
                case APP_STRUCT:
                    absolutePath += Config.P_APP_STRUCT; //10
                    break;
                default:
            }

            File file = new File(absolutePath);
            if (file.exists()) {

                //se o arquivo for muito grande, lê apenas o final do arquivo - 1mb
                if (file.length() > 1048576) {
                    RandomAccessFile aFile = new RandomAccessFile(absolutePath, "r");
                    FileChannel inChannel = aFile.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(1048576); //1MB
                    while (inChannel.read(buffer) > 0) {
                        buffer.flip();

                        String charsetName = "UTF-8";
                        CharBuffer cb = Charset.forName(charsetName).decode(buffer);
                        text = cb.toString();

                        buffer.clear();
                    }
                    inChannel.close();
                    aFile.close();

                } else {
                    FileInputStream f = new FileInputStream(absolutePath);
                    FileChannel ch = f.getChannel();
                    MappedByteBuffer mbb = ch.map(FileChannel.MapMode.READ_ONLY, 0L, ch.size());

                    while (mbb.hasRemaining()) {
                        String charsetName = "UTF-8";
                        CharBuffer cb = Charset.forName(charsetName).decode(mbb);
                        text = cb.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    public static Map<String, String> readMultiFile(SharedPreferences prefs, String folderName) {

        Map<String, String> files = new HashMap<>();
        try {

            String absolutePath;

            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath()+Config.P_ROOT+"/"+prefs.getString(Config.SP_PACKAGE,"")+"/"+ folderName;
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null)+Config.P_ROOT+"/"+folderName;
            }

            File folder = new File(absolutePath);
            if (folder.listFiles() != null && folder.length() > 0) {
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.exists() && fileEntry.isFile()) {

                        FileInputStream f = new FileInputStream(fileEntry.getAbsolutePath());
                        FileChannel ch = f.getChannel();
                        MappedByteBuffer mbb = ch.map(FileChannel.MapMode.READ_ONLY, 0L, ch.size());

                        String text = "";
                        while (mbb.hasRemaining()) {
                            String charsetName = "UTF-8";
                            CharBuffer cb = Charset.forName(charsetName).decode(mbb);
                            text = cb.toString();
                        }
                        files.put(fileEntry.getName(), text);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public static String readHtmlFile(Context context, String fileName) {

        String htmlFile = "";
        try {

            StringBuilder buf = new StringBuilder();
            InputStream html = context.getAssets().open("HTMLFiles" + fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(html, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();

            htmlFile = buf.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlFile;
    }

    public static void zipFolder(String inputFolderPath, String outZipPath) {
        try {

            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outZipPath));
            File srcFile = new File(inputFolderPath);
            addDir(srcFile, zos);
            zos.close();
        } catch (IOException ioe) {
            Log.e("ZIPFILE", ioe.getMessage());
        }
    }

    static void addDir(File srcFile, ZipOutputStream zos) throws IOException {

        File[] files = srcFile.listFiles();
        if(files != null) {
            byte[] buffer = new byte[1024];
            for (File file : files) {

                if (file.isDirectory()) {
                    addDir(file, zos);
                    continue;
                }
                FileInputStream fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(file.getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public static void deleteFile(File file) {
        file.delete();
    }

    public static void writeJsonFile(SharedPreferences prefs, String data, String name) {

        try {

            String absolutePath;

            if (prefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
                absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT + "/" + prefs.getString(Config.SP_PACKAGE, "");
            } else {
                absolutePath = prefs.getString(Config.SP_DATA_DIR, null) + Config.P_ROOT;
            }

            absolutePath += "/"+name;
            File file = new File(absolutePath);

            if (!file.exists()) {

                File path = new File(String.valueOf(file.getParentFile()));
                path.setReadable(true, false);
                path.setExecutable(true, false);
                path.setWritable(true, false);

                path.mkdirs();
                path.setReadable(true, false);
                path.setExecutable(true, false);
                path.setWritable(true, false);

                file.createNewFile();

                file.setReadable(true, false);
                file.setExecutable(true, false);
                file.setWritable(true, false);

            }

            FileOutputStream fOut = new FileOutputStream(file, false);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            myOutWriter.write(data);
            myOutWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package mobi.acpm.inspeckage.util;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.ui.MainActivity;


public class Util {

    /**
     * Lower case Hex Digits.
     */
    private static final String HEX_DIGITS = "0123456789abcdef";

    /**
     * Byte mask.
     */
    private static final int BYTE_MSK = 0xFF;

    /**
     * Hex digit mask.
     */
    private static final int HEX_DIGIT_MASK = 0xF;

    /**
     * Number of bits per Hex digit (4).
     */
    private static final int HEX_DIGIT_BITS = 4;

    public static boolean isInt(String s) {
        try {
            int i = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException er) {
            return false;
        }
    }

    public static String byteArrayToString(byte[] input) {
        if(input==null)
            return "";
        String out = new String(input);
        int tmp = 0;
        for (int i = 0; i < out.length(); i++) {
            int c = out.charAt(i);

            if (c >= 32 && c < 127) {
                tmp++;
            }
        }

        if (tmp > (out.length() * 0.60)) {
            StringBuilder sb = new StringBuilder();
            for (byte b : input) {
                if (b >= 32 && b < 127)
                    sb.append(String.format("%c", b));
                else
                    sb.append('.');
            }
            out = sb.toString();

        } else {
            out = Base64.encodeToString(input, Base64.NO_WRAP);
        }

        return out;
    }

    public static String toHexString(final byte[] byteArray) {
        StringBuilder sb = new StringBuilder(byteArray.length * 2);
        for (int i = 0; i < byteArray.length; i++) {
            int b = byteArray[i] & BYTE_MSK;
            sb.append(HEX_DIGITS.charAt(b >>> HEX_DIGIT_BITS)).append(
                    HEX_DIGITS.charAt(b & HEX_DIGIT_MASK));
        }
        return sb.toString();
    }

    public static byte[] getBytes(InputStream is) throws IOException {

        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        return buf;
    }

    public static void showNotification(Context mContext, String info) {

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.inspectorw)
                        .setContentTitle("Inspeckage")
                        .setContentText(info);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public static void takeScreenshot(String fileName) {

        Process sh;
        try {
            sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();

            if(new File(path + Config.P_ROOT ).exists() && new File("/storage/emulated/legacy").exists()){
                path = "/storage/emulated/legacy";
            }
            os.write(("/system/bin/screencap -p " + path + Config.P_ROOT + "/" + fileName).getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setARPEntry(String ip, String mac) {

        Process sh;
        try {
            sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();

            os.write(("su -c arp -s " + ip + " " + mac + "").getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileRoot(String path, String dest) {

        Process sh;
        try {
            sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();

            os.write(("su -c cat " + path + " > " + dest + "").getBytes("UTF-8"));
            os.flush();
            os.close();
            sh.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static StringBuilder sb = new StringBuilder();

    public static String FileTree(String path, String ul) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return "";

        for (File f : list) {
            if (f.isDirectory()) {

                if (f.getAbsoluteFile().getName().equals("Inspeckage")) {
                    continue;
                }
                //DIR
                sb.append("<li> <span class=\"glyphicon glyphicon-folder-open\" aria-hidden=\"true\"> " + f.getAbsoluteFile().getName() + "</span>");
                sb.append("<ul>");

                FileTree(f.getAbsolutePath(), "</ul></li>");
            } else {
                //FILE
                long fileSizeInBytes = f.length();
                long fileSizeInKB = 0;
                long fileSizeInMB = 0;
                String lengh = String.valueOf(fileSizeInBytes) + " B";
                if (fileSizeInBytes >= 1024) {
                    fileSizeInKB = fileSizeInBytes / 1024;
                    lengh = String.valueOf(fileSizeInKB) + " KB";
                }
                if (fileSizeInKB >= 1024) {
                    fileSizeInMB = fileSizeInBytes / 1024;
                    DecimalFormat df = new DecimalFormat("#,##0.###", new DecimalFormatSymbols(new Locale("pt", "BR")));
                    String mb = df.format(fileSizeInMB);
                    lengh = mb + " MB";
                }
                sb.append("<li><span class=\"glyphicon glyphicon-file\" aria-hidden=\"true\">" +
                        "</span> <button type=\"button\" class=\"btn btn-link\" onclick=\"download_file('" + f.getAbsoluteFile() + "');\" >"
                        + f.getAbsoluteFile().getName() + "    - " + lengh + "</button></li>");
            }
        }

        if (!ul.equals("")) {
            sb.append("</li></ul>");
        }

        return sb.toString();
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String imageToBase64(Drawable drawable)
    {
        Bitmap image = drawableToBitmap(drawable);
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 70, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int inetAddressToInt(InetAddress inetAddr)
            throws IllegalArgumentException {
        byte [] addr = inetAddr.getAddress();
        return ((addr[3] & 0xff) << 24) | ((addr[2] & 0xff) << 16) |
                ((addr[1] & 0xff) << 8) | (addr[0] & 0xff);
    }

    public static byte[] macAddressToByteArr(String mac){
        String macAddress = mac;
        String[] macAddressParts = macAddress.split(":");

        byte[] macAddressBytes = new byte[6];
        for(int i=0; i<6; i++){
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }
        return macAddressBytes;
    }
}

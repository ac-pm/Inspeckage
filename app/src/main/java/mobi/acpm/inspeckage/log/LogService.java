package mobi.acpm.inspeckage.log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.NotYetConnectedException;

import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.util.Config;

/**
 * Created by acpm on 14/03/16.
 */
public class LogService extends Service {

    public static final String TAG = "Inspeckage_Log";
    private boolean isStarted = false;
    private String pid = "";
    private Thread logThread;
    private Thread pidThread;
    private java.lang.Process logProcess;
    private WSocketServer wss;
    private SharedPreferences mPrefs;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Context context = getApplicationContext();

        try {

            int port = 8887;
            String filter = "";

            if (intent != null && intent.getExtras() != null) {
                port = intent.getIntExtra("port", 8887);
                filter = intent.getStringExtra("filter");
            }

            mPrefs = context.getSharedPreferences(Module.PREFS, context.MODE_PRIVATE);

            wss = new WSocketServer(port);
            wss.start();

            startLogger(filter);

            Toast.makeText(this, "LogService started on port " + port, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wss != null)
            try {
                stopLogger();
                wss.stop();
                Toast.makeText(this, "LogService stopped", Toast.LENGTH_LONG).show();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
    }

    private void startLogger(final String filter) {
        if (!isStarted) {
            logThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        Runtime.getRuntime().exec("su -c logcat -c");
                        String cmd = "su -c logcat |grep -v Xposed |grep -v Inspeckage |grep -v E/ |grep -v I/ |grep -v W/ |grep -v F/ |grep -v V/ |grep -v W/ |grep -v D/";

                        String[] filters = filter.split(",");

                        for (String filter1 : filters) {
                            if (cmd.contains(filter1 + "/")) {
                                cmd = cmd.replace("|grep -v " + filter1 + "/", "");
                            }
                        }


                        logProcess = Runtime.getRuntime().exec(cmd);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));

                        String line;
                        while (isStarted && (line = bufferedReader.readLine()) != null) {

                            if (pid.trim().length() > 2 && line.contains(pid.trim())) {
                                wss.sendToClient(line);
                            }
                        }
                        logProcess.destroy();
                    } catch (IOException | NotYetConnectedException e) {
                        Log.e(TAG, "LogService failed: " + e.getMessage());
                    }
                }
            }, "Logger_Thread");
            logThread.start();
            isStarted = true;
            //---PID
            pidThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        while (isStarted) {

                            String name = mPrefs.getString(Config.SP_PACKAGE, "null");
                            String ps = "su -c ps |grep "+name;
                            Process p = Runtime.getRuntime().exec(ps);
                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String var = "";
                            String psline;
                            while ((psline = br.readLine()) != null) {
                                if (psline.contains(" " + name)) {
                                    var = psline;
                                }
                            }

                            if (var.length() > 10) {
                                String tmp = var.replaceAll("\\s+", " ");
                                pid = tmp.split(" ")[1];
                            }

                            synchronized (this) {
                                try {
                                    wait(3000);
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    } catch (IOException | NotYetConnectedException e) {
                        Log.e(TAG, "LogService failed: " + e.getMessage());
                    }
                }
            }, "Logger_Thread");
            pidThread.start();

        }
    }

    public void stopLogger() {
        if (isStarted) {
            isStarted = false;
            try {
                logProcess.destroy();
                pidThread.join();
                logThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

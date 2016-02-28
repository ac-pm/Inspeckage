package mobi.acpm.inspeckage.webserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by acpm on 17/11/15.
 */
public class InspeckageService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private WebServer ws;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Context context = getApplicationContext();
        try {

            ws = new WebServer(intent.getIntExtra("port",8008),context);


        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Service started on port "+String.valueOf(intent.getIntExtra("port",8008)), Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(ws!=null)
            ws.stop();

        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }
}

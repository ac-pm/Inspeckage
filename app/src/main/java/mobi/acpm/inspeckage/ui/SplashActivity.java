package mobi.acpm.inspeckage.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.hooks.entities.BuildItem;
import mobi.acpm.inspeckage.hooks.entities.BuildList;
import mobi.acpm.inspeckage.util.Config;

public class SplashActivity extends AppCompatActivity {

    private static int TIME_OUT = 2000;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mPrefs = getSharedPreferences(Module.PREFS, MODE_WORLD_READABLE);
        loadBuild();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, TIME_OUT);
    }

    private void loadBuild(){
        if(mPrefs.getString(Config.SP_BUILD_HOOKS,"").equals("")){

            BuildList list = new BuildList();
            List<BuildItem> li = new ArrayList<>();
            li.add(new BuildItem("VERSION","RELEASE",Build.VERSION.RELEASE,Build.VERSION.RELEASE,false));

            li.add(new BuildItem("VERSION","CODENAME",Build.VERSION.CODENAME,Build.VERSION.CODENAME,false));
            li.add(new BuildItem("VERSION","INCREMENTAL",Build.VERSION.INCREMENTAL,Build.VERSION.INCREMENTAL,false));

            li.add(new BuildItem("VERSION","SDK",Build.VERSION.SDK,Build.VERSION.SDK,false));
            li.add(new BuildItem("VERSION","SDK_INT",String.valueOf(Build.VERSION.SDK_INT),String.valueOf(Build.VERSION.SDK_INT),false));

            if (android.os.Build.VERSION.SDK_INT >= 23) {
                li.add(new BuildItem("VERSION","BASE_OS",Build.VERSION.BASE_OS,Build.VERSION.BASE_OS,false));
                li.add(new BuildItem("VERSION","PREVIEW_SDK_INT",String.valueOf(Build.VERSION.PREVIEW_SDK_INT),String.valueOf(Build.VERSION.PREVIEW_SDK_INT),false));
                li.add(new BuildItem("VERSION","SECURITY_PATCH",Build.VERSION.SECURITY_PATCH,Build.VERSION.SECURITY_PATCH,false));
            }

            li.add(new BuildItem("BUILD","BOARD",Build.BOARD,Build.BOARD,false));
            li.add(new BuildItem("BUILD","BOOTLOADER",Build.BOOTLOADER,Build.BOOTLOADER,false));
            li.add(new BuildItem("BUILD","BRAND",Build.BRAND,Build.BRAND,false));
            li.add(new BuildItem("BUILD","CPU_ABI",Build.CPU_ABI,Build.CPU_ABI,false));
            li.add(new BuildItem("BUILD","CPU_ABI2",Build.CPU_ABI2,Build.CPU_ABI2,false));
            li.add(new BuildItem("BUILD","DEVICE",Build.DEVICE,Build.DEVICE,false));
            li.add(new BuildItem("BUILD","DISPLAY",Build.DISPLAY,Build.DISPLAY,false));
            li.add(new BuildItem("BUILD","FINGERPRINT",Build.FINGERPRINT,Build.FINGERPRINT,false));
            li.add(new BuildItem("BUILD","HARDWARE",Build.HARDWARE,Build.HARDWARE,false));
            li.add(new BuildItem("BUILD","HOST",Build.HOST,Build.HOST,false));
            li.add(new BuildItem("BUILD","ID",Build.ID,Build.ID,false));
            li.add(new BuildItem("BUILD","MANUFACTURER",Build.MANUFACTURER,Build.MANUFACTURER,false));
            li.add(new BuildItem("BUILD","MODEL",Build.MODEL,Build.MODEL,false));
            li.add(new BuildItem("BUILD","PRODUCT",Build.PRODUCT,Build.PRODUCT,false));
            li.add(new BuildItem("BUILD","RADIO",Build.RADIO,Build.RADIO,false));
            li.add(new BuildItem("BUILD","SERIAL",Build.SERIAL,Build.SERIAL,false));
            li.add(new BuildItem("BUILD","TAGS",Build.TAGS,Build.TAGS,false));
            li.add(new BuildItem("BUILD","TYPE",Build.TYPE,Build.TYPE,false));
            li.add(new BuildItem("BUILD","USER",Build.USER,Build.USER,false));

            list.buildItems = li;

            Gson gson = new GsonBuilder().create();

            SharedPreferences.Editor editor = mPrefs.edit();
            String json = gson.toJson(list);
            Log.d("Inspeckage_DeviceData",json);
            editor.putString(Config.SP_BUILD_HOOKS,json);
            editor.apply();
        }
    }
}

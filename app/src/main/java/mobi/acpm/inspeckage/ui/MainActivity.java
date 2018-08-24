package mobi.acpm.inspeckage.ui;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.File;

import mobi.acpm.inspeckage.Module;
import mobi.acpm.inspeckage.R;
import mobi.acpm.inspeckage.util.Config;
import mobi.acpm.inspeckage.util.FileUtil;
import mobi.acpm.inspeckage.webserver.InspeckageService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mPrefs = getSharedPreferences(Module.PREFS, MODE_PRIVATE);

        //main fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MainFragment mainFragment = new MainFragment(this);
        fragmentTransaction.replace(R.id.container, mainFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean granted = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
            boolean grantedPhone = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
            if (granted || grantedPhone) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, 0);
            }

            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    AdvertisingIdClient.Info idInfo = null;
                    try {
                        idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String advertId = null;
                    try{
                        advertId = idInfo.getId();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return advertId;
                }
                @Override
                protected void onPostExecute(String advertId) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(Config.SP_ADS_ID,advertId);
                    editor.apply();
                    //Toast.makeText(getApplicationContext(), advertId, Toast.LENGTH_SHORT).show();
                }
            };
            task.execute();

        }else{
            File inspeckage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT);
            if (!inspeckage.exists()) {
                inspeckage.mkdirs();
            }
            hideItem();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    File inspeckage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Config.P_ROOT);
                    if (!inspeckage.exists()) {
                        inspeckage.mkdirs();
                    }
                } else {
                    // permission denied
                    //Util.showNotification(getApplicationContext(),"");
                }
                return;
            }
            case 1:{
                return;
            }
        }
    }

    private void hideItem()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_auth).setVisible(false);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 1) {
            stopService();
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        int id = item.getItemId();

        if (id == R.id.nav_clear) {

            clearAll();
            TextView txtAppSelected = (TextView) findViewById(R.id.txtAppSelected);
            if(txtAppSelected!=null) {
                txtAppSelected.setText("... ");
            }

        } else if (id == R.id.nav_close) {

            clearAll();
            stopService();
            super.finish();
            android.os.Process.killProcess(android.os.Process.myPid());

        } else if (id == R.id.nav_config) {

            ConfigFragment configFragment = new ConfigFragment(this);
            fragmentTransaction.replace(R.id.container, configFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_auth) {

            AuthFragment authFragment = new AuthFragment(this);
            fragmentTransaction.replace(R.id.container, authFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_share) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://github.com/ac-pm/Inspeckage");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        }else{

            MainFragment mainFragment = new MainFragment();
            fragmentTransaction.replace(R.id.container, mainFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void stopService() {
        stopService(new Intent(this, InspeckageService.class));
    }

    private void clearAll(){
        SharedPreferences.Editor edit = mPrefs.edit();

        String appPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!mPrefs.getBoolean(Config.SP_HAS_W_PERMISSION, false)) {
            appPath = mPrefs.getString(Config.SP_DATA_DIR, "");
        }

        edit.putString(Config.SP_PROXY_HOST, "");
        edit.putString(Config.SP_PROXY_PORT, "");
        edit.putBoolean(Config.SP_SWITCH_PROXY, false);
        edit.putBoolean(Config.SP_FLAG_SECURE, false);
        edit.putBoolean(Config.SP_UNPINNING, false);
        edit.putBoolean(Config.SP_EXPORTED, false);
        edit.putBoolean(Config.SP_HAS_W_PERMISSION, true);
        edit.putString(Config.SP_SERVER_HOST, null);
        edit.putString(Config.SP_SERVER_PORT, null);
        edit.putString(Config.SP_SERVER_IP, null);
        edit.putString(Config.SP_SERVER_INTERFACES, "");

        edit.putString(Config.SP_PACKAGE, "");
        edit.putString(Config.SP_APP_NAME, "");
        edit.putString(Config.SP_APP_VERSION, "");
        edit.putString(Config.SP_DEBUGGABLE, "");
        edit.putString(Config.SP_APK_DIR, "");
        edit.putString(Config.SP_UID, "");
        edit.putString(Config.SP_GIDS, "");
        edit.putString(Config.SP_DATA_DIR, "");
        //white img
        edit.putString(Config.SP_APP_ICON_BASE64, "iVBORw0KGgoAAAANSUhEUgAAABoAAAAbCAIAAADtdAg8AAAAA3NCSVQICAjb4U/gAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAAJUlEQVRIiWP8//8/A/UAExXNGjVu1LhR40aNGzVu1LhR44aScQDKygMz8IbG2QAAAABJRU5ErkJggg==");

        edit.putString(Config.SP_EXP_ACTIVITIES, "");
        edit.putString(Config.SP_N_EXP_ACTIVITIES, "");
        edit.putString(Config.SP_REQ_PERMISSIONS, "");
        edit.putString(Config.SP_APP_PERMISSIONS, "");
        edit.putString(Config.SP_N_EXP_PROVIDER, "");
        edit.putString(Config.SP_N_EXP_SERVICES, "");
        edit.putString(Config.SP_N_EXP_BROADCAST, "");

        edit.putString(Config.SP_EXP_SERVICES, "");
        edit.putString(Config.SP_EXP_BROADCAST, "");
        edit.putString(Config.SP_EXP_PROVIDER, "");
        edit.putString(Config.SP_SHARED_LIB, "");

        edit.putBoolean(Config.SP_APP_IS_RUNNING, false);
        edit.putString(Config.SP_DATA_DIR_TREE, "");

        edit.putString(Config.SP_USER_HOOKS, "");

        edit.apply();

        File root = new File(appPath + Config.P_ROOT);
        FileUtil.deleteRecursive(root);

    }
}

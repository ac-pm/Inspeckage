package mobi.acpm.inspeckage.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PathPermission;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;

import java.util.List;

import mobi.acpm.inspeckage.Module;

/**
 * Created by acpm on 16/11/15.
 */
public class PackageDetail {

    private PackageInfo mPInfo;
    private ApplicationInfo mAppInfo;
    private Context mContext;
    private SharedPreferences mPrefs;
    private PackageManager pm;

    public PackageDetail(Context context, String app) {

        mPrefs = context.getSharedPreferences(Module.PREFS, context.MODE_PRIVATE);

        mContext = context;
        pm = context.getPackageManager();
        List<PackageInfo> plist = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo pi : plist) {
            if (mPrefs.getBoolean(Config.SP_SWITCH_OUA, true) ? (pi.applicationInfo.flags & 129) == 0 : true) {
                if (pi.packageName.equals(app)) {
                    try {
                        //gohorse to bypass "TransactionTooLargeExceptions"
                        mPInfo = pm.getPackageInfo(app, PackageManager.GET_META_DATA);
                        mPInfo.gids = pm.getPackageInfo(app, PackageManager.GET_GIDS).gids;
                        mPInfo.activities = pm.getPackageInfo(app, PackageManager.GET_ACTIVITIES).activities;//0xFFFFFFFF);

                        mPInfo.providers = pm.getPackageInfo(app, PackageManager.GET_PROVIDERS).providers;
                        mPInfo.receivers = pm.getPackageInfo(app, PackageManager.GET_RECEIVERS).receivers;
                        mPInfo.services = pm.getPackageInfo(app, PackageManager.GET_SERVICES).services;
                        mPInfo.applicationInfo.sharedLibraryFiles = pm.getPackageInfo(app, PackageManager.GET_SHARED_LIBRARY_FILES).applicationInfo.sharedLibraryFiles;

                        mPInfo.permissions = pm.getPackageInfo(app, PackageManager.GET_PERMISSIONS).permissions;
                        mPInfo.requestedPermissions = pm.getPackageInfo(app, PackageManager.GET_PERMISSIONS).requestedPermissions;

                        mAppInfo = pi.applicationInfo;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Intent getLaunchIntent() {
        return pm.getLaunchIntentForPackage(getPackageName());
    }

    public String getPackageName() {
        String pkg_name = "";
        if (mPInfo != null) {
            pkg_name = mPInfo.packageName;
        }
        return pkg_name;
    }

    public String getAppName() {
        return "" + mPInfo.applicationInfo.loadLabel(mContext.getPackageManager()).toString();
    }

    public String getVersion() {
        return mPInfo.versionName;
    }

    public String getRequestedPermissions() {
        StringBuilder sb = new StringBuilder();

        if (mPInfo.requestedPermissions != null) {
            for (String perm : mPInfo.requestedPermissions) {
                sb.append(perm + "\n");
            }
        } else {
            sb.append("-- Permissions\n");
        }

        return sb.toString();
    }

    public String getAppPermissions() {
        StringBuilder sb = new StringBuilder();

        if (mPInfo.permissions != null) {
            for (PermissionInfo perm : mPInfo.permissions) {
                sb.append(perm.name + "\n");
            }
        } else {
            sb.append("-- Permissions\n");
        }

        return sb.toString();
    }

    public String getExportedActivities() {
        StringBuilder sb = new StringBuilder();

        if (mPInfo.activities != null) {
            for (ActivityInfo ai : mPInfo.activities) {
                //ComponentInfo ci = new ComponentInfo(ai);
                if (ai.exported) {
                    if (ai.permission != null) {
                        sb.append(ai.name + " PERM: " + ai.permission + "\n");
                    } else {
                        sb.append(ai.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedActivities() {

        StringBuilder sb = new StringBuilder();

        if (mPInfo.activities != null) {
            for (ActivityInfo ai : mPInfo.activities) {
                if (!ai.exported)
                    sb.append(ai.name + "\n");
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getExportedServices() {
        StringBuilder sb = new StringBuilder();

        if (mPInfo.services != null) {
            for (ServiceInfo si : mPInfo.services) {

                if (si.exported) {
                    if (si.permission != null) {
                        sb.append(si.name + " PERM: " + si.permission + "\n");
                    } else {
                        sb.append(si.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedServices() {
        StringBuilder sb = new StringBuilder();

        if (mPInfo.services != null) {
            for (ServiceInfo si : mPInfo.services) {

                if (!si.exported) {
                    if (si.permission != null) {
                        sb.append(si.name + " PERM: " + si.permission + "\n");
                    } else {
                        sb.append(si.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getExportedBroadcastReceivers() {
        StringBuilder sb = new StringBuilder();

        if (mPInfo.receivers != null) {
            for (ActivityInfo ai : mPInfo.receivers) {

                if (ai.exported) {
                    if (ai.permission != null) {
                        sb.append(ai.name + " PERM: " + ai.permission + "\n");
                    } else {
                        sb.append(ai.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedBroadcastReceivers() {
        StringBuilder sb = new StringBuilder();

        if (mPInfo.receivers != null) {
            for (ActivityInfo ai : mPInfo.receivers) {

                if (!ai.exported) {
                    if (ai.permission != null) {
                        sb.append(ai.name + " PERM: " + ai.permission + "\n");
                    } else {
                        sb.append(ai.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getExportedContentProvider() {
        StringBuilder sb = new StringBuilder();
        if (mPInfo.providers != null) {
            for (ProviderInfo pi : mPInfo.providers) {
                String piName = pi.name;
                if (pi.exported) {

                    //Grant Uri Permissions
                    piName = piName + " GRANT: " + String.valueOf(pi.grantUriPermissions) + "|";

                    if (pi.authority != null) {
                        piName = piName + " AUTHORITY: " + pi.authority + "|";
                    }

                    if (pi.readPermission != null) {
                        piName = piName + " READ: " + pi.readPermission + "|";
                    }
                    if (pi.writePermission != null) {
                        piName = piName + " WRITE: " + pi.writePermission + "|";
                    }
                    PathPermission[] pp = pi.pathPermissions;
                    if (pp != null) {
                        for (PathPermission pathPermission : pp) {
                            piName = piName + " PATH: " + pathPermission.getPath() + "|";
                            piName = piName + "  - READ: " + pathPermission.getReadPermission() + "|";
                            piName = piName + "  - WRITE: " + pathPermission.getWritePermission() + "|";
                        }
                    }
                    sb.append(piName + "\n");
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedContentProvider() {
        StringBuilder sb = new StringBuilder();
        if (mPInfo.providers != null) {
            for (ProviderInfo pi : mPInfo.providers) {
                String piName = pi.name;
                if (!pi.exported) {

                    //Grant Uri Permissions
                    piName = piName + " GRANT: " + String.valueOf(pi.grantUriPermissions) + "|";

                    if (pi.authority != null) {
                        piName = piName + " AUTHORITY: " + pi.authority + "|";
                    }

                    if (pi.readPermission != null) {
                        piName = piName + " READ: " + pi.readPermission + "|";
                    }
                    if (pi.writePermission != null) {
                        piName = piName + " WRITE: " + pi.writePermission + "|";
                    }
                    PathPermission[] pp = pi.pathPermissions;
                    if (pp != null) {
                        for (PathPermission pathPermission : pp) {
                            piName = piName + " PATH: " + pathPermission.getPath() + "|";
                            piName = piName + "  - READ: " + pathPermission.getReadPermission() + "|";
                            piName = piName + "  - WRITE: " + pathPermission.getWritePermission() + "|";
                        }
                    }
                    sb.append(piName + "\n");
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getUID() {
        String uid = "";
        if (mAppInfo != null) {
            uid = String.valueOf(mAppInfo.uid);
        } else {
            uid = "-- null";
        }
        return uid;
    }

    public String getProcessName() {
        String pname = "";
        if (mAppInfo != null) {
            pname = mAppInfo.processName;
        } else {
            pname = "-- null";
        }
        return pname;
    }

    public String getDataDir() {
        String dir = "";
        if (mAppInfo != null) {
            dir = mAppInfo.dataDir;
        }
        return dir;
    }

    public String getGIDs() {
        String gidList = "";
		if (mPInfo.gids != null && mPInfo.gids.length != 0) {
            for (int gid : mPInfo.gids) {
                gidList = gidList + "" + gid + "-";
            }
        } else {
            gidList = "-- null";
        }
        return gidList.substring(0, gidList.length() - 1);
    }

    public String isDebuggable() {
        Boolean isDebuggable = false;

        if (0 != (mAppInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            isDebuggable = true;
        }
        return String.valueOf(isDebuggable);
    }

    public String allowBackup() {
        Boolean allow = false;

        if (0 != (mAppInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP)) {
            allow = true;
        }
        return String.valueOf(allow);
    }

    public String getIconBase64() {
        String icon = "";
        if (mAppInfo != null) {
            icon = Util.imageToBase64(mAppInfo.loadIcon(pm));
        }
        return icon;
    }

    public String getSharedUserId() {
        String suserid = "";
        if (mPInfo.sharedUserId != null) {
            suserid = mPInfo.sharedUserId;
        } else {
            suserid = "-- null";
        }
        return suserid;
    }

    public String getApkDir() {
        String sourceDir = "";
        if (mPInfo.applicationInfo.publicSourceDir != null)
            sourceDir = mPInfo.applicationInfo.publicSourceDir;

        return sourceDir;
    }

    public String getSharedLibraries() {

        StringBuilder sb = new StringBuilder();
        if (mPInfo.applicationInfo.sharedLibraryFiles != null) {
            for (String sl : mPInfo.applicationInfo.sharedLibraryFiles) {
                sb.append(sl + "\n");
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public void extractInfoToFile() {

        StringBuilder sb = new StringBuilder();
        sb.append("Package: " + getPackageName() + "\n");
        sb.append("Process Name: " + getProcessName() + "\n");
        sb.append("APK Dir: " + getApkDir() + "\n");
        sb.append("UID: " + getUID() + "\n");
        sb.append("GIDs: " + getGIDs() + "\n");
        sb.append("Is Debuggable: " + isDebuggable() + "\n");
        sb.append("Allow Backup: " + allowBackup() + "\n");
        sb.append("Shared User ID: " + getSharedUserId() + "\n");

        sb.append(getRequestedPermissions());
        sb.append(getAppPermissions());
        sb.append(getExportedActivities());
        sb.append(getNonExportedActivities());
        sb.append(getExportedServices());
        sb.append(getNonExportedServices());

        sb.append(getExportedBroadcastReceivers());
        sb.append(getNonExportedBroadcastReceivers());
        sb.append(getExportedContentProvider());
        sb.append(getNonExportedContentProvider());
        sb.append(getSharedLibraries());

        FileUtil.writeToFile(mPrefs, sb.toString(), FileType.PACKAGE, "");
                /**
        FileUtil.writeToFile(mPrefs, "Package: " + getPackageName() + "\n", FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, "Process Name: " + getProcessName() + "\n", FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, "APK Dir: " + getApkDir() + "\n", FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, "UID: " + getUID() + "\n", FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, "GIDs: " + getGIDs() + "\n", FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, "Is Debuggable: " + isDebuggable() + "\n", FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, "Shared User ID: " + getSharedUserId() + "\n", FileType.PACKAGE, "");

        FileUtil.writeToFile(mPrefs, getRequestedPermissions(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getAppPermissions(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getExportedActivities(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getNonExportedActivities(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getExportedServices(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getNonExportedServices(), FileType.PACKAGE, "");

        FileUtil.writeToFile(mPrefs, getExportedBroadcastReceivers(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getNonExportedBroadcastReceivers(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getExportedContentProvider(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getNonExportedContentProvider(), FileType.PACKAGE, "");
        FileUtil.writeToFile(mPrefs, getSharedLibraries(), FileType.PACKAGE, "");**/
    }
}

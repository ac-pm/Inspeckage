package mobi.acpm.inspeckage.ui;

import android.graphics.drawable.Drawable;

/**
 * Created by acpm on 17/11/15.
 */
public class ExpandableListItem {

    private String mAppName = "";
    private String mPackageName = "";
    private boolean mIsSelected = false;
    private Drawable mIcon;

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String mAppName) {
        this.mAppName = mAppName;
    }

    public String getPckName() {
        return mPackageName;
    }

    public void setPckName(String packName) {
        this.mPackageName = packName;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean bypassed) {
        this.mIsSelected = bypassed;
    }
}

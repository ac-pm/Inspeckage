package mobi.acpm.inspeckage.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import mobi.acpm.inspeckage.R;

/**
 * Created by acpm on 17/11/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<String> mListDataHeader;
    private HashMap<String, List<ExpandableListItem>> mListDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<ExpandableListItem>> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final ExpandableListItem childText = (ExpandableListItem) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        ImageView iconImage = (ImageView)convertView.findViewById(R.id.imageViewIcon);
        iconImage.setImageDrawable(childText.getIcon());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(90, 90);
        iconImage.setLayoutParams(layoutParams);

        TextView txtListChild = (TextView) convertView.findViewById(R.id.txtListItem);
        txtListChild.setText(childText.getAppName());

        TextView txtListPkg = (TextView) convertView.findViewById(R.id.txtListPkg);
        txtListPkg.setText(childText.getPckName());

        /**
        if(childText.isSelected()) {
            txtListChild.setTextColor(0xFF00BFA5);
            txtListPkg.setTextColor(0xFF00BFA5);
        }else{
            txtListChild.setTextColor(Color.BLACK);
            txtListPkg.setTextColor(Color.BLACK);
            txtListChild.setBackgroundColor(Color.WHITE);
            txtListPkg.setBackgroundColor(Color.WHITE);
        }**/

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
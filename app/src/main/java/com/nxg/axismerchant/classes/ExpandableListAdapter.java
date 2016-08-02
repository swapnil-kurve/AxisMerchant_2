package com.nxg.axismerchant.classes;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxg.axismerchant.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dell on 26-05-2016.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, UserList> _listDataChild;
    private ArrayList<UserList> userListArrayList;

    public ExpandableListAdapter(Context context, ArrayList<String> listDataHeader,
                                 HashMap<String, UserList> listChildData, ArrayList<UserList> userListArrayList) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.userListArrayList = userListArrayList;
    }

    private static class ViewHolder
    {
        TextView lblListHeader;
        TextView txtEmailID;
        TextView txtMobileNo;
        TextView txtAddedOn;
        ImageView imgIcon;
    }


    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition));//.get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

//        final String childText = (String) getChild(groupPosition, childPosition);
        UserList childText = this._listDataChild.get(this.userListArrayList.get(groupPosition).getRegUsersID());//             .get(childPosition);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.layout_child, null);

            holder = new ViewHolder();

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
            holder.txtEmailID = (TextView) convertView.findViewById(R.id.txtEmailID);
            holder.txtMobileNo = (TextView) convertView.findViewById(R.id.txtMobileNumber);
            holder.txtAddedOn = (TextView) convertView.findViewById(R.id.txtAddedOn);

            holder.txtEmailID.setText(childText.getEmailid());
            holder.txtMobileNo.setText(childText.getMobileNo());
            holder.txtAddedOn.setText(childText.getAddedDate());
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        ViewHolder holder;
//        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.layout_group, null);

            holder = new ViewHolder();

            convertView.setTag(holder);
//        }
        holder = (ViewHolder) convertView.getTag();
        holder.lblListHeader = (TextView) convertView.findViewById(R.id.txtUserName);
        holder.imgIcon = (ImageView) convertView.findViewById(R.id.iconExpand);

        holder.lblListHeader.setText(headerTitle);

        if (isExpanded)
        {
            holder.imgIcon.setImageResource(R.mipmap.substract);
            holder.lblListHeader.setTypeface(null, Typeface.BOLD);
        }else
        {
            holder.imgIcon.setImageResource(R.mipmap.add);
            holder.lblListHeader.setTypeface(null, Typeface.NORMAL);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getGroupTypeCount() {
        return this._listDataHeader.size();
    }

    @Override
    public int getGroupType(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getChildTypeCount() {
        return 1;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return super.getChildType(groupPosition, childPosition);
    }
}

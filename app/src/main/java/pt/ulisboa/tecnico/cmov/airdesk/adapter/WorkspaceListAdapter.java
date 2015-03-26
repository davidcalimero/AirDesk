package pt.ulisboa.tecnico.cmov.airdesk.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.R;

public class WorkspaceListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> groupHeaders = new ArrayList<>();
    private HashMap<String, ArrayList<String>> childHeaders = new HashMap<>(); //Format (groupTitle, childTitle)

    public WorkspaceListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.childHeaders.get(groupHeaders.get(groupPosition)).get(childPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.groupHeaders.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childHeaders.get(groupHeaders.get(groupPosition)).size();
    }

    @Override
    public int getGroupCount() {
        return groupHeaders.size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childTitle = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.itemHeader);
        textView.setText(childTitle);
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.groupHeader);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setText(groupTitle);
        return convertView;
    }

    public void createGroup(String title){
        groupHeaders.add(title);
        childHeaders.put(title, new ArrayList<String>());
    }

    public void addChild(String groupTitle, String childTitle){
        childHeaders.get(groupTitle).add(childTitle);
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

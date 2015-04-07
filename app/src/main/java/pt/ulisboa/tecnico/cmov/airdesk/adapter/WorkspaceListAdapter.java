package pt.ulisboa.tecnico.cmov.airdesk.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.airdesk.R;

public class WorkspaceListAdapter extends BaseExpandableListAdapter {

    private class Item{
        public String tag;
        public String group;
        public ArrayList<String> files;

        public Item(String tag, String group){
            this.tag = tag;
            this.group = group;
            this.files = new ArrayList<>();
        }

        @Override
         public boolean equals(Object o) {
            if(o instanceof Item){
                Item item = (Item) o;
                if(item.tag.equals(this.tag) && item.group.equals(this.group))
                    return true;
            }
            return false;
        }
    }

    private Context context;
    private int group_xml;
    private int item_xml;
    private ArrayList<Item> items = new ArrayList<>();

    public WorkspaceListAdapter(Context context, int group_xml, int item_xml) {
        this.context = context;
        this.group_xml = group_xml;
        this.item_xml = item_xml;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return items.get(groupPosition).files.get(childPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return items.get(groupPosition).group;
    }

    public Object getTag(int groupPosition) {
        return items.get(groupPosition).tag;
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
        return items.get(groupPosition).files.size();
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childTitle = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(item_xml, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.itemHeader);
        textView.setText(childTitle);
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);
        String tagTitle = (String) getTag(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(group_xml, null);
        }

        //Tag Header
        TextView tagView = (TextView) convertView.findViewById(R.id.tagHeader);
        tagView.setTypeface(null, Typeface.BOLD);
        tagView.setText(tagTitle);

        //Group Header
        TextView groupView = (TextView) convertView.findViewById(R.id.groupHeader);
        groupView.setTypeface(null, Typeface.BOLD);
        groupView.setText(groupTitle);
        return convertView;
    }

    public void addGroup(String tag, String title) {
        if(!items.contains(new Item(tag, title))) {
            items.add(new Item(tag, title));
            Log.e("WorkspaceListAdapter", "addGroup: " + tag + " "  + title);
        }
    }

    public void removeGroup(String tag, String title) {
        items.remove(new Item(tag, title));
        Log.e("WorkspaceListAdapter", "removeGroup: " + tag + " " + title);
    }

    public void addChild(String tag, String groupTitle, String childTitle) {
        for(Item item : items){
            if(item.equals(new Item(tag, groupTitle))){
                if(!item.files.contains(childTitle)) {
                    Log.e("WorkspaceListAdapter", "addChild: " + tag + " " + groupTitle + " " + childTitle);
                    item.files.add(childTitle);
                    return;
                }
            }
        }
    }

    public void removeChild(String tag, String groupTitle, String childTitle) {
        for(Item item : items){
            if(item.equals(new Item(tag, groupTitle))){
                Log.e("WorkspaceListAdapter", "removeChild: " + tag + " " + groupTitle + " " + childTitle);
                item.files.remove(childTitle);
                return;
            }
        }
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

package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;



class CheckBoxListEntry {
    private boolean selected = false;
    private String name;

    public interface OnCheckBoxListEntryListener {
        public void onSelected(CheckBoxListEntry entry);
    }
    static private OnCheckBoxListEntryListener listener = null;
    static void setListener(OnCheckBoxListEntryListener l) {
        listener = l;
    }

    CheckBoxListEntry(String name) {
        this.name = name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        if (listener != null)
            listener.onSelected(this);
    }

    boolean getSelected() {
        return selected;
    }
}

public class CheckBoxAdapter extends ArrayAdapter<CheckBoxListEntry> {
    private ArrayList<CheckBoxListEntry> listItems;
    private int layoutId;

    public CheckBoxAdapter(Context context, int textViewResourceId, ArrayList<CheckBoxListEntry> items) {
        super(context, textViewResourceId, items);

        listItems = items;
        layoutId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)parent.getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layoutId, null);
        }
        assert convertView != null;
        CheckBoxListEntry entry = listItems.get(position);

        CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
        assert checkBox != null;
        checkBox.setTag(entry);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CheckBoxListEntry cookie = (CheckBoxListEntry)compoundButton.getTag();
                cookie.setSelected(b);
            }
        });
        TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
        assert textView != null;
        checkBox.setChecked(entry.getSelected());
        textView.setText(entry.getName());
        return convertView;
    }
}
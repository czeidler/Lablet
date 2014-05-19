/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import nz.ac.aucklanduni.physics.lablet.R;

import java.util.ArrayList;


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
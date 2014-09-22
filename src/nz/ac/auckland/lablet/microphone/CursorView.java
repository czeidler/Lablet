/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.table.DataTableColumn;
import nz.ac.auckland.lablet.views.table.MarkerDataTableAdapter;
import nz.ac.auckland.lablet.views.table.TableView;


class HCursorColumn extends DataTableColumn {
    @Override
    public int size() {
        return dataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return dataModel.getMarkerDataAt(index).getPosition().y;
    }

    @Override
    public String getHeader() {
        return "H-Cursor Position";
    }
}

class HCursorDiffToPrevColumn extends DataTableColumn {
    @Override
    public int size() {
        return dataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        if (index == 0)
            return 0;
        return dataModel.getMarkerDataAt(index).getPosition().y - dataModel.getMarkerDataAt(index - 1).getPosition().y;
    }

    @Override
    public String getHeader() {
        return "Diff to Prev";
    }
}

class VCursorColumn extends DataTableColumn {
    @Override
    public int size() {
        return dataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return dataModel.getMarkerDataAt(index).getPosition().x;
    }

    @Override
    public String getHeader() {
        return "V-Cursor Position";
    }
}

class VCursorDiffToPrevColumn extends DataTableColumn {
    @Override
    public int size() {
        return dataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        if (index == 0)
            return 0;
        return dataModel.getMarkerDataAt(index).getPosition().x - dataModel.getMarkerDataAt(index - 1).getPosition().x;
    }

    @Override
    public String getHeader() {
        return "Diff to Prev";
    }
}


public class CursorView extends LinearLayout {
    private MarkerDataModel hDataModel;
    private MarkerDataModel vDataModel;

    public CursorView(Context context, MarkerDataModel hDataModel, MarkerDataModel vDataModel) {
        super(context);

        this.hDataModel = hDataModel;
        this.vDataModel = vDataModel;

        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup hCursorView = (ViewGroup)inflater.inflate(R.layout.frequency_cursor, this, false);
        TextView titleTextView = (TextView)hCursorView.findViewById(R.id.titleTextView);
        titleTextView.setText("Horizontal Cursors:");
        TableView tableView = (TableView)hCursorView.findViewById(R.id.tableView);
        MarkerDataTableAdapter tableAdapter = new MarkerDataTableAdapter(hDataModel);
        tableAdapter.addColumn(new HCursorColumn());
        tableAdapter.addColumn(new HCursorDiffToPrevColumn());
        tableView.setAdapter(tableAdapter);

        addView(hCursorView);

        ViewGroup vCursorView = (ViewGroup)inflater.inflate(R.layout.frequency_cursor, this, false);
        titleTextView = (TextView)vCursorView.findViewById(R.id.titleTextView);
        titleTextView.setText("Vertical Cursors:");
        tableView = (TableView)vCursorView.findViewById(R.id.tableView);
        tableAdapter = new MarkerDataTableAdapter(vDataModel);
        tableAdapter.addColumn(new VCursorColumn());
        tableAdapter.addColumn(new VCursorDiffToPrevColumn());
        tableView.setAdapter(tableAdapter);
        addView(vCursorView);
    }
}

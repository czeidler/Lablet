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
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;
import nz.ac.auckland.lablet.views.plotview.PlotView;
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
    final private PlotView frequencyView;
    final private MarkerDataModel hDataModel;
    final private MarkerDataModel vDataModel;

    public CursorView(Context context, PlotView frequencyView, MarkerDataModel hDataModel, MarkerDataModel vDataModel) {
        super(context);

        this.frequencyView = frequencyView;
        this.hDataModel = hDataModel;
        this.vDataModel = vDataModel;

        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                1);
        addView(setupHCursorView(inflater), params);
        addView(new Space(context), new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20));
        addView(setupVCursorView(inflater), params);
    }

    class EnableButtonOnMarkerSelectedListener implements MarkerDataModel.IListener {
        final Button button;

        public EnableButtonOnMarkerSelectedListener(Button button) {
            this.button = button;
        }

        @Override
        public void onDataAdded(MarkerDataModel model, int index) {

        }

        @Override
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {

        }

        @Override
        public void onDataChanged(MarkerDataModel model, int index, int number) {

        }

        @Override
        public void onAllDataChanged(MarkerDataModel model) {

        }

        @Override
        public void onDataSelected(MarkerDataModel model, int index) {
            if (index < 0)
                button.setEnabled(false);
            else
                button.setEnabled(true);
        }
    }

    // keep hard references!
    private EnableButtonOnMarkerSelectedListener hMarkerListener;
    private EnableButtonOnMarkerSelectedListener vMarkerListener;

    private View setupHCursorView(LayoutInflater inflater) {
        ViewGroup hCursorView = (ViewGroup)inflater.inflate(R.layout.frequency_cursor, this, false);
        TextView titleTextView = (TextView)hCursorView.findViewById(R.id.titleTextView);
        titleTextView.setText("Horizontal Cursors:");
        final TableView tableView = (TableView)hCursorView.findViewById(R.id.tableView);
        if (hDataModel.getMarkerCount() == 0)
            tableView.setVisibility(INVISIBLE);
        MarkerDataTableAdapter tableAdapter = new MarkerDataTableAdapter(hDataModel);
        tableAdapter.addColumn(new HCursorColumn());
        tableAdapter.addColumn(new HCursorDiffToPrevColumn());
        tableView.setAdapter(tableAdapter);
        tableView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0)
                    return;
                hDataModel.selectMarkerData(i - 1);
            }
        });

        final Button removeButton = (Button)hCursorView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSelectedCursor(hDataModel, removeButton, tableView);
            }
        });
        hMarkerListener = new EnableButtonOnMarkerSelectedListener(removeButton);
        hDataModel.addListener(hMarkerListener);
        Button addButton = (Button)hCursorView.findViewById(R.id.addButton);
        if (hDataModel.getSelectedMarkerData() < 0)
            removeButton.setEnabled(false);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                addCursor(hDataModel, removeButton, tableView);
            }
        });
        return hCursorView;
    }

    private View setupVCursorView(LayoutInflater inflater) {
        ViewGroup vCursorView = (ViewGroup)inflater.inflate(R.layout.frequency_cursor, this, false);
        TextView titleTextView = (TextView)vCursorView.findViewById(R.id.titleTextView);
        titleTextView.setText("Vertical Cursors:");
        final TableView tableView = (TableView)vCursorView.findViewById(R.id.tableView);
        if (vDataModel.getMarkerCount() == 0)
            tableView.setVisibility(INVISIBLE);
        MarkerDataTableAdapter tableAdapter = new MarkerDataTableAdapter(vDataModel);
        tableAdapter.addColumn(new VCursorColumn());
        tableAdapter.addColumn(new VCursorDiffToPrevColumn());
        tableView.setAdapter(tableAdapter);
        tableView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0)
                    return;
                vDataModel.selectMarkerData(i - 1);
            }
        });

        final Button removeButton = (Button)vCursorView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSelectedCursor(vDataModel, removeButton, tableView);
            }
        });
        vMarkerListener = new EnableButtonOnMarkerSelectedListener(removeButton);
        vDataModel.addListener(vMarkerListener);
        if (vDataModel.getSelectedMarkerData() < 0)
            removeButton.setEnabled(false);
        Button addButton = (Button)vCursorView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                addCursor(vDataModel, removeButton, tableView);
            }
        });
        return vCursorView;
    }

    private void addCursor(MarkerDataModel markerDataModel, Button removeButton, TableView tableView) {
        MarkerData markerData = new MarkerData(markerDataModel.getLargestRunId() + 1);
        markerData.setPosition(frequencyView.getRangeMiddle());
        markerDataModel.addMarkerData(markerData);
        markerDataModel.selectMarkerData(markerData);

        if (markerDataModel.getMarkerCount() > 0)
            tableView.setVisibility(VISIBLE);
    }

    private void removeSelectedCursor(MarkerDataModel markerDataModel, Button removeButton, TableView tableView) {
        int selected = markerDataModel.getSelectedMarkerData();
        if (selected < 0 || markerDataModel.getMarkerCount() == 0)
            return;

        markerDataModel.removeMarkerData(selected);

        if (markerDataModel.getMarkerCount() == 0)
            tableView.setVisibility(INVISIBLE);
    }
}

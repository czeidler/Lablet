/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.CursorDataModelPainter;
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


public class CursorView extends ScrollView {
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
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout.addView(setupHCursorView(inflater));
        Space space = new Space(context);
        space.setLayoutParams(new LayoutParams(0, 20));
        layout.addView(space);
        layout.addView(setupVCursorView(inflater));

        addView(layout);
    }

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
        final Button removeButton = (Button)hCursorView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSelectedCursor(hDataModel, removeButton, tableView);
            }
        });
        Button addButton = (Button)hCursorView.findViewById(R.id.addButton);
        if (hDataModel.getMarkerCount() == 0)
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
        final Button removeButton = (Button)vCursorView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSelectedCursor(vDataModel, removeButton, tableView);
            }
        });
        if (vDataModel.getMarkerCount() == 0)
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

        if (markerDataModel.getMarkerCount() > 0) {
            removeButton.setEnabled(true);
            tableView.setVisibility(VISIBLE);
        }
    }

    private void removeSelectedCursor(MarkerDataModel markerDataModel, Button removeButton, TableView tableView) {
        int selected = markerDataModel.getSelectedMarkerData();
        if (selected < 0 || markerDataModel.getMarkerCount() == 0)
            return;

        markerDataModel.removeMarkerData(selected);

        if (markerDataModel.getMarkerCount() == 0) {
            removeButton.setEnabled(false);
            tableView.setVisibility(INVISIBLE);
        }
    }
}

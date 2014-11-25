/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of a table view. Uses the {@link nz.ac.auckland.lablet.views.table.ITableAdapter}.
 */
public class TableView extends ListView {
    protected TableListAdapter adapter = null;

    public TableView(Context context) {
        super(context);
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(ITableAdapter tableAdapter) {
        if (this.adapter != null) {
            this.adapter.release();
            setAdapter((ListAdapter)null);
        }
        if (tableAdapter == null) {
            this.adapter = null;
            return;
        }

        this.adapter = new TableListAdapter(tableAdapter);
        setAdapter(adapter);
    }
}

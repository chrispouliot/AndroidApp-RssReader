package com.android.acios.blocly.api.model;

/**
 * Created by christophepouliot on 15-07-12.
 */
public abstract class Model {
    private final long rowId;

    public Model(long rowId) {
        this.rowId = rowId;
    }

    public long getRowId() {
        return rowId;
    }
}

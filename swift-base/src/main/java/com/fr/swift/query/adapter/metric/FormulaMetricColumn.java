package com.fr.swift.query.adapter.metric;

import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.column.BitmapIndexedColumn;
import com.fr.swift.segment.column.Column;
import com.fr.swift.segment.column.DetailColumn;
import com.fr.swift.segment.column.DictionaryEncodedColumn;
import com.fr.swift.util.Crasher;

/**
 * Created by pony on 2018/5/11.
 */
public class FormulaMetricColumn implements Column {
    private String formula;
    private Segment segment;

    public FormulaMetricColumn(String formula, Segment segment) {
        this.formula = formula;
        this.segment = segment;
    }

    @Override
    public DictionaryEncodedColumn getDictionaryEncodedColumn() {
        return Crasher.crash("unsupported");
    }

    @Override
    public BitmapIndexedColumn getBitmapIndex() {
        return new FormulaIndexColumn();
    }

    @Override
    public DetailColumn getDetailColumn() {
        return new FormulaDetailColumn(formula, segment);
    }

    @Override
    public IResourceLocation getLocation() {
        return Crasher.crash("unsupported");
    }
}

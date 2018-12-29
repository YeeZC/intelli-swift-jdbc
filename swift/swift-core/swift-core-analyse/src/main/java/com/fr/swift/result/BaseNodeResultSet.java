package com.fr.swift.result;

import com.fr.swift.result.qrs.QueryResultSet;
import com.fr.swift.result.qrs.QueryResultSetMerger;
import com.fr.swift.source.SwiftMetaData;

/**
 * Created by lyon on 2018/12/29.
 */
public abstract class BaseNodeResultSet implements QueryResultSet<SwiftNode> {

    private int fetchSize;

    public BaseNodeResultSet(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    @Override
    public int getFetchSize() {
        return fetchSize;
    }

    @Override
    public <Q extends QueryResultSet<SwiftNode>> QueryResultSetMerger<SwiftNode, Q> getMerger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SwiftResultSet convert(SwiftMetaData metaData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {

    }
}

package com.fr.swift.result.serialize;

import com.fr.swift.query.query.QueryInfo;
import com.fr.swift.query.query.QueryRunnerProvider;
import com.fr.swift.query.query.RemoteQueryInfoManager;
import com.fr.swift.result.DetailResultSet;
import com.fr.swift.result.SwiftRowIteratorImpl;
import com.fr.swift.segment.SegmentDestination;
import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.structure.Pair;
import com.fr.swift.util.Crasher;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * @author yee
 * @date 2018/6/11
 */
public class SerializableDetailResultSet implements DetailResultSet, SerializableResultSet {
    private static final long serialVersionUID = -2306723089258907631L;

    private transient DetailResultSet resultSet;
    private String queryId;
    private SwiftMetaData metaData;
    private List<Row> rows;
    private boolean hasNextPage = true;
    private boolean originHasNextPage;
    private transient Iterator<Row> rowIterator;

    public SerializableDetailResultSet(String queryId, DetailResultSet resultSet) throws SQLException {
        this.queryId = queryId;
        this.resultSet = resultSet;
        init();
    }

    private void init() throws SQLException {
        this.metaData = resultSet.getMetaData();
        this.rows = resultSet.getPage();
        this.originHasNextPage = resultSet.hasNextPage();
    }

    @Override
    public List<Row> getPage() {
        hasNextPage = false;
        List<Row> ret = rows;
        if (originHasNextPage) {
            // TODO: 2018/6/14 向远程节点拉取下一页数据
            Pair<QueryInfo, SegmentDestination> pair = RemoteQueryInfoManager.getInstance().get(queryId);
            if (pair == null) {
                Crasher.crash("invalid remote queryInfo!");
            }
            try {
                resultSet = (DetailResultSet) QueryRunnerProvider.getInstance().executeRemoteQuery(pair.getKey(), pair.getValue());
                hasNextPage = true;
                init();
            } catch (SQLException e) {
                Crasher.crash(e);
            }
        }
        return ret;
    }

    @Override
    public boolean hasNextPage() {
        return hasNextPage || originHasNextPage;
    }

    @Override
    public int getRowCount() {
        return resultSet.getRowCount();
    }

    @Override
    public SwiftMetaData getMetaData() throws SQLException {
        return metaData;
    }

    @Override
    public boolean next() throws SQLException {
        if (rowIterator == null) {
            rowIterator = new SwiftRowIteratorImpl(this);
        }
        return rowIterator.hasNext();
    }

    @Override
    public Row getRowData() throws SQLException {
        return rowIterator.next();
    }

    @Override
    public void close() throws SQLException {

    }
}

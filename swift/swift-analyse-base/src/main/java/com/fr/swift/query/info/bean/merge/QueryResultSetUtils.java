package com.fr.swift.query.info.bean.merge;

import com.fr.swift.query.info.bean.query.FunnelQueryBean;
import com.fr.swift.query.query.QueryBean;
import com.fr.swift.result.SwiftResultSet;
import com.fr.swift.result.funnel.FunnelQueryResultSetMerger;
import com.fr.swift.result.qrs.QueryResultSet;
import com.fr.swift.result.qrs.QueryResultSetConverter;
import com.fr.swift.result.qrs.QueryResultSetMerger;
import com.fr.swift.source.SwiftMetaData;

import java.io.Serializable;

/**
 * Created by lyon on 2018/11/27.
 */
public class QueryResultSetUtils {

    public static <T extends Serializable> QueryResultSetMerger<T> createMerger(QueryBean queryBean) {
        switch (queryBean.getQueryType()) {
            case DETAIL:
                return null;
            case GROUP:
                return null;
            case FUNNEL:
                QueryResultSetMerger merger = new FunnelQueryResultSetMerger(((FunnelQueryBean) queryBean).getSteps().length);
                return merger;
        }
        return null;
    }

    public static SwiftResultSet convert(QueryResultSet resultSet, SwiftMetaData metaData) {
        QueryResultSetConverter converter = null;
        switch (resultSet.type()) {
            case ROW:
            case NODE:
                converter = null;
        }
        return converter.convert(resultSet, metaData);
    }
}

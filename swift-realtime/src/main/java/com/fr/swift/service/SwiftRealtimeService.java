package com.fr.swift.service;

import com.fr.swift.context.SwiftContext;
import com.fr.swift.exception.SwiftServiceException;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.query.builder.QueryBuilder;
import com.fr.swift.query.query.QueryInfo;
import com.fr.swift.query.query.QueryResultSetManager;
import com.fr.swift.query.query.QueryType;
import com.fr.swift.query.session.AbstractSession;
import com.fr.swift.query.session.Session;
import com.fr.swift.query.session.SessionBuilder;
import com.fr.swift.query.session.factory.SessionFactory;
import com.fr.swift.result.DetailResultSet;
import com.fr.swift.result.GroupNode;
import com.fr.swift.result.NodeMergeResultSet;
import com.fr.swift.result.NodeResultSet;
import com.fr.swift.result.SwiftNode;
import com.fr.swift.result.serialize.LocalAllNodeResultSet;
import com.fr.swift.result.serialize.LocalPartNodeResultSet;
import com.fr.swift.result.serialize.SerializableDetailResultSet;
import com.fr.swift.result.serialize.SerializableResultSet;
import com.fr.swift.rpc.annotation.RpcMethod;
import com.fr.swift.rpc.annotation.RpcService;
import com.fr.swift.rpc.annotation.RpcServiceType;
import com.fr.swift.segment.Incrementer;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.recover.SwiftSegmentRecovery;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.util.concurrent.CommonExecutor;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author pony
 * @date 2017/10/10
 */
@RpcService(type = RpcServiceType.CLIENT_SERVICE, value = RealtimeService.class)
public class SwiftRealtimeService extends AbstractSwiftService implements RealtimeService, Serializable {

    @Override
    public void insert(SourceKey tableKey, SwiftResultSet resultSet) throws SQLException {
        SwiftLoggers.getLogger().info("insert");

        new Incrementer(tableKey).increment(resultSet);
    }

    @Override
    @RpcMethod(methodName = "merge")
    public void merge(List<SegmentKey> tableKeys) {
        SwiftLoggers.getLogger().info("merge");
    }

    @Override
    @RpcMethod(methodName = "recover")
    public void recover(List<SegmentKey> tableKeys) {
        SwiftLoggers.getLogger().info("recover");
    }

    @Override
    @SuppressWarnings("Duplicates")
    @RpcMethod(methodName = "realTimeQuery")
    public <T extends SwiftResultSet> T query(final QueryInfo<T> queryInfo) throws SQLException {
        SessionFactory sessionFactory = SwiftContext.getInstance().getBean(SessionFactory.class);
        return sessionFactory.openSession(new SessionBuilder() {
            @Override
            public Session build(long cacheTimeout) {
                return new AbstractSession(cacheTimeout) {
                    @Override
                    protected <T extends SwiftResultSet> T query(QueryInfo<T> queryInfo) throws SQLException {
                        // 先到QueryResultSetManager找一下有没有缓存，没有则构建查询。
                        SwiftResultSet resultSet = QueryResultSetManager.getInstance().get(queryInfo.getQueryId());
                        if (resultSet == null) {
                            resultSet = QueryBuilder.buildQuery(queryInfo).getQueryResult();
                        }
                        SerializableResultSet result;
                        QueryType type = queryInfo.getType();
                        switch (type) {
                            case LOCAL_GROUP_ALL:
                                result = new LocalAllNodeResultSet(queryInfo.getQueryId(), (NodeResultSet<SwiftNode>) resultSet);
                                break;
                            case LOCAL_GROUP_PART:
                                result = new LocalPartNodeResultSet(queryInfo.getQueryId(), (NodeMergeResultSet<GroupNode>) resultSet);
                                break;
                            default:
                                result = new SerializableDetailResultSet(queryInfo.getQueryId(), (DetailResultSet) resultSet);
                        }
                        return (T) result;
                    }
                };
            }

            @Override
            public String getQueryId() {
                return queryInfo.getQueryId();
            }
        }).executeQuery(queryInfo);
    }

    @Override
    public boolean start() throws SwiftServiceException {
        super.start();

        recover0();

        return true;
    }

    private static void recover0() {
        CommonExecutor.get().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    // 恢复所有realtime块
                    SwiftSegmentRecovery.getInstance().recoverAll();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.REAL_TIME;
    }

    private static final long serialVersionUID = 4719723736240190155L;

    public SwiftRealtimeService(String id) {
        super(id);
    }

    public SwiftRealtimeService() {
    }
}
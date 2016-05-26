package com.fr.bi.etl.analysis.data;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.base.BICore;
import com.fr.bi.base.BIUser;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.db.IPersistentTable;
import com.fr.bi.stable.data.db.PersistentField;
import com.fr.bi.stable.data.source.AbstractCubeTableSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.code.BILogger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 小灰灰 on 2015/12/24.
 */
public abstract class AbstractAnalysisIDTableSource<T extends CubeTableSource> extends AbstractCubeTableSource implements AnalysisCubeTableSource {
    protected String tableID;
    protected transient T baseTable;
    private transient Map<Long, UserCubeTableSource> userBaseTableMap = new ConcurrentHashMap<Long, UserCubeTableSource>();

    public AbstractAnalysisIDTableSource() {
    }

    public AbstractAnalysisIDTableSource(String tableID, long userId) {
        this.tableID = tableID;
        this.baseTable = getBaseTableByID(new BITableID(tableID), new BIUser(userId));
        if (baseTable == null) {
            BILogger.getLogger().error("Analysis ETL table id : " + tableID + " create by : " + userId + " missed!");
        }
    }

    protected abstract T getBaseTableByID(BITableID tableID, BIUser user);

    @Override
    public BICore fetchObjectCore() {
        return baseTable.fetchObjectCore();
    }


    /**
     * key为层次
     *
     * @return
     */
    @Override
    public Map<Integer, Set<CubeTableSource>> createGenerateTablesMap() {
        return baseTable.createGenerateTablesMap();
    }

    /**
     * key为层次
     *
     * @return
     */
    @Override
    public List<Set<CubeTableSource>> createGenerateTablesList() {
        return baseTable.createGenerateTablesList();
    }

    /**
     * 写简单索引
     *
     * @param travel
     * @param field
     * @param loader
     * @return
     */
    @Override
    public long read(Traversal<BIDataValue> travel, ICubeFieldSource[] field, ICubeDataLoader loader) {
        return 0;
    }

    @Override
    public IPersistentTable getPersistentTable() {
        if (dbTable == null) {
            IPersistentTable ptable = baseTable.getPersistentTable();
            dbTable = createBITable();
            for (PersistentField column : ptable.getFieldList()) {
                dbTable.addColumn(column);
            }
        }
        return dbTable;
    }

    @Override
    public UserCubeTableSource createUserTableSource(long userId) {
        UserCubeTableSource source = userBaseTableMap.get(userId);
        if (source == null) {
            synchronized (userBaseTableMap) {
                UserCubeTableSource tmp = userBaseTableMap.get(userId);
                if (tmp == null) {
                    source = createNewUserSource(userId);
                    userBaseTableMap.put(userId, source);
                } else {
                    source = tmp;
                }
            }
        }
        return source;
    }

    public abstract UserCubeTableSource createNewUserSource(long userId);
}
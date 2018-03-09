package com.fr.swift.adaptor.update;

import com.finebi.base.constant.FineEngineType;
import com.finebi.conf.internalimp.bean.table.UpdatePreviewTableBean;
import com.finebi.conf.internalimp.bean.update.UpdatePreview;
import com.finebi.conf.internalimp.response.update.TableUpdateSetting;
import com.finebi.conf.internalimp.update.GlobalUpdateInfo;
import com.finebi.conf.internalimp.update.GlobalUpdateLog;
import com.finebi.conf.internalimp.update.GlobalUpdateSetting;
import com.finebi.conf.internalimp.update.TableUpdateInfo;
import com.finebi.conf.internalimp.update.UpdateLog;
import com.finebi.conf.internalimp.update.UpdateNeedSpace;
import com.finebi.conf.internalimp.update.UpdateStatus;
import com.finebi.conf.provider.SwiftTableConfProvider;
import com.finebi.conf.service.engine.update.EngineUpdateManager;
import com.finebi.conf.structure.bean.table.FineBusinessTable;
import com.fr.swift.adaptor.preview.SwiftDataPreviewer;
import com.fr.swift.adaptor.transformer.IndexingDataSourceFactory;
import com.fr.swift.increment.Increment;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.manager.ProviderManager;
import com.fr.swift.provider.IndexStuffInfoProvider;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.source.SwiftSourceTransfer;
import com.fr.swift.source.container.SourceContainerManager;
import com.fr.swift.source.db.QueryDBSource;
import com.fr.swift.source.manager.IndexStuffProvider;
import com.fr.swift.stuff.HistoryIndexStuffImpl;
import com.fr.swift.stuff.IndexingStuff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class created on 2018-1-12 14:17:13
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI Analysis 1.0
 */
public class SwiftUpdateManager implements EngineUpdateManager {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(SwiftUpdateManager.class);

    @Override
    public Map<FineBusinessTable, TableUpdateInfo> getTableUpdateInfo() {
        return null;
    }

    @Override
    public List<TableUpdateInfo> getTableUpdateInfo(FineBusinessTable table) {
        TableUpdateInfo tableUpdateInfo = new TableUpdateInfo();
        tableUpdateInfo.setTableName(table.getName());
        List<TableUpdateInfo> tableUpdateInfoList = new ArrayList<TableUpdateInfo>();
        tableUpdateInfoList.add(tableUpdateInfo);
        return tableUpdateInfoList;
    }

    @Override
    public TableUpdateInfo getPackageUpdateInfo(String packageId) {
        return null;
    }

    @Override
    public void saveUpdateSetting(TableUpdateInfo updateInfo, FineBusinessTable table) throws Exception {
        Map<FineBusinessTable, TableUpdateInfo> infoMap = new HashMap<FineBusinessTable, TableUpdateInfo>();
        infoMap.put(table, updateInfo);
        saveUpdateSetting(infoMap);
    }

    @Override
    public void triggerTableUpdate(TableUpdateInfo updateInfo, FineBusinessTable table) {

    }

    @Override
    public void saveUpdateSetting(Map<FineBusinessTable, TableUpdateInfo> infoMap) throws Exception {
        if (infoMap == null) {
        }
        List<String> updateTableSourceKeys = new ArrayList<String>();
        List<String> updateRelationSourceKeys = new ArrayList<String>();
        List<String> updatePathSourceKeys = new ArrayList<String>();
        SourceContainerManager updateSourceContainer = new SourceContainerManager();

        Map<String, List<Increment>> incrementMap = new HashMap<String, List<Increment>>();
        IndexingDataSourceFactory.transformDataSources(infoMap, updateTableSourceKeys, updateSourceContainer, incrementMap);

        IndexingStuff indexingStuff = new HistoryIndexStuffImpl(updateTableSourceKeys, updateRelationSourceKeys, updatePathSourceKeys);
        IndexStuffProvider indexStuffProvider = new IndexStuffInfoProvider(indexingStuff, updateSourceContainer, incrementMap);
        ProviderManager.getManager().registProvider(0, indexStuffProvider);
    }

    @Override
    public void saveTableUpdateSetting(TableUpdateSetting tableUpdateSetting) throws Exception {
        FineBusinessTable fineBusinessTable = new SwiftTableConfProvider().getSingleTable(tableUpdateSetting.getTableName());
        Map<FineBusinessTable, TableUpdateInfo> infoMap = new HashMap<FineBusinessTable, TableUpdateInfo>();
        infoMap.put(fineBusinessTable, tableUpdateSetting.getSettings().get(tableUpdateSetting.getTableName()));
        this.saveUpdateSetting(infoMap);
    }

    @Override
    public void savePackageUpdateSetting(String packId, TableUpdateInfo info) {
    }

    @Override
    public void triggerPackageUpdate(String packId) {

    }

    @Override
    public Map<String, UpdateStatus> getTableUpdateStatus(FineBusinessTable table) {
        return null;
    }

    @Override
    public UpdateStatus getTableUpdateState(String tableName) {
        return null;
    }

    @Override
    public UpdateStatus getPackUpdateStatus(String packId) {
        return null;
    }

    @Override
    public List<UpdateLog> getTableUpdateLog(FineBusinessTable table) {
        return null;
    }


    @Override
    public void triggerAllUpdate(TableUpdateInfo info) {

    }

    @Override
    public GlobalUpdateSetting getUpdateInfo() {
        return null;
    }

    @Override
    public void updateAll(GlobalUpdateSetting info) {

    }

    @Override
    public GlobalUpdateInfo checkGlobalUpdateInfo() {
        return null;
    }

    @Override
    public GlobalUpdateLog getGlobalUpdateLog() {
        return null;
    }

    @Override
    public UpdateNeedSpace getUpdateNeedSpace() {
        return null;
    }

    @Override
    public boolean shouldUpdate() {
        return false;
    }

    @Override
    public FineEngineType getEngineType() {
        return FineEngineType.Cube;
    }

    @Override
    public UpdatePreview getUpdatePreview(UpdatePreviewTableBean updatePreviewTableBean) {
        try {
            DataSource queryDBSource = new QueryDBSource(updatePreviewTableBean.getSql(), updatePreviewTableBean.getConnectionName());
            SwiftSourceTransfer transfer = SwiftDataPreviewer.createPreviewTransfer(queryDBSource, 100);
            SwiftResultSet resultSet = transfer.createResultSet();
            Object[] data = new Object[100];
            Object[] fieldNames = new Object[queryDBSource.getMetadata().getColumnCount()];
            for (int i = 1; i <= queryDBSource.getMetadata().getColumnCount(); i++) {
                fieldNames[i - 1] = queryDBSource.getMetadata().getColumnName(i);
            }
            int count = 0;
            while (resultSet.next()) {
                Row row = resultSet.getRowData();
                List<Object> rowList = new ArrayList<Object>();
                for (int i = 1; i <= queryDBSource.getMetadata().getColumnCount(); i++) {
                    rowList.add(row.getValue(i - 1));
                }
                data[count] = rowList;
                count++;
            }

            UpdatePreview updatePreview = new UpdatePreview(updatePreviewTableBean.getSql(), data, fieldNames);
            return updatePreview;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new UpdatePreview();
        }
    }
}
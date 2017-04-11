package com.finebi.cube.tools;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.base.TableData;
import com.fr.bi.base.BICore;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.db.IPersistentTable;
import com.fr.bi.stable.data.db.PersistentTable;
import com.fr.bi.stable.data.source.AbstractCubeTableSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.data.source.SourceFile;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLableReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kary on 16/5/17.
 */
public class BINationDataSource extends AbstractCubeTableSource{
    public String sourceID;
    public List<ICubeFieldSource> fieldList;
    public int rowCount;
    public Map<Integer, List> contents;

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public void setFieldList(List<ICubeFieldSource> fieldList) {
        this.fieldList = fieldList;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public void setContents(Map<Integer, List> contents) {
        this.contents = contents;
    }

    @Override
    public IPersistentTable getPersistentTable() {
        return  new PersistentTable(StringUtils.EMPTY, getSourceID(), StringUtils.EMPTY);
    }

    @Override
    public String getSourceID() {
        return sourceID;
    }

    @Override
    public ICubeFieldSource[] getFieldsArray(Set<CubeTableSource> sources) {
        ICubeFieldSource[] result = new ICubeFieldSource[fieldList.size()];
        for (int i = 0; i < fieldList.size(); i++) {
            result[i] = fieldList.get(i);
        }
        return result;
    }


    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public long read(Traversal<BIDataValue> travel, ICubeFieldSource[] field, ICubeDataLoader loader) {
        for (int i = 0; i < rowCount; i++) {
            Iterator<Map.Entry<Integer, List>> it = contents.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, List> entry = it.next();
                Integer columnNumber = entry.getKey();
                List value = entry.getValue();
                travel.actionPerformed(new BIDataValue(i, columnNumber, value.get(i)));
            }
        }
        return rowCount;
    }

    @Override
    public long read4Part(Traversal<BIDataValue> travel, ICubeFieldSource[] field, ICubeDataLoader loader, int start, int end) {
        return 0;
    }

    @Override
    public long read4Part(Traversal<BIDataValue> traversal, ICubeFieldSource[] cubeFieldSources, String sql, long oldRowCount) {
        for (int i = 0; i < rowCount; i++) {
            Iterator<Map.Entry<Integer, List>> it = contents.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, List> entry = it.next();
                Integer columnNumber = entry.getKey();
                List value = entry.getValue();
                traversal.actionPerformed(new BIDataValue((int) (i+oldRowCount), columnNumber, value.get(i)));
            }
        }
        return rowCount+oldRowCount;

    }

    @Override
    public Set getFieldDistinctNewestValues(String fieldName, ICubeDataLoader loader, long userId) {
        return null;
    }

    @Override
    public Set getFieldDistinctValuesFromCube(String fieldName, ICubeDataLoader loader, long userId) {
        return null;
    }

    @Override
    public JSONObject createPreviewJSON(ArrayList<String> fields, ICubeDataLoader loader, long userId) throws Exception {
        return null;
    }

    @Override
    public TableData createTableData(List<String> fields, ICubeDataLoader loader, long userId) throws Exception {
        return null;
    }

    @Override
    public JSONObject createPreviewJSONFromCube(ArrayList<String> fields, ICubeDataLoader loader) throws Exception {
        return null;
    }

    @Override
    public SourceFile getSourceFile() {
        return null;
    }


    @Override
    public boolean needGenerateIndex() {
        return false;
    }

    @Override
    public Map<BICore, CubeTableSource> createSourceMap() {
        return null;
    }

    @Override
    public Set<String> getUsedFields(CubeTableSource source) {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public String getModuleName() {
        return BIBaseConstant.MODULE_NAME.CORE_MODULE;
    }

    @Override
    public BICore fetchObjectCore() {
        return null;
    }

    @Override
    public JSONObject createJSON() throws Exception {
        return null;
    }

    @Override
    public void readXML(XMLableReader xmLableReader) {

    }

    @Override
    public void writeXML(XMLPrintWriter xmlPrintWriter) {

    }

    @Override
    public Object clone()  {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BIMemoryDataSource)) {
            return false;
        }

        BIMemoryDataSource that = (BIMemoryDataSource) o;

        return !(sourceID != null ? !sourceID.equals(that.sourceID) : that.sourceID != null);

    }

    @Override
    public int hashCode() {
        return sourceID != null ? sourceID.hashCode() : 0;
    }



}

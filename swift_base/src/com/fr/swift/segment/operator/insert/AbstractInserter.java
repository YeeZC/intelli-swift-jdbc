package com.fr.swift.segment.operator.insert;

import com.fr.swift.bitmap.BitMaps;
import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.bitmap.MutableBitMap;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.column.Column;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.segment.operator.utils.InserterUtils;
import com.fr.swift.source.ColumnTypeConstants;
import com.fr.swift.source.ColumnTypeUtils;
import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.source.SwiftResultSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class created on 2018/3/26
 *
 * @author Lucifer
 * @description 指定数据和块insert，块必须是新块。
 * todo 支持断点输入流式数据，也支持实时导入数据、实时查询数据功能。
 * @since Advanced FineBI Analysis 1.0
 */
public abstract class AbstractInserter implements Inserter {

    protected Segment segment;
    protected List<String> fields;

    protected Map<String, MutableBitMap> nullMap = new ConcurrentHashMap<String, MutableBitMap>();
    private int row = 0;
    private ImmutableBitMap allShowIndex;
    private Map<String, Column> columnMap = new HashMap<String, Column>();
    private Map<String, ColumnTypeConstants.ClassType> classTypeMap = new HashMap<String, ColumnTypeConstants.ClassType>();


    public AbstractInserter(Segment segment) throws Exception {
        this(segment, segment.getMetaData().getFieldNames());
    }

    /**
     * todo 目前row、allshowindex、nullmap都不支持断点
     *
     * @param segment
     * @param fields
     * @throws Exception
     */
    public AbstractInserter(Segment segment, List<String> fields) throws Exception {
        this.fields = fields;
        this.segment = segment;
        SwiftMetaData metaData = segment.getMetaData();
        for (int i = 0; i < fields.size(); i++) {
            SwiftMetaDataColumn metaDataColumn = metaData.getColumn(fields.get(i));
            ColumnTypeConstants.ClassType clazz = ColumnTypeUtils.getClassType(metaDataColumn);
            ColumnKey columnKey = new ColumnKey(metaDataColumn.getName());
            Column column = segment.getColumn(columnKey);
            columnMap.put(fields.get(i), column);
            classTypeMap.put(fields.get(i), clazz);
            nullMap.put(metaDataColumn.getName(), BitMaps.newRoaringMutable());
        }
    }

    @Override
    public boolean insertData(List<Row> rowList) {
        allShowIndex = BitMaps.newAllShowBitMap(rowList.size());
        for (Row rowData : rowList) {
            for (int i = 0; i < fields.size(); i++) {
                Column column = columnMap.get(fields.get(i));
                if (null != column) {
                    ColumnTypeConstants.ClassType type = classTypeMap.get(fields.get(i));
                    Object value = rowData.getValue(i);
                    if (InserterUtils.isBusinessNullValue(value)) {
                        column.getDetailColumn().put(row, InserterUtils.getNullValue(type));
                        InserterUtils.setNullIndex(fields.get(i), row, nullMap);
                    } else {
                        column.getDetailColumn().put(row, value);
                    }
                }
            }
            row++;
        }
        segment.putRowCount(row);
        segment.putAllShowIndex(allShowIndex);
        release();
        return true;
    }

    @Override
    public boolean insertData(SwiftResultSet swiftResultSet) throws Exception {
        try {
            while (swiftResultSet.next()) {
                Row rowData = swiftResultSet.getRowData();
                for (int i = 0; i < fields.size(); i++) {
                    Column column = columnMap.get(fields.get(i));
                    if (null != column) {
                        ColumnTypeConstants.ClassType type = classTypeMap.get(fields.get(i));
                        Object value = rowData.getValue(i);
                        if (InserterUtils.isBusinessNullValue(value)) {
                            column.getDetailColumn().put(row, InserterUtils.getNullValue(type));
                            InserterUtils.setNullIndex(fields.get(i), row, nullMap);
                        } else {
                            column.getDetailColumn().put(row, value);
                        }
                    }
                }
                row++;
            }
        } finally {
            swiftResultSet.close();
        }

        allShowIndex = BitMaps.newAllShowBitMap(row);
        segment.putRowCount(row);
        segment.putAllShowIndex(allShowIndex);
        release();
        return true;
    }

    public abstract void release();

    @Override
    public List<String> getFields() {
        return fields;
    }

}

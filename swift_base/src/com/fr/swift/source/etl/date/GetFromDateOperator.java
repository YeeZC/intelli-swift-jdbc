package com.fr.swift.source.etl.date;

import com.fr.swift.source.*;
import com.fr.swift.source.core.CoreField;
import com.fr.swift.source.core.MD5Utils;
import com.fr.swift.source.etl.AbstractOperator;
import com.fr.swift.source.etl.OperatorType;
import com.fr.swift.source.ColumnTypeConstants.ColumnType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Handsome on 2018/3/2 0002 17:22
 */
public class GetFromDateOperator extends AbstractOperator {

    @CoreField
    private String field;
    @CoreField
    private int type;
    @CoreField
    private String columnName;//新增列名
    @CoreField
    private ColumnType columnType;

    public GetFromDateOperator(String field, int type, String columnName, ColumnType columnType) {
        this.field = field;
        this.type = type;
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getField() {
        return field;
    }

    public int getType() {
        return type;
    }

    public String getColumnName() {
        return columnName;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    @Override
    public List<SwiftMetaDataColumn> getColumns(SwiftMetaData[] metaDatas) {
        List<SwiftMetaDataColumn> columnList = new ArrayList<SwiftMetaDataColumn>();
        columnList.add(new MetaDataColumn(MD5Utils.getMD5String(new String[]{(this.columnName)}), this.columnName,
                ColumnTypeUtils.columnTypeToSqlType(this.columnType), MD5Utils.getMD5String(new String[]{(this.columnName)})));
        return columnList;
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.GETDATE;
    }

    public String getColumnMD5() {
        return MD5Utils.getMD5String(new String[]{(this.columnName)});
    }
}

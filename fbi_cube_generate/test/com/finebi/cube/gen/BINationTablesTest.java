package com.finebi.cube.gen;

import com.finebi.cube.BICubeTestBase;
import com.finebi.cube.gen.oper.BIFieldIndexGenerator;
import com.finebi.cube.gen.oper.BIRelationIndexGenerator;
import com.finebi.cube.gen.oper.BISourceDataTransport;
import com.finebi.cube.structure.BICubeRelation;
import com.finebi.cube.structure.BICubeTablePath;
import com.finebi.cube.structure.ICubeRelationEntityGetterService;
import com.finebi.cube.structure.column.BIColumnKey;
import com.finebi.cube.structure.column.ICubeColumnReaderService;
import com.finebi.cube.tools.BINationDataFactory;
import com.finebi.cube.utils.BITableKeyUtils;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.db.DBField;
import com.fr.bi.stable.data.source.ITableSource;
import com.fr.bi.stable.exception.BITablePathConfusionException;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.RoaringGroupValueIndex;
import com.fr.bi.stable.gvi.traversal.SingleRowTraversalAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wuk on 16/5/17.
 */
public class BINationTablesTest extends BICubeTestBase {
    private BISourceDataTransport dataTransport;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testFieldPathIndex() {
        try {

            /*写入表信息*/
            transport(BINationDataFactory.createTableNation());
            transport(BINationDataFactory.createTablePerson());
            //生成自身索引
            fieldIndexGenerator(BINationDataFactory.createTableNation(), 0);
            fieldIndexGenerator(BINationDataFactory.createTablePerson(), 1);
            fieldIndexGenerator(BINationDataFactory.createTablePerson(), 2);
            //生成依赖关系
            BIRelationIndexGenerator indexGenerator = new BIRelationIndexGenerator(cube, generatePersonsAndNationsRelation());
            indexGenerator.mainTask(null);


            //relations集合,当relations.siez>2时使用
            BICubeTablePath biCubeTablePath=getAllRelations();

            //测试relation
            ICubeRelationEntityGetterService iCubeRelationEntityGetterService = cube.getCubeRelation(BITableKeyUtils.convert(BINationDataFactory.createTablePerson()), biCubeTablePath);
            assertEquals(iCubeRelationEntityGetterService.getBitmapIndex(0), RoaringGroupValueIndex.createGroupValueIndex(new Integer[]{0}));
            assertEquals(iCubeRelationEntityGetterService.getNULLIndex(0), RoaringGroupValueIndex.createGroupValueIndex(new Integer[]{}));

            //根据value查找索引
            final ICubeColumnReaderService iCubeColumnReaderService = cube.getCubeColumn(BITableKeyUtils.convert(BINationDataFactory.createTablePerson()), BIColumnKey.covertColumnKey(new DBField("person", "name", DBConstant.CLASS.STRING, 255)));

            //获取本表对应位置索引值
            assertEquals(iCubeColumnReaderService.getIndexByGroupValue("nameA"),RoaringGroupValueIndex.createGroupValueIndex(new Integer[]{0,2}));
            //根据行号(rowId来查询value
            assertEquals(iCubeColumnReaderService.getOriginalValueByRow(1),"nameB");


            //select id from persons where name='nameA'
            GroupValueIndex indexByGroupValue = iCubeColumnReaderService.getIndexByGroupValue("nameA");
            final List<Integer> ids = new ArrayList<Integer>();
            indexByGroupValue.Traversal(new SingleRowTraversalAction() {
                @Override
                public void actionPerformed(int row) {
                    ids.add(row);
                }
            });
            assertEquals(ids.toArray(), new int[]{0, 2});


            //select name from persons where id in (0,1)
            final List<String> idList = new ArrayList<String>();
            idList.add((String) iCubeColumnReaderService.getOriginalValueByRow(0));
            idList.add((String) iCubeColumnReaderService.getOriginalValueByRow(1));
            assertEquals(idList.toArray(), new String[]{"nameA", "nameB"});


////按流程从上到下走一遍
////            ITableSource iTableSource=new ITableSource();
//            ITableKey iTableKey=new ITableKey() {
//                @Override
//                public String getSourceID() {
//                    return null;
//                }
//            };
//            tableEntity = (BICubeTableEntity) cube.getCubeTableWriter();
//            Set<BIColumnKey> cubeColumnInfo = tableEntity.getCubeColumnInfo();
//            for (BIColumnKey biColumnKey : cubeColumnInfo) {
//                    biColumnKey.getColumnName();
//            }


        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }


    /**写入*/
    public void transport(ITableSource tableSource) {
        try {
            dataTransport = new BISourceDataTransport(cube, tableSource, new HashSet<ITableSource>(), new HashSet<ITableSource>());
            dataTransport.mainTask(null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    /**生成索引*/
    public void fieldIndexGenerator(ITableSource tableSource, int columnIndex) {
        try {
            setUp();
            BISourceDataTransportTest transportTest = new BISourceDataTransportTest();
            transportTest.transport(tableSource);
            DBField field = tableSource.getFieldsArray(null)[columnIndex];
            Iterator<BIColumnKey> columnKeyIterator = BIColumnKey.generateColumnKey(field).iterator();
            while (columnKeyIterator.hasNext()) {
                BIColumnKey columnKey = columnKeyIterator.next();
                BIFieldIndexGenerator fieldIndexGenerator = new BIFieldIndexGenerator(cube, tableSource, tableSource.getFieldsArray(null)[columnIndex], columnKey);
                fieldIndexGenerator.mainTask(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    /**生成relations的集合*/
    protected BICubeTablePath getAllRelations() throws BITablePathConfusionException {
        BICubeTablePath path = new BICubeTablePath();

        path.addRelationAtHead(generatePersonsAndNationsRelation());
        return path;
    }
    /**生成relation*/
    protected BICubeRelation generatePersonsAndNationsRelation() throws BITablePathConfusionException {
        ITableSource persons;
        ITableSource nations;
        nations = BINationDataFactory.createTableNation();
        persons = BINationDataFactory.createTablePerson();
        BICubeRelation biCubeRelation = new BICubeRelation(
                BIColumnKey.covertColumnKey(new DBField(persons.getSourceID(), "nationId", DBConstant.CLASS.LONG, 255)),
                BIColumnKey.covertColumnKey(new DBField(nations.getSourceID(), "id", DBConstant.CLASS.LONG,255)),
                BITableKeyUtils.convert(persons),
                BITableKeyUtils.convert(nations));
        return biCubeRelation;
    }


}

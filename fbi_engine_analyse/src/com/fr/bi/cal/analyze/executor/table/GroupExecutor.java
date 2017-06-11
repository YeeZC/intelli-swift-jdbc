package com.fr.bi.cal.analyze.executor.table;

import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.base.Style;
import com.fr.bi.base.FinalInt;
import com.fr.bi.cal.analyze.cal.index.loader.CubeIndexLoader;
import com.fr.bi.cal.analyze.cal.index.loader.MetricGroupInfo;
import com.fr.bi.cal.analyze.cal.result.CrossExpander;
import com.fr.bi.cal.analyze.cal.result.Node;
import com.fr.bi.cal.analyze.cal.result.NodeAndPageInfo;
import com.fr.bi.cal.analyze.executor.iterator.StreamPagedIterator;
import com.fr.bi.cal.analyze.executor.iterator.TableCellIterator;
import com.fr.bi.cal.analyze.executor.paging.Paging;
import com.fr.bi.cal.analyze.executor.utils.ExecutorUtils;
import com.fr.bi.cal.analyze.report.report.widget.TableWidget;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.cal.report.engine.CBCell;
import com.fr.bi.conf.report.style.BITableStyle;
import com.fr.bi.conf.report.style.DetailChartSetting;
import com.fr.bi.conf.report.widget.field.dimension.BIDimension;
import com.fr.bi.field.target.target.BISummaryTarget;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.report.key.TargetGettingKey;
import com.fr.general.ComparatorUtils;
import com.fr.general.DateUtils;
import com.fr.general.GeneralUtils;
import com.fr.general.Inter;
import com.fr.json.JSONObject;
import com.fr.stable.ExportConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 小灰灰 on 2015/6/30.
 */
public class GroupExecutor extends AbstractTableWidgetExecutor<Node> {

    private Rectangle rectangle;

    private BIDimension[] usedDimensions;

    private CrossExpander expander;

    public GroupExecutor(TableWidget widget, Paging paging, BISession session, CrossExpander expander) {

        super(widget, paging, session);
        usedDimensions = widget.getViewDimensions();
        this.expander = expander;
    }

    public TableCellIterator createCellIterator4Excel() throws Exception {

        final Node tree = getCubeNode();
        if (tree == null) {
            return new TableCellIterator(0, 0);
        }
        int rowLength = usedDimensions.length;
        int summaryLength = usedSumTarget.length;
        int columnLen = rowLength + summaryLength;
        DetailChartSetting chartSetting = widget.getChartSetting();
        TargetGettingKey[] keys = new TargetGettingKey[summaryLength];
        for (int i = 0; i < summaryLength; i++) {
            keys[i] = usedSumTarget[i].createTargetGettingKey();
        }
        //显示不显示汇总行
        int rowLen = chartSetting.showRowTotal() ? tree.getTotalLengthWithSummary() : tree.getTotalLength();
        //        final boolean useTargetSort = widget.useTargetSort() || BITargetAndDimensionUtils.isTargetSort(usedDimensions);
        rectangle = new Rectangle(rowLength + widget.isOrder(), 1, columnLen + widget.isOrder() - 1, rowLen);
        final TableCellIterator iter = new TableCellIterator(columnLen + widget.isOrder(), rowLen + 1);
        new Thread() {

            public void run() {

                try {
                    FinalInt start = new FinalInt();
                    generateTitle(widget, usedDimensions, usedSumTarget, iter.getIteratorByPage(start.value));
                    generateCells(tree, widget, widget.getViewDimensions(), iter, start, new FinalInt());
                } catch (Exception e) {
                    BILoggerFactory.getLogger().error(e.getMessage(), e);
                } finally {
                    iter.finish();
                }
            }
        }.start();
        return iter;
    }

    /**
     * @param widget         ComplexGroupExecutor复用时需要的参数
     * @param usedDimensions ComplexGroupExecutor复用时需要的参数
     * @param usedSumTarget  ComplexGroupExecutor复用时需要的参数
     * @param pagedIterator
     * @throws Exception
     */
    public static void generateTitle(TableWidget widget, BIDimension[] usedDimensions, BISummaryTarget[] usedSumTarget, StreamPagedIterator pagedIterator) throws Exception {
        Style style = BITableStyle.getInstance().getTitleDimensionCellStyle(0);
        if (widget.isOrder() != 0) {
            CBCell cell = ExecutorUtils.createCell(Inter.getLocText("BI-Number_Index"), 0, 1, 0, 1, style);
            pagedIterator.addCell(cell);
        }
        int columnIdx = widget.isOrder();
        for (int i = 0; i < usedDimensions.length; i++) {
            CBCell cell = ExecutorUtils.createCell(usedDimensions[i].getText(), 0, 1, columnIdx++, 1, style);
            //            List<CBCell> cellList = new ArrayList<CBCell>();
            //            cellList.add(cell);
            //            CBBoxElement cbox = new CBBoxElement(cellList);
            //            BITarget rowCol = usedSumTarget[i];
            //            cbox.setName(rowCol.getValue());
            //            cbox.setType(CellConstant.CBCELL.TARGETTITLE_Y);
            //            cbox.setSortTargetName(rowCol.getValue());
            //            cbox.setSortTargetValue("[]");
            //            if (widget.getTargetSort() != null && ComparatorUtils.equals(widget.getTargetSort().getName(),
            //                    usedSumTarget[i].getValue())) {
            //                cbox.setSortType((Integer) widget.getTargetSort().getObject());
            //            } else {
            //                cbox.setSortType(BIReportConstant.SORT.NONE);
            //            }
            //            cell.setBoxElement(cbox);
            pagedIterator.addCell(cell);
        }

        for (int i = 0; i < usedSumTarget.length; i++) {
            CBCell cell = ExecutorUtils.createCell(usedSumTarget[i].getText(), 0, 1, columnIdx++, 1, style);
            pagedIterator.addCell(cell);
        }
    }

    /**
     * @param n             ComplexGroupExecutor复用时需要的参数
     * @param widget        ComplexGroupExecutor复用时需要的参数
     * @param rowDimensions ComplexGroupExecutor复用时需要的参数
     * @param iter
     * @param start         ComplexGroupExecutor复用时需要的参数
     * @param rowIdx        ComplexGroupExecutor复用时需要的参数
     */
    public static void generateCells(Node n, TableWidget widget, BIDimension[] rowDimensions, TableCellIterator iter, FinalInt start, FinalInt rowIdx) {
        while (n.getFirstChild() != null) {
            n = n.getFirstChild();
        }
        int[] oddEven = new int[rowDimensions.length];
        //需要根据行表头的个数来确定会总行的columnIndex
        int rowDimLength = rowDimensions.length;
        Object[] dimensionNames = new Object[rowDimensions.length];
        while (n != null) {
            Node temp = n;
            rowIdx.value++;
            int newRow = rowIdx.value & ExportConstants.MAX_ROWS_2007 - 1;
            if (newRow == 0) {
                iter.getIteratorByPage(start.value).finish();
                start.value++;
            }
            StreamPagedIterator pagedIterator = iter.getIteratorByPage(start.value);
            generateTargetCells(temp, widget, rowDimensions, pagedIterator, rowIdx.value, false);
            generateDimNames(temp, widget, rowDimensions, dimensionNames, oddEven, pagedIterator, rowIdx.value);
            generateSumCells(temp, widget, rowDimensions, pagedIterator, rowIdx, rowDimLength - 1);
            n = n.getSibling();
        }
    }

    private static void generateSumCells(Node temp, TableWidget widget, BIDimension[] rowDimensions, StreamPagedIterator pagedIterator, FinalInt rowIdx, int columnIdx) {
        //isLastSum 是否是最后一行会总行
        if ((widget.getViewTargets().length != 0) && checkIfGenerateSumCell(temp)) {
            if (temp.getParent().getChildLength() != 1) {
                Style style = BITableStyle.getInstance().getYSumStringCellStyle();
                rowIdx.value++;
                if (widget.isOrder() == 1 && temp.getSibling() == null) {
                    CBCell cell = ExecutorUtils.createCell(Inter.getLocText("BI-Summary_Values"), rowIdx.value, 1, 0, 1, style);
                    pagedIterator.addCell(cell);
                }
                CBCell cell = ExecutorUtils.createCell(Inter.getLocText("BI-Summary_Values"), rowIdx.value, 1, columnIdx + widget.isOrder(), rowDimensions.length - columnIdx, style);
                pagedIterator.addCell(cell);
                generateTargetCells(temp.getParent(), widget, rowDimensions, pagedIterator, rowIdx.value, true);
            }
            //开辟新内存，不对temp进行修改
            Node parent = temp.getParent();
            generateSumCells(parent, widget, rowDimensions, pagedIterator, rowIdx, columnIdx - 1);
        }
    }

    private static void generateTargetCells(Node temp, TableWidget widget, BIDimension[] rowDimensions, StreamPagedIterator pagedIterator, int rowIdx, boolean isSum) {
        int targetsKeyIndex = 0;
        for (TargetGettingKey key : widget.getTargetsKey()) {
            int columnIdx = targetsKeyIndex + rowDimensions.length + widget.isOrder();
            Object data = temp.getSummaryValue(key);
            Style style;
            boolean isPercent = ComparatorUtils.equals(widget.getChartSetting().getNumberLevelByTargetId(key.getTargetName()), BIReportConstant.TARGET_STYLE.NUM_LEVEL.PERCENT);
            if (!isSum) {
                style = BITableStyle.getInstance().getNumberCellStyle(data, rowIdx % 2 == 1, isPercent);
            } else {
                style = BITableStyle.getInstance().getYTotalCellStyle(data, 1, isPercent);
            }
            CBCell cell = ExecutorUtils.createCell(data, rowIdx, 1, columnIdx, 1, style);
            pagedIterator.addCell(cell);
            targetsKeyIndex++;
        }
    }

    private static void generateDimNames(Node temp, TableWidget widget, BIDimension[] rowDimensions, Object[] dimensionNames, int[] oddEven,
                                         StreamPagedIterator pagedIterator, int rowIdx) {
        //维度第一次出现即addCell
        int i = rowDimensions.length;
        while (temp.getParent() != null) {
            BIDimension dim = rowDimensions[--i];
            String data = dim.toString(temp.getData());
            //年月日字段格式化
            if (dim.getGroup().getType() == BIReportConstant.GROUP.YMD && GeneralUtils.string2Number(data) != null) {
                data = DateUtils.DATEFORMAT2.format(new Date(GeneralUtils.string2Number(data).longValue()));
            }
            Object v = dim.getValueByType(data);
            if (v != dimensionNames[i] || (i == rowDimensions.length - 1)) {
                int rowSpan = widget.getViewTargets().length != 0 ? temp.getTotalLengthWithSummary() : temp.getTotalLength();
                oddEven[i]++;
                Style style = BITableStyle.getInstance().getDimensionCellStyle(v instanceof Number, rowIdx % 2 == 1);
                CBCell cell = ExecutorUtils.createCell(v, rowIdx, rowSpan, i + widget.isOrder(), 1, style);
                pagedIterator.addCell(cell);
                if (i == 0 && widget.isOrder() == 1) {
                    CBCell orderCell = ExecutorUtils.createCell(oddEven[0], rowIdx, rowSpan, 0, 1, style);
                    pagedIterator.addCell(orderCell);
                }
                dimensionNames[i] = v;
            }
            temp = temp.getParent();
        }
    }

    @Override
    public Node getCubeNode() throws Exception {
        if (session == null) {
            return null;
        }
        int rowLength = usedDimensions.length;
        int summaryLength = usedSumTarget.length;
        int columnLen = rowLength + summaryLength;
        if (columnLen == 0) {
            return null;
        }
        long start = System.currentTimeMillis();
        int calpage = paging.getOperator();
        CubeIndexLoader cubeIndexLoader = CubeIndexLoader.getInstance(session.getUserId());
        Node tree = cubeIndexLoader.loadPageGroup(false, widget, createTarget4Calculate(), usedDimensions,
                allDimensions, allSumTarget, calpage, widget.isRealData(), session, expander.getYExpander());
        if (tree == null) {
            tree = new Node(allSumTarget.length);
        }
        BILoggerFactory.getLogger().info(DateUtils.timeCostFrom(start) + ": cal time");
        return tree;
    }

    @Override
    public JSONObject createJSONObject() throws Exception {
        return getCubeNode().toJSONObject(usedDimensions, widget.getTargetsKey(), -1);
    }

    @Override
    public List<MetricGroupInfo> getLinkedWidgetFilterGVIList() throws Exception {
        if (session == null) {
            return null;
        }
        int rowLength = usedDimensions.length;
        int summaryLength = usedSumTarget.length;
        int columnLen = rowLength + summaryLength;
        if (columnLen == 0) {
            return null;
        }
        int calPage = paging.getOperator();
        CubeIndexLoader cubeIndexLoader = CubeIndexLoader.getInstance(session.getUserId());
        List<NodeAndPageInfo> infoList = cubeIndexLoader.getPageGroupInfoList(false, widget, createTarget4Calculate(), usedDimensions,
                allDimensions, allSumTarget, calPage, widget.isRealData(), session, expander.getYExpander());
        ArrayList<MetricGroupInfo> gviList = new ArrayList<MetricGroupInfo>();
        for (NodeAndPageInfo info : infoList) {
            gviList.addAll(info.getIterator().getRoot().getMetricGroupInfoList());
        }
        return gviList;
    }

    public Node getStopOnRowNode(Object[] stopRow) throws Exception {
        if (session == null) {
            return null;
        }
        int rowLength = usedDimensions.length;
        int summaryLength = usedSumTarget.length;
        int columnLen = rowLength + summaryLength;
        if (columnLen == 0) {
            return null;
        }
        int calPage = paging.getOperator();
        CubeIndexLoader cubeIndexLoader = CubeIndexLoader.getInstance(session.getUserId());
        String oldName = widget.getWidgetName();
        widget.setWidgetName(getRandWidgetName());
        //cubeIndexLoader.getGroupNodeWidthGvi(widget,);
        Node n = cubeIndexLoader.getStopWhenGetRowNode(stopRow, widget, createTarget4Calculate(), usedDimensions,
                allDimensions, allSumTarget, calPage, session, CrossExpander.ALL_EXPANDER.getYExpander());
        // TODO 这一步是否有必要
        widget.setWidgetName(oldName);
        return n;
    }

    private String getRandWidgetName() {

        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public Rectangle getSouthEastRectangle() {

        return rectangle;
    }

}
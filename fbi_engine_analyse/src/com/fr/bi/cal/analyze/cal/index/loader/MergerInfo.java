package com.fr.bi.cal.analyze.cal.index.loader;import com.finebi.cube.conf.table.BusinessTable;import com.finebi.cube.relation.BITableSourceRelation;import com.fr.bi.cal.analyze.cal.result.NodeExpander;import com.fr.bi.cal.analyze.cal.sssecret.NoneDimensionGroup;import com.fr.bi.cal.analyze.cal.sssecret.RootDimensionGroup;import com.fr.bi.cal.analyze.cal.sssecret.TreeNoneDimensionGroup;import com.fr.bi.cal.analyze.cal.sssecret.TreeRootDimensionGroup;import com.fr.bi.cal.analyze.cal.sssecret.sort.SortedRootDimensionGroup;import com.fr.bi.cal.analyze.cal.sssecret.sort.SortedTree;import com.fr.bi.cal.analyze.report.report.widget.BISummaryWidget;import com.fr.bi.cal.analyze.session.BISession;import com.fr.bi.conf.report.widget.field.dimension.BIDimension;import com.fr.bi.field.target.target.BISummaryTarget;import com.fr.bi.stable.data.BITable;import com.fr.bi.stable.gvi.GVIFactory;import com.fr.bi.stable.gvi.GroupValueIndex;import com.fr.bi.stable.report.key.TargetGettingKey;import com.fr.bi.stable.report.result.DimensionCalculator;import com.fr.bi.stable.report.result.LightNode;import com.fr.bi.stable.report.result.TargetCalculator;import java.util.ArrayList;/** * Created by Hiram on 2015/1/7. */class MergerInfo {    public static GroupValueIndex ALL_SHOW = GVIFactory.createAllShowIndexGVI(1);    public static GroupValueIndex ALL_EMPTY = GVIFactory.createAllEmptyIndexGVI();    BISession session;    BISummaryTarget biDimensionTarget;    GroupValueIndex gvi;    RootDimensionGroup rootDimensionGroup;    NoneDimensionGroup root;    TargetCalculator summary;    TargetGettingKey targetGettingKey;    BIDimension[] biDimensions;    BISummaryTarget[] allSumTarget;    private BISummaryWidget widget;    private GroupValueIndex filterIndex;    private NodeExpander expander;    private boolean needSummary;    private SortedTree sortedTree;    private LightNode tree;    MergerInfo() {    }    MergerInfo(BISummaryTarget biDimensionTarget, GroupValueIndex gvi, RootDimensionGroup rootDimensionGroup, NoneDimensionGroup root, TargetCalculator summary, TargetGettingKey targetGettingKey, BISession session, BIDimension[] biDimensions, NodeExpander expander, BISummaryTarget[] allSumTarget, BISummaryWidget widget) {        this.biDimensionTarget = biDimensionTarget;        this.gvi = gvi;        this.rootDimensionGroup = rootDimensionGroup;        this.root = root;        this.summary = summary;        this.targetGettingKey = targetGettingKey;        this.session = session;        this.biDimensions = biDimensions;        this.expander = expander;        this.allSumTarget = allSumTarget;        this.widget = widget;    }    BISummaryTarget getBiDimensionTarget() {        return biDimensionTarget;    }    void setBiDimensionTarget(BISummaryTarget biDimensionTarget) {        this.biDimensionTarget = biDimensionTarget;    }    GroupValueIndex getGroupValueIndex() {        return gvi;    }    void setGroupValueIndex(GroupValueIndex gvi) {        this.gvi = gvi;    }    RootDimensionGroup getRootDimensionGroup() {        return rootDimensionGroup;    }    void setRootDimensionGroup(RootDimensionGroup rootDimensionGroup) {        this.rootDimensionGroup = rootDimensionGroup;    }    NoneDimensionGroup getRoot() {        return root;    }    void setRoot(NoneDimensionGroup root) {        this.root = root;    }    TargetCalculator getSummary() {        return summary;    }    void setSummary(TargetCalculator summary) {        this.summary = summary;    }    TargetGettingKey getTargetGettingKey() {        return targetGettingKey;    }    void setTargetGettingKey(TargetGettingKey targetGettingKey) {        this.targetGettingKey = targetGettingKey;    }    BISession getSession() {        return session;    }    void setSession(BISession session) {        this.session = session;    }    BIDimension[] getBiDimensions() {        return biDimensions;    }    void setBiDimensions(BIDimension[] biDimensions) {        this.biDimensions = biDimensions;    }    SummaryDimensionGroup createSummaryDimensionGroup(int deep) {        DimensionCalculator[] columnKeys = createColumnKey();        if (columnKeys.length <= deep) {            return null;        }        DimensionCalculator columnKey = columnKeys[deep];        return new SummaryDimensionGroup(this, columnKey);    }    DimensionCalculator[] createColumnKey() {        DimensionCalculator[] row = new DimensionCalculator[biDimensions.length];        if (biDimensionTarget == null) {            for (int i = 0; i < row.length; i++) {                row[i] = biDimensions[i].createCalculator(biDimensions[i].getStatisticElement(), new ArrayList<BITableSourceRelation>());            }        } else {            LoaderUtils.fillRowDimension(widget, row, biDimensions, biDimensions.length, biDimensionTarget);        }        return row;    }    public boolean isNeedSummary() {        return needSummary;    }    public void setNeedSummary(boolean needSummary) {        this.needSummary = needSummary;    }    public NoneDimensionGroup createNoneDimensionGroup() {        return createNoneDimensionGroup(gvi);    }    public void applyFilterIndex(GroupValueIndex index) {        if (index == ALL_SHOW) {            return;        }        NoneDimensionGroup filterRoot = createFilterNoneDimensionGroup(index);        this.rootDimensionGroup = new RootDimensionGroup(filterRoot, createColumnKey(), biDimensions, expander, session, summary, widget, rootDimensionGroup.isUseRealData());        gvi = gvi.AND(index);    }    public RootDimensionGroup createFinalRootDimensionGroup() {        if (hasTreeRootDimensionGroup()) {            return createTreeRootDimensionGroup();        } else {            return createIteratorRoot();        }    }    private RootDimensionGroup createIteratorRoot() {        NoneDimensionGroup filterRoot = createFilterNoneDimensionGroup();        if (sortedTree != null) {            return new SortedRootDimensionGroup(filterRoot, createColumnKey(), biDimensions, expander, session, summary, sortedTree, widget, rootDimensionGroup.isUseRealData());        } else {            return new RootDimensionGroup(filterRoot, createColumnKey(), biDimensions, expander, session, summary, widget, rootDimensionGroup.isUseRealData());        }    }    public NoneDimensionGroup createFilterNoneDimensionGroup() {        return createFilterNoneDimensionGroup(filterIndex);    }    private NoneDimensionGroup createFilterNoneDimensionGroup(GroupValueIndex index) {        if (index == ALL_SHOW) {            return createNoneDimensionGroup();        }        return createNoneDimensionGroup(gvi.AND(index));    }    public GroupValueIndex getFilterIndex() {        return filterIndex;    }    public void setFilterIndex(GroupValueIndex filterIndex) {        this.filterIndex = filterIndex;    }    public SortedTree getSortedTree() {        return sortedTree;    }    public void setSortedTree(SortedTree sortedTree) {        this.sortedTree = sortedTree;    }    private NoneDimensionGroup createNoneDimensionGroup(GroupValueIndex filterGetter) {        return NoneDimensionGroup.createDimensionGroup(getTableKeyWithRoot(), filterGetter, session.getLoader());    }    private BusinessTable getTableKeyWithRoot() {        return root.getTableKey() == BITable.BI_EMPTY_TABLE() ? root.getTableKey() : summary.createTableKey();    }    public boolean hasTreeRootDimensionGroup() {        return tree != null;    }    public void setTreeNode(LightNode tree) {        this.tree = tree;    }    public TreeRootDimensionGroup createTreeRootDimensionGroup() {        TreeNoneDimensionGroup treeRoot = new TreeNoneDimensionGroup(tree);        return new TreeRootDimensionGroup(treeRoot, createColumnKey(), biDimensions, expander, session, summary, widget, rootDimensionGroup.isUseRealData());    }}
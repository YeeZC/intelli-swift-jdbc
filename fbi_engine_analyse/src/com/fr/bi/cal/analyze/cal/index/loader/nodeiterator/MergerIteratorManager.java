package com.fr.bi.cal.analyze.cal.index.loader.nodeiterator;import com.fr.bi.cal.analyze.cal.sssecret.GroupConnectionValue;import com.fr.bi.cal.analyze.cal.sssecret.NodeDimensionIterator;import com.fr.bi.cal.analyze.cal.sssecret.RootDimensionGroup;import com.fr.bi.stable.data.Table;/** * Created by Hiram on 2016/3/1. */public class MergerIteratorManager implements IteratorManager {	private RootMapper rootMapper = new RootMapper();	private RootDimensionGroup[] rootDimensionGroups;	private NodeDimensionIterator[] nodeDimensionIterators;	public MergerIteratorManager(RootDimensionGroup[] rootDimensionGroups) {		this.rootDimensionGroups = rootDimensionGroups;		nodeDimensionIterators = new NodeDimensionIterator[rootDimensionGroups.length];		rootMapper.parse(rootDimensionGroups);	}	@Override	public void moveNext() {		for (int i = 0; i < rootDimensionGroups.length; i++) {			nodeDimensionIterators[i] = rootDimensionGroups[i].moveNext();		}	}	@Override	public GroupConnectionValue[] getNextGroupConnectionValues() {		GroupConnectionValue[] ret = new GroupConnectionValue[rootDimensionGroups.length];		for (int i = 0; i < ret.length; i++) {			int groupIndex = rootMapper.getGroupIndex(i);			if (groupIndex == i) {				ret[i] = nodeDimensionIterators[i].next();			}else{				ret[i] = ret[groupIndex];			}		}		return ret;	}}class RootMapper {	int[] mapper;	void parse(RootDimensionGroup[] rootDimensionGroups) {		mapper = new int[rootDimensionGroups.length];		Table[] biTargetKeys = getBITableKeys(rootDimensionGroups);		for (int i = 0; i < biTargetKeys.length; i++) {			for (int j = 0; j <= i; j++) {				if (isSameTable(biTargetKeys[i], biTargetKeys[j])) {					mapper[i] = j;					break;				}			}		}	}	private boolean isSameTable(Table key1, Table key2) {		if (key1 == key2) {			return true;		}		return key1.equals(key2);	}	private Table[] getBITableKeys(RootDimensionGroup[] rootDimensionGroups) {		Table[] biTableKeys = new Table[rootDimensionGroups.length];		for (int i = 0; i < rootDimensionGroups.length; i++) {			biTableKeys[i] = getBITableKeys(rootDimensionGroups[i]);		}		return biTableKeys;	}	private Table getBITableKeys(RootDimensionGroup rootDimensionGroup) {		return rootDimensionGroup.getRoot().getTableKey();	}	int getGroupIndex(int i) {		return mapper[i];	}}
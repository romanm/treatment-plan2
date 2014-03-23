package com.qwit.service;

import com.qwit.domain.Tree;

public class DoseModCreator extends DbNodeCreator {
	public Tree addChild() {
		Tree t = super.addChild();
//		SchemaMtl schemaMtl = (SchemaMtl)treeManager;
		return t;
	}
}

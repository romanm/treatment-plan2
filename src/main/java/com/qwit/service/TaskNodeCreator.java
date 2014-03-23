package com.qwit.service;

import com.qwit.domain.Tree;
import com.qwit.model.DrugMtl;


public class TaskNodeCreator extends DbNodeCreator {
	public Tree addChild() {
		Tree t = super.addChild();
		treeManager.nextCurrentId();
//		SchemaMtl schemaMtl = (SchemaMtl)treeManager;
		DrugMtl schemaMtl = (DrugMtl)treeManager;
		Tree parentT = getParentT();
		if("dose".equals(parentT.getTabName())
		|| "day".equals(parentT.getTabName())
		){
			schemaMtl.neNr(parentT.getParentT());
			if("notice".equals(t.getTabName())){
				log.debug(parentT);
				/*
				List<Tree> ddNE = schemaMtl.getDdNE(parentT);
				ddNE.add(t);
				 * */
			}
		}
		if(t.getChildTs()!=null)
		for(Tree t1:t.getChildTs()){
			if("dose".equals(t1.getTabName())){
//				schemaMtl.getDrugDose().put(t, t1);
//			}else if("day".equals(t1.getTabName())){
//				schemaMtl.getDrugDay(t).add(t1);
//				for(Tree t2:t1.getChildTs()){
//					if("times".equals(t2.getTabName())){
//						schemaMtl.getDayTime(t1).add(t2);
//					}
//				}
			}
		}
		return t;
	}
}

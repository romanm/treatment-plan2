package com.qwit.service;

import java.util.List;

import com.qwit.domain.Checklist;
import com.qwit.domain.MObject;

public class DbChecklistCreator extends DbObjMCreator {

	Checklist checklist;
	public DbChecklistCreator setMtlO(Checklist checklist) {
		this.checklist = checklist;
		return this;
	}
	public DbChecklistCreator (){
		setSql(" SELECT d FROM Checklist d WHERE " +
				" upper(d.checklist)= upper(:checkitem) AND d.type=:type ");
//		checklist = new Checklist();
	}
	@Override
	public void create() {
		log.info("----- 1 "+checklist);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(checklist.getChecklist())
		.setParam(checklist.getType());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		String sql2 = "SELECT * FROM Checklist " +
				" WHERE checklist = '"+checklist.getChecklist() +"' " +
				" AND type='"+checklist.getType()+"'";
		List<Checklist> dL = 
//			em.createNativeQuery(sql2,Checkitem.class).getResultList();
			em.createQuery(getSql())
		.setParameter("checkitem",	checklist.getChecklist())
		.setParameter("type",		checklist.getType())
		.getResultList();
		Checklist pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!checklist.hasChecklist()&&!checklist.hasType()) return true;
		return false;
	}

}

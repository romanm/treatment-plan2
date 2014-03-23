package com.qwit.service;

import java.util.List;

import com.qwit.domain.Labor;
import com.qwit.domain.MObject;

public class DbLaborCreator extends DbObjMCreator {

	Labor labor;
	public DbLaborCreator setMtlO(Labor checkitem) {
		this.labor = checkitem;
		return this;
	}
	public DbLaborCreator (){
		setSql(" SELECT d FROM Labor d WHERE " +
				" d.labor= :labor AND d.unit=:unit AND d.type=:type ");
	}
	@Override
	public void create() {
		log.info("----- 1 "+labor);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(labor.getLabor())
		.setParam(labor.getUnit())
		.setParam(labor.getType());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		String sql2 = "SELECT * FROM Labor " +
				" WHERE labor = '"+labor.getLabor() +"' "
				+" AND unit='"+labor.getUnit()+"'"
				+" AND type='"+labor.getType()+"'";
		List<Labor> dL = 
			em.createNativeQuery(sql2,Labor.class).getResultList();
//			em.createQuery(sql)
//		.setParameter("checkitem",	checkitem.getCheckitem())
//		.setParameter("type",	checkitem.getType())
//		.getResultList();
		Labor pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!labor.hasLabor()&&!labor.hasUnit()&&!labor.hasType()) return true;
		return false;
	}

}

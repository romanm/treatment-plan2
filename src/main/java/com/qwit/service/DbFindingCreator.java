package com.qwit.service;

import java.util.List;

import com.qwit.domain.Finding;
import com.qwit.domain.MObject;

public class DbFindingCreator extends DbObjMCreator {

	Finding finding;
	public DbFindingCreator setMtlO(Finding finding) {
		this.finding = finding;
		return this;
	}
	public DbFindingCreator (){
		setSql(" SELECT d FROM Finding d WHERE " +
				" d.finding= :finding AND d.unit=:unit ");
	}
	@Override
	public void create() {
		log.info("----- 1 "+finding);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(finding.getFinding())
		.setParam(finding.getUnit());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		String sql2 = "SELECT * FROM Finding " +
				" WHERE finding = '"+finding.getFinding() +"' " +
				" AND unit='"+finding.getUnit()+"'";
		List<Finding> dL = 
			em.createNativeQuery(sql2,Finding.class).getResultList();
//			em.createQuery(sql)
//		.setParameter("checkitem",	checkitem.getCheckitem())
//		.setParameter("type",	checkitem.getType())
//		.getResultList();
		Finding pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!finding.hasFinding()&&!finding.hasUnit()) return true;
		return false;
	}

}

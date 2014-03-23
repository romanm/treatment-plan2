package com.qwit.service;

import java.util.List;

import com.qwit.domain.Checkitem;
import com.qwit.domain.MObject;

public class DbCheckitemCreator extends DbObjMCreator {

	Checkitem checkitem;
	public DbCheckitemCreator setMtlO(Checkitem checkitem) {
		this.checkitem = checkitem;
		return this;
	}
	public DbCheckitemCreator (){
//		setSql(" SELECT d FROM Checkitem d WHERE d.checkitem= :checkitem AND d.type=:type ");
		setSql(" SELECT d FROM Checkitem d WHERE " +
		" upper(d.checkitem)= upper(:checkitem) AND d.type=:type ");
	}
	@Override
	public void create() {
		log.info("----- 1 "+checkitem);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(checkitem.getCheckitem())
		.setParam(checkitem.getType());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		String sql2 = "SELECT * FROM Checkitem " +
				" WHERE upper(checkitem) = upper('"+checkitem.getCheckitem() +"') " +
				" AND type='"+checkitem.getType()+"'";
		log.debug(sql2);
		List<Checkitem> dL = 
			em.createNativeQuery(sql2,Checkitem.class).getResultList();
//			em.createQuery(sql)
//		.setParameter("checkitem",	checkitem.getCheckitem())
//		.setParameter("type",	checkitem.getType())
//		.getResultList();
		log.debug(dL);
		Checkitem pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!checkitem.hasCheckitem()&&!checkitem.hasType()) return true;
		return false;
	}

}

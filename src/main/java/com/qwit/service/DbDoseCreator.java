package com.qwit.service;

import java.util.List;

import com.qwit.domain.Dose;
import com.qwit.domain.MObject;

public class DbDoseCreator extends DbObjMCreator {

	private Dose dose;

	@Override
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer()
		.append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(dose.getValue().toString())
		.setParam(dose.getUnit())
		.setParam(dose.getApp())
		.setParam(dose.getPro())
		.setParam(dose.getType());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	public DbDoseCreator() {
		setSql("SELECT d FROM Dose d WHERE d.value = :value AND d.unit=:unit AND d.app=:app AND d.pro=:pro AND d.type=:type");
	}
	@Override
	public MObject read() {
		Dose pvDb=null;
		String sql = "SELECT d.* FROM Dose d WHERE" +
				" d.value = '"	+dose.getValue()+"'" +
				" AND d.unit = '"	+dose.getUnit()+"'" +
				" AND d.app = '"	+dose.getApp()+"'" +
				" AND d.pro = '"	+dose.getPro()+"'" +
				" AND d.type = '"	+dose.getType()+"'";
		List dL = em.createNativeQuery(sql, Dose.class)
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=(Dose) dL.get(0);
		}
		return pvDb;
	}
	public MObject read2() {
		Dose pvDb=null;
		List dL = em.createQuery(getSql())
		.setParameter("value",	dose.getValue())
		.setParameter("unit",	dose.getUnit())
		.setParameter("app",	dose.getApp())
		.setParameter("pro",	dose.getPro())
		.setParameter("type",	dose.getType())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=(Dose) dL.get(0);
		}
		return pvDb;
	}

	public DbObjMCreator setMtlO(Dose dose) {
		this.dose=dose;
		return this;
	}

	@Override
	public boolean isNull() {
		if(!dose.hasValue()&&!dose.hasUnit()&&!dose.hasApp())return true;
		return false;
	}

}

package com.qwit.service;

import java.util.List;

import com.qwit.domain.Dose;
import com.qwit.domain.MObject;
import com.qwit.domain.Tablet;

public class DbTabletCreator extends DbObjMCreator {
	private Tablet tablet;
	@Override
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer()
		.append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(tablet.getValue().toString())
		.setParam(tablet.getPortion().toString())
		.setParam(tablet.getUnit())
		.setParam(tablet.getType());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}
	public DbTabletCreator() {
		setSql("SELECT d FROM Tablet d "+
			" WHERE d.value = :value AND d.portion=:portion  AND d.unit=:unit AND d.type=:type");
	}
	@Override
	public MObject read() {
		Tablet pvDb=null;
		String sql = "SELECT d.* FROM Tablet d WHERE" +
				" d.value = '"	+tablet.getValue()+"'" +
				" AND d.portion = '"	+tablet.getPortion()+"'" +
				" AND d.unit = '"	+tablet.getUnit()+"'" +
				" AND d.type = '"	+tablet.getType()+"'";
		List dL = em.createNativeQuery(sql, Tablet.class)
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=(Tablet) dL.get(0);
		}
		return pvDb;
	}
	public MObject read2() {
		Dose pvDb=null;
		List dL = em.createQuery(getSql())
		.setParameter("value",		tablet.getValue())
		.setParameter("portion",	tablet.getPortion())
		.setParameter("unit",		tablet.getUnit())
		.setParameter("type",		tablet.getType())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=(Dose) dL.get(0);
		}
		return pvDb;
	}
	public DbObjMCreator setMtlO(Tablet tablet) {
		this.tablet=tablet;
		return this;
	}
	@Override
	public boolean isNull() {
		if(!tablet.hasValue()&&!tablet.hasPortion()&&!tablet.hasUnit()&&!tablet.hasType())return true;
		return false;
	}
}

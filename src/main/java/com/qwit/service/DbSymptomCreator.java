package com.qwit.service;

import java.util.List;

import com.qwit.domain.MObject;
import com.qwit.domain.Symptom;

public class DbSymptomCreator extends DbObjMCreator {

	private Symptom symptom;

	@Override
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(symptom.getSymptom().toString()).setParam(symptom.getType()).setParam(symptom.getUnit());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	public DbSymptomCreator() {
		setSql("SELECT d FROM Symptom d WHERE d.symptom = :symptom AND d.type=:type AND d.unit=:unit");
//		dose=new Notice();
	}
	@Override
	public MObject read() {
		if(symptom.getType()==null)	symptom.setType("");
		if(symptom.getUnit()==null)	symptom.setUnit("");
		symptom.setSymptom(symptom.getSymptom().trim());
		Symptom pvDb=null;
		List<Symptom> dL = em.createQuery(getSql())
		.setParameter("symptom",	symptom.getSymptom())
		.setParameter("type",	symptom.getType())
		.setParameter("unit",	symptom.getUnit())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}

	public DbObjMCreator setSymptom(Symptom symptom) {
		this.symptom=symptom;
		return this;
	}

	@Override
	public boolean isNull() {
		if(!symptom.hasSymptom())return true;
		return false;
	}

}

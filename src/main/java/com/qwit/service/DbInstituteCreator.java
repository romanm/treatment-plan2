package com.qwit.service;

import java.util.List;

import com.qwit.domain.Institute;
import com.qwit.domain.MObject;


public class DbInstituteCreator extends DbObjMCreator {
	Institute institute;
	public DbInstituteCreator setMtlO(Institute institute) {
		this.institute = institute;
		return this;
	}
	public DbInstituteCreator (){
		setSql("SELECT d FROM Institute d WHERE d.institute= :institute ");
		institute = new Institute();
	}
	@Override
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(institute.getInstitute())
		/*
		.setParam(institute.getCity())
		.setParam(institute.getZip())
		.setParam(institute.getStreet())
		.setParam(institute.getCountry())
		
		.setParam(institute.getPhone())
		.setParam(institute.getFax())
		.setParam(institute.getStyletask())
		.setParam(institute.getShortname())
		.setParam(institute.getTaskstart())
		
		.setParam(institute.getDoseRoundPercent().toString())
		.setParam(institute.getMorning())
		.setParam(institute.getNoon())
		.setParam(institute.getEvening())
		.setParam(institute.getNight())
		
		.setParam(institute.getBsaformula())
		.setParam(institute.getDaysintensive())
		.setParam(institute.getNewtherapydayhour())
		
		.setParam(institute.getOldtimescalc())
		.setParam(institute.getPrintpaper())
		.setParam(institute.getPlTaskShow())
		.setParam(institute.getTaskEdDtt())
		
		.setParam(institute.getTaskCycleTemplate())
		.setParam(institute.getTaskstartHide())
		 * */
		;
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		Institute pvDb=null;
		List<Institute> dL = em.createQuery(getSql())
		.setParameter("institute",	institute.getInstitute())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!institute.hasInstitute()) return true;
		return false;
	}

}

package com.qwit.service;

import java.util.List;

import com.qwit.domain.Diagnose;
import com.qwit.domain.MObject;

public class DbDiagnoseCreator extends DbObjMCreator {

/**
 * 
 */
	private Diagnose diagnose;
	public DbDiagnoseCreator setMtlO(Diagnose diagnose) {
		this.diagnose = diagnose;
		return this;
	}

	public DbDiagnoseCreator (){
		setSql(" SELECT d FROM Diagnose d WHERE " +
				" d.diagnose= :diagnose ");
//	" upper(d.diagnose)= upper(:diagnose) ");
		diagnose = new Diagnose();
	}
	@Override
	public void create() 
	{
		log.info("----- 1 "+diagnose);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(diagnose.getDiagnose());
		getDbUpdate().updateDirect(sqls);
		log.info("----- 2 ");
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		String sql2 = "SELECT * FROM Diagnose " +
				" WHERE diagnose = '"+diagnose.getDiagnose() +"'; ";
		log.debug(getSql());
		List<Diagnose> dL = 
//		em.createNativeQuery(sql2,Diagnose.class).getResultList();
		(List<Diagnose>) em.createQuery(getSql())
		.setParameter("diagnose",	diagnose.getDiagnose())
		.getResultList();
		Diagnose pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}

	@Override
	public boolean isNull() {
		if(!diagnose.hasDiagnose()) return true;
		return false;
	}

}

package com.qwit.service;

import java.util.List;

import com.qwit.domain.Arm;
import com.qwit.domain.MObject;

/**
 * @author roman
 *
 */
public class DbArmCreator extends DbObjMCreator {

/**
 * 
 */
private Arm arm;

public DbArmCreator setMtlO(Arm arm) {
	this.arm = arm;
	return this;
}
	public DbArmCreator (){
		setSql(" SELECT d FROM Arm d WHERE d.arm= :arm ");
		arm = new Arm();
	}
	@Override
	public void create() 
	{
		log.info("----- 1 "+arm);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(arm.getArm());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
//		String sql2 = "SELECT * FROM Contactperson " +
//				" WHERE contactperson = '"+arm.getContactperson() +"' " +
//				" AND forename='"+arm.getForename()+"'";
		List<Arm> dL = 
//			em.createNativeQuery(sql2,Contactperson.class).getResultList();
			em.createQuery(getSql())
		.setParameter("arm",	arm.getArm())
		.getResultList();
		Arm pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!arm.hasArm()) return true;
		return false;
	}

}

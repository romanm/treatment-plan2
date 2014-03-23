package com.qwit.service;

import java.util.List;

import com.qwit.domain.MObject;
import com.qwit.domain.Personrole;

/**
 * @author roman
 *
 */
public class DbPersonroleCreator extends DbObjMCreator {

/**
 * 
 */
private Personrole personrole;

public DbPersonroleCreator setMtlO(Personrole arm) {
	this.personrole = arm;
	return this;
}
	public DbPersonroleCreator (){
		setSql(" SELECT d FROM Personrole d WHERE d.personrole= :personrole ");
		personrole = new Personrole();
	}
	@Override
	public void create() 
	{	
		log.info("----- 1 "+personrole);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(personrole.getPersonrole());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
//		String sql2 = "SELECT * FROM Contactperson " +
//				" WHERE contactperson = '"+arm.getContactperson() +"' " +
//				" AND forename='"+arm.getForename()+"'";
		List<Personrole> dL = 
//			em.createNativeQuery(sql2,Contactperson.class).getResultList();
			em.createQuery(getSql())
		.setParameter("personrole",	personrole.getPersonrole())
		.getResultList();
		Personrole pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!personrole.hasPersonrole()) return true;
		return false;
	}

}

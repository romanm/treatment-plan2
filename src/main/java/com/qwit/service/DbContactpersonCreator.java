package com.qwit.service;

import java.util.List;

import com.qwit.domain.Contactperson;
import com.qwit.domain.MObject;

public class DbContactpersonCreator extends DbObjMCreator {

/**
 * 
 */
	private Contactperson contactperson;
	public DbContactpersonCreator setMtlO(Contactperson contactperson) {
		this.contactperson = contactperson;
		return this;
	}

	public DbContactpersonCreator (){
		setSql(" SELECT d FROM Contactperson d WHERE " +
				" upper(d.contactperson)= upper(:contactperson) AND d.forename=:forename ");
		contactperson = new Contactperson();
	}
	@Override
	public void create() 
	{	
		log.info("----- 1 "+contactperson);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(contactperson.getContactperson())
		.setParam(contactperson.getPhone())
		.setParam(contactperson.getMobil())
		.setParam(contactperson.getNote())
		.setParam(contactperson.getEmail())
		.setParam(contactperson.getFax())
		.setParam(contactperson.getForename())
		.setParam(contactperson.getTitle());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		String sql2 = "SELECT * FROM Contactperson " +
				" WHERE contactperson = '"+contactperson.getContactperson() +"' " +
				" AND forename='"+contactperson.getForename()+"'";
		List<Contactperson> dL = 
			em.createNativeQuery(sql2,Contactperson.class).getResultList();
//			em.createQuery(sql)
//		.setParameter("contactperson",	contactperson.getContactperson())
//		.setParameter("type",	contactperson.getType())
//		.getResultList();
		Contactperson pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!contactperson.hasContactperson()&&!contactperson.hasForename()) return true;
		return false;
	}

}

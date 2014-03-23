package com.qwit.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.qwit.domain.MObject;
import com.qwit.domain.Protocol;
import com.qwit.domain.Tree;
import com.qwit.util.FlowObjCreator;

/**
 * @author roman
 *
 */
public class DbProtocolCreator extends DbObjMCreator {

/**
 * 
 */
private Protocol protocol;
private FlowObjCreator foc;

public DbProtocolCreator setProtocol(Protocol arm) {
	this.protocol = arm;
	return this;
}
	public DbProtocolCreator (){
		setSql("SELECT d FROM Protocol d WHERE d.protocol= :protocol AND d.protocolvar= :protocolvar  ");
		protocol = new Protocol();
	}
	@Override
	public void create(){
		log.info("----- 1 "+protocol);
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		Method[] protocolMethods = Protocol.class.getMethods();
		Integer nextId = nextDbid();
		log.info("----- 1 "+nextId);
		String sql1 = "INSERT INTO protocol (idprotocol";
		String sql2 = " VALUES ("+nextId;
		for (Method method : protocolMethods) {
			String name = method.getName();
			if(name.indexOf("get")==0){
//				log.debug("name: " + name);
				if("getClass".equals(name))
					continue;
				if("getId".equals(name))
					continue;
				try 
				{
					Object invoke = method.invoke(protocol);
					if(invoke!=null){
						log.debug(name+"="+invoke);
						String field = name.substring(3).toLowerCase();
						sql1+=", " +field +"";
						sql2+=",'" +invoke +"'";
					}
				}
				catch (IllegalArgumentException e)	{e.printStackTrace();}
				catch (IllegalAccessException e)	{e.printStackTrace();}
				catch (InvocationTargetException e)	{e.printStackTrace();}
			}
		}
//		sql1=sql1.replace("(,", "(");
//		sql2=sql2.replace("(,", "(");
		String sqlIns = sql1+") "+sql2+")";
		log.info("----- 1 "+foc.getIdf());
		String sqlTree = "INSERT INTO tree (id,idclass,tab_name,did) VALUES (" +
				nextId +"," +nextId +",'protocol'," +foc.getIdf() +")";
		log.info(sqlTree);
		getDbUpdate().getSimpleJdbc().update(sqlTree);
//		log.info("----- #"+sql2);
		log.info("----- 1 "+sqlIns);
		getDbUpdate().getSimpleJdbc().update(sqlIns);
		if(true)	return;
		getDbUpdate().setParamL()
		.setParam(protocol.getProtocol())
		.setParam(protocol.getProtocolvar())
		.setParam(protocol.getProtocoltype())
		.setParam(protocol.getPhase())
		.setParam(protocol.getProspective())
		.setParam(protocol.getRandomized())
		.setParam(protocol.getExpansion())
		.setParam(protocol.getIndication())
		.setParam(protocol.getInfostatus())
		.setParam(protocol.getIntention())
		.setParam(protocol.getStatus())
		.setParam(protocol.getStudynumber())
		
		.setParam(protocol.getStarttime().toString())
		.setParam(protocol.getEnddate().toString())
		.setParam(protocol.getStarttext())
		.setParam(protocol.getStand().toString())
		.setParam(protocol.getAmendment()==null?"":protocol.getAmendment().toString())
		.setParam(protocol.getAmendmentnr()==null?"":protocol.getAmendmentnr().toString())
		.setParam(protocol.getInfodate().toString())
		
		.setParam(protocol.getLongname())
		;
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
//		String sql2 = "SELECT * FROM Contactperson " +
//				" WHERE contactperson = '"+arm.getContactperson() +"' " +
//				" AND forename='"+arm.getForename()+"'";
		List<Protocol> dL =
//			em.createNativeQuery(sql2,Contactperson.class).getResultList();
		em.createQuery(getSql())
		.setParameter("protocol", protocol.getProtocol())
		.setParameter("protocolvar", protocol.getProtocolvar())
		.getResultList();
		Protocol pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!protocol.hasProtocol()) return true;
		return false;
	}
	public DbProtocolCreator setFoc(FlowObjCreator foc) {
		this.foc=foc;
		return this;
	}
	

}

package com.qwit.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Ivariable;

public class DbIvariableCreator extends DbObjMCreator{
	private Ivariable iv;

	public DbIvariableCreator (){
		setSql("SELECT d FROM Ivariable d WHERE d.ivariable= :ivariable AND d.ivalue=:ivalue ");
	}

	public void setIval(Integer pval){setIvalue(pval);}
	public void setIvar(String pvar){setIvariable(pvar);}
	public DbIvariableCreator setIvalue(Integer pval){
		getIv().setIvalue(pval);
		return this;
	}

	private Ivariable getIv() {
		if(iv==null)
			iv=new Ivariable();
		return iv;
	}
	public DbIvariableCreator setIvariable(String pvar){
		getIv().setIvariable(pvar);
		return this;
	}
	
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL().setParam(getIv().getIvariable()).setParam(getIv().getIvalue().toString());
		getDbUpdate().updateDirect(sqls);
		sqls.append("\n-- DB UPDATE END \n");
		log.info(sqls);
	}
	public Ivariable read() {
		Ivariable pvDb=null;
		List<Ivariable> dL = em.createQuery(getSql())
		.setParameter("ivariable",	getIv().getIvariable())
		.setParameter("ivalue",		getIv().getIvalue())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	public DbIvariableCreator setPv(Ivariable pv) {
		this.iv=pv;
		return this;
	}
	protected final Log log = LogFactory.getLog(getClass());
	@Override
	public boolean isNull() {
		if(!getIv().hasIvariable()&&!getIv().hasIvalue())return true;
		return false;
	}

	public DbObjMCreator setMtlO(Ivariable mtlO) {
		iv=mtlO;
		return this;
	}
}

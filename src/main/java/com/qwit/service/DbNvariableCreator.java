package com.qwit.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Nvariable;

public class DbNvariableCreator extends DbObjMCreator{
	private Nvariable nv;

	public DbNvariableCreator (){
		setSql("SELECT d FROM Nvariable d WHERE d.nvariable= :nvariable AND d.nvalue=:nvalue ");
	}

	private Nvariable getNv() {
		if(nv==null)
			nv=new Nvariable();
		return nv;
	}

	public void setNval(BigDecimal pval){setNvalue(pval);}
	public DbNvariableCreator setNvalue(BigDecimal pval){
		getNv().setNvalue(pval);
		return this;
	}

	public void setNvar(String pvar){setNvariable(pvar);}
	public DbNvariableCreator setNvariable(String pvar){
		getNv().setNvariable(pvar);
		return this;
	}
	
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL().setParam(getNv().getNvariable())
			.setParam(getNv().getNvalue().toString());
		getDbUpdate().updateDirect(sqls);
		sqls.append("\n-- DB UPDATE END \n");
		log.info(sqls);
	}
	public Nvariable read() {
		Nvariable pvDb=null;
		List<Nvariable> dL = em.createQuery(getSql())
		.setParameter("nvariable",	getNv().getNvariable())
		.setParameter("nvalue",		getNv().getNvalue())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	public DbNvariableCreator setPv(Nvariable pv) {
		this.nv=pv;
		return this;
	}
	protected final Log log = LogFactory.getLog(getClass());
	@Override
	public boolean isNull() {
		if(!getNv().hasNvariable()&&!getNv().hasNvalue())
			return true;
		return false;
	}

	public DbObjMCreator setMtlO(Nvariable mtlO) {
		nv=mtlO;
		return this;
	}
}

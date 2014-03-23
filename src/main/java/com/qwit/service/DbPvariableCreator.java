package com.qwit.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Pvariable;


public class DbPvariableCreator extends DbObjMCreator{
	private Pvariable pv;

	public DbPvariableCreator (){
		setSql("SELECT d FROM Pvariable d WHERE d.pvariable= :pvariable AND d.pvalue=:pvalue AND d.unit=:unit ");
	}

	public DbPvariableCreator setUnit(String unit){
		getPv().setUnit(unit);
		return this;
	}

	public void setPval(String pval){setPvalue(pval);}
	public void setPvar(String pvar){setPvariable(pvar);}
	public void setUnt(String unit){setUnit(unit);}
	public DbPvariableCreator setPvalue(String pval){
		getPv().setPvalue(pval);
		return this;
	}

	private Pvariable getPv() {
		if(pv==null)
			pv=new Pvariable();
		return pv;
	}
	public DbPvariableCreator setPvariable(String pvar){
		getPv().setPvariable(pvar);
		return this;
	}
	
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL().setParam(getPv().getPvariable()).setParam(getPv().getPvalue()).setParam(getPv().getUnit());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}
	public Pvariable read() {
		Pvariable pvDb=null;
		List<Pvariable> dL = em.createQuery(getSql())
		.setParameter("pvariable",	getPv().getPvariable())
		.setParameter("pvalue",		getPv().getPvalue())
		.setParameter("unit",		getPv().getUnit())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	public DbPvariableCreator setPv(Pvariable pv) {
		this.pv=pv;
		return this;
	}
	protected final Log log = LogFactory.getLog(getClass());
	@Override
	public boolean isNull() {
		if(!getPv().hasPvariable()&&!getPv().hasPvalue()&&!getPv().hasUnit())return true;
		return false;
	}

	public DbObjMCreator setMtlO(Pvariable mtlO) {
		pv=mtlO;
		return this;
	}
}

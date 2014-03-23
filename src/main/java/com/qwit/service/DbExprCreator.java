package com.qwit.service;

import java.util.List;

import com.qwit.domain.Expr;
import com.qwit.domain.MObject;

public class DbExprCreator extends DbObjMCreator {

	private Expr expr;

	@Override
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL().setParam(expr.getExpr()).setParam(expr.getValue()).setParam(expr.getUnit()).setParam(expr.getType());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	public DbExprCreator() {
		setSql("SELECT d FROM Expr d WHERE d.expr= :expr AND d.value=:value AND d.unit=:unit AND d.type=:type");
//		dose=new Notice();
	}
	@Override
	public MObject read() {
		Expr pvDb=null;
		List<Expr> dL = em.createQuery(getSql())
		.setParameter("expr",	expr.getExpr())
		.setParameter("value",	expr.getValue())
		.setParameter("unit",	expr.getUnit())
		.setParameter("type",	expr.getType())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}

	public DbObjMCreator setMtlO(Expr expr) {
		this.expr=expr;
		return this;
	}

	@Override
	public boolean isNull() {
		if(!expr.hasExpr()&&!expr.hasValue()&&!expr.hasUnit()&&!expr.hasType())return true;
		return false;
	}

}

package com.qwit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Expr;
import com.qwit.domain.Labor;
import com.qwit.domain.MObject;

public class ExprUnit implements Serializable{
	private static final long serialVersionUID = 1L;
	protected final Log log = LogFactory.getLog(getClass());

	private static String ruleIfExprValue[] = {
		"getBeginDateDelay", 
		"cycleNr",
		"drugInput",
		"calcDose",
		"forBegin",
		"age"
		};
	private static String ifThenExprValue[] = {"cancel", "toGive"};
	private static String ruleLaborsLabor[] = {"Leukozyten", "Thrombozyten"};

	public static String[] getRuleIfExprValue() {return ruleIfExprValue;}
	public static String[] getIfThenExprValue() {return ifThenExprValue;}
	public static String[] getRuleLaborsLabor() {return ruleLaborsLabor;}

	private List<Labor> ruleLabors;
	public void setRuleLabors(List<Labor> ruleLabors) {this.ruleLabors = ruleLabors;}
	public List<Labor> getRuleLabors() {return ruleLabors;}
	private List<Expr> ruleIfExpr,ifThenExpr;
	public void setRuleIfExpr(List<Expr> ruleIfExpr) {this.ruleIfExpr = ruleIfExpr;}
	public void setIfThenExpr(List<Expr> ifThenExpr) {this.ifThenExpr = ifThenExpr;}
	public List<Expr> getIfThenExpr() {return ifThenExpr;}
	public List<Expr> getRuleIfExpr() {return ruleIfExpr;}
	
	private String sqlLabor	= "SELECT o FROM Labor o WHERE o.labor=:param1";
	private String sqlExpr	= "SELECT o FROM Expr o WHERE o.expr='functionCall' AND o.value=:param1";
	public void read(EntityManager em, DrugMtl docMtl){
		if(null==ruleLabors){
			ruleLabors=new ArrayList<Labor>();
			read(em, ruleLabors, ruleLaborsLabor,sqlLabor);
//			log.debug(ruleLabors.size());
		}
		if(null==ruleIfExpr){
			ruleIfExpr=new ArrayList<Expr>();
			read(em, ruleIfExpr, ruleIfExprValue,sqlExpr);
		}
		if(null==ifThenExpr){
			ifThenExpr=new ArrayList<Expr>();
			read(em, ifThenExpr, ifThenExprValue,sqlExpr);
		}
	}
	private void read(EntityManager em, List list,	String[] listValue,String sql) {
			Query q = em.createQuery(sql);
			for(String v:listValue){
				List resultList	= q.setParameter("param1", v).getResultList();
				MObject exprO	= (MObject) resultList.get(0);
//				log.debug(exprO);
				list.add(exprO);
			}
	}
}

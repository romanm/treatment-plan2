package com.qwit.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.qwit.domain.Expr;
import com.qwit.domain.Labor;
import com.qwit.domain.MObject;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Tree;
import com.qwit.model.DrugMtl;
import com.qwit.model.ExprUnit;
import com.qwit.model.SchemaMtl;

@Service("ruleService")
@Repository
public class RuleService extends MtlDbService {
	private final Log log = LogFactory.getLog(getClass());
	
	public void openEditExpr(SchemaMtl docMtl,ExprUnit exprUnit){
		if(null!=docMtl.getPatientMtl())
		{
		}else{
			if(docMtl.getEditNodeT().getIdClass()!=null){
				Expr editNodeExpr=(Expr)docMtl.getEditNodeT().getMtlO();
				log.debug("------------"+editNodeExpr);
				if(docMtl.isIf(docMtl.getEditNodeT())){
				}else if(docMtl.isAndOrExpr(docMtl.getEditNodeT())){
					docMtl.openEditAndOrExpr();
				}else if(docMtl.isEquals(docMtl.getEditNodeT())){
					docMtl.openEditEqualsExpr();
				}else if(docMtl.isElse(docMtl.getEditNodeT())){
				}else if(docMtl.isThen(docMtl.getEditNodeT())){
				}else if(docMtl.isThen(docMtl.getEditNodeT())){
					//not in use
					docMtl.setEditPvC(new Pvariable());
					if(docMtl.getEditNodeT().hasChild()){
						Tree thenChildT = docMtl.getEditNodeT().getChildTs().get(0);
						if(thenChildT.getMtlO() instanceof Pvariable){
							docMtl.copyAtt(docMtl.getEditPvC(),(Pvariable)thenChildT.getMtlO());
						}
					}else{
						docMtl.getEditPvC().setUnit("%");
						docMtl.getEditPvC().setPvalue("100");
					}
				}else{
					docMtl.setEditExprC(new Expr());
					docMtl.copyAtt(editNodeExpr,docMtl.getEditExprC());
					//				docMtl.set(editExprC,editNodeExpr);
					//				log.debug("------------"+editExprC);
					log.debug("------------"+docMtl.getEditExprC());
				}
			}
			log.debug("------------"+docMtl.getEditNodeT().getIdClass());
//			exprUnit.read(em,docMtl);
			init(exprUnit);
		}
	}

	public void init(ExprUnit exprUnit){
		if(null== exprUnit.getRuleLabors()){
			exprUnit.setRuleLabors(new ArrayList<Labor>());
			initLabor(exprUnit.getRuleLabors(), "Leukozyten","/microl","double");
			initLabor(exprUnit.getRuleLabors(), "Thrombozyten","/microl","int");
		}
		if(null== exprUnit.getRuleIfExpr()){
			exprUnit.setRuleIfExpr(new ArrayList<Expr>());
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","getBeginDateDelay","day","int");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","cycleNr","","int");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","drugInput","","int");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","inputPauseForInit","month","int");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","inputPauseForInit","week","int");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","calcDose","","double");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","forBegin","","boolean");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","age","year","int");
			initExprUnitExpr(exprUnit.getRuleIfExpr(), "functionCall","age","month","int");
		}
		if(null== exprUnit.getIfThenExpr()){
			exprUnit.setIfThenExpr(new ArrayList<Expr>());
			initExprUnitExpr(exprUnit.getIfThenExpr(), "functionCall","cancel","","");
			initExprUnitExpr(exprUnit.getIfThenExpr(), "functionCall","toGive","","");
		}
	}
	private void initLabor(List<Labor> ll, String labor, String unit, String type) {
		Labor l = new Labor();
		l.setLabor(labor);
		l.setUnit(unit);
		l.setType(type);
		l = (Labor) laborCreator.setMtlO(l).build();
		ll.add(l);
	}

	private void initExprUnitExpr(List list, String expr, String value,String unit, String type) {
		Expr ex = new Expr(expr,value,unit,type);
		ex = (Expr) exprCreator.setMtlO(ex).build();
		list.add(ex);
	}
	private void read(EntityManager em, List list,	String[] listValue,String sql) {
		Query q = em.createQuery(sql);
		for(String v:listValue){
			List resultList	= q.setParameter("param1", v).getResultList();
			MObject exprO	= (MObject) resultList.get(0);
			list.add(exprO);
		}
}
	private Query exprExprValueQ,pvariableQ;
	@Autowired	@Qualifier("ruleCreator")	TaskNodeCreator	ruleCreator;
	public void newElseIf(SchemaMtl docMtl) {
		Tree editNodeT = docMtl.correcturEditNodeT();
		log.debug("-----------------"+editNodeT);
		Tree ifT = docMtl.getEditIfT();
		log.debug("-----------------"+ifT);
		Tree elseT = docMtl.getElseT(ifT);
		log.debug("-----------------"+elseT);
		if(null!=elseT){
			if(elseT.hasChild()){
				List<Tree> childTs = new ArrayList<Tree>(elseT.getChildTs());
				Tree ifPart = addIfPart(docMtl, elseT);
				Tree thenT = ifPart.getChildTs().get(1);
				for (Tree tree : childTs) {
					docMtl.adoption(tree, thenT);
				}
			}else{
				addIfPart(docMtl, elseT);
			}
		}else{
			elseT=newIfElse(docMtl);
			addIfPart(docMtl, elseT);
		}
	}
	public Tree newIfElse(SchemaMtl docMtl) {
		Tree editNodeT = docMtl.correcturEditNodeT();
		Tree ifT = docMtl.getEditIfT();
		Tree elseT = null;
		if(null!=ifT){
			initQuery();
			elseT = addNode2edit(docMtl, ifT, "expr");
			Expr elseO = (Expr) exprExprValueQ.setParameter("expr", "else").setParameter("value", "").getSingleResult();
			docMtl.addMObject(elseO, elseT);
		}
		return elseT;
	}
	
	private void initQuery() {
		if(null==exprExprValueQ){
			exprExprValueQ = em.createQuery("select o from Expr o where o.expr=:expr and o.value=:value");
			pvariableQ = em.createQuery("select o from Pvariable o where o.pvariable=:var and o.pvalue=:val and o.unit=:unit");
		}
	}
	public void andOrExpr(SchemaMtl docMtl, String andOrExpr) {
		initQuery();
		log.debug("----------------"+andOrExpr);
		Tree ifT = docMtl.getEditIfT();
		if(docMtl.isIf(ifT)){
			Tree eqT = ifT.getChildTs().get(0);
			Expr andOrExprO = (Expr) exprExprValueQ.setParameter("expr", "andOrExpr")
				.setParameter("value", andOrExpr).getSingleResult();
			if(docMtl.isEquals(eqT)){
				Tree andOrExprT = addNode2edit(docMtl, ifT, "expr");
				andOrExprT.setSort(1);
				docMtl.addMObject(andOrExprO, andOrExprT);
				docMtl.adoption(eqT,andOrExprT);
				docMtl.initOld2newIdM();
				docMtl.pasteT(eqT, andOrExprT);
//			}else if(andOrExpr.equals(((Expr) eqT.getMtlO()).getExpr())){
			}else if(docMtl.isAndOrExpr(eqT)){
				docMtl.addMObject(andOrExprO, eqT);
			}
		}
	}

//	public void newRule(SchemaMtl docMtl) {
	public void newRule(DrugMtl docMtl) {
		docMtl.setEditNodeT();
		Tree editNodeT = docMtl.getEditNodeT();
		addIfPart(docMtl, editNodeT);
	}
	private Tree addIfPart(DrugMtl docMtl, Tree ifParentT) {
		initQuery();
		Expr ifO = (Expr) em.createQuery("select o from Expr o where o.expr=:expr and o.type=:type")
			.setParameter("expr", "if").setParameter("type", "2").getSingleResult();
		Expr eqO = (Expr) exprExprValueQ.setParameter("expr", "equality").setParameter("value", "=").getSingleResult();
		Expr expr1O = (Expr) exprExprValueQ.setParameter("expr", "expr").setParameter("value", "1").getSingleResult();
		Expr expr2O = (Expr) exprExprValueQ.setParameter("expr", "expr").setParameter("value", "2").getSingleResult();
		Expr thenO = (Expr) exprExprValueQ.setParameter("expr", "then").setParameter("value", "").getSingleResult();
		Pvariable pv_qO = (Pvariable) pvariableQ.setParameter("var", "").setParameter("val", "?").setParameter("unit", "").getSingleResult();
		Tree newRuleT = ruleCreator.setTreeManager(docMtl).setParentT(ifParentT)
			.setIdclass1(ifO.getId())
			.setIdclass2(eqO.getId())
			.setIdclass3(expr1O.getId())
			.setIdclass4(expr2O.getId())
			.setIdclass5(pv_qO.getId())
			.setIdclass6(thenO.getId())
			.addChild();
		docMtl.setIdt(newRuleT.getId());
		docMtl.addClass(newRuleT, docMtl.getDocT().getId(), em);
		return newRuleT;
	}
	public void newForBeginDrug(DrugMtl docMtl) {
		addNode2edit(docMtl, docMtl.getEditNodeT(), "drug");
	}
	public void thenNode(DrugMtl docMtl, String tabName) {
		Tree nodeT=null;
		if(docMtl.getEditNodeT().hasChild())
			for(Tree t:docMtl.getEditNodeT().getChildTs())
				if(tabName.equals(t.getTabName())||docMtl.isPvPv(tabName, t))
					nodeT=t;
		if(null==nodeT){
			if("procent".equals(tabName)){
				addNode2edit(docMtl, docMtl.getEditNodeT(), "pvariable");
			}else{
				addNode2edit(docMtl, docMtl.getEditNodeT(), tabName);
			}
		}
		/*
		if(docMtl.getEditNodeT().hasChild()){
			Tree doseT = docMtl.getEditNodeT().getChildTs().get(0);
			if(!tabName.equals(doseT.getTabName())){
				docMtl.setInstance(doseT,tabName);
			}
			docMtl.setIdt(doseT.getId());
			docMtl.setEditNodeT();
		}else{
			addNode2edit(docMtl, docMtl.getEditNodeT(), tabName);
		}
		 */
	}
	public void reviseExpr(SchemaMtl docMtl, ExprUnit exprUnit){
		Tree editNodeT = docMtl.getEditNodeT();
		log.debug("---------"+editNodeT);
		if(docMtl.isThen(editNodeT)||docMtl.isElse(editNodeT)){
			log.debug("---------");
			if("ruleProcent".equals(docMtl.getTypeOfDialog())){
				docMtl.getEditPvC().setUnit("%");
				log.debug("---------"+docMtl.getEditPvC());
				MObject thenElementO = pvCreator.setMtlO(docMtl.getEditPvC()).build();
				log.debug("---------"+thenElementO);
				docMtl.editChild0(editNodeT, treeCreator, thenElementO);
			}else if("selectExpr".equals(docMtl.getTypeOfDialog())){
				MObject expr1exprO = docMtl.seekEditO(exprUnit.getIfThenExpr());
				if(null!=expr1exprO){
					docMtl.editChild0(editNodeT, treeCreator, expr1exprO);
				}
			}
		}else if(docMtl.isAndOrExpr(editNodeT)){
			log.debug("---------"+docMtl.getAndOrExprO());
			Expr andOrExprO = (Expr) exprCreator.setMtlO(docMtl.getAndOrExprO()).build();
			log.debug("---------"+andOrExprO);
			docMtl.addMObject(andOrExprO, editNodeT);
		}else if(docMtl.isEquals(editNodeT)){
			//			Tree	editEqvalT = editNodeT.getChildTs().get(0);
			Tree	editEqvalT = editNodeT;
			log.debug("---------"+editEqvalT);
			log.debug("---------"+editEqvalT.getMtlO());
			Tree	expr2PvT = editEqvalT.getChildTs().get(1).getChildTs().get(0);
			Expr editEqvalO = (Expr) exprCreator.setMtlO(docMtl.getEditEqvalO()).build();
			Pvariable editPvC = (Pvariable) pvCreator.setMtlO(docMtl.getEditPvC()).build();
			docMtl.addMObject(editEqvalO, editEqvalT);
			docMtl.addMObject(editPvC, expr2PvT);
			MObject expr1exprO = docMtl.seekEditO(exprUnit.getRuleIfExpr());
			log.debug(expr1exprO);
			if(null==expr1exprO){
				expr1exprO = docMtl.seekEditO(exprUnit.getRuleLabors());
				log.debug(expr1exprO);
			}
			log.debug(expr1exprO);
			if(null!=expr1exprO){
				docMtl.editChild0(editEqvalT.getChildTs().get(0), treeCreator, expr1exprO);
			}
		}
	}
	
	@Autowired @Qualifier("treeCreator")	DbNodeCreator		treeCreator;
	@Autowired @Qualifier("exprCreator")	DbExprCreator		exprCreator;
	@Autowired @Qualifier("laborCreator")	DbLaborCreator		laborCreator;
	@Autowired @Qualifier("pvCreator")		DbPvariableCreator	pvCreator;
}

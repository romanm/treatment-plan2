package com.qwit.service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Dose;
import com.qwit.domain.Expr;
import com.qwit.domain.Tablet;
import com.qwit.domain.Tree;
import com.qwit.model.DrugMtl;
import com.qwit.model.TreeManager;

@Service("drugService")
@Repository
public class DrugService extends MtlDbService{
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired @Qualifier("exprCreator")	DbExprCreator		exprCreator;
	@Autowired @Qualifier("doseCreator")	DbDoseCreator		doseCreator;
	/**
	 * Preparing individual and cumulative max dose dialogue before 
	 * @param docMtl - edit document
	 */
	public void summeDoseEntry(DrugMtl docMtl){
		if(docMtl.getDocT()==docMtl.getEditNodeT())
		{
			addNotice(docMtl,"totalsdose");
			docMtl.setEditNodeT();
		}
		log.debug(docMtl.getEditNodeT());
		docMtl.openEditNotice(docMtl.getEditNodeT());
		Tree maxDoseExprT = seekMaxDoseExprT(docMtl, "functionCall", "maxDose");
		log.debug(maxDoseExprT);
		if(null!=maxDoseExprT&&maxDoseExprT.hasChild())
		{
			Tree maxDoseT = maxDoseExprT.getChildTs().get(0);
			Float maxDoseVal = docMtl.getDoseO(maxDoseT).getValue();
			docMtl.setMaxDose(maxDoseVal.intValue());
			docMtl.setMaxDoseUnit(docMtl.getDoseO(maxDoseT).getUnit());
		}
		Tree maxDoseCumulativeExprT = seekMaxDoseExprT(docMtl, "functionCall", "maxDoseCumulative");
		log.debug(maxDoseCumulativeExprT);
		if(null!=maxDoseCumulativeExprT&&maxDoseCumulativeExprT.hasChild())
		{
			Tree maxDoseT = maxDoseCumulativeExprT.getChildTs().get(0);
			Float maxDoseVal = docMtl.getDoseO(maxDoseT).getValue();
			docMtl.setMaxDoseCumulative(maxDoseVal.intValue());
			docMtl.setMaxDoseCumulativeUnit(docMtl.getDoseO(maxDoseT).getUnit());
		}
		
	}
	/**
	 * save individual and cumulative max dose dialogue in DB 
	 * @param docMtl - edit document
	 */
	public void summeDoseSave(DrugMtl docMtl){
		log.debug(docMtl.getEditNodeT());
		log.debug(docMtl.getMaxDose());
		if(null!=docMtl.getMaxDose()&&docMtl.getMaxDose()>0)
			buildExprDose(docMtl, "functionCall", "maxDose",docMtl.getMaxDose(), docMtl.getMaxDoseUnit());
		log.debug(docMtl.getMaxDoseCumulative());
		if(null!=docMtl.getMaxDoseCumulative()&&docMtl.getMaxDoseCumulative()>0)
			buildExprDose(docMtl, "functionCall", "maxDoseCumulative",docMtl.getMaxDoseCumulative(), docMtl.getMaxDoseCumulativeUnit());
	}
	private void buildExprDose(DrugMtl docMtl, String exprExpr, String exprValue,Integer doseVal, String doseUnit) {
		Tree maxDoseExprT = seekMaxDoseExprT(docMtl, exprExpr, exprValue);
		if(null==maxDoseExprT) {
			maxDose(docMtl, exprExpr, exprValue, doseVal, doseUnit);
		}else{
			Tree maxDoseT = maxDoseExprT.getChildTs().get(0);
			log.debug(maxDoseT);
			log.debug(doseVal);
			Dose maxDoseDC = buildDoseO(doseVal, doseUnit);
			log.debug(maxDoseDC);
			maxDoseT.setMtlO(maxDoseDC);
			log.debug(maxDoseT);
		}
	}
	private Tree seekMaxDoseExprT(DrugMtl docMtl, String exprExpr, String exprValue) {
		Tree maxDoseExprT = null;
		for(Tree exprT:docMtl.getEditNodeT().getChildTs()) 
			if(docMtl.isExprO(exprT)
			&&exprExpr.equals(docMtl.getExprO(exprT).getExpr())
			&&exprValue.equals(docMtl.getExprO(exprT).getValue())
			)
				maxDoseExprT=exprT;
		return maxDoseExprT;
	}
	private void maxDose(TreeManager docMtl,String exprExpr, String exprValue, Integer maxDose, String doseUnit) {
		Tree maxDoseET = nodeCreator.setTagName("expr").setTreeManager(docMtl).setParentT(docMtl.getEditNodeT()).addChild();
		Tree maxDoseDT = nodeCreator.setTagName("dose").setTreeManager(docMtl).setParentT(maxDoseET).addChild();
		
		Expr maxDoseEC = buildExprO(exprExpr, exprValue);
		maxDoseET.setMtlO(maxDoseEC);
		
		Dose maxDoseDC = buildDoseO(maxDose, doseUnit);
		maxDoseDT.setMtlO(maxDoseDC);
	}
	private Dose buildDoseO(Integer maxDose, String doseUnit) {
		Dose maxDoseDC = new Dose();
		maxDoseDC.setUnit(doseUnit);
		maxDoseDC.setValue(maxDose);
		maxDoseDC=(Dose) doseCreator.setMtlO(maxDoseDC).build();
		return maxDoseDC;
	}
	private Expr buildExprO(String exprExpr, String exprValue) {
		Expr maxDoseEC = new Expr(exprExpr,exprValue,"","");
		maxDoseEC=(Expr) exprCreator.setMtlO(maxDoseEC).build();
		return maxDoseEC;
	}
	
	public void newTabletEntry(DrugMtl docMtl){
		Tablet tabletO = new Tablet();
		tabletO.setUnit("mg");
		tabletO.setType("tab");
		docMtl.setEditTabletO(tabletO);
	}
	@Transactional
	public void newTabletSave(DrugMtl docMtl){
		Tablet editTabletO = docMtl.getEditTabletO();
		log.debug(editTabletO);
		Integer editTabletInt = docMtl.getEditTabletInt();
		log.debug(editTabletInt);
		editTabletO.setValue(editTabletInt.floatValue());
		log.debug(editTabletO);
		Tablet tabletO = (Tablet) tabletCreator.setMtlO(editTabletO).build();
		Tree docT = docMtl.getDocT();
		Tree tabletT=addChild("tablet", docT, docT);
		tabletT.setIdClass(tabletO.getId());
	}
	@Autowired @Qualifier("tabletCreator")	DbTabletCreator tabletCreator;

}

package com.qwit.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.qwit.domain.App;
import com.qwit.domain.Day;
import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Expr;
import com.qwit.domain.Finding;
import com.qwit.domain.History;
import com.qwit.domain.Ivariable;
import com.qwit.domain.Labor;
import com.qwit.domain.Literature;
import com.qwit.domain.MObject;
import com.qwit.domain.Notice;
import com.qwit.domain.Protocol;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Symptom;
import com.qwit.domain.Tablet;
import com.qwit.domain.Task;
import com.qwit.domain.Times;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.domain.TsTask;
import com.qwit.service.DbDayCreator;
import com.qwit.service.DbDoseCreator;
import com.qwit.service.DbExprCreator;
import com.qwit.service.DbTaskCreator;
import com.qwit.service.JXPathBean;
import com.qwit.service.MtlDbService;
import com.qwit.service.TaskNodeCreator;

public class DrugMtl extends TreeManager implements Serializable{
	private static final long serialVersionUID = 1L;
	protected final Log log = LogFactory.getLog(getClass());

	private int									timesNr;
	private Map<Tree, Integer>					timesNrM;
	public void setTimesNr(Map<Tree, Integer> timesNrM) {this.timesNrM = timesNrM;}
	public Map<Tree, Integer>	getTimesNr()	{return timesNrM;}

	private UsedDose usedDose;
	public UsedDose getUsedDose() {	return usedDose;}

	Integer blockId;
	public void		setBlockId(Integer blockId) {this.blockId = blockId;}
	public Integer	getBlockId() {return blockId;}
	public Integer blockNr(Tree blockT){
		int nr = 1;
		for(Tree t:getDocT().getChildTs())
			if("task".equals(t.getTabName())){
				if(t==blockT)
					break;
				nr++;
			}
		return nr;
	}
	public Tree getFolderT(){
		Tree folderT = getDocT().getParentT();
		Tree docT = getDocT();
		if("task".equals(docT.getTabName())){
			docT = getProtocolT();
			folderT = docT.getParentT();
			if("patient".equals(folderT.getTabName()))
				folderT = folderT.getParentT();
		}else	if("protocol".equals(docT.getTabName())){
		}else	if("drug".equals(docT.getTabName())){
		}
		return folderT;
	}
	public Tree getProtocolT() {
		Tree docT = getDocT().getDocT();
		if(!"protocol".equals(docT.getTabName()))
				docT=null;
		return docT;
	}
	public Map<Tree, Set< Tree>> getGrule()	{return null;}

	List<Tree> tradeL;
	public List<Tree> getTradeL() {return tradeL;}
	public DrugMtl(Tree docT) {
		super(docT);
//		beginHHMM = "09:00";
		beginHHMM = "10:00";
		setPlanToNull();
		tradeL = new ArrayList<Tree>();
		usedDose = new UsedDose(this);
//		dDrugMp		= new HashMap<Tree, List<Tree>>();
		drugNeCnt	= new HashMap<Tree, Integer>();
		neNrM		= new HashMap<Tree, Integer>();
//		drugDoseMp	= new HashMap<Tree, Tree>();
//		drugAppMp	= new HashMap<Tree, Tree>();
		exprM		= new HashMap<Integer, Expr>();
		timesAbsL	= new ArrayList<Tree>();
		timesRefL	= new ArrayList<Tree>();
	}

	public void newMaxDose(String fName, DbExprCreator exprCreator, DbDoseCreator doseCreator, TaskNodeCreator taskNodeCreator){
		Expr maxDoseEC = new Expr();
		maxDoseEC.setExpr("functionCall");
		maxDoseEC.setValue(fName);
		maxDoseEC=(Expr) exprCreator.setMtlO(maxDoseEC).build();
		setEditNodeT();
		Tree maxDoseET = taskNodeCreator.setTagName("expr").setTreeManager(this).setParentT(getEditNodeT()).addChild();
		maxDoseET.setIdClass(maxDoseEC.getId());
		
		Dose maxDoseDC = new Dose();
		maxDoseDC.setUnit("mg");
		maxDoseDC=(Dose) doseCreator.setMtlO(maxDoseDC).build();
		Tree maxDoseDT = taskNodeCreator.setTagName("dose").setTreeManager(this).setParentT(maxDoseET).addChild();
		maxDoseDT.setIdClass(maxDoseDC.getId());
		
		setIdt(maxDoseDT.getId());
		setEditNodeT();
		nextCurrentId();
	}
	public void setBlockSupportDrug(TaskNodeCreator taskDrugCreator, DbTaskCreator taskCreator, TaskNodeCreator nodeCreator){
		Tree blockSupportT = //getTaskSupportT();
			getInsideTask(getBlockT(), "support");
		if(blockSupportT==null){
			Tree blockT = getBlockT();
			Task taskC = (Task)taskCreator.setTask("support").setType("").setTaskvar("").build();
			getClassM().put(taskC.getId(), taskC);
			blockSupportT = nodeCreator.setTagName("task").setTreeManager(this).setParentT(blockT).addChild();
			blockSupportT.setIdClass(taskC.getId());
			blockSupportT.setMtlO(taskC);
		}
		Tree newSupportDrug = taskDrugCreator.setTreeManager(this).setParentT(blockSupportT).addChild();
//		getDdrug(blockSupportT).add(newSupportDrug);
		setIdt(newSupportDrug.getId());
	}
	public Tree getBlockT() {
		Tree blockT = getTree().get(getBlockId());
		if(blockT==null && getEditNodeT()!=null){
			blockT=getEditNodeT();
			while (!"task".equals(blockT.getTabName())) 
				blockT=blockT.getParentT();
			if ("task".equals(blockT.getParentT().getTabName())) 
				blockT=blockT.getParentT();
		}
		return blockT;
	}

	public void deleteSelectNode()
	{
		if(getIdc()==null) return;
		Tree delT = getTree().get(getIdc());
		deleteNode(delT);
	}
	public void deleteEditNode_depr()
	{
		if(getIdt()==null) return;
		Tree delT = getTree().get(getIdt());
		deleteNode(delT);
	}

	//App editor
	private App editAppC;
	public App getEditAppC() {return editAppC;}
	public void openEditApp()
	{
		editAppC=new App();
		if(getEditNodeT().getIdClass()!=null)
		{
			App editNodeAppC=(App) getClassM().get(getEditNodeT().getIdClass());
			editAppC.setAppapp(editNodeAppC.getAppapp());
			editAppC.setUnit(editNodeAppC.getUnit());
		}
	}

	private	Pvariable	editPvC;
	public void setEditPvC(Pvariable editPvC) {
		this.editPvC = editPvC;
	}
	public Pvariable getEditPvC() {return editPvC;}
	public void openEditPvariable()
	{
		editPvC=new Pvariable();
	}

	
	@Override	void add1ClassObj_depr(Tree tree, MObject classM) {}
	@Override	void addMtlMap(MObject mtlC) {}
	@Override	void addTreeClassObj_depr(MObject objM, Tree tree) {}
/*
	@Override	
	void addTreePaar_depr(Tree tree) {
		String tabName=tree.getTabName();
		Tree parentT = tree.getParentT();
		String parentTabName = parentT.getTabName();
		boolean isIdClass = tree.getIdClass()!=null;
		if("dose".equals(tabName))
		{
			if(isIdClass) {
				Dose doseC = (Dose) classM(tree);
				getClassM().put(doseC.getId(),doseC);
			}
//			if("drug".equals(parentTabName))				getDrugDose().put(parentT, tree);
		}else if("drug".equals(tabName)) {
			if(isIdClass){
				Drug drugC = (Drug) classM(tree);
			}
//			if("drug".equals(parentTabName)) {
//				List<Tree> ddrug = getDdrug(parentT);
//				if(!ddrug.contains(tree))
//					ddrug.add(tree);
//			} else 
			if("task".equals(parentTabName)){
				neNr(tree);
			}
		}else if("drug".equals(parentTabName)&&"app".equals(tabName)) {
			if(isIdClass){
				App appC = (App) classM(tree);
			}
//			getDrugApp().put(parentT, tree);
//		}else if("day".equals(tabName)) {
//			if (isIdClass) {
//				Day dayC = (Day) classM(tree);
//			}
//			List<Tree> drugDay = getDrugDay(parentT);
//			if(!drugDay.contains(tree))	drugDay.add(tree);
		} else if("expr".equals(tabName)) {
//			List<Tree> ddNE = getDdNE(parentT);
//			if(!ddNE.contains(tree))	ddNE.add(tree);
		} else if("notice".equals(tabName)) {
			if (isIdClass) {
				Notice noticeC = (Notice) classM(tree);
			}
//			List<Tree> ddNE = getDdNE(parentT);
//			if(!ddNE.contains(tree))	ddNE.add(tree);
		} else if("day".equals(parentTabName)&&"times".equals(tabName)){
//			List<Tree> dayTime = getDayTime(parentT);
//			if(!dayTime.contains(tree))	dayTime.add(tree);
			addRefOrAbs(tree);
		} else if("task".equals(tabName)){
			if(isIdClass){
			}
		}
	}
 */

	public void addRefOrAbs(Tree tree) {
		if(tree.getRef()!=null) timesRefL.add(tree); else timesAbsL.add(tree);
	}
	@Override	boolean isThisDocumentTag(Tree tree) {return false;}
	
	public List<Tree> getDdNE(Tree doseT)
	{
//		if(ddNoExMp==null)
//			ddNoExMp=new HashMap<Tree, List<Tree>>();
//		List<Tree> parentHm = ddNoExMp.get(doseT);
//		if(parentHm==null)
//		{
//			parentHm=new ArrayList<Tree>();
//			ddNoExMp.put(doseT, parentHm);
//		}
//		return parentHm;
		return null;
	}
	
//	public List<Tree> getDayTime(Tree dayT)
//	{
//		if(dayTimeMp==null)
//			dayTimeMp=new HashMap<Tree, List<Tree>>();
//		List<Tree> parentHm = dayTimeMp.get(dayT);
//		if(parentHm==null)
//		{
//			parentHm=new ArrayList<Tree>();
//			dayTimeMp.put(dayT, parentHm);
//		}
//		return parentHm;
//	}
	
	/**
	 * @param drugT
	 * @return
	public List<Tree> getDdrug(Tree drugT)
	{
		List<Tree> parentHm = dDrugMp.get(drugT);
		if(parentHm==null)
		{
			parentHm=new ArrayList<Tree>();
			dDrugMp.put(drugT, parentHm);
		}
		return parentHm;
	}
	 */

	/**
	 * @param drugT
	 * @return Days of drug.
	public List<Tree> getDrugDay(Tree drugT)
	{
		if(drugDayMp==null)
			drugDayMp=new HashMap<Tree, List<Tree>>();
		List<Tree> parentHm = drugDayMp.get(drugT);
		if(parentHm==null)
		{
			parentHm=new ArrayList<Tree>();
			drugDayMp.put(drugT, parentHm);
		}
		return parentHm;
	}
	 */
	Integer iNE;
	public void neNr(Tree tree) {
		if(tree.getChildTs()==null) return;
		iNE = 1;
		for(Tree dr2E:tree.getChildTs()){
			if("dose".equals(dr2E.getTabName())){
				ne1Nr(dr2E);
			}else if("day".equals(dr2E.getTabName())){
				ne1Nr(dr2E);
			}else if("drug".equals(dr2E.getTabName())){
				for(Tree dr21E:dr2E.getChildTs()){
					if("dose".equals(dr21E.getTabName())){
						ne1Nr(dr21E);
					}
				}
			}
		}
	}
	private void ne1Nr(Tree dr2E) {
		for(Tree dr3E:dr2E.getChildTs())
			if(dr3E.getTabName().equals("notice")||dr3E.getTabName().equals("expr"))
			{
				neNrM.put(dr3E, iNE++);
			}
	}
	public void neNr_old(Tree tree) {
		if(tree.getChildTs()==null) return;
		Integer i = 1,j=0;
		for(Tree dr2E:tree.getChildTs()){
			if("dose".equals(dr2E.getTabName())){
				for(Tree dr3E:dr2E.getChildTs())
					if(dr3E.getTabName().equals("notice")||dr3E.getTabName().equals("expr"))
				{
					j++;
					if(!neNrM.containsKey(dr2E))
						neNrM.put(dr2E, i++);
				}
			}else if("day".equals(dr2E.getTabName())){
				for(Tree dr3E:dr2E.getChildTs())
					if("notice".equals(dr3E.getTabName())||"expr".equals(dr3E.getTabName()))
				{
					j++;
					if(!neNrM.containsKey(dr2E))
						neNrM.put(dr2E, i++);
				}
			}else if("drug".equals(dr2E.getTabName())){
				for(Tree dr21E:dr2E.getChildTs())
					if("dose".equals(dr21E.getTabName()))
						for(Tree dr3E:dr21E.getChildTs())
							if("notice".equals(dr3E.getTabName())||"expr".equals(dr3E.getTabName()))
						{
							j++;
							if(!neNrM.containsKey(dr21E))
								neNrM.put(dr21E, i++);
						}
			}
			drugNeCnt.put(tree, j);
		}
	}	
	private Map<Tree, Integer>				neNrM, drugNeCnt;
	public Map<Tree, Integer>	getDrugNeCnt()	{return drugNeCnt;}
	public Map<Tree, Integer>	getNeNr()		{return neNrM;}

//	private Map<Tree, Tree>			drugDoseMp, drugAppMp;
//	public Map<Tree, Tree> getDrugApp()		{return drugAppMp;}
//	public Map<Tree, Tree> getDrugDose()	{return drugDoseMp;}
	
	/*
	public Tree getDrugDose(Tree drugT)	{
		for(Tree t:drugT.getChildTs())
			if("dose".equals(t.getTabName()))
				return t;
		return null;
	}
	 * */

//	private Tree taskSupportT;
//	void setTaskSupportT(Tree tree)	{taskSupportT=tree;}
	//public Tree getTaskSupportT()	{return taskSupportT;}
//	public Tree getTaskSupportT()	{return getInsideTask(getBlockT(), "support"); }

	protected	Drug	editDrugC;
	public	Drug	getEditDrugC()	{return editDrugC;}
	public void openEditDrug(){
		editDrugC=new Drug();
		log.debug(getEditNodeT());
		if(getEditNodeT().getIdClass()!=null)
		{
			Drug editNodeAppC=(Drug) getClassM().get(getEditNodeT().getIdClass());
			editDrugC.setDrug(editNodeAppC.getDrug());
		}else{
			editDrugC.setDrug("");
		}
	}
	public String seekDrugAction(Drug drugC){
		if(!editDrugC.hasDrug())	return "nullDrug";
		else if(drugC==null)		return "nullSeekDrug";
		return "findDrug";
	}
	public void openEditidExpr()
	{
		
	}
	private Dose editDoseC;
	public void setEditDoseC(Dose editDoseC) {this.editDoseC = editDoseC;}
	public Dose getEditDoseC()		{return editDoseC;}
	/**
	 * Make new dose object for edit. 
	 */
	public void openEditDose()
	{
		editDoseC=new Dose();
		openEditDoseOnly();
	}
	protected void openEditDoseOnly() {
		Dose editNodeDoseC=(Dose) getClassM().get(getEditNodeT().getIdClass());
		if(editNodeDoseC!=null){
			editDoseC.setValue(editNodeDoseC.getValue());
			editDoseC.setUnit(editNodeDoseC.getUnit());
			editDoseC.setApp(editNodeDoseC.getApp());
			editDoseC.setPro(editNodeDoseC.getPro());
		}
	}
//	private void openEditDoseMod() {}

	private void dayTimesNt(Tree drugT) {
		for (Tree dayT : drugT.getChildTs()) 
			if("day".equals(dayT.getTabName()))
				for (Tree timesT : dayT.getChildTs()) 
					if("times".equals(timesT.getTabName())){
						timesNrM.put(timesT, ++timesNr);
					}
	}
	public void setTimesNr(JXPathContext jxpContext) {
		timesNr=0;
		timesNrM	= new HashMap<Tree,Integer>();
		//	"childTs/childTs[childTs/mtlO/unit='ml']/childTs/childTs[mtlO/unit='mmol/l']");
		JXPathContext newContext = jxpContext.newContext(getDocT());
		setTimesNr(newContext,
		"childTs[tabName='drug']/childTs/childTs[tabName='times']");
		setTimesNr(newContext,
		"childTs[mtlO/task='taskPremedication']/childTs/childTs/childTs[tabName='times']");
		setTimesNr(newContext,
		"childTs[mtlO/task='support']/childTs[tabName='drug'" +
		" and not(childTs/childTs/mtlO/type='rqm') and not(childTs/childTs/mtlO/type='sup')]" +
		"/childTs/childTs[tabName='times']");
		setTimesNr(newContext,
		"childTs[mtlO/task='support']/childTs[tabName='drug'" +
		" and childTs/childTs/mtlO/type='sup']" +
		"/childTs/childTs[tabName='times']");
		setTimesNr(newContext,
		"childTs[mtlO/task='support']/childTs[tabName='drug'" +
		" and childTs/childTs/mtlO/type='rqm']" +
		"/childTs/childTs[tabName='times']");
	}
	private void setTimesNr(JXPathContext jxpContext, String xp) {
		Iterator<Pointer> iteratePointers = jxpContext.iteratePointers(xp);
		while (iteratePointers.hasNext()) {
			Tree tree = (Tree)  iteratePointers.next().getValue();
			timesNrM.put(tree, ++timesNr);
		}
	}
	public void setTimesNr(Tree blockT) {
		timesNr=0;
		for(Tree drugT : blockT.getChildTs()){					//chemoTree
//			if(getDrugDay().containsKey(drugT))
			if("drug".equals(drugT.getTabName()))
				dayTimesNt(drugT);
		}
		Tree sT = getInsideTask(blockT, "support");
		if(sT != null)
		for(Tree drugT:sT.getChildTs()){				//supportTree
//			if(getDrugDay().containsKey(drugT))
			if("drug".equals(drugT.getTabName()))
				dayTimesNt(drugT);
		}
		
		Tree taskPremedicationT = getTaskPremedicationT();
		if(taskPremedicationT != null)
//			if(getTaskPremedicationT() != null)
		for(Tree drugT:taskPremedicationT.getChildTs()){		//premedicTree
//			for(Tree drugT:getTaskPremedicationT().getChildTs()){		//premedicTree
//			if(getDrugDay().containsKey(drugT))
			if("drug".equals(drugT.getTabName()))
				dayTimesNt(drugT);
		}
		for(Tree t1:blockT.getChildTs()){//laborTree
		}
		for(Tree t1:blockT.getChildTs()){//befundTree
		}
		for(Tree t1:blockT.getChildTs()){//notice
		}
		for(Tree t1:blockT.getChildTs()){//expr
		}
	}
	protected boolean isChemo(Tree drugT)		{return drugT.getParentT().getId()==getDocT().getId();}
//	private Tree
//	toxSheetT,
//	taskHomeTabletT;
//	taskPremedicationT;
//	public void setToxSheetT(Tree tree)		{toxSheetT=tree;}
//	public void setTaskPremedicationT(Tree taskPremedicationT)
//											{this.taskPremedicationT = taskPremedicationT;}
//	public void setTaskHomeTabletT(Tree taskHomeTabletT)
//											{this.taskHomeTabletT = taskHomeTabletT;}
	public Tree getToxSheetT()			{return getInsideTask(getBlockT(), "toxSheet"); }
	public Tree getTaskPremedicationT()	{return getInsideTask(getBlockT(), "taskPremedication"); }
	public Tree getTaskHomeTabletT()	{return getInsideTask(getBlockT(), "taskHomeTablet"); }
	public Tree getTaskSupportT()		{return getSupportTask(getBlockT());}
	public Tree getSupportTask(Tree parentT) {
		return getInsideTask(parentT, "support");
	}
	private Map<Integer, Expr>				exprM;
	public Map<Integer, Expr>	getExpr()		{return exprM;}
	
	private List<Tree>			timesRefL, timesAbsL;
	public List<Tree> getTimesRefL() {return timesRefL;}
	public List<Tree> getTimesAbsL() {return timesAbsL;}

	//Literature editor
	private	Literature	editLiteratureO;
	public Literature getEditLiteratureO() {return editLiteratureO;}
	public void openEditLiterature()
	{
		editLiteratureO=new Literature();
		if(getEditNodeT().getIdClass()!=null){
//			Literature editNodeLiterature=(Literature) getClassM().get(getEditNodeT().getIdClass());
			Literature editNodeLiteratureO=(Literature) getEditNodeT().getMtlO();
			log.debug(editNodeLiteratureO);
			editLiteratureO.setTitle(editNodeLiteratureO.getTitle());
			editLiteratureO.setAuthors(editNodeLiteratureO.getAuthors());
			editLiteratureO.setSpring(editNodeLiteratureO.getSpring());
			editLiteratureO.setSpringtype(editNodeLiteratureO.getSpringtype());
			editLiteratureO.setVolume(editNodeLiteratureO.getVolume());
			editLiteratureO.setYear(editNodeLiteratureO.getYear());
			editLiteratureO.setPage(editNodeLiteratureO.getPage());
			editLiteratureO.setUrl(editNodeLiteratureO.getUrl());
		}
	}

	//Day editor
	private	Day	editDayC;
	public	Day	getEditDayC()			{return editDayC;}
	public void openEditDay()
	{
		editDayC=new Day();
		if(getEditNodeT().getIdClass()!=null){
			Day editNodeDay=(Day) getClassM().get(getEditNodeT().getIdClass());
			editDayC.setAbs(editNodeDay.getAbs());
			editDayC.setNewtype(editNodeDay.getNewtype());
		}
	}
	
	Integer appDuration=7;
	public Integer getAppDuration() {return appDuration;}
	public void setAppDuration(Integer appDuration) {this.appDuration = appDuration;}

	Integer duration=14;
	public Integer getDuration() {return duration;}
	public void setDuration(Integer duration) {this.duration = duration;}

	//Time editor
	private	Times		editTimesC;
	private	Integer		oldTimesRef;
	Boolean weekMealViewSplit;
	public Boolean getWeekMealViewSplit(){return weekMealViewSplit;}
	public void setWeekMealViewSplit(Boolean weekMealViewSplit)
	{
		this.weekMealViewSplit = weekMealViewSplit;
	}
	public	Times	getEditTimesC()	{return editTimesC;}
	public void openEditTime()
	{
		weekMealViewSplit=getWeekSplit(getEditNodeT())!=null;
		oldTimesRef=getEditNodeT().getRef();
		editTimesC=new Times();
		if(getEditNodeT().getIdClass()!=null){
			Times editNodeTime=(Times) getClassM().get(getEditNodeT().getIdClass());
			editTimesC.setAbs(editNodeTime.getAbs());
			editTimesC.setRelvalue(editNodeTime.getRelvalue());
			editTimesC.setRelunit(editNodeTime.getRelunit());
			editTimesC.setApporder(editNodeTime.getApporder());
			editTimesC.setVisual(editNodeTime.getVisual());
		}
	}
	private Tree getWeekSplit(Tree timesT) {
		if(timesT.getChildTs()==null)
			return null;
		Tree weekSplitT = null;
		for(Tree weekSplit1T:timesT.getChildTs()){
			if(classM(weekSplit1T) instanceof Pvariable)
			{
				Pvariable weekSplit1C = (Pvariable) classM(weekSplit1T);
				if("weekMealView".equals(weekSplit1C.getPvariable())){
					weekSplitT = weekSplit1T;
				}
			}
		}
		return weekSplitT;
	}
	public Pvariable getNewPvWeekMealView() {
		return new Pvariable("weekMealView","split","");
	}
	public void changeWeekSplit(List tL) {
		Tree weekSplitT = getWeekSplit(getEditNodeT());
		if(weekSplitT==null&&weekMealViewSplit){
			weekSplitT = addChild(getEditNodeT(), "pvariable", getDocT());
			Pvariable weekSplitC ;
			if(tL.size()>0){
				weekSplitC = (Pvariable) tL.get(0);
				weekSplitT.setIdClass(weekSplitC.getId());
			}else{
				weekSplitC = getNewPvWeekMealView();
				addNewTreeObjM(weekSplitC, weekSplitT);
			}
		}
		if(!weekMealViewSplit&&weekSplitT!=null){
			removeNodeS.add(weekSplitT);
			nextCurrentId();
		}
	}
	
	/**
	 * Map from timesT to time ordered Ts objects set.
	 */
	private Map<Tree, Set<Ts>>				timesTsMp;
	/**
	 * Time ordered set of all Ts objects.
	 */
	private Set<Ts>	planTsS;
	/**
	 * Map from times id  to Ts object. 
	 * Map from timestamp number to Ts object. 
	 */
	private Map<Integer,Ts>	tsM;
	public Map<Integer, Ts>	getTsM() {return tsM;}
	private String beginHHMM;
	public void setBeginHHMM(String beginHHMM) {this.beginHHMM = beginHHMM;}

	private Ts taskTs;
	static Pattern absDayMinus = Pattern.compile("-?(\\d+)");
	
	public void setPlanToNull(){
		planTsS = new ConcurrentSkipListSet<Ts>();
		tsM = new HashMap<Integer, Ts>();
	}
	private String getDrugDayTimes(Tree timesT) {
		String ddt="/";
		MObject mtlO = timesT.getParentT().getParentT().getMtlO();
		if(mtlO instanceof Drug){
			ddt+=((Drug)mtlO).getDrug()+"/";
		}
		Day dayO = (Day) timesT.getParentT().getMtlO();
		if(null!=dayO){
			ddt+=dayO.getAbs()+"/";
			ddt+=timesT.getId()+"/";
		}
		return ddt;
	}
	public Set<Ts> getPlan(){
		if(!(planTsS.size()>0)){
			timesTsMp=new HashMap<Tree, Set<Ts>>();
			JXPathContext docTContext = JXPathContext.newContext(getDocT());
			String xp = "childTs/childTs/childTs[not(ref)][tabName='times']" +
			"|childTs/childTs/childTs/childTs[not(ref)][tabName='times']";
			Iterator<Pointer> timeAbtPI = docTContext.iteratePointers(xp);
			while (timeAbtPI.hasNext()) {
				Tree timesT = (Tree) timeAbtPI.next().getValue();
//			}for(Tree timesT:getTimesAbsL()) if(timesT.getRef()==null){
				Day dayC = (Day) getClassM().get(timesT.getParentT().getIdClass());
				if(dayC==null||dayC.getAbs()==null)continue;//not day
				if(!timesTsMp.containsKey(timesT)){
					calcTimes(timesT);
				}
			}
			xp = "childTs/childTs/childTs[not(not(ref))][tabName='times']" +
			"|childTs/childTs/childTs/childTs[not(not(ref))][tabName='times']";
//			log.debug(xp);
			timeAbtPI = docTContext.iteratePointers(xp);
			while (timeAbtPI.hasNext()) {
//				log.debug("--------------");
				Tree timesT = (Tree) timeAbtPI.next().getValue();
//			for(Tree timesT:getTimesRefL()){
				if(timesT.getRef()==null||timesT.getRef().equals(getDocT().getId())){
					Day dayC = (Day) getClassM().get(timesT.getParentT().getIdClass());
					if(dayC==null||dayC.getAbs()==null)continue;//not day
					if(!timesTsMp.containsKey(timesT)){
						calcTimes(timesT);
					}
				}
			}
//			log.debug("--------------");
			runRule();
			addTsId();
			makeSameDayMap();
			makeTsDoseTMap();
		}
		return planTsS;
	}
	
	Map<Ts,Tree> tsDoseM;
	public Map<Ts, Tree> getTsDoseM() {return tsDoseM;}
	private void makeTsDoseTMap(){
		Tree pschemaT = getPschemaT();
		if(null==pschemaT)
			return;
		tsDoseM=new HashMap<Ts, Tree>();
		for (Tree noticeT : pschemaT.getChildTs())
			for(Tree doseModT:  noticeT.getChildTs())
				if(null!=doseModT.getRef())
				{
					Tree doseT = getTree().get(doseModT.getRef());
					Tree drugT = doseT.getParentT();

					Ivariable i2 =  null;
					for(Tree taskTsNrT : doseModT.getChildTs())
						if(isIvariableO(taskTsNrT) && "taskTsNr".equals(getIvariableO(taskTsNrT).getIvariable()))
							i2=getIvariableO(taskTsNrT);
					if(null!=i2){
						Integer i=1;
						for(Tree dayT : drugT.getChildTs()) if(isDayO(dayT) )
							for(Tree timesT : dayT.getChildTs()) if(isTimesO(timesT))
								for(Ts ts : getTimesTs(timesT))
								{
									if(tsDoseM.containsKey(ts)){
									}else if(i.equals(i2.getIvalue()))
										tsDoseM.put(ts, doseModT);
									else if(getDayO(dayT).getAbs().contains("-") && i>=i2.getIvalue())
										tsDoseM.put(ts, doseModT);
									i++;
								}
					}
				}
	}

	
	
	void runRule() {
		log.debug("b");
	}
	
	public Tree getPschemaT() {
		if(null!=getPatientMtl()){
			Tree pSchemaT = getPatientMtl().getPschemaRefM().get(getDocT().getId());
			if(null!=pSchemaT)
				return pSchemaT;
//			for(Tree t:getPatientMtl().getDocT().getChildTs()){
//				if(getDocT().getId().equals(t.getRef()))
//					return t;
//			}
		}
		return null;
	}
	
	
	Map<Integer,Integer> sameDayM;
	public Map<Integer, Integer> getSameDayM() {return sameDayM;}
	private void makeSameDayMap() {
		sameDayM = new HashMap<Integer, Integer>();
		int currDay = -111,firstEqDay=currDay, thisDaySum=0, previousDaySum=thisDaySum-1;
		boolean step1day = false;
		for (Ts ts : planTsS){
			if(ts.getCday()!=currDay){
				step1day = ts.getCday()-currDay==1;
				if(step1day)
					putSameDayM(currDay, firstEqDay, thisDaySum, previousDaySum);
				if(previousDaySum!=thisDaySum || !step1day)
					firstEqDay=currDay;
				currDay=ts.getCday();
				previousDaySum=thisDaySum;
				thisDaySum=0;
			}
			thisDaySum+=ts.getTimesT().getId();
		}
		if(step1day)
			putSameDayM(currDay, firstEqDay, thisDaySum, previousDaySum);
	}
	private void putSameDayM(int currDay, int firstEqDay, int thisDaySum, int previousDaySum) {
		if(previousDaySum==thisDaySum){
			sameDayM.put(firstEqDay, currDay);
		}
	}
	static int HOUR_CLINICDAY_BEGIN = 3;
	private void addTsId() {
		int nr = 1, currDay = -111;
		DateTime dayStartDT = null;
		for (Ts ts : planTsS){
			ts.setNr(nr++);
			String hhmm = "00:00";
			boolean isInRelation = 
				null != getTree().get(ts.getTimesT().getRef())
				|| null != getRef2idMS().get(ts.getTimesT().getId());
			if(isInRelation&&currDay!=ts.getCday()){
				dayStartDT = new DateTime(ts);
				if(dayStartDT.hourOfDay().get()>=HOUR_CLINICDAY_BEGIN){
					currDay=ts.getCday();
				}
			}
			if(isInRelation){
				hhmm = getHhmm(dayStartDT,ts);
			}
			if(null==ts.getTimesT().getMtlO())
				hhmm+=".";
			ts.setHhmm(hhmm);
		}
	}

	/*
	private String getHhmm(long dayStart, Ts ts) {
		DateTime dayStartDT = new DateTime(dayStart);
		return getHhmm(dayStartDT, ts);
	}
	 */
	private String getHhmm(DateTime dayStartDT, Ts ts) {
		DateTime tsDT = new DateTime(ts);
		Minutes min = Minutes.minutesBetween(dayStartDT,tsDT);
		
		int minAfter = min.getMinutes();
		int hAfter = minAfter/60;
		minAfter-=hAfter*60;
		
		String hhmm = "";
		if(hAfter<9)hhmm="0";
		hhmm+=hAfter+":";
		if(minAfter<9)hhmm+="0";
		hhmm+=minAfter;
		return hhmm;
	}

	private void calcTimes(Tree timesT){
//		log.debug(timesT.getParentT().getParentT());
		Times timesC = (Times) timesT.getMtlO();
		Tree dayT = timesT.getParentT();
		if(dayT.getMtlO()==null)
			return;
		String absDayVal=getDayAbs(dayT);
		if("timesRelativ".equals(getDayO(dayT).getNewtype())){
			if(!timesT.hasRef())
				log.error("timesRelativ without times.ref");
			else {
				Tree refTimesT = getTree().get(timesT.getRef());
				Set<Integer> thisTsDay=new HashSet<Integer>();
				for (Ts ts : getTimesTs(refTimesT)) {
					Ts nts=makeRefTs(timesT, ts.getCday(), refTimesT);
					thisTsDay.add(ts.getCday());
				}
				int deep = Integer.parseInt(getDayO(dayT).getAbs());
				if(refTimesT.hasRef()&&2==deep){
					refTimesT = getTree().get(refTimesT.getRef());
					for (Ts ts : getTimesTs(refTimesT))
						if(!thisTsDay.contains(ts.getCday()))
							makeRefTs(timesT, ts.getCday(), refTimesT);
				}
			}
			return;
		}else
			if(absDayVal.length()==0)
				return;
		//day has abs
		Matcher m_absPeriod = JXPathBean.getAbsPeriod().matcher(absDayVal);
		if(m_absPeriod.matches()){
			makeTs(timesT, m_absPeriod);
		}else if(getDayO(timesT.getParentT()).getNewtype().contains("forRule")){
			String[] forRuleN = absDayVal.split(",");
			int day1 = Integer.parseInt(forRuleN[0]);
			makeTs(timesT,day1);
			log.debug(getDayO(timesT.getParentT()));
			String[] forRuleT = getDayO(timesT.getParentT()).getNewtype().split("_");
			Integer duration = getTaskO(getDocT()).getDuration();
			int dayPeriod = 0;
			try {
				dayPeriod = Integer.parseInt(forRuleN[1]);
			} catch (Exception e) {
				System.out.println("forRuleN[1] ist keine Zahl");
				return;
			}
			if("w".equals(forRuleT[2]))
				dayPeriod=dayPeriod*7;
			for(int day=day1+dayPeriod;day<duration;day+=dayPeriod)
				makeTs(timesT,day);
		}else if(absDayVal.indexOf(",")>0){
			for(String abs1:absDayVal.split(",")){
				m_absPeriod = JXPathBean.getAbsPeriod().matcher(abs1);
				if(m_absPeriod.matches()){
					makeTs(timesT, m_absPeriod);
				}else{
					if(!abs1.matches("\\d"))
						abs1=DbDayCreator.chageWeekType(abs1);
					int day = Integer.parseInt(abs1);
					makeTs(timesT,day);
				}
			}
		}else if(timesC!=null&&!timesC.allNull()){
			Matcher m_absDayMinus = absDayMinus.matcher(absDayVal);
			if(m_absDayMinus.find()){
				String group = m_absDayMinus.group();
				int day = Integer.parseInt(group);
				makeTs(timesT,day);
			}
		}else{//day==1day and timeC==null
			int day = Integer.parseInt(absDayVal);
			makeTs(timesT,day);
		}
		Set<Integer> refIs = getRef2idMS().get(timesT.getId());
		if(refIs!=null)
		{
			for(Integer id:refIs)
			{
				Tree tree = getTree().get(id);
				if(tree!=null)
				{
					if(!timesTsMp.containsKey(tree))
					{
						calcTimes(tree);
					}
				}
			}
		}
	}
	public List<Integer> calcDayList(String absDayVal) {
		List<Integer> dayList = new ArrayList<Integer>();
		if(absDayVal.length()!=0){
			Matcher m_absPeriod = JXPathBean.getAbsPeriod().matcher(absDayVal);
			if(m_absPeriod.matches()){
				calcPeriodDayList(dayList, m_absPeriod);
			}else if(absDayVal.indexOf(",")>0){
				for(String abs1:absDayVal.split(",")){
					m_absPeriod = JXPathBean.getAbsPeriod().matcher(abs1);
					if(m_absPeriod.matches()){
						calcPeriodDayList(dayList, m_absPeriod);
					}else{
						int day = Integer.parseInt(abs1);
						dayList.add(day);
					}
				}
			} 
		}
		return dayList;
	}
	private void calcPeriodDayList(List<Integer> dayList, Matcher m_absPeriod) {
		int b = Integer.parseInt(m_absPeriod.group(1));
		int e = Integer.parseInt(m_absPeriod.group(2));
		for (int day = b; day <= e; day++)
			dayList.add(day);
	}
	private void makeTs(Tree timesT, Matcher m_absPeriod) {
		int b = Integer.parseInt(m_absPeriod.group(1));
		int e = Integer.parseInt(m_absPeriod.group(2));
		for (int day = b; day <= e; day++)
			makeTs(timesT,day);
	}
	private String getDayAbs(Tree dayT) {
		Day dayC=(Day) dayT.getMtlO();
		return dayC.getAbs();
	}
	private void makeTs(Tree timesT, int day) {
		Times timesC = getTimesO(timesT);
		if((timesC==null||timesC.allNull())&&timesT.getRef()==null){
			makeDayHHMMTs(timesT,day,beginHHMM);
		}else if(timesT.hasRef()){
			makeRefTs(timesT, day);
		}else{
			String abs = timesC.getAbs();
			if(!timesC.hasAbs()){
				makeDayHHMMTs(timesT,day,beginHHMM);
			}else if(abs.indexOf("=")>=0){
				makeDayHHMMTs(timesT,day,"22:00");
				doseMeal(timesT);
			}else if(abs.indexOf(",")>0){
				for(String abs1:abs.split(",")){
					makeDayHHMMTs(timesT,day,abs1);
				}
			}else if(abs.length()>0){
				makeDayHHMMTs(timesT,day,abs);
			}
		}
	}
	private void doseMeal(Tree timesT) {
//		if(getDoseMod()==null) return;
		Tree doseT = JXPathBean.getDose(timesT.getParentT().getParentT());
//			getDrugDose().get(timesT.getParentT().getParentT());
		
		Dose doseC = (Dose) getClassM().get(doseT.getIdClass());
		Float calcDose = getCalcDose().get(doseT);
//		Tree doseModT = getDoseMod().get(doseT);
//		if(doseModT!=null){
//			doseT=doseModT;
//			calcDose = getCalcDose().get(doseT);
//			doseC = (Dose) getPatientMtl().getClassM().get(doseT.getIdClass());
//		}
		
		
		if(calcDose!=null&&"day".equals(doseC.getPro())){
			initCortisol(timesT, timesT.getMtlO());
			Map<String, Tree> mealM = getCortisol().get(timesT);
			int mm = mealM.size();
			Float calcDoseMeal = round(calcDose);
			Float doseMeal = calcDoseMeal/mm;
			for(String mealN:mealM.keySet()){
				Tree mgProMalT = mealM.get(mealN);
				Pvariable mgProMalC = (Pvariable) classM(mgProMalT);
				String dd = float2string(doseMeal);
				mgProMalC.setPvalue(dd);
			}
		}
	}

	private void initCortisol(Tree eptT, MObject eptC) {
		if(cortisol==null)
			cortisol=new HashMap<Tree, Map<String,Tree>>();
		if(cortisol.containsKey(eptT))
			return;
		if(eptC instanceof Expr){
			Tree doseT = eptT.getParentT();
			cortisol.put(doseT, new HashMap<String, Tree>());
		}else if(eptC instanceof Times){
			HashMap<String, Tree> mgProMalM = new HashMap<String, Tree>();
			cortisol.put(eptT, mgProMalM);
			Times timesC = (Times) eptC;
			int mn = timesC.getAbs().replace("=", "").replace("0", "").length();
			for (Integer unit = 1; unit <= mn; unit++) {
				Tree mgProMalT = new Tree();
				Pvariable mgProMalC = new Pvariable();
				mgProMalC.setUnit(unit.toString());
				mgProMalC.setPvalue("1");
				mgProMalT.setId(TreeManager._5000+unit);
				mgProMalT.setIdClass(mgProMalT.getId());
				mgProMalC.setId(mgProMalT.getId());
				getClassM().put(mgProMalC.getId(), mgProMalC);
				mgProMalM.put(unit.toString(), mgProMalT);
			}
		}else if(eptC instanceof Pvariable){
			Map<String, Tree> mgProMalM = cortisol.get(eptT.getParentT().getParentT());
			String unit = ((Pvariable) eptC).getUnit();
			mgProMalM.put(unit, eptT);
		}
	}
	Map<Tree,Map<String,Tree>> cortisol;
	public Map<Tree, Map<String, Tree>> getCortisol() {return cortisol;}
	
	public void addCalcDoseR(Tree t, String s){
		if(getCalcDoseR().get(t)==null)
			getCalcDoseR().put(t, s);
	}
	public Map<Tree, String>	getCalcDoseR(){
		if(calcDoseRM==null)	calcDose();
		return calcDoseRM;
	}
	public Map<Tree, Float>		getCalcDose(){
		if(calcDoseM==null)		calcDose();
		return calcDoseM;
	}
	private Map<Tree, String>	calcDoseMalRM;
	private Map<Tree, String>	calcDoseRM;
	private Map<Tree, Float>	calcDoseM;
	public Map<Tree, String>	getCalcDoseMalR(){
		if(calcDoseMalRM==null)	calcDose();
		return calcDoseMalRM;
	}
	public void calcDose() {
		calcDoseM		= new HashMap<Tree, Float>();
		calcDoseRM		= new HashMap<Tree, String>();
		calcDoseMalRM	= new HashMap<Tree, String>();
//		if(getPatientMtl()!=null){
		if(null!=getPatientMtl()&&null!=patientM.getWeight()){
			calcDocDose(getDocT(),getClassM());
			calcDocDose(getPatientMtl().getDocT(),getPatientMtl().getClassM());
		}
	}
	private void calcDocDose(Tree parentT,Map<Integer, MObject> classM) {
//		if(parentT.getChildTs()!=null)
		for(Tree doseT:parentT.getChildTs()){
			if(isDoseNotNullO(doseT)){
				Dose doseC=getDoseO(doseT);
				calc1Dose(doseC, doseT);
			}
			calcDocDose(doseT,classM);
		}
	}

	private Float calc1Dose(Dose doseO, Tree doseT) {
//		log.debug(doseT);
		Float calc = calc(doseO,getPatientMtl());
//		log.debug("calc="+calc);
		boolean hasDose2DoseCalc = doseO.hasDose2DoseCalc();
		if(hasDose2DoseCalc)
		{
			Tree dose2T =JXPathBean.getDose(doseT.getParentT().getParentT());
			if(dose2T!=null)
			{
				Dose dose2C = (Dose) getClassM().get(dose2T.getIdClass());
				float l = calc(dose2C,getPatientMtl())/1000;
				calc=doseO.getValue()*l;
			}
		}
		calc=maxDose(doseT,calc);
//		log.debug("calc="+calc);
		calc=roundMultiple(doseT,calc);
//		log.debug("calc="+calc);
		calcDoseM.put(doseT, calc);
		if(calc==null){
			calcDoseRM.put(doseT, null);
		}else if("AUC".equals(doseO.getUnit())){
			Float round = round(calc);
			String float2string = float2string(round);
			calcDoseRM.put(doseT, float2string);
		}else if(!doseO.getUnit().contains("/")){
			log.debug(1);
			if(doseO.getValue()>doseO.getValue().intValue()){
				log.debug(2);
				String string = doseO.getValue().toString();
				calcDoseRM.put(doseT, string);
			}else{
				String string = ""+doseO.getValue().intValue();
				calcDoseRM.put(doseT, string);
			}
		}else{
			Float round = round(calc);
			String float2string = float2string(round);
			calcDoseRM.put(doseT, float2string);
		}
		
		if("patient".equals(doseT.getDocT().getTabName()))
			return calc;
		
		Tree dayT	= JXPathBean.getChild(doseT.getParentT(), "day", 0);
		if(dayT==null)
			return calc;
		Tree timesT	= JXPathBean.getChild(dayT, "times", 0);
		Times timesC=(Times) timesT.getMtlO();
		if(timesC!=null && "drug".equals(doseT.getParentT())){
			if(timesC.getAbs().contains("=")){
				if("day".equals(doseO.getPro())){
					int l = timesC.getAbs().replaceAll("=", "").replaceAll("0", "").length();
					calc/=l;
				}
				Float round = round(calc);
				String float2string = float2string(round);
				calcDoseRM.put(doseT, float2string);
			}
		}
		Float calcDose = getCalcDose(doseT);
		return calc;
	}

	private Float roundMultiple(Tree doseT, Float calc) {
		Float r=calc;
		Pvariable pvC=null;
		Tree roundMultipleT = getFunction(doseT, "roundMultiple");
		if(roundMultipleT!=null)
			if(roundMultipleT.getChildTs()!=null)
				for (Tree maxDoseT : roundMultipleT.getChildTs()) 
					if(classM(maxDoseT) instanceof Pvariable)
						pvC=(Pvariable) classM(maxDoseT);
		if(pvC!=null){
			String pvl = pvC.getPvalue().replace(",", ".");
			if(pvl.contains(".")||pvl.contains(",")){
				float rmf = Float.parseFloat(pvl);
				int mali = (int) (calc/rmf);
				float r1 = mali*rmf;
				float r2 = (mali+1)*rmf;
				r = r1;
				if(r1-calc>r2-calc)r=r2;
			}else{
				Integer round = new Integer(pvl);
				Integer rr = calc.intValue()/round*round;
				if(round>calc)
					rr=round;
				r=rr.floatValue();
			}
		}
		return r;
	}
	public Float getCalcDose(Tree doseT){
		if(doseT.getIdClass()==null) return new Float(0);
//		Dose doseC = (Dose) getClassM().get(doseT.getIdClass());
		Dose doseC = getDoseO(doseT);
		if(doseC==null) return new Float(0);
		Float calc = calc(doseC,getPatientMtl());
		if(doseC.hasDose2DoseCalc()){
			Tree dose2C = JXPathBean.getDose(doseT.getParentT().getParentT());
//				getDrugDose().get(doseT.getParentT().getParentT());
			if(dose2C!=null){
			}
		}
		return calc;
	}
	boolean withoutSmoothRound = true;
	Float round(Float value) {
		if(value>11){
			if(withoutSmoothRound){
//				Integer vi = value.intValue();
				Integer vi = Math.round(value);
				value = vi.floatValue();
			}else{
				Integer round2 = roundInteger(value.intValue(), 0);
				value=round2.floatValue();
			}
		}else if(value>10){		value=round(value, 0);
		}else if(value>0.5){	value=round(value, 1);
		}else					value=round(value, 2);
		return value;
	}
	public Float round(float d, int r){
		double r1 = Math.pow(10.0, r*1.0);
		BigDecimal clDec = new BigDecimal(""+(d*r1) );
		clDec = clDec.setScale( 0, BigDecimal.ROUND_HALF_UP );
		float f = (float) (clDec.floatValue()/r1);
		return f;
	}
//	static int roundChain[]= {1000,500,250,200,100,50,20,10,8,6,4,2};
	//static int roundChain[]= {1000,500,250,200,100,50,20,10,8,6,5,4,2};
	static int roundChain[]= {1000,500,250,200,100,50,20,10,5,4,2};
	private Integer doseRoundProcent=5;

	private int roundInteger(int value,int i) {
		if(i==roundChain.length)return value;
		if(roundChain[i]>value)return roundInteger(value, ++i);
		int v2 = value/roundChain[i]*roundChain[i];
		int v3 = v2+roundChain[i];
		float p2 = Math.abs((v2-value)*100/(float)value);
		float p3 = Math.abs((v3-value)*100/(float)value);
		if(p2>doseRoundProcent&&p3>doseRoundProcent) return roundInteger(value, ++i);
		if(p3<p2)return v3;
		return v2;
	}
	private Float maxDose(Tree doseT, Float calc) {
		Dose doseO=null;
		/*
		if(doseT.getChildTs()!=null)
			for(Tree maxDoseExprT : doseT.getChildTs()) 
				if(maxDoseExprT.getIdClass()!=null)
					if(getMObject(maxDoseExprT) instanceof Expr)
						if("maxDose".equals(((Expr) getMObject(maxDoseExprT)).getValue()))
		 * */
		Tree maxDoseExprT = getFunction(doseT, "maxDose");
		if(maxDoseExprT!=null)
			if(maxDoseExprT.getChildTs()!=null)
				for (Tree maxDoseT : maxDoseExprT.getChildTs()) 
					if(classM(maxDoseT) instanceof Dose)
						doseO=(Dose) classM(maxDoseT);
		if(doseO==null){
			if(isDrugO(doseT.getParentT())){
				Drug drugO=(Drug)doseT.getParentT().getMtlO();
				Integer idGeneric = drugO.getGeneric().getId();
//				log.debug("drug maxDose "+idGeneric);
				SchemaMtl sm=(SchemaMtl)this;
				if(null!=sm.getnTypeDrugNoticeMObjMObjM()){
					Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>> totaldose_map = sm.getnTypeDrugNoticeMObjMObjM().get("totalsdose");
					if(null!=totaldose_map)	{
						Map<Integer, Map<Integer, Map<Integer, Integer>>> map = totaldose_map.get(idGeneric);
						if(null!=map){
							for (Integer idNotice : map.keySet()) {
								if(null!=map.get(idNotice)){
									for (Integer idExpr : map.get(idNotice).keySet()) {
										for (Integer idExpr2 : map.get(idNotice).get(idExpr).keySet()) {
											Integer idDose = map.get(idNotice).get(idExpr).get(idExpr2);
											MObject dose2O = getClassM().get(idDose);
											if(dose2O instanceof Dose){
												if(getClassM().get(idExpr) instanceof Expr){
													Expr expr2O = (Expr)getClassM().get(idExpr);
													if("maxDose".equals(expr2O.getValue())){
														doseO=(Dose) dose2O;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
			
		if(doseO!=null)
			calc=Math.min(calc, doseO.getValue());
		return calc;
	}
	
	private Tree getFunction(Tree doseT, String string) {
		Tree functionT=null;
		if(doseT.getChildTs()!=null)
			for(Tree maxDoseExprT : doseT.getChildTs()) 
				if(maxDoseExprT.getIdClass()!=null)
					if(classM(maxDoseExprT) instanceof Expr)
						if(string.equals(((Expr) classM(maxDoseExprT)).getValue()))
							functionT=maxDoseExprT;
		return functionT;
	}
	public Tree getInsideTask(Tree parentT, String task) {
		if(parentT!=null && parentT.getChildTs()!=null)
			for(Tree taskT : parentT.getChildTs())
				if(taskT.getIdClass()!=null)
					if(taskT.getMtlO() instanceof Task)
						if(task.equals( ((Task) taskT.getMtlO()).getTask()))
							return taskT;
		return null;
	}
	private Ts makeDayHHMMTs(Tree timesT,int day, String abs) {
		String[] hhmm = abs.split(":");
		int h = Integer.parseInt(hhmm[0]);
		int min = Integer.parseInt(hhmm[1]);
		DateTime tsDateTime = getTsDate(day);
		tsDateTime=tsDateTime.withTime(h, min, 0, 100);
		long timeInMillis = tsDateTime.getMillis();
		return makeCalsTs(timesT, day, timeInMillis);
	}
	private DateTime getTsDate(int day) {
		DateTime tsDate2;
		if(day>0){
			tsDate2 = getBeginCalendar().plusDays(day-1);
		}else{
			log.debug("day="+Math.abs(day)+" "+getBeginCalendar());
			tsDate2 = getBeginCalendar().minusDays(Math.abs(day));
			log.debug(tsDate2);
		}
		return tsDate2;
	}
	private DateTime getTsDate2(int day) {
		day+=day>0?-1:0;
		DateTime tsDate2 = getBeginCalendar().plusDays(day);
		return tsDate2;
	}
	/*
	private Ts makeDayHHMMTs_DEPR(Tree timesT,int day, String abs) {
			String[] hhmm = abs.split(":");
			Calendar tsCalendar = getTsDate(day);
			tsCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmm[0]));
			tsCalendar.set(Calendar.MINUTE, Integer.parseInt(hhmm[1]));
			tsCalendar.set(Calendar.SECOND, 5);
			long timeInMillis = tsCalendar.getTimeInMillis();
			Task taskC=(Task) classM(timesT.getParentT().getParentT().getParentT());
			return makeCalsTs(timesT, day, timeInMillis);
	}
	 * */
	/*
	private Calendar getTsDate(int day) {
		day+=day>0?-1:0;
		Calendar tsDate2 =  (Calendar) getBeginCalendar().clone();
		tsDate2.add(Calendar.DATE, day);
		return tsDate2;
	}
	 * */
	private void makeRefTs(Tree timesT, int day) {
		if(timesT.getMtlO()==null)return;
		makeRefTs(timesT, day, getTree().get(timesT.getRef()));
	}
	private Ts makeRefTs(Tree timesT, int day, Tree refT) {
		Ts nts=null;
		Times timesO=getTimesO(timesT);
		if(refT!=null && refT.getId().equals(getDocT().getId())){
			long calcTs = timesO.calcTs(getTaskTs());
			calcTs+=(day>0?day-1:day)*24*60*60*1000;
			nts=makeCalsTs(timesT, day, calcTs);
		}else{
			boolean find = false;
			if(timesTsMp.get(refT)!=null){
				for(Ts ts:timesTsMp.get(refT)){
					if(ts.getDay()==day) {
						Calendar c = Calendar.getInstance();
						c.setTimeInMillis(ts.getTime());
						if(c.get(Calendar.HOUR_OF_DAY)==0
								&& c.get(Calendar.MINUTE)==0
								&& c.get(Calendar.SECOND)==0
								&& ts.getDay()!=ts.getCday()
						)continue;//begin new day for 24h infusion
						find=true;
						long calcTs = timesO.calcTs(ts);
						nts=makeCalsTs(timesT, day, calcTs);
					}
				}
			}
			if(!find){
				nts=makeDayHHMMTs(timesT,day,beginHHMM);
			}
		}
		return nts;
	}

	private Ts makeCalsTs(Tree timesT, int day, long timeInMillis) {
		Ts ts = new Ts(timeInMillis, timesT, day);
//		setCday(ts);
		setCday2(ts);
		rqmDayM(ts);
		Tree appT = JXPathBean.getApp(timesT.getParentT().getParentT());
		int hms1 = Ts._1s;
		if(null!=appT && null!=appT.getMtlO()){
			int hms = getHms(hms1, appT);
			calcTs(ts,timeInMillis, hms);
			Ts ts1 = ts, ts2 = ts;
			while(ts2.getYearDay()!=ts2.getYearDayEnd()){
				ts2 = (Ts) ts1.clone();
				ts2.setBegin(ts);
				ts1.setBeyond(ts2);
				ts.set24End();
				ts2.set00();
				ts1=ts2;
				addTs(timesT, ts2);
			}
		}else{
			calcTs(ts,timeInMillis, hms1*5);
		}
		addTs(timesT, ts);
		Tree tT = timesT.getParentT().getParentT();
		if(tT.getTabName().equals("labor"))
		{
			Labor lC = (Labor) classM(tT);
		}
		return ts;
	}
	protected int getHms(int hms1, Tree appT) {
		App appO=getAppO(appT);
		if(appO.getUnit().equals("h"))hms1=Ts._1h;
		else if(appO.getUnit().equals("min"))hms1=Ts._1m;
		int appValue = 0;
		Integer appapp = appO.getAppapp();
		if(appapp!=null)
			appValue = appapp.intValue();
		int hms = appValue*hms1;
		if(0==hms)
			hms=5*Ts._1s;
		return hms;
	}
	protected void calcTs(Ts ts,long timeInMillis,  int hms) {
		Times timesO=(Times) ts.getTimesT().getMtlO();
		if(timesO!=null&&"3".equals(timesO.getApporder())){
			Timestamp tsEnd = new Timestamp(timeInMillis);
			ts.setTsEnd(tsEnd);
			ts.setTime(ts.getTsEnd().getTime()-hms);
		}else{
//			long tsEnd = ts.getTsEnd().getTime()+hms;//2011.03.18
			long tsEnd = ts.getTime()+hms;
			ts.setTsEnd(new Timestamp(tsEnd));
		}
	}
	/*
	private void setCday2(TsTask ts) {
		Calendar beginCalendar2 = getBeginCalendar();
		beginCalendar2.set(Calendar.HOUR_OF_DAY, 3);
		long ms = ts.getTime()-beginCalendar2.getTimeInMillis();
		int cday = (int) (ms/TsTask._1d);
		cday+=cday>=0?1:-1;
		cday+=ms<0?-2:0;
		ts.setCday(cday);
	}
	*/
	private void setCday2(Ts ts) {
		LocalDateTime bdt = getBeginCalendar().withTime(3, 0, 0, 0).toLocalDateTime();
		LocalDateTime cdt = new DateTime(ts.getTime()).toLocalDateTime();
		int cday = Days.daysBetween(bdt, cdt).getDays();
		if(0==cday){
			int hours = Hours.hoursBetween(bdt, cdt).getHours();
			if(0==hours){
				int minutes = Minutes.minutesBetween(bdt, cdt).getMinutes();
				if(0==minutes)		cday++;
				else if(minutes<0)	cday--;
				else				cday++;
			}else if(hours<0)		cday--;
			else					cday++;
		}else if(cday<0)			cday--;
		else						cday++;
		ts.setCday(cday);
	}
	/*
	private void setCday2(Ts ts) {
		LocalDateTime bdt = getBeginCalendar().withTime(3, 0, 0, 0).toLocalDateTime();
		LocalDateTime cdt = new DateTime(ts.getTime()).toLocalDateTime();
		Days d = Days.daysBetween(bdt, cdt);
		int cday = d.getDays();
		log.debug(bdt+" - "+cdt+" - cday="+cday);
		cday+=cday>=0?1:-1;
		ts.setCday(cday);
	}
	 * */
	/*
	private void setCday2(Ts ts) {
		Calendar beginCalendar2 = getBeginCalendar();
		DateTime bdt = new DateTime(beginCalendar2.getTimeInMillis());
		DateTime cdt = new DateTime(ts.getTime());
		Days d = Days.daysBetween(bdt, cdt);
		int cday = d.getDays();
		log.debug(cdt+"-"+bdt+"="+cday);
		cday+=cday>=0?1:-1;
		log.debug(cdt+"-"+bdt+"="+cday);
		ts.setCday(cday);
	}
	private void setCday2_depr(Ts ts) {
		Calendar beginCalendar2 = getBeginCalendar();
		beginCalendar2.set(Calendar.MINUTE, 0);
		beginCalendar2.set(Calendar.HOUR_OF_DAY, 3);
		Date date = new Date(ts.getTime());
		String string = date+"/"+beginCalendar2.getTime()+"/";
		long ms = ts.getTime()-beginCalendar2.getTimeInMillis();
		string+=ms+"/";
		int cday = (int) (ms/TsTask._1d);
		string+="("+ms+"/"+TsTask._1d+")/";
		string+=cday+"/";
		cday+=cday>=0?1:-1;
		string+=cday+"/";
		cday+=ms<0?-2:0;
		string+=cday+"/";
		log.debug(string);
		ts.setCday(cday);
	}
	private void setCday(Ts ts) {
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		long ms = ts.getTime()-beginTherapy.getTimeInMillis();
		String cde = c.getTime()+"/"+beginTherapy.getTime()+"/";
		int cday = (int) (ms/Ts._1d);
		cde+=cday+"/";
		if(cday>0){
			cday+=1;
		}else if (cday<0){
			cday-=2;
		}else{// 0 day
			cday=1;
		}
		Drug drugO2 = getDrugO(ts.getTimesT().getParentT().getParentT());
		cde+=cday+"/"+drugO2;
		log.debug(cde);
		ts.setCday(cday);
	}
	private void setCday_depr(Ts ts) {
		long ms = ts.getTime()-beginTherapy.getTimeInMillis();
		String cde = "";
		int cday = (int) (ms/Ts._1d);
		cde+=cday+"/";
		cday+=cday>=0?1:-1;
		cde+=cday+"/";
		cday+=ms<0?-2:0;
		cde+=cday+"/";
		log.debug(cde);
		ts.setCday(cday);
	}
	 * */
	private void addTs(Tree timesT, Ts ts) {
		int s = 1;
		Times timesC = (Times) getClassM().get(timesT.getIdClass());
		if(timesC!=null && timesC.directionRelvalue()<0) s=0-s;
		while (!planTsS.add(ts)) ts.addTime(s);
		getTimesTs(timesT).add(ts);
	}
	public int getTsNrInTask(Ts ts) {
		int n = 0;
		for (Tree dayT : ts.getTaskT().getChildTs()) if(isDayO(dayT))
			for (Tree timesT : dayT.getChildTs()) if(isTimesO(timesT))
				for (Ts ts2 : getTimesTs(timesT)) {
					n++;
					if(ts.equals(ts2))
						return n;
				}
		return n;
	}
	public Ts getTimesTs(Tree timesT, int i) {
		Set<Ts> tt = timesTsMp.get(timesT);
		int n = 1;
		if(null!=tt)
			for (Ts ts : tt) 
				if(n++==i)
					return ts;
		return null;
	}
	public Ts getDrugTs1(Tree drugT) {
		Set<Ts> drugTs = getDrugTs(drugT);
		if(null!=drugTs)
			for (Ts ts : drugTs) {
				return ts;
			}
		return null;
	}
	public Set<Ts> getDrugTs(Tree drugT) {
		Set<Ts> drugTs = null;
		for (Tree dayT : drugT.getChildTs()) 
		{
			if(isDayO(dayT)){
				for (Tree timesT : dayT.getChildTs()) 
				{
					if(isTimesO(timesT)){
						drugTs = getTimesTs().get(timesT);
					}
				}
			}
		}
		return drugTs;
	}
	public Set<Ts> getTimesTs(Tree timesT) {
		Set<Ts> tt = getTimesTs().get(timesT);
		if(tt==null){
			tt = new ConcurrentSkipListSet<Ts>();
			timesTsMp.put(timesT, tt);
		}
		return tt;
	}
	public Map<Tree, Set<Ts>>	getTimesTs(){
		if(timesTsMp==null)
			getPlan();
		return timesTsMp;
	}
	Map<Integer,Set<Ts>> rqmMS=new HashMap<Integer, Set<Ts>>();
	public Map<Integer, Set<Ts>> getRqmMS() {return rqmMS;}
	HashMap<Tree, Tree> dayRqm = new HashMap<Tree, Tree>();
	private void rqmDayM(Ts ts) {
		if(dayRqm.containsKey(ts.getTimesT().getParentT())){
			Integer day = ts.getCday();
			Set<Ts> rqmDayS = rqmMS.get(day);
			if(rqmDayS==null){
				rqmDayS=new HashSet<Ts>();
				rqmMS.put(day, rqmDayS);
			}
			rqmDayS.add(ts);
		}
	}
	private Ts getTaskTs(){
		if(taskTs==null){
			long millis = getBeginCalendar().getMillis();
			taskTs = new Ts(millis, getDocT(), 1);
//			taskTs = new Ts(getBeginCalendar().getTimeInMillis(), getDocT(), 1);
		}
		return taskTs;
	}
	
	public void changeIdClass(Integer idDose, EntityManager em)
	{
		Tree doseT = getEditNodeT();
		if("drug".equals(doseT.getTabName()))
			doseT= JXPathBean.getDose(doseT);
		doseT.setIdClass(idDose);
		addClassM(doseT, em);
		nextCurrentId();
	}
	
	/*******************************************************
	 * 
	 * 	LABOR EDITOR SECTION
	 * 	Labor Editor Section
	 * 
	 *******************************************************/
	private	Labor	editedLaborC;
	public Labor getEditedLaborC() {return editedLaborC;}
	public void openEditLabor()
	{
		editedLaborC=new Labor();
		if(getEditNodeT().getIdClass()!=null){
			Labor editNodeLabor =(Labor)getClassM().get(getEditNodeT().getIdClass());
			editedLaborC.setLabor(editNodeLabor.getLabor());
			editedLaborC.setUnit(editNodeLabor.getUnit());
		}
	}

	private	Symptom	editSymptomC;

	public Symptom getEditSymptomC() {return editSymptomC;}
	public void openEditSymptom()
	{
		editSymptomC= new Symptom();
		if(getEditNodeT().getIdClass()!=null)
		{
			Symptom editNodeAppC=(Symptom) getClassM().get(getEditNodeT().getIdClass());
			editSymptomC.setSymptom(editNodeAppC.getSymptom());
		}else{
			editSymptomC.setSymptom("");
		}
	}

//	private JXPathContext newContext;
	private List<Map<String, Object>>
		drugUsedDose, drugUsedDayTimes, drugUsedDay, drugUsedTimes, drugUsedNotice, drugTask;
	public List<Map<String, Object>> getDrugUsedDayTimes()	{return drugUsedDayTimes;}
	public List<Map<String, Object>> getDrugUsedDose()		{return drugUsedDose;}
	public List<Map<String, Object>> getDrugUsedDay()		{return drugUsedDay;}
	public List<Map<String, Object>> getDrugUsedTimes()		{return drugUsedTimes;}
	public List<Map<String, Object>> getDrugUsedNotice()	{return drugUsedNotice;}
	public List<Map<String, Object>> getDrugTask()			{return drugTask;}

	public static String getSqlFolderRecursive(int id) {
		return sqlFolderRecursive.replace("x1", ""+id);
	}
	
	private static String 
	sqlFolderRecursive =
	" WITH RECURSIVE fr AS (" +
	" SELECT f.* FROM folder AS f WHERE f.idfolder = x1 " +
	" UNION " +
	" SELECT f.* FROM folder AS f, fr WHERE fr.idfolder = f.fdid " +
	" ) SELECT idfolder FROM fr ",
	sqlTaskInFolder=
	" SELECT folder,idfolder,t2.iddoc AS taskIddoc,t2.id AS taskId" +
	" FROM folder,tree t1,tree t2 " +
	" WHERE idfolder IN (" +getSqlFolderRecursive(1263295)+" ) AND idfolder=t1.did " +
	" AND t1.id=t2.iddoc AND t2.id=t2.idclass AND t2.id!=t1.id",
	//END FOLDER SELECT
	sqlDrugDocument = 
	" SELECT t1.*,t0.*" +
	" FROM (" +sqlTaskInFolder +") t0,tree t1,drug " +
	" WHERE t0.taskId=t1.iddoc AND t1.idclass=iddrug" +
	" AND idgeneric=?",
	sqlDrugDoseDocument = 
	" SELECT t1.*,t0.*,t2.id AS doseId " +
	" FROM (" +sqlTaskInFolder +") t0, tree t1,drug,tree t2 " +
	" WHERE t0.taskId=t1.iddoc AND t1.idclass=iddrug " +
	" AND t1.id=t2.did AND t2.tab_name='dose' " +
	" AND idgeneric=? AND t2.idclass=? ",
	sqlDrugDayDocument = 
	" SELECT t1.*,t0.*,t2.id AS dayId " +
	" FROM (" +sqlTaskInFolder +") t0, tree t1,drug,tree t2 " +
	" WHERE t0.taskId=t1.iddoc AND t1.idclass=iddrug " +
	" AND t1.id=t2.did AND t2.tab_name='day' " +
	" AND idgeneric=? AND t2.idclass=? ",
	sqlDrugTimesDocument = 
	" SELECT t1.*,t0.*,t2.id AS dayId " +
	" FROM (" +sqlTaskInFolder +") t0, tree t1,drug,tree t2,tree t3 " +
	" WHERE t0.taskId=t1.iddoc AND t1.idclass=iddrug " +
	" AND t1.id=t2.did AND t2.id=t3.did AND t3.tab_name='times' " +
	" AND idgeneric=? AND t3.idclass=? ",
	sqlDrugDayTimesDocument = 
	" SELECT t1.*,t0.*" +
	" FROM (" +sqlTaskInFolder +") t0,tree t1,drug,tree t2,tree t3" +
	" WHERE t0.taskId=t1.iddoc AND t1.idclass=iddrug" +
	" AND t1.id=t2.did AND t2.id=t3.did AND t3.tab_name='times' " +
	" AND idgeneric=? AND t2.idclass x1 AND t3.idclass x2",
	/*
	" SELECT t1.*,t0.iddoc AS taskIddoc,t0.id AS taskId" +
	" FROM tree t0,tree t1,drug,tree t2,tree t3" +
	" WHERE t0.id=t0.idclass AND t0.id=t1.iddoc AND t1.idclass=iddrug" +
	" AND t2.did=t1.id AND t3.did=t2.id" +
	" AND idgeneric=? AND t2.idclass x1 AND t3.idclass x2",
	 */
	//END INNER SELECT
	sqlDrugUsedDose =
	" SELECT count(*) AS cnt,unit,value,app,pro,iddose " +
	" FROM (x1) t1, tree t2, dose " +
	" WHERE t1.id=t2.did AND t2.idclass=iddose " +
	" GROUP BY unit,value,app,pro,iddose ORDER BY cnt DESC ",
	sqlDrugUsedDay = 
	" SELECT count(*) AS cnt,abs,newtype,idday " +
	" FROM (x1) t1, tree t2 LEFT JOIN day ON t2.idclass=idday " +
	" WHERE t1.id=t2.did " +
	" GROUP BY abs,newtype,idday ORDER BY cnt DESC ",
	sqlDrugUsedTimes = 
	" SELECT count(*) AS cnt,abs,apporder,visual,relunit,relvalue,idtimes " +
	" FROM (x1) t1, tree t2,tree t3 LEFT JOIN times ON t3.idclass=idtimes " +
	" WHERE t1.id=t2.did AND t2.id=t3.did " +
	" GROUP BY abs,apporder,visual,relunit,relvalue,idtimes ORDER BY cnt DESC ",
	sqlDrugUsedDayTimes1 =
	" SELECT t2.idclass idday, count(t2.idclass) cntDay, " +
	" t3.idclass idtimes, count(t3.idclass) cntTimes " +
	" FROM (x1) t1, tree t2,tree t3 " +
	" WHERE t1.id=t2.did AND t2.id=t3.did and t3.tab_name='times' " +
	" GROUP BY t3.idclass,t2.idclass ",
	sqlDrugUsedDayTimes =
	"SELECT d1.abs dabs, t2.abs AS tabs, * FROM (" +sqlDrugUsedDayTimes1+" ) t1" +
	" LEFT JOIN day d1 ON t1.idday=d1.idday" +
	" LEFT JOIN times t2 ON t1.idtimes=t2.idtimes" +
	" ORDER BY cntDay DESC, cntTimes DESC",
	sqlDrugUsedNotice = 
	" SELECT n1.*,cnt FROM notice n1," +
	" (SELECT idnotice, count(*) AS cnt FROM (x1) t1, tree t2,tree t3, notice " +
	" WHERE t1.id=t2.did AND t2.id=t3.did AND t3.idclass=idnotice " +
	" GROUP BY idnotice) n2 " +
	" WHERE n1.idnotice=n2.idnotice ORDER BY cnt DESC ",
	sqlDrugDoseUsedNotice = 
	" SELECT n1.*,cnt FROM notice n1, "+
	" (SELECT idnotice, count(*) AS cnt FROM ("+sqlDrugDocument+") t1, tree t2,tree t3, notice,tree t4,dose "+
	" WHERE t1.id=t2.did AND t2.id=t3.did AND t3.idclass=idnotice " +
	" AND t1.id=t4.did AND t4.idclass=iddose AND iddose=? "+
	" GROUP BY idnotice) n2 " +
	" WHERE n1.idnotice=n2.idnotice ORDER BY cnt DESC ",
	sqlDrugDoseTask =
	" SELECT * FROM (x1) t1,task WHERE t1.taskId=idtask ORDER BY folder,task";
	
	public void drugUsedDose_old(SimpleJdbcTemplate simpleJdbc) {
		String sql;
		if(hasDayTimes()){
			sql = addInnerDayTimesSql();
		}else{
			sql = getInnereSql();
		}
		sql = sqlDrugUsedDose.replace("x1", sql);
		drugUsedDose = simpleJdbc.queryForList(sql, getDocT().getId());
	}
	public void drugUsedDose(SimpleJdbcTemplate simpleJdbc) {
		if(hasDay()){
			drugUsedDose = drugUsedDay(sqlDrugUsedDose, simpleJdbc);
		}else if(hasTimes()){
		}else{
			drugUsedDose = drugUsed(sqlDrugUsedDose, simpleJdbc);
		}
		
	}
	public void drugUsedDay(SimpleJdbcTemplate simpleJdbc) {
		if(hasDose()){
			drugUsedDay = drugUsedDose(sqlDrugUsedDay, simpleJdbc);
		}else{
			drugUsedDay = drugUsed(sqlDrugUsedDay, simpleJdbc);
		}
	}
	public void drugUsedTimes(SimpleJdbcTemplate simpleJdbc) {
		Integer idGeneric = getDocT().getId();
		if(hasDose()){
			drugUsedTimes = drugUsedDose(sqlDrugUsedTimes, simpleJdbc);
		}else if(hasDay()){
			drugUsedTimes = drugUsedDay(sqlDrugUsedTimes, simpleJdbc);
		}else{
			drugUsedTimes = drugUsed(sqlDrugUsedTimes, simpleJdbc);
		}
	}
	public void drugUsedNotice(SimpleJdbcTemplate simpleJdbc){
		if(hasDose()){
			drugUsedNotice = drugUsedDose(sqlDrugUsedNotice, simpleJdbc);
		}else if(hasDayTimes()){
			String sql = addInnerDayTimesSql();
			sql = sqlDrugUsedNotice.replace("x1", sql);
			drugUsedNotice = simpleJdbc.queryForList(sql, getDocT().getId());
		}else{
			drugUsedNotice = drugUsed(sqlDrugUsedNotice, simpleJdbc);
		}
	}
	private List<Map<String, Object>> drugUsedDay(String sql, SimpleJdbcTemplate simpleJdbc) {
		return drugUsedIdclass(sql, sqlDrugDayDocument, OwsSession.getOwsSession().getIdDay(), simpleJdbc);
	}
	private List<Map<String, Object>> drugUsedDose(String sql, SimpleJdbcTemplate simpleJdbc) {
		return drugUsedIdclass(sql, sqlDrugDoseDocument, OwsSession.getOwsSession().getIdDose(), simpleJdbc);
	}
	private List<Map<String, Object>> drugUsedIdclass(String sql,String sql2, Integer idClass, SimpleJdbcTemplate simpleJdbc) {
		Integer idGeneric = getDocT().getId();
		sql = sql.replace("x1", sql2);
		return simpleJdbc.queryForList(sql, idGeneric,idClass);
	}
	private List<Map<String, Object>> drugUsed(String sql, SimpleJdbcTemplate simpleJdbc) {
		Integer idGeneric = getDocT().getId();
		sql = sql.replace("x1", sqlDrugDocument);
		return simpleJdbc.queryForList(sql, idGeneric);
	}
	private String getInnereSql() {
		Integer idDoc	= getDocT().getId();
		String sql;
		Integer idDose = OwsSession.getOwsSession().getIdDose();
		if(hasDayTimes()){
			sql = addInnerDayTimesSql();
//		}else if(null!=idDose){
//			sql = sqlDrugDoseDocument;
		}else{
			sql = sqlDrugDocument;
		}
		return sql;
	}
	private String addInnerDayTimesSql() {
		Integer idDay	= OwsSession.getOwsSession().getIdDay();
		Integer idTimes	= OwsSession.getOwsSession().getIdTimes();
		String sql = sqlDrugDayTimesDocument.replaceFirst("x1", null==idDay?" IS NULL ":(" = "+idDay));
		sql = sql.replaceFirst("x2", null==idTimes?" IS NULL ":(" = "+idTimes));
		return sql;
	}
	
	private boolean hasDay() {
		return null!=OwsSession.getOwsSession().getIdDay();
	}
	private boolean hasTimes() {
		return null!=OwsSession.getOwsSession().getIdTimes();
	}
	private boolean hasNotice() {
		return null!=OwsSession.getOwsSession().getIdNotice();
	}
	private boolean hasDose() {
		return null!=OwsSession.getOwsSession().getIdDose();
	}
	private boolean hasDayTimes() {
		return null!=OwsSession.getOwsSession().getIdDay()
		||null!=OwsSession.getOwsSession().getIdTimes();
	}
	public void drugUsedDayTimes(SimpleJdbcTemplate simpleJdbc){
		Integer idDoc=getDocT().getId();
		String sql ;
		if(hasDose()){
			sql = sqlDrugUsedDayTimes.replace("x1", sqlDrugDoseDocument);
			Integer idDose = OwsSession.getOwsSession().getIdDose();
			drugUsedDayTimes = simpleJdbc.queryForList(sql, idDoc,idDose);
		}else{
			sql = sqlDrugUsedDayTimes.replace("x1", sqlDrugDocument);
			drugUsedDayTimes = simpleJdbc.queryForList(sql, idDoc);
		}
//		drugUsedDayTimes = simpleJdbc.queryForList(sqlDrugUsedDayTimes, idDoc);
	}
	
	public void drugTask(SimpleJdbcTemplate simpleJdbc){
		Integer idGeneric = getDocT().getId();
//		reserv1(idDose);
		String sql;
		if(hasDay()){
			Integer idDay = OwsSession.getOwsSession().getIdDay();
			sql = sqlDrugDoseTask.replace("x1", sqlDrugDayDocument);
			drugTask = simpleJdbc.queryForList(sql, idGeneric,idDay);
		}else if(hasDose()){
			Integer idDose = OwsSession.getOwsSession().getIdDose();
			sql = sqlDrugDoseTask.replace("x1", sqlDrugDoseDocument);
			drugTask = simpleJdbc.queryForList(sql, idGeneric,idDose);
		}else if(hasTimes()){
			Integer idTimes = OwsSession.getOwsSession().getIdTimes();
			sql = sqlDrugDoseTask.replace("x1", sqlDrugTimesDocument);
			drugTask = simpleJdbc.queryForList(sql, idGeneric,idTimes);
		}else{
			sql = sqlDrugDoseTask.replace("x1", sqlDrugDocument);
			drugTask = simpleJdbc.queryForList(sql, idGeneric);
		}
	}
	
	public void drugUsedNotice_Old(SimpleJdbcTemplate simpleJdbc){
		Integer idDoc=getDocT().getId();
		Integer idDose = OwsSession.getOwsSession().getIdDose();
		if(hasDayTimes()){
			drugUsedNotice = simpleJdbc.queryForList(sqlDrugUsedNotice, idDoc);
		}else if(hasDose()){
			drugUsedNotice = simpleJdbc.queryForList(sqlDrugDoseUsedNotice, idDoc,idDose);
		}else{
			drugUsedNotice = simpleJdbc.queryForList(sqlDrugUsedNotice, idDoc);
		}
	}
	public void paste(){
		log.debug("----");
		if(null!=getIdClass2copy()){
			log.debug("----");
			pasteSourceTarget();
		}else{
			log.debug("----");
			paste(getCopyNodeT());
		}
	}
	public void paste(Tree copyNodeT) {
		log.debug("----"+copyNodeT);
		if(copyNodeT==null){
			log.info("it's not to copy");
			return;
		}
		log.debug("----");
		Tree parentT = getTree().get(getIdc());
		if("task".equals(parentT.getTabName())){
			if("drug".equals(copyNodeT.getTabName())){
				addPaste(parentT);
				addCopyRef(copyNodeT);
			}else if("finding".equals(copyNodeT.getTabName())){
				addPaste(parentT);
			}else if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT);
			}else if("task".equals(copyNodeT.getTabName())){
				if(!"task".equals(parentT.getParentT().getTabName())
					&&!"task".equals(copyNodeT.getParentT().getTabName())){
					setAction("copyTaskAll");
				}
			}
		}else if("times".equals(parentT.getTabName())){
			if("times".equals(copyNodeT.getTabName())){
				parentT.setIdClass(copyNodeT.getIdClass());
				if(null==parentT.getMtlO()&&null!=copyNodeT.getMtlO()){
				}
			}
		}else if("labor".equals(parentT.getTabName())){
			if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT.getParentT());
			}
		}else if("notice".equals(parentT.getTabName())){
			if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT.getParentT());
			}
		}else if("day".equals(parentT.getTabName())){
			if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT);
			}else if("drug".equals(parentT.getParentT().getTabName())){
				if("day".equals(copyNodeT.getTabName())){
					addPaste(parentT.getParentT());
					addCopyRef(copyNodeT);
				}
			}
		}else if("drug".equals(parentT.getTabName())){
			if("drug".equals(copyNodeT.getTabName())){
				if	("task".equals(parentT.getParentT().getTabName())
					&&	"task".equals(copyNodeT.getParentT().getTabName())
					)
				{
					addPaste(parentT.getParentT());
					addCopyRef(copyNodeT);
				}
			}else if("notice".equals(copyNodeT.getTabName())||
					"expr".equals(copyNodeT.getTabName())){
				if("dose".equals(copyNodeT.getParentT().getTabName())){
					for(Tree doseT:parentT.getChildTs())
						if("dose".equals(doseT.getTabName()))
							addPaste(doseT);
				}else{
					for(Tree t1:copyNodeT.getChildTs()){
						if("day".equals(t1.getTabName())){
							//						addPaste(parentT.getParentT());
							addPaste(parentT.getDocT());
							break;
						}
					}
				}
			}else if("day".equals(copyNodeT.getTabName())){
				
			}
		}else if(isExprO(parentT)){
			if(isExprO(copyNodeT)){
			}
		}else if(parentT.getMtlO() instanceof App){
			if("expr".equals(copyNodeT.getTabName())
			||"notice".equals(copyNodeT.getTabName())){
				addPaste(parentT);
			}
		}else if(parentT.getMtlO() instanceof Dose){
			if("dose".equals(copyNodeT.getTabName())){
				parentT.setIdClass(copyNodeT.getIdClass());
				nextCurrentId();
			}else if("expr".equals(copyNodeT.getTabName())){
				addPaste(parentT);
			}else if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT);
			}
		}
	}
	
	public void addCopyRef(Tree copyNodeT)
	{
		if(copyNodeT.getRef()!=null){
			Tree tree = getCopyNodeDocMtl().getTree().get(copyNodeT.getRef());
			if(tree==null)
				return;
			Integer idNew = getOld2newIdM().get(copyNodeT.getId());
			Tree treeNew = getTree().get(idNew);
			if(tree.equals(copyNodeT.getDocT())){
				Integer id = getDocT().getId();
				treeNew.setRef(id);
			}else if(treeNew.getDocT().getId().equals(copyNodeT.getDocT().getId())){
				treeNew.setRef(copyNodeT.getRef());
			}else{
				addRef(copyNodeT);
			}
		}
		if(copyNodeT.getChildTs()!=null)
			for(Tree t:copyNodeT.getChildTs())
				addCopyRef(t);
	}
	
	//calc Ts begin 2010.09.04
	private Map<Long,TsTask> plan2TsS;
	private Map<Long,WeekDrug> weekDrugM;
	public Collection<WeekDrug> getWeekDrugCollection() {
		getCalcTs();
		return weekDrugM.values();
	}
	public Collection<TsTask> getPlanNew() {
		getCalcTs();
		return plan2TsS.values();
	}
	public Map<Long,TsTask> getCalcTs() {
		if(plan2TsS!=null)
			return plan2TsS;
		else{
			plan2TsS = new ConcurrentSkipListMap<Long,TsTask>();
			weekDrugM = new ConcurrentSkipListMap<Long,WeekDrug>();
		}
		if(!getDocT().hasTses())
			getDocT().addTs(new TsTask(getTimeInMS(0), getDocT(), 1),plan2TsS);
		refSeq=new HashSet<Tree>();//Infinite loop control.
		for (Integer id : getTimesM().keySet()) {
			Tree timesT = getTimesM().get(id);
			if(!timesT.hasTses()){
				refSeq.add(timesT);//Infinite loop control.
				calc1Times(timesT);
			}
		}
		setPlanWeekDayNumber();
		return plan2TsS;
	}
	Set<Tree> refSeq;//Infinite loop control in times references chain.
	Map<Integer,WeekDrug> weekDrugdayIdM;// DayId to weekDrug.
	public Map<Integer, WeekDrug> getWeekDrugdayIdM()	{return weekDrugdayIdM;}
	private void setPlanWeekDayNumber() {
		int dayNum = 1;
		weekDrugdayIdM=new HashMap<Integer,WeekDrug>();
		for (TsTask ts : plan2TsS.values()) {
			if(ts.getTimesT().getParentT().getParentT().getMtlO() instanceof Protocol)
				continue;
			ts.setNr(dayNum++);
			Integer dayId = ts.getTimesT().getParentT().getId();
			if(!weekDrugdayIdM.containsKey(dayId)) {
				WeekDrug weekDrug = new WeekDrug(ts);
				weekDrugM.put(ts.getTime(),weekDrug);
				weekDrugdayIdM.put(dayId, weekDrug);
			}
		}
		int weekNum = 0;
		for (WeekDrug weekDrug : weekDrugM.values())
			weekDrug.setNumber(weekNum++);
	}
	private void calc1Times(Tree timesT) {
		Tree timesT_refT = getDocT();
		if(timesT.getRef()!=null && !timesT.getRef().equals(getDocT().getId()))
			timesT_refT = getTimesM().get(timesT.getRef());
		if(refSeq.contains(timesT_refT))//Infinite loop control.
			timesT_refT = getDocT();
		else
			refSeq.add(timesT_refT);
		if(!timesT_refT.hasTses()) 
			calc1Times(timesT_refT);
		calcTimes(timesT,timesT_refT);
	}
	private void calcTimes(Tree timesT, Tree timesT_refT){
		refSeq.clear();//Infinite loop control.
		String absDayVal=getDayAbs2(timesT.getParentT());
		if(absDayVal==null)
			return;
		Times timesC = (Times) timesT.getMtlO();
//		Matcher m_absPeriod = JsfFunctions.getAbsPeriod().matcher(absDayVal);
		Matcher m_absPeriod = JXPathBean.getAbsPeriod().matcher(absDayVal);
		if(m_absPeriod.matches()){
			int b = Integer.parseInt(m_absPeriod.group(1));
			int e = Integer.parseInt(m_absPeriod.group(2));
			for (int day = b; day <= e; day++)
				makeTs(timesT,day,timesT_refT);
		}else if(absDayVal.indexOf(",")>0){
			for(String abs1:absDayVal.split(",")){
				int day = Integer.parseInt(abs1);
				makeTs(timesT,day,timesT_refT);
			}
		}else if(timesC!=null&&!timesC.allNull()){
			Matcher m_absDayMinus = absDayMinus.matcher(absDayVal);
			if(m_absDayMinus.find()){
				int day = Integer.parseInt(m_absDayMinus.group());
				makeTs(timesT,day,timesT_refT);
			}
		}else{//day==1day and timeC==null
			int day = Integer.parseInt(absDayVal);
			makeTs(timesT,day,timesT_refT);
		}
	}
	private void makeTs(Tree timesT,Integer day, Tree timesT_refT) {
		long time =0;
		Times timesO = (Times) timesT.getMtlO();
		if(timesO!=null&&timesO.hasAbs()){
			String abs = timesO.getAbs();
			if(abs.indexOf(",")>0){
				for(String abs1:abs.split(",")){
					addTs(timesT, day, abs1);
				}
			}else if(abs.indexOf("=")>=0){
				addTs(timesT, day, "22:00");
			}else{
				addTs(timesT, day, abs);
			}
		}else{
			for (TsTask ts : timesT_refT.getTses()) 
				if(day.equals(ts.getDay())) {
					time = ts.getTime()+getDistance(timesT);
					break;
				}
			if(time==0)
				time = getTimeInMS(day);
			addTs(timesT, day, time);
		}
	}
	private void addTs(Tree timesT, Integer day, String hhmmStr) {
		String[] hhmm = hhmmStr.split(":");
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getTimeInMS(day));
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmm[0]));
		c.set(Calendar.MINUTE, Integer.parseInt(hhmm[1]));
		long time = c.getTimeInMillis();
		addTs(timesT, day, time);
	}
	private void addTs(Tree timesT, Integer day, long time) {
		TsTask ts = new TsTask(time, timesT, day);
		timesT.addTs(ts,plan2TsS);
//		setCday2(ts);
	}
	private int getDistance(Tree timesT) {
		Times timesO=(Times)timesT.getMtlO();
		if(timesO!=null&&timesO.getAbs()==null){
			return timesO.getDistance();
		}
		return 0;
	}
	private long getTimeInMS(Integer day) {
		int addDay = day>0?day-1:day;
		return getBeginCalendar().plusDays(addDay).getMillis()+1;
	}
	/*
	private long getTimeInMS(Integer day) {
		Calendar c = getBeginCalendar();
		int addDay = day>0?day-1:day;
		c.add(Calendar.DAY_OF_MONTH, addDay);
		return c.getTimeInMillis()+1;
	}
	 * */
	private String getDayAbs2(Tree dayT) {
		if(dayT.getMtlO()==null)
			return null;
		Day dayC=(Day) dayT.getMtlO();
		String absDayVal = dayC.getAbs();
		if(absDayVal.length()==0)
			return null;
		return absDayVal;
	}
	
	
	public DateTime getBeginCalendar() {
		if(null==beginTherapy)
		{
//			beginTherapy=new DateTime().withTime(9, 0, 0, 100);
			beginTherapy=new DateTime().withTime(10, 0, 0, 100);
			History ofDate = getOfDate();
			if(null!=ofDate&&null!=ofDate.getMdate()){
				log.debug(ofDate);
				beginTherapy=new DateTime(ofDate.getMdate().getTime());
				if(0==beginTherapy.hourOfDay().get()){
					beginTherapy=beginTherapy.withTime(4, 0, 0, 100);
				}
			}
			setBeginHHMM(DateTimeFormat.forPattern("kk:mm").print(beginTherapy));
		}
		return beginTherapy;
	}
	DateTime	beginTherapy;
	/*
	public Calendar getBeginCalendar() {
		if(null==beginTherapy)
		{
			beginTherapy=Calendar.getInstance();
			History ofDate = getOfDate();
			if(null!=ofDate){
				log.debug(ofDate);
				beginTherapy.setTime(getOfDate().getMdate());
				if(0==beginTherapy.get(Calendar.HOUR_OF_DAY))
					beginTherapy.set(Calendar.HOUR_OF_DAY,4);
			}else{
				beginTherapy.set(Calendar.HOUR_OF_DAY,	9);
				beginTherapy.set(Calendar.MINUTE,		0);
				beginTherapy.set(Calendar.SECOND,		0);
			}
			SimpleDateFormat df = new SimpleDateFormat("kk:mm");
			setBeginHHMM(df.format(beginTherapy.getTime()));
			beginTherapy.set(Calendar.MILLISECOND,	100);
		}
		return beginTherapy;
	}
	 * */
	/*
	Calendar	beginTherapy;
	public Calendar getBeginCalendar() {
		if(beginTherapy==null){
			beginTherapy = getBeginCalendar2();
			if(getOfDate()!=null){
				log.debug(getOfDate());
				beginTherapy.setTime(getOfDate().getMdate());
				if(0==beginTherapy.get(Calendar.HOUR_OF_DAY))
					beginTherapy.set(Calendar.HOUR_OF_DAY,4);
				SimpleDateFormat df = new SimpleDateFormat("kk:mm");
				setBeginHHMM(df.format(beginTherapy.getTime()));
			}
			beginTherapy.set(Calendar.SECOND, 5);
		}
		return beginTherapy;
	}
	 */
	public History getOfDate() {
		if(ofDate==null)
			if(getPatientMtl()!=null){
				for(Tree t:getPatientMtl().getDocT().getChildTs()){
					if(getDocT().getId().equals(t.getRef())){
						for(Tree t2:t.getChildTs())
							if(t2.getHistory()!=null)
								ofDate=t2.getHistory();
					}
				}
			}else{
				ofDate=new History();
//				Calendar c = getBeginCalendar();
//				long timeInMillis = c.getTimeInMillis();
				long timeInMillis = getBeginCalendar().getMillis();
				ofDate.setMdate(new Timestamp(timeInMillis));
			}
		return ofDate;
	}
	private History ofDate;

	
	public Tree getDoseNoticeParentT(){
		Tree doseNoticeParentT = getEditNodeT();
		if(isDrugO(doseNoticeParentT))
			for(Tree doseT:doseNoticeParentT.getChildTs())
				if(isDoseO(doseT))
					doseNoticeParentT=doseT;
		return doseNoticeParentT;
	}
	public void setDoseNoticeParentT_old(int id){
		setEditNodeT();
		Tree editDrugT = getEditNodeT();
		if("drug".equals(editDrugT.getTabName()))
			for(Tree t:editDrugT.getChildTs())
				if("dose".equals(t.getTabName()))
					editDrugT=t;
//		setParentT(editDrugT);
	}
	public void setEditDrugT() {
		log.debug("----------");
		setEditNodeT();
		log.debug("----------");
		Tree editDrugT = getEditNodeT();
		if("dose".equals(editDrugT.getTabName()))
			editDrugT=editDrugT.getParentT();
		if("drug".equals(editDrugT.getParentT().getTabName()))
			editDrugT=editDrugT.getParentT();
//		setParentT(editDrugT);
	}
	public boolean refLoop(Tree tree){
		int i = 0;//end los loop control
		while(null!=tree.getRef()&&i++<8){
			if(tree.getRef().equals(getEditNodeT().getId()))
				return true;
			else
				tree =getTree().get(tree.getRef());
		}
		return false;
	}
	
	public void rewrite(MtlDbService documentService, OwsSession owsSession) {
		Integer idGeneric = getDocT().getId();
		Integer idClassS = owsSession.getSourceIdClass();
		if(0==idClassS)idClassS=null;
		Integer idClassT = owsSession.getTargetIdClass();
		Tree classT = owsSession.getSourceT();
		if(null==classT)
			classT = owsSession.getTargetT();
		if(null==classT||!classT.getId().equals(idClassT)){
			classT=documentService.getEntityManager().find(Tree.class, idClassT);
		}
		
		if(null==classT)	return;
		String whereIdClass ;
		if(null==idClassT || 0==idClassT){
			whereIdClass = " AND tT.idclass IS NULL ";
		}else{
			whereIdClass = " AND tT.idclass="+idClassT;
		}
		String sql =" UPDATE tree SET idclass=? WHERE id IN (x1) ";
		if("times".equals(classT.getTabName())
			||"notice".equals(classT.getTabName())
		){
			sql=sql.replace("x1", 
			" SELECT tT.id FROM (x1) t1, tree t2, tree tT " +
			" WHERE t1.id=t2.did AND t2.id=tT.did AND tT.tab_name='" +classT.getTabName() +"' " +whereIdClass 
			);
		}else if("day".equals(classT.getTabName())
			||"dose".equals(classT.getTabName())
		){
			sql=sql.replace("x1", 
			" SELECT tT.id FROM (x1) t1, tree tT " +
			" WHERE t1.id=tT.did AND tT.tab_name='" +classT.getTabName() +"' " +whereIdClass
			);
		}else{
			return;
		}
		if(hasDay()){
			Integer idDay = owsSession.getIdDay();
			sql =sql.replace("x1", sqlDrugDayDocument);
			documentService.getSimpleJdbc().update(sql, idClassS,idGeneric,idDay);
		}else if(hasDose()){
			Integer idDose = owsSession.getIdDose();
			sql =sql.replace("x1", sqlDrugDoseDocument);
			documentService.getSimpleJdbc().update(sql, idClassS,idGeneric,idDose);
		}else if(hasNotice()){
		}else {
			sql =sql.replace("x1", sqlDrugDocument);
			documentService.getSimpleJdbc().update(sql, idClassS,idGeneric);
		}
		owsSession.setTargetIdClass(null);
		owsSession.setTargetT(null);
	}
	public void rewrite_danger2(MtlDbService documentService, OwsSession owsSession) {
		Integer idGeneric = getDocT().getId();
		String sql =" UPDATE tree SET idclass=? WHERE id IN " +
		" ( SELECT t1.id FROM (x1) t0, tree t1 WHERE t0.taskId=t1.iddoc AND t1.idclass=? ) ";
		Integer idClassS = owsSession.getSourceIdClass();
		if(0==idClassS)idClassS=null;
		Integer idClassT = owsSession.getTargetIdClass();
		if(hasDay()){
			Integer idDay = owsSession.getIdDay();
			sql =sql.replace("x1", sqlDrugDayDocument);
			documentService.getSimpleJdbc().update(sql, idClassS,idGeneric,idDay,idClassT);
		}else if(hasDose()){
			Integer idDose = owsSession.getIdDose();
			sql =sql.replace("x1", sqlDrugDoseDocument);
			documentService.getSimpleJdbc().update(sql, idClassS,idGeneric,idDose,idClassT);
		}else if(hasNotice()){
			sql =sql.replace("x1", sqlDrugDocument);
			documentService.getSimpleJdbc().update(sql, idClassS,idGeneric,idClassT);
		}
		owsSession.setTargetIdClass(null);
		owsSession.setTargetT(null);
	}
	public void rewrite_danger(MtlDbService documentService, OwsSession owsSession) {
		String innerSqlNotice=
		" SELECT t3.id FROM tree t0, tree t1,drug, tree t2, tree t3" +
		" WHERE t0.id=t0.idclass AND t0.id=t1.iddoc AND t1.idclass=iddrug AND idgeneric=?" +
		" AND t2.did=t1.id AND t3.did=t2.id" +
		" AND t3.idclass=? ";
//		" AND t3.idclass=? ";
		String innerSql = innerSqlNotice;
		Integer idClassT = owsSession.getTargetIdClass();
		if("dose".equals(owsSession.getTargetT().getTabName())){
			innerSql=" SELECT t1.doseId FROM (" +sqlDrugDoseDocument +") t1";
			idClassT = owsSession.getTargetT().getMtlO().getId();
		}
		String sql ="UPDATE tree SET idclass=? WHERE id IN (" +innerSql+")";
		Integer idClassS = owsSession.getSourceIdClass();
		Integer idDrug = getDocT().getId();
		documentService.getSimpleJdbc().update(sql, idClassS,idDrug,idClassT);
		owsSession.setTargetIdClass(null);
		owsSession.setTargetT(null);
	}
	public String getDddSql() {
		Tree doseDayT = getEditNodeT().getParentT();
		Tree drugT = doseDayT.getParentT();
		Integer idGeneric = ((Drug) drugT.getMtlO()).getGeneric().getId();
		String sql = "" +
		" SELECT * FROM notice n1,(" +
		" SELECT idnotice idclass, count(*) cnt FROM (" +sqlTaskInFolder +
		") t0, tree t1, drug,tree t2,tree t3, notice" +
		" WHERE t0.taskId=t1.iddoc" +
		" AND t1.id=t2.did AND t2.id=t3.did AND t3.idclass=idnotice" +
		" AND t1.idclass=iddrug AND idgeneric = " +idGeneric+
		" AND t2.tab_name='" +doseDayT.getTabName() +"'" +
		" GROUP BY idnotice" +
		" ) n2" +
		" WHERE n1.idnotice=n2.idclass" +
		" ORDER BY n2.cnt DESC ";
		return sql;
	}
	/*
	sqlDrugUsedNotice2=" SELECT n1.*,cnt FROM notice n1,(" +
	" SELECT idnotice, count(*) AS cnt FROM tree t1,drug,tree t2,tree t3,notice,tree t4" +
	" WHERE t1.idclass=iddrug AND idgeneric=?" +
	" AND t2.did=t1.id AND t3.did=t2.id AND t3.idclass=idnotice" +
	" AND t4.id=t1.iddoc AND t4.id=t4.idclass" +
	" GROUP BY idnotice ) n2" +
	" WHERE n1.idnotice=n2.idnotice" +
	" ORDER by cnt DESC",
	 */
	/*
	sqlDrugUsedDose =
	" SELECT count(*) AS cnt,unit,value,app,iddose "+
	" FROM tree t1,drug, tree t2, dose "+
	" WHERE t1.id=t2.did AND t2.idclass=iddose AND t1.idclass=iddrug and idgeneric=? "+
	" GROUP BY unit,value,app,iddose ORDER BY cnt DESC ",
	 */
	/*
	sqlDrugDocument = 
	" SELECT t1.*" +
	" FROM tree t0,tree t1,drug " +
	" WHERE t0.id=t0.idclass AND t0.id=t1.iddoc AND t1.idclass=iddrug" +
	" AND idgeneric=?",
	 */

	public void editStep(){
		if(!hasIdt()) setIdc(null);
	}

	private String tradeName;
	public String getTradeName()	{return tradeName;}
	public void setTradeName(String tradeName)		{this.tradeName = tradeName;}

	Drug drugO;
	public Drug getDrugO() {return drugO;}
	public void setDrugO(Drug drugO) {this.drugO=drugO;}
	/**
	 * --------------------------------------
	 * Rule section. BEGIN
	 * --------------------------------------
	 */
	
	public Tree getEditIfT() {return getIfT(getEditNodeT());}
	public Tree getIfT(Tree t) {
		if(!isExprO(t))
			return null;
		Tree ifT = t;
		while (null!=ifT && ifT!=getDocT()) {
			if(isIf(ifT))
				break;
			ifT=ifT.getParentT();
		}
		return ifT;
	}
	public Tree getElseT(Tree ifT) {
		if(!isIf(ifT))
			return null;
		if(null==ifT.getChildTs()
		|| 3!=ifT.getChildTs().size()
		)
			return null;
		return ifT.getChildTs().get(2);
	}
	public boolean isRuleNotValid(Tree ifT){
		if(isRuleEqualityNotValid(ifT))
			return true;
		return false;
	}
	private boolean isRuleEqualityNotValid(Tree ifT) {
		for(Tree eqT:ifT.getChildTs())
			if(isEquals(eqT))
			{
				if(isEqualityNotValid(eqT))
					return true;
			}else if(isAndOrExpr(eqT))
				return isRuleEqualityNotValid(eqT);
		return false;
	}
	private boolean isEqualityNotValid(Tree eqT) {
		Tree eq0T = eqT.getChildTs().get(0);
		if(!eq0T.hasChild())
			return true;
		Tree t1 = eq0T.getChildTs().get(0);
		return false;
	}
	public boolean isTsRule(Tree ifT){
		if(ifT.hasChild())
			for (Tree t : ifT.getChildTs()){
				if(isEquals(t)){
					if(t.getChildTs().get(0).hasChild())
						if(isTsExpr(t.getChildTs().get(0).getChildTs().get(0)))
							return true;
				}else if(isIf(t))
					return isTsRule(t);
			}
		return false;
	}
	private boolean isTsExpr(Tree tree) {
		boolean exprValue = 
				isExprValue(tree, "drugInput")||
				isExprValue(tree, "inputPauseForInit")
				;
		return exprValue;
	}
	public boolean isIf(Tree exprT)		{return isExprExpr(exprT, "if");}
	public boolean isEquals(Tree eqT)	{return isExprExpr(eqT, "equality");}
	public boolean isAndOrExpr(Tree eqT){return isExprExpr(eqT, "andOrExpr");}
	public boolean isThen(Tree exprT)	{return isExprExpr(exprT, "then");}
	public boolean isElse(Tree exprT)	{return isExprExpr(exprT, "else");}
	public boolean isElseIf(Tree exprT)	{
		if(exprT.hasChild())
			return isExprExpr(exprT.getChildTs().get(0), "if");
		return false;
	}
	public boolean isPvO(Tree t)		{return t.getMtlO() instanceof Pvariable;}
	public boolean isPvPv(String pvar, Tree t) {
		return isPvO(t) && pvar.equals(getPvariableO(t).getPvariable());
	}

	public static Tree		getDrugDoseT(Tree drugT) {
		if(isDrugO(drugT))
			for(Tree doseT:drugT.getChildTs())
				if(isDoseO(doseT))
					return doseT;
		return null;
	}

	public static Finding	getFindingO(Tree t)		{return (Finding) t.getMtlO();}
	public static Drug		getDrugO(Tree t)		{return (Drug) t.getMtlO();}
	public static Labor		getLaborO(Tree t)		{return (Labor) t.getMtlO();}
	public static Dose		getDoseO(Tree t)		{return (Dose) t.getMtlO();}
	public static App		getAppO(Tree t)			{return (App) t.getMtlO();}
	public static Expr		getExprO(Tree t)		{return (Expr) t.getMtlO();}
	public static Day		getDayO(Tree t)			{return (Day) t.getMtlO();}
	public static Times		getTimesO(Tree t)		{return (Times) t.getMtlO();}
	public static Tablet	getTabletO(Tree t)		{return (Tablet) t.getMtlO();}
	public static Pvariable	getPvariableO(Tree t)	{return (Pvariable) t.getMtlO();}
	
//	public static Ivariable	getIvariableO(Tree t)	{return (Ivariable) t.getMtlO();}
	public boolean isDoseType(Tree t, String type) {
		return isDoseO(t) && type.equals(getDoseO(t).getType());
	}
	public boolean isNoticeType(Tree t, String type) {
		return isNoticeO(t) && type.equals(((Notice) t.getMtlO()).getType());
	}
	/**
	 * @param exprT
	 * @param expr
	 * @return
	 */
	private boolean isExprExpr(Tree exprT, String expr) {
		return isExprO(exprT) && expr.equals(((Expr) exprT.getMtlO()).getExpr());
	}
	public boolean isIvariableIvariable(Tree exprT, String ivariable) {
		return isIvariableO(exprT) && ivariable.equals(((Ivariable) exprT.getMtlO()).getIvariable());
	}
	public static boolean isFindingO(Tree t)	{return null!=t && t.getMtlO() instanceof Finding;}
	public static boolean isPvariableO(Tree t)	{return null!=t && t.getMtlO() instanceof Pvariable;}
	public static boolean isExprO(Tree t)		{return null!=t && (t.getMtlO() instanceof Expr || "expr".equals(t.getTabName()));}
	public static boolean isDoseO(Tree t)		{return null!=t && (t.getMtlO() instanceof Dose || "dose".equals(t.getTabName()));}
	public static boolean isDoseNotNullO(Tree t){return null!=t && t.getMtlO() instanceof Dose;}
	public static boolean isTaskO(Tree t)		{
		return null!=t && (t.getMtlO() instanceof Task || "task".equals(t.getTabName()));}
	public static boolean isDrugO(Tree t)		{
		return null!=t && (t.getMtlO() instanceof Drug || "drug".equals(t.getTabName()));}
	public static boolean isLaborO(Tree t)		{
		return null!=t && (t.getMtlO() instanceof Labor || "labor".equals(t.getTabName()));}
	public static boolean isDayO(Tree t)		{
		return null!=t &&  t.getMtlO() instanceof Day  || "day".equals(t.getTabName());}
	public static boolean isAppO(Tree t)		{return null!=t &&  t.getMtlO() instanceof App;}
	public static boolean isTimesO(Tree t) {
		return t.getMtlO() instanceof Times||"times".equals(t.getTabName());
	}
	/**
	 * @param exprT
	 * @param expr
	 * @return
	 */
	private boolean isExprValue(Tree exprT, String expr) {
		return
		isExprO(exprT)
		&&
		expr.equals(((Expr) exprT.getMtlO()).getValue());
	}
	public Boolean isRuleForTs(Tree ifT, Ts processTs,JXPathBean jxp){
		String xp = "childTs/childTs/childTs[mtlO/value='inputPauseForInit']"
				+"|childTs/childTs/childTs/childTs[mtlO/value='inputPauseForInit']";
		Pointer inputPauseForInitP = jxp.jxp(ifT)
				.getPointer(xp);
		if(null!=inputPauseForInitP.getValue())
			if(1==getTaskTsNr(processTs))
				return true;
		return false;
	}
	/**
	 * Actuele rule process Ts object.
	 */
	Ts processTs=null;
	public Boolean isRightThenElse(Tree thenElseT, Ts processTs){
		log.debug("-------------");
		this.processTs=processTs;
		Tree ifT = thenElseT.getParentT();
		boolean ifIsTrue = isIfTrue(ifT);
		log.debug(ifIsTrue);
		boolean thenExpr = isExprExpr(thenElseT, "then");
		if(thenExpr)
			return ifIsTrue;
		else{//thenElseT is else
			
			if(isElseIf(thenElseT))
				return null;
			else{//thenElseT is else and not elseIf
				if(ifIsTrue)
					return false;
				else{
					do{
						ifT=getIfT(ifT.getParentT());
						if(null!=ifT){
							ifIsTrue = isIfTrue(ifT);
							if(ifIsTrue)
								return null;//Up if is true
						}
					}while(null!=ifT);
				}
				return true;
			}
		}
	}
	private boolean isIfTrue(Tree ifT) {
		log.debug("-------------");
		for (Tree t : ifT.getChildTs()) {
			if(isEquals(t))
				return isEqualityTrue(t);
			else if (isAndOrExpr(t))
				return isAndOrExprTrue(t);
		}
		return false;
	}
	private Boolean isAndOrExprTrue(Tree t) {
		boolean andTrue = isExprValue(t, "and");
		boolean orTrue = isExprValue(t, "or");
		Boolean thisTrue = null;
		for (Tree t1 : t.getChildTs()) 
			if(isEquals(t1))
			{
				boolean x = isEqualityTrue(t1);
				if(andTrue && !x)
					return false;
				else if(orTrue){
					if(null==thisTrue)
						thisTrue=x;
					thisTrue=thisTrue||x;
				}
			}
		return thisTrue;
	}
	private boolean isEqualityTrue(Tree t) {
		log.debug("------------"+t);
		Expr eqO = (Expr) t.getMtlO();
		Tree t1 = t.getChildTs().get(0).getChildTs().get(0);
		Tree t2 = t.getChildTs().get(1).getChildTs().get(0);
		Object t1O = getEqExprValue(t1);
		log.debug(t1O);
		Object t2O = getEqExprValue(t2);
		String eqVal = eqO.getValue();
		t2O=convertTypeO(t1O,t2O);
		log.debug(t1O+eqVal+t2O);
		if(t1O instanceof Integer){
			int v1=((Integer)t1O);
			int v2=((Integer)t2O);
			if("=".equals(eqVal)){
				return v1==v2;
			}else if("<".equals(eqVal)){
				return v1<v2;
			}else if("<=".equals(eqVal)){
				return v1<=v2;
			}else if(">".equals(eqVal)){
				return v1>v2;
			}else if(">=".equals(eqVal)){
				return v1>=v2;
			}
		}
		return false;
	}
	private Object convertTypeO(Object t1O, Object t2O) {
		if(t1O instanceof Integer){
			if(t2O instanceof String){
				return Integer.parseInt((String)t2O);
			}
		}
		return null;
	}
	private Object getEqExprValue(Tree t) {
		Object value=null;
		if(isExprO(t)){
			return getExprValue(t);
		}else if(isPvO(t)){
			Pvariable pvO = (Pvariable) t.getMtlO();
			return pvO.getPvalue();
		}
		return value;
	}
	private Object getExprValue(Tree t) {
		log.debug(t);
		log.debug(processTs);
		if(isExprValue(t, "inputPauseForInit"))
		{
			return getInputPauseWeek();
		}else if(isExprValue(t, "drugInput"))
			return getTaskTsNr(processTs);
		return null;
	}
	public Integer getModDoseTsTaskTsNr() { return getTaskTsNr(getModDoseTs()); }
	/**
	 * Get chronologically number of ts for your drug 
	 * @param ts - Ts to compare
	 * @return Drug ts chronologically number.
	 */
	public Integer getTaskTsNr(Ts ts) {
		if(null==ts)
			return null;
		Tree drugT = ts.getTimesT().getParentT().getParentT();
//		for (Ts ts2 : getPlan()) 
		Integer i = 0;
		for (Ts ts2 : planTsS)
			if(drugT.equals(ts2.getTimesT().getParentT().getParentT())){
				i++;
				if(ts==ts2)
					return i;
			}
		return 0;
	}
	Ts task4TsNr ;
	public Ts getTask4TsNr() {return task4TsNr;}
	public Ts getTask4TsNr(Tree drugT, int taskTsNr) {
		task4TsNr=null;
		Integer i = 1;
		for (Ts ts : planTsS)
			if(drugT.equals(ts.getTimesT().getParentT().getParentT())){
				if(i++==taskTsNr)
					task4TsNr=ts;
					break;
			}
		return task4TsNr;
	}
	/**
	 * Number of ts for dose modification in day.
	 */
	Integer tsNr;
	public Integer getTsNr() {return tsNr;}
	public void setTsNr(Integer tsNr) {this.tsNr = tsNr;}
	public Ts getDrugTsNr(Tree drugT, int tsNr){
		int lTsNr = 1;
		for(Tree dayT:drugT.getChildTs())
			for(Tree timesT:dayT.getChildTs())
				if(getTimesTs().containsKey(timesT))
					for (Ts ts : getTimesTs().get(timesT)) {
						if(lTsNr==tsNr)
							return ts;
						lTsNr++;
					}
		return null;
	}
	public Ts getModDoseTs(){
		if(tsNr!=null){
//			log.debug(getEditNodeT());
//			log.debug(getEditNodeT().getParentT());
			Tree taskT = getParentTaskT(getEditNodeT().getParentT());
			for(Tree dayT:taskT.getChildTs())
				for(Tree timesT:dayT.getChildTs()){
					Set<Ts> set = getTimesTs().get(timesT);
					if(set!=null)
						for (Ts ts : set)
							if(tsNr.equals(ts.getNr()))
								return ts;
				}
		}
		return null;
	}
	/**
	 * task=drug|labor|symptom node.
	 * @param taskT
	 * @return
	 */
	public Tree getParentTaskT(Tree taskT) {
		if(taskT.getId().equals(taskT.getDocT().getId()))
			return null;
		if(isDrugO(taskT))
			return taskT;
		return getParentTaskT(taskT.getParentT());
	}
	public boolean ruleCancel(Tree drugT,Ts ts,JXPathBean jxp){
		Iterator<Pointer> jxpip = jxp.var("if").var("forBegin").var("drug")
		.jxpip(drugT,"childTs[mtlO/expr=$if]/childTs/childTs" +
		"/childTs[mtlO/value=$forBegin]/childTs[tabName=$drug]");
		boolean ifLogixExpr = false;
		Tree thenCancel=null,elseCancel=null; 
		while (jxpip.hasNext()) {
			Tree forBeginDrugT = (Tree) jxpip.next().getValue();
			Drug forBeginDrugO = getDrugO(forBeginDrugT);
			boolean after = false;
			Tree ifT = forBeginDrugT.getParentT().getParentT().getParentT().getParentT();
			thenCancel = (Tree) jxp.var("then").var("cancel")
			.jxp(ifT).getPointer("childTs[mtlO/expr=$then]/childTs[mtlO/value=$cancel]").getValue();
			elseCancel = (Tree) jxp.var("else").var("cancel")
			.jxp(ifT).getPointer("childTs[mtlO/expr=$else]/childTs[mtlO/value=$cancel]").getValue();
			for (Ts ts2 : getPlan())
				if(ts2.getCday()==ts.getCday())
			{
				if(ts2==ts)	after=true;
				if(after){
					Tree drug2T = ts2.getTimesT().getParentT().getParentT();
					if(isDrugO(drug2T)){
						Drug drug2O = getDrugO(drug2T);
						if(forBeginDrugO.getGeneric()==drug2O.getGeneric()){
							ifLogixExpr=true;
						}
					}
				}
			}
		}
		boolean ruleCancel = false;
		if(ifLogixExpr){//then
			ruleCancel=null!=thenCancel;
		}else{//else
			ruleCancel=null!=elseCancel;
		}
		return ruleCancel;
	}
	/**
	 * --------------------------------------
	 * Rule section. END
	 * --------------------------------------
	 */
	public Float calc(Tree doseT){
		Dose doseO = getDoseO(doseT);
		PatientMtl patientMtl = getPatientMtl();
		Float calc = calc(doseO,patientMtl);
		return calc;
	}
	public Float calc(Dose doseO, PatientMtl patientM){
		return calc(patientM, doseO.getUnit(), doseO.getValue());
	}
	public static final Float CREATININE_micromoll2mgdl = new Float(.01131);
	public Float getCk() {
		return CREATININE_micromoll2mgdl;
	}
	public Float calc(PatientMtl patientM, String unit, Float value) {
		Float calc = value;
		if(patientM==null)			return calc;
		if(patientM.getBsa()==null)	return calc;
		if(null!=unit && patientM!=null){
			float bsa = 1;
			if((unit.indexOf("/m2")>0||unit.indexOf("/m²")>0)&&patientM!=null){
				bsa = patientM.getBsa();
			}else if(unit.indexOf("/kg")>0&&null!=patientM.getWeight()){
				log.debug(patientM.getWeight());
				bsa = patientM.getWeight();
			}else if("AUC".equals(unit)){
				Tree patientT = getPatientMtl().getDocT();
				JXPathContext patientTContext = JXPathContext.newContext(patientT);
//				String xpath = "childTs[tabName='labor'][mtlO/labor='GFR' or mtlO/labor='Kreatinin']/childTs[tabName='ivariable']";
				String xpath = "childTs[mtlO/labor='GFR' or mtlO/labor='Creatinine']/childTs[tabName='nvariable']";
				Tree gfrLaborT = (Tree) patientTContext.getPointer(xpath).getValue();
				if(null!=gfrLaborT)
				{
					Labor laborO = getLaborO(gfrLaborT.getParentT());
					Float gfr;
					if("Creatinine".equals(laborO.getLabor()))
					{
//						gfr = creatinine2gfr(patientM, gfrLaborT, laborO);
//						gfr = creatinine2gfr(patientM, gfrLaborT, laborO);
						gfr = creatinine2gfr(gfrLaborT);
					}else 
//						if("GFR".equals(laborO.getLabor()))
					{
						gfr = getNvariableO(gfrLaborT).getNvalue().floatValue();
					}
					calc=value*(gfr.floatValue()+25);
					return calc;
				}
			}
			calc = value*bsa;
		}
		return calc;
	}
//	private Float creatinine2gfr(PatientMtl patientM, Tree gfrLaborT, Labor laborO) {
//	private Float creatinine2gfr(Tree gfrLaborT, Labor laborO) {
	public Float creatinine2gfr(Tree gfrLaborT) {
		Labor laborO = getLaborO(gfrLaborT.getParentT());
		PatientMtl patientM=this.getPatientMtl();
		Float gfr;
		Integer w = patientM.getWeight();
		Integer ageYear = patientM.getAgeYear();
		float creatinine = getNvariableO(gfrLaborT).getNvalue().floatValue();
		if("µmol/l".equals(laborO.getUnit()))
			creatinine*=CREATININE_micromoll2mgdl;
		//Cockroft-Gault Formel
//		gfr = (140-ageYear)*w/(72*creatinine);
		//Jeliffe Formel
		Double gfrDouble = (98.0-(0.8*(ageYear-20)))/creatinine*(patientM.getBsa()/1.73);
		patientO=getPatientO(patientM.getDocT());
		if("F".equals(patientO.getSex()))
			gfrDouble*=.9;
		gfr=gfrDouble.floatValue();
		return gfr;
	}

	Integer editTabletInt;
	public Integer getEditTabletInt() {return editTabletInt;}
	public void setEditTabletInt(Integer editTabletInt) {this.editTabletInt = editTabletInt;}

	Tablet editTabletO;
	public Tablet getEditTabletO() {return editTabletO;}
	public void setEditTabletO(Tablet tabletO) {this.editTabletO=tabletO;}
	//Rule inputPauseForInit
	private Tree inputPauseForInitT;
	public Tree getInputPauseForInitT() {return inputPauseForInitT;}
	public void addInputPauseForInitT(Tree tree) {
		this.inputPauseForInitT=tree;
	}
	DateTime inputPausedateTime ;
	public DateTime getInputPausedateTime() {return inputPausedateTime;}
	public void setInputPauseDateTime(Timestamp sqlTs,String absDayVal) {
		inputPausedateTime = new DateTime(sqlTs);
		log.debug(inputPausedateTime);
		List<Integer> calcDayList = calcDayList(absDayVal);
		log.debug(calcDayList);
		if(calcDayList.size()>0){
			Integer day = calcDayList.get(calcDayList.size()-1);
			log.debug(day);
			log.debug(inputPausedateTime);
			inputPausedateTime = inputPausedateTime.plusDays(day);
			log.debug(inputPausedateTime);
		}
	}
	public int getInputPauseWeek(){
		int initPauseWeeks = 111;
		if(null!=inputPausedateTime) {
			DateTime begindate = getBeginCalendar();
			log.debug(inputPausedateTime+" - "+begindate);
			Period period = new Period(inputPausedateTime.getMillis(),begindate.getMillis(),PeriodType.weeks());
			initPauseWeeks = period.getWeeks();
//			period = new Period(begindate.getMillis(),inputPausedateTime.getMillis(),PeriodType.weeks());
//			log.debug(weeks);
		}
		log.debug(initPauseWeeks);
		return initPauseWeeks;
	}
	//END: Rule inputPauseForInit
	//maxDoseCumulative
	Integer maxDoseCumulative,maxDose;
	String maxDoseCumulativeUnit="mg",maxDoseUnit="mg";

	public Integer getMaxDose() {return maxDose;}
	public Integer getMaxDoseCumulative() {return maxDoseCumulative;}
	public void setMaxDose(Integer maxDose) {this.maxDose = maxDose;}
	public void setMaxDoseCumulative(Integer maxDoseCumulative) {this.maxDoseCumulative = maxDoseCumulative;}
	
	public String getMaxDoseCumulativeUnit() {return maxDoseCumulativeUnit;}
	public void setMaxDoseCumulativeUnit(String maxDoseCumulativeUnit) {this.maxDoseCumulativeUnit = maxDoseCumulativeUnit;}
	public String getMaxDoseUnit() {return maxDoseUnit;}
	public void setMaxDoseUnit(String maxDoseUnit) {this.maxDoseUnit = maxDoseUnit;}
	//END: maxDoseCumulative
}

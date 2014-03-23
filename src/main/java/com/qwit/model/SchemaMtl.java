package com.qwit.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import javax.persistence.EntityManager;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.qwit.domain.Arm;
import com.qwit.domain.Contactperson;
import com.qwit.domain.Day;
import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Expr;
import com.qwit.domain.Finding;
import com.qwit.domain.Folder;
import com.qwit.domain.Literature;
import com.qwit.domain.MObject;
import com.qwit.domain.Notice;
import com.qwit.domain.Owuser;
import com.qwit.domain.Position;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Symptom;
import com.qwit.domain.Task;
import com.qwit.domain.Times;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.service.JXPathBean;
import com.qwit.service.MtlDbService;
import com.qwit.service.TaskNodeCreator;

public class SchemaMtl extends DrugMtl implements Serializable {
	protected static final Log log = LogFactory.getLog(SchemaMtl.class);
//	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;

	public int getDelayDays(JXPathBean jxp) {
		int delay = 0;
		if(null!=getPschemaT()){
			Iterator jxpip = jxp.jxpip(getPschemaT()
				, "childTs[mtlO/ivariable='delayedDay']/childTs[mtlO/ivalue!=0]");
			while (jxpip.hasNext()) {
				Tree ivalT = (Tree) ((Pointer) jxpip.next()).getValue();
				delay+=getIvariableO(ivalT).getIvalue();
			}
		}
		return delay;
	}
	
	private HourPlan hourPlan;
	public HourPlan getHourPlan() {
		if(null==hourPlan)
			hourPlan=new HourPlan(this);
		return hourPlan;
	}

	private Map<Tree,Map<Integer,List<Ts>>>	drugDayTsM;
	Dose2Tablete		dose2Tablete;

	ConceptMtl conceptMtl;
	public ConceptMtl getConceptMtl() {	return conceptMtl;}
	public void setConceptMtl(ConceptMtl conceptMtl) {this.conceptMtl = conceptMtl;}

	private Map<Expr, Set<Tree>>	exprUse;
//	private Boolean					chemoDoseMod, isPatientData;

	private Integer modDay;
	public Integer getModDay() {return modDay;}
	public void setModDay(Integer modDay) {this.modDay = modDay;}

//	public Boolean getChemoDoseMod() {return chemoDoseMod;}
//	public void setAction(String action){
//		this.action=action;
//		if(getPatientMtl()!=null)
//			if("chemoDoseMod".equals(action))		chemoDoseMod=true;
//			else if("simpleDoseMod".equals(action))	chemoDoseMod=false;
//	}

	public SchemaMtl(Tree docT){
		super(docT);
		tradeM		= new HashMap<Integer, Drug>();
		exprUse		= new HashMap<Expr,Set<Tree>>();
		gRule		= new HashMap<Tree, Set<Tree>>();
		//timesNr=
//		chemoDoseMod=false;
	}
	private Map<Integer, Drug> tradeM;
	public void initTradeM() {this.tradeM = new HashMap<Integer, Drug>();}
	public Map<Integer, Drug> getTrade() {return tradeM;}
	public Map<Integer, Drug> getDrugM() {return tradeM;}
	/*
	 * 
	public void setTrade(SimpleJdbcTemplate simpleJdbc, EntityManager em) {
		String sql = "SELECT d FROM Tree t1,Tree t2,Drug d,Tree t3 " +
				"WHERE t1.id=t2.parentT.id AND t2.idClass=d.id " +
				"AND t3.idClass=d.generic.id AND t1.id in (109563,229703)" +
				"AND t3.docT.id="+getDocT().getId();
		List<Drug> tradeList = em.createQuery(sql).getResultList();
		setTrade(tradeList);
	}
	 */
	public void setTrade(List<Drug> tradeDrugList) {
		for (Drug tradeDrugO : tradeDrugList)
			tradeM.put(tradeDrugO.getGeneric().getId(), tradeDrugO);
	}
	
//	public void calc1Dose(MObject dose1C) {
//		Dose doseC=(Dose)dose1C;
////		Set<Tree> doseSet = getClassMUse().get(doseC);
//		if(doseSet!=null)
//			for(Tree doseT : doseSet)
//				calc1Dose(doseC, doseT);
////		calc = calc1Dose(doseC, doseT);
//	}
	

	public static void main(String[] args) {
		float d = (float) 2.6;
		String pvl = "0.5";
		if(pvl.contains(".")||pvl.contains(",")){
			float rmf = Float.parseFloat(pvl);
			int mali = (int) (d/rmf);
			System.out.println(mali);
			float r1 = mali*rmf;
			float r2 = (mali+1)*rmf;
			System.out.println(r1);
			System.out.println(r2);
			float r = r1;
			if(r1-d>r2-d)r=r2;
			System.out.println(r);
		}else{
			//integer
		}
	}

	/*
	public void saveBeginDate(){
		log.debug("---------------");
	}
	void addTreePaar_depr(Tree tree)
	{
		super.addTreePaar_depr(tree);
		String tabName=tree.getTabName();
		Tree parentT = tree.getParentT();
		String parentTabName = parentT.getTabName();
		boolean isIdClass = tree.getIdClass()!=null;
		if(classM(tree) instanceof Pvariable) {
			if (isIdClass) {
				Pvariable pvC = (Pvariable) classM(tree);
				if("mgProMal".equals(pvC.getPvariable())){
					initCortisol(tree,pvC);
				}
			}
		} else if("day".equals(parentTabName)&&"times".equals(tabName)){
			if(isIdClass){
				Times timesC=(Times) classM(tree);
				if(timesC==null)return;
				if(timesC.getAbs()!=null&&timesC.getAbs().contains("=")){
					initCortisol(tree,timesC);
				}
			}
		} else if("expr".equals(tabName)) {
			if (isIdClass) {
				Expr exprC = (Expr) classM(tree);
				if("SteroidDayDose".equals(exprC.getValue())){
					initCortisol(tree,exprC);
				}
				getExpr().put(exprC.getId(),exprC);
				getExprUse(exprC).add(tree);
			}
		}
	}
	 */
	/*
	 */
	/*
	void addTreeClassObj_depr(MObject objM, Tree tree){
		String parentTabName = tree.getParentT().getTabName();
		if(objM instanceof Notice){
		}
	}
	 */
	
	public HashMap<Tree, Tree> getDayRqm() {return dayRqm;}
	boolean hasPeriod=false;
	public boolean isHasPeriod() {return hasPeriod;}
	void add1ClassObj_depr(Tree tree, MObject objM){
		String parentTabName = tree.getParentT().getTabName();
		if(objM instanceof Arm){
			Arm armC=(Arm) objM;
			if("cycle".equals(armC.getArm())){
				cycleIdClass=armC.getId();
			}
		}else if(objM instanceof Drug){
			if(tree.getRef()!=null){
				
			}
		}else if(objM instanceof Notice){
			Notice noticeC=(Notice) objM;
			if("rqm".equals(noticeC.getType())){
				if("day".equals(parentTabName)){
					dayRqm.put(tree.getParentT(), tree);
				}
			}
//		}else if(objM instanceof Task){
//			if("task".equals(parentTabName)){
//				Task taskC=(Task) getMObject(tree);
//				if("inside".equals(taskC.getType())&&"toxSheet".equals(taskC.getTask())){
//					setToxSheetT(tree);
//				}else 
//				if("inside".equals(taskC.getType())&&"taskCyclePlane".equals(taskC.getTask())){
//					setTaskCyclePlaneT(tree);
//				}else if("inside".equals(taskC.getType())&&"taskHomeTablet".equals(taskC.getTask())){
//					setTaskHomeTabletT(tree);
//				}else if("inside".equals(taskC.getType())&&"taskPremedication".equals(taskC.getTask())){
//					setTaskPremedicationT(tree);
//				}else if("support".equals(taskC.getTask())){
//					setTaskSupportT(tree);
//				}
//				getClassMUse(taskC).add(tree);
//			}
//			if(tree.getId()==getDocT().getId()){
//				docTaskC=(Task)getClassM().get(getDocT());
//			}
		}
	}
//	
	

	
	/*
	public Set<Tree> getAppUse(App app) {
		if(!appUse.containsKey(app)) appUse.put(app, new HashSet<Tree>());
		return appUse.get(app);
	}
	public Set<Tree> getDrugUse(Drug drug) {
		if(!drugUse.containsKey(drug)) drugUse.put(drug, new HashSet<Tree>());
		return drugUse.get(drug);
	}
	public Set<Tree> getDoseUse(Dose dose) {
		if(!doseUse.containsKey(dose)) doseUse.put(dose, new HashSet<Tree>());
		return doseUse.get(dose);
	}
	public Set<Tree> getTimesUse(Times times) {
		if(!timesUse.containsKey(times)) timesUse.put(times, new HashSet<Tree>());
		return timesUse.get(times);
	}
	public Set<Tree> getDayUse(Day day) {
		if(!dayUse.containsKey(day)) dayUse.put(day, new HashSet<Tree>());
		return dayUse.get(day);
	}
	
	public Set<Tree> getNoticeUse(Notice notice) {
		if(!noticeUse.containsKey(notice)) noticeUse.put(notice, new HashSet<Tree>());
		return noticeUse.get(notice);
	}
	 * */
	public Set<Tree> getExprUse(Expr exprC) {
		if(!exprUse.containsKey(exprC)) exprUse.put(exprC, new HashSet<Tree>());
		return exprUse.get(exprC);
	}
	public Map<Tree, Map<Integer, List<Ts>>> getDrugDayTs() {
		if(drugDayTsM==null){
			drugDayTsM=new HashMap<Tree, Map<Integer, List<Ts>>>();
			for(Tree drugT:getDocT().getChildTs()){
				if(drugT.getTabName().equals("drug")){
					Map<Integer, List<Ts>> daysTsL = drugDayTsM.get(drugT);
					if(daysTsL==null){
						daysTsL=new HashMap<Integer, List<Ts>>(); 
						drugDayTsM.put(drugT, daysTsL);
					}
					
					/*
//					for(Tree dayT:getDrugDay().get(drugT)){
					for(Tree dayT:drugT.getChildTs()) if("day".equals(dayT.getTabName())){
//						for(Tree timeT:getDrugDay().get(dayT)){
						for(Tree timeT:dayT.getChildTs()) if ("times".equals(timeT.getTabName())){
							int i = -111;
							for (Ts ts : getTimesTs(timeT)) {
								List<Ts> dayTsL = daysTsL.get(ts.getDay());
								if(dayTsL==null){
//									daysTsL.put(ts.getDay(), daysTsL);
								}
							}
						}
					}
					 */
				}
			}
		}
		return drugDayTsM;
	}

	Map<Integer, Set<String>> dayIntensivHm;
	public Map<Integer, Set<String>> getDayIntensiv()
	{
		log.debug(1);
		if(dayIntensivHm==null){
			log.debug(2);
			initDayIntensiv();
			Set<Ts> planLs = getPlan();
			boolean isIntensDay = false;
			planHD = new ConcurrentSkipListSet<Ts>();
			planDinH = new ConcurrentSkipListMap<Ts, Set<Ts>>();
			int intensivDay = -111;
			Set<Ts> dayDayS = null;
			for(Ts ts:planLs) {
				Set<String> dayIntensivS = dayIntensivHm.get(ts.getCday());
				if(dayIntensivS!=null) {
					isIntensDay=true;
					String ikey = getIkey(ts);
					dayIntensivS.add(ikey);
					if(intensivDay!=ts.getCday()){
						planHD.add(ts);
						intensivDay = ts.getCday();
						planDinH.put(ts, new ConcurrentSkipListSet<Ts>());
					}
					continue;
				}else if((isIntensDay && ts.getCday()>intensivDay)||intensivDay==-111){
					planHD.add(ts);
					isIntensDay=false;
					dayDayS = new ConcurrentSkipListSet<Ts>();
					planDinH.put(ts, dayDayS);
				}
				if(dayDayS!=null){
					dayDayS.add(ts);
				}
			}
		}
		return dayIntensivHm;
	}
	private void initDayIntensiv() {
		dayIntensivHm = new HashMap<Integer, Set<String>>();
		for(Tree tree:getDocT().getChildTs()){
			if(tree.getTabName().equals("pvariable")){
				Pvariable varC = (Pvariable) classM(tree);
				if(varC.getPvariable().equals("daysintensive"))
					for(Tree tree2:tree.getChildTs())
						if(tree2.getTabName().equals("day")){
							log.debug(tree2);
							Day dayC = (Day) classM(tree2);
							if(dayC.getAbs()==null)
								continue;
//								Matcher m_absPeriod = JsfFunctions.getAbsPeriod().matcher(dayC.getAbs());
							Matcher m_absPeriod = JXPathBean.getAbsPeriod().matcher(dayC.getAbs());
							if(m_absPeriod.matches()){
								int b = Integer.parseInt(m_absPeriod.group(1));
								int e = Integer.parseInt(m_absPeriod.group(2));
								for (int day = b; day <= e; day++)
									dayIntensivHm.put(day, new ConcurrentSkipListSet<String>());
							}else if(dayC.getAbs().indexOf(",")>0)
								for(String abs1:dayC.getAbs().split(","))
									dayIntensivHm.put(new Integer(abs1), new ConcurrentSkipListSet<String>());
							else if(dayC.getAbs()!=null)
								dayIntensivHm.put(new Integer(dayC.getAbs()), new ConcurrentSkipListSet<String>());
						}
			}
		}
	}
	Set<Ts> planHD;
	public Set<Ts> getPlanHD() {return planHD;}
	Map<Ts,Set<Ts>> planDinH;
	public Map<Ts, Set<Ts>> getPlanDinH() {return planDinH;}
	/*
	 * drug
	 * iv	chemo	11
	 * iv	noChemo	12
	 * noiv	chemo	21
	 * noiv	nochemo	22
	 * noDrug
	 * 				31
	 */
	private String getIkey(Ts ts) {
		String ikey = "9";
		Tree drugT = ts.getTimesT().getParentT().getParentT();
		if(drugT.getTabName().equals("drug")){
//			Tree doseT = getDrugDose().get(drugT);
			Tree doseT =JXPathBean.getDose(drugT);
			if(doseT.getIdClass()!=null){
				Dose doseC = (Dose) getClassM().get(doseT.getIdClass());
				if(doseC.getApp().indexOf("i.v.")>=0){
					if(isChemo(drugT)){
						ikey="11";
					}else{
						ikey="12";
					}
				}else{
					if(isChemo(drugT)){
						ikey="21";
					}else{
						ikey="22";
					}
				}
			}
		}else
			ikey="31";
		String idStr = drugT.getId().toString();
		String s0 = "";
		for (int i = 10; i > idStr.length(); i--)
			s0+="0";
		ikey+="_"+s0+idStr;
		return ikey;
	}
	
	/*
	private Tree 
		taskCyclePlaneT;
	public Tree getTaskCyclePlaneT()		{return taskCyclePlaneT;}
	public void setTaskCyclePlaneT(Tree taskCyclePlaneT) 
											{this.taskCyclePlaneT = taskCyclePlaneT;}
	 * */

	
	//Dose editor
	private String doseModType;
	private Integer procent;
	private Dose chemoPcDoseC;
	public Dose getChemoPcDoseC()	{return chemoPcDoseC;}
	public Integer getProcent()		{return procent;}
	public void setProcent(Integer procent)
									{this.procent = procent;}
	public String getDoseModType()	{return doseModType;}
	public void setDoseModType(String doseModType)
									{this.doseModType = doseModType;}

	/**
	 * Make object for dose edit in definition or dose modification in patient regime.
	 * @see com.qwit.model.DrugMtl#openEditDose()
	 */
	public void openEditDose()
	{
		setEditDoseC(new Dose());
		if(isNotChemoDoseMod()){
			openEditDoseOnly();
		}else{
			setEditNoticeC(new Notice());
			openEditDoseMod();
		}
	}

	public void openEditDoseMod()
	{
		Dose editDoseC = getEditDoseC();
		//		editDoseC=new Dose();
		chemoPcDoseC=new Dose();
		Dose doseC=(Dose) getClassM().get(getEditNodeT().getIdClass());
		if(doseC!=null){
			editDoseC.setValue(doseC.getValue());
			editDoseC.setUnit(doseC.getUnit());
			editDoseC.setApp(doseC.getApp());
		}
		/*
		if(patientDoseProcentDoseTM.containsKey(getEditNodeT())||patientDoseProcentTM.containsKey(getEditNodeT())){
			initProcent(getEditNodeT());
		}else{
			procent=100;
		}
		 * */
//		procent = JsfFunctions.procent(this, getEditNodeT());
		procent = JXPathBean.procent(this, getEditNodeT());
		/*
		Tree patientDoseProcentDoseT = patientDoseProcentDoseTM.get(getEditNodeT());
		 * */
		Tree drugT = getEditNodeT().getParentT();
//		Tree patientDoseProcentDoseT = JsfFunctions.pDoseProcentDoseT(this, drugT);
		Tree patientDoseProcentDoseT = JXPathBean.pDoseProcentDoseT(this, drugT);
		if(patientDoseProcentDoseT!=null){
			Dose doseModC = (Dose) getPatientMtl().getClassM().get(patientDoseProcentDoseT.getIdClass());
			if("pc".equals(doseModC.getType())){
				chemoPcDoseC.setValue(doseModC.getValue());
				if(doseC.getUnit().contains("/m")){
					float doseValue = doseModC.getValue()/getPatientMtl().getBsa();
					editDoseC.setValue(doseValue);
				}
			}else if("p".equals(doseModC.getType())){
				Float calcDose = getCalcDose(patientDoseProcentDoseT);
				chemoPcDoseC.setValue(calcDose);
			}
			doseC=doseModC;
		}else if(getCalcDose().get(getEditNodeT())!=null){
			Float calcDose = getCalcDose().get(getEditNodeT());
			chemoPcDoseC.setValue(calcDose*procent/100);
		}
//		Tree pDoseProcentT = JsfFunctions.pDoseProcentT(this, drugT,0);
		Tree pDoseProcentT = JXPathBean.pDoseProcentT(this, drugT,0);
		if(pDoseProcentT!=null){
			Tree noticeT = getDoseModNoticeT(pDoseProcentT);
			openEditNotice(noticeT);
		}
	}
	public Tree getDoseModNoticeT(Tree pDoseProcentT) {
		Tree noticeT = pDoseProcentT.getParentT().getParentT().getParentT().getParentT();
//		if("modDay".equals(getAction()))
		if("task".equals(noticeT.getTabName()))
			noticeT=noticeT.getParentT();
		return noticeT;
	}

	public boolean isNotChemoDoseMod() {
		if(getPatientMtl()==null) return true;
		//if(!chemoDoseMod) return true;
//		if("chemoDoseMod".equals(getAction())) return true;
		if(!isChemoDose(getEditNodeT()))return true;
		return false;
	}

	//	private boolean isChemoDose(Tree editNodeT) {return editNodeT.getParentT().getParentT().getId().equals(getDocT().getId());}
	public boolean isChemoDose(Tree editNodeT) {
		boolean b = editNodeT.getParentT().getParentT()==getDocT();
		return b;
	}
	
	private boolean isNotChemoDoseMod2() {//moved from DrugMtl
		boolean b = getPatientMtl()==null||!getEditNodeT().getParentT().getParentT().equals(getDocT());
		return b;
	}
	
	public Tree getPvProcent() {
		for(Tree pvT:getEditNodeT().getChildTs())
			if(classM(pvT) instanceof Pvariable
					&& ((Pvariable) classM(pvT)).getUnit().equals("%"))
				return pvT;
		return null;
	}

//	public Task getDocTaskC(){return docTaskC;}
	/*
	public <T extends MObject> void openEditObject(T obj)
	{
		
	}
	 */
	
	
	//the function is obsolete, job done by taskLaborCreator bean in ows-update.xml and show-flow.xml
	//make an entry in the doc tree for the new labor
//	public Integer openNewLabor(Integer parentId)
//	{
//		Tree parentT = getTree().get(parentId);
//		Tree laborT = addChild(parentT, "labor", getDocT());
//		setEditNodeT(laborT);
//		addTaskElementDay(laborT);
//		setIdt(getEditNodeT().getId());
//		return getIdt();
//	}
	
//	public Task getSchemaC(){return (Task) getClassM().get(getDocT().getIdClass());}
	public Task getSchemaC(){return (Task) getDocT().getMtlO();}

	/*
	private	Task	editTaskO;
	public void openEditTaskDoc()
	{
		setIdt(getDocT().getId());
		setEditNodeT();
	}
	 */
	/*******************************************************
	 * 
	 * 	FINDING EDITOR SECTION
	 * 
	 *******************************************************/
	private	Finding	editedFindingC;
	public Finding getEditedFindingC() {return editedFindingC;}
	public void openEditFinding()
	{
		editedFindingC=new Finding();
		if(getEditNodeT().getIdClass()!=null){
			Finding editNodeFinding =(Finding)getClassM().get(getEditNodeT().getIdClass());
			editedFindingC.setFinding(editNodeFinding.getFinding());
			editedFindingC.setUnit(editNodeFinding.getUnit());
		}
	}
	//the function is obsolete, job done by taskFindingCreator bean in ows-update.xml and show-flow.xml
	//make an entry in the doc tree for the new Finding
//	public Integer openNewFinding(Integer parentId)
//	{
//		Tree parentT = getTree().get(parentId);
//		Tree findingT = addChild(parentT, "finding", getDocT());
//		setEditNodeT(findingT);
//		addTaskElementDay(findingT);
//		setIdt(getEditNodeT().getId());
//		return getIdt();
//	}
//	
	/******************************************************
	 *  /FINDING EDITOR SECTION
	 ******************************************************/
	/*
	//Notice editor
	private	Notice	editNoticeC;
	public Notice getEditNoticeC()			{return editNoticeC;}
	public void openEditNotice()
	{
		editNoticeC=new Notice();
		if(getEditNodeT().getIdClass()!=null){
			Notice editNodeNotice=(Notice)getClassM().get(getEditNodeT().getIdClass());
			editNoticeC.setNotice(editNodeNotice.getNotice());
			editNoticeC.setType(editNodeNotice.getType());
		}
	}
	 * */
	public void editToxSheetNotice1()
	{
		if(getEditNodeT()==null||getDocT().getId().equals(getEditNodeT().getId()))
		{
			setNewNotice(getToxSheetT(), "toxSheetNotice1");
		}
	}

	
	public void editToxSheetHead()
	{
		if(getEditNodeT()==null||getDocT().getId().equals(getEditNodeT().getId()))
		{
			setNewNotice(getToxSheetT(), "toxSheetHead");
		}
	}
	private void setNewNotice(Tree toxSheetT2, String type) {
		Tree toxSheetHeadT = addChild(toxSheetT2, "notice", getDocT());
		Notice toxSheetHeadC = new Notice();
		toxSheetHeadC.setType(type);
		toxSheetHeadC.setId(nextCurrentId());
		getClassM().put(toxSheetHeadC.getId(), toxSheetHeadC);
		toxSheetHeadT.setIdClass(toxSheetHeadC.getId());
		setIdt(toxSheetHeadT.getId());
	}

	private Task insideTaskC;
	private Tree insideTaskT;

	public void reviseInsideTask(List tL)
	{
		if(tL==null||tL.size()==0)	return;
		Task taskC = (Task) tL.get(0);
		if(taskC!=null&&insideTaskC.getId()<TreeManager._5000){
			Tree tmpEdNoT = getEditNodeT();
			setEditNodeT(insideTaskT);
			Task dd = insideTaskC;
//			removeUse(dd, getClassMUse());
			setEditNodeT(insideTaskT);
			insideTaskC=(Task) equivalent(insideTaskC, tL, getClassM());
			setEditNodeT(tmpEdNoT);
		}
	}
	public Tree setInsideTaskT(String task, String type) {
		insideTaskT = addChild(getDocT(), "task", getDocT());
		insideTaskC = new Task();
		insideTaskC.setTask(task);
		insideTaskC.setType(type);
		addNewTreeObjM(insideTaskC,insideTaskT);
		return insideTaskT;
	}
	
	
	public void pvMealWeek()
	{
		log.debug("------------"+getIdt());
	}
	
	public Integer openNewApp(Integer drugId)
	{
		Tree drugT = getTree().get(drugId);
		Integer editedAppId = addNewEditNodeT(drugT, "app");
		return editedAppId;
	}
	
	public Integer openNewNotice(Integer parentId)
	{
		Tree parentT = getTree().get(parentId);
		Tree noticeT = addChild(parentT, "notice", getDocT());
		if(parentT.getId().equals(getDocT().getId())){
			addTaskElementDay(noticeT);
		}else if(getClassM().get(parentT.getIdClass()) instanceof Dose){
			neNr(parentT.getParentT());
			getDdNE(parentT).add(noticeT);
		}else if("dose".equals(parentT.getTabName())){
			neNr(parentT.getParentT());
			getDdNE(parentT).add(noticeT);
		}
		getDrugNeCnt();
		setEditNodeT(noticeT);
		setIdt(getEditNodeT().getId());
		return getIdt();
	}
	public Integer openNewNotice(Integer parentId, MtlDbService sms)
	{
		Tree parentT = getTree().get(parentId);
//		Tree noticeT = addChild(parentT, "notice", getDocT());
		Tree noticeT = addChild(parentT, "notice", getDocT(),sms.nextDbid());
		if(parentT.getId().equals(getDocT().getId())){
			addTaskElementDay(noticeT,sms);
		}else if(getClassM().get(parentT.getIdClass()) instanceof Dose){
			neNr(parentT.getParentT());
			getDdNE(parentT).add(noticeT);
		}else if("dose".equals(parentT.getTabName())){
			neNr(parentT.getParentT());
			getDdNE(parentT).add(noticeT);
		}
		getDrugNeCnt();
		setEditNodeT(noticeT);
		setIdt(getEditNodeT().getId());
		return getIdt();
	}
	public Integer openNewDay(Integer oldDayId)
	{
		Tree oldDayT = getTree().get(oldDayId);
		Tree drugT = oldDayT.getParentT();
		Tree dayT = addTaskElementDay(drugT);
		setEditNodeT(dayT);
		setIdt(getEditNodeT().getId());
		return getIdt();
	}
	
	
	
	private Tree addTaskElementDay(Tree task1T, MtlDbService sms) {
		Tree dayT = addChild(task1T, "day", getDocT(),sms.nextDbid());
		addChild(dayT, "times", getDocT(),sms.nextDbid());
		return dayT;
	}
	public Tree addTaskElementDay(Tree drugT) {
		Tree dayT = addChild(drugT, "day", getDocT());
		addChild(dayT, "times", getDocT());
		return dayT;
	}
	public void reviseDrug(Drug drugDbC)
	{
		Drug drugC= (Drug) getClassM().get(getEditNodeT().getIdClass());
		if(drugDbC.compareTo(drugC)!=0)
		{
			drugDbC=(Drug) equivalent(drugDbC, null, getClassM());
		}
	}

	
	public void setDose(Integer idDose){
		Dose doseC = (Dose) getClassM().get(idDose);
		if(getEditNodeT().getTabName().equals("drug")){
			Tree doseT = JXPathBean.getDose(getEditNodeT());
			relocate(doseC, doseT);
			setIdt(doseT.getId());
		}else if(getEditNodeT().getTabName().equals("dose")){
			relocate(doseC, getEditNodeT());
		}
	}
	private void relocate(Dose doseC, Tree doseT){
		if(doseT.getIdClass()!=null){
			Dose oldDoseC = (Dose) getClassM().get(doseT.getIdClass());
		}
		doseT.setIdClass(doseC.getId());
		getClassM().put(doseC.getId(), doseC);
		nextCurrentId();
	}
	

	private boolean isTypeDoseMod() {return "p".equals(doseModType)||"pc".equals(doseModType);}

	//Drug editor
	private	Drug	editTradeC;
	private	Tree	editDrugT;
	public	Drug	getEditTradeC()	{return editTradeC;}
	public	Tree	getEditDrugT()	{return editDrugT;}
	
	public	void	removeTaskDrug(){
		Tree drugT = getEditNodeT();
		drugT.getParentT().getChildTs().remove(drugT);
	}

	public void reviseNewGeneric(Drug editDrugC) {
		Drug drugC= (Drug) getClassM().get(getEditNodeT().getIdClass());
		if(editDrugC.compareTo(drugC)!=0)
		{
			editDrugC= (Drug) equivalent(editDrugC, null, getClassM());
			getClassM().put(editDrugC.getId(), editDrugC);
			if(drugC!=null){
			}
			getEditNodeT().setIdClass(editDrugC.getId());
			editDrugC.setGeneric(editDrugC);
		}
	}
	
	public void newTrade()
	{
		editTradeC=new Drug();
		Drug editDrugC=getEditDrugC();
		editTradeC.setDrug(editDrugC.getDrug());
		editTradeC.setId(nextCurrentId());
		editDrugC.setDrug(null);
		editDrugT = getEditNodeT();
	}
	
	
	void addMtlMap(MObject mtlC)
	{
		if(mtlC instanceof Drug)
		{
		}
	}
	
	/***************************************
	 *  BEGIN "USED DOSE IN DRUG"  SECTION
	 *  
	 **************************************/
	/***************************************
	 * END "USED DOSE IN DRUG" SECTION
	 * 
	 ***************************************/
	/**
	 * old "USED DOSE IN DRUG" SECTION
	 */
	private List<String> appKeyL;
	public List<String> getAppKeyL() {return appKeyL;}
	Map<String, List<String>> appMapUnitL;
	public Map<String, List<String>> getAppMapUnitL() {return appMapUnitL;}
	private Map<String,Map<String,List<Integer>>>	appUnitIdclassL;
	public Map<String, Map<String, List<Integer>>> getAppUnitIdclassL() {return appUnitIdclassL;}

//	private void makeAppUnitValueL_old(List<Map<String, Object>> drugDoseList2) {
//		appKeyL			= new ArrayList<String>();
//		appMapUnitL		= new HashMap<String,List<String>>();
//		appUnitIdclassL	= new HashMap<String, Map<String,List<Integer>>>();
//		for (Map rowMap : drugDoseList){
//			String app = (String) rowMap.get("app");
//			if(app.length()==0)continue;
//			String unit = (String) rowMap.get("unit");
//			List<String> appUnitL=appMapUnitL.get(app);
//			if(appUnitL==null){
//				appKeyL.add(app);
//				appUnitL=new ArrayList<String>();
//				appMapUnitL.put(app, appUnitL);
//			}
//			if(!appUnitL.contains(unit)) appUnitL.add(unit);
//			Map<String, List<Integer>> appUnitKeyMap = appUnitIdclassL.get(app);
//			if(appUnitKeyMap==null){
//				appUnitKeyMap=new HashMap<String, List<Integer>>();
//				appUnitIdclassL.put(app, appUnitKeyMap);
//			}
//			List<Integer> valueL = appUnitKeyMap.get(unit);
//			if(valueL==null){
//				valueL=new ArrayList<Integer>();
//				appUnitKeyMap.put(unit, valueL);
//			}
////			valueL.add(convertDose(rowMap));
//		}
//	}
	
	
	/**
	 * @author Simon
	 * Seek & change edit MeTaL object to our equivalent from DB or RAM.
	 * @param editMC	- Edit MeTaL object.
	 * @param tL		- Equivalenten from DB.
	 * @param classMM	- Map of all document objects same type as edit object.
	 * @return Equivalent of edit MeTaL object or old object with next current id.
	 */
	@SuppressWarnings("unchecked")
	public MObject equivalent_new(MObject editMC, MObject dbObj, Map classMM) {
		editMC = eq(editMC, dbObj, classMM);
		setEditNodeClass(editMC);
		return editMC;
	}
	/**
	 * @author Simon
	 * @param editMC
	 * @param tL
	 * @param classMM
	 * @return
	 */
	private MObject eq(MObject editMC, MObject dbObj, Map classMM) {
		if(dbObj!=null)
		{//new object is in DB
			editMC = dbObj;
			if(!classMM.containsKey(editMC.getId()))
				classMM.put(editMC.getId(), editMC);
		}else{
			//new object is not in DB
			//is new object in new edited objects
			for (MObject classM : (Collection<MObject>) classMM.values()){
				if(editMC.getClass()== classM.getClass()){
					if(classM.getId()<TreeManager._5000){
						if(editMC.compareTo(classM)==0){
							editMC = classM;
						}
					}
				}
			}
			if(editMC.getId()==null){
				editMC.setId(nextCurrentId());
				classMM.put(editMC.getId(), editMC);
//				getClassMUse(editMC).add(getEditNodeT());
			}
		}
		return editMC;
	}
		
	/**
	 * @author Roman
	 * Seek & change edit MeTaL object to our equivalent from DB or RAM.
	 * @param editMC	- Edit MeTaL object.
	 * @param tL		- Equivalenten from DB.
	 * @param classMM	- Map of all document objects same type as edit object.
	 * @return Equivalent of edit MeTaL object or old object with next current id.
	 */
	@SuppressWarnings("unchecked")
	public MObject equivalent(MObject editMC, List tL, Map classMM) {
		editMC = eqOld(editMC, tL, classMM);
		setEditNodeClass(editMC);
		return editMC;
	}

	
	/**
	 * @author roman
	 * @param editObjM
	 * @param tL
	 * @param classMM
	 * @return
	 */
	public MObject eqOld(MObject editObjM, List tL, Map classMM) {
		if(tL!=null&&tL.size()>0)
		{//new object is in DB
			editObjM = (MObject) tL.get(0);
		}else{
			//new object is not in DB
			//is new object in new edited objects
			for (MObject objM : (Collection<MObject>) classMM.values())
				if(editObjM.getClass()== objM.getClass())
					if(objM.getId()<TreeManager._5000)
						if(editObjM.compareTo(objM)==0)
							editObjM = objM;
			if(editObjM.getId()==null)
				editObjM.setId(nextCurrentId());
		}
		if(!classMM.containsKey(editObjM.getId()))
			classMM.put(editObjM.getId(), editObjM);
		return 
		editObjM;
	}
	
	public void upNode_depr()
	{
		Tree idcT = getTree().get(getIdc());
		List<Tree> childTs = idcT.getParentT().getChildTs();
		long sort=1;
		Tree upT=null, previousT=null;
		for(Tree tree:childTs)
		{
			if(tree.getSort()==null||tree.getSort()<=sort)
				tree.setSort(++sort);
			else
				sort=tree.getSort();
			if(tree==idcT&&previousT!=null)	upT=previousT;
			previousT=tree;
		}
		if(upT!=null){
			Long upSort = upT.getSort();
			upT.setSort(idcT.getSort());
			idcT.setSort(upSort);
		}
		nextCurrentId();
	}
		
	public void sort1to2(String upDown){
		String part = OwsSession.getOwsSession().getSchemaPart();
		if("plan".equals(part)&&"times".equals(getEditNodeName())){
			Ts ts = getTsM().get(getIdt());
			Set<Ts> plan = getPlan();
		}else{
			moveNode(upDown);
		}
	}
	
	public void downNode_depr()
	{
		Tree idcT = getTree().get(getIdc());
		List<Tree> childTs = idcT.getParentT().getChildTs();
		long sort=1;
		Tree downT=null, previousT=null;
		for(Tree tree:childTs)
		{
			if(tree.getSort()==null||tree.getSort()<=sort)
				tree.setSort(++sort);
			else
				sort=tree.getSort();
			if(previousT==idcT&&downT==null)
				downT=tree;
			previousT=tree;
		}
		if(downT!=null){
			Long downSort = downT.getSort();
			downT.setSort(idcT.getSort());
			idcT.setSort(downSort);
		}
		nextCurrentId();
	}
	
	public void deleteEditNode_depr()
	{
		Integer idt = getIdt();
		if(idt==null) return;
		Tree editT = getTree().get(idt);
		List<Tree> childTs = editT.getParentT().getChildTs();
		childTs.remove(editT);
		getTree().remove(editT);
		nextCurrentId();
		if(editT.getId()>SchemaMtl._5000){
			removeNodeS.add(editT);
		}
	}
	
//	private Map<Tree, Tree> gRule;
//	public Map<Tree, Tree> getGrule()	{return gRule;}
	private Map<Tree, Set< Tree>> gRule;
	public Map<Tree, Set< Tree>> getGrule()	{return gRule;}
//	public Set<Tree> getgRuleS()		{return gRule.keySet();}
	public void setGRule(String sql, SimpleJdbcTemplate simpleJdbc, EntityManager em) {
		Integer idDoc = getDocT().getId();
		log.debug(sql);
		List<Map<String, Object>> sl = simpleJdbc.queryForList(sql,idDoc, idDoc);
		if(sl.size()>0)
			for (Map<String, Object> map : sl) {
				Integer	idNode = (Integer) map.get("idNode");
				Integer idExpr = (Integer) map.get("idExpr");
				Integer idDocG = (Integer) map.get("idDoc");
				Tree nodeT = getTree().get(idNode);
				Tree gExprT = em.find(Tree.class, idExpr);
//				gRule.put(nodeT, gExprT);
				addGrule(nodeT, gExprT);
				addClass(gExprT,idDocG,em);
			}
	}
public void addGrule(Tree nodeT, Tree gExprT) {
	if(!gRule.containsKey(nodeT))
		gRule.put(nodeT, new HashSet<Tree>());
	Set<Tree> set = gRule.get(nodeT);
	set.add(gExprT);
}
	
	public Map<String, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>> getnTypeDrugNoticeMObjMObjM() {
		return nTypeDrugNoticeMObjMObjM;
	}

	/**
	 * String notice.type
	 * Integer drug.id
	 * Integer notice.id
	 * Integer symptom.id|expr.id (mtlO.id)
	 * Integer dose.id
	 */
	Map<String,Map<Integer,Map<Integer,Map<Integer,Map<Integer,Integer>>>>>
								nTypeDrugNoticeMObjMObjM=null;

	public void setDrugNotice(SimpleJdbcTemplate simpleJdbc) {
		if(nTypeDrugNoticeMObjMObjM!=null)			return;
		nTypeDrugNoticeMObjMObjM	= new HashMap<String, Map<Integer,Map<Integer,Map<Integer,Map<Integer,Integer>>>>>();
		String sql = " SELECT * FROM tree, notice, " +
				" (SELECT count(*),idgeneric FROM tree, drug WHERE " +
				" iddoc=? AND idclass=iddrug " +
				" GROUP BY idgeneric) g " +
				" WHERE did=g.idgeneric AND idclass=idnotice ";
		List<Map<String, Object>> sl = simpleJdbc.queryForList(sql,getDocT().getId());
		for (Map<String, Object> map : sl) {
			Notice	n			= new Notice(
					(String) map.get("notice"),
					(String) map.get("type"),
					(Integer) map.get("idnotice"));
			setDrugInfo(map, n, n.getType());
		}
		sql=" SELECT n.type AS ntype,* FROM tree t1, notice n, " +
				" (SELECT count(*),idgeneric FROM tree, drug WHERE  iddoc=? AND idclass=iddrug  GROUP BY idgeneric) g, " +
				" tree t2, symptom " +
				" WHERE t1.did=g.idgeneric AND t1.idclass=n.idnotice " +
				" AND t2.did=t1.id AND t2.idclass=idsymptom ";
		sl = simpleJdbc.queryForList(sql,getDocT().getId());
		for (Map<String, Object> map : sl) {
			Symptom	symptomO	= new Symptom(
					(Integer) map.get("idsymptom"),
					(String) map.get("symptom"));
			if(!getClassM().containsKey(symptomO.getId()))	getClassM().put(symptomO.getId(), symptomO);
			Map<Integer, Map<Integer, Integer>> mapSymptom = getMapMtlO(map);
			mapSymptom.put(symptomO.getId(), null);
		}
		sql=" SELECT n.type AS ntype,d.value AS dvalue,e.value AS evalue,* FROM tree t1, notice n, " +
		" (SELECT count(*),idgeneric FROM tree, drug WHERE iddoc=? AND idclass=iddrug  GROUP BY idgeneric) g," +
		" tree t2, expr e, tree t3, dose d" +
		" WHERE t1.did=g.idgeneric AND t1.idclass=n.idnotice " +
		" AND t2.did=t1.id AND t2.idclass=e.idexpr "+
		" AND t3.did=t2.id AND t3.idclass=d.iddose ";
		sl = simpleJdbc.queryForList(sql,getDocT().getId());
		for (Map<String, Object> map : sl) {
			Expr exprO = new Expr(
					(Integer) map.get("idexpr"),
					(String) map.get("expr"),
					(String) map.get("evalue"));
			Dose doseO = new Dose(
					(Integer) map.get("iddose"),
					(String) map.get("unit"),
					(Float) map.get("dvalue"));
			if(doseO.getValue()==0)
				continue;
			if(!getClassM().containsKey(exprO.getId()))	getClassM().put(exprO.getId(), exprO);
			if(!getClassM().containsKey(doseO.getId()))	getClassM().put(doseO.getId(), doseO);
			Map<Integer, Map<Integer, Integer>> mapExpr = getMapMtlO(map);
			HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
			hashMap.put(exprO.getId(), doseO.getId());
			mapExpr.put(exprO.getId(), hashMap);
		}
		sql=" SELECT * FROM tree, literature,  (SELECT count(*),idgeneric FROM tree, drug " +
				" WHERE  iddoc=? AND idclass=iddrug  GROUP BY idgeneric) g " +
				" WHERE did=g.idgeneric AND idclass=idliterature ";
		sl = simpleJdbc.queryForList(sql,getDocT().getId());
		for (Map<String, Object> map : sl) {
			Literature	n			= new Literature(
					(Integer) map.get("idliterature"),
					(String) map.get("title"),
					(String) map.get("authors"),
					(String) map.get("spring"),
					(String) map.get("springtype"),
					(Integer) map.get("year"),
					(String) map.get("page"),
					(String) map.get("url")
				);
			setDrugInfo(map,n,"literature");
		}
	}

	private void setDrugInfo(Map<String, Object> map, MObject n, String type) {
		if(!getClassM().containsKey(n.getId()))	getClassM().put(n.getId(), n);
		Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>
				mapDrug		= nTypeDrugNoticeMObjMObjM.get(type);
		if(mapDrug == null){
			mapDrug = new HashMap<Integer, Map<Integer,Map<Integer,Map<Integer,Integer>>>>();
			nTypeDrugNoticeMObjMObjM.put(type, mapDrug);
		}
		Integer	idGeneric	= (Integer) map.get("idgeneric");
		Map<Integer, Map<Integer, Map<Integer, Integer>>>	mapNotice	= mapDrug.get(idGeneric);
		if(mapNotice==null){
			mapNotice		= new HashMap<Integer, Map<Integer,Map<Integer,Integer>>>();
			mapDrug.put(idGeneric, mapNotice);
		}
		mapNotice.put(n.getId(), null);
	}

	private Map<Integer, Map<Integer, Integer>> getMapMtlO(Map<String, Object> map) {
		Integer	idNotice	= (Integer) map.get("idnotice");
		String type = (String) map.get("ntype");
		Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>
			mapDrug		= nTypeDrugNoticeMObjMObjM.get(type);
		Map<Integer, Map<Integer, Map<Integer, Integer>>>
			mapNotice	= mapDrug.get((Integer) map.get("idgeneric"));
		Map<Integer, Map<Integer, Integer>>	mapMtlO	= mapNotice.get(idNotice);
		if(mapMtlO==null){
			mapMtlO	= new HashMap<Integer, Map<Integer,Integer>>();
			mapNotice.put(idNotice, mapMtlO);
		}
		return mapMtlO;
	}
	public String className(MObject objM){return objM.getClass().getSimpleName().toLowerCase();}

	public void setDrugNotice_depr(SimpleJdbcTemplate simpleJdbc, EntityManager em) {
		String sql="SELECT n.*,t2.idClass AS iddrug FROM tree t1,tree t2,tree t3,tree t4,notice n " +
				" WHERE t2.iddoc=?"+
				" AND t1.iddoc=84626 " +
				" AND t1.idclass=t2.idclass" +
				" AND t1.id=t3.did and t3.id=t4.did" +
				" AND t4.idclass=n.idnotice" ;
		List<Map<String, Object>> sl = simpleJdbc.queryForList(sql,getDocT().getId());
		for (Map<String, Object> map : sl) {
			Integer	iddrug		= (Integer) map.get("iddrug"),
					idNotice	= (Integer) map.get("idnotice");
			String	notice		= (String) map.get("notice"),
					type		= (String) map.get("type");
			Notice	n		= new Notice(notice,type,idNotice);
			Drug drugC = (Drug) getClassM().get(iddrug);
		}
	}

	


	private void initProcent(Tree doseT) {
		Dose doseC = (Dose) getClassM().get(doseT.getIdClass());
		Tree pvT = JXPathBean.pDoseProcentT(this, doseT.getParentT(),0);
		Tree patientDoseProcentDoseT = JXPathBean.pDoseProcentDoseT(pvT);
		if(patientDoseProcentDoseT!=null){
			Dose doseModC = (Dose) getPatientMtl().getClassM().get(patientDoseProcentDoseT.getIdClass());
			if("pc".equals(doseModC.getType())){
				procent=(int) (doseModC.getValue()/getCalcDose(doseT)*100);
			}else
			if(doseModC.getUnit().contains("/")){
				procent= (int) (doseModC.getValue()/doseC.getValue()*100);
			}else{
				procent=(int) (doseModC.getValue()/getCalcDose().get(doseT)*100);
			}
		}else if(pvT!=null){
			Pvariable pvC = (Pvariable) getPatientMtl().getClassM().get(pvT.getIdClass());
			if(pvC==null)
				procent= 100;
			else
				procent= Integer.parseInt(pvC.getPvalue());
		}else{
			procent= 100;
		}
	}

	public void addObjecte(EntityManager em) {
		long sT = System.currentTimeMillis();
		List ol = em.createQuery("SELECT c FROM Times c, Tree t WHERE t.idClass=c.id AND t.docT.id = :id")
		.setParameter("id", getDocT().getId())
		.getResultList();
		for (Object object : ol) {
			MObject object2 = (MObject)object;
			getClassM().put(object2.getId(), object2);
			getClassM().put(object2.getId(), (Times) object);
		}
	}
	
	Map<String,String> dddL;// =new HashMap<String,String>();
	Set<Integer> weekDayS=new ConcurrentSkipListSet<Integer>();
	public Set<Integer> getWeekDayS(){return weekDayS;}
	public int getLastWeekDay(){
		if(weekDayS.size()==0)
			return 1;
		Object[] array = weekDayS.toArray();
		Integer lastDay = (Integer) array[array.length-1];
		return lastDay;
	}
	SchemaWeekPlan schemaWeekPlan;
	public SchemaWeekPlan getSchemaWeekPlan() {
		if(null==schemaWeekPlan){
			schemaWeekPlan=new SchemaWeekPlan(this);
		}
		return schemaWeekPlan;
	}
	public Map<String, String> getDrugDoseDaysM() {
		if(null==dddL){
			dddL =new HashMap<String,String>();
			weekDayS=new ConcurrentSkipListSet<Integer>();
			Set<Day> daysPeriotTS=new HashSet<Day>();
			for(Ts ts:getPlan()){
				if(ts.getBegin()!=null) continue;
				Tree	dayT	= ts.getTimesT().getParentT();
				Day		dayC	= (Day) classM(dayT);
				if(dayC!=null){
					Matcher m_absPeriod = JXPathBean.getAbsPeriod().matcher(dayC.getAbs());
					if(m_absPeriod.matches()){
						Integer b = Integer.parseInt(m_absPeriod.group(1));
						Integer e = Integer.parseInt(m_absPeriod.group(2));
						weekDayS.add(b);
						weekDayS.add(e);
						daysPeriotTS.add(dayC);
						if(e-b>5)continue;
					}
				}
				Tree drugT = dayT.getParentT();
				Tree taskT = drugT.getParentT();
				if(taskT==getDocT()
						||taskT==getInsideTask(getBlockT(), "support")
						||taskT==getTaskPremedicationT())
				{}else continue;
				weekDayS.add(ts.getCday());
				Tree doseT = JXPathBean.getDose(drugT);

				String idClassDose = getIdClass(doseT);

				String ref = "";
				if(ts.getTimesT().getRef()!=null)
					ref=ts.getTimesT().getRef().toString();
				String idClassTimes = getIdClass(ts.getTimesT());

				String drugDoseS = drugT.getIdClass()+":"+idClassDose+":"+ref+":"+idClassTimes;
				String ddDaysS = dddL.get(drugDoseS);
				if(ddDaysS==null) ddDaysS="";
				String d = ts.getCday().toString();
				ddDaysS+=d+",";
				dddL.put(drugDoseS, ddDaysS);
			}
			for(Day dayC:daysPeriotTS){
				Matcher m_absPeriod = JXPathBean.getAbsPeriod().matcher(dayC.getAbs());
				if(m_absPeriod.matches()){
					Integer b = Integer.parseInt(m_absPeriod.group(1));
					Integer e = Integer.parseInt(m_absPeriod.group(2));
					int pr = -111;
					for(Integer d:weekDayS){
						if(pr==b&&d==e){
							weekDayS.add(b+1);
						}
						pr=d;
					}
				}
			}
		}
		return dddL;
	}
	private String getIdClass(Tree doseT) {
		String idClassDose="";
		if(doseT!=null&&doseT.getIdClass()!=null)
			idClassDose = doseT.getIdClass().toString();
		return idClassDose;
	}
	
	public Dose2Tablete makeDose2Tablete(int idt) {
		setIdt(idt);
		dose2Tablete = new Dose2Tablete(this);
		return dose2Tablete;
	}

	public Dose2Tablete getDose2Tablete() {return dose2Tablete;}

	/**
	 * 
	 *	BEGIN LABOR SECTION 
	 * 	AUTHOR Simon 
	 *
	LaborEditorNode laborEditor = null;

	public LaborEditorNode getLaborEditor() {
		if(laborEditor==null)
			laborEditor = new LaborEditorNode(this);
		return laborEditor;
	}
	public void setLaborEditor(LaborEditorNode laborEditorNode) {
		this.laborEditor = laborEditorNode;
	}
	 */
	

	/**
	 * 
	 *	BEGIN FINDING SECTION 
	 * 	AUTHOR Simon 
	 *
	FindingEditorNode findingEditor = null;

	public FindingEditorNode getFindingEditor() {
		if(findingEditor==null)
			findingEditor = new FindingEditorNode(this);
		return findingEditor;
	}
	public void setFindingEditor(FindingEditorNode findingEditor) {
		this.findingEditor = findingEditor;
	}
	 */
	
	/*
	SymptomEditorNode symptomEditor = null;

	public SymptomEditorNode getSymptomEditor() {
		if(symptomEditor==null)
			symptomEditor = new SymptomEditorNode(this);
		return symptomEditor;
	}
	 * */

	//Cycle application count block.
	
	private List<Map<String, Object>> modL;
	public List<Map<String, Object>> getModL() {return modL;}
	public int getModNr(){return modL.size();}
	public void setModL(List<Map<String, Object>> ll){modL=ll;}
	public List<Tree> getModTL() {
		ArrayList<Tree> modTL = new ArrayList<Tree>();
		for(Tree t1:getPatientMtl().getDocT().getChildTs())
			for(Tree t2:t1.getChildTs())
				if(t2.getRef()!=null&&t2.getRef().equals(getDocT().getId()))
					modTL.add(t1);
		return modTL;
	}
	public List<Tree> getModTL_depr() {
		ArrayList<Tree> modTL = new ArrayList<Tree>();
		for (int i = 0; i < modL.size(); i++) {
			Tree t = getPatientMtl().getTree().get((Integer) modL.get(i).get("id"));
			if(t.getParentT().getId().equals(getPatientMtl().getDocT().getId())){
				modTL.add(t);
			}
		}
		return modTL;
	}
	@Override
	boolean isThisDocumentTag(Tree tree) {return true;}
	
	
	
	Tree testT;
	public Tree getTestT() {return testT;}
	public void setTestT(Tree t) {this.testT=t;}
	
	
	public void correcturNotice() {
		if(getEditNoticeC().hasNotice())
			getEditNoticeC().setNotice(getEditNoticeC().getNotice()
				.replace("<br _moz_editor_bogus_node=\"TRUE\" />", "").trim());
	}
	public Tree getBlockT() {return getDocT();}
	
	private Owuser copyUser;
	public Owuser getCopyUser() {return copyUser;}
	public void setCopyUser(Owuser owUser) {this.copyUser=owUser;}
	
	public Notice noticeC(String position,String foot)
	{
		Notice noticeC=null;
		for(Tree noticeT:getDocT().getChildTs()){
			if(noticeT.getMtlO() instanceof Notice){
//				if(schemaMtl.classM(noticeT) instanceof Notice){
			for(Tree dayT:noticeT.getChildTs())
				for(Tree positionT:dayT.getChildTs())
					if(positionT.getMtlO() instanceof Position) {
//						if(schemaMtl.classM(positionT) instanceof Position)
						Position positionC=(Position) positionT.getMtlO();
//						Position positionC=(Position) schemaMtl.classM(positionT);
						if(position.equals(positionC.getPosition()) && foot.equals(positionC.getFoot()))
							noticeC=(Notice) noticeT.getMtlO();
//						noticeC=(Notice) schemaMtl.classM(noticeT);
					}
			}
		}
		return noticeC;
	}
	private DrugMtl drugMtl;
	public DrugMtl getDrugMtl() {return drugMtl;}
	public void setDrugMtl(DrugMtl drugMtl) {this.drugMtl=drugMtl;}
	
	public void takenBlock(TaskNodeCreator taskDrugCreator, MtlDbService schemaMtlService){
		Tree drugBlockT = getDrugMtl().getTree().get(getIdt());
		for(Tree t:drugBlockT.getChildTs())
			if(t.getMtlO() instanceof Task)
		{
			String task = ((Task) t.getMtlO()).getTask();
			Tree innerTask = 
				schemaMtlService.initInsideTask(this, task);
			for(Tree drugT:t.getChildTs())
				if(drugT.getMtlO() instanceof Drug)
				{
					Tree supportDrugT = getChild(innerTask,drugT.getIdClass());
					if(supportDrugT==null){
						supportDrugT = taskDrugCreator.setTreeManager(this)
						.setParentT(innerTask)
						.setIdclass1(drugT.getIdClass())
						.addChild();
						supportDrugT.setMtlO(drugT.getMtlO());
					}
					for(Tree t2:drugT.getChildTs()){
						Tree suportDrugChildT = null;
						if(t2.getMtlO() instanceof Day){
							suportDrugChildT = getChild(supportDrugT, "day");
						}else if(t2.getMtlO() instanceof Dose){
							suportDrugChildT = getChild(supportDrugT, "dose");
						}
						if(suportDrugChildT!=null)
							suportDrugChildT.setMtlO(t2.getMtlO());
					}
				}
		}	else if(t.getIdClass().equals(getEditNodeT().getIdClass()))
		{//edited drug.
			Tree drugBlockDoseT=null;
			for(Tree t2:t.getChildTs())
				if(t2.getMtlO() instanceof Dose)	drugBlockDoseT=t2;
			Tree editDoseT=null;
			for(Tree t2:getEditNodeT().getChildTs()) 
				if("dose".equals(t2.getTabName()))	editDoseT=t2;
			editDoseT.setIdClass(drugBlockDoseT.getIdClass());
		}
	}

	private Tree getChild(Tree parentT,String tabName) {
		if(parentT.getChildTs()!=null)
		for (Tree tree : parentT.getChildTs())
			if(tabName.equals(tree.getTabName()))
				return tree;
	return null;
}
	private Tree getChild(Tree parentT,Integer idClass) {
		if(parentT.getChildTs()!=null)
		for (Tree tree : parentT.getChildTs()) 
			if(idClass.equals(tree.getIdClass()))
				return tree;
		return null;
	}
	public List patientDoseProcentT(Tree drugT,List dosePvL){
		Integer idTask = drugT.getDocT().getId();
		if(getPatientMtl()!=null){
			Tree patientT = getPatientMtl().getDocT();
			for(Tree t1:patientT.getChildTs())
				for(Tree t2:t1.getChildTs())
					if(idTask.equals(t2.getRef()))//task
						for(Tree t3:t2.getChildTs())//drug
							if(drugT.getId().equals(t3.getRef()))
								for(Tree t4:t3.getChildTs())//day
									if(t4.getMtlO() instanceof Day)
										for(Tree t5:t4.getChildTs())//dose
											for(Tree t6:t5.getChildTs())//pv
												dosePvL.add(t6);
		}
		return dosePvL;
	}
	public Ts getTs4day(Tree drugT, Integer day){
		if(drugT.hasChild())
		for(Tree dayT:drugT.getChildTs()){
			if(dayT.hasChild())
			for(Tree timesT:dayT.getChildTs())
			{
				Set<Ts> set = getTimesTs().get(timesT);
				if(set!=null)
					for (Ts ts : set)
						if(day.equals(ts.getCday()))
							return ts;
			}
		}
		return null;
	}
	public Set tsInDay(Ts ts){
		Set<Ts> hashSet = new HashSet<Ts>();
		for (Ts ts2 : getTimesTs().get(ts.getTimesT()))
			if(ts2.getCday().equals(ts.getCday()))
				hashSet.add(ts2);
		return hashSet;
	}
	public Integer malInDay(Ts ts){
		Integer i = 0;
		for (Ts ts2 : getTimesTs().get(ts.getTimesT()))
			if(ts2.getCday().equals(ts.getCday()))
				i++;
		return i;
	}
	public Integer doseProMal(Ts ts, JXPathBean jxp){
		Integer malInDay = malInDay(ts);
		Tree drugT = ts.getTimesT().getParentT().getParentT();
		Iterator<Pointer> iteratePointers = jxp.getJxp(drugT).iteratePointers(
				"childTs/childTs/childTs[mtlO/pvariable='mgProMal']");
		while (iteratePointers.hasNext()) {
			Tree mgProMalT = (Tree)iteratePointers.next().getValue();
		}
		return 0;
	}
	private boolean printSite=false;
	public boolean isPrintSite(){return printSite;}
	public void setPrintSite(boolean printSite) {this.printSite=printSite;}
	public MObject seekEditO(List list) {
		Integer id=getEditExpr1Id();
		MObject editMtlO=null;
		for (int i = 0; i < list.size(); i++) {
			MObject mtlO = (MObject) list.get(i);
			if(mtlO.getId().equals(id))
				editMtlO=mtlO;
		}
		return editMtlO;
	}
	//-----------------------
	//Expression editor
	//-----------------------
	public void openEditEqualsExpr(){
		Tree eqT = getEditNodeT();
		Expr eqO = (Expr) eqT.getMtlO();
		log.debug(eqO);
		Tree expr1T = eqT.getChildTs().get(0);
		Tree expr2T = eqT.getChildTs().get(1);
		Tree pvExpr2T = expr2T.getChildTs().get(0);
		editEqvalO=new Expr();
		copyAtt(eqO,editEqvalO);
		log.debug(editEqvalO);
		Pvariable pvExpr2O = (Pvariable) pvExpr2T.getMtlO();
		setEditPvC(new Pvariable());
		copyAtt(getEditPvC(),pvExpr2O);
	}
	public void openEditAndOrExpr(){
		Tree eqT = getEditNodeT();
		Expr eqO = (Expr) eqT.getMtlO();
		andOrExprO=new Expr();
		copyAtt(eqO,andOrExprO);
	}

	String typeOfDialog;
	public String getTypeOfDialog() {return typeOfDialog;}
	public void setTypeOfDialog(String typeOfDialog) {this.typeOfDialog = typeOfDialog;}

	Integer editExpr1Id;
	public Integer getEditExpr1Id()	{return editExpr1Id;}
	public void setEditExpr1Id(Integer editExpr1Id) {this.editExpr1Id = editExpr1Id;}
	
	private	Expr editExprC, editEqvalO,andOrExprO;
	public void setEditExprC(Expr editExprC) {this.editExprC = editExprC;}
	public Expr getEditEqvalO()	{return editEqvalO;}
	public Expr getAndOrExprO()	{return andOrExprO;}
	public Expr getEditExprC()	{return editExprC;}
	
	public void copyAtt(Expr editNodeExpr,Expr editExprC) {
		editExprC.setExpr(editNodeExpr.getExpr());
		editExprC.setType(editNodeExpr.getType());
		editExprC.setUnit(editNodeExpr.getUnit());
		editExprC.setValue(editNodeExpr.getValue());
	}
	public void copyAtt(Pvariable editPvO, Pvariable pvExpr2O) {
		editPvO.setPvariable(pvExpr2O.getPvariable());
		editPvO.setPvalue(pvExpr2O.getPvalue());
		editPvO.setUnit(pvExpr2O.getUnit());
	}
	public Tree correcturEditNodeT() {
		Tree editNodeT = getEditNodeT();
		if(null==editNodeT){
			setEditNodeT();
			editNodeT = getEditNodeT();
		}
		return editNodeT;
	}

	void runRule(){
		log.debug("a");
		Tree pschemaT = getPschemaT();
		String xp="childTs[tabName='drug']/childTs/childTs[mtlO/ivariable='taskTsNr']";
		Iterator iteratePointers = JXPathContext.newContext(pschemaT).iteratePointers(xp);
		while (iteratePointers.hasNext()) {
			Tree taskTsNrT = (Tree) ((Pointer) iteratePointers.next()).getValue();
			Integer taskTsNr = getIvariableO(taskTsNrT).getIvalue();
			Tree drugT = getTree().get(taskTsNrT.getParentT().getParentT().getRef());
			Ts task4TsNr = getTask4TsNr(drugT, taskTsNr);
			log.debug(task4TsNr);
			Tree appT = (Tree) JXPathContext.newContext(taskTsNrT.getParentT()).getPointer("childTs[tabName='app']").getValue();
			if(null!=appT)
			{
				ruleApp(appT);
			}
		}
	}

	private void ruleApp(Tree appT) {
		Ts task4TsNr=getTask4TsNr();
		Tree drugT = task4TsNr.getTimesT().getParentT().getParentT();
		Tree appOldT = (Tree) JXPathContext.newContext(drugT).getPointer("childTs[tabName='app']").getValue();
		int hmsOld = getHms(Ts._1s, appOldT);
		int hms = getHms(Ts._1s, appT);
		log.debug(hms);
		log.debug(task4TsNr);
		Timestamp tsEndOld = task4TsNr.getTsEnd();
		log.debug(tsEndOld);
		long timeEndOld = tsEndOld.getTime();
		calcTs(task4TsNr,task4TsNr.getTime(), hms);
		log.debug(task4TsNr);
		Timestamp tsEnd = task4TsNr.getTsEnd();
		log.debug(tsEnd);
		long timeEndDiff = tsEnd.getTime()-timeEndOld;
		log.debug(timeEndDiff);
		log.debug(timeEndDiff/1000);
		log.debug(timeEndDiff/1000/60);
		log.debug(timeEndDiff/1000/60/60);
		ruleAddDiff2ts(task4TsNr, timeEndDiff);
	}
	private void ruleAddDiff2ts(Ts ts1, long timeEndDiff){
		ruleAddDiff2ts(ts1, timeEndDiff, "childTs/childTs/childTs[ref='"+ts1.getTimesT().getId()+"']");
		ruleAddDiff2ts(ts1, timeEndDiff, "childTs/childTs/childTs/childTs[ref='"+ts1.getTimesT().getId()+"']");
	}
	private void ruleAddDiff2ts(Ts ts1, long timeEndDiff,  String xp) {
//		log.debug("........................ \n"+ts1.getTimesT().getParentT().getParentT()+"\n"+ts1);
//		Tree timesT = ts1.getTimesT();
//		Iterator iteratePointers = JXPathContext.newContext(timesT.getDocT()).iteratePointers(xp);
		Iterator iteratePointers = JXPathContext.newContext(getDocT()).iteratePointers(xp);
		while (iteratePointers.hasNext()) {
			Tree times2T = (Tree) ((Pointer) iteratePointers.next()).getValue();
			for (Ts ts2 : getTimesTs(times2T)){
				//if(ts.getCday()==ts1.getCday()){
//				if(ts2.getDay()==ts1.getDay()){
				if(ts2.getDay()==getTask4TsNr().getDay()){
					if("0".equals(getTimesO(times2T).getApporder())){//after
						ts2.setTime(ts2.getTime()+timeEndDiff);
						ts2.getTsEnd().setTime(ts2.getTsEnd().getTime()+timeEndDiff);
					}else if("1".equals(getTimesO(times2T).getApporder())&&ts1!=getTask4TsNr()){//before
						log.debug(times2T.getParentT().getParentT()+"\n"+ts2);
						ts2.setTime(ts2.getTime()+timeEndDiff);
						ts2.getTsEnd().setTime(ts2.getTsEnd().getTime()+timeEndDiff);
						log.debug(ts2);
					}
					ruleAddDiff2ts(ts2,timeEndDiff);
				}
			}
		}
	}
	private void makeSpEL(Tree timesT) {
		ExpressionParser parser = new SpelExpressionParser();
		log.debug(timesT.getDocT());
		StandardEvaluationContext context = new StandardEvaluationContext(timesT.getDocT());
		context.setVariable("timesId", timesT.getId());
		context.setVariable("dose", "dose");
//		String eStr = "childTs.?[tabName=='drug']!.childTs.?[tabName=='day']";
		String eStr = "childTs[1].childTs";
//		String eStr = "getChildTs().getChildTs().?[tabName=='day']";
		List<Tree> value = (List<Tree>) parser.parseExpression(eStr).getValue(context);
		log.debug(value);
		log.debug(value.size());
	}
	/*
	Integer cycleNumber;
	public Integer getCycleNumber() {return cycleNumber;}
	public void setCycleNumber(Integer cycleNumber) {this.cycleNumber = cycleNumber;}
	 * */
	
	
	private Tree firstSchemaT;
	private Integer idClassUseTask,idClassNewUseTask;
	public Tree getFirstSchemaT() {return firstSchemaT;}
	public void initUseVariantSchema(Integer idClassUseTask,Integer idClassNewUseTask){
		firstSchemaT=null;
		this.idClassUseTask=idClassUseTask;
		this.idClassNewUseTask=idClassNewUseTask;
	}
	public void correcturUseVarianteSchema(Tree pT) {
		if(pT.hasChild())
			for(Tree t:pT.getChildTs())
				if(idClassUseTask.equals(t.getIdClass())
				&&!t.getId().equals(t.getIdClass())
				){
					t.setIdClass(idClassNewUseTask);
					if(null==firstSchemaT)
						firstSchemaT=t;
					if(idClassUseTask.equals(idClassNewUseTask))
						return ;
				}else if("choice".equals(t.getTabName())||"studyarm".equals(t.getTabName())){
					correcturUseVarianteSchema(t);
				}
	}
	//////////////////
	// footnotes BEGIN
	//////////////////
	/**
	 * Notice or expr tree to theirs number.
	 */
	Map<Tree, Integer>		footnotesM;
	Map<Integer, Tree>		i2neTM;
	Map<Integer,Set<Ts>>	neNr2tsSM;
	Set<Integer>			neNrS;
	public Map<Integer, Set<Ts>>	getNeNr2tsSM()	{return neNr2tsSM;}
	public Set<Integer>				getNeNrS()		{return neNrS;}
	public Map<Integer, Tree>		getI2neTM()		{return i2neTM;}
	public Map<Tree, Integer> getFootnotesM(){
		if(null==footnotesM){
			footnotesM=new HashMap<Tree, Integer>();
			i2neTM=new HashMap<Integer, Tree>();
			neNr2tsSM=new HashMap<Integer, Set<Ts>>();
			if(getDocT().hasChild())
				for(Tree t:getDocT().getChildTs())//drug|task
				{
					if(t.hasChild())
						for(Tree t1:t.getChildTs())//day|dose|drug/drug|task/drug
						{
							if(isNE(t1)&&!(t1.getParentT().getMtlO() instanceof Task)){
								log.debug(t1);
								addFootnote(t1);
							}
							if(t1.hasChild())
								for(Tree t2:t1.getChildTs()){//notice|expr|drug/drug/dose|
									addFootnote(t2);
									if(t2.hasChild())
										for(Tree t3:t2.getChildTs()){
											addFootnote(t3);
											if(t3.hasChild())
												for(Tree t4:t3.getChildTs())
													addFootnote(t4);
										}
								}
						}
				}
			neNrS = new ConcurrentSkipListSet<Integer>(footnotesM.values());
			for(Ts ts:getPlan()){
				Tree drugT = ts.getTimesT().getParentT().getParentT();
				if(drugT.hasChild())
					for(Tree t1:drugT.getChildTs())//dose|day
					{
						if(isNE(t1))
							addTs2NeNr(ts, t1);
						if(t1.hasChild())
							for(Tree t2:t1.getChildTs()){
								if(isNE(t2))
									addTs2NeNr(ts, t2);
								if(t2.hasChild())
									for(Tree t3:t2.getChildTs())
										if(isNE(t3))
											addTs2NeNr(ts, t3);
							}
					}
			}
		}
		return footnotesM;
	}
	private void addTs2NeNr(Ts ts, Tree neT) {
		Integer neNr = footnotesM.get(neT);
		Set<Ts> tsS = neNr2tsSM.get(neNr);
		if(null==tsS){
			tsS=new ConcurrentSkipListSet<Ts>();
			neNr2tsSM.put(neNr, tsS);
		}
		tsS.add(ts);
	}
	private void addFootnote(Tree t) {
		if(isNE(t)){
			int i = footnotesM.size()+1;
			footnotesM.put(t, i);
			i2neTM.put(i,t);
		}
	}
	private boolean isNE(Tree t) {
		return (isNoticeO(t)&&!(
				"rqm".equals(getNoticeO(t).getType())
				||"sup".equals(getNoticeO(t).getType()))
				)
		||(isExprO(t)&&!isExprO(t.getParentT()));
//		return isNoticeO(t)||(isExprO(t)&&!isExprO(t.getParentT()));
	}
	public Integer neTimesNr(Tree neT){
		Tree drugT = neT.getParentT().getParentT();
		if(isDrugO(drugT.getParentT()))
			drugT=drugT.getParentT();
		if(isDrugO(neT.getParentT()))
			drugT=neT.getParentT();
		for(Tree dayT:drugT.getChildTs()){
			if(isDayO(dayT))
				for(Tree timesT:dayT.getChildTs())
					if(isTimesO(timesT))
						return getTimesNr().get(timesT);
		}
		return 0;
	}
	//////////////////
	// footnotes END
	//////////////////

	Integer cycleIdClass=0;
	Integer cycleNr,appNr;
	public Integer getCycleIdClass()		{return cycleIdClass;}
	public Boolean getIsApp()				{return isApp;}
	public void setCycleNr(Integer cycleNr)	{this.cycleNr = cycleNr;}
	public void setAppNr(Integer appNr)		{this.appNr = appNr;}
	public void setIsApp(Boolean isApp)		{this.isApp = isApp;}
	private Boolean isApp;
	public Integer getAppNr() {
		
		return appNr;
	}

	public Integer getCycleNr(){//deprecated go to patientMtl.cycleNr(schemaT)
		if(null==cycleNr){
			cycleNr=0;
			if(null!=getPatientMtl()){
				Tree tree = getDocT();
				HashMap<Integer, Tree> pSchemaRefM = getPatientMtl().getPschemaRefM();
				cycleNr=getCycleNr(tree,pSchemaRefM);
			}
		}
		return cycleNr;
	}
	public static Integer getCycleNr(Tree tree,HashMap<Integer, Tree> pSchemaRefM) {
		Integer cycleNr=0;
		if(pSchemaRefM.containsKey(tree.getId()))
			for(Tree vCycleNrT:pSchemaRefM.get(tree.getId()).getChildTs())
				if(isPvariableO(vCycleNrT)&&"cycle".equals(getPvariableO(vCycleNrT).getPvariable()))
				{
//					log.debug(vCycleNrT);
					String pvalue = getPvariableO(vCycleNrT).getPvalue();
					if(pvalue.length()>0)
						cycleNr=Integer.parseInt(pvalue);
				}
		return cycleNr;
	}
	public Integer getCycleNr_depr(){//deprecated go to patientMtl.cycleNr(schemaT)
		if(null==cycleNr){
			cycleNr=0;
			if(null!=getPatientMtl()){
//				String xp = "childTs[idclass=" +getDocT().getMtlO().getId() +"]" +
				String xp = "childTs[ref=" +getDocT().getId() +"]" +
				"/childTs[mtlO/pvariable='cycle']";
				log.debug(xp);
				Tree vCycleNrT = (Tree)JXPathContext.newContext(getPatientMtl().getDocT())
				.getPointer(xp).getValue();
				if(isPvariableO(vCycleNrT)){
					String pvalue = getPvariableO(vCycleNrT).getPvalue();
					cycleNr=Integer.parseInt(pvalue);
				}
			}
		}
		return cycleNr;
	}
	
	public void calcCycleNr_depr() {
		cycleNr=0;
		appNr=0;
		isApp=false;
		Tree protocolT = getProtocolT();
		if("subchemo".equals(getSchemaC().getType())){
			isApp=true;
			if(protocolT!=null)
				for(Tree choiceT:protocolT.getChildTs())
					if(choiceT.getChildTs()!=null) 
						for(Tree armT:choiceT.getChildTs()){
							if(getConceptMtl()!=null && getConceptMtl().getCycleIdClass()!=null
								&&
								getConceptMtl().getCycleIdClass().equals(armT.getIdClass())
							){
								cycleNr++;
								appNr=0;
								for(Tree schemaT:armT.getChildTs())
									if(schemaT.getIdClass()!=null&&schemaT.getIdClass().equals(getSchemaC().getId()))
										appNr++;
							}
						}
		}else if("chemo".equals(getSchemaC().getType())){
			if(protocolT!=null)
				for(Tree taskT:protocolT.getChildTs())
					if(taskT.getIdClass()!=null && getSchemaC().getId().equals(taskT.getIdClass()))
						cycleNr++;
		}
		if(cycleNr==0)	cycleNr=1;
		if(appNr==0)	appNr=1;
	}
	Folder conceptFolderO;
	public Folder getConceptFolderO() {
		return conceptFolderO;
	}
	public void setConceptFolderO(Folder conceptFolderO) {
		this.conceptFolderO=conceptFolderO;
	}
	static int taskLength = 30;
	public String getTitle(){
		Task taskO = getTaskO(getDocT());
		String task = taskO.getTask();
		if(task.length()>taskLength){
			int tlh = taskLength/2;
			String nt = task.substring(0,tlh);
			nt+="...";
			nt+= task.substring(task.length()-tlh,task.length());
			task=nt;
		}
		return task;
	}
	public Tree getApplicationDurationT(JXPathContext docContext) {
		Tree applicationDurationT = (Tree) docContext.getPointer("childTs[mtlO/ivariable='applicationDuration']").getValue();
		return applicationDurationT;
	}
	public Tree getApplicationDurationT(JXPathBean docContext) {
		JXPathContext jxp = docContext.jxp();
		return getApplicationDurationT(jxp);
	}
	
	private Set<IntensivDay > intensivDays;
	private Set<Integer>  therapyDaySet;
	public void setTherapyDaySet(Set<Integer> therapyDaySet) {this.therapyDaySet = therapyDaySet;}
	public Set<Integer> getTherapyDaySet() {return therapyDaySet;}
	
	public Set<IntensivDay> getIntensivDays() {return intensivDays;}
	public void setIntensivDays(Set<IntensivDay> intensivDays) {
		this.intensivDays = intensivDays;
	}
	
	public static IntensivDay getIntensivDay(int i) {
		if(i==1)
			return IntensivDay.valueOf("PA");
		else if(i==2)
			return IntensivDay.valueOf("PB");
		return IntensivDay.valueOf("P"+i);
	}
	public Tree ancestor(Tree t, String find) {
		if("rootIf".equals(find))
		{
			if(isExprO(t))
			{
				if("if".equals(getExprO(t).getExpr())){
					if(!isExprO(t.getParentT()))
					{
						return t;
					}
					
				}
			}
		}else if("drug".equals(find))
		{
			if(isDrugO(t))
				return t;
		}
		if(null==t.getParentT())
			return null;
		else if(t.getDocT()!=t.getParentT().getDocT())
			return null;
		return ancestor(t.getParentT(), find);
	}
	
	Map<Integer, Contactperson> historyM = null;
	public Map<Integer, Contactperson> getHistoryM()
	{
		if(historyM == null)
			historyM=new HashMap<Integer, Contactperson>();
		return historyM;
	}
	
	public void setHistoryM(Map<Integer, Contactperson> historyM) {	this.historyM = historyM;}

	Map<String, Object> historyM2 = null;
	public Map<String, Object> getHistoryM2() {return historyM2;}
	public void setHistoryM2(Map<String, Object> historyM) {	this.historyM2 = historyM;}

}

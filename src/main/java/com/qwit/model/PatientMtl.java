package com.qwit.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.qwit.domain.Diagnose;
import com.qwit.domain.Dose;
import com.qwit.domain.History;
import com.qwit.domain.Institute;
import com.qwit.domain.MObject;
import com.qwit.domain.Patient;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;

//public class PatientMtl extends TreeManager implements Serializable
public class PatientMtl extends SchemaMtl implements Serializable
{
	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	private int bmiMax = 60;
	
	public PatientMtl(Tree docT) {
		super(docT);
		ofDateM=new HashMap<Tree, Tree>();
	}
	public List<Tree> nextCycleList(Tree parentT){
		List<Tree> dd=new ArrayList<Tree>();
		for(Tree schemaT:parentT.getChildTs())
		{
			
		}
		return dd;
	}
	
	public int cycleNr(Tree schemaT){
		int cycleNr=0,cycleNrPlus=0,childNr=schemaT.childNr();
		for (int i = childNr; i >= 0; i--) 
		{
			Tree tree = schemaT.getParentT().getChildTs().get(i);
			if(isClassOrVariant(schemaT, tree))
			{
				if(getPschemaRefM().containsKey(tree.getId()))
				{
					cycleNr=SchemaMtl.getCycleNr(tree, getPschemaRefM());
					break;
				}
				cycleNrPlus++;
			}
		}
		return cycleNr+cycleNrPlus;
	}
	
	private HashMap<Integer, Tree> pschemaRefM;
	public HashMap<Integer, Tree> getPschemaRefM() 
	{
		if(null==pschemaRefM)
		{
			pschemaRefM=new HashMap<Integer, Tree>();
			for(Tree pSchemaT:getDocT().getChildTs())
				if(null!=pSchemaT.getRef())
					pschemaRefM.put(pSchemaT.getRef(), pSchemaT);
		}
		return pschemaRefM;
	}

	HistoryYearManager year =null;
	public HistoryYearManager getYear(){
		if(null==year)
		{
			year=new HistoryYearManager(this);
		}
		return year;
	}
	public Tree getFolderT(){return getDocT().getParentT();}
	Map<Tree,Tree> ofDateM;
	
	public Map<Tree, Tree> getOfDateM() {return ofDateM;}


	Integer idMovestation;
	public Integer getIdMovestation() {return idMovestation;}
	public void setIdMovestation(Integer idMovestation) {this.idMovestation = idMovestation;}

	
	/*
	 * (non-Javadoc)
	 * @see com.qwit.model.SchemaMtl#addTreePaar_depr(com.qwit.domain.Tree)
	@Override
	void addTreePaar_depr(Tree tree) {
		String tabName=tree.getTabName();
		String parentTabName = tree.getParentT().getTabName();
//		if(tabName.equals("pvariable")){
		if(getClassM().get(tree.getIdClass()) instanceof Pvariable){
			Pvariable pvC=(Pvariable) getClassM().get(tree.getIdClass());
			if(pvC!=null && pvC.getPvariable().equals("ofDate")){
				ofDateM.put(tree.getParentT(), tree);
			}
			if(parentTabName.equals("finding")){
				values2bsa(tree);
			}
		}
	}
	 */

	public void calcDose(SchemaMtl schemaMtl){
		for (MObject dose1C : getClassM().values())
			if(dose1C instanceof Dose){
//				schemaMtl.calc1Dose(dose1C);
			}
	}
	/*
	private void values2bsa(Tree tree) {
		Finding varO = getFindingO(tree.getParentT());
		Ivariable valO = getIvariableO(tree);
		if(valO.getIvariable().equals(tree.getParentT().getTabName())){
			if(weight==null&&varO.getFinding().equals("weight")){
				weight=valO.getIvalue();
			}else if(height==null&&varO.getFinding().equals("height")){
				height=valO.getIvalue();
			}
		}
	}
	private void values2bsa_depr(Tree tree) {
		Finding varC = (Finding) getClassM().get(tree.getParentT().getIdClass());
		Pvariable valC = (Pvariable) getClassM().get(tree.getIdClass());
		if(valC.getPvariable().equals(tree.getParentT().getTabName())){
			String numberStr = valC.getPvalue();
//			log.debug(pvalue);
			if(weight==null&&varC.getFinding().equals("weight")){
				numberStr = numberStr.replace(",", ".");
//				weight	= Float.parseFloat(numberStr);
			}else if(height==null&&varC.getFinding().equals("height")){
				log.debug(numberStr);
				int indexOf = numberStr.indexOf(".");
				if(indexOf>0)
					numberStr = numberStr.substring(0,indexOf);
				height	= Integer.parseInt(numberStr);
			}
		}
	}
	 * */

	private boolean viewBsaFormula = false;
	public boolean isViewBsaFormula() {return viewBsaFormula;}
	public void setViewBsaFormula() {viewBsaFormula=viewBsaFormula?false:true;}

	public void calcBsa() {
		log.debug(1);
		setIsBsaCalculated(true);
		log.debug(getIsBsaCalculated());
		log.debug(1);
//		if(weight!=null&&height!=null){
		if(getWeight()!=null&&getHeight()!=null){
			log.debug(1);
			calc2Bsa();
			log.debug(1);
		}
		log.debug(1);
	}

	public double getBmi()
	{	
		return getBmi(weight, height);
	}

	public double getBmi(Integer weight, Integer height)
	{
		//what to do if height Zero?
		if(height==null)
			return 0;
//		double w = getWeight();
//		double h = getHeight()/100;
		double w = weight;
		double h = height/100.0;
		double r = w / (h*h);
//		log.debug("getBmi.w: " +w);
//		log.debug("getBmi.h: " +h);
//		log.debug("getBmi.r: " +r);
		return r;
	}
	
	//returns 0 if bmi higher than 60
	public int getBmiCategory()
	{		
		double bmi = getBmi();
		if( bmi < 16)
			return 1;
		else if(bmi >= 16.0 && bmi < 17.0)
			return 2;
		else if(bmi >= 17.0 && bmi < 18.5)
			return 3;
		else if(bmi >= 18.5 && bmi < 25.0)
			return 4;
		else if(bmi >= 25.0 && bmi < 30.0)
			return 5;
		else if(bmi >= 30.0 && bmi < 35.0)
			return 6;
		else if(bmi >= 35.0 && bmi < 40.0)
			return 7;
		else if(bmi >= 40)
			return 8;
	
		return 0;
	}
	
	public int getBmiMax() {return bmiMax;}
	public boolean getIsBmiUnreal() 
	{
		return isBmiUnreal(weight, height);
	}
	public boolean isBmiUnreal(Integer	weight, Integer	height) 
	{
		log.debug("isBmiUnreal w:" + weight);
		log.debug("isBmiUnreal h:" + height);
		log.debug("bsaType:" + bsaType);
		log.debug("isBmiUnreal bmi:" + getBmi(weight, height));
		log.debug("isBmiUnreal bmiMax:" + bmiMax);
		return ((getBmi(weight, height) > bmiMax) ? true:false);
	}
	
//	Float	weight;
//	public void setWeight(Float weight) {this.weight = weight;}
	Integer	weight;
	String	weightUnit;
	public void setWeight(Integer weight) {this.weight = weight;}
	public Integer getWeight() {
		if(weight==null){
			Tree wT = getFindingIValT("weight");
			if(null!=wT){
				weight=getIvariableO(wT).getIvalue();
				weightUnit=getFindingO(wT.getParentT()).getUnit();
			}
//			if(weight==null) return 1;
		}
		return weight;
	}

	public DateTime getOfDateHistory(Tree parentT) {
		Tree pt = getOfDate(parentT);
		log.debug(pt);
		History h = pt.getHistory();
//		Calendar c = Calendar.getInstance();
//		c.setTimeInMillis(h.getMdate().getTime());
		DateTime dateTime = new DateTime(h.getMdate());
		return dateTime;
	}
	Tree getOfDate(Tree parentT) {
		log.debug(parentT);
		if(null!=parentT&&parentT.hasChild())
		for (Tree t : parentT.getChildTs()) 
			if(isPvariableO(t)&&"ofDate".equals(getPvariableO(t).getPvariable()))
				return t;
		return null;
	}
	public Tree getFindingSValT(String finding) {
		JXPathContext docTContext = JXPathContext.newContext(getDocT());
		String xp = "childTs[mtlO/finding='"+finding +"']/childTs[mtlO/pvariable='finding']";
		Tree wT = (Tree) docTContext.getPointer(xp).getValue();
		return wT;
	}
	public Tree getFindingIValT(String finding) {
		JXPathContext docTContext = JXPathContext.newContext(getDocT());
//		docTContext.setValue("finding", finding);
//		String xp = "childTs[mtlO/finding=$finding]/childTs[mtlO/ivariable='finding']";
		String xp = "childTs[mtlO/finding='"+finding +"']/childTs[mtlO/ivariable='finding']";
		Tree wT = (Tree) docTContext.getPointer(xp).getValue();
		return wT;
	}
	Integer	height;
	public void setHeight(Integer height) {this.height = height;}
	public Integer getHeight() {
		if(null==height)
		{
			Tree hT = getFindingIValT("height");
			if(null!=hT){
				height=getIvariableO(hT).getIvalue();
			}
		}
		return height;
	}
	String	bsaType;
	public void setBsaType(String bsaType) {this.bsaType = bsaType;}
	public String getBsaType() {
		if(null==bsaType)
		{
			Tree bsaTypeT = getFindingSValT("bsaType");
			if(null!=bsaTypeT){
				bsaType=getPvariableO(bsaTypeT).getPvalue();
			}
		}
		return bsaType;
	}


	public void reviseBsa(){
		bsa=null;
		getBsa();
	}
	
	private Float bsaReal=new Float(0),bsaAibw, bsa2,bsa2a, bsa;
	public Float getBsaReal()	{return bsaReal;}
	public Float getBsaAibw()	{return bsaAibw;}
	public Float getBsa2()		{return bsa2;}
	public Float getBsa2a()		{return bsa2a;}
	public Float getBsa() {
//		if(weight==null&&height==null)
		if(getWeight()==null||getHeight()==null)
			return (float)1;//danger
		if(bsa==null){
			calc2Bsa();
		}
		return bsa;
	}

	private double wa;
	String bmiType="max2";
	public String getBmiType() {return bmiType;}
	public Float calc2Bsa(){
		bsaReal	= calcBsaReal();
		bsa2	= calcBsa2();
		bsaAibw	= calcBsaAibw();
		bsa2a	= calcBsa2a();
		if(bmiType==null)					bsa=bsaReal;
		else if(bmiType.equals("true"))		bsa=bsaReal;
		else if(bmiType.equals("real"))		bsa=bsaReal;
		else if(bmiType.equals("aibw"))		bsa=bsaAibw;
		else if(bmiType.equals("max2"))		bsa=bsa2;
		else if(bmiType.equals("2m2"))		bsa=bsa2;
		else if(bmiType.equals("max2aibw"))	bsa=bsa2;
		else if(bmiType.equals("2m2aibw"))	bsa=bsa2;
		String bsaType2 = getBsaType();
		if(bsaType2==null)					bsa=bsaReal;
		else if(bsaType2.equals("real"))	bsa=bsaReal;
		else if(bsaType2.equals("2m2"))		bsa=bsa2;
		else if(bsaType2.equals("aibw"))	bsa=bsaAibw;
		else if(bsaType2.equals("2m2aibw"))	bsa=bsa2a;
		
		return bsa;
	}

	public Float calcBsa2a() {
		bsa2a=bsaAibw>2?2:bsaAibw;
		return bsa2a;
	}
	public Float calcBsaAibw() {
		double ibw = getIbw();
//		wa				= ibw+0.4*(weight-ibw);
		wa				= getAibw();
		if(wa>=weight)
			return calcBsaReal();
		double	sqrt	= Math.sqrt(Math.sqrt(Math.pow(height*wa/3600,2)));//Retzel
		bsaAibw			= (float) sqrt;
		return bsaAibw;
	}

	public double getAibw() {
//		if(weight==null)
//			return 0;
		double ibw = getIbw();
		double aibw = ibw+0.4*(weight-ibw);
		return aibw;
	}
	public double getIbw() {
		Patient patientC = getPatientC();
		double	sk		= patientC.getSex().equals("M")? 50.0 : 45.5,
				ibw		= sk + (2.3*(height/2.53 -60));
		return ibw;
	}
	public Integer ageMonthInYear(){
		Calendar today = Calendar.getInstance();
		Calendar birthDate = getBirthDateCalendar();
		int bdMM = birthDate.get(Calendar.MONTH);
		int todayMM = today.get(Calendar.MONTH);
		int mm1;
		if(bdMM > todayMM){
			mm1 = bdMM-todayMM;
		}else{
			mm1 = 12+todayMM-bdMM;
		}
		return mm1;
	}
	public int getAgeInMonth() {return getAgeYear()*12+ageMonthInYear();}
	public Integer getAgeYear(){
		Calendar today = Calendar.getInstance();
		Calendar bd = getBirthDateCalendar();
		Integer ageYear = today.get(Calendar.YEAR) - bd.get(Calendar.YEAR);
		if((bd.get(Calendar.MONTH) > today.get(Calendar.MONTH))
				|| (bd.get(Calendar.MONTH) == today.get(Calendar.MONTH)
						&& bd.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH)))
		{
			ageYear--;
		}
		return ageYear;
	}

	private Calendar getBirthDateCalendar() {
		Calendar bd = Calendar.getInstance();
		Timestamp pbd = getPatientC().getBirthdate();
		bd.setTimeInMillis(pbd.getTime());
		return bd;
	}

	
	public Patient getPatientC() {
		return (Patient) getClassM().get(getDocT().getIdClass());
	}
	public Float calcBsa2() {
		bsa2= (bsaReal>2)?2:bsaReal;
		return bsa2;
	}
	public Float calcBsaReal(){
//		Float weightInKg = weight;
		Integer weightInKg = getWeight();
		Integer heightInCm = getHeight();
		if(null==weightInKg||null==heightInCm)
			return null;
		if(weightInKg>0&&heightInCm>0)
			bsaReal = surface(heightInCm,weightInKg);
		return bsaReal;
	}
//	private Float surface(Integer heightInCm, Float weightInKg) {
	private Float surface(Integer heightInCm, Integer weightInKg) {
		Float surfase= new Float(0.0);
		String bsaformula = getBsaFormula();
		if(bsaformula.equals("mosteller"))		surfase = surfaceMosteller(heightInCm, weightInKg);
		else if(bsaformula.equals("haycock"))	surfase = surfaceHaycock(heightInCm, weightInKg);
		else if(bsaformula.equals("dubois"))	surfase = surfaceDubois(heightInCm, weightInKg);
		else if(bsaformula.equals("gehan"))		surfase = surfaceGehanGeorge(heightInCm, weightInKg);
		else if(bsaformula.equals("boyd"))		surfase = surfaceBoyd(heightInCm, weightInKg);
		return surfase;
	}
	public String getBsaFormula() {
		String bsaformula = "dubois";
		OwsSession owsSession = OwsSession.getOwsSession();
		if(null!=owsSession) {
//			Institute stationO = owsSession.getStationO();
			Map<String, Object> stationM = owsSession.getStationM();
			if(null!=stationM) {
				String bsaformula2 = (String) stationM.get("bsaformula");
				if(null!=bsaformula2)
					bsaformula = bsaformula2;
			}
		}
		return bsaformula;
	}
	public float surfaceBoyd(double heightInCm,double weightInKg) {
		return (float) (0.0003207 * Math.pow(heightInCm,0.3) * 
			Math.pow(weightInKg*1000,0.7285 - 0.0188 * Math.log10(weightInKg*1000)));
	}
	public float surfaceGehanGeorge(double heightInCm,double weightInKg) {
		return (float) (0.0235 * Math.pow(heightInCm,0.42246) * Math.pow(weightInKg,0.51456));
	}
	public float surfaceDubois(double heightInCm,double weightInKg) {
		return (float) (0.007184 * Math.pow(heightInCm,0.725) * Math.pow(weightInKg,0.425));
	}
	public float surfaceHaycock(double heightInCm,double weightInKg) {
		return (float) (0.024265*Math.pow(heightInCm,0.3964)*Math.pow(weightInKg,0.5378));
	}
	/**
	 * Formeln nach http://flexicon.doccheck.com/K%F6rperoberfl%E4che
	 * body surface area (BSA)
	 * @param heightInCm
	 * @param weightInKg
	 * @return
	 */
	public float surfaceMosteller(double heightInCm,double weightInKg) {
		return (float) Math.sqrt(heightInCm*weightInKg/3600.0);
	}
	public float getBsa_mosteller() {return (null!=weight&&null!=height)?surfaceMosteller(height, weight):0;}
	public float getBsa_haycock() {return (null!=weight&&null!=height)?surfaceHaycock(height, weight):0;}
	public float getBsa_dubois() {return (null!=weight&&null!=height)?surfaceDubois(height, weight):0;}
	public float getBsa_gehanGeorge() {return (null!=weight&&null!=height)?surfaceGehanGeorge(height, weight):0;}
	public float getBsa_boyd() {return (null!=weight&&null!=height)?surfaceBoyd(height, weight):0;}

	@Override
	public void paste() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void addMtlMap(MObject mtlC) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void add1ClassObj_depr(Tree tree,MObject classM) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void addTreeClassObj_depr(MObject objM, Tree tree) {
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean isThisDocumentTag(Tree tree) {
		if(!tree.getDocT().equals(getDocT()))
			return false;
		return true;
	}

	Integer tuboId;
	public Integer getTuboId() {
		if(null==tuboId){
			Tree tuboDocT = getTuboDocT();
			if(null!=tuboDocT)
				tuboId=tuboDocT.getRef();
		}
		return tuboId;
	}
	public void setTuboId(Integer id) {this.tuboId=id;}
	
	public Tree getTuboDocT(){
		Tree eT = getEditNodeT();
		while (null!=eT) {
			if(eT.getMtlO() instanceof Patient){
				break;
			}else if(eT.getMtlO() instanceof Task){
				Task tuboTaskO=(Task) eT.getMtlO();
				if("tubo".equals(tuboTaskO.getType()))
					return eT;
				else
					eT=eT.getParentT();
			}else{
				eT=eT.getParentT();
			}
		}
		return null;
	}
	/*
	public String handleAction(String a){
		String handleAction=null;
		if(null!=a&&a.length()>0){
			handleAction=a;
			log.debug("a="+handleAction);
//		}
//		if(null!=handleAction&&!"editStep".equals(handleAction)){
		}else if(hasAction()){
			handleAction=getAction();
			log.debug("getAction()="+handleAction);
//		}else if(null!=getEditNodeT()){
		}else if(null!=getIdt()){
			handleAction=getEditNodeName();
			log.debug("getEditNodeName()="+handleAction);
		}
//		isNewEdit();
		log.debug("----------------------END-");
		return handleAction;
	}
	 * */
	public void checkCloseEditId(){
		if(!hasIdt())
			closeEditId();
	}

	public void openEditDiagnose(){
		Tree editNodeT = getEditNodeT();
	}
	private Diagnose editDiagnoseO;
	public void openEditDose(){
		editDiagnoseO=new Diagnose();
		Diagnose editNodeDiagnoseO=(Diagnose) getEditNodeT().getMtlO();
		if(null!=editNodeDiagnoseO){
			editDiagnoseO.setDiagnose(editNodeDiagnoseO.getDiagnose());
		}
	}

	Map<Integer, Integer> chronologNr ;
	public Map<Integer, Integer> getChronologNr() {return chronologNr;}
	public void calcChronologNr() {
		int size = getDocT().getChildTs().size();
		chronologNr = new HashMap<Integer, Integer>();
		for(Tree t:getDocT().getChildTs())
			chronologNr.put(t.getId(), size--);
	}

	private Set<Integer> task4protocol;
	public Set<Integer> getTask4protocol() {return task4protocol;}
	public void setTask4protocol(Set<Integer> task4protocol) {this.task4protocol = task4protocol;}

	private Map<Integer, Map<String, Object>> therapyChange;
	public Map<Integer, Map<String, Object>> getTherapyChange() {return therapyChange;}
	public void setTherapyChange(Map<Integer, Map<String, Object>> tcMap) {therapyChange=tcMap;}
	
	private boolean isBsaCalculated = false;
	public void setIsBsaCalculated(boolean isCalculated) {	this.isBsaCalculated = isCalculated;}
	public boolean getIsBsaCalculated() {return isBsaCalculated;}

}

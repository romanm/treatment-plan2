package com.qwit.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.qwit.domain.Patient;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Tablet;

public class PatientSchema implements Serializable{
	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;

	private String action;

	public String getAction()			{return action;}
	public void setAction(String action){this.action = action;}

	String laborUnit;
	public String getLaborUnit() {return laborUnit;}
	public void setLaborUnit(String laborUnit) {this.laborUnit = laborUnit;}

	BigDecimal nlaborValue;
	public BigDecimal getNlaborValue() {return nlaborValue;}
	public void setNlaborValue(BigDecimal nlaborValue) {this.nlaborValue = nlaborValue;}

	Integer laborValue;
	public Integer getLaborValue() { return laborValue; }
	public void setLaborValue(Integer laborValue) { this.laborValue = laborValue; }

	boolean allDay;
	public boolean isAllDay() {return allDay;}
	public void setAllDay(boolean allDay) {this.allDay = allDay;}

	Integer tsNr;
	public Integer getTsNr()			{return tsNr;}
	public void setTsNr(Integer tsNr)	{this.tsNr = tsNr;}

	Integer idt;
	public Integer getIdt()			{return idt;}
	public void setIdt(Integer idt)	{this.idt = idt;}

	Integer dateDay, dateMonth, dateYear;
	public Integer getDateDay() {
		return dateDay;
	}
	public void setDateDay(Integer dateDay) {
		this.dateDay = dateDay;
	}
	public Integer getDateMonth() {
		return dateMonth;
	}
	public void setDateMonth(Integer dateMonth) {
		this.dateMonth = dateMonth;
	}
	public Integer getDateYear() {
		return dateYear;
	}
	public void setDateYear(Integer dateYear) {
		this.dateYear = dateYear;
	}

	Patient patientO;
	public Patient getPatientO() {
		if(null==patientO)
			patientO=new Patient();
		return patientO;
	}

	/**
	 * def|proc|calc
	 */
	String doseModType;
	public String getDoseModType() {return doseModType;}
	public void setDoseModType(String doseModType) {this.doseModType = doseModType;}

	Float doseDeff,doseCalcf;
	public Float getDoseCalcf() { return doseCalcf; }
	public void setDoseCalcf(Float doseCalcf) { this.doseCalcf = doseCalcf; }
	public Float getDoseDeff() {return doseDeff;}
	public void setDoseDeff(Float doseDeff) {this.doseDeff = doseDeff;}

	Integer doseDef,doseDef100,doseProc,doseCalc,doseCalc100;
	public Integer getDoseCalc100()	{return doseCalc100;}
	public Integer getDoseDef100()	{return doseDef100;}
	public Integer getDoseDef()		{return doseDef;}
	public Integer getDoseProc()	{return doseProc;}
	public Integer getDoseCalc()	{return doseCalc;}
	public void setDoseDef100(Integer doseDef)	{this.doseDef100 = doseDef;}
	public void setDoseCalc100(Integer doseCalc){this.doseCalc100 = doseCalc;}
	public void setDoseDef(Integer doseDef)		{this.doseDef = doseDef;}
	public void setDoseProc(Integer doseProc)	{this.doseProc = doseProc;}
	public void setDoseCalc(Integer doseCalc)	{this.doseCalc = doseCalc;}

	private String notice;
	public String getNotice() {return notice;}
	public void setNotice(String notice) {this.notice = notice;}
	public boolean hasNotice() {return null!=notice&&notice.length()>0;}

	// Tablet divisor BEGIN
	/**
	 * Tablet quantitas
	 */
	private Integer t0quantity,t0dquantity,t1quantity,t1dquantity,t2quantity,t2dquantity,t3quantity,t3dquantity;
	public Integer getT0quantity()	{return t0quantity;}
	public Integer getT0dquantity()	{return t0dquantity;}
	public Integer getT1quantity()	{return t1quantity; }
	public Integer getT1dquantity()	{return t1dquantity; }
	public Integer getT2quantity()	{return t2quantity; }
	public Integer getT2dquantity()	{return t2dquantity; }
	public Integer getT3quantity()	{return t3quantity; }
	public Integer getT3dquantity()	{return t3dquantity; }
	public void setT0quantity(Integer t0quantity) {this.t0quantity = t0quantity;}
	public void setT0dquantity(Integer t0dquantity) {this.t0dquantity = t0dquantity;}
	public void setT1quantity(Integer t1quantity) { this.t1quantity = t1quantity; }
	public void setT1dquantity(Integer t1dquantity) { this.t1dquantity = t1dquantity; }
	public void setT2quantity(Integer t2quantity) { this.t2quantity = t2quantity; }
	public void setT2dquantity(Integer t2dquantity) { this.t2dquantity = t2dquantity; }
	public void setT3quantity(Integer t3quantity) { this.t3quantity = t3quantity; }
	public void setT3dquantity(Integer t3dquantity) { this.t3dquantity = t3dquantity; }

	Integer drugReception;
	public Integer getDrugReception() {return drugReception;}
	public void setDrugReception(Integer drugReception) {this.drugReception = drugReception;}

	Integer tsn;
	public Integer getTsn() {return tsn;}
	public void setTsn(Integer tsn) {this.tsn = tsn;}

	/**
	 * Drug tablet list.
	 */
	private List<Tablet> drugTabletL;
	public List<Tablet> getDrugTabletL() {return drugTabletL;}
	public void setDrugTabletL(List<Tablet> drugTabletL) {this.drugTabletL=drugTabletL;}

	private Integer tabletGabeDiv;
	public void setTabletGabeDiv(Integer tableGabeDiv) {this.tabletGabeDiv = tableGabeDiv;}
	public Integer getTabletGabeDiv() {return tabletGabeDiv;}
	// Tablet divisor END
	// bsa BEGIN
//	int heightCm,weightKg;
//	public int getHeightCm() {return heightCm;}
//	public void setHeightCm(int heightCm) {this.heightCm = heightCm;}
//	public int getWeightKg() {return weightKg;}
//	public void setWeightKg(int weightKg) {this.weightKg = weightKg;}

//	String bsaType;
//	public	String	getBsaType()				{return bsaType;}
//	public	void	setBsaType(String bsaType)	{this.bsaType = bsaType;}
//	float bsa;
//	public void setBsa(float bsa) {this.bsa = bsa;}
//	public float getBsa() {return bsa;}
	private	SchemaMtl	schemaMtl;
	private	PatientMtl	patientMtl;
	public void setPatientMtl(PatientMtl patientMtl) {this.patientMtl = patientMtl;}
	public PatientMtl getPatientMtl() {return patientMtl;}	
	public	SchemaMtl	getSchemaMtl() {return schemaMtl;}
	public	void		setSchemaMtl(SchemaMtl docMtl) {this.schemaMtl=docMtl;}
//	Tree weightVarT=null, heightVarT=null, bsaTypeVarT=null;
//	public Tree getWeightVarT() {return weightVarT;}
//	public Tree getHeightVarT() {return heightVarT;}
//	public Tree getBsaTypeVarT() {return bsaTypeVarT;}
//	Tree weightValT=null, heightValT=null, bsaTypeValT=null;
//	public Tree getWeightValT() {return weightValT;}
//	public Tree getHeightValT() {return heightValT;}
//	public Tree getBsaTypeValT() {return bsaTypeValT;}
	
//	Calendar weightDC=null, heightDC=null, bsaTypetDC=null;
//	public Calendar getWeightDC() {return weightDC;}
//	public Calendar getHeightDC() {return heightDC;}
//	public Calendar getBsaTypetDC() {return bsaTypetDC;}
	/*
	public void initBsa(PatientMtl docMtl) {

		//below function is wrong and absolete 
		setSchemaMtl(docMtl);		
		//use this
		setPatientMtl(docMtl);
		
		weightValT = docMtl.getFindingIValT("weight");
		if(null!=weightValT){
			weightVarT = weightValT.getParentT();
			weightKg	= docMtl.getIvariableO(weightValT).getIvalue();
		}
		heightValT = docMtl.getFindingIValT("height");
		if(null!=heightValT){
			heightVarT = heightValT.getParentT();
			heightCm	= docMtl.getIvariableO(heightValT).getIvalue();
		}
		bsaTypeValT = docMtl.getFindingSValT("bsaType");
		if(null!=bsaTypeValT){
			bsaTypeVarT = bsaTypeValT.getParentT();
			bsaType		= docMtl.getPvariableO(bsaTypeValT).getPvalue();
		}
	}
	 * */
	/*

	public void initBsa(SchemaMtl docMtl) {
		setSchemaMtl(docMtl);
		PatientMtl patientMtl = docMtl.getPatientMtl();
		Tree patientT = patientMtl.getDocT();
//		Pvariable weightP=null, heightP=null;
//		Ivariable weightI=null, heightI=null;
		for(Tree t1:patientT.getChildTs()){
			if(PatientMtl.isFindingO(t1)){
				Tree ofDateT = null;
				for(Tree t2:t1.getChildTs()){
					if(PatientMtl.isIvariableO(t2)){
						Ivariable iv = PatientMtl.getIvariableO(t2);
						if("finding".equals(iv.getIvariable())){
							Finding varO = PatientMtl.getFindingO(t1);
							log.debug(varO);
							if(heightVarT==null&&"cm".equals(varO.getUnit())){
								heightVarT=t1;
								heightValT=t2;
								heightCm=iv.getIvalue();
								//								heightI=iv;
							}else if(weightVarT==null&&"kg".equals(varO.getUnit())){
								weightVarT=t1;
								weightValT=t2;
								weightKg=iv.getIvalue();
								//								weightI=iv;
							}
						}
					}else if(patientMtl.isPvariableO(t2)){//for old ows only
						Pvariable pv =	PatientMtl.getPvariableO(t2);
						if("ofDate".equals(pv.getPvariable())){
							ofDateT=t2;
						}else if(bsaTypeVarT==null&&"bsaType".equals(pv.getUnit())){
							bsaTypeVarT=t1;
							bsaTypeValT=t2;
						}else if("finding".equals(pv.getPvariable())){
							if(heightVarT==null&&"cm".equals(pv.getUnit())){
								heightVarT=t1;
//								heightP=pv;
								heightCm	= pv2int(pv);
							}else if(weightVarT==null&&"kg".equals(pv.getUnit())){
								weightVarT=t1;
								weightValT=t2;
//								weightP=pv;
								weightKg= pv2int(pv);
							}
						}
					}
				}
				
//				if(ofDateT.getParentT()==heightVarT && heightDC==null){
//					Calendar c = Calendar.getInstance();
//					c.setTimeInMillis(ofDateT.getHistory().getMdate().getTime());
//					heightDC=c;
//				}else if(ofDateT.getParentT()==bsaTypeValT && bsaTypetDC==null){
//					Calendar c = Calendar.getInstance();
//					c.setTimeInMillis(ofDateT.getHistory().getMdate().getTime());
//					bsaTypetDC=c;
//				}else if(ofDateT.getParentT()==weightVarT && weightDC==null){
//					Calendar c = Calendar.getInstance();
//					c.setTimeInMillis(ofDateT.getHistory().getMdate().getTime());
//					weightDC=c;
//				}
			}
			if(null!=weightVarT&&null!=heightVarT)
				continue;
		}
	}
		 * */
	private int pv2int(Pvariable pv) {
		String numberStr = pv.getPvalue();
		numberStr = numberStr.replace(",", ".");
		int indexOf = numberStr.indexOf(".");
		if(indexOf>0)
			numberStr = numberStr.substring(0,indexOf);
		int parseInt = Integer.parseInt(numberStr);
		return parseInt;
	}
	// bsa END
	int delayedDay;
	public int getDelayedDay() {return delayedDay;}
	public void setDelayedDay(int delayedDay) {this.delayedDay = delayedDay;}

	// newCycle BGIN
	int cycleNr;
	public int	getCycleNr()			{return cycleNr;}
	public void	setCycleNr(int cycleNr)	{this.cycleNr = cycleNr;}
	
//	@DateTimeFormat(pattern = "MM-dd-yyyy")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	bdate;
	public Date getBdate() {return bdate;}
	public void setBdate(Date bdate) {this.bdate = bdate;}

	private Integer currCycleId;
	public void setCurrCycleId(Integer currCycleId) {this.currCycleId=currCycleId;}
	public Integer getCurrCycleId() {return currCycleId;}

	private Timestamp currCycleOfDate;
	public void setCurrCycleOfDate(Timestamp mdate) {this.currCycleOfDate=mdate;}
	public Timestamp getCurrCycleOfDate() {return currCycleOfDate;}
	
        /**
         * A minimal date constraint for the dijitDateTextBox.
         * Note: Dojo only understand a string in the format yyyy-MM-dd 
         * (international standard ISO 8601) as Date or the Date object itself.
         * 
         * @return The date in the format yyyy-MM-dd.
         */
        public String getMinBeginDateConstraint() {
		DateTime minBeginDate = new DateTime(bdate);
		return minBeginDate.toString("yyyy-MM-dd");
	}
        
	public Timestamp getCurrCycleOfDate(int days) {
		DateTime dt = new DateTime(currCycleOfDate);
		dt=dt.plusDays(days);
		Timestamp ts = new Timestamp(dt.toDate().getTime());
		return ts;
	}
	// newCycle END

	String diagnose,jsonDiagnose;
	public String getDiagnose() {return diagnose;}
	public void setDiagnose(String diagnose) {this.diagnose = diagnose;}

	int idDiagnose;
	public int getIdDiagnose() {return idDiagnose;}
	public void setIdDiagnose(int idDiagnose) {this.idDiagnose = idDiagnose;}

	public void setJsonDiagnose(String jsonDiagnose) {this.jsonDiagnose=jsonDiagnose;}
	public String getJsonDiagnose() {return jsonDiagnose;}

	private List<Map<String, Object>> seekDzL;
	public List<Map<String, Object>> getSeekDzL() {return seekDzL;}
	public void setSeekDzL(List<Map<String, Object>> seekDzL) {this.seekDzL=seekDzL;}

	private List<Map<String, Object>> gcsfBlockList;
	public List<Map<String, Object>> getGcsfBlockList() {return gcsfBlockList;}
	public void setGcsfBlockList(List<Map<String, Object>> gcsfBlockList) {this.gcsfBlockList=gcsfBlockList;}

}

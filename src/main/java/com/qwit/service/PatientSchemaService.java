package com.qwit.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Diagnose;
import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Finding;
import com.qwit.domain.History;
import com.qwit.domain.Institute;
import com.qwit.domain.Ivariable;
import com.qwit.domain.Labor;
import com.qwit.domain.MObject;
import com.qwit.domain.Notice;
import com.qwit.domain.Nvariable;
import com.qwit.domain.Patient;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Tablet;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.model.InstituteMtl;
import com.qwit.model.PatientMtl;
import com.qwit.model.PatientSchema;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.util.MtlObjectePool;

@Service("patientSchemaService")
@Repository
public class PatientSchemaService extends MtlDbService {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired @Qualifier("findingCreator")	DbFindingCreator	findingCreator;
	@Autowired @Qualifier("nvCreator")		DbNvariableCreator	nvCreator;
	@Autowired @Qualifier("ivCreator")		DbIvariableCreator	ivCreator;
	@Autowired @Qualifier("doseCreator")	DbDoseCreator		doseCreator;
	@Autowired @Qualifier("deleteService")	DeleteService		deleteService;

	public void editPatientEntry(PatientSchema ps, PatientMtl docMtl){
		Patient patientO = docMtl.getPatientO(docMtl.getDocT());
		log.debug("------------"+patientO);
		ps.getPatientO().setForename(patientO.getForename());
		ps.getPatientO().setPatient(patientO.getPatient());
		ps.getPatientO().setSex(patientO.getSex());
		ps.getPatientO().setInsurance(patientO.getInsurance());
		Timestamp birthdate = patientO.getBirthdate();
		ps.getPatientO().setBirthdate(birthdate);
		DateTime bdDT = new DateTime(birthdate.getTime());
		ps.setDateDay(bdDT.dayOfMonth().get());
		ps.setDateMonth(bdDT.monthOfYear().get());
		ps.setDateYear(bdDT.yearOfEra().get());
		log.debug("------------"+ps.getDateDay());
		log.debug("------------"+ps.getDateMonth());
		log.debug("------------"+ps.getDateYear());
	}
	@Transactional
	public void editPatientSave(PatientSchema ps, PatientMtl docMtl){
		log.debug("------------");
		Patient patientO = docMtl.getPatientO(docMtl.getDocT());
		patientO.setForename(ps.getPatientO().getForename());
		patientO.setPatient(ps.getPatientO().getPatient());
		patientO.setSex(ps.getPatientO().getSex());
		patientO.setInsurance(ps.getPatientO().getInsurance());
		DateTime bdDT = new DateTime(ps.getDateYear(),ps.getDateMonth(),ps.getDateDay(),1,1,1,1);
		patientO.setBirthdate(new Timestamp(bdDT.getMillis()));
		em.merge(patientO);
	}
	public void editSchemaPropertyEntry(PatientSchema ps, TreeManager docMtl){
		log.debug("------------");
		Task taskO = getTaskO(docMtl);
		Integer appDuration = taskO.getDuration();
		for(Tree appDurationT:docMtl.getDocT().getChildTs())
			if(docMtl.isIvariableO(appDurationT)
			&& "applicationDuration".equals(docMtl.getIvariableO(appDurationT).getIvariable())
			)
				appDuration=docMtl.getIvariableO(appDurationT).getIvalue();
		schemaMtl(docMtl).setAppDuration(appDuration);
	}
	@Transactional(readOnly = true)
	public void editSchemaPropertySave(PatientSchema ps, TreeManager docMtl){
		log.debug("------------");
		Task taskO = getTaskO(docMtl);
		Integer appDuration = schemaMtl(docMtl).getAppDuration();
		log.debug("------------");
		em.merge(taskO);
		JXPathContext docContext = JXPathContext.newContext(docMtl.getDocT());
		Tree applicationDurationT = schemaMtl(docMtl).getApplicationDurationT(docContext);
		log.debug("------------"+applicationDurationT);
		log.debug("------------"+taskO.getDuration());
		log.debug("------------"+appDuration);
		log.debug("------------"+(0==appDuration));
		log.debug("------------"+(appDuration>=taskO.getDuration()));
		log.debug("------------"+(0==appDuration||appDuration>=taskO.getDuration()));
		if(0==appDuration||appDuration>=taskO.getDuration()) {
			if(null!=applicationDurationT) {
				log.debug("-----delete-------"+applicationDurationT);
			}
		}else{
			log.debug("------------");
			Ivariable applicationDurationO 
			= (Ivariable) ivCreator.setIvariable("applicationDuration")
				.setIvalue(appDuration).build();
			log.debug("------------"+applicationDurationO);
			if(null==applicationDurationT) {
				applicationDurationT = addChild("ivariable", docMtl.getDocT(), docMtl.getDocT());
				applicationDurationT.setSort(0);
				log.debug("------------"+applicationDurationT);
			}
			applicationDurationT.setIdClass(applicationDurationO.getId());
		}
	}
	
	private Task getTaskO(TreeManager docMtl) {
		SchemaMtl schemaMtl=schemaMtl(docMtl);
		Task taskO = schemaMtl.getTaskO(docMtl.getDocT());
		return taskO;
	}
	@Transactional(readOnly = true)
	public void patientDiagnoseEntry(PatientSchema ps, TreeManager docMtl){
		log.debug("------------");
		long sT = System.currentTimeMillis();
		String sql=" SELECT d FROM Diagnose d,Tree t " +
				" WHERE t.id=d.id " +
				" AND t.parentT.id!=418199 " +
				" AND t.parentT.id!=479962 " +
				" ORDER BY d.diagnose ";
		String str="";
		log.debug("------------");
		List<Diagnose> dzL = em.createQuery(sql).getResultList();
		log.debug("------------"+dzL.size());
		try {
			JSONObject put = new JSONObject().put("identifier", "name").put("label", "name");
			JSONArray ja = new JSONArray();
			for (Diagnose dz : dzL) {
				ja.put(new JSONObject().put("name", dz.getDiagnose()));
			}
			put.put("items", ja);
			str = put.toString();
		} catch (JSONException e) {e.printStackTrace();}
		log.debug("------------");
		ps.setJsonDiagnose(str);
		PatientMtl patientMtl = docMtl.getPatientMtl();
//		PatientMtl patientMtl = getPatientMtl(docMtl);
		if(null!=patientMtl)
		for (Tree t1 : patientMtl.getDocT().getChildTs()) if(docMtl.isDiagnoseO(t1)){
				ps.setDiagnose(docMtl.getDiagnoseO(t1).getDiagnose());
				for (Tree t2 : t1.getChildTs()) if(docMtl.isNoticeO(t2)){
						Notice noticeO = docMtl.getNoticeO(t2);
						ps.setNotice(noticeO.getNotice());
					}
			}
	}
	
	public void 	creatinineLabor2aucEntry(PatientSchema ps, TreeManager docMtl){
		gfrLabor2aucEntry(ps, docMtl);
	}
	@Transactional
	public void creatinineLabor2aucSave(PatientSchema ps, TreeManager docMtl){
		String laborUnit = ps.getLaborUnit();
		log.debug("------------"+laborUnit);
		String replace = laborUnit.replace("/", "").toLowerCase();
		log.debug("------------"+replace);
		String laborName = "Creatinine";
		String objectKey = laborName +"_"+replace;
		log.debug("------------"+objectKey);
		Labor laborO = mtlObjPool.buildLaborO(objectKey);
		Tree laborT = addPatientHistoryElement(docMtl, "labor", laborO);
		BigDecimal gfrValue = ps.getNlaborValue();
		log.debug("gfrValue="+gfrValue);
		
		
		addLaborNValue(laborT, laborName, gfrValue);
	}
	
	public void 	gfrLabor2aucEntry(PatientSchema ps, TreeManager docMtl){
		ps.setBdate(DateTime.now().toDate());
	}
	@Transactional
	public void gfrLabor2aucSave(PatientSchema ps, TreeManager docMtl){
		log.debug("------------");
		//		Labor laborO = mtlObjPool.getLaborO("GFR");
		ps.getLaborValue();
		String laborName = "GFR";
		Labor laborO = mtlObjPool.buildLaborO(laborName+"_mLmin");
		log.debug("------------"+laborO);
		Tree laborT = addPatientHistoryElement(docMtl, "labor", laborO);
//		Integer gfrValue = ps.getLaborValue();
		BigDecimal gfrValue = ps.getNlaborValue();
		log.debug("gfrValue="+gfrValue);
		
		addLaborNValue(laborT, laborName, gfrValue);
//		addLaborIValue(laborT, laborName, gfrValue);
	}
	private void addLaborNValue(Tree laborT, String laborName, BigDecimal gfrValue) {
		Nvariable gfrLaborValueO
			= (Nvariable) nvCreator.setNvariable(laborName)
				.setNvalue(gfrValue).build();
		log.debug("gfrLaborValueO="+gfrLaborValueO);
		Tree laborValT = addChild("nvariable", laborT);
		laborValT.setIdClass(gfrLaborValueO .getId());
		log.debug("laborValT="+laborValT);
	}
	private void addLaborIValue(Tree laborT, String laborName, Integer gfrValue) {
		Ivariable gfrLaborValueO
			= (Ivariable) ivCreator.setIvariable(laborName)
				.setIvalue(gfrValue).build();
		log.debug("gfrLaborValueO="+gfrLaborValueO);
		Tree laborValT = addChild("ivariable", laborT);
		laborValT.setIdClass(gfrLaborValueO .getId());
		log.debug("laborValT="+laborValT);
	}
	// adGcsf BEGIN
	public void 	addGcsfEntry(PatientSchema ps, TreeManager docMtl){
		log.debug("..............................1");
		List<Map<String, Object>> gcsfBlockList = simpleJdbc.queryForList("SELECT taskT.id FROM task taskO,tree taskT, tree drugT" +
				" where taskO.type='gcsf' and taskO.task='drug' " +
				" and taskO.idtask=taskT.idclass " +
				" and drugT.did=taskT.id");
		ps.setGcsfBlockList(gcsfBlockList);
		for (Map<String, Object> map : gcsfBlockList) {
			Integer id = (Integer) map.get("id");
			Tree gcsfT = em.find(Tree.class, id);
			docMtl.addClass(gcsfT, gcsfT.getDocT().getId(), em);
			docMtl.getTree().put(id, gcsfT);
			log.debug(id +" " + gcsfT);
		}
		log.debug("..............................2");
	}
	@Transactional
	public void 	addGcsfSave(PatientSchema ps, SchemaMtl docMtl){
		log.debug("..............................1");
		log.debug(docMtl.getIdc());
		Tree gcsfT = docMtl.getTree().get(docMtl.getIdc());
		log.debug(gcsfT);
		Tree schemaGcsfT=null;
		for (Tree schemaGcsf2T : docMtl.getDocT().getChildTs()) {
			if(docMtl.isTaskO(schemaGcsf2T)&&"gcsf".equals(docMtl.getTaskO(schemaGcsf2T).getType())){
				schemaGcsfT=schemaGcsf2T;
				log.debug(schemaGcsfT);
			}
		}
		for (Tree drugT : gcsfT.getChildTs()) {
			log.debug(drugT);
			addPaste(schemaGcsfT, docMtl, drugT);
		}
		log.debug("..............................2");
	}
	// adGcsf END
	private Tree addPatientHistoryElement(TreeManager docMtl, String tagName,
			Labor laborO) {
		Tree patientT = docMtl.getPatientMtl().getDocT();
		log.debug("------------"+patientT);
		Tree laborT = addChild(tagName, patientT);
		log.debug("------------"+laborT);
		laborT.setIdClass(laborO.getId());
		laborT.setSort(0-Calendar.getInstance().getTimeInMillis());
		addOfDate(laborT);
		return laborT;
	}
	@Autowired @Qualifier("mtlObjPool")	MtlObjectePool		mtlObjPool;
	
	@Transactional
	public void patientDiagnoseSave(PatientSchema ps, TreeManager docMtl){
		String diagnose = ps.getDiagnose();
		log.debug("------------"+diagnose);
		log.debug("------------"+ps.getIdDiagnose());
		String notice = ps.getNotice();
		log.debug("------------"+notice);
		log.debug("------------");
		List<Diagnose> dzL = getDiagnoseL(diagnose);
		log.debug("------------"+dzL);
		Diagnose diagnoseO = null;
		if(dzL.size()>0){
			diagnoseO = dzL.get(0);
		}else{
			return;
		}
		log.debug("------------"+diagnoseO);
		//		PatientMtl patientMtl = getPatientMtl(docMtl);
		PatientMtl patientMtl = docMtl.getPatientMtl();
		Tree patientT = patientMtl.getDocT();
		log.debug("------------"+patientT);
		Tree diagnoseT = addChild("diagnose", patientT);
		log.debug("------------"+diagnoseT);
		diagnoseT.setIdClass(diagnoseO.getId());
		diagnoseT.setSort(0-Calendar.getInstance().getTimeInMillis());
		addOfDate(diagnoseT);
		if(ps.hasNotice()){
			Notice noticeO = new Notice(notice,"diagnose");
			log.debug("------------"+noticeO);
			noticeO = (Notice) noticeCreator.setMtlO(noticeO).build();
			log.debug("------------"+noticeO);
			Tree noticeT = addChild("notice", diagnoseT);
			noticeT.setIdClass(noticeO.getId());
			log.debug("------------");
		}
	}
	public List<Diagnose> getDiagnoseL(String diagnose) {
		String sql="SELECT d FROM Diagnose d WHERE d.diagnose=:diagnose ";
		List<Diagnose> dzL = em.createQuery(sql)
		.setParameter("diagnose", diagnose)
		.getResultList();
		return dzL;
	}
	/*
	private PatientMtl getPatientMtl(TreeManager docMtl) {
		PatientMtl patientMtl = docMtl.getPatientMtl();
		if(docMtl instanceof PatientMtl)
			patientMtl=(PatientMtl) docMtl;
		return patientMtl;
	}
	 * */
	public void patientDiagnoseExit(PatientSchema ps, TreeManager docMtl){
		log.debug("------------");
	}

	// bsa BEGIN
//	public void bsaDialogEntry(PatientSchema ps, TreeManager docMtl){
//		log.debug("------------");
//		initBsa(ps, docMtl);
////		ps.setCalc1(false);
//	}
//	private void initBsa(PatientSchema ps, TreeManager docMtl) {
//		PatientMtl patientMtl = getPatientMtl(docMtl);
////		ps.initBsa(patientMtl);
//		log.debug("------------");
//	}
	private PatientMtl getPatientMtl(TreeManager docMtl) {
		PatientMtl patientMtl = docMtl.getPatientMtl();
		if(docMtl instanceof PatientMtl)
			patientMtl=(PatientMtl) docMtl;
		return patientMtl;
	}
//	public void bsaDialogCalc(PatientSchema ps, SchemaMtl docMtl){
//		log.debug("------------");
//		PatientMtl patientMtl = getPatientMtl(docMtl);
////		patientMtl.setHeight(ps.getHeightCm());
////		patientMtl.setWeight(ps.getWeightKg());
////		patientMtl.calcBsa();
////		if(ps.getHeightCm()>0&&ps.getWeightKg()>0)
////			ps.setCalc1(true);
//	}
	/*
	 * <finding finding="bsaType">
	 * 	<pvariable pvalue="real|aibw|max2m2|max2m2aibw" unit="bsaType"/>
	 */
	@Transactional
//	public void bsaDialogSave(PatientMtl ps, SchemaMtl docMtl){
//		PatientMtl patientMtl = docMtl.getPatientMtl();
	public void bsaDialogSave(PatientMtl docMtl){
		PatientMtl patientMtl = docMtl;
		log.debug("------------");
//		Calendar c = Calendar.getInstance();
		DateTime c = new DateTime();
//		Integer weight= ps.getWeightKg();
		Integer weight= patientMtl.getWeight();
		log.debug(weight);
		Tree weightValT = patientMtl.getFindingIValT("weight");
		Tree weightVarT = null == weightValT ? null : weightValT.getParentT();

		log.debug(weightVarT);
		
//		Calendar weightDC = ps.getWeightDC();
//		log.debug(weightDC.getTime());
//		log.debug(c.getTime());
		DateTime weightDC = null;
		if(weightVarT!=null)
			weightDC = patientMtl.getOfDateHistory(weightVarT);
		Tree patientT=patientMtl.getDocT();
//		if(weightVarT!=null && weightDC.get(Calendar.DATE)==c.get(Calendar.DATE)){
		if(weightVarT!=null && weightDC.dayOfYear().get()==c.dayOfYear().get()){
			Ivariable ivO = (Ivariable)ivCreator.setIvariable("finding")
			.setIvalue(weight).build();
			log.debug(ivO);
			//			Pvariable pvC = new Pvariable("finding",weight.toString(), "kg");
			//			pvC = sourcePv(pvC);
//			Tree weightValT=ps.getWeightValT();
			weightValT.setIdClass(ivO.getId());
			em.merge(weightValT);
		}else{
			hisVarVal(docMtl, patientT, getWeightC(), weight, "kg");
		}
//		Integer height = ps.getHeightCm();
		Integer height = patientMtl.getHeight();
//		Tree heightVarT = pas.getHeightVarT();
		Tree heightValT = patientMtl.getFindingIValT("height");
		Tree heightVarT = null == heightValT ? null : heightValT.getParentT();
//		Calendar heightDC = ps.getHeightDC();
//		log.debug(heightDC.getTime());
		DateTime heightDC = null;
		if(heightVarT!=null)
			heightDC = patientMtl.getOfDateHistory(heightVarT);
//		if(heightVarT!=null && heightDC.get(Calendar.WEEK_OF_YEAR)==c.get(Calendar.WEEK_OF_YEAR)){
		if(heightVarT!=null && heightDC.weekOfWeekyear().get()==c.weekOfWeekyear().get()){
			Ivariable ivO = (Ivariable)ivCreator.setIvariable("finding")
			.setIvalue(height).build();
//			Tree heightValT = ps.getHeightValT();
			heightValT.setIdClass(ivO.getId());
			em.merge(heightValT);
		}else{
			hisVarVal(docMtl, patientT, getHeightC(), height, "cm");
		}
//		String bsaType = ps.getBsaType();
		String bsaType = patientMtl.getBsaType();
		log.debug("bsaType="+bsaType);
//		Tree bsaTypeVarT = ps.getBsaTypeVarT();
		Tree bsaTypeValT = patientMtl.getFindingSValT("bsaType");
		Tree bsaTypeVarT = null == bsaTypeValT ? null : bsaTypeValT.getParentT();
		log.debug(bsaTypeVarT);
//		Calendar bsaTypetDC = ps.getBsaTypetDC();
//		log.debug(bsaTypetDC.getTime());
		DateTime bsaTypetDC = null;
		if(bsaTypeVarT!=null)
			bsaTypetDC = patientMtl.getOfDateHistory(bsaTypeVarT);
		Pvariable bsaTypeValO = (Pvariable) pvCreator
				.setPvariable("finding").setPvalue(bsaType).setUnit("bsaType").build();
		log.debug("bsaTypeValO="+bsaTypeValO);
		if(bsaTypeVarT!=null && bsaTypetDC.weekOfWeekyear().get()==c.weekOfWeekyear().get()){
//			Tree bsaTypeValT = ps.getBsaTypeValT();
			bsaTypeValT.setIdClass(bsaTypeValO.getId());
		}else{
//			hisVarVal(docMtl, patientT, getHeightC(), height, "cm");
			Finding bsaTypeVarO = new Finding("bsaType","bsaType");
			bsaTypeVarO = (Finding) findingCreator.setMtlO(bsaTypeVarO).build();
			bsaTypeVarT=addChild("finding", patientT);
//			Tree bsaTypeValT = addChild("pvariable", bsaTypeVarT);
			bsaTypeValT = addChild("pvariable", bsaTypeVarT);
			bsaTypeVarT.setIdClass(bsaTypeVarO.getId());
			bsaTypeValT.setIdClass(bsaTypeValO.getId());
			addOfDate(bsaTypeVarT);
		}
		owsSession.setDocMtl(null);
	}
	/*

	public void saveBsa(PatientSchema ps, SchemaMtl docMtl){
		PatientMtl patientMtl = docMtl.getPatientMtl();
		Tree patientT = patientMtl.getDocT();
		Tree weightVarT=null, heightVarT=null;
		Tree weightValT=null, heightValT=null;
//		Pvariable weightP=null, heightP=null;
//		Ivariable weightI=null, heightI=null;
		Calendar weightDC=null, heightDC=null;
		for(Tree t1:patientT.getChildTs()){
			if(PatientMtl.isFindingO(t1)){
				Tree ofDateT = null;
				for(Tree t2:t1.getChildTs()){
					if(PatientMtl.isIvariableO(t2)){
						Ivariable iv = PatientMtl.getIvariableO(t2);
						if("finding".equals(iv.getIvariable())){
							Finding varO = PatientMtl.getFindingO(t1);
							if(heightVarT==null&&"cm".equals(varO.getUnit())){
								heightVarT=t1;
								heightValT=t2;
//								heightI=iv;
							}else if(weightVarT==null&&"kg".equals(varO.getUnit())){
								weightVarT=t1;
								weightValT=t2;
//								weightI=iv;
							}
						}
//					}else if(patientMtl.isPvariableO(t2)){//for old ows only
//						Pvariable pv =	PatientMtl.getPvariableO(t2);
//						if("ofDate".equals(pv.getPvariable())){
//							ofDateT=t2;
//						}else if("finding".equals(pv.getPvariable())){
//							if(heightT==null&&"cm".equals(pv.getUnit())){
//								heightT=t1;
//								heightP=pv;
//							}else if(weightT==null&&"kg".equals(pv.getUnit())){
//								weightT=t1;
//								weightValT=t2;
//								weightP=pv;
//							}
//						}
					}
				}
				if(ofDateT.getParentT()==heightVarT && heightDC==null){
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(ofDateT.getHistory().getMdate().getTime());
					heightDC=c;
				}else if(ofDateT.getParentT()==weightVarT && weightDC==null){
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(ofDateT.getHistory().getMdate().getTime());
					weightDC=c;
				}
			}
			if(null!=weightVarT&&null!=heightVarT)
				continue;
		}
		Calendar c = Calendar.getInstance();
//		Integer weight= ps.getWeightKg();
		Integer weight= patientMtl.getWeight();
		log.debug(weight);
		log.debug(weightVarT);
		log.debug(weightDC);
		log.debug(c.getTime());
		if(weightVarT!=null && weightDC.get(Calendar.DATE)==c.get(Calendar.DATE)){
			Ivariable ivO = (Ivariable)ivCreator.setIvariable("finding")
			.setIvalue(weight).build();
			log.debug(ivO);
//			Pvariable pvC = new Pvariable("finding",weight.toString(), "kg");
//			pvC = sourcePv(pvC);
			weightValT.setIdClass(ivO.getId());
			em.merge(weightValT);
		}else{
			hisVarVal(docMtl, patientT, getWeightC(), weight, "kg");
		}
//		Integer height = ps.getHeightCm();
		Integer height = patientMtl.getHeight();
		if(heightVarT!=null && heightDC.get(Calendar.WEEK_OF_YEAR)==c.get(Calendar.WEEK_OF_YEAR)){
			Pvariable pvC = new Pvariable("finding",height.toString(), "cm");
			pvC = sourcePv(pvC);
			heightValT.setIdClass(pvC.getId());
			em.merge(heightValT);
		}else{
			hisVarVal(docMtl, patientT, getHeightC(), height, "cm");
		}
		owsSession.setDocMtl(null);
	}
	 * */
	
	private Finding		weightC, heightC;
	public Finding getWeightC() {
		if(null==weightC)
		weightC = getFinding("weight");
		return weightC;
	}
	public Finding getHeightC() {
		if(null==heightC)
		heightC = getFinding("height");
		return heightC;
	}
	public Finding getFinding(String finding) {
		String sql = "SELECT f FROM Finding f WHERE f.finding=:finding";
		List<Finding> dL = em.createQuery(sql)
		.setParameter("finding",	finding)
		.getResultList();
		Finding findingC=null;
		if(dL.size()>0)
		{
			findingC=dL.get(0);
		}
		return findingC;
	}
	
	private void hisVarVal(SchemaMtl schemaMtl, Tree patientT, Finding fC, String pvalue, String unit) {
		
	}
	private void hisVarVal(SchemaMtl schemaMtl, Tree patientT, Finding fC, Integer ivalue, String unit) {
		Tree hisVarT = //schemaMtl.addChild(patientT, "finding", patientT, nextDbid());
		addChild("finding", patientT);
		log.debug(hisVarT);
		hisVarT.setIdClass(fC.getId());
		addOfDate(hisVarT);
		/*
//		Pvariable pvWeightC = getPvariable("finding",pvalue,unit);
//		Pvariable pvWeightC = (Pvariable)pvCreator.setPvariable("finding").setPvalue(pvalue).setUnit(unit).build();
		if(pvWeightC==null){
			pvWeightC=new Pvariable("finding",pvalue,unit);
//			persistMtlObject(schemaMtl, pvWeightC);
			persistMtlObject(pvWeightC);
		}
		Tree hisValT = schemaMtl.addChild(hisVarT, "pvariable", patientT, nextDbid());
		 */
		Tree hisValT = addChild("ivariable", hisVarT);
		Ivariable ivWeightO = (Ivariable)ivCreator
		.setIvariable("finding").setIvalue(ivalue).build();
		hisValT.setIdClass(ivWeightO.getId());
		/*
		em.persist(hisVarT);
		 * */
		em.merge(patientT);
	}
	private Pvariable sourcePv(Pvariable pvC) {
		MObject dbObjectM=seekObjM(pvC);
		if(dbObjectM==null){
//				persistMtlObject(schemaMtl, pvC);
			persistMtlObject(pvC);
		}else{
			pvC=(Pvariable) dbObjectM;
		}
		return pvC;
	}
	
	// bsa END
	// Tablet divisor BEGIN
	public void tabletDivisorEntry(PatientSchema ps, SchemaMtl docMtl){
		Tree editNodeT = docMtl.getEditNodeT();
		log.debug(editNodeT);
//		if(editNodeT.getMtlO() instanceof Drug)
		if(docMtl.isDrugO(editNodeT)){
			if(null==ps.getDrugTabletL()){
				List<Tablet> drugTabletL = new ArrayList<Tablet>();
				Iterator iteratePointers = JXPathContext.newContext(editNodeT).iteratePointers("childTs/childTs[tabName='tablet']");
				while (iteratePointers.hasNext()) {
					Tree tabletT = (Tree) ((Pointer) iteratePointers.next()).getValue();
					Tablet tabletO = docMtl.getTabletO(tabletT);
					drugTabletL.add(tabletO);
				}
				if(drugTabletL.size()==0){
					Drug drugO = docMtl.getDrugO(editNodeT);
					Integer idGeneric = drugO.getGeneric().getId();
					String sql = "SELECT tablet.* " +
					" FROM tree t1,tablet " +
					" WHERE t1.idclass=idtablet AND t1.did="+idGeneric+
					" ORDER BY value DESC"
					;
					drugTabletL = em.createNativeQuery(sql, Tablet.class).getResultList();
				}
				ps.setDrugTabletL(drugTabletL);
				log.debug(drugTabletL);

			}
			dose2tablet(ps,docMtl);
		}
	}
	private void dose2tablet(PatientSchema ps, SchemaMtl docMtl) {
		Tree doseT = JXPathBean.getDose(docMtl.getEditNodeT());
		Float calcDose = docMtl.getCalcDose(doseT);
		log.debug("calcDose ="+calcDose);
		Integer restCalcDose = calcDose.intValue();
		int tsn=0;
		for(Tablet tablet:ps.getDrugTabletL()){
			if(0==restCalcDose)
				break;
			log.debug(tablet);
			log.debug("restCalcDose ="+restCalcDose);
			Integer value = tablet.getValue().intValue();
			log.debug("value ="+value);
			if(0==value)
				continue;
			int tXq = restCalcDose/value;
			log.debug(restCalcDose+"/"+value+"="+tXq);
			restCalcDose= restCalcDose%value;
			log.debug(restCalcDose);
			if(0==tsn){
				ps.setT0quantity(tXq);
			}else if(1==tsn){
				ps.setT1quantity(tXq);
			}else if(2==tsn){
				ps.setT2quantity(tXq);
			}
			tsn++;
		}
		ps.setTsn(tsn);
		ps.setTabletGabeDiv(1);
		log.debug("ps.getTsn() ="+ps.getTsn());
	}
	public void calculationTablet(PatientSchema ps, SchemaMtl docMtl){
		log.debug(1);
		Tree doseT = JXPathBean.getDose(docMtl.getEditNodeT());
		Ivariable tabletGabeDivO = (Ivariable)ivCreator.setIvariable("tabletGabeDiv").setIvalue(ps.getTabletGabeDiv()).build();
		Tree tabletGabeDivT= addMtlNode(docMtl, doseT, "ivariable", tabletGabeDivO);
		Integer tXquantity=0,tXdquantity=0;
		for(int tsn=0;tsn<ps.getDrugTabletL().size();tsn++){
			log.debug(tsn);
			Tablet tabletO=ps.getDrugTabletL().get(tsn);
			log.debug(tabletO);
			if(0==tsn){
				tXquantity = ps.getT0quantity();
				tXdquantity = ps.getT0dquantity();
			}else if(1==tsn){
				tXquantity = ps.getT1quantity();
				tXdquantity = ps.getT1dquantity();
			}else if(2==tsn){
				tXquantity = ps.getT2quantity();
				tXdquantity = ps.getT2dquantity();
			}
			log.debug(tXquantity);
			log.debug(tXdquantity);
			Tree tabletT= addMtlNode(docMtl, tabletGabeDivT, "tablet", tabletO);
			if(tXquantity>0){
				Ivariable tQuantityO = (Ivariable)ivCreator.setIvariable("tQuantity").setIvalue(tXquantity).build();
				addMtlNode(docMtl, tabletT, "ivariable", tQuantityO);
			}
			if(tXdquantity>0){
				Ivariable tdQuantityO = (Ivariable)ivCreator.setIvariable("tdQuantity").setIvalue(tXquantity).build();
				addMtlNode(docMtl, tabletT, "ivariable", tdQuantityO);
			}
		}
		int tsn=0;
		for(Tablet tablet:ps.getDrugTabletL()){
			tsn++;
		}
	}
	public void addDelTablet(PatientSchema ps, SchemaMtl docMtl){
		String idtParameter = owsSession.getRequest().getParameter("idt");
		log.debug(idtParameter);
	}
	@Transactional
	public void schemaTabletDivisorSave(PatientSchema ps, SchemaMtl docMtl){
		log.debug(1);
	}
	// Tablet divisor END

	// Chemo dose modification BEGIN
	public void chemoDoseModEditStep(PatientSchema ps, SchemaMtl docMtl){
		docMtl.setIdt(ps.getIdt());
		chemoDoseModEntry(ps, docMtl);
	}
	/**
	 * 
	 * @param ps
	 * @param docMtl
	 */
	public void chemoDoseModEntry(PatientSchema ps, SchemaMtl docMtl){
		docMtl.setEditNodeT();
		log.debug("docMtl.getEditNodeT()= "+docMtl.getEditNodeT());
		Tree editNodeT = docMtl.getEditNodeT();
		Tree editTherapyChangeT = getEditTherapyChangeFirst(docMtl);
		log.debug(editTherapyChangeT);
		if(null!=editTherapyChangeT){
			log.debug(editTherapyChangeT);
			Tree noticeT = editTherapyChangeT.getParentT();
			log.debug(noticeT);
			if(docMtl.isNoticeType(noticeT, "therapyChange")) 
			{
				Notice noticeO = docMtl.getNoticeO(noticeT);
				ps.setNotice(noticeO.getNotice());
		}	}
		Float defDose100 = docMtl.getDoseO(editNodeT).getValue();
		Float defDose = defDose100;
		String cd100 = docMtl.getCalcDoseR().get(editNodeT);
		log.debug("cd100="+cd100);
		Float calcDose100 = Float.parseFloat(cd100);
		Float calcDose = calcDose100;
		int doseProc = 100;
		log.debug(1);
		log.debug(editTherapyChangeT);
		if(docMtl.isIvariableIvariable(editTherapyChangeT, "procent")){
			log.debug(2);
			ps.setDoseModType("proc");
			doseProc = getDoseProcent(docMtl, editTherapyChangeT);
			defDose = defDose*doseProc/100;
			calcDose = calcDose*doseProc/100;
		}else if(docMtl.isDoseType(editTherapyChangeT,"p")){
			log.debug(3);
			ps.setDoseModType("def");
			defDose = docMtl.getDoseO(editTherapyChangeT).getValue();
			doseProc = (int) (defDose*100/defDose100);
			calcDose = calcDose100*doseProc/100;
		}else if(docMtl.isDoseType(editTherapyChangeT,"pc")){
			log.debug(4);
			ps.setDoseModType("calc");
			calcDose = docMtl.getDoseO(editTherapyChangeT).getValue();
			doseProc = (int) (calcDose*100/calcDose100);
			defDose = defDose*doseProc/100;
		}
		log.debug(5);
		ps.setDoseDef100(defDose100.intValue());
		log.debug("defDose="+defDose);
		ps.setDoseDeff(defDose);
//		ps.setDoseDef(defDose.intValue());
		ps.setDoseProc(doseProc);
		ps.setDoseCalc100(calcDose100.intValue());
		ps.setDoseCalcf(calcDose);
		log.debug("calcDose="+calcDose);
//		ps.setDoseCalc(calcDose.intValue());
		Integer drugTsNr = docMtl.getModDoseTsTaskTsNr();
		log.debug("drugTsNr="+drugTsNr);
		if(null==drugTsNr)
			ps.setAllDay(true);
		else
			ps.setTsNr(drugTsNr);
	}
	/**
	 * patient/task[@ref]/{notice|expr}/{dose|ivariable}[taskTsNr or not taskTsNr]
	 * @param docMtl
	 * @return
	 */
	private Tree getEditTherapyChangeFirst(SchemaMtl docMtl) {
		Integer taskTsNr = docMtl.getModDoseTsTaskTsNr();
		log.debug("taskTsNr="+taskTsNr);
		log.debug("docMtl.getEditNodeT()="+docMtl.getEditNodeT());
		Tree doseChangeT=null;
		for(Tree notice2doseChangeT:docMtl.getPschemaT().getChildTs())
			if(isNoticeTherapyChange(docMtl, notice2doseChangeT))
				for(Tree procentOrDoseT:notice2doseChangeT.getChildTs()){
					log.debug(procentOrDoseT.getRef());
					if(docMtl.getEditNodeT().getId().equals(procentOrDoseT.getRef())){
						log.debug(taskTsNr);
						doseChangeT=procentOrDoseT;
						if(null==taskTsNr)
							return procentOrDoseT;
						else
							if(procentOrDoseT.hasChild())
								for(Tree t3:procentOrDoseT.getChildTs())
									if(docMtl.isIvariableIvariable(t3, "taskTsNr")
									&& taskTsNr>=docMtl.getIvariableO(t3).getIvalue()
//									&& taskTsNr.equals(docMtl.getIvariableO(t3).getIvalue())
									)
										return procentOrDoseT;
					}
				}
		return doseChangeT;
	}
	private int getDoseProcent(SchemaMtl docMtl, Tree tcT) {
		int doseProc = 100;
		log.debug(tcT);
		if(docMtl.isIvariableO(tcT))
			doseProc=docMtl.getIvariableO(tcT).getIvalue();
		return doseProc;
	}

	public void cmDayDelayEntry(PatientSchema ps, SchemaMtl docMtl){
		log.debug("--------------------------");
		log.debug("--------------------------"+docMtl.getIdc());
		DateTime dayDateTime = getDayDateTime(ps, docMtl);
		log.debug("--------------------------"+dayDateTime);
		ps.setBdate(dayDateTime.toDate());
		log.debug("--------------------------"+ps.getBdate());
	}
	private DateTime getDayDateTime(PatientSchema ps, SchemaMtl docMtl) {
		Integer day = docMtl.getIdt();
		log.debug("day="+day);
		ps.setDelayedDay(day);
		DateTime beginCalendar = docMtl.getBeginCalendar();
		log.debug("--------------------------"+beginCalendar);
		if(day<0){
			beginCalendar=beginCalendar.plusDays(day);
		}else if(day>0){
			beginCalendar=beginCalendar.plusDays(day-1);
		}
		return beginCalendar;
	}

	@Transactional
	public void cmDayDelaySave(PatientSchema ps, SchemaMtl docMtl){
		log.debug("--------------------------");
		DateTime oldDayDateTime = getDayDateTime(ps, docMtl);
		DateTime newDayDateTime = new DateTime(ps.getBdate().getTime());
		int dayDelay = newDayDateTime.getDayOfYear()-oldDayDateTime.getDayOfYear();
		log.debug("--------------------------"+oldDayDateTime);
		log.debug("--------------------------"+newDayDateTime);
		log.debug("--------------------------"+dayDelay);
		if(dayDelay<0) return;
		log.debug("--------------------------");
		Notice delayedDayNoticeO=null ;
		int delayedDay = ps.getDelayedDay();
		for (Tree delayedDayT : docMtl.getPschemaT().getChildTs()) 
			if(docMtl.isIvariableO(delayedDayT)) {
				Ivariable delayedDayO = docMtl.getIvariableO(delayedDayT);
				if((delayedDay==delayedDayO.getIvalue()) 
				&& "delayedDay".equals(delayedDayO.getIvariable())
				){
					deleteNative(delayedDayT);
					for (Tree delayedDayChildT : delayedDayT.getChildTs())
						if(docMtl.isNoticeO(delayedDayChildT)) {
							delayedDayNoticeO = docMtl.getNoticeO(delayedDayChildT);
						}
				}
			}
		if(dayDelay==0) return;
		Ivariable delayedDayO	= (Ivariable) ivCreator.setIvariable("delayedDay").setIvalue(delayedDay).build();
		Ivariable dayDelayO		= (Ivariable) ivCreator.setIvariable("dayDelay").setIvalue(dayDelay).build();
		log.debug("--------------------------");
		Tree delayedDayT = addChild("ivariable", docMtl.getPschemaT(), docMtl.getPschemaT().getDocT());
		log.debug("--------------------------"+delayedDayT);
		delayedDayT.setSort(getHistorySort());
		delayedDayT.setIdClass(delayedDayO.getId());
		Tree dayDelayT = addChild("ivariable", delayedDayT, docMtl.getPschemaT().getDocT());
		dayDelayT.setIdClass(dayDelayO.getId());
		log.debug("--------------------------"+delayedDayT);
		Tree delayedDayNoticeT = addChild("notice", delayedDayT, docMtl.getPschemaT().getDocT());
		if(null==delayedDayNoticeO){
			delayedDayNoticeO = (Notice) noticeCreator.setMtlO(new Notice(ps.getNotice(),"")).build();
		}
		delayedDayNoticeT.setIdClass(delayedDayNoticeO.getId());
//		em.merge(delayedDayT);
	}
	
//	@Transactional
	public void chemoDoseModSave(PatientSchema ps, SchemaMtl docMtl){
		log.debug(ps.getDoseModType());
		Tree editNodeT = docMtl.getEditNodeT();
		Float defDoseValue = docMtl.getDoseO(editNodeT).getValue();
		Float calcDose = docMtl.getCalcDose().get(editNodeT);
		Tree editTherapyChange = checkTherapyChange(docMtl,ps);
		int doseProc = getDoseProcent(docMtl, editTherapyChange);
		Float doseCalcf = ps.getDoseCalcf();
//		Integer doseCalc = ps.getDoseCalc();
		Float doseDef ;
		if(null!=ps.getDoseDef())
			doseDef = new Float(ps.getDoseDef());
		else
			doseDef = new Float(ps.getDoseDeff());
		log.debug(doseCalcf+"!="+calcDose.intValue()
		+"||"+doseDef+"!="+defDoseValue.intValue()
		+"||"+ps.getDoseProc()+"!="+doseProc);
		log.debug(doseCalcf!=calcDose.intValue()
				||defDoseValue.intValue()!=doseDef||ps.getDoseProc()!=doseProc);
		if(doseCalcf!=calcDose.intValue()
		||doseDef!=defDoseValue.intValue()
		||ps.getDoseProc()!=doseProc
		){
			String doseModType = ps.getDoseModType();
			log.debug(doseModType);
			Tree doseModT=null;
			if("def".equals(doseModType)){
				doseModT=addDoseDef(ps, docMtl, editTherapyChange);
			}else if("proc".equals(doseModType)){
				doseModT=addDoseProc(ps, docMtl, editTherapyChange);
			}else if("calc".equals(doseModType)){
				doseModT=addDoseCalc(ps, docMtl, editTherapyChange);
			}
			addHistory(doseModT);
			addTaskTsNr(docMtl, doseModT);
			deleteOldDoseChange(docMtl,ps,doseModT,editTherapyChange);
		}
		docMtl.setIdt(null);
		docMtl.editStep();
	}
	private void deleteOldDoseChange(SchemaMtl docMtl, PatientSchema ps, Tree doseModT, Tree editTherapyChange){
		Tree editNodeT = docMtl.getEditNodeT();
		for(Tree t:editTherapyChange.getChildTs())
			if(editNodeT.getId().equals(t.getRef())&&t!=doseModT)
			{
				if(ps.isAllDay())
					deleteService.deleteTree(t.getId());
				else{
					Integer tsNr = ps.getTsNr();
					for(Tree t2:t.getChildTs())
						if(docMtl.isIvariableIvariable(t2,"taskTsNr")&&docMtl.getIvariableO(t2).getIvalue().equals(tsNr))
						{
							deleteService.deleteTree(t.getId());
							continue;
						}
				}
			}
	}
	private Tree addDoseCalc(PatientSchema ps, SchemaMtl docMtl, Tree editTherapyChange) {
		Dose doseOldDefO = docMtl.getDoseO(docMtl.getEditNodeT());
		Dose doseNewDefO = new Dose();
		doseNewDefO.setValue(ps.getDoseCalc());
		doseNewDefO.setType("pc");
		if(doseOldDefO.getUnit().indexOf("/")>0){
			String calcUnit = doseOldDefO.getUnit().split("/")[0];
			doseNewDefO.setUnit(calcUnit);
		}
		doseNewDefO=(Dose) doseCreator.setMtlO(doseNewDefO).build();
		Tree doseDefModT = addDoseModT(docMtl, editTherapyChange, "dose",doseNewDefO);
		return doseDefModT;
	}
	private Tree addDoseDef(PatientSchema ps, SchemaMtl docMtl, Tree editTherapyChange) {
		Dose doseOldDefO = docMtl.getDoseO(docMtl.getEditNodeT());
		Dose doseNewDefO = new Dose(doseOldDefO.getValue(),doseOldDefO.getUnit(),doseOldDefO.getApp(),doseOldDefO.getPro(),"p");
		log.debug(ps.getDoseDef());
		log.debug(ps.getDoseDeff());
		if(null!=ps.getDoseDef())
			doseNewDefO.setValue(ps.getDoseDef());
		else
			doseNewDefO.setValue(ps.getDoseDeff());
		doseNewDefO=(Dose) doseCreator.setMtlO(doseNewDefO).build();
		Tree doseDefModT = addDoseModT(docMtl, editTherapyChange, "dose",doseNewDefO);
		return doseDefModT;
	}
	private Tree addDoseProc(PatientSchema ps, SchemaMtl docMtl, Tree editTherapyChange) {
		Ivariable pDoseProcentO = getDoseProcentO(ps);
		Tree pDoseProcentT = addDoseModT(docMtl, editTherapyChange, "ivariable",pDoseProcentO);
		return pDoseProcentT;
	}
	private Tree addDoseModT(SchemaMtl docMtl, Tree parentT, String tagName,MObject pDoseModO) {
		Tree pDoseModT = addMtlNode(docMtl, parentT, tagName, pDoseModO);
		pDoseModT.setRef(docMtl.getEditNodeT().getId());
		return pDoseModT;
	}
	private void addTaskTsNr(SchemaMtl docMtl, Tree pDoseProcentT) {
		Integer drugTsNr = docMtl.getModDoseTsTaskTsNr();
//		log.debug(drugTsNr);
		if(null!=drugTsNr){
			Ivariable taskTsNrO = (Ivariable)ivCreator
			.setIvariable("taskTsNr").setIvalue(drugTsNr).build();
			Tree taskTsNrT = addMtlNode(docMtl, pDoseProcentT, "ivariable", taskTsNrO);
//			log.debug(taskTsNrT);
			Ts modDoseTs = docMtl.getModDoseTs();
//			log.debug(modDoseTs);
			Integer idTimesOfTs = modDoseTs.getTimesT().getId();
			taskTsNrT.setRef(idTimesOfTs);
		}
	}
	private Ivariable getDoseProcentO(PatientSchema ps) {
		Ivariable pDoseProcentO = (Ivariable)ivCreator
		.setIvariable("procent").setIvalue(ps.getDoseProc()).build();
		return pDoseProcentO;
	}
	public void chemoDoseModExit(PatientSchema ps, SchemaMtl docMtl){
		log.debug("-----------");
		docMtl.setAction(ps.getAction());
		docMtl.setTsNr(ps.getTsNr());
		log.debug("-----------");
	}
	/**
	 * Find or create therapy change notice.
	 * @param docMtl
	 * @param ps
	 * @return Notice therapy change tree object.
	 */
	private Tree checkTherapyChange(SchemaMtl docMtl, PatientSchema ps) {
		Tree pSchemaT=docMtl.getPschemaT();
		Tree therapyChangeT = getTherapyChange(pSchemaT, docMtl);
		if(null==therapyChangeT){
			therapyChangeT=addChild("notice", pSchemaT);
			addHistory(therapyChangeT);
		}
		log.debug("-----------" + ps.getNotice());
		Notice noticeO = (Notice) noticeCreator.setMtlO(new Notice(ps.getNotice(), "therapyChange")).build();
		log.debug("-----------" + noticeO);
		docMtl.addMObject(noticeO, therapyChangeT);
		log.debug("-----------" + therapyChangeT);
		return therapyChangeT;
	}
	private Tree getTherapyChange(Tree pSchemaT, SchemaMtl docMtl) {
		for(Tree t:pSchemaT.getChildTs())
			if(isNoticeTherapyChange(docMtl, t))
				return t;
		return null;
	}
	private boolean isNoticeTherapyChange(SchemaMtl docMtl, Tree t) {return docMtl.isNoticeType(t,"therapyChange");}
	// Chemo dose modification END


	// Schema rule BEGIN
	@Transactional
	public void schemaExprUseSave(PatientSchema ps, SchemaMtl docMtl){
		Tree editNodeT = docMtl.getEditNodeT();
		log.debug("------------------"+editNodeT);
		Tree pSchemaT = docMtl.getPschemaT();
		log.debug("------------------"+pSchemaT);
		
		Tree modDrugT=null,drugT = docMtl.getParentTaskT(editNodeT);
		log.debug("------------------"+drugT);
		for(Tree t:pSchemaT.getChildTs())
			if(drugT.getId().equals(t.getRef()))
				modDrugT=t;
		log.debug("-------------modDrugT-----"+modDrugT);
		if(null==modDrugT){
			modDrugT=addChild(drugT.getTabName(), pSchemaT);
			modDrugT.setIdClass(drugT.getIdClass());
			modDrugT.setRef(drugT.getId());
		}
		deleteOldRuleThenElse(docMtl);
		
		Tree patientThenElseExprT=addChild(editNodeT.getTabName(), modDrugT);
		addHistory(patientThenElseExprT);
		patientThenElseExprT.setRef(editNodeT.getId());
		patientThenElseExprT.setIdClass(editNodeT.getIdClass());
		
		for(Tree t:editNodeT.getChildTs()){
			Tree patientThenElseElT=addChild(t.getTabName(), patientThenElseExprT, pSchemaT.getDocT());
			patientThenElseElT.setIdClass(t.getIdClass());
		}
		addTaskTsNr(docMtl, patientThenElseExprT);
	}

	private void deleteOldRuleThenElse(SchemaMtl docMtl) {
		Tree editNodeT = docMtl.getEditNodeT();
		Tree pSchemaT = docMtl.getPschemaT();
		for(Tree t:pSchemaT.getChildTs())
			if(editNodeT.getId().equals(t.getRef()))
				deleteService.deleteTree(t.getId());
			else
				if(t.hasChild())
					for(Tree t1:t.getChildTs())
						if(editNodeT.getId().equals(t1.getRef()))
							deleteService.deleteTree(t1.getId());
	}
	// Schema rule END

	// New schema cycle BEGIN
	public void newCycleEntry(PatientSchema ps, SchemaMtl docMtl) {
		log.debug("------------------");
		PatientMtl patientMtl = docMtl.getPatientMtl();
		int cycleNr = docMtl.getCycleNr();
		ps.setCycleNr(cycleNr);
	}
	public void newCycleSave(PatientSchema ps, SchemaMtl docMtl) {
		
	}
	private void initVariable() {
		getProcent100();
		getOfDateFixed();
	}
	private Integer correcturUseVarianteSchema(SchemaMtl docMtl, Tree docProtocolT) {
		Integer idClassTask = docMtl.getDocT().getIdClass();
		Integer idProtocol = docProtocolT.getId();
		String sql = " SELECT * FROM tree where iddoc=? and idclass=? ";
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, idProtocol, idClassTask);
		if(queryForList.size()>1){
			return idClassTask;
		}
		sql=" SELECT * FROM ( " +
		" SELECT idclass,count(idclass) AS cnt FROM tree t1,task k1 " +
		" WHERE t1.iddoc=? AND t1.idclass=k1.idtask AND k1.cyclename IN " +
		" (SELECT cyclename FROM tree t1,task k1 where k1.idtask=t1.id AND  t1.id=?) " +
		" GROUP BY t1.idclass " +
		" ORDER BY cnt DESC) t1 " +
		" WHERE t1.cnt>1 ";
		queryForList = simpleJdbc.queryForList(sql, idProtocol, idClassTask);
		Integer idClass=null;
		for (Map<String, Object> map : queryForList) {
			idClass=(Integer) map.get("idclass");
			break;
		}
		return idClass;
	}
	
	private Integer sqlLastCycleNrAndBeginDatum(PatientSchema ps, SchemaMtl docMtl) {
		Integer idClassTask = docMtl.getDocT().getMtlO().getId();
		Patient newPatientC = docMtl.getEditPatientC();
		Integer idt = docMtl.getIdt();
		log.debug(idt);
		Tree newSchemaCycleT = em.find(Tree.class, idt);
		log.debug(newSchemaCycleT);
		if(docMtl.getDocT().getDocT().getId().equals(newSchemaCycleT.getDocT().getId()))
		{
			log.debug(newSchemaCycleT);
		}
		Patient patientO = owsSession.getPatientO();
		log.debug(patientO);
		log.debug(newPatientC);
		if((null==newPatientC||null==newPatientC.getId())&&idt.equals(patientO.getId()))
			newPatientC=patientO;
		Integer lastCycleNr=0;
		String sql = 
		" SELECT p1.*,mdate,duration,t1.ref " +
		" FROM tree t1,task,tree t2,pvariable p1,tree t3,pvariable p2,history" +
		" WHERE t1.did=? AND t1.idclass=? AND t1.id=t2.did " +
		" AND t2.idclass=p1.idpvariable AND p1.pvariable='cycle'" +
		" AND t1.id=t3.did AND t3.idclass=p2.idpvariable AND p2.pvariable='ofDate'" +
		" AND t3.id=idhistory AND t1.idclass=idtask" +
		" ORDER BY t1.sort";
		log.debug(sql+" -- "+newPatientC.getId()+" "+idClassTask);
		List<Map<String, Object>> patientSchemaList = simpleJdbc
				.queryForList(sql, newPatientC.getId(),idClassTask);
		if(patientSchemaList.size()>0){
			Map<String, Object> patientSchemaMap = patientSchemaList.get(0);
			String lastCycleNrS = (String) patientSchemaMap.get("pvalue");
			log.debug(lastCycleNrS);
			lastCycleNr = Integer.parseInt(lastCycleNrS);
			Timestamp beginDateTimestamp = (Timestamp) patientSchemaMap.get("mdate");
			log.debug(beginDateTimestamp);
			Integer duration = (Integer) patientSchemaMap.get("duration");
			log.debug(duration);
			DateTime beginDateTime = new DateTime(beginDateTimestamp.getTime()).plusDays(duration);
			log.debug(docMtl.getPschemaT());
			int delay = docMtl.getDelayDays(new JXPathBean());
			beginDateTime=beginDateTime.plusDays(delay);
			Date bdate = beginDateTime.toDate();
			ps.setBdate(bdate);
			Integer ref = (Integer) patientSchemaMap.get("ref");
			log.debug(ref);
			ps.setCurrCycleId(ref);
			log.debug(ps.getCurrCycleId());
			ps.setCurrCycleOfDate(beginDateTimestamp);
		}else{
			sql = " SELECT mdate, duration, delayDays AS durationDelayDays " +
			" FROM tree t1 " +
			" LEFT JOIN (SELECT pSchemaT.id, SUM(dayDelayO.ivalue) AS delayDays " +
			" FROM tree pSchemaT, tree delayedDayT, tree dayDelayT,ivariable dayDelayO " +
			" WHERE pSchemaT.id=delayedDayT.did AND delayedDayT.id=dayDelayT.did AND dayDelayT.idClass=dayDelayO.idivariable" +
			" GROUP BY pSchemaT.id) AS schemaDelayDays ON schemaDelayDays.id=t1.id " +
			",task,tree t2,pvariable p1,tree t3,pvariable p2,history " +
			" WHERE t1.did=? AND t1.id=t2.did AND t2.idclass=p1.idpvariable AND p1.pvariable='cycle' " +
			" AND t1.id=t3.did AND t3.idclass=p2.idpvariable AND p2.pvariable='ofDate' " +
			" AND t3.id=idhistory AND t1.idclass=idtask ORDER BY t1.sort";
			log.debug(sql+" -- "+newPatientC.getId());
			patientSchemaList = simpleJdbc
					.queryForList(sql, newPatientC.getId());
			if(patientSchemaList.size()>0){
				Map<String, Object> patientSchemaMap = patientSchemaList.get(0);
				Timestamp beginDateTimestamp = (Timestamp) patientSchemaMap.get("mdate");
				log.debug(beginDateTimestamp);
				Integer duration = (Integer) patientSchemaMap.get("duration");
				Long delayDays = (Long) patientSchemaMap.get("delayDays");
				log.debug(delayDays);
				int ddd = null==delayDays?0:delayDays.intValue();
				DateTime beginDateTime = new DateTime(beginDateTimestamp.getTime()).plusDays(duration+ddd);
				Date bdate = beginDateTime.toDate();
				ps.setBdate(bdate);
			}
		}
		return lastCycleNr;
	}
	@Transactional
	public void changeBeginDateCycleNr(PatientSchema ps, SchemaMtl docMtl){
		Date bdate = ps.getBdate();
		Tree pschemaT = docMtl.getPschemaT(), psOfdateT = null, cycleNrT = null;
		for(Tree t:pschemaT.getChildTs())
			if(docMtl.isPvO(t)&&"ofDate".equals(docMtl.getPvariableO(t).getPvariable()))
				psOfdateT = t;
			else if(docMtl.isPvO(t)&&"cycle".equals(docMtl.getPvariableO(t).getPvariable()))
				cycleNrT = t;
		int cycleNr = ps.getCycleNr();
		Pvariable 
		cycleNrO=new Pvariable("cycle", ""+cycleNr, docMtl.getPvariableO(cycleNrT).getUnit());
		cycleNrO = (Pvariable) pvCreator.setPv(cycleNrO).build();
		cycleNrT.setIdClass(cycleNrO.getId());
		History history = psOfdateT.getHistory();
		history.setMdate(bdate);
		em.merge(history);
		em.merge(cycleNrT);
	}
	public void beginDateCycleNrEntry(PatientSchema ps, SchemaMtl docMtl){
		Integer lastCycleNr = sqlLastCycleNrAndBeginDatum(ps,docMtl);
		ps.setCycleNr(lastCycleNr);
		Integer duration = docMtl.getTaskO(docMtl.getDocT()).getDuration();
		DateTime bdateTime = new DateTime(ps.getBdate().getTime()).plusDays(-duration);
		Date bdate = bdateTime.toDate();
		ps.setBdate(bdate);
	}
	//schemaBeginDate BEGIN
	public void schemaBeginDateEntry(PatientSchema ps, SchemaMtl docMtl){
		Integer cycleNr;
		if(null!=docMtl.getPatientMtl()){
			docMtl.setEditPatientC();
		}else{
			docMtl.editStepPatient();
		}
		Integer lastCycleNr = sqlLastCycleNrAndBeginDatum(ps,docMtl);
		cycleNr=lastCycleNr+1;
		ps.setCycleNr(cycleNr);
	}
	private String sqlFreeTask=
	"\n SELECT conceptSchemaSiblingT.id" +
	"\n FROM tree patientSchemaT, tree conceptSchemaT, " +
	" tree conceptSchemaSiblingT LEFT JOIN tree patientSchemaSiblingT " +
	"\n ON conceptSchemaSiblingT.id=patientSchemaSiblingT.ref " +
	"\n WHERE patientSchemaT.id=? " +
	" AND conceptSchemaT.id=patientSchemaT.ref " +
	" AND conceptSchemaSiblingT.did=conceptSchemaT.did" +
	" AND conceptSchemaSiblingT.tab_name='task'" +
	" AND patientSchemaSiblingT.id is null" +
	"\n ORDER BY conceptSchemaSiblingT.sort" +
	"\n";
	@Transactional
	public void saveSchema2Patient(PatientSchema ps,SchemaMtl docMtl){
		Timestamp bdate = new Timestamp(ps.getBdate().getTime());
		docMtl.setBdate(bdate);
		docMtl.setCycleNr(ps.getCycleNr());
		Integer lastCycleNr = sqlLastCycleNrAndBeginDatum(ps, docMtl);
		if(lastCycleNr>0){
			Patient newPatientC = docMtl.getEditPatientC();
			Tree patientT= em.find(Tree.class, newPatientC.getId());
//			Integer id = docMtl.getDocT().getId(), pTaskId = 0, idTaskFromConceptFree = 0;
			Integer id = ps.getCurrCycleId(), pTaskId = 0, idTaskFromConceptFree = 0;
			List<Tree> resultList = em.createQuery("FROM Tree t WHERE t.ref=:ref")
				.setParameter("ref", id).getResultList();
			if(resultList.size()>0)
				pTaskId=resultList.get(0).getId();
			List<Map<String, Object>> queryForList = simpleJdbc
				.queryForList(sqlFreeTask, pTaskId);
			if(queryForList.size()>0)
				idTaskFromConceptFree = (Integer) queryForList.get(0).get("id");
//			Tree schemaDocT = docMtl.getDocT();
			if(0 == idTaskFromConceptFree){
//				Tree taskFromConceptT = schemaDocT;
				Tree taskFromConceptT = em.find(Tree.class, id);
				Tree parentT = taskFromConceptT.getParentT();
				Tree newTask = addChild("task", parentT,taskFromConceptT.getDocT());
				newTask.setIdClass(taskFromConceptT.getIdClass());
				initPatientSchema(docMtl, patientT, newTask);
			}else{
				Tree startSchemaT= em.find(Tree.class, idTaskFromConceptFree);
				initPatientSchema(docMtl, patientT, startSchemaT);
			}
		}else{
			docMtl.setBdate(bdate);
			docMtl.setCycleNr(ps.getCycleNr());
			saveSchema2Patient2(docMtl);
		}
	}
	//schemaBeginDate END
	public void saveSchema2Patient2(SchemaMtl schemaMtl){
		initVariable();
		Date bdate2 = schemaMtl.getBdate();
		Timestamp bdate = new Timestamp(bdate2.getTime());
		Patient newPatientC = schemaMtl.getEditPatientC();
		Patient editPatientC = newPatientC;
		Tree newPatientT ;
		boolean isNewPatient = false;
		log.debug("-----------------1");
		if(editPatientC.getId()!=null&&editPatientC.getId()>TreeManager._5000){
			log.debug("---------------2--");
			newPatientT = readDocT(editPatientC.getId());
		}else{
			log.debug("---------------3--");
			isNewPatient = true;
			Tree npT = null;
			npT = getFolderNewPatientT();
			newPatientT = setNewDocT("patient",npT);
			newPatientC.setId(newPatientT.getId());
			em.persist(newPatientT);
			em.persist(newPatientC);
		}
		log.debug("---------------4-- "+newPatientT);
		Tree schemaDocT = schemaMtl.getDocT();
		schemaMtl.setIdc(null);
		schemaMtl.initOld2newIdM();
		Tree docProtocolT = schemaDocT.getDocT();
		log.debug("---------------5-- "+docProtocolT);
		if(docProtocolT!=null&&"protocol".equals(docProtocolT.getTabName())){
			log.debug("---------------6-- ");
			Tree doc2copyT = readDocT(docProtocolT.getId());
			log.debug("---------------6-- "+doc2copyT);
			Integer idClassVariantInFlow=correcturUseVarianteSchema(schemaMtl,doc2copyT);
			Tree newPatientProtocolT 
				= copyNode(schemaMtl, newPatientT, newPatientT, doc2copyT);
			addOfDate(newPatientProtocolT);
			log.debug("---------------6-- "+newPatientProtocolT);
			copyChild2Patient(schemaMtl, doc2copyT, newPatientProtocolT, newPatientProtocolT,false);
			/*
			 */
			if(null!=idClassVariantInFlow)
			{
				schemaMtl.initUseVariantSchema(idClassVariantInFlow,schemaDocT.getId());
				schemaMtl.correcturUseVarianteSchema(newPatientProtocolT);
			}
			em.persist(newPatientProtocolT);
			Tree startSchemaT = schemaMtl.getFirstSchemaT();
			log.debug(startSchemaT);
			initPatientSchema(schemaMtl, newPatientT, startSchemaT);
		}else{
			log.debug("---------------7-- ");
			Tree newPatientSchemaT = copyNode(schemaMtl, newPatientT, newPatientT, schemaDocT);
			addOfDate(newPatientSchemaT);
			copyChild2Patient(schemaMtl, schemaDocT, newPatientSchemaT, newPatientSchemaT,true);//?true
			em.persist(newPatientSchemaT);
			owsSession.setDocMtl(null);
			schemaMtl.setIdt(newPatientSchemaT.getId());
		}
		log.debug("---------------8-- ");
	}
	private void initPatientSchema(SchemaMtl schemaMtl, Tree newPatientT, Tree startSchemaInConceptT) {
		schemaMtl.initOld2newIdM();
		Tree schemaDocT=schemaMtl.getDocT();
		Tree doc2copyT = readDocT(schemaDocT.getId());
		Tree newPatientSchemaT = copyNode(schemaMtl, newPatientT, newPatientT, doc2copyT);
		Date bdate = schemaMtl.getBdate();
		long sortBeginDate = 0-bdate.getTime();
		log.debug(newPatientSchemaT);
		log.debug(startSchemaInConceptT);
		newPatientSchemaT.setRef(startSchemaInConceptT.getId());
		Tree addOfDate2 = addOfDate(newPatientSchemaT);
		newPatientSchemaT.setSort(sortBeginDate);
		addOfDate2.getHistory().setMdate(bdate);
		
		cycleNumber(schemaMtl,newPatientSchemaT);
		copyChild2Patient(schemaMtl, doc2copyT, startSchemaInConceptT, startSchemaInConceptT,true);
		em.persist(newPatientSchemaT);
		updateRef(schemaMtl);
		owsSession.setDocMtl(null);
		schemaMtl.setIdt(newPatientSchemaT.getRef());
	}
	
	private void cycleNumber(SchemaMtl docMtl, Tree newPatientSchemaT) {
		Integer cycleNr = docMtl.getCycleNr();
		Pvariable cycleNrO = (Pvariable) pvCreator.setPvariable("cycle").setPvalue(""+cycleNr).setUnit("number").build();
		Tree ofDateFixedT = docMtl.addChild(newPatientSchemaT, "pvariable", newPatientSchemaT.getParentT(), nextDbid());
		docMtl.addMObject(cycleNrO, ofDateFixedT);
	}
	/*
	private void copyChild2Patient(TreeManager docMtl, Tree parentT2copy, Tree parentNewT, Tree docNewT) {
		if(parentT2copy.getChildTs()!=null)
		for(Tree tree2copy:parentT2copy.getChildTs()){
			if("definition".equals(tree2copy.getTabName())) continue;
			Tree newT = copyNode2(docMtl, parentNewT, docNewT, tree2copy);
			copyChild2Patient(docMtl, tree2copy, newT, docNewT);
		}
	}
	private Tree copyNode2(TreeManager schemaMtl, Tree parentNewT, Tree docNewT, Tree tree2copy) {
		Tree newT = copyNode(schemaMtl, parentNewT, docNewT, tree2copy);
		schemaMtl.getOld2newIdM().put(tree2copy.getId(), newT.getId());
		return newT;
	}
	private Tree copyNode(TreeManager schemaMtl, Tree parentNewT, Tree docNewT, Tree tree2copy) {
		Tree newT=schemaMtl.addChild(parentNewT, tree2copy.getTabName(), docNewT, nextDbid());
		newT.copyAtt(tree2copy);
		return newT;
	}
	 * */
	private Tree getFolderNewPatientT() {
		Tree npT=testPatientCreator.find();
		return npT;
	}
	@Autowired @Qualifier("testPatientCreator")	DbNodeCreator	testPatientCreator;
	// New schema cycle END

	// Move patient BEGIN
	public void patientMoveStationEntry(PatientMtl docMtl){
		//doctor station
//		Institute stationO = owsSession.getStationO();
		Map<String, Object> stationM = owsSession.getStationM();
		if(null!=stationM)
		{
			Integer idinstitute=(Integer) stationM.get("idinstitute");
			docMtl.setIdMovestation(idinstitute);
		}
		//patient station
		Integer idPatientFolder = docMtl.getDocT().getParentT().getId();
		setPatientStationO(idPatientFolder);
	}
	private void setPatientStationO(Integer idPatientFolder) {
		String sql = "SELECT stationO FROM " +
" Tree patientInstitutFolderT" +
" ,Tree t1" +
" , Tree stationT , Institute stationO" +
" where " +
" stationT.id=stationO.id" +
" and stationT.id=t1.parentT.id and t1.id=patientInstitutFolderT.parentT.id" +
" and patientInstitutFolderT.idClass=:idPatientFolder";
		log.debug(sql+" -- "+idPatientFolder);
		List<Institute> resultList = em.createQuery(sql).setParameter("idPatientFolder", idPatientFolder).getResultList();
		log.debug(resultList);
		if(1==resultList.size())
			owsSession.setPatientStationO(resultList.get(0));
	}
	public void patientMoveStationConfirm(PatientMtl docMtl){
		log.debug(1);
		log.debug(docMtl.getIdMovestation());
		String sql = "SELECT * FROM folder,institute i WHERE folder =i.institute AND idinstitute=?";
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, docMtl.getIdMovestation());
		log.debug(queryForList);
		log.debug(queryForList.size());
		if(queryForList.size()>0){
			Integer idfolder = (Integer) queryForList.get(0).get("idfolder");
			log.debug(idfolder);
		}
	}
	@Transactional
	public void patientMoveStationSave(PatientMtl docMtl){
		log.debug(1);
		log.debug(docMtl.getIdMovestation());
		String inInstitute = ""+docMtl.getIdMovestation();
		List<Map<String, Object>> queryForList = getStationPatientFolders(inInstitute);
		if(queryForList.size()==1){
			Tree patientT = docMtl.getDocT();
			//history record about old folder
			Integer idOldFolderClass = patientT.getParentT().getIdClass();
			addPatientHistoryRecord(patientT,"folder",idOldFolderClass);
			//move to new folder
			Integer idFolder = (Integer) queryForList.get(0).get("idFolder");
			log.debug(idFolder);
			Tree moveToFolderT = em.find(Tree.class, idFolder);
			patientT.setParentT(moveToFolderT);
		}
	}
	//
	@Transactional
	public void archivPatientSave(PatientMtl docMtl){
		Integer idFolder = docMtl.getDocT().getParentT().getId();
		log.debug(idFolder);
		String sql="SELECT t2.id FROM tree t1,tree t2,folder f1,folder f2 " +
		" WHERE t1.id=t2.did AND f1.idfolder=t1.id " +
		" AND f1.folder='archivPatient' " +
		" AND f2.idfolder=t2.idclass AND f2.idfolder=? ";
		List<Map<String, Object>> archivFolderList = simpleJdbc.queryForList(sql, idFolder);
		if(archivFolderList.size()>0)
		{
			log.debug(11);
			Integer idArchivFolder=(Integer)archivFolderList.get(0).get("id");
			log.debug(idArchivFolder);
			simpleJdbc.update("UPDATE tree SET did=? WHERE id=?", idArchivFolder,docMtl.getDocT().getId());
		}
	}
	@Transactional
	public void fromArchivSave(InstituteMtl docMtl){
		log.debug("------"+docMtl.getIdt());
		Tree stationFolderT=null;
		for (Tree patientT:docMtl.getDocT().getChildTs())
			if(docMtl.isFolderO(patientT) && "patient".equals(docMtl.getFolderO(patientT).getFolder()))
				for (Tree stationFolder1T:patientT.getChildTs())
					stationFolderT=stationFolder1T;
		if(null!=stationFolderT)
		{
			Tree patientFolderT = em.find(Tree.class, stationFolderT.getIdClass());
			Tree patientT = em.find(Tree.class, docMtl.getIdt());
			patientT.setParentT(patientFolderT);
			em.merge(patientT);
		}
	}
	private List<Map<String, Object>> getStationPatientFolders(String inInstitute) {
		String sqlInstitutepatientFolder = getSqlInstitutepatientFolder(inInstitute);
		log.debug(sqlInstitutepatientFolder);
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sqlInstitutepatientFolder);
		return queryForList;
	}
	// Move patient END

}

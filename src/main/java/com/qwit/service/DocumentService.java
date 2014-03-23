package com.qwit.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.config.DbConfig;
import com.qwit.crypt.EncryptionService;
import com.qwit.domain.App;
import com.qwit.domain.Arm;
import com.qwit.domain.Contactperson;
import com.qwit.domain.Day;
import com.qwit.domain.Diagnose;
import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Expr;
import com.qwit.domain.Finding;
import com.qwit.domain.Folder;
import com.qwit.domain.History;
import com.qwit.domain.Institute;
import com.qwit.domain.Labor;
import com.qwit.domain.MObject;
import com.qwit.domain.Notice;
import com.qwit.domain.Owuser;
import com.qwit.domain.Patient;
import com.qwit.domain.Protocol;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Symptom;
import com.qwit.domain.Task;
import com.qwit.domain.Times;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.domain.Url;
import com.qwit.model.ConceptMtl;
import com.qwit.model.DrugMtl;
import com.qwit.model.ExplorerMtl;
import com.qwit.model.InstituteMtl;
import com.qwit.model.IntensivDay;
import com.qwit.model.MtlXml;
import com.qwit.model.OwsSession;
import com.qwit.model.PatientMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.model.UserMtl;
import com.qwit.util.FlowObjCreator;
import com.qwit.util.FormUtil;
import com.qwit.util.RegisterUserForm;

/**
 * Methods for Qtoo document editing.
 * @author roman
 */
@Service("documentService")
@Repository
public class DocumentService extends MtlDbService{
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired	private DbUpdateContainer dbUpdateContainer;
	@Autowired	@Qualifier("tumorboardDate")		TumorboardDate	tumorboardDate;
	@Autowired	@Qualifier("testPatientCreator")	DbNodeCreator	testPatientCreator;
	@Autowired	@Qualifier("contactpersonManager")	MailService		contactpersonManager;
	@Autowired	private EncryptionService	encoder;

	private Arm			cycleArmC;

	private void init() {
		log.debug("-----");
//		procent100 = getPvariable("percent","100","%");
		log.debug("-----");
//		ofDateFixed = getPvariable("ofDate","fixed","");
//		owsSession.setOfDateFixedId(ofDateFixed.getId());
		cycleArmC = new Arm();
		cycleArmC.setArm("cycle");
		cycleArmC=(Arm) armL(cycleArmC).get(0);
		log.debug("-----");
		dbUpdateContainer.dbupdate();
		log.debug("-----");
		owsSession.getUserId(simpleJdbc);
		log.debug("-----"+testPatientCreator);
		log.debug("-----"+testPatientCreator.getDbUpdate().getIfNotDbObj());
//		getSourceFolderT("notice");
	}

	public String getDoseApp(){
		String string = "{}";
		try {
			JSONObject put = new JSONObject()
			.put("identifier", "name")
			.put("label", "name")
			;
			JSONArray ja = new JSONArray();
			ja.put(new JSONObject().put("name", "i.v."));
			ja.put(new JSONObject().put("name", "inf."));
			ja.put(new JSONObject().put("name", "Kurz Inf."));
			ja.put(new JSONObject().put("name", "p.o."));
			ja.put(new JSONObject().put("name", "i.m."));
			ja.put(new JSONObject().put("name", "i.th."));
			ja.put(new JSONObject().put("name", "s.c."));
			ja.put(new JSONObject().put("name", "lokal"));
			put.put("items", ja);
			string = put.toString();
		} catch (JSONException e) {e.printStackTrace();}
		return string;
	}

	public String getJsonDoseUnit()
	{
		String string = "{}";
		try {
			JSONObject put = new JSONObject()
			.put("identifier", "name")
			.put("label", "name")
			;
			JSONArray ja = new JSONArray();
			ja.put(new JSONObject().put("name", "mg"));
			ja.put(new JSONObject().put("name", "mg/m²"));
			ja.put(new JSONObject().put("name", "mg/kg"));
			ja.put(new JSONObject().put("name", "µg/kg"));
			ja.put(new JSONObject().put("name", "g"));
			ja.put(new JSONObject().put("name", "ml"));
			ja.put(new JSONObject().put("name", "ml/m²"));
			ja.put(new JSONObject().put("name", "Amp"));
			ja.put(new JSONObject().put("name", "I.E."));
                        ja.put(new JSONObject().put("name", "Mio I.E."));
			ja.put(new JSONObject().put("name", "U"));
			ja.put(new JSONObject().put("name", "AUC"));
                        ja.put(new JSONObject().put("name", "Gy"));
			put.put("items", ja);
			string = put.toString();
		} catch (JSONException e) {e.printStackTrace();}
		log.debug(string);
		return string;
	}

	@Transactional(readOnly = true)
	public String getJsonSymptoms(){return getJsonSymptoms("SELECT t FROM Symptom t ");}

	@Transactional(readOnly = true)
	public String getJsonLabors(){return getJsonLabors("SELECT t FROM Labor t WHERE t.unit != '' AND t.unit != 'yes_no' AND t.unit != 'note' ");}
	public String getJsonLaborUnits()
	{
		String string = "{'identifier':'name', " +
						" 'label': 'name'," +
						"  'items': [ " +
						"  {'name': '/microl'}, " +
						"  {'name': 'ml/min/m²'}, " +
						"  {'name': 'micromol/l'}, " +
						"  {'name': '10^6/microl'}, " +
						"  {'name': 'pH'}, " +
						"  {'name': 'mg/dl'}, " +
						"  {'name': 'g/dl'} " +
						" ]" +
						" }";
		
		/*String string = "{}";
		try {
			JSONObject put = new JSONObject()
			.put("identifier", "name")
			.put("label", "name")
			;
			JSONArray ja = new JSONArray();
			ja.put(new JSONObject().put("name", "ml"));
			ja.put(new JSONObject().put("name", "ml/m²"));
			ja.put(new JSONObject().put("name", "g"));
			ja.put(new JSONObject().put("name", "mg"));
			ja.put(new JSONObject().put("name", "mg/m²"));
			put.put("items", ja);
			string = put.toString();
		} catch (JSONException e) {e.printStackTrace();}
		*/
		return string;
	}
	public String getJsonFindingUnits()
	{
		String string = "{'identifier':'name', " +
		" 'label': 'name'," +
		"  'items': [ " +
		"  {'name': 'yes|no'}, " +
		"  {'name': '0-4'}, " +
		"  {'name': '1-5'}, " +		
		" ]" +
		" }";		
		return string;
	}
	
	@Transactional(readOnly = true)
	public String getJsonFindings(){return getJsonFindings("SELECT t FROM Finding t ");}
	
	
	/**
	*	The universal List To JSON conversion function
	*	needs that every DAO-Class implements getName function, that returns the name of Drug.getDrug(), Dose.getDose(), etc...
	* @author SK
	* @param sql
	* @return
	*/
//	private <T> String convertListToJson(List<T extends MClass> objLust) {
//		long sT = System.currentTimeMillis();
//				String str="";	
//			try 
//			{
//				JSONObject put = new JSONObject().put("identifier", "name").put("label", "name");
//				JSONArray ja = new JSONArray();
//				for (T obj : objList) {
					//TODO: write in DAO-Classes the getName function that returns the name of drug, dose, notice, etc...
//					ja.put(new JSONObject().put("name", obj.getName())); 
//				}
//				put.put("items", ja);
//				str = put.toString();
//			} catch (JSONException e) {e.printStackTrace();}
//			log.debug("---------------- "+sql+drL.size()+"/"+(System.currentTimeMillis()-sT));
//		
		//		log.debug("---------------- "+(System.currentTimeMillis()-sT));
//		return str;
//	}
//	
	
	private String getJsonLabors(String sql) {
		long sT = System.currentTimeMillis();
//		if(drugs.equals("{}")){
		String str="";	
			List<Labor> drL = em.createQuery(sql).getResultList();
			try {
				JSONObject put = new JSONObject().put("identifier", "name").put("label", "name");
				JSONArray ja = new JSONArray();
				for (Labor it : drL) {
					ja.put(new JSONObject().put("name", it.getLabor() + " | " + it.getUnit()));
				}
				put.put("items", ja);
				str = put.toString();
			} catch (JSONException e) {e.printStackTrace();}
//		}
		return str;
	}
	
	private String getJsonFindings(String sql) {
		long sT = System.currentTimeMillis();
//		if(drugs.equals("{}")){
		String str="";	
		List<Finding> drL = em.createQuery(sql).getResultList();
		try {
			JSONObject put = new JSONObject().put("identifier", "name").put("label", "name");
			JSONArray ja = new JSONArray();
			for (Finding it : drL) {
				ja.put(new JSONObject().put("name", it.getFinding()));
			}
			put.put("items", ja);
			str = put.toString();
		} catch (JSONException e) {e.printStackTrace();}
//		}
		return str;
	}
	
	private String getJsonSymptoms(String sql) {
		long sT = System.currentTimeMillis();
//		if(drugs.equals("{}")){
			String symptoms = "{}";
			List<Symptom> drL = em.createQuery(sql).getResultList();
			try {
				JSONObject put = new JSONObject().put("identifier", "name").put("label", "name");
				JSONArray ja = new JSONArray();
				for (Symptom drug : drL) {
					ja.put(new JSONObject().put("name", drug.getSymptom()));
				}
				put.put("items", ja);
				symptoms = put.toString();
			} catch (JSONException e) {e.printStackTrace();}
//		}
		return symptoms;
	}
	
	private HashMap<String, Tree> fsIdH=new HashMap<String, Tree>();
	
	private Tree getSourceFolderT_depr(String tableName)
	{
//		if(fsIdH==null){
		if(fsIdH.size()==0){
			log.debug("------folder-----------");
			Tree folderT=getFolderTree();
			log.debug(folderT);
			HashMap<Integer, Tree> ff = new HashMap<Integer, Tree>();
//			folderT.getTabName();//TODO gegen lazy session verlust.
			//Hibernate.initialize(folderT);//TODO gegen lazy session verlust.
			for(Tree folder1T:folderT.getChildTs()){
				ff.put(folder1T.getId(), folder1T);
				for(Tree folder2T:folder1T.getChildTs()){
					ff.put(folder2T.getId(), folder2T);
				}
			}
//			fsIdH = new HashMap<String, Tree>();
			/*
			Folder folderDoc = getRootDocFolder();
			for(Folder folder1F:folderDoc.getChildFs()){
				fsIdH.put(folder1F.getFolder(), ff.get(folder1F.getId()));
				for(Folder folder2F:folder1F.getChildFs()){
					fsIdH.put(folder2F.getFolder(), ff.get(folder2F.getId()));
					if("patient".equals(folder1F.getFolder())){
						if("new".equals(folder2F.getFolder())){
							fsIdH.put("newPatient", ff.get(folder2F.getId()));
						}
					}
				}
			}
			 * */
		}
		return fsIdH.get(tableName);
	}
	
	//TODO: decide move or not to ExplorerMTLService or MtlDBService
	public Folder getFolder(String string)
	{
		Folder folder=getRootDocFolder();
		/*
		for(Folder folder2:folder.getChildFs()){
			if(folder2.getFolder().equals(string))
			{
				folder=folder2;
				break;
			}
		}
		 * */
		return folder;
	}
	
	@Transactional(readOnly = true)
	public SchemaMtl readContactPerson(String userName) 
	{
		Owuser owUser =readUser(userName);
		Tree owUserT = readDbDoc(owUser.getId());
		Tree contactPersonT = readDbDoc(owUserT.getParentT().getId());
		SchemaMtl contactPersonM = new SchemaMtl(contactPersonT);
		contactPersonM.addClass(contactPersonT,contactPersonT.getId(),em);
		return contactPersonM;
	}
	private Owuser readUser(String userName) 
	{
		Owuser owUser = (Owuser) em.createQuery("SELECT t FROM Owuser t WHERE  t.owuser = :userName ")
		.setParameter("userName", userName)
		.getSingleResult();
		return owUser;
	}
	
	private Tree getCareSchemeT(String userName)
	{
		Owuser owuser = readUser(userName);
		Integer instituteId = owuser.getInstitute();
		Tree careSchemeT = (Tree)em.createQuery("SELECT t FROM Tree t WHERE t.did=:instituteId and t.tabName='task'")
		.setParameter("instituteId",instituteId)
		.getResultList();
		log.debug("--- ho ho ho, it work's!");
		return careSchemeT;
	}
	
	@Transactional(readOnly = true)
	public Patient seekPatient(String givenname, String familyname) {
		List<Patient> patientList = em.createQuery("SELECT p FROM Patient p " +
				" WHERE p.patient=:givenname AND p.forename=:familyname")
				.setParameter("givenname", givenname)
				.setParameter("familyname", familyname)
				.getResultList();
		if(patientList.size()==1){
			return (Patient)patientList.get(0);
		}else if(patientList.size()>0){
		}else{
		}
		return null;
	}
	@Transactional(readOnly = true)
	public Url makeUrlO(Integer idUrl){
		return em.find(Url.class, idUrl);
	}
	@Transactional(readOnly = true)
	public Patient makePatientO(Integer idPatient){
		return em.find(Patient.class, idPatient);
	}
	@Transactional(readOnly = true)
	public PatientMtl makePatientMtl(Integer idDoc){
		log.debug(idDoc);
		long sT = System.currentTimeMillis();
		Tree patientT = readDocT(idDoc);
		PatientMtl docMtl = new PatientMtl(patientT);
		
		docMtl.setAccessRight(getRight(docMtl.getFolderT().getId()));
		owsSession.setPatientT(docMtl.getDocT());
		owsSession.setAccessRight2patient(docMtl.getAccessRight());
		long sT1 = System.currentTimeMillis();
		docMtl.setMsCnt(0);
		docMtl.addClass(patientT,patientT.getId(),em);
		docMtl.addClassM(patientT.getParentT(), em);
		
		findTherapyChange(docMtl);
		docMtl.readUser(simpleJdbc);
//		owsSession.initPatientO(idDoc, em);
		docMtl.calcChronologNr();
		log.debug("----TimeMillis----------"
				+docMtl.getDocT().getTabName()+":"+docMtl.getClassM().size()+"/"+(System.currentTimeMillis()-sT));
		setSessionStationM();
		setPatientUserStation(docMtl.getDocT());
		owsSession.setModus("patient");
		return docMtl;
	}

	private void setPatientUserStation(Tree docT) {
		Integer idPatientFolder = docT.getParentT().getId();
		log.debug(idPatientFolder);
		List<Map<String, Object>> userStationList = owsSession.getUserStationList();
		if(null!=userStationList)
		for (Map<String, Object> userStationM : userStationList) {
			Integer idStationPatientFolder=(Integer)userStationM.get("idStationPatientFolder");
			log.debug(idStationPatientFolder);
			if(idPatientFolder.equals(idStationPatientFolder))
				owsSession.setStationM(userStationM);
		}
		log.debug(owsSession.getStationM());
	}

	@Transactional(readOnly = true)
	public void addIdtPatient(Integer idPatient,TreeManager docM) {
		docM.initPatiantO(idPatient, em);
	}
	/**
	 * Add session patient in list.
	 * Used in web flow.
	 */
	public void addSessionPatient(TreeManager docM) {
//		public void addSessionPatient(SchemaMtl docM) {
		Patient patientO = owsSession.getPatientO();
		log.debug(patientO);
		if(patientO!=null){
			List<Patient> patientL = docM.getPatientL();
			if(patientL==null){
				patientL = new ArrayList<Patient>();
				docM.setPatientL(patientL);
			}
			patientL.add(patientO);
		}
	}
	private void addSessionSchema(SchemaMtl docM) {
		if(docM.getPatientMtl()==null)
			owsSession.setTaskO((Task) docM.getDocT().getMtlO());
//		else{
//			owsSession.initPatientO(docM.getPatientMtl().getDocT().getId(), em);
//		}
		/*
		UserCareSession ucs = owsSession.getUserCareSession();
		ucs.setId2userName(simpleJdbc);
		ucs.set(this);
		//229547
		if(docM instanceof SchemaMtl){
			SchemaMtl	schemaCareMtl = null;
			if(ucs.getSchemaCareMtl()==null){
				Tree schemaCareT = readDocT(229547);
				if(schemaCareT!=null){
					schemaCareMtl = new SchemaMtl(schemaCareT);
					schemaCareMtl.addClass(schemaCareT, schemaCareT.getId(), em);
					ucs.setSchemaCareMtl(schemaCareMtl);
				}
			}
		}
		docM.setUserCare(ucs);
		 * */
	}
	/**
	 * Add drug document to schema document.
	 * Call from Spring Web Flow.
	 * @param schemaMtl
	 */
	@Transactional(readOnly = true)
	public void addDrugMtl(SchemaMtl schemaMtl){
		log.debug("-----------"+schemaMtl.getIdt());
		schemaMtl.setEditDrugT();
		Tree editDrugT = schemaMtl.getEditNodeT();
		log.debug("-----------"+editDrugT);
		log.debug("-----------"+editDrugT.getParentT());
		Integer idDrug = editDrugT.getIdClass();
		log.debug(idDrug);
		if(idDrug!=null){
			DrugMtl readDrugMtl = readDrugMtl(idDrug);
			log.debug(readDrugMtl);
			schemaMtl.setDrugMtl(readDrugMtl);
		}
		log.debug("-----------");
	}
	@Transactional(readOnly = true)
	public DrugMtl makeDrugMtl(Integer idDoc){
		log.debug("-----------------");
		Drug drugC = em.find(Drug.class, idDoc);
		Integer idGeneric = drugC.getGeneric().getId();
		//		Tree schemaT = readDocT(idDoc);
		DrugMtl docMtl = readDrugMtl(idGeneric);
		Tree folderT=docMtl.getDocT().getParentT();
		docMtl.setAccessRight(getRight(folderT.getId()));
		Folder folderO = null;
		do{
			docMtl.addClassM(folderT, em);
			folderO = (Folder) folderT.getMtlO();
			folderT=folderT.getParentT();
		}while(!"drug".equals(folderO.getFolder()));

		if(isDrugUsage()){
			String idDoseS	= owsSession.getRequest().getParameter("iddose");
			String idDayS	= owsSession.getRequest().getParameter("idday");
			String idTimesS	= owsSession.getRequest().getParameter("idtimes");
			if(idDoseS!=null){
				Integer idDose = Integer.parseInt(idDoseS);
				if(0==idDose) idDose=null;
				owsSession.setIdDose(idDose);
			}
			if(idDayS!=null){
				if(0==idDayS.length()){
					owsSession.setIdDay(null);
				}else{
					owsSession.setIdDay(Integer.parseInt(idDayS));
				}
			}
			if(idTimesS!=null){
				if(0==idTimesS.length()){
					owsSession.setIdTimes(null);
				}else{
					owsSession.setIdTimes(Integer.parseInt(idTimesS));
				}
			}
			/*
			if(dayS!=null&&timesS!=null){
				Integer idDay=null;
				if(!"".equals(dayS)&&!"0".equals(dayS))
					idDay = Integer.parseInt(dayS);
				owsSession.setIdDay(idDay);
				Integer idTimes = null;
				if(!"".equals(timesS)&&!"0".equals(timesS))
					idTimes = Integer.parseInt(timesS);
				owsSession.setIdTimes(idTimes);
			}
			 * */
//			log.debug("---------1--");
			docMtl.drugUsedDose(simpleJdbc);
			docMtl.drugUsedDay(simpleJdbc);
			docMtl.drugUsedTimes(simpleJdbc);
//			log.debug("---------2--");
			docMtl.drugUsedNotice(simpleJdbc);
//			log.debug("---------3--");
//			log.debug("---------4--");
			docMtl.drugTask(simpleJdbc);
//			log.debug("---------5--");
		}else{
			docMtl.getUsedDose().readDrugDose(simpleJdbc, idGeneric);
			String sql = "SELECT * FROM drug where idgeneric =? AND iddrug!=?";
			List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, idGeneric, idGeneric);
			for (Map<String, Object> map : queryForList) {
				Integer idDrug = (Integer) map.get("iddrug");
				Tree tradeT = readDocT(idDrug);
				docMtl.addClass(tradeT,tradeT.getId(),em);
				docMtl.getTradeL().add(tradeT);
			}
//			drugMtl.addClass(genericT,genericT.getId(),em);
			docMtl.setTimesNr(new HashMap<Tree, Integer>());
			Tree docT = docMtl.getDocT();
			if(docT.getChildTs()!=null)
				for(Tree t:docT.getChildTs())
					if(t.getMtlO() instanceof Task)
			{
						docMtl.setTimesNr(t);
			}
			docMtl.getPlan();
		}
		copySourceTarget(docMtl);
		return docMtl;
	}

	private void copySourceTarget(TreeManager docMtl) {
		Integer targetIdClass = owsSession.getTargetIdClass();
		if(null!=targetIdClass && 
			(null==owsSession.getTargetT()
			|| !targetIdClass.equals(owsSession.getTargetT().getIdClass())
			)
		){
			Tree tT = getTreeById(docMtl, targetIdClass);
			owsSession.setTargetT(tT);
		}
		Integer sourceIdClass = owsSession.getSourceIdClass();
		log.debug("sourceIdClass="+sourceIdClass+"/"+owsSession.getSourceT());
		if(null!=sourceIdClass && 
			(null==owsSession.getSourceT() 
			|| !sourceIdClass.equals(owsSession.getSourceT())
			)
		){
			log.debug("--------------");
//			if(null!=sourceIdClass && null==owsSession.getSourceT()){
			Tree sT = getTreeById(docMtl, sourceIdClass);
			owsSession.setSourceT(sT);
		}
	}

	private boolean isDrugUsage() {
		return	"usage".equals(owsSession.getDrugPart())
		||		"usage".equals(owsSession.getRequest().getParameter("part"));
	}

	private DrugMtl readDrugMtl(Integer idGeneric) {
		Tree genericT = readDocT(idGeneric);
		DrugMtl drugMtl = new DrugMtl(genericT);
		drugMtl.addClass(genericT,genericT.getId(),em);
		return drugMtl;
	}

	@Transactional(readOnly = true)
	public ConceptMtl makeConceptMtl(Integer idDoc)
	{
		long sT = System.currentTimeMillis();
		Tree conceptT = readDocT(idDoc);
		ConceptMtl docMtl = makeConceptMtl(conceptT);
		Tree patientT = docMtl.getDocT().getDocT();
		setProtocolFolder(docMtl, docMtl.getDocT());
		notFlowNotVariant(docMtl);
		if(patientT!=null&&"patient".equals(patientT.getTabName()))
		{
			log.debug(patientT);
			makePatientMtl(docMtl, patientT);
		}
		Tree conceptDefinitionT = em.find(Tree.class, conceptT.getIdClass());
		log.debug(conceptDefinitionT);
		for(Tree definitionT:conceptDefinitionT.getChildTs())
			for(Tree schemaT:definitionT.getChildTs())
				if(schemaT.getId().equals(schemaT.getIdClass()))
				{
					docMtl.addClassM(schemaT,em);
					docMtl.addDefintitionSchemaT(schemaT);
					log.debug(schemaT);
				}
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()
			+ ":"+docMtl.getClassM().size()
			+"/" +docMtl.getTree().size() 
			+"/" +docMtl.getMsCnt() 
			+ "/"+(System.currentTimeMillis()-sT));
//		final byte[] bytes = Prefactor.getBytes(docMtl);
		return docMtl;
	}
	
	public void notFlowNotVariant(TreeManager docMtl) {
		Integer id = docMtl.getDocT().getId();
		if(docMtl instanceof SchemaMtl)
			id = docMtl.getDocT().getDocT().getId();
		String sql = 
			"SELECT * FROM (SELECT t1.* FROM (" +
			" SELECT * FROM tree t1,task WHERE t1.iddoc=? AND t1.id=idtask) t1 " +
			" LEFT JOIN tree t2 ON t2.iddoc=t1.iddoc AND t2.id!=t2.idclass AND t2.idclass=t1.id" +
			" WHERE t2 IS NULL" +
			" ) t1 LEFT JOIN task s2 ON s2.cyclename=t1.cyclename AND s2.idtask!=t1.idtask" +
			" WHERE s2 IS NOT NULL OR t1.cyclename IS NULL OR t1.cyclename='' OR t1.cyclename=''||t1.idtask";
		log.debug(sql+" "+id);
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, id);
		if(queryForList.size()>0){
			Set<Integer> s = new HashSet();
			for(Map map:queryForList)
				s.add((Integer) map.get("id"));
			docMtl.setNotFlowNotVariant(s);
		}
	}

	protected ConceptMtl makeConceptMtl(Tree conceptT) {
		ConceptMtl conceptMtl = new ConceptMtl(conceptT);
		conceptMtl.addClass(conceptT,conceptT.getId(), em);
		/*
		log.debug("-----------"+conceptMtl.getDocT());
		log.debug("-----------"+conceptMtl.getDocT().hasChild());
		log.debug("-----------"+conceptMtl.getDocT().getChildTs());
		showChoose(conceptMtl.getDocT());
		log.debug("-----------");
		 * */
		return conceptMtl;
	}
	
	private void showChoose(Tree parentT) {
		if(parentT.hasChild())
			for(Tree t:parentT.getChildTs()){
				if("choice".equals(t.getTabName())){
					log.debug(t);
				}else{
					
				}
				showChoose(t);
			}
	}

	@Transactional(readOnly = true)
	public MtlXml makeDoc2xml(Integer idDoc)
	{
		log.debug("---------------");
		long sT = System.currentTimeMillis();
		Tree docT = readDocT(idDoc);
		SchemaMtl schemaMtl = new SchemaMtl(docT);
		schemaMtl.addClass(docT,docT.getId(),em);
		log.debug("-TimeMillis-------------"+schemaMtl.getDocT().getTabName()
				+ ":"+schemaMtl.getClassM().size()+"/" +schemaMtl.getMsCnt() 
				+ "/"+(System.currentTimeMillis()-sT));
		MtlXml mtlXml = new MtlXml(schemaMtl);
		mtlXml.makeTreeDoc2xml();
		log.debug("-TimeMillis-------------"+schemaMtl.getDocT().getTabName()
				+ ":"+schemaMtl.getClassM().size()+"/" +schemaMtl.getMsCnt() 
				+ "/"+(System.currentTimeMillis()-sT));
		return mtlXml;
	}
	@Transactional
	public void saveDocState(DbNodeCreator docStatusCreator, DbPvariableCreator pvCreator,DbNoticeCreator noticeCreator
			, FormUtil fu, SchemaMtl schemaMtl)
	{
		log.debug("nextDbid="+nextDbid());
		Pvariable pvO = fu.getPvO();
		pvO.setPvariable("infostatus");
		pvO= (Pvariable) pvCreator.setPv(pvO).build();
		Notice noticeO = fu.getEditNoticeC();
		noticeO=(Notice) noticeCreator.setMtlO(noticeO).build();
		UserMtl userMtl = makeUserMtl();
//		OwsSessionContainer.getOwsSessionContainer().getUserId(simpleJdbc);//make user id
		OwsSession.getOwsSession().getUserId(simpleJdbc);//make user id
		log.debug("nextDbid="+nextDbid());
		log.debug(noticeO);
		Tree pvStatusT = docStatusCreator
		.setRef1(fu.getIdt())
		.setIdclass1(pvO.getId())
		.setIdclass2(fu.getIdt())
		.setIdclass3(noticeO.getId())
		.setTreeManager(userMtl)
		.setParentT(userMtl.getDocT()).addChild();
		log.debug(pvStatusT);
		log.debug("nextDbid="+nextDbid());
		userMtl.addHistory(pvStatusT);
		Tree docT = docStatusCreator.getTree2();
		log.debug(docT);
		schemaMtl.initOld2newIdM();
//		schemaMtl.initRef2idMS();
		schemaMtl.addRefR(schemaMtl.getDocT());
		log.debug("nextDbid="+nextDbid());
		copyChild(schemaMtl, schemaMtl.getDocT(), docT, docT);
		log.debug("nextDbid="+nextDbid());
		em.persist(pvStatusT);
		schemaMtl.updateRef2(em);
		em.merge(pvStatusT);
		log.debug("nextDbid="+nextDbid());
//		if(true)			return;
	}
	@Autowired @Qualifier("copyPasteService") CopyPasteService copyPasteService;
	public List<Map<String, Object>> test(UserMtl userMtl){
//		String tabName = userMtl.getDocT().getTabName();
		String sqlApp = "select * from app";
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sqlApp);
		return queryForList;
	}
	@Transactional
	public void insert(UserMtl userMtl,Integer idt){
//		Tree pasteT = userMtl.getSourceT();
		Tree pasteT = owsSession.getSourceT();
		log.debug(idt);
		log.debug(pasteT);
		insert(idt,pasteT,idt);
	}
	private void insert(Integer idt, Tree pasteT, Integer idDoc) {
		SchemaMtl targetSchemaMtl = makeSchemaMtl(idDoc);
		targetSchemaMtl.setIdc(idt);
//		targetSchemaMtl.paste(pasteT);
		Tree sT = owsSession.getSourceT();
		log.debug(sT);
		owsSession.setCopyNodeT(sT);
		copyPasteService.paste(targetSchemaMtl);
		saveSchemaNative(targetSchemaMtl);
	}
	@Transactional
	public void rewriteElement(UserMtl userMtl,Integer idt){
//		Tree targetT = userMtl.getTargetT();
//		Tree sourceT = userMtl.getSourceT();
		Tree sourceT = owsSession.getSourceT();
		Tree targetT = owsSession.getTargetT();
		String targetName = targetT.getTabName();
		if(null==sourceT)
			return;
		String sourceName = sourceT.getTabName();
		log.debug(targetName+" "+targetT);
		log.debug(sourceName+" "+sourceT);
		Tree idT = em.find(Tree.class, idt);
		if("notice".equals(targetName)){
			if("notice".equals(sourceName)){
				updateIdClass(idt, sourceT);
			}
		}else if("labor".equals(targetName)){
			if("notice".equals(sourceName)){
				insertDelete(userMtl, idt, targetT);
			}
		}else if("drug".equals(targetName)){
			if("drug".equals(sourceName)){
				
				insertDelete(userMtl, idt, targetT);
			}
		}
	}
	private void insertDelete(UserMtl userMtl, Integer idt, Tree targetT) {
//		Tree pasteT = userMtl.getSourceT();
		Tree pasteT = owsSession.getSourceT();
		Tree idT = em.find(Tree.class, idt);
		log.debug("idT= "+idT);
		Integer idDoc = idT.getDocT().getId();
		insert(idt, pasteT, idDoc);
		String sql="DELETE FROM tree WHERE id=?";
		log.debug(sql+" "+targetT.getId());
		log.debug(sql+" "+idt);
		simpleJdbc.update(sql,idt);
	}

	private void updateIdClass(Integer idt, Tree sourceT) {
		String sql="UPDATE tree SET idclass=? WHERE id=?";
		log.debug(sql+" "+idt);
		simpleJdbc.update(sql,sourceT.getIdClass(), idt);
	}

	@Transactional(readOnly = true)
	public SchemaMtl makeSendableSchemaMtl(Integer idDoc){
		Tree schemaT = readDocT(idDoc);
		log.debug("-TimeMillis-------------"+ ":" +idDoc );
		SchemaMtl docMtl = new SchemaMtl(schemaT);
		docMtl.addClass(schemaT,schemaT.getId(),em);
		return docMtl;
	}
	
	@Transactional(readOnly = true)
	public ConceptMtl makeSendableConceptMtl(Integer idDoc){
		Tree conceptT = readDocT(idDoc);
		log.debug("-TimeMillis-------------"+ ":" +idDoc );
		ConceptMtl docMtl = new ConceptMtl(conceptT);
		docMtl.addClass(conceptT,conceptT.getId(),em);
		Iterator iteratePointers = JXPathContext.newContext(docMtl.getDocT())
		.iteratePointers("childTs[tabName='definition']/childTs[tabName='task']");
		while (iteratePointers.hasNext()) {
			Tree taskT = (Tree) ((Pointer) iteratePointers.next()).getValue();
			log.debug(taskT);
			SchemaMtl schemaMtl = makeSendableSchemaMtl(taskT.getId());
			docMtl.setDefSchemaMtl(schemaMtl);
		}
		log.debug(1);
//		setImportChild_depr(conceptT,conceptT);
		log.debug(2);
		return docMtl;
	}

	private void setImportChild_depr(Tree tree,Tree docT) {
		boolean importChild = false;
		importChild = 
		tree.getDocT()!=null
		&& (docT==tree.getDocT()
		||docT==tree
		)
		&& tree.hasChild();
		tree.setImportChild(importChild);
		log.debug(importChild+" "+tree.getTabName()+" "+tree.getId());
		if(tree.isImportChild())
			for(Tree childT:tree.getChildTs()){
				setImportChild_depr(childT,docT);
			}
	}

	public String getDocUrlType(Integer idDoc) {
		String docType = "schema";
		Tree find = em.find(Tree.class, idDoc);
		if("patient".equals(find.getTabName()))
			docType = "patient";
		return docType;
	}

	
	
	public void setHistoryM(SchemaMtl docMtl)
	{
		
		String sql = "SELECT x.idauthor, c.idcontactperson, c.contactperson, c.forename, c.title  FROM tree owuserT, contactperson c, " +
					 " (SELECT idauthor,count(*) FROM tree docNodeT , history " +
					 " WHERE ? in (docNodeT.id, docNodeT.iddoc) AND id=idhistory GROUP BY idauthor) x " +
					 "	WHERE owuserT.id=x.idauthor AND owuserT.did=c.idcontactperson ";
		
//		String sql ="SELECT docNodeT.id, c.* FROM tree docNodeT, history, owuser, tree owuserT, " +
//					"contactperson c "+
//					"WHERE docNodeT.id=idhistory AND idauthor=idowuser "+
//					"AND owuserT.id=idowuser AND owuserT.did=c.idcontactperson "+
//					"AND ? in (docNodeT.id, docNodeT.iddoc) ORDER BY mdate DESC ";
		
		List<Map<String, Object>> mapList = simpleJdbc.queryForList(sql, new Object[]{docMtl.getDocT().getId()});
		log.debug("getHistoryM:docMtl.getDocT().getId()="+ docMtl.getDocT().getId());
		log.debug("getHistoryM:mapList"+mapList);
		
		if(!mapList.isEmpty())
		{
			Map<Integer, Contactperson> historyContactPersonM = docMtl.getHistoryM();
			for (Map<String, Object> map : mapList) 
			{
				Integer idauthor =(Integer) map.get("idauthor");
				Contactperson c = new Contactperson();
				c.setId((Integer) map.get("idcontactperson"));
				c.setContactperson((String) map.get("contactperson"));
				c.setForename((String) map.get("forename"));
				c.setTitle((String) map.get("title"));
				historyContactPersonM.put(idauthor, c);
			}
			  
		}
	}
	
	
	@Transactional(readOnly = true)
	public SchemaMtl makeSchemaMtl(Integer idDoc){
//		if	(	procent100==null||	ofDateFixed==null)init();
		log.debug("--------------"+idDoc);
		long sT = System.currentTimeMillis();
		Tree schemaT = readDocT(idDoc);
		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		SchemaMtl docMtl = new SchemaMtl(schemaT);
		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		docMtl.addClass(schemaT,schemaT.getId(),em);
		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
//		if(true) return schemaMtl;
		JXPathContext jxpContext = JXPathContext.newContext(docMtl);
		docMtl.setTimesNr(jxpContext);
//		schemaMtl.setTimesNr(schemaMtl.getDocT());
		Tree protocolT = docMtl.getDocT().getDocT();
//		SchemaMtl schemaMtl = schemaMtl1;
//		Tree protocolT = schemaT.getDocT();
		setProtocolFolder(docMtl, protocolT);
		Tree patientT = protocolT.getParentT();
		docMtl.addClassM(patientT, em);
		if(patientT.getMtlO() instanceof Patient){
			PatientMtl patientMtl = makePatientMtl(patientT.getId());
			docMtl.setPatientMtl(patientMtl);
		}
		log.debug(docMtl.getFolderT());
		docMtl.setAccessRight(getRight(docMtl.getFolderT().getId()));
//		schemaMtl.setTimesNr(schemaT.getDocT());
		
//		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
//		setGRule(docMtl);
		setDrugRule(docMtl);
		initRule(docMtl);
//		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		//old bad
//		docMtl.setTrade(simpleJdbc,em);
//		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		//old bad
//		setGeneric(docMtl);
		//new true
//		Institute stationO = owsSession.getStationO();
		Map<String, Object> stationM = owsSession.getStationM();
		if(null!=stationM)
		{
//			docMtl.initTradeM();
			Integer idinstitute=(Integer) stationM.get("idinstitute");
			List<Drug> tradeList = makeSchemaStationTradeList(docMtl.getDocT().getId(),idinstitute);
			docMtl.setTrade(tradeList);
		}
//		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		setCopyHistory(docMtl);
//		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		docMtl.setDrugNotice(simpleJdbc);
//		log.debug("-TimeMillis-----before concept----"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		/* test - if not necessary delete 
		if(protocolT!=null){
			ConceptMtl conceptMtl = makeConceptMtl(protocolT);
			schemaMtl.setConceptMtl(conceptMtl);
			Tree patientT = protocolT.getDocT();
			if(patientT!=null&&patientT.getTabName().equals("patient")){
				makePatientMtl(schemaMtl, patientT);
//				schemaMtl.setModDose(simpleJdbc);
			}
		}
		 */
		log.debug("-TimeMillis-----after concept----"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		addSessionSchema(docMtl);
//		docMtl.calcCycleNr();
		docMtl.calcDose();
		if(docMtl.getPatientMtl()==null)
		{
			log.debug("---------------mmol/l-----");
			String xp = "childTs/childTs[childTs/mtlO/unit='ml']/childTs/childTs[mtlO/unit='mmol/l']";
			Iterator<Pointer> iteratePointers = jxpContext.newContext(docMtl.getDocT())
			.iteratePointers(xp);
			log.debug("---------------mmol/l-----"+iteratePointers);
			while (iteratePointers.hasNext()) {
				Tree doseT = (Tree) ((Pointer) iteratePointers.next()).getValue();
				Dose doseO=(Dose) doseT.getMtlO();
				Dose dose1O=(Dose)JXPathContext.newContext(jxpContext, doseT.getParentT().getParentT())
				.getValue("childTs/mtlO[unit='ml']");
				Float l = dose1O.getValue();
				if("ml".equals(dose1O.getUnit())) l/=1000;
				Integer mmol2l = doseO.getValue().intValue();
				float mmol = mmol2l*l;
				int mmolI = (int) mmol;
//				doseO.setCalcDoseR(""+mmol);
//				schemaMtl.addCalcDoseR(doseT,""+mmol);
				docMtl.addCalcDoseR(doseT,""+mmolI);
				log.debug(doseT);
			}
		}else{
			Integer idProtocol = docMtl.getDocT().getDocT().getIdClass();
			Tree conceptT = em.find(Tree.class, idProtocol);
			log.debug(conceptT);
			Integer conceptFolderId = conceptT.getParentT().getId();
			Folder conceptFolderO = em.find(Folder.class, conceptFolderId);
			log.debug(conceptFolderO);
			docMtl.setConceptFolderO(conceptFolderO);
		}
		docMtl.getPlan();
		docMtl.getFootnotesM();
//		docMtl.getDayIntensiv(); // goto see HourPlan
		if("info".equals((String) owsSession.getRequest().getParameter("part"))){
			docMtl.getCalcTs();
		}
		if("docStatus".equals(owsSession.getRequest().getParameter("part"))){
			UserMtl makeUserMtl = makeUserMtl();
		}
		log.debug("test");
		log.debug(owsSession.getSchemaPart());
		if("changelog".equals(owsSession.getSchemaPart()))
		{
			
//			List<Dose> tL = em.createQuery("SELECT t FROM Dose t WHERE t.value = :value AND t.unit=:unit AND t.app=:app")
//			List<Map<Integer, Contactperson>> rmap = em.createQuery("SELECT doct.id, c FROM tree doct join history h join owuser o, tree owusert, " +
//											"contactperson c "+
//											"WHERE doct.id=h.idhistory AND idauthor=idowuser "+
//											"AND owusert.id=idowuser AND owusert.did=c.idcontactperson "+
//											"AND :docid in (doct.id, doct.iddoc) ORDER BY mdate DESC ")
//					.setParameter("docid",	docMtl.getDocT().getId()).getResultList();
//					
//			log.debug("makeSchemaMtl:rmap"+rmap);
			
			setHistoryM(docMtl);
		}
		
		String idt=owsSession.getRequest().getParameter("idt");
		log.debug(idt);
		if(null!=idt&&idt.length()>0){
			int idti = Integer.parseInt(idt);
			log.debug(idti);
			docMtl.setIdt(idti);
		}
		log.debug(docMtl.getIdt());
//		schemaMtl.makeDose2Tablete(schemaMtl.getDocT().getId());
		
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()
				+ ":" +docMtl.getDocT().getId() +":"+docMtl.getClassM().size()+"/" +docMtl.getMsCnt() 
				+ "/"+(System.currentTimeMillis()-sT));
		setSessionStationM();
		//owsSession.setDocMtl(docMtl);
		
//		Date time = Calendar.getInstance().getTime();
//		Session currentSession = getHibernateSessionFactory().getObject().getCurrentSession();
//		log.debug(currentSession);
		return docMtl;
	}

	private void findTherapyChange(PatientMtl docMtl) {
		Integer patientId = docMtl.getDocT().getId();
		String sql = "SELECT t3.ref AS t3ref, d1.*,d2.drug,dose.* " +
		" FROM tree t1,tree t2,tree t3,tree t33, dose,tree t22, drug d1,drug d2 " +
		" WHERE t1.did=? AND t1.id=t2.did AND t2.id=t3.did " +
		" AND t33.id=t3.ref AND t22.id=t33.did AND t22.idclass=d1.iddrug " +
		" AND d1.idgeneric=d2.iddrug AND t33.idclass=iddose ";
		Map<Integer, Map<String, Object>> tcMap = new HashMap<Integer, Map<String,Object>>();
		List<Map<String, Object>> tcDrugDose = simpleJdbc.queryForList(sql, patientId);
		log.debug(sql+"\n--"+patientId);
		for (Map<String, Object> map : tcDrugDose) {
			Integer t3ref=(Integer) map.get("t3ref");
			tcMap.put(t3ref,map);
		}
		docMtl.setTherapyChange(tcMap);
	}

	private void setProtocolFolder(TreeManager docMtl, Tree protocolT) {
		docMtl.addClassM(protocolT, em);
		Tree folderT = protocolT.getParentT();
		docMtl.addClassM(folderT, em);
		log.debug(folderT);
		if(folderT.getMtlO() instanceof Patient){
			folderT = folderT.getParentT();
		}
		log.debug(folderT);
		docMtl.addClassM(folderT, em);
	}

	private void setCopyHistory(SchemaMtl schemaMtl) {
		Tree parentT = schemaMtl.getDocT().getParentT();
		if("pvariable".equals(parentT.getTabName())){
			History find = em.find(History.class, parentT.getId());
			Owuser owUser = em.find(Owuser.class, find.getIdauthor());
			schemaMtl.setCopyUser(owUser);
		}
	}

//	private void setGeneric(SchemaMtl schemaMtl) {
//		String sql = "SELECT d FROM Tree t1,Tree t2,Drug d,Tree t3 " +
//		" WHERE t1.id=t2.parentT.id AND t2.idClass=d.id " +
//		" AND t3.idClass=d.generic.id AND t1.id in (109563,229703)" +
//		" AND t3.docT.id="+schemaMtl.getDocT().getId();
//		List<Drug> ol = em.createQuery(sql).getResultList();
//		//log.debug(sql+ol.size());
//		for (Drug drug : ol)
//			schemaMtl.getDrugM().put(drug.getGeneric().getId(), drug);
//	}

	private void initRule(SchemaMtl docMtl) {
		if(null==docMtl.getPatientMtl())
			return;
		if(null!=docMtl.getInputPauseForInitT())
		{
			Tree inputPauseForInitT = docMtl.getInputPauseForInitT();
			Tree rootIf = docMtl.ancestor(inputPauseForInitT,"rootIf");
			log.debug("--------------- "+rootIf);
			if(rootIf.getDocT()!=docMtl.getDocT())
				for (Tree drugT : docMtl.getGrule().keySet())
					for (Tree ifT : docMtl.getGrule().get(drugT)) 
						if(rootIf==ifT)
					{
						log.debug("--------------- "+drugT);
						String sql = "SELECT mdate,duration,abs FROM tree patientT" +
" ,tree schemaT JOIN tree drugT ON schemaT.ref=drugT.iddoc AND drugT.idclass=?" +
" ,tree ofDateT, pvariable,history" +
" ,task" +
" ,tree dayT, day" +
" WHERE patientT.id=schemaT.did AND patientT.id=?" +
" AND schemaT.ref!=?" +
" AND schemaT.id=ofDateT.did AND ofDateT.idclass=idpvariable AND pvariable='ofDate' AND ofDateT.id=idhistory" +
" AND schemaT.idClass=idtask" +
" AND drugT.id=dayT.did AND dayT.idclass=idday" +
" ORDER BY mdate DESC";
//log.debug(sql.replaceFirst("\\?", ""+drugT.getIdClass())
//		.replaceFirst("\\?", ""+docMtl.getPatientMtl().getDocT().getId()));
						List<Map<String, Object>> drugInputL = simpleJdbc.queryForList(sql
						, drugT.getIdClass()
						, docMtl.getPatientMtl().getDocT().getId()
						, docMtl.getDocT().getId()
						);
						if(drugInputL.size()>0){
							Timestamp sqlTs = (Timestamp) drugInputL.get(0).get("mdate");
							String absDayVal = (String) drugInputL.get(0).get("abs");
							docMtl.setInputPauseDateTime(sqlTs,absDayVal);
						}
						docMtl.getInputPauseWeek();
					}
		}
	}

	private String sqlDrugRuleForSchema = 
	" SELECT drugExprT FROM Tree schemaDrugT,Drug d, Tree drugExprT,Expr e " +
	" WHERE drugExprT.parentT.id=d.generic.id AND drugExprT.idClass=e.id AND e.expr='if' AND " +
	" schemaDrugT.idClass=iddrug AND schemaDrugT.docT.id=:iddoc ";
	private void setDrugRule(SchemaMtl docMtl) {
		List<Tree> drugRuleL = em.createQuery(sqlDrugRuleForSchema)
				.setParameter("iddoc", docMtl.getDocT().getId())
				.getResultList();
		for (Tree exprT : drugRuleL) {
			Integer idGeneric = exprT.getDocT().getId();
			docMtl.addClass(exprT,idGeneric,em);
			log.debug(exprT);
			for (Tree drugInSchemaT : docMtl.getIdClass2TreeMS().get(idGeneric))
				docMtl.addGrule(drugInSchemaT, exprT);
		}
	}
	private void setGRule(SchemaMtl schemaMtl) {
		String sql = " SELECT t1.id AS idNode ,t3.id AS idExpr, t2.iddoc AS idDoc "+
		" FROM tree t1, dose,tree t2, tree t3,expr"+
		" WHERE t1.iddoc=? AND t1.idclass=iddose AND t3.did=t2.id AND t3.idclass=idexpr"+
		" AND t1.idclass=t2.idclass  AND t2.iddoc IN "+
		" (SELECT t3.id FROM tree t1,tree t2,tree t3,task ta1 "+
		" WHERE t1.id=? AND t2.id=t1.idclass AND t3.iddoc=t2.iddoc AND t3.id=ta1.idtask " +
		" AND ta1.type='general') ";
		schemaMtl.setGRule(sql,simpleJdbc,em);
//		String sql = " SELECT t1.id AS idNode ,t3.id AS idExpr FROM tree t1,tree t2, tree t3 " +
//		" WHERE t1.iddoc=? AND t3.did=t2.id AND t3.tab_name='expr' AND t1.idclass=t2.idclass AND t1.tab_name='dose' " +
//		" AND t2.iddoc IN (SELECT t3.id FROM tree t1,tree t2,tree t3,task ta1 " +
//		" WHERE t1.id=? AND t2.id=t1.idclass AND t3.iddoc=t2.iddoc AND t3.id=ta1.idtask AND ta1.type='general') ";
//		sql=" SELECT t2.id AS idNode, t4.id AS idExpr" +
//		" FROM tree t1,tree t2,tree t3,tree t4 " +
//		" WHERE t2.did=? AND t3.did=t1.id AND t3.tab_name='dose' " +
//		" AND t4.did=t3.id AND t4.tab_name='expr' AND t2.idclass=t1.idclass " +
//		" AND t1.did IN (" +
//		" SELECT t3.id FROM tree t1,tree t2,tree t3,task ta3 " +
//		" WHERE t2.id=t1.idclass " +
//		" AND t3.did=t2.did AND t3.id=ta3.idtask AND ta3.type='general'" +
//		" AND t1.id=?" +
//		" )";
		/*
		sql=" SELECT t2.id AS idNode, t4.id AS idExpr" +
				" FROM tree t1,tree t2,tree t3,tree t4 " +
				" WHERE t2.did=? AND t3.did=t1.id AND t3.tab_name='dose' " +
				" AND t4.did=t3.id AND t4.tab_name='expr' AND t2.idclass=t1.idclass " +
				" AND t1.did IN (" +
				" SELECT t3.id FROM tree t1,tree t2,tree t3,task ta3 " +
				" WHERE t2.id=t1.idclass " +
				" AND t3.did=t2.did AND t3.id=ta3.idtask AND ta3.type='general'" +
				" AND t1.id=?" +
				" )";
		schemaMtl.setGRule(sql,simpleJdbc,em);
		 * */
	}
	private void makePatientMtl(TreeManager docMtl, Tree patientT) {
		PatientMtl patientMtl = new PatientMtl(patientT);
		docMtl.setPatientMtl(patientMtl);
		patientMtl.addClass(patientT,patientT.getId(),em);
		patientMtl.calcBsa();
	}
	private void makePatientMtl_depr(TreeManager docMtl, Tree patientT) {
		PatientMtl patientM = new PatientMtl(patientT);
		docMtl.setPatientMtl(patientM);
//		patientM.addClass(patientT,patientT.getId(),em);
		log.debug("------------------"+patientT);
		docMtl.addClass(patientT,patientT.getId(),em);
		log.debug("------------------"+patientM.getClassM());
		log.debug("------------------"+docMtl.getClassM());
		patientM.getClassM().putAll(docMtl.getClassM());
		log.debug("------------------"+patientM.getClassM());
		patientM.calcBsa();
//		docMtl.addClassM(patientT,em);
//		schemaM.getPlan();//Calc times.
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void reviseDose_depr(SchemaMtl schemaMtl)
	{
		log.debug("----------------");
		Dose
		doseC		= (Dose) schemaMtl.getClassM().get(schemaMtl.getEditNodeT().getIdClass()),
		editDoseC	= schemaMtl.getEditDoseC();
		if(editDoseC.getValue()==null&&editDoseC.getUnit()==null&&editDoseC.getApp()==null&&editDoseC.getPro()==null)return;
		log.debug("----------------"+doseC);
		log.debug("----------------"+editDoseC);
		if	(doseC==null
			||	!(editDoseC.getValue().equals(doseC.getValue())
				&&editDoseC.getUnit().equals(doseC.getUnit())
				&&editDoseC.getApp().equals(doseC.getApp())
				)
			)
		{
			List<Dose> tL = em.createQuery("SELECT t FROM Dose t WHERE t.value = :value AND t.unit=:unit AND t.app=:app")
			.setParameter("value",	editDoseC.getValue())
			.setParameter("unit",	editDoseC.getUnit())
			.setParameter("app",	editDoseC.getApp())
			.getResultList();
			if(tL.size()>0)
			{
				schemaMtl.setEditNodeClass(tL.get(0));
			}else{
				//TODO in SQL
				if(editDoseC.getPro()==null)
					editDoseC.setPro("");
				if(editDoseC.getType()==null)
					editDoseC.setType("p");
//				persistMtlObject(schemaMtl, editDoseC);
				persistMtlObject( editDoseC);
			}
		}
	}
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List appL(App editAppC) {
		List tL = em.createQuery("SELECT t FROM App t WHERE  t.unit=:unit AND t.appapp=:appapp"
		+" AND t.fdr is null"
		+" AND t.h24 is null"
		+" AND t.type='a'"
		)
		.setParameter("unit",	editAppC.getUnit())
		.setParameter("appapp",	editAppC.getAppapp())
		.getResultList();
		return tL;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Pvariable getPvariable(String pvariable, String pvalue, String unit) {
		log.debug("--");
		String sql = "SELECT d FROM Pvariable d WHERE d.pvariable= :pvariable AND d.pvalue=:pvalue AND d.unit=:unit ";
		Pvariable ofDateC=null;
		log.debug(pvariable+" "+pvalue+" "+unit);
		log.debug(sql);
		List<Pvariable> dL = em.createQuery(sql)
		.setParameter("pvariable",	pvariable)
		.setParameter("pvalue",	pvalue)
		.setParameter("unit",	unit)
		.getResultList();
		if(dL.size()>0)
		{
			ofDateC=dL.get(0);
		}
		return ofDateC;
	}
	@Transactional(readOnly = true)
	public List pvL(Pvariable pvC) {
		List dL = em.createQuery("SELECT d FROM Pvariable d WHERE d.pvariable=:pvariable AND d.pvalue=:pvalue AND d.unit=:unit")
		.setParameter("pvariable",	pvC.getPvariable())
		.setParameter("pvalue",		pvC.getPvalue())
		.setParameter("unit",		pvC.getUnit())
		.getResultList();
		return dL;
	}
	@Transactional(readOnly = true)
	public List taskL(Task editTaskC) {
		log.debug("---------------");
		if(editTaskC==null)return null;
		log.debug(editTaskC);
		List dL ;
		if(editTaskC.getType()==null){
			dL = em.createQuery("SELECT d FROM Task d WHERE d.task=:task AND d.type is null")
			.setParameter("task",	editTaskC.getTask())
			.getResultList();
		}else{
			dL = em.createQuery("SELECT d FROM Task d WHERE d.task=:task AND d.type=:type")
			.setParameter("task",	editTaskC.getTask())
			.setParameter("type",	editTaskC.getType())
			.getResultList();
		}
		return dL;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List dayL(Day editDayC) {
		List dL = em.createQuery("SELECT d FROM Day d WHERE d.abs=:abs AND d.newtype=:newtype")
		.setParameter("abs",		editDayC.getAbs())
		.setParameter("newtype",	editDayC.getNewtype())
		.getResultList();
		return dL;
	}
	
	@Transactional(readOnly = true)
	public List armL(Arm s) {
		List dL = em.createQuery("SELECT d FROM Arm d WHERE d.arm=:arm ")
		.setParameter("arm",		s.getArm())
		.getResultList();
		return dL;
	}
	@Transactional(readOnly = true)
	public Labor seek(Labor s) {
		Labor dbS=null;
		return dbS;
	}
	@Transactional(readOnly = true)
	public Symptom seek(Symptom s) {
		log.debug(s);
		Symptom dbS=null;
		List sL = symptomL(s);
		log.debug(sL);
		if(sL.size()>0)
			dbS=(Symptom) sL.get(0);
		log.debug(dbS);
		return dbS;
	}
	
	@Transactional(readOnly = true)
	public List symptomL(Symptom s) {
		return em.createQuery("SELECT d FROM Symptom d WHERE d.symptom=:symptom ")
		.setParameter("symptom",		s.getSymptom())
		.getResultList();
	}
	@SuppressWarnings("unchecked")
//	@Transactional(readOnly = true)
	public Labor seekLabor(Labor lab){
		String sps = " | ";
		if(lab.getLabor().contains(sps)){
			String[] split = lab.getLabor().split(sps);
			lab.setLabor(split[0]);
			lab.setUnit(split[2]);
		}
		List<Labor> laborCL = laborL(lab);
		
		Labor laborC=null;
		if(laborCL.size()>0)
			laborC=laborCL.get(0);
		log.debug("------seekLabor-----------------"+laborCL);
		log.debug("------seekLabor-----------------"+laborC);
		return laborC;
	}
	
	@SuppressWarnings("unchecked")
//	@Transactional(readOnly = true)
	public Finding seekFinding(Finding lab){
		String sps = " | ";
		if(lab.getFinding().contains(sps)){
			String[] split = lab.getFinding().split(sps);
			lab.setFinding(split[0]);
			lab.setUnit(split[2]);
		}
		List<Finding> findingCL = findingL(lab);
		
		Finding findingC=null;
		if(findingCL.size()>0)
			findingC=findingCL.get(0);
		log.debug("------seekLabor-----------------"+findingCL);
		log.debug("------seekLabor-----------------"+findingC);
		return findingC;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List laborL(Labor l) {
		List dL = em.createQuery("SELECT d FROM Labor d WHERE d.labor=:labor AND d.unit != '' ")
		.setParameter("labor",		l.getLabor())
		.getResultList();
		return dL;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List findingL(Finding l) {
		List dL = em.createQuery("SELECT d FROM Finding d WHERE d.finding=:finding")
		.setParameter("finding",		l.getFinding())
		.getResultList();
		return dL;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List timesL(Times editTimesC) {
		log.debug("---"+editTimesC);
		log.debug("---"+editTimesC.getRelunit());
		List tL;
		if(editTimesC.hasAbs()){
			log.debug("---"+editTimesC);
			tL = em.createQuery("SELECT t FROM Times t WHERE t.abs = :abs " 
//					+" AND t.apporder is null"
//					+" AND t.visual is null"
//					+" AND t.relunit is null"
//					+" AND t.relvalue is null"
					)
					.setParameter("abs", editTimesC.getAbs())
					.getResultList();
		}else{
			log.debug("---"+editTimesC);
			String sql = "SELECT t FROM Times t WHERE "
				+" t.relunit = :relunit "
				+"AND t.apporder = :apporder "
				+"AND t.visual = :visual "
				+"AND t.relvalue = :relvalue "
//				+"AND t.abs is null "
				;
			tL = em.createQuery(sql
			)
			.setParameter("relunit", editTimesC.getRelunit())
			.setParameter("apporder", editTimesC.getApporder())
			.setParameter("visual", editTimesC.getVisual())
			.setParameter("relvalue", editTimesC.getRelvalue())
			.getResultList();
		}
		log.debug("---"+tL.size());
		log.debug("---"+tL);
		return tL;
	}
	
	/****************************************************
	 * 
	 * REVISE SECTION
	 * 
	 ****************************************************/
	public void reviseMObject(MObject editObjM, DrugMtl drugMtl) {
		if(editObjM.getId()==null) editObjM.setId(drugMtl.nextCurrentId());
		MObject oldObjM = drugMtl.getClassM().get(drugMtl.getEditNodeT().getIdClass());
		drugMtl.registerNewObj(editObjM,oldObjM);
		if(!drugMtl.getClassM().containsKey(editObjM.getId())) drugMtl.getClassM().put(editObjM.getId(), editObjM);
	}
	
	//---used in showFlow but not in drug-flow, obsolete
	public void reviseMObject(MObject editObjM, SchemaMtl schemaMtl) {
		if(editObjM.getId()==null) editObjM.setId(schemaMtl.nextCurrentId());
		MObject oldObjM = schemaMtl.getClassM().get(schemaMtl.getEditNodeT().getIdClass());
		schemaMtl.registerNewObj(editObjM,oldObjM);
		if(!schemaMtl.getClassM().containsKey(editObjM.getId())) schemaMtl.getClassM().put(editObjM.getId(), editObjM);
	}
	private void reviseMObject(MObject editObjM, List tL, SchemaMtl schemaMtl) {
		log.debug(editObjM);
		MObject oldObjM = schemaMtl.getClassM().get(schemaMtl.getEditNodeT().getIdClass());
		log.debug(oldObjM);
		if(editObjM.compareTo(oldObjM)!=0) {
			log.debug("----");
			editObjM = schemaMtl.eqOld(editObjM, tL, schemaMtl.getClassM());
			schemaMtl.registerNewObj(editObjM, oldObjM);
			schemaMtl.nextCurrentId();
			log.debug("----"+schemaMtl.getEditNodeT());
			log.debug("----"+schemaMtl.getClassM().get(editObjM.getId()));
		}
	}
	public void reviseTimes_depr(SchemaMtl schemaMtl) {
		Times etC = schemaMtl.getEditTimesC();
		log.debug("-----------------"+etC);
		Tree editedTimesT = schemaMtl.getEditNodeT();
		log.debug("----"+editedTimesT);
		if(etC.hasAbs()){
			log.debug("-----------------");
			editedTimesT.setRef(null);
			reviseMObject(etC, timesL(etC), schemaMtl);
			log.debug("----"+editedTimesT);
		}else if(editedTimesT.hasRef()){
			log.debug("-----------------");
			reviseMObject(etC, timesL(etC), schemaMtl);
		}else{
			log.debug("-----------------");
//			schemaMtl.removeOldEditObj(schemaMtl.getClassM().get(editedTimesT.getIdClass()));
			editedTimesT.setIdClass(null);
			editedTimesT.setRef(null);
		}
		log.debug("-----------------");
		log.debug("----"+schemaMtl.getEditNodeT());
	}
	@Autowired @Qualifier("timesCreator") DbTimesCreator timesCreator;
	public void reviseTimes(DrugMtl docMtl) {
//		public void reviseTimes(SchemaMtl schemaMtl) {
		log.debug("--1-------");
		Tree editedTimesT = docMtl.getEditNodeT();
		log.debug(editedTimesT);
		Times editTimesC = docMtl.getEditTimesC();
		log.debug(editTimesC);
		log.debug(editTimesC.hasApporder());
		Times timesC = null;
		log.debug("--2-------");
		if(editedTimesT.hasRef()){
			log.debug("--3-------");
			log.debug(editTimesC.hasRelvalue());
			if(!editTimesC.hasRelvalue()){
				editTimesC.setRelvalue(1);
				editTimesC.setRelunit("S");
			}
			editTimesC.setAbs("");
			timesC = (Times) timesCreator.setMtlO(editTimesC).build();
		}else{
			log.debug("----4-----");
			editedTimesT.setRef(null);
			if(!editTimesC.hasAbs()){
				log.debug("----4--1---");
				editedTimesT.setMtlO(null);
				docMtl.nextCurrentId();
			}else{
				log.debug("----4--2---");
				timesC = (Times) timesCreator.setMtlO(editTimesC).build();
				log.debug("----4--3---");
			}
		}
		docMtl.nextCurrentId();
		log.debug("----5-----");
		log.debug(editTimesC);
		log.debug(timesC);
		log.debug(editedTimesT);
		docMtl.addMObject(timesC, editedTimesT);
		log.debug(editedTimesT);
		docMtl.addRefOrAbs(editedTimesT);
		/*
		schemaMtl.addEditMObject(timesC);
		if(!editedTimesT.hasRef()){
			editedTimesT.setRef(null);
			if(timesC==null||!timesC.hasAbs()){
				editedTimesT.setIdClass(null);
				schemaMtl.nextCurrentId();
			}
		}
		 * */
		log.debug("----"+docMtl.getEditNodeT());
		docMtl.setPlanToNull();
		docMtl.getPlan();
	}
	public void reviseDay(SchemaMtl schemaMtl) {
		Day editDayC = schemaMtl.getEditDayC();
		log.debug(editDayC);
		MObject dayT = dayCreator.setMtlO(editDayC).build();
		log.debug(dayT);
		schemaMtl.addEditMObject(dayT);
	}
	public void reviseSymptom(DrugMtl drugMtl) {
		log.debug("------------");
//		public void reviseSymptom(SchemaMtl schemaMtl) {
		drugMtl.addEditMObject(symptomCreator.setSymptom(drugMtl.getEditSymptomC()).build());
		log.debug("------------");
	}
	public void newNotice(DrugMtl docMtl) {
		Integer idt = owsSession.getRqIntParam("idt");
		docMtl.setIdt(idt);
		docMtl.setEditNodeT();
		Tree doseNoticeParentT = docMtl.getDoseNoticeParentT();
		log.debug(doseNoticeParentT);
		Tree noticeT = nodeCreator.setTagName("notice").setTreeManager(docMtl).setParentT(doseNoticeParentT).addChild();
		log.debug(noticeT);
		docMtl.setIdt(noticeT.getId());
	}

	@Autowired @Qualifier("dayCreator")			DbDayCreator		dayCreator;
	@Autowired @Qualifier("symptomCreator")		DbSymptomCreator	symptomCreator;
	@Autowired @Qualifier("dayTimesCreator")	TaskNodeCreator		dayTimesCreator;
	
	@Autowired @Qualifier("drugDrugCreator")	TaskNodeCreator		drugDrugCreator;

	public void addDDrugDose(DrugMtl docMtl) {
		log.debug(1);
		String ddrugId = owsSession.getRequest().getParameter("ddrugId");
		log.debug("ddrugId="+ddrugId);
		String doseId = owsSession.getRequest().getParameter("doseId");
		log.debug("doseId="+doseId);
		Tree drugT = docMtl.getEditNodeT();
		if("dose".equals(drugT.getTabName()))
			drugT=drugT.getParentT();
		log.debug(drugT);
		Tree ddrugT = drugDrugCreator.setTreeManager(docMtl).setParentT(drugT).addChild();
		log.debug(ddrugT);
		int idDDrug = Integer.parseInt(ddrugId);
		ddrugT.setIdClass(idDDrug);
		docMtl.addClassM(ddrugT, em);
		log.debug(ddrugT);
		Tree drugDoseT = docMtl.getDrugDoseT(ddrugT);
		int idDose= Integer.parseInt(doseId);
		log.debug(drugDoseT);
		drugDoseT.setIdClass(idDose);
		docMtl.addClassM(drugDoseT, em);
		log.debug(drugDoseT);
	}
	public void reviseNotice(TreeManager docMtl) {
		Tree editNodeT = docMtl.getEditNodeT();
		if(null==editNodeT)
			return;
//		public void reviseNotice(DrugMtl docMtl) {
		Notice editNoticeC = correcturEditNoticeO(docMtl);
		DrugMtl drugMtl = null;
		if(docMtl instanceof DrugMtl)
			drugMtl = (DrugMtl) docMtl;
		log.debug(editNodeT);
		log.debug(editNoticeC);
		if(!editNoticeC.hasNotice()
			&& !editNoticeC.hasType()
			&& "task".equals(editNodeT.getParentT().getTabName())
		){
			if(drugMtl!=null)
				drugMtl.deleteNode(editNodeT);
		}else if(!editNoticeC.hasNotice()&&!editNoticeC.hasType()){
			if(drugMtl!=null)
				drugMtl.deleteNode(editNodeT);
		}else{
			docMtl.addEditMObject(noticeCreator.setMtlO(editNoticeC).build());
		}
	}

	private Notice correcturEditNoticeO(TreeManager docMtl) {
		Notice editNoticeC = docMtl.getEditNoticeC();
		log.debug(editNoticeC);
		if(editNoticeC.hasNotice())
			editNoticeC.setNotice(editNoticeC.getNotice()
				.replace("<br _moz_editor_bogus_node=\"TRUE\" />", "").trim());
		if("labor".equals(editNoticeC.getType())){
//			SchemaMtl schemaMtl=(SchemaMtl)docMtl;
			DrugMtl schemaMtl=(DrugMtl)docMtl;
			log.debug(schemaMtl.getLaborChain());
			if(schemaMtl.getLaborChain()!=null&&schemaMtl.getLaborChain().length()>0){
				editNoticeC.setNotice(
					schemaMtl.getLaborChain()+
					SchemaMtl.NoticeLaborExtraDivider+
					editNoticeC.getNotice()
				);
			}
		}
		return editNoticeC;
	}
	@Autowired @Qualifier("appCreator") DbAppCreator appCreator;
	public void reviseApp(DrugMtl docMtl){
		App editAppC = docMtl.getEditAppC();
		log.debug(editAppC);
		MObject build = appCreator.setMtlO(editAppC).build();
		docMtl.addEditMObject(build);
	}
	public void addApp(DrugMtl docMtl) {
		Tree drugT = docMtl.getEditNodeT();
		if(docMtl.isDoseO(drugT))
			drugT=drugT.getParentT();
		log.debug(drugT);
		Tree newAppT = addNode2edit(docMtl, drugT, "app");
		log.debug(docMtl.getIdt());
		addHistory(newAppT);
	}

	
//	public void thenApp(DrugMtl docMtl){thenNode(docMtl, "app");}
//	public void thenDose(DrugMtl docMtl){thenNode(docMtl, "dose");}

	
	@Autowired @Qualifier("exprCreator")	DbExprCreator		exprCreator;
	@Autowired @Qualifier("treeCreator")	DbNodeCreator		treeCreator;
	
	@Autowired @Qualifier("doseCreator")	DbDoseCreator		doseCreator;
	public void reviseDose(DrugMtl drugMtl){
		Dose editDoseC = drugMtl.getEditDoseC();
		log.debug(editDoseC);
		MObject objM = doseCreator.setMtlO(editDoseC).build();
		log.debug(objM);
		drugMtl.addEditMObject(objM);
	}
	public void reviseDose(SchemaMtl docMtl){
		Dose editDoseC = docMtl.getEditDoseC();
		log.debug("---------"+editDoseC);
		if(editDoseC==null)
			return;
		if(editDoseC.getPro()==null)
			editDoseC.setPro("");
		log.debug("---------");
		log.debug("---------"+docMtl.getTsNr());
		log.debug("---------");
		if(docMtl.isNotChemoDoseMod()){
			log.debug("---------");
			MObject doseO = doseCreator.setMtlO(editDoseC).build();
			log.debug("---------"+doseO);
			docMtl.addEditMObject(doseO);
//			reviseMObject(editDoseC, doseL(editDoseC), schemaMtl);
		}else{
			log.debug("---------");
			reviseDoseMod(docMtl);
		}
		log.debug("---------");
//		docMtl.closeEditId();//step edit dose to edit day not work with this
	}
	/*
	private void reviseDoseMod_depr(SchemaMtl schemaMtl) {
		log.debug("-----");
//		log.debug("-----"+dL);
		log.debug("-----"+schemaMtl.getDoseModType());
		log.debug("-----"+schemaMtl.getProcent());
//		log.debug("-----"+editDoseC);
//		log.debug("-----"+chemoPcDoseC);
		//Tree patientDoseProcentT = schemaMtl.patientDoseProcentTM.get(getEditNodeT());
		Tree patientDoseProcentT = schemaMtl.getDosePvMod().get(schemaMtl.getEditNodeT());
//		Tree patientDoseProcentDoseT = patientDoseProcentDoseTM.get(schemaMtl.getEditNodeT());
		Tree patientDoseProcentDoseT = schemaMtl.getDoseMod().get(schemaMtl.getEditNodeT());
		if(patientDoseProcentT==null){
			createPatientDoseProcentT(schemaMtl);
			patientDoseProcentT=schemaMtl.getDosePvMod().get(schemaMtl.getEditNodeT());
		}
//		if(doseModType==null){
		doseMod2type(schemaMtl, patientDoseProcentT, patientDoseProcentDoseT);
		log.debug("-----"+schemaMtl.getTestT());
	}
	 * */

//	private void reviseDoseMod(List dL) {

	public void createOpenDoseMod(SchemaMtl schemaMtl) {
		if(schemaMtl.getPatientMtl()==null)
			return;
		log.debug("----------------"+schemaMtl.getAction());
		log.debug("----------------"+schemaMtl.getModDay());
		log.debug("----------------"+schemaMtl.getIdt());
		schemaMtl.setEditNodeT();
		log.debug("----------------"+schemaMtl.getEditNodeT());
		Tree drugT = schemaMtl.getEditNodeT().getParentT();
		log.debug("----------------"+drugT);
		List<Tree> dosePvL = new ArrayList<Tree>();
		schemaMtl.patientDoseProcentT(drugT, dosePvL);
		log.debug("----------------"+dosePvL);
		Integer day=0;
		if("modDay".equals(schemaMtl.getAction()))
			day=schemaMtl.getModDay();
//		Tree pDoseProcentT = JsfFunctions.pDoseProcentT(schemaMtl, drugT,day);
		Tree pDoseProcentT = JXPathBean.pDoseProcentT(schemaMtl, drugT,day);
		log.debug("----------------"+pDoseProcentT);
		schemaMtl.openEditDose();
		if(pDoseProcentT==null){
			pDoseProcentT=createPatientDoseProcentT(schemaMtl);
			log.debug("----------------"+pDoseProcentT);
		}
		log.debug("----------------"+pDoseProcentT);
	}
	private void reviseDoseMod(SchemaMtl schemaMtl) {
		log.debug("----------------");
		log.debug("----------------"+schemaMtl.getEditNoticeC());
		log.debug("----------------"+schemaMtl.getAction());
		log.debug("----------------"+schemaMtl.getModDay());
		Integer modDay = 0;
		if("modDay".equals(schemaMtl.getAction())){
			modDay = schemaMtl.getModDay();
		}
		Tree	drugT				= schemaMtl.getEditNodeT().getParentT(),
				pDoseProcentT		= JXPathBean.pDoseProcentT(schemaMtl, drugT, modDay),
				pDoseProcentDoseT	= JXPathBean.pDoseProcentDoseT(pDoseProcentT);
//		pDoseProcentT		= JsfFunctions.pDoseProcentT(schemaMtl, drugT, modDay),
//		pDoseProcentDoseT	= JsfFunctions.pDoseProcentDoseT(pDoseProcentT);
		log.debug(pDoseProcentT);
//		if(pDoseProcentT==null){
//			pDoseProcentT=createPatientDoseProcentT(schemaMtl);
//		}else 
		if("modDay".equals(schemaMtl.getAction())){
//			Integer modDay = schemaMtl.getModDay();
			log.debug(modDay);
			MObject mObject = schemaMtl.getPatientMtl().getClassM().get(pDoseProcentT.getParentT().getIdClass());
			Tree modDrugT = pDoseProcentT.getParentT().getParentT();
			if(!"drug".equals(modDrugT.getTabName()))
				modDrugT=modDrugT.getParentT();
			if(mObject instanceof Day){
				int parseInt = Integer.parseInt(((Day) mObject).getAbs());
				log.debug(parseInt);
				if(parseInt!=modDay){
					pDoseProcentT=pDayDoseProcentT(schemaMtl, modDrugT, doseModDayCreator);
					pDoseProcentDoseT=null;
				}
			}else{
				pDoseProcentT=pDayDoseProcentT(schemaMtl, modDrugT, doseModDayCreator);
				pDoseProcentDoseT=null;
			}
		}
		log.debug("-----------");
		doseMod2type(schemaMtl, pDoseProcentT, pDoseProcentDoseT);
		log.debug("-----------");
		Notice editNoticeC = correcturEditNoticeO(schemaMtl);
		log.debug("----------------"+editNoticeC);
		editNoticeC=(Notice) noticeCreator.setMtlO(editNoticeC).build();
		log.debug("----------------"+editNoticeC);
//		Tree noticeT = pDoseProcentT.getParentT().getParentT().getParentT().getParentT();
//		if("modDay".equals(schemaMtl.getAction()))
//			noticeT=noticeT.getParentT();
		Tree noticeT = schemaMtl.getDoseModNoticeT(pDoseProcentT);

		log.debug("-----------"+noticeT);
		schemaMtl.addMObject(editNoticeC, noticeT);
		log.debug("-----------"+noticeT);
		log.debug("-----------");
	}

	private void doseMod2type(SchemaMtl schemaMtl, Tree patientDoseProcentT, Tree patientDoseProcentDoseT) {
		log.debug("------------"+schemaMtl.getDoseModType());
		if(schemaMtl.getDoseModType()==null){
			log.debug("---dddddddddddd--");
		}else if("%".equals(schemaMtl.getDoseModType())){
			log.debug("------------");
			Pvariable patientDoseProcentC = (Pvariable)pvCreator
			.setPvariable("").setPvalue(schemaMtl.getProcent().toString()).setUnit("%").build();
			if(!patientDoseProcentC.getId().equals(patientDoseProcentT.getIdClass())){
				schemaMtl.nextCurrentId();
			}
			schemaMtl.getPatientMtl().addMObject(patientDoseProcentC, patientDoseProcentT);
			if(patientDoseProcentDoseT!=null){
				schemaMtl.getPatientMtl().getRemoveNodeS().add(patientDoseProcentDoseT);
			}
		}else if("p".equals(schemaMtl.getDoseModType())){
			log.debug("------------");
			schemaMtl.getEditDoseC().setType(schemaMtl.getDoseModType());
			setDoseMod(schemaMtl, patientDoseProcentT, patientDoseProcentDoseT);
		}else if("pc".equals(schemaMtl.getDoseModType())){
			log.debug("------------");
			schemaMtl.getEditDoseC().setType(schemaMtl.getDoseModType());
			schemaMtl.getEditDoseC().setValue(schemaMtl.getChemoPcDoseC().getValue());
			String unit = schemaMtl.getEditDoseC().getUnit();
			if(unit.contains("/"))	schemaMtl.getEditDoseC().setUnit(unit.split("/")[0]);
			setDoseMod(schemaMtl, patientDoseProcentT, patientDoseProcentDoseT);
		}
		log.debug("------------");
	}

	private void setDoseMod(SchemaMtl schemaMtl, Tree patientDoseProcentT, Tree pDoseProcentDoseT) {
		log.debug("----------------"+pDoseProcentDoseT);
		Dose doseC = (Dose) doseCreator.setMtlO(schemaMtl.getEditDoseC()).build();
		log.debug("----------------"+doseC);
		if(pDoseProcentDoseT==null){
			Tree docT = schemaMtl.getPatientMtl().getDocT();
			pDoseProcentDoseT = nodeCreator.setTagName("dose")
			.setTreeManager(schemaMtl).setDocT(docT).setParentT(patientDoseProcentT).addChild();
			log.debug(pDoseProcentDoseT);
		}else if(!pDoseProcentDoseT.getIdClass().equals(doseC.getId())){
			schemaMtl.nextCurrentId();
		}
		schemaMtl.getPatientMtl().addMObject(doseC,pDoseProcentDoseT);
	}
	@Autowired @Qualifier("drugModCreator")		DbNodeCreator drugModCreator;
	@Autowired @Qualifier("doseModTaskCreator")	DbNodeCreator doseModTaskCreator;
	@Autowired @Qualifier("doseModDayCreator")	DbNodeCreator doseModDayCreator;
	public Tree createPatientDoseProcentT(SchemaMtl schemaMtl) {
		Tree doseT = schemaMtl.getEditNodeT();
		log.debug(doseT);
		Tree ntdT = drugModCreator
		.setTreeManager(schemaMtl).setParentT(schemaMtl.getPatientMtl().getDocT())
		.setDocT(schemaMtl.getPatientMtl().getDocT())
		.setRef1(schemaMtl.getDocT().getId())//task
		.setIdclass1(schemaMtl.getDocT().getIdClass())//task
		.setRef2(doseT.getParentT().getId())//drug
		.setIdclass2(doseT.getParentT().getIdClass())//drug
		.addChild();
		log.debug("-----------------");
		Tree modDrugT = drugModCreator.getTree2();
		DbNodeCreator doseModCreator;
		if("modDay".equals(schemaMtl.getAction()))
				doseModCreator=doseModDayCreator;
		else	doseModCreator=doseModTaskCreator;
		log.debug("-----------------");
		pDayDoseProcentT(schemaMtl, modDrugT, doseModCreator);
		return doseModCreator.getTree2();
	}

	private Tree pDayDoseProcentT(SchemaMtl schemaMtl, Tree modDrugT, DbNodeCreator doseModCreator) {
		Tree doseT = schemaMtl.getEditNodeT();
		Tree pvT = schemaMtl.getPvProcent();
		Tree dayDoseModT = doseModCreator.setTreeManager(schemaMtl).setParentT(modDrugT)
		.setDocT(schemaMtl.getPatientMtl().getDocT())
		.setIdclass1(doseT.getIdClass())//dose
		.setRef1(pvT.getId())//notice & dose
		.setIdclass2(pvT.getIdClass())//dose
		.addChild();
		log.debug("-----------------");
		if("modDay".equals(schemaMtl.getAction())){
			Integer md = schemaMtl.getModDay();
			log.debug("-----------------"+md);
			Day day = (Day) dayCreator.setMtlO(new Day()).setDayAbs(md).setDayNewtype("a").build();
//			Day day = (Day) dayCreator.setDayAbs(md).setDayNewtype("a").build();
			schemaMtl.addMObject(day, dayDoseModT);
		}
		return doseModCreator.getTree2();
	}
//	@Autowired @Qualifier("doseModCreator") DbNodeCreator doseModCreator;
//	public Tree createPatientDoseProcentT_depr(SchemaMtl schemaMtl) {
//		Tree doseT = schemaMtl.getEditNodeT();
//		Tree pvT = schemaMtl.getPvProcent();
//		Tree t = doseModCreator
//		.setRef1(pvT.getId())//notice & dose
//		.setRef2(schemaMtl.getDocT().getId())//task
//		.setIdclass1(schemaMtl.getDocT().getIdClass())//task
//		.setIdclass2(doseT.getParentT().getIdClass())//drug
//		.setIdclass3(doseT.getIdClass())//dose
//		.setIdclass4(pvT.getIdClass())//pv%
//		.setTreeManager(schemaMtl)
//		.setParentT(schemaMtl.getPatientMtl().getDocT())
//		.setDocT(schemaMtl.getPatientMtl().getDocT())
//		.addChild();
//		schemaMtl.setTestT(t);
//		log.debug("---------------------"+doseModCreator.getTree1());
////		schemaMtl.getDosePvMod().put(doseT, doseModCreator.getTree1());
//		return t;
//	}
	

	/**************************************************************
	 * 
	 *  /REVISE SECTION
	 *  
	 **************************************************************/

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List noticeL(Notice editNoticeC) {
		return em.createQuery("SELECT n FROM Notice n WHERE n.notice=:notice AND n.type=:type")
		.setParameter("notice",		editNoticeC.getNotice())
		.setParameter("type",		editNoticeC.getType())
		.getResultList();
	}
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List exprL(Expr editExprC) {
		List eL = em.createQuery("SELECT e FROM Expr e WHERE"
				+" e.expr=:expr "
				+"AND e.type=:type "
				+"AND e.unit=:unit "
				+"AND e.value=:value")
		.setParameter("expr",		editExprC.getExpr())
		.setParameter("type",		editExprC.getType())
		.setParameter("unit",		editExprC.getUnit())
		.setParameter("value",		editExprC.getValue())
		.getResultList();
		return eL;
	}
	public void reviseTimes_old(SchemaMtl schemaMtl)
	{
		Times
		timesC		= (Times) schemaMtl.getClassM().get(schemaMtl.getEditNodeT().getIdClass()),
		editTimesC	= schemaMtl.getEditTimesC();
		String abs = editTimesC.getAbs();
		editTimesC.setAbs(abs.replaceAll(" ", ""));
		if(schemaMtl.getEditNodeT().getRef()!=null)
		{
			log.debug("---------ref------------"+timesC);
			log.debug("---------ref------------"+editTimesC);
			if	(timesC==null 
				|| !editTimesC.getRelunit().equals(timesC.getRelunit())
				|| !editTimesC.getRelvalue().equals(timesC.getRelvalue())
				|| !editTimesC.getApporder().equals(timesC.getApporder())
				|| !editTimesC.getVisual().equals(timesC.getVisual())
				)
			{
			log.debug("---------ref------------");
				List<Times> tL = em.createQuery("SELECT t FROM Times t WHERE"
						+" t.relunit = :relunit "
						+"AND t.relvalue = :relvalue "
						+"AND t.apporder= :apporder "
						+"AND t.visual= :visual "
				)
				.setParameter("relunit", editTimesC.getRelunit())
				.setParameter("relvalue", editTimesC.getRelvalue())
				.setParameter("apporder", editTimesC.getApporder())
				.setParameter("visual", editTimesC.getVisual())
				.getResultList();
				change(schemaMtl, editTimesC, tL);
			}
//		}else if(abs!=null){
		}else if(editTimesC.hasAbs()){
//			if(timesC==null || timesC.getAbs()==null || !abs.equals(timesC.getAbs()))
			if(timesC==null || !abs.equals(timesC.getAbs()))
			{
				List<Times> tL = em.createQuery("SELECT t FROM Times t WHERE t.abs = :abs")
				.setParameter("abs", abs)
				.getResultList();
				change(schemaMtl, editTimesC, tL);
			}
		}
	}
	private void change(SchemaMtl schemaMtl, Times editTimesC, List<Times> tL) {
		if(tL.size()>0)
		{
			schemaMtl.setEditNodeClass(tL.get(0));
		}else{
//			persistMtlObject(schemaMtl, editTimesC);
			persistMtlObject( editTimesC);
		}
		em.merge(schemaMtl.getEditNodeT());
	}
	private Tree persist2MtlObject(MObject mtlC) {
		String	name	= mtlC.getClass().getSimpleName().toLowerCase();
		Tree sourceT = setClassT(name);
		mtlC.setId(sourceT.getId());
//		em.flush();//Flush for two some insert in one transaction!
		return sourceT;
	}
	private void persistMtlObjectNative(MObject objC) {
		log.debug(objC);
		String	name	= objC.getClass().getSimpleName().toLowerCase();
		Tree sourceT = setClassT(name);
		objC.setId(sourceT.getId());
		log.debug(objC);
		if(objC instanceof Drug){
			Drug drugC = (Drug) objC;
			drugC.setGeneric(drugC);
			sourceT.setDocT(sourceT);
		}
		log.debug(sourceT);
		insert(sourceT);
		insert(objC);
	}
	private void insert(MObject objC) {
		log.debug(objC);
		String insert = "INSERT INTO ";
		if(objC instanceof Notice)		insert += insertM((Notice) objC);
		else if(objC instanceof Drug)	insert += insertM((Drug) objC);
		else if(objC instanceof Labor)	insert += insertM((Labor) objC);
		else if(objC instanceof Finding)	insert += insertM((Finding) objC);
		else if(objC instanceof Dose)	insert += insertM((Dose) objC);
		else if(objC instanceof Day)	insert += insertM((Day) objC);
		else if(objC instanceof Times)	insert += insertM((Times) objC);
		insert+=") ";
		insert(insert);
	}
	private String insertM(Finding laborC) {
		String insert = "Finding (idfinding,finding,unit) VALUES (";
		insert+=laborC.getId()+", ";
		insert+="'"+laborC.getFinding()+"', ";
		insert+="'"+laborC.getUnit()+"' ";
		return insert;
	}
	private String insertM(Labor laborC) {
		log.debug(laborC);
		String insert = "Labor (idlabor,labor,unit) VALUES (";
		insert+=laborC.getId()+", ";
		insert+="'"+laborC.getLabor()+"', ";
		insert+="'"+laborC.getUnit()+"' ";
		return insert;
	}

	private String insertM(Drug drugC) {
		log.debug(drugC);
		String insert = "Drug (iddrug,drug,idgeneric) VALUES (";
		insert+=drugC.getId()+", ";
		insert+="'"+drugC.getDrug()+"', ";
		insert+=drugC.getGeneric().getId()+" ";
		return insert;
	}

	private String insertM(Times timesC) {
		log.debug(timesC);
		String insert;
		if(timesC.hasAbs()){
			insert = "Times (idtimes,abs) VALUES (";
			insert+=timesC.getId()+", ";
			insert+="'"+timesC.getAbs()+"' ";
		}else{
			insert = "Times (idtimes,apporder,visual,Relunit,Relvalue) VALUES (";
			insert+=timesC.getId()+", ";
			insert+="'"+timesC.getApporder()+"', ";
			insert+=timesC.getVisual()+", ";
			insert+="'"+timesC.getRelunit()+"', ";
			insert+=timesC.getRelvalue()+" ";
		}
		return insert;
	}
	private String insertM(Day dayC) {
		String insert = "Day (idday,abs,newtype) VALUES (";
		insert+=dayC.getId()+", ";
		insert+="'"+dayC.getAbs()+"', ";
		insert+="'"+dayC.getNewtype()+"' ";
		return insert;
	}
	private String insertM(Dose doseC) {
		String insert = "Dose (iddose,value,unit,pro,app,type) VALUES (";
		insert+=doseC.getId()+", ";
		insert+=doseC.getValue()+", ";
		insert+="'"+doseC.getUnit()+"', ";
		if(doseC.getPro()==null){
			insert+="'', ";
		}else{
			insert+="'"+doseC.getPro()+"', ";
		}
		insert+="'"+doseC.getApp()+"', ";
		insert+="'"+doseC.getType()+"' ";
		return insert;
	}
	private String insertM(Notice noticeC) {
		if(noticeC.getNotice()==null)	noticeC.setNotice("");
		if(noticeC.getType()==null)		noticeC.setType("");
		if(noticeC.getB()==null)		noticeC.setB("");
		String insert = "Notice (idnotice,notice,type) VALUES (";
		insert+=noticeC.getId()+", ";
		insert+="'"+noticeC.getNotice()+"', ";
		if(noticeC.getType()==null){
			insert+=" null ";
		}else{
			insert+=" '"+noticeC.getType()+"' ";
		}
		return insert;
	}
	private String insert(History history) {
		String insert = "History (idhistory,mdate,idauthor) VALUES (";
		insert+=history.getIdhistory()+", ";
		insert+=history.getMdate()+", ";
		insert+=history.getIdauthor()+", ";
		return insert;
	}
	private void insert(Tree tree) {
		String insert = "INSERT INTO Tree (id,did,iddoc,tab_name,idclass,sort,ref) VALUES (";
		insert+=tree.getId()+", ";
		insert+=tree.getParentT().getId()+", ";
		if(tree.getDocT()==null){
			insert+=" null , ";
		}else{
			insert+=tree.getDocT().getId()+", ";
		}
		insert+="'"+tree.getTabName()+"', ";
		if(tree.getIdClass()==null){
			insert+=" null , ";
		}else{
			insert+=tree.getIdClass()+", ";
		}
		insert+=tree.getSort()+", ";
		if(tree.getRef()==null){
			insert+=" null  ";
		}else{
			insert+=tree.getRef()+" ";
		}
		insert+=") ";
		insert(insert);
	}

	private void insert(String insert) {
		log.debug(insert);
		int executeUpdate = em.createNativeQuery(insert).executeUpdate();
		log.debug(executeUpdate);
	}

//	private void persistMtlObject(SchemaMtl schemaMtl, MClass mtlC) {
//	private void persistMtlObject(TreeManager schemaMtl, MClass mtlC) {
	
	//create tree obj from name (name=[labor, drug, notice,...])
	
	
	@SuppressWarnings("unchecked")
	public Drug seekDrug(String drug){
		log.debug("------seekDrug-----------------"+drug);
		List<Drug> drugCL = em.createQuery("SELECT c FROM Drug c WHERE c.drug=:drug")
		.setParameter("drug", drug)
		.getResultList();
		Drug drugC=null;
		if(drugCL.size()>0)
			drugC=drugCL.get(0);
		log.debug("------seekDrug-----------------"+drugC);
		return drugC;
	}
	
	public void reviseSourceNative(Map<Integer,MObject> classM, TreeManager schemaMtl){
		for (MObject objM : classM.values()){
			if(objM==null)
				return;
			if(objM.getId()!=null && objM.getId()<TreeManager._5000)
			{
				MObject dbObjectM=seekObjM(objM);
				MObject class2M = objM;
				if(dbObjectM==null){
					persistMtlObjectNative(objM);
				}else{
					class2M=dbObjectM;
				}
//				for (Tree tree : schemaMtl.getClassMUse(objM)){
//					tree.setIdClass(class2M.getId());//replacement to all down
//				}
			}
		}
	}
	public void reviseSource(Map<Integer,MObject> classM, TreeManager schemaMtl){
		for (MObject objM : classM.values())
			if(objM.getId()!=null && objM.getId()<TreeManager._5000)
			{
				log.debug("----qqqqqqqqqqqqqqqqqqqqq------");
				log.debug(objM);
				MObject dbObjectM=seekObjM(objM);
				log.debug(dbObjectM);
				MObject class2M = objM;
				if(dbObjectM==null){
//					persistMtlObject(schemaMtl, classM);
					persistMtlObject(objM);
				}else{
					class2M=dbObjectM;
					log.debug(class2M);
				}
				log.debug("---------"+objM);
//				for (Tree tree : schemaMtl.getClassMUse(objM)){
////					tree.setIdClass(classM.getId());//replacement to all down
//					log.debug(tree.getDocT()==schemaMtl.getDocT());
//					log.debug(tree);
//					tree.setIdClass(class2M.getId());//replacement to all down
//					log.debug(tree+"\n"+tree.getParentT());
//				}
				log.debug("----ddddddddddddddddddddd------");
			}
	}
	
	//seek Obj of class MClass in database and return it if found else return null
	

	@Transactional(readOnly = true)
	public void seekPatient(TreeManager docMtl) {
//		public void seekPatient(SchemaMtl schemaMtl) {
		
		log.debug(docMtl.getEditPatientC());
		String sql = "FROM Patient p ",sql2="";
		int sl = sql.length();
		int fl = docMtl.getEditPatientC().getForename().length();
		if(fl>0){
			sql2+=sql2.length()==0?"":" OR ";
			sql2+=" lower(substring(p.forename,1," +fl +"))='" 
			+docMtl.getEditPatientC().getForename().toLowerCase() +"'";
		}
		int pl = docMtl.getEditPatientC().getPatient().length();
		if(pl>0){
			sql2+=sql2.length()==0?"":" OR ";
			sql2+=" lower(substring(p.patient,1," +pl +"))='" 
			+docMtl.getEditPatientC().getPatient().toLowerCase() +"'";
			if(fl==0){
				sql2+=" OR lower(substring(p.forename,1," +pl +"))='" 
				+docMtl.getEditPatientC().getPatient().toLowerCase() +"'";
			}
		}
		if(docMtl.getEditPatientC().getSex()!=null){
			sql+=sql.length()==sl?" WHERE ":" AND ";
			sql+=" p.sex = '" +docMtl.getEditPatientC().getSex() +"' ";
		}
		if(sql2.length()>0){
			sql+=sql.length()==sl?" WHERE ":" AND ";
			sql+="(" +sql2 +")";
		}
		log.debug(sql);
		docMtl.setPatientL(em.createQuery(sql).setMaxResults(10).getResultList());
	}

	public void addDay(SchemaMtl schemaMtl) {
		Tree drugT = schemaMtl.getEditNodeT().getParentT();
		log.debug(drugT);
		Tree dayT = dayTimesCreator.setTreeManager(schemaMtl).setParentT(drugT).addChild();
		log.debug(dayT);
//		schemaMtl.getDrugDay(drugT).add(dayT);
//		for(Tree t:dayT.getChildTs())
//			schemaMtl.getDayTime(dayT).add(t);
		schemaMtl.setIdt(dayT.getId());
	}

	public void signatureBlockEntry(TreeManager docMtl) {
		docMtl.setEditNoticeC(new Notice());
		Tree editNodeT = docMtl.getEditNodeT();
		if(docMtl.isNoticeO(editNodeT))
		{
			Notice noticeO = docMtl.getNoticeO(editNodeT);
			docMtl.getEditNoticeC().setNotice(noticeO.getNotice());
		}
		
	}
	@Transactional
	public void signatureBlockSave(TreeManager docMtl) {
		Tree editNodeT = docMtl.getEditNodeT();
		if(!docMtl.isNoticeO(editNodeT))
			editNodeT = addChild("notice", docMtl.getDocT());
		Notice editNoticeC = docMtl.getEditNoticeC();
		log.debug(editNoticeC);
		log.debug(editNoticeC.getNotice());
		editNoticeC.setType("signatureBlock");
		editNoticeC=(Notice) noticeCreator.setMtlO(editNoticeC).build();
		editNodeT.setMtlO(editNoticeC);
	}
	
	public void setBlockSupportDrug(DrugMtl drugMtl, TaskNodeCreator taskDrugCreator){
		log.debug("----------");
		Tree blockSupportT=initInsideTask(drugMtl, "support");
		log.debug("----------"+blockSupportT);
		Tree newSupportDrug = taskDrugCreator.setTreeManager(drugMtl).setParentT(blockSupportT).addChild();
		log.debug(newSupportDrug);
//		getDdrug(blockSupportT).add(newSupportDrug);
		drugMtl.setIdt(newSupportDrug.getId());
	}
	
	/**
	 * Insert new drug block in drug document. 
	 * Drug block is schema similar task element with internal drug elements.
	 * @param drugMtl - Work SWF drug object.
	 * @param task - Task field in task table.
	 * @param type - Type field in task table.
	 */
	public void initTaskInsideDrug(DrugMtl drugMtl, String task, String type){
		Task taskO = (Task)taskCreator.setTask(task).setType(type).setTaskvar("").build();
		log.debug(taskO);
		Tree innerTaskT = nodeCreator.setTagName("task")
		.setTreeManager(drugMtl).setParentT(drugMtl.getDocT()).addChild();
		log.debug(innerTaskT);
		
//		if(innerTask!=null)
			drugMtl.addMObject(taskO, innerTaskT);
		for(Tree t:drugMtl.getDocT().getChildTs())
			if(t.getTabName().equals("task"))
				log.debug("--------"+t);
		
		log.debug(innerTaskT);
		log.debug(drugMtl.getIdt());
	}
	@Transactional
	public void saveDose2Tablete(SchemaMtl schemaMtl){
		log.debug("-----------");
		Tree drugT = schemaMtl.getDose2Tablete().getDrugT();
//		Tree doseT = schemaMtl.getDrugDose().get(drugT);
		Tree doseT = JXPathBean.getDose(drugT);
		Map<Integer, List<Integer>> drugMalTabletML = schemaMtl.getDose2Tablete().getDrugMalTabletML(drugT);
		for(Tree t1:doseT.getChildTs()){
			if(schemaMtl.classM(t1) instanceof Expr){
				Expr doseInTabletC=(Expr) schemaMtl.classM(t1);
				if("SteroidDayDose".equals(doseInTabletC.getValue())){
					log.debug("-----------");
					for (Tree t2 : t1.getChildTs()){
						deleteNative(t2);
					}
					for (Integer mal : drugMalTabletML.keySet()) {
						Tree mgProMalT = schemaMtl.addChild(t1, "pvariable", schemaMtl.getDocT(), nextDbid());
						mgProMalT.setSort((long)mal);
						Pvariable pvC = (Pvariable)pvCreator.setPvariable("mgProMal").setPvalue(""+mal).setUnit(""+mal).build();
//						Pvariable pvC = getPvariable("mgProMal",""+mal,""+mal);
//						log.debug(pvC);
//						if(pvC==null){
//							pvC=new Pvariable("mgProMal",""+mal,""+mal);
//							log.debug(pvC);
//							persistMtlObject(pvC);
//							log.debug(pvC);
//						}
						mgProMalT.setIdClass(pvC.getId());
						log.debug(drugMalTabletML);
						List<Integer> list = drugMalTabletML.get(mal);
						for (int i = 0; i < list.size(); i++) {
							Integer malTablete = list.get(i);
							if(malTablete>0){
								Tree tableteT = t1.getParentT().getChildTs().get(i);
								Tree tabletInMalT = copyNode(schemaMtl, mgProMalT, schemaMtl.getDocT(), tableteT);
								//pvariable:55871:pvariable:tabletNum:pvalue:3:unit:
								Tree tabletNumT = schemaMtl.addChild(tabletInMalT, "pvariable", schemaMtl.getDocT(), nextDbid());
//								Pvariable tabletNumC = getPvariable("tabletNum",""+malTablete,"tabletNum");
								Pvariable tabletNumC = (Pvariable)pvCreator
									.setPvariable("tabletNum").setPvalue(""+malTablete).setUnit("tabletNum").build();
								log.debug(tabletNumC);
								if(tabletNumC==null){
									tabletNumC=new Pvariable("tabletNum",""+malTablete,"tabletNum");
									log.debug(tabletNumC);
									persistMtlObject(tabletNumC);
									log.debug(tabletNumC);
								}
								log.debug(tabletNumC.getId());
								tabletNumT.setIdClass(tabletNumC.getId());
							}
						}
					}
				}
			}
		}
		em.merge(schemaMtl.getDocT());
	}
	

	

	
	@Transactional
	public void saveChangePatient(SchemaMtl schemaMtl){
		Patient editPatientC = schemaMtl.getEditPatientC();
		Timestamp t = new Timestamp(schemaMtl.getBdate().getTime());
		editPatientC.setBirthdate(t);
		em.merge(editPatientC);
	}
	@Transactional
	public void saveTubo2Patient(TreeManager docMtl){
		Date bdate = docMtl.getBdate();
		log.debug(bdate);
		String sql = "SELECT p FROM Protocol p where p.protocoltype='tubo'";
		Protocol protocolO = (Protocol) em.createQuery(sql).getSingleResult();
		log.debug(protocolO);
		Tree doc2copyT = readDocT(protocolO.getId());
		log.debug(doc2copyT);
		log.debug(docMtl.getPatientO());
		Tree patientT = em.find(Tree.class, docMtl.getPatientO().getId());
		Tree newDocT = copyNode(docMtl, patientT, patientT, doc2copyT);
		saveOfDate(docMtl, patientT, newDocT);
		em.persist(newDocT);
		docMtl.initOld2newIdM();
		copyChild2Patient(docMtl, doc2copyT, newDocT, newDocT,false);
		Tree startSchemaT =null;
		for(Tree t:newDocT.getChildTs()) if("task".equals(t.getTabName()))
			startSchemaT=t;
		log.debug(startSchemaT);
		doc2copyT = readDocT(startSchemaT.getIdClass());
		Tree newPatientSchemaT = copyNode(docMtl, patientT, patientT, doc2copyT);
		log.debug(newPatientSchemaT);
//		docMtl.addOfDate2(newPatientSchemaT, patientT,nextDbid());
		addOfDate(newPatientSchemaT);
		em.persist(newPatientSchemaT);
		Tree find = em.find(Tree.class, startSchemaT.getId());
		Tree find2 = em.find(Tree.class, newPatientSchemaT.getId());
		find2.setRef(find.getId());
//		newPatientSchemaT.setRef(startSchemaT.getId());
	}
	@Transactional
	public void saveSchema2Patient(SchemaMtl schemaMtl){
//		schemaMtl.saveBeginDate();
		Date bdate2 = schemaMtl.getBdate();
		Timestamp bdate = new Timestamp(bdate2.getTime());
		log.debug("---------------");
		Patient newPatientC = schemaMtl.getEditPatientC();
		Patient editPatientC = newPatientC;
		Tree newPatientT ;
		boolean isNewPatient = false;
		if(editPatientC.getId()!=null&&editPatientC.getId()>TreeManager._5000){
			newPatientT = readDocT(editPatientC.getId());
		}else{
			isNewPatient = true;
			Tree npT = null;
			/*
			Folder npF = schemaMtl.getNewPatientF();
			log.debug(npF);
			Tree folderT=getFolderTree();
			log.debug(folderT);
			for(Tree t:folderT.getDocNodes()){
				if(t.getId().equals(npF.getId())){
					npT=t;
					break;
				}
			}
			 * */
			npT = getFolderNewPatientT();
			log.debug(npT);
			newPatientT = setNewDocT("patient",npT);
//			newPatientT = getSourceFolderT("newPatient");
			log.debug("--------------- "+newPatientT);
			log.debug(newPatientT);
			newPatientC.setId(newPatientT.getId());
			log.debug(newPatientC);
//			em.merge(newPatientT);
			em.persist(newPatientT);
			em.persist(newPatientC);
			log.debug("--------------- "+newPatientT);
		}
		log.debug("---------------"+editPatientC);
		Tree schemaDocT = schemaMtl.getDocT();
		log.debug("----------------"+schemaMtl.getIdc());
		schemaMtl.setIdc(null);
		log.debug("----------------"+schemaMtl.getIdc());
		schemaMtl.initOld2newIdM();
		Tree docProtocolT = schemaDocT.getDocT();
		if(docProtocolT!=null&&docProtocolT.getTabName().equals("protocol")){
			Tree doc2copyT = readDocT(docProtocolT.getId());
			log.debug(doc2copyT);
			Tree newDocT = copyNode(schemaMtl, newPatientT, newPatientT, doc2copyT);
			saveOfDate(schemaMtl, newPatientT, newDocT);
			Tree ddd=correcturUseVarianteSchema(schemaMtl,doc2copyT);
			log.debug("----------------"+ddd);
			copyChild2Patient(schemaMtl, doc2copyT, newDocT, newDocT,false);
			log.debug("----------------");
//			if(isNewPatient){
//				em.merge(newDocT);
//			}else{
//				em.persist(newDocT);
//			}
			em.persist(newDocT);
			log.debug("----------------");
			//getIdc() has newId of first this schema.
			log.debug("----------------"+schemaMtl.getIdc());
			Tree startSchemaT = schemaMtl.getTree().get(schemaMtl.getIdc());
			
			doc2copyT = readDocT(schemaDocT.getId());
			Tree newPatientSchemaT = copyNode(schemaMtl, newPatientT, newPatientT, doc2copyT);
			log.debug(newPatientSchemaT);
			log.debug(startSchemaT);
			newPatientSchemaT.setRef(startSchemaT.getId());
			addOfDate(newPatientSchemaT);
//			schemaMtl.addOfDate2(newPatientSchemaT, newPatientT,nextDbid());
			
			copyChild2Patient(schemaMtl, doc2copyT, startSchemaT, startSchemaT,true);
			log.debug("----------------");
//			if(isNewPatient){
//				em.merge(newPatientSchemaT);
//			}else{
//				em.persist(newPatientSchemaT);
//			}
			em.persist(newPatientSchemaT);
			log.debug("----------------");
//			schemaMtl.updateRef();
			em.merge(newPatientSchemaT);
			
			schemaMtl.updateRef(em);
			
			owsSession.setDocMtl(null);
//			owsSession.getUserCareSession().setDocMtl(null);
			schemaMtl.setIdt(newPatientSchemaT.getRef());
		}else{
			log.debug("------");
			Tree newPatientSchemaT = copyNode(schemaMtl, newPatientT, newPatientT, schemaDocT);
			log.debug("------");
//			schemaMtl.addOfDate2(newPatientSchemaT, newPatientT,nextDbid());
			addOfDate(newPatientSchemaT);
			/*
			log.debug("------");
			 * */
			copyChild2Patient(schemaMtl, schemaDocT, newPatientSchemaT, newPatientSchemaT,true);
			log.debug("------");
			em.persist(newPatientSchemaT);
			owsSession.setDocMtl(null);
//			owsSession.getUserCareSession().setDocMtl(null);
			schemaMtl.setIdt(newPatientSchemaT.getId());
		}
	}

	private void saveOfDate(TreeManager schemaMtl, Tree patientT, Tree newDocT) {
		log.debug(newDocT);
		/*
		Pvariable ofDateFixedC = getPvariable("ofDate","fixed","");
		schemaMtl.setOfDateFixedC(ofDateFixedC);
		Integer idOwuser = owsSession.getUserId(simpleJdbc);
		log.debug("idOwuser="+idOwuser);
		Tree dd = schemaMtl.addOfDate2(newDocT, patientT, nextDbid());
		 */
		Tree dd=addOfDate(newDocT);
		log.debug(dd);
	}

	private Tree correcturUseVarianteSchema(SchemaMtl schemaMtl, Tree docProtocolT) {
		Tree firstSchemaUseT=null;
		log.debug("  schemaMtl.getDocT().getId()="+schemaMtl.getDocT().getId());
		Task taskO = (Task)schemaMtl.getDocT().getMtlO();
		Tree definitionT=null;
		Set<Tree> set = schemaMtl.getIdClass2TreeMS().get(taskO.getId());
		log.debug(set);
		for (Tree tree : set) {
			log.debug(tree);
			if("definition".equals(tree.getParentT().getTabName()))
				definitionT=tree.getParentT();
			else if(firstSchemaUseT==null)
				return tree;
		}
		log.debug(definitionT);
		log.debug(taskO);
		if(taskO.getCyclename()!=null){
			ConceptMtl conceptMtl = makeConceptMtl(docProtocolT);
			for (Tree tree : definitionT.getChildTs()) {
				Task task2O=(Task)tree.getMtlO();
				if(task2O==null)
					return null;
				if(taskO.getCyclename().equals(task2O.getCyclename())){
					log.debug(task2O);
					set = conceptMtl.getIdClass2TreeMS().get(task2O.getId());
					log.debug(set);
					for (Tree tree2 : set) {
						if("definition".equals(tree2.getParentT().getTabName()))
							continue;
						if(firstSchemaUseT==null)
							firstSchemaUseT=tree2;
						tree2.setIdClass(taskO.getId());
					}
					if(firstSchemaUseT!=null)
						return firstSchemaUseT;
				}
			}
		}
		return null;
	}

	private Tree getFolderNewPatientT() {
		Tree npT=testPatientCreator.find();
		return npT;
	}
	private Tree getFolderNewPatientT_depr() {
		String sql="SELECT t2.* FROM Tree t2, Folder f1, Folder f2 " +
				" WHERE f1.folder='patient' and f2.folder='new' " +
				" AND f1.idfolder=f2.fdid AND t2.id=f2.idfolder";
		log.debug("---------------"+sql);
		Tree npT = null;
		List resultList = em.createNativeQuery(sql, Tree.class).getResultList();
		if(resultList.size()>0)
			npT = (Tree) resultList.get(0);
		log.debug(npT);
		return npT;
	}

	private void copyChild(SchemaMtl schemaMtl, Tree parentT2copy, Tree parentNewT, Tree docNewT) {
		if(parentT2copy.getChildTs()!=null)
			for(Tree tree2copy:parentT2copy.getChildTs()){
				Tree newT = copyNode2(schemaMtl, parentNewT, docNewT, tree2copy);
				log.debug(newT);
				copyChild(schemaMtl, tree2copy, newT, docNewT);
			}
	}
//	private void copyChild2Patient(SchemaMtl schemaMtl, Tree parentT2copy, Tree parentNewT, Tree docNewT) {
	

//	private Tree copyNode(SchemaMtl schemaMtl, Tree parentNewT, Tree docNewT, Tree tree2copy) {
	
	private Tree copy(Tree originalT) {
		Tree newT = new Tree();
		newT.setId(nextDbid());
		newT.copyAtt(originalT);
		return null;
	}
	public void copy(UserMtl userMtl, String view) {
		log.debug(1);
		Integer idc=userMtl.getIdc();
		log.debug(idc);
		if(null!=userMtl.getIdClass2copy()){
			log.debug(2);
			Tree find = em.find(Tree.class, userMtl.getIdClass2copy());
			userMtl.addClassM(find, em);
		}
		log.debug(3);
		log.debug("view="+view);
		if("massMaintenance_notice".equals(view)){
			log.debug(4);
//			Integer idc = userMtl.getIdc();
			log.debug("idc="+idc);
			if(null!=idc){
				findTreeWithClass(userMtl, idc);
				userMtl.copy();
			}
		}
		log.debug(111);
		Tree copyNodeT = userMtl.getCopyNodeT();
		log.debug(copyNodeT);
		if(null!=idc&&null!=copyNodeT&&!idc.equals(copyNodeT.getId()))
		{
			copyNodeT=findTreeWithClass(userMtl, idc);
			log.debug(copyNodeT);
			owsSession.setCopyNodeT(copyNodeT);
		}
	}

	private Tree findTreeWithClass(TreeManager docMtl, Integer idc) {
		Tree tree = em.find(Tree.class, idc);
		log.debug("tree="+tree);
		docMtl.addClassM(tree, em);
		log.debug("tree="+tree);
		return tree;
	}

	private void historySchemaT(Timestamp bdate, Tree patientT, TreeManager docM, Tree historyT) {
		addAtt(historyT,"task",patientT, patientT);
		Tree psOfDateT = docM.addOfDate(historyT, patientT,docM);
		psOfDateT.getHistory().setMdate(bdate);
	}
	/**
	 * Change protocol past cycle and application number and schema type.
	 * @param schemaMtl
	 */
	@Transactional
	public void saveCycleNr(SchemaMtl schemaMtl)
	{
		log.debug("----");
		Integer newCycleNr = schemaMtl.getCycleNr();
		Integer newAppNr = schemaMtl.getAppNr();
		Boolean newIsApp = schemaMtl.getIsApp();
//		schemaMtl.calcCycleNr();
		Integer oldCycleNr = schemaMtl.getCycleNr();
		Integer oldAppNr = schemaMtl.getAppNr();
		Boolean oldIsApp = schemaMtl.getIsApp();
		log.debug("----"+oldCycleNr+" to "+newCycleNr);
		log.debug("----"+oldAppNr+" to "+newAppNr);
		log.debug("----"+oldIsApp+" to "+newIsApp);
		log.debug("----");
		Tree protocolT = schemaMtl.getProtocolT();
		Task schemaC = schemaMtl.getSchemaC();
		log.debug("----"+newIsApp+":"+schemaC);
		if(newIsApp){
			schemaMtl.getSchemaC().setType("subchemo");
		}else{
			schemaMtl.getSchemaC().setType("chemo");
		}
		log.debug("----"+newIsApp+":"+schemaC);
		em.merge(schemaMtl.getSchemaC());
		log.debug("----");
		int cycleTaskNr = 0;
		int cycleAppNr = 0;
		int appNr = 0;
		boolean isChemo = !newIsApp;
		boolean isSubChemo = newIsApp;
		if(protocolT!=null){
			Integer //cycleIdClass = schemaMtl.getCycleIdClass();
			cycleIdClass=cycleArmC.getId();
			log.debug("---"+cycleIdClass);
			for(Tree cycleT:protocolT.getChildTs()){
				if("task".equals(cycleT.getTabName())){
					if(schemaC.getId().equals(cycleT.getIdClass()))
						cycleTaskNr++;
					if(cycleTaskNr>newCycleNr)
						delete(cycleT);
				}else if(cycleT.getChildTs()!=null){
					for(Tree cycleArmT:cycleT.getChildTs()){
						if(cycleIdClass.equals(cycleArmT.getIdClass())){
							if(isChemo){
								delete(cycleT);
							}else if(isSubChemo){
								cycleAppNr++;
								if(cycleAppNr>newCycleNr)
									delete(cycleT);
								appNr=0;
								for(Tree schemaT:cycleArmT.getChildTs()){
									if(schemaT.getIdClass()!=null&&schemaT.getIdClass().equals(schemaC.getId())){
										appNr++;
										if(appNr>newAppNr)
											delete(schemaT);
									}
								}
								for (int i = appNr; i < newAppNr; i++) {
									addSchema(schemaC.getId(), cycleArmT, protocolT);
								}
							}
						}
					}
				}
			}
//			for (int i = oldCycleNr; i < newCycleNr; i++) {
				if(isChemo){
					for (int i = cycleTaskNr; i < newCycleNr; i++) {
						addSchema(schemaC.getId(), protocolT, protocolT);
					}
				}else if(isSubChemo){
					for (int i = cycleAppNr; i < newCycleNr; i++) {
						Tree cycleT = addChild("choice",protocolT,protocolT);
						Tree cycleArmT = addChild("studyarm",cycleT,protocolT);
						cycleArmT.setIdClass(cycleIdClass);
						for (int j = 0; j < newAppNr; j++) {
							Tree schemaT = addSchema(schemaC.getId(), cycleArmT, protocolT);
						}
					}
				}
			em.merge(protocolT);
		}
	}

	private Tree addSchema(Integer idClass, Tree parentT, Tree docT) {
		Tree taskT = addChild("task",parentT,docT);
		taskT.setIdClass(idClass);
		return taskT;
	}

	private void delete(Tree tree) {
		em.createNativeQuery("DELETE FROM Tree WHERE id="+tree.getId()).executeUpdate();
	}
	
	@Autowired @Qualifier("simpleJdbc2")	private	SimpleJdbcTemplate	simpleJdbc2;
	
	/**
	 * Import protocol from jdbc/ows2
	 * @param foc
	 */
	
	@Autowired	@Qualifier("dbConfig")		DbConfig	dbConfig;
	/**
	 * Save new user
	 * @param docMtl
	 */
	
	@Transactional
	public void saveNewUser(RegisterUserForm ruf){
		//CODE:01347
		//	dieser abschnittfunktioniert, fügt password-salt support hinzu
		// comment above 2 lines and uncomment the below one
		//String hashedPassword = encoder.encodePassword(ruf.getPassword());
		
		//public void saveNewUser(RegistryMtl docMtl){
		int idfc = simpleJdbc.queryForInt(
		"SELECT idfolder FROM folder WHERE folder='contactperson'");
		Tree folderT = em.find(Tree.class, idfc);
		//oder so ist besser
//		Owuser newUserO = docMtl.getNewOwuser();
//		Map<String, Object> owuser = owsSession.getOwuser(simpleJdbc, "admin");
//		log.debug(owuser);
//		int idregistrator=(Integer) owuser.get("idowuser");

//		Tree newContactpersonT = setNewDocT("contactperson",folderT);
		Tree newContactpersonT = setNewDocTWithoutHistory("contactperson", folderT);
		em.persist(newContactpersonT);
		log.debug(newContactpersonT);

		Contactperson contactpersonO = new Contactperson();
		contactpersonO.setId(newContactpersonT.getId());
		contactpersonO.setForename(ruf.getFirstName());
		contactpersonO.setContactperson(ruf.getSecondName());
		contactpersonO.setEmail(ruf.getEmail());
		newContactpersonT.setMtlO(contactpersonO);
		em.persist(contactpersonO);
		log.debug(newContactpersonT);

		String userName = ruf.getUserName();
		//what is the ID of the Internet Institute?
		//newUserO.setInstitute(0);
//		Tree owuserT = treeCreator.add1Child(newContactpersonT, "owuser", 0, newContactpersonT);
		String password = ruf.getPassword();
		Tree owuserT = addOwuser(password, newContactpersonT, userName);

		addHistory(newContactpersonT, owuserT.getId());
//		String folder = userName;
//		Integer
//		idf = dbConfig.getIdUserProtocolFolder();
//		newFolder(idf, folder, owuserT.getId(),userName);
//		idf = dbConfig.getIdUserPatientFolder();
//		newFolder(idf, folder, owuserT.getId(),userName);
		
//		boolean forLocale = true;
//		if(forLocale)
//			return;
		
		//create the confirmation hash: md5( current_time+ salt);
//		Date dt = new Date();
//		String confirmKey 	= "" + dt;
//		String salt			= "this is some salt for security reasons";
//		String confirmHash 	= passwordEncoder.encodePassword(confirmKey, salt);	
		Date		dt	= new Date();
		Timestamp	now	= new Timestamp(dt.getTime());
		
		String confKey		= "" + dt;
		String confirmKey	= encoder.encodeConfirmAccountHash(confKey);
		String sql = "INSERT INTO confirmaccount(username, createdon, confirmkey) VALUES (?,?,?);";
//		simpleJdbc.update(sql,userName,now,confirmKey);
		
		log.debug(sql + " " +userName + " " +now + " " +confirmKey);
//		em.createNativeQuery(sql).setParameter(1, userName).setParameter(2, now).setParameter(3, confirmKey).executeUpdate();
	}

	//CODE:01347
	// forse this function to use salt
	@Transactional
	public void createRemindedPassword(RegisterUserForm ruf)
	{
		Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
		String hashedPassword = passwordEncoder.encodePassword(ruf.getPassword(), null);
	
		// create remindpassword for the user
		
		Date 		dt 	= new Date();
		Timestamp 	now = new Timestamp(dt.getTime());
		
		String confKey 	= "" + dt;
		String confirmKey = encoder.encodeConfirmAccountHash(confKey);
		String sql = "UPDATE confirmaccount SET confirmkey=:key, createdon=:date, remindpassword=BIT'1'" +
				" WHERE username=:user";
		em.createNativeQuery(sql)
		.setParameter("user", ruf.getUserName())
		.setParameter("date", now)
		.setParameter("key", confirmKey)
		.executeUpdate();	
	}
	
	/**
	 * Save new patient .
	 * @param foc
	 */
	@Transactional
	public void saveNewPatient(FlowObjCreator foc){
		log.debug("-----------");
		log.debug("-----------"+foc.getIdf());
		log.debug("-----------"+foc.getBdate());
		Timestamp bd = new Timestamp(foc.getBdate().getTime());
		Tree folderT = readDocT(foc.getIdf());
		Patient newPatientO = foc.getNewPatient();
		newPatientO.setBirthdate(bd);
		
		saveNewPatient(folderT, newPatientO);
	}

	
	/**
	 * Save new drug trade name.
	 * @param foc
	 */
	@Transactional
	public void saveNewTrade(DrugMtl docMtl) {
		log.debug("-----");
		Tree docT = docMtl.getDocT();
		long timeInMillis = Calendar.getInstance().getTimeInMillis();
		Tree newTradeT = treeCreator.addChild(docT.getParentT(), "drug", timeInMillis, docT, docMtl);
		newTradeT.setIdClass(newTradeT.getId());
		addHistory(newTradeT);
		Drug newTradeO = makeNewDrug(newTradeT, docMtl.getTradeName());
		newTradeO.setGeneric((Drug) docT.getMtlO());
		em.persist(newTradeT);
		em.persist(newTradeO);
	}
    
	/**
	 * Save new labor
	 * @param foc
	 */
	@Transactional
	public void saveNewLabor(FlowObjCreator foc) {
		Tree folderT = readDocT(foc.getIdf());
		Tree laborT = new Tree();
		addAtt(laborT,"labor",folderT, laborT);
		laborT.setIdClass(laborT.getId());
		String laborName = foc.getLaborName();
		String laborUnit = foc.getLaborUnit();
		Labor laborO = new Labor();
		laborO.setLabor(laborName);
		laborO.setUnit(laborUnit);
		laborO.setId(laborT.getId());
		addHistory(laborT);
		em.persist(laborT);
		em.persist(laborO);
	}
	/**
	 * Save new generic drug
	 * @param foc
	 */
	@Transactional
	public void saveNewGeneric(FlowObjCreator foc) {
		Tree folderT = readDocT(foc.getIdf());
		Tree newGenericT = setNewDocT("drug",folderT);
		Drug newGenericO = makeNewDrug(newGenericT, foc.getGenericName());
		newGenericO.setGeneric(newGenericO);
		em.persist(newGenericT);
		em.persist(newGenericO);
	}

	private Drug makeNewDrug(Tree newDrugT, String drugName) {
		Drug newDrugO = new Drug();
		newDrugO.setDrug(drugName);
		newDrugO.setId(newDrugT.getId());
		return newDrugO;
	}
	public void saveNewProtocolEntry(FlowObjCreator foc, ExplorerMtl docMtl) {
		log.debug(foc);
		log.debug(foc.getNewProtocol());
		log.debug(foc.getNewProtocol().getDuration());
		if(null==foc.getNewProtocol().getDuration())
			foc.getNewProtocol().setDuration(14);
		String folder = docMtl.getFolderO().getFolder();
		log.debug(folder);
		foc.setFolder(folder);
	}
	/**
	 * Save new therapy (protocol).
	 * @param foc
	 */
	@Transactional
	public void saveNewProtocol(FlowObjCreator foc) {
		Integer idf = foc.getIdf();
		Tree newProtocolT = saveNewDocRoot(idf, "protocol");
		Protocol newProtocolO = foc.getNewProtocol();
		log.debug(newProtocolO);
		ConceptMtl.reviseIntention(newProtocolO);
		log.debug(newProtocolO);
		newProtocolO.setProtocoltype("clinic");
		newProtocolO.setId(newProtocolT.getId());
		Tree definitionT = addChild("definition",newProtocolT,newProtocolT);
		
		Tree newTaskT = addChild("task",definitionT,newProtocolT);
		newTaskT.setIdClass(newTaskT.getId());
		int duration = 14;
		duration = newProtocolO.getDuration();
		Task newTaskC = newTaskC(newProtocolO.getSchemaName(),"chemo",duration, newTaskT);
		addHistory(newTaskT);
		
//		Task supportTaskC = new Task();
//		supportTaskC.setTask("support");
//		supportTaskC = (Task) taskL(supportTaskC).get(0);
		SchemaMtl schemaMtl = new SchemaMtl(newTaskT);
		Tree supportTaskC=initInsideTask(schemaMtl, "support");
//		addSchema(supportTaskC.getId(), newTaskT, newTaskT);
		
		Tree newTask2T = addChild("task",newProtocolT,newProtocolT);
		newTask2T.setIdClass(newTaskT.getIdClass());
//		em.merge(newProtocolT);
		em.persist(newProtocolT);
		em.persist(newProtocolO);
		em.persist(newTaskC);
		//formUtil.setNewTaskT(newTaskT);
		foc.setIdf(newTaskT.getId());
	}

//	/**
//	 * Save new therapy (protocol).
//	 * @param formUtil
//	 */
//	@Transactional
//	public void saveNewProtocolOld(FormUtil formUtil)
//	{
//		Tree newProtocolT = setNewDocT("protocol",formUtil.getFolderT());
//		Protocol newProtocolC = formUtil.getNewDoc();
//		newProtocolC.setProtocoltype("clinic");
//		newProtocolC.setId(newProtocolT.getId());
//		Tree defT = addChild("definition",newProtocolT,newProtocolT);
//		
//		Tree newTaskT = newConceptSchema(newProtocolT, defT, newProtocolC.getProtocol());
//		
//		formUtil.setNewTaskT(newTaskT);
//		
//		Task supportTaskC = new Task();
//		supportTaskC.setTask("support");
//		supportTaskC = (Task) taskL(supportTaskC).get(0);
//		addSchema(supportTaskC.getId(), newTaskT, newTaskT);
//		
//		Tree newTask2T = addChild("task",newProtocolT,newProtocolT);
//		newTask2T.setIdClass(newTaskT.getIdClass());
////		em.merge(newProtocolT);
//		em.persist(newProtocolT);
//		em.persist(newProtocolC);
//		em.persist(newTaskT.getMtlO());
////		em.persist(newTaskC);
////		formUtil.setNewTaskT(newTaskT);
//	}

	private Tree newConceptSchema(Tree newProtocolT, Tree dT, Task taskO) {
		Tree newTaskT = addChild("task",dT,newProtocolT);
		newTaskT.setIdClass(newTaskT.getId());
		int duration = 14;
		duration=taskO.getDuration();
		Task newTaskC = newTaskC(taskO.getTask(),"chemo",duration, newTaskT);
		newTaskT.setMtlO(newTaskC);
		addHistory(newTaskT);
		return newTaskT;
	}

	private Task newTaskC(String task,String type, int duration, Tree newTaskT) {
		Task newTaskC = new Task();
		newTaskC.setTask(task);
		newTaskC.setType(type);
		newTaskC.setDuration(duration);
		newTaskC.setId(newTaskT.getId());
		newTaskC.setCyclename(newTaskT.getId().toString());
		return newTaskC;
	}
	
	

	/**
	 * Save user document.
	 * @param docMtl
	 */
	@Transactional
	public void saveUserDoc(TreeManager docMtl){
		log.debug("----------------");
		log.debug(docMtl);
		log.debug("----------------");
		for (Tree tree : docMtl.getRemoveNodeS()){
			String sql = "DELETE FROM Tree WHERE id="+tree.getId();
			em.createNativeQuery(sql).executeUpdate();
		}
		mergeTreeNative(docMtl.getDocT());
		owsSession.setDocMtl(null);
//		owsSession.getUserCareSession().setDocMtl(null);
	}
	
	@Autowired @Qualifier("armCreator")	DbArmCreator	armCreator;
	@Transactional
	public void saveArmName(ConceptMtl docMtl){
		Tree editNodeT = docMtl.getEditNodeT();
		Arm editArmO = docMtl.getEditArmO();
		if(editArmO.hasArm()){
			MObject build = armCreator.setMtlO(editArmO).build();
			editNodeT.setIdClass(build.getId());
		}else{
			editNodeT.setIdClass(null);
		}
	}
	@Transactional
	public void saveTaskVariant(ConceptMtl docMtl){
		log.debug("---------???-----getIds----"+docMtl.getIdt());
		log.debug("---------???------getIds---"+docMtl.getIds());
		if(1==docMtl.getIds())
			docMtl.setIds(docMtl.getIdt());
		log.debug("---------???-----getIds----"+docMtl.getIds());
		log.debug("---------???---------"+docMtl.getIdc());
		Tree idtT = docMtl.getTree().get(docMtl.getIdt());
		Tree idsT = docMtl.getTree().get(docMtl.getIds());
		Task idtO = (Task) idtT.getMtlO();
		idtO.setCyclename(idsT.getIdClass().toString());
		em.merge(idtO);
		log.debug("---------???---------");
	}
	/**
	 * @param docMtl
	 */
	@Transactional
	public void saveConceptSchema(ConceptMtl docMtl){
		log.debug("---------???---------");
//		delete(docMtl);
		deleteNative(docMtl);
		reviseSourceNative(docMtl.getClassM(), docMtl);
		Tree protocolT = docMtl.getDocT();
		Tree definitionT = JXPathBean.getChild(docMtl.getDocT(), "definition");
//		Tree definitionT=null;
//		for (Tree tree : protocolT.getChildTs()) 
//			if("definition".equals(tree.getTabName()))
//			{
//				definitionT=tree;
//			}
		if(definitionT==null) return;
		log.debug("-----------"+definitionT);
		Task taskO = docMtl.getEditSchemaO();
		log.debug("-----------"+definitionT);
		log.debug("-----------"+taskO);
		if(taskO!=null){
			Tree newTaskT = newConceptSchema(protocolT, definitionT, taskO);
//			Tree newTaskT = newConceptSchema(protocolT, definitionT, taskO.getTask());
			em.merge(protocolT);
			em.persist(newTaskT.getMtlO());
			docMtl.setIdt(newTaskT.getId());
		}else{
			em.merge(protocolT);
		}
	}
	

	/**
	 * Save drug document.
	 * @param schemaMtl
	 */
	@Transactional
	public void saveDrugDoc(DrugMtl docMtl){
		log.debug("----------------");
		if(isDrugUsage()){
		}else{
			//		public void saveDrugDoc(DrugMtl docMtl){
//			delete(docMtl);
			deleteNative(docMtl);
			mergeTreeNative(docMtl.getDocT());
			owsSession.setDocMtl(null);
			//		owsSession.getUserCareSession().setDocMtl(null);
		}
	}

	@Transactional
	public void savePatientNative(PatientMtl docMtl){
//		delete(docMtl);
		deleteNative(docMtl);
		mergeTreeNative(docMtl.getDocT());
	}
	/**
	 * Save therapy schema native.
	 * @param docMtl
	 */
	@Transactional
	public void saveSchemaNative(SchemaMtl docMtl){
		log.debug("---------------------"+docMtl.getTestT());
//		delete(docMtl);
		deleteNative(docMtl);
		log.debug("---------------------"+docMtl.getTestT());
		newDrugTradeSource(docMtl);
		log.debug("---------------------"+docMtl.getTestT());
		reviseSourceNative(docMtl.getClassM(), docMtl);
		log.debug("---------------------"+docMtl.getTestT());
		if(docMtl.getPatientMtl()!=null){
			log.debug("---------------------");
			for (Tree tree : docMtl.getPatientMtl().getRemoveNodeS()){
				log.debug("---------------------");
deleteNative(tree);
			}
			reviseSourceNative(docMtl.getPatientMtl().getClassM(), docMtl.getPatientMtl());
			mergeTreeNative(docMtl.getPatientMtl().getDocT());
		}
		mergeTreeNative(docMtl.getDocT());
		log.debug("---------------------"+docMtl.getTestT());
		if("copyTaskAll".equals(docMtl.getAction())){
			log.debug("---copyTaskAll------------------");
			Tree fromSchemaT = docMtl.getCopyNodeT();
			Task fromSchemaO = (Task) fromSchemaT.getMtlO();
			Task schemaO = (Task) docMtl.getDocT().getMtlO();
			schemaO.setDuration(fromSchemaO.getDuration());
			em.merge(schemaO);
			copyTaskAll(docMtl, fromSchemaT);
		}
//		em.flush();
		log.debug("---------------------");
		owsSession.setDocMtl(null);
//		owsSession.getUserCareSession().setDocMtl(null);
	}
	/**
	 * Save therapy schema.
	 * @param schemaMtl
	 */
	@Transactional
	public void saveSchema(SchemaMtl schemaMtl){
		log.debug("---------------------"+schemaMtl);
		deleteNative(schemaMtl);
		log.debug("---------------------");
		newDrugTradeSource(schemaMtl);
		log.debug("---------------------");
		reviseSource(schemaMtl.getClassM(), schemaMtl);
		log.debug("---------------------");
		if(schemaMtl.getPatientMtl()!=null){
			log.debug("---------------------");
			for (Tree tree : schemaMtl.getPatientMtl().getRemoveNodeS()){
deleteNative(tree);
			}
			reviseSource(schemaMtl.getPatientMtl().getClassM(), schemaMtl.getPatientMtl());
			mergeTree(schemaMtl.getPatientMtl().getDocT());
		}
		log.debug("---------------------");
		mergeTree(schemaMtl.getDocT());
		log.debug("---------------------");
//		em.flush();
		owsSession.setDocMtl(null);
//		owsSession.getUserCareSession().setDocMtl(null);
	}

//	private void updateRef(Tree drugT, DrugMtl schemaMtl) {
	
	private void mergeTreeNative(Tree docT) {
		mergeTree1Native(docT);
	}
	
	private void mergeTree1Native(Tree parentT) {
//		log.debug(parentT);
		if(parentT.getChildTs()!=null)
			for(Tree tree:parentT.getChildTs()){
				if(tree.getId()<TreeManager._5000){
					tree.setId(nextDbid());
//					insert(tree);
					if(tree.getHistory()!=null){
						tree.getHistory().setIdhistory(tree.getId());
//						insert(tree.getHistory());
					}
				}
				
				mergeTree1Native(tree);
			}
	}

	private void mergeTree_depr(Tree docT) {
		List<History> historyL=new ArrayList<History>();
		for(Tree tree:docT.getDocNodes()){
			if(tree.getId()<TreeManager._5000){
				tree.setId(nextDbid());
				if(tree.getHistory()!=null){
					tree.getHistory().setIdhistory(tree.getId());
					historyL.add(tree.getHistory());
				}
			}
		}
		em.merge(docT);
	}
	
	private void newDrugTradeSource(SchemaMtl schemaMtl){
		Drug tradeC = schemaMtl.getEditTradeC();
		log.debug(tradeC);
		if(tradeC!=null){
			Integer idGeneric = schemaMtl.getEditDrugC().getId();
			if(idGeneric>TreeManager._5000){
				Tree genericT = em.find(Tree.class, idGeneric);
				tradeC.setGeneric(schemaMtl.getEditDrugC());
				Tree tradeT = setClassT("drug", genericT.getParentT());
				tradeT.setDocT(genericT);
				tradeC.setId(tradeT.getId());
				insert(tradeT);
				insert(tradeC);
//				em.persist(tradeT);
//				em.persist(tradeC);
//				schemaMtl.getEditDrugT().setIdClass(idGeneric);
				schemaMtl.getEditDrugT().setIdClass(tradeC.getId());
			}
		}
	}

//	String modInHistorySql = "SELECT t1.id FROM tree t1, tree t2 WHERE t1.iddoc=? AND t2.iddoc=? AND t1.ref=t2.id";
	String modInHistorySql = "SELECT t3.id FROM tree t1, tree t2, tree t3 " +
			" WHERE t1.iddoc=? AND t2.id=? AND t1.ref=t2.id AND t3.id=t1.did AND t3.tab_name='notice'";
	public List<Map<String, Object>> seekMod(SchemaMtl schemaMtl){
		Integer idS = schemaMtl.getDocT().getId();
		Integer idP = schemaMtl.getPatientMtl().getDocT().getId();
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(modInHistorySql, idP,idS);
		return queryForList;
	}

	@Transactional
	public void rewriteSchema(SchemaMtl schemaMtl){
		log.debug("---------");
		Tree docT = schemaMtl.getDocT();
		Integer idS = docT.getId();
		Integer idP = schemaMtl.getPatientMtl().getDocT().getId();
		String[] sqls = modInHistorySql.split("\\?");
		String sql = sqls[0]+idP.toString()+sqls[1]+idS.toString()+sqls[2];
		log.debug("---------"+sql);
		sql = "DELETE FROM tree WHERE id IN (" +sql +")";
		log.debug("---------"+sql);
		em.createNativeQuery(sql).executeUpdate();
		String sql1 = "UPDATE Tree SET ref=null where iddoc="+docT.getId();
		log.debug("---------"+sql1);
		em.createNativeQuery(sql1).executeUpdate();
		String sql2 = "DELETE FROM Tree where did="+docT.getId();
		log.debug("---------"+sql2);
		em.createNativeQuery(sql2).executeUpdate();
		Tree schemaT = readDocT(docT.getIdClass());
		
		copyTaskAll(schemaMtl, schemaT);
//		em.flush();
		log.debug("---------");
	}

	

//	public List<Map<String, Object>> addNoticeList(SchemaMtl docMtl){
	public List<Map<String, Object>> addNoticeList(DrugMtl docMtl){
		log.debug("----------");
		Tree doseDayT = docMtl.getEditNodeT().getParentT();
		Tree drugT = doseDayT.getParentT();
		log.debug("----------"+drugT);
		if("drug".equals(drugT.getTabName())&&
			(	"dose".equals(doseDayT.getTabName())
				||"day".equals(doseDayT.getTabName())
			)
		){
			String sql=docMtl.getDddSql();
			log.debug(sql);
			List<Map<String, Object>> list = simpleJdbc.queryForList(sql);
			return list;
		}
		String type = docMtl.getEditNoticeC().getType();
		if(type==null||type.length()==0)
			return null;
//		String sql = "SELECT * FROM notice, (SELECT count(*) AS cnt,idclass" +
//				" FROM tree GROUP BY idclass) AS cnt" +
//				" WHERE cnt.idclass=idnotice AND type=?" +
//				" AND cnt>1 " +
//				" ORDER BY cnt.cnt DESC, notice";
		String sql=" SELECT * FROM notice" +
		" , (SELECT idclass, count(*) AS cnt FROM tree GROUP BY idclass) cnt" +
		" , (SELECT t2.idclass, count(*) AS doccnt FROM tree t1,tree t2 WHERE t1.id=t2.iddoc AND t1.id=t1.idclass GROUP BY t2.idclass) doc" +
		" WHERE type=?" +
		" AND cnt.idclass=idnotice" +
		" AND doc.idclass=idnotice" +
		" ORDER BY cnt.cnt DESC";
		log.debug(sql+" "+type);
		List<Map<String, Object>> list = simpleJdbc.queryForList(sql, type);
		log.debug(list.size());
		return list;
	}

	@Transactional(readOnly = true)
	public PatientMtl makeTumorboard(Integer id,Integer tuboIdParam) {
		Integer tuboId=id;
		Tree docT = em.find(Tree.class, id);
		if("patient".equals(docT.getTabName())){
			docT = em.find(Tree.class, tuboIdParam);
			tuboId=docT.getId();
			while(!"task".equals(docT.getTabName())){
				docT=docT.getParentT();
				tuboId=docT.getId();
				if("patient".equals(docT.getTabName())){
					tuboId=owsSession.getTuboId();
					break;
				}
			}
		}
		return makeTumorboard(tuboId);
	}
	@Transactional(readOnly = true)
	public PatientMtl makeTumorboard(Integer id) {
		Integer tuboId, idPatient ;
		Tree taskT = em.find(Tree.class, id);
		if("patient".equals(taskT.getParentT().getTabName())){
			idPatient = taskT.getParentT().getId();
			tuboId=taskT.getRef();
		}else{
			log.debug(taskT);
			Tree protocolT = em.find(Tree.class, taskT.getDocT().getId());
			log.debug(protocolT);
			Tree patientT = protocolT.getParentT();
			log.debug(patientT);
			idPatient = patientT.getId();
			tuboId=id;
		}
		PatientMtl patientMtl = makePatientMtl(idPatient);
		patientMtl.setTuboId(tuboId);
		owsSession.setTuboId(tuboId);
		Contactperson cp = new Contactperson();
		cp.setEmail("roman.mishchenko@gmail.com");
		cp.setForename("Dyadya");
		cp.setContactperson("Wasa");
		cp.setId(11);
//		log.debug("------------------"+contactpersonManager);
//		contactpersonManager.placeOrder(cp);
//		log.debug("------------------");
		owsSession.setTumorboardDate(tumorboardDate.getInstance());
		return patientMtl;
	}
	
	public void addNewDiagnose(PatientMtl docMtl){
		Tree diagnoseT = docMtl.getEditNodeT();
		if(!"diagnose".equals(diagnoseT.getTabName())){
			diagnoseT = addPatientHistoryNode(docMtl,"diagnose");
			docMtl.setNewEditNodeT(diagnoseT);
		}
		docMtl.openEditDiagnose();
	}

	private Tree addPatientHistoryNode(PatientMtl docMtl, String tabName) {
		Calendar instance = Calendar.getInstance();
		long sort = 0-instance.getTimeInMillis();
		Tree phT = treeCreator.addChild(docMtl.getDocT(),tabName, sort, docMtl.getDocT(), docMtl);
		/*
		owsSession.getUserId(simpleJdbc);
		docMtl.addOfDate2(phT, docMtl.getDocT(), nextDbid());
		 */
		addOfDate(phT);
		return phT;
	}
	public void reviseDiagnose(PatientMtl docMtl, Integer idDiagnose){
		Diagnose diagnoseO = em.find(Diagnose.class, idDiagnose);
		docMtl.addMObject(diagnoseO, docMtl.getEditNodeT());
	}

	@Transactional(readOnly = true)
	public InstituteMtl makeInstituteMtl(){
		String paramId = owsSession.getRequest().getParameter("id");
		if(null==paramId){
			InstituteMtl instituteMtl = new InstituteMtl(null);
			return instituteMtl;
		}
		Integer parseInt = Integer.parseInt(paramId);
		return makeInstituteMtl(parseInt);
	}


	@Transactional(readOnly = true)
	public InstituteMtl makeInstituteMtl(Integer idDoc) {
		log.debug("--------------"+idDoc);
		long sT = System.currentTimeMillis();
		Tree instituteT = readDocT(idDoc);
		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		InstituteMtl docMtl = new InstituteMtl(instituteT);
		docMtl.addClassM(instituteT,em);
		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		
		String stationPart = owsSession.getStationPart();
		log.debug(stationPart);
		if("chronoPlan".equals(stationPart)){
			log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
			makeStationChronoPlan(docMtl);
			for(Tree t1:docMtl.getDocT().getChildTs())
				if("notice".equals(t1.getTabName()))
					docMtl.addClassM(t1,em);
			log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
		}else if("instituteUsedTrade".equals(stationPart)){
//			List<Drug> tradeList = makeStationTradeList(docMtl.getDocT().getId());
//			docMtl.setTradeList(tradeList);
			for(Tree t1:docMtl.getDocT().getChildTs())
				if("drug".equals(t1.getTabName()))
					docMtl.addClassM(t1,em);
		}else if("userAccess".equals(stationPart)){
			for(Tree t1:docMtl.getDocT().getChildTs())
				if("owuser".equals(t1.getTabName()))
					docMtl.addClassM(t1,em);
			setFolderAdminMap(docMtl);
			setUserFolderList(docMtl);
		}else if("colleagues".equals(stationPart)){
			for(Tree t1:docMtl.getDocT().getChildTs()){
				if("contactperson".equals(t1.getTabName()))
					docMtl.addClassM(t1,em);
				else if("folder".equals(t1.getTabName()))
					docMtl.addClassM(t1,em);
				if(t1.hasChild())
					for(Tree t2:t1.getChildTs())
						if("folder".equals(t1.getTabName()))
							docMtl.addClassM(t2,em);
			}
		}else{
			docMtl.addClass(instituteT,instituteT.getId(),em);
			log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()
					+ ":" +docMtl.getDocT().getId() +":"+docMtl.getClassM().size()+"/" +docMtl.getMsCnt() 
					+ "/"+(System.currentTimeMillis()-sT));
		}
		Integer id = docMtl.getDocT().getParentT().getId();
		short right = getRight(id);
		docMtl.setAccessRight(right);
		log.debug("-TimeMillis-------------"+ ":" +idDoc + "/"+(System.currentTimeMillis()-sT));
//		owsSession.setStationO((Institute) docMtl.getDocT().getMtlO());
		setStation((Institute) docMtl.getDocT().getMtlO());
		return docMtl;
	}
private String sqlStationChronoPlan="\n SELECT " +
	" idpatient,patient,forename,birthdate, " +
	" task,schemaT.id,schemaT.ref AS taskId,mdate,duration " +
	" ,mdate + CAST( duration||' days' AS interval ) AS nextdate " +
	",cycleO.pvalue AS cycleNr " +
	" ,cycleO.pvalue " +
	"\n FROM folder , noderole ," +
	" tree patientT,patient, " +
	" tree schemaT , task " +
	" ,tree ofDateT, pvariable ofDateO,history " +
	" ,tree cycleT ,pvariable cycleO " +
	"\n WHERE " +
	" idfolder=idnode and username=? " +
	" AND idfolder=patientT.did AND patientT.id=idpatient " +
	" AND idpatient=schemaT.did AND schemaT.idclass=idtask " +
	" AND schemaT.id=ofDateT.did AND ofDateT.idclass=ofDateO.idpvariable AND ofDateO.pvariable='ofDate' AND ofDateT.id=idhistory " +
	" AND schemaT.id=cycleT.did AND cycleT.idclass=cycleO.idpvariable AND cycleO.pvariable='cycle' " +
	"\n ORDER BY nextdate DESC " +
	" LIMIT 20";
	private void makeStationChronoPlan(InstituteMtl docMtl) {
		String stationListeType = owsSession.getStationListe();
		String userName = owsSession.getRequest().getUserPrincipal().getName();
		if(!"my".equals(stationListeType)){
			userName = "i_"+docMtl.getDocT().getId();
		}
		log.debug(sqlStationChronoPlan+" -- "+userName);
		List<Map<String, Object>> patientSchemaListe = simpleJdbc.queryForList(sqlStationChronoPlan, userName);
		docMtl.setPatientSchemaListe(patientSchemaListe);
	}

	//Station and trade
	String schemaStationTradeListQL = "SELECT d FROM Tree schemaT, Tree stationT, Drug d" +
	" WHERE schemaT.docT.id=:idSchema AND stationT.docT.id=:idStation " +
	" AND stationT.idClass=d.id AND schemaT.idClass=d.generic.id ";
	private List<Drug> makeSchemaStationTradeList(Integer idSchema, Integer idStation) {
		log.debug(schemaStationTradeListQL);
		List<Drug> tradeList = em.createQuery(schemaStationTradeListQL)
				.setParameter("idSchema", idSchema)
				.setParameter("idStation", idStation)
				.getResultList();
		log.debug(tradeList.size());
		return tradeList;
	}
	String stationTradeListQL = "SELECT d FROM Tree t, Drug d WHERE t.docT.id=:iddoc AND t.idClass=d.id";
	private List<Drug> makeStationTradeList( Integer iddoc) {
		log.debug(stationTradeListQL);
		List<Drug> tradeList = em.createQuery(stationTradeListQL).setParameter("iddoc", iddoc).getResultList();
		log.debug(tradeList.size());
		return tradeList;
	}
	//END Station and trade

	@Transactional(readOnly = true)
	public UserMtl makeUserMtl(){
		//		String name = owsSession.getUserName();
		Integer idDoc = owsSession.getUserDocId(simpleJdbc);
		String paramId = owsSession.getRequest().getParameter("id");
		log.debug("--------------"+paramId);
		if(null!=paramId){
			int pId = Integer.parseInt(paramId);
			idDoc=pId;
		}
		log.debug("--------------"+idDoc);
		if(null==idDoc)
			return null;
		return makeUserMtl(idDoc);
	}

	@Transactional
	public void setBsaFormul(Integer idDoc) {
		String bsaFormul = owsSession.getRequest().getParameter("bsaFormul");
		log.debug("bsaFormul "+bsaFormul);
	}
	@Transactional
	public void setAdminStationTree(Integer idStation) {
		
	}
	@Transactional
	public void setAdminStation(Integer idDoc) {
		String userName = getUserName(idDoc);
		boolean isAdminStationDB = getAdminStation(userName);
		String adminStation = owsSession.getRequest().getParameter("adminStation");
		boolean isAdminStationRq = "on".equals(adminStation);
		log.debug(1);
		if(isAdminStationDB!=isAdminStationRq)
		{
			log.debug(1);
			if(isAdminStationDB&&!isAdminStationRq)
			{
				String deleteAdminStationSql = "DELETE FROM authorities " +
				" WHERE authority='ROLE_ADMINSTATION' AND username=?";
				simpleJdbc.update(deleteAdminStationSql, userName);
			}else if(!isAdminStationDB&&isAdminStationRq){
				log.debug(1);
				String insertAdminStationSql = "INSERT INTO authorities " +
				" (authority,username) VALUES ('ROLE_ADMINSTATION',?)";
				simpleJdbc.update(insertAdminStationSql, userName);
			}
		}
	}
	@Transactional
	public void setRWNodeRole(Integer idDoc) {
		log.debug("------------------ "+idDoc);
		String userName = getUserName(idDoc);
		log.debug("------------------ "+userName);
		List<Map<String, Object>> userFolderList = getUserFolderList(userName);
		for (Map<String, Object> map : userFolderList) {
			Integer idNode=(Integer) map.get("idnode");
			short accessRight = getRight(idNode);
			log.debug(idNode+" -- "+accessRight);
			if(accessRight==TreeManager.RIGHT_AdminYes)
			{
				String urlVal = owsSession.getRequest().getParameter("nw_"+idNode);
				String sqlUpdate=" UPDATE noderole SET write='0' WHERE idnode=? AND username=? ";
				if(null!=urlVal&&"on".equals(urlVal))
					sqlUpdate=" UPDATE noderole SET write='1' WHERE idnode=? AND username=? ";
				simpleJdbc.update(sqlUpdate, idNode,userName);
			}
		}
	}

	private String getUserName(Integer idDoc) {
		String userNameSql = "SELECT owuser " +
		" FROM tree t1,tree t2,owuser " +
		" WHERE t1.id=t2.did AND t2.id=idowuser AND t1.id=? ";
		log.debug(userNameSql+" -- "+idDoc);
		String userName=simpleJdbc.queryForObject(userNameSql,String.class, idDoc);
		return userName;
	}
	@Transactional
	public void setNodeRoleField(Integer idNode, String columnName, boolean admin) {
		boolean dd = owsSession.getRequest().isUserInRole("ROLE_SUPERVISOR");
		log.debug(dd);
		dd = owsSession.getRequest().isUserInRole("ROLE_USER");
		log.debug(dd);
		String sql = "UPDATE noderole SET "+columnName+"='0' WHERE idnode=?";
		if(admin)
			sql = "UPDATE noderole SET "+columnName+"='1' WHERE idnode=?";
		simpleJdbc.update(sql, idNode);
	}
	@Transactional(readOnly = true)
	public UserMtl makeUserMtl(Integer idDoc) {
		long sT = System.currentTimeMillis();
		owsSession.getRequest();//to init owsSession only
		log.debug("--------------"+idDoc);
		Tree userT = readDocT(idDoc);
		log.debug("--------------"+idDoc);
		UserMtl docMtl = new UserMtl(userT);
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()+ "/"+(System.currentTimeMillis()-sT));
		docMtl.addClassM(docMtl.getDocT(),em);
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()+ "/"+(System.currentTimeMillis()-sT));
		Owuser owuserO = null;
		for(Tree t:docMtl.getDocT().getChildTs())
			if(t.getId().equals(t.getIdClass()))
		{
				docMtl.addClassM(t, em);
				if(t.getMtlO() instanceof Owuser)
					owuserO = (Owuser) t.getMtlO();
		}
//		Tree owuserT = (Tree) JXPathContext.newContext(docMtl.getDocT())
//			.getPointer("childTs[tabName='owuser']").getValue();
//		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()+ "/"+(System.currentTimeMillis()-sT));
//		log.debug(owuserT);
//		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()+ "/"+(System.currentTimeMillis()-sT));
//		docMtl.addClassM(owuserT,em);
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()+ "/"+(System.currentTimeMillis()-sT));
//		Owuser owuserO = (Owuser) owuserT.getMtlO();
		log.debug(owuserO);
		String userPart = setUserStation(owuserO);
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()+ "/"+(System.currentTimeMillis()-sT));
		if("drugRewrite".equals(userPart)){
			drugReplaceSchemaList(docMtl);
		}else if("userAccess".equals(userPart)){
			setFolderAdminMap(docMtl);
			setUserFolderList(docMtl);
			setAdminStation(docMtl);
		}else if("copyPatienten".equals(userPart)){
//			copyPatienten(docMtl);
		}else if("changeId".equals(userPart)){
			changeId();
		}
		setSessionStationM();
		log.debug(owsSession.getUserStationList());
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()
//				+ ":" +docMtl.getDocT().getId() +":"+docMtl.getClassM().size()+"/" +docMtl.getMsCnt() 
				+ "/"+(System.currentTimeMillis()-sT));
		if(true)
			return docMtl;
		docMtl.addClass(userT,userT.getId(),em);
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()
				+ "/"+(System.currentTimeMillis()-sT));
		Contactperson userO = docMtl.getUser();
		log.debug(userO);

		return docMtl;
	}

	
	private void changeId() {
		String checkid = owsSession.getRequest().getParameter("checkid");
		log.debug(checkid);
	}

	private String setUserStation(Owuser owuserO) {
		String userPart = owsSession.getUserPart();
		log.debug("userPart = "+userPart);
		if(null!=owuserO && "userCommon".equals(userPart)){
			String owuser = owuserO.getOwuser();
			setUserStationList(owuser);
		}
		return userPart;
	}

	private void drugReplaceSchemaList(UserMtl docMtl) {
		String dirSelect = getDirSelect();
		log.debug("dirSelect = "+dirSelect);

		Map<Integer, Short> userFolderRightM = getUserFolderRightM(dirSelect);
		docMtl.setUserFolderRightM(userFolderRightM);

		dirSelect=dirSelect.replace("SELECT * FROM tree", "SELECT id FROM tree");
//		log.debug("dirSelect = "+dirSelect);
		Tree targetT = owsSession.getTargetT();
		if(null==targetT)
			return;
		JXPathContext newContext = JXPathContext.newContext(targetT);
//		log.debug(targetT);
		Tree rqmSupNoticeT = (Tree) newContext.getPointer("childTs/childTs[mtlO/type='sup' or mtlO/type='rqm']").getValue();
//		log.debug(rqmSupNoticeT);
		Integer idClass = targetT.getIdClass();
		String sql;
		List<Map<String, Object>> replaceSchemaL;
		if(null!=rqmSupNoticeT){
			sql = " SELECT t1.id AS idt,* FROM tree t1,tree t2,tree t3,tree t4,task,tree t5,folder " +
			" WHERE t1.idclass=? " +
			" AND t2.did=t1.id AND t3.did=t2.id " +
			" AND t3.idclass=? " +
			" AND t4.id=t1.iddoc AND t4.id=idtask " +
			" AND t5.id=t4.iddoc " +
			" AND idfolder=t5.did" +
			" AND idfolder IN (" +dirSelect +") " +
			" ORDER BY idfolder DESC ";
			Integer idClass2 = rqmSupNoticeT.getIdClass();
//			log.debug(sql+" -- "+idClass+" -- "+idClass2);
			replaceSchemaL = simpleJdbc.queryForList(sql,idClass,idClass2);
			docMtl.setSeekLogic("drugRqmSupNotice");
		}else{
			Tree noticeT = (Tree) newContext.getPointer("childTs/childTs[tabName='notice']").getValue();
			if(null!=noticeT) {
				sql = " SELECT t1.id AS idt,* FROM tree t1,tree t2,tree t3,tree t4,task,tree t5,folder" +
				" WHERE t1.idclass=? " +
				" AND t2.did=t1.id AND t3.did=t2.id " +
				" AND t3.idclass=? " +
				" AND t4.id=t1.iddoc AND t4.id=idtask " +
				" AND t5.id=t4.iddoc " +
				" AND idfolder=t5.did " +
				" AND idfolder IN (" +dirSelect +") " +
				" ORDER BY idfolder DESC ";
				Integer idClass2 = noticeT.getIdClass();
//				log.debug(sql+" -- "+idClass+" -- "+idClass2);
				replaceSchemaL = simpleJdbc.queryForList(sql,idClass,idClass2);
				docMtl.setSeekLogic("drugSimpleNotice");
			}else{
				String tabName = targetT.getTabName();
				sql = " SELECT folder,task,idtask,t1.id AS idt, *" +
				" FROM " +tabName +",tree t1,tree t2,task,tree t3,folder " +
				" WHERE id" +tabName +"=? " +
				" AND t1.idclass=id"+tabName+" AND t1.iddoc=t2.id AND t2.id=idtask " +
				" AND t3.id=t2.iddoc " +
				" AND idfolder=t3.did " +
				" AND idfolder IN (" +dirSelect +") " +
				" ORDER BY idfolder DESC ";
//				log.debug(sql+" -- "+idClass);
				replaceSchemaL = simpleJdbc.queryForList(sql,idClass);
				docMtl.setSeekLogic("idClassOnly");
			}
		}
		docMtl.setReplaceSchemaL(replaceSchemaL);
		
	}

	private Map<Integer, Short> getUserFolderRightM(String dirSelect) {
		Map<Integer,Short> userFolderRightM= new HashMap<Integer,Short>();
		List<Map<String, Object>> dirL = simpleJdbc.queryForList(dirSelect);
		for (Map<String, Object> map : dirL) {
			Integer idfolder=(Integer) map.get("idfolder");
			boolean wr=(Boolean) map.get("wr");
			short userFolderRight = wr?TreeManager.RIGHT_WriteYes:TreeManager.RIGHT_ReadYes;
//			log.debug(idfolder+" "+wr+" "+userFolderRight);
			userFolderRightM.put(idfolder, userFolderRight);
		}
		return userFolderRightM;
	}

	@Transactional
	public void rewrite(DrugMtl docMtl){
		docMtl.rewrite(this,owsSession);
//		simpleJdbc.update("commit");
	}

	/**
	 * Reset Password of user in DB.
	 * @param confirmKey
	 * @param newPassword
	 */
	@Transactional
	public void resetPassword(String confirmKey, String newPassword)
	{	
		log.debug("confirmKey:" + confirmKey);
		Map<String,Object> rmap = getConfirmAccountEntry(confirmKey);
		String username = (String) rmap.get("username");
		
		log.debug("rmap:"+rmap +"; newPassword="+newPassword+" username="+username);
		simpleJdbc.update("UPDATE users SET password=md5('" +newPassword +"') WHERE username=?",username);
//		simpleJdbc.update("UPDATE owuser SET owuser=md5('" +newPassword +"') WHERE owuser=?",username);
	}
	
	public void entryChangeUserdata(UserMtl docMtl,RegisterUserForm registerUserForm){
		Contactperson user = docMtl.getUser();
		registerUserForm.setTitle(user.getTitle());
		registerUserForm.setFirstName(user.getForename());
		registerUserForm.setSecondName(user.getContactperson());
		registerUserForm.setEmail(user.getEmail());
		Owuser owuserO = docMtl.getOwuser();
		String owuser = owuserO.getOwuser();
		registerUserForm.setUserName(owuser);
	}

	@Transactional
	public void saveChangeUserdata(UserMtl docMtl,RegisterUserForm registerUserForm){
		Contactperson user = docMtl.getUser();
		user.setTitle(registerUserForm.getTitle());
		user.setForename(registerUserForm.getFirstName());
		user.setContactperson(registerUserForm.getSecondName());
		user.setEmail(registerUserForm.getEmail());
		Owuser owuserO = docMtl.getOwuser();
		owuserO.setOwuser(registerUserForm.getUserName());
	}

	@Transactional
	public void changePassword(UserMtl docMtl){
		String 
		newPassword = docMtl.getNewPass(),
		username = docMtl.getUserName();
		log.debug("newPassword="+newPassword+" username="+username);
		simpleJdbc.update("UPDATE users SET password=md5('" +newPassword +"') WHERE username=?",username);
//		simpleJdbc.update("UPDATE owuser SET owuser=md5('" +newPassword +"') WHERE owuser=?",username);
	}
	@Transactional
	public void folder2institute(InstituteMtl docMtl){
		Folder folderO = docMtl.getFolderO();
		short accessRight = getRight(folderO.getId());
		if(accessRight==TreeManager.RIGHT_AdminYes){
			String username = docMtl.getOwuser().getOwuser();
//			String username = "i_"+docMtl.getDocT().getId();
//			if(docMtl instanceof UserMtl)
//				username = ((UserMtl)docMtl).getUserName();
			folder2noderole(username, folderO);
		}
	}

	String userAllfolderAdminSql	= "SELECT * FROM noderole,folder " +
	" WHERE idnode=idfolder AND admin='1' AND username=? ";
	String adminStationSQL			= "SELECT * FROM authorities " +
	" WHERE authority='ROLE_ADMINSTATION' AND username=?";

//	private void setUserFolderList(UserMtl docMtl) {
	private void setAdminStation(InstituteMtl docMtl){
		String userName = docMtl.getOwuser().getOwuser();
		boolean isAdminStation = getAdminStation(userName);
		docMtl.setAdminStation(isAdminStation);
	}

	private boolean getAdminStation(String userName) {
		log.debug(adminStationSQL+" -- "+userName);
		List<Map<String, Object>> adminStationList = simpleJdbc.queryForList(adminStationSQL, userName);
		log.debug(adminStationList);
		boolean isAdminStation = adminStationList.size()>0;
		log.debug(isAdminStation);
		return isAdminStation;
	}
	private void setUserFolderList(InstituteMtl docMtl){
		log.debug(docMtl.getDocT());
		String userName = docMtl.getOwuser().getOwuser();
		List<Map<String, Object>> userFolderList = getUserFolderList(userName);
		docMtl.setUserFolderList(userFolderList);
	}

	private List<Map<String, Object>> getUserFolderList(String userName) {
		String sql = "SELECT f00.idfolder ppId, f0.folder parentFolder, f0.idfolder parentId, f1.*,n1.* " +
		" FROM folder f1, noderole n1, folder f0 " +
		" LEFT JOIN folder f00 ON f00.idfolder=f0.fdid " +
		" LEFT JOIN " +
		" (SELECT idnode,min(username) " +
		" FROM noderole WHERE username!=?" +
		" GROUP BY idnode) n0 " +
		" ON n0.idnode=f0.idfolder " +
		" WHERE f0.idfolder=f1.fdid AND n1.idnode=f1.idfolder " +
		" AND n1.username=? " +
		" ORDER BY f00.idfolder, f0.idfolder, f1.folder ";
		log.debug(sql+" -- "+userName);
		List<Map<String, Object>> userFolderList = simpleJdbc.queryForList(sql, userName, userName);
		log.debug(userFolderList.size());
		return userFolderList;
	}
//	private void setFolderAdminMap(UserMtl docMtl) {
	private void setFolderAdminMap(InstituteMtl docMtl) {
		Folder folderO = docMtl.getFolderO();
		String name = owsSession.getRequest().getUserPrincipal().getName();
		List<Map<String, Object>> folderAdminList = simpleJdbc.queryForList(userAllfolderAdminSql, name);
		if(folderAdminList.size()>0)
		{
			Map<Integer,Map<String, Object>> m = new HashMap<Integer, Map<String,Object>>();
			for (Map<String, Object> map : folderAdminList) {
				Integer idnode=(Integer) map.get("idnode");
				m.put(idnode, map);
			}
			docMtl.setFolderAdminMap(m);
		}
	}

	
	public void copy(TreeManager docMtl){
		Integer idc = docMtl.getIdc();
		log.debug("-----------"+idc);
		if(idc!=null){
			Tree copyNodeT = docMtl.getTree().get(idc);
			log.debug("-----------"+copyNodeT);
			if(copyNodeT!=null){
				owsSession.setCopyNodeT(copyNodeT);
				owsSession.setCopyNodeDocMtl(docMtl);
				owsSession.setCopyNodeId(copyNodeT.getIdClass());
			}
		}
	}
	public void paste(ConceptMtl docMtl){
		Tree copyT = docMtl.getCopyNodeT();
		log.debug("---------------");
		if(null==copyT){
			log.info("it's not to copy");
			return;
		}
		log.debug("Copy: "+copyT);
		Tree parentT = docMtl.getTree().get(docMtl.getIdc());
		log.debug(" to :"+parentT);
		if("protocol".equals(parentT.getDocT().getTabName())&&
			"protocol".equals(copyT.getDocT().getTabName())){
		}else
			return;
		log.debug("---------------");
		Tree pparentT = parentT.getParentT();
		if("definition".equals(pparentT.getTabName())){
			
		}else if(parentT.getTabName().equals(copyT.getTabName())){
			log.debug("-------");
			if(copyT.getId().equals(copyT.getIdClass())){//definition/task
				pasteOne(docMtl, copyT, pparentT);
			}else{
				pasteT(docMtl,copyT, pparentT);
			}
		}else if("protocol".equals(parentT.getTabName())){
			if("task".equals(copyT.getTabName())){
				pasteOne(docMtl, copyT, parentT);
			}
		}else if("studyarm".equals(parentT.getTabName())){
			if("task".equals(copyT.getTabName())){
				pasteOne(docMtl, copyT, parentT);
			}
		}
	}
	private void pasteT(TreeManager docMtl,Tree copyNodeT, Tree parentT){
		parentT = pasteOne(docMtl, copyNodeT, parentT);
		if(null!=copyNodeT.getChildTs())
			for (Tree tree : copyNodeT.getChildTs())
				pasteT(docMtl,tree, parentT);
	}
	private Tree pasteOne(TreeManager docMtl, Tree copyNodeT, Tree parentT) {
		log.debug(copyNodeT+"\n to "+parentT);
		Integer nextCurrentId = nextDbid();
		parentT = docMtl.addChild(parentT, copyNodeT.getTabName(), docMtl.getDocT(), nextCurrentId);
		parentT.setIdClass(copyNodeT.getIdClass());
		return parentT;
	}
	public void addChoose(ConceptMtl docMtl){
		Tree docT = docMtl.getDocT();
		Tree chooseT=docMtl.addChild(docT, "choice", docT, nextDbid());
		docMtl.addChild(chooseT, "studyarm", docT, nextDbid());
	}
	public void nullSeekDrug(SchemaMtl docMtl, FormUtil fu){
		fu.setMode("nullSeekDrug");
		docMtl.setIdt(docMtl.getEditNodeT().getId());
		int drugFolderId = simpleJdbc.queryForInt("select idfolder from folder where folder=?", "drug");
		fu.setDrugFolderId(drugFolderId);
	}


	public List<Map<String, Object>> getNewlyCreatedDocuments()
	{
		String sql ="SELECT t2.cnt, * FROM tree t1 LEFT JOIN (SELECT did,count(*) " +
		"AS cnt FROM tree GROUP BY did) t2 ON t1.id=t2.did,history " +
		"WHERE  t1.id=t1.idclass " +
		"AND t1.id=t1.iddoc " +
		"AND t1.id=idhistory " +
		"AND t2.cnt>=1 " +
		"ORDER BY mdate DESC LIMIT 10";
		
		List<Map<String, Object>> rmap = simpleJdbc.queryForList(sql);
		
		log.debug("getNewlyCreatedDocuments");
		
		if(rmap !=null && !rmap.isEmpty())
			return rmap;
		else
			return null;
	}

	public void setIntensivDayEntry(SchemaMtl docMtl){
		log.debug(1);
		docMtl.setIntensivDays(new HashSet<IntensivDay>());
		//		int i = 2;
		//		docMtl.getIntensivDays().add(docMtl.getIntensivDay(i));
		docMtl.setTherapyDaySet(new HashSet<Integer>());
		for (Ts ts : docMtl.getPlan()){
			Integer cday = ts.getCday();
			docMtl.getTherapyDaySet().add(cday);
		}
		log.debug(docMtl.getTherapyDaySet());
		Tree daysIntensiveT = getDaysIntensiveT(docMtl);
		if(null!=daysIntensiveT)
		{
			Tree iDaysT = daysIntensiveT.getChildTs().get(0);
			Day dayO = docMtl.getDayO(iDaysT);
			String[] split = dayO.getAbs().split(",");
			for (String string : split) {
				IntensivDay valueOf = IntensivDay.valueOf("P"+string);
				docMtl.getIntensivDays().add(valueOf);
			}
		}
	}

	
	@Transactional
	public void setIntensivDaySave(SchemaMtl docMtl)
	{
		log.debug(docMtl.getIntensivDays());
		String abs = "";
		if(null!=docMtl.getIntensivDays())
		{
			for(IntensivDay d:docMtl.getIntensivDays())
				abs+=d.name().replace("P", ",").replace("N", ",-");
					abs=abs.substring(1);
		}
		log.debug(abs);
		Tree daysIntensiveT = null, iDaysT = null;
		Day iDaysO = null;
		daysIntensiveT = getDaysIntensiveT(docMtl);
		log.debug(daysIntensiveT);
		if(null==daysIntensiveT&&abs.length()>0){
			log.debug(1);
			daysIntensiveT = nodeCreator.setTagName("pvariable")
					.setTreeManager(docMtl).setParentT(docMtl.getDocT()).addChild();
			log.debug(2);
			log.debug(daysIntensiveT);
			iDaysT = nodeCreator.setTagName("day")
					.setTreeManager(docMtl).setParentT(daysIntensiveT).addChild();
			log.debug(iDaysT);
			log.debug(3);
			Pvariable daysintensiveO = new Pvariable("daysintensive", "", "");
			log.debug(daysintensiveO);
			daysintensiveO = (Pvariable) pvCreator.setMtlO(daysintensiveO).build();
			daysIntensiveT.setMtlO(daysintensiveO);
			log.debug(daysintensiveO);
			iDaysO = (Day) dayCreator.setMtlO(new Day(abs,"a")).build();
			log.debug(iDaysO);
			iDaysT.setMtlO(iDaysO);
		}else if(abs.length()==0){
//			em.remove(daysIntensiveT);
			delete(daysIntensiveT);
		}else{
			iDaysT = daysIntensiveT.getChildTs().get(0);
			iDaysO = (Day) dayCreator.setMtlO(new Day(abs,"a")).build();
			iDaysT.setMtlO(iDaysO);
//			iDaysO = docMtl.getDayO(iDaysT);
		}
		log.debug(daysIntensiveT);
	}

	private Tree getDaysIntensiveT(SchemaMtl docMtl) {
		Tree daysIntensiveT=null;
		for (Tree tree : docMtl.getDocT().getChildTs()) 
			if(docMtl.isPvPv("daysintensive", tree))
				daysIntensiveT=tree;
		return daysIntensiveT;
	}
	@Autowired @Qualifier("pvCreator")				DbPvariableCreator		pvCreator;


}

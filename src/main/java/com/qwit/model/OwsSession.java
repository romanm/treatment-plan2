package com.qwit.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.qwit.domain.Drug;
import com.qwit.domain.Institute;
import com.qwit.domain.Patient;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;

/**
 * 
 * @author roman, sklebeck
 *
 */
public class OwsSession implements Serializable{
	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;

	public static OwsSession getOwsSession() {
//		ServletRequestAttributes currentRequestAttributes = 
//			(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		OwsSession attribute = (OwsSession) getRequest()
		.getSession().getAttribute("scopedTarget.owsSession");
		return attribute;
	}
	
	public static SecurityContext getSecurityContext() {
		SecurityContext attribute = (SecurityContext) OwsSession.getRequest()
		.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		return attribute;
	}
	
//	public static OwsSessionContainer getOwsSessionContainer() {
//		HttpSession session = getRequest().getSession();
//		return (OwsSessionContainer) session.getAttribute("scopedTarget.owsSessionContainer");
//	}
	public boolean isRequestEvent(String eventId ) {
		String rqEventIdVal = getRequest().getParameter("_eventId");
		boolean b = null!=rqEventIdVal&&rqEventIdVal.equals(eventId);
		return b;
	}
	public static Integer getRqIntParam(String paramName) {
		String parameter = getRequest().getParameter(paramName);
		if(null!=parameter&&!"".equals(parameter))
		{
			Integer parseInt = Integer.parseInt(parameter);
			return parseInt;
		}
		return null;
	}
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes currentRequestAttributes = (ServletRequestAttributes) 
				RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = currentRequestAttributes.getRequest();
		return request;
	}

	private Tree copyNodeT;
	public Tree getCopyNodeT() {return copyNodeT;}
	public void setCopyNodeT(Tree copyNodeT) {this.copyNodeT = copyNodeT;}

	private String prefixc;
	public String getPrefixc() {return prefixc;}
	public void setPrefixc(String prefixc) {this.prefixc = prefixc;}

	private Integer idClass2copy;
	public Integer getIdClass2copy() {return idClass2copy;}
	public void setIdClass2copy(Integer idClass2copy) {this.idClass2copy = idClass2copy;}

	private boolean sameDayTogether;
	public boolean isSameDayTogether() {return sameDayTogether;}
	public void setSameDayTogether(boolean sameDayTogether) {this.sameDayTogether = sameDayTogether;}

	private Integer timesRelativPanel;
	public Integer getTimesRelativPanel()
							{return timesRelativPanel;}
	public void setTimesRelativPanel(Integer timesRelativPanel)
							{this.timesRelativPanel = timesRelativPanel;}

	private Integer idOwuser,idUserDoc;
	public Integer getIdOwuser() {return idOwuser;}
	public Integer getUserId(SimpleJdbcTemplate simpleJdbc) {
		if(idOwuser==null)
			idOwuser = (Integer) getOwuser(simpleJdbc).get("idowuser");
		return idOwuser;
	}
	public Integer getUserDocId(SimpleJdbcTemplate simpleJdbc) {
		if(idUserDoc==null && null!=getSecurityContext())
			idUserDoc = (Integer) getOwuser(simpleJdbc).get("iddoc");
		return idUserDoc;
	}
	private Map<String, Object> getOwuser(SimpleJdbcTemplate simpleJdbc) {
		String name = getSecurityContext().getAuthentication().getName();
		return getOwuser(simpleJdbc, name);
	}

	public Map<String, Object> getOwuser(SimpleJdbcTemplate simpleJdbc, String name) {
		String sql = "SELECT idowuser,iddoc FROM tree t1, owuser " +
		" WHERE t1.idclass=idowuser AND owuser=?";
		log.debug(sql+" "+name);
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, name);
		Map<String, Object> map = queryForList.get(0);
		log.debug(map);
		return map;
	}

	/*
	public String getUserName() {
		String name = getSecurityContext().getAuthentication().getName();
		return name;
	}
	 * */

	//depricated
	private TreeManager docMtl;
	public TreeManager getDocMtl() {return docMtl;}
	public void setDocMtl(TreeManager docMtl) {this.docMtl = docMtl;}
	
	/**
	 * ID of copied node, this is used in explorer for some jdbc related transactions
	 */
	private Integer copyNodeId;
	public void setCopyNodeId(Integer copyNodeId) {this.copyNodeId = copyNodeId;}
	public Integer getCopyNodeId() {return copyNodeId;}

	private Integer cutNodeId;
	public Integer getCutNodeId() {return cutNodeId;}
	public void setCutNodeId(Integer cutNodeId) {this.cutNodeId = cutNodeId;}

	private Integer idStationPatient, idStationProtocol;
	public Integer getIdStationPatient() { return idStationPatient; }
	public void setIdStationPatient(Integer idStationPatient) { this.idStationPatient = idStationPatient;}

	public Integer getIdStationProtocol() { return idStationProtocol;} 
	public void setIdStationProtocol(Integer idStationProtocol) { this.idStationProtocol = idStationProtocol; }

	/**
	 * Mtl document object of copy node.
	 */
	private TreeManager copyNodeDocMtl;
	public TreeManager getCopyNodeDocMtl() {return copyNodeDocMtl;}
	public void setCopyNodeDocMtl(TreeManager treeManager) {
		this.copyNodeDocMtl=treeManager;
	}
	//rest to revise
	/*
	public OwsSessionContainer(){
		userCareSession=new UserCareSession();
	}
	private UserCareSession userCareSession;

	public UserCareSession getUserCareSession() {return userCareSession;}
	public void setUserCareSession(UserCareSession userCareSession) {
		this.userCareSession = userCareSession;
	}


	private TreeManager docMtl;
	public TreeManager getDocMtl() {return docMtl;}
	public void setDocMtl(TreeManager docMtl) {this.docMtl = docMtl;}

	private Tree copyNodeT;
	public Tree getCopyNodeT() {
		log.debug(copyNodeT);
		return copyNodeT;
	}

	public void setCopyNodeT(Tree copyNodeT) {
		this.copyNodeT = copyNodeT;
		log.debug(copyNodeT);
	}

	private String userName;
	private Integer userId;
	public String getUserName()	{return userName;}
	public void setUserName(String userName) {this.userName = userName;}
	public Integer getUserId()		{return userId;}
//	public Integer getUserId()		{return userName2id.get(userName);}
	public Integer getUserDocId()	{return userName2idUserDoc.get(userName);}

	private Map<String, Integer> userName2id;
	private Map<String, Integer> userName2idUserDoc;
	private Map<Integer, String> id2userName;

	public Map<Integer, String> getId2userName() {return id2userName;}
	public void setId2userName(SimpleJdbcTemplate simpleJdbc) {
		log.debug("------USERCARESESSION - setId2userName");
		if(id2userName == null){
			id2userName = new HashMap<Integer, String>();
			userName2id = new HashMap<String, Integer>();
			userName2idUserDoc = new HashMap<String, Integer>();
//			String sql = "SELECT idowuser,owuser FROM owuser";
			String sql = "select iddoc,owuser,idowuser from owuser,tree where idclass=idowuser and iddoc is not null";
			List<Map<String, Object>> owsUserList = simpleJdbc.queryForList(sql);
			for (Map<String, Object> map : owsUserList) {
				String owuser = (String) map.get("owuser");
				Integer idowuser = (Integer) map.get("idowuser");
				Integer iddoc = (Integer) map.get("iddoc");
				id2userName.put(idowuser, owuser);
				userName2id.put(owuser, idowuser);
				userName2idUserDoc.put(owuser, iddoc);
			}
		}
	}
*/

	private Task taskO;
	public Task getTaskO() {return taskO;}
	public void setTaskO(Task taskO) {this.taskO = taskO;}

	private Integer ofDateFixedId;
	public Integer getOfDateFixedId() {return ofDateFixedId;}
	public void setOfDateFixedId(Integer id) {this.ofDateFixedId=id;}

	private String userNoticeType=null;
	public String getUserNoticeType() {return userNoticeType;}
	public void setUserNoticeType(String type) {this.userNoticeType=type;}

	Integer tuboId;
	public Integer getTuboId() {return tuboId;}
	public void setTuboId(Integer tuboId) {this.tuboId=tuboId;}

	private Calendar tumorboardDate;
	public Calendar getTumorboardDate() {return tumorboardDate;}
	public void setTumorboardDate(Calendar instance) {tumorboardDate=instance;}

	private Patient patientO;
	private Tree patientT;
	public void setPatientT(Tree patientT) {this.patientT = patientT;}
	public Tree getPatientT()		{return patientT;}

	private short accessRight2patient;
	public short getAccessRight2patient() {return accessRight2patient;}
	public void setAccessRight2patient(short accessRight2patient) {
		this.accessRight2patient=accessRight2patient;
	}
//	public Patient getPatientO()	{return patientO;}
	public Patient getPatientO()	{return (Patient) patientT.getMtlO();}
	
//	public Patient initPatientO(Integer idPatient, EntityManager em) {
//		log.debug("idPatient="+idPatient);
//		log.debug("patientO="+patientO);
//		if(idPatient!=null&&(patientO==null||!idPatient.equals(patientO.getId())))
//		{
//			patientO = em.find(Patient.class, idPatient);
//			patientT = em.find(Tree.class, idPatient);
//			patientT.setMtlO(patientO);
//		}
//		return patientO;
//	}

	/**
	private Pvariable ofDateFixed, procent100;
	public Pvariable getProcent100()	{return procent100;}
	public Pvariable getOfDateFixed()	{return ofDateFixed;}

	 *  missing description! what does this Function? set does it set the SchemaMtlService? - no wrong function name!
	@Autowired @Qualifier("pvCreator") DbPvariableCreator	pvCreator;
	public void set(SchemaMtlService schemaMtlService) {
		if(procent100==null){
			procent100 = schemaMtlService.getPvariable("percent","100","%");
//			procent100 = pvCreator.setPvariable("percent").setPvalue("100").setUnit("%").read();
		}
		if(ofDateFixed==null){
			ofDateFixed = schemaMtlService.getPvariable("ofDate","fixed","");
//			ofDateFixed = pvCreator.setPvariable("ofDate").setPvalue("fixed").read();
		}
	}
	*/

	/*
	SchemaMtl schemaCareMtl=null;
	public SchemaMtl getSchemaCareMtl() {return schemaCareMtl;}
	public void setSchemaCareMtl(SchemaMtl schemaCareMtl) {this.schemaCareMtl=schemaCareMtl;}
	 * */

	private String userPart;
	public String getUserPart() {return userPart;}
	public void setUserPart(String userPart) {this.userPart = userPart;}

	private String patientPart="times";
	public String getPatientPart() {return patientPart;}
	public void setPatientPart(String patientPart) {this.patientPart = patientPart;}

	private String schemaPart="ed";
	public String getSchemaPart() {return schemaPart;}
	public void setSchemaPart(String schemaPart) {this.schemaPart = schemaPart;}

	private String drugPart;
	public String getDrugPart() {return drugPart;}
	public void setDrugPart(String drugPart) {this.drugPart = drugPart;}


	private String getParamPart() {
		String paramPart = getRequest().getParameter("part");
		return paramPart;
	}

	private Tree targetT,sourceT;
	private Integer sourceIdClass,targetIdClass;

	public Integer getSourceIdClass() {return sourceIdClass;}
	public Integer getTargetIdClass() {return targetIdClass;}
	public void setSourceIdClass(Integer idClass2copy) {this.sourceIdClass=idClass2copy;}
	public void setTargetIdClass(Integer idClass2copy) {this.targetIdClass=idClass2copy;}
	public Tree getSourceT() {return sourceT;}
	public void setSourceT(Tree sourceT) {this.sourceT = sourceT;}
	public Tree getTargetT() {return targetT;}
	public void setTargetT(Tree targetT) {this.targetT = targetT;}

	public void cancelSourceTarget() {
		log.debug("-----------------");
		setSourceIdClass(null);
		setTargetIdClass(null);
		setSourceT(null);
		setTargetT(null);
		log.debug("-----------------");
	}

	Integer idDose,idTimes,idDay,idNotice,idFolder;

	public Integer getIdFolder() {return idFolder;}
	public void setIdFolder(Integer idFolder) {this.idFolder = idFolder;}

	public Integer getIdNotice() {return idNotice;}
	public void setIdNotice(Integer idNotice) {this.idNotice = idNotice;}

	public Integer getIdDay() {return idDay;}
	public void setIdDay(Integer idDay) {this.idDay = idDay;}
	
	public Integer getIdTimes() {return idTimes;}
	public void setIdTimes(Integer idTimes) {this.idTimes = idTimes;}
	
	public Integer getIdDose() {return idDose;}
	public void setIdDose(Integer idDose) {this.idDose = idDose;}

	boolean subDir=false;
	public boolean isSubDir() {return subDir;}
	public void setSubDir(boolean b) {this.subDir=b;}

	private boolean withInfo;
	public boolean isWithInfo() {return withInfo;}
	public void setWithInfo(boolean withInfo) {this.withInfo=withInfo;}
	//	public boolean getWithInfo() {return withInfo;}

	private boolean drugNotice;
//	public boolean getDrugNotice() {return drugNotice;}
	public boolean isDrugNotice() {return drugNotice;}
	public void setDrugNotice(boolean drugNotice) {this.drugNotice=drugNotice;}

	/*
	private String stationName;
	public String getStationName() {return stationName;}
	public void setStationName(String stationName) {this.stationName=stationName;}
	private Integer stationId;
	public Integer getStationId() {return stationId;}
	public void setStationId(Integer id) {this.stationId=id;}
	 */
//	public void setStation(Integer idStation) {
//		List<Institute> userStationList = getUserStationList();
//		for (Institute instituteO : userStationList)
//			if(instituteO.getId().equals(idStation))
//				setStationO(instituteO);
//	}

	private Institute stationO,patientStationO;
	private Map<String, Object> stationM;
	public Map<String, Object> getStationM() {return stationM;}
	private List<Map<String, Object>> userStationList;
	public Institute getPatientStationO() {return patientStationO;}
	public void setPatientStationO(Institute patientStationO) {this.patientStationO = patientStationO;}
//	public Institute getStationO() {return stationO;}
	public void setStationO(Institute stationO) { this.stationO=stationO; }
	public void setStationM(Map<String, Object> stationM) {this.stationM=stationM;}

	public void setUserStationList(List<Map<String, Object>> userStationList) {this.userStationList = userStationList;	}
	public List<Map<String, Object>> getUserStationList() {	return userStationList;	}
	public Map getUserStation(int id) {
		log.debug(id);
		for(Map sM:userStationList)
		{
			log.debug(sM);
			Integer idinstitute=(Integer)sM.get("idinstitute");
			log.debug(idinstitute);
			if(idinstitute.equals(id))
				return sM;
		}
		return null;
	}

	//	private List<Institute> userStationList;
//	public void setUserStationList(List<Institute> userStationList) {this.userStationList = userStationList;}
//	public List<Institute> getUserStationList() {return userStationList;}
//	public Institute getUserStation(int id) {
//		for(Institute sO:userStationList)
//			if(sO.getId().equals(id))
//				return sO;
//		return null;
//	}

	private String stationPart,stationListe;
	public void setStationListe(String stationListe) {this.stationListe = stationListe;}
	public String getStationListe() {
		String paramListe = getRequest().getParameter("liste");
		if(null!=paramListe&&paramListe.length()>0)
			stationListe=paramListe;
		return stationListe;
	}

	public void setStationPart(String stationPart) {this.stationPart = stationPart;}
	public String getStationPart() {
		String paramPart = getParamPart();
		if(null!=paramPart&&paramPart.length()>0)
			stationPart=paramPart;
		return stationPart;
	}
	private String[] intentions;
	public String[] getIntentions() {return intentions;}
	public void setIntentions(String[] intentions) {this.intentions=intentions;}

	Set<Drug> drugSet;
	public Set<Drug> getDrugset(){
		if(null==drugSet)
			initDrugSet();
		return drugSet;
	}

	public void initDrugSet() {
		drugSet=new ConcurrentSkipListSet<Drug>();
	}
	public String getInDrug() {
		String inDrug = "0";
		if(null!=drugSet)
		for (Drug drug : drugSet)
			inDrug+=","+drug.getId();
		return inDrug;
	}

	private String modus;
	public String getModus() {return modus;}
	public void setModus(String modus) {
            this.modus=modus;
        }

	private String schemaSeek;
	public String getSchemaSeek() {return schemaSeek;}
	public void setSchemaSeek(String seek) {this.schemaSeek=seek;}
	
	/*
	 * id from central DB
	 * to id in this DB
	 */
	private HashMap<Integer, Integer> importIdToSynchron;
	public HashMap<Integer, Integer> getImportIdToSynchron() {
		if(null==importIdToSynchron)
			importIdToSynchron=new HashMap<Integer, Integer>();
		return importIdToSynchron;
	}


	

	/*
	private String inDrug;
	public String getInDrug() {
		if(null==this.inDrug)
			removeInDrug();
		return inDrug;
	}
	public void removeInDrug() {this.inDrug="0";}
	public void removeInDrug(Integer id)	{inDrug = getInDrug().replace(","+id, "");}
	public void setInDrug(Integer id)		{
		String id2 = ","+id;
		inDrug+=!getInDrug().contains(id2)?id2:"";
	}
	 * */

}

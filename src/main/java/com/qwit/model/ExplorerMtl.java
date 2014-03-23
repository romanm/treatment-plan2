package com.qwit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Drug;
import com.qwit.domain.Folder;
import com.qwit.domain.MObject;
import com.qwit.domain.Protocol;
import com.qwit.domain.Tree;
import com.qwit.util.FormUtil;


public class ExplorerMtl extends TreeManager implements Serializable
{

	private static final long serialVersionUID = 1L;
	protected final Log log = LogFactory.getLog(getClass());
	void runRule() {
	}
	private List<Map<String, Object>> folderContent;
	
	private DBTableEnum dbTableEnum; //for use of table enumeration in jsp 
//	private boolean isPatient=true;
	private Folder	folderDoc;
	private Integer	currentFolderId;
	private List<Folder> treeBreadcrumb;
	
	
	public List<Folder> getTreeBreadcrumb() {
		return treeBreadcrumb;
	}

	public void setTreeBreadcrumb(List<Folder> treeBreadcrumb) {
		this.treeBreadcrumb = treeBreadcrumb;
	}

	public Integer getCurrentFolderId() {
		return currentFolderId;
	}

	public void setCurrentFolderId(Integer currentFolderId) {
		this.currentFolderId = currentFolderId;
	}

	/**
	 *  for use in new therapy schema button
	 */
	Protocol newProtocol=null;
	public Protocol getNewProtocol()
	{
		if(newProtocol==null)	newProtocol=new Protocol();
		return newProtocol;
	}
	
	
	
	public ExplorerMtl(String folderType)
	{
		super(null);
		this.folderType = folderType;
	}
	
	private List<Map<String, Object>> patientMoveList;
	public List<Map<String, Object>> getPatientMoveList() {return patientMoveList;}
	public void setPatientMoveList(List<Map<String, Object>> patientMoveList)
	{
		this.patientMoveList=patientMoveList;
	}
	
	public void setFolderContent(List<Map<String, Object>> patientList)
	{
		this.folderContent = patientList;
	}

	public List<Map<String, Object>> getFolderContent()
	{
		return folderContent;
	}

	private String folderType;
	public void setFolderType(String folderType){this.folderType = folderType;}
	public String getFolderType(){return folderType;}

	public void setDbTableEnum(DBTableEnum dbTableEnum)
	{
		this.dbTableEnum = dbTableEnum;
	}

	public DBTableEnum getDbTableEnum()
	{
		return dbTableEnum;
	}
	
	public boolean getIsPatientFolder()
	{
		return (folderType.equals(DBTableEnum.patient.getTableName()));
	}
	public boolean getIsDrugFolder()
	{
		return (folderType.equals(DBTableEnum.drug.getTableName()));
	}
	public boolean getIsConceptFolder()
	{
		return (folderType.equals(DBTableEnum.concept.getTableName()));
	}
	public boolean getIsLaborFolder()
	{
		return (folderType.equals(DBTableEnum.labor.getTableName()));
	}
	public boolean getIsFindingFolder()
	{
		return (folderType.equals(DBTableEnum.finding.getTableName()));  
	}
	public boolean getIsDiagnoseFolder()
	{
		return (folderType.equals(DBTableEnum.diagnose.getTableName()));  
	}
	public boolean getIsSchemaFolder()
	{
		return (folderType.equals(DBTableEnum.schema.getTableName()));  
	}
	public boolean getIsValidPath()
	{
		//security!
		//iterate through our table enumeration 
		//forbid execution for unknown tableNames
		DBTableEnum[] n = DBTableEnum.values();
		for (DBTableEnum e : n)
		{
			if(e.getTableName().equals(this.folderType))
				return true;
		}
		return false;
	}

	public Integer getDir(){
		int dir = 0;
//		for(Folder f1:folderDoc.getChildFs()){
//			if(f1.getFolder().equals(path))
//				dir=f1.getId();
//		}
		return dir;
	}
	public void setFolderDoc(Folder fd) {
		folderDoc = fd;
	}

	@Override
	void addMtlMap(MObject mtlC) {
	}
	
	/*
	@Override
	void addTreePaar_depr(Tree tree) {
	}
	 */
	@Override
	void add1ClassObj_depr(Tree tree,MObject classM) {
	}

	@Override
	void addTreeClassObj_depr(MObject objM, Tree tree) {
	}

	@Override
	boolean isThisDocumentTag(Tree tree) {
		return false;
	}

	private List<Drug> drugs;
	public List<Drug> getDrugs() {return drugs;}
	public void setDrugs(List<Drug> tL) {this.drugs=tL;}

	private int drugsSize;
	public int getDrugsSize() {return drugsSize;}
	public void setDrugsSize(int n) {this.drugsSize=n;}

	private Map<Integer, Map<String,Object>> prTaSt,taStMap;
	private List<Integer> prIdList;
	private String inPvid;

	public String getInPvid() {return inPvid;}
	public List<Integer> getPrIdList() {return prIdList;}
	public Map<Integer, Map<String, Object>> getPrTaSt() {return prTaSt;}
	public Map<Integer, Map<String, Object>> getTaStMap() {return taStMap;}

	public void setPrTaSt(List<Map<String, Object>> folderContentFromDBJDBC) {
		prIdList=new ArrayList<Integer>();
		prTaSt=new HashMap<Integer, Map<String,Object>>();
		taStMap=new HashMap<Integer, Map<String,Object>>();
		inPvid="";
		protocolCnt=0;
		schemaCnt=0;
		for (Map<String, Object> map : folderContentFromDBJDBC) {
			Integer idPr = (Integer) map.get("idprotocol");
			Map<String, Object> prMap = prTaSt.get(idPr);
			if(prMap==null){
				prIdList.add(idPr);
				prTaSt.put(idPr, map);
				map.put("taskIdList", new ArrayList<Integer>());
				prMap=map;
				protocolCnt++;
			}
			Integer idTask = (Integer) map.get("idtask");
			Map<String, Object> taMap = (Map<String, Object>) taStMap.get(idTask);
			if(taMap==null){
				((List<Integer>) prMap.get("taskIdList")).add(idTask);
				taStMap.put(idTask, map);
				schemaCnt++;
			}
			Integer 
			pvid=(Integer) map.get("pvTaskid");
			if(pvid!=null)	inPvid+=","+pvid;
			pvid=(Integer) map.get("pvPrid");
			if(pvid!=null)	inPvid+=","+pvid;
		}
	}

	private Integer protocolCnt,schemaCnt;
	public Integer getSchemaCnt() {return schemaCnt;}
	public Integer getProtocolCnt() {return protocolCnt;}
	public void setProtocolCnt(Integer protocolCnt) {
		this.protocolCnt=protocolCnt;
	}

	HashMap<Integer, Map<String, Object>> docStateMap;
	public void setDocStateMap(HashMap<Integer, Map<String, Object>> docStateMap) {
		this.docStateMap = docStateMap;
	}
	public HashMap<Integer, Map<String, Object>> getDocStateMap() {
		return docStateMap;
	}

//	public void setDocStatus(ExplorerMtl e, List<Map<String, Object>> res) {
	public void setDocStatus( List<Map<String, Object>> res) {
		for (Map<String, Object> map : res){
			Integer taskId = (Integer) map.get("ref");
			Map<String, Object> taskAuthorStatusMap = getTaStMap().get(taskId);
			List<String> authorsList = (List<String>) taskAuthorStatusMap.get("authorsList");
			if(authorsList==null){
				authorsList=new ArrayList<String>();
				taskAuthorStatusMap.put("authorsList", authorsList);
				taskAuthorStatusMap.put("authorsMap", map);
			}
			Map<String,Map<String,Object>> authorsMap 
				= (Map<String,Map<String,Object>>) taskAuthorStatusMap.get("authorsMap");
			String author=(String) map.get("owuser");
			Map<String, Object> authorMap = authorsMap.get(author);
			if(authorMap==null){
				authorMap=map;
				authorMap.put("statusIdList", new ArrayList<Integer>());
				authorMap.put("statusenMap", new HashMap<Integer,Map<String,Object>>());
				authorsMap.put(author, authorMap);
				authorsList.add(author);
			}
			HashMap<Integer,Map<String,Object>> statusenMap 
				= (HashMap<Integer,Map<String,Object>>) authorMap.get("statusenMap");
			Integer statusId=(Integer) map.get("id");
			statusenMap.put(statusId, map);
			List<Integer> statusIdList=(List<Integer>) authorMap.get("statusIdList");
			statusIdList.add(statusId);
		}
	}

	Folder folderO;
	public Folder getFolderO() {return folderO;}
	public void setFolderO(Folder f) {this.folderO=f;}
	public String getFolderTypeName(){
		Folder f=folderO;
		while (!"folder".equals(f.getParentF().getFolder()))
			f=f.getParentF();
		return f.getFolder();
	}

	@Override
	public void paste() {
		// TODO Auto-generated method stub
		
	}

	public String getAction() {
		String ac = super.getAction();
		if(null==ac)
			ac=OwsSession.getRequest().getParameter("a");
		return ac;
	}

	Drug editDrugC;
	public Drug getEditDrugC() {
		if(null==editDrugC)	editDrugC=new Drug();
		return editDrugC;
	}
	String intention;

	public String getIntention() {return intention;}
	public void setIntention(String intention) {this.intention = intention;}

	private FormUtil formUtil;
	public FormUtil getFormUtil() {return formUtil;}
	public void setFormUtil(FormUtil formUtil) {
		this.formUtil=formUtil;
	}

}

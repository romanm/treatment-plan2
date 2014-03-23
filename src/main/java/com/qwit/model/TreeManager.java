package com.qwit.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.qwit.domain.App;
import com.qwit.domain.Arm;
import com.qwit.domain.Checkitem;
import com.qwit.domain.Checklist;
import com.qwit.domain.Contactperson;
import com.qwit.domain.Day;
import com.qwit.domain.Diagnose;
import com.qwit.domain.Document;
import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Expr;
import com.qwit.domain.Finding;
import com.qwit.domain.Folder;
import com.qwit.domain.History;
import com.qwit.domain.Institute;
import com.qwit.domain.Ivariable;
import com.qwit.domain.Labor;
import com.qwit.domain.Literature;
import com.qwit.domain.MObject;
import com.qwit.domain.Notice;
import com.qwit.domain.Nvariable;
import com.qwit.domain.Owuser;
import com.qwit.domain.Patient;
import com.qwit.domain.Personrole;
import com.qwit.domain.Protocol;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Symptom;
import com.qwit.domain.Tablet;
import com.qwit.domain.Task;
import com.qwit.domain.Times;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.domain.Url;
import com.qwit.service.DbNodeCreator;

public abstract class TreeManager implements Serializable{
	protected final Log log = LogFactory.getLog(getClass());
	abstract void runRule();
	
	public static short RIGHT_ReadNo	= 0;
	public static short RIGHT_ReadYes	= 1;
	public static short RIGHT_WriteYes	= 3;
	public static short RIGHT_AdminYes	= 5;

	public short getRightReadNo()	{return RIGHT_ReadNo;}
	public short getRightReadYes()	{return RIGHT_ReadYes;}
	public short getRightWriteYes()	{return RIGHT_WriteYes;}
	public short getRightAdminYes()	{return RIGHT_AdminYes;}

	public static final int _5000 = 5000;
	private static final long serialVersionUID = 1L;

	private HashMap<Integer, Integer> idClassMap = null;
	//obsolete
	/*
	public TreeManager setMap2copy() {
		idClassMap = new HashMap<Integer, Integer>();
		return this;
	}
	
	public TreeManager initIdClassMap() {
		idClassMap = new HashMap<Integer, Integer>();
		return this;
	}
	 * */
	protected boolean isClassOrVariant(Tree schemaT, Tree tree) {
		return schemaT.getIdClass()==tree.getIdClass();
	}

	public Tree previousSibling(Tree tree) {
		int childNr = tree.getParentT().getChildTs().indexOf(tree);
		if(childNr>0)
			return tree.getParentT().getChildTs().get(childNr-1);
		return null;
	}

	private short accessRight;
	public short getAccessRight() {return accessRight;}
	public void setAccessRight(short accessRight) {this.accessRight=accessRight;}

	public Map<Integer, Integer> getIdClassMap() {
		if(null==idClassMap)
			idClassMap=new HashMap<Integer, Integer>();
		return idClassMap ;
	}

	private String action;
	public boolean hasAction() {return null!=action&&action.length()>0;}
	public String getAction() {return action;}
	public void setAction(String action) {this.action = action;}

	private Notice editNoticeC;
	private String laborChain;
	private String editNoticeType;
	public String getLaborChain() {return laborChain;}
	public void setLaborChain(String laborChain) {this.laborChain = laborChain;}
	public void setEditNoticeC(Notice editNoticeC) {this.editNoticeC = editNoticeC;}
	public Notice getEditNoticeC(){return editNoticeC;}
	public void setEditNoticeType(String noticeType) {
		this.editNoticeType=noticeType;
	}
	
	public void openEditNotice()
	{
		openEditNotice(getEditNodeT());
	}
	static final public String NoticeLaborExtraDivider = ":extra:";
	public String getNoticeLaborExtraDivider() {//for EL
		return NoticeLaborExtraDivider;
	}
	public void openEditNotice(Tree editNodeT) {
		log.debug(editNodeT);
		editNoticeC=new Notice();
		if(editNodeT.getIdClass()!=null){
			Notice editNodeNoticeO=(Notice)editNodeT.getMtlO();
			editNoticeC.setNotice(editNodeNoticeO.getNotice());
			log.debug(editNodeNoticeO);
			if("labor".equals(editNodeNoticeO.getType())){
				int indexOf = editNodeNoticeO.getNotice().indexOf(TreeManager.NoticeLaborExtraDivider);
				if(indexOf>0){
					laborChain = editNodeNoticeO.getNotice().substring(0, indexOf);
					String notice = editNodeNoticeO.getNotice().substring(
							indexOf+TreeManager.NoticeLaborExtraDivider.length()
							,editNodeNoticeO.getNotice().length());
					editNoticeC.setNotice(notice);
				}
			}
			editNoticeC.setType(editNodeNoticeO.getType());
			if(editNodeT.getParentT().getParentT().getMtlO() instanceof Notice){
				editNoticeC.setType("day");
			}
		}
		if(editNoticeType!=null){
			editNoticeC.setType(editNoticeType);
			if("notice".equals(editNoticeType))
				editNoticeC.setType(null);
			editNoticeType=null;
		}
		log.debug(editNoticeC);
	}

	/*
	protected Tree parentT;
	public Tree getParentT() {return parentT;}
	protected void setParentT(Tree tree) {parentT = tree;}
	public void setParentT(Integer parentId) {
		Tree tree = getTree().get(parentId);
		setParentT(tree);
	}
	 * */

////	private JXPathContext jxpContext;
//	public JXPathContext getJXP(Tree t) {
//		return JXPathContext.newContext(getJXP(), t);
//	}
//	public JXPathContext getJXP() {
//		if(jxpContext==null)
//			jxpContext = JXPathContext.newContext(this);
//		return jxpContext;
//	}

	/**
	 * JXPath iterator to child with one tabName.
	 * @param tree - Parent tree.
	 * @param tabName
	 * @return iterator child with one tabName.
	 */
//	public Iterator childs(Tree tree, String tabName){
////		JXPathContext treeXPath = JXPathContext.newContext(tree);
//		JXPathContext treeXPath = getJXP(tree);
//		Iterator iteratePointers = treeXPath.iteratePointers("childTs[tabName='" +tabName +"']");
//		return iteratePointers;
//	}
//	public Object childs(Tree tree, String tabName, Integer n){
//		JXPathContext treeXPath = getJXP(tree);
//		return treeXPath.getPointer("childTs[tabName='" +tabName +"'][" +n +"]").getValue();
//	}
	
	
	public String float2string(Float value) {
		String attVal = "";
		if(value!=null){
			if(value>value.intValue())	attVal= value.toString();
			else attVal+=value.intValue();
		}
		return attVal;
	}
	
	public MObject classM(Tree tree) {
		Integer idClass = tree.getIdClass();
		return getClassM().get(idClass);
//		return mtlO(idClass);
	}
	
	private UserCareSession userCareSession;
	public void setUserCare(UserCareSession userCareSession) {this.userCareSession = userCareSession;}
	public UserCareSession getUserCare() {return userCareSession;}

	Set<Tree> removeNodeS;
	public Set<Tree> getRemoveNodeS() {return removeNodeS;}

	protected PatientMtl patientM;
	public PatientMtl getPatientMtl() {
		if(this instanceof PatientMtl)
			return (PatientMtl)this;
		return patientM;
	}
	public void setPatientMtl(PatientMtl patientM) {this.patientM=patientM;}

	private Integer					currentId;
	public Integer getCurrentId()	{return currentId;}
	public Integer nextCurrentId()	{return ++currentId;}

	private Integer ids;//source
	public Integer getIds() {return ids;}
	public void setIds(Integer ids) {this.ids = ids;}

	private Integer idt;//target
	public Integer getIdt()	{return idt;}
	public void setIdt(Integer idt)	{
		this.idt = idt;
		log.debug("----------"+idt);
	}
	public void closeEditId(){
		setIdt(null);
		setIdc(null);
	}
	public Boolean isNewEdit()	{return idt!=null&&idt>0;}

	private MObject mObj2Copy;
	public MObject getmObj2Copy() {return mObj2Copy;} 
	private Integer idClass2copy;
	public void setIdClass2copy(Integer idClass2copy) {this.idClass2copy = idClass2copy;}
	public Integer getIdClass2copy() {return idClass2copy;}

	private String prefixc;//copy prefix
	public String getPrefixc() {return prefixc;}
	public void setPrefixc(String prefixc) {this.prefixc = prefixc;}

	private Integer idc;//copy id
	public void setIdc(Integer idc)	{this.idc = idc;}
	public Integer getIdc()	{return idc;}
	public boolean hasIdc() {return null!=idc&&idc!=0&&getTree().containsKey(idc);}
	protected boolean hasIdt() {return null!=idt&&idt!=0;}

//	public boolean hasIdc()	{return null!=idc;}

	private Integer idf;//folder id
	public void setIdf(Integer idf) {this.idf = idf;}
	public Integer getIdf() {return idf;}
	
	public Tree addOfDate(Tree historyT, Tree patientT) {
		return addOfDate(historyT, patientT, getPatientMtl());
	}
	public Tree addOfDate(Tree historyT, Tree patientT, TreeManager doc2Mtl) {
		addHistory(historyT);
		Pvariable ofDateFixedC = getUserCare().getOfDateFixed();
		Tree ofDateFixedT = doc2Mtl.addChild(historyT, "pvariable", patientT);
		ofDateFixedT.setIdClass(ofDateFixedC.getId());
		addHistory(ofDateFixedT);
		return ofDateFixedT;
	}
	Pvariable ofDateFixedC;
	public void setOfDateFixedC(Pvariable ofDateFixedC) {this.ofDateFixedC = ofDateFixedC;}
	public Tree addOfDate3(Tree patientHistoryT, Tree patientT,Integer nextCurrentId
			, Pvariable ofdateFixO, Timestamp bdate) {
		addHistory(patientHistoryT);
		Tree ofDateFixedT = addChild(patientHistoryT, "pvariable", patientT, nextCurrentId);
		ofDateFixedT.setIdClass(ofdateFixO.getId());
		addHistory(ofDateFixedT,bdate);
		return null;
	}
	
	public void addHistory(Tree tree) {
		Timestamp mdate = new Timestamp(System.currentTimeMillis());
		addHistory(tree, mdate);
	}
	private void addHistory(Tree tree, Timestamp mdate) {
		History history = new History();
		history.setMdate(mdate);
		OwsSession owsSession = OwsSession.getOwsSession();
		log.debug("----"+owsSession);
		Integer idOwuser = owsSession.getIdOwuser();
		log.debug("----"+idOwuser);
		history.setIdauthor(idOwuser);
		tree.setSort(0-mdate.getTime());
		log.debug(history);
		tree.setHistory(history);
		history.setTree(tree);
	}
	
	void removeUse(MObject mObj, Map<?, Set<Tree>> useS) {
		if(mObj!=null&&useS.containsKey(mObj)){
			boolean dd = useS.get(mObj).remove(editNodeT);
			if(useS.get(mObj).size()==0){
				useS.remove(mObj);
				classM.remove(mObj.getId());
				mObj=null;
			}
		}
	}
	
	private Tree editNodeT;
	public Tree getEditNodeT() {return editNodeT;}
	protected void setEditNodeT(Tree editNodeT) {
		this.editNodeT=editNodeT;
		firstCallEditNode=false;
	}

//	public void setCopyNodeT(Tree copyNodeT) {this.copyNodeT = copyNodeT;}
	protected void addPaste(Tree parentT)
	{
		initOld2newIdM();
		log.debug("-------");
		pasteT(getCopyNodeT(), parentT);
		nextCurrentId();
	}
	public Tree pasteT(Tree copyNodeT, Tree parentT)
	{
		log.debug(copyNodeT+"\n to "+parentT);
		Tree pasteT = addChild(parentT, copyNodeT.getTabName(), getDocT());
		pasteT.setIdClass(copyNodeT.getIdClass());
		pasteT.setSort(copyNodeT.getSort());
		getOld2newIdM().put(copyNodeT.getId(), pasteT.getId());
		
		if(null!=copyNodeT.getChildTs())
		for (Tree tree : copyNodeT.getChildTs())
			pasteT(tree, pasteT);
		return pasteT;
	}
	public Tree addChild(Tree parentT, String tabName, Tree docT) {
		Integer nextCurrentId = nextCurrentId();
		Tree childT = addChild(parentT, tabName, docT, nextCurrentId);
//		docT.getDocNodes().add(childT);
//		log.debug("----"+docT.getDocNodes().size()+"/"+childT);
//		addTreePaar_depr(childT);
		return childT;
	}
	public Tree addChild(Tree parentT, String tabName, Tree docT,Integer nextId) {
		Tree childT = new Tree();
		childT.setId(nextId);
//		childT.setSort(childT.getId().longValue());
		childT.setSort(Calendar.getInstance().getTimeInMillis());
		childT.setTabName(tabName);
		addChild(parentT, childT);
		childT.setDocT(docT);
		getTree().put(childT.getId(), childT);
		return childT;
	}
	public void relocateEditNode(Tree parentT) {
		log.debug(parentT);
		log.debug(idt);
		log.debug(getIdt());
		setEditNodeT();
		log.debug(editNodeT);
		if(editNodeT.getParentT()!=null&&editNodeT.getParentT().getChildTs()!=null){
			editNodeT.getParentT().getChildTs().remove(editNodeT);
		}
		addChild(parentT, editNodeT);
		log.debug(getIdt());
		nextCurrentId();
		log.debug(getIdt());
		setIdt(null);
		//setEditNodeT();
	}
	private void addChild(Tree parentT, Tree childT) {
//		if(parentT.getChildTs()==null)
//			parentT.setChildTs(new ArrayList<Tree>());
//		parentT.getChildTs().add(childT);
		childT.setParentT(parentT);
	}
	/**
	 * Document specifically information.
	 * @param tree
	abstract void addTreePaar_depr(Tree tree);
	 */
	/**
	 * Paste is document type specifically.
	 */
	public abstract void paste();
//	private Tree copyNodeT;
	public TreeManager getCopyNodeDocMtl() {
		OwsSession owsSession = OwsSession.getOwsSession();
		if(owsSession==null)
			return null;
		TreeManager copyNodeDocMtl = owsSession.getCopyNodeDocMtl();
		return copyNodeDocMtl;
	}
	public Tree getCopyNodeT() {
		OwsSession owsSession = OwsSession.getOwsSession();
		if(owsSession==null)
			return null;
		Tree copyNodeT = owsSession.getCopyNodeT();
		return copyNodeT;
	}
	public void copy(){
		log.debug("-----------idt="+idt);
		log.debug("-----------idc="+idc);
		if(idc!=null){
			Tree copyNodeT = getTree().get(idc);
			log.debug("-----------"+copyNodeT);
			OwsSession owsSession = OwsSession.getOwsSession();
			if(copyNodeT!=null){
				MObject copyObjectM = getClassM().get(copyNodeT.getIdClass());
				//			log.debug("-----------"+copyObjectM);
				if(owsSession!=null){
					owsSession.setCopyNodeT(copyNodeT);
					owsSession.setCopyNodeDocMtl(this);
					owsSession.setCopyNodeId(copyNodeT.getIdClass());
				}
			}else{
				prefixc=owsSession.getRequest().getParameter("prefixc");
				log.debug("prefixc="+prefixc);
				if(this instanceof UserMtl || this instanceof DrugMtl){
					log.debug("idc="+idc);
					idClass2copy=idc;
					if(owsSession!=null){
						owsSession.setIdClass2copy(idClass2copy);
					}
				}else if(this instanceof ExplorerMtl){
					ExplorerMtl eMtl= (ExplorerMtl)this;
					Folder folderO = eMtl.getFolderO();
					log.debug(folderO);
					if(idc.equals(folderO.getId())){
						log.debug(folderO);
						owsSession.setCopyNodeDocMtl(this);
					}else{
						//we are about to copy something else as the folderO in explorer
						
						//copy protocols
						log.debug("copyNodeId:" + getIdc());
						owsSession.setCopyNodeId(getIdc());
						owsSession.setPrefixc(prefixc);
					}
				}
			}
			//			log.debug("-----------"+owsSession);
		}
	}
	Integer addNewEditNodeT(Tree parentT, String tabName) {
		Tree newT = addChild(parentT, tabName, getDocT());
		setEditNodeT(newT);
		setIdt(getEditNodeT().getId());
		return getIdt();
	}
	private boolean firstCallEditNode;
	public boolean isFirstCall(Tree editT){
		if(null==editNodeT) return false;
		if(editNodeT.getId().equals(editT.getId())){
			if(firstCallEditNode)
				return false;
			firstCallEditNode=true;
			return true;
		}
		return false;
	}
	public String getEditNodeName(){
		String name = null;
		if(editNodeT!=null)
			name=editNodeT.getTabName();
		return name;
	}
	public void setNewEditNodeT(Tree tree) {
		setIdt(tree.getId());
		setEditNodeT();
	}
	public void setEditNodeT(){
		log.debug(idt);
		if(!hasIdt()&&hasIdc()){
			idt=idc;
		}
		log.debug(idt);
		if(idt==null||idt==0){
			setEditNodeT(null);
			log.debug("-------");
		}else{
			log.debug("-------"+getDocT().getId());
			Tree editNodeT2 = getTree().get(idt);
			log.debug("-------"+editNodeT2);
			setEditNodeT(editNodeT2);
			if(null==editNodeT && this instanceof DrugMtl){
				DrugMtl dm = (DrugMtl)this;
				Ts ts = dm.getTsM().get(idt);
				if(null!=ts)
					setEditNodeT(ts.getTimesT());
			}
			log.debug("-------"+editNodeT);
			if(editNodeT==null&&getPatientMtl()!=null)
				setEditNodeT(getPatientMtl().getTree().get(idt));
			log.debug("-------"+editNodeT);
//			copy();
			idc=idt;
		}
	}
//	public void removeOldEditObj(MObject oldObjM){
//		if(oldObjM!=null){
//			getClassMUse(oldObjM).remove(getEditNodeT());
//			nextCurrentId();
//		}
//	}
	abstract void addMtlMap(MObject mtlC);
	
	
	public void deleteEditNode(EntityManager em){
		log.debug(getIdt());
		if(getIdt()!=null)
			editNodeT = getTree().get(getIdt());
		log.debug(editNodeT);
		if(editNodeT!=null){
			if("times".equals(editNodeT.getTabName())){
				delNotlast("times");
			}else if("day".equals(editNodeT.getTabName())){
				delNotlast("day");
//			}else if("pvariable".equals(editNodeT.getTabName())){
//				del();
//			}else if("notice".equals(editNodeT.getTabName())){
//				del();
//			}else if("drug".equals(editNodeT.getTabName())){
//				del();
			}else{
				del();
				log.debug("-------------");
				em.remove(editNodeT);
				log.debug("-------------");
			}
			nextCurrentId();
		}
	}
	private void delNotlast(String tabName) {
		boolean toDel = false;
		for(Tree t:editNodeT.getParentT().getChildTs())
			if(tabName.equals(t.getTabName())&&t!=editNodeT)
				toDel=true;
		if(toDel){
			del();
		}else{
			editNodeT.setIdClass(null);
		}
	}
	private void del() {
		editNodeT.getParentT().getChildTs().remove(editNodeT);
		removeNodeS.add(editNodeT);
	}
	public  Folder getFolderById(Integer idFolder, SimpleJdbcTemplate simpleJdbc)
	{
		String sql = "SELECT * FROM folder WHERE idfolder = ? ";
		List<Map<String, Object>>	res = simpleJdbc.queryForList(sql,idFolder);
		Folder f = null;
		if(res.size()>0){
			Map<String, Object> map = res.get(0);
			Integer	idfolder	= (Integer) map.get("idfolder");
			String	folder		= (String) map.get("folder");
			f = new Folder(idfolder,folder);
			log.debug(f);
		}
		/*
		String sql2 = "SELECT * FROM folder WHERE idfolder = :id ";
		Folder f = (Folder) em.createNativeQuery(sql2, Folder.class)
		.setParameter("id", idFolder)
		.getSingleResult();
		 * */
		return f;
	}
	public void addClassM(Tree tree, EntityManager em)
	{
		treeM.put(tree.getId(),tree);
		Integer idClass = tree.getIdClass();
		if(idClass==null)return;
		MObject mtlO = getClassM().get(idClass);
//		if(!getClassM().containsKey(idClass))
//		log.debug(tree);
		if(mtlO==null)
		{
			String tabName = tree.getTabName();
//			MClass objM;
			if(tabName.equals("app"))				mtlO=em.find(App.class, idClass);
			else if(tabName.equals("checkitem"))	mtlO=em.find(Checkitem.class, idClass);
			else if(tabName.equals("checklist"))	mtlO=em.find(Checklist.class, idClass);
			else if(tabName.equals("contactperson"))
													mtlO=em.find(Contactperson.class, idClass);
			else if(tabName.equals("day"))			mtlO=em.find(Day.class, idClass);
			else if(tabName.equals("dose"))			mtlO=em.find(Dose.class, idClass);
			else if(tabName.equals("expr"))			mtlO=em.find(Expr.class, idClass);
			else if(tabName.equals("folder"))		mtlO=em.find(Folder.class, idClass);
			else if(tabName.equals("finding"))		mtlO=em.find(Finding.class, idClass);
			else if(tabName.equals("labor"))		mtlO=em.find(Labor.class, idClass);
			else if(tabName.equals("literature"))	mtlO=em.find(Literature.class, idClass);
			else if(tabName.equals("notice"))		mtlO=em.find(Notice.class, idClass);
			else if(tabName.equals("owuser")){
				mtlO=em.find(Owuser.class, idClass);
			}
			else if(tabName.equals("personrole"))	mtlO=em.find(Personrole.class, idClass);
			else if(tabName.equals("institute"))	mtlO=em.find(Institute.class, idClass);
			else if(tabName.equals("document"))		mtlO=em.find(Document.class, idClass);
			else if(tabName.equals("diagnose"))		mtlO=em.find(Diagnose.class, idClass);
			else if(tabName.equals("patient"))		mtlO=em.find(Patient.class, idClass);
			else if(tabName.equals("protocol"))		mtlO=em.find(Protocol.class, idClass);
			else if(tabName.equals("pvariable"))	mtlO=em.find(Pvariable.class, idClass);
			else if(tabName.equals("ivariable"))	mtlO=em.find(Ivariable.class, idClass);
			else if(tabName.equals("nvariable"))	mtlO=em.find(Nvariable.class, idClass);
			else if(tabName.equals("symptom"))		mtlO=em.find(Symptom.class, idClass);
			else if(tabName.equals("studyarm"))		mtlO=em.find(Arm.class, idClass);
			else if(tabName.equals("tablet"))		mtlO=em.find(Tablet.class, idClass);
			else if(tabName.equals("task"))			mtlO=em.find(Task.class, idClass);
			else if(tabName.equals("url"))			mtlO=em.find(Url.class, idClass);
			else if(tabName.equals("times"))		mtlO=em.find(Times.class, tree.getIdClass());
			else if(tabName.equals("drug")){
				Drug drugO=em.find(Drug.class, idClass);
				getClassM().put(drugO.getGeneric().getId(), drugO.getGeneric());
				mtlO=drugO;
			}
//			log.debug(mtlO);
			getClassM().put(idClass, mtlO);
//			add1ClassObj(tree,mtlO);
		}
//		Set<Tree> classMUse = getClassMUse(objM);
//		classMUse.add(tree);
//		addTreeClassObj(mtlO,tree);
//		log.debug(tree);
		if(mtlO!=null){
			tree.setMtlO(mtlO);
			addIdclassTreeS(tree);
			ruleAddition(tree);
		}
	}

	private void ruleAddition(Tree tree) {
		if(this instanceof DrugMtl) {
			DrugMtl docMtl = (DrugMtl)this;
			if(docMtl.isExprO(tree)) {
				if("inputPauseForInit".equals(docMtl.getExprO(tree).getValue())) {
					docMtl.addInputPauseForInitT(tree);
				}
			}
		}
	}
	/**
	 * Add info to necessary map and set.
	 * @param objM
	 * @param tree
	 */
	abstract void addTreeClassObj_depr(MObject objM, Tree tree);
	/**
	 * Add document specific to 1 MeTaL class object 
	 * @param classM
	 */
	abstract void add1ClassObj_depr(Tree tree,MObject classM);

	private int msCnt;
	public int getMsCnt() {return msCnt;}
	public void setMsCnt(int msCnt) {this.msCnt = msCnt;}
	
	int node2read=0;
	public void setNode2read(int node2read) {this.node2read = node2read;}
	public void addClass(Tree tree,int idDoc, EntityManager em)
	{
		if(tree.getDocT()!=null&&(idDoc==tree.getDocT().getId()||idDoc==tree.getId())){
			node2read++;
			if(node2read>500){
				log.info("stop "+node2read);
				return;
			}
			if("times".equals(tree.getTabName())){
				getTimesM().put(tree.getId(), tree);
			}
			/*
			if(node2read>1 && tree.getId()==idDoc){
				log.info("2 mal to  "+idDoc +" after "+node2read);
				return;
			}
			long cms = System.currentTimeMillis();
			 * */
//			if(tree.getIdClass()!=null)
				//			if(isDocClass(tree))
				addClassM(tree,em);
//			treeM.put(tree.getId(),tree);
			if(tree.getRef()!=null)
				addRef(tree);
//			addTreePaar_depr(tree);
//			int l = (int) (System.currentTimeMillis()-cms);
//			msCnt+=l;
			//		if(l>10)
//			log.debug("+"+l+"="+msCnt+"/"+tree.getTabName());
//			long cms2 = System.currentTimeMillis();
//			int i=0;
			if(null!=tree.getChildTs())
			for(Tree childT:tree.getChildTs()){
					//				if(isThisDocumentTag(tree))
					//			if(tree.getDocT().equals(getDocT()))
//				if(childT.getId()!=childT.getParentT().getId())
//				if(childT.getId()!=tree.getId())
				if(null==childT.getDocT())
					continue;
				if(childT.getDocT().getId()==idDoc)
					addClass(childT,idDoc, em);
//				i++;
			}
//			int l2 = (int) (System.currentTimeMillis()-cms2);
//			log.debug("+"+l2+"="+msCnt+"/"+tree.getTabName()+"/"+tree.getId()+"/"+i);
		}
	}
	/**
	 * Is tag from this document.
	 * @param tree
	 */
	abstract boolean isThisDocumentTag(Tree tree);
	/*
	boolean isDocClass(Tree tree) {
		return
			tree.getTabName().equals("drug")
		||	tree.getTabName().equals("dose")
		||	tree.getTabName().equals("day")
		||	tree.getTabName().equals("diagnose")
		||	tree.getTabName().equals("app")
		||	tree.getTabName().equals("times")
		||	tree.getTabName().equals("expr")
		||	tree.getTabName().equals("notice")
		||	tree.getTabName().equals("finding")
		||	tree.getTabName().equals("labor")
		||	tree.getTabName().equals("task")
		||	tree.getTabName().equals("tablet")
		||	tree.getTabName().equals("owuser")
		||	tree.getTabName().equals("pvariable")
		||	tree.getTabName().equals("patient")
		||	tree.getTabName().equals("protocol")
		||	tree.getTabName().equals("symptom")
		||	tree.getTabName().equals("studyarm")
		||	tree.getTabName().equals("checklist")
		||	tree.getTabName().equals("checkitem")
		||	tree.getTabName().equals("contactperson");
	}
	* */
	

	private Tree docT;
	/**
	 * Map id to mtlO
	 */
	private Map<Integer, MObject>	classM;
	/**
	 * Map id to tree.
	 */
	private Map<Integer, Tree>		treeM;
	/**
	 * Map id to tree with tabName times.
	 */
	private Map<Integer, Tree>				timesM;
	/**
	 * Map of idClass and set of tree with it.
	 */
	Map<Integer, Set<Tree>>			idClass2TreeMS;
	
	public Map<Integer, Tree> getTree(){return treeM;}
	public Map<Integer, Tree>	getTimesM()	{
		if(timesM==null)
			timesM=new HashMap<Integer, Tree>();
		return timesM;
	}
	public Map<Integer, Set<Tree>> getIdClass2TreeMS() {return idClass2TreeMS;}
	public void addIdclassTreeS(Tree tree) {
		Set<Tree> set = idClass2TreeMS.get(tree.getIdClass());
		if(set==null){
			set=new HashSet<Tree>();
			idClass2TreeMS.put(tree.getIdClass(), set);
		}
		set.add(tree);
	}
	/**
	 * Map of id and set of ids from tree with ref to it.
	 */
	Map<Integer, Set<Integer>>		ref2idMS;
	public Map<Integer, Set<Integer>> getRef2idMS() {
		if(null==ref2idMS)
			ref2idMS=new HashMap<Integer, Set<Integer>>();
		return ref2idMS;
	}
	public void addRef(Tree tree) {
		Integer ref = tree.getRef();
		Set<Integer> refITs = getRef2idMS().get(ref);
		if(refITs==null){
			refITs=new ConcurrentSkipListSet<Integer>();
			ref2idMS.put(ref, refITs);
		}
		Integer id = tree.getId();
		refITs.add(id);
//		log.debug("old id="+id+" ref="+ref);
	}
	public void addRefR(Tree tree){
		if(tree.getRef()!=null)				addRef(tree);
		for(Tree childT:tree.getChildTs())	addRefR(childT);
	}
	Map<Integer, Integer>		old2newIdM=null;
	public Map<Integer, Integer> getOld2newIdM(){
		if(old2newIdM==null)
			old2newIdM = new HashMap<Integer, Integer>();
		return old2newIdM;
	}
	public void initOld2newIdM()	{old2newIdM	=new HashMap<Integer, Integer>();}
//	public void initRef2idMS()		{ref2idMS	=new HashMap<Integer, Set<Integer>>();}
	
	public void updateRef2(EntityManager em){
		for (Integer ref : ref2idMS.keySet()) {
			Integer newRef = old2newIdM.get(ref);
//			log.debug("old ref="+ref+" new ref="+newRef);
			for (Integer id : ref2idMS.get(ref)) {
				Tree newIdT = getTree().get(old2newIdM.get(id));
				newIdT.setRef(newRef);
//				log.debug("old id="+id+" new id="+newIdT.getId()+"\n"+newIdT);
				em.merge(newIdT);
			}
		}
	}
	public void updateRef(EntityManager em){
		log.debug("---------");
		for (Integer ref : ref2idMS.keySet()) {
			Integer newRef = old2newIdM.get(ref);
			log.debug("---------"+ref+">>"+newRef);
			if(newRef==null) continue;
			for (Integer id : ref2idMS.get(ref)) {
				Tree newIdT = getTree().get(old2newIdM.get(id));
				String sql = "UPDATE Tree SET ref="+newRef+" WHERE id="+newIdT.getId();
				log.debug("---------"+id+">>"+newIdT.getId()+" ref = "+newRef+"\n"+sql);
//				em.createNativeQuery(sql).executeUpdate();
				newIdT.setRef(newRef);
				em.merge(newIdT);
				log.debug("---");
				log.debug(newIdT);
			}
		}
	}
	public TreeManager(Tree docT) 
	{
		this.docT=docT;
		removeNodeS	= new HashSet<Tree>();
		classM		= new HashMap<Integer,MObject>();
//		classMUse	= new HashMap<MObject,Set<Tree>>();//one for all 117-125

		treeM		= new HashMap<Integer,Tree>();
		ref2idMS	= new HashMap<Integer, Set<Integer>>();
		idClass2TreeMS
					= new HashMap<Integer, Set<Tree>>();
		currentId=0;
//		copyNodeT=null;
	}
	public String getClassName(Object o){return o.getClass().getSimpleName().toLowerCase();}
	/**
	 * Integer=idClass, Object={drug,dose,...}
	 * @return
	 */
	public Map<Integer, MObject>	getClassM(){return classM;}
	public MObject idClassMtlO(String idt){
		int parseInt = Integer.parseInt(idt);
		MObject mObject = getClassM().get(parseInt);
		return mObject;
	}
	public Tree getDocT() {return docT;}

//	private Map<MObject, Set<Tree>>	classMUse;//new concept for 70-75
//	public Map<MObject, Set<Tree>> getClassMUse() {return classMUse;}
//	public Set<Tree> getClassMUse(MObject classM) {
//		if(!classMUse.containsKey(classM)) classMUse.put(classM, new HashSet<Tree>());
//		return classMUse.get(classM);
//	}
	public void addNewTreeObjM(MObject mClassObj,Tree t) {
		mClassObj.setId(nextCurrentId());
		addMObject(mClassObj, t);
	}
	public void registerNewObj(MObject editObjM,MObject oldObjM) {
		setEditNodeClass(editObjM);
//		removeOldEditObj(oldObjM);
	}
	public void addEditMObject(MObject objM) {
//		if(objM==null)return;
		addMObject(objM, editNodeT);
	}
	public void addMObject(MObject objM, Tree tree) {
		if(objM==null)return;
		getClassM().put(objM.getId(), objM);
		setNodeClass(objM, tree);
	}
	public void setEditNodeClass(MObject objM){
		setNodeClass(objM, editNodeT);
//		editNodeT.setIdClass(objM.getId());
//		getClassMUse(objM).add(editNodeT);
	}
	private void setNodeClass(MObject mtlO, Tree tree) {
		if(!mtlO.getId().equals(tree.getIdClass())){
			nextCurrentId();
		}
//		tree.setIdClass(mtlO.getId());
		tree.setMtlO(mtlO);
		tree.setTabName(instanceTree(mtlO));
//		getClassMUse(mClassObj).add(tree);
	}
	public void setInstance(Tree tree, String tabName) {
		tree.setMtlO(null);
		tree.setTabName(tabName);
	}
	private String instanceTree(MObject mtlO) {
		String tabName = mtlO.getClass().getSimpleName().toLowerCase();
		if(mtlO instanceof Arm)		tabName="studyarm";
		/*
		else if(mtlO instanceof Expr)		tabName="expr";
		else if(mtlO instanceof App)		tabName="app";
		else if(mtlO instanceof Checkitem)	tabName="checkitem";
		else if(mtlO instanceof Checklist)	tabName="checklist";
		else if(mtlO instanceof Contactperson)
											tabName="contactperson";
		else if(mtlO instanceof Day)		tabName="day";
		else if(mtlO instanceof Dose)		tabName="dose";
		else if(mtlO instanceof Drug)		tabName="drug";
		else if(mtlO instanceof Expr)		tabName="expr";
		else if(mtlO instanceof Folder)		tabName="folder";
		else if(mtlO instanceof Finding)	tabName="finding";
		else if(mtlO instanceof Labor)		tabName="labor";
		else if(mtlO instanceof Literature)	tabName="literature";
		else if(mtlO instanceof Notice)		tabName="notice";
		else if(mtlO instanceof Owuser)		tabName="owuser";
		else if(mtlO instanceof Personrole)	tabName="personrole";
		else if(mtlO instanceof Institute)	tabName="institute";
		else if(mtlO instanceof Document)	tabName="document";
		else if(mtlO instanceof Diagnose)	tabName="diagnose";
		else if(mtlO instanceof Patient)	tabName="patient";
		else if(mtlO instanceof Protocol)	tabName="protocol";
		else if(mtlO instanceof Protocol)	tabName="protocol";
		else if(mtlO instanceof Pvariable)	tabName="pvariable";
		else if(mtlO instanceof Symptom)	tabName="symptom";
		else if(mtlO instanceof Tablet)		tabName="tablet";
		else if(mtlO instanceof Task)		tabName="task";
		else if(mtlO instanceof Times)		tabName="times";
		 * */
		return tabName;
	}
	public void moveNode(String upDown) {
		Tree idcT = getTree().get(getIdc());
		log.debug(idcT);
		if(isNoticeO(idcT)){
			Notice noticeO = getNoticeO(idcT);
			String type = noticeO.getType();
			for(Tree tree:idcT.getParentT().getChildTs()) 
				if(isNoticeO(tree)&&type.equals(getNoticeO(idcT).getType()))
			{
			}
		}else{
			
		}
		log.debug(idcT);
		moveNode(idcT, upDown);
	}
	Tree t1T,t2T,tLastT;
	public void moveNode(Tree idcT, String upDown){
		log.debug(idcT);
		String tabName = idcT.getTabName();
		t1T = null;
		t2T = null;
		tLastT = null;
		boolean isChemo = false, sortItselfLike=false;
		if(this instanceof SchemaMtl)
		{
			isChemo = getDocT().getId()==idcT.getParentT().getId();
		}
		if((isNoticeO(idcT)||"drug".equals(idcT.getTabName()))&&isChemo)
			sortItselfLike=true;
		log.debug("isChemo="+isChemo);
		long sort=1;
		boolean up = "up".equals(upDown);
		for(Tree tree:idcT.getParentT().getChildTs())
		{
			log.debug(tree);
			tree.setSort(++sort);
			if(up){
				log.debug("--");
				if(tree==idcT)					t2T=tree;
				else if(isChemo&&"task".equals(tree.getTabName()))	continue;
				else if(sortItselfLike&&!tabName.equals(tree.getTabName()))	continue;
				else if(t2T==null){
					t1T=tree;
				}
			}else{//down
				if(tree==idcT)					t1T=tree;
				else if(isChemo&&"task".equals(tree.getTabName()))	continue;
				else if(sortItselfLike&&!tabName.equals(tree.getTabName()))	continue;
				else if(t1T!=null&&t2T==null)	t2T=tree;
			}
			tLastT = tree;
		}
		log.debug("-----"+t1T);
		log.debug("-----"+t2T);
		if(t1T!=null&&t2T!=null){
			Long t1Sort = t1T.getSort();
			t1T.setSort(t2T.getSort());
			t2T.setSort(t1Sort);
			nextCurrentId();
		}else if(t1T==null && t2T!=tLastT){
			t2T.setSort(sort++);
			log.debug("setsort: " + sort);
		}
		else
		{
			log.debug("%%%%% error %%%%%");
		}
		log.debug("-----"+t1T);
		log.debug("-----"+t2T);
	}
	public void editChild0(Tree parentT, DbNodeCreator treeCreator, MObject mtlO) {
		String tabName = instanceTree(mtlO);
		log.debug("---------"+parentT);
		Tree tree;
		if(parentT.hasChild())
			tree = parentT.getChildTs().get(0);
		else
			tree = treeCreator.addChild(parentT,tabName, 1, getDocT(), this);
		log.debug("---------"+tree);
		addMObject(mtlO, tree);
	}
	Map<Integer, Map<String, Object>> userM;
	public Map<Integer, Map<String, Object>> getUserM() {return userM;}
	public void readUser(SimpleJdbcTemplate simpleJdbc) {
		userM=new HashMap<Integer, Map<String, Object>>();
		String sql2 = "SELECT idauthor,owuser.*,c.* FROM tree t1,history , owuser, " +
				"(SELECT contactperson.*,t2.id AS idu FROM contactperson, tree t1 JOIN tree t2 ON t1.id=t2.did " +
				"WHERE t1.id=idcontactperson) c " +
				"WHERE t1.iddoc=? AND t1.id=idhistory " +
				"AND idauthor=idowuser AND idauthor=c.idu ";
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql2, getDocT().getId());
		for (Map<String, Object> map : queryForList) {
			Integer idauthor = (Integer) map.get("idauthor");
			userM.put(idauthor, map);
		}
	}
	//PatientData editor
	private List<Patient>	patientL;
	public void setPatientL(List<Patient> patientL) {this.patientL = patientL;}
	public List<Patient>	getPatientL()	{return patientL;}
	private Patient	editPatientC;
	public void setEditPatientC() {editPatientC = getPatientMtl().getPatientC();}
	public Patient	getEditPatientC()		{return editPatientC;}
	public void editStepPatient() {
		for(Patient p:patientL)
			if(p.getId().equals(getIdt()))
				editPatientC=p;
	}
	public void openEditPatient()
	{
		if(getPatientMtl()!=null){
			setEditPatientC();
			setBdate(editPatientC.getBirthdate());
		}else{
			editPatientC=new Patient();
			editPatientC.setPatient("");
			editPatientC.setForename("");
		}
	}
	
//	@DateTimeFormat(pattern = "MM-dd-yyyy")
	//Begindate editor
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	bdate;
	public Date getBdate() {return bdate;}
	public void setBdate(Date bdate) {this.bdate = bdate;}
	
	Folder newPatientF=null;
	public Folder getNewPatientF() {return newPatientF;}
	public void setNewPatient(Folder patientF){
		editPatientC.setBirthdate(new Timestamp(getBdate().getTime()));
		Calendar c = Calendar.getInstance();
		setBdate(c.getTime());
//		log.debug(patientF.getParentF());
		newPatientF=patientF;
		patientF.getFolder();//TODO was gegen lazy.
//		Hibernate.initialize(patientF);//TODO was gegen lazy.
		/*
		for(Folder newPatient1F:patientF.getChildFs()){
			if("new".equals(newPatient1F.getFolder())){
				newPatientF=newPatient1F;
			}
		}
		 * */
	}
	private Boolean					isPatientData;
	public Boolean getIsPatientData() {return isPatientData;}
	public void setIsPatientData(Boolean isPatientData) {this.isPatientData = isPatientData;}

	Patient patientO;
	public Patient getPatientO() {return patientO;}
	public Patient getPatientO(Tree docT) {return (Patient)docT.getMtlO();}
	public Patient initPatiantO(Integer idPatient, EntityManager em) {
		log.debug("idPatient="+idPatient);
		log.debug("patientO="+patientO);
		if(idPatient!=null&&(patientO==null||!idPatient.equals(patientO.getId())))
			patientO = em.find(Patient.class, idPatient);
		return patientO;
	}

	private Folder targetF;
	public void setTargetF(Folder targetF) {this.targetF = targetF;}
	public Folder getTargetF() {return targetF;}

	protected void pasteSourceTarget() {
		log.debug("getIdc()"+getIdc()+":"+"Copy: "+getCopyNodeT()+" getIdClass2copy()"+getIdClass2copy());
		if(null!=getIdClass2copy()){
			Tree t = getTree().get(getIdClass2copy());
			log.debug(t);
		}
		OwsSession owsSession = OwsSession.getOwsSession();
		if(getIdc()==null){
//		}else if(null!=getIdClass2copy()){
			
		}else if(getIdc().equals(3)){
			TreeManager copyNodeDocMtl = owsSession.getCopyNodeDocMtl();
			if(copyNodeDocMtl instanceof ExplorerMtl){
				ExplorerMtl eMtl = (ExplorerMtl) copyNodeDocMtl;
				Folder folderO = eMtl.getFolderO();
				log.debug(folderO);
				log.debug(eMtl.getIdc());
				if(eMtl.getIdc()!=null && folderO!=null && eMtl.getIdc().equals(folderO.getId()))
				{
					targetF=folderO;
				}
			}
		}else if(getIdc().equals(2)){
			owsSession.setSourceT(getCopyNodeT());
			owsSession.setSourceIdClass(getIdClass2copy());
		}else if(getIdc().equals(1)){
			owsSession.setTargetT(getCopyNodeT());
			owsSession.setTargetIdClass(getIdClass2copy());
		}
	}
	public void editStepCancel(){
		log.debug("------------");
		Tree editNodeT = getEditNodeT();
		log.debug("------------");
		if(null==editNodeT){
		}else if(editNodeT.getMtlO() instanceof Drug){
			Drug drugO=(Drug) editNodeT.getMtlO();
			log.debug("------------");
			if(!drugO.hasDrug()){
				log.debug("------------");
				deleteNode(editNodeT);
			}
		}else if("drug".equals(editNodeT.getTabName())){
			deleteNode(editNodeT);
		}else if(editNodeT.getMtlO() instanceof Notice){
			Notice noticeO=(Notice) editNodeT.getMtlO();
			if(!noticeO.hasNotice()){
				deleteNode(editNodeT);
			}
		}
		setIdt(null);
		setEditNodeT();
		OwsSession.getOwsSession().cancelSourceTarget();
	}
	public void deleteNode(Tree delT) {
		log.debug(getIdt());
		if("notice".equals(delT.getTabName())){
//			log.debug(getDdNE());
			log.debug(delT.getParentT());
//			List<Tree> list = getDdNE().get(delT.getParentT());
//			log.debug(list);
//			if(list!=null&&list.contains(delT))
//				list.remove(delT);
		}
		delT.getParentT().getChildTs().remove(delT);
		/*
		getTree().remove(delT);
		delT.setParentT(null);
		getDrugNoticeM();
		 * */
		nextCurrentId();
		if(delT.getId()>SchemaMtl._5000){
			log.debug("----------------");
		}
		removeNodeS.add(delT);
	}
	public void checkEditNodeId(Integer idt, Integer idc){
		log.debug("idt="+idt);
		log.debug("idc="+idc);
		setIdt(idt);
		if(!hasIdt() && null!=idc)
			setIdt(idc);
		
		log.debug("getIdt()="+getIdt());
		setEditNodeT();
		log.debug("getEditNodeT()="+getEditNodeT());
	}
//	return b||idt!=0;}


	Set<Integer> notFlowNotVariant;
	public Set<Integer> getNotFlowNotVariant() {return notFlowNotVariant;}
	public void setNotFlowNotVariant(Set<Integer> s) {notFlowNotVariant=s;}
	public boolean isLastAndOrExpr(Tree editNodeT) {
		return 
		"equality".equals(((Expr) editNodeT.getMtlO()).getExpr())
		&&
		"andOrExpr".equals((((Expr) editNodeT.getParentT().getMtlO())).getExpr())
		&&
		2==editNodeT.getParentT().getChildTs().size()
		;
		
	}
	public boolean isThenExpr(Tree editNodeT) {
		return "then".equals(((Expr) editNodeT.getMtlO()).getExpr());
	}
	public void adoption(Tree childT, Tree parentT) {
		if(null!=childT.getParentT())
			childT.getParentT().getChildTs().remove(childT);
		childT.setParentT(parentT);
	}
	public static DateTime getDateTime(Date mdate) {
		DateTime dateTime = new DateTime(mdate.getTime());
		return dateTime;
	}
	public static DateTime nextTherapyBegin(Date mdate,int duration) {
		DateTime dateTime = new DateTime(mdate.getTime()).plusDays(duration);
		return dateTime;
	}

	private Tree newDefinitionT;
	public Tree getNewDefinitionT() {return newDefinitionT;}
	public void setNewDefinitionT(Tree newT) {newDefinitionT=newT;}
	protected HashMap<Integer, Tree> idClassTaskMap = null;
	public Map<Integer, Tree> getIdClassTaskMap(){
		if(null==idClassTaskMap)
			this.idClassTaskMap=new HashMap<Integer,Tree>();
		return idClassTaskMap;
	}

	private Tree docT2copy;
	public Tree getDocT2new() {return docT2copy;}
	public void setDocT2new(Tree conceptT) {docT2copy=conceptT;}

	public static boolean	isFolderO(Tree t)	{return null!=t && t.getMtlO() instanceof Folder;}
	public static boolean	isDiagnoseO(Tree t)	{return null!=t && t.getMtlO() instanceof Diagnose;}
	public static boolean	isIvariableO(Tree t){return null!=t && t.getMtlO() instanceof Ivariable;}
	public static boolean	isNoticeO(Tree t)	{return null!=t && t.getMtlO() instanceof Notice;}
	public static Ivariable	getIvariableO(Tree t){return (Ivariable) t.getMtlO();}
	public static Nvariable	getNvariableO(Tree t){return (Nvariable) t.getMtlO();}
	public static Diagnose	getDiagnoseO(Tree t){return (Diagnose) t.getMtlO();}
	public static Folder	getFolderO(Tree t)	{return (Folder) t.getMtlO();}
	public static Notice	getNoticeO(Tree t)	{return (Notice) t.getMtlO();}
	public static Task		getTaskO(Tree t)		{return (Task) t.getMtlO();}
	
}

package com.qwit.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

import com.qwit.domain.Drug;
import com.qwit.domain.Folder;
import com.qwit.domain.MObject;
import com.qwit.domain.Owuser;
import com.qwit.domain.Tree;

public class InstituteMtl extends TreeManager implements Serializable{
	private static final long serialVersionUID = 1L;

	public InstituteMtl(Tree docT) {
		super(docT);
	}

	void runRule() {
	}
	//	public String handleAction(String a){
//		log.debug(a);
//		String handleAction=null;
//		if(null!=a&&a.length()>0){
//			handleAction=a;
//		}else if(hasAction()){
//			handleAction=getAction();
//		}else if(null!=getIdt()){
//			handleAction=getEditNodeName();
//		}
//		if("paste".equals(handleAction)){
//			OwsSessionContainer owsSession = OwsSessionContainer.getOwsSession();
//			if(null!=getIdc()&&getIdc().equals(3)){
//				TreeManager copyNodeDocMtl = owsSession.getCopyNodeDocMtl();
//				log.debug(copyNodeDocMtl);
//				Tree copyNodeT = owsSession.getCopyNodeT();
//				log.debug(copyNodeT);
//				if(copyNodeT!=null&&"contactperson".equals(copyNodeT.getTabName())){
//					handleAction="pasteUser";
//					log.debug("-----------");
//				}else if(copyNodeDocMtl instanceof ExplorerMtl){
//					ExplorerMtl eMtl = (ExplorerMtl) copyNodeDocMtl;
//					folderO = eMtl.getFolderO();
//					handleAction="pasteFolder";
//				}
//			}
//		}
//		return handleAction;
//	}
	private Folder folderO;
	public void setFolderO(Folder folderO) {this.folderO=folderO;}
	public Folder getFolderO() {return folderO;}	

	/*
	@Override
	void addTreePaar_depr(Tree tree) {
		// TODO Auto-generated method stub
	}
	 */

	@Override
	public void paste() {
		pasteSourceTarget();
	}
	
	@Override
	void addMtlMap(MObject mtlC) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void addTreeClassObj_depr(MObject objM, Tree tree) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void add1ClassObj_depr(Tree tree, MObject classM) {
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean isThisDocumentTag(Tree tree) {
		// TODO Auto-generated method stub
		return false;
	}
	
	List<Drug> tradeList;
	public List<Drug> getTradeList() {return tradeList;}
	public void setTradeList(List<Drug> tradeList) {this.tradeList = tradeList;}
	
	List<Map<String, Object>> patientSchemaListe;
	public List<Map<String, Object>> getPatientSchemaListe() {return patientSchemaListe;}
	public void setPatientSchemaListe(List<Map<String, Object>> patientSchemaListe) {this.patientSchemaListe = patientSchemaListe;}

	private String newStation;
	public String getNewStation() {return newStation;}
	public void setNewStation(String newStation) {this.newStation = newStation;}
	
	private String newStationUserContactperson,newStationUserForename,newStationUserLoginName;
	public String getNewStationUserLoginName() {return newStationUserLoginName;}
	public void setNewStationUserLoginName(String newStationUserLoginName) {this.newStationUserLoginName = newStationUserLoginName;}
	public String getNewStationUserContactperson() {return newStationUserContactperson;}
	public void setNewStationUserContactperson(String newStationUserContactperson) {this.newStationUserContactperson = newStationUserContactperson;}
	public String getNewStationUserForename() {return newStationUserForename;}
	public void setNewStationUserForename(String newStationUserForename) {this.newStationUserForename = newStationUserForename;}

	private Map<Integer, Map<String, Object>> folderAdminMap;
	public void setFolderAdminMap(Map<Integer, Map<String, Object>> folderAdminMap) {
		this.folderAdminMap=folderAdminMap;
	}
	public Map<Integer, Map<String, Object>> getFolderAdminMap() {return folderAdminMap;}
	private List<Map<String, Object>> userFolderList;
	public List<Map<String, Object>> getUserFolderList() {
		return userFolderList;
	}
	public void setUserFolderList(List<Map<String, Object>> userFolderList) {
		this.userFolderList=userFolderList;
	}
	public Owuser getOwuser() {
		Pointer pointer = JXPathContext.newContext(getDocT()).getPointer("childTs[tabName='owuser']");
		log.debug(pointer);
		Tree value = (Tree) pointer.getValue();
		log.debug(value);
		Owuser mtlO = (Owuser) value.getMtlO();
		return mtlO;
	}

	private boolean isAdminStation;
	public boolean isAdminStation() {return isAdminStation;}
	public void setAdminStation(boolean isAdminStation) {
		this.isAdminStation = isAdminStation;
	}
}

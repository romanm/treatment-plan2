package com.qwit.util;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Notice;
import com.qwit.domain.Protocol;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Tree;

public class FormUtil implements Serializable{
	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	String name,schemaView,viewState,mode,popup
	,jsonDrugs,jsonGeneric,jsonDoseUnit,jsonDoseApp
	, folderTreeJSON
	, jsonLabor, jsonLaborUnits,jsonSymptoms, jsonFinding, jsonFindingUnits;

	public String getJsonFindingUnits() {
		return jsonFindingUnits;
	}
	public void setJsonFindingUnits(String jsonFindingUnits) {
		this.jsonFindingUnits = jsonFindingUnits;
	}
	public String getJsonLaborUnits() {
		return jsonLaborUnits;
	}
	public void setJsonLaborUnits(String jsonLaborUnits) {
		this.jsonLaborUnits = jsonLaborUnits;
	}
	public String getJsonFinding() {
		return jsonFinding;
	}
	public void setJsonFinding(String jsonFinding) {
		this.jsonFinding = jsonFinding;
	}
	public String getJsonSymptoms() {return jsonSymptoms;}
	public void setJsonSymptoms(String jsonSymptoms) {this.jsonSymptoms = jsonSymptoms;}
	public String getJsonLabor() {return jsonLabor;}
	public void setJsonLabor(String jsonLabor) {this.jsonLabor = jsonLabor;}

	Notice editNoticeC=null;
	public Notice getEditNoticeC() {
		if(editNoticeC==null)	editNoticeC=new Notice();
		return editNoticeC;
	}

	Pvariable pvO=null;
	public Pvariable getPvO() {
		if(pvO==null)	pvO=new Pvariable();
		return pvO;
	}

	Protocol newDoc=null;
	public Protocol getNewDoc()
	{
		if(newDoc==null)	newDoc=new Protocol();
		return newDoc;
	}
	Tree folderT;
	public Tree getFolderT()				{return folderT;}
	public void setFolderT(Tree folderT)	{this.folderT = folderT;}

	String part;
	public String getPart()				{return part;}
	public void setPart(String part)	{this.part = part;}

	/**
	 * @return the patientTreeJSON
	 */
	public String getFolderTreeJSON() {	return folderTreeJSON;}
	/**
	 * @param patientTreeJSON the patientTreeJSON to set
	 */
	public void setFolderTreeJSON(String s) {this.folderTreeJSON = s;}
	
	public String getJsonDoseApp() {
		return jsonDoseApp;
	}
	public void setJsonDoseApp(String jsonDoseApp) {
		this.jsonDoseApp = jsonDoseApp;
	}
	public void logline(Boolean i)
	{
		log.debug("--------------------"+i);
	}
	public void logerror(String i){
		log.error(
				"\n-------FLOW ERROR-------------\n"
				+i
				+"\n-------FLOW ERROR-------------\n"
				);
	}
	public void logline(String i){
		log.debug("--------------------"+i);
	}
	public void logline(int i)
	{
		log.debug("--------------------"+i);
	}
	public String getJsonDoseUnit() {
		return jsonDoseUnit;
	}
	public void setJsonDoseUnit(String jsonDoseUnit) {
		this.jsonDoseUnit = jsonDoseUnit;
	}
	public String getJsonGeneric() {return jsonGeneric;}
	public void setJsonGeneric(String jsonGeneric) {this.jsonGeneric = jsonGeneric;}
	public String getJsonDrugs() {return jsonDrugs;}
	public void setJsonDrugs(String jsonDrugs) {this.jsonDrugs = jsonDrugs;}
	
	public String getPopup() {return popup;}
	public void setPopup(String popup) {this.popup = popup;}
	public String getMode(){return mode;}
	public void setMode(String mode) {this.mode = mode;}
	public String getViewState(){return viewState;}
	public void setViewState(String viewState){this.viewState = viewState;}
	Integer idt;
	public Integer getIdt()
	{
		return idt;
	}
	public void setIdt(Integer idt)
	{
		this.idt = idt;
	}
	public String getSchemaView()
	{
		return schemaView;
	}
	public void setSchemaView(String schemaView)
	{
		this.schemaView = schemaView;
	}
	
	@SuppressWarnings("unchecked")
	public FormUtil()
	{
		name="Name1";
	}
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	private Tree newTaskT;
	public Tree getNewTaskT() {return newTaskT;}
	public void setNewTaskT(Tree newTaskT) {this.newTaskT = newTaskT;}
	private int drugFolderId;
	public int getDrugFolderId() {return drugFolderId;}
	public void setDrugFolderId(int drugFolderId) {this.drugFolderId=drugFolderId;}
}

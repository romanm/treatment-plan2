package com.qwit.util;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.format.annotation.DateTimeFormat;

import com.qwit.domain.Drug;
import com.qwit.domain.Folder;
import com.qwit.domain.Patient;
import com.qwit.domain.Protocol;
import com.qwit.domain.Tree;

public class FlowObjCreator implements Serializable
{
	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	
	/**
	 * Identifier of import document.
	 */
	private Integer idimport;
	public Integer getIdimport() {return idimport;}
	public void setIdimport(Integer idimport) {this.idimport = idimport;}

	private String genericName;
	public String getGenericName()	{return genericName;}
	public void setGenericName(String genericName)	{this.genericName = genericName;}

	private String laborName,laborUnit;

	public String getLaborName() {return laborName;}
	public void setLaborName(String laborName) {this.laborName = laborName;}
	public String getLaborUnit() {return laborUnit;}
	public void setLaborUnit(String laborUnit) {this.laborUnit = laborUnit;}

	/**
	 * Folder id.
	 */
	private String folder;
	public void setFolder(String folder) {this.folder = folder;}
	public String getFolder() {return folder;}

	private Folder folderO;
	public Folder getFolderO() {return folderO;}
	public void setFolderO(Folder folderO) {this.folderO = folderO;}

	private Integer idf;
	public void setIdf(Integer idf)	{this.idf = idf;}
	public Integer getIdf()			{return idf;}

	/**************************************************
	 * Patient Section
	 **************************************************/
	Patient newPatient=null;
	public Patient getNewPatient()
	{
		if(newPatient==null)	newPatient=new Patient();
		return newPatient;
	}
	@DateTimeFormat(pattern = "MM-dd-yyyy")
	private Date	bdate;
	public Date getBdate() {return bdate;}
	public void setBdate(Date bdate) {this.bdate = bdate;}

	private Integer day,month,year;	
	public Integer getDay() {return day;}
	public void setDay(Integer day) {this.day = day;}
	public Integer getMonth() {return month;}
	public void setMonth(Integer month) {this.month = month;}
	public Integer getYear() {return year;}
	public void setYear(Integer year) {this.year = year;}

	/**************************************************
	 * Protocol Section
	 **************************************************/
	Tree oldProtocolT;
	public Tree getOldProtocolT() {return oldProtocolT;}
	public void setOldProtocolT(Tree oldProtocolT) {this.oldProtocolT = oldProtocolT;}

	Protocol newProtocol=null;
	public Protocol getNewProtocol()
	{
		if(newProtocol==null)	newProtocol=new Protocol();
		return newProtocol;
	}
	private Drug drugO;
	public Drug getDrugO() {return drugO;}
	public void setDrugO(Drug drug) {this.drugO=drug;}
	
	private HashMap<Tree, Tree> importIdToSynchron;
	public HashMap<Tree, Tree> getImportIdToSynchron() {
		if(null==importIdToSynchron)
			importIdToSynchron=new HashMap<Tree, Tree>();
		return importIdToSynchron;
	}
	
}
package com.qwit.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.qwit.domain.Contactperson;
import com.qwit.domain.MObject;
import com.qwit.domain.Owuser;
import com.qwit.domain.Tree;

//public class UserMtl extends TreeManager implements Serializable{
public class UserMtl extends InstituteMtl implements Serializable{
	
	//Copy Patient
//	List<Map<String, Object>> copyPatientenList;
//	Map<Integer,Map<String, Object>> copyPatientenMap;
//	int cntCopiedPatient, cntNotCopiedPatient;

//	public Map<Integer, Map<String, Object>> getCopyPatientenMap() {return copyPatientenMap;}
//	public List<Map<String, Object>> getCopyPatientenList() {return copyPatientenList;}
//	public int getCntCopiedPatient() {return cntCopiedPatient;}
//	public int getCntNotCopiedPatient() {return cntNotCopiedPatient;}

//	public void setCopyPatientenList(List<Map<String, Object>> copyPatientenList) {
//		this.copyPatientenList = copyPatientenList;
//		copyPatientenMap=new HashMap<Integer, Map<String,Object>>();
//		cntCopiedPatient=0;
//		cntNotCopiedPatient=0;
//		for (Map<String, Object> map : copyPatientenList) {
//			Integer idpatient =(Integer) map.get("idpatient");
//			Integer idt =(Integer) map.get("idt");
//			if(null==idt){
//				cntNotCopiedPatient++;
//			}else{
//				cntCopiedPatient++;
//			}
//			copyPatientenMap.put(idpatient, map);
//		}
//	}
	//END: Copy Patient

	private Tree sourceT;
	public Tree getSourceT() {return sourceT;}
	public void setSourceT(Tree sourceT) {this.sourceT = sourceT;}

	public UserMtl(Tree docT) {
		super(docT);
	}

	private static final long serialVersionUID = 1L;

	@Override
	void add1ClassObj_depr(Tree tree, MObject classM) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void addMtlMap(MObject mtlC) {
		// TODO Auto-generated method stub
		
	}

	/*
	@Override
	void addTreeClassObj_depr(MObject objM, Tree tree) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void addTreePaar_depr(Tree tree) {
		// TODO Auto-generated method stub
		
	}
	 */

	@Override
	boolean isThisDocumentTag(Tree tree) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void paste() {
		pasteSourceTarget();
	}
	
	private String	oldPass;
	public void setOldPass(String oldPass) {this.oldPass = oldPass;	}
	public String getOldPass() {return oldPass;}
 
	private String	newPass;
	public String getNewPass() {return newPass;	}
	public void setNewPass(String newPass) {this.newPass = newPass;	}

	private String	newPassRetyped;
	public void setNewPassRetyped(String newPassRetyped) {this.newPassRetyped = newPassRetyped;}
	public String getNewPassRetyped() {return newPassRetyped;}


	public String getUserName() {
		String username = null;
		for(Tree owuserT:getDocT().getChildTs())
			if(owuserT.getMtlO() instanceof Owuser){
				Owuser owuserO = (Owuser)owuserT.getMtlO();
				if(!"drroman".equals(owuserO.getOwuser()))
					username=owuserO.getOwuser();
			}
		return username;
	}
	public Contactperson getUser() {
		Contactperson mtlO = (Contactperson) getDocT().getMtlO();
		return mtlO;
	}

	private List<Map<String, Object>> replaceSchemaL;
	public List<Map<String, Object>> getReplaceSchemaL() {return replaceSchemaL;}
	public void setReplaceSchemaL(List<Map<String, Object>> replaceSchemaL) {this.replaceSchemaL=replaceSchemaL;}

	private String seekLogic;
	public String getSeekLogic() {return seekLogic;}
	public void setSeekLogic(String seekLogic) {this.seekLogic=seekLogic;}
	private Map<Integer, Short> userFolderRightM;
	public Map<Integer, Short> getUserFolderRightM() {return userFolderRightM;}
	public void setUserFolderRightM(Map<Integer, Short> userFolderRightM) {this.userFolderRightM=userFolderRightM;}


}

package com.qwit.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.qwit.domain.Pvariable;
import com.qwit.domain.Tree;
import com.qwit.service.DbPvariableCreator;
import com.qwit.service.DocumentService;


public class UserCareSession implements Serializable{
	private static final long serialVersionUID = 1L;
	protected final Log log = LogFactory.getLog(getClass());

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
	public String getUserName()	{return userName;}
	public void setUserName(String userName) {this.userName = userName;}
	public Integer getUserId()		{return userName2id.get(userName);}
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

	private Pvariable ofDateFixed, procent100;
	public Pvariable getProcent100()	{return procent100;}
	public Pvariable getOfDateFixed()	{return ofDateFixed;}

	/**
	 *  missing description! what does this Function? set does it set the SchemaMtlService? - no wrong function name!
	 */
	@Autowired @Qualifier("pvCreator") DbPvariableCreator	pvCreator;
	public void set(DocumentService schemaMtlService) {
		if(procent100==null){
			procent100 = schemaMtlService.getPvariable("percent","100","%");
//			procent100 = pvCreator.setPvariable("percent").setPvalue("100").setUnit("%").read();
		}
		if(ofDateFixed==null){
			ofDateFixed = schemaMtlService.getPvariable("ofDate","fixed","");
//			ofDateFixed = pvCreator.setPvariable("ofDate").setPvalue("fixed").read();
		}
	}

	SchemaMtl schemaCareMtl=null;
	public SchemaMtl getSchemaCareMtl() {return schemaCareMtl;}
	public void setSchemaCareMtl(SchemaMtl schemaCareMtl) {this.schemaCareMtl=schemaCareMtl;}
	
}

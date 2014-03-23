package com.qwit.service;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

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
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Folder;
import com.qwit.domain.History;
import com.qwit.domain.MObject;
import com.qwit.domain.Notice;
import com.qwit.domain.Owuser;
import com.qwit.domain.Patient;
import com.qwit.domain.Protocol;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;
import com.qwit.model.ConceptMtl;
import com.qwit.model.DrugMtl;
import com.qwit.model.InstituteMtl;
import com.qwit.model.OwsSession;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.model.UserMtl;
import com.qwit.util.FlowObjCreator;

@Repository
public class MtlDbService extends MtlDbEntityManager {

	protected void addPaste(Tree parentT,TreeManager docMtl)
	{
		Tree copyNodeT = owsSession.getCopyNodeT();
		addPaste(parentT, docMtl, copyNodeT);
	}
	protected void addPaste(Tree parentT, TreeManager docMtl, Tree copyNodeT) {
		docMtl.initOld2newIdM();
		log.debug("-------"+copyNodeT);
		Tree pastedNodeT = pasteT(copyNodeT, parentT,docMtl);
		
//		if(docMtl instanceof SchemaMtl)
//		{
//			if( "task".equals(pastedNodeT.getTabName()) || 
//				"drug".equals(pastedNodeT.getTabName()) || 
//				"day".equals(pastedNodeT.getTabName()) || 
//				"times".equals(pastedNodeT.getTabName()) || 
//				"notice".equals(pastedNodeT.getTabName()) || 
//				"expr".equals(pastedNodeT.getTabName())	)
//			{
//				//add history
//				addHistory(pastedNodeT);
//			}
//		}
//		else if(docMtl instanceof ConceptMtl)
//		{
//			if("expr".equals(pastedNodeT.getTabName()))
//			{
//				//add history
//				addHistory(pastedNodeT);
//			}
//		}
		addHistory(pastedNodeT);
//		addHistory(docMtl.getDocT());
	}
	private Tree pasteT(Tree copyNodeT, Tree parentT, TreeManager docMtl){
		Tree pasteT = addPaste1T(copyNodeT, parentT, docMtl);
		
		if(copyNodeT.hasChild())
			for (Tree t : copyNodeT.getChildTs())
				pasteT(t, pasteT,docMtl);
		return pasteT;
	}
	Tree addPaste1T(Tree node2copyT, Tree parentT, TreeManager docMtl) {
		log.debug(node2copyT+"\n to "+parentT);
//		Tree pasteT = addChild(parentT, copyNodeT.getTabName(), docMtl.getDocT());
		Tree pasteT = docMtl.addChild(parentT, node2copyT.getTabName(), docMtl.getDocT(), nextDbid());
		pasteT.setIdClass(node2copyT.getIdClass());
		pasteT.setSort(node2copyT.getSort());
		docMtl.getOld2newIdM().put(node2copyT.getId(), pasteT.getId());
		return pasteT;
	}
	@Transactional
	public void user2institute(InstituteMtl docMtl){
		Tree docInstituteT = docMtl.getDocT();
		Tree copyNodeContactpersonT = owsSession.getCopyNodeT();
		log.debug("--------------"+copyNodeContactpersonT);
		user2institute(docInstituteT, copyNodeContactpersonT);
	}

	protected void user2institute(Tree docInstituteT, Tree copyNodeContactpersonT) {
		long timeInMillis = Calendar.getInstance().getTimeInMillis();
		Tree userT = treeCreator.add1Child(docInstituteT, "contactperson", timeInMillis, docInstituteT);
		log.debug(userT);
		userT.setIdClass(copyNodeContactpersonT.getIdClass());
		log.debug(userT);
	}
	@Transactional
	public void user2institute4admin(UserMtl docMtl) {
		log.debug(2);
		Tree docInstituteT = em.find(Tree.class, docMtl.getIdc());
		log.debug(docInstituteT);
		Tree copyNodeContactpersonT = docMtl.getCopyNodeT();
		log.debug(copyNodeContactpersonT);
		user2institute(docInstituteT, copyNodeContactpersonT);
	}

	protected void saveNewPatient(Tree folderT, Patient newPatientO) {
		Tree newPatientT = setNewDocT("patient",folderT);
		log.debug("-----------"+newPatientT);
		newPatientO.setId(newPatientT.getId());
		log.debug("-----------"+newPatientO);
		em.persist(newPatientT);
		em.persist(newPatientO);
		log.debug("-----------");
	}
	
	@Autowired @Qualifier("taskNoticeCreator")	TaskNodeCreator		taskNoticeCreator;
	public void addNotice(DrugMtl docMtl, String noticeType) {
		Tree noticeT = taskNoticeCreator.setTreeManager(docMtl).addChild();
		Notice noticeO = new Notice();
		noticeO.setType(noticeType);
		noticeT.setMtlO(noticeO);
		docMtl.setIdt(noticeT.getId());
		docMtl.setEditNoticeType(noticeType);
	}

	protected void addPatientHistoryRecord(Tree patientT,String tagName,Integer idClass) {
		Tree oldFolderT = addChild(tagName, patientT);
		log.debug("------------"+oldFolderT);
		oldFolderT.setIdClass(idClass);
		oldFolderT.setSort(0-Calendar.getInstance().getTimeInMillis());
		addOfDate(oldFolderT);
	}
	protected Tree addChild(String tagName, Tree parentT) {
		return addChild(tagName, parentT, parentT.getDocT());
	}
	protected Tree addChild(String tagName, Tree parentT, Tree docT) {
		Tree t = new Tree();
		addAtt(t,tagName,parentT, docT);
		/*
		 * is in tree.setParentT(xx);
		if(!parentT.hasChild())
			parentT.setChildTs(new ArrayList<Tree>());
		parentT.getChildTs().add(t);
		 * */
		return t;
	}
	protected String inInstitute() {
		String inInstitute = "";
		Integer stationId=null;
		Map<String, Object> stationM = owsSession.getStationM();
		log.debug(stationM);
		if(null!=stationM)
			stationId=(Integer) stationM.get("idinstitute");
		log.debug(stationId);
		List<Map<String, Object>> userStationList = owsSession.getUserStationList();
		if(null!=userStationList)
			for (Map instituteM : userStationList) 
			{
				Integer idInstitute=(Integer) instituteM.get("idinstitute");
				if(null==stationId||!stationId.equals(idInstitute))
					inInstitute+=", "+idInstitute;
			}
		if(inInstitute.length()>0)
			inInstitute = inInstitute.substring(1);
		return inInstitute;
	}
	protected String inInstitute_old() {
		String inInstitute = "";
		/*
		Institute stationO = owsSession.getStationO();
		List<Institute> userStationList = owsSession.getUserStationList();
		if(null!=userStationList)
			for (Institute instituteO : userStationList) 
				if(null==stationO||!stationO.getId().equals(instituteO.getId()))
					inInstitute+=", "+instituteO.getId();
		if(inInstitute.length()>0)
			inInstitute = inInstitute.substring(1);
		 */
		return inInstitute;
	}
	protected String getSqlInstitutepatientFolder(String inInstitute) {
		String sqlInstitutepatientFolder = 
		" SELECT folder2O.idFolder FROM Tree instituteT, Tree folder1T, Tree  folder2T " +
		" ,Folder folder1O, Folder folder2O" +
		" WHERE instituteT.id IN (" +inInstitute +")" +
		" AND instituteT.id=folder1T.did AND folder1T.id=folder2T.did " +
		" AND folder1T.idClass=folder1O.idFolder AND folder2T.idClass=folder2O.idFolder" +
		" AND folder1O.folder='patient'";
		return sqlInstitutepatientFolder;
	}
	protected Tree getTreeById(TreeManager docMtl, Integer id) {
		Tree tree ;
		if(0==id){
			tree = new Tree();
			tree.setId(0);
		}else{
			tree = em.find(Tree.class, id);
			docMtl.addClassM(tree, em);
		}
		return tree;
	}

	public void setSessionStationM() {
		Principal userPrincipal = OwsSession.getRequest().getUserPrincipal();
		String userName = null==userPrincipal?null:userPrincipal.getName();
		setSessionStationM(userName);
	}
	private void setSessionStationM(String userName) {
		if(null!=userName){
//			if(true || null==owsSession.getStationO()){
			if(null==owsSession.getStationM()){
				setUserStationList(userName);
				log.debug("--------------");
//				setStationO(userName);
			}
		}
	}
	
	public Tree checkTreeById(Integer id) {
		Tree treeById = checkId(id);
		if(null!=treeById)
		{
			SchemaMtl docMtl = new SchemaMtl(treeById);
			docMtl.addClassM(treeById,em);
			docMtl.addClassM(treeById.getParentT(),em);
			docMtl.addClassM(treeById.getDocT(),em);
		}
		return treeById;
	}
	@Transactional
	public int changePatientId(Integer id2import) {
		int nextId = nextDbid();
		String sql = "UPDATE Tree SET id=? WHERE tab_name='patient' AND id=?";
		log.debug(sql+" "+nextId+" "+id2import);
		int updateNr = simpleJdbc.update(sql, nextId,id2import);
		return updateNr;
	}
	@Transactional
	public int changeId(Integer id2import) {
		return changeIdnotTransaction(id2import);
	}

	protected int changeIdnotTransaction(Integer id2import) {
		int nextId = nextDbid();
		return changeNotIdClassId(id2import, nextId);
	}

	public Tree checkId(Integer id2import) {
		Query createQuery = em.createQuery("SELECT t FROM Tree t WHERE t.id=:id");
		List<Tree> resultList = createQuery.setParameter("id", id2import).getResultList();
		log.debug(resultList);
		boolean b = resultList.size()>0;
		Tree treeById=b?resultList.get(0):null;
		return treeById;
	}
	protected int changeNotIdClassId(Integer id2import, Integer nextId) {
		String qlString = "UPDATE Tree  SET id="+nextId+" WHERE (idClass IS NULL OR id!=idClass) AND id="+id2import;
		log.debug(qlString);
		int executeUpdate = simpleJdbc.update(qlString);
////		String qlString = "UPDATE Tree t SET t.id=:nextId WHERE (t.idClass IS NULL OR t.id!=t.idClass) AND t.id=:id2import";
//		String qlString = "UPDATE Tree t SET t.id="+nextId+" WHERE (t.idClass IS NULL OR t.id!=t.idClass) AND t.id="+id2import;
//		log.debug(qlString);
//		Query createQuery = em.createQuery(qlString);
////		log.debug(createQuery);
////		createQuery.setParameter(0, nextId);
////		log.debug(createQuery);
////		createQuery.setParameter(1, id2import);
//		log.debug(createQuery);
//		int executeUpdate = createQuery.executeUpdate();
		log.debug(executeUpdate);
		return executeUpdate;
		/*
		String sql = "UPDATE Tree SET id=? WHERE (idclass IS NULL OR id!=idclass) AND id=?";
		log.debug(sql.replaceFirst("\\?", nextId.toString()).replaceFirst("\\?", id2import.toString()));
		int updateNr = simpleJdbc.update(sql, nextId, id2import);
		Tree find = em.find(Tree.class, nextId);
		log.debug(find);//This row not to delete. It's work only with this row.
		return updateNr;
		 */
	}

	@Autowired JpaImportDao owsImportDao;
	@Transactional
	public void copyProtocol(FlowObjCreator foc){
		OwsSession.getOwsSession().getUserId(simpleJdbc);//make user id
		Integer idimport = foc.getIdimport();
		log.debug(idimport);
		Protocol protocolO = owsImportDao.setFoc(foc).protocolCopy(idimport);
		log.debug(protocolO);
		Tree protocolT = em.find(Tree.class, protocolO.getId());
		log.debug(protocolT);

		ConceptMtl conceptMtl = new ConceptMtl(protocolT).setMap2copy();
		log.debug(conceptMtl);
		conceptMtl.addHistory(protocolT);
		log.debug(foc.getIdf());
		
		Tree folderT = em.find(Tree.class, foc.getIdf());
		log.debug(folderT);
		owsImportDao.buildCopyProtocol(idimport,conceptMtl,folderT,em);
		log.debug("------------1"+protocolT);
//		em.merge(protocolT);
		log.debug("------------2");
	}

	@Transactional
	public void importProtocol(FlowObjCreator foc){
//		OwsSessionContainer.getOwsSessionContainer().getUserId(simpleJdbc);//make user id
		OwsSession.getOwsSession().getUserId(simpleJdbc);//make user id
		Integer idimport = foc.getIdimport();
		log.debug(idimport);
		Protocol protocolO = owsImportDao.setFoc(foc).protocol(idimport);
		log.debug(protocolO);
		Tree protocolT = em.find(Tree.class, protocolO.getId());
		log.debug(protocolT);

		ConceptMtl conceptMtl = new ConceptMtl(protocolT).setMap2copy();
		log.debug(conceptMtl);
		conceptMtl.addHistory(protocolT);
		log.debug(foc.getIdf());
		
		Tree folderT = em.find(Tree.class, foc.getIdf());
		log.debug(folderT);
		owsImportDao.buildImportProtocol(idimport,conceptMtl,folderT,em);
		log.debug("------------1"+protocolT);
//		em.merge(protocolT);
		log.debug("------------2");
	}
	protected boolean lastDefinitionSchema(Tree definitionSchemaT) {
		boolean lastDefinitionSchema = true;
		for(Tree dsT:definitionSchemaT.getParentT().getChildTs())
			if("task".equals(dsT.getTabName())&&dsT!=definitionSchemaT)
				lastDefinitionSchema=false;
		return lastDefinitionSchema;
	}
//	protected void trashSchemaDefinition(ExplorerMtl docMtl, Tree delT) {
	protected void trashSchemaDefinition(TreeManager docMtl, Tree delT) {
		Integer schemaId = docMtl.getIdc();
		List<Map<String, Object>> sqlMap2 = simpleJdbc.queryForList(
		" SELECT * FROM tree t1,task,patient " +
		" WHERE t1.did=idpatient AND t1.idclass=idtask AND idclass=?",schemaId);
		if(sqlMap2.size()>0){
			move2trash(delT);
		}else{
			String sql = "SELECT * FROM tree where idclass=? AND id!=idclass AND iddoc!=?";
			sqlMap2 = simpleJdbc.queryForList(sql,schemaId,delT.getDocT().getId());
			if(sqlMap2.size()>0){
				move2trash(delT);
			}else{
				String sql1 = "DELETE FROM tree WHERE iddoc=? AND idclass=? AND id!=idclass";
				log.debug("----------- "+sql1+" "+delT.getDocT().getId()+" "+schemaId);
				simpleJdbc.update(sql1,delT.getDocT().getId(), schemaId);
				String sql2 = "DELETE FROM tree WHERE id=?";
				log.debug("----------- "+sql2+" "+schemaId);
				simpleJdbc.update(sql2, schemaId);
			}
		}
	}
//	private void move2trash(ExplorerMtl docMtl, Tree delT) {
	private void move2trash(Tree delT) {
		Integer placeOldIdClass = delT.getDocT().getId();
		List<Map<String, Object>> sqlMap = simpleJdbc.queryForList(
		" SELECT t2.id FROM folder,protocol,tree t1,tree t2 " +
		" WHERE t1.did=idfolder AND t1.id=idprotocol " +
		" AND t1.id=t2.did AND folder='trash' AND protocol='trash' AND t2.tab_name='definition' ");
		int idDefinition = (Integer) sqlMap.get(0).get("id");
		log.debug("----------- "+idDefinition);
		Tree definitionT = em.find(Tree.class, idDefinition);
		delT.setDocT(definitionT.getDocT());
		move2trash_depr(definitionT, delT, placeOldIdClass, "protocol");
	}
	//move2trash
	private Tree getTrashFolderT() {
		int idTrash = simpleJdbc.queryForInt(
				" SELECT f1.idfolder FROM folder f1,folder f0 " +
				" WHERE f0.idfolder=f1.fdid AND f1.folder='trash' AND f0.folder='folder' ");
		Tree trashFolderT = em.find(Tree.class, idTrash);
		return trashFolderT;
	}
	protected void move2trashFolder(Tree delT) {
		log.debug(delT);
		Tree trashFolderT = getTrashFolderT();
		log.debug(trashFolderT);
		delT.setParentT(trashFolderT);
		log.debug(delT);
		/*
		if("folder".equals(delT.getTabName()))
		{
			Folder delF = em.find(Folder.class, delT.getId());
			log.debug(delF);
			Folder trashFolderF = em.find(Folder.class, trashFolderT.getId());
			delF.setParentF(trashFolderF);
			log.debug(delF);
		}
		 */
		Tree placeOldT = treeCreator.add1Child(delT, delT.getParentT().getTabName(), 0, trashFolderT);
		log.debug(placeOldT);
		placeOldT.setIdClass(delT.getParentT().getId());
		addHistory(placeOldT);
		
	}
//	protected void move2trash(ExplorerMtl docMtl, Tree parentT, Tree delT, int placeOldIdClass, String tabName) {
	protected void move2trash_depr(Tree parentT, Tree delT, int placeOldIdClass, String tabName) {
		delT.setParentT(parentT);
//		Tree placeOldT = treeCreator.addChild(delT,tabName, 0, delT, docMtl);
		Tree placeOldT = treeCreator.add1Child(delT,tabName, 0, delT);
		addHistory(placeOldT);
		placeOldT.setIdClass(placeOldIdClass);
//		docMtl.addHistory(placeOldT);
		em.merge(delT);
	}
	//END:move2trash
	@Autowired @Qualifier("treeCreator")	DbNodeCreator		treeCreator;
	/**
	 * Sets new directory.
	 * 
	 * @param idf -- Parent folder id.
	 * @param folder -- New directory name.
	 * @param userId -- User identifier.
	 * @param userName -- User name.
	 */
	protected void newFolder(Integer idf, String folder, Integer userId, String userName) {
		log.debug(idf);
		Tree folderT = em.find(Tree.class, idf);
		log.debug(folderT);
		Folder folderO = em.find(Folder.class, idf);
		log.debug(folderO);
		Tree newFolderT = new Tree();
		newFolderT.setTabName("folder");
		log.debug(newFolderT);
		newFolderT.setParentT(folderT);
		newFolderT.setId(nextDbid());
		newFolderT.setIdClass(newFolderT.getId());
		newFolderT.setDocT(getFolderTree());
		log.debug(newFolderT);
//		owsSession.getUserId(simpleJdbc);
//		eMtl.addHistory(newFolderT);
		addHistory(newFolderT,userId);
		Folder newFolderO = new Folder();
//		String folder = foc.getFolder();
		log.debug(folder);
		newFolderO.setFolder(folder);
		newFolderO.setParentF(folderO);
		newFolderO.setId(newFolderT.getId());
		log.debug(newFolderO);
		em.merge(folderT);
		log.debug(1);
		em.persist(newFolderO);
		log.debug(2);
		/*
		String sql="UPDATE folder SET folder=? WHERE idfolder=?";
		simpleJdbc.update(sql, foc.getFolder(), idf);
		**/
		log.debug(newFolderO);
		newFolderO=em.find(Folder.class, newFolderO.getId());
		em.flush();
		log.debug(newFolderO);
		log.debug(userName);
		folder2noderole(userName, newFolderO);
		String sql = "UPDATE noderole SET write='1', admin='1'  WHERE idnode=? AND username=? ";
		log.debug(sql+" -- "+newFolderO.getId()+" "+userName);
		simpleJdbc.update(sql, newFolderO.getId(),userName);
	}
	

	/**
	 * Set security role to folder.
	 * @param username -- User name or station name
	 * @param folderO -- Folder object for new role sets.
	 */
	protected void folder2noderole(String username, Folder folderO) {
		log.debug("--------------"+folderO);
		Integer idFolder = folderO.getId();
		String sql = " WITH RECURSIVE fr AS ( " +
		" SELECT f.* FROM folder AS f WHERE f.idfolder = "+idFolder+" UNION " +
		" SELECT f.* FROM folder AS f, fr WHERE fr.idfolder = f.fdid ) " +
		" SELECT " +
		"'INSERT INTO noderole (idnode,username) VALUES ('||idfolder||',\"" +username +"\")' " +
		" FROM fr LEFT JOIN noderole ON fr.idfolder=idnode AND username='" +username +"' " +
		" WHERE idnode IS NULL ";
		log.debug("--------------"+sql);
		DbUpdate dbUpdate = new DbUpdate();
		dbUpdate.setDbUpdateDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		dbUpdate.setSimpleJdbc(getSimpleJdbc());
		dbUpdate.setSqlL(new ArrayList<String>());
		dbUpdate.getSqlL().add(sql);
		log.debug("--------------"+folderO);
		folderO=folderO.getParentF();
		while (!"folder".equals(folderO.getFolder())) {
			log.debug("--------------"+folderO);
			String sql1 = " SELECT * FROM tree,noderole WHERE " +
			" id=? AND id=idnode AND (username='all' OR username=?) ";
			log.debug(sql1+" -- "+folderO.getId()+" "+username);
			List<Map<String, Object>> l = simpleJdbc.queryForList(
			sql1,folderO.getId(),username);
			log.debug("--------------"+l.size());
			if(l.size()>0)
				break;
			String sql2 = "INSERT INTO noderole (idnode,username)" +
			" VALUES ("+folderO.getId()+",'" +username+ "')";
			log.debug(sql2);
			dbUpdate.getSqlL().add(sql2);
			folderO=folderO.getParentF();
		}
		log.debug("--------------");
		dbUpdate.update("structure");
		log.debug("--------------");
	}
	private String getMd5Password(String password) {
		Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
		String hashedPassword = passwordEncoder.encodePassword(password, null);
		return hashedPassword;
	}
	protected Tree addOwuser(String password, Tree newContactpersonT, String userName) {
		String hashedPassword = getMd5Password(password);

		Tree owuserT=addChild("owuser", newContactpersonT);
		em.persist(owuserT);
		log.debug(owuserT);

		Owuser newUserO = new Owuser();
		newUserO.setId(owuserT.getId());
		newUserO.setOwuser(userName);
		newUserO.setPword(hashedPassword);
//		newUserO.setPword(ruf.getPassword());
		newUserO.setLang("de");
		newUserO.setOwrole("edit");
		newUserO.setOwview("x");
		log.debug(newUserO);
//		newUserO.setOwview("t");
		owuserT.setMtlO(newUserO);
		em.persist(newUserO);
		/*
		Users user= new Users();
		user.setEnabled(true);
		user.setUsername(userName);
		user.setPassword(hashedPassword);
		 * */
//		u.setPassword(ruf.getPassword());
		
		//zum selbst erweitern
		//insert into users

//		em.find(Owuser.class, newUserO.getId());
		em.flush();
//		simpleJdbc.update("INSERT INTO users (username,password,enabled)VALUES(?,?, B'0')",userName,hashedPassword);
		simpleJdbc.update("INSERT INTO users (username,password,enabled)VALUES(?,?, B'1')",userName,hashedPassword);
		simpleJdbc.update("INSERT INTO authorities (username,authority)VALUES(?,'ROLE_USER')",userName);

		return owuserT;
	}
	private String		drugs="{}";
	@Transactional(readOnly = true)
	public String getJsonGeneric(){return getJsonDrugs("SELECT t FROM Drug t WHERE t.generic.id=t.id");}
	@Transactional(readOnly = true)
	public String getJsonDrugs(){return getJsonDrugs("SELECT t FROM Drug t ORDER BY t.drug");}
	private String getJsonDrugs(String sql) {
		long sT = System.currentTimeMillis();
		//if(drugs.equals("{}")){
			List<Drug> drL = em.createQuery(sql).getResultList();
			try {
				JSONObject put = new JSONObject().put("identifier", "name").put("label", "name");
				JSONArray ja = new JSONArray();
				for (Drug drug : drL) {
					ja.put(new JSONObject().put("name", drug.getDrug()));
				}
				put.put("items", ja);
				drugs = put.toString();
			} catch (JSONException e) {e.printStackTrace();}
		//}
		return drugs;
	}

	protected Tree saveNewDocRoot(Integer idf, String docName) {
		log.debug(idf);
//		Tree fT=em.find(Tree.class, foc.getIdf());
		Tree folderT = readDocT(idf);
		log.debug(folderT);
		Tree newProtocolT = setNewDocT(docName,folderT);
		log.debug(newProtocolT);
		newProtocolT.setParentT(folderT);
		return newProtocolT;
	}
	protected Tree setNewDocT(String tagName,Tree parentT) {
		Tree newDocT = setNewDocTWithoutHistory(tagName, parentT);
		addHistory(newDocT);
		return newDocT;
	}
	protected Tree setNewDocTWithoutHistory(String tagName, Tree parentT) {
		Tree newDocT = new Tree();
		addAtt(newDocT,tagName,parentT, newDocT);
		newDocT.setId(nextDbid());
		newDocT.setIdClass(newDocT.getId());
		newDocT.setDocT(newDocT);
		return newDocT;
	}

	private void delete(TreeManager docMtl) {
		for (Tree tree : docMtl.getRemoveNodeS()){
			updateRef(tree,docMtl);
			deleteNative(tree);
		}
	}
	protected void deleteNative(TreeManager schemaMtl) {
		for (Tree tree : schemaMtl.getRemoveNodeS()){
			updateRef(tree,schemaMtl);
			deleteNative(tree);
		}
	}
	protected void updateRef(Tree drugT, TreeManager schemaMtl) {
//		private void updateRef(Tree drugT, SchemaMtl schemaMtl) {
		if(drugT.getParentT()==null)return;//??
		if("task".equals(drugT.getParentT().getTabName())){
//			if(schemaMtl.getDrugDay().get(drugT)!=null)
//			for (Tree dayT : schemaMtl.getDrugDay().get(drugT)) {
			if(drugT.getChildTs()!=null)
			for (Tree dayT : drugT.getChildTs()) {
//				if(schemaMtl.getDayTime()!=null&&schemaMtl.getDayTime().get(dayT)!=null)
//				for (Tree timesT : schemaMtl.getDayTime().get(dayT)) {
				if(dayT.getChildTs()!=null)
				for (Tree timesT : dayT.getChildTs()) 
						if("times".equals(timesT.getTabName())){
					Set<Integer> set = schemaMtl.getRef2idMS().get(timesT.getId());
					if(set!=null)
					for (Integer id : set) {
						Tree refTimesT = schemaMtl.getTree().get(id);
						String sql = "UPDATE Tree SET ref=null, idClass=null WHERE id="+id;
						log.debug("---------------------"+sql);
						em.createNativeQuery(sql).executeUpdate();
//						refTimesT.setRef(null);
//						refTimesT.setIdClass(null);
//						em.merge(refTimesT);
					}
				}
			}
		}
	}
	protected void deleteNative(Tree tree) {
		String sql = "DELETE FROM Tree WHERE id="+tree.getId();
		log.debug("---------------------"+sql);
		em.createNativeQuery(sql).executeUpdate();
	}

	protected long getHistorySort() {
		long timeInMillis = Calendar.getInstance().getTimeInMillis();
		return 0-timeInMillis;
	}
	protected void copyTaskAll(SchemaMtl schemaMtl, Tree fromSchemaT) {
		Tree toSchemaT=schemaMtl.getDocT();
		schemaMtl.initOld2newIdM();
		schemaMtl.addRefR(fromSchemaT);
		copyChild2Patient(schemaMtl, fromSchemaT, toSchemaT, toSchemaT,true);
		mergeTree(toSchemaT);
		log.debug("---------");
		schemaMtl.updateRef(em);
	}

//	private Tree copyNode2(SchemaMtl schemaMtl, Tree parentNewT, Tree docNewT, Tree tree2copy) {
	protected Tree copyNode2(TreeManager schemaMtl, Tree parentNewT, Tree docNewT, Tree tree2copy) {
		Tree newT = copyNode(schemaMtl, parentNewT, docNewT, tree2copy);
		schemaMtl.getOld2newIdM().put(tree2copy.getId(), newT.getId());
//		log.debug("oldId="+tree2copy.getId()+" newId="+newT.getId());
//		log.debug(schemaMtl.getOld2newIdM().get(id));
//		log.debug(newT);
		return newT;
	}
	protected void mergeTree(Tree docT) {
//		List<History> historyL=new ArrayList<History>();
//		mergeTree1(docT,historyL);
		mergeTree1(docT);
		em.merge(docT);
	}
	private void mergeTree1(Tree parentT) {
		if(parentT.getChildTs()!=null)
		for(Tree tree:parentT.getChildTs()){
			if(tree.getId()<TreeManager._5000){
				tree.setId(nextDbid());
				log.debug(tree);
				if(tree.getHistory()!=null){
					tree.getHistory().setIdhistory(tree.getId());
//					historyL.add(tree.getHistory());
				}
			}
//			mergeTree1(tree, historyL);
			mergeTree1(tree);
		}
	}
	protected Tree copyNode(TreeManager schemaMtl, Tree parentNewT, Tree docNewT, Tree tree2copy) {
		Tree newT=schemaMtl.addChild(parentNewT, tree2copy.getTabName(), docNewT, nextDbid());
		newT.copyAtt(tree2copy);
		return newT;
	}
	protected void copyChild2Patient(TreeManager schemaMtl, Tree parentT2copy, Tree parentNewT, Tree docNewT,boolean isSchema) {
		if(parentT2copy.getChildTs()!=null)
		for(Tree tree2copy:parentT2copy.getChildTs()){
			if("definition".equals(tree2copy.getTabName())) continue;
			Tree newT = copyNode2(schemaMtl, parentNewT, docNewT, tree2copy);
			if(!isSchema&&"task".equals(tree2copy.getTabName())) continue;
			log.debug(newT);
			//for start schema.
//			log.debug("------------------------------schemaMtl.getIdc()--"+schemaMtl.getIdc());
//			log.debug("------------------------------schemaMtl.getDocT().getIdClass()--"+schemaMtl.getDocT().getIdClass());
//			log.debug("------------------------------tree2copy.getIdClass()--"+tree2copy.getIdClass());
			if(null==schemaMtl.getDocT()){}
			else if(schemaMtl.getIdc()==null&&schemaMtl.getDocT().getIdClass().equals(tree2copy.getIdClass())){
				log.debug("--------------------------------");
				schemaMtl.setIdc(newT.getId());
				log.debug("--------------------------------"+schemaMtl.getIdc());
			}
			
			//pv 100% for chemo dose.
			if(schemaMtl.getPatientMtl()==null){}
			else if	(	tree2copy.getParentT().getParentT().getId().equals(tree2copy.getDocT().getId())
				&&	tree2copy.getTabName().equals("dose")
				)
			{
				/*
				if(procent100==null){
					log.debug("---");
					procent100 = getPvariable("percent","100","%");
				}
				 */
//				Pvariable pv100C = schemaMtl.getUserCare().getProcent100();
				Tree pvT=schemaMtl.addChild(newT, "pvariable", docNewT, nextDbid());
				pvT.setIdClass(getProcent100().getId());
//				pvT.setIdClass(pv100C.getId());
				log.debug(pvT);
			}
			copyChild2Patient(schemaMtl, tree2copy, newT, docNewT,isSchema);
		}
	}
	public SchemaMtl schemaMtl(TreeManager docMtl) {return (SchemaMtl) docMtl;}
	public Tree addOfDate(Tree historyT) {
		log.debug(historyT);
		addHistory(historyT);
//		Tree ofDateFixedT = addChild(historyT, "pvariable", patientT);
//		Tree ofDateFixedT = addChild(historyT, "pvariable", patientT, nextDbid());
		Tree ofDateFixedT = addChild("pvariable", historyT);
		ofDateFixedT.setIdClass(getOfDateFixed().getId());
//		Pvariable ofDateFixedC = getUserCare().getOfDateFixed();
//		ofDateFixedT.setIdClass(ofDateFixedC.getId());
//		Integer ofDateFixedId = OwsSessionContainer.getOwsSession().getOfDateFixedId();
//		Integer ofDateFixedId = owsSession.getOfDateFixedId();
//		log.debug("ofDateFixedId="+ofDateFixedId);
		
		addHistory(ofDateFixedT);
		return ofDateFixedT;
	}
	private Pvariable	procent100, ofDateFixed;

	public Pvariable getProcent100() {
		if(null==procent100)
			procent100 = (Pvariable)pvCreator.setPvariable("percent").setPvalue("100").setUnit("%").build();
		return procent100;
	}
	public Pvariable getOfDateFixed() {
		if(null==ofDateFixed)
			ofDateFixed = (Pvariable)pvCreator.setPvariable("ofDate").setPvalue("fixed").setUnit("").build();
		return ofDateFixed;
	}
	protected void persistMtlObject(MObject mtlC) {
		String	name	= mtlC.getClass().getSimpleName().toLowerCase();
		Tree sourceT = setClassT(name);
		log.debug(sourceT);
		mtlC.setId(sourceT.getId());
		log.debug(mtlC);
		if(mtlC instanceof Drug){
			Drug drugC = (Drug) mtlC;
			drugC.setGeneric(drugC);
			log.debug(drugC);
			sourceT.setDocT(sourceT);
			log.debug(sourceT);
		}
		log.debug(sourceT);
		//em.persist(sourceT);
		//test von Ruslan
		em.merge(sourceT);
		log.debug(mtlC);
		em.persist(mtlC);
		log.debug("---------------");
//		em.flush();//Flush for two some insert in one transaction!
		log.debug("---------------");
	}
	protected Tree setClassT(String name) {
		long t = Calendar.getInstance().getTimeInMillis();
		Tree parentT = getSourceFolderT(name);
		log.debug((Calendar.getInstance().getTimeInMillis()-t)/1000);
		log.debug(parentT);
		return setClassT(name, parentT);
	}
	protected Tree setClassT(String name, Tree parentT) {
		Tree sourceT = new Tree();
		sourceT.setTabName(name);
		int nextId = nextDbid();
		log.debug("-------------"+nextId);
		sourceT.setId(nextId);
		sourceT.setIdClass(nextId);
		sourceT.setSort(System.currentTimeMillis());
		sourceT.setParentT(parentT);
		log.debug(sourceT);
		return sourceT;
	}

	@Autowired @Qualifier("findFolderWithName") DbNodeCreator findFolderWithName;
	//TODO: decide move or not to ExplorerMTLService
	/**
	 * Folder tree object as parent for MClass drug,day,times... (for source).
	 * @param tableName Name of MClass table and simultaneously value of folder field in folder table.
	 * @return Tree object.
	 */
	private Tree getSourceFolderT(String tableName)
	{
		return findFolderWithName.find(tableName);
	}
	protected MObject seekObjM(MObject classM) {
		MObject dbObjectM=null;
		if(classM instanceof Pvariable){
			log.debug("----ddddddddddddddddddddd------if(classM instanceof Pvariable) == true");
			Pvariable pv=(Pvariable) classM;
//			dbObjectM=getPvariable(pv.getPvariable(), pv.getPvalue(), pv.getUnit());
			dbObjectM=pvCreator.setPv(pv).build();
		}else if(classM instanceof Dose){
			Dose doseC=(Dose) classM;
			List dd = doseL(doseC);
			if(dd.size()>0)
				dbObjectM=(MObject) dd.get(0);
		}
		return dbObjectM;
	}
	@Autowired @Qualifier("pvCreator")		DbPvariableCreator	pvCreator;
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List doseL(Dose editDoseC) {
		log.debug(editDoseC);
		List dL = em.createQuery("SELECT d FROM Dose d WHERE d.value = :value AND d.unit=:unit AND d.app=:app AND d.pro=:pro")
		.setParameter("value",	editDoseC.getValue())
		.setParameter("unit",	editDoseC.getUnit())
		.setParameter("app",	editDoseC.getApp())
		.setParameter("pro",	editDoseC.getPro())
		.getResultList();
		log.debug(dL);
		return dL;
	}
	
	
	protected void addAtt(Tree t, String tabName, Tree parentT, Tree docT) {
		t.setTabName(tabName);
		t.setId(nextDbid());
		t.setSort(Calendar.getInstance().getTimeInMillis());
		t.setDocT(docT);
		t.setParentT(parentT);
	}
	@Autowired @Qualifier("nodeCreator")		TaskNodeCreator	nodeCreator;
	public Tree addNode2edit(DrugMtl docMtl, Tree parentT, String tabName) {
		Tree newT = nodeCreator.setTreeManager(docMtl).setParentT(parentT).setTagName(tabName).addChild();
		log.debug(newT);
		docMtl.setIdt(newT.getId());
		docMtl.setEditNodeT();
		return newT;
	}
	/**
	 * BUG 134 UserCareSession::setId2UserName misplaced
	 * this is a workaround so delete the below lines if the above BUG is fixed
	 * 
	 */
	
	@Autowired @Qualifier("noticeCreator") DbNoticeCreator noticeCreator;

	
	public SimpleJdbcTemplate getSimpleJdbc() {
		return simpleJdbc;
	}
//	public OwsSessionContainer getUserCareSessionContainer() {
//		return userCareSessionContainer;
//	}
	
	
	private final Log log = LogFactory.getLog(getClass());
	private Folder folderF;
	private Tree folderT;
/*
	public int nextDbid() {
		int intValue = ((BigInteger) em.createNativeQuery("SELECT nextval('dbid')").getSingleResult()).intValue();
		return intValue;
	}
 * */

	@Transactional
	public void setFlushModeCommit() {em.setFlushMode(FlushModeType.COMMIT);}

	
	@Transactional(readOnly = true)
	public Tree getFolderTree()
	{
		if(folderT==null){
			folderT = readDbDoc(9930);
			log.debug("---");
			SchemaMtl schemaMtl = new SchemaMtl(folderT);
			log.debug("---");
			schemaMtl.setNode2read(0);
			log.debug("---");
//			schemaMtl.addClass(folderT,9930,em);//to slow
			log.debug("---");
			schemaMtl.setNode2read(0);
			log.debug("---");
		}
		return folderT;
	}
	
	@Transactional(readOnly = true)
	public Folder readFolderDoc() 
	{
		String sql = "SELECT t FROM Folder t WHERE t.id = t.parentF.id ";
		Folder folderDocRootO = (Folder) em.createQuery(sql).getSingleResult();
		return folderDocRootO;
	}
	@Transactional(readOnly = true)
	public Folder getRootDocFolder() 
	{
		if(folderF==null)
			folderF = (Folder) readDbDoc(9930, "SELECT t FROM Folder t WHERE t.id = :id ");
		return folderF;
	}
	@Transactional(readOnly = true)
	public Tree readDbDoc(Integer idDoc) 
	{
		Tree readDbDoc = (Tree) readDbDoc(idDoc, "SELECT t FROM Tree t WHERE t.id = :id ");
		log.debug("--------------"+readDbDoc.getTabName()+":"+readDbDoc.getId());
		if(readDbDoc!=null)
			treeDocStatistic(readDbDoc);
		return readDbDoc;
	}
	
	/**
	 * Read MeTaL document tree root object.
	 * @param idDoc
	 * @return MeTaL document tree root object.
	 */
	public Tree readDocT(Integer idDoc) 
	{
		long sT = System.currentTimeMillis();
		Tree docT = em.find(Tree.class, idDoc);
		if(docT==null){
			log.info("Document " +idDoc +" not found");
		}else{
			log.info("-------------- "+docT.getTabName()+":"+docT.getId()+"/"+(System.currentTimeMillis()-sT));
		}
		return docT;
	}
	/**
	 * Cascade read MeTaL document.
	 * @param idDoc Document id.
	 * @param sql
	 * @return
	 */
	public Object readDbDoc(Integer idDoc, String sql) 
	{
		if (idDoc!= null) 
		{
			long sT = System.currentTimeMillis();
			log.debug("-------------- "+sql+idDoc);
			Object docTree = em.createQuery(sql)
			.setParameter("id", idDoc)
			.getSingleResult();
			log.debug("-------------- "+(System.currentTimeMillis()-sT));
			return docTree;
		} else {
			return null;
		}
	}
	private void treeDocStatistic(Tree docTree) 
	{
		log.debug("-------------");
//		log.debug("-------------docTree.getDocNodes() = "+(docTree.getDocNodes().size()));
//		log.debug("-------------nodeLR(docTree,0) = "+(nodeLR(docTree,0)));
//		log.debug("-------------nodeUD(docTree,0) = "+(nodeUD(docTree,0)));
	}
	private int nodeUD(Tree parentTree,int n) 
	{//not for folder
		n+=parentTree.getChildTs().size();
		for(Tree tree:parentTree.getChildTs())
			n=nodeUD(tree, n);
		return n;
	}
	private int nodeLR(Tree parentTree,int n) 
	{//not for folder
		n++;
		for(Tree tree:parentTree.getChildTs() )
			n=nodeLR(tree, n);
		return n;
	}
	/*
	protected EntityManager em;
	public EntityManager getEntityManager() {return em;}
	@PersistenceContext
	public void setEntityManager(EntityManager em) {this.em = em;}
	protected History addHistory(Tree tree) {
	 * */
	public History addHistory(Tree tree) {
		Integer userId = owsSession.getUserId(simpleJdbc);
		return addHistory(tree, userId);
	}
	public History addHistory(Tree tree, Timestamp mdate) {
		Integer userId = owsSession.getUserId(simpleJdbc);
		return addHistory(tree, userId,mdate);
	}

	protected History addHistory(Tree tree, Integer userId) {
		Timestamp mdate = new Timestamp(System.currentTimeMillis());
		return addHistory(tree, userId, mdate);
	}

	private History addHistory(Tree tree, Integer userId, Timestamp mdate)
	{
		log.debug("");
		log.debug("---------------------------------------");
		//add by Simon Klebeck
		//if a history object exist - change its properties, else make a new history object for the tree
		History historyO = tree.getHistory();
		if(  historyO != null)
		{
			log.debug("history object is not null");
			log.debug("tree.getTabName = "+ tree.getTabName());
			log.debug(tree.getHistory());
			historyO = tree.getHistory();
			historyO.setMdate(mdate);
			historyO.setIdauthor(userId);
			tree.setSort(0-mdate.getTime());
			log.debug(tree.getHistory());
//			log.debug(historyO);
		}
		else
		{
			log.debug("history object null");
			historyO = new History();
			historyO.setMdate(mdate);
			historyO.setIdauthor(userId);
			tree.setSort(0-mdate.getTime());
			tree.setHistory(historyO);
			historyO.setTree(tree);
			log.debug(tree.getTabName());
			log.debug(historyO);
		}
		log.debug("---------------------------------------");
		log.debug("");
		return historyO;
	}
	
	public Map<String, Object> getConfirmAccountEntry(String confirmKey)
	{
		String sql ="SELECT username, confirmkey, createdon, remindpassword from confirmaccount where confirmkey=?";
		List<Map<String, Object>> rmap = simpleJdbc.queryForList(sql, new Object[]{confirmKey});
		log.debug("getConfirmAccountEntry:c"+confirmKey);
		log.debug("getConfirmAccountEntry:rmap"+rmap);
		if(!rmap.isEmpty())
			return rmap.get(0);
		else
			return null;
	}
	protected Tree addMtlNode(SchemaMtl docMtl, Tree parentT, String tagName, MObject pDoseModO) {
		Tree pDoseModT = addChild(tagName,parentT,parentT.getDocT());
		docMtl.addMObject(pDoseModO, pDoseModT);
		return pDoseModT;
	}
	protected String getPrefixc(DrugMtl docMtl) {
		String prefixc = owsSession.getRequest().getParameter("prefixc");
		if(null==prefixc||prefixc.length()==0)
			prefixc = docMtl.getPrefixc();
		return prefixc;
	}
	public Tree initInsideTask(DrugMtl schemaMtl, String task) {
		log.debug(task);
		Tree parentT = schemaMtl.getDocT();
		if(schemaMtl instanceof DrugMtl){
			parentT=schemaMtl.getBlockT();
			if(parentT==null){
				parentT=schemaMtl.getEditNodeT();
				while (!("task".equals(parentT.getTabName())&&!"task".equals(parentT.getParentT().getTabName())))
				{
					log.debug(parentT);
					parentT=parentT.getParentT();
				}
			}
		}
		schemaMtl.setBlockId(parentT.getId());
		Tree innerTask = schemaMtl.getInsideTask(parentT, task);
		if(innerTask==null){
			innerTask = nodeCreator.setTagName("task")
			.setTreeManager(schemaMtl).setParentT(parentT).addChild();
		}
		Task innerTaskC = null;
		if("support".equals(task)){
			innerTaskC = (Task)taskCreator.setTask(task).setType("").build();
		}else{
			innerTaskC = (Task)taskCreator.setTask(task).setType("inside").build();
		}
		if(innerTask!=null)
			schemaMtl.addMObject(innerTaskC, innerTask);
		log.debug(innerTask);
		return innerTask;
	}
	@Autowired @Qualifier("taskCreator")	DbTaskCreator	taskCreator;
	public void updateRef(TreeManager docMtl){
		for (Integer ref :  docMtl.getRef2idMS().keySet()) {
			Integer newRef = docMtl.getOld2newIdM().get(ref);
			log.debug("---------"+ref+">>"+newRef);
			if(newRef==null) continue;
			Tree newRefT = docMtl.getTree().get(newRef);
			em.persist(newRefT);
			for (Integer id : docMtl.getRef2idMS().get(ref)) {
				Tree newIdT = docMtl.getTree().get(docMtl.getOld2newIdM().get(id));
				newIdT.setRef(newRefT.getId());
			}
		}
	}
}

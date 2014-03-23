package com.qwit.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.security.core.context.SecurityContext;

import com.qwit.domain.Folder;
import com.qwit.domain.Institute;
import com.qwit.model.OwsSession;
import com.qwit.model.TreeManager;

public class MtlDbEntityManager {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired	protected OwsSession owsSession;
	@Autowired	protected SimpleJdbcTemplate simpleJdbc;
	@Autowired	private AnnotationSessionFactoryBean hibernateSessionFactory;
	public AnnotationSessionFactoryBean getHibernateSessionFactory() {return hibernateSessionFactory;}

	DbUpdate dbUpdate;
	public DbUpdate getDbUpdate() {return dbUpdate;}
	public void setDbUpdate(DbUpdate dbUpdate) {this.dbUpdate = dbUpdate;}

//	protected String sql;
	private String sql;
	public String getSql() {return sql;}
	public void setSql(String sql) {this.sql = sql;}

	protected EntityManager em;
	@PersistenceContext 
	public void setEntityManager(EntityManager em) {this.em = em;}
	public EntityManager getEntityManager() {return em;}

	String user1folderAdminSql		= "SELECT noderole.* FROM noderole,folder " +
	" WHERE idnode=idfolder AND username=? AND idnode=?";
//	" WHERE idnode=idfolder AND admin='1' AND username=? AND idnode=?";

	protected short getRight(Integer idNode){
		log.debug("----------------------------------------------------------------");
		log.debug(idNode);
		log.debug(owsSession.getRequest().isUserInRole("ROLE_SUPERVISOR"));
		if(owsSession.getRequest().isUserInRole("ROLE_SUPERVISOR"))
			return TreeManager.RIGHT_AdminYes;
		log.debug(owsSession.getRequest().getUserPrincipal());
		if(null==owsSession.getRequest().getUserPrincipal())
			return TreeManager.RIGHT_ReadNo;
		String userName = owsSession.getRequest().getUserPrincipal().getName();
		short userRight = getUserRight(idNode, userName);
		log.debug(userRight+" > "+TreeManager.RIGHT_ReadNo);
		if(userRight>=TreeManager.RIGHT_ReadNo)
			return userRight;
		setStationO(userName);
		if(null!=owsSession.getStationM()){
			userRight = getUserRight(idNode, "i_"+owsSession.getStationM().get("idinstitute"));
			if(userRight>=TreeManager.RIGHT_ReadNo)
				return userRight;
		}
		return TreeManager.RIGHT_ReadNo;
	}

	private short getUserRight(Integer idNode, String userName) {
		log.debug(user1folderAdminSql+" -- "+userName+" -- "+idNode);
		List<Map<String, Object>> folderRightList 
			= simpleJdbc.queryForList(user1folderAdminSql, userName,idNode);
		log.debug(folderRightList);
		if(folderRightList.size()>0){
			Map<String, Object> folderRight = folderRightList.get(0);
			if(((boolean) ((Boolean) folderRight.get("admin"))))
				return TreeManager.RIGHT_AdminYes;
			if(((boolean) ((Boolean) folderRight.get("write"))))
				return TreeManager.RIGHT_WriteYes;
			if(((boolean) ((Boolean) folderRight.get("read"))))
				return TreeManager.RIGHT_ReadYes;
			else
				return TreeManager.RIGHT_ReadNo;
		}
		return -1;
	}

	protected void setStationO(String userName) {
		log.debug(1);
		if(null==owsSession.getStationM()){
			log.debug(2);
//			List<Institute> userStationList = getUserInstituteList(userName);
			List<Map<String, Object>> userStationList = getUserStationList(userName);
			log.debug(userStationList.size());
//			if(userStationList.size()==1){ // ghensel edit los
			if(userStationList.size()>1){
				log.debug(userStationList.get(0));
				setStationM(userStationList.get(0));
			}
		}
	}
	protected void setUserStationList(String owuser) {
		log.debug(owuser);
//		List<Institute> userStationList = getUserInstituteList(owuser);
		List<Map<String, Object>> userStationList = getUserStationList(owuser);
		log.debug(userStationList);
		if(userStationList.size()==1)
			setStationM(userStationList.get(0));
		if(userStationList.size()>=1)
			owsSession.setUserStationList(userStationList);
	}
	public void setStation(Integer id) {
		Institute stationO=em.find(Institute.class, id);
		setStation(stationO);
	}
	
	private void setStationM(Map<String, Object> stationM) {
		owsSession.setStationM(stationM);
	}
	protected void setStation(Institute stationO) {
		owsSession.setStationO(stationO);
		log.debug(stationO);
		String institute = stationO.getInstitute();
//		log.debug(institute);
		String sql="SELECT  f FROM Folder f where f.folder = :folder";
//		log.debug(sql+" -- "+institute);
		List<Folder> resultList = em.createQuery(sql).setParameter("folder", institute).getResultList();
		for (Folder folderO : resultList) {
			if(isParentFolder(folderO.getParentF(),"patient"))
				owsSession.setIdStationPatient(folderO.getId());
			if(isParentFolder(folderO.getParentF(),"protocol"))
				owsSession.setIdStationProtocol(folderO.getId());
		}
//		log.debug("owsSession.getIdStationPatient() = "+owsSession.getIdStationPatient());
//		log.debug("owsSession.getIdStationProtocol() = "+owsSession.getIdStationProtocol());
	}
	private boolean  isParentFolder(Folder folderO, String folder) {
		log.debug(folderO+" -- "+folder);
		if(folderO.getFolder().equals(folder))
			return true;
		if(!folderO.getParentF().equals(folderO))
			return isParentFolder(folderO.getParentF(), folder);
		return false;
	}
	/*
	protected List<Institute> getUserInstituteList(String owuser) {
		String sql="SELECT i " +
		" FROM Owuser o,Tree t1,Contactperson c,Tree t2, Institute i,Tree t3,Tree t4" +
		" WHERE o.owuser=:owuser" +
		" AND t1.idClass=o.id" +
		" AND t2.id=t1.parentT.id AND t2.idClass=c.id" +
		" AND t3.idClass=i.id AND t3.id=t4.parentT.id AND t4.idClass=c.id";
		log.debug(sql+" -- "+owuser);
		List<Institute> resultList = em.createQuery(sql).setParameter("owuser", owuser).getResultList();
		log.debug(resultList);
		return resultList;
	}
	 * */
	protected List<Map<String, Object>> getUserStationList(String owuser) {
		String sql=" SELECT institute.idinstitute,institute.institute " +
		" ,institute.fax ,institute.phone ,bsaformula " +
		" ,patientFolderT.idclass AS idStationPatientFolder" +
		" FROM owuser,tree t1,contactperson,tree t2, institute,tree t3,tree t4 " +
		" ,tree patientT,folder patientO " +
		" ,tree patientFolderT,folder patientFolderO " +
		" WHERE owuser=? AND t1.idclass=idowuser AND t2.id=t1.did AND t2.idclass=idcontactperson AND t3.idclass=idinstitute " +
		" AND t3.id=t4.did AND t4.idclass=idcontactperson " +
		" AND t3.id=patientT.did AND patientT.idclass=patientO.idfolder AND patientO.folder='patient' " +
		" AND patientT.id=patientFolderT.did AND patientFolderT.idclass=patientFolderO.idfolder";
		log.debug(sql+" -- "+owuser);
		List<Map<String, Object>> userStationList = simpleJdbc.queryForList(sql, owuser);
		return userStationList;
	}
	protected List<Map<String, Object>> getUserStationList_old(String owuser) {
		String sql="SELECT institute.* " +
		" FROM owuser,tree t1,contactperson,tree t2, institute,tree t3,tree t4" +
		" WHERE owuser=?" +
		" AND t1.idclass=idowuser" +
		" AND t2.id=t1.did AND t2.idclass=idcontactperson" +
		" AND t3.idclass=idinstitute AND t3.id=t4.did AND t4.idclass=idcontactperson";
		log.debug(sql+" -- "+owuser);
		List<Map<String, Object>> userStationList = simpleJdbc.queryForList(sql, owuser);
		return userStationList;
	}

	public int nextDbid() {
		int intValue = ((BigInteger) em.createNativeQuery("SELECT nextval('dbid')").getSingleResult()).intValue();
		return intValue;
	}
	protected String getDirSelect() {
		String sql = " SELECT * FROM tree t, folder f " +
		" WHERE f.idfolder = t.id ORDER BY t.did, t.sort ASC ";
		SecurityContext securityContext = owsSession.getSecurityContext();
		if(null!=securityContext){
			String userName = securityContext.getAuthentication().getName();
			log.debug(" userName = "+userName);
			if(null!=userName&&!"admin".equals(userName)){
				sql=sql.replace(" folder ",getReadWriteFolder(userName));
			}
		}
		return sql;
	}
	protected String getReadWriteFolder(String userName) {
		return " (" +
		" SELECT " +
		" CASE " +
		" WHEN n1.write IS NOT NULL THEN n1.write::int=1 " +
		" WHEN n2.write IS NOT NULL THEN n2.write::int=1 " +
		" WHEN n3.write IS NOT NULL THEN n3.write::int=1" +
		" END AS wr , " +
		"* FROM folder " +
		" LEFT JOIN noderole n1 ON idfolder=n1.idnode and n1.username='" +userName +"' " +
		" LEFT JOIN ( " +
		" SELECT idnode, max(read::int)::bit AS read , max(write::int)::bit AS write FROM noderole, " +
		"( " +
		" SELECT o1.owuser FROM institute,tree t1,owuser o1,tree t2, " +
		"( SELECT * FROM contactperson, tree t1, owuser o1 " +
		" WHERE t1.did=idcontactperson AND t1.id=o1.idowuser AND  o1.owuser='" +userName +"' " +
		") t3" +
		" WHERE t1.did=idinstitute AND t1.id=o1.idowuser AND t2.did=t1.did AND t2.idclass=t3.did " +
		") o1 " +
		" WHERE username=o1.owuser " +
		" GROUP BY idnode " +
		" ) n2 ON idfolder=n2.idnode " +
		" LEFT JOIN noderole n3 ON idfolder=n3.idnode AND n3.username='all' " +
		" WHERE NOT( n1.idnode IS NULL AND n2.idnode IS NULL AND n3.idnode IS NULL ) AND " +
		" CASE " +
		" WHEN n1.read IS NOT NULL THEN n1.read::int=1" +
		" WHEN n2.read IS NOT NULL THEN n2.read::int=1" +
		" WHEN n3.read IS NOT NULL THEN n3.read::int=1" +
		" END " +
		") ";
	}
}

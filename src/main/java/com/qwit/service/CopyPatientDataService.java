package com.qwit.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Diagnose;
import com.qwit.domain.Patient;
import com.qwit.domain.Tree;
import com.qwit.model.PatientMtl;
import com.qwit.model.UserMtl;

@Service("copyPatientDataService")
@Repository
public class CopyPatientDataService extends MtlDbService {
	private final Log log = LogFactory.getLog(getClass());

	@Autowired @Qualifier("copyPasteService")	private	CopyPasteService	copyPasteService;
	@Autowired @Qualifier("simpleJdbc2")		private	SimpleJdbcTemplate	simpleJdbc2;
	@Autowired JpaImportDao owsImportDao;

	//updatePatient
//	public Map update1patient(UserMtl docMtl,Integer ids) {
	@Transactional
	public void update1patient(UserMtl docMtl,Integer ids, Map stM) {
//		public Map<String, PatientMtl> update1patient(UserMtl docMtl,Integer ids) {
		log.debug(1);
//		Map<String, Object> patientM = getPatientM(docMtl, ids);
		Map<String, Object> patientM = (Map<String, Object>) ((Map) stM.get("copyPatientenMap")).get(ids);
		log.debug(2);
		Tree folderT = em.find(Tree.class, idFolder);
		log.debug(3);
		Integer idt = (Integer) patientM.get("idt");
		log.debug("idt="+idt);
		Tree patientT = em.find(Tree.class, idt);
		log.debug(patientT);
//		Map<String, PatientMtl> stM = new HashMap<String, PatientMtl>();
//		Map stM = createPatientCopyMap();
		PatientMtl targetPatientMtl = new PatientMtl(patientT);
		stM.put("targetPatientMtl", targetPatientMtl);
		
		log.debug(targetPatientMtl.getDocT());
		PatientMtl sourcePatientMtl = owsImportDao.buildCopyPatient(ids,targetPatientMtl);
		
		updatePatient(sourcePatientMtl,targetPatientMtl);
		stM.put("sourcePatientMtl", sourcePatientMtl);
		log.debug(4);
//		return stM;
	}

	public Map createPatientCopyMap() {return new HashMap();}

	private void updatePatient(PatientMtl sourcePatientMtl, PatientMtl targetPatientMtl) {
		for (Tree phT : sourcePatientMtl.getDocT().getChildTs()) {
			if(phT.getMtlO() instanceof Diagnose){
				updatePatientHistoryT(targetPatientMtl,targetPatientMtl.getDocT(), phT);
//			}else if(phT.getMtlO() instanceof Finding){
//				updatePatientHistoryT(targetPatientMtl,targetPatientMtl.getDocT(), phT);
			}
		}
	}

	private void updatePatientHistoryT(PatientMtl targetPatientMtl, Tree parentT, Tree node2copyT) {
		Tree pasteT = copyPasteService.addPaste1T(node2copyT, parentT, targetPatientMtl);
		if(null!=node2copyT.getHistory())
			addHistory(pasteT,node2copyT.getHistory().getMdate());
		log.debug(pasteT);
		if(node2copyT.hasChild())
			for(Tree childT:node2copyT.getChildTs())
				updatePatientHistoryT(targetPatientMtl, pasteT, childT);
	}
	//END: updatePatient

//	public void copy1patient(UserMtl docMtl,Integer idt) {
	@Transactional
	public void copy1patient(UserMtl docMtl,Integer idt, Map stM) {
//		Map<String, Object> patientM = getPatientM(docMtl, idt);
		Map<String, Object> patientM = (Map<String, Object>) ((Map) stM.get("copyPatientenMap")).get(idt);
		
		Tree folderT = em.find(Tree.class, idFolder);
		log.debug(folderT);

		Patient newPatientO = new Patient();

		String patient = (String) patientM.get("patient");
		String forename = (String) patientM.get("forename");
		String sex = (String) patientM.get("sex");
		Timestamp birthdate = (Timestamp) patientM.get("birthdate");

		newPatientO.setPatient(patient);
		newPatientO.setForename(forename);
		newPatientO.setSex(sex);
		newPatientO.setBirthdate(birthdate);
		log.debug(newPatientO);

		saveNewPatient(folderT, newPatientO);
	}

//	private Map<String, Object> getPatientM(UserMtl docMtl, Integer ids) {
//		log.debug(ids);
//		Map<String, Object> patientM = docMtl.getCopyPatientenMap().get(ids);
//		log.debug(patientM);
//		return patientM;
//	}
	

	int idFolder=1048962;
//	public void copyPatienten(UserMtl docMtl) {
	public void copyPatienten(Map stM) {
		copyPatienten2tmp(stM);
		copyPatientenHistory2tmp(stM);
	}

	private void copyPatientenHistory2tmp(Map stM) {
		simpleJdbc.update("DELETE FROM tmp.phtree");
		String sourcePatientenHistory=
" SELECT " +
" patientHistoryT.id ,patientHistoryT.tab_name ,patientHistoryT.did ,patientHistoryT.idclass" +
" ,patientHistoryT.sort " +
" FROM patient,tree patientHistoryT" +
" WHERE idpatient=patientHistoryT.did" +
" ORDER BY did,sort"+
"";
		log.debug(sourcePatientenHistory);
		List<Map<String, Object>> copyPatientenHistoryList = simpleJdbc2.queryForList(sourcePatientenHistory);
		for (Map<String, Object> map : copyPatientenHistoryList)
			simpleJdbc.update("INSERT INTO tmp.phtree (id, tab_name, did, idclass,sort)" +
			" VALUES (?,?,?,?,?) ",map.values().toArray());
		String copiedPatientHistory="select s.id,t.id,s.did,t.did idpatient FROM" +
" (SELECT * FROM tmp.patient,tmp.phtree WHERE idpatient=did) s" +
" , ( SELECT patient p, forename f,birthdate b,sex s,sort,id,did FROM patient,tree WHERE idpatient=did) t" +
" WHERE " +
" s.patient=t.p " +
" AND s.forename=t.f " +
" AND s.sex=t.s " +
" AND s.sort=t.sort " +
//" AND CAST(s.birthdate AS date)=CAST(t.b AS date)" +
"";
		Map<Integer,List<Map<String, Object>>> copiedPatientHistoryMapList= new HashMap<Integer, List<Map<String,Object>>>();
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(copiedPatientHistory);
		for (Map<String, Object> map : queryForList) {
			Integer idpatient=(Integer) map.get("idpatient");
			if(!copiedPatientHistoryMapList.containsKey(idpatient))
				copiedPatientHistoryMapList.put(idpatient, new ArrayList<Map<String,Object>>());
			List<Map<String, Object>> list = copiedPatientHistoryMapList.get(idpatient);
			list.add(map);
		}
		stM.put("copiedPatientHistoryMapList", copiedPatientHistoryMapList);
		
		String cntSourcePatientHistory="SELECT did, count(did) cnt FROM tmp.phtree GROUP BY did";
		log.debug(cntSourcePatientHistory);
		Map<Integer,Long> cntSourcePatientHistoryMap=new HashMap<Integer, Long>();
		List<Map<String, Object>> queryForList2 = simpleJdbc.queryForList(cntSourcePatientHistory);
		for (Map<String, Object> map : queryForList2) {
			Integer idpatient =(Integer) map.get("did");
			Long cnt =(Long) map.get("cnt");
			cntSourcePatientHistoryMap.put(idpatient, cnt);
		}
		stM.put("cntSourcePatientHistoryMap", cntSourcePatientHistoryMap);
	}

	private void copyPatienten2tmp(Map stM) {
		String sourcePatienten="SELECT patient, forename, sex, birthdate, idpatient, patientT.did AS idfolder " +
		" FROM patient,tree patientT " +
		" WHERE idpatient=patientT.id " +
		" AND patientT.did="+idFolder +
		"";
		log.debug(sourcePatienten);
		List<Map<String, Object>> copyPatientenList = simpleJdbc2.queryForList(sourcePatienten);
		log.debug("delete BEGIN");
		simpleJdbc.update("DELETE FROM tmp.patient");
		log.debug("copy BEGIN");
		for (Map<String, Object> map : copyPatientenList)
			simpleJdbc.update("INSERT INTO tmp.patient (patient, forename, sex, birthdate,idpatient,idfolder)" +
			" VALUES (?,?,?,?,?,?) ",map.values().toArray());
		
		log.debug("END");
		String targetPatienten=" SELECT " +
" sp.patient, sp.forename, sp.sex, sp.birthdate, sp.idpatient AS ids, tp.idpatient AS idt " +
" FROM tmp.patient sp LEFT JOIN patient tp " +
" ON sp.patient=tp.patient AND sp.forename=tp.forename AND sp.sex=tp.sex " +
" AND CAST(sp.birthdate AS date)=CAST(tp.birthdate AS date) " +
"";
		log.debug(targetPatienten);
		setCopyPatientenList(simpleJdbc.queryForList(targetPatienten),stM);
		log.debug("END");
	}
	public void setCopyPatientenList(List<Map<String, Object>> stPatientenList, Map stM) {
		stM.put("stPatientenList", stPatientenList);
		Map<Integer, Map<String, Object>> copyPatientenMap = new HashMap<Integer, Map<String,Object>>();
		Map<Integer,Integer> t2s= new HashMap<Integer, Integer>();
		int cntCopiedPatient=0, cntNotCopiedPatient=0;
		for (Map<String, Object> map : stPatientenList) {
			Integer ids =(Integer) map.get("ids");
			Integer idt =(Integer) map.get("idt");
			if(null==idt){
				cntNotCopiedPatient++;
			}else{
				cntCopiedPatient++;
				t2s.put(idt, ids);
			}
			copyPatientenMap.put(ids, map);
		}
		stM.put("t2s", t2s);
		stM.put("copyPatientenMap", copyPatientenMap);
		stM.put("cntCopiedPatient", cntCopiedPatient);
		stM.put("cntNotCopiedPatient", cntNotCopiedPatient);
	}
}

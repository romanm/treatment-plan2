package com.qwit.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Notice;
import com.qwit.domain.Patient;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;
import com.qwit.model.ConceptMtl;
import com.qwit.model.DrugMtl;
import com.qwit.model.PatientMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;

@Service("schemaService")
@Repository
public class SchemaService extends MtlDbService {
	private final Log log = LogFactory.getLog(getClass());
	
	@Autowired @Qualifier("taskCreator")		DbTaskCreator	taskCreator;
	@Autowired @Qualifier("taskDrugCreator")	TaskNodeCreator	taskDrugCreator;
	
	public void addSupportDrug(SchemaMtl docMtl){
		if(TreeManager.RIGHT_WriteYes>getRight(docMtl.getFolderT().getId()))
			return;
		docMtl.setBlockSupportDrug(taskDrugCreator, taskCreator, nodeCreator);
	}
	public void addTaskDrug(DrugMtl docMtl) {
		log.debug("-----------"+docMtl);
		if(TreeManager.RIGHT_WriteYes>getRight(docMtl.getFolderT().getId()))
			return;
		Tree parentT = docMtl.getBlockT();
		String idtStr = owsSession.getRequest().getParameter("idt");
		log.debug("-----------"+docMtl.getDocT());
		int idtParent = Integer.parseInt(idtStr);
		log.debug("-----------"+idtParent);
//		docMtl.setParentT(idtParent);
		log.debug("-----------");
		Integer idt = taskDrugCreator.setTreeManager(docMtl).setParentT(parentT).addChild().getId();
		log.debug("-----------");
		docMtl.setIdt(idt);
		log.debug("-----------");
	}
	public void addTaskDrug(SchemaMtl docMtl) {
		log.debug("-----------"+docMtl);
		if(TreeManager.RIGHT_WriteYes>getRight(docMtl.getFolderT().getId()))
			return;
		String idtStr = owsSession.getRequest().getParameter("idt");
		log.debug("-----------"+docMtl.getDocT());
		int idtParent = Integer.parseInt(idtStr);
		log.debug("-----------"+idtParent);
//		docMtl.setParentT(idtParent);
		log.debug("-----------");
		Integer idt = taskDrugCreator.setTreeManager(docMtl).addChild().getId();
		log.debug("-----------");
		docMtl.setIdt(idt);
		log.debug("-----------");
	}
	
	public void newOnDemandDrug(SchemaMtl docMtl){
		Tree newSupportDrug = newSupportDrug(docMtl);
		for(Tree t:newSupportDrug.getChildTs())
			if("day".equals(t.getTabName()))
			{
				Notice notice1O=(Notice) noticeCreator.setMtlO(new Notice("","rqm")).build();
				Tree pDoseModT = addMtlNode(docMtl, t, "notice", notice1O);
			}
	}
	private Tree newSupportDrug(SchemaMtl docMtl) {
		log.debug("------------");
		Tree blockSupportT = //getTaskSupportT();
			docMtl.getInsideTask(docMtl.getBlockT(), "support");
		if(blockSupportT==null){
			Tree blockT = docMtl.getBlockT();
			log.debug(blockT);
			Task taskC = (Task)taskCreator.setTask("support").setType("").setTaskvar("").build();
			docMtl.getClassM().put(taskC.getId(), taskC);
			log.debug(taskC);
			blockSupportT = nodeCreator.setTagName("task").setTreeManager(docMtl).setParentT(blockT).addChild();
			blockSupportT.setIdClass(taskC.getId());
			blockSupportT.setMtlO(taskC);
			log.debug(blockSupportT);
		}
		Tree newSupportDrug = taskDrugCreator.setTreeManager(docMtl).setParentT(blockSupportT).addChild();
		log.debug(newSupportDrug);
//		getDdrug(blockSupportT).add(newSupportDrug);
		docMtl.setIdt(newSupportDrug.getId());
		log.debug("------------");
		return newSupportDrug;
	}
	
	/*
	private void addAtt(Tree t, String tabName, Tree parentT, Tree docT) {
		t.setTabName(tabName);
		t.setId(nextDbid());
		t.setSort(Calendar.getInstance().getTimeInMillis());
		t.setDocT(docT);
		t.setParentT(parentT);
	}
	 * */
	
	/*
	private void saveOfDate(TreeManager schemaMtl, Tree patientT, Tree newDocT) {
		log.debug(newDocT);
		Pvariable ofDateFixedC = getPvariable("ofDate","fixed","");
		schemaMtl.setOfDateFixedC(ofDateFixedC);
		Integer idOwuser = owsSession.getUserId(simpleJdbc);
		log.debug("idOwuser="+idOwuser);
		Tree dd = schemaMtl.addOfDate2(newDocT, patientT, nextDbid());
		log.debug(dd);
	}
	 */
	private ConceptMtl makeConceptMtl(Tree conceptT) {
		ConceptMtl conceptMtl = new ConceptMtl(conceptT);
		conceptMtl.addClass(conceptT,conceptT.getId(), em);
		return conceptMtl;
	}
	private void setProtocolFolder(TreeManager schemaMtl, Tree protocolT) {
		schemaMtl.addClassM(protocolT, em);
		Tree folderT = protocolT.getParentT();
		if(folderT.getMtlO() instanceof Patient){
			folderT = folderT.getParentT();
		}
		schemaMtl.addClassM(folderT, em);
	}
	private void makePatientMtl(TreeManager docMtl, Tree patientT) {
		PatientMtl patientMtl = new PatientMtl(patientT);
		docMtl.setPatientMtl(patientMtl);
		patientMtl.addClass(patientT,patientT.getId(),em);
		patientMtl.calcBsa();
	}
	@Transactional(readOnly = true)
	public ConceptMtl makeConceptMtl(Integer idDoc)
	{
		long sT = System.currentTimeMillis();
		Tree conceptT = readDocT(idDoc);
		ConceptMtl docMtl = makeConceptMtl(conceptT);
		Tree patientT = docMtl.getDocT().getDocT();
		setProtocolFolder(docMtl, docMtl.getDocT());
		notFlowNotVariant(docMtl);
		if(patientT!=null&&patientT.getTabName().equals("patient"))
			makePatientMtl(docMtl, patientT);
		log.debug("-TimeMillis-------------"+docMtl.getDocT().getTabName()
			+ ":"+docMtl.getClassM().size()
			+"/" +docMtl.getTree().size() 
			+"/" +docMtl.getMsCnt() 
			+ "/"+(System.currentTimeMillis()-sT));
		return docMtl;
	}
	public void notFlowNotVariant(TreeManager docMtl) {
		Integer id = docMtl.getDocT().getId();
		if(docMtl instanceof SchemaMtl){
			id = docMtl.getDocT().getDocT().getId();
		}
		String sql = 
			"SELECT t1.id FROM (" +
			" SELECT * FROM tree t1,task WHERE t1.iddoc=? AND t1.id=idtask) t1" +
			" LEFT JOIN tree t2 ON t2.iddoc=t1.iddoc AND t2.id!=t2.idclass AND t2.idclass=t1.id" +
			" WHERE t2 IS NULL";
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, id);
		if(queryForList.size()>0){
			Set<Integer> s = new HashSet();
			for(Map map:queryForList)
				s.add((Integer) map.get("id"));
			docMtl.setNotFlowNotVariant(s);
		}
	}
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Pvariable getPvariable(String pvariable, String pvalue, String unit) {
		log.debug("--");
		String sql = "SELECT d FROM Pvariable d WHERE d.pvariable= :pvariable AND d.pvalue=:pvalue AND d.unit=:unit ";
		Pvariable ofDateC=null;
		log.debug(pvariable+" "+pvalue+" "+unit);
		log.debug(sql);
		List<Pvariable> dL = em.createQuery(sql)
		.setParameter("pvariable",	pvariable)
		.setParameter("pvalue",	pvalue)
		.setParameter("unit",	unit)
		.getResultList();
		if(dL.size()>0)
		{
			ofDateC=dL.get(0);
		}
		return ofDateC;
	}
	private Tree correcturUseVarianteSchema2(SchemaMtl schemaMtl, Tree docProtocolT) {
		Task taskO = (Task)schemaMtl.getDocT().getMtlO();
		JXPathContext protocolContext = JXPathContext.newContext(docProtocolT);
		protocolContext.getVariables().declareVariable("idClass", taskO.getId());
//		Iterator iteratePointers = protocolContext.iteratePointers("childTs[mtlO/id=$idClass]");
		Iterator iteratePointers = protocolContext.iteratePointers("childTs");
		log.debug(iteratePointers);
		while (iteratePointers.hasNext()) {
			Pointer object = (Pointer) iteratePointers.next();
			log.debug(object);
			log.debug(object.getValue());
		}
		return null;
	}
	
	private Tree correcturUseVarianteSchema_dep(SchemaMtl schemaMtl, Tree docProtocolT) {
		Tree firstSchemaUseT=null;
		log.debug(" schemaMtl.getDocT().getId()="+schemaMtl.getDocT().getId());
		Task taskO = (Task)schemaMtl.getDocT().getMtlO();
		Tree definitionT=null;
		Set<Tree> set = schemaMtl.getIdClass2TreeMS().get(taskO.getId());
		log.debug(set);
		for (Tree tree : set) {
			log.debug(tree);
			if("definition".equals(tree.getParentT().getTabName()))
				definitionT=tree.getParentT();
			else if(firstSchemaUseT==null)
				return tree;
		}
		log.debug(definitionT);
		log.debug(taskO);
		if(taskO.getCyclename()!=null){
			ConceptMtl conceptMtl = makeConceptMtl(docProtocolT);
			for (Tree tree : definitionT.getChildTs()) {
				Task task2O=(Task)tree.getMtlO();
				if(task2O==null)
					return null;
				if(taskO.getCyclename().equals(task2O.getCyclename())){
					log.debug(task2O);
					set = conceptMtl.getIdClass2TreeMS().get(task2O.getId());
					log.debug(set);
					for (Tree tree2 : set) {
						if("definition".equals(tree2.getParentT().getTabName()))
							continue;
						if(firstSchemaUseT==null)
							firstSchemaUseT=tree2;
						tree2.setIdClass(taskO.getId());
					}
					if(firstSchemaUseT!=null)
						return firstSchemaUseT;
				}
			}
		}
		return null;
	}
	
	@Autowired @Qualifier("pvCreator")		DbPvariableCreator	pvCreator;
	private Pvariable	procent100, ofDateFixed=null;
	public Pvariable getProcent100() {
		if(null==procent100)
		{
			procent100 = (Pvariable)pvCreator.setPvariable("percent").setPvalue("100").setUnit("%").build();
		}
		return procent100;
	}
	public Pvariable getOfDateFixed() {
		if(null==ofDateFixed){
			ofDateFixed = (Pvariable)pvCreator.setPvariable("ofDate").setPvalue("fixed").setUnit("").build();
		}
		return ofDateFixed;
	}
	
	
	
}

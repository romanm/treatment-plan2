package com.qwit.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.amqp.ProtocolRequest;
import com.qwit.config.client.AmqpConfigClient;
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
import com.qwit.domain.Institute;
import com.qwit.domain.Ivariable;
import com.qwit.domain.Labor;
import com.qwit.domain.MObject;
import com.qwit.domain.Notice;
import com.qwit.domain.Personrole;
import com.qwit.domain.Protocol;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Tablet;
import com.qwit.domain.Task;
import com.qwit.domain.Times;
import com.qwit.domain.Tree;
import com.qwit.model.ConceptMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.util.FlowObjCreator;

@Controller
public class ImportService extends MtlDbService{
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired @Qualifier("simpleJdbc2")			SimpleJdbcTemplate		simpleJdbc2;
	@Autowired @Qualifier("treeCreator")			DbNodeCreator			treeCreator;
	@Autowired @Qualifier("checkitemCreator")		DbCheckitemCreator		checkitemCreator;
	@Autowired @Qualifier("checklistCreator")		DbChecklistCreator		checklistCreator;
	@Autowired @Qualifier("instituteCreator")		DbInstituteCreator		instituteCreator;
	@Autowired @Qualifier("contactpersonCreator")	DbContactpersonCreator	contactpersonCreator;
	@Autowired @Qualifier("diagnoseCreator")		DbDiagnoseCreator		diagnoseCreator;
	@Autowired @Qualifier("documentCreator")		DbDocumentCreator		documentCreator;
	@Autowired @Qualifier("protocolCreator")		DbProtocolCreator		protocolCreator;
	@Autowired @Qualifier("taskCreator")			DbTaskCreator			taskCreator;
	@Autowired @Qualifier("armCreator")				DbArmCreator			armCreator;
	@Autowired @Qualifier("personroleCreator")		DbPersonroleCreator		personroleCreator;
	@Autowired @Qualifier("drugCreator")			DbDrugCreator			drugCreator;
	@Autowired @Qualifier("doseCreator")			DbDoseCreator			doseCreator;
	@Autowired @Qualifier("tabletCreator")			DbTabletCreator			tabletCreator;
	@Autowired @Qualifier("appCreator")				DbAppCreator			appCreator;
	@Autowired @Qualifier("dayCreator")				DbDayCreator			dayCreator;
	@Autowired @Qualifier("timesCreator")			DbTimesCreator			timesCreator;
	@Autowired @Qualifier("noticeCreator")			DbNoticeCreator			noticeCreator;
	@Autowired @Qualifier("exprCreator")			DbExprCreator			exprCreator;
	@Autowired @Qualifier("laborCreator")			DbLaborCreator			laborCreator;
	@Autowired @Qualifier("findingCreator")			DbFindingCreator		findingCreator;
	@Autowired @Qualifier("pvCreator")				DbPvariableCreator		pvCreator;
	@Autowired @Qualifier("ivCreator")				DbIvariableCreator		ivCreator;
	@Autowired @Qualifier("documentService")		DocumentService			docService;

//	@Autowired @Qualifier("mtlDServiceGateway") RabbitGatewaySupport gateway;
//	public ConceptMtl schema2import(SchemaMtl docMtl){
	public ConceptMtl schema2import(TreeManager docMtl){
		log.debug("---------------------------"+docMtl);
		Integer idImport = docMtl.getDocT().getId();
		log.debug(idImport);
		RabbitGatewaySupport gateway = getGatewey();
		log.debug(1);
		if(null==gateway)
			return null;
		log.debug(2);
		ConceptMtl importDocMtl =(ConceptMtl) gateway.getRabbitTemplate()
				.convertSendAndReceive(new ProtocolRequest(idImport));
		return importDocMtl;
	}

//	public void importSchemaViaNetwork(ConceptMtl importDocMtl, SchemaMtl docMtl){
	public void importSchemaViaNetwork(ConceptMtl importDocMtl, TreeManager docMtl){
		log.debug("---------------------------"+docMtl);
		log.debug("---------------------------"+importDocMtl);
		if(null==importDocMtl)
			return;
		Tree thisDbSchema = docMtl.getDocT();
		if(docMtl instanceof SchemaMtl)
		{
			SchemaMtl schemaMtl = (SchemaMtl)docMtl;
			Task thisDbTask=schemaMtl.getTaskO(docMtl.getDocT());
			log.debug(thisDbTask);
			log.debug(importDocMtl.getDocT());
			log.debug(importDocMtl.getClassM());
			Task importTask = (Task) importDocMtl.getClassM().get(importDocMtl.getDocT().getId());
			log.debug(importDocMtl);
			thisDbTask.setTask(importTask.getTask());
			thisDbTask.setTaskvar(importTask.getTaskvar());
			thisDbTask.setDuration(importTask.getDuration());
		}

		simpleJdbc.update("UPDATE tree SET ref=NULL WHERE iddoc=?", thisDbSchema.getId());
		simpleJdbc.update("DELETE FROM tree WHERE did=?", thisDbSchema.getId());

		importSchema(importDocMtl,thisDbSchema);
	}

	private RabbitGatewaySupport getGatewey() {
		
		log.debug("a");
		ApplicationContext contextClient = null;
		try{
			contextClient = new AnnotationConfigApplicationContext(AmqpConfigClient.class);
//			contextClient = new AnnotationConfigApplicationContext("com.qwit.config.client");
		}catch (Exception e) {
			log.debug(e.getMessage());
		}
		log.debug("b");
		if(null==contextClient)
			return null;
		RabbitGatewaySupport gateway = contextClient.getBean(RabbitGatewaySupport.class);
		log.debug("b "+gateway);
		return gateway;
	}

	
	
	@Transactional
	public void importProtocolViaNetwork(FlowObjCreator f){
		RabbitGatewaySupport gateway = getGatewey();
		if(null==gateway)
			return;
		//read concept
		Integer idImport = f.getIdimport();
		log.debug(idImport);
		ConceptMtl conceptMtl2Import =(ConceptMtl) gateway.getRabbitTemplate()
				.convertSendAndReceive(new ProtocolRequest(idImport));
		Protocol conceptO2import = (Protocol) conceptMtl2Import.getDocT().getMtlO();
		Tree conceptT2Import = conceptMtl2Import.getDocT();
		
		int nextDbid = nextDbid()+1;
		String importVariant = "_Import_"+nextDbid;

		//write concept root
		conceptO2import.setProtocolvar(importVariant);
		Protocol conceptO = (Protocol) protocolCreator.setFoc(f)
		.setProtocol(conceptO2import).build();
		Tree conceptT=em.find(Tree.class, conceptO.getId());
		conceptT.setDocT(conceptT);
		log.debug(conceptO);
		log.debug(conceptT);
		addHistory(conceptT);
		f.getImportIdToSynchron().put(conceptT2Import, conceptT);
		owsSession.getImportIdToSynchron().put(conceptT2Import.getId(), conceptT.getId());
		
		for (MObject mObject : conceptMtl2Import.getClassM().values()) {
			MObject buildMtlO = null;
			if(mObject instanceof Arm){
				buildMtlO = armCreator.setMtlO((Arm) mObject).build();
			}else if(mObject instanceof Institute){
				buildMtlO = instituteCreator.setMtlO((Institute) mObject).build();
			}else if(mObject instanceof Checklist){
				buildMtlO = checklistCreator.setMtlO((Checklist) mObject).build();
			}else if(mObject instanceof Checkitem){
				buildMtlO = checkitemCreator.setMtlO((Checkitem) mObject).build();
			}else if(mObject instanceof Contactperson){
				buildMtlO = contactpersonCreator.setMtlO((Contactperson) mObject).build();
			}else if(mObject instanceof Document){
				buildMtlO = documentCreator.setMtlO((Document) mObject).build();
			}else if(mObject instanceof Diagnose){
				buildMtlO = diagnoseCreator.setMtlO((Diagnose) mObject).build();
			}else if(mObject instanceof Personrole){
				buildMtlO = personroleCreator.setMtlO((Personrole) mObject).build();
			}else if(mObject instanceof Task){
				Task taskO = (Task) mObject;
				taskO.setTaskvar(importVariant);
				Tree schemaT = taskCreator.setMtlO(taskO).createReadTree();
				log.debug(schemaT);
				buildMtlO=schemaT.getMtlO();
				conceptMtl2Import.getTree().put(schemaT.getId(), schemaT);
				conceptMtl2Import.getOld2newIdM().put(taskO.getId(), schemaT.getId());
				Tree schemaT2Import = conceptMtl2Import.getTree().get(taskO.getId());
				log.debug(1);
				addHistory(schemaT);
				log.debug(2);
				f.getImportIdToSynchron().put(schemaT2Import, schemaT);
				owsSession.getImportIdToSynchron().put(schemaT2Import.getId(), schemaT.getId());
				log.debug(3);
			}else continue;
			log.debug(4);
			conceptMtl2Import.getIdClassMap().put(mObject.getId(), buildMtlO.getId());
			log.debug(5);
		}
		log.debug(6);
		conceptMtl2Import.setDocT2new(conceptT);
		Tree concept2importT = conceptMtl2Import.getDocT();
		log.debug(concept2importT);
		copyTree2(conceptT, concept2importT,conceptMtl2Import);
		log.debug("END concept import ");
		for (SchemaMtl schemaMtl : conceptMtl2Import.getDefSchemaMtlMap().values()) {
			Integer task2importId= schemaMtl.getDocT().getId();
			log.debug("task2importId = "+task2importId);
			Tree newTaskT = conceptMtl2Import.getTree().get(conceptMtl2Import.getOld2newIdM().get(task2importId));
			log.debug(newTaskT);
			importSchema(schemaMtl,newTaskT);
			log.debug("END task2importId = "+task2importId);
		}
		log.debug("Hello World!");
	}

	public void importProtocolSynchron(){
		for (Integer tree2import : owsSession.getImportIdToSynchron().keySet()) {
			boolean idInUse = isIdInUse(tree2import);
			if(idInUse)
				changeNotIdClassId(tree2import,nextDbid());
			changeIdClassId(tree2import, owsSession.getImportIdToSynchron().get(tree2import));
		}
	}
//	@Transactional
	public void importProtocolSynchron(FlowObjCreator f){
		log.debug(6);
		for (Tree tree2import : f.getImportIdToSynchron().keySet()) {
			log.debug(tree2import);
			importId(tree2import,f.getImportIdToSynchron().get(tree2import));
		}
		log.debug(8);
	}
	
	public boolean isIdInUse(Integer id2import) {
		String qlString = "SELECT * FROM Tree  WHERE (idClass is null or  id!=idClass) AND id="+id2import;
		log.debug(qlString);
//		String qlString = "SELECT t FROM Tree t WHERE t.id!=t.idClass AND t.id=:id";
//		Query createQuery = em.createQuery(qlString);
//		List<Tree> resultList = createQuery.setParameter("id", id2import).getResultList();
		List<Map<String, Object>> resultList = simpleJdbc.queryForList(qlString);
		log.debug(resultList);
		return resultList.size()>0;
	}
	private void changeIdClassId(Integer id2import, Integer id2change) {
		String qlString = "UPDATE Tree  SET id="+id2import+" WHERE id=idClass AND id="+id2change;
		log.debug(qlString);
		int executeUpdate = simpleJdbc.update(qlString);
//		String qlString = "UPDATE Tree t SET t.id="+id2import+" WHERE t.id=t.idClass AND t.id="+id2change;
//		Query createQuery = em.createQuery(qlString);
//		log.debug(createQuery);
//		int executeUpdate = createQuery.executeUpdate();
		log.debug(executeUpdate);
	}

	public Tree importId( Tree tree2import, Tree importedTree){
		log.debug(1);
		log.debug(importedTree);
		log.debug(3);
		Integer id2import=tree2import.getId();
		log.debug(id2import);
		Tree treeById = checkId(id2import);
		log.debug(treeById);
		if(null!=treeById) {
			if(id2import!=treeById.getIdClass()) {
				log.debug(treeById);
				int updateNr = changeIdnotTransaction(id2import);
				log.debug(updateNr);
				importedTree=changeId(id2import, importedTree);
				log.debug(treeById);
			}else if("patient".equals(treeById.getTabName())){
				//new id?
			}
		}else{
			importedTree=changeId(id2import, importedTree);
			log.debug(4);
		}
		return importedTree;
	}
	private Tree changeId(Integer id2import, Tree importedTree) {
		Integer id2change = importedTree.getId();
		String qlString = "UPDATE Tree t SET t.id="+id2import+" WHERE t.id="+id2change;
		log.debug(qlString);
		Query createQuery = em.createQuery(qlString);
//		Tree dd=em.find(Tree.class, importedTree.getId());
//		log.debug(dd);
//		String qlString2 = "UPDATE Tree t SET id="+id2import+" WHERE id="+importedTree.getId();
//		log.debug(qlString2);
//		Query createQuery = em.createNativeQuery(qlString2);
		log.debug(createQuery);
		int executeUpdate = createQuery.executeUpdate();
		log.debug(executeUpdate);
		return null;
////		importedTree.setId(id2import);
//		String sql = "UPDATE Tree SET id=? WHERE id=? ";
//		log.debug(sql.replaceFirst("\\?", ""+id2import).replaceFirst("\\?", ""+importedTree.getId()));
//		simpleJdbc.update(sql, id2import,importedTree.getId());
//		importedTree=em.find(Tree.class, id2import);
//		log.debug(importedTree);//This row not to delete. It's work only with this row.
//		return importedTree;
	}
	public void importProtocolViaNetwork_DEPR(FlowObjCreator f){
		em.setFlushMode(FlushModeType.AUTO);
		RabbitGatewaySupport gateway = getGatewey();
		if(null==gateway)
			return;
		Integer idImport = f.getIdimport();
		log.debug(idImport);

		//funktioniert
		ConceptMtl conceptMtl2Import =(ConceptMtl) gateway.getRabbitTemplate()
			.convertSendAndReceive(new ProtocolRequest(idImport));
		log.debug("--------------"+conceptMtl2Import);
		log.debug("--------------"+conceptMtl2Import.getDocT());
		log.debug("--------------"+conceptMtl2Import.getTree().size());
		log.debug("--------------"+conceptMtl2Import.getDocT().getChildTs());

		//import vars
		Protocol conceptO2import = (Protocol) conceptMtl2Import.getDocT().getMtlO();
		Tree conceptT2Import = conceptMtl2Import.getDocT();

		int nextDbid = nextDbid()+1;
		String importVariant = "_Import_"+nextDbid;

//		conceptO2import.setProtocol(conceptO2import.getProtocol() + importSuffix);
		conceptO2import.setProtocolvar(importVariant);
		Protocol conceptO = (Protocol) protocolCreator.setFoc(f)
		.setProtocol(conceptO2import).build();
		Tree conceptT=em.find(Tree.class, conceptO.getId());
		conceptT.setDocT(conceptT);
		addHistory(conceptT);
		log.debug(conceptO);
		log.debug(conceptT);
		Map<Tree, Tree> docRestoreIdMap = new HashMap<Tree, Tree>();
		docRestoreIdMap.put(conceptT2Import, conceptT);
		// gehe durch die Objecte-Map von concept2Import, schau für jedes Objekt nach ob es schon existiert 
		//in unserer DB (wenn nicht wird es durch den jeweiligen Creator erstellt)
		//und mache eine IdClaaMap um Tree von Concept2Import ins comceptMtl zu kopieren
		//persist(conceptMtl)
		for (MObject mObject : conceptMtl2Import.getClassM().values()) {
			log.debug(mObject);
			MObject buildMtlO = null;
			if(mObject instanceof Arm){
				buildMtlO = armCreator.setMtlO((Arm) mObject).build();
			}else if(mObject instanceof Institute){
				buildMtlO = instituteCreator.setMtlO((Institute) mObject).build();
			}else if(mObject instanceof Checklist){
				buildMtlO = checklistCreator.setMtlO((Checklist) mObject).build();
			}else if(mObject instanceof Checkitem){
				buildMtlO = checkitemCreator.setMtlO((Checkitem) mObject).build();
			}else if(mObject instanceof Contactperson){
				buildMtlO = contactpersonCreator.setMtlO((Contactperson) mObject).build();
			}else if(mObject instanceof Document){
				buildMtlO = documentCreator.setMtlO((Document) mObject).build();
			}else if(mObject instanceof Diagnose){
				buildMtlO = diagnoseCreator.setMtlO((Diagnose) mObject).build();
			}else if(mObject instanceof Personrole){
				buildMtlO = personroleCreator.setMtlO((Personrole) mObject).build();
			}else if(mObject instanceof Task){
				Task taskO = (Task) mObject;
				log.debug(taskO);
//				taskO.setTask(taskO.getTask() + importVariant);
				taskO.setTaskvar(importVariant);
				log.debug(taskO);
				Tree schemaT = taskCreator.setMtlO(taskO).createReadTree();
				schemaT=em.find(Tree.class, schemaT.getId());
				buildMtlO=schemaT.getMtlO();
				conceptMtl2Import.getTree().put(schemaT.getId(), schemaT);
				conceptMtl2Import.getOld2newIdM().put(taskO.getId(), schemaT.getId());
				Tree schemaT2Import = conceptMtl2Import.getTree().get(taskO.getId());
				log.debug(schemaT2Import);
				log.debug(schemaT);
				docRestoreIdMap.put(schemaT2Import, schemaT);
				log.debug(1);
				addHistory(schemaT);
				log.debug(2);
			}else continue;
			log.debug(3);
			conceptMtl2Import.getIdClassMap().put(mObject.getId(), buildMtlO.getId());
			log.debug(4);
		}
		log.debug(1);
		conceptMtl2Import.setDocT2new(conceptT);
		Tree doc2iT = conceptMtl2Import.getDocT();
		log.debug(2);
		log.debug(doc2iT);
		log.debug(2);
		copyTree2(conceptT, doc2iT,conceptMtl2Import);
		log.debug(3);
		em.merge(conceptT);
		//import concept schemata.
		Iterator iteratePointers = JXPathContext.newContext(conceptMtl2Import.getDocT())
			.iteratePointers("childTs[tabName='definition']/childTs[tabName='task']");
		while (iteratePointers.hasNext()) {
			Tree taskT2import = (Tree) ((Pointer) iteratePointers.next()).getValue();
			log.debug(taskT2import);
			Integer task2importId = taskT2import.getId();
			Integer taskIdnew = conceptMtl2Import.getOld2newIdM().get(task2importId);
			Tree taskTnew = conceptMtl2Import.getTree().get(taskIdnew);
			log.debug(taskTnew);
			SchemaMtl schemaMtl = conceptMtl2Import.getDefSchemaMtlMap().get(task2importId);
			log.debug(schemaMtl);
			importSchema(schemaMtl,taskTnew);
			log.debug(1);
			em.merge(taskTnew);
			log.debug(taskTnew.getChildTs());
			log.debug(2);
		}
		log.debug(1);
//		conceptT=em.find(Tree.class, conceptO.getId());
		log.debug("em.getFlushMode()="+em.getFlushMode());
//		em.flush();
		log.debug(docRestoreIdMap.keySet().size());
		for (Tree tree2import : docRestoreIdMap.keySet()) {
			log.debug(tree2import);
			if("protocol".equals(tree2import.getTabName()))
			{
			}
			importId(tree2import,docRestoreIdMap.get(tree2import));
		}
	}
	
//	private void importSchema(SchemaMtl schemaMtl2import, Tree taskTnew) {
	private void importSchema(TreeManager schemaMtl2import, Tree taskTnew) {
		for (MObject mObject : schemaMtl2import.getClassM().values()) {
			log.debug(mObject);
			MObject buildMtlO = null;
			if(mObject instanceof Dose){
				buildMtlO = doseCreator.setMtlO((Dose) mObject).build();
			}else if(mObject instanceof Tablet){
				buildMtlO = tabletCreator.setMtlO((Tablet) mObject).build();
			}else if(mObject instanceof App){
				buildMtlO = appCreator.setMtlO((App) mObject).build();
			}else if(mObject instanceof Task){
				buildMtlO = taskCreator.setMtlO((Task) mObject).build();
			}else if(mObject instanceof Pvariable){
				buildMtlO = pvCreator.setMtlO((Pvariable) mObject).build();
			}else if(mObject instanceof Ivariable){
				buildMtlO = ivCreator.setMtlO((Ivariable) mObject).build();
			}else if(mObject instanceof Drug){
				buildMtlO = drugCreator.setMtlO((Drug) mObject).build();
			}else if(mObject instanceof Day){
				buildMtlO = dayCreator.setMtlO((Day) mObject).build();
			}else if(mObject instanceof Times){
				buildMtlO = timesCreator.setMtlO((Times) mObject).build();
			}else if(mObject instanceof Notice){
				buildMtlO = noticeCreator.setMtlO((Notice) mObject).build();
			}else if(mObject instanceof Expr){
				buildMtlO = exprCreator.setMtlO((Expr) mObject).build();
			}else if(mObject instanceof Labor){
				buildMtlO = laborCreator.setMtlO((Labor) mObject).build();
			}else if(mObject instanceof Finding){
				buildMtlO = findingCreator.setMtlO((Finding) mObject).build();
			}else continue;
			schemaMtl2import.getIdClassMap().put(mObject.getId(), buildMtlO.getId());
		}
		schemaMtl2import.setDocT2new(taskTnew);
		log.debug(taskTnew);
		log.debug(schemaMtl2import.getDocT());
		copyTree2(taskTnew, schemaMtl2import.getDocT(),schemaMtl2import);
		updateRef(schemaMtl2import);
	}
	private void copyTree2(Tree thisDbT, Tree parentT2import, TreeManager docMtl) {
//		log.debug(1);
//		log.debug(parentT2import);
//		log.debug(parentT2import.isImportChild());
		if(parentT2import.hasChild()){
//		if(parentT2import.isImportChild()){
//			log.debug(2);
			for (Tree tree2copy : parentT2import.getChildTs()) {
//				log.debug(3);
				if(docMtl instanceof ConceptMtl 
						&& null==tree2copy.getParentT().getIdClass()
						&& "definition".equals(tree2copy.getParentT().getTabName())
						&&"task".equals(tree2copy.getTabName())
						){
					log.debug(4);
					Integer idTaskNew = docMtl.getIdClassMap().get(tree2copy.getId());
					Tree newTaskT = docMtl.getTree().get(idTaskNew);
					newTaskT.setParentT(docMtl.getNewDefinitionT());
					newTaskT.setDocT(docMtl.getDocT2new());
					continue;
				}
				Tree newT = newTree(tree2copy, thisDbT,docMtl);
				newT.setDocT(docMtl.getDocT2new());
				//				log.debug(newT);
				docMtl.getOld2newIdM().put(tree2copy.getId(), newT.getId());
				if(tree2copy.getRef()!=null)
					docMtl.addRef(tree2copy);
				if(docMtl instanceof ConceptMtl && "definition".equals(tree2copy.getTabName()))
					docMtl.setNewDefinitionT(newT);
				copyTree2(newT, tree2copy,docMtl);
			}
		}
	}
	private Tree newTree(Tree tree2copy, Tree parentT, TreeManager docMtl) {
		Long sort = null==tree2copy.getSort()?0:tree2copy.getSort();
		Tree addChild = treeCreator.addChild(parentT,tree2copy.getTabName(), sort, docMtl.getDocT(), docMtl);
		addChild.setIdClass(docMtl.getIdClassMap().get(tree2copy.getIdClass()));
		return addChild;
	}
}

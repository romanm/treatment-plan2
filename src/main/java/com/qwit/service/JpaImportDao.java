package com.qwit.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;

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
import com.qwit.domain.Folder;
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
import com.qwit.model.PatientMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.util.FlowObjCreator;

public class JpaImportDao {

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

	private JpaTemplate jpaTemplate;
	public JpaTemplate getJpaTemplate() {return jpaTemplate;}
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.jpaTemplate = new JpaTemplate(emf);
	}
	
	private Tree doc2CopyT;
	public Tree getDoc2CopyT() {return doc2CopyT;}
	private List<Drug> drugs;
	
	@SuppressWarnings("unchecked")
	public Collection readDrugs(final String sql2, final SchemaMtl schemaMtl) throws DataAccessException {
		return (Collection) this.jpaTemplate.execute(new JpaCallback() {

			public Object doInJpa(EntityManager em2import) throws PersistenceException {
				drugs = em2import.createNativeQuery(sql2, Drug.class).getResultList();
				for (Drug drugO : drugs) {
					Tree drugT = em2import.find(Tree.class, drugO.getId());
					Folder folderF = em2import.find(Folder.class, drugT.getParentT().getId());
					while (!"drug".equals(folderF.getParentF().getFolder())) 
						folderF=folderF.getParentF();
					drugO.setFolder(folderF);
				}
				return null;
			}
		});
	}
	private String getJpaSql4mtlO(String tabName) {
		String tabNameUpper = tabName.substring(0, 1).toUpperCase() + tabName.substring(1, tabName.length());
		String sql2 = "SELECT DISTINCT c FROM "+tabNameUpper+" c, Tree t WHERE t.idClass=c.id AND t.docT.id=:idDoc ";
		return sql2;
	}
	@SuppressWarnings("unchecked")
	public Collection readDoc2copyO(final Integer id, final TreeManager docMtl) throws DataAccessException {
		return (Collection) this.jpaTemplate.execute(new JpaCallback() {

			@Override
			public Object doInJpa(EntityManager em2copy) throws PersistenceException {
				Integer idDoc2copy=docMtl.getDocT().getId();
				for (Tree tree : docMtl.getDocT().getDocNodes()) {
					if(!docMtl.getClassM().containsKey(tree.getIdClass())){
						String jpaSql4mtlO = getJpaSql4mtlO(tree.getTabName());
						List<MObject> resultList = em2copy.createQuery(jpaSql4mtlO).setParameter("idDoc", idDoc2copy).getResultList();
						for (MObject mtlO : resultList) 
							docMtl.getClassM().put(mtlO.getId(), mtlO);
					}
					MObject mObject = docMtl.getClassM().get(tree.getIdClass());
					tree.setMtlO(mObject);
					docMtl.getTree().put(tree.getId(), tree);
					docMtl.addIdclassTreeS(tree);
				}
				
//				List<Map<String, Object>> queryForList = simpleJdbc2.queryForList(sqlDocMtlO, idDoc2copy);
//				for (Map<String, Object> map : queryForList) {
//					String tabName = (String) map.get("tab_name");
//					String jpaSql4mtlO = getJpaSql4mtlO(tabName);
//					List<MObject> resultList = em2copy.createQuery(jpaSql4mtlO).setParameter("idDoc", idDoc2copy).getResultList();
//					for (MObject mtlO : resultList) 
//						docMtl.getClassM().put(mtlO.getId(), mtlO);
//				}
				return null;
			}
			
		});
	}
	@SuppressWarnings("unchecked")
	public Collection readDoc2copyT(final Integer id, final TreeManager docMtl) throws DataAccessException {
		return (Collection) this.jpaTemplate.execute(new JpaCallback() {

			@Override
			public Object doInJpa(EntityManager em2copy) throws PersistenceException {
				Tree doc2CopyT = em2copy.find(Tree.class, id);
				docMtl.setDocT2new(doc2CopyT);
				log.debug("doc2CopyT="+doc2CopyT);
				//lazily initialize a collection
				readDoc2copyT(doc2CopyT,doc2CopyT.getId());
				//lazily initialize a collection
				doc2CopyT.getDocNodes().size();
//				log.debug(docNodes.size());
				return null;
			}

			private void readDoc2copyT(Tree parentT, Integer id) {
//				System.out.print(parentT.getId()+", ");
				if((id.equals(parentT.getDocT().getId())||id.equals(parentT.getId()))&& parentT.hasChild())
					for (Tree childT : parentT.getChildTs()) 
						readDoc2copyT(childT, id);
			}
		});

	}
	@SuppressWarnings("unchecked")
	public Collection copyTree(final Integer id, final TreeManager docMtl) throws DataAccessException {
//		public Collection copyTree(final Integer id, final ConceptMtl conceptMtl) throws DataAccessException {
		return (Collection) this.jpaTemplate.execute(new JpaCallback() {

			public Object doInJpa(EntityManager em2import) throws PersistenceException {
				doc2CopyT = em2import.find(Tree.class, id);
				log.debug("doc2CopyT="+doc2CopyT);
				
				log.debug("docMtl.getDocT()="+docMtl.getDocT());
				/* REM for patient locale copy */
				importChild(doc2CopyT,docMtl.getDocT());
				log.debug("docMtl.getOld2newIdM().size()="+docMtl.getOld2newIdM().size());
//				docMtl.addRefR(doc2CopyT);
				return null;
			}

			private void importChild(Tree tree,Tree parentT) {
				List<Tree> childTs = tree.getChildTs();
				if(childTs!=null)
				for(Tree tree2copy:childTs){
					if("definition".equals(tree.getTabName())&&"task".equals(tree2copy.getTabName()))
						continue;
					Integer oldId = tree2copy.getId();
					Tree newT = newTree(tree2copy, parentT);
					Integer newId = newT.getId();
//					log.debug(" oldId="+oldId+" newId="+newId);
					docMtl.getOld2newIdM().put(oldId, newId);
					if(tree2copy.getRef()!=null)
						docMtl.addRef(tree2copy);
					importChild(tree2copy,newT);
				}
			}

			private Tree newTree(Tree tree2copy, Tree parentT) {
				Tree addChild = treeCreator.addChild(parentT,tree2copy.getTabName(), tree2copy.getSort(), docMtl.getDocT(), docMtl);
				addChild.setIdClass(docMtl.getIdClassMap().get(tree2copy.getIdClass()));
				return addChild;
			}

		});
	}
	
	public void buildImportTask(ConceptMtl conceptMtl, EntityManager em ) {
		for(Integer taskId:conceptMtl.getIdClassTaskMap().keySet()) {
			Tree tree = conceptMtl.getIdClassTaskMap().get(taskId);
//			SchemaMtl schemaMtl = (SchemaMtl) new SchemaMtl(tree).setMap2copy();
			SchemaMtl schemaMtl = (SchemaMtl) new SchemaMtl(tree);
			List<Map<String, Object>> queryForList = simpleJdbc2.queryForList(sqlDocMtlO, taskId);
			for (Map<String, Object> map : queryForList) {
				String tabName = (String) map.get("tab_name");
				String sql2 = getSql2(tabName);
				if("drug".equals(tabName)){
					sql2 = sql2.replace("?", taskId.toString());
					readDrugs(sql2, schemaMtl);
					drug(schemaMtl);
					continue;
				}
				List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql2, taskId);
				log.debug(sql2+" -- "+taskId+" "+queryForList2.size());
				if("dose".equals(tabName)){
					dose(queryForList2,schemaMtl);
				}else if("tablet".equals(tabName)){
					tablet(queryForList2,schemaMtl);
				}else if("app".equals(tabName)){
					app(queryForList2,schemaMtl);
				}else if("task".equals(tabName)){
					task(queryForList2,schemaMtl);
				}else if("day".equals(tabName)){
					day(queryForList2,schemaMtl);
				}else if("times".equals(tabName)){
					times(queryForList2,schemaMtl);
				}else if("notice".equals(tabName)){
					notice(queryForList2,schemaMtl);
				}else if("expr".equals(tabName)){
					expr(queryForList2,schemaMtl);
				}else if("labor".equals(tabName)){
					labor(queryForList2,schemaMtl);
				}else if("pvariable".equals(tabName)){
					pvariable(queryForList2,schemaMtl);
				}else if("finding".equals(tabName)){
					finding(queryForList2,schemaMtl);
				}
			}
			
			log.debug("------------------------------ 1");
			log.debug(tree);
//			log.debug("------------------------------ 2");
			
			schemaMtl.initOld2newIdM();
//			log.debug("------------------------------ 3");
			log.debug("taskId="+taskId+" schemaMtl.getDocT().getId()="+schemaMtl.getDocT().getId());
			schemaMtl.getOld2newIdM().put(taskId, schemaMtl.getDocT().getId());
//			log.debug("------------------------------ 4");
//			schemaMtl.initRef2idMS();
//			log.debug("------------------------------ 5");
			schemaMtl.addRefR(schemaMtl.getDocT());
			copyTree(taskId, schemaMtl);
			em.merge(schemaMtl.getDocT());
//			log.debug("------------------------------ 6");
			schemaMtl.updateRef2(em);
//			em.merge(schemaMtl.getDocT());
			log.debug("------------------------------ 7");
		}
	}
	
	private void app(List<Map<String, Object>> queryForList2, SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idapp = (Integer) map.get("idapp");
			if(schemaMtl.getIdClassMap().containsKey(idapp))	continue;
			App mtlO = new App().addAtt(map);
			MObject build = appCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idapp, build.getId());
		}
		
	}
	private void pvariable(List<Map<String, Object>> queryForList2, SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idday = (Integer) map.get("idpvariable");
			if(schemaMtl.getIdClassMap().containsKey(idday))	continue;
			Pvariable mtlO = new Pvariable().addAtt(map);
			MObject build = pvCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idday, build.getId());
		}
	}
	private void task(List<Map<String, Object>> queryForList2, SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idday = (Integer) map.get("idtask");
			if(schemaMtl.getIdClassMap().containsKey(idday))	continue;
			Task mtlO = new Task().addAtt(map);
			MObject build = taskCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idday, build.getId());
		}
	}
	private void finding(List<Map<String, Object>> queryForList2, SchemaMtl docMtl) {
//	private void finding(List<Map<String, Object>> queryForList2, TreeManager docMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idday = (Integer) map.get("idfinding");
			if(docMtl.getIdClassMap().containsKey(idday))	continue;
			Finding mtlO = new Finding().addAtt(map);
			MObject build = findingCreator.setMtlO(mtlO).build();
			docMtl.getIdClassMap().put(idday, build.getId());
		}
	}

	private void labor(List<Map<String, Object>> queryForList2, SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idday = (Integer) map.get("idlabor");
			if(schemaMtl.getIdClassMap().containsKey(idday))	continue;
			Labor mtlO = new Labor().addAtt(map);
			MObject build = laborCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idday, build.getId());
		}
	}

	private void expr(List<Map<String, Object>> queryForList2 ,  SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idday = (Integer) map.get("idexpr");
			if(schemaMtl.getIdClassMap().containsKey(idday))	continue;
			Expr mtlO = new Expr().addAtt(map);
			MObject build = exprCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idday, build.getId());
		}
	}

	private void notice(List<Map<String, Object>> queryForList2 , SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idday = (Integer) map.get("idnotice");
			if(schemaMtl.getIdClassMap().containsKey(idday))	continue;
			Notice mtlO = new Notice().addAtt(map);
			MObject build = noticeCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idday, build.getId());
		}
	}


	private void day(List<Map<String, Object>> queryForList2 , SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idday = (Integer) map.get("idday");
			if(schemaMtl.getIdClassMap().containsKey(idday))	continue;
			Day mtlO = new Day().addAtt(map);
			MObject build = dayCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idday, build.getId());
		}
	}

	private void times(List<Map<String, Object>> queryForList2 ,  SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idclass = (Integer) map.get("idtimes");
			if(schemaMtl.getIdClassMap().containsKey(idclass))	continue;
			Times mtlO = new Times().addAtt(map);
			log.debug(mtlO);
			MObject build = timesCreator.setMtlO(mtlO).build();
			log.debug(build);
			log.debug(idclass);
			log.debug(build.getId());
			schemaMtl.getIdClassMap().put(idclass, build.getId());
		}
	}

	private void tablet(List<Map<String, Object>> queryForList2 , SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer idtablet = (Integer) map.get("idtablet");
			if(schemaMtl.getIdClassMap().containsKey(idtablet))	continue;
			Tablet mtlO = new Tablet().addAtt(map);
			MObject build = tabletCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(idtablet, build.getId());
		}
	}
	private void dose(List<Map<String, Object>> queryForList2 , SchemaMtl schemaMtl) {
		for (Map<String, Object> map : queryForList2){
			Integer iddose = (Integer) map.get("iddose");
			if(schemaMtl.getIdClassMap().containsKey(iddose))	continue;
			Dose mtlO = new Dose().addAtt(map);
			MObject build = doseCreator.setMtlO(mtlO).build();
			schemaMtl.getIdClassMap().put(iddose, build.getId());
		}
	}

	private void drug( SchemaMtl schemaMtl) {
		for (Drug drug : drugs){
			if(schemaMtl.getIdClassMap().containsKey(drug.getId()))	continue;
			MObject build = drugCreator.setMtlO(drug).build();
			schemaMtl.getIdClassMap().put(drug.getId(), build.getId());
		}
	}
	String sqlDocMtlO = "SELECT count(*),tab_name FROM tree WHERE idDoc=? GROUP BY tab_name";
	
	private String getSql2(String tabName) {
		String tabNameUpper = tabName.substring(0, 1).toUpperCase() + tabName.substring(1, tabName.length());
		String sql2 = "SELECT c.* FROM "+tabNameUpper+" c, tree WHERE idclass=c.id"+tabName+" AND idDoc=? ";
		return sql2;
	}
	public PatientMtl buildCopyPatient(Integer idDoc2copy, PatientMtl targetDocMtl){
		log.debug(1);
		readDoc2copyT(idDoc2copy, targetDocMtl);
		PatientMtl docMtl2copy = new PatientMtl(targetDocMtl.getDocT2new());
		readDoc2copyO(idDoc2copy, docMtl2copy);
		for (MObject mtlO2copy : docMtl2copy.getClassM().values()) {
			MObject newMtlO=null;
			if(mtlO2copy instanceof Pvariable){
				newMtlO = pvCreator.setMtlO((Pvariable)mtlO2copy).build();
			}else if(mtlO2copy instanceof Ivariable){
				newMtlO = ivCreator.setMtlO((Ivariable)mtlO2copy).build();
			}else if(mtlO2copy instanceof Diagnose){
				newMtlO = diagnoseCreator.setMtlO((Diagnose)mtlO2copy).build();
			}else if(mtlO2copy instanceof Finding){
				newMtlO = findingCreator.setMtlO((Finding)mtlO2copy).build();
			}
			//Preparation to simple paste with idclass
			if(null!=newMtlO&&!newMtlO.getId().equals(mtlO2copy.getId())){
//				docMtl2copy.getOld2newIdM().put(mtlO2copy.getId(), newMtlO.getId());
				for (Tree tree : docMtl2copy.getIdClass2TreeMS().get(mtlO2copy.getId())) {
					log.debug(tree);
					tree.setIdClass(newMtlO.getId());
					log.debug(tree);
				}
			}
		}
		

//		List<Map<String, Object>> queryForList = simpleJdbc2.queryForList(sqlDocMtlO, idDoc2copy);
//		for (Map<String, Object> map : queryForList) {
//			String tabName = (String) map.get("tab_name");
//			String sql2 = getSql2(tabName);
//			List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql2, idDoc2copy);
//			log.debug(sql2+" -- "+idDoc2copy+" "+queryForList2.size());
//			if("diagnose".equals(tabName)){
//				diagnose(idDoc2copy, sql2, docMtl2copy);
//			}else if("pvariable".equals(tabName)){
//				pvariable(queryForList2,docMtl2copy);
//			}else if("finding".equals(tabName)){
//				finding(queryForList2,docMtl2copy);
//			}
//		}
//		log.debug("docMtl2copy.getIdClassMap().size()="+docMtl2copy.getIdClassMap().size());
		log.debug(2);
		return docMtl2copy;
	}
	public void buildCopyProtocol(Integer idImport, ConceptMtl conceptMtl, Tree folderT, EntityManager em){
		conceptMtl.getDocT().setParentT(folderT);
		conceptMtl.getDocT().setDocT(conceptMtl.getDocT());

		List<Map<String, Object>> queryForList = simpleJdbc2.queryForList(sqlDocMtlO, idImport);
		for (Map<String, Object> map : queryForList) {
			String tabName = (String) map.get("tab_name");
			//first char to upper case
			String sql2 = getSql2(tabName);
			if("definition|include|exclude|choice".contains(tabName))	continue;
			
			if("protocol".equals(tabName)){
			}else if("studyarm".equals(tabName)){
				studyarm(idImport, sql2,conceptMtl);
			}else if("institute".equals(tabName)){
				institute(idImport, sql2,conceptMtl);
			}else if("checklist".equals(tabName)){
				checklist(idImport, sql2,conceptMtl);
			}else if("checkitem".equals(tabName)){
				checkitem(idImport, sql2,conceptMtl);
//				checkitem(idImport, "SELECT c.* FROM checkitem c,tree WHERE idclass=c.idcheckitem AND idDoc=? ",conceptMtl);
			}else if("contactperson".equals(tabName)){
				contactperson(idImport, sql2,conceptMtl);
			}else if("document".equals(tabName)){
				document(idImport, sql2,conceptMtl);
			}else if("diagnose".equals(tabName)){
				diagnose(idImport, sql2,conceptMtl);
			}else if("personrole".equals(tabName)){
				personrole(idImport, sql2,conceptMtl);
			}
		}
		conceptMtl.initOld2newIdM();
		copyTask(idImport, conceptMtl);
		copyTree(idImport, conceptMtl);
		em.merge(conceptMtl.getDocT());
		JXPathBean jxp = new JXPathBean();
		jxp.setTreeManager(conceptMtl);
		Tree definitionT = (Tree) jxp.getJxp().getValue("docT/childTs[tabName='definition']");
		if(definitionT!=null)
			for(Tree taskT:conceptMtl.getIdClassTaskMap().values()){
				taskT.setParentT(definitionT);
			}
		buildImportTask(conceptMtl,em);
	}

	public void buildImportProtocol(Integer idImport,ConceptMtl conceptMtl , Tree folderT, EntityManager em){
		conceptMtl.getDocT().setParentT(folderT);
		conceptMtl.getDocT().setDocT(conceptMtl.getDocT());

		List<Map<String, Object>> queryForList = simpleJdbc2.queryForList(sqlDocMtlO, idImport);
		for (Map<String, Object> map : queryForList) {
			String tabName = (String) map.get("tab_name");
			//first char to upper case
			String sql2 = getSql2(tabName);
			if("definition|include|exclude|choice".contains(tabName))	continue;
			
			if("protocol".equals(tabName)){
			}else if("studyarm".equals(tabName)){
				studyarm(idImport, sql2,conceptMtl);
			}else if("institute".equals(tabName)){
				institute(idImport, sql2,conceptMtl);
			}else if("checklist".equals(tabName)){
				checklist(idImport, sql2,conceptMtl);
			}else if("checkitem".equals(tabName)){
				checkitem(idImport, sql2,conceptMtl);
//				checkitem(idImport, "SELECT c.* FROM checkitem c,tree WHERE idclass=c.idcheckitem AND idDoc=? ",conceptMtl);
			}else if("contactperson".equals(tabName)){
				contactperson(idImport, sql2,conceptMtl);
			}else if("document".equals(tabName)){
				document(idImport, sql2,conceptMtl);
			}else if("diagnose".equals(tabName)){
				diagnose(idImport, sql2,conceptMtl);
			}else if("personrole".equals(tabName)){
				personrole(idImport, sql2,conceptMtl);
			}
		}
		conceptMtl.initOld2newIdM();
		task(idImport, conceptMtl);
		copyTree(idImport, conceptMtl);
		em.merge(conceptMtl.getDocT());
		JXPathBean jxp = new JXPathBean();
		jxp.setTreeManager(conceptMtl);
		Tree definitionT = (Tree) jxp.getJxp().getValue("docT/childTs[tabName='definition']");
		if(definitionT!=null)
			for(Tree taskT:conceptMtl.getIdClassTaskMap().values()){
				taskT.setParentT(definitionT);
			}
		buildImportTask(conceptMtl,em);
	}

	private void personrole(Integer idImport, String sql, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer id = (Integer) map.get("idpersonrole");
			if(conceptMtl.getIdClassMap().containsKey(id))	continue;
			Personrole doc = new Personrole();
			doc.setPersonrole((String) map.get("personrole"));
			MObject build = personroleCreator.setMtlO(doc).build();
			conceptMtl.getIdClassMap().put(id, build.getId());
		}
	}
	
	public Protocol protocolCopy(Integer idImport) {
		String sql = "SELECT * FROM protocol WHERE idprotocol=?";
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		Map<String, Object> map = queryForList2.get(0);
		Protocol doc = new Protocol();
		doc.setProtocol((String) map.get("protocol"));
		int nextDbid = protocolCreator.nextDbid()+1;
		doc.setProtocolvar(nextDbid+" "+(String) map.get("protocolvar"));
		doc.setProtocoltype((String) map.get("protocoltype"));
		doc.setPhase((String) map.get("phase"));
		doc.setProspective((String) map.get("prospective"));
		doc.setRandomized((String) map.get("randomized"));
		doc.setExpansion((String) map.get("expansion"));
		doc.setIndication((String) map.get("indication"));
		doc.setInfostatus((String) map.get("infostatus"));
		doc.setIntention((String) map.get("intention"));
		doc.setStatus((String) map.get("status"));
		doc.setStudynumber((String) map.get("studynumber"));
		
		doc.setStarttime((Timestamp) map.get("starttime"));
		doc.setEnddate((Timestamp) map.get("enddate"));
		doc.setStarttext((String) map.get("starttext"));
		doc.setStand((Timestamp) map.get("stand"));
		doc.setAmendment((Timestamp) map.get("amendment"));
		doc.setAmendmentnr((Integer) map.get("amendmentnr"));
		doc.setInfodate((Timestamp) map.get("infodate"));
		
		doc.setLongname((String) map.get("longname"));
		Protocol build = (Protocol) protocolCreator.setFoc(foc).setProtocol(doc).build();
		return build;
	}
	public Protocol protocol(Integer idImport) {
		String sql = "SELECT * FROM protocol WHERE idprotocol=?";
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		Map<String, Object> map = queryForList2.get(0);
		Protocol doc = new Protocol();
		doc.setProtocol((String) map.get("protocol"));
		doc.setProtocolvar((String) map.get("protocolvar"));
		doc.setProtocoltype((String) map.get("protocoltype"));
		doc.setPhase((String) map.get("phase"));
		doc.setProspective((String) map.get("prospective"));
		doc.setRandomized((String) map.get("randomized"));
		doc.setExpansion((String) map.get("expansion"));
		doc.setIndication((String) map.get("indication"));
		doc.setInfostatus((String) map.get("infostatus"));
		doc.setIntention((String) map.get("intention"));
		doc.setStatus((String) map.get("status"));
		doc.setStudynumber((String) map.get("studynumber"));
		
		doc.setStarttime((Timestamp) map.get("starttime"));
		doc.setEnddate((Timestamp) map.get("enddate"));
		doc.setStarttext((String) map.get("starttext"));
		doc.setStand((Timestamp) map.get("stand"));
		doc.setAmendment((Timestamp) map.get("amendment"));
		doc.setAmendmentnr((Integer) map.get("amendmentnr"));
		doc.setInfodate((Timestamp) map.get("infodate"));
		
		doc.setLongname((String) map.get("longname"));
		Protocol build = (Protocol) protocolCreator.setFoc(foc).setProtocol(doc).build();
		return build;
	}

	private String sqlProtocolTask = "select task.* " +
			" from tree t1,tree t2,task where t1.id=? and t2.iddoc=t1.id and t2.id=idtask";
	private FlowObjCreator foc;
	public void copyTask(Integer idImport, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sqlProtocolTask, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer id = (Integer) map.get("idtask");
			Task task = new Task();
			String taskTask = (String) map.get("task");
			taskTask=taskTask.replace("'", "′");
			task.setTask(taskTask);
			task.setType((String) map.get("type"));
			task.setCyclename((String) map.get("cyclename"));
			task.setDuration((Integer) map.get("duration"));
			task.setStartnum((Integer) map.get("startnum"));
			String taskvar=""+(protocolCreator.nextDbid()+1);
			task.setTaskvar(taskvar);
			log.debug(task);
			Tree t = taskCreator.setMtlO(task).createReadTree();
			conceptMtl.getIdClassMap().put(id, t.getId());
			conceptMtl.getIdClassTaskMap().put(id, t);
			t.setDocT(conceptMtl.getDocT());
		}
	}
	public void task(Integer idImport, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sqlProtocolTask, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer id = (Integer) map.get("idtask");
			Task task = new Task();
			String taskTask = (String) map.get("task");
			taskTask=taskTask.replace("'", "′");
			task.setTask(taskTask);
			task.setType((String) map.get("type"));
			task.setCyclename((String) map.get("cyclename"));
			task.setDuration((Integer) map.get("duration"));
			task.setStartnum((Integer) map.get("startnum"));
			log.debug(task);
			Tree t = taskCreator.setMtlO(task).createReadTree();
			conceptMtl.getIdClassMap().put(id, t.getId());
			conceptMtl.getIdClassTaskMap().put(id, t);
			t.setDocT(conceptMtl.getDocT());
		}
	}

	private void studyarm(Integer idImport, String sql, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer id = (Integer) map.get("idstudyarm");
			if(conceptMtl.getIdClassMap().containsKey(id))	continue;
			Arm arm = new Arm();
			arm.setArm((String) map.get("studyarm"));
			MObject build = armCreator.setMtlO(arm).build();
			conceptMtl.getIdClassMap().put(id, build.getId());
		}
	}

	private void document(Integer idImport, String sql, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer id = (Integer) map.get("iddocument");
			if(conceptMtl.getIdClassMap().containsKey(id))	continue;
			Document doc = new Document();
			doc.setDocument((String) map.get("document"));
			doc.setContenttype((String) map.get("contenttype"));
			doc.setFilename((String) map.get("filename"));
			doc.setType((String) map.get("type"));
			doc.setUrl((String) map.get("url"));
			MObject build = documentCreator.setMtlO(doc).build();
			conceptMtl.getIdClassMap().put(id, build.getId());
		}
	}

	private void checklist(Integer idImport, String sql, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer idchecklist = (Integer) map.get("idchecklist");
			if(conceptMtl.getIdClassMap().containsKey(idchecklist))	continue;
			Checklist cl = new Checklist().addAtt(map);
			MObject build = checklistCreator.setMtlO(cl).build();
			conceptMtl.getIdClassMap().put(idchecklist, build.getId());
		}
	}

	private void checkitem(Integer idImport, String sql, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer idcheckitem = (Integer) map.get("idcheckitem");
			if(conceptMtl.getIdClassMap().containsKey(idcheckitem))	continue;
			Checkitem ci = new Checkitem();
//			ci.setCheckitem(stringCorrectur(map));
			ci.setCheckitem((String) map.get("checkitem"));
			ci.setType((String) map.get("type"));
			MObject build = checkitemCreator.setMtlO(ci).build();
			if(build==null)
				return;
			conceptMtl.getIdClassMap().put(idcheckitem, build.getId());
		}
	}

	private void institute(Integer idImport, String sql, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2){
			Integer idinstitute = (Integer) map.get("idinstitute");
			if(conceptMtl.getIdClassMap().containsKey(idinstitute))
				continue;
			Institute in = new Institute();
			in.setInstitute((String) map.get("institute"));
			in.setCity((String) map.get("city"));
			in.setZip((String) map.get("zip"));
			in.setStreet((String) map.get("street"));
			in.setCountry((String) map.get("country"));
			
			in.setPhone((String) map.get("phone"));
			in.setFax((String) map.get("fax"));
			in.setStyletask((String) map.get("styletask"));
			in.setShortname((String) map.get("shortname"));
			in.setTaskstart((String) map.get("taskstart"));
			
			in.setDoseRoundPercent((Integer) map.get("dose_round_percent"));
			in.setMorning((String) map.get("morning"));
			in.setNoon((String) map.get("noon"));
			in.setEvening((String) map.get("evening"));
			in.setNight((String) map.get("night"));
			
			in.setBsaformula((String) map.get("bsaformula"));
			in.setDaysintensive((String) map.get("daysintensive"));
			in.setNewtherapydayhour((String) map.get("newtherapydayhour"));
		
			in.setOldtimescalc((String) map.get("oldtimescalc"));
			in.setPrintpaper((String) map.get("printpaper"));
			in.setPlTaskShow((String) map.get("pl_task_show"));
			in.setTaskEdDtt((String) map.get("task_ed_dtt"));
			
			in.setTaskCycleTemplate((String) map.get("task_cycle_template"));
			in.setTaskstartHide((String) map.get("taskstart_hide"));
			/*
			 * */
			
			MObject build = instituteCreator.setMtlO(in).build();
			conceptMtl.getIdClassMap().put(idinstitute, build.getId());
		}
		
	}

	private void contactperson(Integer idImport, String sql, ConceptMtl conceptMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2)
		{
			Integer id = (Integer) map.get("idcontactperson");
			if(conceptMtl.getIdClassMap().containsKey(id))
				continue;
			
			Contactperson cl = new Contactperson();
			cl.setContactperson((String) map.get("contactperson"));
			cl.setForename((String) map.get("forename"));
			
			MObject build = contactpersonCreator.setMtlO(cl).build();
		}
	}

//	private void diagnose(Integer idImport, String sql, ConceptMtl conceptMtl) {
	private void diagnose(Integer idImport, String sql, TreeManager docMtl) {
		List<Map<String, Object>> queryForList2 = simpleJdbc2.queryForList(sql, idImport);
		for (Map<String, Object> map : queryForList2)
		{
			Integer id = (Integer) map.get("iddiagnose");
			if(docMtl.getIdClassMap().containsKey(id))	continue;
			
			Diagnose cl = new Diagnose();
			cl.setDiagnose((String) map.get("diagnose"));
			
			MObject build = diagnoseCreator.setMtlO(cl).build();
			docMtl.getIdClassMap().put(id, build.getId());
		}
	}
	public JpaImportDao setFoc(FlowObjCreator foc) {
		this.foc=foc;
		return this;
	}

//	private String stringCorrectur(Map<String, Object> map) {
//		String string = (String) map.get("checkitem");
//		string=string.replace("  ", " ")
//		.replace("and", "AND")
//		.replace("vor", "vOR")
//		.replace("for", "fOR");
//		return string;
//	}

}

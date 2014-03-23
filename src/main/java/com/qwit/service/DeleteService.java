package com.qwit.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Drug;
import com.qwit.domain.Folder;
import com.qwit.domain.Tree;
import com.qwit.model.ConceptMtl;
import com.qwit.model.DrugMtl;
import com.qwit.model.PatientMtl;
import com.qwit.model.PatientSchema;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.util.FlowObjCreator;

@Service("deleteService")
public class DeleteService extends MtlDbService {
	private final Log log = LogFactory.getLog(getClass());
	
	public void schemaRewriteConfirmEntry(PatientSchema ps, TreeManager docMtl){
		log.debug("-------------");
		log.debug("-------------");
	}
	@Transactional
	public void schemaRewriteConfirmSave(PatientSchema ps, TreeManager docMtl){
		log.debug("-------------");
		Tree pschemaT = schemaMtl(docMtl).getPschemaT();
		log.debug("-------------");
		em.createQuery("DELETE FROM Tree t WHERE t.parentT.id=:did AND t.tabName='notice'")
		.setParameter("did", pschemaT.getId())
		.executeUpdate();
		log.debug("-------------");
		deleteTaskWithRef(docMtl.getDocT().getId());
		log.debug("-------------");
		Integer idClass = docMtl.getDocT().getIdClass();
		log.debug("-------------");
		Tree fromSchemaT = readDocT(idClass);
		log.debug(fromSchemaT);
		copyTaskAll(schemaMtl(docMtl), fromSchemaT);
	}
	public void deleteFolderEntry(FlowObjCreator foc,Integer idc){
		foc.setIdf(idc);
		Folder idcF=em.find(Folder.class, idc);
		foc.setFolderO(idcF);
	}
	@Transactional
	public void deleteFolder(FlowObjCreator foc){
		log.debug("-------------");
		foc.getFolderO().getParentF();
		Folder folderO = foc.getFolderO();
		log.debug("-------------"+folderO);
		Integer id = folderO.getId();
		log.debug("------------- id= "+id);
		Query createQuery = em.createQuery("SELECT t FROM Tree t WHERE t.parentT.id=:did");
		List resultList = createQuery.setParameter("did", id).getResultList();
		log.debug("-------------"+resultList);
		if(resultList.size()==0){
			Tree fT=em.find(Tree.class, id);
			String nodeInDeletedNode = "SELECT t FROM Tree t WHERE t.id!=t.idClass AND t.id=:id";
			List resultList2 = em.createQuery(nodeInDeletedNode).setParameter("id", id).getResultList();
			log.debug("-------------"+resultList2);
//			if(resultList2.size()==0){
			if(resultList2.size()!=0){
				log.debug("-------------");
				move2trashFolder(fT);
			}else{
				log.debug("-------------");
				Integer id2 = folderO.getParentF().getId();
				log.debug("-------------"+fT);
				foc.setIdf(id2);
				log.debug("-------------");
				simpleJdbc.update("DELETE FROM folder WHERE idFolder=? ", id);
				log.debug("-------------");
				simpleJdbc.update("DELETE FROM tree WHERE id=? ", id);
				log.debug("-------------");
				foc.setIdf(fT.getParentT().getId());
				//			//em.remove(fT); ATTENTION not use 
				//em.remove(folderO);ATTENTION this command delete all folder.
			}
		}
	}
	@Transactional(readOnly = true)
	public void confirm(PatientMtl docMtl){
		Tree editNodeT = docMtl.getEditNodeT();
		if("protocol".equals(editNodeT.getTabName())){
			log.debug(editNodeT);
			log.debug(editNodeT.getId());
			String sql = "SELECT t1.id FROM tree t1,tree t2 WHERE t1.ref=t2.id AND t2.iddoc=?";
			Set<Integer> set=new HashSet<Integer>();
			for (Map<String, Object> map : simpleJdbc.queryForList(sql, editNodeT.getId())) 
				set.add((Integer) map.get("id"));
			docMtl.setTask4protocol(set);
		}
	}
	@Transactional
	public void deletePatientHistory(PatientMtl docMtl){
		Tree editNodeT = docMtl.getEditNodeT();
		log.debug(editNodeT);
		if("protocol".equals(editNodeT.getTabName())){
			for (Integer taskId : docMtl.getTask4protocol())
				deleteTree(taskId);
			deleteTree(editNodeT.getId());
		}else if("app".equals(editNodeT.getTabName())){
			Tree exprT = editNodeT.getParentT();
			if("expr".equals(exprT.getTabName())){
				deleteTree(exprT.getId());
				if(1==exprT.getParentT().getChildTs().size())
					deleteTree(exprT.getParentT().getId());
			}
		}else if("diagnose".equals(editNodeT.getTabName())){
			deleteTree(editNodeT.getId());
		}else if("labor".equals(editNodeT.getTabName())){
			deleteTree(editNodeT.getId());
		}else if("finding".equals(editNodeT.getTabName())){
			deleteTree(editNodeT.getId());
		}else if("notice".equals(editNodeT.getTabName())){
			deleteTree(editNodeT.getId());
		}else if("ivariable".equals(editNodeT.getTabName())){
			deleteTree(editNodeT.getId());
		}else if("task".equals(editNodeT.getTabName())){
			String sql = "SELECT t2.iddoc AS idprotocol " +
			" FROM tree t0, tree t1, tree t2,tree t3,tree t4 " +
			" WHERE t1.id=? " +
			" AND t2.id=t1.ref AND t0.id=t1.did AND t3.iddoc=t2.iddoc " +
			" AND t0.id=t4.did AND t4.ref=t3.id ";
			log.debug(sql+" "+editNodeT.getId());
			List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, editNodeT.getId());
			deleteTree(editNodeT.getId());
			deleteTaskWithRef(editNodeT.getRef());
			if(1==queryForList.size()){
				deleteTree((Integer) queryForList.get(0).get("idprotocol"));
			}
		}
	}
	public void deleteTree(Integer id) {
		simpleJdbc.update("DELETE FROM tree WHERE id=?", id);
	}
	private void deleteTaskWithRef(Integer did) {
		simpleJdbc.update("UPDATE tree SET ref=NULL WHERE ref IS NOT NULL AND iddoc=?", did);
		simpleJdbc.update("DELETE FROM tree WHERE did=?", did);
	}
	private Tree convertIdTC2editNodeT_depr(TreeManager docMtl) {
		Tree editNodeT=null;
		if(docMtl.getIdt()!=null)
			editNodeT = docMtl.getTree().get(docMtl.getIdt());
		log.debug(editNodeT);
		if(docMtl.getIdc()!=null) {
			editNodeT = docMtl.getTree().get(docMtl.getIdc());
			if(	null==editNodeT
			&&	null!=docMtl.getPatientMtl()
			&&	null!=docMtl.getPatientMtl().getTree().get(docMtl.getIdc())
			){
				editNodeT=docMtl.getPatientMtl().getTree().get(docMtl.getIdc());
			}
		}
		return editNodeT;
	}
	private Tree convertIdTC2editNodeT(TreeManager docMtl) {
		Tree editNodeT = getRequestTree(docMtl, "idt");
		if(null==editNodeT)
			editNodeT = getRequestTree(docMtl, "idc");
		return editNodeT;
	}
	private Tree getRequestTree(TreeManager docMtl, String paramName) {
		log.debug(paramName);
		Tree editNodeT=null;
		String paramId=owsSession.getRequest().getParameter(paramName);
		log.debug(paramId);
		if(null!=paramId&&paramId.length()>0){
			int id = Integer.parseInt(paramId);
			editNodeT = docMtl.getTree().get(id);
			if(null==editNodeT&&null!=docMtl.getPatientMtl())
			{
				editNodeT = docMtl.getPatientMtl().getTree().get(id);
			}
		}
		return editNodeT;
	}
	public void deleteConceptEditNode(ConceptMtl docMtl){
		Tree editNodeT = convertIdTC2editNodeT(docMtl);
		log.debug("-------------"+editNodeT);
		if(editNodeT.getDocT().equals(editNodeT))//not delete the document from document
			return;
		if(editNodeT!=null){
			if("task".equals(editNodeT.getTabName())){
				if(editNodeT.getId().equals(editNodeT.getIdClass()))
					//definition schema
				{
					//not delete last definition schema
					boolean lastDefinitionSchema = lastDefinitionSchema(editNodeT);
					if(lastDefinitionSchema)
						return;
					//delete schema
					docMtl.setIdc(editNodeT.getId());
					trashSchemaDefinition(docMtl, editNodeT);
				}else{
					del(editNodeT,docMtl);
				}
			}else if("studyarm".equals(editNodeT.getTabName())){
				del(editNodeT,docMtl);
			}else if("choice".equals(editNodeT.getTabName())){
				del(editNodeT,docMtl);
			}
		}
	}
	

	public void deleteEditNode(TreeManager docMtl){
		log.debug(docMtl.getIdc());
		log.debug(docMtl.getIdt());
		Tree editNodeT = convertIdTC2editNodeT(docMtl);
		log.debug(editNodeT);
		if(editNodeT.getDocT().equals(editNodeT))//not delete the document from document
			return;
		log.debug("--------------"+docMtl.getAccessRight());
		if(TreeManager.RIGHT_WriteYes>docMtl.getAccessRight())
			return;//no right to delete
		log.debug("--------------");
		if(editNodeT!=null){
			log.debug("--------------");
			if("times".equals(editNodeT.getTabName())){
				delNotlast(editNodeT,docMtl,"times");
			}else if("day".equals(editNodeT.getTabName())){
				delNotlast(editNodeT,docMtl,"day");
			}else if("expr".equals(editNodeT.getTabName())){
				if(docMtl.isThenExpr(editNodeT)){
				}else if(docMtl.isLastAndOrExpr(editNodeT)){
					log.debug("----------------");
					Tree andOrT = editNodeT.getParentT();
					Tree ifT = andOrT.getParentT();
					Tree eqLastT=null;
					for(Tree t:andOrT.getChildTs())
						if(t!=editNodeT)
					{
							eqLastT=t;
							break;
					}
					log.debug("----------------");
					docMtl.initOld2newIdM();
					Tree pasteT = docMtl.pasteT(eqLastT, ifT);
					pasteT.setSort(1);
					log.debug("----------------");
					del(andOrT,docMtl);
					log.debug("----------------");
				}else{
					del(editNodeT,docMtl);
				}
			}else if("dose".equals(editNodeT.getTabName())){
				if("expr".equals(editNodeT.getParentT().getTabName())){
					del(editNodeT,docMtl);
				}else{
					editNodeT.setIdClass(null);
				}
			}else if("task".equals(editNodeT.getTabName())){
				log.debug("---------------------"+docMtl);
				if(docMtl instanceof SchemaMtl){
					log.debug("---------------------");
					simpleJdbc.update("UPDATE tree SET ref = NULL WHERE id IN (" +
					" SELECT t1.id FROM tree t1 WHERE t1.iddoc=? AND ref IS NOT NULL)", editNodeT.getId());
					simpleJdbc.update("DELETE FROM tree WHERE did=?", editNodeT.getId());
				}else if(docMtl instanceof DrugMtl){
//					if("drug".equals(editNodeT.getTabName())){
					if("drug".equals(editNodeT.getTabName()) && editNodeT.getId()==editNodeT.getIdClass()){
						log.debug("---------------------"+editNodeT);
						log.debug("---------------------"+editNodeT.getMtlO());
						simpleJdbc.update("DELETE FROM drug WHERE iddrug=?", editNodeT.getMtlO().getId());
						log.debug("---------------------");
					}else{
						del(editNodeT,docMtl);
					}
				}else if(docMtl instanceof ConceptMtl){
					log.debug("---------------------");
					del(editNodeT,docMtl);
				}else{
					for(Tree t:editNodeT.getChildTs()){
						del(t,docMtl);
					}
				}
//			}else if("pvariable".equals(editNodeT.getTabName())){
//				del();
//			}else if("notice".equals(editNodeT.getTabName())){
//				del();
//			}else if("drug".equals(editNodeT.getTabName())){
//				del();
			}else{
				log.debug("---------------------");
				del(editNodeT,docMtl);
				log.debug("---------------------");
			}
			docMtl.nextCurrentId();
			
		}else if(docMtl.getPatientMtl()!=null){
			Tree editT = docMtl.getPatientMtl().getTree().get(docMtl.getIdt());
			log.debug(editT);
			if("pvariable".equals(editT.getTabName())){
				Tree noticeT = editT.getParentT().getParentT().getParentT().getParentT();
				if("notice".equals(noticeT.getTabName())){
					del(noticeT,docMtl);
				}else if("task".equals(noticeT.getTabName())){
					del(noticeT.getParentT(),docMtl);
				}
			}else if("day".equals(editT.getTabName())){
				int size = editT.getParentT().getChildTs().size();
				if(size>1){
					del(editT,docMtl);
				}else{
					delUpDoseMod(editT,docMtl);
				}
			}else if("dose".equals(editT.getTabName())){
				int size = editT.getParentT().getChildTs().size();
				if(size>1){
					del(editT,docMtl);
				}else{
					delUpDoseMod(editT,docMtl);
				}
			}
		}
		log.debug("------------END------");
	}
	private void del(Tree editNodeT,TreeManager docMtl) {
//		editNodeT.getParentT().getChildTs().remove(editNodeT);
//		removeNodeS.add(editNodeT);
//		editNodeT.getParentT().getChildTs().remove(editNodeT);
//		editNodeT.setParentT(null);
		docMtl.getRemoveNodeS().add(editNodeT);
//		em.remove(editNodeT);
	}
	private void delUpDoseMod(Tree editT, TreeManager docMtl) {
		Tree drugT = editT.getParentT();
		if(drugT.getMtlO() instanceof Drug){
			int size = drugT.getChildTs().size();
			if(size==1){
				Tree noticeT = drugT.getParentT().getParentT();
				del(noticeT,docMtl);
			}
		}
	}
	private void delNotlast(Tree editNodeT,TreeManager docMtl, String tabName) {
		boolean toDel = false;
		for(Tree t:editNodeT.getParentT().getChildTs())
			if(tabName.equals(t.getTabName())&&t!=editNodeT)
				toDel=true;
		if(toDel){
			del(editNodeT,docMtl);
		}else{
			editNodeT.setIdClass(null);
			if("times"==tabName)
				editNodeT.setRef(null);
		}
	}
	public void deletePatientEditNode(TreeManager docMtl) {
		log.debug("----------------");
		if(docMtl.getIdt()==null)	return;
		log.debug("----------------");
		Tree editNodeT = docMtl.getTree().get(docMtl.getIdt());
		log.debug("----------------");
		if(editNodeT==null)			return;
		log.debug("----------------"+editNodeT);
		if("task".equals(editNodeT.getTabName())){
			del(editNodeT,docMtl);
		}else if("protocol".equals(editNodeT.getTabName())){
			del(editNodeT,docMtl);
		}else if("notice".equals(editNodeT.getTabName())){
			del(editNodeT,docMtl);
		}
	}
	
	@Transactional
	public void deleteAccountConfirmation(String confirmKey)
	{
		em.createNamedQuery("DELETE FROM confirmaccount WHERE confirmkey=?")
		  .setParameter(1, confirmKey).executeUpdate();		
	}
}

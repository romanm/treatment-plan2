package com.qwit.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.App;
import com.qwit.domain.Dose;
import com.qwit.domain.Folder;
import com.qwit.domain.MObject;
import com.qwit.domain.Tree;
import com.qwit.model.DrugMtl;
import com.qwit.model.ExplorerMtl;
import com.qwit.model.InstituteMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.model.UserMtl;

@Service("copyPasteService")
@Repository
public class CopyPasteService extends MtlDbService {
	private final Log log = LogFactory.getLog(getClass());
	public void copy(SchemaMtl docMtl){
		log.debug("-----------"+docMtl.getIdc());
		log.debug("-----------"+docMtl.getIdt());
		copyFromDoc(docMtl);
		docMtl.setIdc(null);
		docMtl.setIdt(null);
		docMtl.setEditNodeT();
	}
	public void copyFromDoc(TreeManager docMtl){
		Integer idc = docMtl.getIdc();
		log.debug("-----------"+idc);
		if(idc!=null){
			Tree copyNodeT = docMtl.getTree().get(idc);
			log.debug("-----------"+copyNodeT);
//			OwsSessionContainer owsSession = OwsSessionContainer.getOwsSession();
			if(copyNodeT!=null){
				MObject copyObjectM = docMtl.getClassM().get(copyNodeT.getIdClass());
				//			log.debug("-----------"+copyObjectM);
				if(owsSession!=null){
					owsSession.setCopyNodeT(copyNodeT);
					owsSession.setCopyNodeDocMtl(docMtl);
					owsSession.setCopyNodeId(copyNodeT.getIdClass());
				}
			}else{
				if(docMtl instanceof UserMtl || docMtl instanceof DrugMtl){
					log.debug("idc="+idc);
					copyNodeT = getTreeById(docMtl, idc);
					log.debug("copyNodeT="+copyNodeT);
					owsSession.setCopyNodeT(copyNodeT);
//					idClass2copy=idc;
					docMtl.setIdClass2copy(idc);
					if(owsSession!=null){
						owsSession.setIdClass2copy(docMtl.getIdClass2copy());
//						owsSession.setIdClass2copy(idClass2copy);
						log.debug("docMtl.getIdClass2copy()="+docMtl.getIdClass2copy());
					}
				}else if(docMtl instanceof ExplorerMtl){
					ExplorerMtl eMtl= (ExplorerMtl)docMtl;
					Folder folderO = eMtl.getFolderO();
					log.debug(folderO);
					if(idc.equals(folderO.getId())){
						log.debug(folderO);
						owsSession.setCopyNodeDocMtl(docMtl);
					}else{
						//we are about to copy something else as the folderO in explorer
						
						//copy protocols
						log.debug("copyNodeId:" + docMtl.getIdc());
						owsSession.setCopyNodeId(docMtl.getIdc());
					}
				}
			}
		}
	}
	//pasteStationFolder
	@Transactional
	public void pasteStationFolder(InstituteMtl docMtl){
		log.debug("-----TODO-------");
		TreeManager copyNodeDocMtl = owsSession.getCopyNodeDocMtl();
		ExplorerMtl eMtl = (ExplorerMtl) copyNodeDocMtl;
		Folder folderO = eMtl.getFolderO();
		log.debug("-----TODO-------"+folderO);
		Folder patientProtocolFolderO = getPatientProtocolFolderO(folderO);
		log.debug("-----TODO------- "+patientProtocolFolderO);
		if(null!=patientProtocolFolderO){
			//find-insert new root folder patient|protocol
			Tree patientProtocolInstitutFolderT 
				= getPatientProtocolInstitutFolderT(docMtl, patientProtocolFolderO);
			if(null==patientProtocolInstitutFolderT)
			{
				patientProtocolInstitutFolderT = treeCreator.setTagName("folder").setTreeManager(docMtl)
						.setParentT(docMtl.getDocT()).addChild();
				patientProtocolInstitutFolderT.setMtlO(patientProtocolFolderO);
			}
			//delete old
			if(patientProtocolInstitutFolderT.hasChild())
				for(Tree t:patientProtocolInstitutFolderT.getChildTs())
					if(folderO.getId().equals(t.getIdClass()))
						continue;
					else
						docMtl.getRemoveNodeS().add(t);
			//insert new
			patientProtocolInstitutFolderT = treeCreator.setTagName("folder").setTreeManager(docMtl)
					.setParentT(patientProtocolInstitutFolderT).addChild();
			patientProtocolInstitutFolderT.setMtlO(folderO);
			deleteNative(docMtl);
		}
	}
	private Tree getPatientProtocolInstitutFolderT(InstituteMtl docMtl, Folder folderO) {
		for(Tree folderT:docMtl.getDocT().getChildTs())
			if(folderO.getId().equals(folderT.getIdClass())
			)
				return folderT;
		return null;
	}
	private Folder getPatientProtocolFolderO(Folder folderO) {
		log.debug(folderO);
		if("folder".equals(folderO.getFolder()))
			return null;
		if("patient".equals(folderO.getFolder()))
			return folderO;
		if("protocol".equals(folderO.getFolder()))
			return folderO;
		return getPatientProtocolFolderO(folderO.getParentF());
	}
	//END:pasteStationFolder

	public void paste(UserMtl docMtl){
		log.debug("getIdc()"+docMtl.getIdc()
				+":"+"Copy: "+docMtl.getCopyNodeT()
				+" getIdClass2copy()"+docMtl.getIdClass2copy());
		if(null!=docMtl.getIdClass2copy()){
		}
		pasteSourceTarget(docMtl);
	}
	public void paste(SchemaMtl docMtl){
		log.debug("-----------"+docMtl.getIdc());
		log.debug("-----------"+docMtl.getIdt());
		if(TreeManager.RIGHT_WriteYes>docMtl.getAccessRight())
			return;//no right to delete

		DrugMtl drugMtl = (DrugMtl)docMtl;
		paste(drugMtl);
	}
	public void paste(DrugMtl docMtl){
		log.debug("---1--- "+docMtl.getIdClass2copy());
		if(null!=docMtl.getIdClass2copy()){
			log.debug("---2---");
			pasteSourceTarget(docMtl);
		}else{
//			log.debug("---3--- "+docMtl.getCopyNodeT());
//			paste(getCopyNodeT());
			paste2parent(docMtl);
		}
		log.debug("---4---");
	}

	
	
	
	private void addCopyRef(Tree copyNodeT,TreeManager docMtl)
	{
		if(copyNodeT.getRef()!=null){
			TreeManager copyNodeDocMtl = owsSession.getCopyNodeDocMtl();
			Tree tree = copyNodeDocMtl.getTree().get(copyNodeT.getRef());
//			Tree tree = getCopyNodeDocMtl().getTree().get(copyNodeT.getRef());
			if(tree==null)
				return;
			Integer idNew = docMtl.getOld2newIdM().get(copyNodeT.getId());
			Tree treeNew = docMtl.getTree().get(idNew);
			if(tree.equals(copyNodeT.getDocT())){
				Integer id = docMtl.getDocT().getId();
				treeNew.setRef(id);
			}else if(treeNew.getDocT().getId().equals(copyNodeT.getDocT().getId())){
				treeNew.setRef(copyNodeT.getRef());
			}else{
				docMtl.addRef(copyNodeT);
			}
		}
		if(copyNodeT.getChildTs()!=null)
			for(Tree t:copyNodeT.getChildTs())
				addCopyRef(t,docMtl);
	}
	private void paste2parent(DrugMtl docMtl) {
//		private void paste(TreeManager docMtl) {
		Tree copyNodeT = owsSession.getCopyNodeT();
		log.debug("---------------");
		if(copyNodeT==null){
			log.info("it's not to copy");
			return;
		}
		log.debug("---------------");
		log.debug("Copy: "+copyNodeT);
		Tree parentT = docMtl.getTree().get(docMtl.getIdc());
		log.debug(" to: parentT := "+parentT);
		String prefixc = getPrefixc(docMtl);
		log.debug(" docMtl.getPrefixc() :"+prefixc);
		if(null==parentT){
			if(null!=prefixc&&("support".equals(prefixc)||"rqm".equals(prefixc))){
				parentT=initInsideTask(docMtl, "support");
			}else if(null!=prefixc&&"taskPremedication".equals(prefixc)){
				parentT=initInsideTask(docMtl, prefixc);
			}else{
				return;
			}
		}
		if("task".equals(parentT.getTabName())){
			if("drug".equals(copyNodeT.getTabName())){
				addPaste(parentT,docMtl);
				addCopyRef(copyNodeT,docMtl);
			}else if("finding".equals(copyNodeT.getTabName())){
				addPaste(parentT,docMtl);
			}else if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT,docMtl);
			}else if("task".equals(copyNodeT.getTabName())){
				log.debug("----");
				if(!"task".equals(parentT.getParentT().getTabName())
					&&!"task".equals(copyNodeT.getParentT().getTabName())){
					log.debug("----");
					docMtl.setAction("copyTaskAll");
				}
			}
		}else if("times".equals(parentT.getTabName())){
			if("times".equals(copyNodeT.getTabName())){
				parentT.setIdClass(copyNodeT.getIdClass());
				if(null==parentT.getMtlO()&&null!=copyNodeT.getMtlO()){
				}
			}
		}else if("labor".equals(parentT.getTabName())){
			if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT.getParentT(),docMtl);
			}
		}else if("notice".equals(parentT.getTabName())){
			if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT.getParentT(),docMtl);
			}
		}else if("day".equals(parentT.getTabName())){
			if(
			"notice".equals(copyNodeT.getTabName())
			||"expr".equals(copyNodeT.getTabName())
			){
				log.debug("--------------");
				addPaste(parentT,docMtl);
				log.debug("--------------");
			}else if("drug".equals(parentT.getParentT().getTabName())){
				if("day".equals(copyNodeT.getTabName())){
					addPaste(parentT.getParentT(),docMtl);
					addCopyRef(copyNodeT,docMtl);
				}
			}
//		}else if("expr".equals(parentT.getTabName())){
		}else if(docMtl.isExprO(parentT)){
			log.debug("--------------");
//			if("dose".equals(copyNodeT.getTabName())){
//				addPaste(parentT,docMtl);
//			}else 
				if("then".equals(docMtl.getExprO(parentT).getExpr())
					||"else".equals(docMtl.getExprO(parentT).getExpr()))
			{
				log.debug("--------------");
				if("day".equals(copyNodeT.getTabName())
				|| "app".equals(copyNodeT.getTabName())
				|| "dose".equals(copyNodeT.getTabName())
				){
					addPaste1T(copyNodeT, parentT, docMtl);
				}else if("drug".equals(copyNodeT.getTabName()))
				{
					Tree newDrugT = addPaste1T(copyNodeT, parentT, docMtl);
					for(Tree doseT:copyNodeT.getChildTs())
						if(docMtl.isDoseO(doseT))
							addPaste1T(doseT, newDrugT, docMtl);
				}
				//			}else if("day".equals(copyNodeT.getTabName())){
				//				if("then".equals(docMtl.getExprO(parentT).getExpr())
				//				||"else".equals(docMtl.getExprO(parentT).getExpr()))
				//				{
				//					addPaste1T(copyNodeT, parentT, docMtl);
				//				}
			}
		}else if("drug".equals(parentT.getTabName())){
			log.debug("--------------");
			if("drug".equals(copyNodeT.getTabName())){
				if("task".equals(parentT.getParentT().getTabName())
				&&"task".equals(copyNodeT.getParentT().getTabName())
				){
					addPaste(parentT.getParentT(),docMtl);
					addCopyRef(copyNodeT,docMtl);
				}else if("drug".equals(copyNodeT.getParentT().getTabName())){
					if("drug".equals(parentT.getParentT().getTabName())){
						addPaste(parentT.getParentT(),docMtl);
					}else{
						addPaste(parentT,docMtl);
					}
				}else if("drug".equals(parentT.getParentT().getTabName())
					&&"task".equals(copyNodeT.getParentT().getTabName())
					){
					log.debug("--------------");
					Tree drugT = addChild("drug", parentT.getParentT());
					drugT.setIdClass(copyNodeT.getIdClass());
					for(Tree s_doseT:copyNodeT.getChildTs())
						if("dose".equals(s_doseT.getTabName())){
							Tree doseT = addChild("dose", drugT);
							doseT.setIdClass(s_doseT.getIdClass());
						}
				}
			}else if("notice".equals(copyNodeT.getTabName())
					||"expr".equals(copyNodeT.getTabName())
					){
					log.debug("-----------------------");
					addPaste(parentT,docMtl);
				if(parentT.getId().equals(parentT.getDocT().getId()))//ParentT is drug document root element.
				{
					log.debug("-----------------------");
				}else if("dose".equals(copyNodeT.getParentT().getTabName())){
					for(Tree doseT:parentT.getChildTs())
						if("dose".equals(doseT.getTabName()))
							addPaste(doseT,docMtl);
				}else{
					log.debug("-----------------------");
					for(Tree t1:copyNodeT.getChildTs()){
						if("day".equals(t1.getTabName())){
							//						addPaste(parentT.getParentT());
							addPaste(parentT.getDocT(),docMtl);
							break;
						}
					}
				}
			}else if("day".equals(copyNodeT.getTabName())){
				
			}
		}else if(docMtl.isExprO(parentT)){
			if(docMtl.isExprO(copyNodeT)){
				log.debug("--------------");
			}
		}else if(parentT.getMtlO() instanceof App){
			if("expr".equals(copyNodeT.getTabName())
			||"notice".equals(copyNodeT.getTabName())){
				addPaste(parentT,docMtl);
			}
		}else if(parentT.getMtlO() instanceof Dose){
			if("dose".equals(copyNodeT.getTabName())){
				parentT.setIdClass(copyNodeT.getIdClass());
//				nextCurrentId();
			}else if("app".equals(copyNodeT.getTabName())){
				addPaste(parentT.getParentT(),docMtl);
			}else if("expr".equals(copyNodeT.getTabName())){
				addPaste(parentT,docMtl);
			}else if("notice".equals(copyNodeT.getTabName())){
				addPaste(parentT,docMtl);
			}
		}
	}
	public void pasteSourceTarget(TreeManager docMtl) {
		log.debug("getIdc()"+docMtl.getIdc()
				+":"+"Copy: "+docMtl.getCopyNodeT()
				+" getIdClass2copy()"+docMtl.getIdClass2copy());
		if(null!=docMtl.getIdClass2copy()){
			Tree t = docMtl.getTree().get(docMtl.getIdClass2copy());
			log.debug(t);
		}
//		OwsSessionContainer owsSession = OwsSessionContainer.getOwsSession();
		if(docMtl.getIdc()==null){
//		}else if(null!=getIdClass2copy()){
			
		}else if(docMtl.getIdc().equals(3)){
			TreeManager copyNodeDocMtl = owsSession.getCopyNodeDocMtl();
			if(copyNodeDocMtl instanceof ExplorerMtl){
				ExplorerMtl eMtl = (ExplorerMtl) copyNodeDocMtl;
				Folder folderO = eMtl.getFolderO();
				log.debug(folderO);
				log.debug(eMtl.getIdc());
				if(eMtl.getIdc()!=null && folderO!=null && eMtl.getIdc().equals(folderO.getId()))
				{
//					targetF=folderO;
					docMtl.setTargetF(folderO);
				}
			}
		}else if(docMtl.getIdc().equals(2)){
			owsSession.setSourceT(docMtl.getCopyNodeT());
			owsSession.setSourceIdClass(docMtl.getIdClass2copy());
		}else if(docMtl.getIdc().equals(1)){
			owsSession.setTargetT(docMtl.getCopyNodeT());
			owsSession.setTargetIdClass(docMtl.getIdClass2copy());
		}else if(docMtl instanceof UserMtl){
			log.debug("DEAD CODE");
//			if(docMtl.getCopyNodeT().getMtlO() instanceof Contactperson)
//			{
//				log.debug(2);
//				Tree instituteT = em.find(Tree.class, docMtl.getIdc());
//				log.debug(instituteT);
//				if("institute".equals(instituteT.getTabName()))
//				{
//					log.debug(3);
//					user2institute4admin(instituteT,docMtl.getCopyNodeT());
//					log.debug(4);
//				}
//				
//			}
//			log.debug(5);
		}else if(docMtl instanceof DrugMtl){
			paste2parent((DrugMtl)docMtl);
		}
	}
}

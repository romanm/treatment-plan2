package com.qwit.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.qwit.domain.Contactperson;
import com.qwit.domain.Tree;
import com.qwit.model.ConceptMtl;
import com.qwit.model.DrugMtl;
import com.qwit.model.ExplorerMtl;
import com.qwit.model.InstituteMtl;
import com.qwit.model.PatientMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;
import com.qwit.model.UserMtl;
import com.qwit.util.RegisterUserForm;

@Service("handleAction")
public class HandleAction extends MtlDbService {
	private final Log log = LogFactory.getLog(getClass());
	public String handleActionTubo(UserMtl docMtl,String a){
		//ha(docMtl,a);
		String ha = handleAction(docMtl,a);
		log.debug(ha);
		return ha;
	}
	public String handleAction(PatientMtl docMtl){
		String a=owsSession.getRequest().getParameter("a");
		log.debug(a);
		if(null==a)
			a=owsSession.getRequest().getParameter("action");
		log.debug(a);
		return ha(docMtl,a);
	}
	public String handleActionTubo(PatientMtl docMtl,String a){
//			String handleAction=null;
//			if(null!=a&&a.length()>0){
//				handleAction=a;
//				log.debug("a="+handleAction);
//			}else if(docMtl.hasAction()){
//				handleAction=docMtl.getAction();
//				log.debug("getAction()="+handleAction);
//			}else if(null!=docMtl.getIdt()){
//				handleAction=docMtl.getEditNodeName();
//				log.debug("getEditNodeName()="+handleAction);
//			}
//			log.debug("----------------------END-");
//			return handleAction;
			return ha(docMtl,a);
	}
	public String handleAction(InstituteMtl docMtl,String a){
		log.debug(a);
		log.debug("getIdc()"+docMtl.getIdc()
				+":"+"Copy: "+docMtl.getCopyNodeT()
				+" getIdClass2copy()"+docMtl.getIdClass2copy());
		String handleAction = ha(docMtl, a);
		if("paste".equals(handleAction)){
			log.debug("---------------");
			TreeManager copyNodeDocMtl = owsSession.getCopyNodeDocMtl();
			if(null!=docMtl.getIdc()&&docMtl.getIdc().equals(4)){
				if(copyNodeDocMtl instanceof ExplorerMtl){
					handleAction="pasteStationFolder";
				}
			}else if(null!=docMtl.getIdc()&&docMtl.getIdc().equals(3)){
				log.debug(copyNodeDocMtl);
				Tree copyNodeT = owsSession.getCopyNodeT();
				log.debug(copyNodeT);
				if(copyNodeT!=null&&"contactperson".equals(copyNodeT.getTabName())){
					handleAction="pasteUser";
					log.debug("-----------");
				}else if(copyNodeDocMtl instanceof ExplorerMtl){
					String userPart = owsSession.getUserPart();
					log.debug(userPart);
					if(docMtl instanceof UserMtl && "view3".equals(userPart))
						return handleAction;
					ExplorerMtl eMtl = (ExplorerMtl) copyNodeDocMtl;
					docMtl.setFolderO(eMtl.getFolderO());
					handleAction="pasteFolder";
				}else if("trade".equals(owsSession.getPrefixc())){
					handleAction="pasteTrade";
				}
			}else if(docMtl instanceof UserMtl){
				log.debug(1);
				if(docMtl.getCopyNodeT().getMtlO() instanceof Contactperson)
				{
					log.debug(2);
					Tree instituteT = em.find(Tree.class, docMtl.getIdc());
					log.debug(instituteT);
					if("institute".equals(instituteT.getTabName()))
					{
						handleAction="pasteUser2institute4admin";
					}
				}
			}
		}
		return handleAction;
	}
	private String ha(TreeManager docMtl, String a) {
		String handleAction=null;
		if(null!=a&&a.length()>0){
			handleAction=a;
		}else if(docMtl.hasAction()){
			handleAction=docMtl.getAction();
		}else if(null!=docMtl.getIdt()){
			handleAction=docMtl.getEditNodeName();
		}
		log.debug(handleAction);
		log.debug(a);
		return handleAction;
	}
	public String handleAction(ExplorerMtl docMtl){
		String ha = ha(docMtl);
		log.debug(ha);
		return ha;
	}
	public String handleAction(ConceptMtl docMtl){return ha(docMtl);}
	public String handleAction(DrugMtl docMtl){
		String haAndNew = haAndNew(docMtl);
		if(docMtl.isNoticeType(docMtl.getEditNodeT(), "totalsdose")){
			if("deleteNode".equals(haAndNew)){
			}else
			if("summeDoseExpr".equals(haAndNew)){
			}else
				haAndNew="totalsdoseNotice";
		}
		return haAndNew;
	}
	/**
	 * Identifies action for therapy regime editor.
	 * @param docMtl - Therapy regime work object.
	 * @return Action name.
	 */
	public String handleAction(SchemaMtl docMtl){
		log.debug("--------------------------------------------------");
		String haAndNew = haAndNew(docMtl);
		log.debug(haAndNew);
		Tree editNodeT = docMtl.getEditNodeT();
		log.debug(editNodeT);
		//		Tree idtT = em.find(Tree.class, docMtl.getIdt());
		/*
		if(null==editNodeT){
			Integer idt = docMtl.getIdt();
			log.debug(idt);
			log.debug(docMtl.getIdc());
			editNodeT = em.find(Tree.class, idt);
		}
		 * */
		if(null!=editNodeT){
//			Tree patientT = editNodeT.getDocT().getDocT();
//			log.debug(patientT);
			log.debug(docMtl.getPatientMtl());
			if(null!=docMtl.getPatientMtl()) {
				log.debug(editNodeT);
				if(docMtl.isThen(editNodeT)||docMtl.isElse(editNodeT)) {
					haAndNew="exprUse";
				}else if(docMtl.isTaskO(editNodeT) && "gcsf".equals(docMtl.getTaskO(editNodeT).getType())){
					haAndNew="addGcsf";
				}
			}else{
				log.debug("---------------");
			}
		}
		return haAndNew;
	}
	public String handleAction(RegisterUserForm ruf,String a){
		return a;
	}
	private String ha(TreeManager docMtl) {
		if(null!=docMtl.getAction())
			return docMtl.getAction();
		return "success";
	}
	private String haAndNew(TreeManager docMtl) {
		if(null==docMtl.getEditNodeT())
			docMtl.setEditNodeT();
		if(docMtl.hasAction())
			return docMtl.getAction();
		if(docMtl.isNewEdit())
			return "yes";
		return "success";
	}
}

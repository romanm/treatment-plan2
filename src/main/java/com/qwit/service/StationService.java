package com.qwit.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Contactperson;
import com.qwit.domain.Folder;
import com.qwit.domain.Institute;
import com.qwit.domain.Tree;
import com.qwit.model.InstituteMtl;

@Service("stationService")
@Repository
public class StationService extends MtlDbService {
	protected final Log log = LogFactory.getLog(getClass());

	public void newStationEntry(InstituteMtl docMtl){
		log.debug("a");
	}
	@Transactional
	public void trade2institute(InstituteMtl docMtl){
		log.debug("------------------BGN-");
		Tree tradeT = treeCreator.setTagName("drug").setTreeManager(docMtl)
		.setParentT(docMtl.getDocT()).addChild();
		log.debug(tradeT);
//		Integer tradeId = owsSession.getIdClass2copy();
		Integer tradeId = owsSession.getCopyNodeId();
		tradeT.setIdClass(tradeId);
		log.debug(tradeT);
		log.debug("------------------END-");
	}
	@Transactional
	public void stationFaxSave(InstituteMtl docMtl){
		log.debug("-------------------");
		Institute mtlO = (Institute) docMtl.getDocT().getMtlO();
		String station = mtlO.getInstitute().trim();
		mtlO.setInstitute(station);
		em.merge(mtlO);
	}

	public void newStationUserEntry(InstituteMtl docMtl){
		log.debug("c");
	}
	@Transactional
	public void newStationSave(InstituteMtl docMtl){
		Folder folderO = (Folder) em.createQuery("SELECT f FROM Folder f where f.folder='institute'").getSingleResult();
		log.debug("b 1 "+folderO);
		Integer idf = folderO.getId();
		log.debug("b 1 "+idf);
		Tree newStationT = saveNewDocRoot(idf, "institute");
		Institute newStationO = new Institute();
		String newStation = docMtl.getNewStation();
		newStationO.setInstitute(newStation);
		newStationO.setId(newStationT.getId());
		em.persist(newStationT);
		em.persist(newStationO);
		docMtl.setIdt(newStationT.getId());
		String instituteName = "i_"+newStationT.getId();
		Tree owuserT = addOwuser("institute", newStationT, instituteName);
		log.debug("b 2");
	}
	private StringBuffer getMd5(String toChapter1) {
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(toChapter1.getBytes());
			byte[] messageDigest = md.digest();
			for (int i=0;i<messageDigest.length;i++)
				hexString.append(Integer.toHexString(0xff & messageDigest[i]));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hexString;
	}

	@Transactional
	public void newStationUserSave(InstituteMtl docMtl){
		log.debug("d");
	}
	@Transactional
	public void newStationUserExist(InstituteMtl docMtl){
		log.debug("e");
		String idtRP = owsSession.getRequest().getParameter("idt");
		int idt = Integer.parseInt(idtRP);
		Contactperson cpO = em.find(Contactperson.class, idt);
		Tree cpT = addChild("contactperson", docMtl.getDocT());
		cpT.setIdClass(cpO.getId());
	}
}

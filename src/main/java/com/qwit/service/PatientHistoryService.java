package com.qwit.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.qwit.model.PatientMtl;

@Service("patientHistoryService")
@Repository
public class PatientHistoryService extends MtlDbService {
	protected final Log log = LogFactory.getLog(getClass());

	public void handleActionEntry(PatientMtl docMtl){
		docMtl.setIdc(getIntParam("idc"));
		if(docMtl.hasIdc()){
			docMtl.setIdt(null);
		}
		docMtl.setEditNodeT();
	}
	private Integer getIntParam(String param ) {
		String idcStr = owsSession.getRequest().getParameter(param);
		log.debug(param+" = "+idcStr);
		Integer idc = null;
		if(null!=idcStr && "" != idcStr){
			idc = Integer.parseInt(idcStr);
		}
		return idc;
	}
	public void editStep(PatientMtl docMtl){
		log.debug("---------");
	}
}

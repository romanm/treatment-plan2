package com.qwit.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.qwit.domain.Drug;
import com.qwit.model.DrugMtl;

@Component
public class DrugMtlValidator extends MtlValidator{
	protected final Log log = LogFactory.getLog(getClass());
	public void validateSaveNewTrade(DrugMtl docMtl,Errors errors){
		Drug drugO = checkDrug(docMtl.getTradeName(),"tradeName", errors);
		docMtl.setDrugO(drugO);
	}
}

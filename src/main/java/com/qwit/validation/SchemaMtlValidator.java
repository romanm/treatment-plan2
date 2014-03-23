package com.qwit.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.qwit.model.SchemaMtl;

@Component
public class SchemaMtlValidator extends MtlValidator{
	protected final Log log = LogFactory.getLog(getClass());
	public void validateSchemaBeginDate(SchemaMtl docMtl,Errors errors){
//		String cycleNumber="cycleNumber";
		String cycleNumber="cycleNr";
		String bdate="bdate";
		log.debug("-----------------------------------------"+cycleNumber);
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, cycleNumber, "error_empty_cycleNumber");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, cycleNumber, "error_empty_bdate");
	}
	public void validateSchemaxViewAndEdit(SchemaMtl docMtl,Errors errors){
		log.debug("----------------------------------------------------------------");
		String drugFieldName="editDrugC.drug";
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, drugFieldName, "error_empty_drugName");
	}

}

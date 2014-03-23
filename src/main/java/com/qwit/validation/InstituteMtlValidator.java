package com.qwit.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.qwit.model.InstituteMtl;

@Component
public class InstituteMtlValidator extends MtlValidator {
	protected final Log log = LogFactory.getLog(getClass());
	public void validateNewStationUser(InstituteMtl docMtl, Errors errors){
		if(owsSession.isRequestEvent("exist"))
			return;
		/*
		String fieldForename="newStationUserForename";
		String sql="SELECT * FROM contactperson where forename||contactperson||email ~* ?";
		String newStationUserForename = docMtl.getNewStationUserForename();
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, newStationUserForename);
		
		if(queryForList.size()>0){
			errors.rejectValue(fieldForename, "error_exist_User");
			docMtl.setQueryForList(queryForList);
		}
		 * */
	}
	
}

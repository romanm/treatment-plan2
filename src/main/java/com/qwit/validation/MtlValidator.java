package com.qwit.validation;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.qwit.domain.Drug;
import com.qwit.model.OwsSession;

public class MtlValidator {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired	protected SimpleJdbcTemplate simpleJdbc;
	@Autowired	protected OwsSession owsSession;
	protected EntityManager em;
	@PersistenceContext 
	public void setEntityManager(EntityManager em) {this.em = em;}
	public EntityManager getEntityManager() {return em;}

	protected Drug checkDrug(String drugName, String fieldName, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, "error_empty_drugName");
		Drug drugO = null;
		String sql=
		"SELECT d1.iddrug AS id,d2.drug AS generic,d1.idgeneric " +
		" FROM drug d1, drug d2 where upper(d1.drug)=upper(?) and d1.idgeneric=d2.iddrug";
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, drugName);
		if(queryForList.size()>0){
			log.debug("----");
			errors.rejectValue(fieldName, "error_exist_drugName");
			Map<String, Object> map = queryForList.get(0);
			drugO = new Drug();
//			String drugName = (String)map.get("drug");
			drugO.setDrug(drugName);
			drugO.setId((Integer)map.get("id"));
			String generic = (String)map.get("generic");
			Drug genericO = drugO;
			if(!drugName.equals(generic)){
				genericO = new Drug();
				genericO.setId((Integer)map.get("idgeneric"));
				genericO.setDrug(generic);
			}
			drugO.setGeneric(genericO);
			log.debug("----"+drugO);
		}
		return drugO;
	}
}

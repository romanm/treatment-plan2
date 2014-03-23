package com.qwit.validation;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.qwit.domain.Drug;
import com.qwit.domain.Tree;
import com.qwit.model.ConceptMtl;
import com.qwit.util.FlowObjCreator;

@Component
public class FlowObjCreatorValidator extends MtlValidator{

	protected final Log log = LogFactory.getLog(getClass());
	public void validateSaveNewProtocol(FlowObjCreator foc,Errors errors){
		log.debug("ddddddddddddddddddd "+owsSession.getRequest().getParameter("_eventId"));
		if("protocol".equals(foc.getFolder()))
			return;
		String fieldNewProtocol = "newProtocol.protocol";
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldNewProtocol, "error_empty_protocolName");
		String sql=" SELECT * FROM protocol where protocol=? AND protocolvar=?";
		String protocolName = foc.getNewProtocol().getProtocol();
		String protocolVar= foc.getNewProtocol().getProtocolvar();
		log.debug("----"+protocolName);
		log.debug("----"+protocolVar);
		log.debug("----"+sql.replaceFirst("\\?", protocolName).replaceFirst("\\?", protocolVar));
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql, protocolName,protocolVar);
		log.debug("----"+queryForList);
		Tree oldProtocolT=null;
		if(queryForList.size()>0){
			log.debug("----");
			errors.rejectValue(fieldNewProtocol, "error_exist_protocolName");
			Map<String, Object> map = queryForList.get(0);
			Integer idprotocol = (Integer) map.get("idprotocol");
			oldProtocolT = em.find(Tree.class, idprotocol);
			Tree oldProtocFolderT = oldProtocolT.getParentT();
			ConceptMtl conceptMtl = new ConceptMtl(oldProtocolT);
			conceptMtl.addClass(oldProtocolT,oldProtocolT.getId(), em);
			conceptMtl.addClass(oldProtocFolderT,oldProtocFolderT.getId(), em);
		}
		foc.setOldProtocolT(oldProtocolT);
	}
	public void validateSaveNewGeneric(FlowObjCreator foc,Errors errors){
		Drug drugO = checkDrug(foc.getGenericName(),"genericName", errors);
		foc.setDrugO(drugO);
	}
	public void validateSaveNewPatient(FlowObjCreator foc,Errors errors){
		log.debug("ddddddddddddddddddd "+owsSession.getRequest().getParameter("_eventId"));
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPatient.forename", "error_empty_forename");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPatient.patient", "error_empty_patientname");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPatient.sex", "error_empty_sex");
		DateTime dateTime = new DateTime();
		dateTime = dateTime.withYear(foc.getYear()).withMonthOfYear(foc.getMonth());
		int dayOfMonth = dateTime.plusMonths(1).withDayOfMonth(1).minusDays(1).getDayOfMonth();
		if(foc.getDay()>dayOfMonth){
			errors.rejectValue("day", "error_dayOfMonth");
		}else{
			dateTime=dateTime.withDayOfMonth(foc.getDay());
			Date bdate = dateTime.toDate();
			foc.setBdate(bdate);
		}
	}

}

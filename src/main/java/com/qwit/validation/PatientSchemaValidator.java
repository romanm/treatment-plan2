package com.qwit.validation;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.qwit.domain.Diagnose;
import com.qwit.model.PatientSchema;
import com.qwit.service.PatientSchemaService;

@Component
public class PatientSchemaValidator  extends MtlValidator{
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired @Qualifier("patientSchemaService")	PatientSchemaService	patientSchemaService;
	public void validatePatientDiagnose(PatientSchema ps,Errors errors){
		log.debug("--------------"+ps);
		String diagnose = ps.getDiagnose();
		log.debug("--------------"+diagnose);
		List<Diagnose> dzL = patientSchemaService.getDiagnoseL(diagnose);
		log.debug("--------------"+dzL);
		if(dzL.size()==0||dzL.size()>1){
			String sql="SELECT diagnose.* FROM diagnose,tree WHERE " +
					" id=iddiagnose and did!=418199 	AND did!=479962 AND" +
					" lower(diagnose) LIKE '%" +diagnose.toLowerCase() +"%'";
			List<Map<String, Object>> seekDzL = simpleJdbc.queryForList(sql);
			log.debug("--------------"+seekDzL);
			ps.setSeekDzL(seekDzL);
			if(seekDzL.size()>0){
				errors.rejectValue("diagnose", "error_seek_diagnose");
			}
		}
	}
	public void validateBsaDialog(PatientSchema ps,Errors errors){
	/*
		SchemaMtl docMtl = ps.getSchemaMtl();
		log.debug("--------------"+docMtl);
		PatientMtl patientMtl = docMtl.getPatientMtl();
		log.debug("--------------"+patientMtl);
		int weightKg = ps.getWeightKg();
		log.debug("--------------"+weightKg);
		patientMtl.setWeight( weightKg);
		int heightCm = ps.getHeightCm();
		log.debug("--------------"+heightCm);
		patientMtl.setHeight(heightCm);
		Float calc2Bsa = patientMtl.calc2Bsa();
		ps.setBsa(calc2Bsa);
		String bsaType = ps.getBsaType();
		log.debug("--------------"+bsaType);
		*/
		
		//empty tests
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "weightKg", "error_invalid_weightKg");
//		if(ps.getWeightKg()==0)
//			errors.rejectValue("weightKg", "error_invalid_weightKg");
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "heightCm", "error_invalid_heightCm");
//		if(ps.getHeightCm()==0)
//			errors.rejectValue("heightCm", "error_invalid_heightCm");
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "bsaType", "error_invalid_bsaType");
//				
//		//rufe die isBmiUnreal funktion auf vom patientMtl
//		//um die angekommenen Werte aus patientSchema zu validieren
//
//		log.debug("validateBsaDialog w:" + ps.getWeightKg());
//		log.debug("validateBsaDialog h:" + ps.getHeightCm());
//		log.debug("validateBsaDialog bmi:" + ps.getPatientMtl().getBmi(ps.getWeightKg(), ps.getHeightCm()) );
//		if(ps.getPatientMtl().isBmiUnreal(ps.getWeightKg(), ps.getHeightCm()) )
//			errors.rejectValue("weightKg", "error_invalid_bmi");
	}
}

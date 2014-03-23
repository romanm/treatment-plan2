package com.qwit.validation;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nl.captcha.Captcha;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.qwit.model.OwsSession;
import com.qwit.util.RegisterUserForm;

@Component
public class RegisterUserFormValidator{
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired	protected SimpleJdbcTemplate simpleJdbc;
	@Autowired	protected OwsSession owsSession;
	private final  static Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\.[a-z]+");
	
	public void validateRegister(RegisterUserForm docMtl, Errors errors)
	{
		log.debug("---------validating Register state-----------");
		//test the old password
		String newPassRetyped = docMtl.getPasswordRetyped();
		String newPass  = docMtl.getPassword();
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "error_invalid_firstName");
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "contactperson.forename", "error_invalid_firstName");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "secondName", "error_invalid_secondName");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "error_invalid_userName");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "error_invalid_password");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passwordRetyped", "error_invalid_password");
		
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList("SELECT * FROM users WHERE username=?", docMtl.getUserName());
		log.debug(queryForList);
		if(queryForList.size()>0)
			errors.rejectValue("userName", "error_invalid_userName");
		//test the newPassword1 and newPassword 2 should be the same
		if(!newPassRetyped.equals(newPass))
			errors.rejectValue("newPassRetyped", "error_invalid_retypedPassword");
	
		//check the length of the new password (6-20)
		if(newPass.length() < 6 || newPass.length() > 20 || newPass.contains(" "))
			errors.rejectValue("newPass", "error_invalid_password");
			
		if(!isEmail(docMtl.getEmail()))
			errors.rejectValue("email", "error_invalid_email");

		if(true)
			return;
		//---------------------CAPTCHA-------------------------------------------------------
		try
		{
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
										 .getRequestAttributes()).getRequest();
			Captcha captcha = (Captcha) request.getSession().getAttribute(Captcha.NAME);
			
			//debug captcha
//			Enumeration enames;
//			String out="";
//			enames = request.getSession().getAttributeNames();
//			while (enames.hasMoreElements()) 
//			{
//				String name = (String) enames.nextElement();
//				out += " " + request.getSession().getAttribute(name) +", ";
//			}
//			log.debug("----session attrs-------:"+ out);
//			log.debug("----session captcha name-------:"+ Captcha.NAME);
//			log.debug("----session captcha answer-------:"+ captcha);

			if (captcha.isCorrect(docMtl.getCaptchaAnswer()) )
			{
				//correct
				log.debug("---------captcha is correct ----------");
			}
			else
				errors.rejectValue("captchaAnswer", "error_invalid_captchaAnswer");
			
			request.getSession().removeAttribute(Captcha.NAME);
		}
		catch(Exception e)
		{
			errors.reject(e.getMessage());
		}
		
		log.debug("---------validating Register state-----------"+errors);
	}
	
	private boolean isEmail(String value)
	{
		return EMAIL_PATTERN.matcher(value).matches();
    }
	
	
	public void validateRemindPassword(RegisterUserForm ruf, Errors errors)
	{
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "error_invalid_userName");
		if(!ruf.getUserName().isEmpty())
		{
			List<Map<String, Object>> queryForList = simpleJdbc.queryForList("SELECT * FROM users WHERE username=?", ruf.getUserName());
			log.debug(queryForList);
			if(queryForList.size()<1)
				errors.rejectValue("userName", "error_invalid_userName");
		}
		
		//if ruf.email is an email?
		if(!isEmail(ruf.getEmail()))		
			errors.rejectValue("email", "error_invalid_email");

		//TODO
		// get the emal for the  ruf.username from db and check if it is equal with ruf.email 
		

	}

}

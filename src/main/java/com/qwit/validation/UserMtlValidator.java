package com.qwit.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.qwit.crypt.EncryptionService;
import com.qwit.model.OwsSession;
import com.qwit.model.UserMtl;

@Component
public class UserMtlValidator 
{
	protected final Log log = LogFactory.getLog(getClass());

	@Autowired private EncryptionService	encoder;
	private Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

	public void validateChangePassword(UserMtl docMtl, Errors errors)
	{
		log.debug("---------validating changePassword state-----------");
		//MessageContext mc = context.getMessageContext();
		
		//test the old password
		String newPassRetyped = docMtl.getNewPassRetyped();
		String newPass  = docMtl.getNewPass();
		//compare the hash of the actual session.user.pass with md5(oldPass)
		OwsSession.getOwsSession().getSecurityContext().getAuthentication().getCredentials();
		

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "oldPass", "error_invalid_password");
		//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPass", "error_invalid_password");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassRetyped", "error_invalid_password");
		
		
		//test the newPassword1 and newPassword 2 should be the same
		if(!newPassRetyped.equals(newPass))
			errors.rejectValue("newPassRetyped", "error_invalid_retypedPassword");
	
		//check the length of the new password (6-20)
		if(newPass.length() < 6 || newPass.length() > 20 || newPass.contains(" "))
			errors.rejectValue("newPass", "error_invalid_password");
		
		//check the old pass 
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();

		if ((docMtl.getOldPass() != null) && (docMtl.getOldPass().length() > 0) && 
				(authentication != null)) 
		{
			UserDetails principal = (UserDetails) authentication.getPrincipal();
//			String hashedPassword = encoder.encodePassword(docMtl.getOldPass());
//			String hashedPassword = passwordEncoder.encodePassword(docMtl.getOldPass(), null);
//			if (!hashedPassword.equals(principal.getPassword())) {
//				errors.rejectValue("oldPass","error_invalid_oldPassword");
//			}

			// CODE:01347 dieser abschnittfunktioniert, fügt password-salt support hinzu. dazu die obere if-zeile auskommentieren, die untere einkommentieren			
//			if(!encoder.isPasswordValid(principal.getPassword(), docMtl.getOldPass()))
			if(!encoder.isPasswordValid(principal.getPassword(), docMtl.getOldPass(), null))
			{
				errors.rejectValue("oldPass","error_invalid_oldPassword");
			}
		}

			
	}
	
//	public void validateChangePassword(UserMtl docMtl, ValidationContext context)
//	{
//		log.debug("---------validating changePassword state-----------");
//		MessageContext mc = context.getMessageContext(); 
//		String newP = docMtl.getNewPassword().trim();
//		if(newP.isEmpty())
//		{
//			mc.addMessage(new MessageBuilder().error().source("changePassword").
//				code("error_incorrect_newPassword").build());
//			
//		}
//		
//		//test the newPassword1 and newPassword 2 should be the same
//		
//		//test the old password
//			
//	}

}

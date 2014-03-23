package com.qwit.crypt;

import java.util.Date;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("encryptionService")
public class EncryptionService extends Md5PasswordEncoder
{

	//private Md5PasswordEncoder encoder = new Md5PasswordEncoder();
	private String passwordSalt 		= "Wkql!9B{Y7?";
	private String confirmAccountSalt 	= "Q*M(4Na%pw�";
	
	public EncryptionService()
	{
//		encoder.setIterations(10);
//		encoder.setEncodeHashAsBase64(true);

//		setIterations(10);
//		setEncodeHashAsBase64(true);
	}
	
	public String encodePassword(String passwordPlain)
	{
//		return encoder.encodePassword(passwordPlain, passwordSalt);
		return encodePassword(passwordPlain, passwordSalt);
	}
	
	public String encodeConfirmAccountHash(String confirmKey)
	{
//		return encoder.encodePassword(confirmKey, confirmAccountSalt);
		return encodePassword(confirmKey, confirmAccountSalt);
	}
	
	public String encodeConfirmAccountHash()
	{
		Date dt = new Date();
		String confirmKey 	= "" + dt;
//		return encoder.encodePassword(confirmKey, confirmAccountSalt);
		return encodePassword(confirmKey, confirmAccountSalt);
	}
	
	public boolean isPasswordValid(String encPass, String rawPass)
	{
//		return encoder.isPasswordValid(encPass,	rawPass, passwordSalt);
		return isPasswordValid(encPass,	rawPass, passwordSalt);
	}

	public boolean isAccountConfirmationValid(String encPass, String rawPass)
	{
//		return encoder.isPasswordValid(encPass,	rawPass, confirmAccountSalt);
		return isPasswordValid(encPass,	rawPass, confirmAccountSalt);
	}
}

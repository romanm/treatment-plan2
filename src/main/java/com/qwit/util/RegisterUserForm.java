package com.qwit.util;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Contactperson;
import com.qwit.domain.Owuser;

public class RegisterUserForm implements Serializable
{
	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	
	private String title;
	private String firstName;
	private String secondName;
	private String userName;
	private String email;
	private String password;
	private String passwordRetyped;
	private String captchaAnswer;
//	private String confirmKey;
//
//	public void setConfirmKey(String confirmKey) {this.confirmKey = confirmKey;	}
//	public String getConfirmKey() {	return confirmKey;	}

	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;}

	public void setFirstName(String firstName) {this.firstName = firstName;}
	public String getFirstName() {return firstName;}

	public void setSecondName(String secondName) {this.secondName = secondName;	}
	public String getSecondName() {	return secondName;}

	public void setUserName(String userName) {this.userName = userName;	}
	public String getUserName() {return userName;}

	public void setEmail(String email) {this.email = email;	}
	public String getEmail() {return email;	}

	public void setPassword(String password) {this.password = password;}
	public String getPassword() {return password;}

	public void setPasswordRetyped(String passwordRetyped) {this.passwordRetyped = passwordRetyped;	}
	public String getPasswordRetyped() {return passwordRetyped;	}

	public void setCaptchaAnswer(String captchaAnswer) {this.captchaAnswer = captchaAnswer;}
	public String getCaptchaAnswer() {return captchaAnswer;	}

	Contactperson contactperson ;
	Owuser owuser ;
	public Contactperson getContactperson() {return contactperson;}
	public Owuser getOwuser() {return owuser;}
	public void openNewUser(){
		contactperson = new Contactperson();
		owuser = new Owuser();
	}
}
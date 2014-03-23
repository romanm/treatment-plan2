package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;



/**
 * The persistent class for the users database table.
 * 
 */
@Entity
public class Users implements Serializable {
//	public class Users implements MObject,Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String username;
	private String password;
	private boolean enabled;
	
	public String toString()
	{
		return "username:"+username+":password:"+password+":enabled:"+enabled;
	}

    public Users() { }
	public String getUsername() {return username;}
	public void setUsername(String username) {this.username = username;	}
	public String getPassword() {return password;}
	public void setPassword(String password) {this.password = password;	}
	public boolean getEnabled() {return enabled;	}
	public void setEnabled(boolean enabled) {this.enabled = enabled;	}

	/*
	public int compareTo(MObject t) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(Integer id) {
		// TODO Auto-generated method stub
		
	}
 * */



}
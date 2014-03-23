package com.qwit.service;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.MObject;

public abstract class DbObjMCreator extends MtlDbEntityManager{
	protected final Log log = LogFactory.getLog(getClass());
	public int nextDbid() {
		int intValue = ((BigInteger) em.createNativeQuery("SELECT nextval('dbid')").getSingleResult()).intValue();
		return intValue;
	}
	//	@Transactional(readOnly = true)
	@Transactional
	public MObject build(){
		if(isNull())
			return null;
		MObject read = read();
		if(read==null){
			create();
			read = read();
		}
		return read;
	}
	public abstract boolean isNull();
	public abstract void create();
	public abstract MObject read();
}

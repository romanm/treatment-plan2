package com.qwit.service;

import java.util.List;

import com.qwit.domain.Day;
import com.qwit.domain.MObject;

public class DbDayCreator extends DbObjMCreator {

	private Day day;

	public static void main(String[] args) {
		String a = "a";
		System.out.println(a.matches("\\d"));
		a="1";
		System.out.println(a.matches("\\d"));
	}
	@Override
	public void create() {
		log.info("----- 1");
		if("w".equals(day.getNewtype()))
		{
			changeWeekType();
		}
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL().setParam(day.getAbs()).setParam(day.getNewtype());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}
	private void changeWeekType() {
		//wday=new Array('So','Mo','Di','Mi','Do','Fr','Sa');
		String abs = day.getAbs();
		log.debug(abs);
		day.setAbs(chageWeekType(abs));
		log.debug(abs);
	}
	public static String chageWeekType(String abs) {
			return abs
			.replace("So", "1")
			.replace("Mo", "2")
			.replace("Di", "3")
			.replace("Mi", "4")
			.replace("Do", "5")
			.replace("Fr", "6")
			.replace("Sa", "7");
	}

	public DbDayCreator() {
		setSql("SELECT d FROM Day d WHERE d.abs=:abs AND d.newtype=:newtype");
	}
	@Override
	public MObject read() {
		if(day.hasAbs())
			day.setAbs(day.getAbs().replaceAll(" ", ""));
		Day pvDb=null;
		List dL = em.createQuery(getSql())
		.setParameter("abs",		day.getAbs())
		.setParameter("newtype",	day.getNewtype())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=(Day) dL.get(0);
		}
		return pvDb;
	}

//	public DbObjMCreator setMtlO(Day day) {
	public DbDayCreator setMtlO(Day day) {
		this.day=day;
		return this;
	}

	@Override
	public boolean isNull() {
		if(!day.hasAbs() && !day.hasNewtype()) return true;
		return false;
	}

	public DbDayCreator setDayAbs(Integer md) {
		day.setAbs(md.toString());
		return this;
	}

	public DbObjMCreator setDayNewtype(String string) {
		day.setNewtype(string);
		return this;
	}
}

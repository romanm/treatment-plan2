package com.qwit.domain;

import java.sql.Timestamp;
import java.util.Calendar;

public class Ts extends Timestamp implements Cloneable{
	
	Timestamp tsEnd;
	/**
	 * Calculated calendar day, day change in 00:00. 
	 */
	int cday;
	int day, nr;
	private Ts beyond;
	private Ts beginTs;
	private String hhmm;
//	private Ts(long time) {super(time);}
	public Ts(long time, Tree timesT,int day) 
	{
		super(time);
		this.timesT=timesT;
		this.day=day;
		setTsEnd(new Timestamp(getTime()+1000));
		beyond=null;
	}
	
	Tree doseT;
	public Tree getDoseT() {
		if(null==doseT)
			for (Tree doseT : getTimesT().getParentT().getParentT().getChildTs()) 
				if(doseT.getMtlO() instanceof Dose || "dose".equals(doseT.getTabName()))
					this.doseT=doseT;
		return doseT;
	}

	Tree timesT;
	public Tree getTimesT()	{return timesT;}
	public Tree getTaskT()	{return timesT.getParentT().getParentT();}
	public int getNr()		{return nr;}
	public void setNr(int nr) {this.nr = nr;}
	public void setBeyond(Ts ts) {
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		ts.setTime(c.getTimeInMillis());
		this.beyond = ts;
	}
	public Ts getBeyond()	{return beyond;}
	public int getDay()		{return day; }
	public int getYearDayEnd()	{return getCalendarEnd().get(Calendar.DAY_OF_YEAR);}
	public int getYearDay()		{return getCalendar().get(Calendar.DAY_OF_YEAR);}
	public Integer getCday()	{return cday;}
	public void setCday(int cday){this.cday = cday;}
	public int getHourEnd()	{return getCalendarEnd().get(Calendar.HOUR_OF_DAY);}
	public Integer getHour()	{return getCalendar().get(Calendar.HOUR_OF_DAY);}

	private Calendar getCalendarEnd()	{return getCalendar(tsEnd);}
	private Calendar getCalendar()		{return getCalendar(this);}
	private Calendar getCalendar(Timestamp ts) {
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		return c;
	}
	
	public String getH14()	{return getH14(getMin());}
	public String getH14End()	{return getH14(getMinEnd());}
	private String getH14(int min) {
		String h14 = "";
//		if(min<=15)h14="¼";
//		else if(min<=30)h14="½";
//		else if(min<=45)h14="¾";
		if(min<=15)h14=".";
		else if(min<=30)h14="..";
		else if(min<=45)h14="...";
		return h14;
	}
	public int getMin()		{return getCalendar().get(Calendar.MINUTE);}
	public int getMinEnd()	{return getCalendarEnd().get(Calendar.MINUTE);}
	
	public Timestamp getTs()	{return (Timestamp)this;}
	public Timestamp getTsEnd()	{return tsEnd;}
	public Timestamp getTsBeyondEnd()	{
		Ts endTs = this;
		while (endTs.getBeyond()!=null) endTs=endTs.getBeyond();
		return endTs.getTsEnd();
	}
	public void setTsEnd(Timestamp tsEnd) { this.tsEnd = tsEnd; }
	public String toString(){
		return "nr:"+nr+":"+ super.toString()+":end:"+tsEnd+":day:"+day
		+":cday:"+cday
		/*
		+"/"+getTime()+"/"+getTsBeyondEnd().getTime()
		+(beyond!=null?"\n:beyond:"+beyond:"")
		 */
		+":hhmm:"+getHhmm()
		/*
		+" "+getTimesT()
		+" "+getTimesT().getParentT()
		 */
		+" "+getTimesT().getParentT().getParentT()
		;
	}
	private static final long serialVersionUID = 1L;
	public void addTime(int s) {
		setTime(getTime()+s);
		tsEnd.setTime(tsEnd.getTime()+s);
	}
	public void set24End() {
		Calendar c = getCalendar();
		c.set(Calendar.HOUR_OF_DAY,23);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		setTsEnd(new Timestamp(c.getTimeInMillis()));
	}
	public void set00() {
		Calendar c = getCalendar();
		c.add(Calendar.DATE, 1);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		setTime(c.getTimeInMillis());
		setCday(getCday()+1);
	}
	
	public void setBegin(Ts ts) {beginTs=ts;}
	public Ts getBegin() {return beginTs;}
	public void setHhmm(String hhmm) {this.hhmm=hhmm;}
	public String getHhmm() {return hhmm;}
	public final static int _1s = 1000;
	public final static int _1m = _1s*60;
	public final static int _1h = _1m*60;
	public final static int _1d = _1h*24;

}

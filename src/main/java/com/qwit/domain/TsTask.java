package com.qwit.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

public class TsTask extends Number implements Serializable,Comparable<TsTask>{

	private static final long serialVersionUID = 1L;
	Timestamp ts,tsEnd;
	TsTask beyond, beginTs;

	public TsTask(long timeInMS, Tree timesT, int day) {
		ts=new Timestamp(timeInMS);
		this.timesT=timesT;
		this.day=day;
		setTsEnd();
		beyond=null;
	}

	private int day;
	public int getDay() {return day;}
	private Tree timesT;
	public Tree getTimesT() {return timesT;}
	private int nr;
	private Tree appT;
	private int duration;
	private int cday;
	public int getNr() {return nr;}
	public void setNr(int nr) {this.nr = nr;}

	public int getDayInMillis() {
		Calendar c = getCalendar();
		int m = c.get(Calendar.HOUR_OF_DAY)*Ts._1h;
		m += c.get(Calendar.MINUTE)*Ts._1m;
		m += c.get(Calendar.SECOND)*Ts._1s;
		m += c.get(Calendar.MILLISECOND);
		return m;
	}
	private Calendar getCalendar()		{return getCalendar(ts);}
	private Calendar getCalendar(Timestamp ts) {
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		return c;
	}

	public long getTime() {return ts.getTime();} 
	public void setTime(long time) {ts.setTime(time);}
	
	public void setTsEnd() { 
		for (Tree appT : timesT.getParentT().getParentT().getChildTs())
			if(appT.getMtlO() instanceof App)
				this.appT=appT;
		duration = TsTask._1s;
		if(appT!=null){
			App appO=(App) appT.getMtlO();
			int hms = TsTask._1s;
			if(appO.getUnit().equals("h"))			hms=TsTask._1h;
			else if(appO.getUnit().equals("min"))	hms=TsTask._1m;
			Integer appValue = 1;
			if(appO.getAppapp()!=null)	appValue = appO.getAppapp();
			duration = appValue*hms;
		}
		MObject timesO = timesT.getMtlO();
		if(timesO!=null&&timesO instanceof Times&&TsTask.apporder_endBeforeBegin.equals(((Times)timesO).getApporder())){
			this.tsEnd = new Timestamp(ts.getTime());
			this.ts= new Timestamp(tsEnd.getTime()-duration);
		}else{
			this.tsEnd = new Timestamp(ts.getTime()+duration);
		}
	}
	public final static String apporder_afterEnd		= "0";
	public final static String apporder_beforeBegin		= "1";
	public final static String apporder_afterBegin		= "2";
	public final static String apporder_endBeforeBegin	= "3";
	public final static int _1s = 1000;
	public final static int _1m = _1s*60;
	public final static int _1h = _1m*60;
	public final static int _1d = _1h*24;
	public int compareTo(TsTask tst) {return (int) (longValue()-tst.longValue());}
	
	public String toString(){
		return "ts" +
				":nr:" +getNr() +
				"ts:"+ts+"(" +ts.getTime() +")"+
				":tsEnd:"+tsEnd+
				":day:"+day+
				":cday:"+cday
				;
	}
	
	@Override public double doubleValue() { return ts.getTime(); } 
	@Override public float floatValue() { return ts.getTime(); } 
	@Override public int intValue() { return (int) ts.getTime(); } 
	@Override public long longValue() { return ts.getTime(); }
	
	public void setCday(int cday) {this.cday=cday;}
	public int getCday() {return cday;}
	
}

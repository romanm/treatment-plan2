package com.qwit.model;

import java.io.Serializable;

import com.qwit.domain.TsTask;

public class WeekDrug extends Number implements Serializable,Comparable<WeekDrug>{
	private static final long serialVersionUID = 1L;
	public WeekDrug(TsTask ts) {
		this.ts=ts;
	}
	TsTask ts;
	public TsTask getTs() {return ts;}
	private Integer getNum()				{return ts.getDayInMillis();}
	public int compareTo(WeekDrug weekDrug)	{return intValue()-weekDrug.intValue();}

	private int number;
	public int getNumber()				{return number;}
	public void setNumber(int number)	{this.number = number;}

	@Override	public double doubleValue()	{return getNum();}
	@Override	public float floatValue()	{return getNum();}
	@Override	public int intValue()		{return getNum();}
	@Override	public long longValue()		{return getNum();}

	public static void main(String[] args) {
		System.out.println("-------------");
		Float f = new Float(05);
		System.out.println("-------------"+f);
		System.out.println("-------------"+f.intValue());
		System.out.println("-------------"+(f>f.intValue()) );
		System.out.println("-------------"+(f==f.intValue()) );
	}

}

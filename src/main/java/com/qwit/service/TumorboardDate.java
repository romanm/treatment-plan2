package com.qwit.service;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TumorboardDate {
	protected final Log log = LogFactory.getLog(getClass());
	private Calendar instance;
	public Calendar getInstance() {return instance;}
	public TumorboardDate(int yearOfcentury,int  monthOfyear,int dayOfmonth,int hourOfDay,int minuteOfhour){
		instance = Calendar.getInstance();
		instance.set(yearOfcentury, monthOfyear-1,dayOfmonth,hourOfDay,minuteOfhour);
		Calendar instance2 = Calendar.getInstance();
//		log.debug("-------------"+instance2.getTime());
//		log.debug("-------------"+instance2.get(Calendar.HOUR_OF_DAY));
//		log.debug("-------------"+instance.get(Calendar.HOUR_OF_DAY));
		if(instance2.get(Calendar.HOUR_OF_DAY)>instance.get(Calendar.HOUR_OF_DAY)){
			instance2.add(Calendar.DATE, 1);
			instance2.set(Calendar.HOUR_OF_DAY, 1);
			log.debug("-------------"+instance2.getTime());
		}
		int wd = instance.get(Calendar.DAY_OF_WEEK);
		int wd2 = instance2.get(Calendar.DAY_OF_WEEK);
		int add=wd+(wd2<=wd?-wd2:7-wd2);
		instance2.add(Calendar.DATE, add);
		instance2.set(Calendar.HOUR_OF_DAY,	instance.get(Calendar.HOUR_OF_DAY));
		instance2.set(Calendar.MINUTE,		instance.get(Calendar.MINUTE));
		instance=instance2;
	}
//	public static void main(String[] args) {
//		TumorboardDate tumorboardDate = new TumorboardDate();
//		tumorboardDate.init();
//	}
}

package com.qwit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Day;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.service.JXPathBean;

public class HourPlan implements Serializable{
	protected final Log log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	private SchemaMtl docMtl;

	public HourPlan(SchemaMtl schemaMtl) {
		this.docMtl=schemaMtl;
		initIntensivDayDrugM();
	}
	
	private Map<Integer,List<Tree>> intensivDayDrugM;
	private Map<Integer,Map<Tree,Map<Integer,String>>> intensivDayDrugHourM;
	public Map<Integer, List<Tree>> getIntensivDayDrugM() {
		return intensivDayDrugM;
	}
	public Map<Integer, Map<Tree, Map<Integer, String>>> getIntensivDayDrugHourM() {
		return intensivDayDrugHourM;
	}
	
	public void initIntensivDayDrugM() {
		intensivDayDrugM = new HashMap<Integer,List<Tree>>();
		intensivDayDrugHourM = new HashMap<Integer, Map<Tree,Map<Integer,String>>>();
		for(Tree daysintensiveT:docMtl.getDocT().getChildTs())
			if(docMtl.isPvPv("daysintensive", daysintensiveT))
				for(Tree dayT:daysintensiveT.getChildTs())
					if(docMtl.isDayO(dayT))
					{
						Day dayO = docMtl.getDayO(dayT);
						if(null==dayO.getAbs())
							continue;
						Matcher m_absPeriod = JXPathBean.getAbsPeriod().matcher(dayO.getAbs());
						if(m_absPeriod.matches())
						{
							int b = Integer.parseInt(m_absPeriod.group(1));
							int e = Integer.parseInt(m_absPeriod.group(2));
							for (int day = b; day <= e; day++)
								initDayM(day);
						}else if(dayO.getAbs().indexOf(",")>0){
							for(String abs1:dayO.getAbs().split(","))
								initDayM(new Integer(abs1));
						}else if(dayO.getAbs()!=null){
							initDayM(new Integer(dayO.getAbs()));
						}
					}
		for(Ts ts: docMtl.getPlan())
			if(intensivDayDrugHourM.containsKey(ts.getCday()))
			{
				Integer day = ts.getCday();
				Tree drugT = ts.getTimesT().getParentT().getParentT();
				Map<Integer, String> dayDrugM = initDayDrugM(day, drugT);
				Integer hour = ts.getHour();
				int hourEnd = ts.getHourEnd();
				String minuteDot4begin = addMinuteDot(ts.getMin());
				if(hourEnd>hour)
				{
//					dayDrugM.put(hour, minuteDot4begin+"|-");
					dayDrugM.put(hour, minuteDot4begin+"|⇒");
					for (int h = hour+1; h < hourEnd; h++)
						dayDrugM.put(h, "⇒");
//					dayDrugM.put(h, "-");
					dayDrugM.put(hourEnd, "|");
				}else{
					int minDuration = ts.getMinEnd()-ts.getMin();
					if(minDuration>1)
						dayDrugM.put(hour, minuteDot4begin+"|" + addMinuteDot(minDuration)+"|");
					else
						dayDrugM.put(hour, minuteDot4begin+"X");
				}
			}
	}
	private String addMinuteDot(int minDuration) {
		String minuteDot = "";
		if(minDuration>=45)
			minuteDot="...";
		else if(minDuration>=30)
			minuteDot="..";
		else if(minDuration>=15)
			minuteDot=".";
		return minuteDot;
	}
	private Map<Integer, String> initDayDrugM(Integer day, Tree drugT) {
		Map<Tree, Map<Integer, String>> dayX_DrugHourM = intensivDayDrugHourM.get(day);
		Map<Integer, String> dayDrugM1 = dayX_DrugHourM.get(drugT);
		if(null==dayDrugM1)
		{
			dayDrugM1 = new HashMap<Integer, String>();
			dayX_DrugHourM.put(drugT, dayDrugM1);
			intensivDayDrugM.get(day).add(drugT);
		}
		Map<Integer, String> dayDrugM = dayDrugM1;
		return dayDrugM;
	}
	private void initDayM(Integer day) {
		intensivDayDrugM.put(day, new ArrayList<Tree>());
		intensivDayDrugHourM.put(day, new HashMap<Tree,Map<Integer,String>>());
	}
}

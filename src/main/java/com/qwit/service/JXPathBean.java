package com.qwit.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.qwit.domain.Day;
import com.qwit.domain.Dose;
import com.qwit.domain.Folder;
import com.qwit.domain.Notice;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;
import com.qwit.model.PatientMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.TreeManager;

public class JXPathBean {
	protected final Log log = LogFactory.getLog(getClass());
	public static Integer week(Integer day){return day/7+(day%7>0?1:0);}
	private String hello="Hello World.";
	private JXPathContext jxpContext;
	public JXPathContext jxpc() {return jxpContext;}
	public String getHello() {return hello;}

	public void setFolder(Folder folder) {jxpContext = JXPathContext.newContext(folder);}
	public void setTreeManager(TreeManager treeManager) {
		jxpContext = JXPathContext.newContext(treeManager);
	}
	
	public JXPathContext jxp() {return jxpContext;}
//	public JXPathContext jxp(Tree t) {return JXPathContext.newContext(getJxp(), t);}
	public Pointer getPointer(String xp) {return jxpContext.getPointer(xp);}
	public JXPathBean jxp(Tree t) {
//		JXPathContext.newContext(getJxp(), t);
		jxpContext =JXPathContext.newContext(getJxp(), t);
		return this;
	}
	public Iterator jxpip(Tree t, String xPath) {return jxp(t).jxpc().iteratePointers(xPath);}
	public Iterator jxpip(String xPath) {return getJxp().iteratePointers(xPath);}

	public JXPathBean var(String varNameValue) {return declareVariable(varNameValue, varNameValue);}
	public JXPathBean var(String varName, Integer varValue) {return declareVariable(varName, varValue.toString());}
	public JXPathBean var(String varName, String varValue) {return declareVariable(varName, varValue);}
	
	public JXPathContext getJxp() {return jxpContext;}
	public JXPathContext getJxp(Tree t) {return JXPathContext.newContext(getJxp(), t);}
	public JXPathBean declareVariable(String varNameValue) {return declareVariable(varNameValue, varNameValue);}
	public JXPathBean declareVariable(String varName, String varValue) {
		jxpContext.getVariables().declareVariable(varName, varValue);
		return this;
	}

	public Tree patientDoseProcentT(Tree drugT, String dayNr){
		Iterator<Pointer> iteratePointers = getJxp().iteratePointers("patientMtl/docT/childTs");
		while (iteratePointers.hasNext()) {
			String xp = "childTs[ref='"+drugT.getDocT().getId() +"']/childTs[ref='"+drugT.getId() 
					+"']/childTs[mtlO/abs='"+dayNr+"']/childTs/childTs";
			JXPathContext treeXPath2 = getJxp((Tree) ((Pointer) iteratePointers.next()).getValue());
			Iterator<Pointer> iteratePointers2 = treeXPath2.iteratePointers(xp);
			if (iteratePointers2.hasNext()) {
				Pointer pointer2 = (Pointer) iteratePointers2.next();
				log.debug("----------"+pointer2.getValue());
				return (Tree) pointer2.getValue();
			}
		}
		return null;
	}
	/**
	 * JXPath iterator to child with one tabName.
	 * @param tree - Parent tree.
	 * @param tabName
	 * @return iterator child with one tabName.
	 */
	public Iterator<Pointer> childs(Tree tree, String tabName){
//		if(null==tree)
//			return null;
		JXPathContext treeXPath = getJxp(tree);
		String xp = "childTs[tabName='" +tabName +"']";
		Iterator<Pointer> iteratePointers = treeXPath.iteratePointers(xp);
		return iteratePointers;
	}
	
	public List childList(Tree tree, String tabName){
		JXPathContext treeXPath = getJxp(tree);
		String xp = "childTs[tabName='" +tabName +"']";
		List selectNodes = treeXPath.selectNodes(xp);
		return selectNodes;
	}
	
	public static Date getYMDate(Integer year, Integer month) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		return c.getTime();
	}

	public static Tree getApp(Tree drugT) {return getChild(drugT, "app");}
	public static Tree getDose(Tree drugT) {return getChild(drugT, "dose");}
	public static Tree getParentT(Tree tree, String tabName) {
		if(tabName.equals(tree.getTabName()))
			return tree;
		if(null==tree.getParentT())
			return null;
		return JXPathBean.getParentT(tree.getParentT(), tabName);
	}
	public static Tree getChild(Tree parentT, String tabName) {
		return getChild(parentT, tabName, 1);
	}
	/**
	 * @param parentT
	 * @param tabName
	 * @param n - number 0..n
	 * @return
	 */
	public static Tree getChild(Tree parentT, String tabName, Integer n){
		int i = 1;
		for(Tree t:parentT.getChildTs()){
			if(tabName.equals(t.getTabName()))
				if(i++==n)
					return t;
		}
		return null;
	}
	public Tree child(Tree parentT, String tabName, Integer n){
		if(parentT==null)
			return null;
		JXPathContext treeXPath = getJxp(parentT);
		return (Tree) treeXPath.getPointer("childTs[tabName='" +tabName +"'][" +n +"]").getValue();
	}
	public List<Tree> noticeNull(Tree parentT){
		ArrayList<Tree> arrayList = new ArrayList<Tree>();
		for(Tree t:parentT.getChildTs())
			if("notice".equals(t.getTabName()) && t.getMtlO()==null)
				arrayList.add(t);
		return arrayList;
	}
	public Map<String, List<Tree>> noticeByType(Tree parentT){
		Map<String, List<Tree>> treeMap = new TreeMap<String, List<Tree>>();
		Iterator<Pointer> childs = childs(parentT, "notice");
		while (childs.hasNext()) {
			Tree tree = (Tree) childs.next().getValue();
			if(tree.getMtlO()==null)
				continue;
			String type = ((Notice) tree.getMtlO()).getType();
			if(!treeMap.containsKey(type))
				treeMap.put(type, new ArrayList<Tree>());
			treeMap.get(type).add(tree);
		}
		return treeMap;
	}
//	public Set<String> noticeType(Tree parentT){
//		Set<String> treeSet = new TreeSet<String>();
//		Iterator<Pointer> childs = childs(parentT, "notice");
//		while (childs.hasNext()) {
//			Tree tree = (Tree) childs.next().getValue();
//			String type = ((Notice) tree.getMtlO()).getType();
//			if(!treeSet.contains(type))
//				treeSet.add(type);
//		}
//		return treeSet;
//	}

	public static Integer procent(SchemaMtl schemaMtl, Tree doseT) {
		Integer procent = 100;
		Dose doseC = (Dose) schemaMtl.getClassM().get(doseT.getIdClass());
		if(doseC==null) return procent;
//		Tree pvT = pDoseProcentT(schemaMtl, doseT.getParentT());
		Tree pvT = pDoseProcentT(schemaMtl, doseT.getParentT(),0);
//		Tree patientDoseProcentDoseT = JsfFunctions.pDoseProcentDoseT(pvT);
		Tree patientDoseProcentDoseT = pDoseProcentDoseT(pvT);
		if(patientDoseProcentDoseT!=null){
			Dose doseModC = (Dose) schemaMtl.getPatientMtl().getClassM().get(patientDoseProcentDoseT.getIdClass());
			if("pc".equals(doseModC.getType())){
				procent=(int) (doseModC.getValue()/schemaMtl.getCalcDose(doseT)*100);
			}else
			if(doseModC.getUnit().contains("/")){
				procent= (int) (doseModC.getValue()/doseC.getValue()*100);
			}else{
				procent=(int) (doseModC.getValue()/schemaMtl.getCalcDose().get(doseT)*100);
			}
		}else if(pvT!=null){
			Pvariable pvC = (Pvariable) schemaMtl.getPatientMtl().getClassM().get(pvT.getIdClass());
			if(pvC==null)
				procent= 100;
			else
				procent= Integer.parseInt(pvC.getPvalue());
		}
		return procent;
	}
	public static Tree pDoseProcentT(SchemaMtl schemaMtl, Tree drugT, Integer day) {
		Tree patientDoseProcentT=null;
		if(drugT!=null&&schemaMtl.getPatientMtl()!=null){
			for(Tree noticeT:schemaMtl.getPatientMtl().getDocT().getChildTs())
//				if("notice".equals(noticeT.getTabName()))
			{
				for(Tree taskT:noticeT.getChildTs())
					if(taskT.getMtlO() instanceof Task)
				{
					for(Tree pDrugT:taskT.getChildTs())
						if(drugT.getId().equals(pDrugT.getRef()))
					{
						for(Tree doseT:pDrugT.getChildTs())
							if(doseT.getMtlO() instanceof Dose)
						{
							for(Tree pvT:doseT.getChildTs()){
								patientDoseProcentT = pvT;
							}
						}else if(doseT.getMtlO() instanceof Day){
							Day dayO = (Day) doseT.getMtlO();
							String abs = dayO.getAbs();
							int parseInt = Integer.parseInt(abs);
							if(day.equals(parseInt))
							for(Tree dayDoseT:doseT.getChildTs())
								if(dayDoseT.getMtlO() instanceof Dose)
							{
								for(Tree pvT:dayDoseT.getChildTs()){
									patientDoseProcentT = pvT;
								}
							}
						}
					}
				}
			}
		}
		return patientDoseProcentT;
	}
	public static Tree pDoseProcentDoseT(SchemaMtl schemaMtl, Tree drugT) {
//		Tree pDoseProcentT = pDoseProcentT(schemaMtl, drugT);
		Tree pDoseProcentT = pDoseProcentT(schemaMtl, drugT,0);
		Tree pDoseProcentDoseT = pDoseProcentDoseT(pDoseProcentT);
		return pDoseProcentDoseT;
	}
	public static Tree pDoseProcentDoseT(Tree pPvT) {
		Tree patientDoseProcentDoseT=null;
		if(pPvT!=null && pPvT.getChildTs()!=null)
			for(Tree pPvDoseT:pPvT.getChildTs())
				patientDoseProcentDoseT = pPvDoseT;
		return patientDoseProcentDoseT;
	}
	public static Date weekDayDate(String wd) 
	{
		System.out.println("wd="+wd);
		if(!wd.matches("\\d"))
		{
			wd=DbDayCreator.chageWeekType(wd);
		}
		System.out.println("wd="+wd);
		int iwd = Integer.parseInt(wd);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_WEEK, iwd);
		return c.getTime();
	}
	public static Tree diagnoseT(PatientMtl patientMtl) 
	{
		Tree dsT=null;
		for(Tree diagnoseT:patientMtl.getDocT().getChildTs())
			if("diagnose".equals(diagnoseT.getTabName()))
			{
				dsT=diagnoseT;
				break;
			}
		return dsT;
	}
	public static String calcNotice(String notice,PatientMtl patientMtl) {
		if(notice==null);
		else if(patientMtl==null);
		else{
			int sex = patientMtl.getPatientC().getSex().equals("M")?1:0;
			if(notice.indexOf("Bloodpressure")>0){
				notice=calcAgeBloodPressure(notice,sex,patientMtl.getAgeYear());
				notice=removeColgroup(notice);
			}else if(notice.indexOf("roundPercent")>0){
				notice=roundPercent(notice,patientMtl.getBsa());
			}
		}
		return notice;
	}
	public static String calcAgeBloodPressure(String notice, int sex, Integer ageYear) {
		Matcher m = p.matcher(notice);
		
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			String s1 = "", g1 = m.group(1), g2 = m.group(2);
			if(g1.equals("sys")){
				if(g2.equals("10"))s1 += getSysBloodpressure(10, sex, ageYear);
				else if(g2.equals("90"))s1 += getSysBloodpressure(90, sex, ageYear);
			}else if(g1.equals("dia")){
				if(g2.equals("10"))s1 += getDiaBloodpressure(10, sex, ageYear);
				else if(g2.equals("90"))s1 += getDiaBloodpressure(90, sex, ageYear);
			}
			m.appendReplacement(sb, s1);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	private static int getSysBloodpressure(int percentile, int sex, int ageInt) {
		if(!(sex==1 || sex==0)) 
			throw new IllegalArgumentException("Sex must be either 0 (girl) or 1 (boy)");
		/*
		if(age < 2.0 || age >= 19.0) 
			throw new IllegalArgumentException("Age must be between 2.0 and 18.0");
		*/
		if (ageInt < 2) ageInt=2; else if (ageInt > 18) ageInt = 18; 
		if(!(percentile == 10 || percentile == 90))
			throw new IllegalArgumentException("Percentile must be either 10 or 90");
	
		if(percentile==10) 
			return (int)Math.round(percentiles[0][sex][0][ageInt]);
		return (int)Math.round(percentiles[1][sex][0][ageInt]);
	}
	static double[][][][] percentiles = new double[2][2][2][19];
	static {
		/* 1st value = percentile 0=10%, 1=90%
		 * 2nd value = sex, 0=girl, 1=boy
		 * 3rd value = 0=sys, 1=dias
		 * 4th value = age, from 2 till 18 (0 and 1 are left empty)	
		 */
		// girls, systolisch
		percentiles[0][0][0][2] = 81.12;
		percentiles[0][0][0][3] = 81.12;
		percentiles[0][0][0][4] = 81.12;
		percentiles[0][0][0][5] = 81.12;
		percentiles[0][0][0][6] = 82.23;
		percentiles[0][0][0][7] = 83.93;
		percentiles[0][0][0][8] = 86.51;
		percentiles[0][0][0][9] = 89.23;
		percentiles[0][0][0][10] = 92.17;
		percentiles[0][0][0][11] = 94.89;
		percentiles[0][0][0][12] = 97.61;
		percentiles[0][0][0][13] = 100.;
		percentiles[0][0][0][14] = 102.71;
		percentiles[0][0][0][15] = 104.83;
		percentiles[0][0][0][16] = 105.91;
		percentiles[0][0][0][17] = 106.45;
		percentiles[0][0][0][18] = 106.45;
		
		// girls diastolisch
		percentiles[0][0][1][2] = 50.; 
		percentiles[0][0][1][3] = 50.;
		percentiles[0][0][1][4] = 50.;
		percentiles[0][0][1][5] = 50.;
		percentiles[0][0][1][6] = 50.52;
		percentiles[0][0][1][7] = 51.05;
		percentiles[0][0][1][8] = 51.57;
		percentiles[0][0][1][9] = 52.63;
		percentiles[0][0][1][10] = 54.37;
		percentiles[0][0][1][11] = 57.37;
		percentiles[0][0][1][12] = 60.;
		percentiles[0][0][1][13] = 62.17;
		percentiles[0][0][1][14] = 64.13;
		percentiles[0][0][1][15] = 65.;
		percentiles[0][0][1][16] = 65.43;
		percentiles[0][0][1][17] = 65.43;
		percentiles[0][0][1][18] = 65.43;
		
		// boys, systolisch
		percentiles[0][1][0][2] = 81.11;
		percentiles[0][1][0][3] = 81.11;
		percentiles[0][1][0][4] = 81.11;
		percentiles[0][1][0][5] = 81.11;
		percentiles[0][1][0][6] = 82.22;
		percentiles[0][1][0][7] = 83.33;
		percentiles[0][1][0][8] = 85.55;
		percentiles[0][1][0][9] = 87.78;
		percentiles[0][1][0][10] = 90.;
		percentiles[0][1][0][11] = 93.26;
		percentiles[0][1][0][12] = 96.52;
		percentiles[0][1][0][13] = 99.24;
		percentiles[0][1][0][14] = 102.17;
		percentiles[0][1][0][15] = 104.35;
		percentiles[0][1][0][16] = 106.52;
		percentiles[0][1][0][17] = 108.69;
		percentiles[0][1][0][18] = 110.;
		
		// boys, diastolisch
		percentiles[0][1][1][2] = 50.;
		percentiles[0][1][1][3] = 50.;
		percentiles[0][1][1][4] = 50.;
		percentiles[0][1][1][5] = 50.;
		percentiles[0][1][1][6] = 50.52;
		percentiles[0][1][1][7] = 51.89;
		percentiles[0][1][1][8] = 54.21;
		percentiles[0][1][1][9] = 56.31;
		percentiles[0][1][1][10] = 58.42;
		percentiles[0][1][1][11] = 60.;
		percentiles[0][1][1][12] = 61.63;
		percentiles[0][1][1][13] = 63.26;
		percentiles[0][1][1][14] = 64.35;
		percentiles[0][1][1][15] = 64.89;
		percentiles[0][1][1][16] = 65.43;
		percentiles[0][1][1][17] = 65.98;
		percentiles[0][1][1][18] = 66.;
		
		// 90% percentile
		// girls systolisch
		percentiles[1][0][0][2] = 110.;
		percentiles[1][0][0][3] = 110.;
		percentiles[1][0][0][4] = 110.;
		percentiles[1][0][0][5] = 110.;
		percentiles[1][0][0][6] = 111.08;
		percentiles[1][0][0][7] = 113.26;
		percentiles[1][0][0][8] = 116.52;
		percentiles[1][0][0][9] = 121.66;
		percentiles[1][0][0][10] = 126.52;
		percentiles[1][0][0][11] = 130.;
		percentiles[1][0][0][12] = 132.72;
		percentiles[1][0][0][13] = 134.34;
		percentiles[1][0][0][14] = 135.97;
		percentiles[1][0][0][15] = 137.6;
		percentiles[1][0][0][16] = 138.15;
		percentiles[1][0][0][17] = 138.69;
		percentiles[1][0][0][18] = 139.;
		
		// girls diastolisch
		percentiles[1][0][1][2] = 77.06; 
		percentiles[1][0][1][3] = 77.39;
		percentiles[1][0][1][4] = 77.61;
		percentiles[1][0][1][5] = 77.71;
		percentiles[1][0][1][6] = 78.15;
		percentiles[1][0][1][7] = 78.8;
		percentiles[1][0][1][8] = 80.;
		percentiles[1][0][1][9] = 81.11;
		percentiles[1][0][1][10] = 82.22;
		percentiles[1][0][1][11] = 83.33;
		percentiles[1][0][1][12] = 84.44;
		percentiles[1][0][1][13] = 85.55;
		percentiles[1][0][1][14] = 86.66;
		percentiles[1][0][1][15] = 87.7;
		percentiles[1][0][1][16] = 88.8;
		percentiles[1][0][1][17] = 89.44;
		percentiles[1][0][1][18] = 90.;
		
		// boys, systolisch
		percentiles[1][1][0][2] = 109.2;
		percentiles[1][1][0][3] = 109.78;
		percentiles[1][1][0][4] = 109.78;
		percentiles[1][1][0][5] = 110.;
		percentiles[1][1][0][6] = 110.05;
		percentiles[1][1][0][7] = 111.63;
		percentiles[1][1][0][8] = 113.26;
		percentiles[1][1][0][9] = 115.43;
		percentiles[1][1][0][10] = 118.69;
		percentiles[1][1][0][11] = 123.26;
		percentiles[1][1][0][12] = 128.69;
		percentiles[1][1][0][13] = 133.8;
		percentiles[1][1][0][14] = 138.69;
		percentiles[1][1][0][15] = 142.17;
		percentiles[1][1][0][16] = 144.35;
		percentiles[1][1][0][17] = 145.43;
		percentiles[1][1][0][18] = 146.52;
		
		// boys, diastolisch
		percentiles[1][1][1][2] = 74.56;
		percentiles[1][1][1][3] = 74.89;
		percentiles[1][1][1][4] = 75.;
		percentiles[1][1][1][5] = 75.;
		percentiles[1][1][1][6] = 75.43;
		percentiles[1][1][1][7] = 75.98;
		percentiles[1][1][1][8] = 77.06;
		percentiles[1][1][1][9] = 78.15;
		percentiles[1][1][1][10] = 80.;
		percentiles[1][1][1][11] = 81.11;
		percentiles[1][1][1][12] = 82.77;
		percentiles[1][1][1][13] = 84.44;
		percentiles[1][1][1][14] = 86.11;
		percentiles[1][1][1][15] = 87.22;
		percentiles[1][1][1][16] = 87.77;
		percentiles[1][1][1][17] = 88.33;
		percentiles[1][1][1][18] = 88.88;
	}
	/** 
	 * @param percentile must be either 10 or 90
	 * @param age the age between 2.0 and 19.0
	 * @param sex sex must be 1 vor boys and 0 for girls
	 * @return for given age and sex the diastolic blood pressure of the 10% or 90% percentile
	 * @author Frank
	 */
	public static int getDiaBloodpressure(int percentile, int sex, double age) {
		if(!(sex==1 || sex==0)) 
			throw new IllegalArgumentException("Sex must be either 0 (girl) or 1 (boy)");
		/*
		if(age < 2.0 || age >= 19.0) 
			throw new IllegalArgumentException("Age must be between 2.0 and 18.0");
		*/
		if (age < 2.0) age=2.0; else if (age > 18.0) age = 18.0; 
		if(!(percentile == 10 || percentile == 90))
			throw new IllegalArgumentException("Percentile must be either 10 or 90");
		if(percentile==10) 
			return (int)Math.round(percentiles[0][sex][1][(int)Math.floor(age)]);
		else
			return (int)Math.round(percentiles[1][sex][1][(int)Math.floor(age)]);
	}
	private static String roundPercent(String notice, float bsa) {
		Matcher m = p1.matcher(notice);
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			String s1 = "", g1 = m.group(1), g2 = m.group(2);
			double k1 = bsa*Double.parseDouble(g1),
			k2 = Double.parseDouble(g2);
			if(k1==0)	return notice;
			Float roundPercent = (float)roundPercent(k1, k2);
			String k3 = float2string(roundPercent);
			m.appendReplacement(sb, k3);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	public static String float2string(Float value) {
		String attVal = "";
		if(value!=null){
			if(value>value.intValue())	attVal= value.toString();
			else attVal+=value.intValue();
		}
		return attVal;
	}
	public static double roundPercent(double d,double maxPercentError) {
		int s  = sround(d,maxPercentError);
		BigDecimal bd10 = new BigDecimal("1").movePointRight(s);
		BigDecimal bd = new BigDecimal(Double.toString(d));
		bd = bd.divide(bd10,BigDecimal.ROUND_HALF_EVEN).setScale(0,BigDecimal.ROUND_HALF_EVEN).multiply(bd10);
		return bd.doubleValue();
	}
	static Pattern
	p=Pattern.compile("\\{mtl.(sys|dia)Bloodpressure\\((10|90),\\$age,\\$sex\\)\\}"),
	p1=Pattern.compile("\\{return mtl.roundPercent\\((\\d+.?\\d*)\\*\\$surface,(\\d+)\\)\\}");
	/**
	 * Hilfroutine zu, @see roundPercent
	 * @param d
	 * @param maxPercentError
	 * @return
	 */
	private static int sround(double d,double maxPercentError) {
		final double maxError = maxPercentError *1.0001;
		
		// Tabelle wäre auch ok
		int s = (int) Math.log10(d);
		double x = Math.pow(10,s);
		// nach wahl:
		//if (x >= 100.0) x = 10.0;
		double dr = x * Math.rint(d / x);
		double e = Math.abs(100*(1-d/dr));
		// mehr als 1-2 schleifen passieren hier nicht
		while (e > maxError) {
			x = Math.pow(10,--s);
			dr = x * Math.rint(d / x);
			e = Math.abs(100*(1-d/dr));
		}
		return s;
	}
	private static String removeColgroup(String notice) {
		SAXReader saxReader = new SAXReader();
		Element ndE = DocumentHelper.createElement("div");
		try {
			notice="<div>"+notice+"</div>";
			Document nd = saxReader.read(new ByteArrayInputStream(notice.getBytes()));
			ndE=nd.getRootElement();
		} catch (DocumentException e) 
		{	
			e.printStackTrace();
		}
		List<Element> cgEll = ndE.selectNodes(".//colgroup");
		if(cgEll.size()>0){
			for (Element element : cgEll) 	element.getParent().remove(element);
			notice=ndE.asXML();
		}

		return notice;
	}
	public static Date dateAddDay(Date ts, Integer addDay){
		Calendar c = Calendar.getInstance();
		if(ts!=null){
			c.setTime(ts);
			c.add(Calendar.DATE, addDay);
		}
		return c.getTime();
	}
	public static Tree ruleState(Tree ifT,SchemaMtl schemaMtl) {
		Set<Integer> refSet = schemaMtl.getRef2idMS().get(ifT.getId());
		if(refSet!=null){
			Object[] array = refSet.toArray();
			for (int i = array.length-1; i >=0 ; i--) {
				Integer id=(Integer) array[i];
				Tree ruleStateT = schemaMtl.getTree().get(id);
				if(ruleStateT.getDocT()==schemaMtl.getDocT())
					if(ruleStateT.getChildTs()!=null)
						for (Tree t : ruleStateT.getChildTs())
							if(t.getRef()!=null)
								return t;
			}
		}
		return null;
	}
	public static String[] mealKey(String abs) {
		String abs2 = abs.replace("=", "");
		int nn = abs2.replace("0", "").length();
		String[] mealA=new String[nn];
		int i = abs2.indexOf("1");
		for (int j = 0; j < mealA.length; j++) {
			String m = 
				abs2.substring(0,i).replace("1", "0")
				+"1"
				+abs2.substring(i+1).replace("1", "0");
			String ml = "uiMeal"+m;
			mealA[j]=ml;
			i += abs2.substring(i+1).indexOf("1")+1;
		}
		return mealA;
	}
	static Pattern absPeriod = Pattern.compile("(-?\\d+)-(-?\\d+)");
	public static Boolean isPeriod(String abs) {return absPeriod.matcher(abs).matches();}
	public static Pattern getAbsPeriod() {return absPeriod;}
	public static Tree activeArm(Tree chooseT) {
		Tree pvT=null, armT=null;
		for(Tree t:chooseT.getChildTs()) if(t.getRef()!=null) pvT=t;
		if(pvT!=null) for(Tree t:chooseT.getChildTs()){
			if(t.getId().equals(pvT.getRef())) armT=t;
		}
		return armT;
	}
	public Calendar addDay2calendar(Calendar c, int day){
		Calendar cc = (Calendar) c.clone();
		cc.add(Calendar.DATE, day);
		return cc;
	}
}

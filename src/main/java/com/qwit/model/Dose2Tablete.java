package com.qwit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Expr;
import com.qwit.domain.Pvariable;
import com.qwit.domain.Tablet;
import com.qwit.domain.Task;
import com.qwit.domain.Times;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.service.JXPathBean;

public class Dose2Tablete implements Serializable
{
	private Dose2Tablete(){}
	private static final long serialVersionUID = 1L;
	protected final Log log = LogFactory.getLog(getClass());
	private SchemaMtl schemaMtl;
	public SchemaMtl getSchemaMtl() {return schemaMtl;}
	private Tree doseTabletT;
	private Tree drugT;
	Map<Tree,Map<Integer,List<Integer>>> drugMalTablet1MML;
	Map<Tree,Map<Integer,List<Integer>>> drugMalTablet2MML;
	Map<Tree,Map<Integer,List<Integer>>> drugMalTabletDbMML;
	public Map<Tree, Map<Integer, List<Integer>>> getDrugMalTablet1ML()		{return drugMalTablet1MML;}
	public Map<Tree, Map<Integer, List<Integer>>> getDrugMalTablet2ML()		{return drugMalTablet2MML;}
	public Map<Tree, Map<Integer, List<Integer>>> getDrugMalTabletDbML()	{return drugMalTabletDbMML;}
	public Map<Tree, Map<Integer, List<Integer>>> getDrugMalTabletML()		{
		if(division==1)
			return drugMalTablet1MML;
		if(drugMalTabletDbMML.size()>0)
			return drugMalTabletDbMML;
		return drugMalTablet1MML;
	}
	private Map<Tree, List<Tablet>> drugTableteM;
	public Map<Tree, List<Tablet>> getDrugTableteM() {return drugTableteM;}
	private Map<Tree, Tree> poMealTimesT;
	private Map<Tree, Tree> poTimesT;
	private Map<Tree, Ts> firstTsM;
	public Map<Tree, Ts> getFirstTsM() {return firstTsM;}
	public Map<Tree, Tree> getPoMealTimesT() {return poMealTimesT;}
	public Map<Tree, Tree> getPoTimesT() {return poTimesT;}

	public Dose2Tablete(SchemaMtl schemaMtl){
		this.schemaMtl=schemaMtl;
		division=1;
		drugMalTablet1MML=new HashMap<Tree, Map<Integer,List<Integer>>>();
		drugMalTablet2MML=new HashMap<Tree, Map<Integer,List<Integer>>>();
		drugMalTabletDbMML=new HashMap<Tree, Map<Integer,List<Integer>>>();
		drugTableteM=new HashMap<Tree, List<Tablet>>();
		firstTsM=new HashMap<Tree, Ts>();
		poMealTimesT=new HashMap<Tree, Tree>();
		poTimesT=new HashMap<Tree, Tree>();
		Tree idtT = schemaMtl.getTree().get(schemaMtl.getIdt());
		log.debug(idtT);
		if("expr".equals(idtT.getTabName())){
			doseTabletT=idtT;
			drugT=doseTabletT.getParentT().getParentT();
//			Tree dayT = schemaMtl.getDrugDay().get(drugT).get(0);
//			JXPathBean.getChild(drugT, "day", 0);
		}
		seekPo();
		log.debug(poTimesT);
		log.debug(firstTsM);
	}
	
	public Tree getDrugT() {return drugT;}
	
	public void	setMal1tablete1(Integer i)	{getMalTableteL(1).set(0, i);}
	public void	setMal1tablete2(Integer i)	{getMalTableteL(1).set(1, i);}
	public void	setMal1tablete3(Integer i)	{getMalTableteL(1).set(2, i);}
	public void	setMal2tablete1(Integer i)	{getMalTableteL(2).set(0, i);}
	public void	setMal2tablete2(Integer i)	{getMalTableteL(2).set(1, i);}
	public void	setMal2tablete3(Integer i)	{getMalTableteL(2).set(2, i);}
	public void	setMal3tablete1(Integer i)	{getMalTableteL(3).set(0, i);}
	public void	setMal3tablete2(Integer i)	{getMalTableteL(3).set(1, i);}
	public void	setMal3tablete3(Integer i)	{getMalTableteL(3).set(2, i);}
	public void	setMal4tablete1(Integer i)	{getMalTableteL(4).set(0, i);}
	public void	setMal4tablete2(Integer i)	{getMalTableteL(4).set(1, i);}
	public void	setMal4tablete3(Integer i)	{getMalTableteL(4).set(2, i);}
	public Integer	getMal1tablete1()	{return getMalTableteL(1).get(0);}
	public Integer	getMal1tablete2()	{return getMalTableteL(1).get(1);}
	public Integer	getMal1tablete3()	{return getMalTableteL(1).get(2);}
	public Integer	getMal2tablete1()	{return getMalTableteL(2).get(0);}
	public Integer	getMal2tablete2()	{return getMalTableteL(2).get(1);}
	public Integer	getMal2tablete3()	{return getMalTableteL(2).get(2);}
	public Integer	getMal3tablete1()	{return getMalTableteL(3).get(0);}
	public Integer	getMal3tablete2()	{return getMalTableteL(3).get(1);}
	public Integer	getMal3tablete3()	{return getMalTableteL(3).get(2);}
	public Integer	getMal4tablete1()	{return getMalTableteL(4).get(0);}
	public Integer	getMal4tablete2()	{return getMalTableteL(4).get(1);}
	public Integer	getMal4tablete3()	{return getMalTableteL(4).get(2);}
	
	private List<Integer> getMalTableteDbL(int mal) {
		return getDrugMalTabletML(drugT,drugMalTabletDbMML).get(mal);
	}
	private List<Integer> getMalTablete2L(int mal) {
		return getDrugMalTabletML(drugT,drugMalTablet2MML).get(mal);
	}
	private List<Integer> getMalTablete1L(int mal) {
		return getDrugMalTabletML(drugT,drugMalTablet1MML).get(mal);
	}
	private List<Integer> getMalTableteL(int mal) {
		if(division==1)
			return getMalTablete1L(mal);
		List<Integer> malTableteDbL = getMalTableteDbL(mal);
		if(malTableteDbL!=null)
			return malTableteDbL;
		List<Integer> malTablete1L = getMalTableteL(mal);
		return malTablete1L;
	}
	
	public void seekPo() {
		for(Tree t1:schemaMtl.getDocT().getChildTs())
			if(schemaMtl.classM(t1) instanceof Drug)
				seekPoTimes(t1);
			else if(schemaMtl.classM(t1) instanceof Task)
				for(Tree t2:t1.getChildTs())
					if(schemaMtl.classM(t2) instanceof Drug)
						seekPoTimes(t2);
		for(Ts ts : schemaMtl.getPlan())
			if(poTimesT.containsKey(ts.getTimesT()))
				if(!firstTsM.containsKey(ts.getTimesT().getParentT()))
					firstTsM.put(ts.getTimesT().getParentT(), ts);
	}
	private void seekPoTimes(Tree drugT) {
//		log.debug("--"+schemaMtl.getMObject(drugT));
//		Tree doseT = schemaMtl.getDrugDose().get(drugT);
		Tree doseT = JXPathBean.getDose(drugT);
		Dose doseC=(Dose) schemaMtl.classM(doseT);
//		log.debug("--"+doseC);
		if(doseC!=null && "p.o.".equals(doseC.getApp())){
			Tree dayT = JXPathBean.getChild(drugT, "day", 0);
			Tree timesT = JXPathBean.getChild(dayT, "times", 0);
//			Tree timesT=schemaMtl.getDayTime().get(schemaMtl.getDrugDay().get(drugT).get(0)).get(0);
//			log.debug("--"+timesT);
			poTimesT.put(timesT, timesT);
			Times timesC=(Times) schemaMtl.classM(timesT);
			int mal = 1;
			if(timesC!=null&&timesC.getAbs().contains("="))
			{
				poMealTimesT.put(timesT, timesT);
				mal=timesC.getAbs().replaceAll("=", "").replaceAll("0", "").length();
			}
			calcDose2tablete(drugT,mal);
		}
	}
	private void calcDose2tablete(Tree drugT, int mal) {
//		Tree doseT = schemaMtl.getDrugDose().get(drugT);
		Tree doseT = JXPathBean.getDose(drugT);
		initTableteL(drugT, doseT);
//		log.debug(mal);
//		log.debug(getDrugMalTabletDbML(drugT).size());
		for (int i = getDrugMalTabletDbML(drugT).size(); i < mal; i++) {
			List<Integer> tmL = new ArrayList<Integer>();
			for (int j = 0; j < getTableteL(drugT).size(); j++) tmL.add(0);
			getDrugMalTabletDbML(drugT).put(i+1, tmL);
		}
		int calcDose = schemaMtl.getCalcDose().get(doseT).intValue();
		int doseInTablet = calcMethod1(drugT, mal, calcDose);
//		log.debug(doseInTablet);
	}
	private int calcMethod1(Tree drugT, int mal, int calcDose) {
		calc1(calcDose,mal,getTableteL(drugT),getDrugMalTablet1ML(drugT));
		int doseInTablet = getDoseInTablet(calcDose, getTableteL(drugT), getDrugMalTablet1ML(drugT));
		log.debug(doseInTablet);
		if(doseInTablet!=0){
			seekTabletRest(calcDose,doseInTablet,getTableteL(drugT));
			addRest(getTableteL(drugT),getDrugMalTablet1ML(drugT));
		}
		return doseInTablet;
	}
	
	private void initTableteL(Tree drugT, Tree doseT) {
		for (Tree t1 : doseT.getChildTs()) 
			if(schemaMtl.classM(t1) instanceof Tablet)
				getTableteL(drugT).add((Tablet) schemaMtl.classM(t1));
			else if(schemaMtl.classM(t1) instanceof Expr){
				Expr doseInTabletC=(Expr) schemaMtl.classM(t1);
				log.debug("---------"+doseInTabletC);
				if("SteroidDayDose".equals(doseInTabletC.getValue())){
					Map<Integer, List<Integer>> drugMalTabletDbML = getDrugMalTabletDbML(drugT);
					for (Tree t2 : t1.getChildTs()){
						Pvariable mgProMalC=(Pvariable) schemaMtl.classM(t2);
						log.debug("---------"+mgProMalC);
						int mal = Integer.parseInt(mgProMalC.getUnit());
						log.debug("---------"+mal);
						List<Integer> tmL = new ArrayList<Integer>();
						for (Tree t3 : t2.getChildTs()){
							for (Tree t4 : t3.getChildTs()){
								Pvariable tabletNumC=(Pvariable) schemaMtl.classM(t4);
								log.debug("---------"+tabletNumC);
								int tMal = Integer.parseInt(tabletNumC.getPvalue());
								log.debug("---------"+tMal);
								tmL.add(tMal);
							}
						}
						for (int i = tmL.size(); i < getTableteL(drugT).size(); i++)
							tmL.add(0);
						log.debug(tmL);
						drugMalTabletDbML.put(mal, tmL);
						division=3;
					}
				}
			}
	}
	public Map<Integer, List<Integer>> getDrugMalTabletML(Tree drugT) {
		if(division==1)
			return getDrugMalTabletML(drugT, drugMalTablet1MML);
		if(drugMalTabletDbMML.size()>0)
			return getDrugMalTabletML(drugT, drugMalTabletDbMML);
		return getDrugMalTabletML(drugT, drugMalTablet1MML);
	}
	public Map<Integer, List<Integer>> getDrugMalTablet1ML(Tree drugT) {
		return getDrugMalTabletML(drugT, drugMalTablet1MML);
	}
	public Map<Integer, List<Integer>> getDrugMalTablet2ML(Tree drugT) {
		return getDrugMalTabletML(drugT, drugMalTablet2MML);
	}
	public Map<Integer, List<Integer>> getDrugMalTabletDbML(Tree drugT) {
		return getDrugMalTabletML(drugT, drugMalTabletDbMML);
	}
	private Map<Integer, List<Integer>> getDrugMalTabletML(Tree drugT,
			Map<Tree, Map<Integer, List<Integer>>> drugMalTablet2MML2) {
		Map<Integer, List<Integer>> drugMalTabletML = drugMalTablet2MML2.get(drugT);
		if(drugMalTabletML==null){
			drugMalTabletML=new HashMap<Integer, List<Integer>>();
			drugMalTablet2MML2.put(drugT, drugMalTabletML);
		}
		return drugMalTabletML;
	}

	public static void main(String[] args) {
		int calcDose = 2942;
		int mal = 2;
		Dose2Tablete d2t = new Dose2Tablete();
		List<Tablet> drugTableteL = new ArrayList<Tablet>();
		drugTableteL.add(new Tablet(500));
		drugTableteL.add(new Tablet(150));
		Map<Integer, List<Integer>> drugMalTabletML = new HashMap<Integer, List<Integer>>();
		d2t.calc1(calcDose,mal, drugTableteL,drugMalTabletML);
		int doseInTablet = d2t.getDoseInTablet(calcDose,drugTableteL,drugMalTabletML);
		float p = (calcDose-doseInTablet)/(float)calcDose;
		System.out.println(calcDose+":"+doseInTablet+":"+(calcDose-doseInTablet)+":"+p*100);
		if(doseInTablet!=calcDose){
			d2t.seekTabletRest(calcDose,doseInTablet,drugTableteL);
			d2t.addRest(drugTableteL, drugMalTabletML);
			doseInTablet=d2t.getDoseInTablet(calcDose,drugTableteL,drugMalTabletML);
		}
		p = (calcDose-doseInTablet)/(float)calcDose;
		System.out.println(calcDose+":"+doseInTablet+":"+(calcDose-doseInTablet)+":"+p*100);
	}
	int maxRoundProcent=5;
	private int restDose;
	int tabletMal = 0;
	int tabletPosition = 0;
	private int division;
	private void seekTabletRest(int calcDose, int doseInTablet, List<Tablet> drugTableteS) {
		restDose = Math.abs(calcDose-doseInTablet);
		log.debug(calcDose+" doseRound1="+doseInTablet+" rest "+restDose);
		for (int i = 0; i < drugTableteS.size(); i++) {
			Tablet tablet1C = drugTableteS.get(i);
			int tablet = tablet1C.getValue().intValue();
			if(tablet==0)
				continue;//TODO
			int tm1;
			int procent1 ;
			if(restDose>tablet){
				tm1 = restDose/tablet;
				int nd = doseInTablet+tm1*tablet;
				restTablet(tablet1C, tm1, calcDose,doseInTablet,i);
				nd = doseInTablet+(tm1+1)*tablet;
				restTablet(tablet1C, tm1+1, calcDose,doseInTablet,i);
			}else{
				tm1 = 1;
				restTablet(tablet1C, 1, calcDose,doseInTablet,i);
			}
		}
	}
	private void restTablet(Tablet tablet1C, int tm1, int calcDose, int doseTablet, int tp) {
		float r = doseTablet+(tm1*tablet1C.getValue())-calcDose;
		r=Math.abs(r);
		log.debug(r+"<"+restDose);
		if(r<restDose){
			restDose=(int) r;
			tabletMal=tm1;
			tabletPosition=tp;
			log.debug(tabletMal+" x "+tablet1C.getValue());
		}
	}
	
	public int getDoseInTablet(int calcDose, List<Tablet> drugTableteL, Map<Integer, List<Integer>> drugMalTabletML) {
		int dose = 0;
		int mal = 1;
		for (List<Integer> list : drugMalTabletML.values()) {
			String s = mal+" mal ";
			int malDose = 0;
			int tp = 0;
			for (Integer malTablet : list) {
				malDose+=malTablet*drugTableteL.get(tp).getValue().intValue();
				s+=", "+malTablet+" x "+drugTableteL.get(tp).getValue().intValue();
				tp++;
			}
			s+=" = "+malDose;
			log.debug(s);
			dose+=malDose;
			mal++;
		}
		return dose;
	}
	private void addRest(List<Tablet> tableteS, Map<Integer, List<Integer>> drugMalTabletML) {
		while (tabletMal>0)
			for (List<Integer> list : drugMalTabletML.values()) {
				list.set(tabletPosition, list.get(tabletPosition)+1);
				tabletMal--;
				if(tabletMal==0)break;
			}
	}
	private void calc1(int calcDose, int mal, List<Tablet> drugTableteS, Map<Integer, List<Integer>> drugMalTabletML) {
//		log.debug("tile "+calcDose+" "+mal+" mal");
		int malDose = calcDose/mal;
//		log.debug(malDose+" pro mal");
		for (Integer i = 1; i <= mal; i++) {
//			log.debug(i+" mal");
			List<Integer> tmL = new ArrayList<Integer>();
			int justDose = 0;
			int restDose = malDose;
			for (Tablet tablet : drugTableteS){
//				log.debug(tablet);
				int tabletVal = tablet.getValue().intValue();
//				log.debug(tabletVal);
				if(tabletVal==0)
					continue;//TODO
				int t1Mal = 0;
				if(restDose>tabletVal){
					t1Mal = restDose/tabletVal;
					justDose+=t1Mal*tabletVal;
					restDose=malDose-justDose;
				}
				tmL.add(t1Mal);
//				log.debug(t1Mal+" x "+tabletVal+" +"+restDose+" rest");
			}
			drugMalTabletML.put(i, tmL);
		}
//		log.debug(drugMalTabletML);
	}
	public List<Tablet> getTableteL() {return getTableteL(drugT);}
	public List<Tablet> getTableteL(Tree drugT) {
		List<Tablet> tableteL = drugTableteM.get(drugT);
		if(tableteL==null){
			tableteL= new ArrayList<Tablet>();
			drugTableteM.put(drugT, tableteL);
		}
		return tableteL;
	}
	public Tree getDoseTabletT() {return doseTabletT;}
	public void setDoseInTablete1(){
		log.debug("-----------");
		division=1;
		log.debug("-----------");
	}

	

}

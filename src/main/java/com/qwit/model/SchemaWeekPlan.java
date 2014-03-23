package com.qwit.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.qwit.domain.Tree;
import com.qwit.domain.Ts;

public class SchemaWeekPlan implements Serializable{

	private static final long serialVersionUID = 1L;
	private SchemaMtl docMtl;
	/**
	 * Set of appointment drug in day. 
	 */
	Map<Integer,Map<Integer,Set<Ts>>> drugDayAppS;
	public SchemaWeekPlan(SchemaMtl docMtl) {
		this.docMtl=docMtl;
	}

	public Map<Integer, Map<Integer, Set<Ts>>> getDrugDayAppS() {
		if(null==drugDayAppS) {
			init();
		}
		return drugDayAppS;
	}
	private void init() {
		drugDayAppS=new HashMap<Integer, Map<Integer,Set<Ts>>>();
		for (Ts ts : docMtl.getPlan()) {
			Tree drugT = ts.getTaskT();
			Integer cday = ts.getCday();
			Map<Integer, Set<Ts>> dayAppS = getDayAppS(drugT);
			addAppS(dayAppS,ts);
		}
	}
	private void addAppS(Map<Integer, Set<Ts>> dayAppS, Ts ts) {
		Set<Ts> set = dayAppS.get(ts.getCday());
		if(null==set) {
			set=new ConcurrentSkipListSet<Ts>();
			dayAppS.put(ts.getCday(), set);
		}
		set.add(ts);
	}
	private Map<Integer, Set<Ts>> getDayAppS(Tree drugT) {
		Map<Integer, Set<Ts>> map = drugDayAppS.get(drugT.getId());
		if(null==map) {
			map=new HashMap<Integer, Set<Ts>>();
			drugDayAppS.put(drugT.getId(), map);
		}
		return map;
	}

}

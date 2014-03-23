package com.qwit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.qwit.domain.Tree;
import com.qwit.service.JXPathBean;

public class HistoryYearManager implements Serializable{
	private static final long serialVersionUID = 1L;
	protected final Log log = LogFactory.getLog(getClass());

	public static String YMMdd="y-MM-dd";
	public String getPattern(){return YMMdd;}

	Map<String, List<Tree>> m;
	public Map<String, List<Tree>> getM() {return m;}
	public HistoryYearManager(PatientMtl docMtl) {
		m=new HashMap<String, List<Tree>>();
		DateTimeFormatter fmt = DateTimeFormat.forPattern(YMMdd);
		JXPathBean jxp = new JXPathBean();
		Iterator<Pointer> ofDatePI = jxp.jxpip(docMtl.getDocT(),"childTs/childTs[mtlO/pvariable='ofDate']");
		while (ofDatePI.hasNext()) {
			Tree ofDateT = (Tree) ofDatePI.next().getValue();
			DateTime ofDateDT = ofDateT.getHistory().getMdateDT();
			String YMMddKey = fmt.print(ofDateDT);
			List ofDateDayTL;
			if(m.containsKey(YMMddKey)){
				ofDateDayTL=m.get(YMMddKey);
			}else{
				ofDateDayTL=new ArrayList<Tree>();
				m.put(YMMddKey, ofDateDayTL);
			}
			ofDateDayTL.add(ofDateT);
		}
	}

}

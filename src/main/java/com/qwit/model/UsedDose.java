package com.qwit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.MObject;
import com.qwit.domain.Tree;

public class UsedDose implements Serializable{
	private static final long serialVersionUID = 1L;
	protected final Log log = LogFactory.getLog(getClass());
	private List<String> unitKeyL;
//	private int doseListDrugId;
	private TreeManager docMtl;
	
	public UsedDose(TreeManager schemaMtl) {
		this.docMtl=schemaMtl;
		doseListDrugId=0;
	}
	public List<String> getUnitKeyL()	{return unitKeyL;}
	Map<String, List<String>>	unitMapAppL;
	public Map<String, List<String>>	getUnitMapAppL() {return unitMapAppL;}
	private Map<String,Map<String,List<Integer>>>	unitAppIdclassL = null;
	public Map<String, Map<String, List<Integer>>>	getUnitAppIdclassL() {return unitAppIdclassL;}

	public List<String> appList;
	
	public List<String> getAppList() {return appList;}
	public Set<String> getUnitKeySet(){
		if(unitAppIdclassL != null){
			Set<String> s = unitAppIdclassL.keySet();
			return s;
		}
		return null;
	}

	private Integer	doseListDrugId;
	private List<Map<String, Object>>	drugDoseList, ddrugDoseList, drugDrugList;
	public List<Map<String, Object>>	getDdrugDoseList(){return ddrugDoseList;}
	public List<Map<String, Object>>	getDrugDrugList(){return drugDrugList;}
	public List<Map<String, Object>>	getDrugDoseList(){return drugDoseList;}

	public void readDrugDose(SimpleJdbcTemplate simpleJdbc, DrugMtl docMtl){
		Tree drugT = docMtl.getEditNodeT();
		if("dose".equals(drugT.getTabName())){
			drugT=drugT.getParentT();
			if("expr".equals(drugT.getTabName()))
				while (!"drug".equals(drugT.getTabName())) 
					drugT=drugT.getParentT();
		}
		if(!"drug".equals(drugT.getTabName()))return;
		Drug genericC=(Drug) docMtl.getClassM().get(drugT.getIdClass());
		if(genericC==null)return;//new drug
		genericC=genericC.getGeneric();
		readDrugDose(simpleJdbc, genericC.getId());
	}
	public  void readDrugDose(SimpleJdbcTemplate simpleJdbc, Integer idDrug){
		if(idDrug==null || idDrug<SchemaMtl._5000)return;
		Tree editDoseT = docMtl.getEditNodeT();
		if(null==editDoseT)
			return;
		log.debug(editDoseT);
		Tree editDrugT = editDoseT.getParentT();
		log.debug(editDrugT.getParentT());
		if("task".equals(editDrugT.getParentT().getTabName())){
			initDrugDose(simpleJdbc, idDrug);
			String sqlDDrugDose = "SELECT * FROM drug d1,dose d2, ( " +
					" SELECT d3.iddrug,count(d3.iddrug) cntddrug,d4.iddose,count(d4.iddose) cntddose " +
					" FROM tree t1,tree t2,tree t3, drug d3,tree t4, dose d4 " +
					" WHERE t1.id=t2.did AND t1.idclass=? AND t2.idclass=? " +
					" AND t1.id=t3.did AND t3.idclass=d3.iddrug " +
					" AND t3.id=t4.did AND t4.idclass=d4.iddose " +
					" GROUP BY d3.iddrug ,d4.iddose " +
					" ORDER BY cntddrug DESC ,cntddose DESC " +
					" ) dd " +
					" WHERE d1.iddrug=dd.iddrug AND d2.iddose=dd.iddose";
			ddrugDoseList = simpleJdbc.queryForList(sqlDDrugDose, idDrug,editDoseT.getIdClass());
		}else if("drug".equals(editDrugT.getParentT().getTabName())){
			initDrugDose(simpleJdbc, idDrug);
		}
	}
	private void initDrugDose(SimpleJdbcTemplate simpleJdbc, Integer idDrug) {
		String sqlDrugDose = " SELECT d.* FROM dose d, (SELECT iddose,count(iddose) as cnt FROM tree t1,drug,tree t2,dose WHERE "+
				" t1.idclass=iddrug AND t2.idclass=iddose AND t1.id=t2.did and iddrug=? GROUP BY iddose ORDER BY cnt DESC) c "+
				" WHERE d.iddose=c.iddose AND d.app!='' AND d.value!=0 " +
				" ORDER BY d.app DESC,d.unit DESC, d.value DESC ";
		log.debug(sqlDrugDose+" -- "+idDrug);
		drugDoseList = simpleJdbc.queryForList(sqlDrugDose, idDrug);
	}
	public  void readDrugDose_old(SimpleJdbcTemplate simpleJdbc, Integer idDrug){
		long l = System.currentTimeMillis();
		if(idDrug==null || idDrug<SchemaMtl._5000)return;
		if(doseListDrugId!=idDrug){
			doseListDrugId=idDrug;
			String sql = " SELECT d.* FROM dose d, (SELECT iddose,count(iddose) as cnt FROM tree t1,drug,tree t2,dose WHERE "+
			" t1.idclass=iddrug AND t2.idclass=iddose AND t1.id=t2.did and iddrug=? GROUP BY iddose ORDER BY cnt DESC) c "+
			" WHERE d.iddose=c.iddose ORDER BY d.app DESC,d.unit DESC, d.value DESC ";
			log.debug(sql+" -- "+idDrug);
			log.debug(docMtl.getEditNodeT());
			log.debug(docMtl.getEditNodeT().getParentT().getParentT());
			if("task".equals(docMtl.getEditNodeT().getParentT().getParentT().getTabName())){
				log.debug(1);
			}
			drugDoseList = simpleJdbc.queryForList(sql, idDrug);
			makeAppUnitValueL(drugDoseList);
			sql=" SELECT * FROM drug d, "+
			" (SELECT t2.idclass, count(t2.idclass) AS cnt FROM tree t1,tree t2,drug WHERE t1.idclass=? "+
			" AND t1.id=t2.did AND t2.idclass=iddrug GROUP BY t2.idclass) c WHERE c.idclass=d.iddrug ORDER BY c.cnt DESC ";
			drugDrugList = simpleJdbc.queryForList(sql, idDrug);
			for (Map rowMap : drugDrugList){
				Integer iddrug = (Integer) rowMap.get("iddrug");
				if(!docMtl.getClassM().containsKey(iddrug)){
					Drug drugC = new Drug();
					drugC.setId(iddrug);
					drugC.setDrug((String) rowMap.get("drug"));
					drugC.setType((String) rowMap.get("type"));
				}
			}
			MObject drugC = docMtl.getClassM().get(idDrug);
			log.info("-------" +drugC +"---" +idDrug +"----"+drugDoseList.size()+"/"+(System.currentTimeMillis()-l));
		}
	}
	private void makeAppUnitValueL(List<Map<String, Object>> drugDoseList2) {
		unitKeyL			= new ArrayList<String>();
		unitMapAppL			= new HashMap<String,List<String>>();
		unitAppIdclassL		= new HashMap<String, Map<String,List<Integer>>>();
		appList				= new ArrayList<String>();
		for (Map rowMap : drugDoseList){
			//***** make unitMapAppL ****
			//log.debug("--------------"+drugDoseList2);
			
			String app = (String) rowMap.get("app");
			if(app.length()==0)continue;
			
			if(!appList.contains(app))
				appList.add(app);
			
			String unit = (String) rowMap.get("unit");
			
			List<String> appL	= unitMapAppL.get(unit);
			if(appL==null){
				unitKeyL.add(unit);	
				appL=new ArrayList<String>();
				unitMapAppL.put(unit, appL);
			}
			if(!appL.contains(app)) appL.add(app);
			
			Map<String, List<Integer>> unitAppKeyMap = unitAppIdclassL.get(unit);
			if(unitAppKeyMap==null){
				unitAppKeyMap=new HashMap<String, List<Integer>>();
				unitAppIdclassL.put(unit, unitAppKeyMap);
			}
			List<Integer> valueL2 = unitAppKeyMap.get(app);
			if(valueL2==null){
				valueL2=new ArrayList<Integer>();
				unitAppKeyMap.put(app, valueL2);
			}
			valueL2.add(convertDose(rowMap));
		}
	}
	private Integer convertDose(Map<?, ?> rowMap) {
		Integer iddose = (Integer) rowMap.get("iddose");
		if(!docMtl.getClassM().containsKey(iddose)){
			Dose doseC = new Dose();
			doseC.setId(iddose);
			doseC.setValue((Float) rowMap.get("value"));
			doseC.setUnit((String) rowMap.get("unit"));
			doseC.setApp((String) rowMap.get("app"));
			doseC.setPro((String) rowMap.get("pro"));
			docMtl.getClassM().put(iddose, doseC);
		}
		return iddose;
	}
}

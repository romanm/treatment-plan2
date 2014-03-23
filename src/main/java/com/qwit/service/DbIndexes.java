package com.qwit.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

public class DbIndexes extends DbUpdate{
	List<String> mtlTreeL;
	public List<String> getMtlTreeL() {return mtlTreeL;}
	public void setMtlTreeL(List<String> mtlTreeL) {this.mtlTreeL = mtlTreeL;}
	
	@Transactional
	public void updateIndexes() {
		if(getMtlTreeL().size()>0){
			String ifNotDbObjOriginal = getIfNotDbObj();
			List<String> sqlL3 = new ArrayList<String>();
			sqlL3.addAll(getSqlL());
			for (String mtlTable : getMtlTreeL()) {
				if(ifNotDbObjOriginal!=null)
					setIfNotDbObj(ifNotDbObjOriginal.replaceAll("\\?", mtlTable));
				getSqlL().clear();
				for (String sql : sqlL3)
					getSqlL().add(sql.replaceAll("\\?", mtlTable));
				update("structure");
				Timestamp dbUpdateDate2 = getDbUpdateDate();
				dbUpdateDate2.setTime(dbUpdateDate2.getTime()+1000);
				setDbUpdateDate(dbUpdateDate2);
			}
		}
	}

}

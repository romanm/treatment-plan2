package com.qwit.service;

import java.util.List;

import com.qwit.domain.Drug;
import com.qwit.domain.Folder;
import com.qwit.domain.MObject;

public class DbDrugCreator  extends DbObjMCreator {

	private Drug drug;

	public DbDrugCreator(){
		setSql("SELECT d FROM Drug d WHERE d.drug=:drug");
	}
	@Override
	public void create() {
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		Folder folder = drug.getFolder();
		if(!drug.getId().equals(drug.getGeneric().getId())){
			Drug generic = (Drug)read(drug.getGeneric().getDrug());
			if(generic==null){
				create(drug.getGeneric(),folder,sqls);
				generic = (Drug)read(drug.getGeneric().getDrug());
				drug.setGeneric(generic);
			}
			create(drug,folder,sqls);
		}else{
			create(drug,folder,sqls);
		}
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	private void create(Drug drug, Folder folder, StringBuffer sqls) {
		if(folder==null){
		}else if(drug.getGeneric().getId().equals(drug.getId())){
			getDbUpdate().setParamL()
			.setParam(drug.getDrug());
			if("chemo".equals(folder.getFolder())){
				String sql0 = getDbUpdate().getSqlL().get(2);
				getDbUpdate().update1sql(sqls, sql0);
			}else if("support".equals(folder.getFolder())){
				String sql1=getDbUpdate().getSqlL().get(3);
				getDbUpdate().update1sql(sqls, sql1);
			}
		}else {//trade
			getDbUpdate().setParamL()
			.setParam(drug.getGeneric().getId().toString())
			.setParam(drug.getDrug())
			.setParam(drug.getGeneric().getId().toString());
			if("chemo".equals(folder.getFolder())){
				String sql0 = getDbUpdate().getSqlL().get(0);
				getDbUpdate().update1sql(sqls, sql0);
			}else if("support".equals(folder.getFolder())){
				String sql1=getDbUpdate().getSqlL().get(1);
				getDbUpdate().update1sql(sqls, sql1);
			}
		}
	}
	@Override
	public MObject read() {return read(drug.getDrug());}
	private MObject read(String drug2) {
		Drug pvDb=null;
		List<Drug> dL = em.createQuery(getSql())
		.setParameter("drug",		drug2)
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}

	@Override
	public boolean isNull() {return !drug.hasDrug()||!drug.hasGeneric();}

	public DbObjMCreator setMtlO(Drug drug) {
		this.drug=drug;
		return this;
	}

}

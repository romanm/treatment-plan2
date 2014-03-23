package com.qwit.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class DbUpdateContainer {
	protected final Log log = LogFactory.getLog(getClass());
//	static Pattern sql2sql = Pattern.compile("select*insert");
	@Autowired	private MtlDbService			schemaService;
	List<DbUpdate> dbUpdateL;
	private SimpleJdbcTemplate simpleJdbc;
	public void setSimpleJdbc(SimpleJdbcTemplate simpleJdbc) {this.simpleJdbc = simpleJdbc;}
	public List<DbUpdate> getDbUpdateL() {return dbUpdateL;}
	public void setDbUpdateL(List<DbUpdate> dbUpdateL) {this.dbUpdateL = dbUpdateL;}
	public void dbupdate() {
		String sql = 
		" SELECT version FROM oncows_db_version " +
		" WHERE type='structure' ORDER BY version DESC ";
		log.debug(sql);
//		if(true)	return;
		Map<String, Object> map = simpleJdbc.queryForList(sql).get(0);
		Timestamp lastUpdate = (Timestamp) map.get("version");
		log.debug(lastUpdate);
		setFlushModeCommit();
		for (DbUpdate dbUpdate1 : getDbUpdateL()) {
			if(dbUpdate1.getDbUpdateDate().after(lastUpdate)){
				log.debug(dbUpdate1.getDbUpdateDate());
				if(dbUpdate1 instanceof DbIndexes){
					DbIndexes dbUpdate2 = (DbIndexes) dbUpdate1;
					dbUpdate2.updateIndexes();
				}else
					dbUpdate1.update("structure");
		}	}
	}

	@Transactional
	public void setFlushModeCommit() {
		EntityManager em = schemaService.getEntityManager();
		em.setFlushMode(FlushModeType.COMMIT);
	}

}

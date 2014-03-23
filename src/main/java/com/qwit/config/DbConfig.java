package com.qwit.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.service.MtlDbService;

public class DbConfig extends MtlDbService {
	protected final Log log = LogFactory.getLog(getClass());

	Integer idUserProtocolFolder;
	Integer idUserPatientFolder;

	public Integer getIdUserPatientFolder() {
		return idUserPatientFolder;
	}

	public Integer getIdUserProtocolFolder() {
		return idUserProtocolFolder;
	}

	public void setUserProtocol(String userProtocol) {
		idUserProtocolFolder = getFolderId(userProtocol);
	}

	public void setUserPatient(String userPatient) {
		idUserPatientFolder = getFolderId(userPatient);
	}

	private Integer getFolderId(String folderName) {
		String sql = "SELECT idfolder FROM folder WHERE folder=? ";
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql,
				folderName);
		if (queryForList.size() > 0) {
			Map<String, Object> map = queryForList.get(0);
			Integer idFolder = (Integer) map.get("idfolder");
			return idFolder;
		}
		return null;
	}
}

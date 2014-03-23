package com.qwit.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.qwit.domain.Labor;
import com.qwit.service.DbLaborCreator;

public class MtlObjectePool {
	protected final Log log = LogFactory.getLog(getClass());
	private Map<String,Map<String,Map<String,String>>> buildMap;
	@Autowired @Qualifier("laborCreator") DbLaborCreator laborCreator;

	public void setBuildMap(Map<String, Map<String, Map<String, String>>> buildMap) {
		this.buildMap = buildMap;
	}

	public Labor buildLaborO(String objectKey) {
		log.debug("------------");
		Map<String, String> laborAttributenMap = buildMap.get("labor").get(objectKey);
		log.debug("------------"+laborAttributenMap);
		Labor laborO = new Labor();
		laborO.setLabor(laborAttributenMap.get("labor"));
		laborO.setUnit(laborAttributenMap.get("unit"));
		log.debug("------------"+laborO);
		Labor buildLaborO = (Labor)laborCreator.setMtlO(laborO).build();
		log.debug("------------"+buildLaborO);
		return buildLaborO;
	}

}

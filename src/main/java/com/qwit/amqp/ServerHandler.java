package com.qwit.amqp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qwit.model.ConceptMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.service.DocumentService;

@Component
public class ServerHandler {
	@Autowired
	DocumentService ds;

	protected final Log log = LogFactory.getLog(getClass());

	// public MtlDocResponce handleMessage(MtlDocRequest mtlDocRq){
	// log.debug(mtlDocRq);
	// MtlDocResponce mtlDocRs = new MtlDocResponce();
	// mtlDocRs.setIdDoc(mtlDocRq.getIdDoc()+1);
	// log.debug(mtlDocRs);
	// return mtlDocRs;
	// }
	public SchemaMtl handleMessage(MtlDocRequest mtlDocRq) {
		log.debug(mtlDocRq);
		SchemaMtl s = ds.makeSendableSchemaMtl(1286378);
		// SchemaMtl s = new SchemaMtl(null);
		log.debug(s);
		return s;
	}

	public ConceptMtl handleMessage(ProtocolRequest request) {
		log.debug(request);
		ConceptMtl response = ds.makeSendableConceptMtl(request.getIdDoc());
		// SchemaMtl resp = new SchemaMtl(null);
		log.debug(response);
		return response;
	}
}

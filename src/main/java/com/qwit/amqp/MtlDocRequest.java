package com.qwit.amqp;

import java.io.Serializable;

public class MtlDocRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	Integer idDoc;

	public Integer getIdDoc() {
		return idDoc;
	}

	public void setIdDoc(Integer idDoc) {
		this.idDoc = idDoc;
	}

	@Override
	public String toString() {
		return "idDoc=" + idDoc;
	}
}

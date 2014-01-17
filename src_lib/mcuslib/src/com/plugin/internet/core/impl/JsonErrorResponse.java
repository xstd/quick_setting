package com.plugin.internet.core.impl;

import com.plugin.internet.core.ResponseBase;
import com.plugin.internet.core.json.JsonCreator;
import com.plugin.internet.core.json.JsonProperty;

public class JsonErrorResponse extends ResponseBase {

    public static final int UnknownHostException = 10001;

	public int errorCode;

	public String errorMsg;

	@JsonCreator
	public JsonErrorResponse(@JsonProperty("code") int code,
			@JsonProperty("data") String data) {
		this.errorCode = code;
		this.errorMsg = data;
	}
}

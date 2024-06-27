package com.inconsistency.dto;

public class AnalyserResponseDTO {

	private Boolean success;
	private String clientId;
	private String error;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}


}

package com.plugin.services.dto;

public class InconsistenciesResponseDTO {

	private Boolean success;
	private InconsistenciesResponse data;
	private String error;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public InconsistenciesResponse getData() {
		return data;
	}

	public void setData(InconsistenciesResponse data) {
		this.data = data;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}

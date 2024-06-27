package com.inconsistency.dto;

public class InconsistencyErrorDTO {
	
	String clientId;
	String inconsistencyTypeCode;
	String inconsistencyTypeDesc;
	int severity;
	String severityLabel;
	String diagram;
	String propertyType;
	String propertyName;
	String umlPackage;
	String description;
	String cr;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String transactionId) {
		this.clientId = transactionId;
	}

	public String getInconsistencyTypeCode() {
		return inconsistencyTypeCode;
	}

	public void setInconsistencyTypeCode(String inconsistencyTypeCode) {
		this.inconsistencyTypeCode = inconsistencyTypeCode;
	}
	
	public String getInconsistencyTypeDesc() {
		return inconsistencyTypeDesc;
	}

	public void setInconsistencyTypeDesc(String inconsistencyTypeLabel) {
		this.inconsistencyTypeDesc = inconsistencyTypeLabel;
	}
	
	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public String getSeverityLabel() {
		return severityLabel;
	}

	public void setSeverityLabel(String severityLabel) {
		this.severityLabel = severityLabel;
	}

	public String getDiagram() {
		return diagram;
	}

	public void setDiagram(String diagram) {
		this.diagram = diagram;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getUmlPackage() {
		return umlPackage;
	}

	public void setUmlPackage(String umlPackage) {
		this.umlPackage = umlPackage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCr() {
		return cr;
	}

	public void setCr(String cr) {
		this.cr = cr;
	}
}

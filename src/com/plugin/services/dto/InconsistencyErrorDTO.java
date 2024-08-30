package com.plugin.services.dto;

public class InconsistencyErrorDTO {

	private String clientId;

	private String inconsistencyTypeCode;
	private String inconsistencyTypeDesc;

	private String cr;

	private int severity;
	private String severityLabel;

	private double concentration;
	private String concentrationStr;

	private String description;

	private String elId;

	private String parentId;

//	private UMLModelDTO model;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
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

	public void setInconsistencyTypeDesc(String inconsistencyTypeDesc) {
		this.inconsistencyTypeDesc = inconsistencyTypeDesc;
	}

	public String getCr() {
		return cr;
	}

	public void setCr(String cr) {
		this.cr = cr;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity.getValue();
		this.severityLabel = severity.name();
	}

	public String getSeverityLabel() {
		return severityLabel;
	}

	public void setSeverityLabel(String severityLabel) {
		this.severityLabel = severityLabel;
	}

	public double getConcentration() {
		return concentration;
	}

	public void setConcentration(double concentration) {
		this.concentration = concentration;
		this.setConcentrationStr(String.format("%.2f", concentration));
	}

	public void calculateConcentration() {
		if (concentration > 0.7) {
			setSeverity(Severity.HIGH);
		} else if (concentration > 0.3 && concentration <= 0.7) {
			setSeverity(Severity.MEDIUM);
		} else {
			setSeverity(Severity.LOW);
		}
	}

	public String getConcentrationStr() {
		return concentrationStr;
	}

	public void setConcentrationStr(String concentrationStr) {
		this.concentrationStr = concentrationStr;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getElId() {
		return elId;
	}

	public void setElId(String elId) {
		this.elId = elId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String diagramId) {
		this.parentId = diagramId;
	}

//	public UMLModelDTO getModel() {
//		return model;
//	}
//
//	public void setModel(UMLModelDTO model) {
//		this.model = model;
//	}
}

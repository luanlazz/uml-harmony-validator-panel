package com.plugin.services.dto;

public class DiagramStatisticsDTO {

private String id;
	
	private double riskMisinterpretation = 0;
	private String riskMisinterpretationStr;

	private double spreadRate = 0;
	private String spreadRateStr;

	private double concentrationInc = 0;
	private String concentrationIncStr;
	public DiagramStatisticsDTO() {
	}

	public DiagramStatisticsDTO(String id, double riskMisinterpretation, double spreadRate, double concentrationInc) {
		super();
		this.id = id;
		this.setRiskMisinterpretation(riskMisinterpretation);
		this.setSpreadRate(spreadRate);
		this.setConcentrationInc(concentrationInc);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getRiskMisinterpretation() {
		return riskMisinterpretation;
	}

	public void setRiskMisinterpretation(double riskMisinterpretation) {
		this.riskMisinterpretation = riskMisinterpretation;
		this.setRiskMisinterpretationStr(String.format("%.2f", riskMisinterpretation));
	}

	public String getRiskMisinterpretationStr() {
		return riskMisinterpretationStr;
	}

	public void setRiskMisinterpretationStr(String riskMisinterpretationStr) {
		this.riskMisinterpretationStr = riskMisinterpretationStr;
	}

	public double getSpreadRate() {
		return spreadRate;
	}

	public void setSpreadRate(double spreadRate) {
		this.spreadRate = spreadRate;
		this.setSpreadRateStr(String.format("%.2f", spreadRate));
	}

	public String getSpreadRateStr() {
		return spreadRateStr;
	}

	public void setSpreadRateStr(String spreadRateStr) {
		this.spreadRateStr = spreadRateStr;
	}

	public double getConcentrationInc() {
		return concentrationInc;
	}

	public void setConcentrationInc(double concentrationInc) {
		this.concentrationInc = concentrationInc;
		this.setConcentrationIncStr(String.format("%.2f", concentrationInc));
	}

	public String getConcentrationIncStr() {
		return concentrationIncStr;
	}

	public void setConcentrationIncStr(String concentrationIncStr) {
		this.concentrationIncStr = concentrationIncStr;
	}
}

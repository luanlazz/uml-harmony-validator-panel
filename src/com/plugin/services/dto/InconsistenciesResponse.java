package com.plugin.services.dto;

import java.util.ArrayList;
import java.util.List;

public class InconsistenciesResponse {

	List<InconsistencyErrorDTO> inconsistencies = new ArrayList<>();

	List<InconsistencyConcentrationDTO> diagrams = new ArrayList<>();
	List<InconsistencyConcentrationDTO> diagramsElements = new ArrayList<>();

	List<DiagramStatisticsDTO> diagramStatistics = new ArrayList<>();

	public InconsistenciesResponse() {
	}

	public List<InconsistencyErrorDTO> getInconsistencies() {
		return inconsistencies;
	}

	public void setInconsistencies(List<InconsistencyErrorDTO> inconsistencies) {
		this.inconsistencies = inconsistencies;
	}

	public List<InconsistencyConcentrationDTO> getDiagrams() {
		return diagrams;
	}

	public void setDiagrams(List<InconsistencyConcentrationDTO> diagrams) {
		this.diagrams = diagrams;
	}

	public List<InconsistencyConcentrationDTO> getDiagramsElements() {
		return diagramsElements;
	}

	public void setDiagramsElements(List<InconsistencyConcentrationDTO> diagramsElements) {
		this.diagramsElements = diagramsElements;
	}

	public List<DiagramStatisticsDTO> getDiagramStatistics() {
		return diagramStatistics;
	}

	public void setDiagramStatistics(List<DiagramStatisticsDTO> diagramStatistics) {
		this.diagramStatistics = diagramStatistics;
	}
}

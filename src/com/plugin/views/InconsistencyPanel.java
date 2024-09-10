package com.plugin.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.inconsistencies.concentration.table.DiagramsConcentrationTable;
import com.inconsistencies.table.InconsistenciesTable;
import com.plugin.services.dto.InconsistenciesResponse;
import com.plugin.services.dto.InconsistencyConcentrationDTO;
import com.plugin.services.dto.InconsistencyErrorDTO;
import com.plugin.services.dto.Severity;

public class InconsistencyPanel extends ViewPart {

	private static InconsistencyPanel single_instance = null;

	InconsistenciesResponse data = null;

	private InconsistenciesTable inconsistenciesTable = new InconsistenciesTable();

	private DiagramsConcentrationTable diagramConcentrationTable = new DiagramsConcentrationTable("diagram");
	private DiagramsConcentrationTable elementsConcentrationTable = new DiagramsConcentrationTable("elements");

	Label summary = null;

	Label labelDiagramsTable = null;
	Label labelElementsTable = null;
	Label labelInconsistenciesTable = null;

	Label labelTotalPkgs = null;
	Label labelTotalElements = null;
	Label labelTotalInconsistencies = null;

	public InconsistencyPanel() {
		single_instance = this;
	}

	public static InconsistencyPanel instace() {
		if (single_instance == null) {
			single_instance = new InconsistencyPanel();
		}

		return single_instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		int gridCols = 10;
		GridLayout layout = new GridLayout(gridCols, true);
		parent.setLayout(layout);
		Display display = parent.getDisplay();

		// summary
		this.summary = new Label(parent, PROP_TITLE);
		GridData gridSummary = new GridData(SWT.FILL, SWT.CENTER, true, false, gridCols, 1);
		this.summary.setLayoutData(gridSummary);
		FontData[] fD = this.summary.getFont().getFontData();
		fD[0].setHeight(20);
		this.summary.setFont(new Font(display, fD[0]));
		this.summary.setText("Analise o modelo.");

		// Cols span tables
		int tDiagramsCols = 3;
		int tElementsCols = 2;
		int tInconsistenciesCols = 5;

		this.labelDiagramsTable = new Label(parent, PROP_TITLE);
		GridData gridMisinterpretation = new GridData(SWT.FILL, SWT.CENTER, true, false, tDiagramsCols, 1);
		this.labelDiagramsTable.setLayoutData(gridMisinterpretation);
		fD[0].setHeight(16);
		this.labelDiagramsTable.setFont(new Font(display, fD[0]));
		this.labelDiagramsTable.setText("Diagramas");

		this.labelElementsTable = new Label(parent, PROP_TITLE);
		GridData gridSpreadRate = new GridData(SWT.FILL, SWT.CENTER, true, false, tElementsCols, 1);
		this.labelElementsTable.setLayoutData(gridSpreadRate);
		fD[0].setHeight(16);
		this.labelElementsTable.setFont(new Font(display, fD[0]));
		this.labelElementsTable.setText("Elementos");

		this.labelInconsistenciesTable = new Label(parent, PROP_TITLE);
		GridData gridConcentrationInc = new GridData(SWT.FILL, SWT.CENTER, true, false, tInconsistenciesCols, 1);
		this.labelInconsistenciesTable.setLayoutData(gridConcentrationInc);
		fD[0].setHeight(16);
		this.labelInconsistenciesTable.setFont(new Font(display, fD[0]));
		this.labelInconsistenciesTable.setText("Inconsistências");

		// Concentration diagrams
		this.diagramConcentrationTable.initializeColors(parent);
		this.diagramConcentrationTable.initializeTable(parent, tDiagramsCols);

		// Concentration elements
		this.elementsConcentrationTable.initializeColors(parent);
		this.elementsConcentrationTable.initializeTable(parent, tElementsCols);

		// Inconsistencies
		this.inconsistenciesTable.initializeColors(parent);
		this.inconsistenciesTable.initializeTable(parent, tInconsistenciesCols);

		// table details
		this.labelTotalPkgs = new Label(parent, PROP_TITLE);
		GridData gridTotalPkgs = new GridData(SWT.FILL, SWT.FILL, true, true, tDiagramsCols, 1);
		this.labelTotalPkgs.setLayoutData(gridTotalPkgs);
		this.updateTotalPkgs(0);

		this.labelTotalElements = new Label(parent, PROP_TITLE);
		GridData gridTotalElements = new GridData(SWT.FILL, SWT.FILL, true, true, tElementsCols, 1);
		this.labelTotalElements.setLayoutData(gridTotalElements);
		this.updateTotalElements(0);

		this.labelTotalInconsistencies = new Label(parent, PROP_TITLE);
		GridData gridTotalInconsistencies = new GridData(SWT.FILL, SWT.FILL, true, true, tInconsistenciesCols, 1);
		this.labelTotalInconsistencies.setLayoutData(gridTotalInconsistencies);
		this.updateTotalInconsistencies(0);
	}

	public void clearTables() {
		inconsistenciesTable.clearTable();
		diagramConcentrationTable.clearTable();
		elementsConcentrationTable.clearTable();

		updateTotalPkgs(0);
		updateTotalElements(0);
		updateTotalInconsistencies(0);
	}

	public void updateViewData(InconsistenciesResponse responseData) {
		data = responseData;

		List<InconsistencyErrorDTO> inconsistencies = data.getInconsistencies();
		int numInconsistencies = inconsistencies != null ? inconsistencies.size() : 0;
		if (numInconsistencies > 0) {
			List<InconsistencyConcentrationDTO> diagrams = data.getDiagrams();
			diagramConcentrationTable.fillConcentrations(diagrams, data.getDiagramStatistics());
			updateTotalPkgs(diagrams.size());
			
			String diagramId = diagrams.size()>0 ? diagrams.get(0).getId() : null;
			filterElementsByDiagramId(diagramId);
		}

		updateSummary(numInconsistencies);
	}

	public void filterElementsByDiagramId(String diagramId) {
		List<InconsistencyConcentrationDTO> elements = data.getDiagramsElements();

		if (diagramId != null) {
			elements = elements.stream().filter(e -> e.getParentId().equals(diagramId)).toList();
		}

		elementsConcentrationTable.fillConcentrations(elements, data.getDiagramStatistics());
		updateTotalElements(elements.size());
		
		String elementId = elements.size()>0 ? elements.get(0).getId() : null;
		filterInconsistenciesById(elementId);
	}

	public void filterInconsistenciesById(String id) {
		List<InconsistencyErrorDTO> inconsistencies = data.getInconsistencies();

		if (id != null) {
			inconsistencies = inconsistencies.stream().filter(i -> i.getParentId().equals(id) || i.getElId().equals(id))
					.toList();
		}

		inconsistenciesTable.fillInconsistencies(inconsistencies);
		updateTotalInconsistencies(inconsistencies.size());
	}

	public void updateTotalPkgs(int num) {
		this.labelTotalPkgs.setText(String.format("Total de diagramas: %d", num));
	}

	public void updateTotalElements(int num) {
		this.labelTotalElements.setText(String.format("Total de elementos: %d", num));
	}

	public void updateTotalInconsistencies(int num) {
		this.labelTotalInconsistencies.setText(String.format("Total de inconsistências: %d", num));
	}

	public void updateSummary(int num) {
		if (num > 0) {
			this.summary.setText(String.format("Foram identificadas %d inconsistências no modelo.", num));
		} else {
			this.summary.setText("O modelo está consistente!");
		}

		this.summary.pack();
	}

	public long countInconsistenciesBySeverity(List<InconsistencyErrorDTO> inconsistencies, Severity severity) {
		if (inconsistencies == null) {
			return 0;
		}

		return inconsistencies.stream().filter(inconsistency -> inconsistency.getSeverity() == severity.getValue())
				.count();
	}

	public static Image loadImage(Display display, String fileName) {
		// Use the class loader to load the image as a resource
		ClassLoader classLoader = InconsistencyPanel.class.getClassLoader();
		try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
			if (inputStream == null) {
				throw new IOException("Resource not found: " + fileName);
			}
			return new Image(display, inputStream);
		} catch (Exception e) {
			System.out.println("Error to load resource: " + fileName + " - error: " + e.getMessage());
			return null;
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}

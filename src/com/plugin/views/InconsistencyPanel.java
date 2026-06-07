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
import com.plugin.i18n.MessageService;
import com.plugin.services.InconsistencyAnalyserAPI;
import com.plugin.services.dto.InconsistenciesResponse;
import com.plugin.services.dto.InconsistencyConcentrationDTO;
import com.plugin.services.dto.InconsistencyErrorDTO;
import com.plugin.services.dto.Severity;

public class InconsistencyPanel extends ViewPart {

	private MessageService messageService;

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

		this.messageService = MessageService.instance();
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
		parent.setLayout(new GridLayout(gridCols, true));
		
		Display display = parent.getDisplay();

		setupSummaryLabel(parent, gridCols, display);

		int diagramTableCols = 3;
		int elementTableCols = 2;
		int inconsistencyTableCols = 5;

		setupDiagramTableLabel(parent, display, diagramTableCols);
		setupElementTableLabel(parent, display, elementTableCols);
		setupInconsistencyTableLabel(parent, display, inconsistencyTableCols);

		this.diagramConcentrationTable.initializeTable(parent, diagramTableCols);
		this.elementsConcentrationTable.initializeTable(parent, elementTableCols);
		this.inconsistenciesTable.initializeTable(parent, inconsistencyTableCols);

		setupDiagramTableFooter(parent, diagramTableCols);
		setupElementTableFooter(parent, elementTableCols);
		setupInconsistencyTableFooter(parent, inconsistencyTableCols);
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
			fillDiagramTable(diagrams);
		}

		updateSummary(numInconsistencies);
	}

	private void fillDiagramTable(List<InconsistencyConcentrationDTO> diagrams) {
		diagramConcentrationTable.fillConcentrations(diagrams, data.getDiagramStatistics());
		updateTotalPkgs(diagrams.size());

		String diagramId = diagrams.size() > 0 ? diagrams.get(0).getId() : null;
		filterElementsByDiagramId(diagramId);
	}
	
	public void filterElementsByDiagramId(String diagramId) {
		List<InconsistencyConcentrationDTO> elements = data.getDiagramsElements();

		if (diagramId != null) {
			elements = elements.stream().filter(e -> e.getParentId().equals(diagramId)).toList();
		}

		elementsConcentrationTable.fillConcentrations(elements, data.getDiagramStatistics());
		updateTotalElements(elements.size());

		String elementId = elements.size() > 0 ? elements.get(0).getId() : null;
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
		this.labelTotalPkgs.setText(String.format(messageService.get("table.model.footer"), num));
	}

	public void updateTotalElements(int num) {
		this.labelTotalElements.setText(String.format(messageService.get("table.element.footer"), num));
	}

	public void updateTotalInconsistencies(int num) {
		this.labelTotalInconsistencies.setText(String.format(messageService.get("table.inconsistency.footer"), num));
	}

	public void updateSummary(int num) {
		if (num > 0) {
			this.summary.setText(String.format(messageService.get("summary.model.inconsistent"), num));
		} else {
			this.summary.setText(messageService.get("summary.model.consistent"));
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
	
	private void setupSummaryLabel(Composite parent, int gridCols, Display display) {
		this.summary = new Label(parent, PROP_TITLE);
		GridData gridSummary = new GridData(SWT.FILL, SWT.CENTER, true, false, gridCols, 1);
		this.summary.setLayoutData(gridSummary);
		FontData[] fD = this.summary.getFont().getFontData();
		fD[0].setHeight(20);
		this.summary.setFont(new Font(display, fD[0]));
		this.summary.setText(messageService.get("summary.initial"));
	}
	
	private void setupDiagramTableLabel(Composite parent, Display display, int tDiagramsCols) {
		this.labelDiagramsTable = new Label(parent, PROP_TITLE);
		GridData gridMisinterpretation = new GridData(SWT.FILL, SWT.CENTER, true, false, tDiagramsCols, 1);
		this.labelDiagramsTable.setLayoutData(gridMisinterpretation);
		FontData[] fD = this.labelDiagramsTable.getFont().getFontData();
		fD[0].setHeight(16);
		this.labelDiagramsTable.setFont(new Font(display, fD[0]));
		this.labelDiagramsTable.setText(messageService.get("table.diagrams.label"));
	}	

	private void setupElementTableLabel(Composite parent, Display display, int tElementsCols) {
		this.labelElementsTable = new Label(parent, PROP_TITLE);
		GridData gridSpreadRate = new GridData(SWT.FILL, SWT.CENTER, true, false, tElementsCols, 1);
		this.labelElementsTable.setLayoutData(gridSpreadRate);
		FontData[] fD = this.labelElementsTable.getFont().getFontData();
		fD[0].setHeight(16);
		this.labelElementsTable.setFont(new Font(display, fD[0]));
		this.labelElementsTable.setText(messageService.get("table.element.label"));
	}

	private void setupInconsistencyTableLabel(Composite parent, Display display, int tInconsistenciesCols) {
		this.labelInconsistenciesTable = new Label(parent, PROP_TITLE);
		GridData gridConcentrationInc = new GridData(SWT.FILL, SWT.CENTER, true, false, tInconsistenciesCols, 1);
		this.labelInconsistenciesTable.setLayoutData(gridConcentrationInc);
		FontData[] fD = this.labelInconsistenciesTable.getFont().getFontData();
		fD[0].setHeight(16);
		this.labelInconsistenciesTable.setFont(new Font(display, fD[0]));
		this.labelInconsistenciesTable.setText(messageService.get("table.inconsistency.label"));
	}
	
	private void setupDiagramTableFooter(Composite parent, int tDiagramsCols) {
		this.labelTotalPkgs = new Label(parent, PROP_TITLE);
		GridData gridTotalPkgs = new GridData(SWT.FILL, SWT.FILL, true, true, tDiagramsCols, 1);
		this.labelTotalPkgs.setLayoutData(gridTotalPkgs);
		this.updateTotalPkgs(0);
	}

	private void setupElementTableFooter(Composite parent, int tElementsCols) {
		this.labelTotalElements = new Label(parent, PROP_TITLE);
		GridData gridTotalElements = new GridData(SWT.FILL, SWT.FILL, true, true, tElementsCols, 1);
		this.labelTotalElements.setLayoutData(gridTotalElements);
		this.updateTotalElements(0);
	}
	
	private void setupInconsistencyTableFooter(Composite parent, int tInconsistenciesCols) {
		this.labelTotalInconsistencies = new Label(parent, PROP_TITLE);
		GridData gridTotalInconsistencies = new GridData(SWT.FILL, SWT.FILL, true, true, tInconsistenciesCols, 1);
		this.labelTotalInconsistencies.setLayoutData(gridTotalInconsistencies);
		this.updateTotalInconsistencies(0);
	}
}

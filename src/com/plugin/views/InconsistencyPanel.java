package com.plugin.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
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
import com.plugin.services.dto.InconsistenciesResponse;
import com.plugin.services.dto.InconsistencyConcentrationDTO;
import com.plugin.services.dto.InconsistencyErrorDTO;
import com.plugin.services.dto.Severity;

public class InconsistencyPanel extends ViewPart {

	private MessageService messageService;
	
	public static String VIEW_ID = "com.inconsistency.InconsistencyAnalyser";

	private static int GRID_COLS = 10;

	private static int SUMMARY_LABEL_FONT_SIZE = 20;
	private static int TABLE_TITLE_FONT_SIZE = 16;

	private static int DIAGRAM_TABLE_COLS = 3;
	private static int ELEMENT_TABLE_COLS = 2;
	private static int INCONSISTENCY_TABLE_COLS = 5;

	private static InconsistencyPanel single_instance = null;

	private InconsistenciesResponse viewData = null;

	private InconsistenciesTable inconsistenciesTable = new InconsistenciesTable();

	private DiagramsConcentrationTable diagramConcentrationTable = new DiagramsConcentrationTable("diagram");
	private DiagramsConcentrationTable elementsConcentrationTable = new DiagramsConcentrationTable("elements");

	private Label summary = null;

	private Label labelDiagramsTable = null;
	private Label labelElementsTable = null;
	private Label labelInconsistenciesTable = null;

	private Label labelTotalPkgs = null;
	private Label labelTotalElements = null;
	private Label labelTotalInconsistencies = null;

	public InconsistencyPanel() {
		single_instance = this;

		this.messageService = MessageService.instance();
	}

	public static InconsistencyPanel instace() {
		if (single_instance == null) single_instance = new InconsistencyPanel();

		return single_instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(GRID_COLS, true));

		Display display = parent.getDisplay();

		setupSummaryLabel(parent, display);

		setupDiagramTableTitleLabel(parent, display);
		setupElementTableTitleLabel(parent, display);
		setupInconsistencyTableTitleLabel(parent, display);

		this.diagramConcentrationTable.initializeTable(parent, DIAGRAM_TABLE_COLS);
		this.elementsConcentrationTable.initializeTable(parent, ELEMENT_TABLE_COLS);
		this.inconsistenciesTable.initializeTable(parent, INCONSISTENCY_TABLE_COLS);

		setupDiagramTableFooter(parent);
		setupElementTableFooter(parent);
		setupInconsistencyTableFooter(parent);
	}

	public void clearTables() {
		inconsistenciesTable.clearTable();
		diagramConcentrationTable.clearTable();
		elementsConcentrationTable.clearTable();

		updateTotalPkgs(0);
		updateTotalElements(0);
		updateTotalInconsistencies(0);
		clearSummary();
	}

	public void updateViewData(InconsistenciesResponse responseData) {
		viewData = responseData;

		List<InconsistencyErrorDTO> inconsistencies = viewData.getInconsistencies();
		int numInconsistencies = inconsistencies != null ? inconsistencies.size() : 0;
		if (numInconsistencies > 0) {
			List<InconsistencyConcentrationDTO> diagrams = viewData.getDiagrams();
			fillDiagramTable(diagrams);
		}

		updateSummary(numInconsistencies);
	}

	private void fillDiagramTable(List<InconsistencyConcentrationDTO> diagrams) {
		diagramConcentrationTable.fillConcentrations(diagrams, viewData.getDiagramStatistics());
		updateTotalPkgs(diagrams.size());

		String diagramId = diagrams.size() > 0 ? diagrams.get(0).getId() : null;
		filterElementsByDiagramId(diagramId);
	}

	public void filterElementsByDiagramId(String diagramId) {
		List<InconsistencyConcentrationDTO> elements = viewData.getDiagramsElements();

		if (diagramId != null) elements = elements.stream().filter(e -> e.getParentId().equals(diagramId)).toList();

		elementsConcentrationTable.fillConcentrations(elements, viewData.getDiagramStatistics());
		updateTotalElements(elements.size());

		String elementId = elements.size() > 0 ? elements.get(0).getId() : null;
		filterInconsistenciesById(elementId);
	}

	public void filterInconsistenciesById(String id) {
		List<InconsistencyErrorDTO> inconsistencies = viewData.getInconsistencies();

		if (id != null) inconsistencies = inconsistencies.stream().filter(i -> i.getParentId().equals(id) || i.getElId().equals(id)).toList();

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
		this.summary.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		
		if (num > 0) {
			this.summary.setText(String.format(messageService.get("summary.model.inconsistent"), num));
		} else {
			this.summary.setText(messageService.get("summary.model.consistent"));
		}

		this.summary.pack();
	}
	
	public void clearSummary() {
		this.summary.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		this.summary.setText("");
		this.summary.pack();
	}

	public long countInconsistenciesBySeverity(List<InconsistencyErrorDTO> inconsistencies, Severity severity) {
		if (inconsistencies == null) return 0;

		return inconsistencies.stream().filter(inconsistency -> inconsistency.getSeverity() == severity.getValue()).count();
	}

	public static Image loadImage(Display display, String fileName) {
		// Use the class loader to load the image as a resource
		ClassLoader classLoader = InconsistencyPanel.class.getClassLoader();
		try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
			if (inputStream == null) throw new IOException("Resource not found: " + fileName);

			return new Image(display, inputStream);
		} catch (Exception e) {
			System.out.println("Error to load resource: " + fileName + " - error: " + e.getMessage());
			return null;
		}
	}	
	
	public void showInformationDialog(String message) {
		Display.getDefault().asyncExec(() -> {		    
		    MessageDialog.openInformation(Display.getDefault().getActiveShell(), "UML Harmony Validator", message);
		});		
	}
	
	public void showError(String message) {		
		clearTables();
        this.summary.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
        this.summary.setText(message);
		this.summary.pack();
	}

	@Override
	public void setFocus() { }
	
	private void setupSummaryLabel(Composite parent, Display display) {
		this.summary = new Label(parent, SWT.NONE);
		GridData gridSummary = new GridData(SWT.FILL, SWT.CENTER, true, false, GRID_COLS, 1);
		this.summary.setLayoutData(gridSummary);
		FontData[] fD = this.summary.getFont().getFontData();
		fD[0].setHeight(SUMMARY_LABEL_FONT_SIZE);
		this.summary.setFont(new Font(display, fD[0]));
		this.summary.setText(messageService.get("summary.initial"));
	}

	private void setupDiagramTableTitleLabel(Composite parent, Display display) {
		this.labelDiagramsTable = new Label(parent, SWT.NONE);
		GridData gridMisinterpretation = new GridData(SWT.FILL, SWT.CENTER, true, false, DIAGRAM_TABLE_COLS, 1);
		this.labelDiagramsTable.setLayoutData(gridMisinterpretation);
		FontData[] fD = this.labelDiagramsTable.getFont().getFontData();
		fD[0].setHeight(TABLE_TITLE_FONT_SIZE);
		this.labelDiagramsTable.setFont(new Font(display, fD[0]));
		this.labelDiagramsTable.setText(messageService.get("table.diagrams.label"));
	}	

	private void setupElementTableTitleLabel(Composite parent, Display display) {
		this.labelElementsTable = new Label(parent, SWT.NONE);
		GridData gridSpreadRate = new GridData(SWT.FILL, SWT.CENTER, true, false, ELEMENT_TABLE_COLS, 1);
		this.labelElementsTable.setLayoutData(gridSpreadRate);
		FontData[] fD = this.labelElementsTable.getFont().getFontData();
		fD[0].setHeight(TABLE_TITLE_FONT_SIZE);
		this.labelElementsTable.setFont(new Font(display, fD[0]));
		this.labelElementsTable.setText(messageService.get("table.element.label"));
	}

	private void setupInconsistencyTableTitleLabel(Composite parent, Display display) {
		this.labelInconsistenciesTable = new Label(parent, SWT.NONE);
		GridData gridConcentrationInc = new GridData(SWT.FILL, SWT.CENTER, true, false, INCONSISTENCY_TABLE_COLS, 1);
		this.labelInconsistenciesTable.setLayoutData(gridConcentrationInc);
		FontData[] fD = this.labelInconsistenciesTable.getFont().getFontData();
		fD[0].setHeight(TABLE_TITLE_FONT_SIZE);
		this.labelInconsistenciesTable.setFont(new Font(display, fD[0]));
		this.labelInconsistenciesTable.setText(messageService.get("table.inconsistency.label"));
	}

	private void setupDiagramTableFooter(Composite parent) {
		this.labelTotalPkgs = new Label(parent, SWT.NONE);
		GridData gridTotalPkgs = new GridData(SWT.FILL, SWT.FILL, true, true, DIAGRAM_TABLE_COLS, 1);
		this.labelTotalPkgs.setLayoutData(gridTotalPkgs);
		this.updateTotalPkgs(0);
	}

	private void setupElementTableFooter(Composite parent) {
		this.labelTotalElements = new Label(parent, SWT.NONE);
		GridData gridTotalElements = new GridData(SWT.FILL, SWT.FILL, true, true, ELEMENT_TABLE_COLS, 1);
		this.labelTotalElements.setLayoutData(gridTotalElements);
		this.updateTotalElements(0);
	}

	private void setupInconsistencyTableFooter(Composite parent) {
		this.labelTotalInconsistencies = new Label(parent, SWT.NONE);
		GridData gridTotalInconsistencies = new GridData(SWT.FILL, SWT.FILL, true, true, INCONSISTENCY_TABLE_COLS, 1);
		this.labelTotalInconsistencies.setLayoutData(gridTotalInconsistencies);
		this.updateTotalInconsistencies(0);
	}
}

package com.inconsistency;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.inconsistency.dto.InconsistencyErrorDTO;
import com.inconsistency.dto.Severity;

public class InconsistencyPanel extends ViewPart {

	private static InconsistencyPanel single_instance = null;

	private InconsistenciesTable inconsistenciesTable = new InconsistenciesTable();

	Label statusAnalyse = null;
	Label labelTotalInconsistencies = null;
	Label summary = null;

	CLabel highInconsistencies = null;
	Image imgHighWarning = null;
	CLabel mediumInconsistencies = null;
	Image imgMediumWarning = null;
	CLabel lowInconsistencies = null;
	Image imgLowWarning = null;

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
//		MenuManager menuManager = new MenuManager();
//		Menu contextMenu = menuManager.createContextMenu(parent);
//		getSite().registerContextMenu(menuManager, null);
//		parent.setMenu(contextMenu);

		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		Display display = parent.getDisplay();

		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(new GridLayout(2, true));
		row.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

//		Button button = new Button(row1, SWT.PUSH);
//		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//		button.setText("Analyse UML Model");
//		button.addListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event event) {
//				try {
//					statusAnalyse.setText("Analysing...");
//					statusAnalyse.update();
//
//					AnalyserResponseDTO analyseResponse = (AnalyserResponseDTO) analyseModelHandler.execute(null);
//
//					List<InconsistencyErrorDTO> inconsistencies = new ArrayList<>();
//
//					if (analyseResponse.getSuccess()) {
//						statusAnalyse.setText("Fetching result...");
//						statusAnalyse.update();
//
//						Thread.sleep(1000);
//
//						HashMap<String, String> parameters = new HashMap<String, String>();
//						parameters.put("clientId", analyseResponse.getClientId());
//
//						ExecutionEvent executionEvent = new ExecutionEvent(null, parameters, null, null);
//
//						inconsistencies = (List<InconsistencyErrorDTO>) getInconsistenciesHandler
//								.execute(executionEvent);
//					}
//
//					String modelName = null;
//					if (inconsistencies.size() > 0) {
//						inconsistenciesTable.fillInconsistencies(inconsistencies);
//						updateTotalInconsistencies(inconsistencies.size());
//
//						modelName = inconsistencies.get(0).getUmlPackage();
//					} else {
//						inconsistenciesTable.clearInconsistenciesTable();
//						updateTotalInconsistencies(0);
//					}
//					updateSummary(modelName, inconsistencies.size());
//
//					updateHighInconsistencies(countInconsistenciesBySeverity(inconsistencies, "HIGH"));
//					updateMediumInconsistencies(countInconsistenciesBySeverity(inconsistencies, "MEDIUM"));
//					updateLowInconsistencies(countInconsistenciesBySeverity(inconsistencies, "LOW"));
//
//					if (!analyseResponse.getSuccess()) {
//						throw new Exception();
//					}
//
//					statusAnalyse.setText("Check the result!");
//				} catch (Exception e) {
//					System.out.println(e.getMessage());
//					String errorMsg = e.getMessage() != null ? e.getMessage() : "Error when analyse the model.";
//					statusAnalyse.setText(errorMsg);
//				} finally {
//					statusAnalyse.update();
//					statusAnalyse.pack();
//				}
//			}
//		});
//
//		statusAnalyse = new Label(row1, PROP_TITLE);
//		statusAnalyse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		statusAnalyse.pack();

		// summary
		this.summary = new Label(row, PROP_TITLE);
		this.summary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		FontData[] fD = this.summary.getFont().getFontData();
		fD[0].setHeight(20);
		this.summary.setFont(new Font(display, fD[0]));
		this.updateSummary(null, 0);
//		this.summary.setText("Analyze your model to see if it is consistent!");
		this.summary.setText("Analise seu modelo para verificar se é consistente!");

		// summary inconsistencies
		Composite col2 = new Composite(row, SWT.NONE);
		col2.setLayout(new GridLayout(3, true));
		col2.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));

		fD[0].setHeight(18);

		this.highInconsistencies = new CLabel(col2, PROP_TITLE);
		this.highInconsistencies.setFont(new Font(display, fD[0]));
		this.imgHighWarning = loadImage(display, "high-warning.png");
		updateHighInconsistencies(0);

		this.mediumInconsistencies = new CLabel(col2, PROP_TITLE);
		this.mediumInconsistencies.setFont(new Font(display, fD[0]));
		this.imgMediumWarning = loadImage(display, "medium-warning.png");
		updateMediumInconsistencies(0);

		this.lowInconsistencies = new CLabel(col2, PROP_TITLE);
		this.lowInconsistencies.setFont(new Font(display, fD[0]));
		this.imgLowWarning = loadImage(display, "low-warning.png");
		updateLowInconsistencies(0);

		// table
		this.inconsistenciesTable.initializeColors(parent);
		this.inconsistenciesTable.initializeTable(parent);

		// table details
		this.labelTotalInconsistencies = new Label(parent, PROP_TITLE);
		this.updateTotalInconsistencies(0);
	}

	public void updateViewData(List<InconsistencyErrorDTO> inconsistencies) {
		String modelName = null;
		if (inconsistencies.size() > 0) {
			inconsistenciesTable.fillInconsistencies(inconsistencies);
			updateTotalInconsistencies(inconsistencies.size());

			modelName = inconsistencies.get(0).getUmlPackage();
		} else {
			inconsistenciesTable.clearInconsistenciesTable();
			updateTotalInconsistencies(0);
		}
		updateSummary(modelName, inconsistencies.size());

		updateHighInconsistencies(countInconsistenciesBySeverity(inconsistencies, Severity.HIGH));
		updateMediumInconsistencies(countInconsistenciesBySeverity(inconsistencies, Severity.MEDIUM));
		updateLowInconsistencies(countInconsistenciesBySeverity(inconsistencies, Severity.LOW));

//			statusAnalyse.setText("Check the result!");

//			System.out.println(e.getMessage());
//			String errorMsg = e.getMessage() != null ? e.getMessage() : "Error when analyse the model.";
//			statusAnalyse.setText(errorMsg);

	}

	public void updateTotalInconsistencies(int num) {
//		this.labelTotalInconsistencies.setText(String.format("Total inconsistencies: %d", num));
		this.labelTotalInconsistencies.setText(String.format("Total de inconsistencias: %d", num));
	}

	public void updateSummary(String model, int num) {
		if (model != null && num > 0) {
//			this.summary.setText(String.format("Model %s has %d inconsistencies.", model, num));
			this.summary.setText(String.format("O modelo %s contém %d inconsistencias.", model, num));
		} else {
//			this.summary.setText("Your model is consistent!");
			this.summary.setText("O modelo é consistente!");
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

	public void updateHighInconsistencies(long num) {
//		if (num > 0) {
//		this.highInconsistencies.setText("High: " + num);
		this.highInconsistencies.setText("Alta: " + num);
		if (this.imgHighWarning != null) {
			this.highInconsistencies.setImage(this.imgHighWarning);
		}
//		} else {
//			this.highInconsistencies.setText("");
//			this.highInconsistencies.setImage(null);
//		}

		this.highInconsistencies.pack();
	}

	public void updateMediumInconsistencies(long num) {
//		if (num > 0) {
//		this.mediumInconsistencies.setText("Medium: " + num);
		this.mediumInconsistencies.setText("Média: " + num);
		if (this.imgMediumWarning != null) {
			this.mediumInconsistencies.setImage(this.imgMediumWarning);
		}
//		} else {
//			this.mediumInconsistencies.setText("");
//			this.mediumInconsistencies.setImage(null);
//		}

		this.mediumInconsistencies.pack();
	}

	public void updateLowInconsistencies(long num) {
//		if (num > 0) {
//		this.lowInconsistencies.setText("Low: " + num);
		this.lowInconsistencies.setText("Baixa: " + num);
		if (this.imgLowWarning != null) {
			this.lowInconsistencies.setImage(this.imgLowWarning);
		}
//		} else {
//			this.lowInconsistencies.setText("");
//			this.lowInconsistencies.setImage(null);
//		}

		this.lowInconsistencies.pack();
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

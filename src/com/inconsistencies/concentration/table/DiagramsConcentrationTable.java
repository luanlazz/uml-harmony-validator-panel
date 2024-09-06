package com.inconsistencies.concentration.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.plugin.services.dto.DiagramStatisticsDTO;
import com.plugin.services.dto.InconsistencyConcentrationDTO;
import com.plugin.services.dto.Severity;
import com.plugin.views.InconsistencyPanel;

public class DiagramsConcentrationTable {

	private String type;
	private Table table = null;
	private HashMap<Integer, Color> colorBySeverity = new HashMap<>();

	List<InconsistencyConcentrationDTO> concentrations = null;

	public DiagramsConcentrationTable(String type) {
		this.type = type;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public void initializeColors(Composite parent) {
		Color red = new Color(new RGB(255, 0, 0));
		colorBySeverity.put(Severity.HIGH.getValue(), red);

		Color orange = new Color(new RGB(255, 165, 0));
		colorBySeverity.put(Severity.MEDIUM.getValue(), orange);

		Color yellow = new Color(new RGB(255, 255, 0));
		colorBySeverity.put(Severity.LOW.getValue(), yellow);
	}

	public void initializeTable(Composite parent, int cols) {
		setTable(new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION));
		GridData gridTable = new GridData(SWT.FILL, SWT.FILL, true, true, cols, 1);
		gridTable.heightHint = 130;
		table.setLayoutData(gridTable);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		FontData[] fD = table.getFont().getFontData();
		fD[0].setHeight(16);
		table.setFont(new Font(table.getDisplay(), fD[0]));

		List<String> tHead = new ArrayList<String>();

		if (type.equals("diagram")) {
			tHead.addAll(Arrays.asList("Diagrama", "Inc.", "Conc.(%)", "R.M.I. (%)", "T.E.I. (%)"));
			
			table.addListener(SWT.Selection, event -> {
				TableItem item = (TableItem) event.item;
				int tableIndex = table.indexOf(item);
				InconsistencyConcentrationDTO concentration = concentrations.get(tableIndex);
				InconsistencyPanel.instace().filterElementsByDiagramId(concentration.getId());
			});
		} else {
			tHead.addAll(Arrays.asList("Elemento", "Inc.", "Conc.(%)"));
			
			table.addListener(SWT.Selection, event -> {
				TableItem item = (TableItem) event.item;
				int tableIndex = table.indexOf(item);
				InconsistencyConcentrationDTO concentration = concentrations.get(tableIndex);
				InconsistencyPanel.instace().filterInconsistenciesById(concentration.getId());
			});
		}
		
		for (int i = 0; i < tHead.size(); i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(tHead.get(i));
			column.pack();
		}
		
		table.pack();
	}

	public void clearTable() {
		table.removeAll();
	}

	public void fillConcentrations(List<InconsistencyConcentrationDTO> concentrations, List<DiagramStatisticsDTO> diagramStatistics) {
		this.clearTable();
		this.concentrations = concentrations;

		for (InconsistencyConcentrationDTO concentration : concentrations) {
			TableItem tItem = new TableItem(table, SWT.NONE);
			FontData[] fD = tItem.getFont().getFontData();
			fD[0].setHeight(16);
			tItem.setFont(new Font(table.getDisplay(), fD[0]));

			tItem.setData(concentration);

			int i = 0;
			tItem.setText(i++, concentration.getName() != null ? concentration.getName() : "");
			toString();

			tItem.setText(i++,
					concentration.getNumInconsistencies() > 0 ? String.valueOf(concentration.getNumInconsistencies())
							: "0");

			tItem.setText(i, concentration.getConcentrationStr() != null ? concentration.getConcentrationStr() : "-");
			int severity = concentration.getSeverity();
			Color bgItem = colorBySeverity.get(severity > 0 && severity <= 3 ? severity : 1);
			tItem.setBackground(i, bgItem);
			if (severity >= 2) tItem.setForeground(i, this.table.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			
			i++;
			if (type.equals("diagram")) {
				DiagramStatisticsDTO diagramStatistic = diagramStatistics.stream()
						.filter(ds -> ds.getId().equals(concentration.getId())).findFirst().orElse(null);
				
				if (diagramStatistic != null) {
					tItem.setText(i++, diagramStatistic.getRiskMisinterpretationStr() != null ? diagramStatistic.getRiskMisinterpretationStr() : "");
					tItem.setText(i++, diagramStatistic.getSpreadRateStr() != null ? diagramStatistic.getSpreadRateStr(): "");
//					tItem.setText(i++, diagramStatistic.getConcentrationIncStr() != null ? diagramStatistic.getConcentrationIncStr() : "");
				}
			}
		}
	}
}

package com.inconsistency;

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

import com.inconsistency.dto.InconsistencyErrorDTO;
import com.inconsistency.dto.Severity;

public class InconsistenciesTable {

	private Table table = null;
	private HashMap<Integer, Color> colorBySeverity = new HashMap<>();

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

	public void initializeTable(Composite parent) {
		setTable(new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION));
		GridData gridTable = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 2);
		gridTable.heightHint = 200;
		table.setLayoutData(gridTable);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		FontData[] fD = table.getFont().getFontData();
		fD[0].setHeight(16);
		table.setFont(new Font(table.getDisplay(), fD[0]));
		
		// table header
//		String[] titles = { "Severity", "Diagram", "Inconsistency", "Context", "Consistency Rule" };
		String[] titles = { "Gravidade", "Contexto", "Inconsistencia", "Descrição", "Regra(s) de consistência" };

		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			column.pack();
//			column.setImage(images[i]);
			if (i == 3) {
				column.setWidth(700);
			}
		}

		table.addListener(SWT.Selection, event -> {
			String string = event.detail == SWT.CHECK ? "Checked" : "Selected";
			System.out.println(event.item + " " + string);
		});
	}

	public void clearInconsistenciesTable() {
		table.removeAll();
	}

	public void fillInconsistencies(List<InconsistencyErrorDTO> inconsistencies) {
		this.clearInconsistenciesTable();

		for (InconsistencyErrorDTO inconsistency : inconsistencies) {
			TableItem tItem = new TableItem(table, SWT.NONE);
			FontData[] fD = tItem.getFont().getFontData();
			fD[0].setHeight(16);
			tItem.setFont(new Font(table.getDisplay(), fD[0]));
			
			tItem.setData(inconsistencies);

			int i = 0;
//			TItem.setText(i, inconsistency.getSeverityLabel() != null ? inconsistency.getSeverityLabel() : "");
			Color bgItem = colorBySeverity.get(
					inconsistency.getSeverity() > 0 && inconsistency.getSeverity() <= 3 ? inconsistency.getSeverity()
							: 1);
			tItem.setBackground(i++, bgItem);
			tItem.setText(i++, inconsistency.getDiagram() != null ? inconsistency.getDiagram() : "");
			tItem.setText(i++,
					inconsistency.getInconsistencyTypeCode() != null ? inconsistency.getInconsistencyTypeCode() : "");
			tItem.setText(i++, inconsistency.getDescription() != null ? inconsistency.getDescription() : "");
			tItem.setText(i++, inconsistency.getCr() != null ? inconsistency.getCr() : "-");
		}
	}
}

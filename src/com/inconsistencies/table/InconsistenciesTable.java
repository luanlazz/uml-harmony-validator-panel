package com.inconsistencies.table;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.plugin.i18n.MessageService;
import com.plugin.services.dto.InconsistencyErrorDTO;
import com.plugin.services.dto.Severity;

public class InconsistenciesTable {

	private MessageService messageService;

	private Table table = null;
	private HashMap<Integer, Color> colorBySeverity = new HashMap<>();

	public InconsistenciesTable() {
		this.messageService = MessageService.instance();
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
		initializeColors(parent);
		
		setTable(new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION));
		GridData gridTable = new GridData(SWT.FILL, SWT.FILL, true, true, cols, 1);
		gridTable.heightHint = 130;
		table.setLayoutData(gridTable);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] tHead = { this.messageService.get("table.inconsistency.head.concentration"),
				this.messageService.get("table.inconsistency.head.inconsistency"),
				this.messageService.get("table.inconsistency.head.description"),
				this.messageService.get("table.inconsistency.head.consistency.rule") };

		for (int i = 0; i < tHead.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(tHead[i]);
			column.pack();
			if (i == 2) {
				column.setWidth(450);
			}
		}

		table.addListener(SWT.Selection, event -> {
			String string = event.detail == SWT.CHECK ? "Checked" : "Selected";
			System.out.println(event.item + " " + string);
		});

		table.pack();
	}

	public void clearTable() {
		table.removeAll();
	}

	public void fillInconsistencies(List<InconsistencyErrorDTO> inconsistencies) {
		this.clearTable();

		for (InconsistencyErrorDTO inconsistency : inconsistencies) {
			TableItem tItem = new TableItem(table, SWT.NONE);

			tItem.setData(inconsistencies);

			int i = 0;
			tItem.setText(i, inconsistency.getConcentrationStr() != null ? inconsistency.getConcentrationStr() : "-");
			int severity = inconsistency.getSeverity();
			Color bgItem = colorBySeverity.get(severity > 0 && severity <= 3 ? severity : 1);
			tItem.setBackground(i, bgItem);
			if (severity >= 2)
				tItem.setForeground(i, this.table.getDisplay().getSystemColor(SWT.COLOR_WHITE));

			i++;
			tItem.setText(i++,
					inconsistency.getInconsistencyTypeCode() != null ? inconsistency.getInconsistencyTypeCode() : "");
			tItem.setText(i++, inconsistency.getDescription() != null ? inconsistency.getDescription() : "");
			tItem.setText(i++, inconsistency.getCr() != null ? inconsistency.getCr() : "-");
		}
	}
}

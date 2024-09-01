package com.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.plugin.services.InconsistencyAnalyserAPI;

public class Configuration {

	public static void main() {
		Display display = Display.getCurrent();

		int dialogWidth = 629;
		int dialogHeight = 557;

		Shell dialog = new Shell(display, SWT.DIALOG_TRIM);
		dialog.setSize(dialogWidth, dialogHeight);
		dialog.setText("UML Harmony Validator Settings");

		Label label = new Label(dialog, SWT.NONE | SWT.BORDER);
		label.setText("Service URL");
		Text text = new Text(dialog, SWT.NONE);
		text.setText(InconsistencyAnalyserAPI.getUrl());

		GC gc = new GC(text);
		FontMetrics fm = gc.getFontMetrics();
		double charWidth = fm.getAverageCharacterWidth();
		int widthText = text.computeSize((int) (charWidth * 50), SWT.DEFAULT).x;
		gc.dispose();
		FormData data = new FormData(widthText, SWT.DEFAULT);
		text.setLayoutData(data);
		data.left = new FormAttachment(label, 5);
		data.top = new FormAttachment(label, 0, SWT.CENTER);

		FormLayout form = new FormLayout();
		form.marginWidth = form.marginHeight = 8;
		dialog.setLayout(form);

		Button cancelButton = new Button(dialog, SWT.PUSH);
		cancelButton.setText("&Cancel");
		cancelButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dialog.close();
			}
		});

		Button okButton = new Button(dialog, SWT.PUSH);
		okButton.setText("&OK");
		okButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				InconsistencyAnalyserAPI.setUrlBase(text.getText());
				dialog.close();
			}
		});
		FormData okData = new FormData();
		okData.top = new FormAttachment(label, 30);
		okData.right = new FormAttachment(100, 0);
		okButton.setLayoutData(okData);

		FormData cancelData = new FormData();
		cancelData.right = new FormAttachment(okButton, -8);
		cancelData.top = new FormAttachment(okButton, 0, SWT.TOP);
		cancelButton.setLayoutData(cancelData);

		dialog.setDefaultButton(okButton);
		dialog.pack();

		dialog.open();
	}
}

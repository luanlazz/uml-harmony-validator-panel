package com.plugin.handlers;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.plugin.services.InconsistencyAnalyserAPI;
import com.plugin.services.InconsistencyFetchAPI;
import com.plugin.services.dto.AnalyserResponseDTO;
import com.plugin.services.dto.InconsistencyErrorDTO;
import com.plugin.views.InconsistencyPanel;

public class AnalyseInconsistenciesHandler extends AbstractHandler {

	private InconsistencyAnalyserAPI analyserService = new InconsistencyAnalyserAPI();

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object execute(ExecutionEvent event) {
		List<InconsistencyErrorDTO> inconsistencies = new ArrayList<InconsistencyErrorDTO>();

		try {
			InconsistencyPanel.instace().clearTables();

			AnalyserResponseDTO analyseResponse = analyseActiveEditor();

			if (analyseResponse.getSuccess()) {
				int maxRetries = 12;
				long retryDelayInMS = 100; // 100 ms

				InconsistencyFetchAPI fetchAPI = new InconsistencyFetchAPI(analyseResponse.getClientId(), maxRetries,
						retryDelayInMS);
				Display.getDefault().asyncExec(fetchAPI);
			}
		} catch (Exception e) {
			System.out.println("AnalyseInconsistenciesHandler exception: " + e.getMessage());
		}

		return inconsistencies;
	}

	private AnalyserResponseDTO analyseActiveEditor() throws Exception {
		IWorkbenchPart workbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();

		IEditorPart activeEditor = workbenchPart.getSite().getPage().getActiveEditor();
		if (activeEditor == null) {
			throw new ExecutionException("Open a file first!");
		}

		IFile file = activeEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			throw new ExecutionException((new FileNotFoundException()).getMessage());
		}

		AnalyserResponseDTO analyseResponse = analyserService.analyseFile(file);
		if (!analyseResponse.getSuccess()) {
			throw new ExecutionException(analyseResponse.getError());
		}

		return analyseResponse;
	}
}

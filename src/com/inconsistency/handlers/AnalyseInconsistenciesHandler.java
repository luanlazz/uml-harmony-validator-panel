package com.inconsistency.handlers;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.inconsistency.InconsistencyPanel;
import com.inconsistency.dto.AnalyserResponseDTO;
import com.inconsistency.dto.InconsistencyErrorDTO;
import com.inconsistency.services.InconsistencyAnalyserAPI;

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
			InconsistencyPanel.instace().clearTable();
			
			AnalyserResponseDTO analyseResponse = analyseActiveEditor();
						
			if (analyseResponse.getSuccess()) {
				Thread.sleep(1000);

				inconsistencies = fecthInconsistenciesByClientId(analyseResponse.getClientId());
				
				InconsistencyPanel.instace().updateViewData(inconsistencies);
			}
		} catch (Exception e) {
			System.out.println("Error analyse model: " + e.getMessage());
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

	private List<InconsistencyErrorDTO> fecthInconsistenciesByClientId(String clientId) throws Exception {
		List<InconsistencyErrorDTO> response = analyserService.getInconsistenciesByClientId(clientId);

		return response;
	}
}

package com.plugin.handlers;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
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
		IEditorPart activeEditor = resolveActiveEditor();

        byte[] umlBytes = tryExtractFromPapyrusEditor(activeEditor);

        AnalyserResponseDTO analyseResponse;

        if (umlBytes != null) {
            analyseResponse = analyserService.analyseBytes(umlBytes, "test");
        } else {
        	IFile file = activeEditor.getEditorInput().getAdapter(IFile.class);
    		if (file == null) throw new ExecutionException((new FileNotFoundException()).getMessage());
    		analyseResponse = analyserService.analyseFile(file);
        }

		if (!analyseResponse.getSuccess()) throw new ExecutionException(analyseResponse.getError());

		return analyseResponse;
	}
	
	private byte[] tryExtractFromPapyrusEditor(IEditorPart editor) {
		ResourceSet resourceSet = null;

	    resourceSet = editor.getAdapter(ResourceSet.class);

	    if (resourceSet == null) {
	        resourceSet = resourceSetViaReflection(editor);
	    }

	    if (resourceSet == null) return null;

	    Resource umlResource = findUmlResource(resourceSet);
	    if (umlResource == null) return null;

	    try {
	        return serialiseToBytes(umlResource);
	    } catch (Exception e) {
	        logWarning("In-memory serialisation failed; falling back to file.", e);
	        return null;
	    }
    }
	
	private byte[] serialiseToBytes(Resource source) throws Exception {
        ResourceSet temp = new ResourceSetImpl();
        temp.getResourceFactoryRegistry()
            .getExtensionToFactoryMap()
            .putIfAbsent("uml", new XMIResourceFactoryImpl());

        URI memURI = URI.createURI("memory://__snapshot__.uml");
        Resource snapshot = temp.createResource(memURI);

        snapshot.getContents().addAll(new ArrayList<>(source.getContents()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        snapshot.save(out, Collections.emptyMap());

        source.getContents().addAll(snapshot.getContents());

        return out.toByteArray();
    }
	
	private ResourceSet resourceSetViaReflection(IEditorPart editor) {
	    try {
	        Method getEditingDomain = editor.getClass().getMethod("getEditingDomain");
	        Object editingDomain = getEditingDomain.invoke(editor);
	        if (editingDomain == null) return null;

	        Method getResourceSet = editingDomain.getClass().getMethod("getResourceSet");
	        Object rs = getResourceSet.invoke(editingDomain);

	        if (rs instanceof ResourceSet) {
	            return (ResourceSet) rs;
	        }

	    } catch (NoSuchMethodException ignored) {
	        // Editor doesn't expose an editing domain — file fallback will handle it
	    } catch (Exception e) {
	        logWarning("Reflection-based domain lookup failed.", e);
	    }
	    
	    return null;
	}
	
	private Resource findUmlResource(ResourceSet resourceSet) {
        for (Resource resource : resourceSet.getResources()) {
            URI uri = resource.getURI();
            if (uri != null && "uml".equalsIgnoreCase(uri.fileExtension())) {
                return resource;
            }
        }
        return null;
    }
	
	private IEditorPart resolveActiveEditor() throws ExecutionException {
        IWorkbenchPart workbenchPart = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActivePart();

        IEditorPart activeEditor = workbenchPart.getSite().getPage().getActiveEditor();
        if (activeEditor != null) return activeEditor;
        
        throw new ExecutionException("Open a UML model file first!");
    }
	
	private void logWarning(String message, Exception e) {
        System.err.println("[AnalyserHandler] " + message);
        if (e != null) e.printStackTrace();
    }
}

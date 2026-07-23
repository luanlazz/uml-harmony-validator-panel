package com.plugin.handlers;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.plugin.handlers.adapter.ModelSnapshotAdapter;
import com.plugin.i18n.MessageService;
import com.plugin.services.InconsistencyAnalyserAPI;
import com.plugin.services.InconsistencyFetchAPI;
import com.plugin.services.dto.AnalyserResponseDTO;
import com.plugin.services.dto.InconsistencyErrorDTO;
import com.plugin.utils.PluginLogger;
import com.plugin.utils.ValidationError;
import com.plugin.validator.ModelAnalyzeValidator;
import com.plugin.views.InconsistencyPanel;

public class AnalyseInconsistenciesHandler extends AbstractHandler {

	private static final PluginLogger LOGGER = new PluginLogger(AnalyseInconsistenciesHandler.class);

	private InconsistencyAnalyserAPI analyserService = new InconsistencyAnalyserAPI();
	private MessageService messageService;

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object execute(ExecutionEvent event) {
		this.messageService = MessageService.instance();

		List<ValidationError> errorList = ModelAnalyzeValidator.validate();
		if (!errorList.isEmpty()) {
			showInformationDialog(this.messageService.get(errorList.get(0).messageCode));
			return null;
		}

		List<InconsistencyErrorDTO> inconsistencies = new ArrayList<InconsistencyErrorDTO>();

		try {
			InconsistencyPanel.instace().clearTables();

			AnalyserResponseDTO analyseResponse = analyseActiveEditor();

			int maxRetries = 12;
			long retryDelayInMS = 100;

			InconsistencyFetchAPI fetchAPI = new InconsistencyFetchAPI(analyseResponse.getClientId(), maxRetries, retryDelayInMS);
			Display.getDefault().asyncExec(fetchAPI);
		} catch (ExecutionException exception) {
			showInformationDialog(exception.getMessage());
		} catch (Exception exception) {
			LOGGER.error("Error analyze the active editor.", exception);
		}

		return inconsistencies;
	}

	public void updateStatus(String message, boolean isError) {
		Display.getDefault().asyncExec(() -> {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(InconsistencyPanel.VIEW_ID);

			if (view == null) return;

			((InconsistencyPanel) view).setStatus(message, isError);
		});
	}

	public void showInformationDialog(String message) {
		Display.getDefault().asyncExec(() -> {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(InconsistencyPanel.VIEW_ID);

			if (view == null) return;

			((InconsistencyPanel) view).showInformationDialog(message);
		});
	}

	private AnalyserResponseDTO analyseActiveEditor() throws Exception {
		IEditorPart activeEditor = resolveActiveEditor();

		ModelSnapshotAdapter snapshotAdapter = tryExtractFromPapyrusEditor(activeEditor);

		AnalyserResponseDTO analyseResponse;

		if (snapshotAdapter != null) {
			analyseResponse = analyserService.analyseBytes(snapshotAdapter.bytes, snapshotAdapter.fileName);
		} else {
			IFile umlFile = resolveUmlFile(activeEditor);
			analyseResponse = analyserService.analyseFile(umlFile);
		}

		if (!analyseResponse.getSuccess()) throw new ExecutionException(analyseResponse.getError());

		return analyseResponse;
	}

	private ModelSnapshotAdapter tryExtractFromPapyrusEditor(IEditorPart editor) {
		ResourceSet resourceSet = null;

		resourceSet = editor.getAdapter(ResourceSet.class);
		if (resourceSet == null) resourceSet = resourceSetViaReflection(editor);
		if (resourceSet == null) return null;

		Resource umlResource = findUmlResource(resourceSet);
		if (umlResource == null) return null;

		try {
			byte[] bytes = serialiseToBytes(umlResource);
			String fileName = umlResource.getURI().lastSegment();

			return new ModelSnapshotAdapter(bytes, fileName);
		} catch (Exception exception) {
			LOGGER.warn("In-memory serialisation failed; falling back to file.", exception);
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

		for (EObject root : source.getContents()) {
			snapshot.getContents().add(EcoreUtil.copy(root));
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		snapshot.save(out, Collections.emptyMap());

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
		} catch (Exception exception) {
			LOGGER.warn("Reflection-based domain lookup failed.", exception);
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

	private IFile resolveUmlFile(IEditorPart activeEditor) throws ExecutionException {
		IFile openFile = activeEditor.getEditorInput().getAdapter(IFile.class);

		if (openFile != null && "uml".equalsIgnoreCase(openFile.getFileExtension())) return openFile;

		if (openFile != null) {
			IFile sibling = swapExtension(openFile, "uml");
			if (sibling != null) return sibling;
		}

		if (openFile != null) {
			IFile found = findUmlInProject(openFile.getProject());
			if (found != null) return found;
		}

		throw new ExecutionException(messageService.get("info.not.locate.model"));
	}

	private IFile swapExtension(IFile file, String ext) {
		IPath newPath = file.getFullPath().removeFileExtension().addFileExtension(ext);
		IFile candidate = ResourcesPlugin.getWorkspace().getRoot().getFile(newPath);
		return candidate.exists() ? candidate : null;
	}

	private IFile findUmlInProject(IProject project) {
		IFile[] result = { null };
		try {
			project.accept(resource -> {
				if (result[0] != null) return false;
				if (resource instanceof IFile) {
					IFile f = (IFile) resource;
					if ("uml".equalsIgnoreCase(f.getFileExtension())) {
						result[0] = f;
						return false;
					}
				}
				return true;
			});
		} catch (Exception ignored) {
		}

		return result[0];
	}

	private IEditorPart resolveActiveEditor() throws ExecutionException {
		IWorkbenchPart workbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();

		IEditorPart activeEditor = workbenchPart.getSite().getPage().getActiveEditor();
		if (activeEditor != null) return activeEditor;

		throw new ExecutionException(messageService.get("info.open.model"));
	}
}

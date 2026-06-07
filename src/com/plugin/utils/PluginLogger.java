package com.plugin.utils;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class PluginLogger {
	
	private static final String PLUGIN_ID = "UMLHarmonyValidatorPanel";

	private final ILog log;

    public PluginLogger(Class<?> clazz) {
        this.log = Platform.getLog(clazz);
    }

    public void warn(String message, Exception exception) {
        log.log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    public void error(String message, Exception exception) {
        log.log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }

    public void info(String message) {
        log.log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }
}

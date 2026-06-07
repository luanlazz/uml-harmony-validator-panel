package com.plugin.services;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.plugin.utils.PluginLogger;

public class AppPreferences {

	private static final PluginLogger LOGGER = new PluginLogger(AppPreferences.class);

	private static final String PLUGIN_ID = "UMLHarmonyValidatorPanel";

	public AppPreferences() {
	}

	public static void put(String key, String value) {
		Preferences preferences = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		Preferences sub1 = preferences.node("node1");
		sub1.put(key, value);

		try {
			preferences.flush();
		} catch (BackingStoreException exception) {
			LOGGER.error("Error to save preferences.", exception);
		}
	}

	public static String get(String key) {
		Preferences preferences = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		Preferences sub1 = preferences.node("node1");
		return sub1.get(key, "");
	}
}

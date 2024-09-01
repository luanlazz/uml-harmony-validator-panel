package com.plugin.services;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class AppPreferences {

	public static final String APP_ID = "UMLHarmonyValidatorPanel";

	public AppPreferences() {
	}

	public static void put(String key, String value) {
		Preferences preferences = InstanceScope.INSTANCE.getNode(APP_ID);
		Preferences sub1 = preferences.node("node1");
		sub1.put(key, value);

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		Preferences preferences = InstanceScope.INSTANCE.getNode(APP_ID);
		Preferences sub1 = preferences.node("node1");
		return sub1.get(key, "");
	}
}

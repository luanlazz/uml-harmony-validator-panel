package com.plugin.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.plugin.utils.PluginLogger;

public class MessageProvider implements IMessageProvider {

	private static final PluginLogger LOGGER = new PluginLogger(MessageProvider.class);

	private static MessageProvider single_instance = null;

	private ResourceBundle resourceBundle;
	private Locale locale;

	public MessageProvider() {
		single_instance = this;
		this.locale = Locale.getDefault();
		setResourceBundle();
	}

	public static MessageProvider instace() {
		if (single_instance == null) {
			single_instance = new MessageProvider();
		}

		return single_instance;
	}

	public void changeLocale(String locale) {
		switch (locale) {
		case "pt": {
			this.locale = new Locale("pt");
			break;
		}
		default:
			this.locale = Locale.getDefault();
		}

		setResourceBundle();
	}

	@Override
	public String get(String key) {
		return resourceBundle.getString(key);
	}

	public Locale getLocale() {
		return this.locale;
	}

	private void setResourceBundle() {
		try {
			resourceBundle = ResourceBundle.getBundle("resources/ApplicationMessages", this.locale);
		} catch (MissingResourceException exception) {
			LOGGER.error("Failed to load resource bundle [locale: " + this.locale + "].", exception);
			changeLocale("en");
		}
	}
}

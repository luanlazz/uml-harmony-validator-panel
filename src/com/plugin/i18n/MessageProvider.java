package com.plugin.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.plugin.utils.PluginLogger;

public class MessageProvider implements IMessageProvider {

	private static final PluginLogger LOGGER = new PluginLogger(MessageProvider.class);
	private static final String BUNDLE_BASE_NAME = "resources/ApplicationMessages";
	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	private static volatile MessageProvider single_instance;

	private ResourceBundle resourceBundle;
	private Locale locale;

	private MessageProvider() {
		this.locale = Locale.getDefault();
		loadResourceBundle(this.locale);
	}

	public static MessageProvider instance() {
		if (single_instance == null) {
			synchronized (MessageProvider.class) {
				if (single_instance == null) {
					single_instance = new MessageProvider();
				}
			}
		}
		return single_instance;
	}

	public void changeLocale(String locale) {
		Locale newLocale = switch (locale) {
			case "pt" -> new Locale("pt");
			default -> DEFAULT_LOCALE;
		};
		loadResourceBundle(newLocale);
	}

	@Override
	public String get(String key) {
		if (key == null || key.isBlank()) return "!" + key + "!";

		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException exception) {
			LOGGER.warn("Missing message key: [" + key + "]", exception);
			return key;
		}
	}

	public Locale getLocale() {
		return this.locale;
	}

	private void loadResourceBundle(Locale targetLocale) {
		try {
			this.resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, targetLocale);
			this.locale = targetLocale;
		} catch (MissingResourceException exception) {
			LOGGER.error("Failed to load resource bundle [locale: " + targetLocale + "].", exception);
			if (!targetLocale.equals(DEFAULT_LOCALE)) {
				LOGGER.warn("Falling back to default locale: " + DEFAULT_LOCALE, exception);
				loadResourceBundle(DEFAULT_LOCALE);
			}
		}
	}
}

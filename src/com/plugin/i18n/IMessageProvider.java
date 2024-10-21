package com.plugin.i18n;

import java.util.Locale;

public interface IMessageProvider {
	void changeLocale(String locale);

	String get(String key);

	Locale getLocale();
}

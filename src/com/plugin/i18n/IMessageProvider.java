package com.plugin.i18n;

import java.util.Locale;

public interface IMessageProvider {

	public String get(String key);
	public Locale getLocale();
}

package com.plugin.validator;

import java.util.ArrayList;
import java.util.List;

import com.plugin.services.AppPreferences;
import com.plugin.utils.ValidationError;

public class ModelAnalyzeValidator {

	private static final String PREF_KEY_BASE_URL = "base_url";

	public static List<ValidationError> validate() {
		List<ValidationError> errorList = new ArrayList<>();

		String url = AppPreferences.get(PREF_KEY_BASE_URL);
		if (url == null || url.isBlank()) errorList.add(new ValidationError("validation.service.url.not.configured"));

		return errorList;
	}
}

package com.plugin.validator;

import java.util.ArrayList;
import java.util.List;

import com.plugin.services.InconsistencyAnalyserAPI;
import com.plugin.utils.ValidationError;

public class ModelAnalyzeValidator {

	public static List<ValidationError> validate() {
		List<ValidationError> errorList = new ArrayList<>();

		String url = InconsistencyAnalyserAPI.getUrlBase();
		if (url == null || url.isBlank()) errorList.add(new ValidationError("validation.service.url.not.configured"));

		return errorList;
	}
}

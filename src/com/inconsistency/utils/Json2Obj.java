package com.inconsistency.utils;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Json2Obj {

	public static <T> T deserializeObj(String jsonString, Class<T> clazz) {
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat("MM/dd/yy HH:mm:ss");
		Gson gson = builder.create();
		return gson.fromJson(jsonString, clazz);
	}

	public static <T> List<T> deserializeCollection(String jsonString, Class<T> clazz) {
		Type listType = TypeToken.getParameterized(List.class, clazz).getType();
		return new Gson().fromJson(jsonString, listType);
	}
}

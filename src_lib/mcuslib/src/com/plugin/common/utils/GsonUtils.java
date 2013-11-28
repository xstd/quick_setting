package com.plugin.common.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class GsonUtils {

	static Gson gson = null;
	static Object obj = new Object();

	public static <T> T parse(String json, Class<T> cls) {
		if (json == null || cls == null) {
			return null;
		}

		if (gson == null) {
			synchronized (obj) {
				if (gson == null) {
					gson = new GsonBuilder().create();
				}
			}
		}
		try {
			return gson.fromJson(json, cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T parse(String json, Type type) {
		if (json == null || type == null) {
			return null;
		}

		if (gson == null) {
			synchronized (obj) {
				if (gson == null) {
					gson = new GsonBuilder().create();
				}
			}
		}
		try {
			return gson.fromJson(json, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class ExclusionFieldStrategy implements ExclusionStrategy {

		private Class<?>[] klasses;
		private String[] toExculdeFieldNames;

		public ExclusionFieldStrategy(String[] fieldNames) {
			try {
				klasses = new Class<?>[fieldNames.length];
				toExculdeFieldNames = new String[fieldNames.length];
				for (int i = 0; i < fieldNames.length; i++) {
					klasses[i] = Class.forName(fieldNames[i].substring(0, fieldNames[i].lastIndexOf(".")));
					toExculdeFieldNames[i] = fieldNames[i].substring(fieldNames[i].lastIndexOf(".") + 1);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		public boolean shouldSkipClass(Class<?> arg0) {
			return false;
		}

		public boolean shouldSkipField(FieldAttributes f) {
			for (int i = 0; i < klasses.length; i++) {
				if (f.getDeclaringClass() == klasses[i] && f.getName().equals(toExculdeFieldNames[i])) {
					return true;
				}
			}
			return false;
		}

	}

	public static String toJson(Object obj, String... excludeFields) {
		if (obj == null) {
			return null;
		}
		
		Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionFieldStrategy(excludeFields)).create();

		try {
			return gson.toJson(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
}

package com.bonult.money.tools;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * parse int, long, String, double, boolean from an Object
 *
 * @author bonult
 */
public class ParseTool {

	public static String parseString(Object obj, String defaultVal){
		return obj == null ? String.valueOf(obj) : defaultVal;
	}
	public static String parseString(Object obj){
		return parseString(obj, "");
	}

	public static int parseInt(String obj, int defaultVal){
		try{
			defaultVal = Integer.parseInt(String.valueOf(obj));
		}catch(Exception e){
		}
		return defaultVal;
	}

	public static int parseInt(String obj){
		return parseInt(obj, 0);
	}

	public static boolean parseBoolean(String obj, boolean defaultVal){
		try{
			defaultVal = Boolean.parseBoolean(String.valueOf(obj));
		}catch(Exception e){
		}
		return defaultVal;
	}

	public static boolean parseBoolean(String obj){
		return parseBoolean(obj, false);
	}

	public static long parseLong(String obj, long defaultVal){
		try{
			defaultVal = Long.parseLong(String.valueOf(obj));
		}catch(Exception e){
		}
		return defaultVal;
	}

	public static long parseLong(String obj){
		return parseLong(obj, 0);
	}

	public static double parseDouble(String obj, double defaultVal){
		try{
			defaultVal = Double.parseDouble(String.valueOf(obj));
		}catch(Exception e){
		}
		return defaultVal;
	}

	public static double parseDouble(String obj){
		return parseDouble(obj, 0.0);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parse(Class<T> clazz, String parameter){
		Function<String,?> function = ParseHolder.HOLDER.get(clazz);
		if(function == null){
			return null;
		}
		return (T)function.apply(parameter);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] parse(Class<T> componentType, String[] parameters){
		Object ts = Array.newInstance(componentType, parameters.length);
		Map<Class<?>,Function<String,?>> holder = ParseHolder.HOLDER;
		for(int i = 0; i < parameters.length; i++){
			Array.set(ts, i, holder.get(componentType).apply(parameters[i]));
		}
		return (T[])ts;
	}

	public static <T> void register(Class<T> clazz, Function<String,T> function){
		ParseHolder.HOLDER.put(clazz, function);
	}

	private static class ParseHolder {
		static final Map<Class<?>,Function<String,?>> HOLDER = new HashMap<>();

		static{
			HOLDER.put(int.class, Integer::parseInt);
			HOLDER.put(long.class, Long::parseLong);
			HOLDER.put(short.class, Short::parseShort);
			HOLDER.put(byte.class, Byte::parseByte);
			HOLDER.put(double.class, Double::parseDouble);
			HOLDER.put(float.class, Float::parseFloat);
			HOLDER.put(boolean.class, Boolean::valueOf);
			HOLDER.put(char.class, a -> a.charAt(0));

			HOLDER.put(Integer.class, Integer::parseInt);
			HOLDER.put(Long.class, Long::parseLong);
			HOLDER.put(Short.class, Short::parseShort);
			HOLDER.put(Byte.class, Byte::parseByte);
			HOLDER.put(Double.class, Double::parseDouble);
			HOLDER.put(Float.class, Float::parseFloat);
			HOLDER.put(Boolean.class, Boolean::valueOf);
			HOLDER.put(Character.class, a -> a.charAt(0));

			HOLDER.put(String.class, a -> a);


		}
	}
}

package com.googlecode._4s_web.client.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map.Entry;

/**
 * 스토리 시간 (시점, 순서) 클래스로  StoryTime POJO에 대응한다.
 * 시간 순서를 화면 좌표로 바꾸거나, 임의의 시점 이름을 시간 순서값으로 변환할 수 있다.
 */
public class StoryTimePoint {
	
	private static ArrayList<Integer> ordinalPoints = new ArrayList<Integer>();
	
	private static HashMap<String,Integer> annotations = new HashMap<String,Integer>();
	
	/*
	 * wrapper for entity
	 */
	
	public static int getCount() {
		return ordinalPoints.size();
	}

	public static HashMap<String, Integer> getAnnotation() {
		return annotations;
	}

	public static int getPoint(int ordinal) {
		if (ordinal<0 || ordinal>=ordinalPoints.size()) return -1;
		return ordinalPoints.get(ordinal);
	}
	public static int getOrdinal(int point) {
		return ordinalPoints.indexOf(point);
	}
	public static ArrayList<Integer> getOrdinalPoints() {
		return ordinalPoints;
	}
	public static void set(ArrayList<Integer> ordinalPoints, Map<String,Integer> annotations) {
		StoryTimePoint.ordinalPoints.clear();
		StoryTimePoint.ordinalPoints.addAll(ordinalPoints);
		StoryTimePoint.annotations.clear();
		StoryTimePoint.annotations.putAll(annotations);
	}
	
	public static void update(SortedSet<Integer> timepointSet,
			Map<Integer, String> annotedTimepoints) {
		ordinalPoints.clear();
		ordinalPoints.addAll(timepointSet);
		annotations.clear();
		Set<Entry<Integer, String>> entrySet = annotedTimepoints.entrySet();
		for (Entry<Integer, String> e : entrySet) {
			int value = ordinalPoints.indexOf(e.getKey());
			annotations.put(e.getValue(), value);
		}
	}

}

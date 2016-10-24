package com.googlecode._4s_web.client.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 이벤트에서 속성값을 얻고 값을 검사하기 위한 보조 클래스.
 * 
 * @author jehdeiah
 *
 */
class EventQueryHelper {
	public static final String CHARACTER = "Character";
	public static final String INVOLVED_CHARACTER = "InvolvedCharacter";
	public static final String ALL_CHARACTER = "AllCharacter";
	public static final String PLACE = "Place";
	
	public static String query(EventEntity event, String property) {
		String value = null;
		if (property.equals(CHARACTER)) {
			if (event.isAssigned()) {
				CharacterEntity c = LocalCache.get(CharacterEntity.class, event.getMainCharacter());
				return c.getName();
			}
		} else if (property.equals(PLACE)) {
			value = event.getPlace();
		} else {
			value = event.getUserProperty(property);
		}
		if (value==null || value.isEmpty())
			return null;
		return value;
	}

	public static boolean set(EventEntity event, String property, String value) {
		if (property.equals(CHARACTER) || 
				property.equals(INVOLVED_CHARACTER) ||
				property.equals(ALL_CHARACTER)) {
			if (value == null) {
				if (event.getMainCharacter() == -1) return false; 
				event.setMainCharacter(-1);
				return true;
			}
			for (CharacterEntity c : LocalCache.entities(CharacterEntity.class, new CharacterEntity[0])) {
				if (c.getName().equals(value)) {
					if (event.getMainCharacter() == c.getId()) return false;
					event.setMainCharacter(c.getId());
					return true;
				}
			}
		} else if (property.equals(PLACE)) {
			String v = event.getPlace();
			if (value==null && v==null) return false;
			if (value!=null && value.equals(v)) return false;
			event.setPlace(value);
			return true;
		} else {
			String v = event.getUserProperty(property);
			if (value==null && v==null) return false;
			if (value!=null && value.equals(v)) return false;
			event.setUserProperty(property, value);
			return true;
		}
		return false;
	}
	
	public static boolean test(EventEntity event, String property, String... values) {
		if (property.equals(CHARACTER)) {
			String v = query(event, property);
			return contains(v, values);
		} else if (property.equals(INVOLVED_CHARACTER)) {
			for (Long id : event.getInvolvedCharacters()) {
				CharacterEntity c = LocalCache.get(CharacterEntity.class, id);
				if (contains(c.getName(), values)) return true;
			}
			return false;
		} else if (property.equals(ALL_CHARACTER)) {
			boolean res = test(event, CHARACTER, values);
			if (res) return true;
			return test(event, INVOLVED_CHARACTER);
		} else if (property.equals(PLACE)) {
			String v = query(event, property);
			return contains(v, values);
		} else {
			String v = query(event, property);
			return contains(v, values);
		}		
	}
	
	private static boolean contains(String v, String... set) {
		for (String s : set) {
			if (v==null && s==null) return true;
			if (v!=null && v.equals(s)) return true;
		}
		return false;
	}
}

/**
 * 스토리에서 사건을 분류하는 카테고리 엔티티
 * <p>
 * 카테고리 기준은 별도의 질의문 구조를 가지지 않도록 단일 속성으로만 한다.
 * 그러나 하나의 이벤트는 기준에 따라 여러 카테고리에 속할 수가 있다.
 * 이벤트 속성 중 캐릭터, 장소는 기본 카테고리가 된다.
 * 
 * 카테고리 분류 기준:
 * 	1) 특정 속성값에 기준값이 포함되는 경우 (문자열의 부분 일치로 판단한다!)
 * 	2) 속성값이 카테고리 기준값들 중 하나라도 포함하는 경우 (OR)
 * 		장소나 인물들을 그룹으로 묶을 경우가 이 경우에 해당한다.
 *  3) 여러 속성값을 볼 경우는 모든 속성에 대해 만족하는 것으로 한다. (AND)
 * 	4) 예외적으로 인물(CHATEGORY_ALL)에 대해서는 주인공과 참여인물 두 속성을 본다.
 * 
 * TODO: DB 엔티티 만들기
 * (1) DB에는 카테고리 종류가 저장된다. 
 * 		(이벤트에 리스트로 있는 사용자 카테고리만 저장해도 된다.)
 * (2) 카테고리 값에 따라 색깔을 부여하려면 (category, value, color) 쌍이 저장되어야 한다. 
 */
public class Category {

	/**
	 * 카테고리의 각 값을 나타내는 요소.
	 * 카테고리와 해당값, 이벤트의 카테고리 소속 여부를 검사한다.
	 * @author jehdeiah
	 *
	 */
	public abstract class Entry {
		protected String category;
		protected String value;
		protected String color;
		public String category() { return category; }
		public String value() { return value==null ? CATEGORY_VALUE_NULL : value; }
		public String color() { return color; }
		public void setColor(String col) { color = col; }
		public boolean isNull() { return value==null; }
		public abstract boolean member(EventEntity event);
		public abstract boolean setValue(EventEntity event);
		public int hashCode() {
			String code = toString();
			return code.hashCode();
		}
		public String toString() {
			return category + ":" + value;
		}
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj!=null && obj instanceof Entry) {
				Entry e = (Entry)obj;
				if (category.equals(e.category)) {
					if (value==null && e.value==null) return true;
					if (value!=null && value.equals(e.value)) return true;
				}
			}
			return false;
		}
	}

	/**
	 * 이벤트 속성값으로 지정한 카테고리 요소
	 * @author jehdeiah
	 *
	 */
	class ValuedEntry extends Entry {
		protected ValuedEntry(String c, String v) { 
			category = c; 
			value = v; 
			color = "transparent";
		}
		public String category() { return category; }
		public String value() { return value==null ? CATEGORY_VALUE_NULL : value; }
		public boolean setValue(EventEntity event) {
			return EventQueryHelper.set(event, category, value);
		}

		public boolean member(EventEntity event) {
			return EventQueryHelper.test(event, category, value);
		}
	}
	
	/**
	 * 카테고리와 해당값을 새로 정의한 카테고리 요소
	 * @author jehdeiah
	 *
	 */
	class TypedEntry extends Entry {
		private Map<String, String[]> query;
		
		protected TypedEntry(String c, String v, Map<String, String[]> q) {
			category = c;
			value = v;
			query = q;
			color = "transparent";
		}
		
		public boolean setValue(EventEntity event) { return false; }
		
		public boolean member(EventEntity event) {
			for (Map.Entry<String, String[]> q: query.entrySet()) {
				boolean res = EventQueryHelper.test(event, q.getKey(), q.getValue());
				if (value!=null && res==false) return false;
			}
			return (value==null);
		}
	}
	
	/*
	 * 기본 카테고리
	 */
	public static final String CATEGORY_CHARACTER = "Character";
	public static final String CATEGORY_CHARACTER_ALL = "Character (All)";
	public static final String CATEGORY_PLACE = "Place";
	/*
	 * 특수값. 카테고리 지정 안 함 (None), 해당 값 없음 (N/A)
	 */
	public static final String CATEGORY_NONE = "None";
	public static final String CATEGORY_VALUE_NULL = "N/A";

	/**
	 * 전체 카테고리 목록. 카테고리 이름으로 담아둔다.
	 */
	private static Map<String, Category> categoryList = null;
	/**
	 * 카테고리 요소에 대응하는 색깔표
	 * 
	 */
	private static HashMap<Category.Entry, String> categoryColor = null;
	/* 
	 * 자주 쓰는 빈 배열 타입
	 */
	private static final String[] stringArrayType = new String[0];
	
	static {
		categoryList = new HashMap<String, Category>();
		categoryColor = new HashMap<Category.Entry,String>();
		categoryList.put(CATEGORY_CHARACTER, new Category(CATEGORY_CHARACTER));
		categoryList.put(CATEGORY_CHARACTER_ALL, new Category(CATEGORY_CHARACTER_ALL, EventQueryHelper.ALL_CHARACTER));
		categoryList.put(CATEGORY_PLACE, new Category(CATEGORY_PLACE));
	}

	public static void addCategory(Category category) {
		if (categoryList.get(category.getCategory()) == null)
			categoryList.put(category.getCategory(), category);
	}
	
	public static void setColor(Category.Entry entry, String color) {
		categoryColor.put(entry, color);
	}
	
	public static int count() {
		return categoryList.size();
	}
	
	public static Category get(String category) {
		return categoryList.get(category);
	}
	
	public static String[] getCategories() {
		return categoryList.keySet().toArray(stringArrayType);
	}

	/**
	 * 카테고리에 해당하는 CSS 문자열 색깔을 얻는다. 
	 * 인물의 경우는 고유색을 넘긴다.
	 * 
	 * @param category	카테고리
	 * @return	CSS 색깔. 색깔이 지정되지 않았으면 transparent를 넘긴다.
	 */
	public static String getColor(Category.Entry category) {
		return categoryColor.get(category);
	}
	
	/**
	 * 카테고리 이름
	 */
	private final String category;
	/**
	 * 유형화된 카테고리. 
	 * 카테고리 속성값이 미리 정해지고 판단 기준에 의해 따라 부여되는 경우 참값을 갖는다.
	 */
	private boolean typed;
	/**
	 * 카테고리 구분에 쓰이는 이벤트 속성값.
	 * 유형화되지 않은 경우에만 유효하다.
	 */
	private String property;
	/**
	 * 유형화된 카테고리의 값과 그것을 정하는 요소 목록
	 */
	private ArrayList<Category.Entry> values;
	
	public Category(String category) {	
		this.category = category;
		typed = false;
		property = category;
		values = null;
	}
		
	public Category(String category, String property) {	
		this.category = category;
		this.property = property;
		typed = false;
		values = null;
	}
	
	public Category(String category, ArrayList<Category.Entry> values) {
		this.category = category;
		this.values = values;
		typed = true;
		property = null;
	}
	
	public String getCategory(){
		return this.category;
	}
	
	public boolean isTyped() {
		return typed;
	}
	
	public Category.Entry nullValue() {
		Category.Entry entry = null;
		if (isTyped()) {
			entry = new Category.TypedEntry(category, null, null);
		} else {
			entry = new Category.ValuedEntry(category, null);
		}
		return entry;
	}
	
	/**
	 * 이벤트가 속하는 모든 카테고리를 반환하는데, 속하는 카테고리가 없으면 null 값의 카테고리 요소로 넘긴다.
	 * @param event 이벤트 
	 * @return
	 */
	public Category.Entry[] categorize(EventEntity event) {
		if (isTyped()) {
			ArrayList<Category.Entry> res = new ArrayList<Category.Entry>();
			for (Category.Entry entry : values) {
				if (entry.member(event)) {
					res.add(entry);
					String color = getColor(entry);
					if (color != null) entry.setColor(color);
				}
			}
			if (res.isEmpty()) res.add(nullValue());
			return res.toArray(new Category.Entry[0]);
		} else {
			if (category.equals(CATEGORY_CHARACTER_ALL)) {
				ArrayList<Category.Entry> res = new ArrayList<Category.Entry>();
				if (event.isAssigned()) {
					CharacterEntity c = LocalCache.get(CharacterEntity.class, event.getMainCharacter());
					Category.Entry entry = new Category.ValuedEntry(category, c.getName());
					entry.setColor(c.getColor());
					res.add(entry);
				}
				for (Long id : event.getInvolvedCharacters()) {
					CharacterEntity ic = LocalCache.get(CharacterEntity.class, id);
					Category.Entry entry = new Category.ValuedEntry(category, ic.getName());
					entry.setColor(ic.getColor());
					res.add(entry);
				}
				if (res.isEmpty()) res.add(nullValue()); 
				return res.toArray(new Category.Entry[0]); 
			} else {
				Category.Entry[] res = new Category.Entry[1];
				String v = EventQueryHelper.query(event, property);
				res[0] = (v==null) ? nullValue() : new Category.ValuedEntry(category, v);
				if (v!=null && category.equals(CATEGORY_CHARACTER) && event.isAssigned()) {
					CharacterEntity c = LocalCache.get(CharacterEntity.class, event.getMainCharacter());
					res[0].setColor(c.getColor());
				} else {
					String color = getColor(res[0]);
					if (color != null) res[0].setColor(color);
				}
				return res;
			}
		}
	}
}
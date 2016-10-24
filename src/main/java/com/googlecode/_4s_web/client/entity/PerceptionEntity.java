package com.googlecode._4s_web.client.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Client DTO for Perception Link POJO
 * 
 * 사건에서 정보를 인식하는 것을 나타내는 클래스.
 * <p>
 * 한 사건은 담화에서 여러 번 나타날 수 있다.
 * 그러나 등장인물들은 스토리 세계에서 그 사건은 단 한 번 경험한다.
 * 그러므로 등장인물들의 정보 인식은 반복된는 담화 사건에서 모두 동일하다.
 * 달라지는 것은 독자의 인식이다. 그러므로 자료 설계를 다시 해야 할 것 같다.
 * </p>
 * @author jehdeiah
 *
 */
public class PerceptionEntity extends LocalEntity implements KSRelationEntity {

	private static final long serialVersionUID = 1L;
	
	/*
	 * wrapper
	 */
	protected long eventId;
	protected long informationId;
	protected HashMap<Long,Float> values = new HashMap<Long,Float>();
	protected ArrayList<Float> readerValues = new ArrayList<Float>();
	
	/**
	 * 데이터스토어에 저장된 엔티티는 서버에서 만들어서 내려보낸다.
	 */
	protected PerceptionEntity() {}

	/**
	 * 이벤트는 담화에서 여러번 제시될 수 있으나 인식 속성은 하나로 유지되어야 한다.
	 * 그러므로 사건과 정보로부터 인식 속성을 얻게 하는데 순차 검색이므로 성능 개선 여지가 있다.
	 * 
	 * @param eventId 사건 식별자 
	 * @param infoId 정보 식별자 
	 * @return 인식 연결 요소 
	 */
	public static PerceptionEntity get(long eventId, long infoId) {
		PerceptionEntity[] entities = LocalCache.entities(PerceptionEntity.class, new PerceptionEntity[0]);
		for (PerceptionEntity p : entities) {
			if (p.eventId==eventId && p.informationId==infoId)
				return p;
		}
		return null;
	}

	/*
	 * wrapper for character entity
	 */
	public long getEvent() {
		return eventId;
	}
	
	public void setEvent(long event) {
		eventId = event;
	}
	
	public long getInformation() {
		return informationId;
	}
	
	public void setInformation(long info) {
		informationId = info;
	}
	
	public Map<Long,Float> getCharacterPerceptValues() {
		return values;
	}
	
	public ArrayList<Float> getReaderPerceptValues() {
		return readerValues;
	}

	public Float getValidSinglePerceptValueOfReader() {
		EventEntity e = LocalCache.get(EventEntity.class, eventId);
		if (e.getOccurrence() == 1) {
			return readerValues.get(0);
		}
		return null;
	}
	public void initializePerecptValues(float defaultValue) {
		values.clear();
		EventEntity event = LocalCache.get(EventEntity.class, eventId);
		ArrayList<Long> ids = new ArrayList<Long>(event.getInvolvedCharacters());
		long main = event.getMainCharacter();
		final long reader = -2L;
		if (main != -1) ids.add(0, main);
		ids.add(reader);
		for (Long id : ids) {
			values.put(id, defaultValue);
		}
		readerValues.clear();
		for (int i=0; i<event.getOccurrence(); i++) {
			readerValues.add(defaultValue);
		}
	}
}

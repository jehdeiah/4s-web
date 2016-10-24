package com.googlecode._4s_web.client.entity;

/**
 * 이벤트 관계 요소. (인과관계 등)
 * TODO: 속성 정의
 * 
 * @author jehdeiah
 *
 */
public class EventRelationEntity extends LocalEntity {

	private static final long serialVersionUID = 1L;

	/* 관계 유형 */
	public static final int CAUSAL = 1;
	
	protected int type;
	protected long fromId;
	protected long toId;
	protected String description;
	
	protected EventRelationEntity() {
		type = CAUSAL;
		fromId = -1;
		toId = -1;
	}

	/**
	 * 이벤트 관계 연결 요소 찾기
	 * 
	 * @param fromId 첫 사건 식별자 
	 * @param toId 나중 사건 식별자 
	 * @return 관계 연결 요소 
	 */
	public static EventRelationEntity get(long fromId, long toId) {
		EventRelationEntity[] entities = LocalCache.entities(EventRelationEntity.class, new EventRelationEntity[0]);
		for (EventRelationEntity r : entities) {
			if (r.fromId==fromId && r.toId==toId)
				return r;
		}
		return null;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getFromEvent() {
		return fromId;
	}

	public void setFromEvent(long from) {
		this.fromId = from;
	}

	public long getToEvent() {
		return toId;
	}

	public void setToEvent(long to) {
		this.toId = to;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String desc) {
		description = desc;
	}
}

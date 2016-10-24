package com.googlecode._4s_web.client.entity;


/**
 * Client DTO for ImpactLink POJO
 * 
 * @author jehdeiah
 *
 */
public class ImpactEntity extends LocalEntity implements KSRelationEntity {

	private static final long serialVersionUID = 1L;
	
	/*
	 * wrapper
	 */
	protected long knowledgeId;
	protected long informationId;
	protected boolean belief = true;
	protected float impactValue = 1.0f;
	
	/**
	 * 데이터스토어에 저장된 엔티티는 서버에서 만들어서 내려보낸다.
	 */
	protected ImpactEntity() {}

	/*
	 * wrapper for character entity
	 */
	public long getKnowledge() {
		return knowledgeId;
	}
	
	public void setKnowledge(long k) {
		knowledgeId = k;
	}
	
	public long getInformation() {
		return informationId;
	}
	
	public void setInformation(long info) {
		informationId = info;
	}
	
	public float getImpactValue() {
		return impactValue;
	}
	
	public void setImpactValue(float value) {
		impactValue = value;
	}
	
	public boolean getBelief() {
		return belief;
	}
	
	public void setBelief(boolean b) {
		belief = b;
	}
	
	public float getEffectiveImpact() {
		return belief ? impactValue : -impactValue;
	}
}

package com.googlecode._4s_web.client.entity;


/**
 * Client DTO for Knowledge POJO
 * 
 * @author jehdeiah
 *
 */
public class KnowledgeEntity extends LocalEntity implements KSElementEntity {

	private static final long serialVersionUID = 1L;
	
	/*
	 * wrapper
	 */
	protected String name;
	protected boolean truth = true;
	protected String description;
	
	/**
	 * 데이터스토어에 저장된 엔티티는 서버에서 만들어서 내려보낸다.
	 */
	protected KnowledgeEntity() {}

	/*
	 * wrapper for character entity
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String desc) {
		description = desc;
	}
	
	public boolean getTruth() {
		return truth;
	}
	
	public void setTruth(boolean t) {
		truth = t;
	}
}

package com.googlecode._4s_web.client.entity;


/**
 * Client DTO for Information POJO
 * 
 * @author jehdeiah
 *
 */
public class InformationEntity extends LocalEntity implements KSElementEntity {

	private static final long serialVersionUID = 1L;
	
	/*
	 * wrapper
	 */
	protected String name;
	protected String description;
	
	/**
	 * 데이터스토어에 저장된 엔티티는 서버에서 만들어서 내려보낸다.
	 */
	protected InformationEntity() {}

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
}

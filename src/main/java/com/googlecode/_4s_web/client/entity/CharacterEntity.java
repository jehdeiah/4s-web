package com.googlecode._4s_web.client.entity;


/**
 * Client DTO for Character POJO
 * 
 * 이 모듈에서는 동시에 하나의 이야기만 다룬다.
 * 그러므로 캐릭터 모둠을 정적 주머니에 담아둔다.
 * 
 * @author jehdeiah
 *
 */
public class CharacterEntity extends LocalEntity {

	private static final long serialVersionUID = 1L;
	
	/*
	 * wrapper
	 */
	protected String name;
	protected String desciption;
	protected String group;
	/**
	 * 캐릭터 고유색으로 rgb(r,g,b) 형태로 표시한다.
	 */
	protected String color;
	protected int index;

	/**
	 * 데이터스토어에 저장된 엔티티는 서버에서 만들어서 내려보낸다.
	 */
	protected CharacterEntity() {}
	
	/*
	 * wrapper for character entity
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
}

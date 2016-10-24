package com.googlecode._4s_web.client.entity;

import java.io.Serializable;

/**
 * 로컬 엔티티에 대한 추상 클래스.<br>
 * 식별자 id와 로컬 캐시를 위한 맵을 가진다.
 * 
 * 각 엔티티를 구상하는 클래스는 보호된 생성자와 객체 생성을 위한 보호된 정적 메쏘드 <code>newInstance()</code>를 가지며,
 * 서버에서 데이터스토어 엔티티에 대응하는 로컬 엔티티 객체를 생성하여 보낸다. <br>
 * 
 * @author jehdeiah
 *
 */
public abstract class LocalEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	protected long id;

	/*
	 * wrapper of datastore entity
	 * 
	 */
	public long getId() {
		return id;
	}
	protected void setId(long id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		String hashString = getClass() + Long.toString(id);
		return hashString.hashCode();
	}
}

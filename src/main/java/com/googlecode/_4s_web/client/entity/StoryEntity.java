package com.googlecode._4s_web.client.entity;

import java.io.Serializable;

/**
 * Client DTO for Story POJO
 * 
 * @author jehdeiah
 *
 */
public class StoryEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	protected long id;
	protected String title;
	protected String theme;
	protected long duration; // 밀리세컨드 단위 
	/**
	 * 엔티티는 서버에서 만들어서 내려보낸다.
	 */
	protected StoryEntity() {};
	
	public long getId() {
		return id;
	}

	protected void setId(long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
}

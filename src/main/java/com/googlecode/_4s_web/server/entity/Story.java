package com.googlecode._4s_web.server.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

@Entity
@Cache
@ToString
public class Story implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Getter Long id;

	@Getter @Setter String title;

	@Getter @Setter String theme;
	
	@Getter @Setter long duration;
	
	@Load Ref<StoryTime> storyTime;

	public Story() {
	}

	public Story(String title, String theme) {
		this.title = title;
		this.theme = theme;
	}

	public StoryTime getStoryTime() {
		return storyTime==null ? null : storyTime.get(); 
	}
	public void setStoryTime(StoryTime s) {
		//s.setStory(Key.create(this));
		storyTime = Ref.create(s);
	}
}

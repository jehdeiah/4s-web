package com.googlecode._4s_web.server.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

/**
 * 
 * @author jehdeiah
 * 
 */
@Entity
@Cache
@ToString
@EqualsAndHashCode(of={"id", "name", "story"})
public class Character implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Getter Long id;

	@Index
	@Getter @Setter String name;

	@Getter @Setter String color;

	@Getter @Setter String group;
	
	@Parent
	Key<Story> story;
	
	@Getter @Setter int index = -1;	// 타임라인 순서 

	protected static long newCount = 1;

	public Character() {
		name = "Character " + newCount;
		newCount++;
	}

	public Character(String name) {
		this.name = name;
	}

	// public Character(long uuid, String name) {
	// this.id = uuid;
	// this.name = name;
	// }

	public void setStory(Key<Story> story) {
		if (this.story==null || this.story.equivalent(story) != false)
			this.story = story;
	}

}

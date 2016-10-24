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
public class Information implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Getter Long id;

	@Index
	@Getter @Setter String name;

	@Getter @Setter String description;

	@Parent
	Key<Story> story;
	
	protected static long newCount = 1;

	public Information() {
		name = "Information " + newCount;
		newCount++;
	}
	public Information(String name) {
		this.name = name;
	}

	public void setStory(Key<Story> story) {
		if (this.story==null || this.story.equivalent(story) != false)
			this.story = story;
	}

}

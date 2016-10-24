package com.googlecode._4s_web.server.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import lombok.ToString;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

@Entity
@Cache
@ToString
public class StoryTime implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id @Getter Long Id;
	
	@Parent Key<Story> story;
	
	@Getter ArrayList<Integer> ordinalPoints; // should be sorted!
	
	@Getter HashMap<String, Integer> annotations;

	public StoryTime() {
		ordinalPoints = new ArrayList<Integer>();
		annotations = new HashMap<String, Integer>();
	}
	
	public void setStory(Key<Story> story) {
		this.story = story;
	}
	
	public int getCount() {
		return ordinalPoints.size();
	}

	public int getPoint(int ordinal) {
		return ordinalPoints.get(ordinal);
	}
	
	public void update(ArrayList<Integer> ordinalPoints, HashMap<String,Integer> annotations) {
		this.ordinalPoints.clear();
		this.ordinalPoints.addAll(ordinalPoints);
		this.annotations.clear();
		this.annotations.putAll(annotations);
	}
}

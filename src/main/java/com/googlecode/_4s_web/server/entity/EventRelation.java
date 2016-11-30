package com.googlecode._4s_web.server.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

@Entity
@Cache
@ToString
@EqualsAndHashCode(of={"id", "story"})
public class EventRelation implements HasValidation, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Getter Long id;

	@Getter @Setter int type;
	
	@Getter @Setter String description;
	
	@Load Ref<Event> fromEvent;
	
	@Load Ref<Event> toEvent;
	
	@Parent
	Key<Story> story;

	public EventRelation() {
	}
	public void setStory(Key<Story> story) {
		if (this.story==null || this.story.equivalent(story) != false)
			this.story = story;
	}

	public Event getFromEvent() {
		return fromEvent==null ? null : fromEvent.get();
	}
	
	public void setFromEvent(Event e) {
		Ref<Event> ref = Ref.create(e);
		if (ref == null) fromEvent = null;
		else if (!ref.equivalent(fromEvent)) fromEvent = ref;
	}
	
	public Event getToEvent() {
		return toEvent==null ? null : toEvent.get();
	}
	
	public void setToEvent(Event e) {
		Ref<Event> ref = Ref.create(e);
		if (ref == null) toEvent = null;
		else if (!ref.equivalent(toEvent)) toEvent = ref;
	}
	
	@Override
	public ValidationResult validate() {
		if (fromEvent == null) return ValidationResult.Invalid;
		else if (fromEvent.get() == null) {
				fromEvent = null;
				return ValidationResult.Invalid;
		}
		if (toEvent == null) return ValidationResult.Invalid;
		else if (toEvent.get() == null) {
				toEvent = null;
				return ValidationResult.Invalid;
		}
		return ValidationResult.Valid;
	}

}

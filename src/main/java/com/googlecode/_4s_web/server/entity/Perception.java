package com.googlecode._4s_web.server.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Stringify;
import com.googlecode.objectify.stringifier.KeyStringifier;

/**
 * 
 * @author jehdeiah
 * 
 */
@Entity
@Cache
@ToString
@EqualsAndHashCode(of={"id", "story"})
public class Perception implements HasValidation, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Getter Long id;

	@Load Ref<Event> event;
	
	@Load Ref<Information> info;
	
	@Stringify(KeyStringifier.class)
	Map<Key<Character>,Float> perceptValues;
	ArrayList<Float> readerPerceptValues;
	
	@Parent
	Key<Story> story;
	
	protected static long newCount = 1;

	public Perception() {
		newCount++;
	}
	public void setStory(Key<Story> story) {
		if (this.story==null || this.story.equivalent(story) != false)
			this.story = story;
	}

	public Event getEvent() {
		return event==null ? null : event.get();
	}
	
	public void setEvent(Event e) {
		Ref<Event> ref = Ref.create(e);
		if (ref == null) event = null;
		else if (!ref.equivalent(event)) event = ref;
	}
	
	public Information getInformation() {
		return info==null ? null : info.get();
	}
	
	public void setInformation(Information i) {
		Ref<Information> ref = Ref.create(i);
		if (ref == null) info = null;
		else if (!ref.equivalent(info)) info = ref;
	}
	
	public Map<Key<Character>,Float> getCharacterPerceptValues() {
		if (perceptValues == null) perceptValues = new HashMap<Key<Character>,Float>();
		return perceptValues;
	}
	
	public ArrayList<Float> getReaderPerceptValues() {
		if (readerPerceptValues == null) readerPerceptValues = new ArrayList<Float>();
		return readerPerceptValues;
	}

	public ValidationResult validate() {
		if (event == null) return ValidationResult.Invalid;
		else if (event.get() == null) {
				event = null;
				return ValidationResult.Invalid;
		}
		if (info == null) return ValidationResult.Invalid;
		else if (info.get() == null) {
			info = null;
			return ValidationResult.Invalid;
		}
		boolean changed = false;
		Event e = event.get();
		if (perceptValues == null) {
			perceptValues = new HashMap<Key<Character>, Float>();
		} else {
			ArrayList<Key<Character>> chars = new ArrayList<Key<Character>>();
			Character main = e.getMainCharacter();
			if (main != null) chars.add(Key.create(main));
			for (Character c : e.getInvolvedCharacters()) {
				chars.add(Key.create(c));
			}
			for (Key<Character> key : perceptValues.keySet()) {
				if (chars.contains(key) == false) {
					perceptValues.remove(key);
					changed = true;
				}
			}
		}
		if (readerPerceptValues == null) {
			readerPerceptValues = new ArrayList<Float>();
		} else {
			changed = readerPerceptValues.size()!=e.getOccurrence();
			int last = e.getOccurrence();
			for (int i=readerPerceptValues.size()-1; i>=last; i--) {
				readerPerceptValues.remove(last);
			}
			for (int i=readerPerceptValues.size(); i<last; i++) {
				readerPerceptValues.add(0f);
			}
		}		
		return changed ? ValidationResult.ChangedAndValid : ValidationResult.Valid;
	}
	
	public void initializePerceptValues(float defaultPerceptValue) {
		if (event==null || info==null) return;
		if (perceptValues == null) {
			perceptValues = new HashMap<Key<Character>, Float>();
		} else {
			perceptValues.clear();
		}
		if (readerPerceptValues == null) {
			readerPerceptValues = new ArrayList<Float>();
		} else {
			readerPerceptValues.clear();
		}
		Event e = event.get();
		Character main = e.getMainCharacter();
		if (main != null) {
			perceptValues.put(Key.create(main), defaultPerceptValue);
		}
		Collection<Character> chars = e.getInvolvedCharacters();
		for (Character c : chars) {
			perceptValues.put(Key.create(c), defaultPerceptValue);
		}
		int count = e.getOccurrence();
		for (int i=0; i<count; i++) {
			readerPerceptValues.add(defaultPerceptValue);
		}
	}
	
}

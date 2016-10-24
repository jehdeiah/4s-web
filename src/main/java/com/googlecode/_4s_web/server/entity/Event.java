package com.googlecode._4s_web.server.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.googlecode._4s_web.shared.Interval;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;

/**
 * 
 * @author jehdeiah
 *
 */
@Entity
@Cache
@ToString
@EqualsAndHashCode(of={"id", "story", "name"})
public class Event implements HasValidation, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Getter Long id;

	@Parent
	Key<Story> story;
	
	@Getter @Setter String name;

	@Index
	@Load
	Ref<Character> mainCharacter;

	@Load
	Collection<Ref<Character>> involvedCharacters = null;

	@Getter @Setter int ordinalStoryIn = -1;
	@Getter @Setter int ordinalStoryOut = -1;
	//int occurrenceInDiscourse = 0;

	SortedSet<Interval> discourseInOut = new TreeSet<Interval>();

	protected static long newCount = 1;

	public Event() {
		name = "Event" + newCount;
		newCount++;
	}

	public Event(String name) {
		this.name = name;
	}

	public Event(long uuid, String name) {
		this.id = uuid;
		this.name = name;
	}

	public void setStory(Key<Story> story) {
		if (this.story==null || this.story.equivalent(story) != false)
			this.story = story;
	}

	public Character getMainCharacter() {
		return mainCharacter==null ? null : mainCharacter.get();
	}

	public Collection<Character> getInvolvedCharacters() {
		ArrayList<Character> list = new ArrayList<Character>(); 
		if (involvedCharacters != null) {
			for (Ref<Character> c : involvedCharacters) {
				list.add(c.get());
			}
		}
		return list;
	}

	public void setMainCharacter(Character character) {
		if (character == null) {
			mainCharacter = null;
			ordinalStoryIn = ordinalStoryOut = -1;
		} else
			mainCharacter = Ref.create(character);
	}

	public void setInvolvedCharacters(Collection<Character> involved) {
		if (involvedCharacters == null && involved.isEmpty()) return;
		if (involvedCharacters == null) 
			involvedCharacters = new ArrayList<Ref<Character>>();
		else 
			involvedCharacters.clear();
		for (Character c : involved) {
			involvedCharacters.add(Ref.create(c));
		}
	}
	
	public void setOrdinalStoryInOut(int in, int out) {
		ordinalStoryIn = in;
		ordinalStoryOut = out;
	}

	public int getOccurrence() {
		return discourseInOut.size();//occurrenceInDiscourse;
	}

	public SortedSet<Interval> getDiscourseInOut() {
		return discourseInOut;
	}

	public ValidationResult validate() {
		boolean changed = false;
		if (mainCharacter != null) {
			Character main = mainCharacter.get();
			if (main == null) {
				mainCharacter = null;
				changed = true;
			}
		}
		if (involvedCharacters != null) {
			for (Ref<Character> ref : involvedCharacters) {
				Character c = ref.get();
				if (c == null) {
					involvedCharacters.remove(ref);
					changed = true;
				}
			}
		}
		return changed ? ValidationResult.ChangedAndValid : ValidationResult.Valid;
	}
}

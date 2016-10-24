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
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

/**
 * 
 * @author jehdeiah
 * 
 */
@Entity
@Cache
@ToString
@EqualsAndHashCode(of={"id", "belief", "story"})
public class Impact implements HasValidation, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Getter Long id;

	@Load @Index
	Ref<Information> info;
	
	@Load @Index
	Ref<Knowledge> knowledge;
	
	@Getter @Setter boolean belief;
	
	@Getter @Setter float impactValue;

	@Parent
	Key<Story> story;
	
	protected static long newCount = 1;

	public Impact() {
		initializeImpact();
		newCount++;
	}
	public void setStory(Key<Story> story) {
		if (this.story==null || this.story.equivalent(story) != false)
			this.story = story;
	}

	public float getEffectiveImpact() {
		return belief ? impactValue : -impactValue;
	}
	
	public Information getInformation() {
		return info==null ? null : info.get();
	}
	
	public Knowledge getKnowledge() {
		return knowledge==null ? null : knowledge.get();
	}
	
	public void setInformation(Information i) {
		Ref<Information> ref = Ref.create(i);
		if (ref == null) info = null;
		else if (!ref.equivalent(info)) {
			info = ref;
		}
	}
	
	public void setKnowledge(Knowledge k) {
		Ref<Knowledge> ref = Ref.create(k);
		if (ref == null) knowledge = null;
		else if (!ref.equivalent(knowledge)) {
			knowledge = ref;
		}
	}
	
	void initializeImpact() {
		final float defaultImpactValue = 1.0F;
		final boolean defaultBelief = true;
		belief = defaultBelief;
		impactValue = defaultImpactValue;
	}
	
	public ValidationResult validate() {
		if (info == null) return ValidationResult.Invalid;
		else if (info.get() == null) {
				info = null;
				return ValidationResult.Invalid;
		}
		if (knowledge == null) return ValidationResult.Invalid;
		else if (knowledge.get() == null) {
			knowledge = null;
			return ValidationResult.Invalid;
		}
		return ValidationResult.Valid;
	}
}

package com.googlecode._4s_web.client.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode._4s_web.shared.Interval;

/**
 * Client DTO for Event POJO
 * 
 * @author jehdeiah
 *
 */
public class EventEntity extends LocalEntity implements KSElementEntity {

	private static final long serialVersionUID = 1L;

	protected String name;
	protected long mainCharacterId = -1;	// modified
	protected ArrayList<Long> involvedCharacterIds = new ArrayList<Long>(); //modified
	protected int ordinalStoryIn = -1;
	protected int ordinalStoryOut = -1;
	protected SortedSet<Interval> discourseInOut = new TreeSet<Interval>();
	protected String place;
	protected String actionDescription;
	protected HashMap<String, String> userProperties = new HashMap<String, String>();

	/**
	 * 데이터스토어에 저장된 엔티티는 서버에서 만들어서 내려보낸다.
	 */
	protected EventEntity() {}
	protected static EventEntity newInstance(long id) { 
		EventEntity e = new EventEntity();
		e.id = id;
		return e;
	}
	
	/*
	 * wrapper of event entity
	 * 
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public long getMainCharacter() {
		return mainCharacterId;
	}
	public long setMainCharacter(long characterId) {
		long last = mainCharacterId;
		mainCharacterId = characterId;
		return last;
	}
	public Collection<Long> getInvolvedCharacters() {
		return involvedCharacterIds;
	}
	public void setInvolvedCharacters(Collection<Long> involvedIds) {
		involvedCharacterIds.clear();
		for (Long id : involvedIds) {
			if (!involvedCharacterIds.contains(id))
				involvedCharacterIds.add(id);
		}
	}
	public boolean isInvolved(long characterId) {
		return mainCharacterId==characterId || involvedCharacterIds.contains(Long.valueOf(characterId));
	}
	
	public int getOrdinalStoryIn() {
		return ordinalStoryIn;
	}
	public int getOrdinalStoryOut() {
		return ordinalStoryOut;
	}
	public void setOrdinalStoryInOut(int in, int out) {
		ordinalStoryIn = in;
		ordinalStoryOut = out;
	}
	
	public int getOccurrence() {
		return discourseInOut.size();
	}
	public SortedSet<Interval> getDiscourseInOut() {
		return discourseInOut;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String getActionDescription() {
		return actionDescription;
	}
	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}
	public String getUserProperty(String property) {
		return userProperties.get(property);
	}
	public void setUserProperty(String property, String value) {
		userProperties.put(property, value);
	}
	
	/*
	 * client-side methods
	 */	
	/**
	 * 캐릭터 할당 여부 확인
	 * @return
	 */
	public boolean isAssigned() {
		return mainCharacterId != -1;
	}

	/**
	 * 스토리 타임라인 배치 여부 확인
	 * @return
	 */
	public boolean isInStory() {
		return ordinalStoryIn!=-1;
	}
	
	/**
	 * 담화 배치 여부 확인
	 * @return
	 */
	public boolean isPlotted() {
		return (getOccurrence() > 0);
	}
	
	/**
	 * 스토리 타임에 할당된 이벤트인지 반환한다.
	 * @return
	 */
	public boolean isOnStoryTime(){
		return (this.ordinalStoryIn != -1) && (this.ordinalStoryOut != -1);
	}

	public int addToDiscourse(double start, double end) {
		discourseInOut.add(new Interval(start, end));
		return getOccurrence();
	}

	public int removeFromDiscourse(Interval interval) {
		discourseInOut.remove(interval);
		return getOccurrence();
	}
}

package com.googlecode._4s_web.server;

import static com.googlecode._4s_web.server.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode._4s_web.client.StoryService;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.EventRelationEntity;
import com.googlecode._4s_web.client.entity.InformationEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalEntity;
import com.googlecode._4s_web.client.entity.PerceptionEntity;
import com.googlecode._4s_web.client.entity.StoryEntity;
import com.googlecode._4s_web.client.entity.ImpactEntity;
import com.googlecode._4s_web.server.entity.Character;
import com.googlecode._4s_web.server.entity.EntityFactory;
import com.googlecode._4s_web.server.entity.Event;
import com.googlecode._4s_web.server.entity.EventRelation;
import com.googlecode._4s_web.server.entity.Information;
import com.googlecode._4s_web.server.entity.Knowledge;
import com.googlecode._4s_web.server.entity.Perception;
import com.googlecode._4s_web.server.entity.Story;
import com.googlecode._4s_web.server.entity.StoryTime;
import com.googlecode._4s_web.server.entity.Impact;
import com.googlecode.objectify.Key;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class StoryServiceImpl extends RemoteServiceServlet implements
		StoryService {

	/*
	 * Story Information
	 */

	Key<Story> currentStoryKey = null;
	Story currentStory = null;
	final StoryEntity[] storyArrayType = new StoryEntity[0];
	final CharacterEntity[] characterArrayType = new CharacterEntity[0];
	final EventEntity[] eventArrayType = new EventEntity[0];
	final EventRelationEntity[] eventRelationArrayType = new EventRelationEntity[0];
	final InformationEntity[] informationArrayType = new InformationEntity[0];
	final KnowledgeEntity[] knowledgeArrayType = new KnowledgeEntity[0];
	final PerceptionEntity[] perceptArrayType = new PerceptionEntity[0];
	final ImpactEntity[] supportArrayType = new ImpactEntity[0];
	
	Class<?> getEntityClass(String clientEntityName) {
		if (clientEntityName.equals(CharacterEntity.class.getName())) {
			return Character.class;
		} else if (clientEntityName.equals(EventEntity.class.getName())) {
			return Event.class;
		} else if (clientEntityName.equals(EventRelationEntity.class.getName())) {
			return EventRelation.class;
		} else if (clientEntityName.equals(InformationEntity.class.getName())) {
			return Information.class;
		} else if (clientEntityName.equals(KnowledgeEntity.class.getName())) {
			return Knowledge.class;
		} else if (clientEntityName.equals(PerceptionEntity.class.getName())) {
			return Perception.class;
		} else if (clientEntityName.equals(ImpactEntity.class.getName())) {
			return Impact.class;
		} 
		return null;
	}
	
	LocalEntity[] getLocalEntityArray(String clientEntityName) {
		if (clientEntityName.equals(CharacterEntity.class.getName())) {
			return new CharacterEntity[0];
		} else if (clientEntityName.equals(EventEntity.class.getName())) {
			return new EventEntity[0];
		} else if (clientEntityName.equals(EventRelationEntity.class.getName())) {
			return new EventRelationEntity[0];
		} else if (clientEntityName.equals(InformationEntity.class.getName())) {
			return new InformationEntity[0];
		} else if (clientEntityName.equals(KnowledgeEntity.class.getName())) {
			return new KnowledgeEntity[0];
		} else if (clientEntityName.equals(PerceptionEntity.class.getName())) {
			return new PerceptionEntity[0];
		} else if (clientEntityName.equals(ImpactEntity.class.getName())) {
			return new ImpactEntity[0];
		} 
		return null;		
	}

	/*
	 * New Instances...
	 */
	Character newCharacter = null;
	Event newEvent = null;
	Information newInformation = null;
	Knowledge newKnownledge = null;
	Perception newPerception = null;
	Impact newSupport = null;

	public StoryEntity[] loadStoryHeader() {
		// LoadResult<Story> first = ofy().load().type(Story.class).first();
		List<Story> stories = ofy().load().type(Story.class).list();
		ArrayList<StoryEntity> list = new ArrayList<StoryEntity>();
		for (Story s : stories) {
			list.add(EntityFactory.getEntity(s));
		}
		return list.toArray(storyArrayType);
	}

	public void setWorkingStory(Long storyId) {
		Story story = ofy().load().type(Story.class).id(storyId).now();
		// raise exception when storyId is wrong...
		if (story == null) throw new java.lang.NullPointerException("There's no story having ID " + storyId);
		currentStory = story;
		Key<Story> newStoryKey = Key.create(story);
		if (!newStoryKey.equivalent(currentStoryKey)) {
			currentStoryKey = newStoryKey;
			EntityFactory.flush();
		}
	}

	public void saveStoryHeader(StoryEntity story) {
		Story s = ofy().load().type(Story.class).id(story.getId()).now();
		s = EntityFactory.update(s, story);
		ofy().save().entity(s).now();
	}
	
	/**
	 * 스토리를 새로 만들면서 현재 작업 스토리를 새 스토리로 바꾼다.
	 */
	public StoryEntity createNewStory(String title, String theme) {
		currentStory = new Story(title, theme);
		currentStoryKey = ofy().save().entity(currentStory).now();
		return EntityFactory.createEntity(currentStory);
	}

	/**
	 * 스토리를 삭제한다. 
	 * 스토리의 모든 요소를 지우므로 클라이언트에서 충분한 확인을 거치고 서비스를 불러야 한다.
	 * @param storyId
	 */
	public void deleteStory(Long storyId) {
		Story story = ofy().load().type(Story.class).id(storyId).now();
		// raise exception when storyId is wrong...
		if (story == null) throw new java.lang.NullPointerException("There's no story having ID " + storyId);
		if (story == currentStory) throw new java.lang.IllegalArgumentException("Current working story cannot be deleted!");
		List<Object> list = ofy().load().ancestor(story).list();
		ofy().delete().entities(list).now();
		ofy().delete().entity(story);
	}
	
	/*
	 * 데이터스토어 요소들을 불러온다.
	 */
	public LocalEntity[] loadEntities(String type) {
		Class<?> entityClass = getEntityClass(type);
		List<?> list = ofy().load().type(entityClass).ancestor(currentStory).list();
		ArrayList<LocalEntity> result = new ArrayList<LocalEntity>();
		for (Object e : list) {
			LocalEntity le = EntityFactory.getEntity(entityClass, e);
			if (le != null) result.add(le);
			else ofy().delete().entity(e);
		}
		return result.toArray(getLocalEntityArray(type));
	}
	
	/*
	 * 반드시 저장할 것들만 골라서 보낸다.
	 * 모든 엔티티는 서버에서 만들어서 보내므로 객체가 달라도 동일한 엔티티로 취급되기를 바란다.
	 * 또한 클라이언트에서 여러 자료 저장을 동기화하는 코드를 간결하게 하기 위해 null 인자를 허용한다.
	 * 혹시 잘못된 인자가 넘어와서 없으면 일단 그냥 넘긴다.
	 */
	void saveEntities(Class<?> entityClass, LocalEntity[] entities) {
		if (entities==null || entities.length==0) return;
		ArrayList<Long> ids = new ArrayList<Long>();
		for (LocalEntity entity : entities) {
			ids.add(entity.getId());
		}
		ArrayList<Object> list = new ArrayList<Object>();
		Map<Long, ?> map = ofy().load().type(entityClass).parent(currentStoryKey).ids(ids);
		for (LocalEntity from : entities) {
			Object entity = map.get(from.getId());
			if (entity == null) EntityFactory.remove(entityClass, from.getId());
			else list.add(EntityFactory.update(entity, from));
		}
		ofy().save().entities(list).now();
	}

	void saveEntity(Class<?> entityClass, LocalEntity clientEntity) {
		Object entity = ofy().load().type(entityClass).parent(currentStoryKey).id(clientEntity.getId()).now();
		entity = EntityFactory.update(entity, clientEntity);
		ofy().save().entity(entity).now();
	}

	/*
	 * 새 요소를 만드는데 객체를 만들어 반환한다.
	 * 새 요소의 이름을 받는데 null이나 빈 문자열이면 서버 쪽 일련번호를 붙여 이름을 만든다.
	 * FIXME: 만약 메모리 누수가 나타나면 임시 객체를 청소해야 하므로 살펴볼 것!
	 */
	public CharacterEntity createNewCharacter(String name) {
		Character character = EntityFactory.create(Character.class);
		character.setStory(currentStoryKey);
		if (name==null || !name.isEmpty()) character.setName(name);
		ofy().save().entity(character).now();
		CharacterEntity entity = (CharacterEntity)EntityFactory.createEntity(Character.class, character.getId());
		entity.setName(character.getName());
		return entity;
	}
	
	public EventEntity createNewEvent(String name) {
		Event event = EntityFactory.create(Event.class);
		event.setStory(currentStoryKey);
		if (name==null || !name.isEmpty()) event.setName(name);
		ofy().save().entity(event).now();
		EventEntity entity = (EventEntity) EntityFactory.createEntity(Event.class, event.getId());
		entity.setName(event.getName());
		return entity;
	}
	
	public EventRelationEntity createNewEventRelation() {
		EventRelation relation = EntityFactory.create(EventRelation.class);
		relation.setStory(currentStoryKey);
		ofy().save().entity(relation).now();
		return (EventRelationEntity)EntityFactory.createEntity(EventRelation.class, relation.getId());
	}
	
	public InformationEntity createNewInformation(String name) {
		Information info = EntityFactory.create(Information.class);
		info.setStory(currentStoryKey);
		if (name==null || !name.isEmpty()) info.setName(name);
		ofy().save().entity(info).now();
		InformationEntity entity = (InformationEntity) EntityFactory.createEntity(Information.class, info.getId());
		entity.setName(info.getName());
		return entity;
	}
	
	public KnowledgeEntity createNewKnowledge(String name) {
		Knowledge k = EntityFactory.create(Knowledge.class);
		k.setStory(currentStoryKey);
		if (name==null || !name.isEmpty()) k.setName(name);
		ofy().save().entity(k).now();
		KnowledgeEntity entity = (KnowledgeEntity) EntityFactory.createEntity(Knowledge.class, k.getId());
		entity.setName(k.getName());
		return entity;
	}
	
	public PerceptionEntity createNewPerception() {
		Perception p = EntityFactory.create(Perception.class);
		p.setStory(currentStoryKey);
		ofy().save().entity(p).now();
		return (PerceptionEntity)EntityFactory.createEntity(Perception.class, p.getId());
	}
	
	public ImpactEntity createNewImpact() {
		Impact s = EntityFactory.create(Impact.class);
		s.setStory(currentStoryKey);
		ofy().save().entity(s).now();
		return (ImpactEntity)EntityFactory.createEntity(Impact.class, s.getId());
	}
	
	/*
	 * 스토리 시간에 대한 것들  
	 */
	public ArrayList<Integer> loadStoryTime() {
		StoryTime s = currentStory.getStoryTime();
		if (s == null) {
			s = new StoryTime();
			s.setStory(currentStoryKey);
			ofy().save().entity(s).now();
			currentStory.setStoryTime(s);
			ofy().save().entity(currentStory);
		}
		return s.getOrdinalPoints();
	}
	public HashMap<String,Integer> loadStoryTimeAnnotation() {
		StoryTime s = currentStory.getStoryTime();
		if (s == null) {
			s = new StoryTime();
			s.setStory(currentStoryKey);
			ofy().save().entity(s).now();
			currentStory.setStoryTime(s);
			ofy().save().entity(currentStory).now();
		}
		return s.getAnnotations();
	}
	public void saveStoryTime(ArrayList<Integer> ordinalPoints, HashMap<String,Integer> annotations) {
		if (ordinalPoints==null || ordinalPoints.size()==0) return;
		StoryTime s = currentStory.getStoryTime();
		s.update(ordinalPoints, annotations);
		ofy().save().entity(s).now();
	}

		@Override
	public void saveEntity(String type, LocalEntity entity) {
		Class<?> entityClass = getEntityClass(type);
		if (entityClass != null) saveEntity(entityClass, entity);
	}

	@Override
	public void saveEntities(String type, LocalEntity[] entities) {
		Class<?> entityClass = getEntityClass(type);
		if (entityClass != null) saveEntities(entityClass, entities);
	}

	/*
	 * 지울 것들..
	 * 연결 요소는 클라이언트에서 파악해서 전부 삭제 서비스를 요청토록 하므로
	 * 여기서는 해당 객체들만 지운다.
	 */
	
	void deleteEntities(Class<?> entityClass, Long[] ids) {
		Collection<?> entities = ofy().load().type(entityClass).parent(currentStoryKey).ids(ids).values();
		ofy().delete().entities(entities);
	}
	void deleteEntity(Class<?> entityClass, long id) {
		Object entity = ofy().load().type(entityClass).parent(currentStoryKey).id(id).now();
		ofy().delete().entity(entity);
	}

	@Override
	public void deleteEntity(String type, long id) {
		Class<?> entityClass = getEntityClass(type);
		if (entityClass != null) deleteEntity(entityClass, id);
	}

	@Override
	public void deleteEntities(String type, Long[] ids) {
		Class<?> entityClass = getEntityClass(type);
		if (entityClass != null) deleteEntities(entityClass, ids);
	}

}

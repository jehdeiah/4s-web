package com.googlecode._4s_web.server.entity;

import static com.googlecode._4s_web.server.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.EventRelationEntity;
import com.googlecode._4s_web.client.entity.InformationEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalEntity;
import com.googlecode._4s_web.client.entity.PerceptionEntity;
import com.googlecode._4s_web.client.entity.StoryEntity;
import com.googlecode._4s_web.client.entity.ImpactEntity;
import com.googlecode._4s_web.server.entity.HasValidation.ValidationResult;
import com.googlecode.objectify.Key;

/**
 * 데이터스토어 엔티티에 대응하는 클라이언트 엔티티를 만들고, 
 * 클라이언트 엔티티로부터 데이터스토어 엔티티를 갱신하는 팩토리에 대한 인터페이스<br>
 * 생성한 엔티티를 모두 담아두는 맵을 캐시로 가진다.
 * @author jehdeiah
 *
 * @param <D> 데이터스토어 엔티티 자료형
 * @param <E> 클라이언트 엔티티 자료형
 */
interface FactoryEntity<D, E>  {
	public E newClientInstance(long id);
	public E getClientInstance(D entity);
	public D update(D entity, E from);
	public void remove(long id);
	public void flush();
}

/**
 * 클라이언트 팩토리와 같은 패키지가 아니라서 protected 접근이 안 된다.
 * 그러므로 클라이언트 팩토리를 상속받아 서버 패키지에서 접근을 위한 인터페이스로 쓴다.
 * @author jehdeiah
 *
 */
class ClientEntityFactory extends com.googlecode._4s_web.client.entity.EntityFactory {
	protected static <T extends LocalEntity> T create(Class<T> clazz, long id) {
		return (T) newInstance(clazz, id);
	}
	protected static StoryEntity createStory(long id) {
		return newStoryInstance(id);
	}
}

/*
 * 엔티티별 팩토리 클래스 
 * 
 * 엔티티를 새로 추가하면 팩토리 클래스도 만들어야 한다.
 */

class FactoryStory implements FactoryEntity<Story,StoryEntity> {
	private static Map<Long, StoryEntity> bag = new HashMap<Long, StoryEntity>();

	@Override 
	public StoryEntity newClientInstance(long id) {
		StoryEntity se = ClientEntityFactory.createStory(id);
		bag.put(id, se);
		return se;
	}
	
	@Override
	public StoryEntity getClientInstance(Story story) {
		StoryEntity se = bag.get(story.getId());
		if (se == null) {
			se = ClientEntityFactory.createStory(story.getId());
			bag.put(story.getId(), se);
		}
		se.setTitle(story.getTitle());
		se.setTheme(story.getTheme());
		se.setDuration(story.getDuration());
		return se;
	}
	
	@Override
	public Story update(Story story, StoryEntity from)  {
		// ID must be identical!
		if (story.id == from.getId()) {
			story.setTitle(from.getTitle());
			story.setTheme(from.getTheme());
			story.setDuration(from.getDuration());
		}
		return story;
	}
	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}
}

class FactoryCharacter implements FactoryEntity<Character, CharacterEntity> {
	private static Map<Long, CharacterEntity> bag = new HashMap<Long, CharacterEntity>();

	@Override 
	public CharacterEntity newClientInstance(long id) {
		CharacterEntity e = ClientEntityFactory.create(CharacterEntity.class, id);
		bag.put(id, e);
		return e;
	}
	
	@Override
	public CharacterEntity getClientInstance(Character character) {
		CharacterEntity ce = bag.get(character);
		if (ce == null) {
			ce = ClientEntityFactory.create(CharacterEntity.class, character.getId());
			bag.put(character.getId(), ce);
		}
		ce.setName(character.getName());
		ce.setColor(character.getColor());
		ce.setIndex(character.getIndex());
		ce.setGroup(character.getGroup());
		return ce;
	}
	
	@Override
	public Character update(Character character, CharacterEntity from) {
		if (character.id == from.getId()) {
			character.setName(from.getName());
			character.setColor(from.getColor());
			character.setIndex(from.getIndex());
			character.setGroup(from.getGroup());
		}
		return character;
	}
	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}
}

class FactoryEvent implements FactoryEntity<Event,EventEntity> {
	private static Map<Long,EventEntity> bag = new HashMap<Long,EventEntity>();
	
	@Override 
	public EventEntity newClientInstance(long id) {
		EventEntity e = ClientEntityFactory.create(EventEntity.class, id);
		bag.put(id, e);
		return e;
	}
	
	@Override
	public EventEntity getClientInstance(Event event) {
		ValidationResult result = event.validate();
		if (result == ValidationResult.Invalid) {
			ofy().delete().entity(event).now();
			return null;
		}
		if (result == ValidationResult.ChangedAndValid) {
			ofy().save().entity(event).now();
		}
		EventEntity ee = bag.get(event.getId());
		if (ee == null) {
			ee = ClientEntityFactory.create(EventEntity.class, event.getId());
			bag.put(event.getId(), ee);
		}
		ee.setName(event.getName());
		Character main = event.getMainCharacter();
		Collection<Character> involved = event.getInvolvedCharacters();
		ee.setMainCharacter(main==null ? -1 : main.getId());
		if (involved != null) {
			Collection<Long> ids = ee.getInvolvedCharacters();
			for (Character c : involved) 
				if (c != null) ids.add(c.getId());
		}
		ee.setOrdinalStoryInOut(event.getOrdinalStoryIn(), event.getOrdinalStoryOut());
		ee.getDiscourseInOut().clear();
		ee.getDiscourseInOut().addAll(event.getDiscourseInOut());
		return ee;
	}

	@Override
	public Event update(Event event, EventEntity from) {
		if (event.id == from.getId()) {
			event.setName(from.getName());
			Character main = from.getMainCharacter()==-1 ? null : ofy().load().type(Character.class).parent(event.story).id(from.getMainCharacter()).now();
			event.setMainCharacter(main);
			Collection<Character> involved = ofy().load().type(Character.class).parent(event.story).ids(from.getInvolvedCharacters()).values();
			event.setInvolvedCharacters(involved);
			event.ordinalStoryIn = from.getOrdinalStoryIn();
			event.ordinalStoryOut = from.getOrdinalStoryOut();
			event.discourseInOut.clear();
			event.discourseInOut.addAll(from.getDiscourseInOut());
		}
		return event;
	}
	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}
}

class FactoryInformation implements FactoryEntity<Information,InformationEntity> {
	private static Map<Long,InformationEntity> bag = new HashMap<Long,InformationEntity>();

	@Override 
	public InformationEntity newClientInstance(long id) {
		InformationEntity e = ClientEntityFactory.create(InformationEntity.class, id);
		bag.put(id, e);
		return e;
	}
	
	@Override
	public InformationEntity getClientInstance(Information entity) {
		InformationEntity ie = bag.get(entity.getId());
		if (ie == null) {
			ie = ClientEntityFactory.create(InformationEntity.class, entity.getId());
			bag.put(entity.getId(), ie);
		}
		ie.setName(entity.getName());
		ie.setDescription(entity.getDescription());
		return ie;
	}

	@Override
	public Information update(Information entity, InformationEntity from) {
		if (entity.id == from.getId()) {
			entity.setName(from.getName());
			entity.setDescription(from.getDescription());
		}
		return entity;
	}
	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}

}

class FactoryKnowledge implements FactoryEntity<Knowledge,KnowledgeEntity> {
	private static Map<Long,KnowledgeEntity> bag = new HashMap<Long,KnowledgeEntity>();

	@Override 
	public KnowledgeEntity newClientInstance(long id) {
		KnowledgeEntity e = ClientEntityFactory.create(KnowledgeEntity.class, id);
		bag.put(id, e);
		return e;
	}
	
	@Override
	public KnowledgeEntity getClientInstance(Knowledge entity) {
		KnowledgeEntity ke = bag.get(entity.getId());
		if (ke == null) {
			ke = ClientEntityFactory.create(KnowledgeEntity.class, entity.getId());
			bag.put(entity.getId(), ke);
		}
		ke.setName(entity.getName());
		ke.setDescription(entity.getDescription());
		ke.setTruth(entity.isTruth());
		return ke;
	}

	@Override
	public Knowledge update(Knowledge entity, KnowledgeEntity from) {
		if (entity.id == from.getId()) {
			entity.setName(from.getName());
			entity.setDescription(from.getDescription());
			entity.setTruth(from.getTruth());
		}
		return entity;
	}
	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}

}

class FactoryPerception implements FactoryEntity<Perception,PerceptionEntity> {
	private static Map<Long,PerceptionEntity> bag = new HashMap<Long,PerceptionEntity>();

	@Override 
	public PerceptionEntity newClientInstance(long id) {
		PerceptionEntity e = ClientEntityFactory.create(PerceptionEntity.class, id);
		bag.put(id, e);
		return e;
	}
	
	@Override
	public PerceptionEntity getClientInstance(Perception entity) {
		ValidationResult result = entity.validate();
		if (result == ValidationResult.Invalid) {
			ofy().delete().entity(entity).now();
			return null;
		}
		if (result == ValidationResult.ChangedAndValid) {
			ofy().save().entity(entity).now();
		}
		PerceptionEntity e = bag.get(entity.getId());
		if (e == null) {
			e = ClientEntityFactory.create(PerceptionEntity.class, entity.getId());
			bag.put(entity.getId(), e);
		}
		try {
			e.setEvent(entity.getEvent().getId());
			e.setInformation(entity.getInformation().getId());
			Map<Key<Character>,Float> values = entity.getCharacterPerceptValues();
			Map<Key<Character>,Character> characters = ofy().load().keys(values.keySet());
			Map<Long,Float> perceptValues = e.getCharacterPerceptValues();
			for (Map.Entry<Key<Character>, Float> v : values.entrySet()) {
				Character c = characters.get(v.getKey());
				if (c != null) perceptValues.put(c.getId(), v.getValue());
			}
			e.getReaderPerceptValues().addAll(entity.getReaderPerceptValues());
		} catch (NullPointerException ne) {
			if (e != null) bag.remove(entity.getId());
			return null;
		}
		return e;
	}

	@Override
	public Perception update(Perception entity, PerceptionEntity from) {
		if (entity.getId() == from.getId()) {
			Event event = ofy().load().type(Event.class).parent(entity.story).id(from.getEvent()).now();
			Information info = ofy().load().type(Information.class).parent(entity.story).id(from.getInformation()).now();
			entity.setEvent(event);
			entity.setInformation(info);
			Map<Key<Character>,Float> values = entity.getCharacterPerceptValues();
			Map<Long,Float> mapValues = from.getCharacterPerceptValues();
			Collection<Character> agents = ofy().load().type(Character.class).parent(entity.story).ids(mapValues.keySet()).values();
			values.clear();
			for (Character c : agents) {
				values.put(Key.create(c), mapValues.get(c.getId()));
			}
			ArrayList<Float> readerValues = entity.getReaderPerceptValues();
			readerValues.clear();
			readerValues.addAll(from.getReaderPerceptValues());
		}
		return entity;
	}
	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}

}

class FactoryImpact implements FactoryEntity<Impact,ImpactEntity> {
	private static Map<Long,ImpactEntity> bag = new HashMap<Long,ImpactEntity>();

	@Override 
	public ImpactEntity newClientInstance(long id) {
		ImpactEntity e = ClientEntityFactory.create(ImpactEntity.class, id);
		bag.put(id, e);
		return e;
	}
	
	@Override
	public ImpactEntity getClientInstance(Impact entity) {
		ValidationResult result = entity.validate();
		if (result == ValidationResult.Invalid) {
			ofy().delete().entity(entity).now();
			return null;
		}
		if (result == ValidationResult.ChangedAndValid) {
			ofy().save().entity(entity).now();
		}
		ImpactEntity se = bag.get(entity.getId());
		if (se == null) {
			se = ClientEntityFactory.create(ImpactEntity.class, entity.getId());
			bag.put(entity.getId(), se);
		}
		try {
			se.setInformation(entity.getInformation().getId());
			se.setKnowledge(entity.getKnowledge().getId());
			se.setBelief(entity.isBelief());
			se.setImpactValue(entity.getImpactValue());
		} catch (NullPointerException ne) {
			if (se != null) bag.remove(entity.getId());
			return null;
		}
		return se;
	}

	@Override
	public Impact update(Impact entity, ImpactEntity from) {
		if (entity.getId() == from.getId()) {
			Information info = ofy().load().type(Information.class).parent(entity.story).id(from.getInformation()).now();
			Knowledge k = ofy().load().type(Knowledge.class).parent(entity.story).id(from.getKnowledge()).now();
			entity.setInformation(info);
			entity.setKnowledge(k);
			entity.setBelief(from.getBelief());
			entity.setImpactValue(from.getImpactValue());
		}
		return entity;
	}

	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}

}

class FactoryEventRelation implements FactoryEntity<EventRelation,EventRelationEntity> {
	private static Map<Long,EventRelationEntity> bag = new HashMap<Long,EventRelationEntity>();

	@Override 
	public EventRelationEntity newClientInstance(long id) {
		EventRelationEntity e = ClientEntityFactory.create(EventRelationEntity.class, id);
		bag.put(id, e);
		return e;
	}
	
	@Override
	public EventRelationEntity getClientInstance(EventRelation entity) {
		ValidationResult result = entity.validate();
		if (result == ValidationResult.Invalid) {
			ofy().delete().entity(entity).now();
			return null;
		}
		EventRelationEntity re = bag.get(entity.getId());
		if (re == null) {
			re = ClientEntityFactory.create(EventRelationEntity.class, entity.getId());
			bag.put(entity.getId(), re);
		}
		try {
			re.setType(entity.getType());
			re.setFromEvent(entity.getFromEvent().getId());
			re.setToEvent(entity.getToEvent().getId());
			re.setDescription(entity.getDescription());
		} catch (NullPointerException ne) {
			if (re != null) bag.remove(entity.getId());
			return null;
		}
		return re;
	}

	@Override
	public EventRelation update(EventRelation entity, EventRelationEntity from) {
		if (entity.getId() == from.getId()) {
			Event fromEvent = ofy().load().type(Event.class).parent(entity.story).id(from.getFromEvent()).now();
			Event toEvent = ofy().load().type(Event.class).parent(entity.story).id(from.getToEvent()).now();
			entity.setType(from.getType());
			entity.setFromEvent(fromEvent);
			entity.setToEvent(toEvent);
			entity.setDescription(from.getDescription());
		}
		return entity;
	}

	
	@Override
	public void remove(long id) {
		bag.remove(id);
	}
	@Override
	public void flush() {
		bag.clear();
	}

}

/**
 * 서버에서 클라이언트 엔티티를 만들고 취급하는 엔티티 팩토리
 * 
 * @author jehdeiah
 *
 */
public class EntityFactory {
	
	static HashMap<Class<?>, FactoryEntity<?,?>> factoryBag = 
			new HashMap<Class<?>, FactoryEntity<?,?>>();
	
	static void register(Class<?> entityClass, FactoryEntity<?,?> factory) {
		factoryBag.put(entityClass, factory);
	}
	
	/*
	 * 클라이언트 객체를 만들기 위한 엔티티 팩토리 등록
	 * 
	 * 엔티티를 새로 만들면 여기에 추가하고, 아래 update()에도 추가한다.
	 */
	private static FactoryStory factoryStory = new FactoryStory();
	static {
//		register(Story.class, new FactoryStory());
		register(Character.class, new FactoryCharacter());
		register(Event.class, new FactoryEvent());
		register(EventRelation.class, new FactoryEventRelation());
		register(Information.class, new FactoryInformation());
		register(Knowledge.class, new FactoryKnowledge());
		register(Perception.class, new FactoryPerception());
		register(Impact.class, new FactoryImpact());
	}
	
	/*
	 * 새 객체를 만드는데 ID는 보존한다. 다른 속성은 부르는 곳에서 넣어준다.
	 * getEntity()에서는 유효성 검증을 하므로 새로 만드는 것은 createEntity()로 한다. 
	 */
	public static StoryEntity createEntity(Story story) {
		StoryEntity e = factoryStory.newClientInstance(story.getId());
		e.setTitle(story.getTitle());
		e.setTheme(story.getTheme());
		return e;
	}
	
	public static <T> LocalEntity createEntity(Class<T> entityClass, long id) {
		@SuppressWarnings("unchecked")
		FactoryEntity<T,?> factory = (FactoryEntity<T, ?>) factoryBag.get(entityClass);
		if (factory != null) return (LocalEntity)factory.newClientInstance(id);
		return null;	
	}
	
	public static StoryEntity getEntity(Story story) {
		return factoryStory.getClientInstance(story);
	}

	@SuppressWarnings("unchecked")
	public static <T> LocalEntity getEntity(Class<T> entityClass, Object entity) {
		FactoryEntity<T,?> factory = (FactoryEntity<T, ?>) factoryBag.get(entityClass);
		if (factory != null) return (LocalEntity)factory.getClientInstance((T)entity);
		return null;
	}
	
	public static Story update(Story story, StoryEntity from) {
		return factoryStory.update(story, from);
	}
	
	public static Object update(Object entity, LocalEntity from) {
		FactoryEntity<?,?> factory = (FactoryEntity<?, ?>) factoryBag.get(entity.getClass());
		if (factory == null) return entity;
		if ((entity instanceof Character) && (from instanceof CharacterEntity)) {
			return ((FactoryCharacter)factory).update((Character)entity, (CharacterEntity)from);
		}
		if ((entity instanceof Event) && (from instanceof EventEntity)) {
			return ((FactoryEvent)factory).update((Event)entity, (EventEntity)from);
		}
		if ((entity instanceof EventRelation) && (from instanceof EventRelationEntity)) {
			return ((FactoryEventRelation)factory).update((EventRelation)entity, (EventRelationEntity)from);
		}
		if ((entity instanceof Information) && (from instanceof InformationEntity)) {
			return ((FactoryInformation)factory).update((Information)entity, (InformationEntity)from);
		}
		if ((entity instanceof Knowledge) && (from instanceof KnowledgeEntity)) {
			return ((FactoryKnowledge)factory).update((Knowledge)entity, (KnowledgeEntity)from);
		}
		if ((entity instanceof Perception) && (from instanceof PerceptionEntity)) {
			return ((FactoryPerception)factory).update((Perception)entity, (PerceptionEntity)from);
		}
		if ((entity instanceof Impact) && (from instanceof ImpactEntity)) {
			return ((FactoryImpact)factory).update((Impact)entity, (ImpactEntity)from);
		}
		return null;
	}
	
	public static Story createStory() {
		Story story = new Story();
		return story;
	}
	public static void removeStory(long id) {
		factoryStory.remove(id);
	}
	
	public static <T> T create(Class<T> entityClass) {
		try {
			return entityClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void remove(Class<?> entityClass, long id) {
		FactoryEntity<?,?> factory = (FactoryEntity<?, ?>) factoryBag.get(entityClass);
		if (factory != null) factory.remove(id);
	}
	
	public static void flush() {
		//factoryStory.flush();
		for (FactoryEntity<?,?> factory : factoryBag.values()) {
			factory.flush();
		}
	}
}

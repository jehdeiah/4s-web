package com.googlecode._4s_web.client.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 데이터스토어 엔티티들의 로컬 캐시 <br>
 * 자료형에 따라 (식별자, 엔티티) 맵을 구성한다.
 * 자료형을 지정해야 하는 번거로움이 있으나 개별 엔티티에서 정적 요소를 쓰는 것보다
 * 설계와 형상관리 측면에서 이점이 있어 보인다.
 *  
 * @author jehdeiah
 *
 */
public class LocalCache {
	
	/**
	 * 이중 맵으로 자료형에 따라 식별자로 객체를 구분하여 담는다.
	 * Generic으로 특정 자료형에 대한 객체를 생성할 수 없으므로 객체 유형은 추상 클래스인 LocalEntity로 한다.
	 */
	private static Map<Class<? extends LocalEntity>,Map<Long,LocalEntity>> bag
		= new HashMap<Class<? extends LocalEntity>, Map<Long,LocalEntity>>();
	
	/**
	 * 자료형과 식별자로 엔티티 객체를 얻는다.
	 * 
	 * @param entityType
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LocalEntity> T get(Class<T> entityType, long id) {
		Map<Long, LocalEntity> entityBag = bag.get(entityType);
		if (entityBag == null) return null;
		else return (T)entityBag.get(id);
	}
	
	/**
	 * 주어진 자료형의 객체 목록을 얻어온다. 
	 * Generic으로 특정 자료형에 대한 객체를 생성할 수 없으므로 자료형과 배열형 모두 인자로 받아야 한다.
	 * 
	 * @param entityType
	 * @param a
	 * @return
	 */
	public static <T extends LocalEntity> T[] entities(Class<T> entityType, T[] a) {
		Map<Long, LocalEntity> entityBag = bag.get(entityType);
		if (entityBag == null) {
			entityBag = new HashMap<Long,LocalEntity>();
			bag.put(entityType, entityBag);
		}
		return entityBag.values().toArray(a);
	}

	/**
	 * 엔티티를 캐시에서 삭제한다.
	 * @param entity
	 */
	public static <T extends LocalEntity> void delete(T entity) {
		Map<Long, LocalEntity> entityBag = bag.get(entity.getClass());
		if (entityBag != null) entityBag.remove(entity.id);
	}

	/**
	 * 삭제할 엔티티와 모든 참조 객체를 캐시에서 찾아서 삭제하거나 변경이 필요한 엔티티를 담아서 보낸다.
	 * 삭제할 엔티티는 로컬 캐시에서 직접 삭제한다.
	 * 
	 * @param entity		삭제할 엔티티 
	 * @param affected	삭제할 엔티티로 인해 변경된 엔티티 모음
	 * @return 삭제할 엔티티와 함께 삭제할 엔티티 모음. null safe. 
	 */
	public static <T extends LocalEntity> Collection<LocalEntity> deleteAllReferences(T entity, Collection<LocalEntity> affected) {
		assert affected != null;
		ArrayList<LocalEntity> deleted = new ArrayList<LocalEntity>();
		Map<Long, LocalEntity> entityBag = bag.get(entity.getClass());
		if (entityBag != null) {
			if (entityBag.remove(entity.id) == null) return deleted;
			delete(entity);
			deleted.add(entity);
		}
		if (entity instanceof CharacterEntity) {
			for (EventEntity e : entities(EventEntity.class, new EventEntity[0])) {
				if (e.getMainCharacter() == entity.id) {
					e.setMainCharacter(-1);
					affected.add(entity);
				} else if (e.getInvolvedCharacters().remove(entity.id)) {
					affected.add(e);
				}
			}
			for (PerceptionEntity e : entities(PerceptionEntity.class, new PerceptionEntity[0])) {
				if (e.getCharacterPerceptValues().remove(entity.id) != null) {
					affected.add(e);
				}
			}
		} else if (entity instanceof EventEntity) {
			for (EventRelationEntity e : entities(EventRelationEntity.class, new EventRelationEntity[0])) {
				if (e.getFromEvent() == entity.id) deleted.add(e);
				else if (e.getToEvent() == entity.id) deleted.add(e); 
			}
			for (PerceptionEntity e : entities(PerceptionEntity.class, new PerceptionEntity[0])) {
				if (e.getEvent() == entity.id) deleted.addAll(deleteAllReferences(e, affected)); 
			}
		} else if (entity instanceof InformationEntity) {
			for (PerceptionEntity e : entities(PerceptionEntity.class, new PerceptionEntity[0])) {
				if (e.getInformation() == entity.id) deleted.addAll(deleteAllReferences(e, affected));
			}
			for (ImpactEntity e : entities(ImpactEntity.class, new ImpactEntity[0])) {
				if (e.getInformation() == entity.id) deleted.addAll(deleteAllReferences(e,affected));
			}
		} else if (entity instanceof KnowledgeEntity) {
			for (ImpactEntity e : entities(ImpactEntity.class, new ImpactEntity[0])) {
				if (e.getKnowledge() == entity.id) deleted.addAll(deleteAllReferences(e, affected));
			}
		}
		return deleted;
	}

	/**
	 * 엔티티를 캐시에 추가한다.
	 * @param entity
	 */
	public static <T extends LocalEntity> void add(T entity) {
		Map<Long, LocalEntity> entityBag = bag.get(entity.getClass());
		if (entityBag == null) {
			entityBag = new HashMap<Long,LocalEntity>();
			bag.put(entity.getClass(), entityBag);
		}
		entityBag.put(entity.id, entity);
	}

	/**
	 * 특정 엔티티의 개수를 구한다.
	 * @param entityType
	 * @return entityType 자료형의 객체 수
	 */
	public static <T extends LocalEntity> int count(Class<T> entityType) {
		Map<Long, LocalEntity> entityBag = bag.get(entityType);
		return entityBag==null ? 0 : entityBag.size();
	}
	
	/**
	 * 로컬 캐시를 전부 지운다. 작업 스토리를 바꿀 때 쓴다.
	 */
	public static void flush() {
		bag.clear();
	}
}

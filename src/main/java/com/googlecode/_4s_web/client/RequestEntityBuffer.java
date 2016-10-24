package com.googlecode._4s_web.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import com.google.web.bindery.event.shared.EventBus;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.EventRelationEntity;
import com.googlecode._4s_web.client.entity.ImpactEntity;
import com.googlecode._4s_web.client.entity.InformationEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.client.entity.LocalEntity;
import com.googlecode._4s_web.client.entity.PerceptionEntity;
	
class EntityType<T> {
	HashSet<T> buffer;
	int entityKind;
	public EntityType(HashSet<T> buffer, int kind) {
		this.buffer = buffer;
		entityKind = kind;
	}
}

/**
 * 서버에 변경 저장, 삭제를 요청하는 엔티티를 담아두는 버퍼.
 * <p>
 * 전체 클라이언트 화면에서 엔티티 변경 요청을 일괄적으로 이 버퍼를 통해 처리한다.
 * 버퍼에는 변경/삭제할 엔티티 정보를 담고 실제 서버 요청은 메인 모듈에서 하도록 사용자 이벤트를 보낸다.
 *  
 * @author jehdeiah
 *
 */
public class RequestEntityBuffer {
	/**
	 * 커스텀 이벤트 처리를 위한 이벤트 버스 
	 */
	static EventBus storyBus = null;
	
	/**
	 * 데이터스토어에 새로 저장할 변경된 요소들 
	 */
	static HashMap<Class<? extends LocalEntity>, Object> savedBuffer =
			new HashMap<Class<? extends LocalEntity>, Object>();
	/**
	 * 데이터스토어에서 삭제할 요소들 
	 */
	static HashMap<Class<? extends LocalEntity>, Object> deletedBuffer =
			new HashMap<Class<? extends LocalEntity>, Object>();
	
	static <T extends LocalEntity> void register(Class<T> clazz, int entityKind, T[] a) {
		savedBuffer.put(clazz, new EntityType<T>(new HashSet<T>(), entityKind));
		deletedBuffer.put(clazz, new EntityType<Long>(new HashSet<Long>(), entityKind));
		arrayType.put(clazz, a);
	}

	/*
	 * 배열형
	 */
	static HashMap<Class<? extends LocalEntity>,Object> arrayType = 
			new HashMap<Class<? extends LocalEntity>,Object>();
	/*
	 * 저장 순서
	 * (0) 스토리 타임
	 * (1) 캐릭터
	 * (2) 이벤트
	 * (3) 이벤트 관계 연결
	 * (4) 정보
	 * (5) 지식
	 * (6) 사건-정보 인식 연결
	 * (7) 정보-지식 추론 연결 
	 */
	static int[] savingEntityKindOrder = {
		DataSaveEvent.CHARACTER,
		DataSaveEvent.EVENT,
		DataSaveEvent.EVENT_RELATION,
		DataSaveEvent.INFORMATION,
		DataSaveEvent.KNOWLEDGE,
		DataSaveEvent.PERCEPTION,
		DataSaveEvent.IMPACT
	};
	static ArrayList<Class<? extends LocalEntity>> savingEntityOrder = 
			new ArrayList<Class<? extends LocalEntity>>();
	/*
	 * 삭제 순서
	 * (1) 사건-정보 인식 연결
	 * (2) 이벤트 관계 연결
	 * (3) 이벤트
	 * (4) 캐릭터
	 * (5) 정보-지식 추론 연결
	 * (6) 정보
	 * (7) 지식 
	 */
	static int[] deletingEntityKindOrder = {
		DataSaveEvent.PERCEPTION,
		DataSaveEvent.EVENT_RELATION,
		DataSaveEvent.EVENT,
		DataSaveEvent.CHARACTER,
		DataSaveEvent.IMPACT,
		DataSaveEvent.INFORMATION,
		DataSaveEvent.KNOWLEDGE
	};
	static ArrayList<Class<? extends LocalEntity>> deletingEntityOrder = 
			new ArrayList<Class<? extends LocalEntity>>();
	/* 
	 * 엔티티 클래스 등록. 새로 정의되는 엔티티를 여기에 추가한다.
	 */
	static {
		register(CharacterEntity.class, DataSaveEvent.CHARACTER, new CharacterEntity[0]);
		register(EventEntity.class, DataSaveEvent.EVENT, new EventEntity[0]);
		register(EventRelationEntity.class, DataSaveEvent.EVENT_RELATION, new EventRelationEntity[0]);
		register(InformationEntity.class, DataSaveEvent.INFORMATION, new InformationEntity[0]);
		register(KnowledgeEntity.class, DataSaveEvent.KNOWLEDGE, new KnowledgeEntity[0]);
		register(PerceptionEntity.class, DataSaveEvent.PERCEPTION, new PerceptionEntity[0]);
		register(ImpactEntity.class, DataSaveEvent.IMPACT, new ImpactEntity[0]);
		// 저장 순서 등록
		savingEntityOrder.add(CharacterEntity.class);
		savingEntityOrder.add(EventEntity.class);
		savingEntityOrder.add(EventRelationEntity.class);
		savingEntityOrder.add(InformationEntity.class);
		savingEntityOrder.add(KnowledgeEntity.class);
		savingEntityOrder.add(PerceptionEntity.class);
		savingEntityOrder.add(ImpactEntity.class);
		// 삭제 순서 등록
		deletingEntityOrder.add(PerceptionEntity.class);
		deletingEntityOrder.add(EventRelationEntity.class);
		deletingEntityOrder.add(EventEntity.class);
		deletingEntityOrder.add(CharacterEntity.class);
		deletingEntityOrder.add(ImpactEntity.class);
		deletingEntityOrder.add(InformationEntity.class);
		deletingEntityOrder.add(KnowledgeEntity.class);
	}

	/**
	 * 저장한 자료 유형
	 */
	static int savedEntityKind = 0;
	/**
	 * 삭제한 자료 유형
	 */
	static int deletedEntityKind = 0;
	
	public static void initialize(EventBus bus) {
		storyBus = bus;
	}

	/**
	 * 저장할 엔티티 종류를 저장 순서에 맞춰서 반환한다.
	 * @return 엔티티 클래스 대기열
	 */
	public static Queue<Class<? extends LocalEntity>> getSavedEntityQueue() {
		return getSavedEntityQueue(true);		
	}
	public static Queue<Class<? extends LocalEntity>> getSavedEntityQueue(boolean filtered) {
		Queue<Class<? extends LocalEntity>> q = new LinkedList<Class<? extends LocalEntity>>();
		for (int i=0; i<savingEntityKindOrder.length; i++) {
			if (!filtered || (savedEntityKind & savingEntityKindOrder[i])!=0) {
				q.offer(savingEntityOrder.get(i));
			}
		}
		return q;		
	}
	
	/**
	 * 삭제할 엔티티 종류를 삭제 순서에 맞춰서 반환한다.
	 * @return 엔티티 클래스 대기열
	 */
	public static Queue<Class<? extends LocalEntity>> getDeletedEntityQueue() {
		Queue<Class<? extends LocalEntity>> q = new LinkedList<Class<? extends LocalEntity>>();
		for (int i=0; i<deletingEntityKindOrder.length; i++) {
			if ((deletedEntityKind & deletingEntityKindOrder[i]) != 0) {
				q.offer(deletingEntityOrder.get(i));
			}
		}
		return q;
	}
	
	/** 
	 * 스토리 시점들에 변화가 있음을 알린다.
	 */
	public static void saveStoryTime() {
		savedEntityKind |= DataSaveEvent.TIME;
	}
	
	/**
	 * 애노테이션 변경이 있음을 알린다.
	 */
	public static void saveAnnotation() {
		savedEntityKind |= DataSaveEvent.ANNOTATION; 
	}
	
	/**
	 * 저장할 엔티티를 버퍼에 등록한다.
	 * @param entity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LocalEntity> void saveEntity(T entity) {
		EntityType<T> type = (EntityType<T>) savedBuffer.get(entity.getClass());
		type.buffer.add(entity);
		savedEntityKind |= type.entityKind;
	}
	
	/**
	 * 삭제할 엔티티를 버퍼에 등록한다.
	 * 엔티티 삭제는 다른 엔티티에 영향을 미치므로, 같이 삭제되는 것과 변경되는 것을 모두 등록한다.
	 * @param entity
	 */
	public static <T extends LocalEntity> void deleteEntity(T entity) {
		ArrayList<LocalEntity> affected = new ArrayList<LocalEntity>();
		Collection<LocalEntity> deleted = LocalCache.deleteAllReferences(entity, affected);
		for (LocalEntity e : deleted) {
			@SuppressWarnings("unchecked")
			EntityType<Long> type = (EntityType<Long>) deletedBuffer.get(e.getClass());
			type.buffer.add(e.getId());
			deletedEntityKind |= type.entityKind;
		}
		// 변경내용도 저장해야 한다.
		for (LocalEntity e : affected) {
			saveEntity(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends LocalEntity> T[] getSaveBuffer(Class<T> entityClass) {
		EntityType<T> type = (EntityType<T>) savedBuffer.get(entityClass);
		T[] a = (T[])arrayType.get(entityClass);
		return type==null ? null : type.buffer.toArray(a);
	}
	public static Long[] getDeleteBuffer(Class<? extends LocalEntity> entityClass) {
		final Long[] a = new Long[0];
		@SuppressWarnings("unchecked")
		EntityType<Long> type = (EntityType<Long>) deletedBuffer.get(entityClass);
		return type==null ? null : type.buffer.toArray(a);
	}
	
	/**
	 * 실제 서버에 저장/삭제 서비스를 요청한다.
	 */
	public static void flush() {
		DataSaveEvent event = new DataSaveEvent(savedEntityKind, deletedEntityKind);
		storyBus.fireEvent(event);
	}
	
	/**
	 * 모든 요청을 취소하고 버퍼를 비운다.
	 * 이미 서버 요청이 들어간 것은 버퍼 삭제로 저절로 멈추는 데까지 유지한다.
	 */
	public static void clear() {
		for (Object value : savedBuffer.values()) {
			EntityType<?> type = (EntityType<?>)value;
			type.buffer.clear();
		}
		for (Object value : deletedBuffer.values()) {
			EntityType<?> type = (EntityType<?>)value;
			type.buffer.clear();
		}
		savedEntityKind = 0;
		deletedEntityKind = 0;
	}
	
	public static void clearStoryTimeBuffer() {
		savedEntityKind &= (~DataSaveEvent.TIME);
	}
	public static void clearAnnotationBuffer() {
		savedEntityKind &= (~DataSaveEvent.ANNOTATION);
	}

	public static void clearSaveBuffer(Class<? extends LocalEntity> entityClass) {
		EntityType<?> type = (EntityType<?>) savedBuffer.get(entityClass);
		type.buffer.clear();
		savedEntityKind &= ~type.entityKind;
	}

	public static void clearDeleteBuffer(Class<? extends LocalEntity> entityClass) {
		EntityType<?> type = (EntityType<?>) deletedBuffer.get(entityClass);
		type.buffer.clear();
		deletedEntityKind &= ~type.entityKind;
	}
	
}

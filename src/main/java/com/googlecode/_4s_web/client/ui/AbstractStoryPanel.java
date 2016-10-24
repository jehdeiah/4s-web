package com.googlecode._4s_web.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode._4s_web.client.RequestEntityBuffer;
import com.googlecode._4s_web.client.StoryServiceAsync;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.EventRelationEntity;
import com.googlecode._4s_web.client.entity.ImpactEntity;
import com.googlecode._4s_web.client.entity.InformationEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalEntity;
import com.googlecode._4s_web.client.entity.PerceptionEntity;

/**
 * 스토리 앱 모듈에 탭으로 추가될 패널들의 추상 클래스<br>
 * 모듈과의 인터페이스와 클라이언트 UI 패널의 공통 메쏘드를 정의한다.
 * 
 * @author jehdeiah
 *
 */
public abstract class AbstractStoryPanel  extends Composite {
	/**
	 * 서버에 요청하는 서비스 인터페이스
	 */
	final StoryServiceAsync storyService;
	/**
	 * 커스텀 이벤트 처리를 위한 이벤트 버스 
	 */
	final EventBus storyBus;
	
	/*
	 * 배열 변환을 위한 타입 선언용 변수들 
	 */
	final CharacterEntity[] characterArrayType = new CharacterEntity[0];
	final EventEntity[] eventArrayType = new EventEntity[0];
	final EventRelationEntity[] eventRelationArrayType = new EventRelationEntity[0];
	final InformationEntity[] informationArrayType = new InformationEntity[0];
	final KnowledgeEntity[] knowledgeArrayType = new KnowledgeEntity[0];
	final PerceptionEntity[] perceptionArrayType = new PerceptionEntity[0];
	final ImpactEntity[] impacttArrayType = new ImpactEntity[0];

	/**
	 * 스토리 시간 시점들 변화 표시  
	 */
	boolean timepointChanged = false;
	/**
	 * 시간축 어노테이션 변화 표시
	 */
	boolean annotationChanged = false;
	
	protected AbstractStoryPanel() {
		storyService = null;
		storyBus = null;
	}
	
	public AbstractStoryPanel(StoryServiceAsync service, EventBus bus) {
		storyService = service;
		storyBus = bus;
	}
	
	/**
	 * 화면 요소들을 모두 지운다. 스토리 선택이 바뀌거나 할 때 불러 쓴다.
	 */
	public abstract void clear();
	
	/**
	 * 정적 객체로 저장되어 있는 로컬 캐시로부터 패널 내부 자료를 갱신한다.
	 * 로컬 캐시와 데이터스토어는 처음으로 모듈에서 스토리를 열 때와 각 패널에서 저장할 때마다 동기화된다. 
	 * 일반적으로 모듈에서 탭이 바뀔 때 새로 선택한 탭의 메쏘드를 부른다.
	 */
	public abstract void updateAll();
	
	/**
	 * 탭 이동이나 패널을 닫는 등의 작업에서 진행 중이던 UI 작업들을 무효화한다.
	 */
	public abstract void invalidate();
	
	/**
	 * 편집기 등에서 작업한 내용을 로컬 캐시에 반영하고 데이터스토어에 변경 사항을 저장한다.
	 * 일반적으로 모듈에서 탭이 바뀔 때 이전에 선택한 탭의 메쏘드를 부른다.
	 */
	public void applyChanges() {
		updateChangedEntities();
		if (timepointChanged) RequestEntityBuffer.saveStoryTime();
		if (annotationChanged) RequestEntityBuffer.saveAnnotation();
		RequestEntityBuffer.flush();
	}
	
	/**
	 * 엔티티 삭제 요청. 로컬 캐시에서 지우고 삭제 요청 버퍼에 추가한다.
	 * @param entity
	 */
	public void deleteEntity(LocalEntity entity) {
		RequestEntityBuffer.deleteEntity(entity);
		// FIXME: 삭제는 바로 적용?
		applyChanges();
	}
	
	/**
	 * 엔티티 저장 요청.
	 * @param entity
	 */
	public void saveEntity(LocalEntity entity) {
		RequestEntityBuffer.saveEntity(entity);
	}
	
	/**
	 * 변경내용을 선별하여 <code>saveChanges()</code>에서 데이터스토어에 쓰도록 한다.
	 */
	public abstract void updateChangedEntities();

}

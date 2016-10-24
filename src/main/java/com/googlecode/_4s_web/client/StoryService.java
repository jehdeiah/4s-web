package com.googlecode._4s_web.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.EventRelationEntity;
import com.googlecode._4s_web.client.entity.ImpactEntity;
import com.googlecode._4s_web.client.entity.InformationEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalEntity;
import com.googlecode._4s_web.client.entity.PerceptionEntity;
import com.googlecode._4s_web.client.entity.StoryEntity;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("story")
public interface StoryService extends RemoteService {

	/*
	 *  스토리 
	 */
	StoryEntity[] loadStoryHeader();
	
	void setWorkingStory(Long storyId);
	
	void saveStoryHeader(StoryEntity story);
	
	StoryEntity createNewStory(String title, String theme);

	void deleteStory(Long storyId);
	
	/*
	 * 새로 만들기
	 */
	CharacterEntity createNewCharacter(String name);
	EventEntity createNewEvent(String name);
	EventRelationEntity createNewEventRelation();
	InformationEntity createNewInformation(String name);
	KnowledgeEntity createNewKnowledge(String name);
	PerceptionEntity createNewPerception();
	ImpactEntity createNewImpact();

	/*
	 * 불러오기
	 */
	LocalEntity[] loadEntities(String type);

	/* 
	 * 저장하기
	 */
	void saveEntity(String type, LocalEntity entity);
	void saveEntities(String type, LocalEntity[] entities);
	
	/*
	 * 지우기
	 */
	void deleteEntity(String type, long id);
	void deleteEntities(String type, Long[] ids);
		
	/*
	 * 스토리 시간 (시점) 
	 */
	ArrayList<Integer> loadStoryTime();
	
	HashMap<String,Integer> loadStoryTimeAnnotation();
	
	void saveStoryTime(ArrayList<Integer> ordinalPoints, HashMap<String,Integer> annotations);
	

}

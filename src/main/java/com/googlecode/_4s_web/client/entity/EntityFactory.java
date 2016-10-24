package com.googlecode._4s_web.client.entity;

public class EntityFactory {
	@SuppressWarnings("unchecked")
	protected static <T extends LocalEntity> T newInstance(Class<T> clazz, long id) {
		T entity;
		if (clazz.equals(CharacterEntity.class)) {
			entity = (T) new CharacterEntity();
		} else if (clazz.equals(EventEntity.class)) {
			entity = (T) new EventEntity();
		} else if (clazz.equals(InformationEntity.class)) {
			entity = (T) new InformationEntity();
		} else if (clazz.equals(KnowledgeEntity.class)) {
			entity = (T) new KnowledgeEntity();
		} else if (clazz.equals(PerceptionEntity.class)) {
			entity = (T) new PerceptionEntity();
		} else if (clazz.equals(ImpactEntity.class)) {
			entity = (T) new ImpactEntity();
		} else if (clazz.equals(EventRelationEntity.class)) {
			entity = (T) new EventRelationEntity();
		} else {
			return null;
		}
		entity.id = id;
		return entity;
	}
	protected static StoryEntity newStoryInstance(long id) {
		StoryEntity story = new StoryEntity();
		story.id = id;
		return story;
	}
}

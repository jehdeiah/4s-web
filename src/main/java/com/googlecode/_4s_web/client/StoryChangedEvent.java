package com.googlecode._4s_web.client;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.googlecode._4s_web.client.entity.StoryEntity;

/**
 * 스토리가 바뀐 것을 알려주는 이벤트로 스토리 개요에서 모듈로 보낸다.
 * @author jehdeiah
 *
 */
public class StoryChangedEvent extends Event<StoryChangedEvent.Handler> {

	public interface Handler {
		void onChanged(StoryChangedEvent event);
	}
	
	private static final Type<StoryChangedEvent.Handler> TYPE = new Type<StoryChangedEvent.Handler>();

	public static HandlerRegistration register(EventBus eventBus, Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	}
	
	private final StoryEntity story;
	
	public StoryChangedEvent(StoryEntity story) {
		this.story = story;
	}
	
	public StoryEntity getStory() {
		return story;
	}
	
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onChanged(this);
	}

}

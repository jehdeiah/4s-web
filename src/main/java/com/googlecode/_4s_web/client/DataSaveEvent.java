package com.googlecode._4s_web.client;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * 데이터스토어에 저장/삭제를 요청하는 이벤트.
 * 
 * @author jehdeiah
 *
 */
public class DataSaveEvent extends Event<DataSaveEvent.Handler> {
	
	public interface Handler {
		void onRequest(DataSaveEvent event);
	}
	
	private static final Type<DataSaveEvent.Handler> TYPE = new Type<DataSaveEvent.Handler>();

	public static HandlerRegistration register(EventBus eventBus, Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	}
	
	public static final int STORY_INFO = 0x1;	// deprecated!
	public static final int EVENT = 0x10;
	public static final int CHARACTER = 0x20;
	public static final int TIME = 0x40;
	public static final int EVENT_RELATION = 0x80;
	public static final int STORY_TIMELINE = 0xF0;
	public static final int PLOT = 0x100;
	public static final int DISCOURSE_TIMELINE = 0x1F0;
	public static final int INFORMATION = 0x1000;
	public static final int KNOWLEDGE = 0x2000;
	public static final int PERCEPTION = 0x4000;
	public static final int IMPACT = 0x8000;
	public static final int KNOWLEDGE_STRUCTURE = 0xF000;
	public static final int ANNOTATION_TIME = 0x10000;
	public static final int ANNOTATION_PLOT = 0x20000;
	public static final int ANNOTATION = 0x30000;
	
	private final int savedKind;
	private final int deletedKind;
	
	public DataSaveEvent(int savedKind, int deletedKind) {
		this.savedKind = savedKind;
		this.deletedKind = deletedKind;
	}
	
	public int getSavedKind() {
		return savedKind;
	}
	
	public int getDeletedKind() {
		return deletedKind;
	}
	
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onRequest(this);
	}

}

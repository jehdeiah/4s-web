package com.googlecode._4s_web.client.ui;

import com.google.gwt.user.client.ui.SimplePanel;
import com.googlecode._4s_web.client.entity.EventEntity;

/**
 * 이벤트 속성을 보여주는 패널. <br>
 * 다른 요소들과 다르게 이벤트는 여러 편집화면에서 수정할 수 있으므로 공통 속성창을 바탕으로 쓴다.
 * 
 * @author jehdeiah
 *
 */
public class EventPropertyPanel extends PropertyPanel<EventEntity> {

	EventProperty eventProperty;
	HasEventPropertyPanel owner;

	long originalMainCharacter;
	
	public <E,D> EventPropertyPanel(HasEventPropertyPanel p) {
		owner = p;
	}
	
	public long getOriginalCharacter() {
		return originalMainCharacter;
	}
	
	@Override
	void initPanel() {
		setText("Event Properties...");
		property = new SimplePanel();
		eventProperty = new EventProperty();
		property.add(eventProperty);
	}

	@Override
	void updatePanel() {
		if (data != null) {
			eventProperty.setEvent(data);
			originalMainCharacter = data.getMainCharacter();
		}
	}

	@Override
	void saveData() {
		if (data != null) {
			eventProperty.save();
		}
	}

	@Override
	void notifyUpdate(boolean saved) {
		if (saved) {
			saveData();
			owner.updateEventProperties(data);
		}
	}

}

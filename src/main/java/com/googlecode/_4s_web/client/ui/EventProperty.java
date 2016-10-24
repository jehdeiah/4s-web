package com.googlecode._4s_web.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.shared.Interval;

/**
 * 이벤트 속성을 구성하는 패널. 속성 패널 안에 놓인다. <br>
 * 
 * @author jehdeiah, Joonsoo Kim
 *
 */
public class EventProperty extends Composite {

	private static EventPropertyUiBinder uiBinder = GWT
			.create(EventPropertyUiBinder.class);

	interface EventPropertyUiBinder extends
			UiBinder<Widget, EventProperty> {
	}

	EventEntity event;
	
	public EventProperty() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiField
	TextBox name;
	@UiField 
	TextBox place;
	
	@UiField
	Button involveToList, listToInvolve;
	@UiField
	TextArea actionDescription;
	
	@UiField
	ListBox mainCharacterSelect, involvedCharacter, listboxCharacter;

	@UiField
	TextBox storyIn, storyOut;
	@UiField
	ListBox plotIndex;
	@UiField 
	TextBox discourseIn, discourseOut; 
	
	public void setEvent(EventEntity event) {
		this.event = event;
		update();
	}
	
	void update() {
		// 기본 정보
		name.setText(event.getName());
		place.setText(event.getPlace());
		actionDescription.setText(event.getActionDescription());

		// 등장인물 
		mainCharacterSelect.clear();
		listboxCharacter.clear();
		involvedCharacter.clear();

		int numberOfCharacters = LocalCache.count(CharacterEntity.class);
		CharacterEntity[] charArray = new CharacterEntity[numberOfCharacters];
		CharacterEntity[] charSet = LocalCache.entities(CharacterEntity.class, new CharacterEntity[0]);
		
		for (CharacterEntity c : charSet) {
			charArray[c.getIndex()] = c;
		}
		mainCharacterSelect.addItem("(none)", "-1"); // virtual
		for (CharacterEntity c : charArray) {
			String id = Long.toString(c.getId());
			mainCharacterSelect.addItem(c.getName(), id);
			if(c.getId() == event.getMainCharacter()){
				mainCharacterSelect.setSelectedIndex(c.getIndex() + 1);
			}
			for(Long ch : event.getInvolvedCharacters()) {
				if(c.getId()==ch) {
					involvedCharacter.addItem(c.getName(), id);
				}
					
			}
			listboxCharacter.addItem(c.getName(), id);
		}
		involvedCharacter.setVisibleItemCount(charSet.length);
		involvedCharacter.setMultipleSelect(true);
		listboxCharacter.setVisibleItemCount(charSet.length);
		listboxCharacter.setMultipleSelect(true);

		// 시간 정보 
		storyIn.setText(Integer.toString(event.getOrdinalStoryIn()));
		storyOut.setText(Integer.toString(event.getOrdinalStoryOut()));
		
		plotIndex.clear();
		discourseIn.setText("");
		discourseOut.setText("");;
		for (int i=1; i<=event.getOccurrence(); i++) {
			plotIndex.addItem(Integer.toString(i));
		}
		if (event.getOccurrence() > 0) {
			plotIndex.setEnabled(true);
			Interval first = event.getDiscourseInOut().first();
			discourseIn.setText(Double.toString(Math.round(first.getBegin()*100)/100)+"%");
			discourseOut.setText(Double.toString(Math.round(first.getEnd()*100)/100)+"%");
		} else {
			plotIndex.setEnabled(false);
		}
	}

	void save() {
		if (event == null) return;
		event.setName(name.getText());
		event.setPlace(place.getText());
		event.setActionDescription(actionDescription.getText());
		long cID = Long.parseLong(mainCharacterSelect.getValue(mainCharacterSelect.getSelectedIndex()));
		event.setMainCharacter(cID);
		ArrayList<Long> ids = new ArrayList<Long>();
		for (int i=0; i<involvedCharacter.getItemCount(); i++) {
			ids.add(Long.parseLong(involvedCharacter.getValue(i)));
		}
		event.setInvolvedCharacters(ids);
	}
	
	@UiHandler("involveToList")
	void onClickCharacterRemove(ClickEvent e) {
		for(int i=0; i<involvedCharacter.getItemCount();i++) {
			if(involvedCharacter.isItemSelected(i)){
				involvedCharacter.removeItem(i);
			}
		}
	}
	
	@UiHandler("listToInvolve")
	void onClickCharacterAdd(ClickEvent e) {
		Collection<Long> involved = event.getInvolvedCharacters();
		for(int i=0; i<listboxCharacter.getItemCount();i++) {
			if(listboxCharacter.isItemSelected(i) && !involved.contains(Long.parseLong(listboxCharacter.getValue(i)))){
				involvedCharacter.addItem(listboxCharacter.getItemText(i), listboxCharacter.getValue(i));
			}
		}
	}
	
	@UiHandler("plotIndex")
	void onChangePlotIndex(ChangeEvent e) {
		int index = plotIndex.getSelectedIndex();
		Iterator<Interval> iter = event.getDiscourseInOut().iterator();
		for (int i=0; i<index; i++)
			iter.next();
		Interval data = iter.next();
		discourseIn.setText(Double.toString(Math.round(data.getBegin()*100)/100)+"%");
		discourseOut.setText(Double.toString(Math.round(data.getEnd()*100)/100)+"%");

	}
}

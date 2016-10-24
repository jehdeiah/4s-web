package com.googlecode._4s_web.client.ui;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.LocalCache;

/**
 * 등장인물 속성창
 * 
 * @author jehdeiah
 *
 */
class CharacterPropertyPanel extends PropertyPanel<CharacterEntity> {

	/**
	 * 색깔을 고르는 자바 스크립트 실행기 
	 */
	public static native void installJSColor() /*-{
		$wnd.jscolor.init();
	}-*/;

	HasCharacterPropertyPanel owner;
	
	public <E,D> CharacterPropertyPanel(HasCharacterPropertyPanel p) {
		owner = p;
	}

	TextBox name;
	TextBox color;
	ListBox index;
	
	@Override
	void initPanel() {
		property = new VerticalPanel();
		name = new TextBox();
		index = new ListBox();
		property.add(new Label("Name : "));
		property.add(name);
		property.add(new Label("Color : "));
		color = new TextBox();
		color.setReadOnly(true);
		color.getElement().setClassName("color");
		color.addAttachHandler(new AttachEvent.Handler() {
			
			@Override
			public void onAttachOrDetach(AttachEvent arg0) {
				if (arg0.isAttached()) {
					installJSColor();
				}
			}
		}); 
		property.add(color);
		property.add(new Label("Timeline Index :"));
		property.add(index);
		if (!(owner instanceof StoryTimeline)) {
			index.setEnabled(false);
		}
	}
	
	@Override
	void updatePanel() {
		if (data != null) {
			name.setText(data.getName());
			color.setText(CssUtil.getHexRGB(data.getColor()));
			color.getElement().getStyle().setBackgroundColor(data.getColor());
			index.clear();
			int nCharacters = LocalCache.count(CharacterEntity.class);
			for (int i=1; i<=nCharacters; i++) {
				index.addItem(Integer.toString(i));
			}
			index.setItemSelected(data.getIndex(), true);
		}
	}

	@Override
	void saveData() {
		if (data != null) {
			data.setName(name.getText());
			data.setColor(color.getElement().getStyle().getBackgroundColor()); 
			// FIXME: 타임라인 인덱스는 전체가 영향을 받으므로 편집기에서 해야 했는데...
			data.setIndex(index.getSelectedIndex());
		}
	}

	@Override
	void notifyUpdate(boolean saved) {
		if (saved) {
			saveData();
			owner.updateCharacterProperties(data);
		}
	}
	
}
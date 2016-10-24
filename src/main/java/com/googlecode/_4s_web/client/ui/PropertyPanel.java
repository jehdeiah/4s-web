package com.googlecode._4s_web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 속성값을 표시하는 대화상자 뼈대 
 * 
 * @author jehdeiah
 *
 */
public abstract class PropertyPanel<T> extends DialogBox {
	
	class CaptionWithClose extends DialogBox.CaptionImpl {
	
	}
	
	static final int MODAL_MODE = 0x0100;
	static final int MODALESS_MODE = 0x1000;
	public static final int MODAL_OK_CANCEL = 0x0101;
	public static final int MODALESS_EDIT_CLOSE = 0x1001;
	public static final int MODALESS_OK_CANCEL = 0x01002;
	/**
	 * 속성값을 표시할 요소 
	 */
	T data;
	
	/**
	 * 대화상자에 위젯을 하나만 붙일 수 있으므로 내용을 담는 전체 바탕 패널 
	 */
	Panel container;
	/**
	 * 해당 속성값을 표시할 패널 
	 */
	Panel property;
	/**
	 * 위젯 초기화 여부 표시. <code>onLoad()</code>가 대화상자를 띄울 때마다 불리므로
	 * 객체 재사용을 위해 쓴다.
	 */
	boolean initialized = false;

	int mode = MODAL_OK_CANCEL;
	
	void setMode(int mode) {
		this.mode = mode;
	}
	
	/**
	 * 속성값을 표시할 요소를 지정한다.
	 * @param e
	 */
	void setData(T data) {
		this.data = data;
	}
	/**
	 * 속성값 표시 요소를 반환한다.
	 * @return
	 */
	T getData() {
		return data;
	}
	/**
	 * 해당 속성값을 나타내는 패널을 만든다.
	 */
	abstract void initPanel();

	/**
	 * 패널의 표시값들을 해당 속성값에 맞게 바꾼다.
	 */
	abstract void updatePanel();

	/**
	 * 패널의 표시값들로 해당 속성값을 바꾼다.
	 */
	abstract void saveData();
	/**
	 * 대화상자를 닫을 때 처리를 위한 메쏘드.
	 * @param saved 참이면 OK, 거짓이면 Cancel 버튼을 누른 것이다.
	 */
	abstract void notifyUpdate(boolean saved);

	protected void onLoad() {
		/*
		 * show()를 통해 표시할 때마다 불린다.
		 */
		setModal((mode&MODAL_MODE) != 0);
		if (!initialized) {
			initPanel();
			container = new VerticalPanel();
			if (property != null) container.add(property);
			switch (mode) {
			case MODAL_OK_CANCEL: 
			case MODALESS_OK_CANCEL: {
				Button okButton = new Button("OK");
				Button cancelButton = new Button("Cancel");
				HorizontalPanel hPanel = new HorizontalPanel();
				hPanel.add(cancelButton);
				hPanel.add(okButton);
				container.add(hPanel);
				add(container);
				cancelButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						hide();
						notifyUpdate(false);
					}

				});
				okButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						hide();
						notifyUpdate(true);
					}

				});
			}
			break;
			case MODALESS_EDIT_CLOSE: {
				Button editButton = new Button("Edit");
				Button closeButton = new Button("Close");
				HorizontalPanel hPanel = new HorizontalPanel();
				hPanel.add(editButton);
				hPanel.add(closeButton);
				container.add(hPanel);
				add(container);
				editButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						notifyUpdate(true);
					}

				});
				closeButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						hide();
						notifyUpdate(false);
					}

				});
				
			}
			break;
			}
			initialized = true;
		}
		updatePanel();
	}
}
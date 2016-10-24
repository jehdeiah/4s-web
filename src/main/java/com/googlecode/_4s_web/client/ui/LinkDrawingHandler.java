package com.googlecode._4s_web.client.ui;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * 연결선을 그리기 위한 마우스 핸들러.
 * <p> 연결선은 연결 대상 밖으로 그려지므로 적당한 컨테이너에서 이벤트 처리를 해야 한다.
 * 
 * @author jehdeiah
 *
 */
public class LinkDrawingHandler<T extends WidgetLinker> implements MouseMoveHandler, MouseOutHandler, 
							MouseUpHandler, MouseDownHandler, DoubleClickHandler {

	HasLinkDrawingHandler<T> panel;
	
	private Widget from = null;
	private Widget to = null;
	private boolean editPoint = false;

	public LinkDrawingHandler(HasLinkDrawingHandler<T> panel) {
		this.panel = panel;
	}
	
	public void setFrom(Widget from) {
		this.from = from;
		this.to = null;
	}
	public void setTo(Widget to) {
		this.from = null;
		this.to = to;
	}
	
	public void clear() {
		from = to = null;
	}

	/*
	 * 실제 선그리기의 시작은 연결하려는 위젯에서 불리는데 이미 그려진 선을 고치는 경우는 여기서 처리한다. 
	 */
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		event.preventDefault();
		int x = event.getX();
		int y = event.getY();
		int mask = event.getNativeButton() & NativeEvent.BUTTON_LEFT;
		if (mask == 0) { // 버튼을 놓을 경우 선 그리기를 취소한다.
			panel.finishDrawingConnection();
		} else if (editPoint) { // 그려진 선을 고치는 경우 
			editPoint = false;
			if (from != null) panel.startDrawingConnection(from, x, y);
			else if (to != null) panel.startDrawingConnection(x, y, to);
		} else { // 마우스 이동 중... 
			if (from != null) panel.drawConnection(from, x, y);
			else if (to != null) panel.drawConnection(x, y, to);
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		event.preventDefault(); // 기본 선택 동작 막기 
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		// 중간에 버튼 떼면 취소한다.
		panel.finishDrawingConnection();
	}

	/* 
	 * 연결선을 선택하는 것을 여기서 한다. 
	 */
	@Override
	public void onMouseDown(MouseDownEvent event) {
		int x = event.getX();
		int y = event.getY();
		// edit point
		WidgetLinker selectedLinker = panel.getSelectedLink();
		if (selectedLinker != null) {
			int hit = selectedLinker.hitTestEditPoints(x,y);
			if (hit == -1) {
				editPoint = true;
				to = selectedLinker.to;
				return;
			} else if (hit == 1) {
				editPoint = true;
				from = selectedLinker.from;
				return;
			}
		}
		T picked = panel.pickLink(x, y);
		if (picked != null) {
			if (picked != selectedLinker) {
				if (selectedLinker != null) {
					selectedLinker.update();
				}
				picked.select();
				panel.setSelectedLink(picked);
			}
			if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
				panel.showLinkContextMenu(picked, event.getClientX(), event.getClientY());
			}
			return;
		}
		// 빈 공간을 눌렀으므로 기존 선택을 없앤다.
		if (selectedLinker != null) {
			selectedLinker.update();
		}
		panel.setSelectedLink(null);
	}
	
	/*
	 * 두 번 클릭으로 속성창을 띄운다.
	 */
	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		T picked = panel.pickLink(event.getX(), event.getY());
		WidgetLinker selectedLinker = panel.getSelectedLink();
		if (picked != null) {
			if (picked!=selectedLinker) {
				if (selectedLinker != null) {
					selectedLinker.update();
				}
				picked.select();
				panel.setSelectedLink(picked);
			}
			int popupLeft = event.getClientX();
			int popupTop = event.getClientY();
			panel.showLinkProperties(picked, popupLeft, popupTop);
			return;
		}
		// 빈 공간을 눌렀으므로 기존 선택을 없앤다.
		if (selectedLinker != null) {
			selectedLinker.update();
		}
		panel.setSelectedLink(null);
	}

}

package com.googlecode._4s_web.client.ui;

import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * 이벤트 상자를 움직이게 하는 드래그 핸들러.<br>
 * 각각의 편집기에서 필요한 이벤트 처리기를 구현한다.
 * @author jehdeiah
 *
 */
public abstract class EventDragHandler implements DragStartHandler, DragHandler,
		DragEndHandler {

	/**
	 * 
	 */
	public static final int DRAG_MARGIN = 3;
	int marginLeft = DRAG_MARGIN;
	int marginRight = DRAG_MARGIN;
	int marginTop = DRAG_MARGIN;
	int marginBottom = DRAG_MARGIN;
	Widget widget;
	long eventId;
	boolean plot;

	public EventDragHandler(long id, Widget w) {
		eventId = id;
		widget = w;
		plot = false;
	}

	public EventDragHandler(long id, Widget w, boolean plot) {
		eventId = id;
		widget = w;
		this.plot = plot;
	}

	public void setDragMargin(int top, int bottom, int left,
			int right) {
		marginLeft = left;
		marginRight = right;
		marginTop = top;
		marginBottom = bottom;
	}

	public void onDragStart(DragStartEvent event) {
		int x = event.getNativeEvent().getClientX();
		int y = event.getNativeEvent().getClientY();
		int offsetX = x - widget.getAbsoluteLeft();
		int offsetY = y - widget.getAbsoluteTop();
		int offsetRight = widget.getOffsetWidth() - offsetX;
		int offsetBottom = widget.getOffsetHeight() - offsetY;
		if ((offsetX <= marginLeft) || (offsetRight <= marginRight)
				|| (offsetY <= marginTop) || (offsetBottom <= marginBottom)) {
			event.preventDefault();
			return;
		}
		event.setData("event", Long.toString(eventId));
		event.setData("offset_x", Integer.toString(offsetX));
		event.setData("offset_y", Integer.toString(offsetY));
		event.setData("x", Integer.toString(x));
		event.setData("y", Integer.toString(y));
		event.setData("plot", Boolean.toString(plot));
		notifyDragStart(eventId, offsetX, offsetY);
	}

	public void onDrag(DragEvent event) {
	}

	public void onDragEnd(DragEndEvent event) {
	}
	
	protected abstract void notifyDragStart(long eventId, int offsetX, int offsetY);
}
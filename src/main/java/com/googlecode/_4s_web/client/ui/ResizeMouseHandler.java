package com.googlecode._4s_web.client.ui;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * 위젯의 모서리를 움직여 크기를 변경하는 마우스 이벤트 핸들러.
 * 크기를 변경할 때 마우스 포인터가 위젯 밖으로 나갈 수 있으므로 
 * 위젯을 담고 있는 컨테이너에서 적절한 이벤트 처리를 해야 한다.
 * 크기 변경 상태일 때 <code>resizeElement()</code>만 부르면 되게 하고,
 * 중간에 멈추는 것은 크기 변경을 마무리하는 <code>finishResize()</code>를 부르는 것으로 한다.
 * 
 * @author jehdeiah
 *
 */
public abstract class ResizeMouseHandler implements MouseDownHandler,
		MouseMoveHandler,
		MouseUpHandler {

	Widget widget = null;
	Widget container = null;
	int allowedResizeMode = RESIZE_NONE;
	int resizeMode = RESIZE_NONE;
	
	public static final int RESIZE_NONE = 0x00;
	public static final int RESIZE_ON_TOP = 0x01;
	public static final int RESIZE_ON_BOTTOM = 0x10;
	public static final int RESIZE_VERTICAL = 0x11;
	public static final int RESIZE_ON_LEFT = 0x100;
	public static final int RESIZE_ON_RIGHT = 0x1000;
	public static final int RESIZE_HORIZONTAL = 0x1100;

	final int ResizeMargin = 3;
	final int MIN_WIDGET_SIZE = 5;
	
	public ResizeMouseHandler(Widget w, Widget c) {
		widget = w;
		container = c;
		allowedResizeMode = RESIZE_NONE;
	}

	public ResizeMouseHandler(Widget w, Widget c, int resizeMode) {
		widget = w;
		container = c;
		allowedResizeMode = resizeMode;
	}
	
	public void SetContainer(Widget c) {
		container = c;
	}
	
	public boolean gResizing() {
		return (resizeMode != RESIZE_NONE);
	}
	
	public void onMouseDown(MouseDownEvent event) {
		if (event.getNativeButton() != Event.BUTTON_LEFT) return;
		if ((allowedResizeMode & RESIZE_ON_TOP) != 0 && 
				event.getY() <= ResizeMargin) {
			event.preventDefault();
			resizeMode = RESIZE_ON_TOP;
			widget.getElement().getStyle().setCursor(Cursor.ROW_RESIZE);
		} else if ((allowedResizeMode & RESIZE_ON_BOTTOM) != 0
				&& widget.getOffsetHeight() - event.getY() <= ResizeMargin) {
			event.preventDefault();
			resizeMode = RESIZE_ON_BOTTOM;
			widget.getElement().getStyle().setCursor(Cursor.ROW_RESIZE);
		} else if ((allowedResizeMode & RESIZE_ON_LEFT) != 0
				&&
				event.getX() <= ResizeMargin) {
			event.preventDefault();
			resizeMode = RESIZE_ON_LEFT;
			widget.getElement().getStyle().setCursor(Cursor.COL_RESIZE);
		} else if ((allowedResizeMode & RESIZE_ON_RIGHT) != 0
				&& widget.getOffsetWidth() - event.getX() <= ResizeMargin) {
			event.preventDefault();
			resizeMode = RESIZE_ON_RIGHT;
			widget.getElement().getStyle().setCursor(Cursor.COL_RESIZE);
		}
		if (resizeMode != RESIZE_NONE) {
			notifyResizeStarted();
		}
	}

	public void onMouseMove(MouseMoveEvent event) {
		if (resizeMode != RESIZE_NONE) {
			event.preventDefault();
			/*
			 * 마우스를 움직일 떼 버튼이 안 눌렸으면 크기 조절을 끝내려 했는데,
			 * 움직이는 동안에는 마우스 버튼을 얻어오는 것을 믿을 수 없다.
			 * (안 눌렸는데 눌렸다고 나온다. =.=)
			 * 그러므로 중간에 마우스를 떼고 종료하는 것은 MouseUp으로 다룬다.
			 */
			int	newX = event.getRelativeX(container.getElement());
			int	newY = event.getRelativeY(container.getElement());
			resizeElement(newX, newY);
		} else {
			if ((allowedResizeMode & RESIZE_ON_TOP) != 0
					&& event.getY() <= ResizeMargin) {
				event.preventDefault();
				widget.getElement().getStyle().setCursor(Cursor.ROW_RESIZE);
			} else if ((allowedResizeMode & RESIZE_ON_BOTTOM) != 0
					&& widget.getOffsetHeight() - event.getY() <= ResizeMargin) {
				event.preventDefault();
				widget.getElement().getStyle().setCursor(Cursor.ROW_RESIZE);
			} else if ((allowedResizeMode & RESIZE_ON_LEFT) != 0
					&& event.getX() <= ResizeMargin) {
				event.preventDefault();
				widget.getElement().getStyle().setCursor(Cursor.COL_RESIZE);
			} else if ((allowedResizeMode & RESIZE_ON_RIGHT) != 0
					&& widget.getOffsetWidth() - event.getX() <= ResizeMargin) {
				event.preventDefault();
				widget.getElement().getStyle().setCursor(Cursor.COL_RESIZE);
			} else {
				//event.preventDefault();
				widget.getElement().getStyle().setCursor(Cursor.AUTO);
			}
		}
	}
	
	public void onMouseUp(MouseUpEvent event) {
		if (resizeMode != RESIZE_NONE) {
			if ((event.getNativeButton()&NativeEvent.BUTTON_LEFT) != 0) {
				finishResize();
				event.preventDefault();
			}
		}
	}

	public void finishResize() {
		notifyResizeDone();
		resizeMode = RESIZE_NONE;
		widget.getElement().getStyle()
				.setCursor(Cursor.AUTO);		
	}

	/*
	 * 마우스 포인터 위치에 맞게 위젯 크기를 변경하는데 최소 크기가 안 되면 변경하지 않고 둔다.
	 * 여기서 상하/좌우 역전 현상은 없다. 즉, 안 움직이는 쪽은 고정이다.
	 */
	public void resizeElement(int relativeX, int relativeY) {
		int top = widget.getElement().getOffsetTop();
		int bottom = top + widget.getElement().getOffsetHeight();
		int left = widget.getElement().getOffsetLeft();
		int right = left + widget.getElement().getOffsetWidth();
		switch (resizeMode) {
		case RESIZE_ON_TOP:
			top = relativeY;
			if (top > (bottom - MIN_WIDGET_SIZE)) top = bottom - MIN_WIDGET_SIZE;
			break;
		case RESIZE_ON_BOTTOM:
			bottom = relativeY;
			if (bottom < (top + MIN_WIDGET_SIZE)) bottom = top + MIN_WIDGET_SIZE;
			break;
		case RESIZE_ON_LEFT:
			left = relativeX;
			if (left > (right - MIN_WIDGET_SIZE)) left = right - MIN_WIDGET_SIZE;
			break;
		case RESIZE_ON_RIGHT:
			right = relativeX;
			if (right < (left + MIN_WIDGET_SIZE)) right = left + MIN_WIDGET_SIZE;
			break;
		default:
			break;	
		}
		resizeWidget(top, bottom, left, right, relativeX, relativeY);
	}

	/**
	 * 마우스로 크기를 변경할 때마다 위젯에서 처리할 부분을 적는다.
	 * 
	 * 여기서 처리하지 않으면 실제 위젯의 크기는 변하지 않는다.
	 * 
	 * @param top
	 * @param bottom
	 * @param left
	 * @param right
	 * @param mouseX
	 * @param mouseY
	 */
	protected abstract void resizeWidget(int top, int bottom, int left,
			int right, int mouseX, int mouseY);

	/**
	 * 크기 변경을 시작할 때 불리는 콜백 메쏘드.
	 * 
	 */
	protected abstract void notifyResizeStarted();

	/**
	 * 크기 변경을 완료하고 마우스 버튼을 떼고 불리는 콜백 메쏘드.
	 * 
	 * 크기 변경 후 추가로 처리할 부분을 여기에 적는다.
	 */
	protected abstract void notifyResizeDone();
}
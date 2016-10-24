package com.googlecode._4s_web.client.ui;

import com.google.gwt.user.client.ui.Widget;

/**
 * 연결선을 편집을 허용하는 패널 인터페이스 정의
 * 
 * @author jehdeiah
 *
 */
public interface HasLinkDrawingHandler<T extends WidgetLinker> {
	/** 시작 위젯을 지정하고 선긋기를 시작한다. */
	public void startDrawingConnection(Widget from, int x, int y);
	/** 끝 위젯을 지정하고 선긋기를 시작한다. */
	public void startDrawingConnection(int x, int y, Widget to);
	/** 시작 위젯을 지정하고 시작한 선긋기를 계속 한다. */
	public void drawConnection(Widget from, int x, int y);
	/** 끝 위젯을 지정하고 시작한 선긋기를 계속 한다. */
	public void drawConnection(int x, int y, Widget to);
	/** 현재 그리고 있는 연결선을 끝낸다. */
	public void finishDrawingConnection();
	/** 선택된 연결선 얻기 */
	public T getSelectedLink();
	/** 연결선 선택 지정하기 */
	public void setSelectedLink(T selected);
	/** 좌표에 있는 연결선 고르기 */
	public T pickLink(int x, int y);
	/** 연결선 속성창 띄우기 */
	public void showLinkProperties(T link, int left, int top);
	/** 연결선 메뉴 띄우기 */
	public void showLinkContextMenu(T link, int left, int top);
	
}

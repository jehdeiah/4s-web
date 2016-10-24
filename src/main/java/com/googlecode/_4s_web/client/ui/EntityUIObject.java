package com.googlecode._4s_web.client.ui;

import com.google.gwt.user.client.ui.Widget;

/**
 * 로컬 엔티티를 화면에 표시하는 UI 객체 인터페이스 
 * @author jehdeiah
 *
 * @param <T> 로컬 엔티티 클래스 
 */
public interface EntityUIObject<T> {
	/**
	 * 화면 요소에 연결된 로컬 엔티티를 반환한다.
	 * @return 로컬 엔티티 
	 */
	public T getEntity();
	/**
	 * 화면 요소에 로컬 엔티티를 지정한다.
	 * @param entity 로컬 엔티티 
	 */
	//public void setEntity(T entity);
	/**
	 * 연결된 엔티티의 고유 식별자를 얻는다.
	 * @return
	 */
	public long getId();
	/**
	 * 엔티티를 나타내는 주 화면요소 위젯을 반환한다.
	 * @return 주 화면요소 위젯
	 */
	public Widget getWidget();
	/**
	 * 엔티티 속성이 외부에서 바뀐 것을 반영하여 화면 요소를 갱신한다.
	 */
	public void updateFromEntity();
	/**
	 * 화면 요소의 스타일 등을 갱신한다.
	 */
	public void updateWidget();
	/**
	 * 화면 요소에 대한 조작으로 인해 엔티티 속성이 바뀌었는지 확인한다.
	 * @return
	 */
	public boolean isModified();
	/**
	 * 엔티티 속성이 바뀐 것을 명시적으로 나타낸다.
	 */
	public void modify();
	/**
	 * 엔티티 속성이 바뀐 상태를 무시한다. (바뀐 것을 처리한 다음에 부른다.)
	 */
	public void invalidate();
}

package com.googlecode._4s_web.client.ui;

import com.googlecode._4s_web.client.entity.EventEntity;

/**
 * 이벤트 속성창을 가지는 패널 인터페이스 <br>
 * 이벤트는 여러 편집 화면에서 고칠 수 있으므로 관리를 위해 공통 인터페이스를 활용한다.
 * 
 * @author jehdeiah
 */
public interface HasEventPropertyPanel {
	/**
	 * 이벤트 속성창을 띄운다.
	 * @param element	대상 이벤트를 지칭하는 엔티티 
	 */
	public void showEventProperties(EventEntity event);
	/**
	 * 변경한 이벤트 속성을 반영한다.
	 * @param data	속성 변경이 반영된 엔티티
	 */
	public void updateEventProperties(EventEntity event);
}

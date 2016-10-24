package com.googlecode._4s_web.server.entity;

/**
 * 저장 오류로 연결된 요소를 찾을 수 없을 경우 이것을 유효화하는 메쏘드를 제공한다.
 * 
 * @author jehdeiah
 *
 */

public interface HasValidation {
	public enum ValidationResult { Valid, ChangedAndValid, Invalid };
	/**
	 * 찾을 수 없는 요소들을 제거하여 엔티티 객체를 유효화한다.
	 * @return 유효화 과정에서 객체 변경이 있느면 참, 그대로 유효하면 거짓을 되돌린다.
	 */
	public ValidationResult validate();
}

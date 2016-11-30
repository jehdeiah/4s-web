package com.googlecode._4s_web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class QnAPattern extends Composite {

	private static QnAPatternUiBinder uiBinder = GWT
			.create(QnAPatternUiBinder.class);

	interface QnAPatternUiBinder extends UiBinder<Widget, QnAPattern> {
	}

	public QnAPattern() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}

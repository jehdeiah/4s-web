package com.googlecode._4s_web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class SuspenseSituation extends Composite {

	private static SuspenseSituationUiBinder uiBinder = GWT
			.create(SuspenseSituationUiBinder.class);

	interface SuspenseSituationUiBinder extends
			UiBinder<Widget, SuspenseSituation> {
	}

	public SuspenseSituation() {
		initWidget(uiBinder.createAndBindUi(this));
		resultPanel.setVisible(false);
	}

	@UiField
	Button analyzeButton;
	@UiField
	HTMLPanel resultPanel;
	
	@UiHandler("analyzeButton")
	void onClickAnalyze(ClickEvent e) {
		resultPanel.setVisible(true);
	}
}

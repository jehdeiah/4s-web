package com.googlecode._4s_web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalCache;

public class KnowledgeFlow extends AbstractKnowledgeFlowPanel {

	private static KnowledgeFlowUiBinder uiBinder = GWT
			.create(KnowledgeFlowUiBinder.class);

	interface KnowledgeFlowUiBinder extends UiBinder<Widget, KnowledgeFlow> {
	}

	String h1Title = "Knowledge Flow";

	public KnowledgeFlow() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public KnowledgeFlow(String heading) {
		h1Title = heading;
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	protected void onLoad() {
		heading.setHTML("<h1 style=\"margin:0\">" + h1Title + "</h1>");
	}

	@UiField
	HTML heading;
	@UiField
	ListBox agentList;
	@UiField
	ListBox knowledgeList;
	@UiField
	ListBox orderList;
	
	@UiHandler("agentList")
	void onChangeAgent(ChangeEvent e) {
		run();
	}
	
	@UiHandler("knowledgeList")
	void onChangeKnowledge(ChangeEvent e) {
		run();
	}
	
	@UiHandler("orderList")
	void onChangeOrder(ChangeEvent e) {
		run();
	}
	
	protected void run() {
		int agent = agentList.getSelectedIndex() + 1;
		String order = orderList.getSelectedValue();
		String knList = "";
		for (int i=0; i<knowledgeList.getItemCount(); i++) {
			if (knowledgeList.isItemSelected(i)) knList += (i+1) + ",";
		}
		if (knList.length() > 0) {
			String param = "{ \"agent\":" + agent + "," 
							+ "\"knlist\":[" + knList.substring(0, knList.length() - 1) + "],"
							+ "\"order\":\"" + order + "\" }";
			sendParamToShiny(DOM.asOld(getElement()), param);
		}
	}
	
	public	void analyze() {	
		int index;
		// Fill the list boxes.
		agentList.clear();
		knowledgeList.clear();
		agentList.addItem("Reader");
		index = 1;
		for (CharacterEntity e : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
			agentList.addItem(e.getName());
			agentList.addItem(e.getName() + "|Reader");
			++index;
		}
		index = 1;
		for (KnowledgeEntity e : LocalCache.entities(KnowledgeEntity.class, knowledgeArrayType)) {
			knowledgeList.addItem(e.getName());
			knowledgeList.setItemSelected(index-1, true);
			index++;
		}

		if (sendData(DOM.asOld(getElement()), "kf")) run();
	}

}

package com.googlecode._4s_web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.LocalCache;

public class EntropyComplexity extends AbstractKnowledgeFlowPanel  {

	private static EntropyComplexityUiBinder uiBinder = GWT
			.create(EntropyComplexityUiBinder.class);

	interface EntropyComplexityUiBinder extends UiBinder<Widget, EntropyComplexity> {
	}

	public EntropyComplexity() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	//@UiField
	//ListBox agentList;
	
	public	void analyze() {
		if (!sendData(DOM.asOld(getElement()), "ent.comp")) return;
		
		String readerParam = "\"Reader\"";
		String characterParam = "";
		int index;
		// Fill the list boxes.
		//agentList.clear();
		//agentList.addItem("Reader");
		index = 1;
		for (CharacterEntity e : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
			//agentList.addItem(e.getName());
			characterParam += "\"" + e.getName() + "\",";
			++index;
		}
		if (index > 1) characterParam = characterParam.substring(0, characterParam.length() - 1);
		String param = "{ \"reader\": " + readerParam
					  + ", \"agents\": [" +	 characterParam + "]"
					  + ", \"order\": \"discourse\" }";
		sendParamToShiny(DOM.asOld(getElement()), param);
	}

}

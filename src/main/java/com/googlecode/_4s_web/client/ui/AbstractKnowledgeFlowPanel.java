package com.googlecode._4s_web.client.ui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.ImpactEntity;
import com.googlecode._4s_web.client.entity.InformationEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.client.entity.PerceptionEntity;
import com.googlecode._4s_web.shared.Interval;

public abstract class AbstractKnowledgeFlowPanel extends Composite {

	/**
	 * 자바 스크립트로 입력 자료 만들어서 분석 요청하기  
	 */
	public native void sendDataToShiny(Element thisScope, String type, String data) /*-{
		var shinyframe = thisScope.getElementsByTagName("iframe")[0];
		var msg = "{\"type\": \"" + type + "\", \"data\":" + data + "}";
		shinyframe.contentWindow.postMessage(msg, shinyframe.src);
	}-*/;

	public native void sendParamToShiny(Element thisScope, String param) /*-{
		var shinyframe = thisScope.getElementsByTagName("iframe")[0];
		var msg = "{\"param\":" + param + "}";
		shinyframe.contentWindow.postMessage(msg, shinyframe.src);
	}-*/;

	/*
	 * 캐시에서 읽어들일 엔티티 배열형
	 */
	final EventEntity[] eventArrayType = new EventEntity[0];
	final CharacterEntity[] characterArrayType = new CharacterEntity[0];
	final InformationEntity[] informationArrayType = new InformationEntity[0];
	final KnowledgeEntity[] knowledgeArrayType = new KnowledgeEntity[0];
	final PerceptionEntity[] perceptionArrayType = new PerceptionEntity[0];
	final ImpactEntity[] impactArrayType = new ImpactEntity[0];

	/*
	 * 사건 네트워크 기반 복잡도와 동일하게 이벤트 순서를 매긴다.
	 */
	class SimpleEvent {
		public long index;
		public int storyOrder;
		public double discourseTime;
	};
	
	public	abstract void analyze();
	
	protected boolean sendData(Element scope, String type) {
		String characterParam = "";
		String knowledgeParam = "";
		String knowledgeTypeParam = "";
		String infoParam = "";
		String eventSeq, plotSeq;
		HashMap<Long, Integer> agentMap = new HashMap<Long, Integer>();
		HashMap<Long, Integer> eventMap = new HashMap<Long, Integer>();
		HashMap<Long, Integer> infoMap = new HashMap<Long, Integer>();
		HashMap<Long, Integer> knowledgeMap = new HashMap<Long, Integer>();
		int index;
		characterParam = "\"Reader\"";
		index = 1;
		for (CharacterEntity e : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
			characterParam += ", \"" + e.getName() + "\"";
			++index;
			agentMap.put(e.getId(), index);
		}
		index = 1;
		for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) {
			eventMap.put(e.getId(), index);
			index++;
		}
		index = 1;
		for (InformationEntity e : LocalCache.entities(InformationEntity.class, informationArrayType)) {
			infoParam += index + ",";
			infoMap.put(e.getId(), index);
			index++;
		}
		if (index > 1) infoParam = infoParam.substring(0, infoParam.length() - 1);
		index = 1;
		for (KnowledgeEntity e : LocalCache.entities(KnowledgeEntity.class, knowledgeArrayType)) {
			knowledgeParam += "\"" + e.getName() + "\",";
			knowledgeTypeParam += (e.getTruth() ? 1 : 0) + ",";
			knowledgeMap.put(e.getId(), index);
			index++;
		}
		knowledgeParam = knowledgeParam.substring(0, knowledgeParam.length() - 1);
		knowledgeTypeParam = knowledgeTypeParam.substring(0, knowledgeTypeParam.length() - 1);
		// Check whether the story and knowledge structure is properly constructed.
		if (LocalCache.count(EventEntity.class) == 0 ||
			LocalCache.count(CharacterEntity.class) == 0 ||
			LocalCache.count(InformationEntity.class) == 0 ||
			LocalCache.count(KnowledgeEntity.class) == 0 ||
			LocalCache.count(PerceptionEntity.class) == 0 ||
			LocalCache.count(ImpactEntity.class) == 0) {
			return false;
		}
		// Make the event structure
		SortedSet<SimpleEvent> storySequence = new TreeSet<SimpleEvent>(new Comparator<SimpleEvent>() {
			@Override
			public int compare(SimpleEvent arg0, SimpleEvent arg1) {
				return Integer.compare(arg0.storyOrder, arg1.storyOrder);
			}
		});
		SortedSet<SimpleEvent> plotSequence = new TreeSet<SimpleEvent>(new Comparator<SimpleEvent>() {
			@Override
			public int compare(SimpleEvent arg0, SimpleEvent arg1) {
				return Double.compare(arg0.discourseTime, arg1.discourseTime);
			}
		});
		for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) {
			if (e.isOnStoryTime() && e.isPlotted()){
				for (Interval inter : e.getDiscourseInOut()) {
					SimpleEvent se = new SimpleEvent();
					se.index = eventMap.get(e.getId());
					se.storyOrder = e.getOrdinalStoryIn();
					se.discourseTime = inter.getBegin();
					storySequence.add(se);
					plotSequence.add(se);
				}
			}
		}
		if (storySequence.size() == 0) return false;	// not plotted yet
		else {
			Iterator<SimpleEvent> is = storySequence.iterator();
			Iterator<SimpleEvent> ip = plotSequence.iterator();
			eventSeq = Long.toString(is.next().index);
			plotSeq = Long.toString(ip.next().index);
			while (is.hasNext()) {
				eventSeq += "," + is.next().index;
				plotSeq += "," + ip.next().index;
			}
		}
		String perceptEvent = "";
		String perceptAgent = "";
		String perceptInfo = "";
		String perceptValue = "";
		for (PerceptionEntity e : LocalCache.entities(PerceptionEntity.class, perceptionArrayType)) {
			for (Entry<Long, Float> cv : e.getCharacterPerceptValues().entrySet()) {
				perceptEvent += eventMap.get(e.getEvent()) + ",";
				perceptAgent += agentMap.get(cv.getKey()) + ",";	
				perceptInfo += infoMap.get(e.getInformation()) + ",";
				perceptValue += cv.getValue() + ",";
			}
			perceptEvent += eventMap.get(e.getEvent()) + ",";
			perceptAgent += "1,"; 	// reader of index 1
			perceptInfo += infoMap.get(e.getInformation()) + ",";
			perceptValue += e.getReaderPerceptValues().get(0) + ",";
		}
		if (perceptEvent.length() == 0) return false;
		perceptEvent = perceptEvent.substring(0, perceptEvent.length() - 1);
		perceptAgent = perceptAgent.substring(0, perceptAgent.length() - 1);
		perceptInfo = perceptInfo.substring(0, perceptInfo.length() - 1);
		perceptValue = perceptValue.substring(0, perceptValue.length() - 1);
		String perceptionParam = "{ \"event\": [" + perceptEvent + "], \"agent\": [" + perceptAgent
									+ "], \"info\": [" + perceptInfo + "], \"percept\": [" + perceptValue + "] }";
		String impactInfo = "";
		String impactKnowledge = "";
		String impactValue = "";
		for (ImpactEntity e : LocalCache.entities(ImpactEntity.class, impactArrayType)) {
			impactInfo += infoMap.get(e.getInformation()) + ",";
			impactKnowledge += knowledgeMap.get(e.getKnowledge()) + ",";
			impactValue += e.getEffectiveImpact() + ",";
		}
		if (impactInfo.length() == 0) return false;
		impactInfo = impactInfo.substring(0, impactInfo.length() - 1);
		impactKnowledge = impactKnowledge.substring(0, impactKnowledge.length() -1);
		impactValue = impactValue.substring(0, impactValue.length() - 1);
		String impactParam = "{ \"info\": [" + impactInfo + "], \"knowledge\": [" + impactKnowledge
								+ "], \"impact\": [" + impactValue + "] }";
		// Combine all parameters as JSON string.
		String data = "{ \"agents\": [" + characterParam + "]"
					 + ", \"no.events\": " + eventMap.size()  
					 + ", \"event_seq\": [" + eventSeq + "], \"plot_seq\": [" + plotSeq + "]"
					 + ", \"info\": [" + infoParam + "], \"knowledge\": [" + knowledgeParam + "]"
					 + ", \"perception\":" + perceptionParam + ", \"impact\":" + impactParam
					 + ", \"know_type\": [" + knowledgeTypeParam + "] }";
		sendDataToShiny(DOM.asOld(scope), type, data);
		return true;
	}
	
}

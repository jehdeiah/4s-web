package com.googlecode._4s_web.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.shared.Interval;

public class NetworkComplexity extends Composite {

	private static NetworkComplexityUiBinder uiBinder = GWT
			.create(NetworkComplexityUiBinder.class);

	interface NetworkComplexityUiBinder extends
			UiBinder<Widget, NetworkComplexity> {
	}

	/**
	 * 자바 스크립트로 입력 자료 만들어서 분석 요청하기  
	 */
	public native void callShiny(String data) /*-{
		var shinyframe = $doc.getElementById("shiny");
		var msg = "{\"type\": \"str.comp\", \"data\":" + data + "}";
		shinyframe.contentWindow.postMessage(msg, shinyframe.src);
	}-*/;
	
	/*
	 * 캐시에서 읽어들일 엔티티 배열형
	 */
	final EventEntity[] eventArrayType = new EventEntity[0];

	public NetworkComplexity() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	class SimpleEvent {
		public long id;
		public int storyOrder;
		public double discourseTime;
	};
	
	public	void analyze() {
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
					se.id = e.getId();
					se.storyOrder = e.getOrdinalStoryIn();
					se.discourseTime = inter.getBegin();
					storySequence.add(se);
					plotSequence.add(se);
				}
			}
		}
		if (storySequence.size() > 0) {
			Iterator<SimpleEvent> is = storySequence.iterator();
			Iterator<SimpleEvent> ip = plotSequence.iterator();
			String eventSeq = "[" + Long.toString(is.next().id);
			String plotSeq = "[" + Long.toString(ip.next().id);
			while (is.hasNext()) {
				eventSeq += "," + is.next().id;
				plotSeq += "," + ip.next().id;
			}
			eventSeq += "]";
			plotSeq += "]";
			String data = "{ \"event_seq\": " + eventSeq + ", \"plot_seq\": " + plotSeq + " }";
			callShiny(data);
		}
	}

}

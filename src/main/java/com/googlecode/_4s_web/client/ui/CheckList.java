package com.googlecode._4s_web.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.shared.Interval;

/* TODO:
 * 1) Putting the results into a table.
 * 2) Adding check boxes for the 'ignore' column.
 * 3) Rendering 'move' button on the focused row.
 * 4) Implementing analyses...
 */
public class CheckList extends Composite {

	private static CheckListUiBinder uiBinder = GWT
			.create(CheckListUiBinder.class);

	interface CheckListUiBinder extends UiBinder<Widget, CheckList> {
	}

	public CheckList() {
		initWidget(uiBinder.createAndBindUi(this));
		checkList.getCellFormatter().setWidth(0, 1, "50px");
		checkList.getCellFormatter().setWidth(0, 2, "50px");
	}

	DiscourseTimeline discourseTimeline = null;
	
	public void addDiscourseTimeline(DiscourseTimeline dt) {
		discourseTimeline = dt;
	}

	@UiField
	Grid checkList;
	@UiField
	TextArea anomalyDetection;
	// unused methods for now
	/*
	double getTimelineY(int timePoint) {
		return getTimelineY(timePoint, 0);
	}

	double getTimelineY(int timePoint, int deltaPX) {
		double y = 0;
		if (timePoint >= 0 && timePoint < StoryTimePoint.getCount()) {
			y = (discourseTimeline.EventMargin + discourseTimeline.DefaultEventHeight) * timePoint + discourseTimeline.EventMargin
					+ deltaPX;
		} else if (timePoint == StoryTimePoint.getCount()) {
			y = (discourseTimeline.getBasePanelHeight() - discourseTimeline.DefaultEventHeight) + deltaPX;
		} else {
			y = discourseTimeline.getBasePanelHeight() + deltaPX;
		}
		return y / discourseTimeline.getBasePanelHeight() * 100.;
	}
	
	double getTimelineX(int screenOffsetX) {
		return getTimelineX(screenOffsetX, 0);
	}

	double getTimelineX(int screenOffsetX, int delta) {
		final double width = discourseTimeline.getTimelineEditorWidth();
		double pct = (double)(screenOffsetX - delta) / width * 100.0;
		return Math.round(pct*discourseTimeline.getResolutionBase())/discourseTimeline.getResolutionBase();
	}
	*/
	int getTimelineOffsetX(double pct, double zoomRatio) {
		return (int)Math.round((discourseTimeline.getTimelineEditorWidth() * pct /zoomRatio));// + 0.5);
	}

	void analyze() {
		double zoomRatio = 100.0;	// defines zoom ratio: represents maximum percentage of the width of the discourse timeline(100.0 for no zoom).
		EventEntity[] eventArrayType = new EventEntity[0];
		EventEntity[] eventSet = LocalCache.entities(EventEntity.class, eventArrayType);
		SortedSet<Integer> beginSet = new TreeSet<Integer>();
		SortedSet<Integer> endSet = new TreeSet<Integer>();
		double discourseTimelineWidth = discourseTimeline.getTimelineEditorWidth();
		//String anomalyText = anomalyDetection.getText();
		String anomalyText = "";
		
		List<Interval> intervalList = new ArrayList<Interval>();
		
		anomalyText += "--Empty Properties--" + "\n";
		for (EventEntity e : eventSet) {
			// check property empty
			boolean anomalyDetected = false;
			String propertyString = "";
			
			propertyString += e.getName() + ":\t";
			if(e.getPlace().equals("") || e.getPlace() == null) {
				propertyString += "[Place]";
				anomalyDetected = true;
			}
			if(e.getActionDescription().equals("") || e.getActionDescription() == null) {
				propertyString += "[ActionDescription]";
				anomalyDetected = true;
			}
			if(e.getMainCharacter() == -1) {
				propertyString += "[MainCharacter]";
				anomalyDetected = true;
			}
			if(e.getInvolvedCharacters().size() == 0 || e.getInvolvedCharacters() == null) {
				propertyString += "[InvolvedCharacters]";
				anomalyDetected = true;
			}
			if(anomalyDetected) {
				anomalyText += propertyString + "\n";
			}
			
			// Store discourse timeline timepoints
			SortedSet<Interval> discourseInOut = e.getDiscourseInOut();
			for(Interval time: discourseInOut) {
				intervalList.add(time);
				Double beginTime = time.getBegin();
				Double endTime = time.getEnd();
				int beginTimePx = getTimelineOffsetX(beginTime, zoomRatio);
				int endTimePx = getTimelineOffsetX(endTime, zoomRatio);
				//anomalyText += "\n" + String.valueOf(beginTimePx) + "(" + String.valueOf(beginTime) + ")" + "\t" + String.valueOf(endTimePx) + "(" + String.valueOf(endTime) + ")";
				beginSet.add(beginTimePx);
				endSet.add(endTimePx);
				/*
				beginSet.add(Math.round(beginTime*1)/1.0);
				endSet.add(Math.round(endTime*1)/1.0);
				*/
			}
		}
		// for printing discourse timepoints
		/*
		Collections.sort(intervalList);
		for(Interval time: intervalList) {
			Double beginTime = time.getBegin();
			Double endTime = time.getEnd();
			int beginTimePx = getTimelineOffsetX(beginTime);
			int endTimePx = getTimelineOffsetX(endTime);
			anomalyText += "\n" + String.valueOf(beginTimePx) + "(" + String.valueOf(beginTime) + ")" + "\t\t" + String.valueOf(endTimePx) + "(" + String.valueOf(endTime) + ")";
		}
		*/
		// Discourse timeline anomaly check logic
		if(!beginSet.contains((int)0)) {
			anomalyText += "\n" + "Anomally Detected: Begin time point not defined";
		} else {
			beginSet.remove((int)0);
		}
		if(!endSet.contains((int)discourseTimelineWidth)) {
			anomalyText += "\n" + "Anomally Detected: End time point not defined";
		} else {
			endSet.remove((int)discourseTimelineWidth);
		}
		for(Integer beginTime: beginSet) {
			if(!endSet.contains(beginTime)) {
				anomalyText += "\n" + "Anomally Detected: Time point " + String.valueOf(beginTime) + " not connected";
			}
		}
		/*
		for(Double endTime: endSet) {
			if(!beginSet.contains(endTime)) {
				anomalyText += "\n" + "Anomally Detected: Time point " + String.valueOf(endTime) + " not connected";
			}
		}
		*/
		anomalyText += "\n";
		anomalyDetection.setText(anomalyText);
	}

	@UiHandler("analyze")
	void onClickAnalyze(ClickEvent event) {
		analyze();
	}

}

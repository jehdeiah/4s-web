package com.googlecode._4s_web.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Random;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.CheckBox;
import com.googlecode._4s_web.client.RequestEntityBuffer;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.EventRelationEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.shared.Interval;

/**
 * 캐릭터 네트워크 화면
 * 여기서는 캐릭터 속성 편집이 가능하다.
 * 
 * @author Raphael, jehdeiah
 *
 */
public class EventNetwork extends Composite 
					implements HasEventPropertyPanel {

	private static EventNetworkUiBinder uiBinder = GWT
			.create(EventNetworkUiBinder.class);

	interface EventNetworkUiBinder extends UiBinder<Widget, EventNetwork> {
	}

	interface MyStyle extends CssResource {
	}

	/**
	 * 자바 스크립트로 네트워크 그리기
	 * 
	 * @param nodeString		노드 정보 JSON 문자열
	 * @param linkString		연결 정보 JSON 문자열
	 * @param width			네트워크 표시 영역 너비
	 * @param height			네트워크 표시 영역 높이
	 */
	public native void networkDrawing(String nodeString, String linkString, String subLinkString, int width, int height) /*-{
		// 자바스크립트에서 본 클래스를 참조하기 위한 변수 선언
		var thisPanel = this;
		var nodes = JSON.parse(nodeString);
		var links = JSON.parse(linkString);
		alert(subLinkString);
		function createGraph(nodes, links) {
			// 
			// START OF Preprocessing
			//
			// Create a force algorithm on which position calculations will be done.
			var force = $wnd.d3.layout.force()
			    .nodes(nodes)
			    .links(links)
			    .start();
			
			// Initialize prev, next, selected
			nodes.forEach(function(n){
			    n.selected = false;
			    n.prev = new Array(); n.next = new Array();
			});
			    
			// Add prev, next, selected to each node
			links.forEach(function(l){
			    l.selected = false; l.annotation = "none";
			    l.source.next.push(l.target);
			    l.target.prev.push(l.source);
			});
			
			// Other global variables
			var base_node_radius = 5;
			var col = {"causal":"#f55", "character":"#000"},
			    dash = {"causal":"3,3", "character":"1,0"},
			    opa = {1:1.0, 0:0.4};
			var selection = new Array();			
			// 
			// END OF Preprocessing
			//
			
			
			// 
			// START OF Visualization preparation
			//
			//Re-initialize DOM objects
			$wnd.d3.select('#character-graph').select("svg").remove();
			$wnd.d3.select('#event-graph').select("svg").remove();	
			
			//Set Force algorithm visualization features
			force.gravity(.2)
			    .charge(function(d){return (d.selected)?-800:-200;})
			    .linkStrength(function(d){return (d.selected)?0.8:0.2;})
			    .linkDistance(100)
			    .size([width, height])
			    .on("tick", tick)
			    .start();
			
			//Create a svg tag on which visualization will be done
			var svg = $wnd.d3.select('#event-graph').append('svg')
			    .attr("width", width)
			    .attr("height", height);
			// 
			// END OF Visualization preparation
			//
			
			// 
			// Start of Add links
			// 
			var link = svg.append("g").selectAll(".link")
			    .data(links)
			       .enter()
			    .append("path")
			    .attr("class", "link")
			    .attr("id",function(d,i) { return "linkId_" + i; })
			    .attr("marker-end", function(l) { return "url(#"+l.type+")"; })
			    .attr("stroke", function(l) { return col[l.type];})
			    .style("stroke-dasharray", function(l) { return dash[l.type]; })
			    .attr('stroke-width', 2);
			// 
			// End of Add links
			// 
			
			// 
			// Start of Add nodes
			// 
			var node = svg.append("g").selectAll(".node")
			    .data(nodes)
			   .enter()
			    .append("g")
			    .attr('class', 'node')
				.attr("id", function(d,i) {return "event_" + nodes[i].eId; })
			    .on("dblclick", nodeDblClick)
			    .on("click", nodeClick)
			    .call(force.drag);
			
			//Apend circle to node
			node.append("circle")
			    .attr('r', function(d) { d.radius = (parseInt(d.dRange) != 0) ? (base_node_radius + (parseInt(d.dRange)*3)) : base_node_radius; return d.radius; })
			    .attr("fill", function(n) {return n.color;});
			
			//Apend text to node
			node.append('text')
				.attr('dx', '-2em')
				.attr('dy', '1.5em')
			    .attr("font-size","0.5em")
				.style('fill', 'black')
			    .text(function(d) { return d.name });
			// 
			// End of Add nodes
			// 
			
			// 
			// Tick animation - update visualization
			//
			function tick(e) {
			    // 1. update links for opacity
			    svg.selectAll("path")
			        .attr("opacity", function(l) { return opa[(l.selected+0)];})
			    svg.selectAll(".annotation")
			        .text(function(l) { return l.annotation;})
			    // 2. update nodes for opacity and position
			    svg.selectAll(".node")
			        .attr("opacity", function(n) { return opa[(n.selected+0)];})
			    nodes.forEach(function(n){
			        if (n.selected == true) {
			            n.x = (width/2) + n.depth * 100;
			            if (n.depth == 0) n.y = height/2;
			        }});
			    
			    // Transition of nodes and links    
			    link.attr("d", linkTransform);
			    node.attr("transform", transform);
			}
			
			// 
			// START OF miscellaneous functions
			//
			//Select node with the most cardinality as default
			var defaultNode = force.nodes()[0];
			force.nodes().forEach(function(n){if (n.prev.length+n.next.length > defaultNode.prev.length+defaultNode.next.length) defaultNode = n;});
			nodeClick(defaultNode);
			//Select nodes
			function nodeClick(d) {
				force.links().forEach(function(l) {l.selected = false; });
			    force.nodes().forEach(function(n) {n.selected = false; });
			    //set depth to 0
			    d.depth = 0; d.selected = true;
			
			    //Put nodes with directions and depths into an array
			    var nodeToPropagate = [];
			    //Add prev, next nodes to the array
			    d.next.forEach(function(n) {
			    	nodeToPropagate.push({"node":n, "depth":d.depth+1, "toNext":true}); 
			    	n.selected = true;
			    	force.links().forEach(function(l) {if (l.source==d && l.target==n) l.selected = true;});
		    	});
			    d.prev.forEach(function(n) {
			    	nodeToPropagate.push({"node":n, "depth":d.depth-1, "toNext":false}); 
			    	n.selected = true;
			    	force.links().forEach(function(l) {if (l.source==n && l.target==d) l.selected = true;}); 
		    	});
			    
			    //Recursively select nodes
			    addNodeSelection(nodeToPropagate);
			    
			
			    //start force algorithm
			    force.start();
			}
			
			function addNodeSelection(nodeList) {
			    var nextNodeToPropagate = [];
			    //Select node, set depth and recursively run with appropriate depth, ONLY when it is not already selected
			    nodeList.forEach(function(n){
			        n.node.depth = n.depth;
			        
			        if (n.toNext==true) {
			            n.node.next.forEach(function(nNode){
			                if (nNode.selected == false) {
			                    nextNodeToPropagate.push({"node":nNode, "depth":n.depth+1, "toNext":true});
			                    nNode.selected = true;
			                    force.links().forEach(function(l) {if (l.source==n.node && l.target==nNode) l.selected = true;});
			                }
			            });
			        }
			        if (n.toNext==false) {
			            n.node.prev.forEach(function(pNode){
			                if (pNode.selected == false) {
			                    nextNodeToPropagate.push({"node":pNode, "depth":n.depth-1, "toNext":false});
			                    pNode.selected = true;
			                    force.links().forEach(function(l) {if (l.source==pNode && l.target==n.node) l.selected = true;});
			                }
			            });
			        }
			    });
			    if (nextNodeToPropagate.length>0) addNodeSelection(nextNodeToPropagate);
			}
			
			// Move nodes
			function transform(d) {
			    return "translate(" + d.x + "," + d.y + ")";
			}
			
			// Move links
			function linkTransform(d) {
			    var dx = d.target.x - d.source.x,
			        dy = d.target.y - d.source.y,
			        dr = Math.sqrt(dx * dx + dy * dy);
			    offsetXS = (dx * d.source.radius) / dr;	offsetYS = (dy * d.source.radius) / dr;	offsetXT = (dx * d.target.radius) / dr;	offsetYT = (dy * d.target.radius) / dr;
			    return "M" + (d.source.x + offsetXS) + "," + (d.source.y + offsetYS) + "L" + (d.target.x - offsetXT) + "," + (d.target.y - offsetYT);
			}
			
			//Append defs which will be used as arrowheads
			svg.append("defs").selectAll("marker")
			    .data(["character", "causal"])
			    .enter().append("marker")
			    .attr("id", function(d) { return d; })
			    .attr("viewBox", "0 -5 10 10")
			    .attr("refX", 10)
			    .attr("refY", 0)
			    .attr("fill", function(d) {return col[d]; })
			    .attr("orient", "auto")
			    .append("path")
			    .attr("d", "M0,-5L10,0L0,5");
			// 
			// END OF miscellaneous functions
			//
		}
		
		createGraph(nodes, links);
		
		function linkDblClick(d,i) {
			var newAnnotation = prompt("Please enter annotation label for this link", $wnd.d3.select("#annotationId_" + i).textContent);
			$wnd.d3.select("#annotationId_" + i).text(newAnnotation); 
		}
		
		function nodeDblClick(d) {
			thisPanel.@com.googlecode._4s_web.client.ui.EventNetwork::showEventProperties(Ljava/lang/String;)(d.eId);
		}
		
		updateLinks = function() {
			var links = []; var nodes = JSON.parse(nodeString);
			var main = $doc.getElementById("gwt-uid-45").checked;
			var sub = $doc.getElementById("gwt-uid-46").checked;
			if 		(main && sub) {links = JSON.parse(linkString).concat(JSON.parse(subLinkString));}
			else if (main && !sub){links = JSON.parse(linkString);}
			else if (!main && sub){links = JSON.parse(subLinkString);}
			
			createGraph(nodes, links);
		}
		$doc.querySelector("#gwt-uid-45").addEventListener("change", updateLinks);
		$doc.querySelector("#gwt-uid-46").addEventListener("change", updateLinks);
	}-*/;

	/**
	 * 캐릭터 속성 변경 내용을 그래프에 반영한다.
	 * 캐릭터 속성은 JSON 텍스트로 넘긴다.
	 * @param text { "cid": character_id, "name": name, "color": color}
	 */
	public static native void updateEventNode(String text) /*-{
		var node = JSON.parse(text);
		var target = $wnd.d3.select("#event_" + node.eId);
		target.select("circle").style('fill', node.color);
		target.select("text").text(node.name);
	}-*/;

	/*
	 * 캐시에서 읽어들일 엔티티 배열형
	 */
	final EventRelationEntity[] eventRelationArrayType = new EventRelationEntity[0];
	final CharacterEntity[] characterArrayType = new CharacterEntity[0];
	final EventEntity[] eventArrayType = new EventEntity[0];
		
	/*
	 * 속성창 
	 */
	EventPropertyPanel eventProperty = null;


	public EventNetwork() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	protected void onLoad() {
		super.onLoad();
		drawingPanel.getElement().setId("event-graph");
		CheckBox mainCheckBox = new CheckBox("Character Links"); 
		CheckBox subCheckBox = new CheckBox("Annotaion Links");
		mainCheckBox.setValue(true);
		drawingPanel.add(mainCheckBox); drawingPanel.add(subCheckBox);
	}


	/**
	 * 로컬 캐시로부터 캐릭터와 이벤트를 읽어서 네트워크를 그린다.
	 * 자바스크립트로 넘겨줄 입력 문자열을 생성하고 JSNI 메쏘드 <code>networkDrawing()</code>를 부른다.
	 */
	public void draw() {
		/*
		 *  Get to-be-JSON string for Main annotation links
		 *  eventOrder = Sorted Map of eventId list, ordered by its story-in time and character order
		 */
		String links = "[ "; String nodes = "[ ";
		SortedMap<Integer, SortedMap<Integer, Long>> eventOrder = new TreeMap<Integer, SortedMap<Integer, Long>>();		
		for (CharacterEntity c : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
			SortedMap<Integer, Long> subOrder = new TreeMap<Integer, Long>();
			for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) {	
				if (c.getId() == e.getMainCharacter()) { 
					subOrder.put(e.getOrdinalStoryIn(), e.getId());
				}
			}			
			eventOrder.put(c.getIndex(), subOrder);
		}
		
		// Add unassigned events		
//		SortedMap<Integer, Long> nonAssignedEvents = new TreeMap<Integer, Long>();
//		for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) 	
//			if (e.getMainCharacter() == -1) 
//				nonAssignedEvents.put(-1, e.getId());
//		eventOrder.put(9999, nonAssignedEvents);
		
		/*
		 * Iterate through each Character
		 */
		int nodeIndex = 0; HashMap<Long, Integer> idList = new HashMap<Long, Integer>();
		Iterator i1 = eventOrder.entrySet().iterator();
	    while (i1.hasNext()) {
	    	Map.Entry m = (Map.Entry)i1.next();
	    	Iterator i2 = ((SortedMap<Integer, Long>) m.getValue()).entrySet().iterator();
	    	
	    	/*
	    	 * Iterate through each event for given character, in story-time order
	    	 */
	    	long lastEId = -1; int eventIndex = 0;
	    	while (i2.hasNext()) {
		    	Map.Entry mm = (Map.Entry)i2.next();
		    	
		    	/*
		    	 * Add link string if there is a connection between this and last node
		    	 */
		    	if (lastEId != -1) 
		    		links += "{\"source\":" + (nodeIndex-1) + ",\"target\":" + nodeIndex + ",\"type\":\"character\"},";
		    	lastEId = (Long) mm.getValue();
		    	
		    	/*
		    	 * Add node string
		    	 */
		    	EventEntity e = LocalCache.get(EventEntity.class, (Long)mm.getValue());
		    	double range = 0;
		    	Collection<Interval> discourseInOut = e.getDiscourseInOut();
				for (Interval r : discourseInOut) 
					range += r.getEnd() - r.getBegin();
				nodes += 	"{\"name\":\"" + e.getName() + 
						"\",\"dRange\":\"" + Double.toString(range) +
						"\", \"color\":\"" + ((e.getMainCharacter() == -1) ? "rgb(255, 255, 255)" : LocalCache.get(CharacterEntity.class, e.getMainCharacter()).getColor()) + 
						"\",\"cId\":\"" + Long.toString(e.getMainCharacter()) +
						"\",\"cIndex\":\"" + LocalCache.get(CharacterEntity.class, e.getMainCharacter()).getIndex() +
						"\", \"eId\":\"" + Long.toString(e.getId()) + "\"},";
				
				
				/*
				 * Increment indexes accordingly
				 */
				idList.put(e.getId(), nodeIndex);
				nodeIndex++;
	    	}
	    }
	    
	    nodes = nodes.substring(0, nodes.length()-1); nodes += "]";
	    links = links.substring(0, links.length()-1);links += "]";

	    
	    /*
		 *  Get to-be-JSON string for supplementary annotation links
		 */
	    Random r = new Random();
	    String subLinks = "[";
	    
	    for (EventRelationEntity er : LocalCache.entities(EventRelationEntity.class, eventRelationArrayType)){
	    	subLinks += 
	    			"{\"source\":"+idList.get(er.getFromEvent())+
	    			",\"target\":"+idList.get(er.getToEvent())+
	    			",\"type\":\"causal\"},";
	    }
	    
	    subLinks = subLinks.substring(0, subLinks.length()-1);subLinks += "]"; 
	    
	    /*
	     * Draw the network using given strings
	     */
		int width = drawingPanel.getOffsetWidth();
		int height = drawingPanel.getOffsetHeight();
		networkDrawing(nodes, links, subLinks, width, height);
	}

	/**
	 * 자바스크립트에서 선택한 이벤트 ID를 문자열로 받아 속성창을 띄운다.
	 * @param eId 캐릭터 ID
	 */
	public void showEventProperties(String eId) {
		Long id = Long.valueOf(eId);
		EventEntity c = LocalCache.get(EventEntity.class, id);
		if (c != null) 
			showEventProperties(c);
	}

	/**
	 * 캐릭터 엔티티에 대한 속성창을 띄운다.
	 */
	@Override
	public void showEventProperties(EventEntity entity) {
		if (eventProperty == null) {
			eventProperty = new EventPropertyPanel(this);
			eventProperty.setMode(PropertyPanel.MODALESS_OK_CANCEL);
		}
		eventProperty.setData(entity);
		eventProperty.center();
	}

	/**
	 * 속성창에서 속성을 바꿀 수 있으므로 화면에 반영하도록 자바스크립트를 부르고,
	 * 엔티티를 저장 버퍼에 추가한다.
	 */
	@Override
	public void updateEventProperties(EventEntity entity) {
		CharacterEntity c = LocalCache.get(CharacterEntity.class, entity.getMainCharacter());
		String eventString = "{\"eId\": \"" + Long.toString(entity.getId()) + "\"," +
							 "\"name\": \"" + entity.getName() + "\"," +
							 "\"color\": \"" + c.getColor() + "\"}";
		updateEventNode(eventString);
		RequestEntityBuffer.saveEntity(entity);
	}
	
	/*-------------------------------------------------------------
	 * GWT UI Binder 요소와 이벤트 처리기 
	 */	
	@UiField
	FlowPanel drawingPanel;

}
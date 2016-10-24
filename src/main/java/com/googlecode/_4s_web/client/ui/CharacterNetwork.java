package com.googlecode._4s_web.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode._4s_web.client.RequestEntityBuffer;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.LocalCache;

/**
 * 캐릭터 네트워크 화면
 * 여기서는 캐릭터 속성 편집이 가능하다.
 * 
 * @author Raphael, jehdeiah
 *
 */
public class CharacterNetwork extends Composite 
					implements HasCharacterPropertyPanel {

	private static CharacterNetworkUiBinder uiBinder = GWT
			.create(CharacterNetworkUiBinder.class);

	interface CharacterNetworkUiBinder extends UiBinder<Widget, CharacterNetwork> {
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
	public native void networkDrawing(String nodeString, String linkString, int width, int height) /*-{
		// 자바스크립트에서 본 클래스를 참조하기 위한 변수 선언
		var thisPanel = this;
		
		var nodes = JSON.parse(nodeString);
		var links = JSON.parse(linkString);
		
		$wnd.d3.select('#character-graph').select("svg").remove();	
		$wnd.d3.select('#event-graph').select("svg").remove();	
		var color = $wnd.d3.scale.ordinal().range(["#00ff00", "#ff0000", "#0000ff"]); 
		var base_node_radius = 15, link_strength_threshold = 0.25;
		//var width = 1080, height = 800; 
		
		var force = $wnd.d3.layout.force()
			.nodes(nodes)
			.links(links)
			.gravity(.02)
			.linkDistance(300)
			.linkStrength(function(link) {return ((link.weight / link.maxCount) < link_strength_threshold) ? 0 : (link.weight / (link.maxCount*2));})
			.charge(-300)
			.chargeDistance(300)
			.size([width, height])
			.on("tick", tick)
			.start();
		
		var svg = $wnd.d3.select('#character-graph').append('svg')
			.attr('width', width)
			.attr('height', height);
		
		var node = svg.selectAll('.node')
			.data(nodes)
		  .enter().append('g')
			.attr('class', 'node')
			.attr("id", function(d,i) { return "char_" + nodes[i].cid; }) // 캐릭터 지정을 위해 캐릭터 id 등록
			.on("mouseup", function(d) {$wnd.d3.select(this).classed("fixed", d.fixed = true);})
			.on("dblclick", nodeDblClick)
			.call(force.drag);
			
		node.append('circle')
			.attr('r', function(d) { d.radius = base_node_radius + d.weight*5; return d.radius; })
			.style('fill', function(d) { return d.color; })
			.attr('stroke', function(d) { return color(d.weight%3); })
			.attr('stroke-width', function(d) { d.strokeWidth = d.weight; return d.strokeWidth; })
		
		node.append('text')
			.attr('dx', '-2em')
			.attr('dy', '.35em')
			.style('fill', 'black')
			.text(function(d) { return d.name });
	
		svg.append("defs").selectAll("marker")
			.data(["friend", "lover", "rival", "enemy"])
		  .enter().append("marker")
			.attr("id", function(d) { return d; })
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 10)
			.attr("refY", 0)
			.attr("markerWidth", 6)
			.attr("markerHeight", 6)
			.attr("orient", "auto")
		  .append("path")
			.attr("d", "M0,-5L10,0L0,5");
			

		var path = svg.append("g").selectAll("path")
			.data(force.links())
		  .enter().append("path")
			.on("dblclick", linkDblClick)
			.attr("class", "link")
			.attr("id",function(d,i) { return "linkId_" + i; })
			.attr("marker-end", "url(#friend)")
			.attr("fill", "none")
			.attr('stroke', function(d) { var v = ((d.weight / d.maxCount) < link_strength_threshold) ? link_strength_threshold : (d.weight / d.maxCount); var w = Math.floor(255-v*255).toString(16); return "#" + w + w + w; })
			.attr('stroke-width', function(d) { d.strokeWidth = d.weight/2; return d.strokeWidth; });
		
		var annotation = svg.append("g").selectAll("annotation")
			.data(force.links())
		  .enter().append("text")
		    .on("dblclick", linkDblClick)
		    .attr("class", "annotation")
			.attr("x", "50")
			.attr("y", "-20")
		    .attr("text-anchor", "start")
		  .append("textPath")
		    .attr("id",function(d,i) { return "annotationId_" + i; })
    		.attr("xlink:href",function(d,i) { return "#linkId_" + i;})
     		.text("Test");
		     
		function tick() {
			path.attr("d", linkArc);
			node.attr("transform", transform);
		}
		
		function linkArc(d) {
			var dx = d.target.x - d.source.x,
				dy = d.target.y - d.source.y,
				dr = Math.sqrt(dx * dx + dy * dy);
			offsetXS = (dx * (d.source.radius + (d.source.strokeWidth/2))) / dr;
			offsetYS = (dy * (d.source.radius + (d.source.strokeWidth/2))) / dr;
			offsetXT = (dx * (d.target.radius + (d.target.strokeWidth/2))) / dr;
			offsetYT = (dy * (d.target.radius + (d.target.strokeWidth/2))) / dr;
			return "M" + (d.source.x + offsetXS) + "," + (d.source.y + offsetYS) + "A" + dr + "," + dr + " 0 0,1 " + (d.target.x - offsetXT) + "," + (d.target.y - offsetYT);
		}
		
		function transform(d) {
			return "translate(" + d.x + "," + d.y + ")";
		}
		
		function linkDblClick(d,i) {
			var newAnnotation = prompt("Please enter annotation label for this link", $wnd.d3.select("#annotationId_" + i).textContent);
			$wnd.d3.select("#annotationId_" + i).text(newAnnotation); 
		}
		
		function nodeDblClick(d) {
			thisPanel.@com.googlecode._4s_web.client.ui.CharacterNetwork::showCharacterProperties(Ljava/lang/String;)(d.cid);
		}
	}-*/;

	/**
	 * 캐릭터 속성 변경 내용을 그래프에 반영한다.
	 * 캐릭터 속성은 JSON 텍스트로 넘긴다.
	 * @param text { "cid": character_id, "name": name, "color": color}
	 */
	public static native void updateCharacterNode(String text) /*-{
		var node = JSON.parse(text);
		var target = $wnd.d3.select("#char_" + node.cid);
		target.select("circle").style('fill', node.color);
		target.select("text").text(node.name);
	}-*/;

	/*
	 * 캐시에서 읽어들일 엔티티 배열형
	 */
	final CharacterEntity[] characterArrayType = new CharacterEntity[0];
	final EventEntity[] eventArrayType = new EventEntity[0];
		
	/*
	 * 속성창 
	 */
	CharacterPropertyPanel characterProperty = null;


	public CharacterNetwork() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	protected void onLoad() {
		super.onLoad();
		drawingPanel.getElement().setId("character-graph");
	}

	/**
	 * 로컬 캐시로부터 캐릭터와 이벤트를 읽어서 네트워크를 그린다.
	 * 자바스크립트로 넘겨줄 입력 문자열을 생성하고 JSNI 메쏘드 <code>networkDrawing()</code>를 부른다.
	 */
	public void draw() {
		Map<Long, Integer> nodeCounter = new HashMap<Long,Integer>();
		Map<Long, Map<Long, Integer>> linkCounter = new HashMap<Long, Map<Long, Integer>>();
		int linkMaxCount = 0;
		for (CharacterEntity c : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
			nodeCounter.put(c.getId(), 0);
			Map<Long, Integer> counter = new HashMap<Long,Integer>();
			for (CharacterEntity c2 : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
				if(c.getId() != c2.getId()) {
					counter.put(c2.getId(), 0);
				}
			}
			linkCounter.put(c.getId(), counter);
		}
		for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) {
			if (e.isAssigned()){
				CharacterEntity c = LocalCache.get(CharacterEntity.class, e.getMainCharacter());
				nodeCounter.put(c.getId(), nodeCounter.get(c.getId()) + 1);
				for (long ic : e.getInvolvedCharacters()) {
					if (c.getId() != ic) {
						Map<Long, Integer> counter = linkCounter.get(c.getId());
						counter.put(ic, linkCounter.get(c.getId()).get(ic) + 1);
						linkCounter.put(c.getId(), counter);
						if (linkMaxCount < linkCounter.get(c.getId()).get(ic)) linkMaxCount = linkCounter.get(c.getId()).get(ic);
					}
				}
			}							
		}
		String nodes = "[ ";	String links = "[ "; int n1Index = 0;
		for (CharacterEntity c : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
			nodes += 	"{\"name\":\"" + c.getName() + 
						"\",\"count\":" + nodeCounter.get(c.getId()) + 
						", \"color\":\"" + c.getColor() + 
						"\", \"cid\":\"" + Long.toString(c.getId()) + "\"},";
			
			int n2Index = 0;
			for (CharacterEntity c2 : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
				if(c.getId() != c2.getId() && linkCounter.get(c.getId()).get(c2.getId()) != 0) {
					links += "{\"source\":" + n1Index + ",\"target\":" + n2Index + ",\"weight\":" + linkCounter.get(c.getId()).get(c2.getId()) + ",\"maxCount\":" + linkMaxCount + "},";
				}
				n2Index++;
			}
			n1Index++;
		}

		nodes = nodes.substring(0, nodes.length()-1);
		links = links.substring(0, links.length()-1);
		nodes += "]"; links += "]";
		int width = drawingPanel.getOffsetWidth();
		int height = drawingPanel.getOffsetHeight();
		networkDrawing(nodes, links, width, height);
	}

	/**
	 * 자바스크립트에서 선택한 캐릭터 ID를 문자열로 받아 속성창을 띄운다.
	 * @param cid 캐릭터 ID
	 */
	public void showCharacterProperties(String cid) {
		Long id = Long.valueOf(cid);
		CharacterEntity c = LocalCache.get(CharacterEntity.class, id);
		if (c != null) 
			showCharacterProperties(c);
	}

	/**
	 * 캐릭터 엔티티에 대한 속성창을 띄운다.
	 */
	@Override
	public void showCharacterProperties(CharacterEntity entity) {
		if (characterProperty == null) {
			characterProperty = new CharacterPropertyPanel(this);
			characterProperty.setMode(PropertyPanel.MODALESS_OK_CANCEL);
		}
		characterProperty.setData(entity);
		characterProperty.center();
	}

	/**
	 * 속성창에서 속성을 바꿀 수 있으므로 화면에 반영하도록 자바스크립트를 부르고,
	 * 엔티티를 저장 버퍼에 추가한다.
	 */
	@Override
	public void updateCharacterProperties(CharacterEntity entity) {
		String characterString = "{\"cid\": \"" + Long.toString(entity.getId()) +
				"\", \"name\": \"" + entity.getName() +
				"\", \"color\": \"" + entity.getColor() + "\"}";
		updateCharacterNode(characterString);
		RequestEntityBuffer.saveEntity(entity);
	}
	
	/*-------------------------------------------------------------
	 * GWT UI Binder 요소와 이벤트 처리기 
	 */	
	@UiField
	FlowPanel drawingPanel;

}
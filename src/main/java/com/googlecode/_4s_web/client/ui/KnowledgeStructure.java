package com.googlecode._4s_web.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode._4s_web.client.StoryServiceAsync;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.ImpactEntity;
import com.googlecode._4s_web.client.entity.InformationEntity;
import com.googlecode._4s_web.client.entity.KSElementEntity;
import com.googlecode._4s_web.client.entity.KSRelationEntity;
import com.googlecode._4s_web.client.entity.KnowledgeEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.client.entity.PerceptionEntity;
import com.googlecode._4s_web.shared.Interval;

/**
 * 지식 구조 입력기.
 * 
 * TODO: 
 *   서스펜스에서 도입한 패턴 추가
 *   
 * @author jehdeiah
 *
 */
public class KnowledgeStructure extends AbstractStoryPanel 
			implements HasEventPropertyPanel, HasLinkDrawingHandler<KnowledgeStructure.LinkUI> {

	private static KnowledgeStructureUiBinder uiBinder = GWT
			.create(KnowledgeStructureUiBinder.class);

	interface KnowledgeStructureUiBinder extends
	UiBinder<Widget, KnowledgeStructure> {
	}

	interface MyStyle extends CssResource {
		String element();
		String selected();
	}
	
	/**
	 * 여러 이벤트 처리기와 하위 클래스에서 참조할 수 있도록 final로 선언한다.
	 */
	private final KnowledgeStructure thisPanel = this;

	/**
	 * 지식 구조 구성 요소<br>
	 * 이벤트, 정보, 지식, 메타-지식 요소들을 나타낸다.
	 * 
	 * @author jehdeiah
	 *
	 */
	class ElementUI extends Label 
					implements EntityUIObject<KSElementEntity>, MouseDownHandler, MouseUpHandler, MouseMoveHandler, 
					MouseOverHandler, MouseOutHandler, DoubleClickHandler {

		public static final int EVENT 			= 0x01;	// 0000 0001
		public static final int EVENT_STORY 	= 0x03;	// 0000 0011
		public static final int EVENT_PLOT 		= 0x05;	// 0000 0101
		public static final int INFORMATION 	= 0x10; // 0001 0000
		public static final int KNOWLEDGE 		= 0x20; // 0010 0000
		public static final int META_KNOWLEDGE	= 0x40; // 0100 0000

		int type;
		KSElementEntity entity;
		int plotIndex; // EVENT_PLOT only.

		private boolean selected = false;
		private boolean modified = false;

		public ElementUI(String metaKnowledgeTitle) {
			this.type = META_KNOWLEDGE;
			entity = null;
			setText(metaKnowledgeTitle);
		}

		public ElementUI(EventEntity event) {
			type = EVENT_STORY;
			entity = event;
			setText(event.getName());
		}

		public ElementUI(EventEntity event, int occurrence) {
			type = EVENT_PLOT;
			if (occurrence < 0 || occurrence>=event.getOccurrence()) {
				entity = null;
				plotIndex = -1;
			} else {
				entity = event;
				plotIndex = occurrence;
				String name = event.getName();
				if (event.getOccurrence() > 1) name += " #" + (occurrence + 1);
				setText(name);
			}
		}

		public ElementUI(InformationEntity info) {
			type = INFORMATION;
			entity = info;
			setText(info.getName());
		}
		
		public ElementUI(KnowledgeEntity know) {
			type = KNOWLEDGE;
			entity = know;
			setText(know.getName());
		}

		public void release() {
			selected = false;
			removeStyleName(thisPanel.style.selected());
		}

		@Override
		protected void onLoad() {
			setStyleName(style.element());
			updateWidget();

			addMouseDownHandler(this);
			addMouseUpHandler(this);
			addMouseMoveHandler(this);
			addMouseOverHandler(this);
			addMouseOutHandler(this);
			addDoubleClickHandler(this);
			
			sinkEvents(com.google.gwt.user.client.Event.ONCONTEXTMENU);
		}
		
		public void onBrowserEvent(com.google.gwt.user.client.Event e) {
			switch (e.getTypeInt()) {
			case com.google.gwt.user.client.Event.ONCONTEXTMENU:
				e.stopPropagation();
				e.preventDefault();
				thisPanel.showElementContextMenu(this,
						e.getClientX(), e.getClientY());
				break;
			default:
				super.onBrowserEvent(e);
			}
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
				event.preventDefault();
				selected = true;
			}
		}

		/*
		 * 연결선이 끝나는 상자에서 불린다.
		 */
		@Override
		public void onMouseUp(MouseUpEvent event) {
			event.preventDefault();
			selected = false;
			thisPanel.finishDrawingConnection(this);
		}

		/*
		 * HasText라서 텍스트 선택 행동을 하므로 기본 행동을 막는다.
		 * 연결선을 긋는 것은 이 상자 밖이므로 컨테이너에서 한다. 
		 */
		@Override
		public void onMouseMove(MouseMoveEvent event) {
			event.preventDefault();
		}

		@Override
		public void onMouseOver(MouseOverEvent event) {
			addStyleName(thisPanel.style.selected());
			thisPanel.setMouseOverElement(this, true);
		}


		/*
		 * 상자 밖으로 나가면 비로소 연결선을 그리기 시작한다.
		 */
		@Override
		public void onMouseOut(MouseOutEvent event) {
			if (selected) 
				thisPanel.startDrawingConnection(this,
						event.getRelativeX(thisPanel.structureContainer.getElement()), 
						event.getRelativeY(thisPanel.structureContainer.getElement()));
			else removeStyleName(thisPanel.style.selected());
			thisPanel.setMouseOverElement(this, false);
		}

		/*
		 * 상자를 두 번 눌러 속성창을 띄운다.
		 */
		@Override
		public void onDoubleClick(DoubleClickEvent event) {
			event.preventDefault();
			addStyleName(thisPanel.style.selected());
			thisPanel.popupLeft = event.getClientX();
			thisPanel.popupTop = event.getClientY();
			thisPanel.showElementProperties(this);
		}

		@Override
		public void updateWidget() {
			if ((type & EVENT) != 0) {
				EventEntity e = (EventEntity)entity;
				if (e.isPlotted() == false) {
					getElement().getStyle().setBorderStyle(BorderStyle.DOTTED);					}
				if (e.isAssigned()) {
					String bgColor = LocalCache.get(CharacterEntity.class, e.getMainCharacter())
							.getColor();
					getElement().getStyle().setBackgroundColor(bgColor);
				}
				if (!getText().startsWith(e.getName())) {
					String text = e.getName();
					if (type==EVENT_PLOT && e.getOccurrence()>1) text.concat(" #" + plotIndex);
					setText(text);
				}
			} else if (type == INFORMATION) {
				InformationEntity info = (InformationEntity)entity;
				if (!info.getName().equals(getText())) {
					setText(info.getName());
				}
			} else if (type == KNOWLEDGE) {
				KnowledgeEntity k = (KnowledgeEntity)entity;
				if (!k.getName().equals(getText())) {
					setText(k.getName());
				}
				if (k.getTruth()) {
					getElement().getStyle().clearBackgroundColor();
				} else {
					getElement().getStyle().setBackgroundColor("lightgray");
				}
			}
		}

		public int getType() {
			return type;
		}

		@Override
		public KSElementEntity getEntity() {
			return entity;
		}

		@Override
		public long getId() {
			if (entity == null) return -1;
			if ((type&EVENT) != 0) return ((EventEntity)entity).getId();
			else if (type == INFORMATION) return ((InformationEntity)entity).getId();
			else if (type == KNOWLEDGE) return ((KnowledgeEntity)entity).getId();
			return -1;
		}
		
		@Override
		public Widget getWidget() {
			return this;
		}

		@Override
		public void updateFromEntity() {
			updateWidget();
		}

		@Override
		public boolean isModified() {
			return modified;
		}

		@Override
		public void modify() {
			modified = true;
		}

		@Override
		public void invalidate() {
			modified = false;
		}
	}
	
	/**
	 * 지식 구조 연결 요소 <br>
	 * Percept, impact, conjunct link를 화면에 표시한다.
	 * 
	 * @author jehdeiah
	 *
	 */
	class LinkUI extends WidgetLinker 
					implements EntityUIObject<KSRelationEntity>{
		public static final int UNKNOWN = 0;
		public static final int PERCEPTION = 1;
		public static final int IMPACT = 2;
		public static final int CONJUNCT = 3;
		
		int type;
		KSRelationEntity entity;
		
		private boolean modified;
		
		public LinkUI(Panel container) {
			super(container);
			type = UNKNOWN;
		}
		public LinkUI(int type, Panel container) {
			super(container);
			this.type = type;
		}
		
		public LinkUI(PerceptionEntity e, Panel container) {
			super(container);
			type = PERCEPTION;
			entity = e;
			updateWidget();
		}
		
		public LinkUI(ImpactEntity e, Panel container) {
			super(container);
			type = IMPACT;
			entity = e;
			updateWidget();
		}
		
		@Override
		public void updateWidget() {
			if (type == IMPACT) {
				ImpactEntity s = (ImpactEntity)entity;
				lineColor = s.getBelief() ? "black" : "red";
				update();
			}
		}
		
		int getType() {
			return type;
		}
		
		ElementUI getFromElement() {
			return (from instanceof ElementUI) ? (ElementUI)from : null;
		}
		
		ElementUI getToElement() {
			return (to instanceof ElementUI) ? (ElementUI)to : null;
		}

		@Override
		public KSRelationEntity getEntity() {
			return entity;
		}
		@Override
		public long getId() {
			if (entity == null) return -1;
			switch (type) {
			case PERCEPTION:
				return ((PerceptionEntity)entity).getId();
			case IMPACT:
				return ((ImpactEntity)entity).getId();
			default:
				return -1;
			}
		}
		@Override
		public Widget getWidget() {
			return panel;
		}
		@Override
		public void updateFromEntity() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean isModified() {
			return modified;
		}
		@Override
		public void modify() {
			modified = true;
		}
		@Override
		public void invalidate() {
			modified = false;
		}
	}

	/**
	 * Information 속성값 대화상자
	 * @author jehdeiah
	 *
	 */
	class InformationPropertyPanel extends PropertyPanel<InformationEntity> {

		TextBox name;
		TextArea description;
		KnowledgeStructure editor = thisPanel;
		boolean saved = false;

		@Override
		void initPanel() {
			setText("Information Properties...");
			property = new VerticalPanel();
			name = new TextBox();
			description = new TextArea();
			property.add(new HTML("Name :"));
			property.add(name);
			property.add(new HTML("Description :"));
			property.add(description);
		}

		@Override
		void updatePanel() {
			assert (data != null);
			name.setText(data.getName());
			description.setText(data.getDescription());
		}

		@Override
		void notifyUpdate(boolean saved) {
			if (saved) saveData();
			editor.updateInformation(data, saved);
		}

		@Override
		void saveData() {
			if (data != null) {
				data.setName(name.getText());
				data.setDescription(description.getText());
			}
		}
	}
	/**
	 * Knowledge 속성값 대화상자
	 * @author jehdeiah
	 *
	 */
	class KnowledgePropertyPanel extends PropertyPanel<KnowledgeEntity> {

		TextBox name;
		ListBox type;
		TextArea description;
		KnowledgeStructure editor = thisPanel;
		boolean saved = false;
		final String POSITIVE = "Positive";
		final String NEGATIVE = "Negative";

		@Override
		void initPanel() {
			setText("Knowledge Properties...");
			property = new VerticalPanel();
			name = new TextBox();
			type = new ListBox();
			type.addItem(POSITIVE);
			type.addItem(NEGATIVE);
			type.setVisibleItemCount(1); // drop-down
			type.setSelectedIndex(0);
			description = new TextArea();
			property.add(new HTML("Name :"));
			property.add(name);
			property.add(new HTML("Type :"));
			property.add(type);
			property.add(new HTML("Description :"));
			property.add(description);
		}

		@Override
		void updatePanel() {
			assert (data != null);
			name.setText(data.getName());
			type.setSelectedIndex(data.getTruth() ? 0 : 1);
			description.setText(data.getDescription());
		}

		@Override
		void notifyUpdate(boolean saved) {
			if (saved) saveData();
			editor.updateKnowledge(data, saved);
		}

		boolean isPositive() {
			int selected = type.getSelectedIndex();
			return type.getItemText(selected).equals(POSITIVE);
		}

		@Override
		void saveData() {
			if (data != null) {
				data.setName(name.getText());
				data.setTruth(isPositive());
				data.setDescription(description.getText());
			}
		}

	}

	/**
	 * 사건에서 정보를 인식하는 연결선의 속성을 보여주는 패널.<br>
	 * 플롯 이벤트의 경우 몇 번째 것인지 알아야 하므로 화면 요소를 자료로 갖게 한다.
	 */
	class PerceptionLinkPropertyPanel extends PropertyPanel<LinkUI> {

		TextBox event;
		TextBox information;
		Map<Long, TextBox> characterPerceptValues;
		ArrayList<TextBox> readerPerceptValues;
		KnowledgeStructure editor = thisPanel;
		boolean saved = false;

		@Override
		void initPanel() {
			setText("Percept Link Properties...");
			FlexTable p = new FlexTable();
			property = p;
			event = new TextBox();
			event.setReadOnly(true);
			information = new TextBox();
			information.setReadOnly(true);
			p.setWidget(0, 0, new HTML("* Event :"));
			p.setWidget(0, 1, event);
			p.setWidget(1, 0, new HTML("* Information :"));
			p.setWidget(1, 1, information);
			p.setWidget(2, 0, new HTML("* Perception values (0 to 1) :"));
			p.getFlexCellFormatter().setColSpan(2, 0, 2);
			characterPerceptValues = new HashMap<Long, TextBox>();
			readerPerceptValues = new ArrayList<TextBox>();
		}

		@Override
		void updatePanel() {
			FlexTable p = (FlexTable)property;
			ElementUI eventUI = data.getFromElement();
			ElementUI infoUI = data.getToElement();
			PerceptionEntity entity = (PerceptionEntity)data.getEntity();
			event.setText(eventUI.getText());
			information.setText(infoUI.getText());
			int row = 3;
			ArrayList<Long> agents = new ArrayList<Long>();
			EventEntity event = (EventEntity)eventUI.getEntity();
			if (event.getMainCharacter() != -1) agents.add(event.getMainCharacter());
			agents.addAll(event.getInvolvedCharacters());
			Map<Long, Float> characterValues = entity.getCharacterPerceptValues();
			for (TextBox t : characterPerceptValues.values()) {
				t.removeFromParent();
			}
			characterPerceptValues.clear();
			for (Long id : agents) {
				String name = LocalCache.get(CharacterEntity.class, id).getName();
				p.setWidget(row, 0, new HTML(name + " :"));
				TextBox textBox = new TextBox();
				textBox.setText(characterValues.get(id).toString());
				characterPerceptValues.put(id, textBox);
				p.setWidget(row, 1, textBox);
				p.getCellFormatter().getElement(row, 0).getStyle().setPaddingLeft(2, Unit.EM);
				row++;
			}
			// 독자는 스토리 시간일 경우 나타나는 플롯 모두에 대해, 담화 시간인 경우 해당 플롯 위치에서만 인식값을 표시한다.
			for (TextBox t : readerPerceptValues) {
				t.removeFromParent();
			}
			readerPerceptValues.clear();
			ArrayList<Float> readerValues = entity.getReaderPerceptValues();
			if (eventUI.getType() == ElementUI.EVENT_STORY) {
				for (int i=0; i<readerValues.size(); i++) {
					p.setWidget(row, 0, new HTML("Reader #" + i + " :"));
					TextBox textBox = new TextBox();
					textBox.setText(readerValues.get(i).toString());
					readerPerceptValues.add(textBox);
					p.setWidget(row, 1, textBox);
					p.getCellFormatter().getElement(row, 0).getStyle().setPaddingLeft(2, Unit.EM);
					row++;
				}
			} else {
				p.setWidget(row, 0, new HTML("Reader :"));
				TextBox textBox = new TextBox();
				textBox.setText(readerValues.get(eventUI.plotIndex).toString());
				readerPerceptValues.add(textBox);
				p.setWidget(row, 1, textBox);
				p.getCellFormatter().getElement(row, 0).getStyle().setPaddingLeft(2, Unit.EM);
				row++;
			}
			for (; row<p.getRowCount(); row++) {
				p.removeRow(row);
			}
		}

		@Override
		void notifyUpdate(boolean saved) {
			if (saved) {
				saveData();
				editor.updateLink();
			}
		}

		@Override
		void saveData() {
			if (data != null) {
				ElementUI eventUI = data.getFromElement();
				PerceptionEntity entity = (PerceptionEntity)data.getEntity();
				Map<Long, Float> characterValues = entity.getCharacterPerceptValues();
				ArrayList<Float> readerValues = entity.getReaderPerceptValues();
				for (Map.Entry<Long, TextBox> e : characterPerceptValues.entrySet()) {
					characterValues.put(e.getKey(), Float.parseFloat(e.getValue().getText()));
				}
				if (eventUI.getType() == ElementUI.EVENT_STORY) {
					for (int i=0; i<readerValues.size(); i++) {
						readerValues.set(i, Float.parseFloat(readerPerceptValues.get(i).getText()));
					}
				} else {
					readerValues.set(eventUI.plotIndex,Float.parseFloat(readerPerceptValues.get(0).getText()));
				}
			}
		}
		
	}
	
	class ImpactLinkPropertyPanel extends PropertyPanel<ImpactEntity> {

		TextBox information;
		TextBox knowledge;
		ListBox type;
		TextBox impactValue;
		KnowledgeStructure editor = thisPanel;
		boolean saved = false;
		final String TYPE1 = "Belief";
		final String TYPE2 = "Disbelief";

		@Override
		void initPanel() {
			setText("Support Link Properties...");
			FlexTable p = new FlexTable();
			property = p;
			information = new TextBox();
			information.setReadOnly(true);
			knowledge = new TextBox();
			knowledge.setReadOnly(true);
			type = new ListBox();
			type.addItem(TYPE1);
			type.addItem(TYPE2);
			type.setVisibleItemCount(1); // drop-down
			impactValue = new TextBox();
			p.setWidget(0, 0, new HTML("* Information :"));
			p.setWidget(0, 1, information);
			p.setWidget(1, 0, new HTML("* Knowledge :"));
			p.setWidget(1, 1, knowledge);
			p.setWidget(2, 0, new HTML("* Type :"));
			p.setWidget(2, 1, type);
			p.setWidget(3, 0, new HTML("* Impact Value :"));
			p.setWidget(3, 1, impactValue);
		}

		@Override
		void updatePanel() {
			InformationEntity info = LocalCache.get(InformationEntity.class, data.getInformation());
			KnowledgeEntity know = LocalCache.get(KnowledgeEntity.class, data.getKnowledge());
			information.setText(info.getName());
			knowledge.setText(know.getName());
			type.setSelectedIndex(data.getBelief() ? 0 : 1);
			impactValue.setText(Float.toString(data.getImpactValue()));
		}

		@Override
		void notifyUpdate(boolean saved) {
			if (saved) {
				saveData();
				editor.updateLink();
			}
		}

		@Override
		void saveData() {
			if (data != null) {
				data.setBelief(type.getSelectedIndex()==0);
				data.setImpactValue(Float.parseFloat(impactValue.getText()));
			}
		}
	}


	// 처음 화면에 보일 때 크기 조절을 위한 표시. 대응하는 이벤트가 있다면 이벤트 처리로 바꾸는 것이 좋다.
	private boolean layouted;
	
	private final int STORY_TIME = 1;
	private final int DISCOURSE_TIME = 2;
	private int eventOrder = DISCOURSE_TIME;
	
	private final long FOR_ALL = -2L; // 모든 캐릭터 
	/*
	 * 지식 요소 관리 
	 */
	private int infoCount = 0;
	private int knCount = 0;
	private Map<Long,ElementUI> eventBag = null;
	private Map<String,ElementUI> plotEventBag = null;
	private SortedSet<ElementUI> eventsOnStoryTime = null;
	private SortedSet<ElementUI> eventsOnDiscourseTime = null;
	private Map<Long,ElementUI> informationBag = null;
	private Map<Long,ElementUI> knowledgeBag = null;
	private ElementUI metaKnowledge = null;
	private ArrayList<LinkUI> perceptionLinks = null;
	private ArrayList<LinkUI> impactLinks = null;
	private ArrayList<LinkUI> conjunction = null;
	/*
	 * 연결선 편집용 화면 요소
	 */
	private LinkUI selectedLinker = null;
	private LinkUI tempLinker = null; 
	private ElementUI mouseOverElement = null;
	private LinkDrawingHandler<LinkUI> drawingHandler = null;
	/*
	 * 구성요소 속성 편집 관련 
	 */
	final int popupZIndex = 9;
	private int popupLeft = -1;
	private int popupTop = -1;
	private EventPropertyPanel eventProperty = null;
	private InformationPropertyPanel informationProperty = null;
	private KnowledgePropertyPanel knowledgeProperty = null;
	private PerceptionLinkPropertyPanel perceptionLinkProperty = null;
	private ImpactLinkPropertyPanel impactLinkProperty = null;
	/*
	 * 새로 만드는 요소들. 속성창에서 취소하면 바로 지워야 한다.
	 */
	private InformationEntity newInfo = null;
	private KnowledgeEntity newKnowledge = null;
	/*
	 * 컨텍스트 메뉴 관련
	 */
	private PopupPanel menuPanelOnElement = null;
	private PopupPanel menuPanelOnLink = null;
	private ElementUI contextElement = null;
	private LinkUI contextLink = null;
	
	
	public KnowledgeStructure(StoryServiceAsync service, EventBus bus) {
		super(service, bus);
		initWidget(uiBinder.createAndBindUi(this));
		informationBag = new HashMap<Long,ElementUI>();
		knowledgeBag = new HashMap<Long,ElementUI>();
		eventBag = new HashMap<Long,ElementUI>();
		plotEventBag = new HashMap<String,ElementUI>();
		eventsOnStoryTime = new TreeSet<ElementUI>(new Comparator<ElementUI>() {

			public int compare(ElementUI o1, ElementUI o2) {
				// 이것은 스토리 이벤트에만 쓴다. 만약 이벤트가 아닌 경우 맨 뒤로 보낸다.
				if (o1.getType()!=ElementUI.EVENT_STORY || o1.getElement()==null)
					return 1;
				else if (o2.getType() != ElementUI.EVENT_STORY || o2.getElement()==null)
					return -1;
				int t1 = ((EventEntity)o1.getEntity()).getOrdinalStoryIn();
				int t2 = ((EventEntity)o2.getEntity()).getOrdinalStoryIn();
				if (t1 == -1)
					t1 = Integer.MAX_VALUE;
				if (t2 == -1)
					t2 = Integer.MAX_VALUE;
				return (t1 < t2) ? -1 : ((t1 > t2) ? 1 : 0);
			}

		});
		eventsOnDiscourseTime = new TreeSet<ElementUI>(
				new Comparator<ElementUI>() {

					public int compare(ElementUI o1, ElementUI o2) {
						// 이것은 플롯 이벤트에만 쓴다. 만약 이벤트가 아닌 경우 맨 뒤로 보낸다.
						if (o1.getType()!=ElementUI.EVENT_PLOT || o1.getEntity()==null)
							return 1;
						else if (o2.getType()!=ElementUI.EVENT_PLOT || o2.getEntity()==null)
							return -1;
						EventEntity e1 = (EventEntity)o1.getEntity();
						EventEntity e2 = (EventEntity)o2.getEntity();
						double t1 = ((Interval)e1.getDiscourseInOut().toArray()[o1.plotIndex]).getBegin();
						double t2 = ((Interval)e2.getDiscourseInOut().toArray()[o2.plotIndex]).getBegin();
						return (t1 < t2) ? -1 : ((t1 > t2) ? 1 : 0);
					}

				});
		perceptionLinks = new ArrayList<LinkUI>();
		impactLinks = new ArrayList<LinkUI>();
		conjunction = new ArrayList<LinkUI>();
	}

	/**
	 * 위젯을 초기화하는데 화면 요소만 다루고 내용은 취급하지 않는다.
	 */
	protected void onLoad() {
		super.onLoad();
		// 세로 스크롤 추가
		structureContainer.getElement().getParentElement().getStyle()
		.setOverflowY(Overflow.SCROLL);
		/*
		 * 연결선을 그리기 위한 마우스 핸들러 생성 
		 */
		drawingHandler = new LinkDrawingHandler<LinkUI>(this);
		structureContainer.addDomHandler(drawingHandler, MouseMoveEvent.getType());
		structureContainer.addDomHandler(drawingHandler, MouseOutEvent.getType());
		structureContainer.addDomHandler(drawingHandler, MouseUpEvent.getType());
		structureContainer.addDomHandler(drawingHandler, MouseDownEvent.getType());
		structureContainer.addDomHandler(drawingHandler, DoubleClickEvent.getType());
		/*
		 * 속성 편집 요소들 
		 */
		informationProperty = new InformationPropertyPanel();
		knowledgeProperty = new KnowledgePropertyPanel();
		perceptionLinkProperty = new PerceptionLinkPropertyPanel();
		impactLinkProperty = new ImpactLinkPropertyPanel();
		
		informationProperty.getElement().getStyle().setZIndex(popupZIndex);
		knowledgeProperty.getElement().getStyle().setZIndex(popupZIndex);
		perceptionLinkProperty.getElement().getStyle().setZIndex(popupZIndex);
		impactLinkProperty.getElement().getStyle().setZIndex(popupZIndex);
		
		metaKnowledge = new ElementUI("Meta-Knowledge");
		metaKnowledgePanel.add(metaKnowledge);
		
		/*
		 * 컨텍스트 메뉴 요소
		 */
		structureContainer.addDomHandler(new ContextMenuHandler() {
			@Override
			public void onContextMenu(ContextMenuEvent event) {
				event.preventDefault();
			}
			
		}, ContextMenuEvent.getType());
		menuPanelOnElement = new PopupPanel();
		MenuBar elementMenu = new MenuBar(true);
		elementMenu.addItem("Edit Properties...", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				popupLeft = menuPanelOnElement.getPopupLeft();
				popupTop = menuPanelOnElement.getPopupTop();
				menuPanelOnElement.hide();
				showElementProperties(contextElement);
			}
		});
		elementMenu.addItem("Delete", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnElement.hide();
				deleteElement(contextElement);
			}
		});
		menuPanelOnElement.add(elementMenu);
		menuPanelOnElement.getElement().getStyle().setZIndex(popupZIndex);

		menuPanelOnLink = new PopupPanel() {
			public void onBrowserEvent(Event e) {
				e.preventDefault();
			}
		};
		menuPanelOnLink.sinkEvents(Event.ONCONTEXTMENU);
		MenuBar linkMenu = new MenuBar(true);
		linkMenu.addItem("Edit Properties...", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				int left = menuPanelOnLink.getPopupLeft();
				int top = menuPanelOnLink.getPopupTop();
				menuPanelOnLink.hide();
				showLinkProperties(contextLink, left, top);
			}
		});
		linkMenu.addItem("Delete", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnLink.hide();
				deleteLink(contextLink);
			}
		});
		menuPanelOnLink.add(linkMenu);
		menuPanelOnLink.getElement().getStyle().setZIndex(popupZIndex);
	}

	/**
	 * UI 작업으로 바뀐 항목 고르기 
	 */
	@Override
	public void updateChangedEntities() {
		// 플롯 요소가 바뀌면 스토리 요소도 바뀌므로 미리 스토리 요소 변경 플래그를 맞춰둔다.
		for (ElementUI e : eventBag.values()) {
			if (e.isModified()) {
				saveEntity((EventEntity)e.getEntity());
				e.invalidate();;
			}
		}
		for (ElementUI i : informationBag.values()) {
			if (i.isModified()) {
				saveEntity((InformationEntity)i.getEntity());
				i.invalidate();
			}
		}
		for (ElementUI k : knowledgeBag.values()) {
			if (k.isModified()) {
				saveEntity((KnowledgeEntity)k.getEntity());
				k.invalidate();
			}
		}
		for (LinkUI p : perceptionLinks) {
			if (p.isModified()) {
				saveEntity((PerceptionEntity)p.getEntity());
				p.invalidate();
			}
		}
		for (LinkUI s : impactLinks) {
			if (s.isModified()) {
				saveEntity((ImpactEntity)s.getEntity());
				s.invalidate();
			}
		}
	}

	/**
	 * 스토리 화면 요소들을 모두 지운다.
	 */
	@Override
	public void clear() {
		// 이벤트
		for (ElementUI event : eventBag.values()) {
			Widget w = event.getWidget(); 
			if (w.isAttached()) w.removeFromParent();
		}
		eventBag.clear();
		for (ElementUI event : plotEventBag.values()) {
			Widget w = event.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		plotEventBag.clear();
		eventsOnStoryTime.clear();
		eventsOnDiscourseTime.clear();
		// 정보
		for (ElementUI e : informationBag.values()) {
			Widget w = e.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		informationBag.clear();
		// 정보
		for (ElementUI e : informationBag.values()) {
			Widget w = e.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		informationBag.clear();
		// 지식
		for (ElementUI e : knowledgeBag.values()) {
			Widget w = e.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		knowledgeBag.clear();
		// 메타 지식
//		if (metaKnowledge != null) {
//			Widget w = metaKnowledge.getWidget();
//			if (w.isAttached()) w.removeFromParent();
//		}
		// 인식 연관
		for (LinkUI l : perceptionLinks) {
			Widget w = l.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		perceptionLinks.clear();
		// 영향 연관
		for (LinkUI l : impactLinks) {
			Widget w = l.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		impactLinks.clear();
		// 논리 연관
		for (LinkUI l : conjunction) {
			Widget w = l.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		conjunction.clear();		
	}

	/**
	 * 로컬 캐시에서 엔티티를 읽어서 편집기 요소를 갱신한다.<br>
	 * 이벤트와 이벤트에 연결된 인식 연결선을 제외한 캐릭터 목록, 정보, 지식, 연결선 등은 여기서 화면에 표시한다.
	 */
	@Override
	public void updateAll() {
		if (layouted == false) {
			// 스크롤 바를 영역 바깥에 항상 표시하여 비율을 맞춘다.
			Element parentElement = structureContainer.getElement()
					.getParentElement();
			int scrollWidth = parentElement.getOffsetWidth()
					- parentElement.getClientWidth();
			double right = Document.get().getClientWidth()
					- parentElement.getAbsoluteRight();
			parentElement.getStyle().setRight(right - scrollWidth, Unit.PX);
			layouted = true;
		}
		// 캐릭터 읽어오기
		long characterFilter = getSelectedCharacter();
		int index = 0;
		characterList.clear();
		characterList.addItem("(all)", "");
		if (characterFilter == FOR_ALL) characterList.setSelectedIndex(0);
		for (CharacterEntity c : LocalCache.entities(CharacterEntity.class, characterArrayType)) {
			characterList.addItem(c.getName(), Long.toString(c.getId()));
			if (characterFilter == c.getId()) characterList.setSelectedIndex(index);
			index++;
		}
		// 이벤트에서 빠진 것 지우기
		for (ElementUI e : eventsOnStoryTime) {
			EventEntity entity = (EventEntity)e.getEntity();
			EventEntity cache = LocalCache.get(EventEntity.class, entity.getId());
			int plotRemoveFirst = 0;
			if (cache == null) { // 삭제된 이벤트 
				//e.removeFromParent();
				eventsOnStoryTime.remove(e);
				eventBag.remove(entity.getId());
			} else
				plotRemoveFirst = cache.getOccurrence();
			// 플롯 수가 변경된 경우 나머지를 지운다. (삭제된 이벤트는 전체를 지운다.)
			for (int i=plotRemoveFirst; i<entity.getOccurrence(); i++) {
				String key = createPlotEventId(entity, i);
				ElementUI plot = plotEventBag.get(key);
				eventsOnDiscourseTime.remove(plot);
				//plot.removeFromParent();
				plotEventBag.remove(key);
			}
		}
		// 이벤트 읽어오기 
		for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) {
			ElementUI eUI = eventBag.get(e.getId());
			if (eUI == null) {
				eUI = new ElementUI(e);
				eventBag.put(e.getId(), eUI);
				if (e.isAssigned()) eventsOnStoryTime.add(eUI);				
			}
			eUI.updateWidget();
			if (e.getOccurrence() > 0) {
				for (int i = 0; i < e.getOccurrence(); i++) {
					String key = createPlotEventId(e, i);
					ElementUI plotUI = plotEventBag.get(key);
					if (plotUI == null) {
						plotUI = new ElementUI(e, i);
						plotEventBag.put(key, plotUI);
						eventsOnDiscourseTime.add(plotUI);						
					}
					plotUI.updateWidget();
				}
			}
		}
		/*
		 * 정보와 지식 요소 
		 * 이것들은 다른 곳에서 지워지지 않는다. 스토리를 바꿔서 생기는 일은 나중에 생각하자! 
		 */
		informationPanel.clear();
		for (InformationEntity info : LocalCache.entities(InformationEntity.class, informationArrayType)) {
			ElementUI iUI = informationBag.get(info.getId());
			if (iUI == null) {
				iUI = new ElementUI(info);
				informationBag.put(info.getId(), iUI);
			}
			iUI.updateWidget();
			informationPanel.add(iUI);
		}
		knowledgePanel.clear();
		for (KnowledgeEntity know : LocalCache.entities(KnowledgeEntity.class, knowledgeArrayType)) {
			ElementUI kUI = knowledgeBag.get(know.getId());
			if (kUI == null) {
				kUI = new ElementUI(know);
				knowledgeBag.put(know.getId(), kUI);
			}
			kUI.updateWidget();
			knowledgePanel.add(kUI);
		}
		/*
		 * 연결 요소
		 * FIXME: 기존 것을 모두 삭제하고 다시 추가한다. 
		 */
		for (LinkUI l : perceptionLinks) {
			l.remove();
		}
		perceptionLinks.clear();
		for (LinkUI l : impactLinks) {
			l.remove();
		}
		impactLinks.clear();
		for (PerceptionEntity e : LocalCache.entities(PerceptionEntity.class, perceptionArrayType)) {
			EventEntity event = LocalCache.get(EventEntity.class, e.getEvent());
			ElementUI from = eventBag.get(event.getId());
			ElementUI to = informationBag.get(e.getInformation());
			if (from==null || to==null) continue;
			LinkUI l = new LinkUI(e, structureContainer);
			l.setVisible(false);
			l.setWidgets(from, to);
			perceptionLinks.add(l);
			for (int i=0; i<event.getOccurrence(); i++) {
				from = plotEventBag.get(createPlotEventId(event, i));
				l = new LinkUI(e, structureContainer);
				l.setVisible(false);
				l.setWidgets(from, to);
				perceptionLinks.add(l);
			}
		}
		for (ImpactEntity e : LocalCache.entities(ImpactEntity.class, impacttArrayType)) {
			ElementUI from = informationBag.get(e.getInformation());
			ElementUI to = knowledgeBag.get(e.getKnowledge());
			if (from==null || to==null) continue;
			LinkUI l = new LinkUI(e, structureContainer);
			l.setWidgets(from, to);
			impactLinks.add(l);
		}
		conjunctMetaKnowledge();
		// 마지막으로 이벤트 요소와 연결선을 표시한다.
		rearrangeEvents();
	}

	@Override
	public void invalidate() {
		/*
		 * 연결선 선택을 없애고 임시 연결선도 감춘다.
		 * 속성창들은 모달 모드로 뜨므로 그냥 둔다.
		 */
		if (tempLinker!=null && tempLinker.isVisible()) {
			tempLinker.setVisible(false);
			if (selectedLinker != null) selectedLinker.setVisible(true);
			ElementUI fromElement = tempLinker.getFromElement();
			ElementUI toElement = tempLinker.getToElement();
			if (fromElement != null) fromElement.release();
			else if (toElement != null) toElement.release();
			fromElement = toElement = null;
		}
		if (selectedLinker != null) {
			selectedLinker.update();
			selectedLinker = null;
		}
	}
	
	/**
	 * 캐릭터 필터에서 선택된 캐릭터 ID를 얻는다.
	 * @return
	 */
	protected long getSelectedCharacter() {
		long character = FOR_ALL;
		long characterSelected = characterList.getSelectedIndex();
		if (characterSelected != -1) {
			String id = characterList.getValue(characterList.getSelectedIndex());
			if (!id.isEmpty()) character = Long.parseLong(id);
		}
		return character;
	}

	/*---------------------------------------------------------------
	 * 이벤트 관련 메쏘드 
	 */
	
	/**
	 * 동일 이벤트가 담화에서 여러 번 반복될 경우 반복 순서를 이름에 붙여 준다.
	 * @param event			반복되는 이벤트 요소 
	 * @param occurrence	반복 순서 
	 * @return	반복 순서를 붙인 이름 
	 */
	String createPlotEventId(EventEntity event, int occurrence) {
		return Long.toString(event.getId()) + " #" + occurrence; 
	}

	/**
	 * 캐릭터 선택, 시간축 변경 등으로 이벤트 목록이 달라지는 것을 처리하여 화면에 표시한다.
	 */
	void rearrangeEvents() {
		// 캐릭터 선택 얻어오기 
		long characterFilter = getSelectedCharacter();
		/*
		 * 이벤트 목록 정리 
		 */
		eventPanel.clear();
		SortedSet<ElementUI> eventSet = (eventOrder==DISCOURSE_TIME) ? eventsOnDiscourseTime : eventsOnStoryTime;
		for (ElementUI e : eventSet) {
			if (characterFilter == -2) eventPanel.add(e);
			else {
				EventEntity event = (EventEntity)e.getEntity();
				if (characterFilter == event.getMainCharacter() || 
						event.getInvolvedCharacters().contains(Long.valueOf(characterFilter))) {
					eventPanel.add(e);
				}
			}
		}
		/* 
		 * 이벤트에 연결된 인식 연결선 정리 
		 */
		for (LinkUI l : perceptionLinks) {
			// 이벤트 요소가 먼저 그려지므로 DOM에 추가된 것으로 화면표시를 판별한다.
			boolean visible = l.getFromElement().getParent() == eventPanel;
			l.setVisible(visible);
		}
	}
	
	public void showElementContextMenu(ElementUI element, int x, int y) {
		if (element.getType() != ElementUI.META_KNOWLEDGE) {
			contextElement = element;
			menuPanelOnElement.setPopupPosition(x, y);
			menuPanelOnElement.show();
		} else {
			contextElement = null;
		}
	}
	
	@Override
	public void showEventProperties(EventEntity event) {
		if (eventProperty == null) {
			eventProperty = new EventPropertyPanel(this);
			eventProperty.setMode(PropertyPanel.MODAL_OK_CANCEL);
			eventProperty.getElement().getStyle().setZIndex(popupZIndex);
		}
		eventProperty.setData(event);
		eventProperty.setPopupPosition(popupLeft, popupTop);
		eventProperty.show();
	}

	@Override
	public void updateEventProperties(EventEntity event) {
		// TODO 화면에 영향을 주는 것은 캐릭터 할당에 따른 색깔 변경과 캐릭터 필터로 인한 표시 여부 변경이다.
		
	}
	
	/*---------------------------------------------------------------
	 * 지식 요소 관련 메쏘드 
	 */
	
	/**
	 * 지식 요소 위에 마우스 포인터가 올려지면 그 요소를 등록시켜 둔다.
	 * @param e
	 * @param over
	 */
	public void setMouseOverElement(ElementUI e, boolean over) {
		if (over) mouseOverElement = e;
		else if (mouseOverElement == e) mouseOverElement = null;
	}
	
	/**
	 * 지식 요소에 대한 속성창 띄우기 
	 * @param e
	 */
	public void showElementProperties(ElementUI e) {
		switch (e.getType()) {
		case ElementUI.EVENT:
		case ElementUI.EVENT_PLOT:
		case ElementUI.EVENT_STORY:
			showEventProperties((EventEntity)e.getEntity());
			break;
		case ElementUI.INFORMATION:
			informationProperty.setData((InformationEntity)e.getEntity());
			informationProperty.setPopupPosition(popupLeft, popupTop);
			informationProperty.show();
			break;
		case ElementUI.KNOWLEDGE:
			knowledgeProperty.setData((KnowledgeEntity)e.getEntity());
			knowledgeProperty.setPopupPosition(popupLeft, popupTop);
			knowledgeProperty.show();
			break;
		}
	}
	
	public void updateInformation(InformationEntity info, boolean saved) {
		if (info == newInfo) {
			newInfo = null;
			// 취소했을 경우 임시로 만든 것을 삭제한다.
			if (!saved) { 
				deleteEntity(info);
				return;
			}
			LocalCache.add(info);
		} else {
			/*
			 * 요소 선택 표시는 마우스 오버/아웃 핸들러로 처리했다.
			 * 그러므로 대화상자 등으로 마우스 이벤트 처리가 막히면 선택 해제가 안 되므로 강제로 '무조건' 해제한다.
			 * 연결선 요소는 매번 좌표로 계산하고 동시에 하나만 선택되므로 지금 상태로 괜찮다.
			 */
			ElementUI infoUI = informationBag.get(info.getId());
			infoUI.removeStyleName(style.selected());			
		}
		if (saved) {
			ElementUI infoUI = informationBag.get(info.getId());
			if (infoUI == null) {
				infoUI = new ElementUI(info);
				informationBag.put(info.getId(), infoUI);
				informationPanel.add(infoUI);
				infoCount++;
			}
			infoUI.modify();
		} 
	}
	
	public void updateKnowledge(KnowledgeEntity knowledge, boolean saved) {
		if (knowledge == newKnowledge) { 
			newKnowledge = null;
			// 임시로 만든 것을 삭제한다.
			if (!saved) {
				deleteEntity(knowledge);
				return;
			}
			LocalCache.add(knowledge);
		} else {
			/*
			 * 요소 선택 표시는 마우스 오버/아웃 핸들러로 처리했다.
			 * 그러므로 대화상자 등으로 마우스 이벤트 처리가 막히면 선택 해제가 안 되므로 강제로 '무조건' 해제한다.
			 * 연결선 요소는 매번 좌표로 계산하고 동시에 하나만 선택되므로 지금 상태로 괜찮다.
			 */
			ElementUI knowUI = knowledgeBag.get(knowledge.getId());
			knowUI.removeStyleName(style.selected());
		}
		if (saved) {
			ElementUI knowUI = knowledgeBag.get(knowledge.getId());
			if (knowUI == null) {
				knowUI = new ElementUI(knowledge);
				knowledgeBag.put(knowledge.getId(), knowUI);
				knowledgePanel.add(knowUI);
				knCount++;
			} else {
				knowUI.updateWidget();
			}
			knowUI.modify();
			conjunctMetaKnowledge();
		}
	}
	
	public void deleteElement(ElementUI element) {
		switch (element.getType()) {
		case ElementUI.EVENT_STORY: {
			// 연결선 
			for (LinkUI l : perceptionLinks) {
				if (l.getFromElement().equals(element)) {
					perceptionLinks.remove(l);
					l.remove();
				}
			}
			eventBag.remove(element);
			element.removeFromParent();
			// 플롯 요소 모두 지우기
			EventEntity entity = (EventEntity)element.getEntity();
			for (int i=0; i<entity.getOccurrence(); i++) {
				String id = createPlotEventId(entity, i);
				ElementUI eUI = plotEventBag.remove(id);
				for (LinkUI l : perceptionLinks) {
					if (l.getFromElement().equals(eUI)) {
						perceptionLinks.remove(l);
					}
				}
			}
			// 삭제 요청
			deleteEntity(entity);
			break;
		}
		case ElementUI.EVENT_PLOT: {
			// 연결선 
			for (LinkUI l : perceptionLinks) {
				if (l.getFromElement().equals(element)) {
					perceptionLinks.remove(l);
					l.remove();
				}
			}
			plotEventBag.remove(element);
			element.removeFromParent();
			// 나머지 플롯 당기기
			EventEntity entity = (EventEntity)element.getEntity();
			int plotIndex = element.plotIndex;
			for (int i=plotIndex+1; i<entity.getOccurrence(); i++) {
				String id = createPlotEventId(entity, i);
				ElementUI eUI = plotEventBag.remove(id);
				String name = entity.getName();
				if (entity.getOccurrence() > 1) name += " #" + (i-1);
				eUI.setText(name);
				id = createPlotEventId(entity, i-1);
				plotEventBag.put(id, eUI);
			}
			Iterator<Interval> iter = entity.getDiscourseInOut().iterator();
			for (int i=0; i<plotIndex; i++) {
				iter.next();
			}
			entity.getDiscourseInOut().remove(iter.next());
			// 스토리 요소 속성 변경
			ElementUI eUI = eventBag.get(element.getId());
			eUI.updateWidget();
			eUI.modify();
			// FIXME: 변경 저장 요청
			applyChanges();
			break;
		}
		case ElementUI.INFORMATION: {
			// 연결선 
			for (LinkUI l : perceptionLinks) {
				if (l.getToElement().equals(element)) {
					perceptionLinks.remove(l);
					l.remove();
				}
			}
			for (LinkUI l : impactLinks) {
				if (l.getFromElement().equals(element)) {
					perceptionLinks.remove(l);
					l.remove();
				}
			}
			informationBag.remove(element);
			element.removeFromParent();
			deleteEntity((InformationEntity)element.getEntity());
			break;
		}
		case ElementUI.KNOWLEDGE: {
			// 연결선
			for (LinkUI l : impactLinks) {
				if (l.getToElement().equals(element)) {
					impactLinks.remove(l);
					l.remove();
				}
			}
			knowledgeBag.remove(element);
			element.removeFromParent();
			conjunctMetaKnowledge();
			deleteEntity((KnowledgeEntity)element.getEntity());
		}
		}
	}
	
	/*---------------------------------------------------------------
	 * 연결요소 관련 메쏘드 
	 */
	
	@Override
	public LinkUI getSelectedLink() {
		return selectedLinker;
	}
	@Override 
	public void setSelectedLink(LinkUI link) {
		selectedLinker = link;
	}
	/**
	 * 마우스 좌표로 연결 요소를 고른다.
	 * @param x	마우스 X 좌표 
	 * @param y	마우스 Y 좌표 
	 * @return	마우스 좌표 위치에 놓인 연결 요소. 없으면 null을 반환한다.
	 */
	@Override
	public LinkUI pickLink(int x, int y) {
		final int MARGIN = 5;
		int offsetX = CssUtil.getOffsetLeft(eventPanel,  structureContainer) + 
				eventPanel.getOffsetWidth() - MARGIN;
		if (x < offsetX) return null;
		offsetX = CssUtil.getOffsetLeft(informationPanel, structureContainer) + MARGIN;
		if (x < offsetX) {
			for (LinkUI l : perceptionLinks) {
				if (l.isVisible() && l.hitTest(x, y)) return l;
			}
			return null;
		}
		offsetX += informationPanel.getOffsetWidth() - 2*MARGIN;
		if (x < offsetX) return null;
		offsetX = CssUtil.getOffsetLeft(knowledgePanel, structureContainer) + MARGIN;
		if (x < offsetX) {
			for (LinkUI l : impactLinks) {
				if (l.hitTest(x, y)) return l;
			}
			return null;
		}
		return null;
	}
	
	/**
	 * 시작 요소를 고정하고 연결선 긋기를 시작한다. 
	 * 일반적인 연결선 작업과 선택한 연결선의 끝점을 잡아 움직이는 경우에 해당한다.
	 * 
	 * @param from	시작 요소 
	 * @param toX	그리고 있는 선 끝의 가로 좌표 
	 * @param toY	그리고 있는 선 끝의 세로 좌표 
	 */
	@Override
	public void startDrawingConnection(Widget from, int toX, int toY) {
		drawingHandler.setFrom(from);
		if (tempLinker == null) tempLinker = new LinkUI(structureContainer);
		if (selectedLinker != null) selectedLinker.setVisible(false);
		drawConnection(from, toX, toY);
	}

	/**
	 * 끝 요소를 고정하고 연결선 긋기를 시작한다.
	 * 선택한 연결선의 시작점을 잡아 움직이는 경우에 해당한다.
	 * 
	 * @param fromX	그리고 있는 선의 한쪽 가로 좌표 
	 * @param fromY	그리고 있는 선의 한쪽 세로 좌표 
	 * @param to	기존 연결선의 끝 요소 
	 */
	@Override
	public void startDrawingConnection(int fromX, int fromY, Widget to) {
		drawingHandler.setTo(to);
		if (tempLinker == null) tempLinker = new LinkUI(structureContainer);
		else tempLinker.setVisible(true);
		if (selectedLinker != null) selectedLinker.setVisible(false);
		drawConnection(fromX, fromY, to);
	}

	/**
	 * 시작 요소를 고정하고 긋는 연결선 표시하기.
	 * 마우스가 다른 요소 위에 있으면 그 요소와 연결한다. 
	 *  
	 * @param from	시작 요소 
	 * @param toX	현재 마우스 위치 
	 * @param toY	현재 마우스 위치
	 */
	@Override
	public void drawConnection(Widget from, int toX, int toY) {
		if (tempLinker == null) return;
		ElementUI to = mouseOverElement;//pickElement(toX, toY);
		if (from == to) tempLinker.setVisible(false);
		else tempLinker.setVisible(true);
		if (to != null) tempLinker.setWidgets(from, to);
		else tempLinker.setWidgets(from, toX, toY);
	}
	/**
	 * 끝 요소를 고정하고 그리는 연결선 표시하기.
	 * 마우스가 다른 요소 위에 있으면 그 요소와 연결한다.
	 * 
	 * @param fromX	현재 마우스 위치 
	 * @param fromY	현재 마우스 위치 
	 * @param to	끝 요소 
	 */
	@Override
	public void drawConnection(int fromX, int fromY, Widget to) {
		if (tempLinker == null) return;
		ElementUI from = mouseOverElement;//pickElement(fromX, fromY);
		if (from == to) tempLinker.setVisible(false);
		else tempLinker.setVisible(true);
		if (from != null) tempLinker.setWidgets(from, to);
		else tempLinker.setWidgets(fromX, fromY, to);
	}

	@Override
	public void finishDrawingConnection() {
		finishDrawingConnection(null);
	}
	/**
	 * 연결선 그리기를 끝마친다.
	 * 
	 * @param target	새로 연결한 요소. 그리기 방식에 따라 시작 또는 끝 요소가 될 수 있다. 
	 * 					null인 경우에는 그리던 연결선을 취소한다.
	 */
	void finishDrawingConnection(ElementUI target) {
		if (drawingHandler != null) {
			drawingHandler.clear();
		}
		if (selectedLinker != null) {
			selectedLinker.setVisible(true);
			selectedLinker.select();		
		}
		if (tempLinker==null || !tempLinker.isVisible()) return;
		if (target == null) {
			tempLinker.setVisible(false);
			return;
		} 
		ElementUI fromElement = tempLinker.getFromElement();
		ElementUI toElement = tempLinker.getToElement();
		if (selectedLinker != null) {
			if (fromElement != selectedLinker.getFromElement() ||
				 toElement != selectedLinker.getToElement()) {
				selectedLinker.setWidgets(fromElement, toElement);
				selectedLinker.modify();
			}
		} else {
			createLink(fromElement, toElement);
		}
		if (target == fromElement) {
			toElement.release();
		} else {
			fromElement.release();
		}
	}
	
	/**
	 * 연결 요소로부터 연결선 찾기.
	 * 
	 * @param from	시작 요소
	 * @param to	끝 요소
	 * @return	해당 연결선. 없으면 null.
	 */
	LinkUI findLink(ElementUI from, ElementUI to) {
		switch (from.getType()) {
		case ElementUI.EVENT:
		case ElementUI.EVENT_PLOT:
		case ElementUI.EVENT_STORY:
			for (LinkUI l : perceptionLinks) {
				if (l.getFromElement()==from && l.getToElement()==to) {
					return l;
				}
			}
			break;
		case ElementUI.INFORMATION:
			for (LinkUI l : impactLinks) {
				if (l.getFromElement()==from && l.getToElement()==to) {
					return l;
				}
			}
			break;
		case ElementUI.KNOWLEDGE:
			for (LinkUI l : conjunction) {
				if (l.getFromElement()==from && l.getToElement()==to) {
					return l;
				}
			}
		}
		return null;
	}
	
	/**
	 * 지식구조 연결 관계를 추가하는데 from, to 타입이 보장되지 않으므로 방향을 확인하고 추가한다.
	 * @param from
	 * @param to
	 */
	void createLink(ElementUI from, ElementUI to) {
		final float DefaultPerecptValue = 1.0F;
		LinkUI link = null;
		if (findLink(from, to) != null) return;
		switch (from.getType()) {
		case ElementUI.EVENT:
		case ElementUI.EVENT_PLOT:
		case ElementUI.EVENT_STORY:
			if (to.getType() == ElementUI.INFORMATION) {
				link = new LinkUI(LinkUI.PERCEPTION, structureContainer);
				link.setWidgets(from, to);
			}
			break;
		case ElementUI.INFORMATION:
			if ((to.getType()&ElementUI.EVENT) != 0) {
				link = new LinkUI(LinkUI.PERCEPTION, structureContainer);
				link.setWidgets(to, from);
			} else if (to.getType() == ElementUI.KNOWLEDGE) {
				link = new LinkUI(LinkUI.IMPACT, structureContainer);
				link.setWidgets(from, to);
			}
			break;
		case ElementUI.KNOWLEDGE:
			if (to.getType() == ElementUI.INFORMATION) {
				link = new LinkUI(LinkUI.IMPACT, structureContainer);
				link.setWidgets(to, from);
			} else if (to.getType() == ElementUI.META_KNOWLEDGE) {
				link = new LinkUI(LinkUI.CONJUNCT, structureContainer);
				link.setWidgets(from, to);
			}
			break;
		case ElementUI.META_KNOWLEDGE:
			if (to.getType() == ElementUI.KNOWLEDGE) {
				link = new LinkUI(LinkUI.CONJUNCT, structureContainer);
				link.setWidgets(to, from);
			}
			break;
		}
		if (link != null) {
			link.getWidget().addDomHandler(new ContextMenuHandler() {
				@Override
				public void onContextMenu(ContextMenuEvent event) {
					event.preventDefault();
				}
				
			}, ContextMenuEvent.getType());
			link.modify();
			switch (link.getType()) {
			case LinkUI.PERCEPTION:
				perceptionLinks.add(link);
				long eventId = ((EventEntity)link.getFromElement().getEntity()).getId();
				long infoId = ((InformationEntity)link.getToElement().getEntity()).getId();
				PerceptionEntity e = PerceptionEntity.get(eventId, infoId);
				if (e != null) {
					link.entity = e;
				}
				else {
					final LinkUI newLink = link;
					storyService.createNewPerception(new AsyncCallback<PerceptionEntity>() {
	
						@Override
						public void onFailure(Throwable arg0) {
							// TODO Auto-generated method stub
							
						}
	
						@Override
						public void onSuccess(PerceptionEntity e) {
							LocalCache.add(e);
							e.setEvent(((EventEntity)newLink.getFromElement().getEntity()).getId());
							e.setInformation(((InformationEntity)newLink.getToElement().getEntity()).getId());
							e.initializePerecptValues(DefaultPerecptValue);
							newLink.entity = e;
						}
						
					});
				}
				break;
			case LinkUI.IMPACT:
				impactLinks.add(link);
				final LinkUI newLink = link;
				storyService.createNewImpact(new AsyncCallback<ImpactEntity>() {

					@Override
					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onSuccess(ImpactEntity e) {
						LocalCache.add(e);
						e.setInformation(((InformationEntity)newLink.getFromElement().getEntity()).getId());
						e.setKnowledge(((KnowledgeEntity)newLink.getToElement().getEntity()).getId());
						newLink.entity = e;
					}
					
				});
				break;
			}
		}
		
	}
	
	/**
	 * 연결선에 대한 속성창을 띄운다.
	 * @param l
	 */
	@Override
	public void showLinkProperties(LinkUI l, int left, int top) {
		popupLeft = left;
		popupTop = top;
		switch (l.getType()) {
		case LinkUI.PERCEPTION:
			perceptionLinkProperty.setData(l);
			perceptionLinkProperty.setPopupPosition(popupLeft, popupTop);
			perceptionLinkProperty.show();
			break;
		case LinkUI.IMPACT:
			impactLinkProperty.setData((ImpactEntity)l.getEntity());
			impactLinkProperty.setPopupPosition(popupLeft, popupTop);
			impactLinkProperty.show();
			break;
		}
	}

	/**
	 * 선택된 연결선의 속성창을 불러 내용을 변경하면 이 메쏘드를 불러 수정 표시를 한다.
	 */
	public void updateLink() {
		if (selectedLinker != null) {
			selectedLinker.updateWidget();
			selectedLinker.modify();
		}
	}
	
	/**
	 * 메타-지식은 참인 지식들의 조합이므로 지식 목록에 따라 자동으로 연결한다.
	 */
	void conjunctMetaKnowledge() {
		for (LinkUI l : conjunction) {
			l.remove();
		}
		conjunction.clear();
		for (ElementUI e : knowledgeBag.values()) {
			KnowledgeEntity k = (KnowledgeEntity)e.getEntity();
			if (k.getTruth()) {
				LinkUI l = new LinkUI(LinkUI.CONJUNCT, structureContainer);
				l.setWidgets(e, metaKnowledge);
				conjunction.add(l);
			}
		}
	}
	
	@Override
	public void showLinkContextMenu(LinkUI link, int x, int y) {
		contextLink = link;
		menuPanelOnLink.setPopupPosition(x, y);
		menuPanelOnLink.show();
	}
	
	public void deleteLink(LinkUI link) {
		if (selectedLinker == link) {
			selectedLinker = null;
		}
		switch (link.getType()) {
		case LinkUI.PERCEPTION:
			perceptionLinks.remove(link);
			link.remove();
			deleteEntity((PerceptionEntity)link.getEntity());
			break;
		case LinkUI.IMPACT:
			impactLinks.remove(link);
			link.remove();
			deleteEntity((ImpactEntity)link.getEntity());
			break;
		}
	}
	
	@UiField
	MyStyle style;
	@UiField
	LayoutPanel structureContainer;
	@UiField
	FlowPanel perceptLinkContainer;
	@UiField
	FlowPanel supportLinkContainer;
	@UiField
	FlowPanel conjunctLinkContainer;
	@UiField
	ListBox characterList;
	@UiField
	FlowPanel eventPanel;
	@UiField
	FlowPanel informationPanel;
	@UiField
	FlowPanel knowledgePanel;
	@UiField
	FlowPanel metaKnowledgePanel;
	@UiField
	ListBox eventOrderList;

	@UiHandler("newInfoButton")
	void onClickNewInfo(ClickEvent e) {
		String infoName = "Information " + (infoCount + 1);
		// 서버에 요청
		storyService.createNewInformation(infoName, new AsyncCallback<InformationEntity>() {

			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(InformationEntity info) {
				newInfo = info;
				// 속성창을 열고 입력을 받아서 새로 만든다.
				informationProperty.setData(info);
				informationProperty.center();
			}
		});
	}

	@UiHandler("newKnowledgeButton")
	void onClickNewKnowledge(ClickEvent e) {
		String knowName = "Knowledge " + (knCount + 1);
		storyService.createNewKnowledge(knowName, new AsyncCallback<KnowledgeEntity>() {

			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(KnowledgeEntity know) {
				newKnowledge = know;
				// 속성창을 열고 입력을 받아서 새로 만든다.
				knowledgeProperty.setData(know);
				knowledgeProperty.center();
			}
			
		});
	}

	@UiHandler("characterList")
	void onChangeCharacter(ChangeEvent e) {
		rearrangeEvents();
	}
	
	@UiHandler("eventOrderList")
	void onChangeEventOrder(ChangeEvent e) {
		int select = eventOrderList.getSelectedIndex();
		int intValue = Integer.parseInt(eventOrderList.getValue(select));
		switch (intValue) {
		case STORY_TIME:
			eventOrder = STORY_TIME;
			break;
		case DISCOURSE_TIME:
			eventOrder = DISCOURSE_TIME;
			break;
		}
		rearrangeEvents();
	}

}

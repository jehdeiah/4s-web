package com.googlecode._4s_web.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
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
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode._4s_web.client.StoryServiceAsync;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.EventRelationEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.client.entity.StoryTimePoint;

/**
 * 캐릭터와 이벤트를 만들고 스토리 시간축에 배열하는 스토리 타임라인 편집기.
 * <p>
 * 이벤트에 캐릭터를 할당하지 않으면 모두 '해프닝'으로 본다. (15-DEC-14)
 * 이제까지는 해프닝도 별도의 캐릭터로 보았으나 굳이 그럴 필요가 없겠다.
 * 
 * TODO:
 * 시점 관리 (annotation)
 *  - 이벤트를 움직일 때마다 매번 새로 하지 않고 차이만 저장할 수 있을 것 같다.
 *  - 어노테이션에 데한 자료 구조도 생각해 볼 것.
 *  
 * 이벤트 인과관계 연결선
 * 	- 연결선 이벤트 처리를 위해 이벤트 상자를 캐릭터 라인이 아닌 타임라인 패널에 배치해야 한다.
 * 	- 연결선이 다른 캐릭터들의 이벤트들을 연결할 수도 있으므로 연결선은 패널에 위치해야 한다.
 * 	- 이 경우, 이벤트가 패널에 있는 캐릭터 타임라인 안에 있게 되어 DOM 이벤트가 전달되지 않는다.
 *   
 * @author jehdeiah
 *
 */
public class StoryTimeline extends AbstractStoryPanel 
		implements HasEventPropertyPanel, HasCharacterPropertyPanel,
						HasLinkDrawingHandler<StoryTimeline.EventLinkUI> {

	private static StoryTimelineUiBinder uiBinder = GWT
			.create(StoryTimelineUiBinder.class);

	interface StoryTimelineUiBinder extends UiBinder<Widget, StoryTimeline> {
	}
	
	/*
	 * 레이아웃 상수들
	 */
	private static final int CharacterColumnWidthPX = 150;
	private static final int EventWidthPX = 100;
	private static final int MarginBottomEventPX = 10;

	interface MyStyle extends CssResource {
		String event();			// 기본 이벤트 스타일
		String event_new();		// 새 이벤트 풀
		String event_unsorted();	// 담화 시간은 있으나 스토리 시간이 없는 이벤트 풀
		String event_sorted();	// 스토리 타임라인에 배치된 이벤트  
		String event_plotted();	// 담화에 놓인 이벤트 스타일
		String event_selected();	// 마우스가 올라가 있거나 선택된 이벤트
		
		String character_header();
		String character_timeline();

		String timepoint_line();
		String timepoint_line_selected();
		String timepoint_marker();
		String timepoint_text();
	}

	// Default character names...
	private static final String MAIN_CHARACTER = "Main Character";
	private static final String HAPPENINGS = "Happenings\n(None)";
	
	/**
	 * 여러 이벤트 처리기와 하위 클래스에서 참조할 수 있도록 final로 선언한다.
	 */
	final StoryTimeline thisPanel = this;

	/**
	 * 이벤트 상자에 대한 마우스 이벤트 핸들러.
	 * 크기 조절뿐만 아니라 연결선을 긋는 처리도 한다.
	 * 
	 * @author jehdeiah
	 *
	 */
	class EventMouseHandler extends ResizeMouseHandler 
			implements MouseOverHandler, MouseOutHandler {

		Widget linkPanel = thisPanel.timelinePanel;
		EventUI eventUI;
		/**
		 * 마우스 버튼을 누른 상태로 연결선을 그을 준비가 된 상태를 나타낸다. 
		 * 실제 선긋기는 이벤트 상자를 벗어나면 시작한다.
		 */
		boolean readyToLink = false;
		/**
		 * DragStart 이벤트 발생을 위해 MouseMove 이벤트 전파를 막을 필요가 있는 경우를 나타낸다.
		 */
		boolean needStop = false;
		
		public EventMouseHandler(EventUI e, Widget container) {
			super(e, container, ResizeMouseHandler.RESIZE_VERTICAL);
			eventUI = e;
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
				needStop = true;
				int x = event.getX();
				if (x <= ResizeMargin || (eventUI.getOffsetWidth()-x) <= ResizeMargin) {
					readyToLink = true;
					event.preventDefault();
					eventUI.getElement().getStyle().setCursor(Cursor.CROSSHAIR);
					return;
				}
			}
			readyToLink = false;
			super.onMouseDown(event);
		}

		/*
		 * 연결선이 끝나는 상자에서 불린다.
		 */
		@Override
		public void onMouseUp(MouseUpEvent event) {
			needStop = false;
			readyToLink = false;
			if (thisPanel.isDrawingConnection()) {
				event.preventDefault();
				eventUI.release();
				eventUI.getElement().getStyle().setCursor(Cursor.AUTO);
				thisPanel.finishDrawingConnection(eventUI);
			} else {
				super.onMouseUp(event);
			}
		}

		/**
		 * 크기 조절과 연결선을 긋기 위한 핸들러이지만 드래그-드롭도 해야 하므로
		 * dragstart가 불릴 수 있도록 이벤트 전파를 막아야 한다.
		 * 상자 안에서 끌면 드래그를 시작하고, 
		 * 가로선에서는 크기 변경을 하고 세로선 부근에서는 연결선을 긋도록 해야 하므로 
		 * 포인터 모양을 바꿔서 알려준다.
		 * 연결선을 긋는 것은 이 상자 밖이므로 컨테이너에서 한다. 
		 */
		@Override
		public void onMouseMove(MouseMoveEvent event) {
			if (thisPanel.isDragging()) {
				needStop = false;
				return;
			}
			if (thisPanel.isDrawingConnection()) {
				readyToLink = false;
				eventUI.getElement().getStyle().setCursor(Cursor.CROSSHAIR);
				return;
			}
			int x = event.getX();
			if (readyToLink || resizeMode==RESIZE_NONE && 
					(x <= ResizeMargin || (eventUI.getOffsetWidth()-x) <= ResizeMargin)) {
				eventUI.getElement().getStyle().setCursor(Cursor.CROSSHAIR);
				return;
			}
			super.onMouseMove(event);
			if (thisPanel.isResizing()) {
				needStop = false;
				eventUI.getElement().getStyle().setCursor(Cursor.ROW_RESIZE);
			}
			/*
			 * dragstart 이벤트는 mousemove 이벤트 처리가 끝난 뒤에 불린다.
			 * mousemove 이벤트는 상위 요소들까지 차례로 전파된다.
			 * 그러므로 이 이벤트의 전파를 막아야 바로 dragstart를 받아볼 수 있다.
			 */
			if (needStop) {
				event.stopPropagation();
			}
		}
		
		/**
		 * 이벤트 상자 위로 마우스가 오면 선택 표시를 하는데,
		 * 크기 조절 중에 다른 이벤트를 지나가게 되면 지나치는 이벤트는 표시하지 않는다.
		 */
		@Override
		public void onMouseOver(MouseOverEvent event) {
			if (!thisPanel.isResizing()) {
				eventUI.addStyleName(thisPanel.style.event_selected());
				thisPanel.setMouseOverEvent(eventUI, true);
			}
		}

		/**
		 * 상자 밖으로 나가면 비로소 연결선을 그리기 시작한다.
		 */
		@Override
		public void onMouseOut(MouseOutEvent event) {
			if (readyToLink && !thisPanel.isDrawingConnection()) {
				thisPanel.setMouseOverEvent(eventUI, false);
				thisPanel.startDrawingConnection(eventUI,
						event.getRelativeX(linkPanel.getElement()), 
						event.getRelativeY(linkPanel.getElement()));
				readyToLink = false;
			} 
			if (!thisPanel.isResizing()) {
				eventUI.removeStyleName(thisPanel.style.event_selected());
				thisPanel.setMouseOverEvent(eventUI, false);
			}
		}

		@Override
		protected void resizeWidget(int top, int bottom, int left,
				int right, int mouseX, int mouseY) {
			eventUI.updateWidget(top, bottom);
			thisPanel.hilightTimePoint(mouseY);
		}

		@Override
		protected void notifyResizeStarted() {
			thisPanel.setResizingEvent(eventUI);
		}
		
		@Override
		protected void notifyResizeDone() {
			thisPanel.rearrange(eventUI, false);
			thisPanel.setResizingEvent(null);
		}
		
	}
	
	/**
	 * 이벤트의 화면 표시기
	 * 
	 * @author jehdeiah
	 * 
	 */
	 class EventUI extends Label implements EntityUIObject<EventEntity> {
		EventEntity event;
		boolean modified = false;
		EventMouseHandler resizeAndLinkHandler = null;
		ArrayList<HandlerRegistration> eventMouseHR = null;
	
		public EventUI(EventEntity e) {
			event = e;
		}
		@Override
		public EventEntity getEntity() {
			return event;
		}
		
		/**
		 * 연결된 이벤트 엔티티만 바꾸고 화면 요소에 반영하지는 않는다.
		 * @param e 이벤트 엔티티 
		 */
		public void setEntity(EventEntity e) {
			if (event.getId() == e.getId()) {
				event = e;
			}
		}
	
		@Override
		public long getId() {
			return (event==null) ? -1 : event.getId();
		}
		public String getStart() {
			if (isAttached() && event.isAssigned()) {
				int y = getWidgetTop();
				return thisPanel.getTimePoint(y);
			}
			return null;
		}
	
		public String getEnd() {
			if (isAttached() && event.isAssigned()) {
				int y = getWidgetBottom();
				return thisPanel.getTimePoint(y);
			}
			return null;
		}
	
		@Override
		public Widget getWidget() {
			return this;
		}
	
		public int getWidgetTop() {
			if (isAttached()) {
				return getElement().getOffsetTop();
			}
			return 0;
		}
	
		public int getWidgetBottom() {
			if (isAttached()) {
				return getElement().getOffsetTop()
						+ getElement().getOffsetHeight();
			}
			return 0;
		}
	
		// 컨텍스트 메뉴를 띄우기 위해 브라우저 이벤트를 끌어온다.
		@Override
		public void onBrowserEvent(com.google.gwt.user.client.Event e) {
			switch (e.getTypeInt()) {
			case com.google.gwt.user.client.Event.ONCONTEXTMENU:
				e.stopPropagation();
				e.preventDefault();
				thisPanel.showEventContextMenu(event.getId(),
						e.getClientX(), e.getClientY());
				break;
			default:
				super.onBrowserEvent(e);
			}
		}
	
		@Override
		protected void onLoad() {
			setText(event.getName());
			getElement().setId("event_" + Long.toString(event.getId()));
			sinkEvents(Event.ONCONTEXTMENU);
			setStyleName(thisPanel.style.event());
			// 드래그 이벤트 처리
			getElement().setDraggable(Element.DRAGGABLE_TRUE);
			final EventUI thisEvent = this;
			EventDragHandler h = new EventDragHandler(event.getId(), this) {
				public void notifyDragStart(long eventId, int offsetX, int offsetY) {
					thisPanel.setDraggingEvent(thisEvent);
					thisPanel.setDragOffset(offsetY);
				}
			};
			h.setDragMargin(EventDragHandler.DRAG_MARGIN,
					EventDragHandler.DRAG_MARGIN, 0, 0);
			addDragStartHandler(h);
			addDragEndHandler(h);
			// 더블 클릭으로 속성창을 띄운다.
			addDoubleClickHandler(new DoubleClickHandler() {
				public void onDoubleClick(DoubleClickEvent e) {
					thisPanel.showEventProperties(thisEvent.getEntity());
				}
			});
			// applying secondary styles...
			updateWidget();
		}
		
		/**
		 * 속성편집 등으로 외부에서 엔티티 내용이 바꼈을 경우 화면에 반영한다.
		 */
		@Override
		public void updateFromEntity() {
			// 이벤트 화면 요소 자체적으로 할 수 있는 건 이름 변경밖에 없다.
			// 할당 변경은 편집기 단에서 처리해야 한다.
			if (getText().equals(event.getName()) == false) {
				setText(event.getName());
				///modified = true;
			}
			updateWidget();
		}
		
		public void assign(long actorId) {
			if (event.getMainCharacter() != actorId) {
				event.setMainCharacter(actorId);
				modified = true;
			}
		}
		
		@Override
		public void updateWidget() {
			/*
			 * 이벤트 배치 규칙
			 * (1) 스토리와 담화 시간이 모두 없는 경우 (당연히 캐릭터도 없음!) : 새 이벤트 풀
			 * (2) 스토리 시간이 있는 경우: 캐릭터 타임라인
			 *     캐릭터가 없는 것은 자동으로 해프닝으로 처리한다.
			 * (3) 담화 시간은 있으나 스토리 시간이 없는 경우 (캐릭터가 할당되었을 수도 있음) : 정렬되지 않은 이벤트 풀
			 */
			if (!event.isInStory()) {
				getElement().getStyle().clearLeft();
				getElement().getStyle().clearTop();
				getElement().getStyle().clearHeight();
			}
			setStyleName(thisPanel.style.event_sorted(), event.isInStory());
			setStyleName(thisPanel.style.event_unsorted(), !event.isInStory()&&event.isPlotted());
			setStyleName(thisPanel.style.event_new(), !event.isInStory()&&!event.isPlotted()/*&&!event.isAssigned()*/);

			if (event.isAssigned()) {
				CharacterEntity c = LocalCache.get(CharacterEntity.class, event.getMainCharacter());
				String color = c.getColor();
				getElement().getStyle().setBackgroundColor(CssUtil.makeRGBA(color, 0.8));
			} else {
				getElement().getStyle().clearBackgroundColor();
			}
			
			if (!event.isInStory()) {
				// 이벤트 풀로 옮기므로 크기 조절 핸들러를 없앤다.
				if (eventMouseHR != null) {
					for (HandlerRegistration hr : eventMouseHR) {
						hr.removeHandler();
					}
					eventMouseHR.clear();
				}
			}
			setStyleName(thisPanel.style.event_plotted(), event.isPlotted());
		}

		/**
		 * 이벤트 상자의 세로 위치와 길이를 바꾼다. 가로 위치는 캐릭터 타임라인에 추가할 때 정한다.
		 * 이 메쏘드는 실제 타임라인에 배치될 때만 불린다.
		 * 
		 * (참고) 실제 CSS에서 효과를 내는 것은 top과 height이다.
		 * 
		 * @param top
		 * @param bottom
		 */
		public void updateWidget(int top, int bottom) {
			/*
			 * 타임라인에 배치될 때 크기 조절 핸들러를 만든다.
			 * 그러므로 핸들러 등록 여부로 이벤트가 새로 타임라인에 추가되는 것인지 알 수 있다.
			 */
			if (resizeAndLinkHandler == null) {
				resizeAndLinkHandler = new EventMouseHandler(this, thisPanel.timelinePanel);//getParent());
				eventMouseHR = new ArrayList<HandlerRegistration>();
			}
			
			int oldTop = -1;
			int oldBottom = -1;
			// 새롭게 타임라인에 놓이는 경우
			if (eventMouseHR.isEmpty()) {
				eventMouseHR.add(addMouseDownHandler(resizeAndLinkHandler));
				eventMouseHR.add(addMouseMoveHandler(resizeAndLinkHandler));
				eventMouseHR.add(addMouseUpHandler(resizeAndLinkHandler));
				eventMouseHR.add(addMouseOverHandler(resizeAndLinkHandler));
				eventMouseHR.add(addMouseOutHandler(resizeAndLinkHandler));
				
				/* 
				 * 이벤트 엔티티의 스토리 시간은 UI 조작이 끝나고 일괄 갱신하므로,
				 * 여기서 스타일과 가로 위치를 강제로 정한다. (아직 isInStory()==false 상태다.)
				 */
				addStyleName(thisPanel.style.event_sorted());
			} else { // 타임라인에서 이동하는 경우
				oldTop = getWidgetTop();
				oldBottom = getWidgetBottom();
			}
			if (oldTop != top) {
				getElement().getStyle().setTop(top, Unit.PX);
			}
			if (oldTop != top || oldBottom != bottom) {
				int deco = getElement().getOffsetHeight()
					- getElement().getClientHeight();
				getElement().getStyle()
						.setHeight(bottom - top - deco, Unit.PX);
			}
			setStyleName(thisPanel.style.event_plotted(), event.isPlotted());
		}
				
		@Override
		public boolean isModified() { return modified; }
		@Override
		public void modify() { modified = true; }
		@Override
		public void invalidate() { modified = false; }

		public void release() {
			removeStyleName(thisPanel.style.event_selected());
		}
		
	}

	/**
	 * 스토리 타임라인 편집기의 각 스트림을 구성하는 캐릭터 타임라인 클래스
	 * <p>
	 * '해프닝'은 실제로는 존재하지 않는 캐릭터이지만 화면 맨 오른쪽에 표시하도록 한다.
	 * 
	 * 캐릭터 속성 편집은 색상, 위치 변경을 포함한다. 
	 * 
	 * @author jehdeiah
	 * 
	 */
	class CharacterTimelineUI implements EntityUIObject<CharacterEntity>, 
	DragEnterHandler, DragOverHandler, DropHandler {

		CharacterEntity character; // null for Happenings
		int index = -1;
		SortedSet<EventUI> eventSet = null;
		HTMLPanel widget = null;
		Label header = null;
		boolean modified = false;
	
		public CharacterTimelineUI(CharacterEntity c) {
			character = c;
		}
	
		public long getId() {
			return (character==null) ? -1 : character.getId();
		}
	
		public String getName() {
			return (character==null) ? HAPPENINGS : character.getName();
		}
	
		public void setName(String name) {
			if (character == null) return;
			if (name.equals(character.getName()) == false) {
				character.setName(name);
				modify();
			}
			if (header != null) {
				header.setText(name);
			}
		}

		/**
		 * 지정된 캐릭터 엔티티에 맞춰 화면요소 속성을 갱신하는데 타임라인 순서는 여기서 바꾸지 않는다.
		 */
		public void updateFromEntity() {
			if (character != null)
				updateWidget();
		}
		
		public void updateWidget() {
			if (header != null) {
				header.setText(getName());
				header.getElement().getStyle().setBackgroundColor(character.getColor());
			}
			if (widget != null) {
				String bgColor = "fdfdfd"; // 해프닝
				if (character != null) bgColor = CssUtil.makeRGBA(character.getColor(), 0.5);
				widget.getElement().getStyle().setBackgroundColor(bgColor);
			}			
		}
		
		public CharacterEntity getEntity() {
			return character;
		}
		
		public void setEntity(CharacterEntity c) {
			character = c;
		}
	
		@Override
		public Widget getWidget() {
			return widget;
		}
	
		public Widget getHeaderWidget() {
			return header;
		}
	
		public SortedSet<EventUI> getEventSet() {
			return eventSet;
		}
	
		/**
		 * 캐릭터 고유색을 받아서 화면요소를 만든다.
		 * 해프닝의 경우는 입력을 무시하고 무조건 투명색으로 한다.
		 * @param color	캐릭터 고유색
		 */
		public void create(String color) {
			if (character == null) color = "fdfdfd";//"transparent";
			else if (color == null) color = character.getColor();
			else if (color.equals(character.getColor()) == false) {
				character.setColor(color);
				modify();
			}
			eventSet = new TreeSet<EventUI>(new Comparator<EventUI>() {
	
				public int compare(EventUI o1, EventUI o2) {
					if (o1 == o2) return 0;
					int t1 = o1.getWidgetTop();
					int t2 = o2.getWidgetTop();
					return (t1 == t2) ? 0 : ((t1 < t2) ? -1 : 1);
				}
	
			});
			int left = CharacterColumnWidthPX * index;
			// 캐릭터 이름 부분
			// 컨텍스트 메뉴를 띄우기 위해 브라우저 이벤트를 끌어온다.
			header = new Label(getName()) {
				public void onBrowserEvent(com.google.gwt.user.client.Event e) {
					switch (e.getTypeInt()) {
					case com.google.gwt.user.client.Event.ONCONTEXTMENU:
						e.stopPropagation();
						e.preventDefault();
						thisPanel.showCharacterContextMenu(getId(),
								e.getClientX(), e.getClientY());
						break;
					default:
						super.onBrowserEvent(e);
					}
				}
			};
			header.sinkEvents(Event.ONCONTEXTMENU);
			// applying style...
			header.setStyleName(thisPanel.style.character_header());
			if (index > 0) {
				header.getElement().getStyle().setLeft(left, Unit.PX);
			}
			header.getElement().getStyle().setBackgroundColor(color);
			/*
			 * 마우스 이벤트 핸들러 
			 */
			final CharacterTimelineUI thisUI = this;
			header.addDoubleClickHandler(new DoubleClickHandler() {

				@Override
				public void onDoubleClick(DoubleClickEvent arg0) {
					arg0.preventDefault();
					thisPanel.showCharacterProperties(thisUI);
				}
				
			});
			header.addMouseMoveHandler(new MouseMoveHandler () {

				@Override
				public void onMouseMove(MouseMoveEvent arg0) {
					arg0.preventDefault();
				}
				
			});
			header.addMouseDownHandler(new MouseDownHandler () {

				@Override
				public void onMouseDown(MouseDownEvent arg0) {
					arg0.preventDefault();
				}
				
			});

			// 배경색은 주어진 색에서 투명도를 50%로 한다.
			String bgColor = CssUtil.makeRGBA(color, 0.5);
			widget = new HTMLPanel("");
			widget.getElement().setId("character" + getId());
			// applying style...
			widget.setStyleName(thisPanel.style.character_timeline());
			if (index > 0) {
				widget.getElement().getStyle().setLeft(left, Unit.PX);
			}
			widget.getElement().getStyle().setBackgroundColor(bgColor);
			// adding drag-and-drop handlers...
			widget.addDomHandler(this, DragEnterEvent.getType());
			widget.addDomHandler(this, DragOverEvent.getType());
			widget.addDomHandler(this, DropEvent.getType());
		}
	
		public int getTimelineIndex() {
			return index;
		}
	
		public void setTimelineIndex(int index) {
			if (this.index != index) {
				// 캐릭터 속성을 먼저 바꿀 경우 변경 표시가 안 되므로 이전 UI 인덱스와 비교한다.
				this.index = index;
				modify();
			}
			if (character!=null && index!=character.getIndex()) {
				character.setIndex(index);
				modify();
			}
			int left = CharacterColumnWidthPX * index;
			if (header != null) {
				header.getElement().getStyle().setLeft(left, Unit.PX);
			}
			if (widget != null) {
				widget.getElement().getStyle().setLeft(left, Unit.PX);
			}
			// 이벤트도 같이 움직이게 한다.
			for (EventUI eUI : eventSet) {
				setEventPosition(eUI);
			}
		}
	
		public void removeEvent(EventUI event) {
			eventSet.remove(event);
			if (widget != null) {
				widget.remove(event.getWidget());
			}
		}
	
		public void addEvent(EventUI event) {
			int timePointValue = 0;
			if (eventSet.isEmpty() == false) {
				EventUI lastEvent = eventSet.last();
				timePointValue = lastEvent.getWidgetBottom()
						+ MarginBottomEventPX;
			}
			addEvent(event, timePointValue);
		}
	
		public void addEvent(EventUI event, int timePointValue) {
			addEvent(event, timePointValue, false);
		}
	
		/**
		 * 이벤트를 타임라인에 추가한다.
		 * 이벤트 연결선이 다른 캐릭터의 이벤트들과도 연결되므로 이벤트를 캐릭터 타임라인
		 * 위젯(패널)이 아닌 타임라인 패널(캐릭터 타임라인의 부모 패널)에 추가한다.
		 * 
		 * @param event
		 * @param timePointValue
		 * @param stackMode
		 */
		public void addEvent(EventUI event, int timePointValue, boolean stackMode) {
			event.assign(getId());
			if (widget != null) {
				//widget.add(event.getWidget());
				thisPanel.timelinePanel.add(event.getWidget());
				int top = timePointValue;
				int bottom = top + event.getWidget().getElement().getOffsetHeight();
				event.updateWidget(top, bottom);
				setEventPosition(event);
				thisPanel.rearrange(event, stackMode);
			}
		}
		
		void setEventPosition(EventUI event) {
			int margin = (CharacterColumnWidthPX - EventWidthPX)/2;
			int left = widget.getElement().getOffsetLeft() + margin;
			event.getElement().getStyle().setLeft(left, Unit.PX);			
		}
	
		public boolean hasValidTimePoint(int timePointValue) {
			for (EventUI e : eventSet) {
				if (e.getWidgetTop() > timePointValue) return false;
				if (e.getWidgetTop() == timePointValue)
					return true;
				if (e.getWidgetBottom() == timePointValue)
					return true;
			}
			return false;
		}
	
		/**
		 * 이벤트를 캐릭터에 할당하기 위한 핸들러
		 * 
		 * 끌어다 놓으면 기존 것을 지우고 새로 추가하는 형태로 하는데 기존 캐릭터 타임라인을 볼 수 없으므로 타임라인 편집기에서
		 * 옮기도록 한다.
		 * 
		 * @author jehdeiah
		 * 
		 */
		@Override
		public void onDrop(DropEvent event) {
			EventUI eventUI = thisPanel.getDraggingEvent();
			if (eventUI == null) return;
			int offsetY = thisPanel.getDragOffset();
			int relativeTop = event.getNativeEvent().getClientY()
					- widget.getAbsoluteTop() - offsetY;
			int timePoint = widget.getElement().getScrollTop() + relativeTop;
			if (timePoint < 0) timePoint = 0;
			// 이동하면서 커서가 바뀌는 경우가 있다.
			eventUI.getWidget().getElement().getStyle().setCursor(Cursor.AUTO);
			thisPanel.moveEvent(eventUI, getId(), timePoint, false);
			thisPanel.setDraggingEvent(null);
			thisPanel.setDragOffset(0);
		}

		@Override
		public void onDragOver(DragOverEvent event) {
			event.preventDefault();
			int offsetY = thisPanel.dragOffset;
			int relativeTop = event.getNativeEvent().getClientY()
					- widget.getAbsoluteTop() - offsetY;
			int timePoint = widget.getElement().getScrollTop() + relativeTop;
			int eventHeight = thisPanel.getDraggingEvent().getWidget().getElement().getOffsetHeight();
			thisPanel.hilightTimePoint(timePoint, timePoint + eventHeight);
		}

		@Override
		public void onDragEnter(DragEnterEvent event) {
			event.preventDefault();
		}


		@Override
		public boolean isModified() { return modified; }
		@Override
		public void modify() { modified = true; }
		@Override
		public void invalidate() { modified = false; }
	}
	
	/**
	 * 이벤트 관계를 나타내는 연결선.
	 * 현재는 인과관계만 있는데, 종류에 따라 색깔을 다르게 표현하려고 한다.
	 * 
	 * @author jehdeiah
	 *
	 */
	class EventLinkUI extends WidgetLinker implements EntityUIObject<EventRelationEntity> {

		EventRelationEntity entity;
		int type;
		
		boolean modified = false;
		
		public EventLinkUI(Panel container) {
			super(container);
			mode = WidgetLinker.AUTO_RIGHT;
			type = EventRelationEntity.CAUSAL;
			entity = null;
			setStyle();
		}
		
		private void setStyle() {
			switch (type) {
			case EventRelationEntity.CAUSAL:
				lineColor = "brown";
				break;
			default:
				lineColor = "black";
			}
			curved = true;
			arrow = true;
		}
		
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		
		public EventUI getFromEvent() {
			return (from instanceof EventUI) ? (EventUI)from : null;
		}
		public EventUI getToEvent() {
			return (to instanceof EventUI) ? (EventUI)to : null;
		}

		@Override
		public EventRelationEntity getEntity() {
			return entity;
		}

		@Override
		public long getId() {
			return entity==null ? -1 : entity.getId();
		}

		@Override
		public Widget getWidget() {
			return panel;
		}

		@Override
		public void updateFromEntity() {
			if (entity == null) return;
			type = entity.getType();
			updateWidget();
		}

		@Override
		public void updateWidget() {
			setStyle();
			update();
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
	
	class TimepointAnnotation {
		public int timePointValue;
		public String note;
	}
	
	class StoryTimeAnnotationProperty extends PropertyPanel<TimepointAnnotation> {

		TextArea note;
		TextBox time;
		
		@Override
		void initPanel() {
			property = new VerticalPanel();
			note = new TextArea();
			time = new TextBox();
			time.setReadOnly(true);
			property.add(new Label("Note : "));
			property.add(note);
			property.add(new Label("Time-point : "));
			property.add(time);
		}

		@Override
		void updatePanel() {
			note.setText(data.note);
			int order = thisPanel.getOrdinalTimePoint(data.timePointValue);
			time.setText(Integer.toString(order));
		}

		@Override
		void saveData() {
			data.note = note.getText();
		}

		@Override
		void notifyUpdate(boolean saved) {
			if (saved)
				thisPanel.updateTimePointAnnotation(data.timePointValue, data.note);
		}
		
	}
	
	class EventRelationPropertyPanel extends PropertyPanel<EventRelationEntity> {
		
		StoryTimeline editor;
		ListBox type;
		TextBox from;
		TextBox to;
		TextArea description;

		public EventRelationPropertyPanel() {
			editor = null;
		}
		
		public EventRelationPropertyPanel(StoryTimeline editor) {
			this.editor = editor;
		}

		@Override
		void initPanel() {
			setText("Event Relation Properties...");
			property = new VerticalPanel();
			type = new ListBox();
			type.addItem("Causal");
			type.setVisibleItemCount(1); // drop-down
			type.setSelectedIndex(0);
			description = new TextArea();
			from = new TextBox();
			from.setEnabled(false);
			to = new TextBox();
			to.setEnabled(false);
			property.add(new HTML("Type :"));
			property.add(type);
			property.add(new HTML("From :"));
			property.add(from);
			property.add(new HTML("To :"));
			property.add(to);
			property.add(new HTML("Description :"));
			property.add(description);
		}

		@Override
		void updatePanel() {
			assert (data != null);
			from.setText(LocalCache.get(EventEntity.class, data.getFromEvent()).getName());
			to.setText(LocalCache.get(EventEntity.class, data.getToEvent()).getName());
			type.setSelectedIndex(data.getType()-1);
			description.setText(data.getDescription());
		}

		@Override
		void saveData() {
			if (data != null) {
				data.setType(type.getSelectedIndex()+1);
				data.setDescription(description.getText());
			}
		}

		@Override
		void notifyUpdate(boolean saved) {
			if (saved) {
				saveData();
				if (editor != null) editor.updateLinkProperty(data);
			}
		}
		
	}
	// 이벤트 목록
	Map<Long, EventUI> eventBag = null;
	// 캐릭터 목록
	//int numberOfCharacters = 0;
	ArrayList<CharacterTimelineUI> characterList = null;
	Map<Long, CharacterTimelineUI> characterBag = null;
	/** 시점 목록
	 * 이벤트에 적는 스토리 시간은 순서값이고, 화면에서 중요한 것은 좌표값이다.
	 * StoryTimePoint에서는 좌표값을 순서대로 배열에 넣어 순서를 보존하고, 이름 붙인 시점의 값도 순서값으로 한다.
	 * 그러나 여기서는 좌표값을 순서집합에 넣어서 관리하고, 이름 붙인 시점의 값도 좌표값이다.
	 * 이벤트 속성값은 나중에 저장하기 전에 한꺼번에 처리하고, 속성값을 요청할 때는 여기 순서집합에서 위치를 파악하여 보여준다.
	 */
	SortedSet<Integer> timepointSet = null;
	Map<Integer, String> annotedTimepoint = null;
	Map<Integer, SimplePanel> timelineBag = null;
	Map<Integer, SimplePanel> annotationMarkers = null;
	// 시점 가이드
	SimplePanel timepointCursor = null;
	// 이동 중인 시점
	SimplePanel movingAnnotation = null;
	// 메뉴를 띄운 시점 
	SimplePanel contextAnnotation = null;
	int annotationOffset = 0;
	// 시점 컨텍스트 메뉴
	private PopupPanel menuPanelOnAnnotation = null;
	private int popupLeft = 0;
	private int popupTop = 0;
	// 캐릭터 컨텍스트 메뉴
	private PopupPanel menuPanelOnCharacter = null;
	// 컨텍스트 캐릭터
	private CharacterTimelineUI contextCharacter = null;
	// 이벤트 컨텍스트 메뉴
	private PopupPanel menuPanelOnNewEvent = null;
	private PopupPanel menuPanelOnEvent = null;
	private MenuBar submenuAssignTo = null;
	// 컨텍스트 이벤트
	private EventUI contextEvent = null;
	// 크기 조절 이벤트
	private EventUI resizingEvent = null;
//	private int resizeTop = -1;
//	private int resizeBottom = -1;
	// 드래그 이벤트 
	private EventUI draggingEvent = null;
	private int dragOffset = 0;
	
	private EventUI mouseOverEvent = null;
	
	// 컨텍스트 연결선
	private EventLinkUI contextLink = null;
	// 연결선 컨텍스트 메뉴
	private PopupPanel menuPanelOnLink = null;

	/*
	 * 속성창 
	 */
	final int popupZIndex = 9;
	EventPropertyPanel eventProperty = null;
	CharacterPropertyPanel characterProperty = null;
	StoryTimeAnnotationProperty annotationProperty = null;
	EventRelationPropertyPanel relationProperty = null;
	
	/*
	 * 연결선 
	 */
	private EventLinkUI selectedLinker = null;
	private EventLinkUI tempLinker = null;
//	private EventUI fromEvent = null;
//	private EventUI toEvent = null;
	private LinkDrawingHandler<EventLinkUI> linkDrawingHandler;
	private ArrayList<EventLinkUI> linkBag;

	public StoryTimeline(StoryServiceAsync service, EventBus bus) {
		super(service, bus);
		initWidget(uiBinder.createAndBindUi(this));
		eventBag = new HashMap<Long, EventUI>();
		characterBag = new HashMap<Long, CharacterTimelineUI>();
		characterList = new ArrayList<CharacterTimelineUI>();
		// Empty time points
		timepointSet = new TreeSet<Integer>();
		annotedTimepoint = new HashMap<Integer, String>();
		timelineBag = new HashMap<Integer, SimplePanel>();
	}

	CharacterTimelineUI getTimelineByPoint(int clientX) {
		int panelLeft = timelinePanel.getAbsoluteLeft();
		int x = clientX - panelLeft;
		if (x<0 || x>=timelinePanel.getOffsetWidth()) {
			return null;			
		}
		int cIndex = Math.min(x/CharacterColumnWidthPX, getCharacterCount());
		if (cIndex >= characterList.size()) return null;
		return characterList.get(cIndex);
	}

	int getDragOffset() {
		return dragOffset;
	}
	void setDragOffset(int offsetY) {
		dragOffset = offsetY;		
	}

	boolean isDragging() {
		return draggingEvent != null;
	}
	
	EventUI getDraggingEvent() {
		return draggingEvent;
	}

	void setDraggingEvent(EventUI eventUI) {
		draggingEvent = eventUI;
	}

	boolean isResizing() {
		return resizingEvent != null;
	}
	
	void setResizingEvent(EventUI eventUI) {
		resizingEvent = eventUI;
		if (eventUI != null) {
			// 크기 변경하는 것을 가장 나중에 추가하여 위에 오도록 한다.
			eventUI.removeFromParent();
			timelinePanel.add(eventUI);
		}
	}

	public ResizeMouseHandler getResizeHandler() {
		return (resizingEvent==null) ? null : resizingEvent.resizeAndLinkHandler;
	}

	/**
	 * 위젯을 초기화하는데 화면 요소만 등록하고 스토리 내용은 취급하지 않는다.
	 */
	protected void onLoad() {
		super.onLoad();

		// 화면 스크롤에서 제외된 캐릭터 이름 표시줄을 가로 스크롤에 맞춰 움직인다.
		// 시간축을 세로 스크롤에 맞게 움직인다.
		timelineContainer.addDomHandler(new ScrollHandler() {
			public void onScroll(ScrollEvent event) {
				int scrollX = timelineContainer.getElement().getScrollLeft();
				int scrollY = timelineContainer.getElement().getScrollTop();
				characterHeaderPanel.getElement().getStyle()
						.setLeft(-scrollX, Unit.PX);
				timeAxisPanel.getElement().getStyle().setTop(-scrollY, Unit.PX);
			}

		}, ScrollEvent.getType());

		// 연결선 핸들러
		linkDrawingHandler = new LinkDrawingHandler<EventLinkUI>(this);
		linkBag = new  ArrayList<EventLinkUI>();

		/*
		 * 타임라인 패널의 마우스 이벤트 처리
		 *
		 * (1) 시점선 강조 : 마우스 이동 시 만나는 시점선을 강조 표시한다.
		 * (2) 이벤트 크기 변경 
		 * 		이벤트가 캐릭터 타임라인과 같은 수준으로 올라왔으므로,
		 * 		크기 변경 시 이벤트 상자 영역을 벗어나는 경우를 처리한다.
		 * (3) 연결선 처리
		 */
		timelinePanel.addDomHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				/*
				 * 마우스를 움직일 떼 버튼이 안 눌렸으면 크기 조절을 끝내려 했는데,
				 * 움직이는 동안에는 마우스 버튼을 얻어오는 것을 믿을 수 없다.
				 * (안 눌렸는데 눌렸다고 나온다. =.=)
				 * 그러므로 중간에 마우스를 떼고 종료하는 것은 MouseUp으로 다룬다.
				 */
				if (isResizing()) {
					event.preventDefault();
					getElement().getStyle().setCursor(Cursor.ROW_RESIZE);
					ResizeMouseHandler h = thisPanel.getResizeHandler();
					h.resizeElement(event.getX(), event.getY());
					hilightTimePoint(event.getY());
				} else if (isDrawingConnection() || selectedLinker!=null) {
					linkDrawingHandler.onMouseMove(event);
				} else {
					getElement().getStyle().setCursor(Cursor.AUTO);
					hilightTimePoint(event.getY());
				}
			}
		}, MouseMoveEvent.getType());
		timelinePanel.addDomHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				if (isDragging()) {
					if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
						ResizeMouseHandler h = thisPanel.getResizeHandler();
						h.finishResize();
						event.preventDefault();
						getElement().getStyle().setCursor(Cursor.AUTO);
					}
				} else if (isDrawingConnection()) {
					linkDrawingHandler.onMouseUp(event);
				}
			}
		}, MouseUpEvent.getType());
		/* 
		 * move에서 계속 하던 작업을, out이면 Event.preventDefault()로 그냥 잠시 멈춘다.
		 * 연결선 작업과 크기 변경 등에 공통이므로 연결선 핸들러를 쓴다.
		 * FIXME: out에서 in을 할 때 버튼이 떼어져도 인식을 못하는데 한 번 눌러주면 된다.
		 */
		timelinePanel.addDomHandler(linkDrawingHandler, MouseOutEvent.getType());
		timelinePanel.addDomHandler(linkDrawingHandler, MouseDownEvent.getType());
		timelinePanel.addDomHandler(linkDrawingHandler, DoubleClickEvent.getType());
		
		timelinePanel.addDomHandler(new DropHandler() {
			public void onDrop(DropEvent e) {
				CharacterTimelineUI cUI = getTimelineByPoint(e.getNativeEvent().getClientX());
				cUI.onDrop(e);
			}
		}, DropEvent.getType());
		timelinePanel.addDomHandler(new DragEnterHandler() {
			public void onDragEnter(DragEnterEvent e) {
				e.preventDefault();
			}
		}, DragEnterEvent.getType());
		timelinePanel.addDomHandler(new DragOverHandler() {
			public void onDragOver(DragOverEvent e) {
				CharacterTimelineUI cUI = getTimelineByPoint(e.getNativeEvent().getClientX());
				if (cUI != null) cUI.onDragOver(e);
			}
		}, DragOverEvent.getType());
				
		// 새 이벤트 영역으로 이벤트를 옮겨서 캐릭터와 스토리 시간 할당을 제거하는 부분
		// 플롯된 이벤트는 unsorted 이벤트 풀로 옮긴다.
		newEventPool.addDomHandler(new DropHandler() {
			public void onDrop(DropEvent e) {
				long eventId = Long.parseLong(e.getData("event"));
				EventUI eventUI = getEventById(eventId);
				//moveEvent(eventUI, -1, 0, false);
				removeEventFromStory(eventUI, true);
			}
		}, DropEvent.getType());
		newEventPool.addDomHandler(new DragEnterHandler() {
			public void onDragEnter(DragEnterEvent e) {
				e.preventDefault();
			}
		}, DragEnterEvent.getType());
		newEventPool.addDomHandler(new DragOverHandler() {
			public void onDragOver(DragOverEvent e) {
				e.preventDefault();
			}
		}, DragOverEvent.getType());
		
		// 정렬 안 된 이벤트 영역으로 이벤트를 옮겨서 스토리 시간 할당을 제거하는 부분
		// 플롯 안 된 이벤트는 새 이벤트 풀로 옮긴다.
		unsortedEventPool.addDomHandler(new DropHandler() {
			public void onDrop(DropEvent e) {
				long eventId = Long.parseLong(e.getData("event"));
				EventUI eventUI = getEventById(eventId);
				//moveEvent(eventUI, -1, 0, false);
				removeEventFromStory(eventUI, false);
			}
		}, DropEvent.getType());
		unsortedEventPool.addDomHandler(new DragEnterHandler() {
			public void onDragEnter(DragEnterEvent e) {
				e.preventDefault();
			}
		}, DragEnterEvent.getType());
		unsortedEventPool.addDomHandler(new DragOverHandler() {
			public void onDragOver(DragOverEvent e) {
				e.preventDefault();
			}
		}, DragOverEvent.getType());

		// 캐릭터 컨텍스트 메뉴 생성
		Scheduler.ScheduledCommand charEditPropCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnCharacter.hide();
				showCharacterProperties(contextCharacter);
			}
		};
		Scheduler.ScheduledCommand moveLeftCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnCharacter.hide();
				int index = contextCharacter.getTimelineIndex();
				if (index > 0) 
					moveCharacter(contextCharacter, index-1);
			}
		};
		Scheduler.ScheduledCommand moveRightCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnCharacter.hide();
				int index = contextCharacter.getTimelineIndex();
				if (index < (getCharacterCount()-1))
					moveCharacter(contextCharacter, index+1);
			}
		};
		Scheduler.ScheduledCommand charDeleteCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnCharacter.hide();
				deleteCharacter(contextCharacter);
			}
		};
		MenuBar charMenu = new MenuBar(true);
		MenuBar subMoveMenu = new MenuBar(true);
		charMenu.addItem("Edit Properties", charEditPropCmd);
		subMoveMenu.addItem("Left", moveLeftCmd);
		subMoveMenu.addItem("Right", moveRightCmd);
		charMenu.addItem("Move...", subMoveMenu);
		charMenu.addItem("Delete", charDeleteCmd);
		charMenu.setAutoOpen(true);
		menuPanelOnCharacter = new PopupPanel(true);
		menuPanelOnCharacter.add(charMenu);
		menuPanelOnCharacter.getElement().getStyle().setZIndex(popupZIndex);
		
		// 이벤트 컨텍스트 메뉴 생성
		Scheduler.ScheduledCommand editPropCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnNewEvent.hide();
				menuPanelOnEvent.hide();
				showEventProperties(contextEvent.getEntity());
			}
		};
		Scheduler.ScheduledCommand unassignCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnNewEvent.hide();
				menuPanelOnEvent.hide();
				moveEvent(contextEvent, -1, 0, false);
			}
		};
		Scheduler.ScheduledCommand deleteEventCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnNewEvent.hide();
				menuPanelOnEvent.hide();
				deleteEvent(contextEvent);
			}
		};
		Scheduler.ScheduledCommand removeEventCmd = new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnNewEvent.hide();
				menuPanelOnEvent.hide();
				removeEventFromStory(contextEvent, true);
			}
		};
		MenuBar menuOnNewEvent = new MenuBar(true);
		submenuAssignTo = new MenuBar(true);
		menuOnNewEvent.addItem("Edit Properties", editPropCmd);
		menuOnNewEvent.addItem("Assign to...", submenuAssignTo);
		menuOnNewEvent.addItem("Delete", deleteEventCmd);
		menuOnNewEvent.setAutoOpen(true);
		menuPanelOnNewEvent = new PopupPanel(true);
		menuPanelOnNewEvent.add(menuOnNewEvent);
		menuPanelOnNewEvent.getElement().getStyle().setZIndex(popupZIndex);
		MenuBar menuOnEvent = new MenuBar(true);
		menuOnEvent.addItem("Edit Properties", editPropCmd);
		menuOnEvent.addItem("Unassign the Character", unassignCmd);
		menuOnEvent.addItem("Remove", removeEventCmd);
		menuPanelOnEvent = new PopupPanel(true);
		menuPanelOnEvent.add(menuOnEvent);
		menuPanelOnEvent.getElement().getStyle().setZIndex(popupZIndex);
		
		// 이벤트 관계 연결선 컨텍스트 메뉴 생성
		menuPanelOnLink = new PopupPanel() {
			public void onBrowserEvent(Event e) {
				e.preventDefault();
			}
		};
		menuPanelOnLink.sinkEvents(Event.ONCONTEXTMENU);
		MenuBar menuOnLink = new MenuBar(true);
		MenuBar submenuLinkType = new MenuBar(true);
		submenuLinkType.addItem("Causal", new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnLink.hide();
				contextLink.getEntity().setType(EventRelationEntity.CAUSAL);
				contextLink.updateFromEntity();
			}
		});
		menuOnLink.addItem("Edit Properties", new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				int left = menuPanelOnLink.getPopupLeft();
				int top = menuPanelOnLink.getPopupTop();
				menuPanelOnLink.hide();
				showLinkProperties(contextLink, left, top);
			}
		});
		menuOnLink.addItem("Change Type...", submenuLinkType);
		menuOnLink.addItem("Delete", new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				menuPanelOnLink.hide();
				deleteLink(contextLink);
			}
		});
		menuPanelOnLink.add(menuOnLink);
		menuPanelOnLink.getElement().getStyle().setZIndex(popupZIndex);
		
		/*
		 *  시점 애노트
		 *  시간축에서 마우스 이동 시 타임라인에 같이 움직이는 보조선을 그린다.
		 *  시간축 빈 공간을 클릭하면 그곳에 애노테이션을 추가한다.  
		 */
		annotationMarkers = new HashMap<Integer, SimplePanel>();
		timepointCursor = new SimplePanel();
		timepointCursor.addStyleName(style.timepoint_line());
		timepointCursor.setVisible(false);
		timelineContainer.add(timepointCursor);
		timeAxisPanel.addDomHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent e) {
				e.preventDefault();
				if (movingAnnotation != null) {
					timepointCursor.setVisible(true);
					int lastY = movingAnnotation.getElement().getOffsetTop();
					int newY = e.getY() - annotationOffset;
					if (lastY != newY) {
						hilightTimePoint(newY);
						timepointCursor.getElement().getStyle().setTop(newY, Unit.PX);
						movingAnnotation.getElement().getStyle().setTop(newY, Unit.PX);
					}
				} else {
					movingAnnotation = null;
					timepointCursor.getElement().getStyle().setTop(e.getY(), Unit.PX);
					timepointCursor.setVisible(true);
					hilightTimePoint(e.getY());
				}
			}
		}, MouseMoveEvent.getType());
		timeAxisPanel.addDomHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent e) {
				timepointCursor.setVisible(false);
			}
		}, MouseOutEvent.getType());
		timeAxisPanel.addDomHandler(new MouseUpHandler() {
			public void onMouseUp(MouseUpEvent e) {
				if (movingAnnotation != null) {
					finishMovingAnnotation();
				}
			}
		}, MouseUpEvent.getType());
		timeAxisPanel.addDomHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				e.preventDefault();
				if (movingAnnotation == null)
					showTimePointProperty(e.getY(), e.getClientX(), e.getClientY());
			}
		}, ClickEvent.getType());
		// 시점 컨텍스트 메뉴
		menuPanelOnAnnotation = new PopupPanel();
		MenuBar anoteMenu = new MenuBar(true);
		anoteMenu.addItem("Edit Properties...", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnAnnotation.hide();
				if (contextAnnotation != null) {
					int y = contextAnnotation.getElement().getOffsetTop();
					contextAnnotation = null;
					showTimePointProperty(y);
				}
			}
		});
		anoteMenu.addItem("Delete", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnAnnotation.hide();
				if (contextAnnotation != null) {
					int y = contextAnnotation.getElement().getOffsetTop();
					contextAnnotation = null;
					deleteAnnotation(y);
				}
			}
		});
		menuPanelOnAnnotation.add(anoteMenu);
		menuPanelOnAnnotation.getElement().getStyle().setZIndex(popupZIndex);
	}
	
	/**
	 * UI 작업으로 바뀐 항목 고르기 
	 */
	@Override
	public void updateChangedEntities() {
		/*
		 * 이벤트 저장에 관하여...
		 * 
		 * 이벤트 속성이 캐릭터 할당 등 명시적인 메쏘드 호출을 통하는 경우 modified 플래그를 이용해서 판별할 수 있다.
		 * 스토리 시점은 UI 단에서 처리하면서 updateTimePoints()에서 매번 한꺼번에 정리해서 저장한다.
		 * (속성 파악을 위해 스토리 시점을 읽는 메쏘드 getStart(), getEnd()가 정의되어 있다.) 
		 * 
		 */
		for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) {
			EventUI eUI = getEventById(e.getId());
			if (eUI!=null && eUI.isModified()) {
				saveEntity(e);
				eUI.invalidate();
			}
		}
//		changedCharacters.addAll(CharacterEntity.entities());
		for (CharacterTimelineUI cUI : characterList) {
			if (cUI.getId()!=-1 && cUI.isModified()) {
				saveEntity(cUI.getEntity());
				cUI.invalidate();
			}
		}
		for (EventLinkUI link : linkBag) {
			if (link.isModified()) {
				saveEntity(link.getEntity());
				link.invalidate();
			}
		}
		/*
		 * FIXME: 스토리 시간도 변경 내용만 저장하도록 한다.
		 */
		StoryTimePoint.update(timepointSet, annotedTimepoint);
		timepointChanged = true;
	} 

	/**
	 * 스토리 화면 요소들을 모두 지운다.
	 */
	@Override
	public void clear() {
		// 애노테이션
		for (SimplePanel p : annotationMarkers.values()) {
			if (p.isAttached()) p.removeFromParent();
		}
		annotationMarkers.clear();
		annotedTimepoint.clear();
		// 시점 보조선
		for (SimplePanel p : timelineBag.values()) {
			if (p.isAttached()) p.removeFromParent();
		}
		timelineBag.clear();
		// 이벤트 연결선
		for (EventLinkUI l : linkBag) {
			l.remove();
		}
		linkBag.clear();
		// 이벤트
		for (EventUI event : eventBag.values()) {
			Widget w = event.getWidget(); 
			if (w.isAttached()) w.removeFromParent();
		}
		eventBag.clear();
		// 캐릭터
		for (CharacterTimelineUI c : characterList) {
			Widget w = c.getWidget();
			if (w.isAttached()) w.removeFromParent();
			w = c.getHeaderWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		characterList.clear();
		characterBag.clear();
	}
	
	/**
	 * 로컬 캐시에서 엔티티를 읽어서 편집기 요소를 갱신한다.
	 */
	@Override
	public void updateAll() {
		CharacterEntity[] characterSet = LocalCache.entities(CharacterEntity.class, characterArrayType);
		EventEntity[] eventSet = LocalCache.entities(EventEntity.class, eventArrayType);
		
		if (characterSet.length == 0) {
			// 기존 것을 모두 지운다.
			for (CharacterTimelineUI cUI : characterList) {
				cUI.getWidget().removeFromParent();
				cUI.getHeaderWidget().removeFromParent();
			}
			characterBag.clear();
			characterList.clear();
			// 기본 캐릭터 생성
			createDefaultCharacters();			
		}
		else {
			long[] newCharIds = new long[characterSet.length];
			/* 타임라인 인덱스 정리
			 * 인덱스가 -1인 경우는 맨 뒤로 보낸다.
			 */
			Arrays.sort(characterSet, new Comparator<CharacterEntity>() {

				@Override
				public int compare(CharacterEntity arg0, CharacterEntity arg1) {
					if (arg1.getIndex() == -1) return -1;
					if (arg0.getIndex() == -1) return 1;
					return Integer.compare(arg0.getIndex(), arg1.getIndex());
				}
				
			});
			for (int i=0; i<characterSet.length; i++) {
				newCharIds[i] = characterSet[i].getId();
				if (characterSet[i].getIndex() >= 0) {
					characterSet[i].setIndex(i);
				}
			}
			Arrays.sort(newCharIds);
			// 빠진 것들을 지운다.
			characterList.clear();
			for (Map.Entry<Long, CharacterTimelineUI> entry: characterBag.entrySet()) {
				if (Arrays.binarySearch(newCharIds, entry.getKey()) < 0) {
					entry.getValue().getWidget().removeFromParent();
					entry.getValue().getHeaderWidget().removeFromParent();
					characterBag.remove(entry.getKey());
				}
			}
			addCharacters(characterSet);
			addCharacter(null);	// 해프닝 추가
		}

		// 스토리 타임 갱신 - annotation can be modified...
		ArrayList<Integer> ordinalPoints = StoryTimePoint.getOrdinalPoints();
		ArrayList<Integer> newPoints = new ArrayList<Integer>(ordinalPoints);
		Map<String, Integer> ordinalAnoteMap = StoryTimePoint.getAnnotation();
		newPoints.removeAll(timepointSet);
		// 기존 것에서 삭제된 것 없애기
		for (Integer tp : timepointSet) {
			if (!ordinalPoints.contains(tp)) {
				timepointSet.remove(tp);
				SimplePanel line = timelineBag.remove(tp);
				if (line != null) line.removeFromParent();
				if (annotedTimepoint.remove(tp) != null) {
					SimplePanel marker = annotationMarkers.get(tp);
					marker.removeFromParent();
				}
			}
		}
		// 새로 추가된 것들
		for (Integer tp : newPoints) {
			addTimePoint(tp.intValue());
		}
		// 애노테이션 갱신
		for (Map.Entry<String, Integer> entry : ordinalAnoteMap.entrySet()) {
			Integer v = ordinalPoints.get(entry.getValue());
			updateTimePointAnnotation(v, entry.getKey());
		}
		/*
		 * 이벤트 갱신 
		 */
		final int MARGIN = 15;
		final int OFFSET = 40;
		if (ordinalPoints.size() == 0) ordinalPoints.add(MARGIN);
		// 플롯된 것을 반영하고 새로 만든 것을 여기서 만든다.
		for (EventEntity e : eventSet) {
			EventUI eUI = getEventById(e.getId());
			boolean loaded = false;
			if (eUI == null) {
				// 새로 만들기
				loaded = true;
				eUI = new EventUI(e);
				//eUI.create();
				eventBag.put(e.getId(), eUI);				
			} else {
				eUI.setEntity(e);
			}
			// 할당된 사건이면 화면 위치를 잡는다.
			// Annotation 때문에 스토리 시점 순서가 변할 수 있다. 
			if (e.isInStory()) {
				int in = e.getOrdinalStoryIn();
				int out = e.getOrdinalStoryOut();
				int top = -1;
				int bottom = -1;
				int last = ordinalPoints.get(ordinalPoints.size()-1);
				//int offset = eUI.getWidget().getOffsetHeight();
				for (int i=ordinalPoints.size(); i<=out; i++) {
					last += OFFSET;
					ordinalPoints.add(last);
				}
				top = ordinalPoints.get(in);
				bottom = ordinalPoints.get(out);
				if (loaded) {
					//long assignedId = e.getMainCharacter();
					//// 일단 그려야 한다. 이벤트 풀에 놓았다가 옮겨오는 것으로 처리한다.
					//eUI.assign(-1);
					//if (e.isPlotted()) unsortedEventPool.add(eUI.getWidget());
					//else newEventPool.add(eUI.getWidget());
					//eUI.updateWidget(top, bottom);
					//moveEvent(eUI, assignedId, top, false);
					CharacterTimelineUI cUI = characterBag.get(e.getMainCharacter());
					cUI.addEvent(eUI, top);
				} 
				eUI.updateWidget(top, bottom);
			} else {
				// 스토리 타임라인에 놓이지 않은 것들
				eUI.updateWidget();
				if (!loaded) {
					if ((e.isPlotted() && eUI.getWidget().getParent()!=unsortedEventPool) ||
						 (!e.isPlotted() && eUI.getWidget().getParent()!=newEventPool)) {
						eUI.getWidget().removeFromParent();
						loaded = true;
					}
				}
				if (loaded) {
					if (e.isPlotted()) unsortedEventPool.add(eUI.getWidget());
					else newEventPool.add(eUI.getWidget());
				}
			}
		}
		// 혹시 다른 화면에서 지운 것이 있으면 여기서도 뺀다.
		for (Long eventId : eventBag.keySet()) {
			if (LocalCache.get(EventEntity.class, eventId) == null) {
				EventUI eUI = eventBag.remove(eventId);
				eUI.getWidget().removeFromParent();
			}
		}
		
		// 이벤트 연결선 갱신
		for (EventLinkUI linkUI : linkBag) {
			linkUI.remove();
		}
		linkBag.clear();
		for (EventRelationEntity link : LocalCache.entities(EventRelationEntity.class, eventRelationArrayType)) {
			createLink(link); 
		}
	}
	
	@Override
	public void invalidate() {
		// 이 메쏘드는 탭 이동으로 나갈 때 불린다.
		if (eventProperty!=null && eventProperty.isVisible()) {
			eventProperty.hide();
		}
		setDraggingEvent(null);
		setResizingEvent(null);
	}
	
	/*---------------------------------------------------------------
	 * 캐릭터 관련 메쏘드 
	 */

	int getCharacterCount() {
		return characterList.size();
	}

	int getTimelineById(long characterId) {
		return characterList.indexOf(characterBag.get(characterId));
	}

	void showCharacterContextMenu(long id, int x, int y) {
		if (id == -1) return;
		contextCharacter = characterBag.get(id);
		menuPanelOnCharacter.setPopupPosition(x, y);
		menuPanelOnCharacter.show();
	}

	/**
	 * 새 캐릭터를 추가하는데 해프닝을 계속 마지막에 두도록 한다.
	 * @param one
	 */
	void addCharacter(CharacterEntity one) {
		addCharacters(one);
	}
	/**
	 * 캐릭터를 데이터스토어에서 불러오는 상황을 고려하여 정해진 위치에 추가하는 메쏘드.
	 * 읽는 순서가 인덱스 순서대로 보장되지 않고 인덱스에 오류가 있을 수 있다.
	 * 또한 캐릭터 화면 요소가 이미 만들어져 있을 수도 있으므로 이 경우에는 속성만 바꾼다.
	 * 
	 * @param characters 화면에 추가할 캐릭터 엔티티들
	 */
	void addCharacters(CharacterEntity... characters) {
		if (characters==null || characters[0]==null) {
			if (characterBag.containsKey(-1L)) return;
			CharacterTimelineUI c = new CharacterTimelineUI(null);
			c.create(null);
			int lastIndex = characterList.size();
			c.setTimelineIndex(lastIndex);
			characterList.add(lastIndex, c);
			characterBag.put(-1L,c);
			characterHeaderPanel.add(c.getHeaderWidget());
			timelinePanel.add(c.getWidget());
			return;
		}
		CharacterTimelineUI happening = characterBag.get(-1L);
		// 해프닝은 리스트에서 마지막에 있어야 한다!
		if (happening != null) characterList.remove(happening);
		ArrayList<CharacterEntity> append = new ArrayList<CharacterEntity>();
		for (CharacterEntity one : characters) {
			int index = one.getIndex();
			// 인덱스가 없는 캐릭터는 가장 나중에 추가한다.
			if (index == -1) {
				append.add(one);
				continue;
			}
			else {
				boolean newInstance = false;
				CharacterTimelineUI c = characterBag.get(one.getId());
				if (c == null) {
					c = new CharacterTimelineUI(one);
					c.create(one.getColor()==null ? CssUtil.getRandomColor(150) : one.getColor());
					newInstance = true;
				} else {
					c.setEntity(one);
					c.updateFromEntity();
				}
				// 캐릭터 순서가 임의로 들어올 수 있으므로 자리를 만들어 둔다.
				for (int i=characterList.size(); i<=index; i++)
					characterList.add(i, null);
				CharacterTimelineUI cursor = characterList.get(index);
				if (cursor == null) characterList.set(index, c);
				else {
					// 먼저 지정된 인덱스를 보존하고 새 인덱스에 1을 더한다.
					if (cursor.getTimelineIndex() == index) index++;
					characterList.add(index, c);
					for (int i=index+1; i<characterList.size(); i++) {
						CharacterTimelineUI cUI = characterList.get(i);
						if (cUI != null) cUI.setTimelineIndex(i);
					}
				}
				c.setTimelineIndex(index);
				if (newInstance) {
					characterBag.put(one.getId(),c);
					characterHeaderPanel.add(c.getHeaderWidget());
					timelinePanel.add(c.getWidget());
				}
			}
		}
		// 중간에 남은 null을 없애면서 인덱스를 조정한다.
		int i=0;
		for (CharacterTimelineUI cUI : characterList) {
			if (cUI == null) {
				characterList.remove(cUI);
			} else {
				if (cUI.getTimelineIndex() != i) {
					cUI.setTimelineIndex(i);
				}
				i++;
			}
		}
		// 인덱스가 없는 캐릭터를 추가할 경우 맨 뒤에서부터 추가한다.
		int lastIndex = characterList.size();
		for (CharacterEntity one : append) {
			boolean newInstance = false;
			CharacterTimelineUI c = characterBag.get(one.getId());
			if (c == null) {
				c = new CharacterTimelineUI(one);
				c.create(one.getColor()==null ? CssUtil.getRandomColor(150) : one.getColor());
				newInstance = true;
			}
			c.setTimelineIndex(lastIndex);
			characterList.add(lastIndex, c);
			if (newInstance) {
				characterBag.put(one.getId(),c);
				characterHeaderPanel.add(c.getHeaderWidget());
				timelinePanel.add(c.getWidget());
			}
			lastIndex++;
		} 
		// 해프닝이 있었을 때 맨 뒤에 붙인다.
		if (happening != null) {
			happening.setTimelineIndex(lastIndex);
			characterList.add(lastIndex, happening);
		}
	}
	
	/**
	 * 캐릭터 타임라인 순서를 바꾼다. 옮기려는 것은 현재 화면에 보여야 한다.
	 * 
	 * @param c 옮길 캐릭터 타임라인
	 * @param target 옮길 대상 인덱스
	 */
	void moveCharacter(CharacterTimelineUI c, int target) {
		//if (c.getId() == -1) return;	// 해프닝
		// 현재 화면에 없는 캐릭터면 그냥 나간다.
		if (!characterList.remove(c)) return;
		int source = c.getTimelineIndex();
		if (source == target) return;
		// 옮길 위치가 부정확하면 끝으로 보내는데 해프닝보다는 앞에 있어야 한다.
		int numberOfCharacters = getCharacterCount();
		if (target < 0) target = numberOfCharacters - 1;
		else if (target >= numberOfCharacters) target = numberOfCharacters - 1;
		if (characterList.get(target).getId() == -1) target--;
		if (target < 0) return;
		if (source < target) {
			for (int i=source; i<target; i++) {
				characterList.get(i).setTimelineIndex(i);
			}
		} else {
			for (int i=target; i<source; i++) {
				characterList.get(i).setTimelineIndex(i+1);
			}
		}
		c.setTimelineIndex(target);
		characterList.add(target, c);
	}
	
	/**
	 * 새 캐릭터 만들기를 서버에 요청하고 로컬 캐시에 추가한다. 
	 * @param name
	 */
	void requestNewCharacter(String name) {
		storyService.createNewCharacter(name, new AsyncCallback<CharacterEntity>() {

			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onSuccess(CharacterEntity character) {
				LocalCache.add(character);
				thisPanel.addCharacter(character);
			}
			
		});
	}
	
	/**
	 * 새 이야기를 만들고 기본 등장인물인 주인공을 추가한다.
	 * 해프닝은 저절로 추가하고 캐릭터 엔티티로 관리하지 않는다.
	 */
	private void createDefaultCharacters() {
		requestNewCharacter(MAIN_CHARACTER);
		//requestNewCharacter(HAPPENINGS);
	}

	/**
	 * 해당 타임라인에 대응하는 캐릭터 속성창을 띄운다.
	 * @param cUI
	 */
	public void showCharacterProperties(CharacterTimelineUI cUI) {
		if (cUI.getId() == -1) return;	// 해프닝은 무시한다.
		showCharacterProperties(cUI.getEntity());
	}
	
	@Override
	public void showCharacterProperties(CharacterEntity entity) {
		if (characterProperty == null) {
			characterProperty = new CharacterPropertyPanel(this);
			characterProperty.setMode(PropertyPanel.MODALESS_OK_CANCEL);
			characterProperty.getElement().getStyle().setZIndex(popupZIndex);
		}
		characterProperty.setData(entity);
		characterProperty.center();		
	}

	/**
	 * 속성창에서 갱신한 캐릭터에 맞춰 화면 요소를 바꾼다.
	 * 
	 * @param character		갱신할 캐릭터 엔티티
	 */
	public void updateCharacterProperties(CharacterEntity character) {
		if (character == null) return;
		int targetIndex = character.getIndex();
		CharacterTimelineUI cUI = characterBag.get(character.getId());
		cUI.modify();
		cUI.updateFromEntity();
		if (cUI.getTimelineIndex() != targetIndex) {
			moveCharacter(cUI, targetIndex);
		}
	}
	
	public void deleteCharacter(CharacterTimelineUI cUI) {
		if (cUI.getId() == -1) return;	// 해프닝은 못 지운다.
		int index = cUI.getTimelineIndex();
		// 해당 캐릭터에 있던 이벤트를 모두 할당 해제한다.
		for (EventUI eUI : cUI.getEventSet()) {
			//moveEvent(eUI, -1, 0, false);
			removeEventFromStory(eUI, true);
		}
		// 화면 요소 지우기
		cUI.getHeaderWidget().removeFromParent();
		cUI.getWidget().removeFromParent();
		for (int i=index+1; i<getCharacterCount(); i++) {
			moveCharacter(characterList.get(i), i-1);
		}
		characterList.remove(index);
		characterBag.remove(cUI.getId());
		// 로컬 캐시 삭제, 서버 삭제 서비스 요청
		deleteEntity(cUI.getEntity());
	}
	
	/*---------------------------------------------------------------
	 * 이벤트 관련 메쏘드 
	 */

	int getEventCount() {
		return eventBag.size();
	}
	
	EventUI getEventById(long id) {
		return eventBag.get(id);
	}
		
	void showEventContextMenu(long id, int x, int y) {
		contextEvent = getEventById(id);
		if (contextEvent.getEntity().isAssigned()) {
			menuPanelOnEvent.setPopupPosition(x, y);
			menuPanelOnEvent.show();
		} else {
			submenuAssignTo.clearItems();
			for (final CharacterTimelineUI character : characterList) {
				if (character.getId() == -1) continue;
				submenuAssignTo.addItem(character.getName(),
						new Scheduler.ScheduledCommand() {
							public void execute() {
								menuPanelOnNewEvent.hide();
								menuPanelOnEvent.hide();
								character.addEvent(contextEvent);
							}
						});
			}
			menuPanelOnNewEvent.setPopupPosition(x, y);
			menuPanelOnNewEvent.show();
		}
	}

	/**
	 * 주 캐릭터 할당을 변경하여 이벤트를 옮기는 것을 처리한다.
	 * @param event	이벤트 화면 요소
	 * @param sourceId	원래 할당된 캐릭터 ID
	 * @param targetId	새로 할당할 캐릭터 ID
	 */
	void moveEventByChangingCharacter(EventUI event, long sourceId, long targetId) {
		if (sourceId == targetId) return;
		int sourceIndex = getTimelineById(sourceId);
		int targetIndex = getTimelineById(targetId);
		if (sourceIndex != -1) {
			characterList.get(sourceIndex).removeEvent(event);
		}
		if (targetIndex == -1) { // FIXME: 이런 경우가 생길까?
			removeEventFromStory(event, true);
			updateTimePoints();
			return;
		}
		int timePointValue = event.getWidgetTop();
		event.getWidget().removeFromParent();
		characterList.get(targetIndex).addEvent(event, timePointValue,
				false);
	}
	
	/**
	 * 이벤트를 스토리 타임라인에서 뺀다. (캐릭터와 스토리 시간을 없앤다.)
	 * 담화 시간은 경우에 따라 보존한다.
	 * @param event		없앨 이벤트
	 * @param removeFromPlot	담화 시간 제거 여부 
	 */
	public void removeEventFromStory(EventUI event, boolean removeFromPlot) {
		EventEntity entity = event.getEntity();
		if (entity.isInStory()) {
			int sourceIndex = getTimelineById(entity.getMainCharacter());
			characterList.get(sourceIndex).removeEvent(event);
			event.assign(-1);
			entity.setOrdinalStoryInOut(-1, -1);
			if (removeFromPlot) {
				entity.getDiscourseInOut().clear();
			}
			event.getWidget().removeFromParent();
			if (event.getEntity().isPlotted()) unsortedEventPool.add(event.getWidget());
			else newEventPool.add(event.getWidget());
			event.updateWidget();
			event.modify();
			updateTimePoints();
		}
	}
	
	/**
	 * 이벤트를 캐릭터 타임라인으로 옮기는 것을 처리한다. <br>
	 * 이벤트는 원래 할당된 캐릭터 속성을 유지한 상태로 넘어온다.
	 * 
	 * @param event	이벤트 화면 요소
	 * @param targetId	새로 할당할 캐릭터 ID
	 * @param timePointValue	타임라인 패널에서의 y 좌표
	 * @param stackMode	새로 들어가는 이벤트 아래를 등간격으로 밀어내는 스택 모드 사용 표시 
	 */
	public void moveEvent(EventUI event, long targetId, int timePointValue,
			boolean stackMode) {
		int sourceIndex = getTimelineById(event.getEntity().getMainCharacter());
		int targetIndex = getTimelineById(targetId);
		boolean inStory = event.getEntity().isInStory();
		if (inStory) characterList.get(sourceIndex).removeEvent(event); 
		event.getWidget().removeFromParent();
		characterList.get(targetIndex).addEvent(event, timePointValue,
				stackMode);
	}

	/**
	 * 이벤트 삭제
	 * 
	 * @param event 삭제할 이벤트 UI
	 */
	public void deleteEvent(EventUI event) {
		EventEntity entity = event.getEntity();
		long eventId = entity.getId();
		// 편집기에서 삭제
		eventBag.remove(eventId);  
		if (entity.getMainCharacter() != -1) {
			CharacterTimelineUI cUI = characterBag.get(entity.getMainCharacter());
			cUI.getEventSet().remove(event);
			int top = event.getWidgetTop();
			int bottom = event.getWidgetBottom();
			deleteTimePoint(top, false);
			deleteTimePoint(bottom, false);
		}
		// 화면에서 없애기
		event.getWidget().removeFromParent();  
		//updateTimePoints();
		// 로컬 캐시에서 삭제, 서버 서비스 요청
		deleteEntity(entity);
	}

	/**
	 * 주어진 이벤트가 화면에 새로 추가되거나 옮겨졌을 때 전체 이벤트를 다시 정렬한다. <br>
	 * 이미 정렬된 상태이므로 앞선 것에서 겹치는 부분이 있으면 이 이벤트를 뒤로 밀고, 늦게 시작하는 것은 서로 겹치지 않을 때까지
	 * 민다. 주어진 이벤트는 타임라인에 있거나 없을 수 있고, 만약 있다면 집합에서 빼고 해야 한다.
	 * SortedSet을 쓰고 있는데 집합에 넣고 나중에 객체 속성을 바꾸면 순서 보장을 못한다.<br>
	 * 묶음 이동(stackMove)은 한 타임라인 안에서 기존의 간격을 유지하며 해당 이벤트 이후 전체를 이동하는 것이다.
	 * 
	 * 이벤트는 DOM에 추가되어 화면에 표시된 상태에서 오프셋 좌표를 얻어서 처리한다.
	 * 
	 * TODO: 타임라인을 잡아 이동하는 것은 나중에 생각하자.
	 * 
	 * @param event
	 * @param stackMove
	 */
	public void rearrange(EventUI event, boolean stackMove) {
		int timelineIndex = getTimelineById(event.getEntity().getMainCharacter());
		if (timelineIndex == -1) {
			// Exception
			return;
		}
		// 좌표값이 바뀐 채로 온다.
		int top = event.getWidgetTop();
		int bottom = event.getWidgetBottom();
		int delta = 0;
		CharacterTimelineUI cUI = characterList.get(timelineIndex);
		SortedSet<EventUI> eventSet = cUI.getEventSet();
		eventSet.remove(event);
		SortedSet<EventUI> prevSet = eventSet.headSet(event);
		SortedSet<EventUI> restSet = eventSet.tailSet(event);
		if (prevSet.isEmpty() == false) {
			EventUI prev = prevSet.last();
			int prevBottom = prev.getWidgetBottom();
			// 바로 앞 이벤트와 겹치지 않게 하기.
			if (prevBottom > top) {
				delta = prevBottom - top;
				top += delta;
				bottom += delta;
				event.updateWidget(top, bottom);
			}
		}
//		added.add(top); added.add(bottom);
		if (restSet.isEmpty() == false) {
			int prevBottom = bottom;
			ArrayList<EventUI> shifted = new ArrayList<EventUI>();
			for (EventUI e : restSet) {
				int eTop = e.getWidgetTop();
				int eBottom = e.getWidgetBottom();
				if (stackMove) { // 전부 등간격으로 밀기
//					deleted.add(eTop); deleted.add(eBottom);
					eventSet.remove(e);
					e.updateWidget(eTop+delta, eBottom+delta);
					shifted.add(e);
//					added.add(eTop+delta); added.add(eBottom+delta);
				} else {
					if (eTop < prevBottom) { // 겹치는 것까지만 밀기
//						deleted.add(eTop); deleted.add(eBottom);
						eventSet.remove(e);
						delta = prevBottom - eTop;
						eTop += delta;
						eBottom += delta;
						e.updateWidget(eTop, eBottom);
						shifted.add(e);
						prevBottom = eBottom;
//						added.add(eTop); added.add(eBottom);
					} else {
						break;
					}
				}
			}
			eventSet.addAll(shifted);
		}
		eventSet.add(event);
		// 시점 정렬 (전체)
//		deleted.remove(added);
//		for (Integer tp : deleted) {
//			deleteTimePoint(tp, false);
//		}
//		for (Integer tp : added) {
//			addTimePoint(tp);
//		}
		updateTimePoints();
	}

	@Override
	public void showEventProperties(EventEntity event) {
		if (eventProperty == null) {
			eventProperty = new EventPropertyPanel(this);
			eventProperty.setMode(PropertyPanel.MODALESS_EDIT_CLOSE);
			eventProperty.getElement().getStyle().setZIndex(popupZIndex);
		}
		eventProperty.setData(event);
		if (eventProperty.isShowing() == false) {
			eventProperty.setPopupPositionAndShow(new PositionCallback() {
	
				@Override
				public void setPosition(int offsetWidth, int offsetHeight) {
					int width = Window.getClientWidth();
					int height = Window.getClientHeight();
					int left = (width - offsetWidth) / 2;
					int top = height - offsetHeight - 10; // margin
					eventProperty.setPopupPosition(left, top);
				}
				
			});
		} else {
			eventProperty.updatePanel();
		}
	}

	@Override
	public void updateEventProperties(EventEntity data) {
		EventUI eUI = getEventById(data.getId());
		eUI.updateFromEntity();
		long sourceId = eventProperty.getOriginalCharacter();
		long targetId = data.getMainCharacter();
		if (sourceId != targetId) {
			moveEventByChangingCharacter(eUI, sourceId, targetId);
		}
	}

	/*---------------------------------------------------------------
	 * 스토리 시점 관련 메쏘드 
	 */

	/**
	 * 현재 화면 구성대로 시점 전체를 갱신한다.
	 * 꼬리표를 단 시점은 일단 좌표 그대로 유지하도록 한다. (기능 요구사항이 불명확하다.) 
	 */
	public void updateTimePoints() {
		timepointSet.clear();
		for (CharacterTimelineUI c : characterList) {
			for (EventUI e : c.getEventSet()) {
				timepointSet.add(e.getWidgetTop());
				timepointSet.add(e.getWidgetBottom());
			}
		}
		timepointSet.addAll(annotedTimepoint.keySet());
		// 선 긋기 
		ArrayList<SimplePanel> timelineList = new ArrayList<SimplePanel>(timelineBag.values());
		Iterator<SimplePanel> iter = timelineList.iterator();
		timelineBag.clear();
		int i = 0;
		int count = timelineList.size();
		for (Integer tp : timepointSet) {
			SimplePanel line = null;
			if (i < count) line = iter.next();
			else {
				line = new SimplePanel();
				line.setStyleName(style.timepoint_line());
				timelinePanel.add(line);
			}
			line.getElement().getStyle().setTop(tp.doubleValue(), Unit.PX);
			timelineBag.put(tp, line);
			i++;
		}
		// 나머지 선 지우기
		while (iter.hasNext()) {
			iter.next().removeFromParent();
		}
		// 타임라인 크기 보정
		if (timepointSet.isEmpty()) return;
		int last = timepointSet.last().intValue();
		if (last >= timelinePanel.getOffsetHeight()) {
			String height = (last + 5) + "px";
			timelinePanel.setHeight(height);
			timeAxisPanel.setHeight(height);
		}
		// 이벤트 시점 갱신
		for (CharacterTimelineUI c : characterList) {
			for (EventUI eUI : c.getEventSet()) {
				int top = eUI.getWidgetTop();
				int bottom = eUI.getWidgetBottom();
				int sIn = getOrdinalTimePoint(top);
				int sOut = getOrdinalTimePoint(bottom);
				EventEntity e = eUI.getEntity();
				if (e.getOrdinalStoryIn()!=sIn || e.getOrdinalStoryOut()!=sOut) {
					e.setOrdinalStoryInOut(sIn, sOut);
					eUI.modify();
				}
			}
		}
	}
	
	/**
	 * 화면(DOM) 좌표에 대응하는 스토리 시점(순서)를 구한다.
	 * @param y	화면 좌표
	 * @return	순서. 만약 좌표가 등록되어 있지 않더라도 주어진 좌표의 크기 순서를 넘긴다.
	 */
	int getOrdinalTimePoint(int y) {
		Set<Integer> headSet = timepointSet.headSet(y);
		return headSet.size();
	}

	/**
	 * 시점 좌표를 추가한다.
	 * @param timePointValue	추가할 시점 좌표
	 */
	public void addTimePoint(int timePointValue) {
		Integer tp = Integer.valueOf(timePointValue);
		if (timepointSet.add(tp)) {
			SimplePanel p = new SimplePanel();
			p.setStyleName(style.timepoint_line());
			p.getElement().getStyle().setTop(timePointValue, Unit.PX);
			timelinePanel.add(p);
			if (timePointValue >= timelinePanel.getOffsetHeight()) {
				String height = new String((timePointValue + 50) + "px");
				timelinePanel.setHeight(height);
				timeAxisPanel.setHeight(height);
			}
			timelineBag.put(tp, p);
		}
	}
	
	/**
	 * 해당 시점 좌표에서 시작하거나 끝나는 이벤트가 있는지 알아본다.
	 * @param timepointValue 시점 좌표
	 * @return 시점에 걸리는 이벤트 존재 여부
	 */
	boolean hasEventAt(int timepointValue) {
		for (CharacterTimelineUI c : characterList) {
			if (c.hasValidTimePoint(timepointValue)) 
				return true;
		}
		return false;
	}
	/**
	 * 좌표에 대응하는 시점을 삭제하는데, 애노테이션 삭제 여부를 선택할 수 있다.
	 * 삭제할 시점 좌표를 사용하는 다른 것이 없어야 한다.
	 * 
	 * @param timePointValue 삭제할 시점 좌표
	 * @param deleteAnnotation 애노테이션 삭제 여부
	 */
	public void deleteTimePoint(int timePointValue, boolean deleteAnnotation) {
		Integer tp = Integer.valueOf(timePointValue);
		boolean isAnnotation = annotedTimepoint.containsKey(tp);
		if (!deleteAnnotation && isAnnotation) return;
		if (hasEventAt(timePointValue)) {
			if (deleteAnnotation && isAnnotation) {
				annotedTimepoint.remove(tp);
				SimplePanel marker = annotationMarkers.remove(tp);
				if (marker != null) marker.removeFromParent();
			}
			return;
		}
		timepointSet.remove(tp);
		if (annotedTimepoint.remove(tp) != null) {
			SimplePanel marker = annotationMarkers.remove(tp);
			if (marker != null) marker.removeFromParent();
		}
		SimplePanel line = timelineBag.remove(tp);
		if (line != null) line.removeFromParent();
	}
	
	/**
	 * 시점 보조선 중에서 좌표에 해당하는 보조선들을 강조한다.
	 * 
	 * @param timePoints 시점 좌표들
	 */
	public void hilightTimePoint(int... timePoints) {
		TreeSet<Integer> hilights = new TreeSet<Integer>();
		for (int t : timePoints)
			hilights.add(t);
		for (SimplePanel p : timelineBag.values()) {
			int top = p.getElement().getOffsetTop();
			if (hilights.contains(top)) {
				p.addStyleName(style.timepoint_line_selected());
			} else {
				p.removeStyleName(style.timepoint_line_selected());
			}
		}
	}

	/**
	 * 시점 이름을 t_{순서} 형식으로 붙인다. 
	 * @param timePointValue 시점 좌표
	 * @return
	 */
	public String getTimePoint(int timePointValue) {
		Integer tp = Integer.valueOf(timePointValue);
		String name = annotedTimepoint.get(tp); 
		if (name != null) return name;
		if (timepointSet.contains(tp)) {
			SortedSet<Integer> less = timepointSet.headSet(tp);
			return "t_" + less.size();
		}
		return null;
	}

	/**
	 * 시점 속성창을 띄운다. 팝업 위치는 <code>popupLeft</code>, <code>popupRight</code>로 잡는다.
	 * 
	 * @param timePointValue 시점
	 */
	public void showTimePointProperty(int timePointValue) {
		showTimePointProperty(timePointValue, popupLeft, popupTop);
	}
	
	/**
	 * 시점 속성창을 띄운다.
	 * 
	 * @param timePointValue 속성을 보려는 시점
	 * @param x 팝업 가로 좌표
	 * @param y 팝업 세로 좌표
	 */
	public void showTimePointProperty(int timePointValue, int x, int y) {
		TimepointAnnotation data = new TimepointAnnotation();
		data.note = annotedTimepoint.get(timePointValue);
		if (data.note == null) {
			data.note = "A" + annotedTimepoint.size();
		} 
		data.timePointValue = timePointValue;
		if (annotationProperty == null) {
			annotationProperty = new StoryTimeAnnotationProperty();
		}
		annotationProperty.setData(data);
		annotationProperty.setPopupPosition(x, y);
		annotationProperty.show();
	}
	
	/**
	 * 시점 애노테이션을 변경한다.
	 * 
	 * @param timepointValue	시점
	 * @param note	꼬리표, 설명...
	 */
	public void updateTimePointAnnotation(int timepointValue, String note) {
		annotedTimepoint.put(timepointValue, note);
		if (!timepointSet.contains(timepointValue)) {
			addTimePoint(timepointValue);
		}
		showAnnotationMarker(timepointValue, note);
	}
	
	/**
	 * 시점 애노테이션의 좌표를 변경한다.
	 * 이 경우, 예전 애노테이션을 삭제하고 새로 만든다. (삭제 규칙을 따라서 시점이 포개지면 이전 것을 지운다.)
	 * @param oldTimepointValue	 원래 시점
	 * @param newTimepointValue 새로운 시점
	 */
	public void updateTimePointAnnotation(int oldTimepointValue, int newTimepointValue) {
		String note = annotedTimepoint.get(oldTimepointValue);
		deleteAnnotation(oldTimepointValue);
		updateTimePointAnnotation(newTimepointValue, note);
	}
	
	/**
	 * 스토리 시간축에 표시한 시점을 나타낸다.
	 * 
	 * @param timepointValue 시점 위치
	 * @param note 꼬리표 이름
	 */
	void showAnnotationMarker(int timepointValue, String note) {
		SimplePanel marker = annotationMarkers.get(timepointValue);
		if (marker == null) {
			/* 
			 * 시점 표시는 테두리 스타일로 모양을 낸 패널에 레이블을 넣어서 보여준다.
			 * 시점 표시도 다른 화면 요소들처럼 팝업 메뉴를 띄울 수 있다.
			 */
			marker = new SimplePanel() {
				public void onBrowserEvent(Event e) {
					switch (e.getTypeInt()) {
					case com.google.gwt.user.client.Event.ONCONTEXTMENU:
						e.stopPropagation();
						e.preventDefault();
						contextAnnotation = this;
						movingAnnotation = null;
						timepointCursor.setVisible(false);
						// 애노트 메뉴
						popupLeft = e.getClientX();
						popupTop = e.getClientY();
						menuPanelOnAnnotation.setPopupPosition(popupLeft, popupTop);
						menuPanelOnAnnotation.show();
						break;
					default:
						super.onBrowserEvent(e);
					}				
				}
			};
			marker.sinkEvents(Event.ONCONTEXTMENU);
			marker.addStyleName(style.timepoint_marker());
			Label label = new Label();
			label.addStyleName(style.timepoint_text());
			marker.add(label);
			final SimplePanel thisMarker = marker;
			// 시점을 끌어 옮길 때 시작을 버튼이 눌려진 것으로 한다.
			marker.addDomHandler(new MouseDownHandler() {
				public void onMouseDown(MouseDownEvent e) {
					e.preventDefault();
					movingAnnotation = thisMarker;
					annotationOffset = e.getY();
				}
			}, MouseDownEvent.getType());
			// 시점 끌어 옮기는 것 끝내기. 
			// 간혹 밖에서 버튼이 놓일 수도 있으므로 시간축 패널에서도 이벤트를 잡아야 한다.
			marker.addDomHandler(new MouseUpHandler() {
				public void onMouseUp(MouseUpEvent e) {
					e.preventDefault();
					if (movingAnnotation != null) {
						finishMovingAnnotation();
					}
				}
			}, MouseUpEvent.getType());
			// 패널 이벤트와 간섭을 피하기 위해...
			marker.addDomHandler(new ClickHandler() {
				public void onClick(ClickEvent e) {
					e.preventDefault();
					e.stopPropagation();
				}
			}, ClickEvent.getType());
			// 시점 속성창을 띄운다.
			marker.addDomHandler(new DoubleClickHandler() {
				public void onDoubleClick(DoubleClickEvent e) {
					e.preventDefault();
					movingAnnotation = null;
					contextAnnotation = thisMarker;
					timepointCursor.setVisible(false);
					showTimePointProperty(thisMarker.getElement().getOffsetTop(), e.getClientX(), e.getClientY());
				}
			}, DoubleClickEvent.getType());
			annotationMarkers.put(timepointValue, marker);
		}
		((Label)marker.getWidget()).setText(note);
		marker.getElement().getStyle().setTop(timepointValue, Unit.PX);
		if (!marker.isAttached()) timeAxisPanel.add(marker);
	}
	
	/**
	 * 주어진 시점에 있는 애노테이션을 지운다.
	 * 
	 * @param timepointValue 시점
	 */
	public void deleteAnnotation(int timepointValue) {
		deleteTimePoint(timepointValue, true);
	}
	
	/**
	 * 마우스 끌기로 시점 이동하는 것을 끝낸다.
	 */
	public void finishMovingAnnotation() {
		if (contextAnnotation == null) return;
		for (Map.Entry<Integer, SimplePanel> entry : annotationMarkers.entrySet()) {
			if (entry.getValue() == contextAnnotation) {
				updateTimePointAnnotation(entry.getKey(), 
						contextAnnotation.getElement().getOffsetTop());
				contextAnnotation = null;
			}
		}
	}
	
	/*---------------------------------------------------------------
	 * 이벤트 연결선 관련 메쏘드 
	 */

	public void setMouseOverEvent(EventUI event, boolean over) {
		if (over) mouseOverEvent = event;
		else if (mouseOverEvent == event) mouseOverEvent = null;
	}
	
	public boolean isDrawingConnection() {
		return tempLinker != null;
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
//		fromEvent = (EventUI)from;
//		toEvent = null;
		linkDrawingHandler.setFrom(from);
		if (tempLinker == null) {
			tempLinker = new EventLinkUI(timelinePanel);
		}
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
//		fromEvent = null;
//		toEvent = (EventUI)to;
		linkDrawingHandler.setTo(to);
		if (tempLinker == null) {
			tempLinker = new EventLinkUI(timelinePanel);
		}
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
		EventUI to = mouseOverEvent;
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
		EventUI from = mouseOverEvent;
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
	void finishDrawingConnection(EventUI target) {
		if (tempLinker == null) return;
		if (linkDrawingHandler != null) {
			linkDrawingHandler.clear();
		}
		if (!tempLinker.isVisible()) {
			tempLinker.remove();
			tempLinker = null;
//			if (selectedLinker != null) {
//				deleteLink(selectedLinker);
//			}
			return;
		}
		EventUI fromEvent = tempLinker.getFromEvent();
		EventUI toEvent = tempLinker.getToEvent();
		tempLinker.remove();
		tempLinker = null;
		if (selectedLinker != null) {
			selectedLinker.setVisible(true);
			selectedLinker.select();
		}
		if (target == null) {
			if (fromEvent != null) fromEvent.release();
			else if (toEvent != null) toEvent.release();
			fromEvent = toEvent = null;
			return;
		}
		if (selectedLinker != null) {
			if (fromEvent != selectedLinker.getFromEvent() ||
					toEvent != selectedLinker.getToEvent()	) {
				selectedLinker.setWidgets(fromEvent, toEvent);
				//selectedLinker.select();
				//selectedLinker.modify();
			}
		} else {
			createLink(fromEvent, toEvent);
		} 
	}

	@Override
	public EventLinkUI getSelectedLink() {
		return selectedLinker;
	}

	@Override
	public void setSelectedLink(EventLinkUI selected) {
		selectedLinker = selected;
	}

	@Override
	public EventLinkUI pickLink(int x, int y) {
		for (EventLinkUI l : linkBag) {
			if (l.isVisible() && l.hitTest(x, y))
				return l;
		}
		return null;
	}

	/**
	 * 두 이벤트 사이에 연결선 요소를 만든다.
	 * @param from 첫 사건
	 * @param to 둘째 사건
	 * @return 두 사건의 연결선 화면 요소
	 */
	EventLinkUI makeNewLink(EventUI from, EventUI to) {
		EventLinkUI link = findLink(from, to);
		if (link != null) return null;
		link = new EventLinkUI(timelinePanel);
		link.setWidgets(from, to);
		link.getWidget().addDomHandler(new ContextMenuHandler() {
			@Override
			public void onContextMenu(ContextMenuEvent event) {
				event.preventDefault();
			}
			
		}, ContextMenuEvent.getType());
		return link;
	}
	
	/**
	 * 두 이벤트 연결 관계를 추가하는데, 이미 연결선이 있으면 무시하고, 없으면 새로 만들면서 서버에 등록한다.
	 * @param from
	 * @param to
	 */
	void createLink(EventUI from, EventUI to) {
		EventLinkUI link = makeNewLink(from, to);
		if (link == null) return;
		link.modify();
		linkBag.add(link);
		// 서버에 링크 생성 요청
		final EventLinkUI newLink = link;
		storyService.createNewEventRelation(new AsyncCallback<EventRelationEntity>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(EventRelationEntity e) {
				LocalCache.add(e);
				e.setType(newLink.getType());
				e.setFromEvent(newLink.getFromEvent().getId());
				e.setToEvent(newLink.getToEvent().getId());
				newLink.entity = e;
			}
			
		});
	}

	/**
	 * 엔티티로부터 연결선 요소를 만든다.
	 * 만약 엔티티가 중복되는 경우 이전 엔티티에 대해 삭제 요청한다.
	 * @param entity
	 */
	void createLink(EventRelationEntity entity) {
		EventUI from = getEventById(entity.getFromEvent());
		EventUI to = getEventById(entity.getToEvent());
		EventLinkUI link = findLink(from, to);
		if (link != null) {
			if (link.getEntity() != null) {
				if (link.getEntity().getId() != entity.getId())
					deleteEntity(link.getEntity());
			}
			link.entity = entity;
			link.updateFromEntity();
			return;
		}
		link = makeNewLink(from, to);
		if (link != null) {
			linkBag.add(link);
			link.entity = entity;
			link.updateFromEntity();
		}
	}

	EventLinkUI findLink(EventUI from, EventUI to) {
		for (EventLinkUI l : linkBag) {
			if (l.getFromEvent()==from && l.getToEvent()==to) 
				return l;
		}
		return null;
	}
	
	void deleteLink(EventLinkUI link) {
		if (selectedLinker == link) selectedLinker = null;
		linkBag.remove(link);
		link.remove();
		// 이벤트 링크 삭제 요청
		deleteEntity(link.getEntity());
	}

	public void updateLinkProperty(EventRelationEntity data) {
		EventLinkUI link = contextLink;
		if (contextLink.getId() != data.getId()) {
			for (EventLinkUI e : linkBag) {
				if (e.getId() == data.getId()) {
					link = e;
					break;
				}
			}
		}
		link.entity = data;
		link.updateFromEntity();
	}
	@Override
	public void showLinkProperties(EventLinkUI link, int left, int top) {
		if (relationProperty == null) {
			relationProperty = new EventRelationPropertyPanel(this);
		}
		relationProperty.setData(link.getEntity());
		relationProperty.setPopupPosition(left, top);
		relationProperty.show();
	}

	@Override
	public void showLinkContextMenu(EventLinkUI link, int left, int top) {
		contextLink = link;
		menuPanelOnLink.setPopupPosition(left, top);
		menuPanelOnLink.show();
	}
	
	/*---------------------------------------------------------------
	 * GWT UI Binder 요소와 이벤트 처리기 
	 */
	
	@UiField
	MyStyle style;

	@UiField
	FlowPanel newEventPool;

	@UiField
	FlowPanel unsortedEventPool;

	@UiField
	HTMLPanel characterHeaderPanel;

	@UiField
	HTMLPanel timeAxisPanel;

	@UiField
	HTMLPanel timelineContainer;

	@UiField
	HTMLPanel timelinePanel;

	@UiField
	Button newEventButton;

	@UiField
	Button newCharacterButton;
	
	@UiHandler("newEventButton")
	void onClickNewEvent(ClickEvent e) {
		String eventName = "Event " + (getEventCount() + 1);
		storyService.createNewEvent(eventName, new AsyncCallback<EventEntity>() {

			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onSuccess(EventEntity e) {
				LocalCache.add(e);
				EventUI event = new EventUI(e);
				//event.create();
				eventBag.put(event.getEntity().getId(), event);
				newEventPool.add(event.getWidget());								
			}
			
		});
	}

	@UiHandler("newCharacterButton")
	void onClickNewCharacter(ClickEvent e) {
		String characterName = "Character " + (getCharacterCount() + 1);
		requestNewCharacter(characterName);
	}

}
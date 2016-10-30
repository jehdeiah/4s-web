package com.googlecode._4s_web.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode._4s_web.client.StoryServiceAsync;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.client.entity.StoryTimePoint;
import com.googlecode._4s_web.shared.Interval;

/**
 * 이벤트를 담화 시간축에 배열하여 플롯을 구성하는 타임라인 편집기.
 * 
 * TODO:
 * (1) 이벤트 속성 편집 
 * (2) 시점 등록
 * (3) 삽입 컷 
 * (4) 해상도 문제: 확대, 스크롤 등 
 * (5) 시간이 비는 것과 겹치는 것 알림: 이건 해상도 문제와 직결됨.
 * (6) 플롯 구조  
 * 
 * @author jehdeiah
 *
 */
public class DiscourseTimeline extends AbstractStoryPanel 
			implements HasEventPropertyPanel {

	private static DiscourseTimelineUiBinder uiBinder = GWT
			.create(DiscourseTimelineUiBinder.class);
	
	interface DiscourseTimelineUiBinder extends
			UiBinder<Widget, DiscourseTimeline> {
	}

	interface MyStyle extends CssResource {
		String event();
		String event_new();
		String event_unsorted();
		String event_sorted();
		String event_plotted();
		String event_plot_instance();
		// String insertcut();

		String timepoint_line();
		String timepoint_line_selected();
		String discourse_timepoint_line();
		String discourse_timepoint_line_selected();

		String time_axis_area();
		String plot_area();
	}

	/**
	 * 여러 이벤트 처리기와 하위 클래스에서 참조할 수 있도록 final로 선언한다.
	 */
	private final DiscourseTimeline thisPanel = this;

	/**
	 * 이벤트 표시기
	 * 
	 * 이벤트 목록에 있는 표시와 플롯 공간에 배치한 인터페이스 요소를 함께 표현한다.<br>
	 * <code>StoryTimeline.EventUI</code>와 겹치는 부분이 많지만 플롯 특성이 있어 별도록 구현한다.
	 * 
	 * @author jehdeiah
	 * 
	 */
	class EventUI implements EntityUIObject<EventEntity>, Comparable<EventUI> {
		static final int BorderWidth = 2;
		EventEntity event;
		Label widget = null;

		ResizeMouseHandler resizeHandler = null;
		
		boolean modified = false;
		/*
		 * 플롯에 배열된 이벤트 표시와 해당 담화 시간 정보.
		 * 플롯 이벤트가 아닌 경우는 무시한다.
		 */
		boolean plotInstance = false;
		Interval discourseInOut = null;

		public EventUI(EventEntity e) {
			event = e;
		}

		@Override
		public EventEntity getEntity() {
			return event;
		}
		public void setEntity(EventEntity e) {
			event = e;
		}
		@Override
		public long getId() {
			return event==null ? -1 : event.getId();
		}
		@Override
		public Widget getWidget() {
			return widget;
		}
		
		public boolean isPlotInstance() {
			return plotInstance;
		}
		
		public Interval getDiscourseInOut() {
			return discourseInOut;
		}

		public int getWidgetLeft() {
			if (widget != null) {
				// 위젯은 반드시 화면에 있어야 한다.
				// 만약 그렇지 않은 경우 예외 처리로 해서 큰 값을 넘긴다.
				if (!widget.isAttached()) return Integer.MAX_VALUE;
				return widget.getElement().getOffsetLeft();
			}
			return 0;
		}

		public int getWidgetRight() {
			if (widget != null) {
				return widget.getElement().getOffsetLeft()
						+ widget.getElement().getOffsetWidth();
			}
			return 0;
		}

		public int getWidgetTop() {
			if (widget != null) {
				return widget.getElement().getOffsetTop();
			}
			return 0;
		}

		public int getWidgetBottom() {
			if (widget != null) {
				return widget.getElement().getOffsetTop()
						+ widget.getElement().getOffsetHeight();
			}
			return 0;
		}

		/**
		 * 이벤트 표시기를 만든다. plotInstance 값에 따라 목록 요소와 플롯 공간 요소를 구분한다.
		 * 설정된 이벤트 엔티티에 따라 <code>updateWidgetStyle()</code>을 불러 위젯 스타일도 맞춘다.
		 * @param plotInstance
		 *            참이면 플롯 공간 요소, 거짓이면 목록 요소
		 */
		public void create(boolean plotInstance) {
			this.plotInstance = plotInstance;
			if (plotInstance) {
				discourseInOut = new Interval();
			}
			if (widget != null) {
				// 예외 처리의 범주 문제. 일단 만들어진 것이면 트리에서 제거만 한다.
				widget.removeFromParent();
			} else {
				// 컨텍스트 메뉴를 띄우기 위해 브라우저 이벤트를 끌어온다.
				final EventUI thisEvent = this;
				widget = new Label(event.getName()) {
					public void onBrowserEvent(com.google.gwt.user.client.Event e) {
						switch (e.getTypeInt()) {
						case com.google.gwt.user.client.Event.ONCONTEXTMENU:
							e.stopPropagation();
							e.preventDefault();
							thisPanel.showEventContextMenu(thisEvent,
									e.getClientX(), e.getClientY());
							break;
						default:
							super.onBrowserEvent(e);
						}
					}
				};
			}
			widget.sinkEvents(com.google.gwt.user.client.Event.ONCONTEXTMENU);
			// widget.getElement().setId(event.getId());
			widget.getElement().setDraggable(Element.DRAGGABLE_TRUE);
			final EventUI thisEvent = this;
			EventDragHandler h = new EventDragHandler(event.getId(), widget,
					plotInstance) {
				public void notifyDragStart(long eventId, int offsetX, int offsetY) {
					thisPanel.setDraggingEvent(thisEvent);
					thisPanel.setDragOffset(offsetX);
				}
			};
			h.setDragMargin(0, 0, EventDragHandler.DRAG_MARGIN,
					EventDragHandler.DRAG_MARGIN);
			widget.addDragStartHandler(h);
			widget.addDragEndHandler(h);
			widget.addDoubleClickHandler(new DoubleClickHandler() {

				public void onDoubleClick(DoubleClickEvent e) {
					thisPanel.showEventProperties(thisEvent.getEntity());
				}
			});
			if (plotInstance) {
				resizeHandler = new ResizeMouseHandler(widget, thisPanel.timelineEditor,
						ResizeMouseHandler.RESIZE_HORIZONTAL) {

					@Override
					protected void resizeWidget(int top, int bottom, int left,
							int right, int mouseX, int mouseY) {
						updateWidgetPosition(left, right);
						thisPanel.hilightDiscourseTimePoint(mouseX);
					}

					@Override
					protected void notifyResizeStarted() {
						thisPanel.setResizingEvent(thisEvent);
					}
					
					@Override
					protected void notifyResizeDone() {
						thisPanel.setResizingEvent(null);
						thisPanel.updateDiscourseTimePoints();
						thisEvent.modify();
					}

				};
				widget.addMouseDownHandler(resizeHandler);
				widget.addMouseMoveHandler(resizeHandler);
				widget.addMouseUpHandler(resizeHandler);
				//widget.addMouseOutHandler(mh);
			}
			// applying style...
			widget.setStyleName(thisPanel.style.event());
			updateWidget();
		}

		/**
		 * 이벤트 상자의 모양과 스토리 시간 배치를 바꾼다.
		 * 
		 * 이벤트 목록 중 스토리 시간 할당이 안 된 것과 새로 만든 것은 relative position으로 float:left 처리하여
		 * 배치한다.
		 * 
		 * 캐릭터가 할당되어 스토리 시점이 주어진 것과 플롯 요소는 absolute position으로
		 * {style.event_assigned}가 position:absolute를 부여한다.
		 * 
		 * 세로 위치는 스토리 시점이 되고 높이는 목록에서는 1 단위, 플롯 공간에서는 실제 시점 간격이 된다. (미할당 이벤트는
		 * 마지막 시점에서 1단위로 배치한다.)
		 * 
		 */
		@Override
		public void updateWidget() {
			// Events to be in new or unsorted event pool
			if (!event.isInStory() && !plotInstance) {
				widget.getElement().getStyle().clearLeft();
				widget.getElement().getStyle().clearTop();
				widget.getElement().getStyle().clearHeight();
			}
			// Assign additional styles
			if (plotInstance) {
				widget.addStyleName(thisPanel.style.event_plot_instance());
			} else {
				widget.setStyleName(thisPanel.style.event_sorted(), event.isInStory());
				widget.setStyleName(thisPanel.style.event_unsorted(), !event.isInStory()&&event.isPlotted());
				widget.setStyleName(thisPanel.style.event_new(), !event.isInStory()&&!event.isPlotted()/*&&!event.isAssigned()*/);
			}
			widget.setStyleName(thisPanel.style.event_plotted(), event.isPlotted()||plotInstance);
			// Character color
			if (event.isAssigned()) {
				CharacterEntity c = LocalCache.get(CharacterEntity.class, event.getMainCharacter());
				String color = c.getColor();
				widget.getElement().getStyle().setBackgroundColor(color);
			} else {
				widget.getElement().getStyle().clearBackgroundColor();
			}
			// 세로 위치를 스토리 시간에 맞춘다.
			if (plotInstance || event.isInStory()) {
				double top = event.isInStory() ? thisPanel.getTimelineY(event.getOrdinalStoryIn()) : 
					thisPanel.getTimelineY(StoryTimePoint.getCount());
				// 이벤트 높이는 목록에서는 기본 높이, 타임라인 평면에서는 스토리 시간 간격만큼으로 한다.
				double bottom = thisPanel.getTimelineY(
						plotInstance ? 	event.getOrdinalStoryOut() : 
										event.getOrdinalStoryIn() + 1, -BorderWidth);
				widget.getElement().getStyle().setTop(top, Unit.PCT);
				widget.getElement().getStyle()
						.setHeight(bottom - top, Unit.PCT);
			}
		}

		/**
		 * 이벤트 상자의 담화 시간에 따른 위치와 크기를 바꾼다.
		 * 이벤트 상자는 반드시 화면에 이미 표시되어 있어야 한다. 
		 * 화면 픽셀 위치를 입력으로 받고 배율 처리가 쉽도록 퍼센트 위치로 변환하여 스타일을 지정한다.
		 * 
		 * @param left
		 * @param right
		 */
		public void updateWidgetPosition(int left, int right) {
			assert widget.isAttached();
			final int MAX_ADJUST_TRIAL = 5;
			int oldLeft = getWidgetLeft();
			int oldRight = getWidgetRight();
			int deco = widget.getOffsetWidth() - widget.getElement().getClientWidth();
			double leftPCT = thisPanel.getTimelineX(left);
			double rightPCT = thisPanel.getTimelineX(right);
			if (leftPCT < 0) leftPCT = 0;
			if (leftPCT > 100) leftPCT = 100;
			if (rightPCT < 0) rightPCT = 0;
			if (rightPCT > 100) rightPCT = 100;
			//setDiscourseInOut(leftPCT, rightPCT);
			if (left != oldLeft) {
				int newLeft;
				int clientLeft = left;
				for (int i=0; i<MAX_ADJUST_TRIAL; i++) {
					widget.getElement().getStyle().setLeft(leftPCT, Unit.PCT);
					newLeft = getWidgetLeft();
					if (left < newLeft) clientLeft--;
					else if (left > newLeft) clientLeft++;
					else break;
					leftPCT = thisPanel.getTimelineX(clientLeft);
				}
			}
			if (left!=oldLeft || right!=oldRight) {
				int newRight;
				int clientRight = right - deco;
				for (int i=0; i<MAX_ADJUST_TRIAL; i++) {
					rightPCT = thisPanel.getTimelineX(clientRight);
					widget.getElement().getStyle().setWidth(rightPCT-leftPCT, Unit.PCT);
					newRight = getWidgetRight();
					if (right < newRight) clientRight--;
					else if (right > newRight) clientRight++;
					else break;
				}
			}
		}

		/**
		 * 위젯의 왼쪽 위치로 크기를 비교한다.
		 * 화면에 표시된 상태에서 비교해야 하므로 반드시 비교 전에 DOM에 추가되어야 한다.
		 */
		@Override
		public int compareTo(EventUI o) {
			if (this == o) return 0;
			if (o == null) return 1;
			int x = getWidgetLeft();
			int y = o.getWidgetLeft();
			return (x <= y) ? -1 : 1;//(x > y) ? 1 : 0;
		}

		/**
		 * 화면 요소의 수정 표시는 일차적으로 변경된 엔티티를 저장할 때 쓰인다.
		 * 이벤트 엔티티가 변경되는 것은 스토리 이벤트에서 표시하고,
		 * 플롯 요소일 때는 화면에서 변경한 담화 시간을 엔티티에 반영하기만 한다.
		 * 그러므로 편집기에서 담화 시간이 변경된 플롯 요소에 대응하는 스토리 이벤트의 <code>modify()</code>를 반드시 불러야 한다. 
		 */
		@Override
		public void modify() {
			if (plotInstance) {
				event.removeFromDiscourse(discourseInOut);
				double begin = thisPanel.getTimelineX(getWidgetLeft());
				double end = thisPanel.getTimelineX(getWidgetRight());
				discourseInOut.setRange(begin, end);
				event.addToDiscourse(begin, end);
				thisPanel.notifyPlotEventModified(event.getId());
			} else {
				modified = true;
			}
		}
		@Override
		public boolean isModified() { return modified; }
		@Override
		public void invalidate() { modified = false; }
		
		// 초기화 용도이므로 기존의 값을 지우지는 않는다.
		public void setDiscourseInOut(double begin, double end) {
			if (discourseInOut == null) discourseInOut = new Interval(begin, end);
			else discourseInOut.setRange(begin, end);
		}
		@Override
		public void updateFromEntity() {
			// 가능한 속성 변화는 캐릭터 할당에 대한 색깔과 이벤트 이름 변경이다.
			if (widget.getText().equals(event.getName()) == false) {
				widget.setText(event.getName());
			}
			updateWidget();	// 색깔은 여기서 바꾼다.
		}
	}
	

	boolean layouted = false;
	/**
	 *  스토리 구성 요소로서의 이벤트 모음 (Key: 이벤트 ID)
	 */
	Map<Long, EventUI> eventBag;
	
	/*
	 *  담화에 배치된 이벤트 모음
	 */
	
	/**
	 *  이벤트별로 담화에 배치된 이벤트 모음 (Key: 이벤트 ID)
	 */
	Map<Long, SortedSet<EventUI>> plotInstanceSetOfEvent;
	/**
	 *  담화에 배치된 이벤트를 담화 시간별로 정렬하여 모은 집합
	 */
	SortedSet<EventUI> discourseEventBag;
	
	/*
	 *  시점 관리 
	 */
	int[] numberOfEventsAtTimePoint;
	ArrayList<SimplePanel> timePointBag;
	ArrayList<SimplePanel> discourseTimePointBag;

	private PopupPanel menuPanelOnEvent = null;
	private EventUI contextEvent = null;

	final public int EventMargin = 3; // px
	final public int DefaultEventWidth = 100; // px
	final public int DefaultEventHeight = 20; // px

	double timelineEditorWidth;
	EventUI resizingEvent = null;
	// 세로축 화면 높이
	double basePanelHeight;
	
	public double getBasePanelHeight() {
		return basePanelHeight;
	}

	public void setBasePanelHeight(double basePanelHeight) {
		this.basePanelHeight = basePanelHeight;
	}

	// 드래그 이벤트 
	EventUI draggingEvent = null;
	int dragOffset = 0;

	// 편집기 해상도
	double desiredResolution = 1;
	double resolutionBase = 10000;

	public double getResolutionBase() {
		return resolutionBase;
	}

	public void setResolutionBase(double resolutionBase) {
		this.resolutionBase = resolutionBase;
	}

	/*
	 * 속성창 
	 */
	final int popupZIndex = 9;
	EventPropertyPanel eventProperty = null;

	// GWT needs this...?
	protected DiscourseTimeline() {}
	
	protected void setResizingEvent(EventUI event) {
		resizingEvent = event;
	}

	protected void setDragOffset(int offsetX) {
		dragOffset = offsetX;
	}

	protected void setDraggingEvent(EventUI event) {
		draggingEvent = event;
	}

	public DiscourseTimeline(StoryServiceAsync service, EventBus bus) {
		super(service, bus);
		initWidget(uiBinder.createAndBindUi(this));
		eventBag = new HashMap<Long, EventUI>();
		plotInstanceSetOfEvent = new HashMap<Long, SortedSet<EventUI>>();
		discourseEventBag = new TreeSet<EventUI>();
		timePointBag = new ArrayList<SimplePanel>();
		discourseTimePointBag = new ArrayList<SimplePanel>();
	}

	/**
	 * 위젯을 초기화하는데 화면 요소만 등록하고 스토리 내용은 취급하지 않는다.
	 */
	protected void onLoad() {
		super.onLoad();
		// 가로축 스크롤 추가하기. 
		// 환경에 따라 화면 구성이 달라지는 것을 막기 위해 아예 스크롤 레이어를 둔다.
		editorScroll.getElement().getStyle().setOverflowX(Overflow.SCROLL);
		editorScroll.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
		editorScroll.addDomHandler(new ScrollHandler() {
			public void onScroll(ScrollEvent event) {
				int scrollX = editorScroll.getElement().getScrollLeft();
				timelineEditorContainer.getElement().getStyle().setLeft(-scrollX, Unit.PX);
			}
		}, ScrollEvent.getType());
		// 타임라인 편집기 축 그리기
		storyTimeAxis.getElement().getParentElement().addClassName(style.time_axis_area());
		timelineEditor.getElement().getParentElement()
				.addClassName(style.plot_area());
		// 화면 스크롤에서 제외된 시간축을 세로 스크롤에 맞게 움직인다.
		sortedEventContainer.addDomHandler(new ScrollHandler() {
			public void onScroll(ScrollEvent event) {
				int scrollY = sortedEventContainer.getElement().getScrollTop();
				storyTimeAxis.getElement().getStyle().setTop(-scrollY, Unit.PX);
				timelineEditor.getElement().getStyle()
						.setTop(-scrollY, Unit.PX);
			}
		}, ScrollEvent.getType());
		timelineEditor.addDomHandler(new DragEnterHandler() {
			public void onDragEnter(DragEnterEvent event) {
				event.preventDefault();
			}
		}, DragEnterEvent.getType());
		timelineEditor.addDomHandler(new DragOverHandler() {
			public void onDragOver(DragOverEvent event) {
				event.preventDefault();
				int offsetX = dragOffset;
				int relativeLeft = event.getNativeEvent().getClientX()
						- timelineEditor.getAbsoluteLeft() - offsetX;
				int timePoint = /*timelineEditor.getElement().getScrollLeft() +*/ relativeLeft;
				int eventWidth = draggingEvent.getWidget().getElement().getOffsetWidth();
				hilightDiscourseTimePoint(timePoint, timePoint + eventWidth);				
			}
		}, DragOverEvent.getType());

		/*
		 * 이벤트를 타임라인에 놓기 위한 핸들러
		 */
		timelineEditor.addDomHandler(new DropHandler() {
			public void onDrop(DropEvent event) {
				long eventId = Long.parseLong(event.getData("event"));
				boolean plotInstance = Boolean.parseBoolean(event
						.getData("plot"));
				EventEntity e = LocalCache.get(EventEntity.class, eventId);
				int offsetX = Integer.valueOf(event.getData("offset_x"));
				int relativeLeft = event.getNativeEvent().getClientX()
						- timelineEditor.getAbsoluteLeft() - offsetX;
				if (plotInstance) {
					//int point = Integer.valueOf(event.getData("x")) - timelineEditor.getAbsoluteLeft();
					//EventUI eUI = pickEvent(eventId, point);
					movePlottedEvent(draggingEvent, relativeLeft);
				} else {
					plotEvent(e, relativeLeft);
				}
				draggingEvent = null;
				dragOffset = 0;
			}
		}, DropEvent.getType());
		/*
		 * 이벤트 크기 조절을 위한 마우스 핸들러 
		 */
		timelineEditor.addDomHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent e) {
				if (resizingEvent != null) {
					e.preventDefault();
					timelineEditor.getElement().getStyle().setCursor(Cursor.COL_RESIZE);
					resizingEvent.resizeHandler.resizeElement(e.getX(), e.getY());
				} else {
					timelineEditor.getElement().getStyle().setCursor(Cursor.AUTO);
				}
				hilightDiscourseTimePoint(e.getX());
			}
		}, MouseMoveEvent.getType());
		timelineEditor.addDomHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent e) {
				e.preventDefault();
			}
		}, MouseOutEvent.getType());
		timelineEditor.addDomHandler(new MouseUpHandler() {
			public void onMouseUp(MouseUpEvent e) {
				if (resizingEvent != null) {
					if ((e.getNativeButton()&NativeEvent.BUTTON_LEFT) != 0) {
						e.preventDefault();
						resizingEvent.modify();
						resizingEvent.resizeHandler.finishResize();
						timelineEditor.getElement().getStyle().setCursor(Cursor.AUTO);
					}
				}
			}
		}, MouseUpEvent.getType());
		/*
		 * 컨텍스트 메뉴
		 */
		menuPanelOnEvent = new PopupPanel();
		MenuBar menu = new MenuBar(true);
		menu.addItem("Edit Properties...", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnEvent.hide();
				showEventProperties(contextEvent.getEntity());
			}
		});
		menu.addItem("Delete", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnEvent.hide();
				deleteEvent(contextEvent);
			}
		});
		menuPanelOnEvent.add(menu);
		menuPanelOnEvent.getElement().getStyle().setZIndex(popupZIndex);
	}
	
	/**
	 * UI 작업 내용으로 바꾼 항목 고르기 
	 */
	@Override
	public void updateChangedEntities() {
		// TODO: 타임라인 꼬리표 다는 것 처리.
		annotationChanged = true;
		// 캐릭터는 여기서 바뀌는 것이 없다. (아직까지는...)
		// 스토리 시간과 어노테이션은 여기서 한꺼번에 처리한다.
		// 담화 시간 처리는 매번 이루어지고 스토리 이벤트에 변경을 알리므로 여기서는 따로 처리하지 않는다.
		for (EventEntity e : LocalCache.entities(EventEntity.class, eventArrayType)) {
			EventUI eUI = eventBag.get(e.getId());
			if (eUI!=null && eUI.isModified()) {
				saveEntity(e);
				eUI.invalidate();
			}
		}
	}
	
	/**
	 * 스토리 화면 요소들을 모두 지운다.
	 */
	@Override
	public void clear() {
		// 애노테이션
		// 시점 보조선
		for (SimplePanel p : timePointBag) {
			if (p.isAttached()) p.removeFromParent();
		}
		timePointBag.clear();
		for (SimplePanel p : discourseTimePointBag) {
			if (p.isAttached()) p.removeFromParent();
		}
		discourseTimePointBag.clear();
		// 이벤트
		for (EventUI event : eventBag.values()) {
			Widget w = event.getWidget(); 
			if (w.isAttached()) w.removeFromParent();
		}
		eventBag.clear();
		for (EventUI event : discourseEventBag) {
			Widget w = event.getWidget();
			if (w.isAttached()) w.removeFromParent();
		}
		discourseEventBag.clear();
		plotInstanceSetOfEvent.clear();
	}

	/**
	 * 로컬 캐시에서 엔티티를 읽어서 편집기 요소를 갱신한다.
	 */
	@Override
	public void updateAll() {
		if (layouted == false) {
			// 스크롤을 항상 표시하여 옆에 있는 목록과 높이를 맞춘다.
			Element parentElement = timelineEditorContainer.getElement()
					.getParentElement();
			int scrollHeight = parentElement.getOffsetHeight()
					- parentElement.getClientHeight();
			if (scrollHeight == 0) // sometimes it doesn't work...
				scrollHeight = 15;
			timelineEditorContainer.getElement().getStyle()
			.setMarginBottom(-scrollHeight, Unit.PX);
			layouted = true;
		}
		/*
		 * 시간 정보를 얻었으니 캐릭터와 이벤트를 불러오기 전에 화면을 맞춘다.
		 */
		numberOfEventsAtTimePoint = new int[StoryTimePoint.getCount()];
		// 스토리 시점에 따라 패널 높이 지정
		basePanelHeight = EventMargin + (DefaultEventHeight + EventMargin)
				* (StoryTimePoint.getCount() + 2);
		int panelHeight = sortedEventContainer.getElement()
				.getClientHeight();
		if (basePanelHeight < panelHeight) {
			basePanelHeight = panelHeight;
		}
		setZoomY(1.0);
		
		// 캐릭터가 할당된 것을 반영하고 새로 만든 것을 여기서 만든다.
		EventEntity[] eventSet = LocalCache.entities(EventEntity.class, eventArrayType); 
		for (EventEntity e : eventSet) {
			EventUI eUI = eventBag.get(e.getId());
			if (eUI == null) {
				eUI = new EventUI(e);
				eUI.create(false);
				if (e.isInStory()) {
					addSortedEvent(eUI, true);
				} else if (e.isPlotted()) {
					unsortedEventPanel.add(eUI.getWidget());
				} else {
					newEventPool.add(eUI.getWidget());
				}
				eventBag.put(e.getId(), eUI);
			} else {
				eUI.setEntity(e);
				eUI.updateWidget();
				if (e.isInStory()) {
					unsortedEventPanel.remove(eUI.getWidget());
					addSortedEvent(eUI, true);
				} else {
					if (sortedEventPanel.remove(eUI.getWidget())) {
						if (e.isPlotted()) unsortedEventPanel.add(eUI.getWidget());
						else newEventPool.add(eUI.getWidget());
					}
				}
			}
			// 플롯된 사건들을 타임라인 편집기에 추가한다.
			plotEventFromEntity(e);
		}
		// 다른 화면에서 지운 것을 찾아 지운다.
		for (Long eventId : eventBag.keySet()) {
			if (LocalCache.get(EventEntity.class, eventId) == null) {
				EventUI eUI = eventBag.remove(eventId);
				eUI.getWidget().removeFromParent();
				Collection<EventUI> plotEventSet = plotInstanceSetOfEvent.remove(eventId);
				if (plotEventSet != null) {
					for (EventUI de : plotEventSet) {
						de.getWidget().removeFromParent();
						discourseEventBag.remove(de);
					}
				}
				eventBag.remove(eventId);
			}
		}
		// 시점 그리기
		drawStoryTimePoints();
		updateDiscourseTimePoints();
		timelineEditorWidth = timelineEditor.getOffsetWidth();
	}

	@Override
	public void invalidate() {
		
	}
	
	/*---------------------------------------------------------------
	 * 이벤트 관련 메쏘드 
	 */
	
	protected void showEventContextMenu(EventUI event, int x, int y) {
		 contextEvent = event;
		 menuPanelOnEvent.setPopupPosition(x, y);
		 menuPanelOnEvent.show();
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
	public void updateEventProperties(EventEntity event) {
		// 속성창에서 바꾸고 화면에 영향을 주는 것은 캐릭터 할당으로 인한 색깔 변경이다.
		// 속성이 바뀐 것은 스토리 이벤트에 알려야 한다.
		EventUI eUI = eventBag.get(event.getId());
		eUI.setEntity(event);
		eUI.updateFromEntity();
		eUI.modify();
		if (event.getOccurrence() > 0) {
			Collection<EventUI> plotEvents = plotInstanceSetOfEvent.get(event.getId());
			for (EventUI e : plotEvents) {
				e.setEntity(event);
				e.updateFromEntity();
			}
		}
	}

	void addSortedEvent(EventUI e, boolean display) {
		int storyIn = e.getEntity().getOrdinalStoryIn();
		e.updateWidget();
		numberOfEventsAtTimePoint[storyIn]++;
		sortedEventPanel.add(e.getWidget());
	}

	EventUI pickEvent(long eventId, int pickPoint) {
		SortedSet<EventUI> eventSet = plotInstanceSetOfEvent.get(eventId);
		for (EventUI e : eventSet) {
			int left = e.getWidgetLeft();
			int right = e.getWidgetRight();
			if (left <= pickPoint && pickPoint <= right)
				return e;
		}
		return null;
	}

	void movePlottedEvent(EventUI event, int start) {
		if (event == null)
			return;
		if (uniformPlot.getValue()) {
			int count = discourseEventBag.size();
			int width = timelineEditor.getOffsetWidth() / count;
			int pos = event.getWidgetLeft() / width;
			int newPos = (int)Math.ceil((double)start / width);
			if (start <= discourseEventBag.first().getWidgetLeft()) newPos = 0;
			if (pos == newPos) return;
			rearrangeUniformPlot(event, pos, newPos);
		} else {
			int offset = start - event.getWidgetLeft();
			if (offset == 0) return;
			int right = event.getWidgetRight() + offset;
			if (right > timelineEditor.getOffsetWidth())
				right = timelineEditor.getOffsetWidth();
			discourseEventBag.remove(event);
			plotInstanceSetOfEvent.get(event.getId()).remove(event);
			// Deprecated: 테두리 보정: 실제로 그릴 때 테두리 기준으로 그리려고 위젯 너비를 줄이므로 아예 늘려서 넘겨준다.
			event.updateWidgetPosition(start, right);// + EventUI.BorderWidth * 2);
			event.modify();
			discourseEventBag.add(event);
			plotInstanceSetOfEvent.get(event.getId()).add(event);
		}
		updateDiscourseTimePoints();
	}
	
	void notifyPlotEventModified(long eventId) {
		eventBag.get(eventId).modify();
	}

	void rearrangeUniformPlot() {
		rearrangeUniformPlot(null, -1, -1);
		updateDiscourseTimePoints();
	}
	/**
	 * 사건을 전체 담화 시간에 등간격으로 배치한다.
	 * 위젯의 화면 표시는 정리가 된 상태로 넘어와서 위치만 잡아주면 된다.
	 * 
	 * @param event 플롯 변경을 유발하는 사건. 새로 추가되거나 이동/삭제될 사건이다. Null이면 전체를 다시 정렬한다.
	 * @param sourcePos 기존 사건 위치. -1일 때는 새로 추가되는 사건이다.
	 * @param targetPos 옮길 사건 위치. -1일 때는 삭제하는 사건이다.
	 */
	void rearrangeUniformPlot(EventUI event, int sourcePos, int targetPos) {
		if (event!=null && sourcePos==targetPos) return;
		// 원래 이벤트를 빼둔다.
		if (event != null) discourseEventBag.remove(event);
		EventUI[] plotted = discourseEventBag.toArray(new EventUI[0]);
		SortedSet<EventUI> instanceSet = event==null ? null : plotInstanceSetOfEvent.get(event.getId());
		if (event!=null && instanceSet==null) {
			instanceSet = new TreeSet<EventUI>();
			plotInstanceSetOfEvent.put(event.getId(), instanceSet);
		}
		int count = discourseEventBag.size();
		if (event!=null && targetPos!=-1) count++; // 자리 잡고 들어갈 이벤트 하나 추가
		if (count == 0) return;
		int width = timelineEditor.getOffsetWidth() / count;
		int beginPos = 0;
		int endPos = count - 1;
		if (sourcePos != -1) {
			if (sourcePos < targetPos) {
				beginPos = sourcePos;
				endPos = targetPos;
			} else {
				beginPos = targetPos;
				endPos = sourcePos - 1;
			}
		}
		int left = beginPos*width;
		int right = beginPos==(count-1) ? timelineEditor.getOffsetWidth() : left + width;
		for (int i=beginPos; i<=endPos; i++) {
			if (i == targetPos) {
				event.updateWidgetPosition(left, right);
				discourseEventBag.add(event);
				instanceSet.add(event);
				event.modify();
				left += width;
				right = (i==count-1) ? timelineEditor.getOffsetWidth() : left + width;
			}
			if (i < plotted.length) {
				discourseEventBag.remove(plotted[i]);
				SortedSet<EventUI> set = plotInstanceSetOfEvent.get(plotted[i].getId());
				set.remove(plotted[i]);
				plotted[i].updateWidgetPosition(left, right);
				discourseEventBag.add(plotted[i]);
				set.add(plotted[i]);
				plotted[i].modify();
			}
			left += width;
			right = (i==count-1) ? timelineEditor.getOffsetWidth() : left + width;
		}
	}
	
	/**
	 * 타임라인 편집으로 이벤트를 플롯팅하는 것으로 화면상의 시작 좌표를 받아서 기본 너비로 배치하고,
	 * 이벤트 엔티티에 해당 담화 시간 정보를 등록한다.
	 * @param e 이벤트 엔티티 
	 * @param start 화면 시작점 좌표 
	 */
	void plotEvent(EventEntity e, int start) {
		EventUI eUI = new EventUI(e);
		eUI.create(true);
		timelineEditor.add(eUI.getWidget());
		int right = start + DefaultEventWidth;
		if (right > timelineEditor.getOffsetWidth())
			right = timelineEditor.getOffsetWidth();
		/* 
		 * 사건 순서를 중요하게 보고, 모든 사건을 등간격으로 전체 담화 시간에 고루 배치한다.
		 */
		if (uniformPlot.getValue()) {
			int plotCount = discourseEventBag.size();
			int uniformWidth = timelineEditor.getOffsetWidth() / (plotCount+1);
			int pos = (int)Math.ceil((double)start / uniformWidth);
			if (pos > plotCount) pos = plotCount;
			if (discourseEventBag.isEmpty() || discourseEventBag.iterator().next().getWidgetLeft()>=start) pos = 0;
			rearrangeUniformPlot(eUI, -1, pos);
//			start = uniformWidth*plotCount;
//			right = plotCount==0 ? timelineEditor.getOffsetWidth() : start + uniformWidth;
//			/* 기존 플롯 사건들 재정렬 */
//			EventUI[] plotted = discourseEventBag.toArray(new EventUI[0]);
//			int leftPX = 0;
//			discourseEventBag.clear();
//			for (EventUI p : plotted) {
//				SortedSet<EventUI> set = plotInstanceSetOfEvent.get(p.getId());
//				set.remove(p);
//				p.updateWidgetPosition(leftPX, leftPX + uniformWidth);
//				set.add(p);
//				discourseEventBag.add(p);
//				p.modify();
//				//p.setDiscourseInOut(beginPCT, beginPCT + widthPCT);
//				leftPX += uniformWidth;
//			}
		} else {
			eUI.updateWidgetPosition(start, right);// + EventUI.BorderWidth * 2);
			SortedSet<EventUI> set = plotInstanceSetOfEvent.get(e.getId());
			if (set == null) {
				set = new TreeSet<EventUI>();
				plotInstanceSetOfEvent.put(e.getId(), set);
				// updates the corresponding event in the list.
				double beginPCT = getTimelineX(eUI.getWidgetLeft());
				double endPCT = getTimelineX(eUI.getWidgetRight());
				e.addToDiscourse(beginPCT, endPCT);
				eUI.setDiscourseInOut(beginPCT, endPCT);
			}
			set.add(eUI);
			discourseEventBag.add(eUI);
		}
		EventUI storyEventUI = eventBag.get(e.getId());
		storyEventUI.updateWidget();
		storyEventUI.modify();
		
		updateDiscourseTimePoints();
	}
	
	/**
	 * 데이터스토어와 같이 외부에서 가져온 이벤트 엔티티에 대응하는 플롯 이벤트 상자들을 만든다.<br>
	 * 스토리 이벤트는 다른 곳에서 만들고 이 메쏘드를 불러야 한다.
	 * @param event
	 */
	void plotEventFromEntity(EventEntity event) {
		SortedSet<EventUI> plotInstanceSet = plotInstanceSetOfEvent.get(event.getId());
		if (event.getOccurrence()==0 && plotInstanceSet==null) 
			return;
		Collection<Interval> discourseInOut = event.getDiscourseInOut();
		ArrayList<EventUI> plotEventBag = (plotInstanceSet==null) ? new ArrayList<EventUI>() : new ArrayList<EventUI>(plotInstanceSet);
		int oldSize = plotEventBag.size();
		int plotCount = 0;
		for (Interval r : discourseInOut) {
			EventUI eUI = (plotCount < oldSize) ? plotEventBag.get(plotCount) : null;
			if (eUI == null) {
				eUI = new EventUI(event);
				eUI.create(true);
				plotEventBag.add(eUI);
				timelineEditor.add(eUI.getWidget());
			} else {
				discourseEventBag.remove(eUI);
				eUI.setEntity(event);
				eUI.updateWidget(); // 스토리 타임이 바꼈을 수 있다.
			}
			int left = getTimelineOffsetX(r.getBegin());
			int right = getTimelineOffsetX(r.getEnd());
			eUI.updateWidgetPosition(left, right);
			eUI.setDiscourseInOut(r.getBegin(), r.getEnd());
			discourseEventBag.add(eUI);
			plotCount++;
		}
		for (int i=oldSize-1; i>=plotCount; i--) {
			EventUI eUI = plotEventBag.remove(i);
			eUI.getWidget().removeFromParent();
			discourseEventBag.remove(eUI);
		}
		if (plotInstanceSet == null) plotInstanceSet = new TreeSet<EventUI>();
		else plotInstanceSet.clear();
		plotInstanceSet.addAll(plotEventBag);
		plotInstanceSetOfEvent.put(event.getId(), plotInstanceSet);
		EventUI storyEventUI = eventBag.get(event.getId());
		storyEventUI.updateWidget();		
	}

	/**
	 * 이벤트 상자를 선택해서 삭제한다.
	 * 플롯 요소인 경우는 해당 플롯 요소만 삭제하고, 스토리 이벤트인 경우는 플롯된 것까지 전부 삭제한다.
	 * 
	 * @param event 삭제할 이벤트 상자 
	 */
	public void deleteEvent(EventUI event) {
		EventEntity entity = event.getEntity();
		if (event.isPlotInstance()) {
			/* 플롯 요소 삭제 처리 순서
			 * 1) 동일 이벤트 플롯 집합
			 * 2) 전체 담화 순서 집합
			 * 3) 화면에서 제거
			 * 4) 삭제한 담화 구간을 엔티티에서 제거
			 * 5) 스토리 이벤트 속성 갱신
			 * 6) 변경사항 저장 요청
			 */
			SortedSet<EventUI> set = plotInstanceSetOfEvent.get(entity.getId());
			set.remove(event);
			discourseEventBag.remove(event);
			event.getWidget().removeFromParent();
			entity.removeFromDiscourse(event.getDiscourseInOut());
			EventUI storyEvent = eventBag.get(entity.getId());
			storyEvent.updateWidget();
			storyEvent.modify();
			// FIXME: 저장을 언제 할까?
			applyChanges();
			updateDiscourseTimePoints();
		} else {
			/* 스토리 요소 삭제 처리 순서
			 * 1) 플롯 요소 모두 삭제
			 * 2) 담화 순서 삭제
			 * 3) 화면에서 제거
			 * 4) 스토리 이벤트 삭제 요청
			 */
			SortedSet<EventUI> set = plotInstanceSetOfEvent.get(entity.getId());
			if (set!=null && !set.isEmpty()) {
				for (EventUI eUI : set) {
					discourseEventBag.remove(eUI);
					eUI.getWidget().removeFromParent();
				}
			}
			deleteEntity(entity);
			// FIXME: 이벤트가 삭제되면 스토리 시점을 당겨야 할까?
		}
	}
	
	/*---------------------------------------------------------------
	 * 시점 관련 메쏘드 
	 */
	public double getTimelineEditorWidth() {
		return timelineEditorWidth;
	}
	
	void hilightDiscourseTimePoint(int... timePoints) {
		SortedSet<Integer> tps = new TreeSet<Integer>();
		for (int tp: timePoints) {
			tps.add(tp);
		}
		for (SimplePanel line : discourseTimePointBag) {
			if (tps.contains(line.getElement().getOffsetLeft())) {
				line.addStyleName(style.discourse_timepoint_line_selected());
			} else {
				line.removeStyleName(style.discourse_timepoint_line_selected());
			}
		}
	}
	
	void updateDiscourseTimePoints() {
		SortedSet<Integer> tps = new TreeSet<Integer>();
		for (EventUI e: discourseEventBag) {
			tps.add(e.getWidgetLeft());
			tps.add(e.getWidgetRight());
		}
		int i=0;
		int count = discourseTimePointBag.size();
		for (Integer tp : tps) {
			SimplePanel line = null;
			if (i < count) line = discourseTimePointBag.get(i);
			if (line == null) {
				line = new SimplePanel();
				line.setStyleName(style.discourse_timepoint_line());
				timelineEditor.add(line);
				discourseTimePointBag.add(line);
			} else {
				line.removeStyleName(style.discourse_timepoint_line_selected());
			}
			double pct = getTimelineX(tp.intValue());
			line.getElement().getStyle().setLeft(pct, Unit.PCT);
			i++;
		}
		for (; count>i; count--) {
			discourseTimePointBag.remove(i).removeFromParent();
		}
	}
	
	double getTimelineY(int timePoint) {
		return getTimelineY(timePoint, 0);
	}

	double getTimelineY(int timePoint, int deltaPX) {
		double y = 0;
		if (timePoint >= 0 && timePoint < StoryTimePoint.getCount()) {
			y = (EventMargin + DefaultEventHeight) * timePoint + EventMargin
					+ deltaPX;
		} else if (timePoint == StoryTimePoint.getCount()) {
			y = (basePanelHeight - DefaultEventHeight) + deltaPX;
		} else {
			y = basePanelHeight + deltaPX;
		}
		return y / basePanelHeight * 100.;
	}

	double getTimelineX(int screenOffsetX) {
		return getTimelineX(screenOffsetX, 0);
	}

	double getTimelineX(int screenOffsetX, int delta) {
		final double width = timelineEditor.getOffsetWidth();
		double pct = (double)(screenOffsetX - delta) / width * 100.0;
		return Math.round(pct*resolutionBase)/resolutionBase;
	}
	
	int getTimelineOffsetX(double pct) {
		return (int)(timelineEditor.getOffsetWidth() * pct /100);// + 0.5);
	}

	void drawStoryTimePoints() {
		int count = StoryTimePoint.getCount() + 1;
		for (int i = (timePointBag.size() - 1); i >= count; i--) {
			SimplePanel p = timePointBag.remove(i);
			p.removeFromParent();
		}
		if (count < timePointBag.size()) {
			timePointBag.subList(count, timePointBag.size()).clear();
		}
		for (int i = 0; i < count; i++) {
			double top = getTimelineY(i);
			if (i < timePointBag.size()) {
				timePointBag.get(i).getElement().getStyle()
						.setTop(top, Unit.PCT);
			} else {
				SimplePanel p = new SimplePanel();
				p.setStyleName(style.timepoint_line());
				p.getElement().getStyle().setTop(top, Unit.PCT);
				timePointBag.add(p);
				timelineEditor.add(p);
			}
		}
	}

	void setZoomY(double zoomFactor) {
		double height = basePanelHeight * zoomFactor;
		sortedEventPanel.getElement().getStyle().setHeight(height, Unit.PX);
		storyTimeAxis.getElement().getStyle().setHeight(height, Unit.PX);
		timelineEditor.getElement().getStyle().setHeight(height, Unit.PX);
	}

	void setZoomX(double zoomFactor) {
		String width = Double.toString(zoomFactor*100) + "%";
		timelineEditor.getElement().getParentElement().getStyle().setWidth(zoomFactor*100,  Unit.PCT);
		scrollWidth.setWidth(width);
	}
	
	/*---------------------------------------------------------------
	 * GWT UI Binder 요소와 이벤트 처리기 
	 */
	
	@UiField
	MyStyle style;

	@UiField
	FlowPanel newEventPool;

	@UiField
	FlowPanel unsortedEventPanel;

	@UiField
	HTMLPanel sortedEventContainer;

	@UiField
	HTMLPanel sortedEventPanel;

	@UiField
	HTMLPanel storyTimeAxis;

	@UiField
	LayoutPanel timelineEditorContainer;
	
	@UiField
	SimplePanel editorScroll;
	
	@UiField
	HTML scrollWidth;

	@UiField
	HTMLPanel timelineEditor;

	@UiField
	CheckBox uniformPlot;
	
	@UiHandler("newEventButton")
	void onClickNewEvent(ClickEvent e) {
		String eventName = "Event " + (eventBag.size() + 1);
		storyService.createNewEvent(eventName, new AsyncCallback<EventEntity>() {

			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onSuccess(EventEntity e) {
				LocalCache.add(e);
				EventUI event = new EventUI(e);
				event.create(false);
				eventBag.put(event.getEntity().getId(), event);
				newEventPool.add(event.getWidget());								
			}
			
		});
	}
	
	@UiHandler("uniformPlot")
	void onClickUniformPlot(ClickEvent e) {
		boolean value = ((CheckBox)e.getSource()).getValue();
		if (value) {
			rearrangeUniformPlot();
		}
	}

	@UiHandler("clearButton")
	void onClickClearButton(ClickEvent e) {
		/* 모든 플롯을 지운다. */
		TreeSet<Long> eventIds = new TreeSet<Long>();
		for (EventUI eUI : discourseEventBag) {
			eUI.getWidget().removeFromParent();
			eventIds.add(eUI.getId());
		}
		discourseEventBag.clear();
		plotInstanceSetOfEvent.clear();
		for (Long id : eventIds) {
			EventUI eUI = eventBag.get(id);
			eUI.getEntity().getDiscourseInOut().clear();
			eUI.updateWidget();
			eUI.modify();
		}
		updateDiscourseTimePoints();
	}
	
	public boolean checkAnomaly() {
		
		return true;
	}
}
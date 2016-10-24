package com.googlecode._4s_web.client.ui;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode._4s_web.client.StoryServiceAsync;
import com.googlecode._4s_web.client.entity.Category;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.shared.Interval;

/**
 * 카테고리 뷰
 * <p>
 * 플롯된 이벤트를 여러 기준에서 보고 연속성 등을 분석한다.
 * <p>
 * 허용하는 작업: 
 * 	1) 카테고리 선택, 생성, 변경. 
 * 	2) 이벤트를 다른 카테고리로 옮기기.
 * 이벤트의 담화 시간을 바꾸거나 이벤트를 지우는 등 플롯 변경 작업은 하지 않는다.
 *
 * TODO:
 * 	1) 이벤트 속성을 바꾸면 바뀐 것에 넣어 주기 (완료?) 
 *	2) 카테고리 이동 동작 확인 
 *	3) 팝업 메뉴로 카테고리 변경, 제거 (일부 확인)
 *	4) 카테고리가 캐릭터일 경우 속성창 띄우기 
 * 
 * @author 유병국, jehdeiah
 *
 */
public class CategoryView extends AbstractStoryPanel implements HasEventPropertyPanel{	
	
	/**
	 * UI Binder for CategoryView
	 */
	interface CategoryViewUiBinder extends UiBinder<Widget, CategoryView> {}

	/**
	 * 이벤트 핸들러나 하위 클래스에서 참조하는 이 패널
	 */
	final CategoryView thisPanel = this;
	
	/*
	 * 화면 레이아웃 상수들
	 */
	private static final int LINE_ADJUST   = 3;  //px
	private static final int MAIN_CATEGORY_MARGIN = 10; //px
	private static final int SUB_CATEGORY_MARGIN  = 3;  //px
	private static final int DEFAULT_CATEGORY_WIDTH = 100; // px
	private static final int DEFAULT_CATEGORY_HEIGHT = 20; // px	
	
	/**
	 * 카테고리의 UI
	 * 
	 * 각 카테고리 요소별로 존재한다.
	 * 
	 * 화면 좌표는 카테고리 뷰에서 얻는다. (14/12/03 jehdeiah)
	 * 
	 * @author user
	 *
	 */
	class CategoryUI implements Comparable<CategoryUI> {
		
		private final Category.Entry category;
		private final Category.Entry upperCategory;
		
		/*
		 * 카테고리 순서. 1부터 시작한다.
		 */
		/** 메인 카테고리 순서 */
		private int mainOrder = 0;
		/** 메인 카테고리 안에서 서브 카테고리 순서이나, 메인 카테고리일 때 그 안에 있는 서브 카테고리 개수.
		 * 0이면 메인 카테고리로만 분류하는 경우가 된다.
		 */
		private int subOrder = 0;
		/** 카테고리의 세로 배열 순서. 서브 카테고리가 없는 경우 메인 카테고리 순서와 같다. */
		private int totalOrder = 0;
		
		/**
		 * 카테고리에 속한 이벤트 개수
		 */
		private int count = 0;
		

		/**
		 * 카테고리 표시 위젯
		 */
		private Label widget;
		/**
		 *  뷰에서 카테고리 구분 영역 표시 
		 */
		private SimplePanel categoryDiv;
		
		public CategoryUI(Category.Entry category){
			this.upperCategory = null;
			this.category = category;			
			this.widget = new Label(category.value());			
			this.categoryDiv = null;
		}
		
		public CategoryUI(Category.Entry mainCategory, Category.Entry subCategory){
			this.upperCategory = mainCategory;
			this.category = subCategory;			
			this.widget = new Label(category.value());			
			this.categoryDiv = null;
		}
		
		/*
		 * 카테고리에 해당하는 이벤트 개수
		 */
		public void add() { count++; }
		public void clearCount() { count = 0; }
		public int count() { return count; }

		public int getMainOrder() {
			return mainOrder;
		}
		public void setMainOrder(int mainOrder) {
			this.mainOrder = mainOrder;
		}
		public int getSubOrder() {
			return subOrder;
		}
		public void setSubOrder(int subOrder) {
			this.subOrder = subOrder;
		}
		public int getTotalOrder() {
			return totalOrder;
		}
		public void setTotalOrder(int totalOrder) {
			this.totalOrder = totalOrder;
		}

		public Widget getWidget(){
			return this.widget;
		}
		
		public SimplePanel getCategoryDeco(){
			return this.categoryDiv;
		}
		
		public Category.Entry getCategory(){
			return this.category;			
		}

		public String getCategoryValue() {
			return category.value();
		}
		
		public Category.Entry getMainCategory(){
			return isMain() ? category : upperCategory;
		}
		
		public String getMainCategoryValue() {
			return getMainCategory().value();
		}
		
		public boolean isMain(){
			return upperCategory == null;
		}		
		
		public boolean isSubsidiary(){
			return upperCategory != null;
		}
		
		public boolean isNull(){
			return category.isNull();
		}		
		
		public void removeWidget() {
			if (widget.isAttached()) widget.removeFromParent();
			if(categoryDiv.isAttached())
				categoryDiv.removeFromParent();
		}
		
		/**
		 * 해당 카테고리가 마우스 포인터 위치에 속하는지 여부를 반환한다.
		 * @param mousePointY
		 * @return
		 */
		public boolean isAtMousePointY(int mousePointY){
			int top = categoryDiv.getElement().getOffsetTop();
			int bottom = top + categoryDiv.getElement().getOffsetHeight();
			return (mousePointY >= top) && (mousePointY <= bottom);
		}
		
		@Override	
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			
			if((obj != null) && (obj instanceof CategoryUI)){
				CategoryUI c = (CategoryUI) obj;
				if (isMain() && c.isMain()) {
					return category.equals(c.category);
				}
				if (isSubsidiary() && c.isSubsidiary()) {
					return category.equals(c.category) && upperCategory.equals(c.upperCategory);
				}
			}
			
			return false;		
		}
		
		@Override
		public int hashCode() {
			if(isMain())	return category.hashCode();
			String code = upperCategory.toString() + "," + category.toString();
			return code.hashCode();		
		}
		
		public double getY(){
			return getY(0);		
		}
		
		/**
		 * Top 값을 반환한다.
		 * 분류되지 않는 카테고리가 하나일 경우 (메인과 서브 모두 null인 것이 하나인 경우)
		 * 화면 맨 아래로 보낸다.
		 * @param deltaPX
		 * @return top의 퍼센트 값.
		 */
		public double getY(int deltaPX){
			int y = thisPanel.getCategoryY(this) + deltaPX;
			return thisPanel.getYPct(y);
		}
		
		/**
		 * CategoryUI의 형태 및 위치 조정.
		 * 카테고리 순서는 외부에서 미리 정해둬야 한다.
		 */
		public void updateWidget(){
			widget.addStyleName(style.category());		
			widget.getElement().getStyle().setWidth(DEFAULT_CATEGORY_WIDTH, Unit.PX);
			widget.getElement().getStyle().setTop(getY(), Unit.PCT);
			widget.getElement().getStyle().setHeight(thisPanel.getYPct(DEFAULT_CATEGORY_HEIGHT), Unit.PCT);
			widget.getElement().getStyle().setBackgroundColor(category.color());	
		}	
		
		/**
		 *  CategoryUI가 차지하는 영역의 위치 조정.
		 */
		public void updateCategoryDeco(){
			String color = category.color();
			if (color == null) color = "transparent";
			if (categoryDiv == null) {
				categoryDiv = new SimplePanel();
				categoryDiv.setStyleName(isMain() ? style.main_category_div() : style.sub_category_div());		
			}
			double topPCT = getY(0);
			int height = isMain() ? getInterval() : DEFAULT_CATEGORY_HEIGHT + LINE_ADJUST;
			double heightPCT = thisPanel.getYPct(height);
			categoryDiv.getElement().getStyle().setTop(topPCT, Unit.PCT);
			categoryDiv.getElement().getStyle().setHeight(heightPCT, Unit.PCT);
			if (isMain()) {
				if (!isNull() && color.equals("transparent")) color = "#eee";
				categoryDiv.getElement().getStyle().setBorderColor(color);
			}
		}

		/**
		 * CategoryUI 표시 영역의 간격을 정해준다.
		 * @return
		 */
		private int getInterval(){
			int interval = 0;			
		
			if(isMain()){
				interval = (DEFAULT_CATEGORY_HEIGHT + LINE_ADJUST + SUB_CATEGORY_MARGIN) * (subOrder==0 ? 1 : subOrder) - SUB_CATEGORY_MARGIN;
			} else {
				interval = (DEFAULT_CATEGORY_HEIGHT + LINE_ADJUST);
			}
			
			return interval;
		}

		/**
		 * 카테고리 구분선 중에서 마우스 좌표에 해당하는 구분선들을 강조한다. 
		 * @param mousePointYs = 마우스 Y축 좌표들
		 */
		public void hilightCategoryDeco(int mousePointYs) {
			if((isMain() && isNull()) ||
					(isSubsidiary() && upperCategory.isNull() && 
					getSubOrder()==1 && isNull()))
				return;
			
			int start = categoryDiv.getElement().getOffsetTop();
			int end   = start + categoryDiv.getElement().getOffsetHeight();			 
			boolean mouseOver = mousePointYs > start && mousePointYs < end;
			
			if (isMain())
				categoryDiv.setStyleName(style.main_category_div_selected(), mouseOver);
			if (isSubsidiary() || (isMain() && getSubOrder()==0)) {
				if (mouseOver) {
					String color = category.color();
					if (color==null || color.equals("transparent")) 
						color = "lightgray";
					categoryDiv.getElement().getStyle().setBackgroundColor(color);
				} else
					categoryDiv.getElement().getStyle().clearBackgroundColor();
			}			
		}
		
		/**
		 * 기본적으로 카테고리 값을 알파벳 순서로 정렬한다.
		 * 다만, Null 값은 끝으로 보낸다.
		 */
		@Override
		public int compareTo(CategoryUI arg0) {
			if (category.isNull()) return 1;
			return category.value().compareTo(arg0.category.value());
		}

	}

	/**
	 * 이벤트 표시기
	 * 
	 * 카테고리 뷰에서는 플롯 공간에 배치한 인터페이스 요소만 표현하는데 추후 확장을 위해 스토리 요소 구분을 유지한다.<br>
	 * <code>DiscourseTimeline.EventUI</code>를 변형해서 쓰는데 담화 시간 편집은 하지 않는다.
	 * 
	 * 이벤트의 카테고리 판단은 이벤트와 별로로 외부 검사를 통해서 수행한다. (14/12/03 jehdeiah)
	 * 
	 * @author jehdeiah
	 * 
	 */	
	class EventUI implements EntityUIObject<EventEntity> {		
		static final int BorderWidth = 2;
		EventEntity event;
		Label widget = null;

		/**
		 * 플롯에 배열된 이벤트 표시와 해당 담화 시간 정보.
		 */
		Interval discourseInOut = null;		

		/** 메인 카테고리 값 */
		String mainValue = null;
		/** 서브 카테고리 값 */
		String subValue = null;
		
		public EventUI(EventEntity event) {
			this.event = event;
		}
		@Override
		public EventEntity getEntity() {
			return event;
		}
		public void setEntity(EventEntity event) {
			this.event = event;
		}
		@Override
		public long getId() {
			return event==null ? -1 : event.getId();
		}
		@Override
		public Widget getWidget() {
			return widget;
		}
		
		public String getMainCategoryValue() {
			return mainValue;
		}
		public String getSubCategoryValue() {
			return subValue;
		}
		public void setCategoryValues(String value1, String value2) {
			mainValue = value1;
			subValue = value2;
		}
		
		public Interval getDiscourseInOut() {
			return discourseInOut;
		}

		public int getWidgetLeft() {
			if (widget != null) {
				assert widget.isAttached();
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
		 * 이벤트 표시기를 만든다. 
		 * 설정된 이벤트 엔티티에 따라 <code>updateWidgetStyle()</code>을 불러 위젯 스타일도 맞춘다.
		 */
		public void create() {
			discourseInOut = new Interval();
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
					true/*plotInstance*/) {
				public void notifyDragStart(long eventId, int offsetX, int offsetY) {
					thisPanel.draggingEvent = thisEvent;
					thisPanel.dragOffsetX = offsetX;
					thisPanel.dragOffsetY = offsetY;
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
			
			// applying style...
			widget.setStyleName(thisPanel.style.event());
			updateWidget();
		}

		/**
		 * 이벤트 상자의 모양과 카테고리 배치를 바꾼다.
		 * 
		 * 세로 위치는 카테고리 위치가 되고 높이는 목록에서는 1 단위, 플롯 공간에서는 실제 시점 간격이 된다. (미할당 이벤트는
		 * 마지막 시점에서 1단위로 배치한다.)
		 * 
		 */
		@Override
		public void updateWidget() {
			if (event.isAssigned()) {
				CharacterEntity c = LocalCache.get(CharacterEntity.class, event.getMainCharacter());
				String color = c.getColor();
				widget.getElement().getStyle().setBackgroundColor(color);
			} else {
				widget.getElement().getStyle().clearBackgroundColor();
			}
			double top = thisPanel.getCategoryYPct(mainValue, subValue);
			widget.getElement().getStyle().setTop(top, Unit.PCT);
			widget.getElement().getStyle().setHeight(DEFAULT_CATEGORY_HEIGHT, Unit.PX);
			
			widget.setStyleName(thisPanel.style.event_plotted(), event.isPlotted());
		}
		
		/**
		 * 이벤트 상자의 담화 시간에 따른 위치와 크기를 바꾼다.
		 * 이벤트 상자는 반드시 화면에 이미 표시되어 있어야 한다. 
		 * 화면 픽셀 위치를 입력으로 받고 배율 처리가 쉽도록 퍼센트 위치로 변환하여 스타일을 지정한다.
		 * 
		 * @param left
		 * @param width
		 */
		public void updateWidgetPosition(int left, int right) {
			assert widget.isAttached();
			final int MAX_ADJUST_TRIAL = 5;
			int oldLeft = getWidgetLeft();
			int oldRight = getWidgetRight();
			int deco = widget.getOffsetWidth() - widget.getElement().getClientWidth();
			double leftPCT = thisPanel.getTimelineX(left);
			if (leftPCT < 0) leftPCT = 0;
			if (leftPCT > 100) leftPCT = 100;
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
					double rightPCT = thisPanel.getTimelineX(clientRight);
					widget.getElement().getStyle().setWidth(rightPCT-leftPCT, Unit.PCT);
					newRight = getWidgetRight();
					if (right < newRight) clientRight--;
					else if (right > newRight) clientRight++;
					else break;
				}
			}
		}

		/*
		 * 여기서는 modified 깃발을 안 쓴다.
		 */
		@Override
		public void modify() {}
		@Override
		public boolean isModified() { return false; }
		@Override
		public void invalidate() {}
		
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
	
	interface MyStyle extends CssResource {
		String event();
		String event_plotted();
		
		String timepoint_line();
		String timepoint_line_selected();
		
		String discourse_timepoint_line();
		String discourse_timepoint_line_selected();
		
		String plot_area(); 
		String time_axis_area();
		
		String category();

		String main_category_div();
		String sub_category_div();
		String main_category_div_selected();
	}
	
	private static CategoryViewUiBinder uiBinder = GWT.create(CategoryViewUiBinder.class);
			
	/**
	 * 로컬 캐시에서 불러온 EventEntity들에 대하여 속하는 카테고리마다 EventUI를 만들어 담는다.
	 */
	Map<Long, ArrayList<EventUI>> eventBag;
	Map<CategoryUI, ArrayList<EventUI>> categoryEventBag;
		
	/*
	 * 카테고리 관련 변수들
	 * 카테고리 값의 순서를 정하기 위해 SortedSet을 쓰고, 인덱스를 위해 맵을 같이 둔다.
	 */
	Category currentMainCategory;
	Category currentSubCategory;
	ArrayList<CategoryUI> categoryBag;
	Map<String, CategoryUI> mainCategoryBag;
	SortedSet<CategoryUI> mainCategorySet;
	Map<String, Map<String, CategoryUI>> subCategoryBag;
	Map<String, SortedSet<CategoryUI>> subCategorySet;
	
	
	/**
	 * 담화 시간 표시선을 모아두는 Bag.
	 */
	ArrayList<SimplePanel> discourseTimePointBag;

	
	//Layout 업데이트 플래그.
	boolean layouted = false;

	// 바꾼 캐릭터 목록
	Set<Long> changedCharacterIds;
	// 바꾼 이벤트 목록
	Set<Long> changedEventIds;
	/**
	 * 속성창 
	 */
	final int popupZIndex = 9;
	EventPropertyPanel eventProperty = null;
	
	// 세로축 화면 높이
	int basePanelHeight;
	
	// 드래그 이벤트 
	EventUI draggingEvent = null;
	int dragOffsetX = 0;
	int dragOffsetY = 0;
	
	// 편집기 해상도
	double desiredResolution = 1;
	double resolutionBase = 10000;
	
	//이벤트에 마우스 우 클릭 시에 나오는 메뉴	 
	private PopupPanel menuPanelOnEvent = null;
	// 카테고리 관련 메뉴 아이템
	private MenuBar submenuUnassign = null;
	private MenuItem itemUnassignMain = null;
	private MenuItem itemUnassignSub = null;
	private MenuItem itemUnassignBoth = null;
	//이벤트에 마우스 우 클릭 시에 일어나는 이벤트
	private EventUI contextEvent = null;	
	//private final int EventMargin = 3; // px	

	// 왼쪽 카테고리 목록과 타임라인 위치 맞추기
	private final int ALIGN_TOP = 2; // px
	/*
	 * GWT UI 요소
	 */
	@UiField
	MyStyle style;
		
	@UiField
	ListBox mainViewSelection;
	
	@UiField
	ListBox subViewSelection;
	
	@UiField
	HTMLPanel timelineEditor;
	
	@UiField
	LayoutPanel timelineEditorContainer;
	
	@UiField
	HTMLPanel sortedMainCategoryContainer;
	
	@UiField
	HTMLPanel mainCategoryPanel;
	
	@UiField
	HTMLPanel sortedSubCategoryContainer;
	
	@UiField
	HTMLPanel subCategoryPanel;
	
	@UiField
	HTMLPanel storyTimeAxis;
	
	@UiField
	SimplePanel editorScroll;
	
	@UiField
	HTML scrollWidth;	
	
	@UiHandler("mainViewSelection")
	public void onMainViewChange(ChangeEvent event) {
		String newCategory = mainViewSelection.getItemText(mainViewSelection.getSelectedIndex());
		if (newCategory.equals(currentMainCategory.getCategory())) return;
		currentMainCategory = Category.get(newCategory);
		updateSubCategoryList(false);
		//0 for None
		//subViewSelection.setItemSelected(0, true);
		//subCategory = subViewSelection.getItemText(0);
		sortEventsByCurrentCategory(true);
	}	
	
	@UiHandler("subViewSelection")
	public void onSubViewChange(ChangeEvent event) {
		String newCategory = subViewSelection.getItemText(subViewSelection.getSelectedIndex());
		if (currentSubCategory==null && newCategory.equals(Category.CATEGORY_NONE)) return;
		if (currentSubCategory!=null && newCategory.equals(currentSubCategory.getCategory())) return;
		currentSubCategory = Category.get(newCategory); // CATEGORY_NONE -> null, hopefully
		sortEventsByCurrentCategory(false);
	}	
	
	public CategoryView(StoryServiceAsync service, EventBus bus) {
		super(service, bus);
		initWidget(uiBinder.createAndBindUi(this));

		eventBag = new HashMap<Long, ArrayList<EventUI>>();
		categoryEventBag = new HashMap<CategoryUI, ArrayList<EventUI>>();
		discourseTimePointBag = new ArrayList<SimplePanel>();
		categoryBag = new ArrayList<CategoryUI>();
		mainCategoryBag = new HashMap<String, CategoryUI>();
		mainCategorySet = new TreeSet<CategoryUI>();
		subCategoryBag = new HashMap<String, Map<String, CategoryUI>>();
		subCategorySet = new HashMap<String, SortedSet<CategoryUI>>();
		changedCharacterIds = new HashSet<Long>();
		changedEventIds = new HashSet<Long>();
	}
	
	private void updateMainCategoryList(boolean preserve){
		int selected = -1;
		int i=0;
		mainViewSelection.clear();
		for (String itemName : Category.getCategories()) {
			mainViewSelection.addItem(itemName);
			if (currentMainCategory!=null && itemName.equals(currentMainCategory.getCategory())) 
				selected = i;
			i++;
		}
		if (selected!=-1 && preserve) mainViewSelection.setItemSelected(selected, true);
		else {
			currentMainCategory = Category.get(mainViewSelection.getItemText(0));
		}
	}
	
	private void updateSubCategoryList(boolean preserve){
		int selected = -1;
		int i=1;
		subViewSelection.clear();		
				//카테고리 선택 드랍 다운 메뉴에 아이템 추가하기		
		subViewSelection.addItem(Category.CATEGORY_NONE);
		for (String itemName : Category.getCategories()){
			if(itemName.equals(currentMainCategory.getCategory()))
				continue;			
			subViewSelection.addItem(itemName);
			if (currentSubCategory!=null && itemName.equals(currentSubCategory.getCategory())) 
				selected = i;
			i++;
		}
		if (selected!=-1 && preserve) subViewSelection.setItemSelected(selected, true);
		else {
			currentSubCategory = Category.get(subViewSelection.getItemText(0));
		}
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		
		updateMainCategoryList(false);
		updateSubCategoryList(false);
		
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
		timelineEditor.getElement().getParentElement().addClassName(style.plot_area());
		
		// 화면 스크롤에서 제외된 시간축을 세로 스크롤에 맞게 움직인다.
		sortedMainCategoryContainer.addDomHandler(new ScrollHandler() {
			public void onScroll(ScrollEvent event) {
				int scrollY = sortedMainCategoryContainer.getElement().getScrollTop();
				storyTimeAxis.getElement().getStyle().setTop(-scrollY+ALIGN_TOP, Unit.PX);
				timelineEditor.getElement().getStyle()
						.setTop(-scrollY+ALIGN_TOP, Unit.PX);
			}
		}, ScrollEvent.getType());		

		sortedSubCategoryContainer.addDomHandler(new ScrollHandler() {
			public void onScroll(ScrollEvent event) {
				int scrollY = sortedSubCategoryContainer.getElement().getScrollTop();
				storyTimeAxis.getElement().getStyle().setTop(-scrollY+ALIGN_TOP, Unit.PX);
				timelineEditor.getElement().getStyle()
						.setTop(-scrollY+ALIGN_TOP, Unit.PX);
			}
		}, ScrollEvent.getType());
		
		//드래그 이벤트를 받기 위해 핸들러를 추가한다.
		timelineEditor.addDomHandler(new DragEnterHandler() {
			public void onDragEnter(DragEnterEvent event) {
				event.preventDefault();
			}
		}, DragEnterEvent.getType());
		
		//마우스가 이벤트 경계선에 닿는 경우 경계선을 하이라이트 처리한다. 
		timelineEditor.addDomHandler(new DragOverHandler() {
			
			public void onDragOver(DragOverEvent event) {
				event.preventDefault();				
				int eventWidth = draggingEvent.getWidget().getElement().getOffsetWidth();				
				int relativeLeft = event.getNativeEvent().getClientX()
						- timelineEditor.getAbsoluteLeft() - dragOffsetX;			
				int timePoint = /*timelineEditor.getElement().getScrollLeft() +*/ relativeLeft;		
				
				hilightDiscourseTimePoint(timePoint, timePoint + eventWidth);
				
				int mousePointY = event.getNativeEvent().getClientY();
				int centerPointY = getCenterPointOfDraggedEventUI(mousePointY);
				hilightCategoryDeco(centerPointY);				
			}
			
		}, DragOverEvent.getType());		
		
		/*
		 * 이벤트를 타임라인에 놓기 위한 핸들러
		 */		
		timelineEditor.addDomHandler(new DropHandler() {
			public void onDrop(DropEvent event) {
				int mousePointY = event.getNativeEvent().getClientY();
				int centerPointY = getCenterPointOfDraggedEventUI(mousePointY);
				
				CategoryUI[] belongsTo = getCategoryUIsAtPointY(centerPointY);
				
				if ( (Boolean.parseBoolean(event.getData("plot"))) && (belongsTo != null) )
					movePlottedEvent(draggingEvent, belongsTo[0], belongsTo[1]);					

				draggingEvent = null;
				dragOffsetX = 0;
				dragOffsetY = 0;
			}
		}, DropEvent.getType());

		/*
		 * 이벤트 크기 조절을 위한 마우스 핸들러 
		 */
		timelineEditor.addDomHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent e) {
				timelineEditor.getElement().getStyle().setCursor(Cursor.AUTO);
				hilightCategoryDeco(e.getY());
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
			}
		}, MouseUpEvent.getType());
		
		/*
		 * 컨텍스트 메뉴(이벤트에 마우스 우 클릭시에 나타나는 메뉴)
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
		itemUnassignMain = new MenuItem("Unassign Main", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnEvent.hide();
				unassignCategoryFromEventEntity(contextEvent.getEntity(), currentMainCategory);
			}
		});
		itemUnassignSub = new MenuItem("Unassign Sub", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnEvent.hide();
				unassignCategoryFromEventEntity(contextEvent.getEntity(), currentSubCategory);
			}
		});
		itemUnassignBoth = new MenuItem("Unassign All", new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPanelOnEvent.hide();
				unassignCategoryFromEventEntity(contextEvent.getEntity());
			}
		});
		menu.addItem(itemUnassignMain);
		submenuUnassign = new MenuBar(true);
		submenuUnassign.addItem(itemUnassignSub);
		submenuUnassign.addItem(itemUnassignBoth);
		
		menu.addItem("Close", new Scheduler.ScheduledCommand() {			
			@Override
			public void execute() {
				menuPanelOnEvent.hide();				
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
		for (long id : changedCharacterIds) {
			CharacterEntity c = LocalCache.get(CharacterEntity.class, id);
			if (c != null) saveEntity(c);
		}
		changedCharacterIds.clear();
		for (long id : changedEventIds) {
			EventEntity e = LocalCache.get(EventEntity.class, id);
			if (e != null) saveEntity(e);
		}
		changedEventIds.clear();
		//카테고리 뷰에서도 담화 시간 노트가 가능하다.
		timepointChanged = true;
	}
		
	@Override
	public void invalidate() {}
	
	/**
	 * 스토리 화면 요소들을 모두 지운다.
	 */
	@Override
	public void clear() {
		// 애노테이션
		// 시점 보조선
		for (SimplePanel p : discourseTimePointBag) {
			if (p.isAttached()) p.removeFromParent();
		}
		discourseTimePointBag.clear();
		// 이벤트
		for (ArrayList<EventUI> l : eventBag.values()) {
			for (EventUI e : l) {
				Widget w = e.getWidget(); 
				if (w.isAttached()) w.removeFromParent();
			}
		}
		eventBag.clear();
		categoryEventBag.clear();
		// 카테고리
		for (CategoryUI c : categoryBag) {
			c.removeWidget();
		}
		categoryBag.clear();
		mainCategoryBag.clear();
		mainCategorySet.clear();
		subCategoryBag.clear();
		subCategorySet.clear();
	}

	/*----------------------------------------------------------------
	 * 이벤트들을 로컬 캐시와 동기화하고 카테고리 뷰를 갱신한다.
	 * (이벤트 속성값이 바뀌어서 카테고리가 변할 수 있다.)
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
			
		// 카테고리가 바꼈을 수도 있다.
		updateMainCategoryList(true);
		updateSubCategoryList(true);
		
		// 이벤트가 갱신된 것을 반영하여 카테고리 정보를 갱신한다.
		for (CategoryUI cUI : categoryBag) {
			cUI.clearCount();
		}
		EventEntity[] eventSet = LocalCache.entities(EventEntity.class, eventArrayType); 
		for (EventEntity event : eventSet) {
			if (!event.isPlotted()) continue;
			updateCategoryValues(event);
		}
		/*
		 *  사라진 카테고리 값 지우기
		 */
		for (CategoryUI cUI : categoryBag) {
			if (cUI.count() == 0) {
				removeCategory(cUI, false);
			}
		}
		// 카테고리 값에 따라 패널 높이 지정
		int mainCount = mainCategorySet.size();
		int totalCount = categoryBag.size();
		int subCount = totalCount - mainCount;
		if (subCount == 0) subCount = mainCount;
		if (totalCount > 0) {
			basePanelHeight = getCategoryY(mainCount, subCount) + 
					DEFAULT_CATEGORY_HEIGHT + LINE_ADJUST;
		}
		int panelHeight = sortedMainCategoryContainer.getElement().getClientHeight();
		if (basePanelHeight < panelHeight) {
			basePanelHeight = panelHeight;
		}
		setZoomY(1.0);		

		updateCategory();		
		updateEventUI();		
		
		updateCategoryDeco();
		updateDiscourseTimePoints();
	}
	
	/**
	 * 카테고리 UI를 삭제하면서 거기에 속한 이벤트 UI도 같이 지운다.
	 * @param cUI
	 * @param removeEvent 이벤트도 지울 것인지 결정한다.
	 */
	private void removeCategory(CategoryUI cUI, boolean removeEvent) {
		categoryBag.remove(cUI);				
		cUI.removeWidget();
		if (cUI.isMain()) {
			String key = cUI.getCategoryValue();
			mainCategorySet.remove(cUI);
			mainCategoryBag.remove(key);
			Set<CategoryUI> sub = subCategorySet.remove(key);
			if (sub != null) {
				for (CategoryUI subUI : sub) { 
					subUI.removeWidget();
					categoryBag.remove(subUI);
					if (removeEvent) removeCategoryEvents(subUI);
				}
				subCategorySet.remove(key);
				subCategoryBag.remove(key);
			} else {
				if (removeEvent) removeCategoryEvents(cUI);
			}
		} else {
			String key = cUI.getMainCategoryValue();
			Map<String, CategoryUI> bag = subCategoryBag.get(key);
			if (bag != null) bag.remove(cUI.getCategoryValue());
			Set<CategoryUI> sub = subCategorySet.get(key);
			if (sub != null) {
				sub.remove(cUI);
				if (removeEvent) removeCategoryEvents(cUI);
			}
		}		
	}
	
	/**
	 * 카테고리에 속한 이벤트들을 화면에서 지운다.
	 * 
	 * @param cUI
	 */
	private void removeCategoryEvents(CategoryUI cUI) {
		ArrayList<EventUI> eventSet = categoryEventBag.remove(cUI);
		if (eventSet != null) {
			for (EventUI e : eventSet) {
				e.getWidget().removeFromParent();
				ArrayList<EventUI> bag = eventBag.get(e.getId());
				for (int i=0; i<bag.size(); ) {
					EventUI ebag = bag.get(i);
					if (!ebag.getWidget().isAttached()) 
						bag.remove(ebag);
					else i++;
				}
			}
		}		
	}
	/**
	 * 이벤트 상자를 최신 로컬 캐시와 카테고리로 갱신한다.
	 * 이벤트 상자의 세로 위치는 카테고리에서 얻어오므로 
	 * 카테고리를 모두 수집하고 UI까지 갱신한 후에 EvenUI들을 그려준다.
	 */
	private void updateEventUI(){
		/*
		 *  기존 이벤트를 모두 지운다.
		 *  FIXME: 보다 효율적인 방법이 있을지도...
		 */
		for (ArrayList<EventUI> list : eventBag.values()) {
			for (EventUI e : list) {
				e.getWidget().removeFromParent();
			}
		}
		eventBag.clear();
		categoryEventBag.clear();
		for (EventEntity event : LocalCache.entities(EventEntity.class, eventArrayType)) {
			if (event.isPlotted()) {
				sortEventFromEntity(event);
			}
		}
	}
	
	/**
	 * 새로 수집된 카테고리 정보에 맞춰 화면 요소들을 갱신한다.
	 * 특히 카테고리 내에서 순서를 지정하여 세로 위치를 잡도록 한다.
	 */
	private void updateCategory() {
		int i=0; // 주 카테고리 인덱스
		int j=0; // 세로축 인덱스 (전체 부 카테고리 개수. 부 카테고리가 없을 땐 주 카테고리 수)
		boolean noSubCategory = currentSubCategory==null;
		for (CategoryUI cUI : mainCategorySet) {
			i++;
			cUI.setMainOrder(i);
			if (noSubCategory) {
				cUI.setSubOrder(0);
				cUI.setTotalOrder(i);
			} else {
				int k=0; // 주 카테고리 내에서 부 카테고리 인덱스
				cUI.setTotalOrder(j+1);
				for (CategoryUI sUI : subCategorySet.get(cUI.getCategoryValue())) {
					k++; j++;
					sUI.setMainOrder(i);
					sUI.setSubOrder(k);
					sUI.setTotalOrder(j);
					if (!sUI.getWidget().isAttached())
						subCategoryPanel.add(sUI.getWidget());
					sUI.updateWidget();
				}
				cUI.setSubOrder(k);
			}
			if (!cUI.getWidget().isAttached())
				mainCategoryPanel.add(cUI.getWidget());
			cUI.updateWidget();
		}		
	}
	
	/**
	 * 이벤트 엔티티로부터 카테고리 정보를 얻어온다.
	 * @param event
	 */
	private void updateCategoryValues(EventEntity event) {
		if (currentMainCategory == null) return;
		Category.Entry[] main = currentMainCategory.categorize(event);
		for (Category.Entry entry : main) {
			CategoryUI mUI = mainCategoryBag.get(entry.value());
			if (mUI == null) mUI = addNewMainCategory(entry);
			mUI.add();
			if (currentSubCategory != null) updateSubCategoryValues(event, entry);
		}
	}
	
	private void updateSubCategoryValues(EventEntity event, Category.Entry main) {
		Map<String, CategoryUI> subBag = subCategoryBag.get(main.value());
		if (subBag == null) {
			// 서브 카테고리가 없다가 만들어지면 subBag이 안 만들어진 상태일 수 있다.
			subBag = new HashMap<String, CategoryUI>();
			subCategoryBag.put(main.value(), subBag);
			subCategorySet.put(main.value(), new TreeSet<CategoryUI>());
		}
		Category.Entry[] sub = currentSubCategory.categorize(event);
		for (Category.Entry subEntry : sub) {
			CategoryUI sUI = subBag.get(subEntry.value());
			if (sUI == null) sUI = addNewSubCategory(main, subEntry);
			sUI.add();
		}
	}

	private CategoryUI addNewMainCategory(Category.Entry entry) {
		String value = entry.value();
		CategoryUI mUI = mainCategoryBag.get(value);
		if (mUI == null) {
			mUI = new CategoryUI(entry);
			categoryBag.add(mUI);
			mainCategoryBag.put(value, mUI);
			mainCategorySet.add(mUI);
			if (currentSubCategory != null) {
				subCategoryBag.put(value, new HashMap<String, CategoryUI>());
				subCategorySet.put(value, new TreeSet<CategoryUI>());
			}
		}
		return mUI;
	}

	private CategoryUI addNewSubCategory(Category.Entry main, Category.Entry sub) {
		String mainValue = main.value();
		if (!mainCategoryBag.containsKey(mainValue)) 
			addNewMainCategory(main);
		if (currentSubCategory != null) {
			String subValue = sub.value();
			Map<String, CategoryUI> bag = subCategoryBag.get(mainValue);
			CategoryUI sUI = bag.get(subValue);
			if (sUI == null) {
				sUI = new CategoryUI(main, sub);
				categoryBag.add(sUI);
				bag.put(subValue, sUI);
				subCategorySet.get(mainValue).add(sUI);				
			}
			return sUI;
		}
		return null;
	}

	/**
	 * 데이터스토어와 같이 외부에서 가져온 이벤트 엔티티에 대응하는 플롯 이벤트 상자들을 만든다.<br>
	 * 그리고 플롯된 사건들을 카테고리 뷰에 추가한다.
	 * @param event
	 */
	void sortEventFromEntity(EventEntity event) {
		if (event.getOccurrence() == 0) return;
		if (currentMainCategory == null) return;
		Collection<Interval> discourseInOut = event.getDiscourseInOut();
		Category.Entry[] main = currentMainCategory.categorize(event);
		for (Interval r : discourseInOut) {
			if (currentSubCategory != null) {
				Category.Entry[] sub = currentSubCategory.categorize(event);
				for (Category.Entry mainEntry : main) {
					for (Category.Entry subEntry : sub) {
						sortEvent(event, r, mainEntry, subEntry);
					}
				}
			} else {
				for (Category.Entry mainEntry : main) {
					sortEvent(event, r, mainEntry, null);
				}
			}
		}
	}
	
	private void sortEvent(EventEntity event, Interval r, Category.Entry main, Category.Entry sub) {
		// subUI 기준으로 카테고리가 나뉜다.
		String mainValue = main.value();
		String subValue = sub==null ? null : sub.value();
		CategoryUI mainUI = mainCategoryBag.get(mainValue);
		CategoryUI subUI = sub==null ? mainUI : subCategoryBag.get(mainValue).get(subValue);
		EventUI eUI = new EventUI(event);
		eUI.setCategoryValues(mainValue, subValue);
		// Create 함수 안에 updateWidget 함수가 호출된다.				
		eUI.create();
		timelineEditor.add(eUI.getWidget());
		int left = getTimelineOffsetX(r.getBegin());
		int right = getTimelineOffsetX(r.getEnd());
		eUI.updateWidgetPosition(left, right);
		eUI.setDiscourseInOut(r.getBegin(), r.getEnd());
		if (!eventBag.containsKey(event.getId())) {
			eventBag.put(event.getId(), new ArrayList<EventUI>());
		}
		eventBag.get(event.getId()).add(eUI);
		if (!categoryEventBag.containsKey(subUI)) {
			categoryEventBag.put(subUI, new ArrayList<EventUI>());
		}
		categoryEventBag.get(subUI).add(eUI);
	}

	/**
	 * 카테고리 뷰의 속성이 바뀔 경우 화면을 다시 구성한다.
	 */
	private void sortEventsByCurrentCategory(boolean isMain){		
		if (isMain) {
			for (CategoryUI cUI : categoryBag) {
				cUI.removeWidget();
			}
			categoryBag.clear();
			mainCategoryBag.clear();
			mainCategorySet.clear();
		}
		for (int i=0; i<categoryBag.size(); ) {
			CategoryUI cUI = categoryBag.get(i);
			if (cUI.isSubsidiary()) {
				cUI.removeWidget();
				categoryBag.remove(cUI);
			} else i++;
		}
		subCategoryBag.clear();
		subCategorySet.clear();
		updateAll();
	}
	
	/**
	 * Event의 PlotInstance들을 옮기고자 하는 카테고리로 옮긴다.
	 * @param eventUI
	 * @param mainCategoryUI
	 * @param subCategoryUI
	 */
	void movePlottedEvent(EventUI eventUI, CategoryUI mainCategoryUI, CategoryUI subCategoryUI) {
		if (eventUI == null) return; 
				//서브 카테고리의 속성이 None이 아닌 상태에서는 eventUI를 맨 밑으로 드래그할 수 없도록한다.
		// WHY???
//				((subCategoryUI.isNull()) && (!getCurrentSubCategory().equals(Category.CATEGORY_NONE))) )
//			return;
		Category.Entry mainEntry = (mainCategoryUI==null) ? null : mainCategoryUI.getCategory();
		Category.Entry subEntry = (subCategoryUI==null) ? null : subCategoryUI.getCategory();
		modifyCategoryOfEvent(eventUI.getEntity(), mainEntry, subEntry);	
		updatePlotInstanceOfEvent(eventUI.getEntity());		
		updateDiscourseTimePoints();
	}
	
	
	/*---------------------------------------------------------------
	 * Context Menu 관련 메소드
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
	
	/**
	 * EventUI의 Edit Properties를 통해서 이벤트의 속성을 변경할 경우 변경 사항을 UI에 적용한다.
	 * 이벤트 상자가 해당 카테고리마다 생기므로 소속 카테고리 수가 달라지면 이벤트 상자 개수도 달라진다.
	 */
	@Override
	public void updateEventProperties(EventEntity event) {
		changedEventIds.add(event.getId());
		updatePlotInstanceOfEvent(event);
	}
	
	/**
	 * EventEntity의 카테고리 속성을 바꾼다.
	 * @param event
	 * @param categories 바꿀 카테고리 엔티티 (종류, 값)
	 */
	private void modifyCategoryOfEvent(EventEntity event, Category.Entry... categories){
		for (Category.Entry c : categories) {
			if (c!=null && c.setValue(event)) changedEventIds.add(event.getId());
		}
	}
	
	/**
	 * 어떤 event가 수정되었을 경우에, 수정된 카테고리에 맞춰서 같은 이벤트들을 업데이트 해준다.
	 * @param event 이벤트 요소. 이미 모든 값이 갱신된 상태이다.
	 */
	private void updatePlotInstanceOfEvent(EventEntity event){
		ArrayList<EventUI> eUIs = eventBag.get(event.getId());
		int oldSize = eUIs.size();
		int count = 0;
		boolean addNew = false;
		Category.Entry[] main = currentMainCategory.categorize(event);
		for (Category.Entry mainEntry : main) {
			String mainValue = mainEntry.value();
			CategoryUI mUI = mainCategoryBag.get(mainValue);
			if (mUI == null) {
				mUI = new CategoryUI(mainEntry);
				mainCategoryBag.put(mainValue, mUI);
				mainCategorySet.add(mUI);
				categoryBag.add(mUI);
				mUI.add();
				addNew = true;
			}
			if (currentSubCategory == null) {
				EventUI eUI = null;
				if (count < oldSize) {
					eUI = eUIs.get(count);
					eUI.setCategoryValues(mainValue, null);
					count++;
				} else {
					eUI = new EventUI(event);
					eUI.setCategoryValues(mainValue, null);
					eUI.create();
				}
				eUI.updateWidget();
			} else {
				Category.Entry[] sub = currentSubCategory.categorize(event);
				Map<String, CategoryUI> subBag = subCategoryBag.get(mainValue);
				SortedSet<CategoryUI> subSet = subCategorySet.get(mainValue);
				for (Category.Entry subEntry : sub) {
					String subValue = subEntry.value();
					CategoryUI sUI = subBag.get(subValue);
					if (sUI == null) {
						sUI = new CategoryUI(mainEntry, subEntry);
						subBag.put(subValue, sUI);
						subSet.add(sUI);
						categoryBag.add(sUI);
					}
					EventUI eUI = null;
					if (count < oldSize) {
						eUI = eUIs.get(count);
						eUI.setCategoryValues(mainValue, subValue);
						count++;
					} else {
						eUI = new EventUI(event);
						eUI.setCategoryValues(mainValue, subValue);
						eUI.create();
					}
					eUI.updateWidget();
				}
			}
		}
		for (int i=count; i<oldSize; i++) {
			EventUI eUI = eUIs.get(count);
			eUI.getWidget().removeFromParent();
			CategoryUI cUI = null;
			if (currentSubCategory == null) {
				cUI = mainCategoryBag.get(eUI.mainValue);
			} else {
				cUI = subCategoryBag.get(eUI.mainValue).get(eUI.subValue);
			}
			ArrayList<EventUI> list = categoryEventBag.get(cUI);
			list.remove(eUI);
		}
		if (addNew) { // 카테고리를 새로 추가했을 경우 화면 요소가 추가된다.
			updateCategory();
			updateEventUI();
			updateCategoryDeco();
		} 
	}
	
	/*---------------------------------------------------------------
	 * 카테고리 위치 반환 메쏘드.
	 */

	int getCategoryY(CategoryUI category) {
		boolean bottom = false;
		Category.Entry main = category.getMainCategory();
		if (main.isNull()) {
			if (currentSubCategory == null) bottom = true;
			else {
				Map<String, CategoryUI> map = subCategoryBag.get(main.value());
				if (map.size()==1 && map.containsKey(currentSubCategory.nullValue().value()))
					bottom = true;
			}
		}
		if (bottom) {
			return basePanelHeight - DEFAULT_CATEGORY_HEIGHT - LINE_ADJUST*2;
		} else {
			return getCategoryY(category.getMainOrder(), category.getTotalOrder());
		}
	}
	/**
	 * 카테고리의 세로 위치를 구한다.
	 * 메인 카테고리 위로 여백이 있고, 서브 카테고리는 아래로 여백이 있다.
	 * 
	 * @param mainOrder
	 * @param totalOrder
	 * @return
	 */
	int getCategoryY(int mainOrder, int totalOrder) {
		return MAIN_CATEGORY_MARGIN*mainOrder + 
				SUB_CATEGORY_MARGIN*(totalOrder-mainOrder) + 
				(DEFAULT_CATEGORY_HEIGHT + LINE_ADJUST)*(totalOrder-1);	
	}
	double getCategoryYPct(int mainOrder, int totalOrder) {
		return getYPct(getCategoryY(mainOrder, totalOrder));
	}
	
	double getYPct(int y) {
		return (double)y / basePanelHeight * 100;
	}
	int getCategoryY(String mainValue, String subValue) {
		int top = 0;
		if (currentSubCategory == null) {
			CategoryUI mainUI = mainCategoryBag.get(mainValue);
			if (mainUI == null) return 0;
			top = mainUI.getWidget().getElement().getOffsetTop();
		} else {
			CategoryUI subUI = subCategoryBag.get(mainValue).get(subValue);
			top = subUI.getWidget().getElement().getOffsetTop();
		}
		return top;
	}
	double getCategoryYPct(String mainValue, String subValue) {
		return getYPct(getCategoryY(mainValue, subValue));
	}

	/*---------------------------------------------------------------
	 * 구분선 강조 관련 메소드. 
	 */	
	private void hilightDiscourseTimePoint(int... timePoints) {
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
	
	/**
	 * 입력받은 mousePointY를 포함하는 카테고리 구분선을 찾아 강조표시를 해준다.
	 * @param mousePointY
	 */
	private void hilightCategoryDeco(int mousePointY){		
		for (CategoryUI cUI : categoryBag) {
			cUI.hilightCategoryDeco(mousePointY);
		}
	}
	
	/**
	 * 현재 드래그 되고 있는 EventUI의 중점 위치를 반환한다.
	 * @param mousePointY
	 * @return top value of EventUI's center point.
	 */
	private int getCenterPointOfDraggedEventUI(int mousePointY){
		int eventHeight = draggingEvent.getWidget().getElement().getOffsetHeight();
		int relativeTop = mousePointY - timelineEditor.getAbsoluteTop() - dragOffsetY;
		return timelineEditor.getElement().getScrollTop() + relativeTop + eventHeight/2;
	}

	/**
	 * 어떤 좌표가 어떤 카테고리 영역에 포함하는지 체크하고 해당 카테고리를 반환한다.
	 * @param mousePointY
	 * @return 해당 영역을 포함하는 카테고리 객체
	 */
	private CategoryUI[] getCategoryUIsAtPointY(int mousePointY){
		CategoryUI[] categories = new CategoryUI[2];	
		categories[0] = getMainCategoryAtPointY(mousePointY);		

		if(categories[0] == null)
			return null;
		
		categories[1] = getSubCategoryAtPointY(categories[0].getCategoryValue(), mousePointY);
		
		return categories;
	}
	
	/**
	 * 해당 마우스 좌표에 해당하는 MainCategoryUI를 반환한다.
	 * @param mousePointY
	 * @return 없을 경우 null을 반환.
	 */
	private CategoryUI getMainCategoryAtPointY(int mousePointY){
		for (CategoryUI mainCategoryUI : mainCategorySet)
			if(mainCategoryUI.isAtMousePointY(mousePointY))
				return mainCategoryUI;
			
		return null;
	}
	
	/**
	 * 해당 마우스 좌표에 해당하는 SubCategoryUI를 반환한다.
	 * @param mousePointY
	 * @return 없을 경우 null을 반환.
	 */
	private CategoryUI getSubCategoryAtPointY(String mainCategoryValue, int mousePointY){
		if (currentSubCategory == null) return null;
		Set<CategoryUI> subSet = subCategorySet.get(mainCategoryValue);
		if (subSet == null) return null;
		for (CategoryUI subCategoryUI : subSet)
				if(subCategoryUI.isAtMousePointY(mousePointY))
					return subCategoryUI;
		
		return null;
	}	
	
	/**
	 * 카테고리 구분선을 목록 변환에 맞춰 업데이트 시킨다.
	 */
	private void updateCategoryDeco(){
		for (CategoryUI mainCategoryUI : mainCategorySet) {
			if(mainCategoryUI.isNull() && mainCategoryUI.getSubOrder()==0)
				continue;
			
			//Update MainCategory
			mainCategoryUI.updateCategoryDeco();
			
			SimplePanel categoryDeco = mainCategoryUI.getCategoryDeco();
			if(!categoryDeco.isAttached())
				timelineEditor.add(categoryDeco);			
			
			//Update SubCategory
			if (currentSubCategory != null) {
				for (CategoryUI subCategoryUI : subCategorySet.get(mainCategoryUI.getCategory().value())) {
					subCategoryUI.updateCategoryDeco();
					SimplePanel subCategoryDeco = subCategoryUI.getCategoryDeco();
					if(!subCategoryDeco.isAttached())
						timelineEditor.add(subCategoryDeco);
				}
			}
		}
	}
	
	void updateDiscourseTimePoints() {
		SortedSet<Integer> tps = new TreeSet<Integer>();
		for (ArrayList<EventUI> list : eventBag.values()) {
			for (EventUI e: list) {
				tps.add(e.getWidgetLeft());
				tps.add(e.getWidgetRight());
			
			}
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

	void setZoomY(double zoomFactor) {
		double height = basePanelHeight * zoomFactor;
		mainCategoryPanel.getElement().getStyle().setHeight(height, Unit.PX);
		subCategoryPanel.getElement().getStyle().setHeight(height, Unit.PX);
		storyTimeAxis.getElement().getStyle().setHeight(height, Unit.PX);
		timelineEditor.getElement().getStyle().setHeight(height, Unit.PX);
		int top = timelineEditor.getElement().getOffsetTop() - ALIGN_TOP;
		if (top < 0) top = 0;
		double newTop = top * zoomFactor + ALIGN_TOP;
		timelineEditor.getElement().getStyle().setTop(newTop, Unit.PX);
	}

	void setZoomX(double zoomFactor) {
		String width = Double.toString(zoomFactor*100) + "%";
		timelineEditor.getElement().getParentElement().getStyle().setWidth(zoomFactor*100,  Unit.PCT);
		scrollWidth.setWidth(width);
	}
	
//	//윗단의 빈칸에서 삭제가 되면, 이벤트를 카테고리에서 Unassign하는 것으로 해야할 것 같다.
//	public void deleteEvent(EventUI event) {
//		EventEntity entity = event.getEntity();
//		if (event.isPlotInstance()) {
//			/* 플롯 요소 삭제 처리 순서
//			 * 1) 동일 이벤트 플롯 집합
//			 * 2) 전체 담화 순서 집합
//			 * 3) 화면에서 제거
//			 * 4) 삭제한 담화 구간을 엔티티에서 제거
//			 * 5) 스토리 이벤트 속성 갱신
//			 * 6) 변경사항 저장 요청
//			 */
//			SortedSet<EventUI> set = plotInstanceSetOfEvent.get(entity.getId());			
//			set.remove(event);
//			
//			// Plot이 다 지워지는 이벤트의 경우는 어떤 이벤트에 할당된 카테고리가 해제되었음을 뜻한다
//			if(set.isEmpty()){				
//				handleUnassignedEvent(entity);
//			}
//			
//			discourseEventBag.remove(event);
//			event.getWidget().removeFromParent();
//			entity.removeFromDiscourse(event.getDiscourseInOut());
//			EventUI storyEvent = eventBag.get(entity.getId());
//			storyEvent.updateWidget();
//			storyEvent.modify();			
//			updateDiscourseTimePoints();
//			saveChanges();
//		} else {
//			/* 스토리 요소 삭제 처리 순서
//			 * 1) 플롯 요소 모두 삭제
//			 * 2) 담화 순서 삭제
//			 * 3) 화면에서 제거
//			 * 4) 스토리 이벤트 삭제 요청
//			 */
//			SortedSet<EventUI> set = plotInstanceSetOfEvent.get(entity.getId());
//			if (set!=null && !set.isEmpty()) {
//				for (EventUI eUI : set) {
//					discourseEventBag.remove(eUI);
//					eUI.getWidget().removeFromParent();
//				}
//				updateDiscourseTimePoints();
//			}			
//			event.getWidget().removeFromParent();
//			deleteEntity(entity);
//		}
//	}
//	
//	/**
//	 * 카테고리 뷰에서 event의 plotInstance가 모두 사라지는 경우
//	 * 카테고리 할당이 해제되는 것과 같기 때문에 해당 eventEntity를 처리해준다.
//	 * @param unassignedE
//	 */
//	private void handleUnassignedEvent(EventEntity unassignedE){
//		EventUI unassignedEvent = unassignCategoryFromEventEntity(unassignedE, getSelectedCategoryOfEventEntity(unassignedE));
//		eventUIsInNewEventPool.add(unassignedEvent);
//		
//		//newEventPool에 위젯이 이상하게 배치되는 경우가 있는데
//		//보통은 absolute로 속성이 설정되어서 그런 것이므로,
//		//CSS처리에 만전을 기한다.
//		newEventPool.add(unassignedEvent.getWidget());
//	}
	
	/**
	 * event에 할당된 카테고리를 해제시킨다. (Null 값 입력)
	 * @param event
	 */
	private void unassignCategoryFromEventEntity(EventEntity event) {
		if(event != null){
			changedEventIds.add(event.getId());
			currentMainCategory.nullValue().setValue(event);
			if (currentSubCategory != null) {
				currentSubCategory.nullValue().setValue(event);
			}
			updateEventProperties(event);
		}		
	}

	private void unassignCategoryFromEventEntity(EventEntity event, Category category) {
		if(event != null){
			changedEventIds.add(event.getId());
			category.nullValue().setValue(event);
			updateEventProperties(event);
		}		
	}

}

package com.googlecode._4s_web.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.client.entity.LocalEntity;
import com.googlecode._4s_web.client.entity.StoryEntity;
import com.googlecode._4s_web.client.entity.StoryTimePoint;
import com.googlecode._4s_web.client.ui.AbstractStoryPanel;
import com.googlecode._4s_web.client.ui.CategoryView;
import com.googlecode._4s_web.client.ui.CharacterNetwork;
import com.googlecode._4s_web.client.ui.CheckList;
import com.googlecode._4s_web.client.ui.DiscourseTimeline;
import com.googlecode._4s_web.client.ui.EntropyComplexity;
import com.googlecode._4s_web.client.ui.EventNetwork;
import com.googlecode._4s_web.client.ui.KnowledgeFlow;
import com.googlecode._4s_web.client.ui.KnowledgeStructure;
import com.googlecode._4s_web.client.ui.NetworkComplexity;
import com.googlecode._4s_web.client.ui.StoryTimeline;

/**
 * 스토리 웹 앱 모듈 <br>
 * 
 * 스토리 구성 입력과 각종 분석 화면들을 탭으로 가진다.
 * 하나의 스토리에 대응하며, 데이터스토어 읽기/쓰기/삭제 서비스 요청은 이 모듈에서 담당한다.
 * 나머지 화면들에서는 모듈에서 받은 로컬 캐시를 읽고, 
 * 수정사항들을 로컬에 반영 후 각자 데이터스토어 저장을 이 모듈로 요청한다.
 * 
 * @author jehdeiah
 */
public class StoryApp implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Story service.
	 */
	private final StoryServiceAsync storyService = GWT
			.create(StoryService.class);
	
	/**
	 * 사용자 정의 이벤트 처리를 위한 이벤트 버스 
	 */
	private final SimpleEventBus mainBus = new SimpleEventBus();

	/**
	 * 현재 작업 중인 스토리 
	 */
	StoryEntity story = null;

	/**
	 * Tab header 상자에 대한 마우스 이벤트 핸들러.
	 */
	class TabHeaderMouseHandler implements MouseUpHandler, MouseOverHandler, MouseOutHandler {

		TabHeaderUI tabHeaderUI;
		
		public TabHeaderMouseHandler(TabHeaderUI e) {
			tabHeaderUI = e;
		}

		@Override
		public void onMouseUp(MouseUpEvent event) {
			int clickedTabId = tabHeaderUI.getId();
			int currentTabId = tabLayoutPanel.getSelectedIndex(); 
			if (currentTabId != clickedTabId) {
				tabLayoutPanel.selectTab(clickedTabId);
			}
			// There may be exceptions to make wrong...
			for (int i=0; i<tabHeaderUIs.length; i++)
				tabHeaderUIs[i].removeStyleName("tabBar-selected");
			tabHeaderUIs[clickedTabId].addStyleName("tabBar-selected");
		}

		@Override
		public void onMouseOver(MouseOverEvent event) {
			tabHeaderUI.getElement().getStyle().setCursor(Cursor.POINTER);
		}

		@Override
		public void onMouseOut(MouseOutEvent event) {
			tabHeaderUI.getElement().getStyle().setCursor(Cursor.AUTO);
		}
	}
	/**
	 * Tab header
	 */
	class TabHeaderUI extends Label {
		int id;
		public TabHeaderUI(String text, int id) {
			super(text);
			this.id = id;
			setStyleName("tabBar");
			TabHeaderMouseHandler h = new TabHeaderMouseHandler(this);
			addMouseUpHandler(h);
			addMouseOverHandler(h);
			addMouseOutHandler(h);
		}
		public int getId() {
			return id;
		}
	}
	
	/*
	 * 탭 목록. 
	 * 반드시 이 순서대로 탭을 추가하여 getWidget(index)로 탭을 얻을 수 있게 한다.
	 */
	final int NUM_TAB_IN = 4;
	final int TAB_IN_OVERVIEW = 0;
	final int TAB_IN_STORY_TIMELINE = 1;
	final int TAB_IN_DISCOURSE_TIMELINE = 2;
	final int TAB_IN_KNOWLEDGE_STRUCTURE = 3;
	final int TAB_OUT_CHECKLIST = 4;
	final int TAB_OUT_NETWORK = 5;
	final int TAB_OUT_QA_PATTERN = 6;
	final int TAB_OUT_KNOWLEDGE_FLOW = 7;
	final int TAB_OUT_CATEGORY_VIEW = 8;
	final int TAB_OUT_COMPLEXITY = 9;
	final int TAB_OUT_SUSPENSE = 10;
	final String[] tabHeader = { "Narrative Overview", "Story-time Organizer",
			"Discourse-time Organizer", "Knowledge Structure",
			"Check List", "Character & Event Graph", "Q&A Pattern",
			"Knowledge Flow", "Category View", "Complexity", "Suspense Situation"};
	Widget[] tabWidget = new Widget[tabHeader.length];

	/**
	 * 화면 상단 제목 표시에 대응하는 DOM Element
	 */
	Element titleBar;

	/**
	 * 편집화면들이 모여있는 탭 레이아웃 패널 
	 */
	TabLayoutPanel tabLayoutPanel;
	FlowPanel inputTabBar;
	FlowPanel outputTabBar;
	TabHeaderUI[] tabHeaderUIs;
	int currentTabId = 0;
	
	/*
	 * 자료 저장 요청과 탭 전환 처리
	 * 자료 저장 요청이 있었고 저장이 완료되면 후속 처리를 한다.
	 * 탭 이동 전에 저장이 완료될 수도 있고, 탭 이동 후에 완료될 수도 있다.
	 * 탭 갱신은 이동 후에야 가능하다.
	 * 그러므로 완료 보고 때 탭 이동도 끝나 있으면 갱신 처리하고,
	 * 탭 이동 때 완료 보고가 끝났으면 갱신 처리한다. 
	 */
	boolean saveRequested;
	boolean saveDone;
	boolean selectionRequested;
	boolean selectionDone;

	/*
	 * 저장/삭제 요청 엔티티 종류
	 */
	int savedEntityKind;
	int deletedEntityKind;
	/*
	 * 비동기 요청에 의해 이전 저장/삭제 요청 처리 중에 들어온 요청 엔티티 종류
	 * 미뤄두는 요청은 최신 요청 하나만 유지하도록 한다.
	 */
	int defferedSavedEntityKind;
	int defferedDeletedEntityKind;
	
	/**
	 * This is the entry point method.
	 * 머리글만 보이고 스토리를 고르는 것으로 넘어간다.
	 * 편집화면은 스토리가 정해지면 보여준다.
	 */
	public void onModuleLoad() {

		/*
		 * 화면 상단: 작품 제목
		 */
		titleBar = RootPanel.get("title").getElement();
		
		/*
		 * 커스텀 이벤트 처리기
		 */
		RequestEntityBuffer.initialize(mainBus);
		// 1) 패널에서 데이터스토어 저장/삭제 요청
		// 스토리 정보 저장은 예외적으로 StoryOverview에서 한다.
		DataSaveEvent.register(mainBus, new DataSaveEvent.Handler() {
			
			public void onRequest(DataSaveEvent event) {
				if (savedEntityKind==0 && deletedEntityKind==0) {
					savedEntityKind = event.getSavedKind();
					deletedEntityKind = event.getDeletedKind();
					if (savedEntityKind != 0) {
						saveRequested = true;
						saveDone = false;
						saveStoryEntities();
					} else if (deletedEntityKind != 0) {
						saveRequested = true;
						saveDone = false;
						deleteStoryEntities();
					}
				} else {
					defferedSavedEntityKind = event.getSavedKind();
					defferedDeletedEntityKind = event.getDeletedKind();
				}
			}
		});
		// 2) 스토리 선택 보고. 스토리 개요 내용을 바꿔도 이 메시지로 받는다.
		StoryChangedEvent.register(mainBus, new StoryChangedEvent.Handler() {
			
			@Override
			public void onChanged(StoryChangedEvent event) {
				setStory(event.getStory());
			}
		});

		/*
		 * 탭 레이아웃. 
		 */
		RootPanel divPanel = RootPanel.get("workspace");
		tabLayoutPanel = new TabLayoutPanel(0, Unit.EM);
		inputTabBar = new FlowPanel();
		outputTabBar = new FlowPanel();
		tabHeaderUIs = new TabHeaderUI[tabHeader.length];
		for (int i=0; i<tabHeaderUIs.length; i++)
		{
			tabHeaderUIs[i] = new TabHeaderUI(tabHeader[i], i);
		}
		/*
		 * FIXME: 성능을 위해 lazy loading을 고려해 보았으나 문제가 좀 있다. LazyPanel을 쓰면 패널이므로 위젯이
		 * <div>로 감싸지는데, 이것 때문에 레이아웃이 깨지는 것 같다. 나중에 필요하면 위젯 치환으로 고치자.
		 */
		DiscourseTimeline dt = new DiscourseTimeline(storyService, mainBus);
		// Split panel for character & event graphs
		LayoutPanel cen = new LayoutPanel();
		CharacterNetwork cn = new CharacterNetwork();
		EventNetwork en = new EventNetwork();
		cen.add(cn);
		cen.add(en);
		cen.setWidgetLeftWidth(cn, 0, Unit.PCT, 50, Unit.PCT);
		cen.setWidgetRightWidth(en, 0, Unit.PCT, 50, Unit.PCT);
		// checklist
		CheckList cl = new CheckList();
		cl.addDiscourseTimeline(dt);
		// Split panel for knowledge flow
		LayoutPanel kf = new LayoutPanel();
		KnowledgeFlow kf1 = new KnowledgeFlow("Knowledge Flow 1");
		KnowledgeFlow kf2 = new KnowledgeFlow("Knowledge Flow 2");
		kf.add(kf1);
		kf.add(kf2);
		kf.setWidgetLeftWidth(kf1, 0, Unit.PCT, 50, Unit.PCT);
		kf.setWidgetRightWidth(kf2, 0, Unit.PCT, 50, Unit.PCT);
		// Split panel for complexity analysis
		LayoutPanel cmp = new LayoutPanel();
		NetworkComplexity netcomp = new NetworkComplexity();
		EntropyComplexity entcomp = new EntropyComplexity();
		cmp.add(netcomp);
		cmp.add(entcomp);
		cmp.setWidgetLeftWidth(netcomp, 0, Unit.PCT, 50, Unit.PCT);
		cmp.setWidgetRightWidth(entcomp, 0, Unit.PCT, 50, Unit.PCT);
		
		tabWidget[TAB_IN_OVERVIEW] = new StoryOverview(storyService, mainBus); 
		tabWidget[TAB_IN_STORY_TIMELINE] = new StoryTimeline(storyService, mainBus);
		tabWidget[TAB_IN_DISCOURSE_TIMELINE] = dt;  
		tabWidget[TAB_IN_KNOWLEDGE_STRUCTURE] = new KnowledgeStructure(storyService, mainBus);
		tabWidget[TAB_OUT_CHECKLIST] = cl;
		tabWidget[TAB_OUT_NETWORK] = cen;
		tabWidget[TAB_OUT_QA_PATTERN] = new HTML("Q&A Pattern");
		tabWidget[TAB_OUT_KNOWLEDGE_FLOW] = kf;
		tabWidget[TAB_OUT_CATEGORY_VIEW] = new CategoryView(storyService, mainBus);
		tabWidget[TAB_OUT_COMPLEXITY] = cmp;
		tabWidget[TAB_OUT_SUSPENSE] = new HTML("Suspense situation");
		// 개요 탭만 추가하고, 스토리를 고르면 나머지 탭들을 추가한다.
		inputTabBar.add(tabHeaderUIs[0]);
		tabHeaderUIs[0].addStyleName("tabBar-selected");
		tabLayoutPanel.add(tabWidget[TAB_IN_OVERVIEW], tabHeader[TAB_IN_OVERVIEW]);
		
		tabLayoutPanel.setHeight("100%");
		
		/*
		 * 현재는 탭을 전환할 때 이전 탭 작업 내용을 저장한다.
		 * 개요인 경우는 저장할 것이 없다.
		 */
		tabLayoutPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

			public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
				// save any changes in the previous tab
				selectionRequested = true;
				selectionDone = false;
				int previous = tabLayoutPanel.getSelectedIndex();
				switch (previous) {
				case TAB_IN_OVERVIEW:
					break;
				case TAB_IN_STORY_TIMELINE:
				case TAB_IN_DISCOURSE_TIMELINE:
				case TAB_IN_KNOWLEDGE_STRUCTURE:
				case TAB_OUT_CATEGORY_VIEW:
					AbstractStoryPanel panel = (AbstractStoryPanel)tabLayoutPanel.getWidget(previous);
					panel.invalidate();
					panel.applyChanges();
					break;
				case TAB_OUT_NETWORK:
					// 캐릭터 속성 등을 바꿀 수 있다.
					RequestEntityBuffer.flush(); 
					break;
				case TAB_OUT_CHECKLIST:
				case TAB_OUT_QA_PATTERN:
				case TAB_OUT_KNOWLEDGE_FLOW:
				case TAB_OUT_COMPLEXITY:
				case TAB_OUT_SUSPENSE:
					break;
				}
			}
		});
		/*
		 * 탭을 열 때마다 로컬 캐시를 바탕으로  화면 요소를 갱신하는데,
		 * 현재 저장 시점으로는 비동기식 저장이 완료된 다음 각 탭을 갱신토록 했다.
		 * 그러나 개요 탭에서는 저장 요청을 안 하고 저장 완료와 탭 이동 시차가 발생할 수 있으므로 조건을 두고 처리한다.
		 */
		tabLayoutPanel.addSelectionHandler(new SelectionHandler<Integer>() {

			public void onSelection(SelectionEvent<Integer> event) {
				selectionDone = true;
				// 저장이 아직 안 끝났으면 완료 보고 때 갱신 처리
				// FIXME: 일단은 그냥 넘어가는데 타이밍을 보고 저장하는 동안 UI를 막아야 할 수도 있다.
				boolean updateReady = (!saveRequested) || (saveRequested && saveDone);
				if (updateReady) {
					saveRequested = false;
					saveDone = false;
					selectionRequested = false;
					selectionDone = false;
					updateClientWidget();
				}
			}
		});
		divPanel.add(tabLayoutPanel);
		divPanel = RootPanel.get("inputBar");
		divPanel.add(inputTabBar);
		divPanel = RootPanel.get("outputBar");
		divPanel.add(outputTabBar);
	}

	/**
	 * 탭 화면 요소를 로컬 캐시에 맞게 갱신한다.
	 */
	public void updateClientWidget() {
		tabLayoutPanel.forceLayout();
		int tab = tabLayoutPanel.getSelectedIndex();
		switch (tab) {
		case TAB_IN_OVERVIEW:
			break;
		case TAB_IN_STORY_TIMELINE:
		case TAB_IN_DISCOURSE_TIMELINE:
		case TAB_IN_KNOWLEDGE_STRUCTURE:
		case TAB_OUT_CATEGORY_VIEW:
			((AbstractStoryPanel)tabLayoutPanel.getWidget(tab)).updateAll();
			break;
		case TAB_OUT_NETWORK:
			((CharacterNetwork)((LayoutPanel)tabLayoutPanel.getWidget(tab)).getWidget(0)).draw();
			((EventNetwork)((LayoutPanel)tabLayoutPanel.getWidget(tab)).getWidget(1)).draw();
			break;
		case TAB_OUT_CHECKLIST:
			break;
		case TAB_OUT_QA_PATTERN:
			break;
		case TAB_OUT_KNOWLEDGE_FLOW:
			((KnowledgeFlow)((LayoutPanel)tabLayoutPanel.getWidget(tab)).getWidget(0)).analyze();
			((KnowledgeFlow)((LayoutPanel)tabLayoutPanel.getWidget(tab)).getWidget(1)).analyze();
			break;
		case TAB_OUT_COMPLEXITY:
			((NetworkComplexity)((LayoutPanel)tabLayoutPanel.getWidget(tab)).getWidget(0)).analyze();
			((EntropyComplexity)((LayoutPanel)tabLayoutPanel.getWidget(tab)).getWidget(1)).analyze();
			break;
		case TAB_OUT_SUSPENSE:
			break;
		}
	}

	/**
	 * 선택한 스토리를 읽어온다. 
	 */
	private void setStory(StoryEntity story) {
		if (story == null) {
			titleBar.setInnerText("[No story was selected...]");
			while (tabLayoutPanel.getWidgetCount() > 1) {
				tabLayoutPanel.remove(1);
			}
			return;
		}
		boolean changed = this.story==null || this.story.getId()!=story.getId();
		titleBar.setInnerText(story.getTitle());
		if (changed) {
			this.story = story;
			/*
			 * 새 작업 스토리를 설정하고 내용을 읽어온다.
			 * 만약 실패하면 개요 탭만 남기자.
			 */
			storyService.setWorkingStory(story.getId(), new AsyncCallback<Void>() {
	
				public void onFailure(Throwable caught) {
					while (tabLayoutPanel.getWidgetCount() > 1) {
						tabLayoutPanel.remove(1);
					}
					inputTabBar.clear();
					inputTabBar.add(tabHeaderUIs[0]);
					tabHeaderUIs[0].addStyleName("tabBar-selected");
					outputTabBar.clear();
				}
	
				public void onSuccess(Void result) {
					if (tabLayoutPanel.getWidgetCount() == 1) {
						for (int i=1; i<tabWidget.length; i++) {
							tabLayoutPanel.add(tabWidget[i], tabHeader[i]);
							if (i < NUM_TAB_IN) inputTabBar.add(tabHeaderUIs[i]);
							else outputTabBar.add(tabHeaderUIs[i]);
						}
					}
					/*
					 * 클라이언트 화면 요소들을 모두 지운다.
					 */
					for (Widget w : tabWidget) {
						if (w instanceof AbstractStoryPanel)
							((AbstractStoryPanel)w).clear();
					}
					loadStoryEntities();
				}
			});
		}
	}

	/**
	 * 엔티티 저장이 완료되면 불리는 메쏘드. 
	 * 삭제할 것이 있으면 삭제하고, 새로 요청이 왔었으면 처리하고, 없으면 클라이언트 화면 갱신을 시도한다.
	 */
	protected void notifySaveDone() {
		savedEntityKind = 0;
		if (deletedEntityKind != 0) { // 삭제 요청
			deleteStoryEntities();
		} else {
			if (defferedSavedEntityKind != 0) { // 다시 저장 요청이 온 경우
				savedEntityKind = defferedSavedEntityKind;
				defferedSavedEntityKind = 0;
				if (defferedDeletedEntityKind != 0) { // 저장 후 삭제 요청 처리 예약
					deletedEntityKind = defferedDeletedEntityKind;
					defferedDeletedEntityKind = 0;
				}
				saveStoryEntities();
			} else if (defferedDeletedEntityKind != 0) { // 삭제 요청만 쌓였던 경우
				deletedEntityKind = defferedDeletedEntityKind;
				defferedDeletedEntityKind = 0;
				deleteStoryEntities();
			} else { // 모든 요청이 끝난 상태
				saveDone = true;
				if (!selectionRequested) {
					saveRequested = saveDone = false;
				}
				if (selectionDone) {
					saveRequested = false;
					saveDone = false;
					selectionRequested = false;
					selectionDone = false;
					updateClientWidget();
				}
			}
		}
	}
	
	/**
	 * 엔티티 삭제가 완료되면 불리는 메쏘드.
	 * 이미 저장은 끝난 상태이므로 미뤄둔 요청만 없다면 클라이언트 화면 갱신을 시도한다.
	 */
	protected void notifyDeleteDone() {
		deletedEntityKind = 0;
		if (defferedSavedEntityKind != 0) { // 다시 저장 요청이 온 경우
			savedEntityKind = defferedSavedEntityKind;
			defferedSavedEntityKind = 0;
			if (defferedDeletedEntityKind != 0) { // 저장 후 삭제 요청 처리 예약
				deletedEntityKind = defferedDeletedEntityKind;
				defferedDeletedEntityKind = 0;
			}
			saveStoryEntities();
		} else if (defferedDeletedEntityKind != 0) { // 삭제 요청만 쌓였던 경우
			deletedEntityKind = defferedDeletedEntityKind;
			defferedDeletedEntityKind = 0;
			deleteStoryEntities();
		} else { // 모든 요청이 끝난 상태
			saveDone = true;
			if (!selectionRequested) {
				saveRequested = saveDone = false;
			}
			if (selectionDone) {
				saveRequested = false;
				saveDone = false;
				selectionRequested = false;
				selectionDone = false;
				updateClientWidget();
			}
		}
	}
	
	/*------------------------------------------------------------------------
	 * 데이터스토어 관련 서비스 요청 부분
	 * (1) Load
	 * (2) save
	 * (3) Delete
	 */
	
	/**
	 * 데이터스토어에서 스토리 내용 자료를 모두 읽어온다.
	 */
	public void loadStoryEntities() {
		/*
		 *  데이터스토어에서 스토리 자료들을 다음과 같은 순서로 읽어온다.
		 *  이전 콜백에서 비동기 서비스를 요청하여 연속으로 이어지게 한다.
		 *  
		 *  0) 스토리 정보는 이미 읽혀 있다.
		 *  1) 스토리 타임
		 *  2) 저장 순서와 동일한 나머지 요소
		 */
		titleBar.setInnerText(story.getTitle());
		LocalCache.flush();
		final Queue<Class<? extends LocalEntity>> entityQueue = RequestEntityBuffer.getSavedEntityQueue(false);
		// 스토리 타임 
		storyService.loadStoryTime(new AsyncCallback<ArrayList<Integer>>() {

			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}

			public void onSuccess(ArrayList<Integer> result) {
				final ArrayList<Integer> ordinalPoints = result;

				storyService.loadStoryTimeAnnotation(new AsyncCallback<HashMap<String,Integer>>() {

					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}

					public void onSuccess(HashMap<String, Integer> result) {
						// 스토리 시간 로컬 캐시 저장 
						StoryTimePoint.set(ordinalPoints, result);
						// 나머지 요소 불러오기
						loadEntities(entityQueue);
					}
				});
			}
		});
	}
	
	/**
	 * 엔티티들을 유형별로 차례로 읽어서 로컬 캐시에 저장한다.
	 * @param entityQueue	저장한 엔티티 클래스 목록 
	 */
	@SuppressWarnings("rawtypes")
	protected void loadEntities(Queue<Class<? extends LocalEntity>> entityQueue) {
		if (entityQueue==null || entityQueue.isEmpty()) {
			updateClientWidget();
			return;
		}
		final Class headEntity = entityQueue.poll();
		final Queue<Class<? extends LocalEntity>> rest = entityQueue;
		storyService.loadEntities(headEntity.getName(), new AsyncCallback<LocalEntity[]> () {

			public void onFailure(Throwable arg0) {
				loadEntities(rest);
			}

			public void onSuccess(LocalEntity[] entities) {
				for (LocalEntity e : entities)
					LocalCache.add(e);

				loadEntities(rest);
			}
		});
	}
	
	/**
	 * 편집기 등에서 작업한 내용을 로컬 캐시에 반영하고 데이터스토어에 변경 사항을 저장한다.
	 * 일반적으로 모듈에서 탭이 바뀔 때 이전에 선택한 탭의 메쏘드를 부른다.
	 */
	protected void saveStoryEntities() {
		if (storyService==null || savedEntityKind==0) return;
		/*
		 * 저장 순서:
		 * (1) 스토리 타임
		 * (2) 나머지 요소들
		 */
		final Queue<Class<? extends LocalEntity>> q = RequestEntityBuffer.getSavedEntityQueue();
		// 스토리 시간 저장
		if ((savedEntityKind & DataSaveEvent.TIME) != 0) {
			storyService.saveStoryTime(StoryTimePoint.getOrdinalPoints(), 
					StoryTimePoint.getAnnotation(), 
					new AsyncCallback<Void>() {
	
						public void onFailure(Throwable caught) {
							saveEntities(q);
						}
	
						public void onSuccess(Void result) {
							saveEntities(q);
						}
					}
				);
		} else {
			saveEntities(q);
		}
	}
	
	/**
	 * 데이터스토에 큐에 지정된 엔티티 클래스 순서로 요소들을 저장한다. <br>
	 * 비동기식 서비스를 동기식으로 하기 위해 이전 요청의 성공 결과에서 다음 요청을 부른다.
	 * @param entityQueue
	 */
	protected void saveEntities(Queue<Class<? extends LocalEntity>> entityQueue) {
		if (entityQueue==null || entityQueue.isEmpty()) {
			savedEntityKind = 0;
			notifySaveDone();
			return;
		}
		final Class<? extends LocalEntity> headEntity = entityQueue.poll();
		final Queue<Class<? extends LocalEntity>> rest = entityQueue;
		LocalEntity[] saved = RequestEntityBuffer.getSaveBuffer(headEntity);
		if (saved!=null && saved.length>0) {
			storyService.saveEntities(headEntity.getName(), saved,
					new AsyncCallback<Void>() {

						public void onFailure(Throwable caught) {
							// 지금 것은 그냥 넘기고 나머지를 저장한다.
							saveEntities(rest);
						}

						public void onSuccess(Void result) {
							RequestEntityBuffer.clearSaveBuffer(headEntity);
							saveEntities(rest);
						}
			});
		}
	}
	
	/**
	 * 삭제로 인한 변경내용 저장이 완료되면 비로소 삭제 요청을 한다.
	 */
	protected void deleteStoryEntities() {
		if (storyService==null || deletedEntityKind==0) return;
		Queue<Class<? extends LocalEntity>> q = RequestEntityBuffer.getDeletedEntityQueue();
		requestDeleteEntities(q);
	}
	
	/**
	 * 엔티티 유형별로 순차적(동기식)으로 삭제한다.
	 * @param entityQueue 삭제할 엔티티 자료형 대기열 
	 */
	protected void requestDeleteEntities(Queue<Class<? extends LocalEntity>> entityQueue) {
		if (entityQueue==null || entityQueue.isEmpty()) {
			notifyDeleteDone();
			return;
		}
		final Class<? extends LocalEntity> headEntity = entityQueue.poll();
		final Queue<Class<? extends LocalEntity>> rest = entityQueue;
		Long[] deleted = RequestEntityBuffer.getDeleteBuffer(headEntity);
		if (deleted!=null && deleted.length>0) {
			storyService.deleteEntities(headEntity.getName(), deleted, 
					new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							// 지금 것은 그냥 넘기고 나머지를 삭제한다.
							RequestEntityBuffer.clearDeleteBuffer(headEntity);
							requestDeleteEntities(rest);
						}

						@Override
						public void onSuccess(Void result) {
							RequestEntityBuffer.clearDeleteBuffer(headEntity);
							requestDeleteEntities(rest);
						}
				
			});
			return;
		}
	}	

}
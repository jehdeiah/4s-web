package com.googlecode._4s_web.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.LocalCache;
import com.googlecode._4s_web.client.entity.StoryEntity;
import com.googlecode._4s_web.client.entity.StoryTimePoint;
import com.googlecode._4s_web.client.ui.DiscourseTimeline;
import com.googlecode._4s_web.shared.Interval;

/**
 * 스토리 개요와 저장된 목록을 보여주는 화면
 * 
 * @author jehdeiah
 *
 */
public class StoryOverview extends Composite {

	private static StoryOverviewUiBinder uiBinder = GWT
			.create(StoryOverviewUiBinder.class);

	interface StoryOverviewUiBinder extends UiBinder<Widget, StoryOverview> {
	}

	interface MyStyle extends CssResource {
		String focusedRow();

		String selectedRow();
	}

	StoryServiceAsync storyService;
	EventBus storyBus;

	protected StoryOverview() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public StoryOverview(StoryServiceAsync service, EventBus bus) {
		storyService = service;
		storyBus = bus;
		storyBag = new ArrayList<StoryEntity>();
		selectedStoryIndex = -1;
		story = null;
		initWidget(uiBinder.createAndBindUi(this));
	}

	HorizontalPanel focusedCell;
	HTML focusedCellText;
	Button loadButton;
	int selectedStoryIndex;
	int focusedStoryIndex;

	ArrayList<StoryEntity> storyBag;
	StoryEntity story;
	final String noTitle = "무제";
	final String noIdea = "자유롭게 생각을 적으세요.";

	/*
	 * 통신 상황 메시지 표시styleName창
	 */
	DialogBox dialogBox;
	Label textToServerLabel;
	HTML serverResponseLabel;
	Button closeButton;

	@Override
	protected void onLoad() {
		/*
		 * 서버와의 통신 상태를 보여주는 팝업
		 */
		// Create the popup dialog box
		dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		textToServerLabel = new Label();
		serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Request to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				serverResponseLabel.removeStyleName("serverResponseLabelError");
			}
		});

		/*
		 * 스토리 목록에서 스토리를 선택하고 여는 버튼
		 */
		loadButton = new Button();
		loadButton.setText("Load");
		loadButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				focusedCell.removeFromParent();
				storyList.setText(focusedStoryIndex + 1, 1,
						storyBag.get(focusedStoryIndex).getTheme());
				storyList.getRowFormatter().removeStyleName(
						focusedStoryIndex + 1, style.selectedRow());
				selectStory(focusedStoryIndex);
			}

		});
		focusedCell = new HorizontalPanel();
		focusedCellText = new HTML("");
		focusedCell.add(focusedCellText);
		focusedCell.add(loadButton);
		focusedCell.setWidth("100%");
		focusedCell.setHeight("100%");
		loadButton.setWidth("40px");
		// loadButton.setHeight("100%");
		focusedCell.setCellWidth(loadButton, "42px");

		// 스토리 목록 읽어오기
		storyList.getColumnFormatter().setWidth(0, "200px");
		loadStoryList();
	}

	/**
	 * 목록에서 작업 스토리를 고른다.
	 * 
	 * @param index
	 *            선택할 스토리 색인. -1일 경우 예외 상황이므로 현상 유지를 한다.
	 */
	void selectStory(int index) {
		if (index == -1)
			return;
		if (selectedStoryIndex != -1) {
			storyList.getRowFormatter().removeStyleName(selectedStoryIndex + 1,
					style.selectedRow());
		}
		storyList.getRowFormatter()
				.addStyleName(index + 1, style.selectedRow());
		selectedStoryIndex = index;
		story = storyBag.get(selectedStoryIndex);
		title.setText(story.getTitle());
		idea.setText(story.getTheme());
		storyBus.fireEvent(new StoryChangedEvent(story));
	}

	/**
	 * 비동기식 서비스 요청이 완료되면 알린다.
	 */
	public void notifySaved() {
		if (storyBus != null)
			storyBus.fireEvent(new StoryChangedEvent(story));
	}

	/**
	 * Datastore에서 스토리를 읽어온다.
	 */
	void loadStoryList() {
		selectedStoryIndex = -1;
		focusedStoryIndex = -1;
		title.setText("");
		idea.setText("");
		textToServerLabel.setText("Loading story lists...");
		serverResponseLabel.setHTML("");
		closeButton.setVisible(false);
		dialogBox.center();

		storyService.loadStoryHeader(new AsyncCallback<StoryEntity[]>() {

			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
				dialogBox.setText("Remote Procedure Call - Failure");
				serverResponseLabel.addStyleName("serverResponseLabelError");
				serverResponseLabel.setHTML(StoryApp.SERVER_ERROR);
				dialogBox.center();
				closeButton.setVisible(true);
				closeButton.setFocus(true);
				if (story == null) {
					createNewStory(noTitle, noIdea);
				}
			}

			public void onSuccess(StoryEntity[] result) {
				/*
				 * 이야기 고르는 창 만들기
				 */
				if (result == null || result.length == 0) {
					serverResponseLabel
							.setHTML("No saved story found. New Story will be created..");
					dialogBox.center();
					closeButton.setVisible(true);
					closeButton.setFocus(true);
					createNewStory(noTitle, noIdea);
				} else {
					storyBag.clear();
					storyList.resizeRows(result.length + 1);
					int row = 1;
					for (StoryEntity s : result) {
						storyBag.add(s);
						storyList.setText(row, 0, s.getTitle());
						storyList.setText(row, 1, s.getTheme());
						row++;
					}
					dialogBox.hide();
					if (result.length == 1)
						selectStory(0);
				}
			}

		});
	}

	/**
	 * Datastore에서 스토리를 읽어온다.
	 */
	void importFile() {
		/*
		 * selectedStoryIndex = -1; focusedStoryIndex = -1; title.setText("");
		 * idea.setText("");
		 * textToServerLabel.setText("Importing from a file ...");
		 * serverResponseLabel.setHTML(""); closeButton.setVisible(false);
		 * dialogBox.center();
		 */
		VerticalPanel panel = new VerticalPanel();
		// create a FormPanel
		final FormPanel form = new FormPanel();
		// create a file upload widget
		final FileUpload fileUpload = new FileUpload();
		fileUpload.setName("fileUpload");
		// create labels
		Label selectLabel = new Label("Select a file(Tab separated):");
		
		TextBox storyNameTextBox = new TextBox(); 
		//add text to text box
		storyNameTextBox.setText("ImportedFile");
		storyNameTextBox.setName("storyName");
		// create upload button
		Button uploadButton = new Button("Upload File");
		// pass action to the form to point to service handling file
		// receiving operation.
		form.setAction(GWT.getModuleBaseURL() + "file");
		// set form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// add a label
		panel.add(selectLabel);
		// add story name
		panel.add(storyNameTextBox);
		// add fileUpload widget
		panel.add(fileUpload);
		// add a button to upload the file
		panel.add(uploadButton);
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// get the filename to be uploaded
				String filename = fileUpload.getFilename();
				if (filename.length() == 0) {
					Window.alert("No File Specified!");
				} else {
					// submit the form
					form.submit();
				}
			}
		});

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// When the form submission is successfully completed, this
				// event is fired. Assuming the service returned a response
				// of type text/html, we can get the result text here
				loadStoryList();
				//Window.alert(event.getResults());
			}
		});
		panel.setSpacing(10);

		// Add form to the root panel.
		form.add(panel);

		RootPanel.get("workspace").add(form);

	}
	
	void exportFile() {
		VerticalPanel panel = new VerticalPanel();
		Button downloadButton = new Button("Download File");
		String link = GWT.getModuleBaseURL() + "myfiledownload";
        RootPanel.get().add(new HTML("<a href=\"" + link + "\">Download File</a>"));
		/*
		downloadButton.addClickHandler(new ClickHandler() {

		    @Override
		    public void onClick(ClickEvent event) {
		        Window.open("http://127.0.0.1:8888/file.rar", "_self", "enabled");
		    }
		});
		*/
	}

	/**
	 * 스토리를 새로 만들고 그것을 작업 스토리로 선택한다.
	 * 
	 * @param newTitle
	 *            새 스토리 제목
	 * @param theme
	 *            새 스토리 주제
	 */
	void createNewStory(String newTitle, String theme) {
		textToServerLabel.setText("Create a new story...");
		serverResponseLabel.setHTML("");
		closeButton.setVisible(false);
		dialogBox.center();
		storyService.createNewStory(newTitle, theme,
				new AsyncCallback<StoryEntity>() {

					public void onFailure(Throwable caught) {
						dialogBox.setText("Remote Procedure Call - Failure");
						serverResponseLabel
								.addStyleName("serverResponseLabelError");
						serverResponseLabel
								.setHTML("Failed to create a story!");
						dialogBox.center();
						closeButton.setVisible(true);
						closeButton.setFocus(true);
						// selectStory(-1);
					}

					public void onSuccess(StoryEntity result) {
						dialogBox.hide();
						storyBag.add(result);
						int row = storyList.insertRow(storyList.getRowCount());
						storyList.setText(row, 0, result.getTitle());
						storyList.setText(row, 1, result.getTheme());
						selectStory(storyBag.size() - 1);
					}
				});
	}

	/**
	 * Datastore에 스토리 정보를 쓴다.
	 */
	void saveStoryInfo() {
		if (story == null)
			return;
		String newTitle = title.getText();
		String newIdea = idea.getText();
		if (!newTitle.equals(story.getTitle())
				|| !newIdea.equals(story.getTheme())) {

			story.setTitle(newTitle);
			story.setTheme(newIdea);
			// 목록 요소 변경
			storyList.setText(selectedStoryIndex + 1, 0, newTitle);
			storyList.setText(selectedStoryIndex + 1, 1, newIdea);
			storyService.saveStoryHeader(story, new AsyncCallback<Void>() {

				public void onFailure(Throwable caught) {
				}

				public void onSuccess(Void result) {
					notifySaved();
				}
			});
		}
	}

	/*
	 * UI Binder
	 */
	@UiField
	MyStyle style;
	@UiField
	TextBox title;
	@UiField
	TextArea idea;
	@UiField
	Grid storyList;

	@UiHandler("storyList")
	void onClickStoryList(ClickEvent event) {
		HTMLTable.Cell cell = storyList.getCellForEvent(event);
		if (cell == null)
			return;
		int row = cell.getRowIndex();
		if (row > 0 && focusedStoryIndex != (row - 1)) {
			if (focusedStoryIndex != -1) {
				focusedCell.removeFromParent();
				storyList.setText(focusedStoryIndex + 1, 1,
						storyBag.get(focusedStoryIndex).getTheme());
				storyList.getRowFormatter().removeStyleName(
						focusedStoryIndex + 1, style.focusedRow());
			}
			if (selectedStoryIndex == (row - 1)) {
				focusedStoryIndex = -1;
			} else {
				focusedStoryIndex = row - 1;
				focusedCellText.setText(storyBag.get(focusedStoryIndex)
						.getTheme());
				storyList.setWidget(row, 1, focusedCell);
				storyList.getRowFormatter().addStyleName(row,
						style.focusedRow());
			}
		}
	}
	
	@UiHandler("createNew")
	void onClickCreateNew(ClickEvent event) {
		createNewStory(noTitle, noIdea);
	}

	@UiHandler("importFile")
	void onClickImportFile(ClickEvent event) {
		importFile();
	}
	
	@UiHandler("exportFile")
	void onClickExportFile(ClickEvent event) {
		exportFile();
	}

	@UiHandler("refresh")
	void onClickRefresh(ClickEvent event) {
		loadStoryList();
	}

	@UiHandler("title")
	void onKeyPressTitle(KeyPressEvent event) {
		if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
			saveStoryInfo();
		}
	}

	@UiHandler("title")
	void onChangeTitle(ChangeEvent event) {
		saveStoryInfo();
	}

	@UiHandler("idea")
	void onChangeIdea(ChangeEvent event) {
		saveStoryInfo();
	}
}

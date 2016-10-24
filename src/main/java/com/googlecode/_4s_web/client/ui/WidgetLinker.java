package com.googlecode._4s_web.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 기본적으로 GWT 위젯 두 개를 연결하는 선을 HTML5 Canvas에 그린다.
 * 이벤트의 인과관계와 지식 구조 연결 등에 쓰인다.
 * <br>
 * (1) 스토리 타임라인 연결선 (곡선):
 * 		연결 지점은 기본적으로 좌/우 모서리 중앙으로 하고 두 위젯의 위치에 따라 최단거리로 잇는다.
 * 		혹시 위젯 폭이 겹쳐 있으면 같은 열에 있는 것이므로 위젯 옆에서 곡선으로 잇는다.
 * <br>
 * (2) 담화 타임라인 연결선 (곡선):
 * 		위젯 위치에 대한 보장이 없으므로 무조건 방향성을 고려하여 시작 위젯 오른쪽에서 도착 위젯 왼쪽으로 연결한다.
 * <br>
 * (3) 지식 구조 연결선 (직선/곡선):
 * 		인식, 영향 연결선은 다른 열로 연결하는 것이므로 좌/우 모서리 중앙으로 최단거리 직선으로 잇는다.
 * 		패턴은 현재 같은 열에만 생겨서 위젯 폭이 겹치므로 (1)과 같이 위젯 옆에서 곡선으로 잇는다.
 * <br>
 * 위의 쓰임새를 고려하면,
 * 1) 기본 모양을 직선/곡선으로 할 지 선택하고,(<code>curved</code>)
 * 2) 최단 거리로 이을지, 아니면 연결점을 고정할지 선택한다. (AUTO*, LEFT_TO_RIGHT, TOP_TO_BOTTOM)
 * 3) 또한, 최단거리 연결에서 폭이 겹칠 경우, 위/아래 연결을 허용할지, 좌/우 연결점을 정할 것인지 정해야 한다. (AUTO*, AUTO_RIGHT)
 *
 *  <p>
 * 연결선 편집 과정처럼 위젯을 특정 점과 연결할 필요도 있으므로 위젯 없이 연결점 좌표를 지정할 수도 있다.
 * @author jehdeiah
 *
 */
public class WidgetLinker {

	/**
	 * 선 두께와 선택표시 등으로 약간의 여유를 두어야 한다.
	 * 이것은 연결선과 편집점을 고르는 hit test의 여유 반경이기도 하다.
	 */
	final int MARGIN = 3;
	/*
	 * 연결선 종류
	 */
	/** 최단거리 연결점 자동 선택. 폭이 겹칠 경우 위/아래로 연결한다. */
	public static final int AUTO = 0;
	/** 최단거리 연결점 자동 선택. 폭이 겹칠 경우 오른쪽 옆에서 연결한다. */
	public static final int AUTO_RIGHT = 1;
	/** 왼쪽에서 오른쪽 흐름으로 위젯 연결 */
	public static final int LEFT_TO_RIGHT = 2;
	
	/*
	 * 베지어 곡선 제어점 관련 
	 */
	final double bezierWidthRatio = 0.5;
	double fromBezierX, fromBezierY, toBezierX, toBezierY;
	/* 
	 * 화살표 방향 벡터
	 */
	double arrowRX, arrowRY;
	
	/*
	 * 연결선 모양
	 */
	/** 연결선 종류 */
	int mode = AUTO_RIGHT;
	/** 직선/곡선 여부 */
	boolean curved = false;
	/** 화살표 표시 여부 */
	boolean arrow = true;
	/** 선 색깔 */
	String lineColor;
	/** 선 두께 */
	double lineWidth = 1.0;
	/** 연결선 종류에 따른 연결점 보정 여부 */
	boolean adjustPoint = false;
	
	/** 기본 바탕색 */
	int[] transparentRGBA = {0,0,0,0};
	
	
	/** 연결선이 위젯으로 추가되는 부모 패널 */
	Panel container;
	/** 연결선을 그릴 캔버스를 담는 패널 */
	Panel panel;
	/** 연결선을 그릴 캔버스 */
	Canvas canvas;
	/** 연결선을 그리는 랜더링 컨텍스트 */
	Context2d context;
	/** 시작 위젯 */
	Widget from;
	/** 끝 위젯 */
	Widget to;
	/* 연결선 영역 */
	int left, top, right, bottom;
	/* 연결선 꼭지점 */
	int fromX, fromY, toX, toY;
	
	public WidgetLinker(Panel container) {
		this.container = container;
		lineColor = "black";
		create();
	}
	public WidgetLinker(Panel container, int mode) {
		this.container = container;
		this.mode = mode;
		if (mode==AUTO || mode==AUTO_RIGHT) curved = false;
		else curved = true;
		lineColor = "black";
		create();
	}
	
	public void setLineColor(String color) {
		lineColor = color;
		update();
	}
	public void setTransparentColor(int red, int green, int blue, int alpha) {
		transparentRGBA[0] = red;
		transparentRGBA[1] = green; 
		transparentRGBA[2] = blue; 
		transparentRGBA[3] = alpha; 
	}
	
	public void setMode(int mode) {
		this.mode = mode;
		update();
	}
	public void setCurvedStyle(boolean curved) {
		this.curved = curved;
		update();
	}
	public void setArrow(boolean arrow) {
		this.arrow = arrow;
		update();
	}
	public void setLineWidth(double width) {
		lineWidth = width;
		update();
	}
	
	public void setWidgets(Widget from, Widget to) {
		this.from = from;
		this.to = to;
		update();
	}
	
	public void setWidgets(Widget from, int toX, int toY) {
		this.from = from;
		this.to = null;
		this.toX = toX;
		this.toY = toY;
		update();
	}
	
	public void setWidgets(int fromX, int fromY, Widget to) {
		this.from = null;
		this.to = to;
		this.fromX = fromX;
		this.fromY = fromY;
		update();
	}
	
	public void setWidgets(int fromX, int fromY, int toX, int toY) {
		this.from = null;
		this.to = null;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
		update();
	}
	
	void create() {
		panel = new SimplePanel();
		canvas = Canvas.createIfSupported();
		if (canvas != null) {
			panel.addAttachHandler(new AttachEvent.Handler() {

				public void onAttachOrDetach(AttachEvent event) {
					update();
				}
				
			});
			panel.addDomHandler(new MouseDownHandler() {

				@Override
				public void onMouseDown(MouseDownEvent event) {
					// 기본 선택 동작 막기 
					event.preventDefault();
				}
				
			}, MouseDownEvent.getType());
			panel.add(canvas);
			// 이상하게 평행선을 그리는 캔버스가 아래로 밀려서 위치를 절대값으로 잡는다.
			canvas.getElement().getStyle().setPosition(Position.ABSOLUTE);
			container.add(panel);
		}
	}
	
	public void setVisible(boolean visible) {
		panel.setVisible(visible);
		if (visible) update();
	}
	
	public boolean isVisible() {
		return panel.isVisible();
	}

	public void remove() {
		panel.removeFromParent();
	}
	
	/**
	 * 현재 설정에 따라 연결선을 다시 그린다.
	 * 이 때 선택 표시 등 꾸밈 캔버스는 지운다.
	 */
	public void update() {
		final double margin = Math.max(MARGIN,  0.5*lineWidth);
		
		if (panel.isVisible() == false) return;
		if (from!=null && !from.isVisible()) return;
		if (to!=null && !to.isVisible()) return;
		
		calculatePoints();
		int width = Math.max(right - left, 5);
		int height = Math.max(bottom - top, 5);
		int eWidth = width+(int)Math.ceil(margin*2);
		int eHeight = height+(int)Math.ceil(margin*2);
		if (container instanceof LayoutPanel) {
			((LayoutPanel) container).setWidgetLeftWidth(panel, left-(int)margin, Unit.PX, eWidth, Unit.PX);
			((LayoutPanel) container).setWidgetTopHeight(panel, top-(int)margin, Unit.PX, eHeight, Unit.PX);
		} else {
			panel.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.getElement().getStyle().setLeft(left-(int)margin, Unit.PX);
			panel.getElement().getStyle().setWidth(eWidth, Unit.PX);
			panel.getElement().getStyle().setTop(top-(int)margin, Unit.PX);
			panel.getElement().getStyle().setHeight(eHeight, Unit.PX);
		}
		panel.getElement().getStyle().setZIndex(0);
		// 캔버스 초기화
		canvas.setCoordinateSpaceWidth(eWidth);
		canvas.setCoordinateSpaceHeight(eHeight);
		context = canvas.getContext2d();
		context.clearRect(0,  0, eWidth, eHeight);
		// 연결선 그리기
		context.setStrokeStyle(lineColor);
		context.setLineWidth(lineWidth);
		context.translate(margin, margin);
		drawLine();
		if (arrow) drawArrow();
	}
	
	void drawLine() {
		context.translate(-left, -top);
		context.beginPath();
		context.moveTo(fromX, fromY);
		if (curved || adjustPoint) {
			context.bezierCurveTo(fromBezierX, fromBezierY, toBezierX, toBezierY, toX, toY);
		} else {
			context.lineTo(toX, toY);
		}
		context.stroke();
	}
	
	void drawArrow() {
		// 화살표 방향 변환
		context.save();
		context.translate(toX, toY);
		context.rotate(Math.atan2(arrowRY, arrowRX));
		// 화살표 그리기
		final double arrowLength = 10;
		context.setFillStyle(lineColor);
		context.beginPath();
		context.moveTo(0, 0);
		context.lineTo(-arrowLength, -arrowLength*0.3);
		context.lineTo(-arrowLength,  arrowLength*0.3);
		context.closePath();
		context.fill();
		context.restore();
	}
	
	/**
	 * 연결선 좌표들을 계산하고 연결선을 감싸는 사각형 영역을 구한다.
	 */
	void calculatePoints() {
		int width = container.getOffsetWidth();
		//int height = container.getOffsetHeight();
		double bezierWidth = 0;
		
		adjustPoint = false;
		int leftF, rightF, topF, bottomF;
		int leftT, rightT, topT, bottomT;
		if (from != null) {
			leftF = CssUtil.getOffsetLeft(from, container);
			rightF = leftF + from.getElement().getOffsetWidth();
			topF = CssUtil.getOffsetTop(from, container);
			bottomF = topF + from.getElement().getOffsetHeight();
		} else {
			leftF = rightF = fromX;
			topF = bottomF = fromY;
		}
		if (to != null) {
			leftT = CssUtil.getOffsetLeft(to, container);
			rightT = leftT + to.getElement().getOffsetWidth();
			topT = CssUtil.getOffsetTop(to, container);
			bottomT = topT + to.getElement().getOffsetHeight();
		} else {
			leftT = rightT = toX;
			topT = bottomT = toY;
		}
		switch (mode) {
		case AUTO:
		case AUTO_RIGHT:
			if (rightF<leftT || leftF>rightT) {
				// 가로 방향 
				if (from != null) {
					fromX = (rightF < leftT) ? rightF : leftF;
					fromY = (topF + bottomF) / 2;
				}
				if (to != null) {
					toX = (rightF < leftT) ? leftT : rightT;
					toY = (topT + bottomT) / 2;
				}
				fromBezierX = toX;
				fromBezierY = fromY;
				toBezierX = fromX;
				toBezierY = toY;
				if (curved) {
					arrowRX = (toX > fromX) ? 1 : -1;
					arrowRY = 0;
				} else {
					arrowRX = toX - fromX;
					arrowRY = toY - fromY;
					double ss = Math.sqrt(arrowRX*arrowRX + arrowRY*arrowRY);
					arrowRX /= ss;
					arrowRY /= ss;
				}
			} else {
				if (mode == AUTO_RIGHT) {
					adjustPoint = true;
					fromX = rightF;
					fromY = (topF + bottomF) / 2;
					toX = rightT;
					toY = (topT + bottomT) / 2;
					double widgetWidth = Math.max(rightF-leftF, rightT-leftT);
					if (widgetWidth == 0) widgetWidth = 100;
					bezierWidth = widgetWidth * bezierWidthRatio;
					fromBezierX = Math.min(Math.max(rightF, rightT) + bezierWidth, width);
					fromBezierY = fromY;
					toBezierX = fromBezierX;
					toBezierY = toY;
					arrowRX = -1;
					arrowRY = 0;
				} else {
					// 세로 방향 
					if (from != null) {
						fromX = (leftF + rightF) / 2;
						fromY = (topF < topT) ? bottomF : topF;
					}
					if (to != null) {
						toX = (leftT + rightT) / 2;
						toY = (topF < topT) ? topT : bottomT;
					}
					fromBezierX = toX;
					fromBezierY = fromY;
					toBezierX = fromX;
					toBezierY = toY;
					if (curved) {
						arrowRX = 0;
						arrowRY = (fromY < toY) ? -1 : 1;
					} else {
						arrowRX = toX - fromX;
						arrowRY = toY - fromY;
						double ss = Math.sqrt(arrowRX*arrowRX + arrowRY*arrowRY);
						arrowRX /= ss;
						arrowRY /= ss;
					}
				}
			}
			break;
		case LEFT_TO_RIGHT:
			bezierWidth = (rightF - leftF)*bezierWidthRatio;
			fromX = rightF; fromY = (topF + bottomF) / 2;
			toX = leftT; toY = (topT + bottomT) / 2;
			fromBezierX = Math.min(width, fromX + bezierWidth);
			fromBezierY = fromY;
			toBezierX = Math.max(0, toX - bezierWidth);
			toBezierY = toY;
			arrowRX = 1;
			arrowRY = 0;
			break;
		}
		
		left = (int)Math.min(fromBezierX, toBezierX);
		if (mode==AUTO_RIGHT && adjustPoint) left = Math.min(fromX, toX);
		right = (int)Math.max(fromBezierX,  toBezierX);
		top = (int)Math.min(fromBezierY, toBezierY);
		bottom = (int)Math.max(fromBezierY, toBezierY);
	}
	
	/**
	 * 연결선에 대한 좌표 테스트.
	 * <code>canvas.isPointInPath()</code>가 선에 대해 크롬에서 안 먹히므로,
	 * 안전하게 비트맵을 읽어서 처리를 한다. 
	 * 
	 * @param x
	 * @param y
	 * @return 주어진 좌표가 연결선 위에 있으면 참, 아니면 거짓.
	 */
	public boolean hitTest(int x, int y) {
		final double margin = Math.max(MARGIN,  0.5*lineWidth);
		if (x<(left-margin) || x>(right+margin)) return false;
		if (y<(top-margin) || y>(bottom+margin)) return false;
		if (canvas != null) {
			context = canvas.getContext2d();
			ImageData data = context.getImageData(x-left, y-top, margin*2, margin*2);
			for (int ix=0; ix<margin; ix++) {
				for (int iy=0; iy<margin; iy++) {
					if (data.getRedAt(ix, iy)!=transparentRGBA[0] ||
							data.getGreenAt(ix, iy)!=transparentRGBA[1] ||
							data.getBlueAt(ix, iy)!=transparentRGBA[2] ||
							data.getAlphaAt(ix, iy)!=transparentRGBA[3])
						return true;
				}
			}
		}
		return false;
//		if (fromX == toX) 
//			return (Math.abs(x-fromX)<=MARGIN) && (y>=(top-MARGIN)) && (y<=(bottom+MARGIN));
//		if (fromY == toY)
//			return (Math.abs(y-fromY)<=MARGIN) && (x>=(left-MARGIN)) && (x<=(right+MARGIN));			
//		double ly = (double)(toY - fromY)/(toX - fromX)*(x-fromX) + fromY;
//		return (Math.abs(ly - y) <= MARGIN);	
	}
	
	/**
	 *  연결선의 양 끝인 편집점에 대한 테스트. 
	 * @param x
	 * @param y
	 * @return 시작점에 맞으면 -1, 끝점에 맞으면 1, 안 맞으면 0.
	 */
	public int hitTestEditPoints(int x, int y) {
		if (Math.abs(x-fromX)<=MARGIN && Math.abs(y-fromY)<=MARGIN)
			return -1;
		if (Math.abs(x-toX)<=MARGIN && Math.abs(y-toY)<=MARGIN)
			return 1;
		return 0;
	}

	/**
	 * 선택 표시를 한다. 현재는 끝점에 작은 네모를 그린다.
	 */
	public void select() {
		// 선택되면 레이어를 올리는데 효과는 좀더 살펴봐야 한다.
		panel.getElement().getStyle().setZIndex(1);
		context.fillRect(fromX-MARGIN, fromY-MARGIN, MARGIN*2, MARGIN*2);
		context.fillRect(toX-MARGIN, toY-MARGIN, MARGIN*2, MARGIN*2);		
	}
		
}

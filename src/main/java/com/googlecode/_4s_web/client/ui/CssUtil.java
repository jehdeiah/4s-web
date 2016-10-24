package com.googlecode._4s_web.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Widget;

/**
 * CSS, HTML5 관련 도구들.
 * 
 * @author jehdeiah
 *
 */
public class CssUtil {
	
	/**
	 * 임의의 색깔 만들기. 
	 * @param bias [0,255] 사이값으로 RGB 값을 bias 이상으로 한다. (밝은색 선호)
	 * @return
	 */
	public static String getRandomColor(int bias) {
		int r = Random.nextInt(256 - bias) + bias;
		int g = Random.nextInt(256 - bias) + bias;
		int b = Random.nextInt(256 - bias) + bias;
		return makeRGB(r, g, b);
	}

	/**
	 * 투명도가 들어간 임의 색상 만들기 
	 * @param bias [0,255] 사이값으로 RGB 값을 bias 이상으로 한다. (밝은색 선호)
	 * @param alpha [0,1] 사이값으로 투명도를 지정한다. 
	 * @return
	 */
	public static String getRandomColor(int bias, double alpha) {
		int r = Random.nextInt(256 - bias) + bias;
		int g = Random.nextInt(256 - bias) + bias;
		int b = Random.nextInt(256 - bias) + bias;
		return makeRGBA(r, g, b, alpha);
	}

	/**
	 * HTML 색상 문자열 만들기 
	 * @param r
	 * @param g
	 * @param b
	 * @param alpha
	 * @return
	 */
	public static String makeRGBA(int r, int g, int b, double alpha) {
		return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
	}
	
	public static String makeRGB(int r, int g, int b) {
		return "rgb(" + r + "," + g + "," + b + ")";
	}
	
	
	public static String makeRGBA(String rgbaColor, double alpha) {
		// "rgb(R,G,B)" -> { "rgb", R, G, B }
		// "rgba(R,G,B,A)" -> { "rgba", R, G, B, A }
		rgbaColor = rgbaColor.replaceAll("[ ]", "");
		String[] rgba = rgbaColor.split("[\\(\\),]");
		String color = "rgba(" + rgba[1] + "," + rgba[2] + "," + 
							rgba[3] + "," + alpha + ")";
		return color;
	}
	
	public static String getHexRGB(String rgbaColor) {
		rgbaColor = rgbaColor.replaceAll("[ ]", "");
		String[] rgba = rgbaColor.split("[\\(\\),]");
		int r = Integer.parseInt(rgba[1]); 
		int g = Integer.parseInt(rgba[2]);
		int b = Integer.parseInt(rgba[3]);
		int hex = (r<<16) + (g<<8) + b;
		String hexColor = Integer.toHexString(hex);
		int pos = hexColor.length() - 6;
		return hexColor.substring(pos).toUpperCase();
		
	}
	
	/**
	 * 위젯의 왼쪽 좌표값을 기준이 되는 위젯으로부터 오프셋 좌표값을 누적하여 구한다.
	 * @param widget
	 * @param offset
	 * @return
	 */
	public static int getOffsetLeft(Widget widget, Widget offset) {
		Element e = widget.getElement();
		Element root = offset.getElement();
		int left = e.getOffsetLeft();
		for (Element parent=e.getOffsetParent(); 
				parent!=null && parent!=root; 
				parent=parent.getOffsetParent()) {
			left += parent.getOffsetLeft();
		} 
		return left;
	}
	
	/**
	 * 위젯의 위쪽 좌표값을 기준이 되는 위젯으로부터 오프셋 좌표값을 누적하여 구한다.
	 * @param widget
	 * @param offset
	 * @return
	 */
	public static int getOffsetTop(Widget widget, Widget offset) {
		Element e = widget.getElement();
		Element root = offset.getElement();
		int top = e.getOffsetTop();
		for (Element parent=e.getOffsetParent(); 
				parent!=null && parent!=root; 
				parent=parent.getOffsetParent()) {
			top += parent.getOffsetTop();
		} 
		return top;
	}
}
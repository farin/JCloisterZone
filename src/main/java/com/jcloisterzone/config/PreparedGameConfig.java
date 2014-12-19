package com.jcloisterzone.config;

import java.util.List;

public class PreparedGameConfig {

	private List<String> draw;
	public List<String> getDraw() {
		return draw;
	}
	public void setDraw(List<String> draw) {
		this.draw = draw;
	}
	public int totalSize() {
		int endindex = draw.indexOf(".");
		if (endindex == -1){
			return draw.size();
		}
		return endindex;
	}    	
}

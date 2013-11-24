package com.jcloisterzone.ui.animation;

import java.util.concurrent.DelayQueue;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.GridPanel;


public class AnimationService extends Thread {

	private DelayQueue<Animation> animations = new DelayQueue<Animation>();

	public static final int SLEEP = 200;

	private GridPanel gridPanel;

	public AnimationService() {
		super("AnimationService");
		setDaemon(true);
	}

	public void registerAnimation(Position pos, Animation a) {
		animations.add(a);
		gridPanel.repaint();

	}

	public DelayQueue<Animation> getAnimations() {
		return animations;
	}


	public void clearAll() {
		animations.clear();
	}

	public void run() {
		for (;;) {
			try {
				Animation an = animations.take();
				if (an.switchFrame()) {
					animations.add(an);
				}
				if (gridPanel != null) {
					gridPanel.repaint();
				}
			} catch (InterruptedException e) {
				//empty
			}
		}
	}

	public void setGridPanel(GridPanel gridPanel) {
		this.gridPanel = gridPanel;
	}




}

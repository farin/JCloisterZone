package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.ui.Client;

public class PlayerPanel extends JComponent {
	
	//TODO make private / debel only now
	//public static final AlphaComposite BG_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f);
	private static final Color BG_COLOR = new Color(0, 0, 0, 225);
	private static final Color DELIM_TOP_COLOR = new Color(100,100,100);
	private static final Color DELIM_BOTTOM_COLOR = new Color(160,160,160);
	
	private static Font FONT_POINTS = new Font("Georgia", Font.BOLD, 30);
	private static Font FONT_NICKNAME = new Font("Georgia", Font.BOLD, 18);
	private static Font FONT_MEEPLE = new Font("Georgia", Font.BOLD, 18);
	
	private static final int CORNER_DIAMETER = 20;
	private static final int PADDING_L = 9;
	private static final int PADDING_R = 11;
		
	private final Client client;
	private final Player player;
	private final Color color;
	
	//meeple box supplementary context variables
	private int bx, by;
	
	public PlayerPanel(Client client, Player player) {
		this.client = client;
		this.player = player;
		this.color = client.getPlayerColor(player);
		
		//TODO only devel
		setMinimumSize(new Dimension(1, 140));
	}
	
	private void drawDelimiter(Graphics2D g2, int y) {
		int w = getWidth();
		g2.setColor(DELIM_TOP_COLOR);
		g2.drawLine(PADDING_L, y, w-PADDING_R, y);
		g2.setColor(DELIM_BOTTOM_COLOR);
		g2.drawLine(PADDING_L, y+1, w-PADDING_R, y+1);
	}
	
	private void drawTextShadow(Graphics2D g2, String text, int x, int y) {
		//TODO shadow color based on color  
		g2.setColor(Color.DARK_GRAY);
		g2.drawString(text, x+0.8f, y+0.7f);
		g2.setColor(color);
		g2.drawString(text, x, y);
	}
	
	private void drawMeepleBox(Graphics2D g2, Image img, int count) {
		int w = count > 1 ? 47 : 30;
		int h = 22;
		if (bx+w > getWidth()-PADDING_R) {
			bx = PADDING_L;
			by += 32;
		}		
		g2.setColor(Color.WHITE);
		g2.fillRoundRect(bx, by, w, h, 8, 8);
		img = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		g2.drawImage(img, bx, by-4, null);
		if (count > 1) {			
			g2.setColor(Color.BLACK);
			g2.drawString(""+count, bx+32, by+17);
		}
		bx += w + 8;		
	}
		
	private void drawMeepleBox(Graphics2D g2, Class<? extends Meeple> type, int count) {
		Image img = client.getFigureTheme().getFigureImage(type, color, null);
		drawMeepleBox(g2, img, count);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth(), h = getHeight();
		
		g2.setColor(BG_COLOR);
		g2.fillRoundRect(0, 0, w+CORNER_DIAMETER, h, CORNER_DIAMETER, CORNER_DIAMETER);
		
		drawDelimiter(g2, 34);
							
		g2.setFont(FONT_POINTS);
		drawTextShadow(g2, "123", PADDING_L, 27);
			
		g2.setFont(FONT_NICKNAME);
		drawTextShadow(g2, player.getNick(), 78, 27);
		
		g2.setFont(FONT_MEEPLE);
		bx = PADDING_L;
		by = 43;
		int small = 0;						
		for(Follower f : player.getUndeployedFollowers()) { 
			if (f instanceof SmallFollower) {
				small++;
			} else { //all small followers are at beginning of collection
				if (small > 0) {					
					drawMeepleBox(g2, SmallFollower.class, small);
					small = 0;					
				}
				drawMeepleBox(g2, f.getClass(), 1);				
			}
		}			
		if (small > 0) { //case when only small followers are in collection					 
			drawMeepleBox(g2, SmallFollower.class, small);					
		}
		
		//debug
		drawMeepleBox(g2, BigFollower.class, 1);
		drawMeepleBox(g2, Builder.class, 1);
		drawMeepleBox(g2, Pig.class, 1);
		drawMeepleBox(g2, Mayor.class, 1);
		drawMeepleBox(g2, Wagon.class, 1);
		drawMeepleBox(g2, Barn.class, 1);
		
		drawMeepleBox(g2, client.getFigureTheme().getNeutralImage("towerpiece"), 5);
		
		drawMeepleBox(g2, client.getFigureTheme().getNeutralImage("king"), 1);
		drawMeepleBox(g2, client.getFigureTheme().getNeutralImage("robber"), 1);
		
		drawMeepleBox(g2, client.getFigureTheme().getNeutralImage("bridge"), 3);
		drawMeepleBox(g2, client.getFigureTheme().getNeutralImage("castle"), 3);
	}

}


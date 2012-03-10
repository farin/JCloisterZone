package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;
import static com.jcloisterzone.ui.controls.ControlPanel.BG_COLOR;
import static com.jcloisterzone.ui.controls.ControlPanel.PANEL_WIDTH;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

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
import com.jcloisterzone.ui.FakeComponent;

public class PlayerPanel implements FakeComponent {
	
	//public static final AlphaComposite BG_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f);
	//private static final Color BG_COLOR = new Color(0, 0, 0, 225);
	
	private static final Color DELIM_TOP_COLOR = new Color(100,100,100);
	private static final Color DELIM_BOTTOM_COLOR = new Color(160,160,160);
	
	private static Font FONT_POINTS = new Font("Georgia", Font.BOLD, 30);
	private static Font FONT_NICKNAME = new Font("Georgia", Font.BOLD, 18);
	private static Font FONT_MEEPLE = new Font("Georgia", Font.BOLD, 18);
		
	private static final int PADDING_L = 9;
	private static final int PADDING_R = 11;
	private static final int LINE_HEIGHT = 32;
	private static final int DELIMITER_Y = 34;
		
	private final Client client;
	private final Player player;
	private final Color color;
	
	//context coordinates variables
	private int bx, by;
	
	public PlayerPanel(Client client, Player player) {
		this.client = client;
		this.player = player;
		this.color = client.getPlayerColor(player);
	}
	
	private void drawDelimiter(Graphics2D g2, int y) {
		g2.setColor(DELIM_TOP_COLOR);
		g2.drawLine(PADDING_L, y, PANEL_WIDTH-PADDING_R, y);
		g2.setColor(DELIM_BOTTOM_COLOR);
		g2.drawLine(PADDING_L, y+1, PANEL_WIDTH-PADDING_R, y+1);
	}
	
	private void drawTextShadow(Graphics2D g2, String text, int x, int y) {
		//TODO shadow color based on color ??
		/*g2.setColor(Color.DARK_GRAY);
		g2.drawString(text, x+0.8f, y+0.7f);*/
		g2.setColor(ControlPanel.SHADOW_COLOR);
		g2.drawString(text, x+0.6f, y+0.5f);
		g2.setColor(color);
		g2.drawString(text, x, y);
	}
	
	private void drawMeepleBox(Graphics2D g2, Image img, int count) {
		int w = count > 1 ? 47 : 30;
		int h = 22;
		if (bx+w > PANEL_WIDTH-PADDING_R) {
			bx = PADDING_L;
			by += LINE_HEIGHT;
		}		
		g2.setColor(Color.WHITE);
		g2.fillRoundRect(bx, by, w, h, 8, 8);
		img = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		g2.drawImage(img, bx, by-4, null);
		if (count > 1) {			
			g2.setColor(Color.BLACK);
			g2.drawString(""+count, bx+LINE_HEIGHT, by+17);
		}
		bx += w + 8;		
	}
		
	private void drawMeepleBox(Graphics2D g2, Class<? extends Meeple> type, int count) {
		Image img = client.getFigureTheme().getFigureImage(type, color, null);
		drawMeepleBox(g2, img, count);
	}
	
	@Override
	public void paintComponent(Graphics2D parentGraphics) {	
		
		BufferedImage bimg = new BufferedImage(PANEL_WIDTH, 200, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bimg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);		
		
		drawDelimiter(g2, DELIMITER_Y);
							
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
		
		//debug counts
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
		
		int realHeight = by + (bx > PADDING_L ? LINE_HEIGHT : 0);
		
		parentGraphics.setColor(BG_COLOR);
		parentGraphics.fillRoundRect(0, 0, PANEL_WIDTH+CORNER_DIAMETER, realHeight, CORNER_DIAMETER, CORNER_DIAMETER);
		
		parentGraphics.drawImage(bimg, 0, 0, PANEL_WIDTH, realHeight, 0, 0, PANEL_WIDTH, realHeight, null);
		parentGraphics.translate(0, realHeight + 12); //add also padding 
	}

}


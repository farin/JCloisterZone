package com.jcloisterzone.ui.theme;
import static org.junit.Assert.*;

import java.awt.Color;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;


import org.junit.Test;

public class ThemeTest {
	
Theme myTheme = Theme.LIGHT;
Theme myTheme2 = Theme.DARK;	 	 
	
	@Test
	public void testIsDark(){
		
		myTheme.setUiMangerDefaults();
		assertFalse(myTheme.isDark());
	}

	@Test
	public void testGetMainBg(){
		myTheme.setUiMangerDefaults();
		assertEquals(null, myTheme.getMainBg());
	}

	@Test
	public void testGetPanelBg(){
		myTheme.setUiMangerDefaults();
		assertEquals(null, myTheme.getPanelBg());
	}
	
	@Test
	public void testGetTransparentPanelBg() {
		
		assertEquals(new Color(255, 255, 255, 225), myTheme.getTransparentPanelBg());
	
	}
	
	@Test
	public void testGetSemiTransparentBg() {
		assertEquals(new Color(255, 255, 255, 245), myTheme.getSemiTransparentBg());
	}
	
	
	@Test
	public void testGetPlayerBoxBg() {
	
		assertEquals(new Color(219, 219, 219), myTheme.getPlayerBoxBg());
	}
	
	@Test
	public void testGetAlternativeBg() {
	
		assertEquals(new Color(219, 219, 219), myTheme.getAlternativeBg());
	}
	
	@Test
	public void testGetTileDistCountBg() {
	
		assertEquals(Color.WHITE, myTheme.getTileDistCountBg());
	}
	
	@Test
	public void testGetPanelShadow() {
	
		assertEquals(new Color(255, 255, 255, 158), myTheme.getPanelShadow());
	}
	
	@Test
	public void testGetMarkerColor() {
	
		assertEquals(Color.BLACK, myTheme.getMarkerColor());
	}
	
	@Test
	public void testGetHeaderFontColor() {
	
		assertEquals(new Color(190, 190, 190), myTheme.getHeaderFontColor());
	}
	
	@Test
	public void testGetHintColor() {
	
		assertEquals(Color.DARK_GRAY, myTheme.getHintColor());
	}
	
	@Test
	public void testGetDelimiterBottomColor() {
	
		assertEquals(new Color(220,220,220), myTheme.getDelimiterBottomColor());
	}
	
	@Test
	public void testGetDelimiterTopColor() {
	
		assertEquals(new Color(250,250,250), myTheme.getDelimiterTopColor());
	}
	
	@Test
	public void testGetTileBorder() {
	
		assertEquals(Color.WHITE, myTheme.getTileBorder());
	}
	
	@Test
	public void testGetInputBg() {
	
		assertEquals(Color.WHITE, myTheme.getInputBg());
	}
	
	@Test
	public void testGetTransparentInputBg() {
	
		assertEquals(new Color(255, 255, 255, 8), myTheme.getTransparentInputBg());
	}
	
	@Test
	public void testGetChatMyColor() {
	
		assertEquals(Color.BLUE, myTheme.getChatMyColor());
	}
	
	@Test
	public void testGetTilePlacementColor() {
	
		assertEquals(Color.LIGHT_GRAY, myTheme.getTilePlacementColor());
	}
	
	@Test
	public void testGetChatNeutralColor() {
	
		assertEquals(Color.DARK_GRAY, myTheme.getChatNeutralColor());
	}
	
	@Test
	public void testGetChatSystemColor() {
	
		assertEquals(new Color(0, 140, 0), myTheme.getChatSystemColor());
	}
	
	@Test
	public void testGetTextColor() {	
		assertEquals(null, myTheme.getTextColor());
	}
	
	@Test
	public void testGetFontShadowColor() {	
		assertEquals(new Color(0, 0, 0, 60), myTheme.getFontShadowColor());
	}
	
	
	//Testing the Dark Theme Now
	
	@Test
	public void testIsDark2(){
		
		myTheme2.setUiMangerDefaults();
		assertTrue(myTheme2.isDark());
	}

	@Test
	public void testGetMainBg2(){
		myTheme2.setUiMangerDefaults();
		assertEquals(new Color(40, 44, 52), myTheme2.getMainBg());
	}

	@Test
	public void testGetPanelBg2(){
		myTheme2.setUiMangerDefaults();
		assertEquals(new Color(33, 37, 43), myTheme2.getPanelBg());
	}
	
	@Test
	public void testGetTransparentPanelBg2() {
		
		assertEquals(new Color(33, 37, 43, 220), myTheme2.getTransparentPanelBg());
	
	}
	
	@Test
	public void testGetSemiTransparentBg2() {
		assertEquals(new Color(33, 37, 43, 245), myTheme2.getSemiTransparentBg());
	}
	
	
	@Test
	public void testGetPlayerBoxBg2() {
	
		assertEquals(new Color(70, 70, 70), myTheme2.getPlayerBoxBg());
	}
	
	@Test
	public void testGetAlternativeBg2() {
	
		assertEquals(new Color(10, 11, 13), myTheme2.getAlternativeBg());
	}
	
	@Test
	public void testGetTileDistCountBg2() {
	
		assertEquals(new Color(10, 11, 13), myTheme2.getTileDistCountBg());
	}
	
	@Test
	public void testGetPanelShadow2() {
	
		assertEquals(new Color(33, 37, 43, 150), myTheme2.getPanelShadow());
	}
	
	@Test
	public void testGetMarkerColor2() {
	
		assertEquals(Color.WHITE, myTheme2.getMarkerColor());
	}
	
	@Test
	public void testGetHeaderFontColor2() {
	
		assertEquals(new Color(200, 200, 200), myTheme2.getHeaderFontColor());
	}
	
	@Test
	public void testGetHintColor2() {
	
		assertEquals(new Color(200, 200, 200), myTheme2.getHintColor());
	}
	
	@Test
	public void testGetDelimiterBottomColor2() {
	
		assertEquals(new Color(50,50,50), myTheme2.getDelimiterBottomColor());
	}
	
	@Test
	public void testGetDelimiterTopColor2() {
	
		assertEquals(new Color(0,0,0), myTheme2.getDelimiterTopColor());
	}
	
	@Test
	public void testGetTileBorder2() {
	
		assertEquals(new Color(128, 128, 128), myTheme2.getTileBorder());
	}
	
	@Test
	public void testGetInputBg2() {
	
		assertEquals(new Color(30, 33, 39), myTheme2.getInputBg());
	}
	
	@Test
	public void testGetTransparentInputBg2() {
	
		assertEquals(new Color(30, 33, 39, 245), myTheme2.getTransparentInputBg());
	}
	
	@Test
	public void testGetChatMyColor2() {
	
		assertEquals(new Color(91, 183, 254), myTheme2.getChatMyColor());
	}
	
	@Test
	public void testGetTilePlacementColor2() {
	
		assertEquals(Color.GRAY, myTheme2.getTilePlacementColor());
	}
	
	@Test
	public void testGetChatNeutralColor2() {
	
		assertEquals(Color.WHITE, myTheme2.getChatNeutralColor());
	}
	
	@Test
	public void testGetChatSystemColor2() {
	
		assertEquals(new Color(173, 235, 173), myTheme2.getChatSystemColor());
	}
	
	@Test
	public void testGetTextColor2() {	
		assertEquals(new Color(200, 200, 200), myTheme2.getTextColor());
	}
	
	@Test
	public void testGetFontShadowColor2() {	
		assertEquals(null, myTheme2.getFontShadowColor());
	}
	

}

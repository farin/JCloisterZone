package com.jcloisterzone.ui;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

public class Bootstrap  {

	public static boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac");
	}

	public static void main(String[] args) {
		System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JCloisterZone");

		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	String configFile = System.getProperty("config");
	        	if (configFile == null) {
	        		configFile = "config.ini";
	        	}
	        	Client client = new Client(configFile);

	        	if (isMac()) {
	        		Application macApplication = Application.getApplication();
	        		macApplication.setDockIconImage(new ImageIcon(Client.class.getClassLoader().getResource("sysimages/ico.png")).getImage());
	        		macApplication.addApplicationListener(new MacApplicationAdapter(client));
	        	}

	        	if (client.getConfig().get("debug", "autostart", boolean.class)) {
	        		client.createGame();
	        	}
	        }
	    });
	}

	static class MacApplicationAdapter extends ApplicationAdapter {
		private final Client client;

		public MacApplicationAdapter(Client client) {
			this.client = client;
		}

		@Override
		public void handleAbout(ApplicationEvent ev) {
			ev.setHandled(true);
			client.handleAbout();
		}

		@Override
		public void handleQuit(ApplicationEvent ev) {
			ev.setHandled(true);
			client.handleQuit();
		}
	}
}

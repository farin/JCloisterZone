package com.jcloisterzone.ui.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ini4j.Ini;

import com.jcloisterzone.game.PlayerSlot.SlotType;

public class NameProvider {
	
	private static class ReservedName {
		String name;
		Integer slot;
		
		public ReservedName(String name, Integer slot) {
			this.name = name;
			this.slot = slot;
		}				
	}
	
	private Map<SlotType, List<ReservedName>> namesMap = new HashMap<>();


	public NameProvider(Ini config) {
		initNames(SlotType.PLAYER, config.get("players").getAll("name"));
		initNames(SlotType.AI, config.get("players").getAll("ai_name"));		
	}
	
	private void initNames(SlotType type, List<String> names) {
		List<ReservedName> rn = new ArrayList<>();
		namesMap.put(type, rn);
		if (names != null) {			
			for (String name: names) {
				rn.add(new ReservedName(name, null));
			}						
		}
	}

	synchronized
	public String reserveName(SlotType type, int slot) {		
		for (ReservedName rn : namesMap.get(type)) {
			if (rn.slot == null) {
				rn.slot = slot;
				return rn.name;
			}
		}
		return "";
	}
	
	synchronized
	public void releaseName(SlotType type, int slot) {
		for (ReservedName rn : namesMap.get(type)) {
			if (rn.slot != null && rn.slot == slot) { //autoboxing, must check for null
				rn.slot = null;
				return;
			}
		}
	}	

}

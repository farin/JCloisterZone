package com.jcloisterzone.game;

import java.io.Serializable;

public class PlayerSlot implements Serializable {

	private static final long serialVersionUID = 6093356973595538191L;

	public static final int COUNT = 6;

	public enum SlotType { PLAYER, AI, OPEN }

	private final int number;
	private SlotType type = SlotType.OPEN;
	private String nick;
	private Long owner; //clientId
	private Integer serial; //server assign sequence number when type is occupied
	private String aiClassName;

	public PlayerSlot(int number) {
		this.number = number;
	}

	public PlayerSlot(int number, SlotType type, String nick, Long owner) {
		this.number = number;
		this.type = type;
		this.nick = nick;
		this.owner = owner;
	}

	public int getNumber() {
		return number;
	}
	public SlotType getType() {
		return type;
	}
	public void setType(SlotType type) {
		this.type = type;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public Long getOwner() {
		return owner;
	}
	public void setOwner(Long owner) {
		this.owner = owner;
	}
	public Integer getSerial() {
		return serial;
	}

	public void setSerial(Integer serial) {
		this.serial = serial;
	}

	public boolean isOccupied() {
		return type == SlotType.PLAYER || type == SlotType.AI;
	}

	public String getAiClassName() {
		return aiClassName;
	}

	public void setAiClassName(String aiClassName) {
		this.aiClassName = aiClassName;
	}

	@Override
	public String toString() {
		return number + "-" + type + "[" + nick + "]";
	}

}

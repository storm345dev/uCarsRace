package net.stormdev.urace.players;

public class PlayerQuitException extends Exception {
	private static final long serialVersionUID = -7778042634075648668L;
	private String player = "";

	public PlayerQuitException(String player) {
		this.player = player;
	}

	public String getPlayer() {
		return this.player;
	}
}

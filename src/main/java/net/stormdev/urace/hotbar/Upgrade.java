package net.stormdev.urace.hotbar;


public class Upgrade {

	private Unlockable unlocked = null;
	private int quantity = 1;

	public Upgrade(Unlockable unlocked, int quantity) {
		this.unlocked = unlocked;
		this.quantity = quantity;
	}

	public Unlockable getUnlockedAble() {
		return unlocked;
	}

	public int getQuantity() {
		return quantity;
	}

}

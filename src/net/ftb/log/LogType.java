package net.ftb.log;

public enum LogType {
	DEBUG,
	EXTENDED,
	MINIMAL;

	private static Integer currentPrecedence;
	private int precedence = currentPrecedence();

	public boolean includes(LogType other) {
		return other.precedence >= this.precedence;
	}

	public String toString() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}

	/**
	 * Workaround for limitations on usage of static variables in enum field initialisers.
	 */
	private int currentPrecedence() {
		if (currentPrecedence == null) {
			currentPrecedence = 0;
		}
		return currentPrecedence++;
	}
}

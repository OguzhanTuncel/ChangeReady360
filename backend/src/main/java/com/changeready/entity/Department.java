package com.changeready.entity;

public enum Department {
	EINKAUF("Einkauf"),
	VERTRIEB("Vertrieb"),
	LAGER_LOGISTIK("Lager & Logistik"),
	IT("IT"),
	GESCHAEFTSFUEHRUNG("Geschäftsführung");

	private final String displayName;

	Department(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}


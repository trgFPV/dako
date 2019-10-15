package edu.hm.dako.chat.common;

/**
 * Enumeration zur Definition der Audit-PDU-Typen
 *
 * @author P. Mandl
 */
public enum AuditLogPduType {

	UNDEFINED(0, "Undefined"),
	LOGIN_REQUEST(1, "Login "),
	LOGOUT_REQUEST(2, "Logout"),
	CHAT_MESSAGE_REQUEST(3, "Chat  "),
	FINISH_AUDIT_REQUEST(4, "Finish");

	private final int id;
	private final String description;

	AuditLogPduType(int id, String description) {
		this.id = id;
		this.description = description;
	}

	public static AuditLogPduType getId(int id) {
		for (AuditLogPduType e : values()) {
			if (e.getId() == id) {
				return e;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return description;
	}
}
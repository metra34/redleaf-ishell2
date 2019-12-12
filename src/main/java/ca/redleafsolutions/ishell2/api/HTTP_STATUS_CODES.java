package ca.redleafsolutions.ishell2.api;

public enum HTTP_STATUS_CODES {
	OK (200),
	MOVED (301),
	BAD_REQUEST (400),
	UNAUTHORIZED (401),
	PAYMENT_REQUIRED (402),
	FORBIDDEN (403),
	NOT_FOUND (404),
	INTERNAL_SERVER_ERROR (500),
	NOT_IMPLEMENTED (501),
	BAD_GATEWAY (502),
	SERVICE_UNAVAILABLE (503)
	;

	private int code;

	public int getCode () {
		return code;
	}

	private HTTP_STATUS_CODES (int code) {
		this.code = code;
	}
}

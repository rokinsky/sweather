package pl.edu.mimuw.sweather.event;

import java.time.LocalDateTime;

public final class ErrorEvent extends SWeatherEvent {
	private final LocalDateTime timestamp;
	private final Throwable cause;

	public ErrorEvent(Throwable cause) {
		this.timestamp = LocalDateTime.now();
		this.cause = cause;
	}

	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public Throwable getCause() {
		return this.cause;
	}

	@java.lang.Override
	public java.lang.String toString() {
		return "ErrorEvent(timestamp=" + this.getTimestamp() + ", cause=" + this.getCause() + ")";
	}

}
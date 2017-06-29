package pl.edu.mimuw.sweather.event;

import java.time.LocalDateTime;

public final class RawDataEvent extends SWeatherEvent {
	private final LocalDateTime timestamp;
	private final float value;
	private final boolean isDeg;


	public RawDataEvent(final LocalDateTime timestamp, final float value, final boolean isDeg) {
		this.timestamp = timestamp;
		this.value = value;
		this.isDeg = isDeg;
	}

	public boolean isDeg() {
		return isDeg;
	}

	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public float getValue() {
		return this.value;
	}

	@java.lang.Override
	public java.lang.String toString() {
		return "RawDataEvent(timestamp=" + this.getTimestamp() +
				", value=" + this.getValue() + ", isDeg=" + this.isDeg() + ")";
	}

}

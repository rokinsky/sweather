package pl.edu.mimuw.sweather.event;

public class AtmosphereDataEvent extends DataEvent {
    private final float PM25;
    private final float PM10;

    public AtmosphereDataEvent(float PM25, float PM10) {
        this.PM25 = PM25;
        this.PM10 = PM10;
    }

    public float getPM25() {
        return PM25;
    }

    public float getPM10() {
        return PM10;
    }

    @Override
    public String toString() {
        return "AtmosphereDataEvent(" + "PM25=" + PM25 + ", PM10=" + PM10 + ')';
    }
}

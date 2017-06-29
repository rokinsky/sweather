package pl.edu.mimuw.sweather.event;

public class WeatherDataEvent extends DataEvent {

    private final float temp;
    private final float pressure;
    private final float humidity;
    private final float speed;
    private final float deg;
    private final float clouds;

    public WeatherDataEvent(final float temp, final float pressure, final float humidity,
                            final float speed, final float deg,
                            final float clouds) {
        this.temp = temp;
        this.pressure = pressure;
        this.humidity = humidity;
        this.speed = speed;
        this.deg = deg;
        this.clouds = clouds;
    }


    public float getTemp() {
        return temp;
    }

    public float getPressure() {
        return pressure;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getSpeed() {
        return speed;
    }

    public float getDeg() {
        return deg;
    }

    public float getClouds() {
        return clouds;
    }

    @Override
    public String toString() {
        return "WeatherDataEvent(" + "temp=" + temp + ", pressure=" + pressure +
                                 ", humidity=" + humidity + ", speed=" + speed +
                                 ", deg=" + deg + ", clouds=" + clouds + ')';
    }
}
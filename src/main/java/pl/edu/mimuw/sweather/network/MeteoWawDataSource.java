package pl.edu.mimuw.sweather.network;

import io.reactivex.netty.RxNetty;
import org.jsoup.nodes.Document;
import pl.edu.mimuw.sweather.event.WeatherDataEvent;
import org.jsoup.Jsoup;
import rx.Observable;
import rx.exceptions.Exceptions;

public class MeteoWawDataSource extends DataSource {

    private static final String URL = "http://www.meteo.waw.pl/";

    private String idTemp = "PARAM_0_TA";
    private String idPressure = "PARAM_0_PR";
    private String idHummidity = "PARAM_0_RH";
    private String idSpeed = "PARAM_0_WV";
    private String idDeg = "PARAM_WD";

    private class MeteoWawNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;
    };

    @Override
    protected Observable<WeatherDataEvent> makeRequest() {

        return RxNetty.createHttpRequest(prepareHttpGETRequest(URL)).compose(this::unpackResponse).map(htmlSource -> {
            try {
                Document doc = Jsoup.parse(htmlSource);

                return new WeatherDataEvent(
                        Float.parseFloat(doc.getElementById(idTemp).text().replace(',', '.')),
                        Float.parseFloat(doc.getElementById(idPressure).text().replace(',', '.')),
                        Float.parseFloat(doc.getElementById(idHummidity).text().replace(',', '.')),
                        Float.parseFloat(doc.getElementById(idSpeed).text().replace(',', '.')),
                        Float.parseFloat(doc.getElementById(idDeg).text().replace(',', '.')),
                        Float.NaN);
            } catch (Exception e) {
                throw Exceptions.propagate(new MeteoWawNotFoundException());
            }
        });
    }
}

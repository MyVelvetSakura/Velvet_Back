package com.velvet.sakura.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoLocationServiceImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private GeoLocationServiceImpl geoLocationService;

    @Test
    void resolveLocation_conIpLocal127_devuelveMensajeDeRedLocalSinLlamarALaApi() throws Exception {
        String result = geoLocationService.resolveLocation("127.0.0.1");

        assertThat(result).isEqualTo("Ubicación no disponible (red local)");
        org.mockito.Mockito.verifyNoInteractions(httpClient);
    }

    @Test
    void resolveLocation_conIpNula_devuelveMensajeDeRedLocal() {
        String result = geoLocationService.resolveLocation(null);

        assertThat(result).isEqualTo("Ubicación no disponible (red local)");
    }

    @Test
    void resolveLocation_conIpDeRedPrivada192_devuelveMensajeDeRedLocal() {
        String result = geoLocationService.resolveLocation("192.168.1.10");

        assertThat(result).isEqualTo("Ubicación no disponible (red local)");
    }

    @Test
    void resolveLocation_conIpPublicaYRespuestaExitosa_devuelveCiudadRegionYPais() throws Exception {
        String jsonResponse = "{\"status\":\"success\",\"city\":\"Madrid\",\"regionName\":\"Madrid\",\"country\":\"España\"}";

        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);

        String result = geoLocationService.resolveLocation("203.0.113.1");

        assertThat(result).isEqualTo("Madrid, Madrid, España");
    }

    @Test
    void resolveLocation_conRespuestaDeEstadoFail_devuelveMensajeGenerico() throws Exception {
        String jsonResponse = "{\"status\":\"fail\",\"message\":\"invalid query\"}";

        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);

        String result = geoLocationService.resolveLocation("999.999.999.999");

        assertThat(result).isEqualTo("Ubicación no disponible");
    }

    @Test
    void resolveLocation_cuandoLaLlamadaHttpLanzaExcepcion_devuelveMensajeGenericoSinPropagarError() throws Exception {
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("Timeout de red"));

        String result = geoLocationService.resolveLocation("203.0.113.1");

        assertThat(result).isEqualTo("Ubicación no disponible");
    }
}
package africa.semicolon.controllers;

import africa.semicolon.dtos.requests.UpdateTrackingRequest;
import africa.semicolon.dtos.responses.TrackDeliveryResponse;
import africa.semicolon.exceptions.GlobalExceptionHandler;
import africa.semicolon.exceptions.PackageNotFoundException;
import africa.semicolon.services.TrackingServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TrackingControllerTest {
    private TrackingServices trackingServices;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        trackingServices = mock(TrackingServices.class);
        mockMvc = standaloneSetup(new TrackingController(trackingServices))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void trackDeliveryReturnsTrackingHistory() throws Exception {
        TrackDeliveryResponse response = new TrackDeliveryResponse();
        response.setTrackingNumber("ABC123");
        response.setTrackingHistoryResponses(List.of(
                new TrackDeliveryResponse.TrackingHistoryResponse("Received at sending office", LocalDateTime.now())
        ));
        when(trackingServices.trackDelivery("ABC123")).thenReturn(response);

        mockMvc.perform(get("/api/tracking/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value("ABC123"))
                .andExpect(jsonPath("$.data.trackingHistoryResponses[0].event")
                        .value("Received at sending office"));

        verify(trackingServices).trackDelivery("ABC123");
    }

    @Test
    void updateTrackingAddsHistoryAndReturnsCreated() throws Exception {
        TrackDeliveryResponse response = new TrackDeliveryResponse();
        response.setTrackingNumber("ABC123");
        response.setTrackingHistoryResponses(List.of(
                new TrackDeliveryResponse.TrackingHistoryResponse("Received at sending office", LocalDateTime.now()),
                new TrackDeliveryResponse.TrackingHistoryResponse("Out for delivery", LocalDateTime.now())
        ));
        when(trackingServices.updateTracking(eq("ABC123"), any(UpdateTrackingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tracking/ABC123")
                        .contentType("application/json")
                        .content("{\"details\": \"Out for delivery\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value("ABC123"))
                .andExpect(jsonPath("$.data.trackingHistoryResponses.length()").value(2))
                .andExpect(jsonPath("$.data.trackingHistoryResponses[1].event").value("Out for delivery"));

        var captor = forClass(UpdateTrackingRequest.class);
        verify(trackingServices).updateTracking(eq("ABC123"), captor.capture());
        assertEquals("Out for delivery", captor.getValue().getDetails());
    }

    @Test
    void updateTrackingReturnsNotFoundWhenTrackingNumberDoesNotExist() throws Exception {
        when(trackingServices.updateTracking(eq("MISSING"), any(UpdateTrackingRequest.class)))
                .thenThrow(new PackageNotFoundException("Package with tracking number MISSING not found"));

        mockMvc.perform(post("/api/tracking/MISSING")
                        .contentType("application/json")
                        .content("{\"details\": \"Out for delivery\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Package with tracking number MISSING not found"));
    }

    @Test
    void updateTrackingWithBlankDetailsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tracking/ABC123")
                        .contentType("application/json")
                        .content("{\"details\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Tracking details must not be blank"));

        verifyNoInteractions(trackingServices);
    }

    @Test
    void trackDeliveryReturnsNotFoundWhenTrackingNumberDoesNotExist() throws Exception {
        when(trackingServices.trackDelivery("MISSING"))
                .thenThrow(new PackageNotFoundException("Package with tracking number MISSING not found"));

        mockMvc.perform(get("/api/tracking/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data")
                        .value("Package with tracking number MISSING not found"));
    }
}

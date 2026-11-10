package africa.semicolon.controllers;

import africa.semicolon.dtos.requests.SendDeliveryRequest;
import africa.semicolon.dtos.responses.SendDeliveryResponse;
import africa.semicolon.exceptions.GlobalExceptionHandler;
import africa.semicolon.services.DeliveryServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class DeliveryControllerTest {
    private DeliveryServices deliveryServices;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        deliveryServices = mock(DeliveryServices.class);
        mockMvc = standaloneSetup(new DeliveryController(deliveryServices))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void sendDeliveryWithInvalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/deliveries")
                        .contentType("application/json")
                        .content("""
                                {
                                  "description": "",
                                  "weight": 0,
                                  "sender": "Sender",
                                  "recipient": "Recipient"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryServices);
    }

    @Test
    void sendDeliveryPassesRequestToServiceAndReturnsResponse() throws Exception {
        SendDeliveryResponse response = new SendDeliveryResponse();
        response.setTrackingNumber("ABC123");
        response.setDateSent("Mon, 02-11-2026, 01:00,AM");
        response.setSender("Sender");
        response.setRecipient("Recipient");
        response.setDescription("Books");
        when(deliveryServices.sendDelivery(any(SendDeliveryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/deliveries")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Parcel",
                                  "description": "Books",
                                  "weight": 2.5,
                                  "sender": "Sender",
                                  "recipient": "Recipient"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value("ABC123"))
                .andExpect(jsonPath("$.data.dateSent").value("Mon, 02-11-2026, 01:00,AM"))
                .andExpect(jsonPath("$.data.sender").value("Sender"))
                .andExpect(jsonPath("$.data.recipient").value("Recipient"))
                .andExpect(jsonPath("$.data.description").value("Books"));

        var requestCaptor = forClass(SendDeliveryRequest.class);
        verify(deliveryServices).sendDelivery(requestCaptor.capture());
        assertEquals("Parcel", requestCaptor.getValue().getName());
        assertEquals("Books", requestCaptor.getValue().getDescription());
        assertEquals(2.5, requestCaptor.getValue().getWeight());
        assertEquals("Sender", requestCaptor.getValue().getSender());
        assertEquals("Recipient", requestCaptor.getValue().getRecipient());
    }
}

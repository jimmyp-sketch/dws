package com.dws.challenge.web;

import com.dws.challenge.dto.TransferRequest;
import com.dws.challenge.service.impl.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
    }

    @Test
    void testSuccessfulTransfer() {
        // Prepare the request
        TransferRequest transferRequest = new TransferRequest(1L, 2L, 200.0);

        // Mock successful transfer service
        doNothing().when(transferService).transfer(1L, 2L, 200.0);

        // Call the transfer method on the controller
        ResponseEntity<String> response = transferController.transfer(transferRequest);

        // Verify the transfer service was called with correct arguments
        verify(transferService, times(1)).transfer(1L, 2L, 200.0);

        // Assert response status and message
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transfer successful", response.getBody());
    }

    @Test
    void testTransferWithInvalidAmount() {
        // Prepare the request with invalid amount (e.g. negative)
        TransferRequest transferRequest = new TransferRequest(1L, 2L, -100.0);

        // Mock transfer service throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Transfer amount must be positive"))
                .when(transferService).transfer(1L, 2L, -100.0);

        // Call the transfer method on the controller
        ResponseEntity<String> response = transferController.transfer(transferRequest);

        // Verify the transfer service was called
        verify(transferService, times(1)).transfer(1L, 2L, -100.0);

        // Assert response status and message
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Transfer amount must be positive", response.getBody());
    }

    @Test
    void testTransferWithExceptionHandling() {
        // Prepare the request
        TransferRequest transferRequest = new TransferRequest(1L, 2L, 200.0);

        // Mock transfer service throwing a generic exception
        doThrow(new RuntimeException("Unexpected error")).when(transferService).transfer(1L, 2L, 200.0);

        // Call the transfer method on the controller
        ResponseEntity<String> response = transferController.transfer(transferRequest);

        // Verify the transfer service was called
        verify(transferService, times(1)).transfer(1L, 2L, 200.0);

        // Assert response status and message
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Transfer failed", response.getBody());
    }
}


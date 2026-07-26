package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionControllerTest {

    @Test
    void getTransactions_returnsList() {
        TransactionService service = mock(TransactionService.class);
        List<TransactionResponse> list = Arrays.asList(new TransactionResponse(), new TransactionResponse());
        when(service.getTransactions()).thenReturn(list);

        TransactionController controller = new TransactionController(service);
        ResponseEntity<List<TransactionResponse>> resp = controller.getTransactions();

        assertEquals(200, resp.getStatusCode().value());
        assertSame(list, resp.getBody());
        verify(service).getTransactions();
    }

    @Test
    void getTransaction_returnsItem() {
        TransactionService service = mock(TransactionService.class);
        TransactionResponse tr = new TransactionResponse();
        when(service.getTransactionById(1L)).thenReturn(tr);

        TransactionController controller = new TransactionController(service);
        ResponseEntity<TransactionResponse> resp = controller.getTransaction(1L);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(tr, resp.getBody());
        verify(service).getTransactionById(1L);
    }

    @Test
    void createTransaction_callsServiceAndReturns() {
        TransactionService service = mock(TransactionService.class);
        TransactionRequest req = new TransactionRequest();
        TransactionResponse tr = new TransactionResponse();
        when(service.addTransaction(req)).thenReturn(tr);

        TransactionController controller = new TransactionController(service);
        ResponseEntity<TransactionResponse> resp = controller.createTransaction(req);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(tr, resp.getBody());
        verify(service).addTransaction(req);
    }

    @Test
    void updateTransaction_callsServiceAndReturns() {
        TransactionService service = mock(TransactionService.class);
        TransactionRequest req = new TransactionRequest();
        TransactionResponse tr = new TransactionResponse();
        when(service.updateTransaction(1L, req)).thenReturn(tr);

        TransactionController controller = new TransactionController(service);
        ResponseEntity<TransactionResponse> resp = controller.updateTransaction(1L, req);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(tr, resp.getBody());
        verify(service).updateTransaction(1L, req);
    }

    @Test
    void deleteTransaction_callsServiceAndReturnsNoContent() {
        TransactionService service = mock(TransactionService.class);
        TransactionController controller = new TransactionController(service);

        ResponseEntity<Void> resp = controller.deleteTransaction(1L);
        assertEquals(204, resp.getStatusCode().value());
        verify(service).deleteTransaction(1L);
    }
}

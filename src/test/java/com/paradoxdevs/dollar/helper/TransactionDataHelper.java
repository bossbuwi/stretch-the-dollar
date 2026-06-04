package com.paradoxdevs.dollar.helper;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.entity.Transaction;

public final class TransactionDataHelper {
    public final static Long ID = 1L;
    public final static String NAME = "transaction1";
    public final static String DESCRIPTION = "description";
    public final static String TYPE = "SAVINGS";
    public final static Double AMOUNT = 1.0;
    public final static String CURRENCY = "USD";

    public static TransactionRequest createValidTransactionRequest() {
        return TransactionRequest.builder()
                .transactionName(NAME)
                .description(DESCRIPTION)
                .transactionType(TYPE)
                .amount(AMOUNT)
                .currency(CURRENCY)
                .build();
    }

    public static Transaction createValidTransaction() {
        Transaction out = new Transaction();
        out.setId(ID);
        out.setName(NAME);
        out.setDescription(DESCRIPTION);
        out.setTransactionType(TYPE);
        out.setAmount(AMOUNT);
        out.setCurrency(CURRENCY);
        return out;
    }
}

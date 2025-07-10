package org.example.orderservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ProgrammaticTransactionHandleWithTemplate {

    @Autowired
    TransactionTemplate transactionTemplate;

    public void executeInTransaction() {
        TransactionCallback<Object> transactionCallback = new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // Your transactional code here
                // For example, you can perform database operations
                try {
                    // Simulate some transactional work
                    System.out.println("Performing transactional operations");
                    // If an exception occurs, the transaction will be rolled back
                } catch (Exception e) {
                    // Handle exception and mark transaction for rollback if needed
                    System.out.println("Exception occurred: " + e.getMessage());
                    status.setRollbackOnly();
                }
                System.out.println("Executing transactional code");
                return status.isCompleted(); // Return any result if needed
            }
        };
        transactionTemplate.execute(transactionCallback);
    }
}

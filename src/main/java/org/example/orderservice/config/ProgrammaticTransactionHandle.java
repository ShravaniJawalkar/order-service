package org.example.orderservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class ProgrammaticTransactionHandle {

    @Autowired
    PlatformTransactionManager transactionManager;

    public void beginTransaction() {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        try{
            // Perform transactional operations here
            // If successful, commit the transaction
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            // If an error occurs, roll back the transaction
            transactionManager.rollback(transactionStatus);
            throw e; // Re-throw the exception to handle it further up the call stack
        }
    }
}

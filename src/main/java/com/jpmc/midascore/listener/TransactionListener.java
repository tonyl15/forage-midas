package com.jpmc.midascore.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.jpmc.midascore.foundation.Transaction;

@Component
public class TransactionListener {
    
    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        System.out.println("Received Transaction: " + transaction);
    }
}

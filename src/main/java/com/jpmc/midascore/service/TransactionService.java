package com.jpmc.midascore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.dto.Incentive;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;

@Service
public class TransactionService {
    
    private final TransactionRecordRepository transactionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    
    public TransactionService(TransactionRecordRepository transactionRepository, UserRepository userRepository, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

@Transactional
public void process(Transaction transaction) {
    // Validate sender and recipient exist
    UserRecord sender = userRepository.findById(transaction.getSenderId());
    UserRecord recipient = userRepository.findById(transaction.getRecipientId());

    if (sender == null || recipient == null) {
        return;
    }

    // Validate transaction by checking if sender has sufficient balance
    if (sender.getBalance() < transaction.getAmount()) {
        return;
    }

    float incentive = calculateIncentive(transaction);

    // Update balances
    sender.setBalance(sender.getBalance() - transaction.getAmount());
    recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentive);
    System.out.println("Sender " + sender.getName() + " new balance: " + sender.getBalance());
    System.out.println("Recipient " + recipient.getName() + " new balance: " + recipient.getBalance());

    // Save updated user records
    userRepository.save(sender);
    userRepository.save(recipient);

    // Save transaction record
    TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount(), incentive);
    transactionRepository.save(record);
}

private float calculateIncentive(Transaction transaction) {
    Incentive incentiveResponse = restTemplate.postForObject(
        "http://localhost:8080/incentive",
        transaction, 
        Incentive.class);
    return incentiveResponse != null ? incentiveResponse.getAmount() : 0.0f;
}
}


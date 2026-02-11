package com.jpmc.midascore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;

@Service
public class TransactionService {
    
    private final TransactionRecordRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRecordRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
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

    // Update balances
    sender.setBalance(sender.getBalance() - transaction.getAmount());
    recipient.setBalance(recipient.getBalance() + transaction.getAmount());
    System.out.println("Sender " + sender.getName() + " new balance: " + sender.getBalance());
    System.out.println("Recipient " + recipient.getName() + " new balance: " + recipient.getBalance());

    // Save updated user records
    userRepository.save(sender);
    userRepository.save(recipient);

    // Save transaction record
    TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount());
    transactionRepository.save(record);
}
}


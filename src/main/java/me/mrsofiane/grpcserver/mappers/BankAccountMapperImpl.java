package me.mrsofiane.grpcserver.mappers;

import me.mrsofiane.grpcserver.entities.Account;
import me.mrsofiane.grpcserver.entities.AccountTransaction;
import me.mrsofiane.grpcserver.enums.AccountState;
import me.mrsofiane.grpcserver.enums.AccountType;
import me.mrsofiane.grpcserver.enums.TransactionStatus;
import me.mrsofiane.grpcserver.enums.TransactionType;
import me.mrsofiane.grpcserver.grpc.stub.Bank;
import org.springframework.stereotype.Service;

@Service
public class BankAccountMapperImpl {

    public Bank.BankAccount fromBankAccount(Account account) {
        return Bank.BankAccount.newBuilder()
                .setAccountId(account.getId())
                .setBalance(account.getBalance())
                .setType(Bank.AccountType.valueOf(account.getType().name()))
                .setCreatedAt(account.getCreatedAt())
                .setState(Bank.AccountState.valueOf(account.getState().name()))
                .build();
    }

    public Account fromGrpcAccount(Bank.BankAccount account) {
        return Account.builder()
                .id(account.getAccountId())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .type(AccountType.valueOf(account.getType().name()))
                .state(AccountState.valueOf(account.getState().name()))
                .build();

    }

    public Bank.Transaction fromAccountTransaction(AccountTransaction accountTransaction) {
        return Bank.Transaction.newBuilder()
                .setId(accountTransaction.getId())
                .setAmount(accountTransaction.getAmount())
                .setAccountId(accountTransaction.getAccount().getId())
                .setTimestamp(accountTransaction.getTimestamp())
                .setStatus(Bank.TransactionStatus.valueOf(accountTransaction.getStatus().name()))
                .setType(Bank.TransactionType.valueOf(accountTransaction.getType().name()))
                .build();
    }

    public AccountTransaction fromGrpcTransaction(Bank.Transaction transaction) {
        return AccountTransaction.builder()
                .id(transaction.getId())
                .account(Account.builder().id(transaction.getAccountId()).build())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .status(TransactionStatus.valueOf(transaction.getStatus().name()))
                .type(TransactionType.valueOf(transaction.getType().name()))
                .build();
    }
}

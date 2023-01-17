package me.mrsofiane.grpcserver.grpc.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.mrsofiane.grpcserver.entities.Account;
import me.mrsofiane.grpcserver.entities.AccountTransaction;
import me.mrsofiane.grpcserver.entities.Currency;
import me.mrsofiane.grpcserver.enums.TransactionStatus;
import me.mrsofiane.grpcserver.enums.TransactionType;
import me.mrsofiane.grpcserver.grpc.stub.Bank;
import me.mrsofiane.grpcserver.grpc.stub.BankServiceGrpc;
import me.mrsofiane.grpcserver.mappers.BankAccountMapperImpl;
import me.mrsofiane.grpcserver.repositories.AccountRepository;
import me.mrsofiane.grpcserver.repositories.AccountTransactionRepository;
import me.mrsofiane.grpcserver.repositories.CurrencyRepository;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@GrpcService
public class BankGrpcServiceImpl extends BankServiceGrpc.BankServiceImplBase {

    private final CurrencyRepository currencyRepository;
    private final AccountRepository accountRepository;
    private final BankAccountMapperImpl bankAccountMapper;
    private final AccountTransactionRepository accountTransactionRepository;

    public BankGrpcServiceImpl(CurrencyRepository currencyRepository,
                               AccountRepository accountRepository,
                               BankAccountMapperImpl bankAccountMapper,
                               AccountTransactionRepository accountTransactionRepository) {
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.bankAccountMapper = bankAccountMapper;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Override
    public void getBankAccount(Bank.GetBankAccountRequest request, StreamObserver<Bank.GetBankAccountResponse> responseObserver) {
        String accountId = request.getAccountId().toUpperCase();
        Account account = accountRepository.findById(accountId).orElseThrow();
        Bank.BankAccount bankAccount = bankAccountMapper.fromBankAccount(account);
        Bank.GetBankAccountResponse bankAccountResponse = Bank.GetBankAccountResponse.newBuilder()
                .setBankAccount(bankAccount)
                .build();
        responseObserver.onNext(bankAccountResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getListAccounts(Bank.GetListAccountsRequest request, StreamObserver<Bank.GetListAccountsResponse> responseObserver) {
        List<Account> accounts = accountRepository.findAll();
        List<Bank.BankAccount> grpcBankAccounts = accounts.stream()
                .map(bankAccountMapper::fromBankAccount).collect(Collectors.toList());


        Bank.GetListAccountsResponse getListAccountsResponse = Bank.GetListAccountsResponse.newBuilder()
                .addAllBankAccount(grpcBankAccounts)
                .build();

        responseObserver.onNext(getListAccountsResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void convertCurrency(Bank.ConvertCurrencyRequest request, StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        String from = request.getCurrencyFrom();
        String to = request.getCurrencyTo();
        double amount = request.getAmount();

        Currency currencyFrom = currencyRepository.findByName(from).orElseThrow();
        Currency currencyTo = currencyRepository.findByName(to).orElseThrow();


        double result = amount * currencyTo.getPrice() / currencyFrom.getPrice();
        Bank.ConvertCurrencyResponse convertCurrencyResponse = Bank.ConvertCurrencyResponse.newBuilder()
                .setCurrencyFrom(from)
                .setCurrencyTo(to)
                .setAmount(amount)
                .setConversionResult(result)
                .build();

        responseObserver.onNext(convertCurrencyResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getStreamOfTransactions(Bank.GetStreamOfTransactionsRequest request, StreamObserver<Bank.Transaction> responseObserver) {
        String accountId = request.getAccountId();
        List<AccountTransaction> accountTransactions = accountTransactionRepository.findByAccount_id(accountId);
        if (accountTransactions.isEmpty()) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("No Transaction for this account  => "+accountId)
                            .asException()
            );
            return;
        }

        Stack<Bank.Transaction> transactionStack = new Stack<>();
        transactionStack.addAll(
                accountTransactions.stream()
                        .map(bankAccountMapper::fromAccountTransaction)
                        .collect(Collectors.toList())
        );

        Timer timer = new Timer("Timer");
        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               Bank.Transaction transaction = transactionStack.pop();
                               responseObserver.onNext(transaction);
                               if (transactionStack.empty()) {
                                   responseObserver.onCompleted();
                                   this.cancel();
                               }
                           }
                       },
                0,
                1000);



    }

    @Override
    public StreamObserver<Bank.Transaction> performStreamOfTransactions(StreamObserver<Bank.PerformStreamOfTransactionsResponse> responseObserver) {
        List<AccountTransaction> transactions = new ArrayList<>();


        return new StreamObserver<Bank.Transaction>() {
            @Override
            public void onNext(Bank.Transaction transaction) {

                AccountTransaction accountTransaction = bankAccountMapper.fromGrpcTransaction(transaction);
                Account account = accountRepository.findById(accountTransaction.getAccount().getId()).orElse(null);
                if (account == null) {
                    responseObserver.onError(
                            Status.INTERNAL
                                    .withDescription("Account not found => "+accountTransaction.getAccount().getId())
                                    .asException()
                    );
                }
                accountTransaction.setStatus(TransactionStatus.EXECUTED);
                accountTransactionRepository.save(accountTransaction);
                transactions.add(accountTransaction);
                System.out.println("***** TRANSACTION FROM CLIENT *****");
                System.out.println(accountTransaction.getAmount());
                System.out.println(accountTransaction.getStatus().name());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                double creditTransactionsTotalAmount = transactions.stream()
                        .filter(accountTransaction -> accountTransaction.getType().equals(TransactionType.CREDIT))
                        .map(AccountTransaction::getAmount)
                        .reduce(0.0, Double::sum);

                double debitTransactionTotalAmount = transactions.stream()
                        .filter(accountTransaction -> accountTransaction.getType().equals(TransactionType.DEBIT))
                        .map(AccountTransaction::getAmount)
                        .reduce(0.0, Double::sum);


                Bank.PerformStreamOfTransactionsResponse performStreamOfTransactionsResponse =
                        Bank.PerformStreamOfTransactionsResponse.newBuilder()
                                .setExecutedTransactionsCount(transactions.size())
                                .setTotalCreditTransactionsAmount(creditTransactionsTotalAmount)
                                .setTotalDebitTransactionsAmount(debitTransactionTotalAmount)
                                .setTotalTransactionsAmount(creditTransactionsTotalAmount+debitTransactionTotalAmount)
                                .build();
                System.out.println("***** TRANSACTION END *****");
                responseObserver.onNext(performStreamOfTransactionsResponse);
                responseObserver.onCompleted();

            }

        };
    }

    @Override
    public StreamObserver<Bank.Transaction> executeStreamOfTransactions(StreamObserver<Bank.Transaction> responseObserver) {


        return new StreamObserver<Bank.Transaction>() {
            @Override
            public void onNext(Bank.Transaction transaction) {
                if (transaction.getAmount()>100) {
                    Bank.Transaction performedTransaction = Bank.Transaction.newBuilder(transaction)
                            .setStatus(Bank.TransactionStatus.EXECUTED)
                            .build();
                    responseObserver.onNext(performedTransaction);
                }else {
                    responseObserver.onError(Status.INTERNAL.withDescription("Transaction rejected").asException());
                }

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                responseObserver.onError(Status.INTERNAL.withDescription("Transaction rejected").asException());

            }

            @Override
            public void onCompleted() {
                System.out.println("****** END ******");
                responseObserver.onCompleted();
            }
        };
    }
}

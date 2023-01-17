package me.mrsofiane.grpcserver.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import me.mrsofiane.grpcserver.grpc.stub.Bank;
import me.mrsofiane.grpcserver.grpc.stub.BankServiceGrpc;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GrpcClient {

    public static void main(String[] args) throws IOException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",8888)
                .usePlaintext()
                .build();

        BankServiceGrpc.BankServiceBlockingStub blockingStub = BankServiceGrpc.newBlockingStub(channel);
        BankServiceGrpc.BankServiceStub bankServiceStub = BankServiceGrpc.newStub(channel);
/*

        Bank.ConvertCurrencyRequest convertCurrencyRequest = Bank.ConvertCurrencyRequest.newBuilder()
                .setCurrencyFrom("EUR")
                .setCurrencyTo("DZA")
                .setAmount(500.9)
                .build();
        Bank.ConvertCurrencyResponse convertCurrencyResponse = blockingStub.convertCurrency(convertCurrencyRequest);
        System.out.println("*******************************");
        System.out.println(convertCurrencyResponse.toString());
        System.out.println("*******************************");

        bankServiceStub.convertCurrency(convertCurrencyRequest, new StreamObserver<Bank.ConvertCurrencyResponse>() {
            @Override
            public void onNext(Bank.ConvertCurrencyResponse convertCurrencyResponse) {
                System.out.println("-------------------------------");
                System.out.println(convertCurrencyResponse.toString());
                System.out.println("-------------------------------");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("FINISH");
            }
        });

        Bank.GetStreamOfTransactionsRequest getStreamOfTransactionsRequest = Bank.GetStreamOfTransactionsRequest.newBuilder()
                .setAccountId("CC3")
                .build();
        bankServiceStub.getStreamOfTransactions(getStreamOfTransactionsRequest, new StreamObserver<Bank.Transaction>() {
            @Override
            public void onNext(Bank.Transaction transaction) {
                System.out.println("********* NEW TRANSACTION *********");
                System.out.println(transaction.toString());
                System.out.println("********* END TRANSACTION *********");
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("END");
            }
        });
*/


        // Perform stream from client


        StreamObserver<Bank.Transaction> transactionStreamObserver = bankServiceStub.performStreamOfTransactions(new StreamObserver<Bank.PerformStreamOfTransactionsResponse>() {
            @Override
            public void onNext(Bank.PerformStreamOfTransactionsResponse performStreamOfTransactionsResponse) {

                System.out.println("Executed transactions: "+performStreamOfTransactionsResponse.getExecutedTransactionsCount());
                System.out.println("Total Credit Transactions Amount: "+performStreamOfTransactionsResponse.getTotalCreditTransactionsAmount());
                System.out.println("Total Debit Transactions Amount: "+performStreamOfTransactionsResponse.getTotalDebitTransactionsAmount());
                System.out.println("Total Transactions Amount: "+performStreamOfTransactionsResponse.getTotalCreditTransactionsAmount());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("END");
            }
        });

        Timer timer = new Timer("timer");
        timer.schedule(new TimerTask() {
            int count=0;
            @Override
            public void run() {
                count++;
                Bank.Transaction transaction = Bank.Transaction.newBuilder()
                        .setAccountId("CC5")
                        .setAmount(Math.random() * 484544)
                        .setStatus(Bank.TransactionStatus.PENDING)
                        .setType(Math.random() > 0.5 ? Bank.TransactionType.CREDIT : Bank.TransactionType.DEBIT)
                        .setTimestamp(System.currentTimeMillis())
                        .build();

                transactionStreamObserver.onNext(transaction);

                if(count > 19) {
                    transactionStreamObserver.onCompleted();
                    this.cancel();
                }

            }
        },0,1000);

        System.in.read();


    }
}

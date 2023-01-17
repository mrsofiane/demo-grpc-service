package me.mrsofiane.grpcserver;

import me.mrsofiane.grpcserver.entities.Account;
import me.mrsofiane.grpcserver.entities.AccountTransaction;
import me.mrsofiane.grpcserver.entities.Currency;
import me.mrsofiane.grpcserver.enums.AccountState;
import me.mrsofiane.grpcserver.enums.AccountType;
import me.mrsofiane.grpcserver.enums.TransactionStatus;
import me.mrsofiane.grpcserver.enums.TransactionType;
import me.mrsofiane.grpcserver.repositories.AccountRepository;
import me.mrsofiane.grpcserver.repositories.AccountTransactionRepository;
import me.mrsofiane.grpcserver.repositories.CurrencyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@SpringBootApplication
public class GrpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcServerApplication.class, args);
    }

    @Bean
    CommandLineRunner start(
            CurrencyRepository currencyRepository,
            AccountRepository accountRepository,
            AccountTransactionRepository accountTransactionRepository) {
        return args -> {
            Currency usDollar = Currency.builder().name("USD").symbol("$").price(1).build();
            Currency euro = Currency.builder().name("EUR").symbol("€").price(0.93).build();
            Currency algerianDinar = Currency.builder().name("DZA").symbol("DA").price(136.52).build();
            currencyRepository.saveAll(List.of(usDollar, euro, algerianDinar));

            List<Currency> currencies = currencyRepository.findAll();

            currencies.forEach(currency -> {
                System.out.println(currency.toString());
            });


            for (int i = 0; i < 10; i++) {
                accountRepository.save(Account.builder()
                        .id("CC" + i)
                        .currency(currencies.get(new Random().nextInt(currencies.size())))
                        .createdAt(System.currentTimeMillis())
                        .type(Math.random()>0.5? AccountType.CURRENT_ACCOUNT : AccountType.SAVING_ACCOUNT)
                        .state(AccountState.CREATED)
                        .balance(0)
                        .build());
            }

            accountRepository.findAll().forEach(account ->{
                for (int i=0; i<20; i++) {
                    accountTransactionRepository.save(AccountTransaction.builder()
                            .amount(Math.random()*80000)
                            .timestamp(System.currentTimeMillis())
                            .status(TransactionStatus.PENDING)
                            .type(Math.random()>0.5 ? TransactionType.DEBIT : TransactionType.CREDIT)
                            .account(account)
                            .build());
                }
            } );



        };
    }

}

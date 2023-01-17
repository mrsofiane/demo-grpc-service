package me.mrsofiane.grpcserver.repositories;

import me.mrsofiane.grpcserver.entities.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findByAccount_id(String accountId);
}

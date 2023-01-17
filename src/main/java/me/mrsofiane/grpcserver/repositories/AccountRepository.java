package me.mrsofiane.grpcserver.repositories;

import me.mrsofiane.grpcserver.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

}

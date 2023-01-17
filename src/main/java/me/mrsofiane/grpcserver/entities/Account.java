package me.mrsofiane.grpcserver.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.mrsofiane.grpcserver.enums.AccountState;
import me.mrsofiane.grpcserver.enums.AccountType;

import javax.persistence.*;
import java.util.List;

@Entity
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id
    private String id;
    private double balance;
    private long createdAt;

    @Enumerated(EnumType.STRING)
    private AccountType type;
    @Enumerated(EnumType.STRING)
    private AccountState state;

    @ManyToOne
    private Currency currency;

    @OneToMany(mappedBy = "account")
    private List<AccountTransaction> transactions;


}

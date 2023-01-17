package me.mrsofiane.grpcserver.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.mrsofiane.grpcserver.enums.TransactionStatus;
import me.mrsofiane.grpcserver.enums.TransactionType;

import javax.persistence.*;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class AccountTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long timestamp;
    private double amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne
    private Account account;
}

package com.school.entity;

import com.school.payments.entity.MpesaTransactions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "School_funds",
        uniqueConstraints = @UniqueConstraint(columnNames = {"classes_id", "term_id", "funds_type"}))
@Getter
@Setter
@NoArgsConstructor
public class Funds {
    @Id
    @SequenceGenerator(name = "funds_gen", sequenceName = "funds_gen", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funds_gen")
    private Long fundsId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "classes_id", referencedColumnName = "classesId")
    private SchoolClasses classes;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", referencedColumnName = "termId")
    private Term term;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "funds_type", nullable = false)
    private FundsType fundsType;

    @OneToMany(mappedBy = "funds")
    private List<MpesaTransactions> mpesaTransactions = new ArrayList<>();
}
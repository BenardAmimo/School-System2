package com.school.entity;

import com.school.payments.entity.MpesaTransactions;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "School_funds"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Funds {
    @Id
    @SequenceGenerator(
            name = "funds_gen",
            sequenceName = "funds_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "funds_gen"
    )
    private Long fundsId;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(
            name = "student_id",
            referencedColumnName = "studentId"
    )
    private Student students;

    private LocalDateTime createdAt;
    private BigDecimal amount;
    private FundsType fundsType;
    @OneToMany(
            mappedBy = "funds"
    )
    private List<MpesaTransactions> mpesaTransactions;
    @ManyToOne
    @JoinColumn(
            name = "term_id",
            referencedColumnName = "termId"
    )
    private Term term;



}

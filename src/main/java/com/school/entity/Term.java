package com.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Term {
    @Id
    @SequenceGenerator(
            name = "term_gen",
            sequenceName = "term_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy =GenerationType.SEQUENCE,
            generator = "term_gen"
    )
    private Long termId;
    private String name;
    private String year;
    private LocalDate startDate;
    private LocalDate endDate;
    @OneToMany(
            mappedBy = "term"
    )
    private List<Funds> fundings = new ArrayList<>();
}

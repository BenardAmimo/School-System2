package com.school.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Term {
    @Id
    private Long termId;
    private String name;
    private String year;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @OneToMany(
            mappedBy = "term"
    )
    private List<Funds> fundings = new ArrayList<>();
}

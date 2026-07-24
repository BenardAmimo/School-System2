package com.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportStaff {
    @Id
    @SequenceGenerator(
            name = "staff_gen",
            sequenceName = "staff_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "staff_gen"
    )
    private Long staffId;
    private String firstName;
    private String lastName;
    private String workDone;

}

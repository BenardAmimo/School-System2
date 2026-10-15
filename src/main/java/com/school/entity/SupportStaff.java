package com.school.entity;

import com.school.security.entity.UserReg;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.prefs.PreferencesFactory;

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
    private String workDone;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private Long age;
    private String phoneNumber;
    @OneToOne(

    )
    @JoinColumn(
            name = "userId",
            referencedColumnName = "userId"
    )
    private UserReg userReg;

}

package com.school.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invite_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteCode {

    @Id
    @SequenceGenerator(
            name = "inviteCode_gen",
            sequenceName = "inviteCode_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "inviteCode_gen"
    )
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserReg userReg;

    private boolean used;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;


}


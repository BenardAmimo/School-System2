package com.school.security.entity;

import com.school.entity.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "Users"
)
public class UserReg implements UserDetails {
    @Id
    @SequenceGenerator(
            name = "user_generator",
            sequenceName = "user_generator",
            allocationSize = 1
    )
    @GeneratedValue(
            generator = "user_generator",
            strategy = GenerationType.SEQUENCE

    )
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean enabled;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(
            mappedBy = "userReg"
    )
    private Student student;
    @OneToOne(
            mappedBy = "userReg"
    )
    private Teacher teacher;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

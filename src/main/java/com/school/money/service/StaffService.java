package com.school.money.service;

import com.school.money.Access;
import com.school.money.port.StaffDirectoryPort;
import com.school.money.response.StaffResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {
    private final StaffDirectoryPort directory;

    public StaffService(StaffDirectoryPort directory) { this.directory = directory; }

    public List<StaffResponse> staff(Authentication auth) {
        Access.preparer(auth);
        return directory.staff().stream().map(s -> new StaffResponse(s.username(), s.name(), s.role())).toList();
    }
}
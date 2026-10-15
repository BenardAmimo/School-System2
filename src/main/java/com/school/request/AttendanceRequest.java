package com.school.request;


import java.time.LocalDateTime;

public record AttendanceRequest(
        LocalDateTime attendingTime,
        LocalDateTime checkoutTime,
        Long classId
) {


}

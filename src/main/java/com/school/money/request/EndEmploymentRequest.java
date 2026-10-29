package com.school.money.request;
import java.time.LocalDate;
public record EndEmploymentRequest(String staffUsername, LocalDate lastDay) {}
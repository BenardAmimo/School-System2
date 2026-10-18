package com.school.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveResultsRequest(@NotNull List<@Valid ResultRequest> results) {}

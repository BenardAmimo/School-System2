package com.school.payouts.exception;
import com.school.error.MpesaException;
/** Safaricom definitely did NOT accept the request, or we never sent it. Safe to mark FAILED. */
public class B2cRejectedException extends MpesaException {
    public B2cRejectedException(String message) { super(message); }
}
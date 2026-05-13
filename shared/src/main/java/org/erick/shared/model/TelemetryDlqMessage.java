package org.erick.shared.model;

import java.time.Instant;

public class TelemetryDlqMessage {

    private Instant timestamp;
    private String exceptionClass;
    private String errorMessage;
    private String stackTrace;
    private TelemetryEvent originalMessage;

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public TelemetryEvent getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(TelemetryEvent originalMessage) {
        this.originalMessage = originalMessage;
    }

    @Override
    public String toString() {
        return "TelemetryDlqMessage{" +
                "timestamp=" + timestamp +
                ", exceptionClass='" + exceptionClass + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", originalMessage=" + originalMessage +
                '}';
    }
}

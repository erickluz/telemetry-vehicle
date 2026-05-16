package org.erick.telemetryloadgeneratorservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Percentage distribution of generated events by scenario. Values must add up to 100.")
public class ScenarioDistribution {

    @NotNull
    @PositiveOrZero
    @Schema(description = "Percentage of valid normal telemetry events.", example = "80", minimum = "0")
    private Integer NORMAL;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Percentage of valid telemetry events intended to trigger alerts.", example = "10", minimum = "0")
    private Integer ALERT;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Percentage of events marked with VH-RETRY vehicleId prefix for retryable failure scenarios.", example = "5", minimum = "0")
    private Integer RETRYABLE_ERROR;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Percentage of events marked with VH-DLQ vehicleId prefix for permanent failure scenarios.", example = "5", minimum = "0")
    private Integer DLQ_ERROR;

    public Integer getNORMAL() {
        return NORMAL;
    }

    public void setNORMAL(Integer NORMAL) {
        this.NORMAL = NORMAL;
    }

    public Integer getALERT() {
        return ALERT;
    }

    public void setALERT(Integer ALERT) {
        this.ALERT = ALERT;
    }

    public Integer getRETRYABLE_ERROR() {
        return RETRYABLE_ERROR;
    }

    public void setRETRYABLE_ERROR(Integer RETRYABLE_ERROR) {
        this.RETRYABLE_ERROR = RETRYABLE_ERROR;
    }

    public Integer getDLQ_ERROR() {
        return DLQ_ERROR;
    }

    public void setDLQ_ERROR(Integer DLQ_ERROR) {
        this.DLQ_ERROR = DLQ_ERROR;
    }

    public int percentageFor(LoadScenario scenario) {
        return switch (scenario) {
            case NORMAL -> NORMAL;
            case ALERT -> ALERT;
            case RETRYABLE_ERROR -> RETRYABLE_ERROR;
            case DLQ_ERROR -> DLQ_ERROR;
        };
    }

    public int totalPercentage() {
        return NORMAL + ALERT + RETRYABLE_ERROR + DLQ_ERROR;
    }
}

package org.erick.shared.model;

public enum TelemetryDlqStatus {
    PENDENTE("Pendente"),
    REPROCESSANDO("Reprocessando"),
    REPROCESSADO("Reprocessado"),
    FALHA_NO_REPROCESSAMENTO("Falha no reprocessamento"),
    IGNORADO("Ignorado"),
    ARQUIVADO("Arquivado");

    private final String label;

    TelemetryDlqStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

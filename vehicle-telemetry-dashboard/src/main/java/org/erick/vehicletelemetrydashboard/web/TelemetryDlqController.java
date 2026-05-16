package org.erick.vehicletelemetrydashboard.web;

import java.time.Instant;
import java.util.List;

import org.erick.shared.model.TelemetryDlqStatus;
import org.erick.vehicletelemetrydashboard.model.TelemetryDlqForm;
import org.erick.vehicletelemetrydashboard.model.TelemetryDlqRecordView;
import org.erick.vehicletelemetrydashboard.service.TelemetryDlqClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dlq")
public class TelemetryDlqController {

    private final TelemetryDlqClient telemetryDlqClient;

    public TelemetryDlqController(TelemetryDlqClient telemetryDlqClient) {
        this.telemetryDlqClient = telemetryDlqClient;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "status", required = false) TelemetryDlqStatus status,
            Model model) {
        try {
            model.addAttribute("messages", telemetryDlqClient.findAll(status));
        } catch (RestClientException ex) {
            model.addAttribute("messages", List.<TelemetryDlqRecordView>of());
            model.addAttribute("errorMessage", "Nao foi possivel consultar o telemetry-dlq-service.");
        }
        model.addAttribute("statuses", TelemetryDlqStatus.values());
        model.addAttribute("selectedStatus", status);
        return "dlq/list";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("message", toForm(telemetryDlqClient.findById(id)));
            model.addAttribute("formAction", "/dlq/" + id);
            model.addAttribute("statuses", TelemetryDlqStatus.values());
            return "dlq/form";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mensagem da DLQ nao encontrada: " + id + ".");
            return "redirect:/dlq";
        }
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable("id") Long id,
            @ModelAttribute("message") TelemetryDlqForm message,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/dlq/" + id);
            model.addAttribute("statuses", TelemetryDlqStatus.values());
            model.addAttribute("errorMessage", "Revise os campos informados antes de salvar.");
            return "dlq/form";
        }
        try {
            telemetryDlqClient.update(id, toRecordView(message));
            redirectAttributes.addFlashAttribute("successMessage", "Mensagem da DLQ atualizada com sucesso.");
            return "redirect:/dlq";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("formAction", "/dlq/" + id);
            model.addAttribute("statuses", TelemetryDlqStatus.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "dlq/form";
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nao foi possivel atualizar a mensagem " + id + ".");
            return "redirect:/dlq/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/reprocess")
    public String reprocess(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            telemetryDlqClient.reprocess(id);
            redirectAttributes.addFlashAttribute("successMessage", "Mensagem enviada para reprocessamento.");
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nao foi possivel reprocessar a mensagem " + id + ".");
        }
        return "redirect:/dlq";
    }

    @PostMapping("/{id}/ignore")
    public String ignore(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        return updateStatus(id, TelemetryDlqStatus.IGNORADO, "Mensagem ignorada.", redirectAttributes);
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        return updateStatus(id, TelemetryDlqStatus.ARQUIVADO, "Mensagem arquivada.", redirectAttributes);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            telemetryDlqClient.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Mensagem removida da DLQ.");
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nao foi possivel excluir a mensagem " + id + ".");
        }
        return "redirect:/dlq";
    }

    private String updateStatus(
            Long id,
            TelemetryDlqStatus status,
            String successMessage,
            RedirectAttributes redirectAttributes) {
        try {
            telemetryDlqClient.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nao foi possivel atualizar o status da mensagem " + id + ".");
        }
        return "redirect:/dlq";
    }

    private TelemetryDlqForm toForm(TelemetryDlqRecordView record) {
        TelemetryDlqForm form = new TelemetryDlqForm();
        form.setId(record.getId());
        form.setStatus(record.getStatus() == null ? TelemetryDlqStatus.PENDENTE : record.getStatus());
        form.setDlqTimestamp(formatInstant(record.getDlqTimestamp()));
        form.setExceptionClass(record.getExceptionClass());
        form.setErrorMessage(record.getErrorMessage());
        form.setStackTrace(record.getStackTrace());
        form.setVehicleId(record.getVehicleId());
        form.setOriginalTimestamp(formatInstant(record.getOriginalTimestamp()));
        form.setLatitude(record.getLatitude());
        form.setLongitude(record.getLongitude());
        form.setSpeed(record.getSpeed());
        form.setTemperature(record.getTemperature());
        form.setFuelLevel(record.getFuelLevel());
        form.setReprocessCount(record.getReprocessCount());
        return form;
    }

    private TelemetryDlqRecordView toRecordView(TelemetryDlqForm form) {
        TelemetryDlqRecordView record = new TelemetryDlqRecordView();
        record.setId(form.getId());
        record.setStatus(form.getStatus());
        record.setDlqTimestamp(parseInstant(form.getDlqTimestamp(), "Timestamp da DLQ"));
        record.setExceptionClass(form.getExceptionClass());
        record.setErrorMessage(form.getErrorMessage());
        record.setStackTrace(form.getStackTrace());
        record.setVehicleId(form.getVehicleId());
        record.setOriginalTimestamp(parseInstant(form.getOriginalTimestamp(), "Timestamp original"));
        record.setLatitude(form.getLatitude());
        record.setLongitude(form.getLongitude());
        record.setSpeed(form.getSpeed());
        record.setTemperature(form.getTemperature());
        record.setFuelLevel(form.getFuelLevel());
        record.setReprocessCount(form.getReprocessCount());
        return record;
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private Instant parseInstant(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(fieldName + " deve estar em ISO-8601. Exemplo: 2026-05-13T17:00:00Z.");
        }
    }
}

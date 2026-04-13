package org.erick.vehicletelemetrydashboard.web;

import org.erick.shared.model.Vehicle;
import org.erick.shared.model.VehicleStatus;
import org.erick.vehicletelemetrydashboard.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @ModelAttribute("vehicleStatuses")
    public VehicleStatus[] vehicleStatuses() {
        return VehicleStatus.values();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        return "vehicles/list";
    }

    @GetMapping("/new")
    public String newVehicle(Model model) {
        model.addAttribute("vehicle", vehicleService.buildEmptyVehicle());
        model.addAttribute("formMode", "create");
        model.addAttribute("formAction", "/vehicles");
        return "vehicles/form";
    }

    @GetMapping("/{id}/edit")
    public String editVehicle(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        return vehicleService.findById(id)
                .map(vehicle -> {
                    model.addAttribute("vehicle", vehicle);
                    model.addAttribute("formMode", "edit");
                    model.addAttribute("formAction", "/vehicles/" + vehicle.getId());
                    return "vehicles/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Veiculo nao encontrado.");
                    return "redirect:/vehicles";
                });
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("vehicle") Vehicle vehicle,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            model.addAttribute("formAction", "/vehicles");
            return "vehicles/form";
        }
        if (vehicleService.existsById(vehicle.getId())) {
            bindingResult.rejectValue("id", "duplicate", "Ja existe um veiculo cadastrado com este identificador.");
            model.addAttribute("formMode", "create");
            model.addAttribute("formAction", "/vehicles");
            return "vehicles/form";
        }
        try {
            vehicleService.save(vehicle);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("info.plate", "duplicate", ex.getMessage());
            model.addAttribute("formMode", "create");
            model.addAttribute("formAction", "/vehicles");
            return "vehicles/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Veiculo cadastrado com sucesso.");
        return "redirect:/vehicles";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable String id,
            @Valid @ModelAttribute("vehicle") Vehicle vehicle,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        vehicle.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "edit");
            model.addAttribute("formAction", "/vehicles/" + id);
            return "vehicles/form";
        }
        try {
            vehicleService.save(vehicle);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("info.plate", "duplicate", ex.getMessage());
            model.addAttribute("formMode", "edit");
            model.addAttribute("formAction", "/vehicles/" + id);
            return "vehicles/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Veiculo atualizado com sucesso.");
        return "redirect:/vehicles";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
        vehicleService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Veiculo removido com sucesso.");
        return "redirect:/vehicles";
    }
}

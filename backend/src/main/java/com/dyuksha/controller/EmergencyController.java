package com.dyuksha.controller;

import com.dyuksha.dto.EmergencyForm;
import com.dyuksha.model.Emergency;
import com.dyuksha.model.EmergencyStatus;
import com.dyuksha.model.EmergencyType;
import com.dyuksha.model.Severity;
import com.dyuksha.service.EmergencyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Spring MVC controller for emergency incident management.
 *
 * <p>Routes:
 * <ul>
 *   <li>GET  /emergencies              — list with optional filters
 *   <li>POST /emergencies              — create a new emergency
 *   <li>GET  /emergencies/{id}         — view detail
 *   <li>POST /emergencies/{id}/status  — update status
 * </ul>
 */
@Controller
@RequestMapping("/emergencies")
public class EmergencyController {

    private final EmergencyService service;

    public EmergencyController(EmergencyService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // LIST + FILTER
    // -------------------------------------------------------------------------

    @GetMapping
    public String list(
            @RequestParam(required = false) EmergencyType type,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) EmergencyStatus status,
            Model model) {

        List<Emergency> emergencies = service.filter(type, severity, status);

        populateCommonModel(model);
        model.addAttribute("emergencies",    emergencies);
        model.addAttribute("form",           new EmergencyForm());
        model.addAttribute("types",          EmergencyType.values());
        model.addAttribute("severities",     Severity.values());
        model.addAttribute("statuses",       EmergencyStatus.values());
        model.addAttribute("selectedType",     type);
        model.addAttribute("selectedSeverity", severity);
        model.addAttribute("selectedStatus",   status);
        model.addAttribute("showCreateModal",  false);
        return "emergencies";
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") EmergencyForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            // Re-render form with validation errors — keep modal open
            populateCommonModel(model);
            model.addAttribute("emergencies",   service.getAllEmergencies());
            model.addAttribute("types",         EmergencyType.values());
            model.addAttribute("severities",    Severity.values());
            model.addAttribute("statuses",      EmergencyStatus.values());
            model.addAttribute("selectedType",     null);
            model.addAttribute("selectedSeverity", null);
            model.addAttribute("selectedStatus",   null);
            model.addAttribute("showCreateModal", true);
            return "emergencies";
        }

        Emergency created = service.create(form);
        redirectAttrs.addFlashAttribute("successMessage",
                "Emergency " + created.getFormattedId() + " reported successfully.");
        return "redirect:/emergencies";
    }

    // -------------------------------------------------------------------------
    // DETAIL
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            Emergency emergency = service.getById(id);
            populateCommonModel(model);
            model.addAttribute("emergency", emergency);
            model.addAttribute("statuses",  EmergencyStatus.values());
            model.addAttribute("pageTitle", emergency.getFormattedId() + " — " + emergency.getType().getDisplayName());
            return "emergency-detail";
        } catch (EntityNotFoundException ex) {
            return "redirect:/emergencies";
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE STATUS
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam EmergencyStatus status,
            RedirectAttributes redirectAttrs) {
        try {
            Emergency updated = service.updateStatus(id, status);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Status updated to: " + updated.getStatus().getDisplayName());
        } catch (EntityNotFoundException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", "Emergency not found.");
        }
        return "redirect:/emergencies/" + id;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void populateCommonModel(Model model) {
        model.addAttribute("activeEmergencies", service.countActive());
        model.addAttribute("activePage",        "emergencies");
        model.addAttribute("pageTitle",         "Emergencies");
    }
}


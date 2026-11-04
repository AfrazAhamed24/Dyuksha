package com.dyuksha.controller;

import com.dyuksha.dto.ResourceForm;
import com.dyuksha.model.Emergency;
import com.dyuksha.model.Resource;
import com.dyuksha.model.ResourceStatus;
import com.dyuksha.model.ResourceType;
import com.dyuksha.service.EmergencyService;
import com.dyuksha.service.ResourceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final EmergencyService emergencyService;

    public ResourceController(ResourceService resourceService, EmergencyService emergencyService) {
        this.resourceService = resourceService;
        this.emergencyService = emergencyService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status,
            Model model) {

        List<Resource> resources = resourceService.filter(type, status);

        populateCommonModel(model);
        model.addAttribute("resources", resources);
        model.addAttribute("form", new ResourceForm());
        model.addAttribute("types", ResourceType.values());
        model.addAttribute("statuses", ResourceStatus.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("showCreateModal", false);

        // Summary cards
        model.addAttribute("totalResources", resourceService.countTotal());
        model.addAttribute("availableResources", resourceService.countAvailable());
        model.addAttribute("assignedResources", resourceService.countAssigned());
        model.addAttribute("offlineResources", resourceService.countOffline());

        return "resources";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") ResourceForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            populateCommonModel(model);
            model.addAttribute("resources", resourceService.getAllResources());
            model.addAttribute("types", ResourceType.values());
            model.addAttribute("statuses", ResourceStatus.values());
            model.addAttribute("selectedType", null);
            model.addAttribute("selectedStatus", null);
            model.addAttribute("showCreateModal", true);

            model.addAttribute("totalResources", resourceService.countTotal());
            model.addAttribute("availableResources", resourceService.countAvailable());
            model.addAttribute("assignedResources", resourceService.countAssigned());
            model.addAttribute("offlineResources", resourceService.countOffline());
            return "resources";
        }

        Resource created = resourceService.create(form);
        redirectAttrs.addFlashAttribute("successMessage",
                "Resource " + created.getFormattedId() + " (" + created.getName() + ") registered successfully.");
        return "redirect:/resources";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            Resource resource = resourceService.getById(id);
            populateCommonModel(model);
            model.addAttribute("resource", resource);
            model.addAttribute("statuses", ResourceStatus.values());
            model.addAttribute("pageTitle", resource.getFormattedId() + " — " + resource.getName());

            if (resource.getAssignedEmergencyId() != null) {
                try {
                    Emergency em = emergencyService.getById(resource.getAssignedEmergencyId());
                    model.addAttribute("assignedEmergency", em);
                } catch (EntityNotFoundException ignored) {}
            }

            model.addAttribute("activeEmergenciesList", emergencyService.getAllEmergencies().stream()
                    .filter(Emergency::isActive)
                    .toList());

            return "resource-detail";
        } catch (EntityNotFoundException ex) {
            return "redirect:/resources";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam ResourceStatus status,
            RedirectAttributes redirectAttrs) {
        try {
            Resource updated = resourceService.updateStatus(id, status);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Resource status updated to: " + updated.getStatus().getDisplayName());
        } catch (IllegalStateException | EntityNotFoundException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/resources/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignEmergency(
            @PathVariable Long id,
            @RequestParam Long emergencyId,
            RedirectAttributes redirectAttrs) {
        try {
            resourceService.assignResourceToEmergency(id, emergencyId);
            redirectAttrs.addFlashAttribute("successMessage", "Resource successfully assigned to incident EM-" + emergencyId);
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/resources/" + id;
    }

    @PostMapping("/{id}/complete")
    public String completeAssignment(
            @PathVariable Long id,
            RedirectAttributes redirectAttrs) {
        try {
            resourceService.completeResourceAssignment(id);
            redirectAttrs.addFlashAttribute("successMessage", "Assignment completed. Resource is now Available.");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/resources/" + id;
    }

    private void populateCommonModel(Model model) {
        model.addAttribute("activeEmergencies", emergencyService.countActive());
        model.addAttribute("activePage", "resources");
        model.addAttribute("pageTitle", "Resources");
    }
}


package com.dyuksha.controller;

import com.dyuksha.dto.AllocationPlan;
import com.dyuksha.model.Emergency;
import com.dyuksha.service.AllocationOrchestratorService;
import com.dyuksha.service.EmergencyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller serving the AI Resource Allocation recommendation and approval console.
 */
@Controller
@RequestMapping("/allocation")
public class AllocationController {

    private final EmergencyService emergencyService;
    private final AllocationOrchestratorService allocationOrchestratorService;

    public AllocationController(
            EmergencyService emergencyService,
            AllocationOrchestratorService allocationOrchestratorService) {
        this.emergencyService = emergencyService;
        this.allocationOrchestratorService = allocationOrchestratorService;
    }

    @GetMapping
    public String allocationPage(
            @RequestParam(required = false) Long emergencyId,
            Model model) {

        List<Emergency> activeEmergencies = emergencyService.getAllEmergencies().stream()
                .filter(Emergency::isActive)
                .toList();

        model.addAttribute("activePage", "allocation");
        model.addAttribute("pageTitle", "AI Resource Allocation");
        model.addAttribute("activeEmergencies", emergencyService.countActive());
        model.addAttribute("emergencyList", activeEmergencies);
        model.addAttribute("selectedEmergencyId", emergencyId);

        if (emergencyId != null) {
            try {
                AllocationPlan plan = allocationOrchestratorService.generatePlan(emergencyId);
                model.addAttribute("plan", plan);
                model.addAttribute("selectedEmergency", plan.getEmergency());
            } catch (Exception ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }

        return "allocation";
    }

    @PostMapping("/approve")
    public String approveAssignment(
            @RequestParam Long emergencyId,
            @RequestParam Long resourceId,
            RedirectAttributes redirectAttrs) {
        try {
            allocationOrchestratorService.approveAssignment(emergencyId, resourceId);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Resource successfully dispatched and assigned to incident. Multi-agency telemetry logged.");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/allocation?emergencyId=" + emergencyId;
    }

    @PostMapping("/reject")
    public String rejectRecommendation(
            @RequestParam Long emergencyId,
            @RequestParam Long resourceId,
            RedirectAttributes redirectAttrs) {
        try {
            allocationOrchestratorService.rejectRecommendation(emergencyId, resourceId);
            redirectAttrs.addFlashAttribute("infoMessage",
                    "Recommendation dismissed by operator. Asset remains on standby.");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/allocation?emergencyId=" + emergencyId;
    }
}


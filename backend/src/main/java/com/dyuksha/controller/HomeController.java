package com.dyuksha.controller;

import com.dyuksha.model.ResourceType;
import com.dyuksha.service.EmergencyService;
import com.dyuksha.service.ResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * HomeController — serves the command center views.
 *
 * <p>Phase 4: both emergency and multi-agency resource data are dynamically
 * pulled from the H2 database and injected into the command center.</p>
 */
@Controller
public class HomeController {

    private final EmergencyService emergencyService;
    private final ResourceService resourceService;

    public HomeController(EmergencyService emergencyService, ResourceService resourceService) {
        this.emergencyService = emergencyService;
        this.resourceService = resourceService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        long activeEmergencies = emergencyService.countActive();
        long criticalEmergencies = emergencyService.countCritical();

        long availableAmbulances = resourceService.countAvailableByType(ResourceType.AMBULANCE);
        long availableRescueTeams = resourceService.countAvailableByType(ResourceType.FIRE_AND_RESCUE);
        long availablePoliceUnits = resourceService.countAvailableByType(ResourceType.POLICE_UNIT);

        List<Map<String, Object>> emergencyPoints = emergencyService.getActiveForMap();
        List<Map<String, Object>> resourcePoints = resourceService.getActiveForMap();
        Map<String, List<Map<String, Object>>> groupedResources = resourceService.getGroupedResourcesForDashboard();
        List<Map<String, Object>> recentActivity = emergencyService.getRecentActivity(8);

        model.addAttribute("pageTitle", "Command Center");
        model.addAttribute("activePage", "dashboard");

        // Stat cards
        model.addAttribute("activeEmergencies", activeEmergencies);
        model.addAttribute("criticalEmergencies", criticalEmergencies);
        model.addAttribute("availableAmbulances", availableAmbulances);
        model.addAttribute("availableRescueTeams", availableRescueTeams);
        model.addAttribute("availablePoliceUnits", availablePoliceUnits);

        // Data for JS map and tabs
        model.addAttribute("emergencyMapData", emergencyPoints);
        model.addAttribute("resourceMapData", resourcePoints);
        model.addAttribute("groupedResourcesData", groupedResources);
        model.addAttribute("recentActivityData", recentActivity);

        return "dashboard";
    }

    @GetMapping("/flood")
    public String flood(Model model) {
        populateCommon(model, "Flood Monitoring", "flood");
        return "dashboard";
    }

    @GetMapping("/activity")
    public String activity(Model model) {
        populateCommon(model, "Activity Log", "activity");
        return "dashboard";
    }

    private void populateCommon(Model model, String title, String page) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("activePage", page);
        model.addAttribute("activeEmergencies", emergencyService.countActive());
        model.addAttribute("criticalEmergencies", emergencyService.countCritical());
        model.addAttribute("availableAmbulances", resourceService.countAvailableByType(ResourceType.AMBULANCE));
        model.addAttribute("availableRescueTeams", resourceService.countAvailableByType(ResourceType.FIRE_AND_RESCUE));
        model.addAttribute("availablePoliceUnits", resourceService.countAvailableByType(ResourceType.POLICE_UNIT));
        model.addAttribute("emergencyMapData", emergencyService.getActiveForMap());
        model.addAttribute("resourceMapData", resourceService.getActiveForMap());
        model.addAttribute("groupedResourcesData", resourceService.getGroupedResourcesForDashboard());
        model.addAttribute("recentActivityData", emergencyService.getRecentActivity(8));
    }
}

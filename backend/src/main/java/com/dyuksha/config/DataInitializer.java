package com.dyuksha.config;

import com.dyuksha.model.*;
import com.dyuksha.repository.EmergencyRepository;
import com.dyuksha.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inserts realistic sample emergency incidents and multi-agency resources on first startup.
 * Checks repository count before inserting — prevents duplicates across restarts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmergencyRepository emergencyRepository;
    private final ResourceRepository resourceRepository;

    public DataInitializer(EmergencyRepository emergencyRepository, ResourceRepository resourceRepository) {
        this.emergencyRepository = emergencyRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run(String... args) {
        initEmergencies();
        initResources();
    }

    private void initEmergencies() {
        if (emergencyRepository.count() > 0) {
            log.info("Emergency sample data already present ({} records).", emergencyRepository.count());
            return;
        }

        log.info("Inserting sample emergency data...");

        List<Emergency> samples = List.of(
            buildEmergency("Building Fire — MG Road",
                  EmergencyType.FIRE,
                  "Multi-storey commercial building fire on the 4th floor. Smoke visible from street level. Evacuation in progress.",
                  "MG Road, Sector 7, Bangalore",
                  12.9752, 77.6069,
                  Severity.CRITICAL, EmergencyStatus.RESPONSE_IN_PROGRESS,
                  LocalDateTime.now().minusMinutes(8)),

            buildEmergency("Road Accident — NH-48 Bypass",
                  EmergencyType.ROAD_ACCIDENT,
                  "Two vehicles involved in a head-on collision near the Tumkur Road flyover. At least 3 injured.",
                  "NH-48, Tumkur Bypass, Bangalore",
                  13.0358, 77.5560,
                  Severity.HIGH, EmergencyStatus.RESOURCES_ASSIGNED,
                  LocalDateTime.now().minusMinutes(21)),

            buildEmergency("Cardiac Arrest — Indiranagar",
                  EmergencyType.MEDICAL_EMERGENCY,
                  "60-year-old male unresponsive in a restaurant. Bystander CPR in progress. AED on premises.",
                  "Indiranagar 100ft Road, Bangalore",
                  12.9784, 77.6408,
                  Severity.HIGH, EmergencyStatus.RESPONSE_IN_PROGRESS,
                  LocalDateTime.now().minusMinutes(7)),

            buildEmergency("Flood Warning — Bellandur Lake",
                  EmergencyType.FLOOD,
                  "Rising water levels in Bellandur Lake catchment area. 3 low-lying residential blocks at risk.",
                  "Bellandur Lake Zone, Bangalore",
                  12.9344, 77.6896,
                  Severity.MEDIUM, EmergencyStatus.ASSESSING,
                  LocalDateTime.now().minusHours(1)),

            buildEmergency("Gas Leak — Peenya Industrial Area",
                  EmergencyType.OTHER,
                  "LPG pipeline rupture reported by factory workers. Area evacuated within 500m radius. KSPCB contacted.",
                  "Peenya Industrial Area Phase 2, Bangalore",
                  13.0280, 77.5195,
                  Severity.CRITICAL, EmergencyStatus.RESPONSE_IN_PROGRESS,
                  LocalDateTime.now().minusMinutes(14)),

            buildEmergency("Structure Collapse — Old Town",
                  EmergencyType.BUILDING_COLLAPSE,
                  "Partial collapse of a 3-storey old residential building. Reports of persons trapped. NDRF team requested.",
                  "Old Town District, Bangalore",
                  12.9700, 77.5750,
                  Severity.CRITICAL, EmergencyStatus.REPORTED,
                  LocalDateTime.now().minusMinutes(5)),

            buildEmergency("Forest Fire — Bannerghatta",
                  EmergencyType.FIRE,
                  "Wildfire spreading across approximately 15 acres in Bannerghatta National Park. Wind conditions unfavourable.",
                  "Bannerghatta National Park, Bangalore",
                  12.8018, 77.5758,
                  Severity.HIGH, EmergencyStatus.RESPONSE_IN_PROGRESS,
                  LocalDateTime.now().minusHours(2)),

            buildEmergency("Chemical Spill — Whitefield",
                  EmergencyType.OTHER,
                  "Industrial solvent spill at electronics manufacturing plant. Workers evacuated. No injuries reported.",
                  "Whitefield Industrial Zone, Bangalore",
                  12.9698, 77.7500,
                  Severity.MEDIUM, EmergencyStatus.ASSESSING,
                  LocalDateTime.now().minusMinutes(44)),

            buildEmergency("Mass Casualty — Electronic City",
                  EmergencyType.ROAD_ACCIDENT,
                  "Bus overturned on NICE Road near Electronic City exit. Approx. 20 passengers injured, 2 critical.",
                  "NICE Road, Electronic City Phase 1, Bangalore",
                  12.8445, 77.6632,
                  Severity.CRITICAL, EmergencyStatus.RESOURCES_ASSIGNED,
                  LocalDateTime.now().minusMinutes(32)),

            buildEmergency("Power Line Down — HSR Layout",
                  EmergencyType.OTHER,
                  "High-tension power line downed across a residential street following heavy winds. BESCOM notified.",
                  "HSR Layout Sector 2, Bangalore",
                  12.9116, 77.6473,
                  Severity.LOW, EmergencyStatus.ASSESSING,
                  LocalDateTime.now().minusMinutes(90))
        );

        emergencyRepository.saveAll(samples);
        log.info("Sample emergency data initialised ({} records).", samples.size());
    }

    private void initResources() {
        if (resourceRepository.count() > 0) {
            log.info("Resource sample data already present ({} records).", resourceRepository.count());
            return;
        }

        log.info("Inserting sample resources...");

        List<Resource> resources = List.of(
            // Ambulances
            buildResource("Ambulance Alpha-1", ResourceType.AMBULANCE, "BBMP Emergency", "Rajajinagar Base", 13.0021, 77.5508, ResourceStatus.AVAILABLE, "Advanced Cardiac Life Support, Ventilator", null, null, null),
            buildResource("Ambulance Alpha-2", ResourceType.AMBULANCE, "BBMP Emergency", "MG Road Sector 7", 12.9752, 77.6069, ResourceStatus.ASSIGNED, "Basic Life Support, Patient Transport", 1L, null, null),
            buildResource("Ambulance Beta-1", ResourceType.AMBULANCE, "Karnataka 108", "Jayanagar Trauma Unit", 12.9254, 77.5830, ResourceStatus.AVAILABLE, "Advanced Trauma Response, Paramedic Team", null, null, null),
            buildResource("Ambulance Beta-2", ResourceType.AMBULANCE, "Karnataka 108", "Indiranagar 100ft Rd", 12.9784, 77.6408, ResourceStatus.ASSIGNED, "Advanced Cardiac Support, Defibrillator", 3L, null, null),
            buildResource("Ambulance Gamma-1", ResourceType.AMBULANCE, "Apollo Health", "Banashankari Center", 12.9255, 77.5464, ResourceStatus.AVAILABLE, "Critical Care Mobile ICU", null, null, null),
            buildResource("Ambulance Delta-1", ResourceType.AMBULANCE, "Fortis Healthcare", "Cunningham Rd Clinic", 13.0038, 77.5929, ResourceStatus.AVAILABLE, "Pediatric / Adult ICU transport", null, null, null),
            buildResource("Ambulance Echo-1", ResourceType.AMBULANCE, "Manipal Hospitals", "Malleswaram Branch", 13.0038, 77.5700, ResourceStatus.AVAILABLE, "Rapid EMT Response Unit", null, null, null),
            buildResource("Ambulance Echo-2", ResourceType.AMBULANCE, "Manipal Hospitals", "Central Service Depot", 12.9716, 77.5946, ResourceStatus.OFFLINE, "Scheduled vehicle maintenance", null, null, null),

            // Fire & Rescue
            buildResource("Fire Engine Bravo-1", ResourceType.FIRE_AND_RESCUE, "Karnataka Fire Dept", "Yeshwanthpur Fire Station", 13.0203, 77.5500, ResourceStatus.AVAILABLE, "10,000L Water Cannon, Foam Delivery", null, null, null),
            buildResource("Fire Engine Bravo-2", ResourceType.FIRE_AND_RESCUE, "Karnataka Fire Dept", "MG Road Commercial St", 12.9752, 77.6069, ResourceStatus.ASSIGNED, "Hydraulic Aerial Ladder 45m, Breaker", 1L, null, null),
            buildResource("Rescue Team Charlie-1", ResourceType.FIRE_AND_RESCUE, "NDRF Battalion 7", "Old Town Site", 12.9700, 77.5750, ResourceStatus.ASSIGNED, "Urban Search & Rescue (USAR), Canine Unit", 6L, null, null),
            buildResource("Rescue Team Charlie-2", ResourceType.FIRE_AND_RESCUE, "NDRF Battalion 7", "Basavangudi Outpost", 12.9400, 77.5733, ResourceStatus.AVAILABLE, "Flood Evacuation Boats, Inflatable Rafts", null, null, null),
            buildResource("HazMat Unit Delta", ResourceType.FIRE_AND_RESCUE, "KSPCB Response Team", "Peenya Industrial Area", 13.0280, 77.5195, ResourceStatus.ASSIGNED, "Chemical Containment, Neutralization Kits", 5L, null, null),
            buildResource("Fire Unit Echo-1", ResourceType.FIRE_AND_RESCUE, "Karnataka Fire Dept", "Koramangala Station", 12.9352, 77.6300, ResourceStatus.AVAILABLE, "Multi-gas detectors, Extraction Tools", null, null, null),
            buildResource("Forestry Ranger Squad", ResourceType.FIRE_AND_RESCUE, "Forest Dept Karnataka", "Bannerghatta Park", 12.8018, 77.5758, ResourceStatus.ASSIGNED, "Wildland Firefighting, Drones, Water Backpacks", 7L, null, null),

            // Police
            buildResource("PCR Van Alpha", ResourceType.POLICE_UNIT, "Bangalore City Police", "MG Road Traffic Post", 12.9752, 77.6069, ResourceStatus.AVAILABLE, "Crowd Control, Rapid Perimeter Setup", null, null, null),
            buildResource("PCR Van Beta", ResourceType.POLICE_UNIT, "Bangalore City Police", "Tumkur Road NH-48", 13.0358, 77.5560, ResourceStatus.ASSIGNED, "Accident Investigation, Emergency Cordon", 2L, null, null),
            buildResource("Traffic Interceptor 01", ResourceType.POLICE_UNIT, "Traffic Police Division", "Silk Board Flyover", 12.9174, 77.6220, ResourceStatus.AVAILABLE, "Corridor Clearance, Green Corridor Escort", null, null, null),
            buildResource("Traffic Interceptor 02", ResourceType.POLICE_UNIT, "Traffic Police Division", "Hebbal Junction", 13.0458, 77.5912, ResourceStatus.AVAILABLE, "Highway Divert, Traffic Signal Override", null, null, null),
            buildResource("East Division Response", ResourceType.POLICE_UNIT, "Bangalore East Div", "Whitefield Main Gate", 12.9698, 77.7500, ResourceStatus.AVAILABLE, "Perimeter Security, Incident Escort", null, null, null),

            // Hospitals
            buildResource("Manipal Hospital Old Airport Rd", ResourceType.HOSPITAL, "Manipal Health", "HAL Airport Road", 12.9594, 77.6474, ResourceStatus.AVAILABLE, "Level 1 Trauma Center, Burn Unit, Blood Bank", null, 14, 50),
            buildResource("Victoria Govt Hospital", ResourceType.HOSPITAL, "Government of Karnataka", "City Market Fort", 12.9609, 77.5731, ResourceStatus.BUSY, "Major Emergency Surgery, Mass Casualty Wards", null, 4, 80),
            buildResource("Bowring & Lady Curzon Hospital", ResourceType.HOSPITAL, "Government of Karnataka", "Shivajinagar", 12.9780, 77.5986, ResourceStatus.AVAILABLE, "General Trauma, ICU, Pediatric ER", null, 22, 60),
            buildResource("Apollo Hospital Bannerghatta", ResourceType.HOSPITAL, "Apollo Group", "Bannerghatta Main Rd", 12.9153, 77.5947, ResourceStatus.AVAILABLE, "Neuro ICU, Cardiac Emergency, Heli-pad", null, 18, 45),
            buildResource("Fortis Hospital Cunningham", ResourceType.HOSPITAL, "Fortis Healthcare", "Cunningham Road", 13.0038, 77.5929, ResourceStatus.AVAILABLE, "Specialty Trauma, 24/7 Diagnostics", null, 10, 35)
        );

        resourceRepository.saveAll(resources);
        log.info("Sample resources initialised ({} records).", resources.size());
    }

    private Emergency buildEmergency(String title, EmergencyType type, String description,
                            String location, double lat, double lng,
                            Severity severity, EmergencyStatus status,
                            LocalDateTime reportedAt) {
        Emergency e = new Emergency();
        e.setTitle(title);
        e.setType(type);
        e.setDescription(description);
        e.setLocationName(location);
        e.setLatitude(lat);
        e.setLongitude(lng);
        e.setSeverity(severity);
        e.setStatus(status);
        e.setReportedAt(reportedAt);
        return e;
    }

    private Resource buildResource(String name, ResourceType type, String agency, String location,
                                   double lat, double lng, ResourceStatus status, String capabilities,
                                   Long assignedEmId, Integer availableBeds, Integer totalBeds) {
        Resource r = new Resource();
        r.setName(name);
        r.setType(type);
        r.setAgencyName(agency);
        r.setCurrentLocation(location);
        r.setLatitude(lat);
        r.setLongitude(lng);
        r.setStatus(status);
        r.setCapabilities(capabilities);
        r.setAssignedEmergencyId(assignedEmId);
        r.setAvailableBeds(availableBeds);
        r.setTotalBeds(totalBeds);
        r.updateHospitalCapacityStatus();
        return r;
    }
}


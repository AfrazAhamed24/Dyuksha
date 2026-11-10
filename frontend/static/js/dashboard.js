/**
 * CrisisFusion — Command Center Dashboard
 * Phase 2 Frontend Foundation
 *
 * Responsibilities:
 *  - Leaflet map initialisation with incident + resource markers
 *  - Emergency list rendering and severity filter
 *  - Resource availability tabs and table rendering
 *  - Activity feed rendering
 *  - Sidebar clock
 *  - Responsive sidebar toggle
 */

'use strict';

/* ============================================================
   FALLBACK DATA
   ============================================================ */

/** Fallback emergency incidents if running standalone */
const DEFAULT_EMERGENCIES = [
    {
        id: 'EM-001',
        type: 'Building Fire',
        location: 'MG Road, Sector 7',
        severity: 'critical',
        status: 'active',
        lat: 12.9752,
        lng: 77.6069,
        reported: '8 min ago',
        responders: 4
    },
    {
        id: 'EM-002',
        type: 'Road Accident',
        location: 'NH-48, Tumkur Bypass',
        severity: 'high',
        status: 'responding',
        lat: 13.0358,
        lng: 77.5560,
        reported: '21 min ago',
        responders: 2
    }
];

/** Activity log entries fallback */
const DEFAULT_ACTIVITIES = [
    { severity: 'critical', text: '<strong>EM-006</strong> Structure Collapse reported — 6 units dispatched to Old Town District', time: '2 min ago' },
    { severity: 'high',     text: '<strong>EM-011</strong> Cardiac arrest alert — AMB-07 dispatched to Indiranagar', time: '7 min ago' },
    { severity: 'critical', text: '<strong>EM-001</strong> Fire Unit Bravo-2 arrived on scene at MG Road', time: '8 min ago' },
    { severity: 'critical', text: '<strong>EM-005</strong> Haz-Mat Unit Delta deployed to Peenya gas leak', time: '12 min ago' },
    { severity: 'info',     text: 'System: AI resource allocation engine computed 3 optimal assignments', time: '15 min ago' },
    { severity: 'high',     text: '<strong>EM-002</strong> PCR Van Beta en route to NH-48 accident', time: '19 min ago' },
    { severity: 'system',   text: 'System: Flood sensor telemetry received from 14 stations in Bellandur zone', time: '25 min ago' },
    { severity: 'high',     text: '<strong>EM-003</strong> Status updated — Medical team on scene, patient stabilised', time: '30 min ago' },
    { severity: 'medium',   text: '<strong>EM-010</strong> Search party deployed to Lalbagh — 2 officers', time: '52 min ago' },
    { severity: 'info',     text: 'System: Shift handover completed — Ops Commander assumed control', time: '1 hr ago' },
    { severity: 'low',      text: '<strong>EM-009</strong> BESCOM notified of power line down in HSR Layout', time: '1.5 hr ago' },
    { severity: 'system',   text: 'System: CrisisFusion v0.1.0-SNAPSHOT initialised — All subsystems nominal', time: '2 hr ago' }
];

/* ============================================================
   DATA SOURCE INITIALISATION
   ============================================================ */

// Prefer dynamic DB data injected via window.CRISISFUSION_CONFIG if available
const liveConfig = window.CRISISFUSION_CONFIG || {};

const EMERGENCIES = (Array.isArray(liveConfig.emergencies) && liveConfig.emergencies.length > 0)
    ? liveConfig.emergencies.map(e => ({
        id: e.id,
        type: e.title || e.type,
        location: e.locationName || e.location || 'Unknown',
        severity: (e.severity || 'medium').toLowerCase(),
        status: (e.status || 'reported').toLowerCase().replace('_', '-'),
        lat: Number(e.lat),
        lng: Number(e.lng),
        reported: e.reported || 'Recent',
        responders: e.responders || 0
    }))
    : DEFAULT_EMERGENCIES;

const ACTIVITIES = (Array.isArray(liveConfig.recentActivity) && liveConfig.recentActivity.length > 0)
    ? liveConfig.recentActivity
    : DEFAULT_ACTIVITIES;

/** Field resources */
const DEFAULT_RESOURCES = {
    ambulances: [
        { id: 'AMB-01', name: 'Ambulance Alpha-1',  agency: 'BBMP Emergency',   status: 'available',   location: 'Rajajinagar',       lat: 13.0021, lng: 77.5508 },
        { id: 'AMB-02', name: 'Ambulance Alpha-2',  agency: 'BBMP Emergency',   status: 'deployed',    location: 'MG Road (EM-001)',   lat: 12.9752, lng: 77.6069 }
    ],
    fire: [
        { id: 'FR-01', name: 'Fire Unit Bravo-1',   agency: 'Karnataka Fire',   status: 'available',   location: 'Yeshwanthpur FS',   lat: 13.0203, lng: 77.5500 }
    ],
    police: [
        { id: 'PC-01', name: 'PCR Van Alpha',       agency: 'Bangalore City Police', status: 'available',  location: 'MG Road Beat',      lat: 12.9752, lng: 77.6069 }
    ],
    hospitals: [
        { id: 'HSP-01', name: 'Manipal Hospital',   agency: 'Private',       status: 'available',  location: 'Old Airport Road', lat: 12.9594, lng: 77.6474 }
    ]
};

const RESOURCE_CATEGORIES = ['ambulances', 'fire', 'police', 'hospitals'];

const normalizeResource = resource => ({
    ...resource,
    lat: Number(resource.lat),
    lng: Number(resource.lng)
});

const groupedLiveResources = liveConfig.groupedResources || {};
const RESOURCES = RESOURCE_CATEGORIES.reduce((resources, category) => {
    const liveResources = Array.isArray(groupedLiveResources[category])
        ? groupedLiveResources[category]
        : [];
    resources[category] = liveResources.length > 0
        ? liveResources.map(normalizeResource).filter(resource => Number.isFinite(resource.lat) && Number.isFinite(resource.lng))
        : DEFAULT_RESOURCES[category];
    return resources;
}, {});

/* ============================================================
   MAP INITIALISATION
   ============================================================ */

let map = null;
let emergencyLayerGroup = null;
let resourceLayerGroup = null;

function initMap() {
    const mapEl = document.getElementById('map');
    if (!mapEl || typeof L === 'undefined') return;

    // Center on Bangalore coordinates
    map = L.map('map', {
        center: [12.9716, 77.5946],
        zoom: 11,
        zoomControl: true,
        attributionControl: true
    });

    // Use OpenStreetMap tiles for a clean, real map view
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19
    }).addTo(map);

    emergencyLayerGroup = L.layerGroup().addTo(map);
    resourceLayerGroup  = L.layerGroup().addTo(map);

    plotEmergencies();
    plotResources();
    fitMapToBounds();

    // Trigger multiple resize invalidations to ensure Leaflet renders into full viewport
    setTimeout(() => { if (map) map.invalidateSize(); }, 150);
    setTimeout(() => { if (map) map.invalidateSize(); }, 300);
    setTimeout(() => { if (map) map.invalidateSize(); fitMapToBounds(); }, 600);
}

function fitMapToBounds() {
    if (!map) return;
    const bounds = [];
    if (emergencyLayerGroup) {
        emergencyLayerGroup.eachLayer(layer => {
            if (layer.getLatLng) bounds.push(layer.getLatLng());
        });
    }
    if (resourceLayerGroup) {
        resourceLayerGroup.eachLayer(layer => {
            if (layer.getLatLng) bounds.push(layer.getLatLng());
        });
    }
    if (bounds.length > 0) {
        map.fitBounds(bounds, { padding: [30, 30], maxZoom: 14 });
    }
}

/** Colour map for severity */
const SEVERITY_COLOURS = {
    critical: '#ef4444',
    high:     '#f97316',
    medium:   '#d97706',
    low:      '#22c55e'
};

/** Colour map for resource type */
const RESOURCE_COLOURS = {
    ambulances: '#3b82f6',
    fire:       '#a855f7',
    police:     '#0ea5e9',
    hospitals:  '#14b8a6'
};

/** Create a circular SVG div icon */
function makeCircleIcon(colour, label, size = 28) {
    const html = `<div style="
        width:${size}px;height:${size}px;
        background:${colour}22;
        border:2px solid ${colour};
        border-radius:50%;
        display:flex;align-items:center;justify-content:center;
        font-size:9px;font-weight:700;color:${colour};
        box-shadow:0 2px 8px rgba(0,0,0,0.6);
        font-family:Inter,-apple-system,sans-serif;
    ">${label}</div>`;
    return L.divIcon({ html, className: '', iconSize: [size, size], iconAnchor: [size / 2, size / 2] });
}

/** Plot all emergency markers */
function plotEmergencies() {
    emergencyLayerGroup.clearLayers();
    EMERGENCIES
        .filter(em => Number.isFinite(em.lat) && Number.isFinite(em.lng))
        .forEach(em => {
        const colour = SEVERITY_COLOURS[em.severity] || '#888';
        const abbrev = em.severity === 'critical' ? '!' : em.severity[0].toUpperCase();
        const icon = makeCircleIcon(colour, abbrev, em.severity === 'critical' ? 32 : 28);

        const marker = L.marker([em.lat, em.lng], { icon });

        const popupHTML = `
            <div class="map-popup">
                <div class="map-popup-id">${em.id}</div>
                <div class="map-popup-title">${em.type}</div>
                <div class="map-popup-row">
                    <span>Location</span>
                    <span style="color:var(--text-primary)">${em.location}</span>
                </div>
                <div class="map-popup-row">
                    <span>Severity</span>
                    <span class="badge ${em.severity}">${em.severity}</span>
                </div>
                <div class="map-popup-row">
                    <span>Status</span>
                    <span class="badge status-${em.status}">${em.status}</span>
                </div>
                <div class="map-popup-row">
                    <span>Reported</span>
                    <span style="color:var(--text-primary)">${em.reported}</span>
                </div>
                <div class="map-popup-row">
                    <span>Responders</span>
                    <span style="color:var(--text-primary)">${em.responders} unit(s)</span>
                </div>
            </div>`;

        marker.bindPopup(popupHTML, { maxWidth: 240, minWidth: 200 });
        emergencyLayerGroup.addLayer(marker);
        });
}

/** Plot resource markers (ambulances available + fire + police) */
function plotResources() {
    resourceLayerGroup.clearLayers();

    const plotGroup = (list = [], type) => {
        const colour = RESOURCE_COLOURS[type];
        list
            .filter(r => r.status !== 'maintenance' && Number.isFinite(r.lat) && Number.isFinite(r.lng))
            .forEach(r => {
            const abbrev = type === 'ambulances' ? 'A' :
                           type === 'fire'       ? 'F' :
                           type === 'police'     ? 'P' : 'H';
            const icon = makeCircleIcon(colour, abbrev, 22);
            const marker = L.marker([r.lat, r.lng], { icon });

            const popupHTML = `
                <div class="map-popup">
                    <div class="map-popup-id">${r.id}</div>
                    <div class="map-popup-title">${r.name}</div>
                    <div class="map-popup-row">
                        <span>Agency</span>
                        <span style="color:var(--text-primary)">${r.agency}</span>
                    </div>
                    <div class="map-popup-row">
                        <span>Status</span>
                        <span class="avail-dot ${r.status}">${r.status}</span>
                    </div>
                    <div class="map-popup-row">
                        <span>Location</span>
                        <span style="color:var(--text-primary)">${r.location}</span>
                    </div>
                </div>`;

            marker.bindPopup(popupHTML, { maxWidth: 220, minWidth: 190 });
            resourceLayerGroup.addLayer(marker);
            });
    };

    plotGroup(RESOURCES.ambulances, 'ambulances');
    plotGroup(RESOURCES.fire,       'fire');
    plotGroup(RESOURCES.police,     'police');
}

/** Map layer toggle buttons */
function initMapToggles() {
    if (!map || !emergencyLayerGroup || !resourceLayerGroup) return;

    document.getElementById('mapToggleEmergencies')?.addEventListener('click', function () {
        this.classList.toggle('active');
        if (map.hasLayer(emergencyLayerGroup)) {
            map.removeLayer(emergencyLayerGroup);
        } else {
            map.addLayer(emergencyLayerGroup);
        }
    });

    document.getElementById('mapToggleResources')?.addEventListener('click', function () {
        this.classList.toggle('active');
        if (map.hasLayer(resourceLayerGroup)) {
            map.removeLayer(resourceLayerGroup);
        } else {
            map.addLayer(resourceLayerGroup);
        }
    });
}

/* ============================================================
   EMERGENCY LIST
   ============================================================ */

let currentFilter = 'all';

function renderEmergencyList(filter = 'all') {
    const container = document.getElementById('emergencyList');
    if (!container) return;

    const list = filter === 'all'
        ? EMERGENCIES
        : EMERGENCIES.filter(e => e.severity === filter);

    const countEl = document.getElementById('emg-count');
    if (countEl) countEl.textContent = `Showing ${list.length}`;

    if (list.length === 0) {
        container.innerHTML = '<div class="no-results">No emergencies match this filter.</div>';
        return;
    }

    container.innerHTML = list.map(em => `
        <div class="emg-item" data-id="${em.id}" role="listitem"
             tabindex="0" aria-label="${em.type} - ${em.severity} severity">
            <div class="emg-severity-bar ${em.severity}" aria-hidden="true"></div>
            <div class="emg-info">
                <div class="emg-top">
                    <span class="emg-id">${em.id}</span>
                    <span class="emg-time">${em.reported}</span>
                </div>
                <div class="emg-type">${em.type}</div>
                <div class="emg-meta">
                    <span class="emg-location">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
                            <circle cx="12" cy="10" r="3"/>
                        </svg>
                        ${em.location}
                    </span>
                    <span class="badge ${em.severity}">${em.severity}</span>
                    <span class="badge status-${em.status}">${em.status}</span>
                </div>
            </div>
        </div>
    `).join('');

    // Click on item → fly map to location
    container.querySelectorAll('.emg-item').forEach(el => {
        const handler = () => {
            const id = el.dataset.id;
            const em = EMERGENCIES.find(e => e.id === id);
            if (em && map) {
                map.flyTo([em.lat, em.lng], 14, { duration: 1 });
            }
        };
        el.addEventListener('click', handler);
        el.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') handler(); });
    });
}

function initEmergencyFilter() {
    document.querySelectorAll('.filter-tab').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.filter-tab').forEach(b => {
                b.classList.remove('active-filter');
                b.setAttribute('aria-selected', 'false');
            });
            btn.classList.add('active-filter');
            btn.setAttribute('aria-selected', 'true');
            currentFilter = btn.dataset.filter;
            renderEmergencyList(currentFilter);
        });
    });
}

/* ============================================================
   RESOURCE TABLE
   ============================================================ */

function renderResourceTable(type) {
    const data = RESOURCES[type];
    if (!data) return '';

    const rows = data.map(r => `
        <tr>
            <td class="res-id">${r.id}</td>
            <td class="res-name">${r.name}</td>
            <td class="res-agency">${r.agency}</td>
            <td><span class="avail-dot ${r.status}">${capitalize(r.status)}</span></td>
            <td class="res-agency">${r.location}</td>
        </tr>
    `).join('');

    return `
        <div class="res-table-wrap">
            <table class="res-table" role="table" aria-label="${capitalize(type)} resources">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Unit Name</th>
                        <th>Agency</th>
                        <th>Status</th>
                        <th>Location</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        </div>`;
}

function initResourceTabs() {
    const content = document.getElementById('resContent');
    if (!content) return;

    const tabs = document.querySelectorAll('.res-tab');

    const showTab = (type) => {
        tabs.forEach(t => {
            const isActive = t.dataset.resTab === type;
            t.classList.toggle('active', isActive);
            t.setAttribute('aria-selected', isActive ? 'true' : 'false');
        });
        content.innerHTML = renderResourceTable(type);
    };

    // Initial render
    showTab('ambulances');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => showTab(tab.dataset.resTab));
    });
}

/* ============================================================
   ACTIVITY FEED
   ============================================================ */

function renderActivityFeed() {
    const feed = document.getElementById('activityFeed');
    if (!feed) return;

    feed.innerHTML = ACTIVITIES.map((a, i) => `
        <div class="activity-item">
            <div class="activity-dot-wrap">
                <div class="activity-dot ${a.severity}" aria-hidden="true"></div>
                ${i < ACTIVITIES.length - 1 ? '<div class="activity-line" aria-hidden="true"></div>' : ''}
            </div>
            <div class="activity-content">
                <div class="activity-text">${a.text}</div>
                <div class="activity-time">${a.time}</div>
            </div>
        </div>
    `).join('');
}

/* ============================================================
   CLOCK
   ============================================================ */

function updateClock() {
    const clockEl = document.getElementById('sidebar-clock');
    if (!clockEl) return;
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    clockEl.textContent = `${hh}:${mm}:${ss} IST`;
}

/* ============================================================
   SIDEBAR TOGGLE (responsive)
   ============================================================ */

function initSidebarToggle() {
    const btn     = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('appSidebar');
    const overlay = document.getElementById('sidebarOverlay');

    if (!btn || !sidebar) return;

    const open = () => {
        sidebar.classList.add('open');
        overlay?.classList.add('visible');
        btn.setAttribute('aria-expanded', 'true');
        document.body.style.overflow = 'hidden';
    };

    const close = () => {
        sidebar.classList.remove('open');
        overlay?.classList.remove('visible');
        btn.setAttribute('aria-expanded', 'false');
        document.body.style.overflow = '';
    };

    btn.addEventListener('click', () => {
        sidebar.classList.contains('open') ? close() : open();
    });

    overlay?.addEventListener('click', close);

    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && sidebar.classList.contains('open')) close();
    });
}

/* ============================================================
   UTILITIES
   ============================================================ */

function capitalize(str) {
    return str ? str.charAt(0).toUpperCase() + str.slice(1) : '';
}

/* ============================================================
   BOOT
   ============================================================ */

document.addEventListener('DOMContentLoaded', () => {
    // Map
    initMap();
    initMapToggles();

    // Emergencies
    renderEmergencyList('all');
    initEmergencyFilter();

    // Resources
    initResourceTabs();

    // Activity feed
    renderActivityFeed();

    // Clock
    updateClock();
    setInterval(updateClock, 1000);

    // Sidebar toggle
    initSidebarToggle();

    // Invalidate map size after DOM settles (handles flexbox layout shift)
    setTimeout(() => { if (map) map.invalidateSize(); }, 300);
});

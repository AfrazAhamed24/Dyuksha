/**
 * CrisisFusion — Emergencies Management View Scripts
 */

'use strict';

document.addEventListener('DOMContentLoaded', () => {
    initModal();
    initDetailMap();
});

function initModal() {
    const modalBackdrop = document.getElementById('modalBackdrop');
    const openBtn = document.getElementById('openModalBtn');
    const closeBtn = document.getElementById('closeModalBtn');
    const cancelBtn = document.getElementById('cancelModalBtn');
    const autoTrigger = document.getElementById('autoOpenTrigger');

    if (!modalBackdrop) return;

    const open = () => {
        modalBackdrop.classList.add('open');
        modalBackdrop.setAttribute('aria-hidden', 'false');
        const firstInput = modalBackdrop.querySelector('input, select');
        if (firstInput) firstInput.focus();
    };

    const close = () => {
        modalBackdrop.classList.remove('open');
        modalBackdrop.setAttribute('aria-hidden', 'true');
    };

    if (openBtn) openBtn.addEventListener('click', open);
    if (closeBtn) closeBtn.addEventListener('click', close);
    if (cancelBtn) cancelBtn.addEventListener('click', close);

    modalBackdrop.addEventListener('click', (e) => {
        if (e.target === modalBackdrop) close();
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modalBackdrop.classList.contains('open')) {
            close();
        }
    });

    // Auto-open if form had validation errors
    if (autoTrigger && autoTrigger.dataset.open === 'true') {
        open();
    }
}

function initDetailMap() {
    const mapEl = document.getElementById('detailMap');
    if (!mapEl || typeof L === 'undefined') return;

    const lat = parseFloat(mapEl.dataset.lat);
    const lng = parseFloat(mapEl.dataset.lng);
    const title = mapEl.dataset.title || 'Incident';
    const severity = mapEl.dataset.severity || 'critical';

    if (isNaN(lat) || isNaN(lng)) return;

    const detailMap = L.map('detailMap', {
        center: [lat, lng],
        zoom: 14,
        zoomControl: true,
        attributionControl: false
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        subdomains: 'abcd',
        maxZoom: 19
    }).addTo(detailMap);

    const severityColours = {
        critical: '#ef4444',
        high:     '#f97316',
        medium:   '#d97706',
        low:      '#22c55e'
    };

    const color = severityColours[severity] || '#ef4444';
    const html = `<div style="
        width:28px;height:28px;
        background:${color}22;
        border:2px solid ${color};
        border-radius:50%;
        display:flex;align-items:center;justify-content:center;
        font-size:11px;font-weight:700;color:${color};
        box-shadow:0 2px 8px rgba(0,0,0,0.6);
        font-family:Inter,-apple-system,sans-serif;
    ">!</div>`;

    const icon = L.divIcon({ html, className: '', iconSize: [28, 28], iconAnchor: [14, 14] });
    L.marker([lat, lng], { icon }).addTo(detailMap).bindPopup(`<b>${title}</b>`).openPopup();

    setTimeout(() => {
        detailMap.invalidateSize();
    }, 200);
}


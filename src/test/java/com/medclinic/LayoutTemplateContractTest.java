package com.medclinic;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutTemplateContractTest {
    private static final Path TEMPLATE_ROOT = Path.of("src/main/resources/templates");

    @Test
    void publicLayoutProvidesEarlyCsrfAndKeyboardNavigationContracts() throws IOException {
        String main = read("layout/main.html");

        assertTrue(main.contains("name=\"_csrf\""));
        assertTrue(main.contains("th:content=\"${_csrf.token}\""));
        assertTrue(main.contains("class=\"skip-link\" href=\"#main-content\""));
        assertTrue(main.contains("aria-controls=\"navbarMain\""));
        assertTrue(main.contains("aria-expanded=\"false\""));
        assertTrue(main.contains(":focus-visible"));
        assertTrue(main.contains("prefers-reduced-motion"));
        assertTrue(main.contains("form.dataset.submitting"));
        assertBalancedScriptTags(main);
    }

    @Test
    void portalLayoutProvidesAccessibleMobileSidebarContract() throws IOException {
        String dashboard = read("layout/dashboard.html");

        assertTrue(dashboard.contains("id=\"sidebarToggle\""));
        assertTrue(dashboard.contains("aria-controls=\"sidebar-wrapper\""));
        assertTrue(dashboard.contains("id=\"sidebarOverlay\""));
        assertTrue(dashboard.contains("function setSidebar(open)"));
        assertTrue(dashboard.contains("event.key === 'Escape'"));
        assertTrue(dashboard.contains("firstSidebarControl.focus()"));
        assertTrue(dashboard.contains("toggle.focus()"));
        assertTrue(dashboard.contains("tabindex=\"-1\""));
        assertTrue(dashboard.contains("@media (max-width: 767.98px)"));
        assertTrue(dashboard.contains(".table-responsive"));
        assertTrue(dashboard.contains("class=\"skip-link\" href=\"#main-content\""));
        assertBalancedScriptTags(dashboard);

        String reception = read("staff/reception.html");
        assertTrue(reception.contains("<label for=\"receptionSearchInput\""));
    }

    @Test
    void dynamicPortalStatesExposeAccessibleAnnouncementsAndDialogNames() throws IOException {
        String booking = read("patient/booking.html");
        assertTrue(booking.contains("id=\"slotSelectionStatus\""));
        assertTrue(booking.contains("aria-live=\"polite\""));
        assertTrue(booking.contains("role=\"alert\""));
        assertTrue(booking.contains("setAttribute('aria-pressed', 'true')"));

        String reception = read("staff/reception.html");
        assertTrue(reception.contains("aria-labelledby=\"qrScannerModalTitle\""));
        assertTrue(reception.contains("aria-labelledby=\"ticketModalTitle\""));
        assertTrue(reception.contains("class=\"btn btn-sm btn-outline-primary print-ticket-btn\""));
        assertFalse(reception.contains("th:onclick="));
        assertFalse(reception.contains("onclick="));

        String doctor = read("doctor/desk.html");
        assertTrue(doctor.contains("id=\"refreshDoctorQueueBtn\""));
        assertFalse(doctor.contains("onclick="));

        String billing = read("staff/billing.html");
        assertTrue(billing.contains("aria-labelledby=${'payModalTitle-' + inv.id}"));
        assertTrue(billing.contains("th:for=\"${'paymentMethod-' + inv.id}\""));
    }

    @Test
    void adminCrudDialogsExposeProgrammaticNamesAndFormLabels() throws IOException {
        String doctors = read("admin/doctors.html");
        assertTrue(doctors.contains("aria-labelledby=\"addDoctorModalTitle\""));
        assertTrue(doctors.contains("for=\"doctorUsername\""));
        assertTrue(doctors.contains("id=\"doctorUsername\""));
        assertTrue(doctors.contains("aria-labelledby=${'editDoctorModalTitle-' + doc.id}"));
        assertTrue(doctors.contains("th:for=\"${'editDoctorSpecialty-' + doc.id}\""));

        String specialties = read("admin/specialties.html");
        assertTrue(specialties.contains("aria-labelledby=\"addSpecialtyModalTitle\""));
        assertTrue(specialties.contains("aria-labelledby=${'editSpecialtyModalTitle-' + spec.id}"));
        assertTrue(specialties.contains("th:for=\"${'editSpecialtyName-' + spec.id}\""));

        String medicines = read("admin/medicines.html");
        assertTrue(medicines.contains("aria-labelledby=\"addMedicineModalTitle\""));
        assertTrue(medicines.contains("aria-labelledby=${'editMedicineModalTitle-' + med.id}"));
        assertTrue(medicines.contains("th:for=\"${'editMedicineName-' + med.id}\""));
    }

    @Test
    void repairedTemplatesNoLongerUseInlineClickHandlers() throws IOException {
        for (String template : new String[] {
                "auth/login.html", "patient/appointment-detail.html", "staff/billing.html",
                "staff/reception.html", "doctor/desk.html"}) {
            String html = read(template);
            assertFalse(html.contains("onclick="), template + " must use an event listener");
            assertBalancedScriptTags(html);
        }
    }

    @Test
    void publicAuthFormsExposeLabelAssociationsAndSemanticHeadings() throws IOException {
        String login = read("auth/login.html");
        String register = read("auth/register.html");
        String home = read("home/index.html");
        String main = read("layout/main.html");

        assertTrue(login.contains("<h1 class=\"h3"));
        assertTrue(login.contains("label for=\"usernameInput\""));
        assertTrue(login.contains("label for=\"passwordInput\""));
        for (String id : new String[]{"fullName", "username", "password", "confirmPassword", "email", "phone", "gender", "dateOfBirth", "address"}) {
            assertTrue(register.contains("for=\"" + id + "\""), "missing label association for " + id);
        }
        assertTrue(register.contains("<h1 class=\"h3"));
        assertTrue(home.contains("<h2 class=\"h3 fw-bold text-dark\">"));
        assertTrue(home.contains("<h3 class=\"h6 fw-bold mb-2\">"));
        assertTrue(main.contains("<h2 class=\"h6 text-white"));
    }

    @Test
    void doctorDeskSubscribesToAuthenticatedRealtimeQueue() throws IOException {
        String desk = read("doctor/desk.html");
        assertTrue(desk.contains("sockjs-client@1.6.1"));
        assertTrue(desk.contains("@stomp/stompjs@7.2.1"));
        assertTrue(desk.contains("/user/queue/doctor"));
        assertTrue(desk.contains("doctorQueueRealtimeStatus"));
        assertBalancedScriptTags(desk);
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(TEMPLATE_ROOT.resolve(relativePath));
    }

    private void assertBalancedScriptTags(String html) {
        assertEquals(count(html, "<script\\b"), count(html, "</script>"));
    }

    private int count(String source, String expression) {
        return (int) Pattern.compile(expression).matcher(source).results().count();
    }
}

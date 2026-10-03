package com.medclinic;

import com.medclinic.entity.InventoryTransaction;
import com.medclinic.entity.Medicine;
import com.medclinic.entity.User;
import com.medclinic.enums.InventoryTransactionType;
import com.medclinic.repository.InventoryTransactionRepository;
import com.medclinic.repository.MedicineRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class InventoryManagementIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private MedicineRepository medicines;
    @Autowired private InventoryTransactionRepository inventoryTransactions;
    @Autowired private UserRepository users;

    @Test
    void adminCanRestockMedicineAndAuditTransactionIsRendered() throws Exception {
        User admin = users.findByUsername("admin").orElseThrow();
        Medicine medicine = medicines.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .findFirst().orElseThrow();
        medicine.setStockQuantity(10);
        medicine.setReorderLevel(20);
        medicines.saveAndFlush(medicine);

        mockMvc.perform(post("/admin/medicines/{id}/restock", medicine.getId())
                        .with(user(new CustomUserDetails(admin))).with(csrf())
                        .param("quantity", "15")
                        .param("unitCost", "800")
                        .param("note", "Phiếu nhập test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/medicines"));

        Medicine reloaded = medicines.findById(medicine.getId()).orElseThrow();
        assertEquals(25, reloaded.getStockQuantity());

        InventoryTransaction tx = inventoryTransactions.findAll().stream()
                .filter(candidate -> candidate.getMedicine().getId().equals(medicine.getId()))
                .filter(candidate -> candidate.getType() == InventoryTransactionType.RESTOCK)
                .reduce((first, second) -> second).orElseThrow();
        assertEquals(15, tx.getQuantityChange());
        assertEquals(10, tx.getStockBefore());
        assertEquals(25, tx.getStockAfter());
        assertEquals(0, tx.getUnitCost().compareTo(new BigDecimal("800")));
        assertEquals(admin.getId(), tx.getPerformedBy().getId());

        mockMvc.perform(get("/admin/medicines")
                        .with(user(new CustomUserDetails(admin))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lịch sử giao dịch kho gần nhất")))
                .andExpect(content().string(containsString("Phiếu nhập test")))
                .andExpect(content().string(containsString("Nhập kho")));
    }
}

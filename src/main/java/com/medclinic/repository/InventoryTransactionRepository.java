package com.medclinic.repository;

import com.medclinic.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    @EntityGraph(attributePaths = {"medicine", "performedBy"})
    List<InventoryTransaction> findTop20ByOrderByCreatedAtDesc();
}

package com.paradoxdevs.dollar.repository.view;

import com.paradoxdevs.dollar.entity.view.TransactionCategoryDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCategoryDetailsView extends JpaRepository<TransactionCategoryDetails, Long> {
}

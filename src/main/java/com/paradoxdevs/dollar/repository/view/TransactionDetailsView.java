package com.paradoxdevs.dollar.repository.view;

import com.paradoxdevs.dollar.entity.view.TransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionDetailsView extends JpaRepository<TransactionDetails, Long> {
}

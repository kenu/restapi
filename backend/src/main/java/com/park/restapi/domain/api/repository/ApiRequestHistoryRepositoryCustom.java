package com.park.restapi.domain.api.repository;

import com.park.restapi.domain.api.dto.response.RequestHistoryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApiRequestHistoryRepositoryCustom {
    Page<RequestHistoryResponseDTO> searchApiRequestHistory(Pageable pageable);
}

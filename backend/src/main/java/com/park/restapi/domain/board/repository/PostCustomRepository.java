package com.park.restapi.domain.board.repository;

import com.park.restapi.domain.board.dto.response.ApiRecommendPostsResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCustomRepository {

    Page<ApiRecommendPostsResponseDTO> findRecommendPosts(Pageable pageable, String searchType, String searchKey, String sortBy);
}

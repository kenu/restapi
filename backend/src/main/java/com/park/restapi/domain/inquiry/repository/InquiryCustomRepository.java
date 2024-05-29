package com.park.restapi.domain.inquiry.repository;

import com.park.restapi.domain.inquiry.entity.Inquiry;
import com.park.restapi.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryCustomRepository {
    Page<Inquiry> findByInquires(Member member, Pageable pageable, boolean isAdmin);
}
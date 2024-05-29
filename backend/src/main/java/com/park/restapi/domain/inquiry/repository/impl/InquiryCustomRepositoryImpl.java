package com.park.restapi.domain.inquiry.repository.impl;

import com.park.restapi.domain.inquiry.entity.Inquiry;
import com.park.restapi.domain.inquiry.repository.InquiryCustomRepository;
import com.park.restapi.domain.member.entity.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.park.restapi.domain.inquiry.entity.QInquiry.inquiry;
import static com.park.restapi.domain.member.entity.QMember.member;

@RequiredArgsConstructor
@Repository
public class InquiryCustomRepositoryImpl implements InquiryCustomRepository {

    private final JPAQueryFactory queryFactory;

    // 모든 문의 내역 탐색
    @Override
    public Page<Inquiry> findByInquires(Member member, Pageable pageable, boolean isAdmin) {

        List<Inquiry> inquiries = queryFactory
                .selectFrom(inquiry)
                .where(allInquiresCondition(member, isAdmin))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(inquiry.createDate.desc())
                .fetch();

        long total = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(allInquiresCondition(member, isAdmin))
                .fetchOne();

        return new PageImpl<>(inquiries, pageable, total);
    }

    // 모든 문의 내역 조건 쿼리 생성
    private BooleanExpression allInquiresCondition(Member member, boolean isAdmin) {
        if (!isAdmin) {
            return inquiry.member.eq(member);
        }
        return null;
    }
}

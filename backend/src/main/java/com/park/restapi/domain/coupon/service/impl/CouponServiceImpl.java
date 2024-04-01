package com.park.restapi.domain.coupon.service.impl;

import com.park.restapi.domain.coupon.entity.Coupon;
import com.park.restapi.domain.coupon.entity.CouponHistory;
import com.park.restapi.domain.coupon.repository.CouponHistoryRepository;
import com.park.restapi.domain.coupon.repository.CouponRepository;
import com.park.restapi.domain.coupon.service.CouponService;
import com.park.restapi.domain.exception.exception.CouponException;
import com.park.restapi.domain.exception.exception.UserException;
import com.park.restapi.domain.exception.info.CouponExceptionInfo;
import com.park.restapi.domain.exception.info.UserExceptionInfo;
import com.park.restapi.domain.user.entity.User;
import com.park.restapi.domain.user.repository.UserRepository;
import com.park.restapi.util.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponHistoryRepository couponHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void acquisitionCoupon() {
        Long currentUserId = JwtService.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserException(UserExceptionInfo.NOT_FOUND_USER, "유저 데이터 없음"));

        // 오늘 획득한 이력이 있으면 중복 불가.
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
//        CouponHistory byCouponHistory = couponHistoryRepository.findByCouponHistory(startOfDay, endOfDay, user);
//
//        if(byCouponHistory != null){
//            throw new UserException(UserExceptionInfo.ALREADY_GET_COUPON, "이미 쿠폰 획득 완료.");
//        }

        // 쿠폰이 남아있는지 확인
        Coupon coupon = couponRepository.findByCoupon(startOfDay, endOfDay)
                .orElseThrow(() -> new CouponException(CouponExceptionInfo.FAIL_COUPON_DATA, "DB에 쿠폰 데이터 존재하지 않음."));

        if(coupon.getRemainingQuantity() < 1){
            throw new CouponException(CouponExceptionInfo.NOT_EXIST_COUPON, "쿠폰 품절");
        }
        // 쿠폰 감소
        coupon.decreasedCoupon();

        // 유저 토큰 + 1
        user.increasedToken();

        CouponHistory couponHistory = CouponHistory.builder()
                .user(user).build();
        couponHistoryRepository.save(couponHistory);
        log.info("쿠폰 획득 성공");
    }
}
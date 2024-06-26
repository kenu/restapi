package com.park.restapi.domain.coupon.entity;

import com.park.restapi.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer remainingQuantity;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Builder
    public Coupon(Integer remainingQuantity, Integer totalQuantity) {
        this.remainingQuantity = remainingQuantity;
        this.totalQuantity = totalQuantity;
    }

    public void decreasedCoupon() {
        this.remainingQuantity--;
    }

    public void updateCouponQuantity(int quantity) {
        this.remainingQuantity = quantity;
        this.totalQuantity = quantity;
    }
}

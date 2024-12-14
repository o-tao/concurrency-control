package app.concurrencycontrol.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptimisticCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long quantity;

    @Version
    private Long version;

    public OptimisticCoupon(String name, Long quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public void decrease() {
        validateDecrease();
        this.quantity--;
    }

    private void validateDecrease() {
        if (this.quantity < 1) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
    }
}

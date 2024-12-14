package app.concurrencycontrol.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long quantity;

    public Coupon(String name, Long quantity) {
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

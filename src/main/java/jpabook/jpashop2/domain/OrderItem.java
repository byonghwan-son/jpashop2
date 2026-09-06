package jpabook.jpashop2.domain;

import jakarta.persistence.*;
import jpabook.jpashop2.domain.item.Item;
import lombok.*;

@Entity
@Getter @Setter
@ToString(exclude = {"order", "item"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_item_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private Item item;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  private int orderPrice;

  private int count;

  // 생성 메소드
  public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
    OrderItem orderItem = new OrderItem();
    orderItem.setItem(item);
    orderItem.setOrderPrice(orderPrice);
    orderItem.setCount(count);

    item.removeStock(count);
    return orderItem;
  }

  // 비즈니스 로직
  public void cancel() {
    getItem().addStock(count);
  }

  // 조회 로직

  /**
   * 주문 상품 금액 조회
   */
  public int getTotalPrice() {
    return getOrderPrice() * getCount();
  }
}

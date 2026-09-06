package jpabook.jpashop2.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter @Setter
@ToString(exclude = {"orderItems", "delivery", "member"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderItem> orderItems = new ArrayList<>();

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "delivery_id")
  private Delivery delivery;

  private LocalDateTime orderDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status; // 주문상태 [ORDER, CANCEL]

  // == 연관관계 편의 메소드 == //
  public void setMember(Member member) {
    this.member = member;
    member.getOrders().add(this);
  }

  public void setDelivery(Delivery delivery) {
    this.delivery = delivery;
    delivery.setOrder(this);
  }

  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
  }

  // 생성 메소드
  public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
    Order order = new Order();
    order.setMember(member);
    order.setDelivery(delivery);
    for(OrderItem orderItem : orderItems) {
      order.addOrderItem(orderItem);
    }
    order.setStatus(OrderStatus.ORDER);
    order.setOrderDate(LocalDateTime.now());
    return order;
  }

  // == 비즈니스 로직 == //
  /**
   * 주문 취소
   */
  public void cancel() {
    if(delivery.getStatus() == DeliveryStatus.COMP) {
      throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
    }

    this.setStatus(OrderStatus.CANCEL);
    for (OrderItem orderItem : orderItems) {
      orderItem.cancel();
    }
  }

  // 조회 로직
  public int getTotalPrice() {
    return orderItems.stream()
        .mapToInt(OrderItem::getTotalPrice)
        .sum();
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    Order order = (Order) o;
    return getId() != null && Objects.equals(getId(), order.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}

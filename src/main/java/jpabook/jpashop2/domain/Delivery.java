package jpabook.jpashop2.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@ToString(exclude = "order")
@NoArgsConstructor
public class Delivery {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "delivery_id")
  private Long id;

  @OneToOne(mappedBy = "delivery")
  private Order order;

  @Embedded
  private Address address;

  @Enumerated(EnumType.STRING)
  private DeliveryStatus status;
}

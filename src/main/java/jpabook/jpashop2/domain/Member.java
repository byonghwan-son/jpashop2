package jpabook.jpashop2.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter @Setter
@ToString(exclude = "orders")
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @NotEmpty(message = "회원 이름은 필수 입니다.")
  private String name;

  @Embedded
  private Address address;

  @JsonIgnore
  @OneToMany(mappedBy = "member")
  private List<Order> orders = new ArrayList<>();
}

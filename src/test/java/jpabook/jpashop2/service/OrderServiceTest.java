package jpabook.jpashop2.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop2.domain.Address;
import jpabook.jpashop2.domain.Member;
import jpabook.jpashop2.domain.Order;
import jpabook.jpashop2.domain.OrderStatus;
import jpabook.jpashop2.domain.item.Book;
import jpabook.jpashop2.exceptioin.NotEnoughStockException;
import jpabook.jpashop2.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {

  @Autowired EntityManager em;
  @Autowired OrderService orderService;
  @Autowired OrderRepository orderRepository;

  Member member;
  Book book;

  @BeforeEach
  void setUp() {
    member = new Member();
    member.setName("회원1");
    member.setAddress(new Address("서울", "강가", "123-123"));
    em.persist(member);

    book = new Book();
    book.setName("시골 JPA");
    book.setPrice(10000);
    book.setStockQuantity(10);
    em.persist(book);
  }

  @Test
  void order() {
    int orderCount = 2;

    Order order = orderService.order(member.getId(), book.getId(), orderCount);

    Order savedOrder = orderRepository.findOne(order.getId());
    assertThat(savedOrder).isEqualTo(order);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER);
    assertThat(book.getStockQuantity()).isEqualTo(8);
    assertThat(10000 * orderCount).isEqualTo(order.getTotalPrice());
    assertThat(savedOrder.getOrderItems()).hasSize(1);
    assertThat(savedOrder.getOrderItems().getFirst().getItem().getStockQuantity()).isEqualTo(8);
  }

  @Test
  void orderOverStock() {
    int orderCount = 11;

    assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), orderCount));
  }

  @Test
  void cancelOrder() {
    int orderCount = 2;
    Order order = orderService.order(member.getId(), book.getId(), orderCount);

    Order cancelOrder = orderService.cancelOrder(order.getId());

    assertThat(book.getStockQuantity()).isEqualTo(10);
    assertThat(cancelOrder.getOrderItems().getFirst().getItem().getStockQuantity()).isEqualTo(10);
  }

}
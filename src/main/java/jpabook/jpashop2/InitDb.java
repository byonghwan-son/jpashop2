package jpabook.jpashop2;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop2.domain.*;
import jpabook.jpashop2.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitDb {

  private final InitService initService;

  @PostConstruct
  public void init() {
    initService.dbInit1();
    initService.dbInit2();
  }

  @Component
  @RequiredArgsConstructor
  @Transactional
  static class InitService {
    private final EntityManager em;

    public void dbInit1() {
      Member member = createMember("userA", new Address("서울", "1", "1111"));
      em.persist(member);

      Book book1 = createBook("JPA1 BOOK", 10000, 100);
      em.persist(book1);

      Book book2 = createBook("JPA2 BOOK", 20000, 120);
      em.persist(book2);

      Delivery delivery = createDelivery(member);
      OrderItem orderItem = OrderItem.createOrderItem(book1, 10000, 3);
      OrderItem orderItem1 = OrderItem.createOrderItem(book2, 20000, 4);
      Order order = Order.createOrder(member, delivery, orderItem, orderItem1);
      em.persist(order);
    }

    public void dbInit2() {
      Member member = createMember("userB", new Address("부산", "2", "2222"));
      em.persist(member);

      Book book1 = createBook("SPRING1 BOOK", 20000, 30);
      em.persist(book1);

      Book book2 = createBook("SPRING2 BOOK", 40000, 60);
      em.persist(book2);

      Delivery delivery = createDelivery(member);
      OrderItem orderItem = OrderItem.createOrderItem(book1, 20000, 5);
      OrderItem orderItem1 = OrderItem.createOrderItem(book2, 40000, 6);
      Order order = Order.createOrder(member, delivery, orderItem, orderItem1);
      em.persist(order);
    }

    private static Member createMember(String name, Address address) {
      Member member = new Member();
      member.setName(name);
      member.setAddress(address);
      return member;
    }

    private Delivery createDelivery(Member member) {
      Delivery delivery = new Delivery();
      delivery.setAddress(member.getAddress());
      delivery.setStatus(DeliveryStatus.READY);
      em.persist(delivery);
      return delivery;
    }

    private static Book createBook(String name, int price, int stockQuantity) {
      Book book = new Book();
      book.setName(name);
      book.setPrice(price);
      book.setStockQuantity(stockQuantity);
      return book;
    }
  }
}

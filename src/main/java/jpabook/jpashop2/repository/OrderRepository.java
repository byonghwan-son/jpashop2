package jpabook.jpashop2.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jpabook.jpashop2.domain.Order;
import jpabook.jpashop2.domain.OrderStatus;
import jpabook.jpashop2.domain.QMember;
import jpabook.jpashop2.domain.QOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * **쿼리 방식 선택 권장 순서**
 * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
 * 2. 필요하면 페치 조인으로 성능을 최적화 한다. ➡️ 대부분의 성능 이슈가 해결된다.
 * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
 * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
 */
@Repository
@RequiredArgsConstructor
public class OrderRepository {

  private final EntityManager em;

  public Order save(Order order) {
    em.persist(order);
    return order;
  }

  public Order findOne(Long id) {
    return em.find(Order.class, id);
  }

  public List<Order> findAll(OrderSearch orderSearch) {
    QOrder order = QOrder.order;
    QMember member = QMember.member;

    JPAQueryFactory query = new JPAQueryFactory(em);
    return query
        .select(order)
        .from(order)
        .join(order.member, member)
        .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch, member))
        .limit(1000)
        .fetch();
  }

  private BooleanExpression nameLike(OrderSearch orderSearch, QMember member) {
    if(!StringUtils.hasText(orderSearch.getMemberName()))
      return null;
    return member.name.like(orderSearch.getMemberName());
  }

  private BooleanExpression statusEq(OrderStatus statusCond) {
    if(statusCond == null)
      return null;
    return QOrder.order.status.eq(statusCond);
  }

  // QueryDSL로 수정 필요.
  public List<Order> findAll_Old(OrderSearch orderSearch) {
    String jpql = "select o From Order o join o.member m";
    boolean isFirstCondition = true;
    //주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      jpql += " where";
      isFirstCondition = false;
      jpql += " o.status = :status";
    }
    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      if (isFirstCondition) {
        jpql += " where";
      } else {
        jpql += " and";
      }
      jpql += " m.name like :name";
    }

    TypedQuery<Order> query = em.createQuery(jpql, Order.class)
        .setMaxResults(100); //최대 1000건

    if (orderSearch.getOrderStatus() != null) {
      query = query.setParameter("status", orderSearch.getOrderStatus());
    }

    if (StringUtils.hasText(orderSearch.getMemberName())) {
      query = query.setParameter("name", orderSearch.getMemberName());
    }

    return query.getResultList();
  }

  public List<Order> findAllWithFetchJoin(OrderSearch orderSearch) {
    // hibernate 6 이상부터 자동으로 distinct 키워드가 들어가서 목적 데이터에 대한 중복을 제거 한다.
    String jpql = "select o From Order o" +
        " join fetch o.member m " +
        " join fetch o.delivery d " +
        " join fetch o.orderItems oi " +
        " join fetch oi.item i ";

    boolean isFirstCondition = true;
    //주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      jpql += " where";
      isFirstCondition = false;
      jpql += " o.status = :status";
    }
    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      if (isFirstCondition) {
        jpql += " where";
      } else {
        jpql += " and";
      }
      jpql += " m.name like :name";
    }

    TypedQuery<Order> query = em.createQuery(jpql, Order.class);
        // .setMaxResults(100); // fetch join 에서는 페이징 불가능 <<메모리에서 작업함>>
        // HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory

    if (orderSearch.getOrderStatus() != null) {
      query = query.setParameter("status", orderSearch.getOrderStatus());
    }

    if (StringUtils.hasText(orderSearch.getMemberName())) {
      query = query.setParameter("name", orderSearch.getMemberName());
    }

    return query.getResultList();
  }

  public List<Order> findAllWithMemberDelivery() {
    return em.createQuery(
        "select o from Order o" +
            " join fetch o.member m" +
            " join fetch o.delivery d", Order.class
    ).getResultList();
  }

  public List<Order> findAllWithMemberDelivery(int offset, int limit) {
    return em.createQuery(
        "select o from Order o" +
            " join fetch o.member m" +
            " join fetch o.delivery d", Order.class)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

}

package jpabook.jpashop2.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop2.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

  private final EntityManager em;

  public Item save(Item item) {
    if(item.getId() == null) {
      em.persist(item);
    }
    else {
      // 가급적 merge를 사용하지 말자.
      item = em.merge(item);
    }

    return item;
  }

  public Item findOne(Long id) {
    return em.find(Item.class, id);
  }

  public List<Item> findAll() {
    return em.createQuery("select i from Item i", Item.class)
        .getResultList();
  }
}

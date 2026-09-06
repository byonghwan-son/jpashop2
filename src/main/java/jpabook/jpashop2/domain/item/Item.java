package jpabook.jpashop2.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop2.domain.Category;
import jpabook.jpashop2.exceptioin.NotEnoughStockException;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
@ToString(exclude = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "item_id")
  private Long id;

  private String name;
  private int price;
  private int stockQuantity;

  @ManyToMany(mappedBy = "items")
  private List<Category> categories = new ArrayList<>();

  // 비즈니스 로직

  /**
   * 재고 증가
   */
  public void addStock(int quantity) {
    this.stockQuantity += quantity;
  }

  /**
   * 재고 감소
   */
  public void removeStock(int quantity) {
    int restStock = this.stockQuantity - quantity;
    if (restStock < 0) {
      throw new NotEnoughStockException("need more stock");
    }
    this.stockQuantity = restStock;
  }

  protected void updateItem(String name, int price, int stockQuantity) {
    this.name = name;
    this.price = price;
    this.stockQuantity = stockQuantity;
  }
}

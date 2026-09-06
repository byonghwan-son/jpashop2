package jpabook.jpashop2.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("B")
@Getter @Setter
public class Book extends Item {
  private String author;
  private String isbn;

  // 헬퍼 메소드
  public void updateBook(String name, int price, int stockQuantity,
                         String author, String isbn) {
    updateItem(name, price, stockQuantity);
    this.author = author;
    this.isbn = isbn;
  }

}

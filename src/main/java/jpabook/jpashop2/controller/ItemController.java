package jpabook.jpashop2.controller;

import jpabook.jpashop2.domain.item.Book;
import jpabook.jpashop2.domain.item.Item;
import jpabook.jpashop2.repository.ItemRepository;
import jpabook.jpashop2.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;
  private final ItemRepository itemRepository;

  @GetMapping("/items/new")
  public String createForm(Model model) {
    model.addAttribute("form", new BookForm());
    return "items/createItemForm";
  }

  @PostMapping("/items/new")
  public String create(BookForm form) {
    Book book = new Book();
    book.setName(form.getName());
    book.setPrice(form.getPrice());
    book.setStockQuantity(form.getStockQuantity());
    book.setAuthor(form.getAuthor());
    book.setIsbn(form.getIsbn());

    Item item = itemService.saveItem(book);
    return "redirect:/items";
  }

  /**
   * 상품 목록
   */
  @GetMapping(value = "/items")
  public String list(Model model) {
    List<Item> items = itemService.findItems();
    model.addAttribute("items", items);
    return "items/itemList";
  }

  @GetMapping("/items/{itemId}/edit")
  public String updateItemForm(Model model, @PathVariable("itemId") Long id) {
    Book book = (Book) itemService.findOne(id);

    BookForm bookForm = new BookForm();
    bookForm.setId(book.getId());
    bookForm.setName(book.getName());
    bookForm.setAuthor(book.getAuthor());
    bookForm.setIsbn(book.getIsbn());

    bookForm.setPrice(book.getPrice());
    bookForm.setStockQuantity(book.getStockQuantity());

    model.addAttribute("form", bookForm);

    return "items/updateItemForm";
  }

  @PostMapping("/items/{itemId}/edit")
  public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable Long itemId) {
/*
  엔티티를 변경할 때는 항상 변경 감지를 사용하세요
    - 컨트롤러에서 어설프게 엔티티를 생성하지 마세요.
    - 트랜잭션이 있는 서비스 계층에 식별자(`id` )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto)
    - 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요.
    - 트랜잭션 커밋 시점에 변경 감지가 실행됩니다.
 */
    Book book = (Book) itemRepository.findOne(itemId);

    book.updateBook(form.getName(), form.getPrice(), form.getStockQuantity(),
        form.getAuthor(), form.getIsbn());

    itemService.saveItem(book);

    return "redirect:/items";
  }
}

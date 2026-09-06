package jpabook.jpashop2.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WrappedResult<T> {
  private int count;
  private T data;
}

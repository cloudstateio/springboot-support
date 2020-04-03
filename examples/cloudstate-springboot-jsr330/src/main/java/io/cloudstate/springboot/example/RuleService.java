package io.cloudstate.springboot.example;

import com.example.shoppingcart.Shoppingcart;

public interface RuleService {
    boolean isValidAmount(Shoppingcart.AddLineItem item);
}

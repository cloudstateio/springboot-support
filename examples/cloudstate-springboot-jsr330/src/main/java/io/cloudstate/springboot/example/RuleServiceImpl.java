package io.cloudstate.springboot.example;

import com.example.shoppingcart.Shoppingcart;

import javax.inject.Named;

@Named
public class RuleServiceImpl implements RuleService {

    @Override
    public boolean isValidAmount(Shoppingcart.AddLineItem item) {
        return (item.getQuantity() > 0);
    }
}

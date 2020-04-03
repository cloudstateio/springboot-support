package io.cloudstate.springboot.example;

import com.example.shoppingcart.Shoppingcart;
import org.springframework.stereotype.Service;

@Service
public class RuleServiceImpl implements RuleService {

    @Override
    public boolean isValidAmount(Shoppingcart.AddLineItem item) {
        return (item.getQuantity() > 0);
    }
}
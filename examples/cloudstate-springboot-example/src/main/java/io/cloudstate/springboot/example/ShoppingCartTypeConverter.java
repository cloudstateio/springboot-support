package io.cloudstate.springboot.example;

import com.example.shoppingcart.Shoppingcart;
import com.example.shoppingcart.persistence.Domain;
import org.springframework.stereotype.Component;

@Component
public class ShoppingCartTypeConverter {

    public Shoppingcart.LineItem convert(Domain.LineItem item) {
        return Shoppingcart.LineItem.newBuilder()
                .setProductId(item.getProductId())
                .setName(item.getName())
                .setQuantity(item.getQuantity())
                .build();
    }

    public Domain.LineItem convert(Shoppingcart.LineItem item) {
        return Domain.LineItem.newBuilder()
                .setProductId(item.getProductId())
                .setName(item.getName())
                .setQuantity(item.getQuantity())
                .build();
    }
}

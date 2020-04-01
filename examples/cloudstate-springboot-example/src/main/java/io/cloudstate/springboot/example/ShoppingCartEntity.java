package io.cloudstate.springboot.example;

import com.example.shoppingcart.Shoppingcart;
import com.example.shoppingcart.persistence.Domain;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Empty;
import io.cloudstate.javasupport.EntityId;
import io.cloudstate.javasupport.eventsourced.*;
import io.cloudstate.springboot.starter.CloudstateContext;
import io.cloudstate.springboot.starter.CloudstateEntityBean;
import io.cloudstate.springboot.starter.EntityAdditionaDescriptors;
import io.cloudstate.springboot.starter.EntityServiceDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An event sourced entity.
 */
@EventSourcedEntity
@CloudstateEntityBean
public class ShoppingCartEntity {
    private final Map<String, Shoppingcart.LineItem> cart = new LinkedHashMap<>();

    @EntityId
    private String entityId;

    @CloudstateContext
    private EventSourcedContext context;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ShoppingCartTypeConverter typeConverter;

    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor getDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] getAdditionalDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }

    @Snapshot
    public Domain.Cart snapshot() {
        return Domain.Cart.newBuilder()
                .addAllItems(cart.values().stream().map(typeConverter::convert).collect(Collectors.toList()))
                .build();
    }

    @SnapshotHandler
    public void handleSnapshot(Domain.Cart cart) {
        this.cart.clear();
        for (Domain.LineItem item : cart.getItemsList()) {
            this.cart.put(item.getProductId(), typeConverter.convert(item));
        }
    }

    @EventHandler
    public void itemAdded(Domain.ItemAdded itemAdded) {
        Shoppingcart.LineItem item = cart.get(itemAdded.getItem().getProductId());
        if (item == null) {
            item = typeConverter.convert(itemAdded.getItem());
        } else {
            item =
                    item.toBuilder()
                            .setQuantity(item.getQuantity() + itemAdded.getItem().getQuantity())
                            .build();
        }
        cart.put(item.getProductId(), item);
    }

    @EventHandler
    public void itemRemoved(Domain.ItemRemoved itemRemoved) {
        cart.remove(itemRemoved.getProductId());
    }

    @CommandHandler
    public Shoppingcart.Cart getCart() {
        return Shoppingcart.Cart.newBuilder().addAllItems(cart.values()).build();
    }

    @CommandHandler
    public Empty addItem(Shoppingcart.AddLineItem item, CommandContext ctx) {
        if (!ruleService.isValidAmount(item)) {
            ctx.fail("Cannot add negative quantity of to item" + item.getProductId());
        }
        ctx.emit(
                Domain.ItemAdded.newBuilder()
                        .setItem(
                                Domain.LineItem.newBuilder()
                                        .setProductId(item.getProductId())
                                        .setName(item.getName())
                                        .setQuantity(item.getQuantity())
                                        .build())
                        .build());
        return Empty.getDefaultInstance();
    }

    @CommandHandler
    public Empty removeItem(Shoppingcart.RemoveLineItem item, CommandContext ctx) {
        if (!cart.containsKey(item.getProductId())) {
            ctx.fail("Cannot remove item " + item.getProductId() + " because it is not in the cart.");
        }
        ctx.emit(Domain.ItemRemoved.newBuilder().setProductId(item.getProductId()).build());
        return Empty.getDefaultInstance();
    }


}

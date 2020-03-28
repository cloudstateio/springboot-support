# Spring Boot Cloudstate Starter

## Getting Started

Write your Cloudstate function as normal
```java
/**
 * An event sourced entity.
 */
@EventSourcedEntity
public class ShoppingCartEntity {

    private final String entityId;
    private final Map<String, Shoppingcart.LineItem> cart = new LinkedHashMap<>();

    public ShoppingCartEntity(@EntityId String entityId) {
        this.entityId = entityId;
    }

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
                .addAllItems(cart.values().stream().map(this::convert).collect(Collectors.toList()))
                .build();
    }

    @SnapshotHandler
    public void handleSnapshot(Domain.Cart cart) {
        this.cart.clear();
        for (Domain.LineItem item : cart.getItemsList()) {
            this.cart.put(item.getProductId(), convert(item));
        }
    }

    @EventHandler
    public void itemAdded(Domain.ItemAdded itemAdded) {
        Shoppingcart.LineItem item = cart.get(itemAdded.getItem().getProductId());
        if (item == null) {
            item = convert(itemAdded.getItem());
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
        if (item.getQuantity() <= 0) {
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

    private Shoppingcart.LineItem convert(Domain.LineItem item) {
        return Shoppingcart.LineItem.newBuilder()
                .setProductId(item.getProductId())
                .setName(item.getName())
                .setQuantity(item.getQuantity())
                .build();
    }

    private Domain.LineItem convert(Shoppingcart.LineItem item) {
        return Domain.LineItem.newBuilder()
                .setProductId(item.getProductId())
                .setName(item.getName())
                .setQuantity(item.getQuantity())
                .build();
    }
}

```

The only thing other than a normal cloudstate function is the need to register the descriptors explicitly
```java
    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor getDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] getAdditionalDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }
```

Write your main class in the Spring boot style. 
Uses the @EnableCloudstate annotation to tell Spring what to do.
```java
import io.cloudstate.springboot.starter.autoconfigure.EnableCloudstate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableCloudstate
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}

```
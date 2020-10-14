// tag::shopping-cart-entity[]
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
// end::shopping-cart-entity[]

// tag::shopping-cart-configuration[]
import com.example.shoppingcart.Shoppingcart;
import com.google.protobuf.Descriptors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DescriptorsConfiguration {

    @Bean
    public Descriptors.ServiceDescriptor shoppingCartEntityServiceDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @Bean
    public Descriptors.FileDescriptor[] shoppingCartEntityFileDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }
}
// end::shopping-cart-configuration[]

// tag::shopping-cart-main[]
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.cloudstate.springboot.starter.autoconfigure.EnableCloudstate;

@EnableCloudstate
@SpringBootApplication
public class Main {
    public static void main(String[] args) { SpringApplication.run(Main.class, args); }
}
// end::shopping-cart-main[]

// tag::shopping-cart-descriptors[]
@Bean(name = "shoppingCartEntityServiceDescriptor")
public Descriptors.ServiceDescriptor serviceDescriptor() {
    return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
}

@Bean(name = "shoppingCartEntityFileDescriptors")
public Descriptors.FileDescriptor[] fileDescriptors() {
   return new Descriptors.FileDescriptor[] {com.example.shoppingcart.persistence.Domain.getDescriptor()};
}
// end::shopping-cart-descriptors[]

// tag::shopping-cart-descriptors-alternative[]
@Bean
public Descriptors.ServiceDescriptor shoppingCartEntityServiceDescriptor() {
   return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
}

@Bean
public Descriptors.FileDescriptor[] shoppingCartEntityFileDescriptors() {
    return new Descriptors.FileDescriptor[] {com.example.shoppingcart.persistence.Domain.getDescriptor()};
}
// end::shopping-cart-descriptors-alternative[]

// tag::shopping-cart-descriptors-static[]
@EntityServiceDescriptor
public static Descriptors.ServiceDescriptor getDescriptor() {
    return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
}

@EntityAdditionaDescriptors
public static Descriptors.FileDescriptor[] getAdditionalDescriptors() {
    return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
}
// end::shopping-cart-descriptors-static[]

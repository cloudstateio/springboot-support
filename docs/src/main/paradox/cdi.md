# Context Injection

As we saw in the Getting Started example, it is perfectly possible to inject any Bean available in Spring into a 
Cloudstate entity class.
It is also possible to use the annotations present in the javax.inject package 
([JSR330](https://jcp.org/en/jsr/detail?id=330)). 
The Cloudstate Springboot Support library already includes the necessary dependencies so you don't have to worry about it.

You can annotate your entity classes with Spring `@Component` or `@Service` annotations but we have created a convenient 
annotation that we call `@CloudstateEntityBean` that can be used for that too.

## JSR330

The Cloudstate Spring Boot Support library supports JSR330 within the scope of the support provided by Spring itself to 
this specification.
Note that the Cloudstate Java Support library on which we depend allows you to bind Cloudstate and any other DI container 
you want. However, no specific module for any of these other containers has yet been made.

Feel free to contribute or suggest support for more runtimes.

## Injecting EntityId and Cloudstate Context Objects

You can use the `@EntityId` annotation to access the managed entity's id.
It is also possible to have access to the EventSourcedEntityCreationContext created during the activation of the object 
by Cloudstate. However for this you will need to annotate the Context property with the annotation @CloudstateContext 
as in the example below:

```java
@EntityId
private String entityId;

@CloudstateContext
private EventSourcedContext context;
```

### Using properties instead constructors

It is currently not possible to inject Cloudstate's EntityId and Context properties via constructor. 
This is because the life cycles of these properties differ from the life cycle of objects managed directly by Spring.

This is obviously only an issue if you want to inject EntityId or EventSourcedEntityCreationContext.
Otherwise, if you want to inject only other Spring Context Beans, you can use injection via builders as normal.

The builders below would be perfectly acceptable:

```java
@EventSourcedEntity
@CloudstateEntityBean
public final class ShoppingCartEntity {
    private final Map<String, Shoppingcart.LineItem> cart = new LinkedHashMap<>();

    @EntityId
    private String entityId;

    @CloudstateContext
    private EventSourcedContext context;

    private final RuleService ruleService;

    private final ShoppingCartTypeConverter typeConverter;
    
    @Autowired
    public ShoppingCartEntity(RuleService ruleService, ShoppingCartTypeConverter typeConverter){
        this.ruleService = ruleService;
        this.typeConverter = typeConverter;
    }
    
    //......
}
```
@@@ note { title=Important }

As you can see, the constructor injection constraint applies only to EntityId and CreationContext. 
So, as in the example above, you can mix the approaches and get the best of both worlds together 

@@@

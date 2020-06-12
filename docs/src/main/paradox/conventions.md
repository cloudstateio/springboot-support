# Conventions and Restrictions

Both Springboot and Cloudstate have some conventions and / or restrictions for creating objects. 
In the sections below we describe those that are most relevant

## Registering Protobuf Descriptors
As mentioned earlier, there are two ways to register the protobuf descriptor files and here we will explain each one in 
detail.

First using the Spring configuration.
 
Spring's bean declaration conventions define that the method name is exactly the name that will be registered in
the Spring injection container as a qualifier. 
So if you use method names other than those defined in the Cloudstate Springboot support convention:

 (entity.getSimpleName() + "ServiceDescriptor" for example)
 
 @@@ note { title=Important } Remembering that the first letter must always be lowercase, as well as the method and variable naming convention in Java @@@

Then you will need to use the name property of the '**@Bean**' annotation and define the name following these conventions.

If your entity class is called ShoppinCartEntity then you can declare the beans as below:

@@snip [ShoppingCartEntity.java]($base$/docs/src/tests/paradox/ShoppingCartEntity.java) { #shopping-cart-descriptors }

Or like this:

@@snip [ShoppingCartEntity.java]($base$/docs/src/tests/paradox/ShoppingCartEntity.java) { #shopping-cart-descriptors-alternative }

The other way is to use the created entity class itself and declare some annotated static methods like the example below:

@snip [ShoppingCartEntity.java]($base$/docs/src/tests/paradox/ShoppingCartEntity.java) { #shopping-cart-descriptors-static }

We prefer that you adopt the version based on the Spring conventions using configuration classes as in the 
@ref:[Getting Started](gettingstarted.md) example.
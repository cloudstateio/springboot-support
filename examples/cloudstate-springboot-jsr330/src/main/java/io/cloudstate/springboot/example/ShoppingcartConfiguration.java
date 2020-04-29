package io.cloudstate.springboot.example;

import com.example.shoppingcart.Shoppingcart;
import com.google.protobuf.Descriptors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShoppingcartConfiguration {

    @Bean("ShoppingCartServiceDescriptor")
    public Descriptors.ServiceDescriptor getDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @Bean("ShoppingCartFileDescriptors")
    public Descriptors.FileDescriptor[] getAdditionalDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }
}

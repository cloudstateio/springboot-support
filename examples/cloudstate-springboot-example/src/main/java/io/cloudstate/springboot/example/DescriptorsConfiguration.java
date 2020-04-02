package io.cloudstate.springboot.example;

import com.example.shoppingcart.Shoppingcart;
import com.google.protobuf.Descriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DescriptorsConfiguration {
    private Logger log = LoggerFactory.getLogger(DescriptorsConfiguration.class);

    @Bean(name = "shoppingCartEntityServiceDescriptor")
    public Descriptors.ServiceDescriptor shoppingCartServiceDescriptor() {
        return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
    }

    @Bean(name = "shoppingCartEntityFileDescriptors")
    public Descriptors.FileDescriptor[] shoppingCartFileDescriptors() {
        return new Descriptors.FileDescriptor[]{com.example.shoppingcart.persistence.Domain.getDescriptor()};
    }
}

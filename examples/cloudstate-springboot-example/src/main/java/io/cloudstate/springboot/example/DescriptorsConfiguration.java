package io.cloudstate.springboot.example;

/**
 * Spring's bean declaration conventions define that the method name is exactly the name that will be registered in
 * the Spring injection container as a qualifier. So if you use method names other than those defined in the
 * Cloudstate Springboot support convention (entity.getSimpleName() + "ServiceDescriptor" for example)
 * then you will need to use the name property of the @Bean annotation and define the name following these conventions.
 *
 * Examples:
 * If your entity class is called ShoppinCartEntity then you can declare the beans as below:
 *
 * @Bean(name = "shoppingCartEntityServiceDescriptor")
*  public Descriptors.ServiceDescriptor serviceDescriptor() {
*      return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
*  }
 *
 * @Bean(name = "shoppingCartEntityFileDescriptors")
 * public Descriptors.FileDescriptor[] fileDescriptors() {
 *      return new Descriptors.FileDescriptor[] {com.example.shoppingcart.persistence.Domain.getDescriptor()};
 * }
 *
 * Or like this:
 *
 * @Bean
 * public Descriptors.ServiceDescriptor shoppingCartEntityServiceDescriptor() {
 *     return Shoppingcart.getDescriptor().findServiceByName("ShoppingCart");
 * }
 *
 * @Bean
 * public Descriptors.FileDescriptor[] shoppingCartEntityFileDescriptors() {
 *     return new Descriptors.FileDescriptor[] {com.example.shoppingcart.persistence.Domain.getDescriptor()};
 * }
 *
 * */

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

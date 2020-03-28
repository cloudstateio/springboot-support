package io.cloudstate.springboot.starter;

import com.google.protobuf.Descriptors;
import com.google.protobuf.empty.Empty;
import io.cloudstate.javasupport.crdt.CrdtEntity;

@CrdtEntity
public class FakeCrdtEntity {

    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor descriptor(){
        return Empty.descriptor().getFile().findServiceByName("");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] additional(){
        return new Descriptors.FileDescriptor[]{ Empty.descriptor().getFile()};
    }
}

package io.cloudstate.springboot.starter;

import com.google.protobuf.Descriptors;
import com.google.protobuf.EmptyProto;
import com.google.protobuf.empty.Empty;
import io.cloudstate.javasupport.eventsourced.EventSourcedEntity;

@EventSourcedEntity
public class FakeEventSourcedEntity {

    @EntityServiceDescriptor
    public static Descriptors.ServiceDescriptor descriptor(){
        return EmptyProto.getDescriptor().getFile().findServiceByName("");
    }

    @EntityAdditionaDescriptors
    public static Descriptors.FileDescriptor[] additional(){
        return new Descriptors.FileDescriptor[]{ EmptyProto.getDescriptor().getFile()};
    }
}

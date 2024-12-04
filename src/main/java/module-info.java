module com.gomokugamegrpc {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.graphics;
    requires java.desktop;
    requires java.rmi;

    requires io.grpc;
    requires io.netty.buffer;
    requires io.grpc.protobuf;
    requires protobuf.java;
    requires io.grpc.stub;
    requires com.google.common;
    requires java.annotation;
    requires java.sql;
    requires proto.google.common.protos;

    opens com.gomokugamegrpc to javafx.fxml;
    exports com.gomokugamegrpc;

    exports com.gomokugamegrpc.global_objects;
    opens com.gomokugamegrpc.client to javafx.fxml;
    exports com.gomokugamegrpc.client;
    exports com.gomokugamegrpc.global_objects.enums;
    exports com.gomokugamegrpc.server;
    opens com.gomokugamegrpc.server to javafx.fxml;
}
package com.gomokugamegrpc;

import com.gomokugamegrpc.server.GomokuServer;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GRPCServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(8080)
                .addService(new GomokuServer())
                .build();

        server.start();
        System.out.println("Server started on port 8080");
        server.awaitTermination();
    }
}

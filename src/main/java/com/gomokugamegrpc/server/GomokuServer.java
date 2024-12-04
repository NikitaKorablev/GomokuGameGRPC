package com.gomokugamegrpc.server;

import Gomoku.GomokuServiceGrpc.*;
import Gomoku.GomokuServiceOuterClass.*;
import com.gomokugamegrpc.global_objects.Chip;
import com.gomokugamegrpc.global_objects.GameTable;
import com.gomokugamegrpc.global_objects.enums.TableValue;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.util.*;

public class GomokuServer extends GomokuServiceImplBase {
    private final GameTable table = new GameTable();
    private Boolean gameStarted = false;
    private TableValue winnerColor = TableValue.NULL;

    private final List<String> registeredClients = new ArrayList<>();
    private final List<StreamObserver<GameStatusResponse>> gameStatusSubscribers = new ArrayList<>();
    private final Map<String, StreamObserver<ChipAddedResponse>> chipAddedSubscribers = new HashMap<>();

    public GomokuServer() {}

    @Override
    public void registerClient(Empty request, StreamObserver<Response> responseObserver) {
        System.out.println("Registering new client");

        boolean success = false;
        String message;
        boolean setChipIsAllow = false;

        if (registeredClients.size() >= 2)
            message = "All clients is already added";
        else {
            String clientColor;
            switch (registeredClients.size()) {
                case 0 -> {
                    clientColor = "WHITE";
                    setChipIsAllow = true;
                }
                default -> clientColor = "BLACK";
            }

            registeredClients.add(clientColor);
            success = true;
            message = "New player. Color: " + clientColor;
        }

        Response response = Response.newBuilder()
                .setChipIsAllow(setChipIsAllow)
                .setSuccess(success)
                .setMessage(message).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void subscribeToGameStatusUpdates(Empty request, StreamObserver<GameStatusResponse> responseObserver) {
        gameStatusSubscribers.add(responseObserver);

        GameStatusResponse initialResponse = GameStatusResponse.newBuilder()
                .setGameIsStarted(gameStarted)
                .setWinnerColor("NULL")
                .build();
        responseObserver.onNext(initialResponse);

        new Thread(() -> {
            try {
                Context context = Context.current();
                while (!context.isCancelled()) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                gameStatusSubscribers.remove(responseObserver);
                System.out.println("Client stopped subscribe");
            }
        }).start();
    }

    @Override
    public void subscribeToChipUpdates(ClientInfo request, StreamObserver<ChipAddedResponse> responseObserver) {
        String clientColor = request.getClientColor();
        chipAddedSubscribers.put(clientColor, responseObserver);

        ChipAddedResponse response = ChipAddedResponse.newBuilder()
                .setSuccess(true)
                .setMessage("You are now connected. Awaiting a new chips.")
                .build();
        responseObserver.onNext(response);

        new Thread(() -> {
            try {
                Context currentContext = Context.current();
                while (!currentContext.isCancelled()) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                chipAddedSubscribers.remove(clientColor);
                System.out.println("Подписчик на выстрелы был удален.");
            }
        }).start();
    }

    @Override
    public void setChip(ChipInfo request, StreamObserver<Response> responseObserver) {
        boolean success = false;
        String message;
        Chip chip = new Chip(request.getX(), request.getY(), request.getColor());

        if (!gameStarted) message = "Game is not started";
        else {
            table.setChip(chip);
            success = true;
            message = "New Chip";
        }

        Response resp = Response.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();

        if (success) notifyAllNewChip(request, success, message);
        if (table.isWin(chip)) {
            gameStarted = false;
            winnerColor = chip.getColor();
            notifyAllGameStatus();
        }
//        else notifyChipCrushed(chipAddedSubscribers.get(request.getColor()), success, message);
    }

    @Override
    public void startGame(Empty request, StreamObserver<Response> responseObserver) {
        boolean success = false;
        String message;
        if (registeredClients.size() < 2)
            message = "There are not enough players";
        else {
            success = true;
            message = "Game started";
            gameStarted = true;
        }

        Response response = Response.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        if (success) notifyAllGameStatus();
    }

    @Override
    public void nullifyGame(Empty response, StreamObserver<Response> responseStreamObserver){
        table.nullifyTable();
        gameStarted = false;
        notifyAllGameStatus();
    }

    private void notifyAllGameStatus() {
        GameStatusResponse response = GameStatusResponse.newBuilder()
                .setGameIsStarted(gameStarted)
                .setWinnerColor(winnerColor.string())
                .build();

        for (StreamObserver<GameStatusResponse> subscriber: gameStatusSubscribers)
            subscriber.onNext(response);
    }

    private void notifyAllNewChip(ChipInfo chip, boolean success, String message) {
        for (String color: chipAddedSubscribers.keySet()) {
            ChipAddedResponse.Builder chipAddedBuilder = ChipAddedResponse.newBuilder()
                    .setChipInfo(chip)
                    .setChipOwner(chip.getColor())
                    .setSuccess(success)
                    .setMessage(message);

            boolean setChipIsAllow = !color.equals(chip.getColor());
            chipAddedBuilder.setChipIsAllow(setChipIsAllow);

            StreamObserver<ChipAddedResponse> subscriber = chipAddedSubscribers.get(color);
            ChipAddedResponse response = chipAddedBuilder.build();
            subscriber.onNext(response);
        }
    }

    private void notifyChipCrushed(StreamObserver<ChipAddedResponse> subscriber, boolean success, String message) {
        ChipAddedResponse.Builder chipAddedBuilder = ChipAddedResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message);

        ChipAddedResponse response = chipAddedBuilder.build();
        subscriber.onNext(response);
    }
}

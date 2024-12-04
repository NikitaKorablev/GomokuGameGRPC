package com.gomokugamegrpc.client;

import Gomoku.GomokuServiceGrpc;
import Gomoku.GomokuServiceGrpc.*;
import Gomoku.GomokuServiceOuterClass.*;
import com.gomokugamegrpc.global_objects.Chip;
import com.gomokugamegrpc.global_objects.enums.TableValue;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ClientApp {
    TableValue playerColor = TableValue.NULL;
    Boolean gameIsStarted = false;
    Boolean setChipIsAllow = false;
    UpdateGameState updater;
    GomokuServiceBlockingStub blockingStub;
    GomokuServiceStub asyncStub;

    Empty empty = Empty.newBuilder().build();

    public ClientApp(UpdateGameState updater) {
        this.updater = updater;
        try {
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext().build();

            blockingStub = GomokuServiceGrpc.newBlockingStub(channel);
            asyncStub = GomokuServiceGrpc.newStub(channel);

            Response resp = blockingStub.registerClient(empty); // Регистрация на сервере
            playerColor = parseColor(resp.getMessage()).equals("WHITE")?
                    TableValue.WHITE: TableValue.BLACK;
            setChipIsAllow = resp.getChipIsAllow();

            subscribeToGameStatusUpdates();
            subscribeToChipUpdates();
            System.out.println("Клиент зарегистрирован на сервере для получения обновлений.");
        } catch (Exception e) {
            System.err.println("Client not started. " + e.getMessage());
        }
    }

    private void subscribeToGameStatusUpdates() {
        StreamObserver<GameStatusResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(GameStatusResponse gameStatusResponse) {
                boolean gameStatus = gameStatusResponse.getGameIsStarted();

                if (!gameIsStarted && gameStatus) {
                    updater.onStartEvent();

                    if (setChipIsAllow) updater.setMessage("Ваш ход");
                    else updater.setMessage("Ход противника");
                }
                else if (gameIsStarted && !gameStatus) {
                    String winnerColor = gameStatusResponse.getWinnerColor();
                    if (winnerColor.equals(playerColor.string())) {
                        System.out.println("Игра завершена. Вы выйграли.");
                        updater.setMessage("Игра завершена. Вы выйграли.");
                    } else {
                        System.out.println("Игра заврешена. Вы проиграли.");
                        updater.setMessage("Игра завершена. Вы проиграли.");
                    }
                    gameIsStarted = false;
                    setChipIsAllow = false;
                }
            }

            @Override
            public void onError(Throwable err) {
                System.err.println(err.getMessage());
            }

            @Override
            public void onCompleted() {}
        };

        asyncStub.subscribeToGameStatusUpdates(empty, responseObserver);
    }

    private void subscribeToChipUpdates() {
        StreamObserver<ChipAddedResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ChipAddedResponse chipAddedResponse) {
                if (!gameIsStarted || !chipAddedResponse.getSuccess()) return;

                setChipIsAllow = chipAddedResponse.getChipIsAllow();
                ChipInfo chipInfo = chipAddedResponse.getChipInfo();
                Chip chip = new Chip(chipInfo.getX(), chipInfo.getY(), chipInfo.getColor());

                updater.drawNewChip(chip);
                if (setChipIsAllow) updater.setMessage("Ваш ход");
                else updater.setMessage("Ход противника");
            }

            @Override
            public void onError(Throwable err) {
                System.err.println(err.getMessage());

            }

            @Override
            public void onCompleted() {}
        };

        ClientInfo clientInfo = ClientInfo.newBuilder()
                .setClientColor(playerColor.string()).build();
        asyncStub.subscribeToChipUpdates(clientInfo, responseObserver);
    }

    public void startGame() {
        if (gameIsStarted) return;

        Response resp = blockingStub.startGame(empty);
        gameIsStarted = resp.getSuccess();
        if (!gameIsStarted) System.err.println(resp.getMessage());
        else System.out.println(resp.getMessage());
    }

    public Boolean setChip(Chip chip) {
        ChipInfo chipInfo = chip.toGrpc();
        Response resp = blockingStub.setChip(chipInfo);
        return resp.getSuccess();
    }

    public void nullifyTable() {
        Response resp = blockingStub.nullifyGame(empty);
    }

    private static String parseColor(String message) {
        int colorIndex = message.indexOf("Color: ") + "Color: ".length();
        return message.substring(colorIndex).trim().toUpperCase();
    }

    @Override
    public String toString() {
        return "ClientApp{" +
                "playerColor=" + playerColor +
                ", gameIsStarted=" + gameIsStarted +
                ", setChipIsAllow=" + setChipIsAllow +
                ", updater=" + updater +
                ", blockingStub=" + blockingStub +
                ", asyncStub=" + asyncStub +
                ", empty=" + empty +
                '}';
    }
}


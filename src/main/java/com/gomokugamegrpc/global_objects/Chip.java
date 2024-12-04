package com.gomokugamegrpc.global_objects;

import Gomoku.GomokuServiceOuterClass;
import Gomoku.GomokuServiceOuterClass.*;
import com.gomokugamegrpc.global_objects.enums.TableValue;
import javafx.scene.shape.Circle;

import java.io.Serializable;

public class Chip implements Serializable {
    private int x;
    private int y;
    private TableValue color;

    public Chip(int x, int y, TableValue color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Chip(int x, int y, String color) {
        this.x = x;
        this.y = y;
        this.color = switch (color) {
            case "WHITE" -> TableValue.WHITE;
            case "BLACK" -> TableValue.BLACK;
            default -> TableValue.NULL;
        };
    }

    public ChipInfo toGrpc() {
        return ChipInfo.newBuilder()
                .setX(x).setY(y)
                .setColor(color.string())
                .build();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public TableValue getColor() { return color; }

    public void setX(int x) throws Exception {
        if (x < 0 || x >= 15) throw new Exception("X out of bounds");
        this.x = x;
    }
    public void setY(int y) throws Exception {
        if (y < 0 || y >= 15) throw new Exception("Y out of bounds");
        this.y = y;
    }
    public void setColor(TableValue color) { this.color = color; }

    public Circle drawChip(TableValue color, double rad) {
        Circle circle = new Circle(rad);
        circle.setFill(color.getValue());
        circle.setCenterX(rad);
        circle.setCenterY(rad);

        return circle;
    }

    @Override
    public String toString() {
        return "Chip{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }
}

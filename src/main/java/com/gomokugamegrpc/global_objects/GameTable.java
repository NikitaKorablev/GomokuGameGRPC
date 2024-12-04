package com.gomokugamegrpc.global_objects;

import com.gomokugamegrpc.global_objects.enums.TableValue;

import java.util.ArrayList;

public class GameTable {
    private final ArrayList<ArrayList<TableValue>> table = new ArrayList<>();
    private final static int availableChipCount = 4;
    private static int tmpAvailableChipCount = availableChipCount;

    public GameTable() {
        for (int i = 0; i < 15; ++i) {
            ArrayList<TableValue> row = new ArrayList<>();
            for (int j = 0; j < 15; ++j) {
                row.add(TableValue.NULL);
            }
            table.add(row);
        }
    }

    public ArrayList<ArrayList<TableValue>> getTable() {
        return table;
    }

    public void setChip(Chip chip) {
        if (table.get(chip.getX()).get(chip.getY()) != TableValue.NULL) return;

        table.get(chip.getX()).set(chip.getY(), chip.getColor());
    }

    public boolean isWin(Chip chip) {
        if (checkLine(chip, 1, 1)) return true;
        if (checkLine(chip, 1, 0)) return true;
        if (checkLine(chip, 1, -1)) return true;
        return checkLine(chip, 0, 1);
    }

    private boolean checkLine(Chip chip, int x_up, int y_right) {
        int x = chip.getX()-x_up;
        int y = chip.getY()+y_right;
        updateTmpAvailableChipCount(x, y, chip.getColor(), x_up, y_right);

        x = chip.getX()-(-x_up);
        y = chip.getY()+(-y_right);
        updateTmpAvailableChipCount(x, y, chip.getColor(), -x_up, -y_right);

        boolean res = tmpAvailableChipCount == 0;
        tmpAvailableChipCount = availableChipCount;
        return res;
    }

    private void updateTmpAvailableChipCount(int x, int y, TableValue color, int x_up, int y_right) {
        if (x < 0 || y < 0 || x >= 15 || y >= 15) return;
        if (table.get(x).get(y) == TableValue.NULL) return;

        if (table.get(x).get(y) == color) tmpAvailableChipCount--;
        if (tmpAvailableChipCount < 0) return;

        x = x - x_up; y = y + y_right;
        updateTmpAvailableChipCount(x, y, color, x_up, y_right);
    }

    public void nullifyTable() {
        for (int i=0; i < 15; ++i) {
            for (int j=0; j < 15; ++j) {
                table.get(i).set(j, TableValue.NULL);
            }
        }
    }

    @Override
    public String toString() {
        return "GameTable{" +
                "table=" + table +
                '}';
    }
}

package com.gomokugamegrpc.client;

import com.gomokugamegrpc.global_objects.Chip;

public interface UpdateGameState {
    void drawNewChip(Chip chip);
    void onStartEvent();
    void setMessage(String message);
    void setChipIsEnabled(boolean isEnabled);
}

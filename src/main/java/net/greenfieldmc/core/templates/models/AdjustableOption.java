package net.greenfieldmc.core.templates.models;

public interface AdjustableOption<T> {

    String getChatName();

    String getDescription();

    T getAdjustmentValue();
}

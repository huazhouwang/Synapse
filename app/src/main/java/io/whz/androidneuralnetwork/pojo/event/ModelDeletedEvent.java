package io.whz.androidneuralnetwork.pojo.event;

import io.whz.androidneuralnetwork.pojo.dao.Model;

public class ModelDeletedEvent extends NormalEvent<Model> {
    public ModelDeletedEvent(Model obj) {
        super(obj);
    }
}

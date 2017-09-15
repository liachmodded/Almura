/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.config;

import net.minecraftforge.fml.common.eventhandler.GenericEvent;

public class ConfigLoadEvent<C extends Configuration> extends GenericEvent<C> {

    private final ConfigurationAdapter<C> adapter;

    public ConfigLoadEvent(final Class<C> type, final ConfigurationAdapter<C> adapter) {
        super(type);
        this.adapter = adapter;
    }

    public ConfigurationAdapter<C> adapter() {
        return this.adapter;
    }

    public C config() {
        return this.adapter.getConfig();
    }
}

/*
 *     SPDX-License-Identifier: GPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.sirywell.noisypatterns.property;

import net.royawesome.jlibnoise.module.Module;

import java.lang.invoke.MethodHandle;

sealed public abstract class PrimitiveProperty<T>
        extends Property<T>
        permits BooleanProperty, DoubleProperty, IntProperty
{
    private final MethodHandle setter;

    PrimitiveProperty(String name, MethodHandle setter) {
        super(name);
        this.setter = setter;
    }

    @Override
    protected void setUnchecked(Module module, T value) throws Throwable {
        this.setter.invoke(module, value);
    }
}

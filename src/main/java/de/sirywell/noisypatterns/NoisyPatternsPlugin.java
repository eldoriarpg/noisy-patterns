/*
 *     SPDX-License-Identifier: GPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.sirywell.noisypatterns;

import com.sk89q.worldedit.WorldEdit;
import org.bukkit.plugin.java.JavaPlugin;

public class NoisyPatternsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        WorldEdit instance = WorldEdit.getInstance();
        instance.getPatternFactory().register(new NoisePatternParser(instance));
    }
}

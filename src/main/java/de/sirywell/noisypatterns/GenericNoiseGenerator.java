package de.sirywell.noisypatterns;

import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.noise.NoiseGenerator;
import net.royawesome.jlibnoise.module.Module;

public class GenericNoiseGenerator implements NoiseGenerator {
    private final Module rootModule;

    public GenericNoiseGenerator(Module rootModule) {
        this.rootModule = rootModule;
    }

    @Override
    public float noise(Vector2 position) {
        return this.forceRange(this.rootModule.GetValue(position.getX(), 0.0, position.getZ()));
    }

    @Override
    public float noise(Vector3 position) {
        return this.forceRange(this.rootModule.GetValue(position.getX(), position.getY(), position.getZ()));
    }

    private float forceRange(double value) {
        return (float)Math.max(0.0, Math.min(1.0, value / 2.0 + 0.5));
    }
}

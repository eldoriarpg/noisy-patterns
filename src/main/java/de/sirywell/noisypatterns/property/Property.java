package de.sirywell.noisypatterns.property;

import de.sirywell.noisypatterns.Lexer;
import de.sirywell.noisypatterns.NoisePatternParser;
import net.royawesome.jlibnoise.module.Module;

import java.util.Queue;
import java.util.stream.Stream;

sealed public abstract class Property<T> permits ModuleProperty, PrimitiveProperty, Tuple2Property, Tuple3Property {
    private final String name;

    Property(String name) {
        this.name = name;
    }

    protected abstract T parseValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context);

    protected abstract Stream<String> suggestValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context);

    protected final void parseAndSet(Module current, Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
        T value = parseValue(input, context);
        set(current, value);
    }

    public final void set(Module module, T value) {
        try {
            setUnchecked(module, value);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void setUnchecked(Module module, T value) throws Throwable;

    public String name() {
        return name;
    }
}

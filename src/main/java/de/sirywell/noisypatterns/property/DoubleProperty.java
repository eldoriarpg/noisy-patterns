package de.sirywell.noisypatterns.property;

import com.sk89q.worldedit.command.util.SuggestionHelper;
import de.sirywell.noisypatterns.Lexer;
import de.sirywell.noisypatterns.NoisePatternParser;

import java.lang.invoke.MethodHandle;
import java.util.Queue;
import java.util.stream.Stream;

public final class DoubleProperty extends PrimitiveProperty<Double> {

    public DoubleProperty(String name, MethodHandle setter) {
        super(name, setter);
    }

    @Override
    protected Double parseValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
        return Double.parseDouble(ModuleProperty.expectIdentifier(input));
    }

    @Override
    protected Stream<String> suggestValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
        if (input.isEmpty()) {
            return SuggestionHelper.suggestPositiveDoubles("");
        }
        if (!(input.remove() instanceof Lexer.IdentifierToken identifier)) {
            return Stream.empty();
        }
        return SuggestionHelper.suggestPositiveDoubles(identifier.identifier());
    }
}

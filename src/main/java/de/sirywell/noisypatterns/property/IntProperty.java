/*
 *     SPDX-License-Identifier: GPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.sirywell.noisypatterns.property;

import com.sk89q.worldedit.command.util.SuggestionHelper;
import de.sirywell.noisypatterns.Lexer;
import de.sirywell.noisypatterns.NoisePatternParser;

import java.lang.invoke.MethodHandle;
import java.util.Queue;
import java.util.stream.Stream;

public final class IntProperty extends PrimitiveProperty<Integer> {

    public IntProperty(String name, MethodHandle setter) {
        super(name, setter);
    }

    @Override
    protected Integer parseValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
        return Integer.parseInt(ModuleProperty.expectIdentifier(input));
    }

    @Override
    protected Stream<String> suggestValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
        if (input.isEmpty()) {
            return SuggestionHelper.suggestPositiveIntegers("");
        }
        if (!(input.remove() instanceof Lexer.IdentifierToken identifier)) {
            return Stream.empty();
        }
        return SuggestionHelper.suggestPositiveIntegers(identifier.identifier());
    }
}

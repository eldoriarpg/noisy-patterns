/*
 *     SPDX-License-Identifier: GPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.sirywell.noisypatterns;

import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.math.random.NoiseRandom;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import de.sirywell.noisypatterns.property.ModuleProperty;
import net.royawesome.jlibnoise.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Stream;

public class NoisePatternParser extends RichParser<Pattern> implements AliasedParser {
    private static final String PREFIX = "#noise";

    protected NoisePatternParser(WorldEdit worldEdit) {
        super(worldEdit, PREFIX);
    }

    @Override
    protected Stream<String> getSuggestions(String argumentInput, int index) {
        if (index == 1) {
            return worldEdit.getPatternFactory().getSuggestions(argumentInput).stream();
        }
        return stringStream(argumentInput, index);
    }

    @Override
    protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
        if (index == 1) {
            return worldEdit.getPatternFactory().getSuggestions(argumentInput, context).stream();
        }
        return stringStream(argumentInput, index);
    }

    private Stream<String> stringStream(String argumentInput, int index) {
        if (index == 0) {
            List<Lexer.Token> tokens = new Lexer().lex(argumentInput);
            ArrayDeque<Lexer.Token> queue = new ArrayDeque<>(tokens);
            return ModuleProperty.suggestRoot(queue);
        }
        if (index == 2) {
            return SuggestionHelper.suggestPositiveDoubles(argumentInput);
        }
        return Stream.empty();
    }

    @Override
    protected Pattern parseFromInput(@NotNull String @NotNull [] arguments, ParserContext context) throws InputParseException {
        if (arguments.length != 2 && arguments.length != 3) {
            throw new InputParseException(TextComponent.of("Incomplete noise. Example usage: #noise[const[value=0.5]][stone,dirt]"));
        }
        List<Lexer.Token> tokens = new Lexer().lex(arguments[0]);
        ArrayDeque<Lexer.Token> queue = new ArrayDeque<>(tokens);
        Module rootModule = ModuleProperty.parseRoot(queue);
        Pattern pattern = worldEdit.getPatternFactory().parseFromInput(arguments[1], context);
        if (!(pattern instanceof RandomPattern rp)) {
            throw new InputParseException(TextComponent.of("Not a random pattern"));
        }
        double scale = 1d;
        if (arguments.length == 3) {
            scale = parseScale(arguments[2]);
        }
        return new RandomPattern(new NoiseRandom(new GenericNoiseGenerator(rootModule), scale), rp);
    }

    private static double parseScale(String argument) {
        double scale = Double.parseDouble(argument);
        return 1d / Math.max(1, scale);
    }
}

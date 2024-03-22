/*
 *     SPDX-License-Identifier: GPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.sirywell.noisypatterns;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public enum TokenType {
        IDENTIFIER, // everything that isn't something else
        BRACKET_OPEN,
        BRACKET_CLOSE,
        EQUALS,
        COMMA,
    }

    public sealed interface Token {
        TokenType type();
    }

    public record IdentifierToken(String identifier) implements Token {

        @Override
        public TokenType type() {
            return TokenType.IDENTIFIER;
        }

        @Override
        public String toString() {
            return identifier();
        }
    }

    public enum DefaultTokens implements Token {
        BRACKET_OPEN_TOKEN(TokenType.BRACKET_OPEN, "["),
        BRACKET_CLOSE_TOKEN(TokenType.BRACKET_CLOSE, "]"),
        EQUALS_TOKEN(TokenType.EQUALS, "="),
        COMMA_TOKEN(TokenType.COMMA, ","),
        ;
        private final TokenType type;
        private final String representation;

        DefaultTokens(TokenType type, String representation) {
            this.type = type;
            this.representation = representation;
        }


        @Override
        public TokenType type() {
            return type;
        }

        @Override
        public String toString() {
            return representation;
        }
    }

    public List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        int start = 0;
        int pos = 0;
        while (pos < input.length()) {
            boolean resetStart = switch (input.charAt(pos)) {
                case '=' -> finishPreviousToken(input, start, pos, tokens, DefaultTokens.EQUALS_TOKEN);
                case '[' -> finishPreviousToken(input, start, pos, tokens, DefaultTokens.BRACKET_OPEN_TOKEN);
                case ']' -> finishPreviousToken(input, start, pos, tokens, DefaultTokens.BRACKET_CLOSE_TOKEN);
                case ',' -> finishPreviousToken(input, start, pos, tokens, DefaultTokens.COMMA_TOKEN);
                default -> false;
            };
            pos++;
            if (resetStart) {
                start = pos;
            }
        }
        if (start != pos) {
            finishPreviousToken(input, start, pos, tokens, null);
        }
        return tokens;
    }

    private static boolean finishPreviousToken(String input, int start, int pos, List<Token> tokens, Token additional) {
        if (start != pos) {
            tokens.add(new IdentifierToken(input.substring(start, pos)));
        }
        if (additional != null) {
            tokens.add(additional);
        }
        return true;
    }
}

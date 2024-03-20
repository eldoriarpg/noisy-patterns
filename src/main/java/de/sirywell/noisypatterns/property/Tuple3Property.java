package de.sirywell.noisypatterns.property;

import de.sirywell.noisypatterns.Lexer;
import de.sirywell.noisypatterns.util.Tuple2;
import de.sirywell.noisypatterns.util.Tuple3;
import net.royawesome.jlibnoise.module.Module;

import java.lang.invoke.MethodHandle;
import java.util.Queue;
import java.util.stream.Stream;

public final class Tuple3Property<A, B, C> extends Property<Tuple3<A, B, C>> {
	private final Property<A> firstProperty;
	private final Property<B> secondProperty;
	private final Property<C> thirdProperty;
	private final MethodHandle setter;

	Tuple3Property(String name, Property<A> firstProperty, Property<B> secondProperty, Property<C> thirdProperty, MethodHandle setter) {
		super(name);
		this.firstProperty = firstProperty;
		this.secondProperty = secondProperty;
		this.thirdProperty = thirdProperty;
		this.setter = setter;
	}

	@Override
	protected Tuple3<A, B, C> parseValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
		ModuleProperty.expect(Lexer.TokenType.BRACKET_OPEN, input);
		A first = this.firstProperty.parseValue(input, context);
		ModuleProperty.expect(Lexer.TokenType.COMMA, input);
		B second = this.secondProperty.parseValue(input, context);
		ModuleProperty.expect(Lexer.TokenType.COMMA, input);
		C third = this.thirdProperty.parseValue(input, context);
		ModuleProperty.expect(Lexer.TokenType.BRACKET_CLOSE, input);
		return new Tuple3<>(first, second, third);
	}

	@Override
	protected Stream<String> suggestValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
		if (input.isEmpty() || input.element().type() != Lexer.TokenType.BRACKET_OPEN) {
			return Stream.empty();
		}
		input.remove(); // BRACKET_OPEN
		if (input.isEmpty()) {
			return this.firstProperty.suggestValue(input, context).map(s -> "[" + s);
		}
		if (input.size() == 1) {
			return this.firstProperty.suggestValue(input, context).map(s -> "[" + s + ",");
		}
		Lexer.Token firstId = input.remove();
		if (firstId.type() != Lexer.TokenType.IDENTIFIER) {
			return Stream.empty();
		}
		if (input.remove().type() != Lexer.TokenType.COMMA) {
			return Stream.empty();
		}
		if (input.isEmpty()) {
			return this.secondProperty.suggestValue(input, context).map(s -> "[" + firstId + "," + s);
		}
		if (input.size() == 1) {
			return this.secondProperty.suggestValue(input, context).map(s -> "[" + firstId + "," + s + ",");
		}
		Lexer.Token secondId = input.remove();
		if (secondId.type() != Lexer.TokenType.IDENTIFIER) {
			return Stream.empty();
		}
		if (input.remove().type() != Lexer.TokenType.COMMA) {
			return Stream.empty();
		}
		if (input.isEmpty()) {
			return this.thirdProperty.suggestValue(input, context).map(s -> "[" + firstId + "," + secondId + "," + s);
		}
		if (input.size() == 1) {
			return this.thirdProperty.suggestValue(input, context).map(s -> "[" + firstId + "," + secondId + "," + s + "]");
		}
		if (input.remove().type() != Lexer.TokenType.IDENTIFIER) {
			return Stream.empty();
		}
		input.remove(); // assume BRACKET_CLOSE
		return Stream.empty();
	}

	@Override
	protected void setUnchecked(Module module, Tuple3<A, B, C> value) throws Throwable {
		this.setter.invoke(module, value);
	}
}

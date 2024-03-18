package de.sirywell.noisypatterns.property;

import de.sirywell.noisypatterns.Lexer;
import de.sirywell.noisypatterns.util.Tuple2;
import net.royawesome.jlibnoise.module.Module;

import java.lang.invoke.MethodHandle;
import java.util.Queue;
import java.util.stream.Stream;

public final class Tuple2Property<A, B> extends Property<Tuple2<A, B>> {
	private final Property<A> firstProperty;
	private final Property<B> secondProperty;
	private final MethodHandle setter;

	Tuple2Property(String name, Property<A> firstProperty, Property<B> secondProperty, MethodHandle setter) {
		super(name);
		this.firstProperty = firstProperty;
		this.secondProperty = secondProperty;
		this.setter = setter;
	}

	@Override
	protected Tuple2<A, B> parseValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
		ModuleProperty.expect(Lexer.TokenType.BRACKET_OPEN, input);
		A first = this.firstProperty.parseValue(input, context);
		ModuleProperty.expect(Lexer.TokenType.COMMA, input);
		B second = this.secondProperty.parseValue(input, context);
		ModuleProperty.expect(Lexer.TokenType.BRACKET_CLOSE, input);
		return new Tuple2<>(first, second);
	}

	@Override
	protected Stream<String> suggestValue(Queue<Lexer.Token> input, ModuleProperty.ModuleParseContext context) {
		return Stream.empty();
	}

	@Override
	protected void setUnchecked(Module module, Tuple2<A, B> value) throws Throwable {
		this.setter.invoke(module, value);
	}
}

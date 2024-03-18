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
		return Stream.empty();
	}

	@Override
	protected void setUnchecked(Module module, Tuple3<A, B, C> value) throws Throwable {
		this.setter.invoke(module, value);
	}
}

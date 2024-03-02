package de.sirywell.noisypatterns.property;

import de.sirywell.noisypatterns.Lexer;
import de.sirywell.noisypatterns.NoisePatternParser;
import net.royawesome.jlibnoise.module.Cache;
import net.royawesome.jlibnoise.module.Module;
import net.royawesome.jlibnoise.module.combiner.*;
import net.royawesome.jlibnoise.module.modifier.*;
import net.royawesome.jlibnoise.module.source.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ModuleProperty extends Property<Module> {
    private static final Map<String, ModuleData> MODULE_DATA;
    private static final MethodHandle MODULE_SETTER;
    private static final List<ModuleProperty> indexed = new ArrayList<>();

    static {
        List<Class<? extends Module>> moduleTypes = List.of(
                Abs.class,
                Add.class,
                Billow.class,
                Blend.class,
                Cache.class,
                Checkerboard.class,
                Clamp.class,
                Const.class,
                Curve.class,
                Cylinders.class,
                Displace.class,
                Exponent.class,
                Invert.class,
                Max.class,
                Min.class,
                Multiply.class,
                Perlin.class,
                Power.class,
                RidgedMulti.class,
                RotatePoint.class,
                ScaleBias.class,
                ScalePoint.class,
                Select.class,
                Spheres.class,
                Terrace.class,
                TranslatePoint.class,
                Turbulence.class,
                Voronoi.class
        );
        Map<String, ModuleData> modules = new HashMap<>();
        for (Class<? extends Module> type : moduleTypes) {
            try {
                ModuleData moduleData = scanModule(type);
                modules.put(camelCase(type.getSimpleName()), moduleData);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        MODULE_DATA = Map.copyOf(modules);
    }

    static {
        MethodHandle methodHandle;
        try {
            MethodType type = MethodType.methodType(void.class, int.class, Module.class);
            methodHandle = MethodHandles.lookup().findVirtual(Module.class, "SetSourceModule", type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        MODULE_SETTER = methodHandle;
    }


    private final int pos;

    public static Module parseRoot(Queue<Lexer.Token> input) {
        return parseCommon(input, new ModuleParseContext(MODULE_DATA));
    }

    public static List<ModuleProperty> get(int count) {
        while (count > indexed.size()) {
            indexed.add(new ModuleProperty(indexed.size()));
        }
        return indexed.subList(0, count);

    }

    private ModuleProperty(int pos) {
        super(String.valueOf(pos));
        this.pos = pos;
    }

    public static Stream<String> suggestRoot(Queue<Lexer.Token> input) {
        return suggestCommon(input, new ModuleParseContext(MODULE_DATA));
    }

    private static Stream<String> suggestCommon(Queue<Lexer.Token> input, ModuleParseContext context) {
        Object initial = suggestAllOrMatching(input, context.moduleData().keySet());
        if (initial instanceof Stream<?> stream) {
            return stream.map(s -> s + "[");
        }
        String identifier = (String) initial;
        ModuleData moduleData = context.moduleData().get(identifier);
        if (moduleData == null) {
            return Stream.empty();
        }
        Lexer.Token token = input.element();
        if (token.type() != Lexer.TokenType.BRACKET_OPEN) {
            return Stream.empty();
        }
        input.remove();
        String prefix = identifier + "[";
        Stream<String> last;
        List<Lexer.Token> before = new ArrayList<>(input);
        do {
            initial = suggestAllOrMatching(input, moduleData.properties().keySet());
            if (initial instanceof Stream<?> stream) {
                int toIndex = before.size() - input.size();
                if (toIndex > 0 && before.get(toIndex - 1).type() == Lexer.TokenType.IDENTIFIER) {
                    toIndex--; // this is what we're already completing, no need to suggest it
                }
                for (Lexer.Token t : before.subList(0, toIndex)) {
                    prefix += t;
                }
                String finalPrefix = prefix;
                return stream.map(s -> finalPrefix + s + "=");
            }
            identifier = (String) initial;
            Property<?> property = moduleData.properties().get(identifier);
            if (property == null) {
                return Stream.empty();
            }
            token = input.remove();
            if (token.type() != Lexer.TokenType.EQUALS) {
                return Stream.empty();
            }
            // a previous "last" instance
            List<Lexer.Token> prev = before.subList(0, before.size() - input.size());
            for (Lexer.Token t : prev) {
                prefix += t;
            }
            prev.clear();
            last = property.suggestValue(input, context);
        } while (consumeIfType(Lexer.TokenType.COMMA, input));
        if (consumeIfType(Lexer.TokenType.BRACKET_CLOSE, input)) {
            for (Lexer.Token t : before.subList(0, before.size() - input.size())) {
                prefix += t;
            }
            prefix += ",";
            return Stream.of(prefix);
        }
        String finalPrefix = prefix;
        return last.map(s -> finalPrefix + s);
    }

    private static Object suggestAllOrMatching(Queue<Lexer.Token> input, Collection<String> all) {
        if (input.isEmpty()) {
            return all.stream();
        }
        Lexer.Token token = input.remove();
        if (!(token instanceof Lexer.IdentifierToken identifier)) {
            return Stream.empty();
        }
        if (input.isEmpty()) {
            return all.stream().filter(s -> s.startsWith(identifier.identifier()));
        }
        return identifier.identifier();
    }

    @Override
    protected Module parseValue(Queue<Lexer.Token> input, ModuleParseContext context) {
        return parseCommon(input, context);
    }

    @Override
    protected Stream<String> suggestValue(Queue<Lexer.Token> input, ModuleParseContext context) {
        return suggestCommon(input, context);
    }

    private static Module parseCommon(Queue<Lexer.Token> input, ModuleParseContext context) {
        String moduleName = expectIdentifier(input);
        Module module = context.getModuleData(moduleName).moduleFactory().get();
        if (!consumeIfType(Lexer.TokenType.BRACKET_OPEN, input)) {
            return module;
        }
        do {
            String key = expectIdentifier(input);
            Property<?> property = context.getModuleData(moduleName).properties().get(key);
            expect(Lexer.TokenType.EQUALS, input);
            property.parseAndSet(module, input, context);
        } while (consumeIfType(Lexer.TokenType.COMMA, input));
        expect(Lexer.TokenType.BRACKET_CLOSE, input);
        return module;
    }

    @Override
    protected void setUnchecked(Module module, Module value) throws Throwable {
        MODULE_SETTER.invokeExact(module, pos, value);
    }

    private record ModuleData(Map<String, Property<?>> properties, Supplier<Module> moduleFactory) {
    }

    private record PropertyMethod(String propertyName, Class<?> parameterType) {
    }

    private static <M extends Module> ModuleData scanModule(Class<M> moduleClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Constructor<M> constructor = moduleClass.getConstructor();
        M instance = constructor.newInstance();
        int i = instance.GetSourceModuleCount();
        Method[] methods = moduleClass.getMethods();
        Map<String, Property<?>> properties = ModuleProperty.get(i).stream()
                .collect(Collectors.toMap(Property::name, Function.identity(), (a, b) -> a, HashMap::new));
        for (Method method : methods) {
            PropertyMethod propertyMethod = getPropertyMethod(method);
            if (propertyMethod == null) {
                continue;
            }
            String propertyName = propertyMethod.propertyName();
            Class<?> type = propertyMethod.parameterType();
            MethodHandle setter = lookup.unreflect(method);
            Property<?> property = forType(propertyName, setter, type);
            if (property == null) {
                System.err.println("unsupported: " + type + " in " + moduleClass + "#" + method.getName());
                continue;
            }
            properties.put(propertyName, property);
        }
        class ModuleSupplier implements Supplier<Module> {

            @Override
            public Module get() {
                try {
                    return constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ModuleData(Map.copyOf(properties), new ModuleSupplier());
    }

    private static Property<?> forType(String propertyName, MethodHandle setter, Class<?> type) {
        if (type == boolean.class) {
            return new BooleanProperty(propertyName, setter);
        } else if (type == int.class) {
            return new IntProperty(propertyName, setter);
        } else if (type == double.class) {
            return new DoubleProperty(propertyName, setter);
        }
        return null;
    }

    protected record ModuleParseContext(
            Map<String, ModuleData> moduleData
    ) {
        ModuleData getModuleData(String moduleName) {
            return Objects.requireNonNull(
                    moduleData().get(moduleName),
                    () -> "No module with name '" + moduleName + "' exists"
            );
        }
    }

    private static boolean consumeIfType(Lexer.TokenType type, Queue<Lexer.Token> input) {
        if (peekType(type, input)) {
            input.remove();
            return true;
        }
        return false;
    }

    private static boolean peekType(Lexer.TokenType type, Queue<Lexer.Token> input) {
        Lexer.Token peek = input.peek();
        if (peek == null) {
            return false;
        }
        return peek.type() == type;
    }

    static String expectIdentifier(Queue<Lexer.Token> input) {
        Lexer.Token expect = expect(Lexer.TokenType.IDENTIFIER, input);
        return ((Lexer.IdentifierToken) expect).identifier();
    }

    private static Lexer.Token expect(Lexer.TokenType type, Queue<Lexer.Token> input) {
        Lexer.Token remove = input.remove();
        if (remove.type() != type) {
            throw new RuntimeException("wrong type, expected " + type);
        }
        return remove;
    }

    private static String camelCase(String string) {
        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    private static String getPropertyName(Method method) {
        String name = method.getName();
        if (name.startsWith("set") || name.startsWith("Set")) {
            name = name.substring(3);
        }
        return camelCase(name);
    }

    private static PropertyMethod getPropertyMethod(Method method) {
        if (method.getReturnType() != void.class) return null;
        if (method.getParameterCount() != 1) return null;
        // skip methods coming from irrelevant super types
        if (method.getDeclaringClass().getModule() == Object.class.getModule()) return null;
        Class<?> parameterType = method.getParameterTypes()[0];
        if (parameterType.isPrimitive()) {
            return new PropertyMethod(getPropertyName(method), parameterType);
        }
        return null;
    }
}

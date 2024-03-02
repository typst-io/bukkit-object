package io.typst.bukkit.object;

import lombok.Value;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class TypeDef {
    Class<?> javaClass;
    List<TypeDef> typeParameters;
    public static final TypeDef object = new TypeDef(Object.class, Collections.emptyList());
    public static final TypeDef empty = new TypeDef(Void.class, Collections.emptyList());

    public static Optional<TypeDef> from(Type type) {
        // has generic
        if (type instanceof ParameterizedType) {
            TypeDef baseType = from(((ParameterizedType) type).getRawType()).orElse(null);
            if (baseType != null) {
                List<TypeDef> typeParams = Arrays.stream(((ParameterizedType) type).getActualTypeArguments())
                        .flatMap(a -> from(a).map(Stream::of).orElse(Stream.empty()))
                        .collect(Collectors.toList());
                return Optional.of(new TypeDef(baseType.getJavaClass(), typeParams));
            }
        }
        // none generic class
        if (type instanceof Class) {
            return Optional.of(new TypeDef(((Class<?>) type), Collections.emptyList()));
        }
        return Optional.empty();
    }
}

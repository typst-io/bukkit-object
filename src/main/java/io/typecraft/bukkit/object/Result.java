package io.typecraft.bukkit.object;

import lombok.Value;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Result<A> {
    static <A> Result<A> success(A a) {
        return new Success<>(a);
    }

    static <A> Result<A> failure(Throwable failure) {
        return new Failure<>(failure);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <A> Result<A> fromOptional(Optional<A> opt, Supplier<Throwable> f) {
        A a = opt.orElse(null);
        if (a != null) {
            return Result.success(a);
        }
        return Result.failure(f.get());
    }

    default A get() {
        if (this instanceof Success) {
            Success<A> succ = (Success<A>) this;
            return succ.getValue();
        }
        return null;
    }

    default A getOrElse(A elseValue) {
        return asOptional().orElse(elseValue);
    }

    default A getOrThrow() {
        Throwable failure = getFailure().orElse(null);
        if (failure != null) {
            throw new IllegalStateException(failure);
        }
        return get();
    }

    default <B> Result<B> map(Function<A, B> f) {
        if (this instanceof Success) {
            Success<A> succ = (Success<A>) this;
            return new Success<>(f.apply(succ.get()));
        } else if (this instanceof Failure) {
            Failure<A> failure = (Failure<A>) this;
            return new Failure<>(failure.getThrowable());
        }
        throw new UnsupportedOperationException();
    }

    default <B> Result<B> flatMap(Function<A, Result<B>> f) {
        if (this instanceof Success) {
            Success<A> succ = (Success<A>) this;
            return f.apply(succ.get());
        } else if (this instanceof Failure) {
            Failure<A> failure = (Failure<A>) this;
            return new Failure<>(failure.getThrowable());
        }
        throw new UnsupportedOperationException();
    }

    default Optional<Throwable> getFailure() {
        if (this instanceof Failure) {
            Failure<A> failure = (Failure<A>) this;
            return Optional.of(failure.getThrowable());
        }
        return Optional.empty();
    }

    default boolean isSuccess() {
        return get() != null;
    }

    default Optional<A> asOptional() {
        return Optional.ofNullable(get());
    }

    @Value
    class Success<A> implements Result<A> {
        A value;

        private Success(A value) {
            this.value = value;
        }
    }

    @Value
    class Failure<A> implements Result<A> {
        Throwable throwable;

        private Failure(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}

package com.niyiment.patientservice.common;

import java.util.function.Function;

/**
 * Result pattern for functional error handling.
 * Replaces exception-based error handling with explicit success/failure types.
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    /**
     * Creates a successful result with a value.
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed result with an error.
     */
    static <T> Result<T> failure(ResultError error) {
        return new Failure<>(error);
    }

    /**
     * Creates a failed result with error details.
     */
    static <T> Result<T> failure(String code, String message) {
        return new Failure<>(new ResultError(code, message));
    }

    /**
     * Checks if the result is successful.
     */
    boolean isSuccess();

    /**
     * Checks if the result is a failure.
     */
    boolean isFailure();

    /**
     * Gets the value if successful, throws if failure.
     */
    T getValue();

    /**
     * Gets the error if failure, throws if successful.
     */
    ResultError getError();

    /**
     * Maps the success value to a new type.
     */
    <U> Result<U> map(Function<T, U> mapper);

    /**
     * Flat maps the success value to a new Result.
     */
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);

    /**
     * Returns the value if successful, otherwise returns the default value.
     */
    T orElse(T defaultValue);

    /**
     * Returns the value if successful, otherwise computes a default value from the error.
     */
    T orElseGet(Function<ResultError, T> supplier);

    record Success<T>(T value) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public ResultError getError() {
            throw new UnsupportedOperationException("Success result does not have an error");
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            return new Success<>(mapper.apply(value));
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public T orElse(T defaultValue) {
            return value;
        }

        @Override
        public T orElseGet(Function<ResultError, T> supplier) {
            return value;
        }
    }

    record Failure<T>(ResultError error) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public T getValue() {
            throw new UnsupportedOperationException("Failure result does not have a value: " + error.message());
        }

        @Override
        public ResultError getError() {
            return error;
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            @SuppressWarnings("unchecked")
            Result<U> result = (Result<U>) this;
            return result;
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            @SuppressWarnings("unchecked")
            Result<U> result = (Result<U>) this;
            return result;
        }

        @Override
        public T orElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T orElseGet(Function<ResultError, T> supplier) {
            return supplier.apply(error);
        }
    }
}
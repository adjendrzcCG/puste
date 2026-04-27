package com.example.puste.model;

/**
 * Sealed interface hierarchy for the result of a service operation.
 * Java 21 feature: Sealed classes used together with pattern matching
 * so callers can handle every outcome without casting.
 */
public sealed interface OperationResult<T>
        permits OperationResult.Success, OperationResult.Failure, OperationResult.NotFound {

    /** Successful result carrying a payload. */
    record Success<T>(T value) implements OperationResult<T> {}

    /** Failure result carrying an error message. */
    record Failure<T>(String reason) implements OperationResult<T> {}

    /** Signals that the requested resource does not exist. */
    record NotFound<T>(long id) implements OperationResult<T> {}
}

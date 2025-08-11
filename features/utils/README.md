# Utilities Module

This module provides multiplatform utility abstractions for error handling, used throughout the [AI Advent 2025 application](../../README.md).

## Features
- **Typed result type**: `Result<F, S>` for error (`F`) or success (`S`). Extension functions for easy creation and chaining.

---

## Typed Result (`Result<F, S>`)  
A sealed class for representing success or failure in a type-safe, functional style.

### Example
```kotlin
val result: Result<String, Int> = 42.asSuccess()
val error: Result<String, Int> = "Oops".asFailure()

val chained = result.next { value -> (value + 1).asSuccess() }
val folded = result.fold(
    onSuccess = { "Value: $it" },
    onFailure = { "Error: $it" }
)
```

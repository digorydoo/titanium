package ch.digorydoo.titanium.engine.utils

// An Exception represents an abnormal condition that code may reasonably want to handle.
//
// RuntimeException was intended for Java and no longer has a checked/unchecked role in Kotlin. Kotlin’s standard
// library uses it to maintain interoperability with Java, but for Kotlin-only code, the distinction is not relevant.
//
// Error represents serious failures at the runtime or environment level. Application code should not normally catch
// Error or define its own subclasses. Some errors such as NotImplementedError are OK for throwing from application
// code.
//
// With other words, Kotlin-only code should always define its own errors as subclasses of Exception. Therefore, the
// suffix ~Exception is no longer relevant and may be dropped.

class NotForProductionException: Exception("This feature is not meant for production")

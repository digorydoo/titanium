package ch.digorydoo.titanium.engine.utils

// An Exception is a Throwable that a reasonable application should try to catch.
// An Error is a Throwable indicating a serious problem that a reasonable application should not try to catch.
// A RuntimeException is an Exception that does not need to be declared in Java; don't use it in Kotlin, ever.
// Therefore, most exceptions in Kotlin should be instances of Exception.

class NotForProductionError(): Error("not for production")

@file:Suppress("unused")

package ch.digorydoo.titanium.engine.utils

import ch.digorydoo.kutils.string.toPrecision

import kotlin.test.assertTrue

// Kotlin has assertContains(range, value, msg), but these functions describe the error message better.
// Copied from kutils

fun assertWithin(range: ClosedRange<Float>, value: Float, msg: String? = null) =
    assertTrue(
        value in range,
        (msg?.let { "$msg: " } ?: "") + "Expected ${value.toPrecision(6)} to be inside range " +
            range.start.toPrecision(6) + " .. " + range.endInclusive.toPrecision(6)
    )

fun assertWithin(range: ClosedRange<Double>, value: Double, msg: String? = null) =
    assertTrue(
        value in range,
        (msg?.let { "$msg: " } ?: "") + "Expected ${value.toPrecision(8)} to be inside range " +
            range.start.toPrecision(8) + " .. " + range.endInclusive.toPrecision(8)
    )

fun assertWithin(range: OpenEndRange<Float>, value: Float, msg: String? = null) =
    assertTrue(
        value in range,
        (msg?.let { "$msg: " } ?: "") + "Expected ${value.toPrecision(6)} to be inside range " +
            range.start.toPrecision(6) + " ..< " + range.endExclusive.toPrecision(6)
    )

fun assertWithin(range: OpenEndRange<Double>, value: Double, msg: String? = null) =
    assertTrue(
        value in range,
        (msg?.let { "$msg: " } ?: "") + "Expected ${value.toPrecision(8)} to be inside range " +
            range.start.toPrecision(8) + " ..< " + range.endExclusive.toPrecision(8)
    )

fun <T: Comparable<T>> assertLessThan(value: T, expectedMax: T, msg: String? = null) =
    assertTrue(
        value < expectedMax,
        (msg?.let { "$msg: " } ?: "") + "Expected $value to be less than $expectedMax"
    )

fun <T: Comparable<T>> assertLessOrEqual(value: T, expectedMax: T, msg: String? = null) =
    assertTrue(
        value <= expectedMax,
        (msg?.let { "$msg: " } ?: "") + "Expected $value to be less or equal $expectedMax"
    )

fun <T: Comparable<T>> assertGreaterThan(value: T, expectedMin: T, msg: String? = null) =
    assertTrue(
        value > expectedMin,
        (msg?.let { "$msg: " } ?: "") + "Expected $value to be greater than $expectedMin"
    )

fun <T: Comparable<T>> assertGreaterOrEqual(value: T, expectedMin: T, msg: String? = null) =
    assertTrue(
        value >= expectedMin,
        (msg?.let { "$msg: " } ?: "") + "Expected $value to be greater or equal $expectedMin"
    )

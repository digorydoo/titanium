package ch.digorydoo.titanium.engine.utils

/**
 * A small Float that's still significantly larger than Float.MIN_VALUE. May be used to avoid division by almost zero.
 * (Float.MIN_VALUE is about 1.4E-45.)
 */
const val EPSILON = 0.000001f

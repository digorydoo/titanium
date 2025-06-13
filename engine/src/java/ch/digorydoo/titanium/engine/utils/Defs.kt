package ch.digorydoo.titanium.engine.utils

/**
 * A small Float that's still significantly larger than Float.MIN_VALUE. May be used to avoid division by almost zero.
 * (Float.MIN_VALUE is about 1.4E-45.)
 */
const val EPSILON = 0.000001f

/**
 * A small gap in world coordinates. Used by collision strategies to ensure a minimal distance that avoids collision
 * due to floating point inaccuracies. Apparently this also helps reduce the number of iterations needed to fully
 * separate bodies when there is a "congestion".
 */
const val TINY_GAP = 0.00005f // 5 mm/100

package com.steelrazor47.scantrino.utils

import kotlin.math.max
import kotlin.math.min

fun String.similarity(other: String) =
    1 - uppercase().levenshtein(other.uppercase()) / max(length, other.length).toFloat()

fun String.levenshtein(other: String): Int {
    // degenerate cases
    if (this == other) return 0
    if (this == "") return other.length
    if (other == "") return this.length

    // create two integer arrays of distances and initialize the first one
    val v0 = IntArray(other.length + 1) { it }  // previous
    val v1 = IntArray(other.length + 1)         // current

    var cost: Int
    for (i in this.indices) {
        // calculate v1 from v0
        v1[0] = i + 1
        for (j in other.indices) {
            cost = if (this[i] == other[j]) 0 else 1
            v1[j + 1] = min(v1[j] + 1, min(v0[j + 1] + 1, v0[j] + cost))
        }
        // copy v1 to v0 for next iteration
        for (j in 0..other.length) v0[j] = v1[j]
    }
    return v1[other.length]
}

operator fun <T> MutableList<T>.set(item: T, value: T) {
    this[indexOf(item)] = value
}

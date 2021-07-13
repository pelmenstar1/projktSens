package com.pelmenstar.projktSens.shared

/**
 * Stretches the array to specified [newSize] and returns bigger version of array.
 * Note that type [T] have to be known in compile-time (marked as reified).
 */
inline fun<reified T:Any> Array<T?>.stretchArray(newSize: Int): Array<T?> {
    val newArray = arrayOfNulls<T>(newSize)
    System.arraycopy(this, 0, newArray, 0, size)

    return newArray
}

/**
 * Adds particular [value] to receiver array and returns new version of array with specified [value] in the end.
 */
inline fun<reified T> Array<T>.add(value: T): Array<T> {
    val newArray = arrayOfNulls<T>(size + 1)
    System.arraycopy(this, 0, newArray, 0, size)
    newArray[size] = value

    @Suppress("UNCHECKED_CAST")
    return newArray as Array<T>
}

fun IntArray.add(value: Int): IntArray {
    val newArray = IntArray(size + 1)
    System.arraycopy(this, 0, newArray, 0, size)
    newArray[size] = value

    return newArray
}
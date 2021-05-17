package com.pelmenstar.projktSens.shared

inline fun<reified T:Any> Array<T?>.stretchArray(newSize: Int): Array<T?> {
    val newArray = arrayOfNulls<T>(newSize)
    System.arraycopy(this, 0, newArray, 0, size)

    return newArray
}

inline fun<reified T:Any> Array<T?>.addNullable(value: T?): Array<T?> {
    val newArray = arrayOfNulls<T>(size + 1)
    System.arraycopy(this, 0, newArray, 0, size)
    newArray[size] = value

    return newArray
}

inline fun<reified T:Any> Array<T>.add(value: T): Array<T> {
    val newArray = arrayOfNulls<T>(size + 1)
    System.arraycopy(this, 0, newArray, 0, size)
    newArray[size] = value

    @Suppress("UNCHECKED_CAST")
    return newArray as Array<T>
}
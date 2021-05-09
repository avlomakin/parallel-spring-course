package com.github.avlomakin.consumerproducer

interface MyQueue<T>  {
    fun put(element: T)
    fun get() : T
}
package com.github.avlomakin.threadpool

class AggregateException : Throwable {

    constructor(msg: String) : super(msg)
    constructor(inner: Exception) : super(inner)
}


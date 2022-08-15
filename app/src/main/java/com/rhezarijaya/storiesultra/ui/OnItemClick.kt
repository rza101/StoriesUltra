package com.rhezarijaya.storiesultra.ui

interface OnItemClick<T, BINDING> {
    fun onClick(data: T, binding: BINDING)
}
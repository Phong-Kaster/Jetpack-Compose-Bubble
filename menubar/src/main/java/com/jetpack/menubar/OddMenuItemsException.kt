package com.jetpack.menubar


class OddMenuItemsException : Exception() {
    override val message: String? = """Your menu should have non-odd size ¯\_(ツ)_/¯"""
}
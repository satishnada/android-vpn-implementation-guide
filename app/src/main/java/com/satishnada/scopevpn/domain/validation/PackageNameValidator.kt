package com.satishnada.scopevpn.domain.validation

private val ANDROID_PACKAGE_PATTERN =
    Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")

fun isValidAndroidPackageName(value: String): Boolean =
    value.length <= 255 && ANDROID_PACKAGE_PATTERN.matches(value)

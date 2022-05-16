package com.mercadopago.android.px.core.commons.extensions

import java.util.regex.Pattern

const val COMMA = ","
const val EMPTY = ""
const val NL = "\n"
const val SPACE = " "
private val PLACE_HOLDER_PATTERN = Pattern.compile("\\{[0-9]*\\}")

// For java usage
fun CharSequence?.isEmpty() = isNullOrEmpty()

fun CharSequence?.isNotNullNorEmpty() = !isNullOrEmpty()

fun Iterable<String>?.join() = this?.joinToString(COMMA) ?: EMPTY

fun <T : CharSequence> T?.ifNotEmpty(block: (T) -> Unit) {
    if (isNotNullNorEmpty()) {
        block(this!!)
    }
}

fun <T : CharSequence> T?.orIfEmpty(fallback: T) = if (isNotNullNorEmpty()) this!! else fallback

fun String.format(vararg args: CharSequence): String {
    require(args.size == this.count(PLACE_HOLDER_PATTERN)) { "There is a different amount of placeholder than arguments" }
    var result = this
    args.indices.forEach {
        result = result.replace("{$it}", args[it].toString())
    }
    return result
}

internal fun CharSequence?.count(pattern: Pattern): Int {
    val matcher = pattern.matcher(this)
    var count = 0
    var i = 0
    while (matcher.find(i)) {
        count++
        i = matcher.start() + 1
    }
    return count
}

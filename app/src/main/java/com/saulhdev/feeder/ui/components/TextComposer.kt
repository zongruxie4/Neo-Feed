package com.saulhdev.feeder.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle

class TextComposer(
    val paragraphEmitter: (AnnotatedParagraphStringBuilder) -> Unit
) {
    val spanStack: MutableList<Span> = mutableListOf()

    // The identity of this will change - do not reference it in blocks
    var builder: AnnotatedParagraphStringBuilder = AnnotatedParagraphStringBuilder()

    fun terminateCurrentText() {
        if (builder.isEmpty()) {
            // Nothing to emit, and nothing to reset
            return
        }

        paragraphEmitter(builder)

        builder = AnnotatedParagraphStringBuilder()

        for (span in spanStack) {
            when (span) {
                is SpanWithStyle -> builder.pushStyle(span.spanStyle)
                is SpanWithAnnotation -> builder.pushStringAnnotation(
                    tag = span.tag,
                    annotation = span.annotation
                )

                is SpanWithComposableStyle -> builder.pushComposableStyle(span.spanStyle)
                is SpanWithVerbatim -> builder.pushVerbatimTtsAnnotation(span.verbatim)
            }
        }
    }

    val endsWithWhitespace: Boolean
        get() = builder.endsWithWhitespace

    fun ensureDoubleNewline() =
        builder.ensureDoubleNewline()

    fun append(text: String) =
        builder.append(text)

    fun append(char: Char) =
        builder.append(char)

    fun <R> appendTable(block: () -> R): R {
        terminateCurrentText()
        return block()
    }

    fun <R> appendImage(
        link: String? = null,
        onLinkClick: (String) -> Unit,
        block: (
            onClick: (() -> Unit)?
        ) -> R
    ): R {
        val url = link ?: findClosestLink()
        builder.ensureDoubleNewline()
        terminateCurrentText()
        val onClick: (() -> Unit)? = if (url?.isNotBlank() == true) {
            {
                onLinkClick(url)
            }
        } else {
            null
        }
        return block(onClick)
    }

    fun pop(index: Int) =
        builder.pop(index)

    fun pushStyle(style: SpanStyle): Int =
        builder.pushStyle(style)

    fun pushStringAnnotation(tag: String, annotation: String): Int =
        builder.pushStringAnnotation(tag = tag, annotation = annotation)

    fun pushComposableStyle(style: @Composable () -> SpanStyle): Int =
        builder.pushComposableStyle(style)

    fun popComposableStyle(index: Int) =
        builder.popComposableStyle(index)

    private fun findClosestLink(): String? {
        for (span in spanStack.reversed()) {
            if (span is SpanWithAnnotation && span.tag == "URL") {
                return span.annotation
            }
        }
        return null
    }
}

inline fun <R : Any> TextComposer.withParagraph(
    crossinline block: TextComposer.() -> R
): R {
    ensureDoubleNewline()
    return block(this)
}

inline fun <R : Any> TextComposer.withStyle(
    style: SpanStyle,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithStyle(style))
    val index = pushStyle(style)
    return try {
        block()
    } finally {
        pop(index)
        spanStack.removeAt(spanStack.size - 1)
    }
}

inline fun <R : Any> TextComposer.withComposableStyle(
    noinline style: @Composable () -> SpanStyle,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithComposableStyle(style))
    val index = pushComposableStyle(style)
    return try {
        block()
    } finally {
        popComposableStyle(index)
        spanStack.removeAt(spanStack.size - 1)
    }
}

inline fun <R : Any> TextComposer.withAnnotation(
    tag: String,
    annotation: String,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithAnnotation(tag = tag, annotation = annotation))
    val index = pushStringAnnotation(tag = tag, annotation = annotation)
    return try {
        block()
    } finally {
        pop(index)
        spanStack.removeAt(spanStack.size - 1)
    }
}

sealed class Span

data class SpanWithStyle(
    val spanStyle: SpanStyle
) : Span()

data class SpanWithAnnotation(
    val tag: String,
    val annotation: String
) : Span()

data class SpanWithComposableStyle(
    val spanStyle: @Composable () -> SpanStyle
) : Span()

data class SpanWithVerbatim(
    val verbatim: String
) : Span()

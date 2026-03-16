package com.wordle.app.core

enum class LetterState {
    EMPTY,      // Not yet typed
    TYPED,      // Typed but not submitted
    CORRECT,    // Green: right letter, right position
    PRESENT,    // Yellow: right letter, wrong position
    ABSENT      // Gray: letter not in word
}

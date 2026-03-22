package com.wordle.app.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.forAll

/**
 * Property test for WordLength enum completeness.
 *
 * Validates: Requirements 1.1
 */
class WordLengthPropertyTest : StringSpec({

    "every WordLength value has a positive value in {4,5,6,7}" {
        val validValues = setOf(4, 5, 6, 7)
        forAll(Arb.enum<WordLength>()) { wl ->
            wl.value in validValues
        }
    }

    "every WordLength value has a non-blank displayName" {
        forAll(Arb.enum<WordLength>()) { wl ->
            wl.displayName.isNotBlank()
        }
    }

    "WordLength defines exactly FOUR, FIVE, SIX, SEVEN" {
        val entries = WordLength.entries
        entries.size == 4 &&
            entries.contains(WordLength.FOUR) &&
            entries.contains(WordLength.FIVE) &&
            entries.contains(WordLength.SIX) &&
            entries.contains(WordLength.SEVEN)
    }

    "WordLength.value matches its semantic meaning" {
        WordLength.FOUR.value == 4 &&
            WordLength.FIVE.value == 5 &&
            WordLength.SIX.value == 6 &&
            WordLength.SEVEN.value == 7
    }
})

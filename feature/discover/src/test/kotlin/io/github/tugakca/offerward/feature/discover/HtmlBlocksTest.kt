package io.github.tugakca.offerward.feature.discover

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.feature.discover.ui.isHeadingBlock
import io.github.tugakca.offerward.feature.discover.ui.splitHtmlBlocks
import org.junit.Test

class HtmlBlocksTest {

    @Test
    fun splitsParagraphsHeadingsAndLists() {
        val blocks = splitHtmlBlocks("<h2>About</h2><p>One</p><p>Two <b>bold</b></p><ul><li>A</li><li>B</li></ul>")

        assertThat(blocks).containsExactly(
            "<h2>About</h2>",
            "<p>One</p>",
            "<p>Two <b>bold</b></p>",
            "<ul><li>A</li><li>B</li></ul>",
        ).inOrder()
    }

    @Test
    fun aNestedListStaysInsideItsParent() {
        val blocks = splitHtmlBlocks("<ul><li>A<ul><li>B</li></ul></li></ul><p>After</p>")

        assertThat(blocks).containsExactly("<ul><li>A<ul><li>B</li></ul></li></ul>", "<p>After</p>").inOrder()
    }

    @Test
    fun plainTextIsASingleBlock() {
        assertThat(splitHtmlBlocks("Just text")).containsExactly("Just text")
    }

    @Test
    fun textBetweenBlocksBecomesItsOwnBlock() {
        assertThat(splitHtmlBlocks("Intro<p>Body</p>")).containsExactly("Intro", "<p>Body</p>").inOrder()
    }

    @Test
    fun blankInputHasNoBlocks() {
        assertThat(splitHtmlBlocks("  ")).isEmpty()
    }

    @Test
    fun recognisesHeadings() {
        assertThat(isHeadingBlock("<h3>Title</h3>")).isTrue()
        assertThat(isHeadingBlock("<p>Body</p>")).isFalse()
    }
}

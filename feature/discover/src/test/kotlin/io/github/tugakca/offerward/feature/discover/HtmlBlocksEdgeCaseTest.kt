package io.github.tugakca.offerward.feature.discover

import com.google.common.truth.Truth.assertThat
import io.github.tugakca.offerward.feature.discover.ui.splitHtmlBlocks
import org.junit.Test

/** Real-world job HTML that is messier than the happy path. */
class HtmlBlocksEdgeCaseTest {

    @Test
    fun brSeparatedTextStaysOneBlock() {
        assertThat(splitHtmlBlocks("Line one<br>Line two<br/>Line three")).hasSize(1)
    }

    @Test
    fun uppercaseTagsAreSplit() {
        assertThat(splitHtmlBlocks("<P>One</P><H2>Two</H2>")).containsExactly("<P>One</P>", "<H2>Two</H2>").inOrder()
    }

    @Test
    fun emptyParagraphsAreDropped() {
        assertThat(splitHtmlBlocks("<p>One</p>   <p>Two</p>")).containsExactly("<p>One</p>", "<p>Two</p>").inOrder()
    }

    @Test
    fun nestedDivsStayTogether() {
        val html = "<div><div><p>A</p></div><p>B</p></div><p>C</p>"
        assertThat(
            splitHtmlBlocks(html),
        ).containsExactly("<div><div><p>A</p></div><p>B</p></div>", "<p>C</p>").inOrder()
    }

    @Test
    fun strayClosingTagDoesNotSwallowText() {
        val blocks = splitHtmlBlocks("</div><p>A</p></ul><p>B</p>")
        assertThat(blocks.joinToString("")).isEqualTo("</div><p>A</p></ul><p>B</p>")
        assertThat(blocks.any { it.contains("<p>B</p>") && !it.contains("<p>A</p>") }).isTrue()
    }

    @Test
    fun unclosedParagraphsDoNotLoseText() {
        val html = "<p>One<p>Two<ul><li>x</li></ul><p>Three</p>"
        assertThat(splitHtmlBlocks(html).joinToString("")).isEqualTo(html)
    }

    @Test
    fun unclosedParagraphsAreSplitIntoSeparateBlocks() {
        assertThat(splitHtmlBlocks("<p>One<p>Two<p>Three")).containsExactly("<p>One", "<p>Two", "<p>Three").inOrder()
    }

    @Test
    fun tagsWithAttributesAreRecognised() {
        assertThat(splitHtmlBlocks("<p class=\"x\" style='a'>One</p><ul data-x=\"1\"><li>A</li></ul>")).hasSize(2)
    }

    @Test
    fun lookalikeTagsAreNotBlocks() {
        assertThat(splitHtmlBlocks("<header>H</header><pref>x</pref><divider/>")).hasSize(1)
    }

    @Test
    fun blockTagInsideScriptOrCommentDoesNotSplit() {
        val html = "<p>A</p><script>var s = '</p><p>';</script><!-- <div> --><p>B</p>"
        assertThat(splitHtmlBlocks(html).joinToString("")).isEqualTo(html)
    }

    @Test
    fun entitiesArePreserved() {
        assertThat(splitHtmlBlocks("<p>R&amp;D &lt;3</p>")).containsExactly("<p>R&amp;D &lt;3</p>")
    }

    @Test
    fun paragraphOpenedInsideParagraphWithDivDoesNotMergeRest() {
        // <p> then <div> inside it: the </p> is ignored at depth 2, so the following
        // paragraphs should still become their own blocks once the div closes.
        val blocks = splitHtmlBlocks("<p>A<div>x</div></p><p>B</p><p>C</p>")
        assertThat(blocks.last()).isEqualTo("<p>C</p>")
    }

    @Test
    fun largeInputIsFast() {
        val html = buildString { repeat(20_000) { append("<p>Para $it</p><ul><li>i</li></ul>") } }
        val start = System.nanoTime()
        val blocks = splitHtmlBlocks(html)
        assertThat(blocks).hasSize(40_000)
        assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(2_000)
    }
}

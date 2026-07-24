package io.github.digorydoo.titanium.engine.ui.layout.linear_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger
import io.github.digorydoo.titanium.engine.ui.layout.LayoutElement
import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams
import io.github.digorydoo.titanium.engine.ui.layout.frame_layout.FrameLayout
import io.github.digorydoo.titanium.engine.ui.layout.linear_layout.LinearLayout.*
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class HorizontalLinearLayoutArrangerTest {
    private data class MockData(
        val layout: LinearLayout<*>,
        val arranger: HorizontalLinearLayoutArranger,
        val notepad: MutableList<String>,
    )

    private fun getMockElement(name: String, notepad: MutableList<String>, minWidth: Float, minHeight: Float) =
        object: LayoutElement {
            override val minWidth = minWidth
            override val minHeight = minHeight

            override fun onLayoutUpdated(left: Float, top: Float, right: Float, bottom: Float) {
                notepad.add("$name: onLayoutUpdated($left, $top, $right, $bottom)")
            }
        }

    private fun getMockData(
        lambda: LinearLayout<*>.(
            notepad: MutableList<String>,
            setInnerMinWidth: (frame: FrameLayout<*>, minWidth: Float) -> Unit,
        ) -> Unit,
    ): MockData {
        val notepad = mutableListOf<String>()
        val innerMinWidthsMap = mutableMapOf<FrameLayout<*>, Float>()
        val setInnerMinWidth = { frame: FrameLayout<*>, minWidth: Float -> innerMinWidthsMap[frame] = minWidth }
        val rootParams = object: LayoutParams {}

        val root = LinearLayout.build(Orientation.HORIZONTAL, rootParams) {
            lambda(notepad, setInnerMinWidth)
        }

        val delegate = object: LayoutArranger.Delegate {
            override fun arrange(
                layout: Layout<*, *>,
                minWidth: Float,
                maxWidth: Float,
                minHeight: Float,
                maxHeight: Float,
            ) {
                val name = layout::class.simpleName
                val idx = root.children.indexOfFirst { it === layout }
                notepad.add("arrange(${name}@${idx}, w in ($minWidth, $maxWidth), h in ($minHeight, $maxHeight)")
                assertTrue(layout is FrameLayout<*>)
                // We don't involve FrameLayoutArranger here; simply set the computed size to the minimum.
                layout.computed.size.set(
                    maxOf(minWidth, layout.minContentWidth, innerMinWidthsMap[layout] ?: 0f),
                    max(minHeight, layout.minContentHeight),
                )
            }
        }

        val arranger = HorizontalLinearLayoutArranger(delegate)
        return MockData(root, arranger, notepad)
    }

    @Test
    fun `should properly arrange an empty linear layout with align=START, justify=MATCH_PARENT`() {
        val (layout, arranger, notepad) = getMockData { _, _ ->
            align = Align.START
            justify = Justify.MATCH_PARENT

            paddingLeft = 1.0f
            paddingTop = 2.0f
            paddingRight = 4.0f
            paddingBottom = 8.0f
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 40f, minHeight = 5f, maxHeight = 50f)

        // The function should not call onLayoutUpdated, and there were no children to arrange.
        assertEquals("", notepad.joinToString("\n"), "notepad")

        // It's up to the parent to decide the relativePos, so it should still be at (0; 0).
        assertEquals(0f, layout.computed.relativePos.x, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, "computed.relativePos.y")

        // Since justify is MATCH_PARENT, the width should extend to the full maxWidth that was given.
        assertEquals(40.0f, layout.computed.size.x, "computed.size.x")

        // Since align is START, the height should stick to minimal constraints.
        assertEquals(10.0f, layout.computed.size.y, "computed.size.y") // paddingTop + paddingBottom
    }

    @Test
    fun `should properly arrange an empty linear layout with align=STRETCH, justify=WRAP_CONTENT`() {
        val (layout, arranger, notepad) = getMockData { _, _ ->
            align = Align.STRETCH
            justify = Justify.WRAP_CONTENT

            paddingLeft = 1.0f
            paddingTop = 2.0f
            paddingRight = 4.0f
            paddingBottom = 8.0f
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 40f, minHeight = 5f, maxHeight = 50f)

        // The function should not call onLayoutUpdated, and there were no children to arrange.
        assertEquals("", notepad.joinToString("\n"), "notepad")

        // It's up to the parent to decide the relativePos, so it should still be at (0; 0).
        assertEquals(0f, layout.computed.relativePos.x, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, "computed.relativePos.y")

        // Since justify is WRAP_CONTENT, the width should stick to minimal constraints.
        assertEquals(5.0f, layout.computed.size.x, "computed.size.x") // paddingLeft + paddingRight

        // Since align is STRETCH, the height should extend to the full maxHeight that was given.
        assertEquals(50.0f, layout.computed.size.y, "computed.size.y")
    }

    @Test
    fun `should properly arrange a horiz linear layout (START, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.START
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 4f
            paddingBottom = 8f

            frame {
                padding(4f)
                params.apply {
                    minHeight = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minWidth = 72f
                    marginTop = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxWidth = 42f
                    marginLeft = 8f
                    marginRight = 8f
                    marginBottom = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginLeft = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 256f, minHeight = 5f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (40.666668, 40.666668), h in (24.0, Infinity)
            arrange(FrameLayout@1, w in (72.00001, 72.00001), h in (0.0, Infinity)
            arrange(FrameLayout@2, w in (42.0, 42.0), h in (0.0, Infinity)
            arrange(FrameLayout@3, w in (73.33331, 73.33331), h in (0.0, Infinity)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(256.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(80.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(40.666668, 24.0)
            size=(72.00001, 56.0)
            size=(42.0, 8.0)
            size=(73.33331, 32.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(40.666668, 4.0)
            pos=(120.66667, 0.0)
            pos=(170.66667, 0.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a horiz linear layout (END, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.END
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 4f
            paddingBottom = 8f

            frame {
                padding(4f)
                params.apply {
                    minHeight = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minWidth = 72f
                    marginTop = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxWidth = 42f
                    marginLeft = 8f
                    marginRight = 8f
                    marginBottom = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginLeft = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 256f, minHeight = 5f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (40.666668, 40.666668), h in (24.0, Infinity)
            arrange(FrameLayout@1, w in (72.00001, 72.00001), h in (0.0, Infinity)
            arrange(FrameLayout@2, w in (42.0, 42.0), h in (0.0, Infinity)
            arrange(FrameLayout@3, w in (73.33331, 73.33331), h in (0.0, Infinity)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(256.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(80.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(40.666668, 24.0)
            size=(72.00001, 56.0)
            size=(42.0, 8.0)
            size=(73.33331, 32.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 64.0)
            pos=(40.666668, 64.0)
            pos=(120.66667, 60.0)
            pos=(170.66667, 64.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a horiz linear layout (CENTRE, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.CENTRE
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 4f
            paddingBottom = 8f

            frame {
                padding(4f)
                params.apply {
                    minHeight = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minWidth = 72f
                    marginTop = 8f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxWidth = 42f
                    marginLeft = 8f
                    marginRight = 8f
                    marginBottom = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginLeft = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 256f, minHeight = 5f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (40.666668, 40.666668), h in (24.0, Infinity)
            arrange(FrameLayout@1, w in (72.00001, 72.00001), h in (0.0, Infinity)
            arrange(FrameLayout@2, w in (42.0, 42.0), h in (0.0, Infinity)
            arrange(FrameLayout@3, w in (73.33331, 73.33331), h in (0.0, Infinity)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(256.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(84.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(40.666668, 24.0)
            size=(72.00001, 56.0)
            size=(42.0, 8.0)
            size=(73.33331, 32.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 22.0)
            pos=(40.666668, 10.0)
            pos=(120.66667, 28.0)
            pos=(170.66667, 18.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a horiz linear layout (STRETCH, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.STRETCH
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 4f
            paddingBottom = 8f

            frame {
                padding(4f)
                params.apply {
                    minHeight = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minWidth = 72f
                    marginTop = 8f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxWidth = 42f
                    marginLeft = 8f
                    marginRight = 8f
                    marginBottom = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginLeft = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 256f, minHeight = 5f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (40.666668, 40.666668), h in (112.0, Infinity)
            arrange(FrameLayout@1, w in (72.00001, 72.00001), h in (104.0, Infinity)
            arrange(FrameLayout@2, w in (42.0, 42.0), h in (108.0, Infinity)
            arrange(FrameLayout@3, w in (73.33331, 73.33331), h in (112.0, Infinity)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(256f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(128f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(40.666668, 112.0)
            size=(72.00001, 104.0)
            size=(42.0, 108.0)
            size=(73.33331, 112.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(40.666668, 8.0)
            pos=(120.66667, -4.0)
            pos=(170.66667, 0.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a horiz linear layout (START, WRAP_CONTENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.START
            justify = Justify.WRAP_CONTENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 4f
            paddingBottom = 8f

            frame {
                padding(4f)
                params.apply {
                    minHeight = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minWidth = 72f
                    marginTop = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxWidth = 42f
                    marginLeft = 8f
                    marginRight = 8f
                    marginBottom = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginLeft = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 256f, minHeight = 5f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (0.0, 0.0), h in (24.0, Infinity)
            arrange(FrameLayout@1, w in (72.0, 72.0), h in (0.0, Infinity)
            arrange(FrameLayout@2, w in (0.0, 0.0), h in (0.0, Infinity)
            arrange(FrameLayout@3, w in (0.0, 0.0), h in (0.0, Infinity)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(156.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(80.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(16.0, 24.0)
            size=(72.0, 56.0)
            size=(8.0, 8.0)
            size=(32.0, 32.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(16.0, 4.0)
            pos=(96.0, 0.0)
            pos=(112.0, 0.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a horiz linear layout (START, MATCH_PARENT) with one item unexpectedly wide`() {
        val (layout, arranger, notepad) = getMockData { notepad, setInnerMinWidth ->
            align = Align.START
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 4f
            paddingBottom = 8f

            frame {
                padding(4f)
                params.apply {
                    minHeight = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minWidth = 72f
                    maxWidth = 96f
                    marginTop = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    marginLeft = 8f
                    marginRight = 8f
                    marginBottom = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }.also {
                // The algorithm does not see the layout's inner min width (due to its children) until it actually
                // tries to arrange it. We set it to something that's beyond the estimated space, which will cause
                // the algorithm to fix its estimation and try again.
                setInnerMinWidth(it, 80f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginLeft = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 256f, minHeight = 5f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (34.45119, 34.45119), h in (24.0, Infinity)
            arrange(FrameLayout@1, w in (72.0, 72.0), h in (0.0, Infinity)
            arrange(FrameLayout@2, w in (60.774414, 60.774414), h in (0.0, Infinity)
            arrange(FrameLayout@0, w in (29.134478, 29.134478), h in (24.0, Infinity)
            arrange(FrameLayout@1, w in (72.0, 72.0), h in (0.0, Infinity)
            arrange(FrameLayout@2, w in (77.21461, 77.21461), h in (0.0, Infinity)
            arrange(FrameLayout@3, w in (46.865524, 46.865524), h in (0.0, Infinity)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(256.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(80.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(29.134478, 24.0)
            size=(72.0, 56.0)
            size=(80.0, 8.0)
            size=(46.865524, 32.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(29.134478, 4.0)
            pos=(109.134476, 0.0)
            pos=(197.13448, 0.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    companion object {
        private const val TOLERANCE = 0.0001f
    }
}

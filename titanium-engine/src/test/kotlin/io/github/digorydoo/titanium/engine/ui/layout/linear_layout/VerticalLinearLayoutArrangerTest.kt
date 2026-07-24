package io.github.digorydoo.titanium.engine.ui.layout.linear_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger
import io.github.digorydoo.titanium.engine.ui.layout.LayoutElement
import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams
import io.github.digorydoo.titanium.engine.ui.layout.frame_layout.FrameLayout
import io.github.digorydoo.titanium.engine.ui.layout.linear_layout.LinearLayout.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerticalLinearLayoutArrangerTest {
    private data class MockData(
        val layout: LinearLayout<*>,
        val arranger: VerticalLinearLayoutArranger,
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
            setInnerMinHeight: (frame: FrameLayout<*>, minHeight: Float) -> Unit,
        ) -> Unit,
    ): MockData {
        val notepad = mutableListOf<String>()
        val innerMinHeightsMap = mutableMapOf<FrameLayout<*>, Float>()
        val setInnerMinHeight = { frame: FrameLayout<*>, minHeight: Float -> innerMinHeightsMap[frame] = minHeight }
        val rootParams = object: LayoutParams {}

        val root = LinearLayout.build(Orientation.VERTICAL, rootParams) {
            lambda(notepad, setInnerMinHeight)
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
                    maxOf(minWidth, layout.minContentWidth),
                    maxOf(minHeight, layout.minContentHeight, innerMinHeightsMap[layout] ?: 0f),
                )
            }
        }

        val arranger = VerticalLinearLayoutArranger(delegate)
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

        // Since align is START, the width should stick to minimal constraints.
        assertEquals(5f, layout.computed.size.x, "computed.size.x") // paddingLeft + paddingRight

        // Since justify is MATCH_PARENT, the height should extend to the full maxHeight that was given.
        assertEquals(50f, layout.computed.size.y, "computed.size.y")
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

        // Since align is STRETCH, the width should extend to the full maxWidth that was given.
        assertEquals(40f, layout.computed.size.x, "computed.size.x")

        // Since justify is WRAP_CONTENT, the height should stick to minimal constraints.
        assertEquals(10f, layout.computed.size.y, "computed.size.y") // paddingTop + paddingBottom
    }

    @Test
    fun `should properly arrange a vertical linear layout (START, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.START
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 8f
            paddingBottom = 4f

            frame {
                padding(4f)
                params.apply {
                    minWidth = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minHeight = 72f
                    marginLeft = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxHeight = 42f
                    marginTop = 8f
                    marginBottom = 8f
                    marginRight = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginTop = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 5f, maxWidth = 128f, minHeight = 4f, maxHeight = 256f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (24.0, Infinity), h in (40.666668, 40.666668)
            arrange(FrameLayout@1, w in (0.0, Infinity), h in (72.00001, 72.00001)
            arrange(FrameLayout@2, w in (0.0, Infinity), h in (42.0, 42.0)
            arrange(FrameLayout@3, w in (0.0, Infinity), h in (73.33331, 73.33331)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(80.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(256.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(24.0, 40.666668)
            size=(56.0, 72.00001)
            size=(8.0, 42.0)
            size=(32.0, 73.33331)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(4.0, 40.666668)
            pos=(0.0, 120.66667)
            pos=(0.0, 170.66667)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a vertical linear layout (END, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.END
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 8f
            paddingBottom = 4f

            frame {
                padding(4f)
                params.apply {
                    minWidth = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minHeight = 72f
                    marginLeft = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxHeight = 42f
                    marginTop = 8f
                    marginBottom = 8f
                    marginRight = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginTop = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 5f, maxWidth = 128f, minHeight = 4f, maxHeight = 256f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (24.0, Infinity), h in (40.666668, 40.666668)
            arrange(FrameLayout@1, w in (0.0, Infinity), h in (72.00001, 72.00001)
            arrange(FrameLayout@2, w in (0.0, Infinity), h in (42.0, 42.0)
            arrange(FrameLayout@3, w in (0.0, Infinity), h in (73.33331, 73.33331)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(80.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(256.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(24.0, 40.666668)
            size=(56.0, 72.00001)
            size=(8.0, 42.0)
            size=(32.0, 73.33331)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(64.0, 0.0)
            pos=(64.0, 40.666668)
            pos=(60.0, 120.66667)
            pos=(64.0, 170.66667)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a vertical linear layout (CENTRE, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.CENTRE
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 8f
            paddingBottom = 4f

            frame {
                padding(4f)
                params.apply {
                    minWidth = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minHeight = 72f
                    marginLeft = 8f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxHeight = 42f
                    marginTop = 8f
                    marginBottom = 8f
                    marginRight = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginTop = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 5f, maxWidth = 128f, minHeight = 4f, maxHeight = 256f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (24.0, Infinity), h in (40.666668, 40.666668)
            arrange(FrameLayout@1, w in (0.0, Infinity), h in (72.00001, 72.00001)
            arrange(FrameLayout@2, w in (0.0, Infinity), h in (42.0, 42.0)
            arrange(FrameLayout@3, w in (0.0, Infinity), h in (73.33331, 73.33331)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(84.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(256.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(24.0, 40.666668)
            size=(56.0, 72.00001)
            size=(8.0, 42.0)
            size=(32.0, 73.33331)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(22.0, 0.0)
            pos=(10.0, 40.666668)
            pos=(28.0, 120.66667)
            pos=(18.0, 170.66667)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a vertical linear layout (STRETCH, MATCH_PARENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.STRETCH
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 8f
            paddingBottom = 4f

            frame {
                padding(4f)
                params.apply {
                    minWidth = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minHeight = 72f
                    marginLeft = 8f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxHeight = 42f
                    marginTop = 8f
                    marginBottom = 8f
                    marginRight = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginTop = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 5f, maxWidth = 128f, minHeight = 4f, maxHeight = 256f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (112.0, Infinity), h in (40.666668, 40.666668)
            arrange(FrameLayout@1, w in (104.0, Infinity), h in (72.00001, 72.00001)
            arrange(FrameLayout@2, w in (108.0, Infinity), h in (42.0, 42.0)
            arrange(FrameLayout@3, w in (112.0, Infinity), h in (73.33331, 73.33331)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(128f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(256f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(112.0, 40.666668)
            size=(104.0, 72.00001)
            size=(108.0, 42.0)
            size=(112.0, 73.33331)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(8.0, 40.666668)
            pos=(-4.0, 120.66667)
            pos=(0.0, 170.66667)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a vertical linear layout (START, WRAP_CONTENT) whose children all fit`() {
        val (layout, arranger, notepad) = getMockData { notepad, _ ->
            align = Align.START
            justify = Justify.WRAP_CONTENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 8f
            paddingBottom = 4f

            frame {
                padding(4f)
                params.apply {
                    minWidth = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minHeight = 72f
                    marginLeft = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    maxHeight = 42f
                    marginTop = 8f
                    marginBottom = 8f
                    marginRight = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginTop = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 5f, maxWidth = 128f, minHeight = 4f, maxHeight = 256f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (24.0, Infinity), h in (0.0, 0.0)
            arrange(FrameLayout@1, w in (0.0, Infinity), h in (72.0, 72.0)
            arrange(FrameLayout@2, w in (0.0, Infinity), h in (0.0, 0.0)
            arrange(FrameLayout@3, w in (0.0, Infinity), h in (0.0, 0.0)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(80.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(156.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(24.0, 16.0)
            size=(56.0, 72.0)
            size=(8.0, 8.0)
            size=(32.0, 32.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(4.0, 16.0)
            pos=(0.0, 96.0)
            pos=(0.0, 112.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    @Test
    fun `should properly arrange a vertical linear layout (START, MATCH_PARENT) with one item unexpectedly high`() {
        val (layout, arranger, notepad) = getMockData { notepad, setInnerMinHeight ->
            align = Align.START
            justify = Justify.MATCH_PARENT

            paddingLeft = 8f
            paddingTop = 8f
            paddingRight = 8f
            paddingBottom = 4f

            frame {
                padding(4f)
                params.apply {
                    minWidth = 24f
                }
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }
            frame {
                padding(16.0f)
                params.apply {
                    minHeight = 72f
                    maxHeight = 96f
                    marginLeft = 4f
                }
                background = getMockElement("frame1.background", notepad, 64f, 64f)
                content = getMockElement("frame1.content", notepad, 24f, 24f)
            }
            frame {
                padding(2.0f)
                params.apply {
                    weight = 2f
                    marginTop = 8f
                    marginBottom = 8f
                    marginRight = 4f
                }
                background = getMockElement("frame2.background", notepad, 64f, 64f)
                content = getMockElement("frame2.content", notepad, 4f, 4f)
            }.also {
                // The algorithm does not see the layout's inner min height (due to its children) until it actually
                // tries to arrange it. We set it to something that's beyond the estimated space, which will cause
                // the algorithm to fix its estimation and try again.
                setInnerMinHeight(it, 80f)
            }
            frame {
                padding(8.0f)
                params.apply {
                    weight = 2f
                    marginTop = 8f
                }
                background = getMockElement("frame3.background", notepad, 64f, 64f)
                content = getMockElement("frame3.content", notepad, 16f, 16f)
            }
        }
        arranger.arrange(layout, minWidth = 5f, maxWidth = 128f, minHeight = 4f, maxHeight = 256f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (24.0, Infinity), h in (34.45119, 34.45119)
            arrange(FrameLayout@1, w in (0.0, Infinity), h in (72.0, 72.0)
            arrange(FrameLayout@2, w in (0.0, Infinity), h in (60.774414, 60.774414)
            arrange(FrameLayout@0, w in (24.0, Infinity), h in (29.134478, 29.134478)
            arrange(FrameLayout@1, w in (0.0, Infinity), h in (72.0, 72.0)
            arrange(FrameLayout@2, w in (0.0, Infinity), h in (77.21461, 77.21461)
            arrange(FrameLayout@3, w in (0.0, Infinity), h in (46.865524, 46.865524)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(80.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(256.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")
        assertEquals(
            """
            size=(24.0, 29.134478)
            size=(56.0, 72.0)
            size=(8.0, 80.0)
            size=(32.0, 46.865524)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(4.0, 29.134478)
            pos=(0.0, 109.134476)
            pos=(0.0, 197.13448)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos },
        )
    }

    companion object {
        private const val TOLERANCE = 0.0001f
    }
}

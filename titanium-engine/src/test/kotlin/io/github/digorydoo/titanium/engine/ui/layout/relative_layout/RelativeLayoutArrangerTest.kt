package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutArranger
import io.github.digorydoo.titanium.engine.ui.layout.LayoutElement
import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams
import io.github.digorydoo.titanium.engine.ui.layout.frame_layout.FrameLayout
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RelativeLayoutArrangerTest {
    private data class MockData(
        val layout: RelativeLayout<*>,
        val arranger: RelativeLayoutArranger,
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
        lambda: RelativeLayout<*>.(
            notepad: MutableList<String>,
            setInnerMinSize: (frame: FrameLayout<*>, minWidth: Float, minHeight: Float) -> Unit,
        ) -> Unit,
    ): MockData {
        val notepad = mutableListOf<String>()
        val rootParams = object: LayoutParams {}
        val innerMinSizeMap = mutableMapOf<FrameLayout<*>, Vector2f>()

        val setInnerMinSize = { frame: FrameLayout<*>, minWidth: Float, minHeight: Float ->
            innerMinSizeMap[frame] = Vector2f(minWidth, minHeight)
        }

        val root = RelativeLayout.build(rootParams) {
            lambda(notepad, setInnerMinSize)
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
                val innerMinSize = innerMinSizeMap[layout] ?: Vector2f.zero
                layout.computed.size.set(
                    maxOf(minWidth, layout.minContentWidth, innerMinSize.x),
                    maxOf(minHeight, layout.minContentHeight, innerMinSize.y),
                )
            }
        }

        val arranger = RelativeLayoutArranger(delegate)
        return MockData(root, arranger, notepad)
    }

    @Disabled
    @Test
    fun `should properly arrange an empty RelativeLayout`() {
        val (layout, arranger, notepad) = getMockData { _, _ ->
            paddingLeft = 1f
            paddingTop = 2f
            paddingRight = 4f
            paddingBottom = 8f
        }
        arranger.arrange(layout, minWidth = 4f, maxWidth = 40f, minHeight = 16f, maxHeight = 50f)

        // The function should not call onLayoutUpdated, and there were no children to arrange.
        assertEquals("", notepad.joinToString("\n"), "notepad")

        // It's up to the parent to decide the relativePos, so it should still be at (0; 0).
        assertEquals(0f, layout.computed.relativePos.x, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, "computed.relativePos.y")

        // The size should be the minimum size.
        assertEquals(5f, layout.computed.size.x, "computed.size.x") // paddingLeft + paddingRight > minWidth
        assertEquals(16f, layout.computed.size.y, "computed.size.y") // minHeight > paddingTop + paddingBottom
    }

    @Disabled
    @Test
    fun `should properly arrange a RelativeLayout whose children are mostly unconstrained`() {
        val (layout, arranger, notepad) = getMockData { notepad, setInnerMinSize ->
            paddingLeft = 1f
            paddingTop = 2f
            paddingRight = 4f
            paddingBottom = 8f

            frame {
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
            }

            frame {
                params.apply {
                    minWidth = 24f
                    minHeight = 32f
                }
            }

            frame { }.also { setInnerMinSize(it, 16f, 4f) }
        }
        arranger.arrange(layout, minWidth = 16f, maxWidth = 128f, minHeight = 16f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (24.0, 24.0), h in (32.0, 32.0)
            arrange(FrameLayout@1, w in (24.0, 24.0), h in (32.0, 32.0)
            arrange(FrameLayout@2, w in (24.0, 24.0), h in (32.0, 32.0)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(29.0f, layout.computed.size.x, TOLERANCE, "computed.size.x") // 24 + 1 + 4
        assertEquals(42.0f, layout.computed.size.y, TOLERANCE, "computed.size.y") // 32 + 2 + 8
        assertEquals(
            """
            size=(24.0, 32.0)
            size=(24.0, 32.0)
            size=(24.0, 32.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "size=" + it.computed.size },
        )
        assertEquals(
            """
            pos=(0.0, 0.0)
            pos=(0.0, 0.0)
            pos=(0.0, 0.0)
            """.trimIndent(),
            layout.children.joinToString("\n") { "pos=" + it.computed.relativePos.setComponentsToZeroIfSmall(EPSILON) },
        )
    }

    @Disabled
    @Test
    fun `should properly arrange a RelativeLayout whose children do not depend on each-other`() {
        val (layout, arranger, notepad) = getMockData { notepad, setInnerMinSize ->
            paddingLeft = 4f
            paddingTop = 4f
            paddingRight = 4f
            paddingBottom = 4f

            frame {
                background = getMockElement("frame0.background", notepad, 64f, 64f)
                content = getMockElement("frame0.content", notepad, 8f, 8f)
                padding(4f)
                params.apply {
                    alignParentLeft = true
                    alignParentTop = true
                    marginLeft = 4f
                    marginTop = 4f
                    maxWidth = 48f
                    maxHeight = 48f
                }
            }

            frame {
                params.apply {
                    alignParentRight = true
                    alignParentTop = true
                    marginRight = 4f
                    marginTop = 4f
                    minWidth = 8f
                    maxWidth = 48f
                    minHeight = 8f
                    maxHeight = 48f
                }
            }

            frame {
                params.apply {
                    centreHorizontally = true
                    centreVertically = true
                    minWidth = 8f
                    maxWidth = 16f
                    minHeight = 8f
                    maxHeight = 16f
                }
            }

            frame {
                params.apply {
                    alignParentLeft = true
                    alignParentBottom = true
                    marginLeft = 4f
                    marginBottom = 4f
                    maxWidth = 16f
                    maxHeight = 16f
                }
            }.also { setInnerMinSize(it, 16f, 4f) }

            frame {
                params.apply {
                    background = getMockElement("frame3.background", notepad, 64f, 64f)
                    content = getMockElement("frame3.content", notepad, 8f, 8f)
                    alignParentRight = true
                    alignParentBottom = true
                    marginRight = 4f
                    marginBottom = 4f
                    minWidth = 8f
                    maxWidth = 16f
                    minHeight = 8f
                    maxHeight = 16f
                }
            }.also { setInnerMinSize(it, 16f, 8f) }
        }
        arranger.arrange(layout, minWidth = 64f, maxWidth = 128f, minHeight = 64f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (48.0, 48.0), h in (48.0, 48.0)
            arrange(FrameLayout@1, w in (48.0, 48.0), h in (48.0, 48.0)
            arrange(FrameLayout@2, w in (16.0, 16.0), h in (16.0, 16.0)
            arrange(FrameLayout@3, w in (16.0, 16.0), h in (16.0, 16.0)
            arrange(FrameLayout@4, w in (16.0, 16.0), h in (16.0, 16.0)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(64.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(64.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")

        val child0 = layout.children[0]
        val child1 = layout.children[1]
        val child2 = layout.children[2]
        val child3 = layout.children[3]
        val child4 = layout.children[4]

        // Since children are not interconnected, the layout's size is first computed as the min size, and then the
        // children extend to their maximum allowed size.

        assertEquals("(48.0, 48.0)", child0.computed.size.toString()) // x: 64 - 4 - 4 - 4 = 52, but maxWidth=48
        assertEquals("(48.0, 48.0)", child1.computed.size.toString())
        assertEquals("(16.0, 16.0)", child2.computed.size.toString()) // x: 64 - 4 - 4 - 0 = 56, but maxWidth=16
        assertEquals("(16.0, 16.0)", child3.computed.size.toString()) // x: 64 - 4 - 4 - 4 = 52, but maxWidth=16
        assertEquals("(16.0, 16.0)", child4.computed.size.toString()) // x: 64 - 4 - 4 - 4 = 52, but maxWidth=16

        assertEquals("(4.0, 4.0)", child0.computed.relativePos.toString()) // top-left with margins (4; 4)
        assertEquals("(4.0, 4.0)", child1.computed.relativePos.toString()) // x: 64 - 4 - 4 - 4 - 48 = 4
        assertEquals("(20.0, 20.0)", child2.computed.relativePos.toString()) // x: (64 - 4 - 4)/2 - 16/2 = 20
        assertEquals("(4.0, 36.0)", child3.computed.relativePos.toString()) // y: 64 - 4 - 4 - 4 - 16 = 36
        assertEquals("(36.0, 36.0)", child4.computed.relativePos.toString())
    }

    @Disabled
    @Test
    fun `should properly arrange a RelativeLayout whose children are connected in a row left-to-right`() {
        val (layout, arranger, notepad) = getMockData { _, _ ->
            padding(8f)

            val f0 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    alignParentLeft = true
                }
            }

            val f1 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    rightOf = f0
                }
            }

            val f2 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    rightOf = f1
                }
            }

            frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    alignParentRight = true
                    rightOf = f2
                }
            }
        }
        arranger.arrange(layout, minWidth = 64f, maxWidth = 128f, minHeight = 64f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (2.0, 2.0), h in (32.0, 32.0)
            arrange(FrameLayout@1, w in (2.0, 2.0), h in (32.0, 32.0)
            arrange(FrameLayout@2, w in (2.0, 2.0), h in (32.0, 32.0)
            arrange(FrameLayout@3, w in (2.0, 2.0), h in (32.0, 32.0)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(64.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(64.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")

        val child0 = layout.children[0]
        val child1 = layout.children[1]
        val child2 = layout.children[2]
        val child3 = layout.children[3]

        assertEquals("(2.0, 32.0)", child0.computed.size.toString())
        assertEquals("(2.0, 32.0)", child1.computed.size.toString())
        assertEquals("(2.0, 32.0)", child2.computed.size.toString())
        assertEquals("(2.0, 32.0)", child3.computed.size.toString())

        assertEquals("(8.0, 8.0)", child0.computed.relativePos.toString())
        assertEquals("(18.0, 8.0)", child1.computed.relativePos.toString())
        assertEquals("(28.0, 8.0)", child2.computed.relativePos.toString())
        assertEquals("(38.0, 8.0)", child3.computed.relativePos.toString())
    }

    @Disabled
    @Test
    fun `should properly arrange a RelativeLayout whose children are connected in a row right-to-left`() {
        val (layout, arranger, notepad) = getMockData { _, _ ->
            padding(8f)

            val f0 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    alignParentRight = true
                }
            }

            val f1 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    leftOf = f0
                }
            }

            val f2 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    leftOf = f1
                }
            }

            frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentBottom = true
                    alignParentLeft = true
                    leftOf = f2
                }
            }
        }
        arranger.arrange(layout, minWidth = 64f, maxWidth = 128f, minHeight = 64f, maxHeight = 128f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (2.0, 2.0), h in (32.0, 32.0)
            arrange(FrameLayout@1, w in (2.0, 2.0), h in (32.0, 32.0)
            arrange(FrameLayout@2, w in (2.0, 2.0), h in (32.0, 32.0)
            arrange(FrameLayout@3, w in (2.0, 2.0), h in (32.0, 32.0)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(64.0f, layout.computed.size.x, TOLERANCE, "computed.size.x")
        assertEquals(64.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")

        val child0 = layout.children[0]
        val child1 = layout.children[1]
        val child2 = layout.children[2]
        val child3 = layout.children[3]

        assertEquals("(2.0, 32.0)", child0.computed.size.toString())
        assertEquals("(2.0, 32.0)", child1.computed.size.toString())
        assertEquals("(2.0, 32.0)", child2.computed.size.toString())
        assertEquals("(2.0, 32.0)", child3.computed.size.toString())

        assertEquals("(38.0, 8.0)", child0.computed.relativePos.toString())
        assertEquals("(28.0, 8.0)", child1.computed.relativePos.toString())
        assertEquals("(18.0, 8.0)", child2.computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", child3.computed.relativePos.toString())
    }

    @Disabled
    @Test
    fun `should properly arrange a RelativeLayout with a T-like structure`() {
        val (layout, arranger, notepad) = getMockData { _, setInnerMinSize ->
            padding(8f)

            val f0 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentLeft = true
                }
            }.also { setInnerMinSize(it, 16f, 16f) }

            val f1 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    rightOf = f0
                }
            }.also { setInnerMinSize(it, 24f, 24f) }

            val f2 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    rightOf = f1
                    // relPos.x initialized at 77.33f
                }
            }.also { setInnerMinSize(it, 8f, 8f) }

            val f3 = frame {
                params.apply {
                    margin(8f)
                    rightOf = f0
                    below = f1
                }
            }.also { setInnerMinSize(it, 16f, 16f) }

            // f4
            frame {
                params.apply {
                    margin(8f)
                    leftOf = f2 // FIXME this gets a position delta, relPos.x initialised at 8f
                    below = f1
                }
            }.also { setInnerMinSize(it, 8f, 8f) }
        }
        arranger.arrange(layout, minWidth = 128f, maxWidth = 256f, minHeight = 128f, maxHeight = 256f)
        assertEquals(
            """
            arrange(FrameLayout@0, w in (47.0, 47.0), h in (95.0, 95.0)
            arrange(FrameLayout@1, w in (39.0, 39.0), h in (44.0, 44.0)
            arrange(FrameLayout@2, w in (0.0, 0.0), h in (95.0, 95.0)
            arrange(FrameLayout@3, w in (26.0, 26.0), h in (44.0, 44.0)
            arrange(FrameLayout@4, w in (95.0, 95.0), h in (44.0, 44.0)
            """.trimIndent(),
            notepad.joinToString("\n"),
            "notepad"
        )
        assertEquals(0f, layout.computed.relativePos.x, TOLERANCE, "computed.relativePos.x")
        assertEquals(0f, layout.computed.relativePos.y, TOLERANCE, "computed.relativePos.y")
        assertEquals(139.0f, layout.computed.size.x, TOLERANCE, "computed.size.x") // FIXME required 80, should be 128
        assertEquals(128.0f, layout.computed.size.y, TOLERANCE, "computed.size.y")

        val child0 = layout.children[0]
        val child1 = layout.children[1]
        val child2 = layout.children[2]
        val child3 = layout.children[3]

        assertEquals("(46.96179, 94.915184)", child0.computed.size.toString()) // FIXME
        assertEquals("(2.0, 32.0)", child1.computed.size.toString())
        assertEquals("(2.0, 32.0)", child2.computed.size.toString())
        assertEquals("(2.0, 32.0)", child3.computed.size.toString())

        assertEquals("(38.0, 8.0)", child0.computed.relativePos.toString())
        assertEquals("(28.0, 8.0)", child1.computed.relativePos.toString())
        assertEquals("(18.0, 8.0)", child2.computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", child3.computed.relativePos.toString())
    }

    companion object {
        private const val TOLERANCE = 0.0001f
        private const val EPSILON = 1e-30f
    }
}

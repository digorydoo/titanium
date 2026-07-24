package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PreArrangerTest {
    @Test
    fun `should properly compute sizes of an empty layout`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            paddingLeft = 2f
            paddingTop = 4f
            paddingRight = 8f
            paddingBottom = 16f
        }
        val nodeMap = NodeMap.createFromLayout(layout)
        val r = PreArranger().setInitialPosAndSizes(
            layout,
            minWidth = 3f,
            maxWidth = 128f,
            minHeight = 3f,
            maxHeight = 256f,
            nodeMap,
        )
        assertEquals(0f, r.minWidth)
        assertEquals(118f, r.maxWidth) // 128f - paddingLeft - paddingRight
        assertEquals(0f, r.minHeight)
        assertEquals(236f, r.maxHeight) // 256f - paddingTop - paddingBottom
    }

    @Test
    fun `should properly compute sizes of a layout whose children sticks to parent sides and overlap`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            paddingLeft = 2f
            paddingTop = 4f
            paddingRight = 8f
            paddingBottom = 16f

            frame {
                params.apply {
                    alignParentLeft = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
            frame {
                params.apply {
                    centreHorizontally = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
            frame {
                params.apply {
                    alignParentRight = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
            frame {
                params.apply {
                    alignParentRight = true
                    centreVertically = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
            frame {
                params.apply {
                    alignParentRight = true
                    alignParentBottom = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
            frame {
                params.apply {
                    centreHorizontally = true
                    alignParentBottom = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
            frame {
                params.apply {
                    alignParentLeft = true
                    alignParentBottom = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
            frame {
                params.apply {
                    alignParentLeft = true
                    centreVertically = true
                    minWidth = 32f
                    minHeight = 24f
                    margin(8f)
                }
            }
        }
        val nodeMap = NodeMap.createFromLayout(layout)
        val r = PreArranger().setInitialPosAndSizes(
            layout,
            minWidth = 64f,
            maxWidth = 128f,
            minHeight = 64f,
            maxHeight = 256f,
            nodeMap,
        )
        assertEquals(54f, r.minWidth) // 64f - paddingLeft - paddingRight
        assertEquals(118f, r.maxWidth) // 128f - paddingLeft - paddingRight
        assertEquals(44f, r.minHeight) // 64f - paddingTop - paddingBottom
        assertEquals(236f, r.maxHeight) // 256f - paddingTop - paddingBottom

        // Since none of children are connected to each-other, they are all considered single-node chains, which
        // extend to the full available size. Size all of them have the same margin, they end up at exactly the same
        // position and occupying the same area.

        assertEquals("(8.0, 8.0)", layout.children[0].computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", layout.children[1].computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", layout.children[2].computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", layout.children[3].computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", layout.children[4].computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", layout.children[5].computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", layout.children[6].computed.relativePos.toString())
        assertEquals("(8.0, 8.0)", layout.children[7].computed.relativePos.toString())

        assertEquals("(38.0, 28.0)", layout.children[0].computed.size.toString())
        assertEquals("(38.0, 28.0)", layout.children[1].computed.size.toString())
        assertEquals("(38.0, 28.0)", layout.children[2].computed.size.toString())
        assertEquals("(38.0, 28.0)", layout.children[3].computed.size.toString())
        assertEquals("(38.0, 28.0)", layout.children[4].computed.size.toString())
        assertEquals("(38.0, 28.0)", layout.children[5].computed.size.toString())
        assertEquals("(38.0, 28.0)", layout.children[6].computed.size.toString())
        assertEquals("(38.0, 28.0)", layout.children[7].computed.size.toString())
    }

    @Test
    fun `should properly compute sizes of a row left-to-right when width of layout needs to grow`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            paddingLeft = 2f
            paddingTop = 4f
            paddingRight = 8f
            paddingBottom = 16f

            val f0 = frame {
                params.apply {
                    alignParentLeft = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 8f
                    marginTop = 8f
                    marginRight = 8f
                    marginBottom = 8f
                }
            }
            val f1 = frame {
                params.apply {
                    rightOf = f0
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 4f
                    marginTop = 16f
                    marginRight = 4f
                    marginBottom = 2f
                }
            }
            frame {
                params.apply {
                    rightOf = f1
                    alignParentRight = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 8f
                    marginTop = 32f
                    marginRight = 8f
                    marginBottom = 4f
                }
            }
        }
        val nodeMap = NodeMap.createFromLayout(layout)
        val r = PreArranger().setInitialPosAndSizes(
            layout,
            minWidth = 0f,
            maxWidth = 256f,
            minHeight = 0f,
            maxHeight = 256f,
            nodeMap,
        )

        assertEquals(32f * 3f + 8f + 8f + 8f + 8f, r.minWidth)
        assertEquals(246f, r.maxWidth) // 256f - paddingLeft - paddingRight
        assertEquals(32f + 24f + 4f, r.minHeight) // f2.marginTop + f2.minHeight + f2.marginBottom
        assertEquals(236f, r.maxHeight) // 256f - paddingTop - paddingBottom

        assertEquals("(8.0, 8.0)", layout.children[0].computed.relativePos.toString())
        assertEquals("(48.0, 16.0)", layout.children[1].computed.relativePos.toString())
        assertEquals("(88.0, 32.0)", layout.children[2].computed.relativePos.toString())

        // f2 has the largest vertical margin and thus define the layout's height. f2 sticks to its minimal height.
        // f0 and f1 extend to the layout's height, since they get the full weight of a single-node chain.
        assertEquals("(32.0, 44.0)", layout.children[0].computed.size.toString())
        assertEquals("(32.0, 42.0)", layout.children[1].computed.size.toString())
        assertEquals("(32.0, 24.0)", layout.children[2].computed.size.toString())
    }

    @Test
    fun `should properly compute sizes of a row right-to-left when width of layout needs to grow`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            paddingLeft = 2f
            paddingTop = 4f
            paddingRight = 8f
            paddingBottom = 16f

            val f0 = frame {
                params.apply {
                    alignParentRight = true
                    alignParentBottom = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 2f
                    marginTop = 1f
                    marginRight = 2f
                    marginBottom = 4f
                }
            }
            val f1 = frame {
                params.apply {
                    leftOf = f0
                    alignParentBottom = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 4f
                    marginTop = 2f
                    marginRight = 4f
                    marginBottom = 2f
                }
            }
            frame {
                params.apply {
                    leftOf = f1
                    alignParentLeft = true
                    alignParentBottom = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 4f
                    marginTop = 4f
                    marginRight = 4f
                    marginBottom = 1f
                }
            }
        }
        val nodeMap = NodeMap.createFromLayout(layout)
        val r = PreArranger().setInitialPosAndSizes(
            layout,
            minWidth = 0f,
            maxWidth = 128f,
            minHeight = 0f,
            maxHeight = 256f,
            nodeMap,
        )

        assertEquals(32f * 3f + 4f + 2f + 4f + 4f, r.minWidth)
        assertEquals(118f, r.maxWidth) // 128f - paddingLeft - paddingRight
        assertEquals(1f + 24f + 4f, r.minHeight) // f0.marginTop + f0.minHeight + f0.marginBottom
        assertEquals(236f, r.maxHeight) // 256f - paddingTop - paddingBottom

        assertEquals("(76.0, 1.0)", layout.children[0].computed.relativePos.toString())
        assertEquals("(40.0, 2.0)", layout.children[1].computed.relativePos.toString())
        assertEquals("(4.0, 4.0)", layout.children[2].computed.relativePos.toString())

        assertEquals("(32.0, 24.0)", layout.children[0].computed.size.toString())
        assertEquals("(32.0, 25.0)", layout.children[1].computed.size.toString())
        assertEquals("(32.0, 24.0)", layout.children[2].computed.size.toString())
    }

    @Test
    fun `should properly compute sizes of a row left-to-right when min width of layout is larger`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            paddingLeft = 2f
            paddingTop = 4f
            paddingRight = 8f
            paddingBottom = 16f

            val f0 = frame {
                params.apply {
                    alignParentLeft = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 8f
                    marginTop = 8f
                    marginRight = 8f
                    marginBottom = 8f
                }
            }
            val f1 = frame {
                params.apply {
                    rightOf = f0
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 4f
                    marginTop = 16f
                    marginRight = 4f
                    marginBottom = 2f
                }
            }
            frame {
                params.apply {
                    rightOf = f1
                    alignParentRight = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 24f
                    marginLeft = 8f
                    marginTop = 32f
                    marginRight = 8f
                    marginBottom = 4f
                }
            }
        }

        val nodeMap = NodeMap.createFromLayout(layout)

        val r = PreArranger().setInitialPosAndSizes(
            layout,
            minWidth = 170f,
            maxWidth = 256f,
            minHeight = 96f,
            maxHeight = 256f,
            nodeMap,
        )

        assertEquals(160f, r.minWidth) // inner min is only 128, was forced larger by minWidth
        assertEquals(256f - 2f - 8f, r.maxWidth) // 246f
        assertEquals(76f, r.minHeight) // inner min is only 60f, was forced larger by minHeight
        assertEquals(256f - 4f - 16f, r.maxHeight) // 236f

        // The available width was evenly distributed
        assertEquals("(42.0, 60.0)", layout.children[0].computed.size.toString())
        assertEquals("(42.0, 58.0)", layout.children[1].computed.size.toString())
        assertEquals("(42.0, 40.0)", layout.children[2].computed.size.toString())

        // 8 + 42 + 8 + 42 + 8 + 42 + 8 = 158, which is < 160 (available width)
        // Since neither resulting size nor positions have fractional part, the superfluous width results in a larger
        // margin. The left-most child gets this extra margin, because the last child visited (f2) is right-aligned,
        // causing a traversal from right-to-left.
        assertEquals("(10.0, 8.0)", layout.children[0].computed.relativePos.toString())
        assertEquals("(60.0, 16.0)", layout.children[1].computed.relativePos.toString()) // x = 10 + 42 + 8
        assertEquals("(110.0, 32.0)", layout.children[2].computed.relativePos.toString()) // x = 60 + 42 + 8

    }

    @Test
    fun `should properly compute sizes of a layout in a T-like structure`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            padding(8f)

            val f0 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentLeft = true
                    minWidth = 16f
                    minHeight = 16f
                }
            }

            val f1 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    rightOf = f0
                    minWidth = 24f
                    minHeight = 24f
                }
            }

            val f2 = frame {
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    rightOf = f1
                    minWidth = 8f
                    minHeight = 8f
                }
            }

            val f3 = frame {
                params.apply {
                    margin(8f)
                    rightOf = f0
                    below = f1
                    minWidth = 16f
                    minHeight = 16f
                }
            }

            // f4
            frame {
                params.apply {
                    margin(8f)
                    leftOf = f2
                    below = f3
                    minWidth = 8f
                    minHeight = 8f
                }
            }
        }

        val nodeMap = NodeMap.createFromLayout(layout)

        val r = PreArranger().setInitialPosAndSizes(
            layout,
            minWidth = 0f,
            maxWidth = 128f,
            minHeight = 0f,
            maxHeight = 256f,
            nodeMap,
        )

        assertEquals(8f + 16f + 8f + 24f + 8f + 8f + 8f, r.minWidth) // 80
        assertEquals(8f + 24f + 8f + 16f + 8f + 8f + 8f, r.minHeight) // 80

        assertEquals(128f - 8f - 8f, r.maxWidth)
        assertEquals(256f - 8f - 8f, r.maxHeight)

        assertEquals("(8.0, 8.0)", layout.children[0].computed.relativePos.toString())
        assertEquals("(32.0, 8.0)", layout.children[1].computed.relativePos.toString())
        assertEquals("(64.0, 8.0)", layout.children[2].computed.relativePos.toString())
        assertEquals("(32.0, 40.0)", layout.children[3].computed.relativePos.toString()) // 32 = 8 + 16 + 8
        assertEquals("(8.0, 64.0)", layout.children[4].computed.relativePos.toString())

        assertEquals("(16.0, 64.0)", layout.children[0].computed.size.toString())
        assertEquals("(24.0, 24.0)", layout.children[1].computed.size.toString())
        assertEquals("(8.0, 64.0)", layout.children[2].computed.size.toString())

        // The PreArranger cannot distribute sizes correctly when constraints interact.
        // Fixing this is not the PreArranger's concern, but needs to be solved by IterativeArranger.
        // x=80 - 8 - 16 - 8 - 8 = 40; not fully used, because f3 assumes f0 would also extend
        // y is min, because this chain defined the layout height in the first place
        assertEquals("(28.0, 16.0)", layout.children[3].computed.size.toString())

        // x=64 - 8 - 8 = 48; not fully used, because f4 assumes f2 would also extend
        // y is min, because this chain defined the layout height in the first place
        assertEquals("(28.0, 8.0)", layout.children[4].computed.size.toString())
    }
}

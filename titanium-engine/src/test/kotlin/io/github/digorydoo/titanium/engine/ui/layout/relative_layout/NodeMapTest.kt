package io.github.digorydoo.titanium.engine.ui.layout.relative_layout

import io.github.digorydoo.titanium.engine.ui.layout.Layout
import io.github.digorydoo.titanium.engine.ui.layout.LayoutParams
import kotlin.test.*

internal class NodeMapTest {
    @Test
    fun `should build an empty NodeMap from an empty layout`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) { }
        val nodeMap = NodeMap.createFromLayout(layout)
        assertTrue(nodeMap.isEmpty(), "isEmpty")
        assertTrue(nodeMap.alignedToParentLeft.isEmpty(), "alignedToParentLeft")
        assertTrue(nodeMap.alignedToParentTop.isEmpty(), "alignedToParentTop")
        assertTrue(nodeMap.alignedToParentRight.isEmpty(), "alignedToParentRight")
        assertTrue(nodeMap.alignedToParentBottom.isEmpty(), "alignedToParentBottom")
    }

    @Test
    fun `should build a NodeMap with the expected values from a layout whose children are fully disconnected`() {
        val rootParams = object: LayoutParams {}
        var child0: Layout<RelativeLayoutParams, *>? = null
        var child1: Layout<RelativeLayoutParams, *>? = null

        val layout = RelativeLayout.build(rootParams) {
            child0 = frame { padding(4f) }
            child1 = frame { padding(4f) }
        }

        val nodeMap = NodeMap.createFromLayout(layout)
        assertEquals(2, nodeMap.size, "size")

        assertNotNull(child0)
        assertNotNull(child1)

        val node0 = nodeMap[child0]
        val node1 = nodeMap[child1]

        assertSame(child0, node0.child)
        assertSame(child1, node1.child)

        nodeMap.apply {
            assertTrue(alignedToParentLeft.isEmpty())
            assertTrue(alignedToParentTop.isEmpty())
            assertTrue(alignedToParentRight.isEmpty())
            assertTrue(alignedToParentBottom.isEmpty())
        }

        node0.apply {
            assertTrue(left.isEmpty())
            assertTrue(top.isEmpty())
            assertTrue(right.isEmpty())
            assertTrue(bottom.isEmpty())

            assertFalse(isLeftConnected)
            assertFalse(isTopConnected)
            assertFalse(isRightConnected)
            assertFalse(isBottomConnected)

            assertEquals(0, numNodesFromLeftEnd)
            assertEquals(0, numNodesFromTopEnd)
            assertEquals(0, numNodesFromRightEnd)
            assertEquals(0, numNodesFromBottomEnd)

            assertEquals(0f, minWidthTowardsLeftEnd)
            assertEquals(0f, minHeightTowardsTopEnd)
            assertEquals(0f, minWidthTowardsRightEnd)
            assertEquals(0f, minHeightTowardsBottomEnd)
        }

        node1.apply {
            assertTrue(left.isEmpty())
            assertTrue(top.isEmpty())
            assertTrue(right.isEmpty())
            assertTrue(bottom.isEmpty())

            assertFalse(isLeftConnected)
            assertFalse(isTopConnected)
            assertFalse(isRightConnected)
            assertFalse(isBottomConnected)

            assertEquals(0, numNodesFromLeftEnd)
            assertEquals(0, numNodesFromTopEnd)
            assertEquals(0, numNodesFromRightEnd)
            assertEquals(0, numNodesFromBottomEnd)

            assertEquals(0f, minWidthTowardsLeftEnd)
            assertEquals(0f, minHeightTowardsTopEnd)
            assertEquals(0f, minWidthTowardsRightEnd)
            assertEquals(0f, minHeightTowardsBottomEnd)
        }
    }

    @Test
    fun `should build a NodeMap from a layout whose children are internally connected but disconnected from parent`() {
        val rootParams = object: LayoutParams {}
        var child0: Layout<RelativeLayoutParams, *>? = null
        var child1: Layout<RelativeLayoutParams, *>? = null
        var child2: Layout<RelativeLayoutParams, *>? = null

        val layout = RelativeLayout.build(rootParams) {
            child0 = frame {
                padding(4f)
                params.apply {
                    marginLeft = 1f
                    marginRight = 2f
                }
            }
            child1 = frame {
                padding(4f)
                params.apply {
                    leftOf = child0
                    marginLeft = 4f
                    marginRight = 8f
                    marginTop = 1f
                    marginBottom = 2f
                }
            }
            child2 = frame {
                padding(4f)
                params.apply {
                    above = child1
                    marginLeft = 16f
                    marginRight = 32f
                    marginTop = 4f
                    marginBottom = 8f
                }
            }
        }

        val nodeMap = NodeMap.createFromLayout(layout)
        assertEquals(3, nodeMap.size, "size")

        assertNotNull(child0)
        assertNotNull(child1)
        assertNotNull(child2)

        val node0 = nodeMap[child0]
        val node1 = nodeMap[child1]
        val node2 = nodeMap[child2]

        assertSame(child0, node0.child)
        assertSame(child1, node1.child)
        assertSame(child2, node2.child)

        nodeMap.apply {
            assertTrue(alignedToParentLeft.isEmpty())
            assertTrue(alignedToParentTop.isEmpty())
            assertTrue(alignedToParentRight.isEmpty())
            assertTrue(alignedToParentBottom.isEmpty())
        }

        node0.apply {
            assertEquals(1, left.size)
            assertSame(node1, left.first())
            assertEquals(0, top.size)
            assertEquals(0, right.size)
            assertEquals(0, bottom.size)

            assertTrue(isLeftConnected)
            assertFalse(isTopConnected)
            assertFalse(isRightConnected)
            assertFalse(isBottomConnected)

            assertEquals(1, numNodesFromLeftEnd)
            assertEquals(0, numNodesFromTopEnd)
            assertEquals(0, numNodesFromRightEnd)
            assertEquals(0, numNodesFromBottomEnd)

            assertEquals(20f, minWidthTowardsLeftEnd) // maxOf(1, 8) + 8 + 4
            assertEquals(0f, minHeightTowardsTopEnd)
            assertEquals(2f, minWidthTowardsRightEnd) // marginRight
            assertEquals(0f, minHeightTowardsBottomEnd)
        }

        node1.apply {
            assertEquals(0, left.size)
            assertEquals(1, top.size)
            assertSame(node2, top.first())
            assertEquals(1, right.size)
            assertSame(node0, right.first())
            assertEquals(0, bottom.size)

            assertFalse(isLeftConnected)
            assertTrue(isTopConnected)
            assertTrue(isRightConnected)
            assertFalse(isBottomConnected)

            assertEquals(0, numNodesFromLeftEnd)
            assertEquals(1, numNodesFromTopEnd)
            assertEquals(1, numNodesFromRightEnd)
            assertEquals(0, numNodesFromBottomEnd)

            assertEquals(4f, minWidthTowardsLeftEnd) // marginLeft
            assertEquals(20f, minHeightTowardsTopEnd) // maxOf(1, 8) + 8 + 4
            assertEquals(18f, minWidthTowardsRightEnd) // maxOf(8, 1) + 8 + 2
            assertEquals(2f, minHeightTowardsBottomEnd) // marginBottom
        }

        node2.apply {
            assertEquals(0, left.size)
            assertEquals(0, top.size)
            assertEquals(0, right.size)
            assertEquals(1, bottom.size)
            assertSame(node1, bottom.first())

            assertFalse(isLeftConnected)
            assertFalse(isTopConnected)
            assertFalse(isRightConnected)
            assertTrue(isBottomConnected)

            assertEquals(0, numNodesFromLeftEnd)
            assertEquals(0, numNodesFromTopEnd)
            assertEquals(0, numNodesFromRightEnd)
            assertEquals(1, numNodesFromBottomEnd)

            assertEquals(16f, minWidthTowardsLeftEnd) // marginLeft
            assertEquals(4f, minHeightTowardsTopEnd) // marginTop
            assertEquals(32f, minWidthTowardsRightEnd) // marginRight
            assertEquals(18f, minHeightTowardsBottomEnd) // maxOf(8, 1) + 8 + 2
        }
    }

    @Test
    fun `should build a NodeMap from a layout whose children are connected to parent`() {
        val rootParams = object: LayoutParams {}
        var child0: Layout<RelativeLayoutParams, *>? = null
        var child1: Layout<RelativeLayoutParams, *>? = null
        var child2: Layout<RelativeLayoutParams, *>? = null

        val layout = RelativeLayout.build(rootParams) {
            child0 = frame {
                paddingLeft = 1f
                paddingTop = 2f
                paddingRight = 4f
                paddingBottom = 8f
                params.apply {
                    alignParentLeft = true
                    marginLeft = 1f
                    marginRight = 2f
                    marginTop = 1f
                    marginBottom = 2f
                }
            }
            child1 = frame {
                paddingLeft = 16f
                paddingTop = 32f
                paddingRight = 64f
                paddingBottom = 128f
                params.apply {
                    rightOf = child0
                    alignParentTop = true
                    marginLeft = 4f
                    marginRight = 8f
                    marginTop = 4f
                    marginBottom = 8f
                }
            }
            child0.params.below = child1
            child2 = frame {
                paddingLeft = 256f
                paddingTop = 512f
                paddingRight = 1024f
                paddingBottom = 2048f
                params.apply {
                    rightOf = child1
                    below = child0
                    marginLeft = 16f
                    marginRight = 32f
                    marginTop = 16f
                    marginBottom = 32f
                }
            }
        }

        val nodeMap = NodeMap.createFromLayout(layout)
        assertEquals(3, nodeMap.size, "size")

        assertNotNull(child0)
        assertNotNull(child1)
        assertNotNull(child2)

        val node0 = nodeMap[child0]
        val node1 = nodeMap[child1]
        val node2 = nodeMap[child2]

        assertSame(child0, node0.child)
        assertSame(child1, node1.child)
        assertSame(child2, node2.child)

        nodeMap.apply {
            assertEquals(1, alignedToParentLeft.size)
            assertSame(node0, alignedToParentLeft.first())
            assertEquals(1, alignedToParentTop.size)
            assertSame(node1, alignedToParentTop.first())
            assertEquals(0, alignedToParentRight.size)
            assertEquals(0, alignedToParentBottom.size)
        }

        node0.apply {
            assertEquals(0, left.size)
            assertTrue(isLeftConnected) // parent left

            assertEquals(1, top.size)
            assertSame(node1, top.first())
            assertTrue(isTopConnected)

            assertEquals(1, right.size)
            assertSame(node1, right.first())
            assertTrue(isRightConnected)

            assertEquals(1, bottom.size)
            assertSame(node2, bottom.first())
            assertTrue(isBottomConnected)

            assertEquals(0, numNodesFromLeftEnd)
            assertEquals(1, numNodesFromTopEnd)
            assertEquals(2, numNodesFromRightEnd)
            assertEquals(1, numNodesFromBottomEnd)

            assertEquals(1f, minWidthTowardsLeftEnd) // child0.marginLeft
            assertEquals(172f, minHeightTowardsTopEnd) // max(1, 8) + 32 + 128 + 4
            assertEquals(1412f, minWidthTowardsRightEnd) // max(2, 4) + 16 + 64 + max(8, 16) + 256 + 1024 + 32
            assertEquals(2608f, minHeightTowardsBottomEnd) // max(2, 16) + 512 + 2048 + 32
        }

        node1.apply {
            assertEquals(1, left.size)
            assertSame(node0, left.first())
            assertTrue(isLeftConnected)

            assertEquals(0, top.size)
            assertTrue(isTopConnected) // parent top

            assertEquals(1, right.size)
            assertSame(node2, right.first())
            assertTrue(isRightConnected)

            assertEquals(1, bottom.size)
            assertSame(node0, bottom.first())
            assertTrue(isBottomConnected)

            assertEquals(1, numNodesFromLeftEnd)
            assertEquals(0, numNodesFromTopEnd)
            assertEquals(1, numNodesFromRightEnd)
            assertEquals(2, numNodesFromBottomEnd)

            assertEquals(10f, minWidthTowardsLeftEnd) // max(4, 2) + 1 + 4 + 1
            assertEquals(4f, minHeightTowardsTopEnd) // child1.marginTop
            assertEquals(1328f, minWidthTowardsRightEnd) // max(8, 16) + 256 + 1024 + 32
            assertEquals(2626f, minHeightTowardsBottomEnd) // max(8, 1) + 2 + 8 + max(2, 16) + 512 + 2048 + 32
        }

        node2.apply {
            assertEquals(1, left.size)
            assertSame(node1, left.first())
            assertTrue(isLeftConnected)

            assertEquals(1, top.size)
            assertSame(node0, top.first())
            assertTrue(isTopConnected)

            assertEquals(0, right.size)
            assertFalse(isRightConnected)

            assertEquals(0, bottom.size)
            assertFalse(isBottomConnected)

            assertEquals(2, numNodesFromLeftEnd)
            assertEquals(2, numNodesFromTopEnd)
            assertEquals(0, numNodesFromRightEnd)
            assertEquals(0, numNodesFromBottomEnd)

            assertEquals(106f, minWidthTowardsLeftEnd) // max(16, 8) + 16 + 64 + max(4, 2) + 1 + 4 + 1
            assertEquals(198f, minHeightTowardsTopEnd) // max(16, 2) + 2 + 8 + max(1, 8) + 32 + 128 + 4
            assertEquals(32f, minWidthTowardsRightEnd) // child2.marginRight
            assertEquals(32f, minHeightTowardsBottomEnd) // child2.marginBottom
        }
    }

    @Test
    fun `should build a NodeMap from a Layout whose children are arranged in a row left-to-right`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            val f0 = frame {
                params.apply {
                    alignParentLeft = true
                    alignParentTop = true
                    minWidth = 16f
                    minHeight = 16f
                }
            }
            val f1 = frame {
                params.apply {
                    rightOf = f0
                    alignParentTop = true
                    minWidth = 24f
                    minHeight = 24f
                }
            }
            frame {
                params.apply {
                    rightOf = f1
                    alignParentRight = true
                    alignParentTop = true
                    minWidth = 32f
                    minHeight = 32f
                }
            }
        }
        val nodeMap = NodeMap.createFromLayout(layout)

        nodeMap[layout.children[0]].let { n0 ->
            assertEquals(0, n0.left.size)
            assertEquals(0, n0.top.size)
            assertEquals(1, n0.right.size)
            assertEquals(0, n0.bottom.size)

            assertTrue(n0.isLeftConnected)
            assertTrue(n0.isTopConnected)
            assertTrue(n0.isRightConnected)
            assertFalse(n0.isBottomConnected)

            assertEquals(0, n0.numNodesFromLeftEnd)
            assertEquals(0, n0.numNodesFromTopEnd)
            assertEquals(2, n0.numNodesFromRightEnd)
            assertEquals(0, n0.numNodesFromBottomEnd)

            assertEquals(0f, n0.minWidthTowardsLeftEnd)
            assertEquals(0f, n0.minHeightTowardsTopEnd)
            assertEquals(56f, n0.minWidthTowardsRightEnd) // 24 + 32
            assertEquals(0f, n0.minHeightTowardsBottomEnd)
        }
        nodeMap[layout.children[1]].let { n1 ->
            assertEquals(1, n1.left.size)
            assertEquals(0, n1.top.size)
            assertEquals(1, n1.right.size)
            assertEquals(0, n1.bottom.size)

            assertTrue(n1.isLeftConnected)
            assertTrue(n1.isTopConnected)
            assertTrue(n1.isRightConnected)
            assertFalse(n1.isBottomConnected)

            assertEquals(1, n1.numNodesFromLeftEnd)
            assertEquals(0, n1.numNodesFromTopEnd)
            assertEquals(1, n1.numNodesFromRightEnd)
            assertEquals(0, n1.numNodesFromBottomEnd)

            assertEquals(16f, n1.minWidthTowardsLeftEnd)
            assertEquals(0f, n1.minHeightTowardsTopEnd)
            assertEquals(32f, n1.minWidthTowardsRightEnd)
            assertEquals(0f, n1.minHeightTowardsBottomEnd)
        }
        nodeMap[layout.children[2]].let { n2 ->
            assertEquals(1, n2.left.size)
            assertEquals(0, n2.top.size)
            assertEquals(0, n2.right.size)
            assertEquals(0, n2.bottom.size)

            assertTrue(n2.isLeftConnected)
            assertTrue(n2.isTopConnected)
            assertTrue(n2.isRightConnected)
            assertFalse(n2.isBottomConnected)

            assertEquals(2, n2.numNodesFromLeftEnd)
            assertEquals(0, n2.numNodesFromTopEnd)
            assertEquals(0, n2.numNodesFromRightEnd)
            assertEquals(0, n2.numNodesFromBottomEnd)

            assertEquals(40f, n2.minWidthTowardsLeftEnd) // 24 + 16
            assertEquals(0f, n2.minHeightTowardsTopEnd)
            assertEquals(0f, n2.minWidthTowardsRightEnd)
            assertEquals(0f, n2.minHeightTowardsBottomEnd)
        }
    }

    @Test
    fun `should build a NodeMap from a Layout whose children are arranged in a T-like structure`() {
        val rootParams = object: LayoutParams {}
        val layout = RelativeLayout.build(rootParams) {
            padding(8f)

            val f0 = frame {
                name = "f0"
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    alignParentLeft = true
                    minWidth = 16f
                    minHeight = 16f
                }
            }

            val f1 = frame {
                name = "f1"
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    rightOf = f0
                    minWidth = 24f
                    minHeight = 24f
                }
            }

            val f2 = frame {
                name = "f2"
                params.apply {
                    margin(8f)
                    alignParentTop = true
                    rightOf = f1
                    minWidth = 8f
                    minHeight = 8f
                }
            }

            val f3 = frame {
                name = "f3"
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
                name = "f4"
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

        nodeMap[layout.children[0]].let { n0 ->
            assertEquals(0, n0.left.size)
            assertEquals(0, n0.top.size)
            assertEquals(2, n0.right.size)
            assertEquals(0, n0.bottom.size)

            assertTrue(n0.isLeftConnected)
            assertTrue(n0.isTopConnected)
            assertTrue(n0.isRightConnected)
            assertFalse(n0.isBottomConnected)

            assertEquals(0, n0.numNodesFromLeftEnd)
            assertEquals(0, n0.numNodesFromTopEnd)
            assertEquals(2, n0.numNodesFromRightEnd)
            assertEquals(0, n0.numNodesFromBottomEnd)

            assertEquals(8f, n0.minWidthTowardsLeftEnd)
            assertEquals(8f, n0.minHeightTowardsTopEnd)
            assertEquals(56f, n0.minWidthTowardsRightEnd) // 8 + 24 + 8 + 8 + 8
            assertEquals(8f, n0.minHeightTowardsBottomEnd)
        }
        nodeMap[layout.children[1]].let { n1 ->
            assertEquals(1, n1.left.size)
            assertEquals(0, n1.top.size)
            assertEquals(1, n1.right.size)
            assertEquals(1, n1.bottom.size)

            assertTrue(n1.isLeftConnected)
            assertTrue(n1.isTopConnected)
            assertTrue(n1.isRightConnected)
            assertTrue(n1.isBottomConnected)

            assertEquals(1, n1.numNodesFromLeftEnd)
            assertEquals(0, n1.numNodesFromTopEnd)
            assertEquals(1, n1.numNodesFromRightEnd)
            assertEquals(2, n1.numNodesFromBottomEnd)

            assertEquals(32f, n1.minWidthTowardsLeftEnd) // 8 + 16 + 8
            assertEquals(8f, n1.minHeightTowardsTopEnd)
            assertEquals(24f, n1.minWidthTowardsRightEnd) // 8 + 8 + 8
            assertEquals(48f, n1.minHeightTowardsBottomEnd) // 8 + 16 + 8 + 8 + 8
        }
        nodeMap[layout.children[2]].let { n2 ->
            assertEquals(2, n2.left.size)
            assertEquals(0, n2.top.size)
            assertEquals(0, n2.right.size)
            assertEquals(0, n2.bottom.size)

            assertTrue(n2.isLeftConnected)
            assertTrue(n2.isTopConnected)
            assertFalse(n2.isRightConnected)
            assertFalse(n2.isBottomConnected)

            assertEquals(2, n2.numNodesFromLeftEnd)
            assertEquals(0, n2.numNodesFromTopEnd)
            assertEquals(0, n2.numNodesFromRightEnd)
            assertEquals(0, n2.numNodesFromBottomEnd)

            assertEquals(64f, n2.minWidthTowardsLeftEnd) // 8 + 24 + 8 + 16 + 8
            assertEquals(8f, n2.minHeightTowardsTopEnd)
            assertEquals(8f, n2.minWidthTowardsRightEnd)
            assertEquals(8f, n2.minHeightTowardsBottomEnd)
        }
        nodeMap[layout.children[3]].let { n3 ->
            assertEquals(1, n3.left.size)
            assertEquals(1, n3.top.size)
            assertEquals(0, n3.right.size)
            assertEquals(1, n3.bottom.size)

            assertTrue(n3.isLeftConnected)
            assertTrue(n3.isTopConnected)
            assertFalse(n3.isRightConnected)
            assertTrue(n3.isBottomConnected)

            assertEquals(1, n3.numNodesFromLeftEnd)
            assertEquals(1, n3.numNodesFromTopEnd)
            assertEquals(0, n3.numNodesFromRightEnd)
            assertEquals(1, n3.numNodesFromBottomEnd)

            assertEquals(32f, n3.minWidthTowardsLeftEnd) // 8 + 16 + 8
            assertEquals(40f, n3.minHeightTowardsTopEnd) // 8 + 24 + 8
            assertEquals(8f, n3.minWidthTowardsRightEnd)
            assertEquals(24f, n3.minHeightTowardsBottomEnd) // 8 + 8 + 8
        }
        nodeMap[layout.children[4]].let { n4 ->
            assertEquals(0, n4.left.size)
            assertEquals(1, n4.top.size)
            assertEquals(1, n4.right.size)
            assertEquals(0, n4.bottom.size)

            assertFalse(n4.isLeftConnected)
            assertTrue(n4.isTopConnected)
            assertTrue(n4.isRightConnected)
            assertFalse(n4.isBottomConnected)

            assertEquals(0, n4.numNodesFromLeftEnd)
            assertEquals(2, n4.numNodesFromTopEnd)
            assertEquals(1, n4.numNodesFromRightEnd)
            assertEquals(0, n4.numNodesFromBottomEnd)

            assertEquals(8f, n4.minWidthTowardsLeftEnd)
            assertEquals(64f, n4.minHeightTowardsTopEnd) // 8 + 16 + 8 + 24 + 8
            assertEquals(24f, n4.minWidthTowardsRightEnd) // 8 + 8 + 8
            assertEquals(8f, n4.minHeightTowardsBottomEnd)
        }
    }
}

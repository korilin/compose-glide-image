package com.korilin.samples.glide

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.times
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import kotlin.math.max
import kotlin.math.roundToInt

internal fun Modifier.glidePainterNode(
    painter: Painter,
    contentDescription: String? = null,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter? = null,
): Modifier {
    return clipToBounds()
        .semantics {
            if (contentDescription != null) {
                this@semantics.contentDescription = contentDescription
            }
            role = Role.Image
        } then GlidePainterElement(
        painter = painter,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

internal data class GlidePainterElement(
    val painter: Painter,
    val alignment: Alignment,
    val contentScale: ContentScale,
    val alpha: Float,
    val colorFilter: ColorFilter?
) : ModifierNodeElement<GlidePainterNode>() {

    override fun create(): GlidePainterNode {
        return GlidePainterNode(
            painter = painter,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    }

    override fun update(node: GlidePainterNode) {
        val intrinsicsChanged = node.painter.intrinsicSize != painter.intrinsicSize

        node.painter = painter
        node.alignment = alignment
        node.contentScale = contentScale
        node.alpha = alpha
        node.colorFilter = colorFilter

        // Only remeasure if intrinsics have changed.
        if (intrinsicsChanged) {
            node.invalidateMeasurement()
        }
        // redraw because one of the node properties has changed.
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "paint"
        properties["painter"] = painter
        properties["alignment"] = alignment
        properties["contentScale"] = contentScale
        properties["alpha"] = alpha
        properties["colorFilter"] = colorFilter
    }
}

internal class GlidePainterNode(
    var painter: Painter,
    var alignment: Alignment,
    var contentScale: ContentScale,
    var alpha: Float,
    var colorFilter: ColorFilter?,
) : Modifier.Node(), DrawModifierNode, LayoutModifierNode {

    /**
     * Helper property to determine if we should size content to the intrinsic
     * size of the Painter or not. This is only done if the Painter has an intrinsic size
     */
    private val painterIntrinsicSizeSpecified: Boolean
        get() = painter.intrinsicSize.isSpecified

    override val shouldAutoInvalidate: Boolean
        get() = false

    private inline fun log(subtag: String, message: () -> String) {
        Logger.log("GlidePainterNode") {
            "[$subtag] ${message()}"
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val modified = modifyConstraints(constraints)
        log("measure") { "$constraints -> $modified" }
        val placeable = measurable.measure(modified)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        val layoutWidth = measurable.minIntrinsicWidth(height)
        return if (painterIntrinsicSizeSpecified) {
            val constraints = modifyConstraints(Constraints(maxHeight = height))
            max(constraints.minWidth, layoutWidth)
        } else {
            layoutWidth
        }.also { log("minIntrinsicWidth") { "$layoutWidth -> $it" } }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        val layoutWidth = measurable.maxIntrinsicWidth(height)
        return if (painterIntrinsicSizeSpecified) {
            val constraints = modifyConstraints(Constraints(maxHeight = height))
            max(constraints.minWidth, layoutWidth)
        } else {
            layoutWidth
        }.also { log("maxIntrinsicWidth") { "$layoutWidth -> $it" } }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        val layoutHeight = measurable.minIntrinsicHeight(width)
        return if (painterIntrinsicSizeSpecified) {
            val constraints = modifyConstraints(Constraints(maxWidth = width))
            max(constraints.minHeight, layoutHeight)
        } else {
            layoutHeight
        }.also { log("minIntrinsicHeight") { "$layoutHeight -> $it" } }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        val layoutHeight = measurable.maxIntrinsicHeight(width)
        return if (painterIntrinsicSizeSpecified) {
            val constraints = modifyConstraints(Constraints(maxWidth = width))
            max(constraints.minHeight, layoutHeight)
        } else {
            layoutHeight
        }.also { log("maxIntrinsicHeight") { "$layoutHeight -> $it" } }
    }

    private fun calculateScaledSize(dstSize: Size): Size {
        return if (painterIntrinsicSizeSpecified) {
            val srcWidth = if (!painter.intrinsicSize.hasSpecifiedAndFiniteWidth()) {
                dstSize.width
            } else {
                painter.intrinsicSize.width
            }

            val srcHeight = if (!painter.intrinsicSize.hasSpecifiedAndFiniteHeight()) {
                dstSize.height
            } else {
                painter.intrinsicSize.height
            }

            val srcSize = Size(srcWidth, srcHeight)
            if (dstSize.width != 0f && dstSize.height != 0f) {
                srcSize * contentScale.computeScaleFactor(srcSize, dstSize)
            } else {
                Size.Zero
            }

        } else {
            dstSize
        }
    }

    private fun modifyConstraints(constraints: Constraints): Constraints {
        val hasBoundedDimens = constraints.hasBoundedWidth && constraints.hasBoundedHeight
        val hasFixedDimens = constraints.hasFixedWidth && constraints.hasFixedHeight
        if (hasFixedDimens || (!painterIntrinsicSizeSpecified && hasBoundedDimens)) {
            // If we have fixed constraints or we are not attempting to size the
            // composable based on the size of the Painter, do not attempt to
            // modify them. Otherwise rely on Alignment and ContentScale
            // to determine how to position the drawing contents of the Painter within
            // the provided bounds
            return constraints.copy(
                minWidth = constraints.maxWidth,
                minHeight = constraints.maxHeight
            )
        }

        val intrinsicSize = painter.intrinsicSize
        val intrinsicWidth =
            if (intrinsicSize.hasSpecifiedAndFiniteWidth()) {
                intrinsicSize.width.roundToInt()
            } else {
                constraints.minWidth
            }

        val intrinsicHeight =
            if (intrinsicSize.hasSpecifiedAndFiniteHeight()) {
                intrinsicSize.height.roundToInt()
            } else {
                constraints.minHeight
            }

        // Scale the width and height appropriately based on the given constraints
        // and ContentScale
        val constrainedWidth = constraints.constrainWidth(intrinsicWidth)
        val constrainedHeight = constraints.constrainHeight(intrinsicHeight)
        val scaledSize = calculateScaledSize(
            Size(constrainedWidth.toFloat(), constrainedHeight.toFloat())
        )

        // For both width and height constraints, consume the minimum of the scaled width
        // and the maximum constraint as some scale types can scale larger than the maximum
        // available size (ex ContentScale.Crop)
        // In this case the larger of the 2 dimensions is used and the aspect ratio is
        // maintained. Even if the size of the composable is smaller, the painter will
        // draw its content clipped
        val minWidth = constraints.constrainWidth(scaledSize.width.roundToInt())
        val minHeight = constraints.constrainHeight(scaledSize.height.roundToInt())
        return constraints.copy(minWidth = minWidth, minHeight = minHeight)
    }

    override fun ContentDrawScope.draw() {
        val intrinsicSize = painter.intrinsicSize
        val srcWidth = if (intrinsicSize.hasSpecifiedAndFiniteWidth()) {
            intrinsicSize.width
        } else {
            size.width
        }

        val srcHeight = if (intrinsicSize.hasSpecifiedAndFiniteHeight()) {
            intrinsicSize.height
        } else {
            size.height
        }

        val srcSize = Size(srcWidth, srcHeight)

        // Compute the offset to translate the content based on the given alignment
        // and size to draw based on the ContentScale parameter
        val scaledSize = if (size.width != 0f && size.height != 0f) {
            srcSize * contentScale.computeScaleFactor(srcSize, size)
        } else {
            Size.Zero
        }

        val alignedPosition = alignment.align(
            IntSize(scaledSize.width.roundToInt(), scaledSize.height.roundToInt()),
            IntSize(size.width.roundToInt(), size.height.roundToInt()),
            layoutDirection
        )

        val dx = alignedPosition.x.toFloat()
        val dy = alignedPosition.y.toFloat()

        // Only translate the current drawing position while delegating the Painter to draw
        // with scaled size.
        // Individual Painter implementations should be responsible for scaling their drawing
        // content accordingly to fit within the drawing area.
        translate(dx, dy) {
            with(painter) {
                draw(size = scaledSize, alpha = alpha, colorFilter = colorFilter)
            }
        }

        // Maintain the same pattern as Modifier.drawBehind to allow chaining of DrawModifiers
        drawContent()
    }

    private fun Size.hasSpecifiedAndFiniteWidth() = this != Size.Unspecified && width.isFinite()
    private fun Size.hasSpecifiedAndFiniteHeight() = this != Size.Unspecified && height.isFinite()

    override fun toString(): String =
        "GlidePainterNode(" +
                "painter=$painter, " +
                "alignment=$alignment, " +
                "alpha=$alpha, " +
                "colorFilter=$colorFilter)"
}

package pm.gnosis.blockies

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.ImageView
import pm.gnosis.model.Solidity

open class BlockiesImageView(context: Context, attributeSet: AttributeSet?) : ImageView(context, attributeSet) {

    private var blockies: Blockies? = null
    private var painter: BlockiesPainter = BlockiesPainter()

    fun setAddress(address: Solidity.Address?) {
        blockies = address?.let { Blockies.fromAddress(it) }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        blockies?.let { drawBlockies(canvas, it) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        painter.setDimensions(measuredWidth.toFloat(), measuredHeight.toFloat())
    }

    private fun drawBlockies(canvas: Canvas, blockies: Blockies) {
        painter.draw(canvas, blockies)
    }
}

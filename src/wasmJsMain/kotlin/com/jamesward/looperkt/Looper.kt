package com.jamesward.looperkt

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.ImageData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


val RGBA?.isVisible: Boolean
    get() = this?.a?.let { it > 0u } ?: false

fun RGBA?.hide(): RGBA? = this?.copy(a = 0u)

data class RGBA(val r: UByte, val g: UByte, val b: UByte, val a: UByte) {
    constructor(bytes: UInt) : this(
        bytes.shr(24).toUByte(),
        bytes.shr(16).toUByte(),
        bytes.shr(8).toUByte(),
        bytes.toUByte(),
    )
}


data class Pixels(val width: Int, val height: Int, private val bytes: Uint16Array) {

    private fun firstByte(x: Int, y: Int): Int =
        (y * (width * 4) + x * 4)

    operator fun get(x: Int, y: Int): RGBA? =
        if ((x in 0..<width) && (y in 0..<height)) {
            firstByte(x, y).let { first ->
                RGBA(
                    bytes[first].toUByte(),
                    bytes[first + 1].toUByte(),
                    bytes[first + 2].toUByte(),
                    bytes[first + 3].toUByte(),
                )
            }
        }
        else {
            null
        }


    // silent fail?
    operator fun set(x: Int, y: Int, rgba: RGBA?): Unit =
        rgba?.let { set(x, y, it) } ?: Unit

    operator fun set(x: Int, y: Int, rgba: RGBA): Unit =
        if ((x in 0..<width) && (y in 0..<height)) {
            firstByte(x, y).let { first ->
                bytes[first] = rgba.r.toShort()
                bytes[first + 1] = rgba.g.toShort()
                bytes[first + 2] = rgba.b.toShort()
                bytes[first + 3] = rgba.a.toShort()
            }
        }
        else {
            Unit
        }

}


data class Pointer(val x: Int, val y: Int, val down: Boolean)

// from: https://stackoverflow.com/a/54828055/77409
fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}


@OptIn(DelicateCoroutinesApi::class)
data class Looper(val loop: Looper.() -> Unit) {
    private var _ctx: CanvasRenderingContext2D? = null
    private var _imageData: ImageData? = null
    private var _pixels: Pixels? = null

    private val ctx: CanvasRenderingContext2D
        get() = _ctx!!

    private val imageData: ImageData
        get() = _imageData!!

    val pixels: Pixels
        get() = _pixels!!

    private var _pointer: Pointer = Pointer(0, 0, false)

    val pointer: Pointer
        get() = _pointer

    // todo: don't like all this wonky mutability
    private fun updatePixels(canvas: HTMLCanvasElement) {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
        _ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        _imageData = ctx.getImageData(0.0, 0.0, window.innerWidth.toDouble(), window.innerHeight.toDouble())
        _pixels = Pixels(imageData.width, imageData.height, imageData.data.unsafeCast())
    }

    init {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        document.body?.append(canvas)
        updatePixels(canvas)

        canvas.onmousemove = { mouseEvent ->
            _pointer = _pointer.copy(x = mouseEvent.clientX, y = mouseEvent.clientY)
        }
        canvas.onmousedown = { mouseEvent ->
            _pointer = Pointer(mouseEvent.clientX, mouseEvent.clientY, true)
        }
        canvas.onmouseup = { mouseEvent ->
            _pointer = Pointer(mouseEvent.clientX, mouseEvent.clientY, false)
        }

        window.onresize = {
            updatePixels(canvas)
        }

        tickerFlow(10.milliseconds).onEach {
            loop()
            ctx.putImageData(imageData, 0.0, 0.0)
        }.launchIn(GlobalScope)
    }
}

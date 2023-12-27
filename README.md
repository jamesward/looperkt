LooperKt
-----------------

![Maven Central](https://img.shields.io/maven-central/v/com.jamesward/looperkt)

Write some pixels to a browser Canvas via Kotlin/Wasm.

Example: Draw a blue pixel wherever the mouse pointer is:
```kotlin
import com.jamesward.looperkt.*

fun main() {
    Looper {
        pixels[pointer.x, pointer.y] = RGBA(0u, 0u, 255u, 255u)
    }
}
```

API:
```kotlin
Looper {
    // a function that loops and updates the canvas

    // get a pixel
    // x & y are Ints
    val thePixel: RGBA? = pixels[x, y]

    // set a pixel
    // red, green, blue, alpha are Unsigned Bytes (0-255) specified like `255u`
    pixels[x, y] = RGBA(red, green, blue, alpha)

    // you can also use hex notation:
    val white = RGBA(0xFFFFFFFFu)

    // get the pointer position and button down state
    val x = pointer.x
    val y = pointer.y
    val down = pointer.down
}
```

Get started with a sample: [github.com/jamesward/hello-kotlin-looperkt](https://github.com/jamesward/hello-kotlin-looperkt)

```shell
git clone https://github.com/jamesward/hello-kotlin-looperkt.git
cd hello-kotlin-looperkt
./gradlew wasmJsBrowserRun -t
```
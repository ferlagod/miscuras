package com.ferlagod.miscuras.ui.screens
import io.github.sceneview.ar.ArSceneView
fun test(view: ArSceneView) {
    val hitResult = view.currentFrame?.hitTest(100f, 100f)
    val anchor = hitResult?.createAnchor()
}

package com.steelrazor47.scantrino

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextAnalyzer(private val onTextFound: (Text) -> Unit) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        @ExperimentalGetImage
        val image = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(inputImage)
            .addOnSuccessListener {
                onTextFound(it)
                imageProxy.close()
            }
            .addOnFailureListener { error ->
                Log.d(TAG, "Failed to recognize image text")
                error.printStackTrace()
                imageProxy.close()
            }
    }

    companion object {
        private val TAG = TextAnalyzer::class.java.name
    }
}

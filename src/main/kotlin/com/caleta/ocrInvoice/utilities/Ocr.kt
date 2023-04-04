package com.caleta.ocrInvoice.utilities

import com.caleta.ocrInvoice.config.BeanConfig
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.util.ImageHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.awt.image.BufferedImage

class Ocr {
    private val LOG: Logger = LoggerFactory.getLogger(Ocr::class.java)

    @Value("\${x-axis}")
    private val xAxis: Int? = null

    @Value("\${y-axis}")
    private val yAxis: Int? = null

    @Value("\${width}")
    private val width: Int? = null

    @Value("\${height}")
    private val height: Int? = null

    @Autowired
    private lateinit var tesseract: Tesseract

    fun performOcr(doc: PDDocument): String {
        LOG.error("Performing OCR")
        val render = PDFRenderer(doc)
        val image: BufferedImage = render.renderImageWithDPI(0, 300f)
        val croppedImage: BufferedImage = ImageHelper.getSubImage(image, xAxis!!, yAxis!!, width!!, height!!)
        return try {
            tesseract.doOCR(croppedImage).trim()
        } catch (e: Exception) {
            LOG.error("Error detected while performing OCR.", e)
            ""
        }
    }
}
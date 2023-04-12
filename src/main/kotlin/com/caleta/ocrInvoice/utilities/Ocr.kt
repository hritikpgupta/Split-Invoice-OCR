package com.caleta.ocrInvoice.utilities

import com.caleta.ocrInvoice.model.Response
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

    @Value("\${r-x-axis}")
    private val rxAxis: Int? = null

    @Value("\${r-y-axis}")
    private val ryAxis: Int? = null

    @Value("\${r-width}")
    private val rwidth: Int? = null

    @Value("\${r-height}")
    private val rheight: Int? = null

    @Value("\${d-x-axis}")
    private val dxAxis: Int? = null

    @Value("\${d-y-axis}")
    private val dyAxis: Int? = null

    @Value("\${d-width}")
    private val dwidth: Int? = null

    @Value("\${d-height}")
    private val dheight: Int? = null

    @Autowired
    private lateinit var tesseract: Tesseract

    fun performOcr(doc: PDDocument): Response? {
        val render = PDFRenderer(doc)
        val image: BufferedImage = render.renderImageWithDPI(0, 300f)
        val invoiceCrop: BufferedImage = ImageHelper.getSubImage(image, xAxis!!, yAxis!!, width!!, height!!)
        val referenceCrop: BufferedImage = ImageHelper.getSubImage(image, rxAxis!!, ryAxis!!, rwidth!!, rheight!!)
        val dateCrop: BufferedImage = ImageHelper.getSubImage(image,dxAxis!!, dyAxis!!, dwidth!!, dheight!!)
        return try {
            Response(
                tesseract.doOCR(invoiceCrop).trim(),
                tesseract.doOCR(referenceCrop).trim(),
                tesseract.doOCR(dateCrop).trim()
            )
        } catch (e: Exception) {
            LOG.error("Error detected while performing OCR.", e)
            null
        }
    }
}
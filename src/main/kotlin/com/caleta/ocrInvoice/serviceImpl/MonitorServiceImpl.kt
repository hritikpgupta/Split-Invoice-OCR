package com.caleta.ocrInvoice.serviceImpl

import com.caleta.ocrInvoice.model.InvoiceSplitInfo
import com.caleta.ocrInvoice.service.MonitoringService
import com.caleta.ocrInvoice.utilities.Ocr
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.concurrent.Executors
import java.util.function.Consumer


@EnableScheduling
@Service
class MonitorServiceImpl : MonitoringService {
    private val LOG: Logger = LoggerFactory.getLogger(MonitorServiceImpl::class.java)

    @Autowired
    private lateinit var watchService: WatchService

    @Value("\${monitor-folder-path}")
    private lateinit var monitorFolderPath: String

    @Value("\${destination-path}")
    private lateinit var destinationPath: String

    @Value("\${temp-path}")
    private lateinit var tempPath: String

    @Autowired
    private lateinit var ocr: Ocr

    @EventListener(value = [ApplicationReadyEvent::class], condition = "!@environment.acceptsProfiles('test')")
    override fun startMonitoring() {
        var key: WatchKey
        LOG.info("Started Monitoring Folder")
        try {
            while (watchService.take().also { key = it } != null) {
                val executorService = Executors.newCachedThreadPool()
                key.pollEvents().forEach(Consumer { event: WatchEvent<*> ->
                    executorService.execute {
                        if (event.context().toString().contains(".pdf")) {
                            processFile(loadDocument(event.context().toString()))
                        }
                    }
                })
                key.reset()
            }
        } catch (e: Exception) {
            LOG.error("Error detected while monitoring folder.", e)
        }
    }

    private fun loadDocument(name: String): PDDocument {
        return try {
            PDDocument.load(File("$monitorFolderPath\\${name}"))
        } catch (e: IOException) {
            LOG.error("Couldn't load file. ${e.message}")
            loadDocument(name)
        }
    }

    private fun processFile(doc: PDDocument) {
        val splitter = Splitter()
        val pages = splitter.split(doc)
        val infoList = ArrayList<String>()
        val invoiceInfoMap = HashMap<String, InvoiceSplitInfo>()
        pages.forEach {
            val text = ocr.performOcr(it)
            if (text.isNotEmpty()) {
                infoList.add(text)
            } else {
                LOG.error("OCR returned empty string")
            }
        }
        infoList.withIndex().forEach { value ->
            if (invoiceInfoMap.containsKey(value.value)) {
                val info = invoiceInfoMap[value.value]
                invoiceInfoMap[value.value] = InvoiceSplitInfo(info!!.start, value.index + 1)
            } else {
                invoiceInfoMap[value.value] = InvoiceSplitInfo(value.index + 1, value.index + 1)
            }
        }
        invoiceInfoMap.forEach {
            splitPdf(it.value.start, it.value.end, doc, it.key)
        }
    }

    private fun splitPdf(startIndex: Int, endIndex: Int, document: PDDocument, fileName: String) {
        val splitter = Splitter()
        splitter.setStartPage(startIndex)
        splitter.setEndPage(endIndex)
        val pages = splitter.split(document)
        var i = 1
        if (startIndex != endIndex) {
            val pdfMerger = PDFMergerUtility()
            pdfMerger.destinationFileName = "$destinationPath\\$fileName.pdf"
            pages.forEach {
                it.save("$tempPath\\${fileName}-${i++}.pdf")
            }
            i = 1
            pages.forEach { _ ->
                pdfMerger.addSource("$tempPath\\${fileName}-${i++}.pdf")
            }
            i = 1
            pdfMerger.mergeDocuments(null)
            pages.forEach { _ ->
                val f = File("$tempPath\\${fileName}-${i++}.pdf")
                f.delete()
            }
        } else {
            pages.forEach {
                it.save("$destinationPath\\$fileName.pdf")
            }
        }
    }
}
package com.caleta.ocrInvoice.config

import com.caleta.ocrInvoice.utilities.Ocr
import net.sourceforge.tess4j.Tesseract
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File
import java.nio.file.*

@EnableScheduling
@Configuration
class BeanConfig {


    @Value("\${monitor-folder-path}")
    private lateinit var monitorFolderPath: String

    @Value("\${destination-path}")
    private lateinit var destinationPath: String

    @Value("\${temp-path}")
    private lateinit var tempPath: String

    @Value("\${ocr-data-path}")
    private lateinit var datePath: String

    @Value("\${ocr-data-path}")
    private lateinit var dataPath: String

    @Bean("folderWatcherBean")
    fun folderWatcherBean(): WatchService? {
        checkDirectory()
        var watchService: WatchService? = null
        try {
            watchService = FileSystems.getDefault().newWatchService()
            val path: Path? = monitorFolderPath.let { Paths.get(it) }
            watchService?.let { path?.register(it, StandardWatchEventKinds.ENTRY_CREATE) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return watchService
    }

    fun checkDirectory() {
        if (!File(monitorFolderPath).exists()) {
            File(monitorFolderPath).mkdirs()
        }
        if (!File(destinationPath).exists()) {
            File(destinationPath).mkdirs()
        }
        if (!File(tempPath).exists()) {
            File(tempPath).mkdirs()
        }
        if (!File(datePath).exists()) {
            File(datePath).mkdirs()
        }

    }

    @Bean("ocr")
    fun provideOcr(): Ocr {
        return Ocr()
    }

    @Bean("tesseract")
    fun provideTesseract(): Tesseract {
        val tess = Tesseract()
        tess.apply {
            setDatapath(dataPath)
            setLanguage("eng")
            setPageSegMode(1)
            setOcrEngineMode(1)

        }
        return tess
    }

}
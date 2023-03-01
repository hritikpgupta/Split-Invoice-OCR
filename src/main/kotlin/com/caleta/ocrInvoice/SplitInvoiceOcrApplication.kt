package com.caleta.ocrInvoice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SplitInvoiceOcrApplication

fun main(args: Array<String>) {
	runApplication<SplitInvoiceOcrApplication>(*args)
}

package com.caleta.ocrInvoice.utilities

//HW77484-POR-06157619-16/11/21

fun String.formatReference(): String {
    return this.substring(this.indexOf("-")+1, this.length)
}

fun String.formatDate(): String {
    return this.replace("/","")
}
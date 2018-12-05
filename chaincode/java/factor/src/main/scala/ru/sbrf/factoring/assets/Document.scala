package ru.sbrf.factoring.assets

case class Document(id: String, // only for XMLs with rich content
                    batchID: String, // "Номер реестра"
                    shipmentStatus: String, // "Статус поставки"
                    transactionDate: String, //Дата проводки
                    contractID: String, //
                    headerText: String,
                    documentDate: String, //Дата документа
                    orderId: String,
                    shipmentCode: String,
                    totalGross: Double,
                    totalNet: Double = 0,
                    buyer: String,
                    seller: String,
                    factor: String,
                    documentType: String = "Invoice",
                    commodities: Array[Commodity])

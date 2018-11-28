package ru.sbrf.factoring.assets

case class Document(id: Option[String] = None,
                    contractID: String,
                    orderId: String,
                    hash: String,
                    shippingDate: String,
                    totalGross: Double,
                    totalNet: Option[Double] = None,
                    buyer: Option[String] = None,
                    seller: Option[String] = None,
                    factor: Option[String] = None,
                    documentType: String = "Invoice",
                    commodities: List[Commodity])

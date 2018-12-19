package ru.sbrf.factoring.assets

import java.time.Instant

case class Order(id: String,
//                 buyer: Organization,
//                 seller: Organization,
//                 contractID: String,
                 documents: Array[Document],
                 received: Boolean = false,
                 confirmed: Boolean = false,
                 created: Instant)

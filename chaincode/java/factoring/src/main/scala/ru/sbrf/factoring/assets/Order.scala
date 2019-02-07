package ru.sbrf.factoring.assets

case class Order(id: String,
//                 buyer: Organization,
//                 seller: Organization,
//                 contractID: String,
                 documents: Array[Document],
                 received: Boolean = false,
                 confirmed: Boolean = false,
                 created: Long)

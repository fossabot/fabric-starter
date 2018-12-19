package ru.sbrf.factoring.document

import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import com.github.apolubelov.fabric.contract.annotations.ContractOperation
import org.slf4j.{Logger, LoggerFactory}
import ru.sbrf.factoring.Config
import ru.sbrf.factoring.assets.{Document, Order, Organization}

trait Services {


  @ContractOperation
  def createDocuments(context: ContractContext, documents: Array[Document]): ContractResponse = {

    val orders: Seq[Order] = documents map getUpdatedOrder(context)

    for (order <- orders) {
      context.store.put[Order](order.id, order)
    }
    Success(orders.toArray)
  }


  def getUpdatedOrder(context: ContractContext)(incomingDoc: Document): Order = {

    val txAuthor = context.clientIdentity.mspId
    val buyer = context.store.list[Organization].filter(_._2.role == "Buyer")
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    if (buyer.isEmpty) Error(s"Buyer organization was not found. Please create buyer organization for the current contract (channel) ")
    logger.info(s"Found Buyer ${buyer.head._2.id} with mspId = ${buyer.head._2.mspId}")
    logger.info(s"current transaction author: $txAuthor")
    /*No one except buyer can submit a receipt*/
    val isReceiver = txAuthor == buyer.head._2.mspId

    /*Buyer can post both receipts and invoices*/
    val documentType = if (isReceiver) incomingDoc.documentType else Config.INVOICE

    logger.info(s"Got doc for OrderId: ${incomingDoc.orderId} with type: $documentType")
    val doc = incomingDoc.copy(documentType = documentType, created = context.lowLevelApi.getTxTimestamp)
    val received = documentType == Config.RECEIPT
    val confirmed = documentType == Config.INVOICE


    context.store.get[Order](doc.orderId) map { order =>
      //In case of existing order
      //we need to update the previously uploaded document
      val documents = doc +: order.documents.filterNot(_.documentType == documentType)
      //also we have to update order status
      Order(order.id, documents, received || order.received, confirmed || order.confirmed, order.created)
    } getOrElse {
      //New Order
      Order(doc.orderId, Array(doc), received, confirmed, doc.created)
    }
  }

  //  @ContractOperation
  //  def createDocument(context: ContractContext, document: Document): ContractResponse = {
  //    context.store.put(document.id, document)
  //    Success()
  //  }

  //  @ContractOperation
  //  def getDocument(context: ContractContext, id: String): ContractResponse = {
  //    val document: Option[Document] = context.store.get[Document](id)
  //    document.map { document => Success(document) } getOrElse Error(s"No Document with id $id")
  //  }
  //
  //  @ContractOperation
  //  def listDocuments(context: ContractContext): ContractResponse = {
  //    val documents: Array[Document] = context.store.list[Document]
  //      .map(_._2) // take only values
  //      .toArray // use Array, as GSON knows nothing about scala collections
  //    Success(documents)
  //  }
}

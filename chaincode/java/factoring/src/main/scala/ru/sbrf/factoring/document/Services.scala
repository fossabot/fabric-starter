package ru.sbrf.factoring.document

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

import com.github.apolubelov.fabric.contract.annotation.ContractOperation
import com.github.apolubelov.fabric.contract.store.Key
import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import org.slf4j.{Logger, LoggerFactory}
import ru.sbrf.factoring.Config
import ru.sbrf.factoring.assets.{Document, Order, Organization}

trait Services {


  @ContractOperation
  def createDocuments(context: ContractContext, documents: Array[Document]): ContractResponse = {

    val orders: Seq[Order] = documents map getUpdatedOrder(context)

    for (order <- orders) {

      val doc = order.documents.head
      val dateSegmentKey: String = calcSegment(doc.documentDate)

      val key: Key = new Key(
        dateSegmentKey, order.id)
      val logger: Logger = LoggerFactory.getLogger(this.getClass)
      logger.trace(s"documentDate = ${doc.documentDate}, dateSegmentKey = $dateSegmentKey")
      context.store.put[Order](key, order)
    }
    Success(orders.toArray)
  }


  def calcSegment(documentDate: String) = {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm")
    LocalDateTime.parse(
      documentDate + " 00:00", dateFormatter)
      .atZone(ZoneId.of("UTC"))
      .toInstant
      .toEpochMilli
      .toString
  }

  def getUpdatedOrder(context: ContractContext)(incomingDoc: Document): Order = {

    val txAuthor = context.clientIdentity.mspId
    val buyer = context.store.list[Organization].filter(_.value.role == "Buyer")
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    if (buyer.isEmpty) Error(s"Buyer organization was not found. Please create buyer organization for the current contract (channel) ")
    logger.trace(s"Found Buyer ${buyer.head.value.id} with mspId = ${buyer.head.value.mspId}")
    logger.trace(s"current transaction author: $txAuthor")
    /*No one except buyer can submit a receipt*/
    val isBuyer = txAuthor == buyer.head.value.mspId
    /*Buyer can post both receipts and invoices*/
    val documentType = if (isBuyer) Config.RECEIPT else Config.INVOICE

    logger.info(s"Got doc for OrderId: ${incomingDoc.orderId} with type: $documentType")
    val doc = incomingDoc.copy(documentType = documentType, created = context.lowLevelApi.getTxTimestamp.toEpochMilli)
    //    val received = documentType == Config.RECEIPT
    //    val confirmed = documentType == Config.INVOICE


    val maybeOrder: Option[Order] = context
      .store
      .get[Order](Key(calcSegment(doc.documentDate), doc.orderId))

    maybeOrder map { order =>
      //In case of existing order
      //we need to update the previously uploaded document
      logger.trace(s"${doc.orderId} was found with status received: ${order.received} and confirmed: ${order.confirmed}")
      val documents = doc +: order.documents.filterNot(_.documentType == documentType)
      //also we have to update order status
      Order(order.id, documents, isBuyer || order.received, !isBuyer || order.confirmed, order.created)
    } getOrElse {
      logger.trace(s"${doc.orderId} is new!")
      //New Order
      Order(doc.orderId, Array(doc), isBuyer, !isBuyer, doc.created)
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

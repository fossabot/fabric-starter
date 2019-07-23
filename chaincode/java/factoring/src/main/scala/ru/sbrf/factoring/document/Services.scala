package ru.sbrf.factoring.document

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZoneOffset}

import com.github.apolubelov.fabric.contract.annotation.ContractOperation
import com.github.apolubelov.fabric.contract.store.Key
import com.github.apolubelov.fabric.contract._
import org.slf4j.{Logger, LoggerFactory}
import ru.sbrf.factoring.Config._
import ru.sbrf.factoring.assets.{LogCounter, Document, LogRow, Order, Organization}

trait Services {


  import com.github.apolubelov.fabric.contract.ContractResponseConversions._

  @ContractOperation
  def createDocuments(context: ContractContext, collectionId: String): ContractResponse = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)


    val codecs = ContractCodecs()
    logger.error("!getTransient: " + context.lowLevelApi.getTransient.toString)
    logger.error("!data: " + new String(context.lowLevelApi.getTransient.get("data")))
    Option(context.lowLevelApi.getTransient.get("data"))
      .flatMap { el => Option(codecs.ledgerCodec.decode(el, classOf[Array[Document]])) }
      .toRight("no documents found in transient ")
      .map { documents =>
        val distinctDocs = documents.toList.groupBy(_.orderId) //If there are two rows with the same orderId,
          // then merge them with the amount equal to sum
          .map(x => {
          x._2 match {
            case head :: Nil =>
              logger.trace(s"Found single doc $head")
              head
            case xs: List[Document] =>
              logger.trace(s"found list of length ${xs.length}")
              val s = xs.foldLeft(0.toDouble)((acc, d) => acc + d.totalGross)
              logger.trace(s"total amount = $s")
              xs.head.copy(totalGross = s)
          }
        })

        val ordersAndStatus = distinctDocs.map(getUpdatedOrder(context))

        for (order <- ordersAndStatus.map(_._1)) {

          //          val (order,status) = t
          val doc = order.documents.head
          val dateSegmentKey: String = calcSegment(doc.documentDate)

          val key: Key = new Key(
            dateSegmentKey, order.id)
          logger.trace(s"documentDate = ${doc.documentDate}, dateSegmentKey = $dateSegmentKey")
          context.privateStore(collectionId).put[Order](key, order)
        }

        // Making a row
        val txLocalTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3))
        val result: LogRow = ordersAndStatus // We have a list like ( ( Order1, updated) , (Order2, created), (Order3, updated) )
          .groupBy(t => t._2) // => ( created -> (Order2), updated -> (Order1, Order3)
          .mapValues(_.size) // =>  (created -> 1, updated -> 2)
          .map(t => LogRow(context.clientIdentity.mspId, txLocalTime, t._1, t._2))
          .head


        val curVal = LogCounter.getCurrent(context, collectionId)


        context.privateStore(collectionId).put(LOG_COUNTER_KEY, LogCounter(curVal.height + 1))
        context.privateStore(collectionId).put[LogRow](curVal.height.toString, result)

        Success(result)
      }


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

  def getUpdatedOrder(context: ContractContext)(incomingDoc: Document): (Order, Int) = {

    val txAuthor = context.clientIdentity.mspId
    val buyer = context.store.list[Organization].filter(_.value.role == "Buyer")
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    if (buyer.isEmpty) Error(s"Buyer organization was not found. Please create buyer organization for the current contract (channel) ")
    logger.trace(s"Found Buyer ${buyer.head.value.id} with mspId = ${buyer.head.value.mspId}")
    logger.trace(s"current transaction author: $txAuthor")
    /*No one except buyer can submit a receipt*/
    val isBuyer = txAuthor == buyer.head.value.mspId
    /*Buyer can post both receipts and invoices*/
    val documentType = if (isBuyer) RECEIPT else INVOICE

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
      (Order(order.id, documents, isBuyer || order.received, !isBuyer || order.confirmed, order.created), ORDER_STATUS_UPDATED)
    } getOrElse {
      logger.trace(s"${doc.orderId} is new!")
      //New Order
      (Order(doc.orderId, Array(doc), isBuyer, !isBuyer, doc.created), ORDER_STATUS_CREATED)
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

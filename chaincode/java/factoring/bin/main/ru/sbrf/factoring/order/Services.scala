package ru.sbrf.factoring.order

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.github.apolubelov.fabric.contract.annotation.ContractOperation
import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import org.slf4j.{Logger, LoggerFactory}
import ru.sbrf.factoring.Config._
import ru.sbrf.factoring.assets.{LogCounter, LogRow, Order}

trait Services {

  case class
  OrdersQueryParams(unmatched: Boolean = false,
                    from: Long,
                    to: Long,
                    page: Int)

  @ContractOperation
  def createOrder(context: ContractContext, order: Order): ContractResponse = {
    context.store.put(order.id, order)
    Success()
  }

  @ContractOperation
  def getOrder(context: ContractContext, id: String): ContractResponse = {
    //    val a = context.store.ge
    val order: Option[Order] = context.store.get[Order](id)
    order.map { order => Success(order) } getOrElse Error(s"No Order with id $id")
  }

  @ContractOperation
  def listOrders(context: ContractContext, collectionId: String): ContractResponse = {
    val orders: Array[Order] = context.privateStore(collectionId).list[Order]
      .map(_.value) // take only values
      .toArray // use Array, as GSON knows nothing about scala collections
    Success(orders)
  }


  @ContractOperation
  def getLog(context: ContractContext, collectionId: String, depth: Int): ContractResponse = {


    Success {
      val curCounter = LogCounter.getCurrent(context, collectionId).height
      (Math.max(curCounter - depth, 0) to curCounter).
        flatMap(key => context.privateStore(collectionId).list[LogRow](key.toString))
        .map(_.value)
        .toArray
    }
  }

  //  @ContractOperation
  //  def listOrdersWithParams(context: ContractContext, matched: String, from: String, to: String, page: String): ContractResponse = {
  //
  //
  //    val pageSize = 20
  //
  //    val orders = context.store.list[Order]
  //      .map(_._2) // take only values
  //      .filter(o =>
  //      matched.asInstanceOf[Boolean] == (o.confirmed && o.received)
  //        && o.created.isAfter(Instant.ofEpochMilli(from.asInstanceOf[Long]))
  //        && o.created.isBefore(Instant.ofEpochMilli(to.asInstanceOf[Long])))
  //      .slice(pageSize * (page.asInstanceOf[Int] - 1),
  //        pageSize * (page.asInstanceOf[Int] - 1) + pageSize)
  //      .toArray // use Array, as GSON knows nothing about scala collections
  //    Success(orders)
  //  }

  @ContractOperation
  def listOrdersWithParams(context: ContractContext, collectionId: String, queryParams: OrdersQueryParams): ContractResponse = {


    //    def checkBoundaries(o: Order): Boolean = {
    //      val logger: Logger = LoggerFactory.getLogger(this.getClass)
    //
    //      val matchFlagCondition = !queryParams.unmatched || (queryParams.unmatched && (o.confirmed != o.received))
    //
    //      val fromCondition = o.created > queryParams.from //.isAfter(Instant.ofEpochMilli(queryParams.from))
    //
    //      val toCondition = o.created < queryParams.to //.isBefore(Instant.ofEpochMilli(queryParams.to))
    //
    //      logger.trace(s"created = ${o.created} ,from =  ${queryParams.from}, to = ${queryParams.to}, upperBound = ${Instant.ofEpochMilli(queryParams.to)}")
    //      logger.trace(s"order: ${o.id}, ${o.received},${o.confirmed} \n unMatch = $matchFlagCondition,\n  fromCondition = $fromCondition, \n toCondition = $toCondition")
    //      matchFlagCondition && fromCondition && toCondition
    //    }

    //
    //    val ordersFiltered = context.store.list[Order]
    //      .map(_.value) // take only values
    //      .filter(checkBoundaries)
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    val keys: List[String] = getDateKeys(queryParams)
    logger.trace(s"keys:$keys")
    keys foreach logger.trace
    val ordersFiltered = keys
      .flatMap(key => {
        context.privateStore(collectionId).list[Order](key)
      })
      .map(_.value)
      .filter(order => !queryParams.unmatched || (order.confirmed && order.received))
    logger.trace(s"Orders filtered size: ${ordersFiltered.size}")


    val ordersSliced = ordersFiltered
      .slice(PAGE_SIZE * (queryParams.page - 1),
        PAGE_SIZE * (queryParams.page - 1) + PAGE_SIZE)
      .toArray
    // use Array, as GSON knows nothing about scala collections


    Success(ordersSliced)
  }

  //  @ContractOperation
  //  def getOrdersByContract(context: ContractContext, contractId: String): ContractResponse = {
  //    val orders: Array[Order] = context.store.get[Array[Order]]("") getOrElse Array[Order]()
  //    //      .map(_._2) // take only values
  //    //      .toArray // use Array, as GSON knows nothing about scala collections
  //    Success(orders)
  //  }

  //  @ContractOperation
  //  def updateSingleOrder(context: ContractContext, document: Document): ContractResponse = {
  //
  //    updatedOrder(context)(document) map { order =>
  //        val primaryKey = context.lowLevelApi.createCompositeKey("ContractOrder", order.contractID, order.id)
  //      context.store.put[Order](primaryKey.toString, order)
  //      Success(order.id)
  //    } getOrElse Error(s"contract ${document.contractID} not found")
  //
  //  }


  def getDateKeys(queryParams: OrdersQueryParams) = {
    val dayFrom = Instant.ofEpochMilli(queryParams.from).truncatedTo(ChronoUnit.DAYS)
    val dayTo = Instant.ofEpochMilli(queryParams.to).truncatedTo(ChronoUnit.DAYS)
    val keys = (dayFrom.toEpochMilli to dayTo.toEpochMilli by 86400000).map(_.toString).toList
    keys
  }
}

package ru.sbrf.factoring.order

import com.github.apolubelov.fabric.contract.annotations.ContractOperation
import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import ru.sbrf.factoring.assets.Order

trait Services {
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
  def listOrders(context: ContractContext): ContractResponse = {
    val orders: Array[Order] = context.store.list[Order]
      .map(_._2) // take only values
      .toArray // use Array, as GSON knows nothing about scala collections
    Success(orders)
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
  //      val primaryKey = context.lowLevelApi.createCompositeKey("ContractOrder", order.contractID, order.id)
  //      context.store.put[Order](primaryKey.toString, order)
  //      Success(order.id)
  //    } getOrElse Error(s"contract ${document.contractID} not found")
  //
  //  }


}

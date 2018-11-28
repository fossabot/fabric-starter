package ru.sbrf.factoring.order

import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import com.github.apolubelov.fabric.contract.annotations.ContractOperation
import ru.sbrf.factoring.assets.Order

trait Services {
  @ContractOperation
  def createOrder(context: ContractContext, organization: Order): ContractResponse = {
    context.store.put(organization.id, organization)
    Success()
  }

  @ContractOperation
  def getOrder(context: ContractContext, id: String): ContractResponse = {
    val organization: Option[Order] = context.store.get[Order](id)
    organization.map { organization => Success(organization) } getOrElse Error(s"No Order with id $id")
  }

  @ContractOperation
  def listOrders(context: ContractContext): ContractResponse = {
    val organizations: Array[Order] = context.store.list[Order]
      .map(_._2) // take only values
      .toArray // use Array, as GSON knows nothing about scala collections
    Success(organizations)
  }
}

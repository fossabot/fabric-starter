package ru.sbrf.factoring.organization

import com.github.apolubelov.fabric.contract.annotation.ContractOperation
import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import org.slf4j.{Logger, LoggerFactory}
import ru.sbrf.factoring.assets.Organization

trait Services {
  @ContractOperation
  def createOrganization(context: ContractContext, organization: Organization): ContractResponse = {
    context.store.put(organization.id, organization)
    Success()
  }

  @ContractOperation
  def getOrganization(context: ContractContext, id: String): ContractResponse = {
    val organization: Option[Organization] = context.store.get[Organization](id)
    organization.map { organization => Success(organization) } getOrElse Error(s"No Organization with id $id")
  }

  @ContractOperation
  def listOrganizations(context: ContractContext): ContractResponse = {
    val myId = context.clientIdentity.mspId
    val organizations: Array[Organization] = context.store.list[Organization]
      .map(_.value) // take only values
      .toArray // use Array, as GSON knows nothing about scala collections

    val (me, notMe) = organizations.span(_.mspId == myId)

    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    logger.trace(s"found organizations:")
    organizations foreach println
    logger.trace(s"me = ${me.mkString(";")}")
    logger.trace(s"notMe = ${notMe.mkString(";")}")
    Success(me.map(_.copy(isMe = true)) ++ notMe)
  }
}

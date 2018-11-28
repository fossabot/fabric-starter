package ru.sbrf.factoring.organization

import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import com.github.apolubelov.fabric.contract.annotations.ContractOperation
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
    val organizations: Array[Organization] = context.store.list[Organization]
      .map(_._2) // take only values
      .toArray // use Array, as GSON knows nothing about scala collections
    Success(organizations)
  }
}

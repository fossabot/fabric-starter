package ru.sbrf.factoring.contract

import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import com.github.apolubelov.fabric.contract.annotations.ContractOperation
import ru.sbrf.factoring.assets.Contract

trait Services {
  @ContractOperation
  def createContract(context: ContractContext, contract: Contract): ContractResponse = {
    context.store.put(contract.id, contract)
    Success()
  }

  @ContractOperation
  def getContract(context: ContractContext, id: String): ContractResponse = {
    val organization: Option[Contract] = context.store.get[Contract](id)
    organization.map { contract => Success(contract) } getOrElse Error(s"No contract with id $id")
  }

  @ContractOperation
  def listContracts(context: ContractContext): ContractResponse = {
    val contracts: Array[Contract] = context.store.list[Contract]
      .map(_._2) // take only values
      .toArray // use Array, as GSON knows nothing about scala collections
    Success(contracts)
  }
}

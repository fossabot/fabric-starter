package ru.sbrf.factoring.document

import com.github.apolubelov.fabric.contract.{ContractContext, ContractResponse, Error, Success}
import com.github.apolubelov.fabric.contract.annotations.ContractOperation
import ru.sbrf.factoring.assets.Document

trait Services {
  @ContractOperation
  def createDocument(context: ContractContext, document: Document): ContractResponse = {
    context.store.put(document.id.getOrElse(""), document)
    Success()
  }

  @ContractOperation
  def getDocument(context: ContractContext, id: String): ContractResponse = {
    val document: Option[Document] = context.store.get[Document](id)
    document.map { document => Success(document) } getOrElse Error(s"No Document with id $id")
  }

  @ContractOperation
  def listDocuments(context: ContractContext): ContractResponse = {
    val documents: Array[Document] = context.store.list[Document]
      .map(_._2) // take only values
      .toArray // use Array, as GSON knows nothing about scala collections
    Success(documents)
  }
}

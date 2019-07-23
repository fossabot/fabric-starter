package ru.sbrf.factoring

//case class Config()

object Config {
  val RECEIPT = "receipt"
  val INVOICE = "invoice"
  val DEFAULT_ORDERS_PERIOD:Long = 86400*7
  val PAGE_SIZE = 20
  val ORDER_STATUS_UPDATED = 0
  val ORDER_STATUS_CREATED = 1
  val LOG_COUNTER_KEY = "LogCounter"
}

package ru.sbrf.factoring.assets

import com.github.apolubelov.fabric.contract.ContractContext
import ru.sbrf.factoring.Config

case class LogCounter(height: Int) {


}

object LogCounter {
  def getCurrent(context: ContractContext, collectionId: String): LogCounter = {

    context
      .privateStore(collectionId)
      .get[LogCounter](Config.LOG_COUNTER_KEY) getOrElse LogCounter(0)


  }
}

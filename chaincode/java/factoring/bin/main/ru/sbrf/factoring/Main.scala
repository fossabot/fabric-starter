package ru.sbrf.factoring

import com.github.apolubelov.fabric.contract._
import com.github.apolubelov.fabric.contract.annotation.ContractInit
import com.github.apolubelov.fabric.contract.store.Key
import org.hyperledger.fabric.shim.ledger.CompositeKey
import org.slf4j.{Logger, LoggerFactory}
import ru.sbrf.factoring.assets.{Order, Organization}

/*
 * This is just a version of
 * https://github.com/hyperledger/fabric-samples/blob/release-1.3/chaincode/chaincode_example02/java/src/main/java/org/hyperledger/fabric/example/SimpleChaincode.java
 * Hyperledger Fabric example, ported to Scala with use of fabric-contract-base library.
 *
 * @author Alexey Polubelov
 */
object Main extends ContractBase with App
  with organization.Services
  with contract.Services
  with order.Services
  with document.Services {

  // start SHIM chain code
  start(args)

  // setup logging levels
  LoggerFactory
    .getLogger(Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.ERROR)

  LoggerFactory
    .getLogger(classOf[ContractBase].getPackage.getName)
    .asInstanceOf[ch.qos.logback.classic.Logger]
    .setLevel(ch.qos.logback.classic.Level.ERROR)


  @ContractInit
  def init(context: ContractContext, factor: Organization, buyer: Organization): Unit = {

    context.store.list[Organization].foreach(t => context.store.del[Organization](t.key))
    context.store.list[Order].foreach(t => {
      //      context.store.del[Order](Key(t.key.split(CompositeKey.NAMESPACE):_*))
      val Array(first, second) = t.key.split(CompositeKey.NAMESPACE)
      val k = Key(first, second)
      context.store.del(k, classOf[Order])
    }
    )
    context.store.put(factor.mspId, factor)
    context.store.put(buyer.mspId, buyer)
  }

  //    @ContractOperation
  //    def invoke(context: ContractContext, from: String, to: String, x: Int): ContractResponse =
  //        context.store.get[Int](from).map { source =>
  //            context.store.get[Int](to).map { dest =>
  //                context.store.put(from, source - x)
  //                context.store.put(to, dest + x)
  //                Success()
  //            } getOrElse Error(s"No value of $to in the Ledger")
  //        } getOrElse Error(s"No value of $from in the Ledger")

  //
  //  @ContractOperation
  //  def delete(context: ContractContext, key: String): Unit = {
  //    context.store.del[Int](key)
  //  }
  //
  //  @ContractOperation
  //  def query(context: ContractContext, key: String): ContractResponse =
  //    context.store.get[Int](key).map { value =>
  //      Success(value)
  //    } getOrElse Error(s"""{"Error":"Nil amount for "$key"}""")

  //
  // Bonus content
  //
  //
  //    case class DummyAsset(
  //        name: String,
  //        aType: Int,
  //        value: Double
  //    )

  //    override def resolveClassByName(name: String): Option[Class[_]] = Some(classOf[DummyAsset])


}

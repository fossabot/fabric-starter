import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestSuite extends FunSuite
  with ru.sbrf.factoring.document.Services with
  ru.sbrf.factoring.order.Services {

  test("Filter generator must calc segments") {

    assert(calcSegment("28.06.2018") == "1530144000000")

  }


  test("calc date segment") {



    val queryParams = OrdersQueryParams(from = 1530057600000L, to = 1530230400000L, page = 1)

    assert(getDateKeys(queryParams) == List("1530057600000","1530144000000","1530230400000"))

  }


}

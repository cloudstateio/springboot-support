package io.cloudstate.springboot.example

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class LoadTest extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:9000")

  object GetCartResource {
    val get: ChainBuilder = exec(http("GetUserCart")
      .get("/carts/adriano/items"))
  }

  val shoppingCartScenario: ScenarioBuilder = scenario("RampUpUsers")
    .exec(GetCartResource.get)

  setUp(shoppingCartScenario.inject(
    incrementUsersPerSec(20)
      .times(5)
      .eachLevelLasting(5 seconds)
      .separatedByRampsLasting(5 seconds)
      .startingFrom(20)
  )).protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(100))
}

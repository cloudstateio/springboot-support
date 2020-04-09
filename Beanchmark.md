
Gatling test code:

```scala
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

```

Cloudstate Springboot startup:

![cloudstate boot](/docs/img/cloudstate-boot-perf-boot-wow.png)

Cloudstate CLI starting Proxy:

![cloudstate proxy run](/docs/img/cloudstate-cli-boot-perf.png)

Tests results:

![cloudstate gatling](/docs/img/cloudstate-gatling.png)

![gatling 1](/docs/img/gatling-perf-1.png)

![gatling 2](/docs/img/gatling-perf-2.png)

![gatling 3](/docs/img/gatling-perf-3.png)

![gatling 4](/docs/img/gatling-perf-4.png)

Cloudstate Springboot User function:

![cloudstate perf](/docs/img/cloudstate-perf-user-paused.png)

![cloudstate cpug](/docs/img/cloudstate-user-perf-ok.png)

![cloudstate memory](/docs/img/cloudstate-user-perf-memory.png)

Cloudsatate Proxy:

![proxy cpu](/docs/img/proxy-cpu.png)

![proxy memory](/docs/img/proxy-memory.png)

Click on the image below to see the video of the docker statistics:

[![asciirecord](/docs/img/docker-stats.png)](https://asciinema.org/a/H8IzG9tdEsgPqkZiXfrQ6UN3D) "Docker Stats"



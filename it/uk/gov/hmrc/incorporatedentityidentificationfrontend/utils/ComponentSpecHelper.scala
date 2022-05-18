/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.incorporatedentityidentificationfrontend.utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Writes}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.JourneyConfigRepository

import scala.concurrent.ExecutionContext.Implicits.global

trait ComponentSpecHelper extends AnyWordSpec with Matchers
  with CustomMatchers
  with WiremockHelper
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with GuiceOneServerPerSuite {

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl: String = s"http://$mockHost:$mockPort"

  val config: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "play.http.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.base.host" -> mockHost,
    "microservice.services.base.port" -> mockPort,
    "microservice.services.self.host" -> mockHost,
    "microservice.services.self.port" -> mockPort,
    "microservice.services.incorporated-entity-identification.host" -> mockHost,
    "microservice.services.incorporated-entity-identification.port" -> mockPort,
    "microservice.services.incorporation-information.host" -> mockHost,
    "microservice.services.incorporation-information.port" -> mockPort,
    "microservice.services.business-verification.url" -> s"$mockUrl/business-verification"
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build

  lazy val journeyConfigRepository: JourneyConfigRepository = app.injector.instanceOf[JourneyConfigRepository]

  implicit val ws: WSClient = app.injector.instanceOf[WSClient]

  override def beforeAll(): Unit = {
    startWiremock()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    await(journeyConfigRepository.drop)
    resetWiremock()
    super.beforeEach()
  }

  def get[T](uri: String): WSResponse = {
    await(buildClient(uri).withHttpHeaders().get)
  }

  def post(uri: String)(form: (String, String)*): WSResponse = {
    val formBody = (form map { case (k, v) => (k, Seq(v)) }).toMap
    await(
      buildClient(uri)
        .withHttpHeaders("Csrf-Token" -> "nocheck")
        .post(formBody)
    )
  }


  def post(uri: String, json: JsValue): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json")
        .post(json.toString())
    )
  }

  def put[T](uri: String)(body: T)(implicit writes: Writes[T]): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json")
        .put(writes.writes(body).toString())
    )
  }

  val baseUrl: String = "/identify-your-incorporated-business"

  private def buildClient(path: String): WSRequest = ws.url(s"http://localhost:$port$path").withFollowRedirects(false)

}

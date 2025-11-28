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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Writes}
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSCookie, WSRequest, WSResponse}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.mvc.{Cookie, Session, SessionCookieBaker}
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.SessionKeys
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.{testCallingServiceName, testCallingServiceNameFromLabels, testDefaultServiceName}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.JourneyConfigRepository
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

trait ComponentSpecHelper extends AnyWordSpec with Matchers
  with CustomMatchers
  with WiremockHelper
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with GuiceOneServerPerSuite
  with Injecting {

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
    .build()

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

  val cyLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "cy")

  val enLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "en")

  def get[T](uri: String, cookie: WSCookie = enLangCookie): WSResponse = {
    await(buildClient(uri).withHttpHeaders("Authorization" -> "Bearer123").withCookies(cookie, mockSessionCookie).get())
  }

  def extractDocumentFrom(aWSResponse: WSResponse): Document = Jsoup.parse(aWSResponse.body)

  def post(uri: String, cookie: WSCookie = enLangCookie)(form: (String, String)*): WSResponse = {
    val formBody = (form map { case (k, v) => (k, Seq(v)) }).toMap
    await(
      buildClient(uri)
        .withHttpHeaders("Authorization" -> "Bearer123", "Csrf-Token" -> "nocheck")
        .withCookies(cookie, mockSessionCookie)
        .post(formBody)
    )
  }


  def post(uri: String, json: JsValue): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Authorization" -> "Bearer123", "Content-Type" -> "application/json")
        .withCookies(enLangCookie, mockSessionCookie)
        .post(json.toString())
    )
  }

  def put[T](uri: String, cookie: WSCookie = enLangCookie)(body: T)(implicit writes: Writes[T]): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Authorization" -> "Bearer123", "Content-Type" -> "application/json")
        .withCookies(cookie, mockSessionCookie)
        .put(writes.writes(body).toString())
    )
  }

  def correctlyDisplayTheServiceName(result: => WSResponse, expectedWelshServiceName: String): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the service name" in {
      doc.getServiceName.text mustBe expectedWelshServiceName
    }

  }

  def expectedTitle(doc: Document, titlePart: String, titlePartSpecificForEntity: Option[String] = None): String = {
    val title = titlePartSpecificForEntity.getOrElse(titlePart)
    doc.getServiceName.text() match {
      case serviceName if serviceName.equals(testDefaultServiceName) =>
        s"$title - $testDefaultServiceName - GOV.UK"
      case serviceName if serviceName.equals(testCallingServiceNameFromLabels) =>
        s"$title - $testCallingServiceNameFromLabels - GOV.UK"
      case _ =>
        s"$title - $testCallingServiceName - GOV.UK"
    }
  }

  val baseUrl: String = "/identify-your-incorporated-business"

  private def buildClient(path: String): WSRequest = ws.url(s"http://localhost:$port$path").withFollowRedirects(false)

  def mockSessionCookie: WSCookie = {

    def makeSessionCookie(session: Session): Cookie = {
      val cookieCrypto = inject[SessionCookieCrypto]
      val cookieBaker = inject[SessionCookieBaker]
      val sessionCookie = cookieBaker.encodeAsCookie(session)
      val encryptedValue = cookieCrypto.crypto.encrypt(PlainText(sessionCookie.value))
      sessionCookie.copy(value = encryptedValue.value)
    }

    val mockSession = Session(Map(
      SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
      SessionKeys.authToken -> "mock-bearer-token",
      SessionKeys.sessionId -> "mock-sessionid"
    ))

    val cookie = makeSessionCookie(mockSession)

    new WSCookie() {
      override def name: String = cookie.name

      override def value: String = cookie.value

      override def domain: Option[String] = cookie.domain

      override def path: Option[String] = Some(cookie.path)

      override def maxAge: Option[Long] = cookie.maxAge.map(_.toLong)

      override def secure: Boolean = cookie.secure

      override def httpOnly: Boolean = cookie.httpOnly
    }
  }

}

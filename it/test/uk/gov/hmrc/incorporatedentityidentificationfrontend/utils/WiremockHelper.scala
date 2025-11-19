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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.matching.MatchResult.{exactMatch, noMatch}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import play.api.libs.json.{JsDefined, JsObject, JsString, Json}

import scala.util.{Success, Try}

object WiremockHelper extends Eventually with IntegrationPatience {

  val wiremockPort: Int = 11111
  val wiremockHost: String = "localhost"

  def verifyPost(uri: String, optBody: Option[String] = None): Unit = {
    val uriMapping = postRequestedFor(urlEqualTo(uri))
    val postRequest = optBody match {
      case Some(body) => uriMapping.withRequestBody(equalTo(body))
      case None => uriMapping
    }
    verify(postRequest)
  }

  def verifyPut(uri: String, optBody: Option[String] = None): Unit = {
    val uriMapping = putRequestedFor(urlEqualTo(uri))
    val putRequest = optBody match {
      case Some(body) => uriMapping.withRequestBody(equalTo(body))
      case None => uriMapping
    }
    verify(putRequest)
  }

  def verifyGet(uri: String): Unit = {
    verify(getRequestedFor(urlEqualTo(uri)))
  }

  def stubGet(url: String, status: Integer, body: String): StubMapping =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(body)
      )
    )

  def stubPost(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(post(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubPut(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(put(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubPatch(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(patch(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubDelete(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(delete(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubAudit(): StubMapping = {
    stubPost("/write/audit", 200, "{}")
    stubPost("/write/audit/merged", 200, "{}")
  }

  def verifyAudit(): Unit = {
    verifyPost("/write/audit/merged")
  }

  def verifyAuditDetail(expectedAudit: JsObject): Unit = {
    val uriMapping = postRequestedFor(urlPathMatching("/write/audit(?:/merged)?"))

    val postRequest = uriMapping.andMatching { (request: Request) =>
      Try(Json.parse(request.getBodyAsString)) match {
        case Success(auditJson) =>
          auditJson \\ "auditType" match {
            case JsDefined(auditType) if auditType == JsString("IncorporatedEntityRegistration") =>
              auditJson \\ "detail" match {
                case JsDefined(auditDetail) =>
                  val actual = auditDetail.as[JsObject] - "callingService"
                  val expected = expectedAudit - "callingService"
                  val matchesSubset = expected.fields.forall { case (k, v) => actual.value.get(k).contains(v) }
                  if (matchesSubset) exactMatch() else noMatch()
                case _ => noMatch()
              }
            case _ => exactMatch()
          }
        case _ => noMatch()
      }
    }

    verify(postRequest)
  }
}

trait WiremockHelper {

  import WiremockHelper._

  lazy val wmConfig: WireMockConfiguration = wireMockConfig().port(wiremockPort)
  lazy val wireMockServer: WireMockServer = new WireMockServer(wmConfig)

  def startWiremock(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(wiremockHost, wiremockPort)
  }

  def stopWiremock(): Unit = wireMockServer.stop()

  def resetWiremock(): Unit = WireMock.reset()
}

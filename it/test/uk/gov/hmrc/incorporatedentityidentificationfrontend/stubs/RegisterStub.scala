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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait RegisterStub extends WireMockMethods {

  private def jsonBody(journeyId: String, journeyConfig: JourneyConfig): JsObject = {
    Json.obj(
      "journeyId" -> journeyId,
      "businessVerificationCheck" -> journeyConfig.businessVerificationCheck,
      "regime" -> journeyConfig.regime)
  }

  def stubLimitedCompanyRegister(journeyId: String, journeyConfig: JourneyConfig)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = "/incorporated-entity-identification/register-limited-company", jsonBody(journeyId, journeyConfig))
      .thenReturn(
        status = status,
        body = body
      )
  }

  def verifyLimitedCompanyRegister(journeyId: String, journeyConfig: JourneyConfig): Unit = {
    WiremockHelper.verifyPost(
      uri = "/incorporated-entity-identification/register-limited-company", optBody = Some(jsonBody(journeyId, journeyConfig).toString())
    )
  }

  def stubRegisteredSocietyRegister(journeyId: String, journeyConfig: JourneyConfig)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = "/incorporated-entity-identification/register-registered-society", jsonBody(journeyId, journeyConfig))
      .thenReturn(
        status = status,
        body = body
      )
  }

  def verifyRegisteredSocietyRegister(journeyId: String, journeyConfig: JourneyConfig): Unit = {
    WiremockHelper.verifyPost(
      uri = "/incorporated-entity-identification/register-registered-society", optBody = Some(jsonBody(journeyId, journeyConfig).toString())
    )
  }

  def verifyRegisterAudit(): Unit = {
    WiremockHelper.verifyPost(uri = "/write/audit", optBody = Some("Audit"))
    //    WiremockHelper.verifyPost(uri = "/write/audit/merged")
    //    WiremockHelper.verifyPost(uri = "/write/audit")
  }
}

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
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.test.Helpers.UNAUTHORIZED
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WireMockMethods

trait AuthStub extends WireMockMethods {

  val authUrl = "/auth/authorise"

  def stubAuth[T](status: Int, body: T)(implicit writes: Writes[T]): StubMapping = {
    when(method = POST, uri = authUrl)
      .thenReturn(status = status, body = writes.writes(body))
  }

  def stubAuthFailure(): StubMapping = {
    when(method = POST, uri = authUrl)
      .thenReturn(status = UNAUTHORIZED, headers = Map(HeaderNames.WWW_AUTHENTICATE -> s"""MDTP detail="MissingBearerToken""""))
  }

  def successfulAuthResponse(internalId: Option[String], enrolments: JsObject*): JsObject = Json.obj(
    "optionalCredentials" -> Json.obj(
      "providerId" -> testCredentialId,
      "providerType" -> GGProviderId
    ),
    "groupIdentifier" -> testGroupId,
    "internalId" -> internalId,
    "allEnrolments" -> enrolments
  )

  def emptyAuthResponse(): JsObject = Json.obj()

  val irctEnrolment: JsObject = Json.obj(
    "key" -> IRCTEnrolmentKey,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> IRCTReferenceKey,
        "value" -> testCtutr
      )
    )
  )
}

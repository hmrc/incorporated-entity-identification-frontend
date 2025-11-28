/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RegistrationHttpParser.RegistrationHttpReads
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, RegistrationStatus}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(httpClient: HttpClientV2,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) {

  private def buildRegisterJson(journeyId: String, journeyConfig: JourneyConfig): JsObject = {
    Json.obj(
      "journeyId" -> journeyId,
      "businessVerificationCheck" -> journeyConfig.businessVerificationCheck,
      "regime" -> journeyConfig.regime
    )
  }

  def registerLimitedCompany(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[RegistrationStatus] =
    httpClient.post(url"${appConfig.registerLimitedCompanyUrl}")(hc)
      .withBody(buildRegisterJson(journeyId, journeyConfig))
      .execute[RegistrationStatus](RegistrationHttpReads, ec)

  def registerRegisteredSociety(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[RegistrationStatus] =
    httpClient.post(url"${appConfig.registerRegisteredSocietyUrl}")(hc)
      .withBody(buildRegisterJson(journeyId, journeyConfig))
      .execute[RegistrationStatus](RegistrationHttpReads, ec)

}

object RegistrationHttpParser {
  val registrationKey = "registration"

  implicit object RegistrationHttpReads extends HttpReads[RegistrationStatus] {
    override def read(method: String, url: String, response: HttpResponse): RegistrationStatus = {
      response.status match {
        case OK =>
          (response.json \ registrationKey).as[RegistrationStatus]
        case _ =>
          throw new InternalServerException(s"Unexpected response from Register API - status = ${response.status}, body = ${response.body}")
      }
    }
  }

}

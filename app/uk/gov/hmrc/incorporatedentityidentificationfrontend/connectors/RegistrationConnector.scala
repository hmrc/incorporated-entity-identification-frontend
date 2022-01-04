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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.http.Status._
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RegistrationHttpParser.RegistrationHttpReads
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.RegistrationStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(httpClient: HttpClient,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) {

  private def buildRegisterJson(crn: String, ctutr: String): JsObject = {
    Json.obj(
      "crn" -> crn,
      "ctutr" -> ctutr
    )
  }

  def registerLimitedCompany(crn: String, ctutr: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = {
    httpClient.POST[JsObject, RegistrationStatus](appConfig.registerLimitedCompanyUrl, buildRegisterJson(crn, ctutr))(
      implicitly[Writes[JsObject]],
      RegistrationHttpReads,
      hc,
      ec
    )
  }

  def registerRegisteredSociety(crn: String, ctutr: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = {
    httpClient.POST[JsObject, RegistrationStatus](appConfig.registerRegisteredSocietyUrl, buildRegisterJson(crn, ctutr))(
      implicitly[Writes[JsObject]],
      RegistrationHttpReads,
      hc,
      ec
    )
  }

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

/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.BusinessVerificationConnector.BusinessVerificationHttpReads
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessVerificationConnector @Inject()(http: HttpClient, appConfig: AppConfig)
                                             (implicit ec: ExecutionContext) {

  def createBusinessVerificationJourney(ctutr: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val jsonBody: JsObject =
      Json.obj(
        "journeyType" -> "BUSINESS_VERIFICATION",
        "origin" -> "vat",
        "identifiers" -> Json.obj("ctUtr" -> ctutr),
        "continueUrl" -> routes.CaptureBusinessVerificationResultController.show().url
      )
    http.POST[JsObject, Option[String]](appConfig.getBusinessVerificationUrl, jsonBody)(
      implicitly[Writes[JsObject]],
      BusinessVerificationHttpReads,
      hc,
      ec
    )
  }
}

object BusinessVerificationConnector {

  implicit object BusinessVerificationHttpReads extends HttpReads[Option[String]] {
    override def read(method: String, url: String, response: HttpResponse): Option[String] = {
      response.status match {
        case CREATED =>
          (response.json \ "redirectUri").asOpt[String] match {
            case Some(redirectUri) =>
              Some(redirectUri)
            case _ =>
              throw new InternalServerException(s"Business Verification API returned malformed JSON")
          }
        case NOT_FOUND =>
          None
        case status =>
          throw new InternalServerException(s"Business Verification API failed with status: $status")
      }
    }
  }

}

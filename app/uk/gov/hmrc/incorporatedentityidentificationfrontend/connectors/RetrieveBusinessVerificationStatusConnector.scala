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

import javax.inject.Inject
import play.api.http.Status.OK
import play.api.libs.json.JsonValidationError
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RetrieveBusinessVerificationStatusParser.RetrieveBusinessVerificationStatusHttpReads
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationStatus, BusinessVerificationFail, BusinessVerificationPass}

import scala.concurrent.{ExecutionContext, Future}

class RetrieveBusinessVerificationStatusConnector @Inject()(http: HttpClient,
                                                            appConfig: AppConfig
                                                           )(implicit ec: ExecutionContext) {

  def retrieveBusinessVerificationStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[BusinessVerificationStatus] =
    http.GET[BusinessVerificationStatus](appConfig.getBusinessVerificationResultUrl(journeyId))(
      RetrieveBusinessVerificationStatusHttpReads,
      hc,
      ec
    )

}

object RetrieveBusinessVerificationStatusParser {
  val BusinessVerificationPassKey = "PASS"
  val BusinessVerificationFailKey = "FAIL"

  implicit object RetrieveBusinessVerificationStatusHttpReads extends HttpReads[BusinessVerificationStatus] {
    override def read(method: String, url: String, response: HttpResponse): BusinessVerificationStatus = {
      response.status match {
        case OK =>
          (response.json \ "verificationStatus")
            .validate[String]
            .collect(JsonValidationError("Invalid verification status returned from business verification")){
            case BusinessVerificationPassKey => BusinessVerificationPass
            case BusinessVerificationFailKey => BusinessVerificationFail
          }.getOrElse(throw new InternalServerException("Invalid response returned from retrieve Business Verification result"))
        case _ =>
          throw new InternalServerException("Invalid response returned from retrieve Business Verification result")
      }
    }
  }

}

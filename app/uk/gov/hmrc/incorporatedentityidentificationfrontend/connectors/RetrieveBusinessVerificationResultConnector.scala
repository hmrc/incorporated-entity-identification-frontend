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

import javax.inject.Inject
import play.api.http.Status.OK
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RetrieveBusinessVerificationResultParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationState

import scala.concurrent.{ExecutionContext, Future}

class RetrieveBusinessVerificationResultConnector @Inject()(http: HttpClient,
                                                            appConfig: AppConfig
                                                           )(implicit ec: ExecutionContext) {

  def getBusinessVerificationResult(journeyId: String)(implicit hc: HeaderCarrier): Future[BusinessVerificationState] =
    http.GET[BusinessVerificationState](appConfig.getBusinessVerificationResultUrl(journeyId))(
      RetrieveBusinessVerificationResultHttpReads,
      hc,
      ec
    )

}

object RetrieveBusinessVerificationResultParser {

  implicit object RetrieveBusinessVerificationResultHttpReads extends HttpReads[BusinessVerificationState] {

    override def read(method: String, url: String, response: HttpResponse): BusinessVerificationState = {
      response.status match {
        case OK =>
          (response.json \ "verificationStatus").as[BusinessVerificationState]
        case _ =>
          throw new InternalServerException("Invalid response returned from retrieve Business Verification result")
      }
    }

  }

}

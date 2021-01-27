/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.http.Status.CREATED
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.CreateJourneyHttpParser.CreateJourneyHttpReads

import scala.concurrent.{ExecutionContext, Future}

class JourneyConnector @Inject()(httpClient: HttpClient,
                                 appConfig: AppConfig)(implicit ec: ExecutionContext) {
  def createJourney()(implicit hc: HeaderCarrier): Future[String] =
    httpClient.POSTEmpty[String](appConfig.createJourneyUrl)(CreateJourneyHttpReads, hc, ec)
}

object CreateJourneyHttpParser {

  implicit object CreateJourneyHttpReads extends HttpReads[String] {
    override def read(method: String, url: String, response: HttpResponse): String = {
      response.status match {
        case CREATED =>
          (response.json \ "journeyId").as[String]
        case _ =>
          throw new InternalServerException("Invalid response returned from create journey API")
      }
    }
  }

}

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
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.IncorporatedEntityIdentificationHttpParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CompanyNameStored

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorporatedEntityIdentificationConnector @Inject()(http: HttpClient,
                                                          appConfig: AppConfig
                                                         )(implicit ec: ExecutionContext) {

  def storeCompanyName(journeyId: String, companyName: String)(implicit hc: HeaderCarrier): Future[CompanyNameStored.type] = {
    val companyNameKey = "companyName"
    val jsonBody = Json.obj(companyNameKey -> companyName)
    http.POST[JsObject, CompanyNameStored.type](appConfig.storeCompanyNameUrl(journeyId), jsonBody)
  }
}

object IncorporatedEntityIdentificationHttpParser {

  implicit object IncorporatedEntityIdentificationHttpReads extends HttpReads[CompanyNameStored.type] {
    override def read(method: String, url: String, response: HttpResponse): CompanyNameStored.type = {
      response.status match {
        case OK => CompanyNameStored
        case status => throw new InternalServerException(s"Companies House API failed with status: $status")
      }
    }
  }

}

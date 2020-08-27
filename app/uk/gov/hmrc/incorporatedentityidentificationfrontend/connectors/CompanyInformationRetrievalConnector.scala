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
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.CompanyInformationRetrievalHttpParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CompanyInformation

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CompanyInformationRetrievalConnector @Inject()(http: HttpClient,
                                                     appConfig: AppConfig
                                                    )(implicit ec: ExecutionContext) {

  def retrieveCompanyInformation(companyNumber: String)(implicit hc: HeaderCarrier): Future[CompanyInformation] =
    http.GET[CompanyInformation](appConfig.retrieveCompanyInformationUrl(companyNumber))

}

object CompanyInformationRetrievalHttpParser {

  private val companyNameKey = "company_name"

  implicit object CompanyInformationHttpReads extends HttpReads[CompanyInformation] {
    override def read(method: String, url: String, response: HttpResponse): CompanyInformation = {
      response.status match {
        case OK =>
          (response.json \ companyNameKey).asOpt[String] match {
            case Some(companyName) => CompanyInformation(companyName)
            case None => throw new InternalServerException("Companies House API returned invalid JSON")
          }
        case status => throw new InternalServerException(s"Companies House API failed with status: $status")
      }
    }
  }

}

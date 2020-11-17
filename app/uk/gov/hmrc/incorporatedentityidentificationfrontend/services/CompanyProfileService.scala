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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CompanyProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

@Singleton
class CompanyProfileService @Inject()(storageConnector: IncorporatedEntityInformationConnector,
                                      companyProfileConnector: CompanyProfileConnector
                                     )(implicit ec: ExecutionContext) {

  def retrieveAndStoreCompanyProfile(journeyId: String,
                                     companyNumber: String
                                    )(implicit hc: HeaderCarrier): Future[Option[CompanyProfile]] =
    companyProfileConnector.getCompanyProfile(padCrn(companyNumber)).flatMap {
      case Some(companyProfile) =>
        storageConnector.storeData(journeyId, "companyProfile", companyProfile).map {
          _ => Some(companyProfile)
        }
      case None =>
        Future.successful(None)
    }


  private def padCrn(companyNumber: String): String = {
    val crnMaxLength: Int = 8
    val CrnRegex: Regex = "([a-zA-Z]*)([0-9a-zA-Z]*)".r
    val padding: String = Seq.fill(crnMaxLength - companyNumber.length)('0').mkString

    companyNumber match {
      case CrnRegex(prefix, remainder) =>
        prefix + padding + remainder
      case _ =>
        throw new IllegalArgumentException(s"[CompanyProfileService] Company number: '$companyNumber' is invalid") }
  }

}

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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.IncorporatedEntityInformationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.RemoveIncorporatedEntityDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.StorageService._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorageService @Inject()(connector: IncorporatedEntityInformationConnector) {

  def storeCompanyProfile(journeyId: String,
                          companyProfile: CompanyProfile
                         )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[CompanyProfile](journeyId, CompanyProfileKey, companyProfile)

  def storeCtutr(journeyId: String,
                 ctutr: String
                )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[String](journeyId, CtutrKey, ctutr)

  def storeCHRN(journeyId: String,
                chrn: String
               )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[String](journeyId, ChrnKey, chrn)

  def storeBusinessVerificationStatus(journeyId: String,
                                      businessVerification: BusinessVerificationStatus
                                     )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[BusinessVerificationStatus](journeyId, VerificationStatusKey, businessVerification)


  def storeIdentifiersMatch(journeyId: String,
                            identifiersMatch: Boolean
                           )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[Boolean](journeyId, IdentifiersMatchKey, identifiersMatch)

  def storeRegistrationStatus(journeyId: String,
                              registrationStatus: RegistrationStatus
                             )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[RegistrationStatus](journeyId, RegistrationKey, registrationStatus)

  def retrieveCompanyProfile(journeyId: String
                            )(implicit hc: HeaderCarrier): Future[Option[CompanyProfile]] =
    connector.retrieveIncorporatedEntityInformation[CompanyProfile](journeyId, CompanyProfileKey)

  def retrieveCompanyNumber(journeyId: String
                           )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    connector.retrieveIncorporatedEntityInformation[CompanyProfile](journeyId, CompanyProfileKey).map {
      case Some(companyNumber) =>
        companyNumber.companyNumber
      case _ =>
        throw new NotFoundException(s"[IncorporatedEntityInformationService] [retrieveCompanyName] Company Profile was not found for journey ID $journeyId")
    }

  def retrieveBusinessVerificationStatus(journeyId: String
                                        )(implicit hc: HeaderCarrier): Future[Option[BusinessVerificationStatus]] =
    connector.retrieveIncorporatedEntityInformation[BusinessVerificationStatus](journeyId, VerificationStatusKey)

  def retrieveCtutr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveIncorporatedEntityInformation[String](journeyId, CtutrKey)

  def retrieveRegistrationStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[RegistrationStatus]] =
    connector.retrieveIncorporatedEntityInformation[RegistrationStatus](journeyId, RegistrationKey)

  def retrieveIdentifiersMatch(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    connector.retrieveIncorporatedEntityInformation[Boolean](journeyId, IdentifiersMatchKey)

  def retrieveIncorporatedEntityInformation(journeyId: String
                                           )(implicit hc: HeaderCarrier): Future[Option[IncorporatedEntityInformation]] =
    connector.retrieveIncorporatedEntityInformation(journeyId)

  def removeCtutr(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeIncorporatedEntityDetailsField(journeyId, CtutrKey)

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeAllData(journeyId)

  def removeCHRN(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeIncorporatedEntityDetailsField(journeyId, ChrnKey)

}

object StorageService {
  val CompanyProfileKey: String = "companyProfile"
  val CtutrKey: String = "ctutr"
  val ChrnKey: String = "chrn"
  val IdentifiersMatchKey: String = "identifiersMatch"
  val VerificationStatusKey: String = "businessVerification"
  val RegistrationKey: String = "registration"
}

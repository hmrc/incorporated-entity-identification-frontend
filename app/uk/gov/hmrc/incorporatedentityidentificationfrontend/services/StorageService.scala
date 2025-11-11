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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.IncorporatedEntityInformationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.RemoveIncorporatedEntityDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorageService @Inject()(connector: IncorporatedEntityInformationConnector) {

  def storeCompanyProfile(journeyId: String,
                          companyProfile: CompanyProfile
                         )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[CompanyProfile](journeyId, StorageService.CompanyProfileKey, companyProfile)

  def storeCtutr(journeyId: String,
                 ctutr: String
                )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[String](journeyId, StorageService.CtutrKey, ctutr)

  def storeCHRN(journeyId: String,
                chrn: String
               )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[String](journeyId, StorageService.ChrnKey, chrn)

  def storeBusinessVerificationStatus(journeyId: String,
                                      businessVerification: BusinessVerificationStatus
                                     )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[BusinessVerificationStatus](journeyId, StorageService.VerificationStatusKey, businessVerification)


  def storeIdentifiersMatch(journeyId: String,
                            identifiersMatch: IncorporatedEntityDetailsMatching
                           )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[IncorporatedEntityDetailsMatching](journeyId, IncorporatedEntityInformation.IdentifiersMatchKey, identifiersMatch)

  def storeRegistrationStatus(journeyId: String,
                              registrationStatus: RegistrationStatus
                             )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[RegistrationStatus](journeyId, StorageService.RegistrationKey, registrationStatus)

  def retrieveCompanyProfile(journeyId: String
                            )(implicit hc: HeaderCarrier): Future[Option[CompanyProfile]] =
    connector.retrieveIncorporatedEntityInformation[CompanyProfile](journeyId, StorageService.CompanyProfileKey)

  def retrieveCompanyNumber(journeyId: String
                           )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    connector.retrieveIncorporatedEntityInformation[CompanyProfile](journeyId, StorageService.CompanyProfileKey).map {
      case Some(companyNumber) =>
        companyNumber.companyNumber
      case _ =>
        throw new NotFoundException(s"[IncorporatedEntityInformationService] [retrieveCompanyName] Company Profile was not found for journey ID $journeyId")
    }

  def retrieveBusinessVerificationStatus(journeyId: String
                                        )(implicit hc: HeaderCarrier): Future[Option[BusinessVerificationStatus]] =
    connector.retrieveIncorporatedEntityInformation[BusinessVerificationStatus](journeyId, StorageService.VerificationStatusKey)

  def retrieveCtutr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveIncorporatedEntityInformation[String](journeyId, StorageService.CtutrKey)

  def retrieveCHRN(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveIncorporatedEntityInformation[String](journeyId, StorageService.ChrnKey)

  def retrieveRegistrationStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[RegistrationStatus]] =
    connector.retrieveIncorporatedEntityInformation[RegistrationStatus](journeyId, StorageService.RegistrationKey)

  def retrieveIdentifiersMatch(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[IncorporatedEntityDetailsMatching]] =
    connector.retrieveIncorporatedEntityInformation[IncorporatedEntityDetailsMatching](journeyId, IncorporatedEntityInformation.IdentifiersMatchKey)

  def retrieveIncorporatedEntityInformation(journeyId: String
                                           )(implicit hc: HeaderCarrier): Future[Option[IncorporatedEntityInformation]] =
    connector.retrieveIncorporatedEntityInformation(journeyId)

  def removeCtutr(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeIncorporatedEntityDetailsField(journeyId, StorageService.CtutrKey)

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeAllData(journeyId)

  def removeCHRN(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeIncorporatedEntityDetailsField(journeyId, StorageService.ChrnKey)

}

object StorageService {
  val CompanyProfileKey: String = "companyProfile"
  val CtutrKey: String = "ctutr"
  val ChrnKey: String = "chrn"
  val VerificationStatusKey: String = "businessVerification"
  val RegistrationKey: String = "registration"
}

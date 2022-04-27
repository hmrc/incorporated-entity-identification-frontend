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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.{routes => errorRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{AuditService, JourneyService, StorageService, ValidateIncorporatedEntityDetailsService}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.helpers.CheckYourAnswersRowBuilder
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(journeyService: JourneyService,
                                           auditService: AuditService,
                                           storageService: StorageService,
                                           validateIncorporatedEntityDetailsService: ValidateIncorporatedEntityDetailsService,
                                           mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           rowBuilder: CheckYourAnswersRowBuilder,
                                           val authConnector: AuthConnector)
                                          (implicit val config: AppConfig,
                                           ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          for {
            journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
            optCompanyProfile <- storageService.retrieveCompanyProfile(journeyId)
            optCtutr <- storageService.retrieveCtutr(journeyId)
            optChrn <- storageService.retrieveCHRN(journeyId)
            summaryRows = rowBuilder.buildSummaryListRows(journeyId, optCompanyProfile, optCtutr, optChrn, journeyConfig)
          } yield
            Ok(view(
              pageConfig = journeyConfig.pageConfig,
              formAction = routes.CheckYourAnswersController.submit(journeyId),
              summaryRows = summaryRows
            ))
        case None => throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          for {
            journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
            optCompanyProfile <- storageService.retrieveCompanyProfile(journeyId)
            optCtutr <- storageService.retrieveCtutr(journeyId)
            details <- (journeyConfig.businessEntity, optCompanyProfile) match {
              case (LimitedCompany | RegisteredSociety, Some(companyProfile)) => validateIncorporatedEntityDetailsService.validateIncorporatedEntityDetails(companyProfile.companyNumber, optCtutr)
              case (CharitableIncorporatedOrganisation, Some(_)) => Future.successful(DetailsNotProvided)
              case _ => throw new InternalServerException("Invalid state in Check Your Answers Controller for journeyId: " + journeyId)
            }
            result <- details match {
              case DetailsMatched if journeyConfig.businessVerificationCheck => for {
                _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = DetailsMatched)}
                yield Redirect(routes.BusinessVerificationController.startBusinessVerificationJourney(journeyId))
              case DetailsMatched if journeyConfig.businessVerificationCheck.equals(false) => for {
                _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = DetailsMatched)
              } yield Redirect(routes.RegistrationController.register(journeyId))
              case DetailsNotFound if optCtutr.isEmpty => // The absence of a Ct Utr means the entity must be a Registered Society
                for {
                  _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = DetailsNotProvided)
                  _ <-
                    if (journeyConfig.businessVerificationCheck) storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
                    else Future.successful(())
                  _ <- storageService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
                  _ <- auditService.auditJourney(journeyId, authInternalId)
                } yield Redirect(routes.JourneyRedirectController.redirectToContinueUrl(journeyId))
              case DetailsNotFound =>
                for {
                  _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = DetailsNotFound)
                  _ <-
                    if (journeyConfig.businessVerificationCheck) storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
                    else Future.successful(())
                  _ <- storageService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
                  _ <- auditService.auditJourney(journeyId, authInternalId)
                } yield Redirect(errorRoutes.CtutrNotFoundController.show(journeyId))
              case DetailsNotProvided => for {
                _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = DetailsNotProvided)
                _ <- if (journeyConfig.businessVerificationCheck) storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
                else Future.successful(())
                _ <- storageService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
                _ <- auditService.auditJourney(journeyId, authInternalId)
              } yield Redirect(routes.JourneyRedirectController.redirectToContinueUrl(journeyId))
              case DetailsMismatch if optCtutr.isEmpty =>
                for {
                  _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = DetailsMismatch)
                  _ <-
                    if (journeyConfig.businessVerificationCheck) storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
                    else Future.successful(())
                  _ <- storageService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
                  _ <- auditService.auditJourney(journeyId, authInternalId)
                } yield Redirect(errorRoutes.CtutrNotFoundController.show(journeyId))
              case _ =>
                for {
                  _ <- storageService.storeIdentifiersMatch(journeyId, identifiersMatch = DetailsMismatch)
                  _ <-
                    if (journeyConfig.businessVerificationCheck) storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
                    else Future.successful(())
                  _ <- storageService.storeRegistrationStatus(journeyId, RegistrationNotCalled)
                  _ <- auditService.auditJourney(journeyId, authInternalId)
                } yield Redirect(errorRoutes.CtutrMismatchController.show(journeyId))
            }
          } yield result
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }
}

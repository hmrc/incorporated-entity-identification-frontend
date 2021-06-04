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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, internalId}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{EnableIRCTEnrolmentJourney, FeatureSwitching}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.ValidateIncorporatedEntityDetailsHttpParser.DetailsMatched
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CtEnrolled
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{IncorporatedEntityInformationService, JourneyService, ValidateIncorporatedEntityDetailsService}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.EnrolmentUtils.getEnrolmentCtutr
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.confirm_business_name_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmBusinessNameController @Inject()(incorporatedEntityInformationRetrievalService: IncorporatedEntityInformationService,
                                              journeyService: JourneyService,
                                              validateIncorporatedEntityDetailsService: ValidateIncorporatedEntityDetailsService,
                                              mcc: MessagesControllerComponents,
                                              view: confirm_business_name_page,
                                              val authConnector: AuthConnector
                                             )(implicit val config: AppConfig,
                                               ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            journeyConfig =>
              incorporatedEntityInformationRetrievalService.retrieveCompanyProfile(journeyId).map {
                case Some(companiesHouseInformation) =>
                  Ok(view(journeyConfig.pageConfig, routes.ConfirmBusinessNameController.submit(journeyId), companiesHouseInformation.companyName, journeyId))
                case None =>
                  throw new InternalServerException("No company profile stored")
              }
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(allEnrolments) {
        enrolments =>
          if (isEnabled(EnableIRCTEnrolmentJourney)) {
            getEnrolmentCtutr(enrolments) match {
              case Some(ctutr) =>
                incorporatedEntityInformationRetrievalService.retrieveCompanyProfile(journeyId).flatMap {
                  case Some(companyProfile) =>
                    validateIncorporatedEntityDetailsService.validateIncorporatedEntityDetails(companyProfile.companyNumber, ctutr).flatMap {
                      case DetailsMatched =>
                        for {
                          _ <- incorporatedEntityInformationRetrievalService.storeIdentifiersMatch(journeyId, identifiersMatch = true)
                          _ <- incorporatedEntityInformationRetrievalService.storeCtutr(journeyId, ctutr)
                          _ <- incorporatedEntityInformationRetrievalService.storeBusinessVerificationStatus(journeyId, CtEnrolled)
                        } yield Redirect(routes.RegistrationController.register(journeyId))
                      case _ => Future.successful(Redirect(routes.CaptureCtutrController.show(journeyId)))
                    }
                  case _ =>
                    throw new InternalServerException("No data stored")
                }
              case None => Future.successful(Redirect(routes.CaptureCtutrController.show(journeyId)))
            }
          }
          else Future.successful(Redirect(routes.CaptureCtutrController.show(journeyId)))
      }
  }

}

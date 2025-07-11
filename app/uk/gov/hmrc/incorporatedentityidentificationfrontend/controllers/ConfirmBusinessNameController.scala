/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.ConfirmBusinessNameForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.CtEnrolmentService._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{CtEnrolmentService, JourneyService, StorageService}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.confirm_business_name_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmBusinessNameController @Inject()(incorporatedEntityInformationRetrievalService: StorageService,
                                              journeyService: JourneyService,
                                              ctEnrolmentService: CtEnrolmentService,
                                              mcc: MessagesControllerComponents,
                                              view: confirm_business_name_page,
                                              messagesHelper: MessagesHelper,
                                              val authConnector: AuthConnector
                                             )(implicit val config: AppConfig,
                                               ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            journeyConfig =>
              implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
              incorporatedEntityInformationRetrievalService.retrieveCompanyProfile(journeyId).map {
                case Some(companiesHouseInformation) =>
                  Ok(view(journeyConfig.pageConfig,
                    ConfirmBusinessNameForm.form(messages),
                    routes.ConfirmBusinessNameController.submit(journeyId),
                    companiesHouseInformation.companyName, journeyId))
                case None =>
                  throw new InternalServerException("No company profile stored")
              }
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised().retrieve(allEnrolments and internalId) {
      case enrolments ~ Some(authInternalId) =>
        journeyService.getJourneyConfig(journeyId, authInternalId).flatMap { journeyConfig =>
          implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
          ConfirmBusinessNameForm.form(messages).bindFromRequest().fold(
            formWithErrors =>
              incorporatedEntityInformationRetrievalService.retrieveCompanyProfile(journeyId).map {
                case Some(companiesHouseInformation) =>
                  BadRequest(view(
                    journeyConfig.pageConfig,
                    formWithErrors,
                    routes.ConfirmBusinessNameController.submit(journeyId),
                    companiesHouseInformation.companyName,
                    journeyId
                  ))
                case None =>
                  throw new InternalServerException("No company profile stored")
              },
            {
              case ConfirmBusinessNameForm.yes =>
                journeyConfig.businessEntity match {
                  case LimitedCompany | RegisteredSociety =>
                    ctEnrolmentService.checkCtEnrolment(journeyId, enrolments, journeyConfig).map {
                      case Enrolled =>
                        Redirect(routes.RegistrationController.register(journeyId))
                      case EnrolmentMismatch | NoEnrolmentFound =>
                        Redirect(routes.CaptureCtutrController.show(journeyId))
                    }
                  case CharitableIncorporatedOrganisation =>
                    Future.successful(Redirect(routes.CaptureCHRNController.show(journeyId)))
                }
              case ConfirmBusinessNameForm.no =>
                Future.successful(Redirect(routes.CaptureCompanyNumberController.show(journeyId)))
            }
          )
        }
      case _ ~ None =>
        throw new InternalServerException("Internal ID could not be retrieved from Auth")
    }
  }

}

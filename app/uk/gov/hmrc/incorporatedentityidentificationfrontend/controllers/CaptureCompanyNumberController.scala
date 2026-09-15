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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.{routes => errorRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.CaptureCompanyNumberForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.capture_company_number_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureCompanyNumberController @Inject()(companyProfileService: CompanyProfileService,
                                               storageService: StorageService,
                                               journeyService: JourneyService,
                                               mcc: MessagesControllerComponents,
                                               view: capture_company_number_page,
                                               messagesHelper: MessagesHelper,
                                               val authConnector: AuthConnector)
                                              (implicit val config: AppConfig,
                                               ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          for {
            journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
            storedCompanyProfile <- storageService.retrieveCompanyProfile(journeyId)
          } yield {
              implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
              val form = storedCompanyProfile match {
                case Some(companyProfile) => CaptureCompanyNumberForm.form.fill(companyProfile.companyNumber)
                case None                 => CaptureCompanyNumberForm.form
              }
              Ok(view(journeyConfig.pageConfig, routes.CaptureCompanyNumberController.submit(journeyId), form))
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          CaptureCompanyNumberForm.form.bindFromRequest().fold(
            formWithErrors => {
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig =>
                  implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                  BadRequest(view(journeyConfig.pageConfig, routes.CaptureCompanyNumberController.submit(journeyId), formWithErrors))
              }
            },
            companyNumber =>
              companyProfileService.retrieveAndStoreCompanyProfile(journeyId, companyNumber).flatMap {
                case Some(_) =>
                  storageService.removeConfirmBusinessName(journeyId).map {_ =>
                    Redirect(routes.ConfirmBusinessNameController.show(journeyId))
                  }
                case None =>
                  Future.successful(Redirect(errorRoutes.CompanyNumberNotFoundController.show(journeyId)))
              }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

}

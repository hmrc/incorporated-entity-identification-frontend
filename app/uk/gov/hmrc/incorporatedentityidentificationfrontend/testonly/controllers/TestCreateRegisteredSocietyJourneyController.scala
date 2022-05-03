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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.RegisteredSociety
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.connectors.TestCreateJourneyConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.forms.TestCreateJourneyForm.form
import uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.views.html.test_create_journey
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class TestCreateRegisteredSocietyJourneyController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                                             testCreateJourneyConnector: TestCreateJourneyConnector,
                                                             view: test_create_journey,
                                                             val authConnector: AuthConnector
                                                         )(implicit ec: ExecutionContext,
                                                           appConfig: AppConfig) extends FrontendController(messagesControllerComponents) with AuthorisedFunctions {

  private val defaultPageConfig: PageConfig = PageConfig(
    optServiceName = None,
    deskProServiceId = "vrs",
    signOutUrl = appConfig.vatRegFeedbackUrl,
    accessibilityUrl = "/accessibility"
  )

  private val defaultJourneyConfig = JourneyConfig(
    continueUrl = s"${appConfig.selfUrl}/identify-your-incorporated-business/test-only/retrieve-journey",
    pageConfig = defaultPageConfig,
    RegisteredSociety,
    businessVerificationCheck = true,
    regime = "VATC"
  )

  val show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(
          Ok(view(defaultPageConfig, routes.TestCreateRegisteredSocietyJourneyController.submit, form(RegisteredSociety).fill(defaultJourneyConfig)))
        )
      }
  }

  val submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        form(RegisteredSociety).bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(defaultPageConfig, routes.TestCreateRegisteredSocietyJourneyController.submit, formWithErrors))
            ),
          journeyConfig =>
            testCreateJourneyConnector.createRegisteredSocietyJourney(journeyConfig).map {
              journeyUrl => SeeOther(journeyUrl)
            }
        )
      }
  }
}


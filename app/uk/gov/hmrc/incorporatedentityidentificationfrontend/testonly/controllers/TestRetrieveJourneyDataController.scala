/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.connectors.TestRetrieveJourneyDataConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TestRetrieveJourneyDataController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                                  testRetrieveJourneyDataConnector: TestRetrieveJourneyDataConnector,
                                                  val authConnector: AuthConnector
                                                 )(implicit ec: ExecutionContext) extends FrontendController(messagesControllerComponents) with AuthorisedFunctions {

  def retrieveIncorporatedEntityInformation(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        testRetrieveJourneyDataConnector.retrieveJourneyData(journeyId).map(Ok(_))
      }
  }

}

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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.{routes => controllerRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{IncorporatedEntityInformation, JourneyConfig, JourneyLabels, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{JourneyService, StorageService}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.UrlHelper
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyController @Inject()(controllerComponents: ControllerComponents,
                                  journeyService: JourneyService,
                                  incorporatedEntityInformationRetrievalService: StorageService,
                                  val authConnector: AuthConnector,
                                  appConfig: AppConfig,
                                  urlHelper: UrlHelper
                                 )(implicit ec: ExecutionContext) extends BackendController(controllerComponents) with AuthorisedFunctions {

  def createLtdCompanyJourney: Action[JourneyConfig] = createJourney(LimitedCompany)

  def createRegisteredSocietyJourney: Action[JourneyConfig] = createJourney(RegisteredSociety)

  def createCharitableIncorporatedOrganisationJourney: Action[JourneyConfig] = createJourney(CharitableIncorporatedOrganisation)

  private def createJourney(businessEntity: BusinessEntity): Action[JourneyConfig] =
    Action.async(parse.json[JourneyConfig] { json =>
      for {
        continueUrl <- (json \ continueUrlKey).validate[String]
        optServiceName <- (json \ optServiceNameKey).validateOpt[String]
        deskProServiceId <- (json \ deskProServiceIdKey).validate[String]
        signOutUrl <- (json \ signOutUrlKey).validate[String]
        businessVerificationCheck <- (json \ businessVerificationCheckKey).validateOpt[Boolean]
        regime <- (json \ regimeKey).validate[String]
        accessibilityUrl <- (json \ accessibilityUrlKey).validate[String]
        labels <- (json \ labelsKey).validateOpt[JourneyLabels]
      } yield JourneyConfig(
        continueUrl,
        PageConfig(optServiceName, deskProServiceId, signOutUrl, accessibilityUrl, labels),
        businessEntity,
        businessVerificationCheck.getOrElse(true),
        regime)
    }) {
      implicit req =>
        authorised().retrieve(internalId) {
          case Some(authInternalId) =>
            if (urlHelper.areRelativeOrAcceptedUrls(List(req.body.continueUrl, req.body.pageConfig.signOutUrl, req.body.pageConfig.accessibilityUrl))) {
              journeyService.createJourney(authInternalId, req.body).map(
                journeyId =>
                  Created(Json.obj(
                    "journeyStartUrl" -> s"${appConfig.selfUrl}${controllerRoutes.CaptureCompanyNumberController.show(journeyId).url}"
                  )))
            } else Future.successful(BadRequest(Json.toJson("JourneyConfig contained non-relative urls")))
          case _ =>
            throw new InternalServerException("Internal ID could not be retrieved from Auth")
        }
    }

  def retrieveJourneyData(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised() {
        incorporatedEntityInformationRetrievalService.retrieveIncorporatedEntityInformation(journeyId).map {
          case Some(journeyData) =>
            Ok(Json.toJson(journeyData)(IncorporatedEntityInformation.jsonWriterForCallingServices))
          case None =>
            NotFound
        }
      }
  }

}

object JourneyController {
  val continueUrlKey = "continueUrl"
  val optServiceNameKey = "optServiceName"
  val deskProServiceIdKey = "deskProServiceId"
  val signOutUrlKey = "signOutUrl"
  val businessVerificationCheckKey = "businessVerificationCheck"
  val regimeKey = "regime"
  val accessibilityUrlKey = "accessibilityUrl"
  val labelsKey = "labels"
}

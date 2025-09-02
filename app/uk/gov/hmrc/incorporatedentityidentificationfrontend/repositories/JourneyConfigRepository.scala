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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import play.api.libs.json._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.JourneyConfigRepository._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConfigRepository @Inject()(mongoComponent: MongoComponent,
                                        appConfig: AppConfig)(implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "incorporated-entity-identification-frontend",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = Seq(timeToLiveIndex(appConfig.timeToLiveSeconds)),
  extraCodecs = Seq(Codecs.playFormatCodec(journeyConfigMongoFormat))
) {

  def insertJourneyConfig(journeyId: String, authInternalId: String, journeyConfig: JourneyConfig): Future[InsertOneResult] = {

    val document: JsObject = Json.obj(
      JourneyIdKey -> journeyId,
      AuthInternalIdKey -> authInternalId,
      CreationTimestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
    ) ++ Json.toJsObject(journeyConfig)

    collection.insertOne(document).toFuture()
  }

  def findJourneyConfig(journeyId: String, authInternalId: String): Future[Option[JourneyConfig]] = {

    collection.find[JourneyConfig](
      Filters.and(
        Filters.equal(JourneyIdKey, journeyId),
        Filters.equal(AuthInternalIdKey, authInternalId)
      )
    ).headOption()

  }

  def count: Future[Long] = collection.countDocuments().toFuture()

  def removeJourneyConfig(journeyId: String, authInternalId: String): Future[DeleteResult] = {

    collection.deleteOne(
      Filters.and(
        Filters.equal(JourneyIdKey, journeyId),
        Filters.equal(AuthInternalIdKey, authInternalId)
      )
    ).toFuture()

  }

  def drop: Future[Unit] = collection.drop().toFuture().map(_ => ())
}

object JourneyConfigRepository {
  val JourneyIdKey = "_id"
  val AuthInternalIdKey = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"
  val BusinessEntityKey = "businessEntity"
  val LtdCompanyKey = "LtdCompany"
  val RegisteredSocietyKey = "RegisteredSociety"
  val CharitableIncorporatedOrganisationKey = "CharitableIncorporatedOrganisation"
  val ContinueUrlKey = "continueUrl"
  val PageConfigKey = "pageConfig"
  val BusinessVerificationCheckKey = "businessVerificationCheck"
  val RegimeKey = "regime"

  def timeToLiveIndex(timeToLiveDuration: Long): IndexModel = {
    IndexModel(
      keys = ascending(CreationTimestampKey),
      indexOptions = IndexOptions()
        .name("IncorporatedEntityInformationExpires")
        .expireAfter(timeToLiveDuration, TimeUnit.SECONDS)
    )
  }

  implicit val partnershipTypeMongoFormat: Format[BusinessEntity] = new Format[BusinessEntity] {
    override def reads(json: JsValue): JsResult[BusinessEntity] = json.validate[String].collect(JsonValidationError("Invalid entity type")) {
      case LtdCompanyKey => LimitedCompany
      case RegisteredSocietyKey => RegisteredSociety
      case CharitableIncorporatedOrganisationKey => CharitableIncorporatedOrganisation
    }

    override def writes(partnershipType: BusinessEntity): JsValue = partnershipType match {
      case LimitedCompany => JsString(LtdCompanyKey)
      case RegisteredSociety => JsString(RegisteredSocietyKey)
      case CharitableIncorporatedOrganisation => JsString(CharitableIncorporatedOrganisationKey)
    }
  }

  implicit val journeyConfigMongoFormat: OFormat[JourneyConfig] = new OFormat[JourneyConfig] {
    override def reads(json: JsValue): JsResult[JourneyConfig] =
      for {
        continueUrl <- (json \ ContinueUrlKey).validate[String]
        pageConfig <- (json \ PageConfigKey).validate[PageConfig]
        businessEntity <- (json \ BusinessEntityKey).validate[BusinessEntity]
        businessVerificationCheck <- (json \ BusinessVerificationCheckKey).validate[Boolean]
        regime <- (json \ RegimeKey).validate[String]
      } yield {
        JourneyConfig(
          continueUrl,
          pageConfig,
          businessEntity,
          businessVerificationCheck,
          regime)
      }

    override def writes(journeyConfig: JourneyConfig): JsObject = Json.obj(
      ContinueUrlKey -> journeyConfig.continueUrl,
      PageConfigKey -> journeyConfig.pageConfig,
      BusinessEntityKey -> journeyConfig.businessEntity,
      BusinessVerificationCheckKey -> journeyConfig.businessVerificationCheck,
      RegimeKey -> journeyConfig.regime
    )

  }

}
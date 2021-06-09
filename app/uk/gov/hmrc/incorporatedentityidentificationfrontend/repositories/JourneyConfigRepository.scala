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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories

import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Format, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.JourneyConfigRepository._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConfigRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent,
                                        appConfig: AppConfig)
                                       (implicit ec: ExecutionContext) extends ReactiveRepository[JourneyConfig, String](
  collectionName = "incorporated-entity-identification-frontend",
  mongo = reactiveMongoComponent.mongoConnector.db,
  domainFormat = JourneyConfig.format,
  idFormat = implicitly[Format[String]]
) {

  def insertJourneyConfig(journeyId: String, authInternalId: String, journeyConfig: JourneyConfig): Future[WriteResult] = {
    val document = Json.obj(
      JourneyIdKey -> journeyId,
      AuthInternalIdKey -> authInternalId,
      CreationTimestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
    ) ++ Json.toJsObject(journeyConfig)

    collection.insert(true).one(document)
  }

  def findJourneyConfig(journeyId: String, authInternalId: String): Future[Option[JourneyConfig]] =
    collection.find(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId
      ),
      Some(Json.obj(
        JourneyIdKey -> 0,
        AuthInternalIdKey -> 0
      ))
    ).one[JourneyConfig]

  private lazy val ttlIndex = Index(
    Seq(("creationTimestamp", IndexType.Ascending)),
    name = Some("IncorporatedEntityInformationExpires"),
    options = BSONDocument("expireAfterSeconds" -> appConfig.timeToLiveSeconds)
  )

  private def setIndex(): Unit = {
    collection.indexesManager.drop(ttlIndex.name.get) onComplete {
      _ => collection.indexesManager.ensure(ttlIndex)
    }
  }

  setIndex()

  override def drop(implicit ec: ExecutionContext): Future[Boolean] =
    collection.drop(failIfNotFound = false).map { r =>
      setIndex()
      r
    }

}

object JourneyConfigRepository {
  val JourneyIdKey = "_id"
  val AuthInternalIdKey = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"
}
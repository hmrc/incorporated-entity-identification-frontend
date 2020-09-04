/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Format, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.mongo.ReactiveRepository
import play.api.libs.json.Format
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConfigRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)
                                       (implicit ec: ExecutionContext) extends
  ReactiveRepository[JourneyConfig, String](
    collectionName = "incorporated-entity-identification-frontend",
    mongo = reactiveMongoComponent.mongoConnector.db,
    domainFormat = JourneyConfig.format,
    idFormat = implicitly[Format[String]]
  ) {
  def insertJourneyConfig(journeyId: String, journeyConfig: JourneyConfig): Future[WriteResult] = {
    val document = Json.obj(
      "_id" -> journeyId,
    ) ++ Json.toJsObject(journeyConfig)

    collection.insert(true).one(document)
  }
}



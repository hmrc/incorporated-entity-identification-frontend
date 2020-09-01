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
import play.api.libs.json.{Format, JsObject, Json, Writes}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.play.json.JSONSerializationPack.Reader
import reactivemongo.play.json.JsObjectDocumentWriter
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.IncorporatedEntityInformation
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.IncorporatedEntityInformationRepository._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorporatedEntityInformationRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)
                                                       (implicit ec: ExecutionContext) extends
  ReactiveRepository[IncorporatedEntityInformation, String](
    collectionName = "incorporated-entity-identification-frontend",
    mongo = reactiveMongoComponent.mongoConnector.db,
    domainFormat = IncorporatedEntityInformation.format,
    idFormat = implicitly[Format[String]]
  ) {

  def storeCompanyNumber(journeyId: String, companyNumber: String): Future[Unit] =
    upsert[String](journeyId, companyNumberKey, companyNumber).map(_ => ())

  def retrieveCompanyNumber(journeyId: String): Future[Option[String]] =
    retrieve[String](journeyId, companyNumberKey)

  private def upsert[T](journeyId: String, key: String, updates: T)
                       (implicit writes: Writes[T]): Future[UpdateWriteResult] =
    collection.update(ordered = false).one(
      q = Json.obj(idKey -> journeyId),
      u = Json.obj(fields = "$set" -> Json.obj(key -> updates)),
      upsert = true
    ).filter(_.n == 1)

  private def retrieve[T](journeyId: String, key: String)(implicit reads: Reader[T]): Future[Option[T]] =
    collection.find(
      selector = Json.obj(
        idKey -> journeyId
      ),
      projection = Some(
        Json.obj(
          idKey -> 0,
          key -> 1
        )
      )
    ).one[JsObject].map(
      _.map(
        js => (js \ key).as[T]
      )
    )

}

object IncorporatedEntityInformationRepository {
  val idKey = "_id"
  val companyNumberKey = "companyNumber"
}


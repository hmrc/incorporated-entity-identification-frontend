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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, CheckYourAnswers => messages}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._

import scala.collection.JavaConverters._


trait CheckYourAnswersViewTests {
  this: AnyWordSpec with Matchers =>

  def testCheckYourAnswersView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a company number row" in {
        val firstNameRow = summaryListRows.head

        firstNameRow.getSummaryListQuestion mustBe messages.companyNumber
        firstNameRow.getSummaryListAnswer mustBe "12345678"
        firstNameRow.getSummaryListChangeLink mustBe routes.CaptureCompanyNumberController.show().url
        firstNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.companyNumber}"
      }

      "have a last name row" in {
        val lastNameRow = summaryListRows(1)

        lastNameRow.getSummaryListQuestion mustBe messages.ctutr
        lastNameRow.getSummaryListAnswer mustBe "1234567890"
        lastNameRow.getSummaryListChangeLink mustBe routes.CaptureCtutrController.show().url
        lastNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.ctutr}"
      }

      "have a continue and confirm button" in {
        doc.getSubmitButton.first.text mustBe Base.confirmAndContinue
      }

      "have a save and come back later button" in {
        doc.getSubmitButton.get(1).text mustBe Base.saveAndComeBack
      }
    }

  }

}

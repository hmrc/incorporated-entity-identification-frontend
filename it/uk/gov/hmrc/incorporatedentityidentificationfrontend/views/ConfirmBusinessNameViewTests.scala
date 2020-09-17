/*
 * Copyright 2019 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, ConfirmBusinessName => messages}

trait ConfirmBusinessNameViewTests {
  this: AnyWordSpec with Matchers =>

  def testConfirmBusinessNameView(result: => WSResponse, stub: => StubMapping, authStub: => StubMapping, testCompanyName: String): Unit = {
    lazy val doc: Document = {
      authStub
      stub
      Jsoup.parse(result.body)
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.first.text mustBe messages.heading
    }

    "display the company name" in {
      doc.getParagraphs.first.text mustBe testCompanyName
    }

    "Have the correct link" in {
      doc.getLink("change-company").text mustBe messages.change_company
    }

    "have a save and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a save and come back later button" in {
      doc.getSubmitButton.get(1).text mustBe Base.saveAndComeBack
    }
  }

}
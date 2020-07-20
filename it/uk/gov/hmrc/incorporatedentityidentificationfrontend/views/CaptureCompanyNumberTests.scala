
package uk.gov.hmrc.incorporatedentityidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, CaptureCompanyNumber => messages}

trait CaptureCompanyNumberTests {
  this: AnyWordSpec with Matchers =>

  def testCaptureCompanyNumberView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have a view with the correct title" in {
      doc.title mustBe messages.title
    }

    "have a save and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a save and come back later button" in {
      doc.getSubmitButton.get(1).text mustBe Base.saveAndComeBack
    }
  }
}

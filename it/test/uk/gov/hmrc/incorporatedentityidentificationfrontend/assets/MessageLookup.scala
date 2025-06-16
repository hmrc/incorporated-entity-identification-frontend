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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val continue = "Continue"
    val change = "Change"
    val saveAndContinue = "Save and continue"
    val saveAndComeBack = "Save and come back later"
    val tryAgain = "Try again"
    val back = "Back"
    val getHelp = "Is this page not working properly? (opens in new tab)"

    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }

  }

  object Header {
    val signOut = "Sign out"
  }

  object BetaBanner {
    val title = "This is a new service – your feedback will help us to improve it."
  }

  object CaptureCompanyNumber {
    val title = "Company registration number"
    val line_1 = "You can search Companies House for the company registration number (opens in new tab)"
    val label = "What is your company registration number?"
    val hint = "This is 8 characters, like AB123456. It may also be called ‘company number’ or ‘Companies House number’"

    object Error {
      val emptyCompanyNumber = "Enter the company registration number"
      val lengthCompanyNumber = "The company registration number must be 8 characters or fewer"
      val formatCompanyNumber = "Enter the company registration number in the correct format"
    }

  }

  object ConfirmBusinessName {
    val title = "Confirm the company name"
    val heading = "Confirm the company name"
    val change_company = "Change company"
  }

  object CaptureCHRN {
    val title = "What is the charity’s HMRC reference number?"
    val heading = "What is the charity’s HMRC reference number?"
    val insetText = "If the charity has registered for Gift Aid then their HMRC reference number will be the same as their Gift Aid number. This is not the same as the charity number available on the charity register."
    val hintText = "This could be up to 7 characters and must begin with either one or two letters, followed by 1-5 numbers. For example, A999 or AB99999"
    val labelText = "HMRC reference number"
    val noChrnLink = "The charity does not have a HMRC reference number"

    object Error {
      val noChrnEntered = "Enter the HMRC reference number"
      val invalidChrnEntered = "Enter the HMRC reference number in the correct format"
      val invalidLengthChrnEntered = "The HMRC reference number must be 7 characters or fewer"
    }

  }

  object CaptureCtutr {
    val title = "What is the company’s Unique Taxpayer Reference?"
    val heading = "What is the company’s Unique Taxpayer Reference?"
    val line = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Corporation Tax. It may be called ‘reference’, ‘UTR’ or ‘official use’."
    val lostUtr = "Lost the company’s UTR number"
    val registered_society_title = "What is the registered society’s Unique Taxpayer Reference?"
    val registered_society_heading = "What is the registered society’s Unique Taxpayer Reference?"
    val noCtutr = "The business does not have a Unique Taxpayer Reference"
    val dropdown_line_1 = "The UTR helps us identify your business"
    val dropdown_link_1 = "I have lost the businesses UTR number"
    val dropdown_link_2 = "My business does not have a UTR"

    object Error {
      val noCtutrEntered = "Enter the company’s Unique Taxpayer Reference"
      val noCtutrEntered_registeredSociety = "Enter the registered society’s Unique Taxpayer Reference"
      val invalidCtutrEntered = "Enter the company’s Unique Taxpayer Reference in the correct format"
      val invalidCtutrEntered_registeredSociety = "Enter the registered society’s Unique Taxpayer Reference in the correct format"
    }

  }

  object CheckYourAnswers {
    val title = "Check your answers"
    val heading = "Check your answers"
    val ctutr = "Unique Taxpayer Reference (UTR)"
    val noCtutr = "The business does not have a UTR"
    val chrn = "HMRC reference number"
    val noChrn = "The charity does not have a HMRC reference number"
    val companyNumber = "Company registration number"
  }

  object CtutrMismatch {
    val title: String = "We could not confirm your business"
    val heading: String = title
    val paragraph: String = "The information you provided does not match the details we have about your business."
  }

  object CompanyNumberNotFound {
    val title = "We could not confirm your company"
    val heading = "We could not confirm your company"
    val line = "The company number you entered is not on our system."

  }

  object CtutrNotFound {
    val title = "The details you entered did not match our records"
    val heading = "The details you entered did not match our records"
    val line1 = "We could not match the details you entered with records held by HMRC. You can "
    val line1link = "try again using different details"
    val line2 = "If you have used the correct details, you need to"
    val line2link = " contact the Corporation Tax team (opens in a new tab)"
    val line2b = " and tell them there is an issue with your Corporation Tax Unique Tax Reference."
  }

}

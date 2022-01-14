## Test End-Points

### GET test-only/feature-switches

---
Shows all feature switches:

1. Incorporated Entity Identification Frontend

    - Companies House API stub
    - Business Verification Stub
2. Incorporated Entity Identification

    - Get CT reference stub
    - DES stub

### GET test-only/create-limited-company-journey

---
Test entry point for initial call to set up a create limited company journey.

1. ContinueURL (Required)

    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - If nothing is entered, ```Entity Validation Service``` will be used

3. DeskPro Service ID (Required)

    - Used for the `Get help with this page` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

5. Business verification checkbox

    - Used for skipping further verification checks carried out currently by Business Verification (SI)
    - This is currently autofilled but can be changed

6. Regime (Required)

    - This is the Tax Regime Identifier
    - It is passed down to the Registration API
    - Accepted values are PPT or VATC

7. Accessibility statement URL (Required)

    - Shown in the footer - a link to the accessibility statement for the calling service
    - This is currently autofilled but can be changed

### GET test-only/create-registered-society-journey

---
Test entry point for initial call to set up a create registered society journey.

1. ContinueURL (Required)

    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - If nothing is entered, ```Entity Validation Service``` will be used

3. DeskPro Service ID (Required)

    - Used for the `Get help with this page` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

5. Business verification checkbox

    - Used for skipping further verification checks carried out currently by Business Verification (SI)
    - This is currently autofilled but can be changed

6. Regime (Required)

    - This is the Tax Regime Identifier
    - It is passed down to the Registration API
    - Accepted values are PPT or VATC

7. Accessibility statement URL (Required)

     - Shown in the footer - a link to the accessibility statement for the calling service
     - This is currently autofilled but can be changed

### GET test-only/create-cio-journey

---
Test entry point for initial call to set up a create charitable incorporated organisation journey.

1. ContinueURL (Required)
    - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

    - Service Name to use throughout the service
    - If nothing is entered, ```Entity Validation Service``` will be used

3. DeskPro Service ID (Required)

    - Used for the `Get help with this page` link
    - This is currently autofilled but can be changed

4. Sign Out Link (Required)

    - Shown in the HMRC header - typically a link to a feedback questionnaire
    - This is currently autofilled but can be changed

5. Business verification checkbox

    - Used for skipping further verification checks carried out currently by Business Verification (SI)
    - This is currently autofilled but can be changed

6. Regime (Required)

    - This is the Tax Regime Identifier
    - It is passed down to the Registration API
    - Accepted values are PPT or VATC

7. Accessibility statement URL (Required)

     - Shown in the footer - a link to the accessibility statement for the calling service
     - This is currently autofilled but can be changed

### GET test-only/create-journey

#### Deprecated - use one of the specific journey urls instead (e.g. create-limited-company-journey)

---
Test entry point for initial call to set up a create limited company journey.

1. ContinueURL (Required)

     - Where to redirect the user after the journey has been completed

2. Service Name (Optional)

     - Service Name to use throughout the service
     - If nothing is entered, ```Entity Validation Service``` will be used

3. DeskPro Service ID (Required)

     - Used for the `Get help with this page` link
     - This is currently autofilled but can be changed

4. Sign Out Link (Required)

     - Shown in the HMRC header - typically a link to a feedback questionnaire
     - This is currently autofilled but can be changed

5. Business verification checkbox

     - Used for skipping further verification checks carried out currently by Business Verification (SI)
     - This is currently autofilled but can be changed

6. Regime (Required)

     - This is the Tax Regime Identifier
     - It is passed down to the Registration API
     - Accepted values are PPT or VATC

7. Accessibility statement URL (Required)

     - Shown in the footer - a link to the accessibility statement for the calling service
     - This is currently autofilled but can be changed

### GET test-only/:companyNumber/incorporated-company-profile

---
Stubs retrieving the Company Profile from Companies House. The Companies House API stub feature switch will need to be
enabled.

##### Request:

A valid company Number must be sent in the URI

##### Response:

Status:

| Expected Response                       | Reason                              | Example                              |
|-----------------------------------------|-------------------------------------|--------------------------------------|
| ```OK(200)```                           |  ```Company Number exists```        | ```Any other valid Company Number``` |
| ```NOT_FOUND(404)```                    | ```Company Number doesn't exist```  | ```"00000001"```                     |

Example response body:

```
{
  "companyProfile": {
    "companyName":"TestCompanyLtd”,
    “companyNumber":"01234567",
    "dateOfIncorporation":"2020-01-01",
    "unsanitisedCHROAddress": {
      "address_line_1":"testLine1",
      "address_line_2":"test town",
      "care_of":"test name",
      "country":"United Kingdom",
      "locality":"test city",
      "po_box":"123",
      "postal_code":"AA11AA",
      "premises":"1",
      "region":"test region"
    }
  }
}
```

### GET test-only/retrieve-journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.

##### Request:

A valid journeyId must be sent in the URI

##### Response:

Status:

| Expected Response                       | Reason                        |
|-----------------------------------------|-------------------------------|
| ```OK(200)```                           | ```JourneyId exists```        |
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist``` |

Example response body:

```
{
  "companyProfile": {
    "companyName":"TestCompanyLtd”,
    “companyNumber":"01234567",
    "dateOfIncorporation":"2020-01-01",
    "unsanitisedCHROAddress": {
      "address_line_1":"testLine1",
      "address_line_2":"test town",
      "care_of":"test name",
      "country":"United Kingdom",
      "locality":"test city",
      "po_box":"123",
      "postal_code":"AA11AA",
      "premises":"1",
      "region":"test region"
    }
  },
  "ctutr":"1234567890",
  "identifiersMatch":true,
  "businessVerification": {
    "verificationStatus":"PASS"
  },
  "registration": {
    "registrationStatus":"REGISTERED",
    "registeredBusinessPartnerId":"X00000123456789"
  }
}
```

### POST test-only/verification-question/journey

---
Stubs creating a Business Verification journey. The Business Verification Stub Feature Switch will need to be enabled.

##### Request:

No body is required for this request

##### Response:

Status: **Created(201)**

Example Response body:

```
{“redirectUri” : "/testUrl?journeyId=<businessVerificationJourneyId>"}
```

### GET  test-only/verification-question/journey/:journeyId/status

---
Stubs retrieving the result from the Business Verification Service. The Business Verification Stub feature switch will
need to be enabled.

##### Request:

A valid Business Verification journeyId must be sent in the URI

##### Response:

Status: **OK(200)**

Example Response body:

```
{
  "journeyType": "BUSINESS_VERIFICATION",
  "origin": vat,
  "identifier": {
    "ctUtr" -> "1234567890"
  },
  "verificationStatus" -> "PASS"
}
```

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

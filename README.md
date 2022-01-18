
![minSdkVersion](https://img.shields.io/badge/minSdk-23-red.svg)
![compileSdkVersion](https://img.shields.io/badge/compileSdkVersion-30-green.svg)


# CLEAR Mobile Verification SDK for Android

The purpose of the CLEAR Android SDK is to provide properly provisioned partner development teams with the ability to easily integrate CLEAR’s identity verification technology into their own mobile applications. The SDK takes care of both the UI as well as the underlying remote service calls to securely verify a user's identity.

### Support Requirements

* Android 6.0 (API level 23) and above
* [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) 4.1.2 and above
* [AndroidX](https://developer.android.com/jetpack/androidx/)

### Distribution
We provide the SDK as a dependency through Github Package Manager to install follow these steps.

1. Create a Github [PAT](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) and make sure you give read privileges for Github Packages and configure SSO

2. In your project level `build.gradle` you will need to add this code block to authenticate with Github through Gradle.
```kotlin
buildscript {
    google()
    mavenCentral()
    maven {
      url = uri("https://maven.pkg.github.com/clearsecureidentity/clear-android-sdk")
      credentials(HttpHeaderCredentials) {
        name("Authorization")
        value("Bearer <YOUR TOKEN>")
      }
      authentication {
        register("header", HttpHeaderAuthentication)
      }
    }
  }  
```    

3. In your app level `build.gradle` you will need to add the following line: 

```kotlin
implementation('com.clearme.verification.sdk:android:@latest-version')
```

Then sync your project with Gradle and you should see the library resolve. If you have issues with importing the library, sometimes placing the authentication code in the `settings.gradle` will do the trick just **make sure not to commit any credentials by accident to your repository**.

### Signing
When building our SDK into your project it is imperative that you use ONLY `v2` and `v3` [app signing](https://source.android.com/security/apksigning) for Android.

## Getting Started

Before starting, please be sure that you have access to the following credentials:

1. Client ID (provided by CLEAR during onboarding)
2. API Key (provided by CLEAR during onboarding)
2. Device Verification API Key (provided by Google for your app's package identifier)
    - To get this, follow steps 1-5 [here](https://developer.android.com/training/safetynet/attestation#add-api-key)

## 1/ Initialize the SDK

In your app's `Application` class, you should first initialize the SDK with the following parameters:

### Kotlin

```kotlin
ClearSdk.initialize(
    application = this,
    clientId = "CLIENT_ID",
    apiKey = "API_KEY",
    deviceVerificationKey = "API_KEY",
    environment = ClearEnvironment.INTEGRATION
)
```

### Java

```java
ClearSdk.initialize(
    this,
    "CLIENT_ID",
    "API_KEY",
    "DEVICE_VERIFICATION_API_KEY",
    ClearEnvironment.INTEGRATION
);
```

The first parameter should almost always refer to your existing `Application` class, while the next three are values that are either provided to you by CLEAR during onboarding, or as a result of properly configuring your SafetyNet device attestation in the Google Console.

Finally, the environment parameter declares the remote environment that you would like to assign to your build instance. The two possible options are `ClearEnvironment.INTEGRATION`, which should be used for development and debugging purposes, and `ClearEnvironment.PRODUCTION` which should only be used for public releases of your application. It is highly recommended that you assign these environments to correspond to your appropriate build configurations.

## 2/ Place a VerificationView Button in Your Layout

Once initialized, the next step is to place the `VerificationView` on a suitable screen within your app.

### Layout

```xml
<com.clearme.verification.sdk.ui.util.views.VerificationView
    android:id="@+id/verification_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

You will also need to assign a use case to the view, which will declare the intended verification session type you wish to perform. It is possible to place multiple instances within your app, each with a different purpose defined by its corresponding use case.

### View (Activity / Fragment)

#### Kotlin

```kotlin
val verificationView = rootView.findViewById(R.id.verification_view)
verificationView.useCase = VerificationUseCase.VERIFY_WITH
```

#### Java

```java
VerificationView verificationView = findViewById(R.id.verification_view);
verificationView.setUseCase(VerificationUseCase.VERIFY_WITH);
```

| Use Case      | Purpose                                               |
|---------------|-------------------------------------------------------|
| `VERIFY_WITH` | Member identity verification and data sharing consent |
| `SIGNUP_WITH` | Partner authorization for account creation            |
| `LOGIN_WITH`  | Partner authorization for login                       |
| `ACCESS_WITH` | Venue and event admission using CLEAR                 |
| `PAY_WITH`    | Pay using the card on file with CLEAR                 |

## 3/ Handle Button Interaction to Begin a Session

In order to start a verification flow, use `VerificationView`'s `OnButtonClickListener`:

### Kotlin #1

```kotlin
 private val activityResultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult(), this::analyzeActivityResult
  )
  
  val verifyButton = binding.verificationView
    verifyButton.apply {
      useCase = VERIFY_WITH
      setOnButtonClickListener { launchClearVerification() }
    }
  
    private fun launchClearVerification() {
    val verificationIntent = ClearSdk.createIdentityVerificationIntent(this, IdentifierType.Email())
    activityResultLauncher.launch(verificationIntent)
  }
```  
  
### Kotlin #2

```kotlin
companion object {
    private const val VERIFICATION_REQUEST_CODE = 123
}

...

val verificationIntent = ClearSdk.createIdentityVerificationIntent(
    context: Context,
    identifierType: IdentifierType
)

verificationView.setOnButtonClickListener {
    startActivityForResult(verificationIntent, VERIFICATION_REQUEST_CODE)
}
```

### Java

```java
private final int VERIFICATION_REQUEST_CODE = 123;

...

IdentifierType identifierType = new IdentifierType.Email();
Intent intent = ClearSdk.createIdentityVerificationIntent(this, identifierType);

verificationView.setOnButtonClickListener(() -> startActivityForResult(intent, VERIFICATION_REQUEST_CODE));
```

### Note

`createIdentityVerificationIntent`'s `identifierType` is used to configure what data you want to pass into the SDK. The parameter also allows you to configure what flow you want the SDK to function in.

The following options are available to you:

| IdentifierType.*                                                  | SDK Flow                                                                                                                                    | Notes                                                                                                                                                                                                   |
|-------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Email()`                                                         | Starts the SDK on the Email Capture screen with an empty email field                                                                        | -                                                                                                                                                                                                       |
| `Email(email = "hi@clearme.com")`                                 | Starts the SDK on the Email Capture screen with the email field pre-filled with "hi@clearme.com"                                            | -                                                                                                                                                                                                       |
| `Email(email = "hi@clearme.com", flowType = FlowType.STATIC)`     | Starts the SDK on the Email Capture screen with the email field pre-filled and locked with "hi@clearme.com"                                 | The email address you pass in here should be a valid email address. If the email address is invalid, the SDK will throw the `EmailValidationException` which you can choose to handle however you like. |
| `Email(email = "hi@clearme.com", flowType = FlowType.SUPPRESSED)` | Starts the SDK directly on the Face Capture screen with the email set to "hi@clearme.com" in the background                                 | The email address you pass in here should be a valid email address. If the email address is invalid, the SDK will throw the `EmailValidationException` which you can choose to handle however you like. |
| `MemberAsid(asid = "ASID")`                                       | Starts the SDK directly on the Face Capture screen and uses a returning user's Member ASID for verification instead of their email address. | Used to directly pass-in a previously stored Member ASID into the SDK for faster verification.                                                                                                          |

## 4/ Handle the Result of an Identity Verification Session

The Clear Android SDK relies on the Android framework's trusty `onActivityResult` callback to provide results back to your app. In order to wire up the callback, use the following code snippet:

### Kotlin #1
```kotlin
private fun analyzeActivityResult(activityResult: ActivityResult) {
    when (activityResult.resultCode) {
      RESULT_OK ->  {
        // Manage Success State
      }
      RESULT_CANCELED -> {}
      RESULT_FAILED_ASSURANCE -> {}
      RESULT_ACCOUNT_LOCKED -> {}
    }
  }
 ```

### Kotlin #2

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
        VERIFICATION_REQUEST_CODE -> {
            when (resultCode) {
                RESULT_OK -> {
                    val verificationResult = data?.getParcelableExtra<VerificationResult>(ClearSdk.EXTRA_VERIFICATION_RESULT)
                }
                RESULT_CANCELED -> {}
                RESULT_ACCOUNT_LOCKED -> {}
                RESULT_FAILED_ASSURANCE -> {}
            }
        }
    }
}
```

### Java

```java
@Override
protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
        case RESULT_OK:
            VerificationResult verificationResult = data.getParcelableExtra(ClearSdk.EXTRA_VERIFICATION_RESULT);
            break;
        case RESULT_CANCELED:
            break;
        case ClearSdk.RESULT_ACCOUNT_LOCKED:
            break;
        case ClearSdk.RESULT_FAILED_ASSURANCE:
            break;
    }
}
```

| Result Code | Description |
| ------- | --------------------- |
| `RESULT_OK`  | Successful verification. SDK returns back a Member Verification Token that is accessible as part of the returned `Intent` object.     |
| `RESULT_CANCELED`  | Verification process not completed. User abandoned the SDK.       |
| `RESULT_ACCOUNT_LOCKED`  | Unsuccessful verification due to user's account being locked.     |
| `RESULT_FAILED_ASSURANCE`  | Unsuccessful verification due to user failing to provide consent to share data.      |

On `RESULT_OK`, you receive back a `VerificationResult` object which can you used further to communicate with backend services on your end.

### Kotlin

```kotlin
// Code that can be exchanged for an access token
verificationResult.authCode

// App scoped member identifier
verificationResult.memberAsid
```

### Java

```java
// Code that can be exchanged for an access token
verificationResult.getAuthCode()

// App scoped member identifier
verificationResult.getMemberAsid()
```




![minSdkVersion](https://img.shields.io/badge/minSdk-21-blue.svg)
![compileSdkVersion](https://img.shields.io/badge/compileSdkVersion-32-brightgreen.svg)
![Release](https://img.shields.io/badge/Library%20Version-1.0.0-blueviolet)

# CLEAR SDK for Android

> This is CONFIDENTIAL information intended only for verifiable partners of CLEAR

The CLEAR Android SDK provides partner development teams with the ability to easily integrate CLEAR’s identity verification technology into their own mobile applications. 

## Requirements

* Android 5.0 (API level 21) and above
* Android Browser API ([androidx.browser:browser:1.4.0](https://developer.android.com/jetpack/androidx/releases/browser)) (Transitive dependency)

## Getting Started

To set up a partnership, reach out to developersupport@clearme.com. 

Before starting, please be sure that you have access to the following configuration values:

1. Client ID (provided by CLEAR during onboarding)
2. Scope (provided by CLEAR during onboarding)
3. RedirectURI (see instructions below)

### Choosing your Redirect URI

The RedirectURI is a "deep link" to your application that will be used to supply the application with an authorization code after the completion of the CLEAR member verification session. As part of onboarding, you should provide CLEAR with a redirect URI for your application(s).

There are two approaches you can use when implementing a redirect URI: A custom URL scheme or Android App Links.

<details>
  <summary>Using a Custom Scheme</summary>

The most straight forward approach is to define a redirect URI using a [custom scheme](https://developer.mozilla.org/en-US/docs/Learn/Common_questions/What_is_a_URL#scheme). We recommend using your application's `applicationId` as the scheme and for the domain name using "auth" (eg. `com.example.yourapp://auth`). Note that it's important to use a unique URI scheme for your application to prevent the possibility of a "disambiguation dialog" from appearing in the case where two applications installed on the device happen to use the same scheme.

</details><br/>

<details>
  <summary>Using an Android App Link</summary>

[App links](https://developer.android.com/training/app-links) are supported, but require additional set up that is out of scope for this document. Note that if you do choose to use an app link, it is recommended that your application have a minimum API level of 23. 

</details><br/>

Whether or not you are using a custom scheme or App Link, it's important that you pre-register your application's URI with CLEAR during onboarding before proceeding. 

## Usage

After establishing your partner configuration during onboarding, you can integrate the SDK into your Android Application with the following 3 steps.

### 1. Install the SDK

In your application's `gradle.build` file, make the following changes:

* Add the CLEAR maven repository 
    ```
    repositories {
        maven { url 'https://raw.githubusercontent.com/clearsecureidentity/clear-android-sdk/master/maven/' }
    }
    ```
* Add `manifestPlaceholders` to `defaultConfig`
    ```groovy
    defaultConfig {
        applicationId "com.example.yourapp"
        minSdk 21
        targetSdk 32
        versionCode 1
        versionName "1.0"

        manifestPlaceholders = [clearRedirectUriHost: "auth", clearRedirectUriScheme: applicationId]
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    ```
    The `clearRedirectUriHost` and `clearRedirectUriScheme` values are taken from the RedirectURI that was pre-registered during onboarding. In this example we are using the recommended values of "auth" and `applicationId` for the domain and scheme but these values may vary depending on your partner configuration.
* Add a dependency to the current version of the CLEAR SDK
    ```
    dependencies {
      implementation('com.clearme.sdk:clearsdk:1.0.0')
    }
    ```


### 2. Initialize the SDK

Before using the SDK in your application, you will need to initialize it with the following parameters:

```kotlin

import com.clearme.sdk.CLEAR
import com.clearme.sdk.CLEAREnvironment.*

CLEAR.initialize(
    clientId = "00000000-00000000-00000000-00000000",     // Your partner client id, provided during onboarding
    environment = Production,                             // `Production` or `Integration`
    redirectURI = "your.custom.scheme://auth",            // Your redirect URI (See above for description)
    scope = "SCOPE"                                       // Your partner scope, provided during onboarding
)

```

* **clientId** and **scope**: These values are provided by CLEAR during onboarding.
 * **environment**: The environment parameter declares the remote environment that will be used. The two possible options are `Integration`, which should be used for development and debugging purposes, and `Production` which should only be used for public releases of your application. It is recommended that you assign these environments to correspond to your appropriate build configurations.
 * **redirectURI**: This is a redirect URI that corresponds to a pre-registered value that was defined during onboarding. See "Choosing your RedirectURI" above for details.


### 3. Integrate the SDK in your Application code

Once initialized, the next step is to place the `VerifyWithClearButton` on a suitable screen within your app.

Example layout:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffff"
    tools:context=".MainActivity">

    <com.clearme.sdk.ui.VerifyWithClearButton
        android:id="@+id/clearbtn"
        android:layout_width="match_parent"
        android:layout_height="@dimen/clearme_btn_height_default"
        android:layout_marginBottom="30dp"
        android:gravity="center"
        app:layout_constraintWidth_max="@dimen/clearme_btn_width_default"
        app:layout_constraintWidth_min="@dimen/clearme_btn_width_min"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>

```

This will produce a button that is the default size of 327 x 56 dpi. Note that a smaller size button can be configured by using the dimensions `@dimen/clearme_btn_width_small` and `@dimen/clearme_btn_height_small` (236 x 40 dpi). Using a `ConstraintLayout` layout as demonstrated above is the perferred approach but if you require an alternative button size or layout, you may use custom values that align with your design.

Once the button has been included in your layout, the `setOnButtonClickListener` should be wired up to the `CLEAR.verify` method. 

A full kotlin implementation of the integration from a standalone activity is shown in the example below:

```kotlin
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.clearme.sdk.CLEAR
import com.clearme.sdk.CLEAR.Callback
import com.clearme.sdk.CLEAR.ErrorInfo
import com.clearme.sdk.CLEAR.VerificationResult
import com.clearme.sdk.CLEAR.VerificationResult.Cancelled
import com.clearme.sdk.CLEAR.VerificationResult.Completed
import com.clearme.sdk.CLEAR.VerificationResult.Failure
import com.clearme.sdk.CLEAREnvironment.Integration
import com.clearme.sdk.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Typically this would be done in your app initialization but can be anywhere in your code as long is it happens before `verify` is called.
    CLEAR.initialize(
      clientId = "your-client-id-here",
      environment = Integration,
      redirectURI = "com.example.app://auth",
      scope = "your-scope-here"
    )

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.clearbtn.setOnButtonClickListener {
      CLEAR.verify(this, object : Callback {
        override fun onComplete(result: VerificationResult) {

          when (result) {
            Cancelled -> Unit // Typically you will not need to perform any action on cancel but need to reset UI state 
            is Completed -> sendAuthCodeToBackend(result.authorizationCode, binding.clearbtn)
            is Failure -> logError(result.info)
          }

        }
      })
    }
  }

  private fun logError(info: ErrorInfo) {
    // Session ID and Loggable reason should be logged and may be used for triage with CLEAR
    Log.e("CLEARSDK", "Error from sdk with reason ${info.loggableReason} sessionId: ${info.sessionId}")
  }

  private fun sendAuthCodeToBackend(authorizationCode: String, view: View) {
    // Fill in with your backend end integration
  }
}

```



package com.example.sample_app

import android.app.Application
import com.clearme.verification.sdk.ClearEnvironment.INTEGRATION
import com.clearme.verification.sdk.ClearSdk

class SampleApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    ClearSdk.initialize(
      application = this,
      clientId = "YOUR CLIENT ID",
      apiKey = "YOUR API KEY" ,
      deviceVerificationKey = "YOUR DEVICE VERIFICATION KEY",
      environment = INTEGRATION
    )
  }
}
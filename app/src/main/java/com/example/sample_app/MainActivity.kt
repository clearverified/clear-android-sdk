package com.example.sample_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.clearme.verification.sdk.ClearSdk
import com.clearme.verification.sdk.ClearSdk.RESULT_ACCOUNT_LOCKED
import com.clearme.verification.sdk.ClearSdk.RESULT_FAILED_ASSURANCE
import com.clearme.verification.sdk.data.model.IdentifierType
import com.clearme.verification.sdk.data.model.VerificationResult
import com.clearme.verification.sdk.data.model.VerificationUseCase.VERIFY_WITH
import com.example.sample_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  companion object {
    private const val VERIFICATION_REQUEST_CODE = 123
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val verificationView = binding.verifcationView
    verificationView.useCase = VERIFY_WITH

    val type = IdentifierType.Email()
    val verificationIntent = ClearSdk.createIdentityVerificationIntent(
      this, type
    )

    verificationView.setOnButtonClickListener {
      startActivityForResult(
        verificationIntent,
        VERIFICATION_REQUEST_CODE
      )
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      VERIFICATION_REQUEST_CODE -> {
        when (resultCode) {
          RESULT_OK -> {
            val verificationResult =
              data?.getParcelableExtra<VerificationResult>(ClearSdk.EXTRA_VERIFICATION_RESULT)
          }
          RESULT_CANCELED -> {
          }
          RESULT_ACCOUNT_LOCKED -> {
          }
          RESULT_FAILED_ASSURANCE -> {
          }
        }
      }
    }
  }
}
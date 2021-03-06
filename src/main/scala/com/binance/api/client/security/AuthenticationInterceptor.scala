package com.binance.api.client.security

import java.io.IOException

import com.binance.api.client.constant.BinanceApiConstants
import okhttp3._

class AuthenticationInterceptor(val apiKey: String, val secret: String) extends Interceptor {
  @throws[IOException]
  override def intercept(chain: Interceptor.Chain): Response = {
    val original            = chain.request
    val newRequestBuilder   = original.newBuilder
    val isApiKeyRequired    = original.header(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_APIKEY) != null
    val isSignatureRequired = original.header(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED) != null
    newRequestBuilder
      .removeHeader(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED)
      .removeHeader(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED)
    // Endpoint requires sending a valid API-KEY
    if (isApiKeyRequired || isSignatureRequired) newRequestBuilder.addHeader(BinanceApiConstants.API_KEY_HEADER, apiKey)
    // Endpoint requires signing the payload
    if (isSignatureRequired) {
      val payload = original.url.query
      if (payload.nonEmpty) {
        val signature = HmacSHA256Signer(payload, secret)
        val signedUrl = original.url.newBuilder.addQueryParameter("signature", signature).build
        newRequestBuilder.url(signedUrl)
      }
    }
    // Build new request after adding the necessary authentication information
    val newRequest = newRequestBuilder.build
    chain.proceed(newRequest)
  }
}

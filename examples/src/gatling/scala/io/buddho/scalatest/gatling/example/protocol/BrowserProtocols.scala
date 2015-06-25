package io.buddho.scalatest.gatling.example.protocol

import io.gatling.http.config.{HttpProtocol, HttpProtocolBuilder}
import io.gatling.core.Predef._

object BrowserProtocols {

  private lazy val baseBrowser = new HttpProtocolBuilder(HttpProtocol.DefaultHttpProtocol)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip")
    .connection("keep-alive")

  lazy val chromeBrowser:HttpProtocolBuilder = baseBrowser
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.110 Safari/537.36")

}

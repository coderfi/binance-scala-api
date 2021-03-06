package com.binance.api.client.impl

import java.io.{Closeable, IOException}

import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.constant.BinanceApiConstants
import com.binance.api.client.domain.Symbol
import com.binance.api.client.domain.event._
import com.binance.api.client.domain.market.CandlestickInterval
import com.binance.api.client.json.Decoders._
import okhttp3.{OkHttpClient, Request}

/**
  * Binance API WebSocket client implementation using OkHttp.
  */
class BinanceApiWebSocketClientImpl() extends BinanceApiWebSocketClient with Closeable {
  private val client = new OkHttpClient

  override def onDepthEvent(symbol: Symbol)(callback: DepthEvent => Unit): Unit =
    createNewWebSocket(s"${symbol.value.toLowerCase}@depth", new BinanceApiWebSocketListener[DepthEvent](callback))

  override def onCandlestickEvent(symbol:   Symbol,
                                  interval: CandlestickInterval)(callback: CandlestickEvent => Unit): Unit =
    createNewWebSocket(s"${symbol.value.toLowerCase}@kline_${interval.getIntervalId}",
                       new BinanceApiWebSocketListener[CandlestickEvent](callback))

  override def onAggTradeEvent(symbol: Symbol)(callback: AggTradeEvent => Unit): Unit =
    createNewWebSocket(s"${symbol.value.toLowerCase}@aggTrade",
                       new BinanceApiWebSocketListener[AggTradeEvent](callback))

  override def onTradeEvent(symbol: Symbol)(callback: TradeEvent => Unit): Unit =
    createNewWebSocket(s"${symbol.value.toLowerCase}@trade",
                       new BinanceApiWebSocketListener[TradeEvent](callback))

  def onUserDataUpdateEvent(
      listenKey: ListenKey
  )(callback:    Either[OrderTradeUpdateEvent, AccountUpdateEvent] => Unit): Unit =
    createNewWebSocket(listenKey.listenKey, new BinanceApiWebSocketListener(callback))

  private def createNewWebSocket[T](channel: String, listener: BinanceApiWebSocketListener[T]) =
    client.newWebSocket(
      new Request.Builder()
        .url(s"${BinanceApiConstants.WS_API_BASE_URL}/$channel")
        .build,
      listener
    )

  @throws[IOException]
  override def close(): Unit = client.dispatcher.executorService.shutdown()
}

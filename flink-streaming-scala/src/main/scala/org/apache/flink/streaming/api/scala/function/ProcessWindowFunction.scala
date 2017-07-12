/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.api.scala.function

import java.io.Serializable

import org.apache.flink.annotation.PublicEvolving
import org.apache.flink.api.common.functions.AbstractRichFunction
import org.apache.flink.api.common.state.KeyedStateStore
import org.apache.flink.streaming.api.TimeDomain
import org.apache.flink.streaming.api.windowing.windows.Window
import org.apache.flink.util.Collector

/**
  * Base abstract class for functions that are evaluated over keyed (grouped)
  * windows using a context for retrieving extra information.
  *
  * @tparam IN The type of the input value.
  * @tparam OUT The type of the output value.
  * @tparam KEY The type of the key.
  * @tparam W The type of the window.
  */
@PublicEvolving
abstract class ProcessWindowFunction[IN, OUT, KEY, W <: Window]
    extends AbstractRichFunction {

  /**
    * Evaluates the window and outputs none or several elements.
    *
    * @param key      The key for which this window is evaluated.
    * @param context  The context in which the window is being evaluated.
    * @param elements The elements in the window being evaluated.
    * @param out      A collector for emitting elements.
    * @throws Exception The function may throw exceptions to fail the program and trigger recovery.
    */
  @throws[Exception]
  def process(key: KEY, context: Context, elements: Iterable[IN], out: Collector[OUT])

  /**
    * Called when a timer set using [[Context]] fires.
    * @param time The timestamp of the firing timer
    * @param context An [[OnTimerContext]] that allows querying the [[TimeDomain]] of the
    *                the firing timer, and allows registering timers.
    * @param out A collector for emitting elements
    */
  @throws[Exception]
  def onTimer(time: Long, context: OnTimerContext, out: Collector[OUT]) {}

  /**
    * Deletes any state in the [[Context]] when the Window is purged.
    *
    * @param context The context to which the window is being evaluated
    * @throws Exception The function may throw exceptions to fail the program and trigger recovery.
    */
  @throws[Exception]
  def clear(context: Context) {}

  /**
    * The context holding window metadata
    */
  abstract class Context {
    /**
      * Returns the window that is being evaluated.
      */
    def window: W

    /**
      * Returns the current processing time.
      */
    def currentProcessingTime: Long

    /**
      * Returns the current event-time watermark.
      */
    def currentWatermark: Long

    /**
      * Registers a timer to be fired when the event time watermark passes the given time.
      */
    def registerEventTimeTimer(time: Long)

    /**
      * Registers a timer to be fired when processing time passes the given time.
      */
    def registerProcessingTimeTimer(time: Long)

    /**
      * State accessor for per-key and per-window state.
      */
    def windowState: KeyedStateStore

    /**
      * State accessor for per-key global state.
      */
    def globalState: KeyedStateStore
  }


  abstract class OnTimerContext extends Context {

    /**
      * The {@link TimeDomain} of the firing timer.
      */
    def timeDomain: TimeDomain

  }

}

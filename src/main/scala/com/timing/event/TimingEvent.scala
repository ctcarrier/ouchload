package com.timing.event

import java.util.UUID

/**
 * @author chris_carrier
 * @version 12/7/11
 */

sealed trait TimingEventType
object Stat extends TimingEventType
object Init extends TimingEventType
object Start extends TimingEventType
object Stop extends TimingEventType

case class TimingEvent(eventType: TimingEventType, key: Option[String], timestamp: Long, uuid: Option[UUID])
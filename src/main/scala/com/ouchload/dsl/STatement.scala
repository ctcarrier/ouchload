package com.ouchload.dsl

/**
 * @author chris_carrier
 * @version 12/11/11
 */


sealed abstract class Statement

case class Url(url: String) extends Statement
case class Connections(connectionCount: Int) extends Statement
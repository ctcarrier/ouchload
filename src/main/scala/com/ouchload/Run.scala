package com.ouchload

import akka.actor.Actor
import util.parsing.input.CharArrayReader
import dsl._

/**
 * @author chris_carrier
 * @version 12/2/11
 */


object Run {

  def main(args: Array[String]) {

    //val e = new Executor()

    val dsl = """
    load "www.example.com" using 50 connections,
    load "www.google.com" using 1 connections
    """

  LoadingDSL.parse(dsl)

    //Thread.sleep(10000)

    Actor.registry.shutdownAll
    HttpRegistry.http.shutdown

  }
}
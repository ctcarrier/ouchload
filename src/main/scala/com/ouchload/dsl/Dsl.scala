package com.ouchload.dsl

import com.ouchload._
import job.{NewState, LoaderTask, LoaderJob}

/**
 * @author chris_carrier
 * @version 12/9/11
 */


class Dsl {

}

case class LoadCommand(loadCommand: String)

import scala.util.parsing.combinator.syntactical._

object LoadingDSL extends StandardTokenParsers {

  lexical.delimiters ++= List("(", ")", ",")
  lexical.reserved += ("load", "using", "connections")

  def instr: Parser[List[List[Statement]]] =
    rep1sep(loadSpec, ",") ^^ { case li: List[List[Statement]] => li }

  def loadSpec: Parser[List[Statement]] =
    "load" ~> stringLit ~ usingSpec ^^ { case url ~ cs => List(Url(url), cs) }

  def usingSpec: Parser[Statement] =
    "using" ~> connectionSpec ^^ { case cs => cs }

  def connectionSpec: Parser[Statement] =
    numericLit <~ "connections" ^^ { case n => Connections(n.toInt) }


  def parse(s: String) = {

    val result: Either[String, LoaderTask] = instr(new lexical.Scanner(s)) match {
      case success: Success[List[List[Statement]]] => {
        val newJobs: List[LoaderJob] = success.result.map(x => new Interpreter(x).run())
        Right(LoaderTask(jobs = newJobs, state = "new"))
      }
      case Failure(msg, _) => Left(msg)
      case Error(msg, _) => Left(msg)
    }
    result
  }
}
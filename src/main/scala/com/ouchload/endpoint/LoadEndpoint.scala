package com.ouchload.endpoint

import org.bson.types.ObjectId
import akka.event.EventHandler
import cc.spray.http._
import HttpHeaders._
import StatusCodes._
import MediaTypes._
import net.liftweb.json.Serialization._
import net.liftweb.json.DefaultFormats
import cc.spray._
import akka.dispatch.Future
import cc.spray.caching._
import com.ouchload.service.LoadService
import com.ouchload.liftjson.ObjectIdSerializer
import typeconversion.LiftJsonSupport
import com.ouchload.dsl.LoadingDSL
import com.ouchload.auth.FromMongoUserPassAuthenticator
import com.ouchload.response.ErrorResponse
import utils.Logging
import com.ouchload.job.{LoaderTask, LoaderJob}
import com.ouchload.exception.BadLoadDslException
import com.ouchload.dsl.{LoadCommand, LoadingDSL}

/**
 * @author chris carrier
 */

trait LoadEndpoint extends Directives with LiftJsonSupport with Logging {
  implicit val formats = DefaultFormats + new ObjectIdSerializer

  final val NOT_FOUND_MESSAGE = "resource.notFound"
  final val INTERNAL_ERROR_MESSAGE = "error"

  def JsonContent(content: String) = HttpContent(ContentType(`application/json`), content)

  EventHandler.info(this, "Starting actor.")
  val service: LoadService

  def withErrorHandling(ctx: RequestContext)(f: Future[_]): Future[_] = {
    f.onTimeout(f => {
      ctx.fail(StatusCodes.InternalServerError, ErrorResponse(1, ctx.request.path, List("Internal error.")))
      EventHandler.info(this, "Timed out")
    }).onException {
      case e => {
        EventHandler.info(this, "Excepted: " + e)
        ctx.fail(StatusCodes.InternalServerError, ErrorResponse(1, ctx.request.path, List(e.getMessage)))
      }
    }
  }

  def withSuccessCallback(ctx: RequestContext, statusCode: StatusCode = OK)(f: Future[_]): Future[_] = {
    f.onComplete(f => {
      f.result.get match {
        case lt: Option[LoaderTask] => ctx.complete(statusCode, lt)
        case _ => ctx.fail(StatusCodes.NotFound, ErrorResponse(1l, ctx.request.path, List(NOT_FOUND_MESSAGE)))
      }
    })
  }

  //directive compositions
  val objectIdPathMatch = path("^[a-f0-9]+$".r)
  //val directGetProject = authenticate(httpMongo(realm = "mycotrack")) & get
  val directGet = get
  val putProject = content(as[LoadCommand]) & put
  val postProject = path("") & content(as[LoadCommand]) & post

  val restService = {
    // Debugging: /ping -> pong
    path("ping") {
        get {
          _.complete("pong " + new java.util.Date())
      }
    } ~
      // Service implementation.
      pathPrefix("api" / "loaders") {
        objectIdPathMatch {
          resourceId =>
              directGet {
                ctx =>
                  "OK"
              }

        } ~
          postProject {
            resource => ctx =>
              withErrorHandling(ctx) {
                withSuccessCallback(ctx, Created) {
                  Future {
                    log.info("Got a request")
                    val result: Either[String, LoaderTask] = LoadingDSL.parse(resource.loadCommand)
                    result match {
                      case Right(lt) => service.saveTask(lt)
                      case Left(msg) => throw new BadLoadDslException(msg)
                    }
                  }

                }
              }


          } ~
          path("debug") {
            post { ctx =>
              log.info("Got a request " + new String(ctx.request.content.get.buffer))
              ctx.complete(new String(ctx.request.content.get.buffer))
            }
          }
      }


  }

  def httpMongo[U](realm: String = "Secured Resource",
                   authenticator: UserPassAuthenticator[U] = FromMongoUserPassAuthenticator)
  : BasicHttpAuthenticator[U] =
    new BasicHttpAuthenticator[U](realm, authenticator)


}
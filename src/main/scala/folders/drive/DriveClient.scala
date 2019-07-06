package folders.drive

import java.util.UUID

import folders.drive.commands.Command
import scalaj.http.{Http, HttpResponse}
import com.typesafe.config.Config

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

// It's not cool to wrap blocking calls inside futures but since this project is never going to have that much traffic
// this is ok.
trait DriveClient {
  import DriveClient._

  protected implicit def ec: ExecutionContext
  def params: Parameters

  def oauthUrl(state: String): String = s"${params.authEndpoint}?" +
    s"client_id=${params.clientId}&scope=${params.scope}&state=$state&" +
    s"redirect_uri=${params.redirectUri}&response_type=code"

  def accessToken(code: String): Future[HttpResponse[String]] = Future {
    Http(params.accessTokenEndpoint)
      .param("client_id", params.clientId)
      .param("client_secret", params.clientSecret)
      .param("code", code)
      .param("redirect_uri", params.redirectUri)
      .param("grant_type", "authorization_code")
      .postForm
      .asString
  }

  def submitCommand(token: String)(command: Command): Future[HttpResponse[String]] =
    Future {
      Http(params.resourcesEndpoint + command.path)
        .param("access_token", token)
        .header("Content-Type", "application/json")
        .postData(command.payload.toString)
        .asString
    }

  def submitCommandBatch(token: String)(commands: Command*): Future[HttpResponse[String]] =
    Future {
      val batchId       = UUID.randomUUID().toString.replace("-", "")
      val startBoundary = s"--$batchId"
      val endBoundary   = s"\n--$batchId--"

      def makePart(cmd: Command): String =
        s"""
           |Content-Type: application/http
           |
           |POST ${cmd.path} HTTP/1.1
           |Content-Type: application/json
           |accept: application/json
           |content-length: ${cmd.payload.toString.length}
           |
           |${cmd.payload.toString}""".stripMargin

      def makeBody =
        startBoundary +
        commands.map(makePart).mkString(s"\n$startBoundary") +
        endBoundary

      Http(params.resourcesEndpoint + "/batch")
        .header("Authorization", s"Bearer $token")
        .header("Content-Type", s"""multipart/mixed; boundary=$batchId""")
        .postData(makeBody)
        .asString
    }
}

object DriveClient {
  case class Parameters(clientId: String,
                        clientSecret: String,
                        resourcesEndpoint: String,
                        authEndpoint: String,
                        accessTokenEndpoint: String,
                        redirectUri: String,
                        scope: String)
}

object Client {
  import DriveClient.Parameters

  def apply(conf: Config)(implicit exec: ExecutionContext): DriveClient =
    new DriveClient {

      protected implicit def ec: ExecutionContext = exec

      def params: Parameters =
        Parameters(
          clientId            = conf.getString("drive.credentials.clientId") ,
          clientSecret        = conf.getString("drive.credentials.clientSecret"),
          resourcesEndpoint   = conf.getString("drive.endpoints.resources"),
          authEndpoint        = conf.getString("drive.endpoints.auth"),
          accessTokenEndpoint = conf.getString("drive.endpoints.accessToken"),
          redirectUri         = conf.getString("drive.endpoints.redirect"),
          scope               = conf.getString("drive.scope")
        )
    }
}

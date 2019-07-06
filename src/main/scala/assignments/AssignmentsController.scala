package assignments

import skinny.micro.response.{ActionResult, BadRequest, PermanentRedirect, ServiceUnavailable}
import skinny.micro.contrib.csrf.CSRFTokenGenerator
import skinny.micro.cookie.Cookie
import common.Constants
import org.joda.time.DateTime
import assignments.todoist.TodoistClient

import scala.concurrent.{ExecutionContext, Future}

class AssignmentsController(client: TodoistClient)
                           (implicit ec: ExecutionContext) {

  def oauthRedirect(assignments: Seq[String],
                    subjectName: String): Future[ActionResult] = {
    val csrf    = CSRFTokenGenerator()
    val request = AssignmentsRequest(subjectName, assignments.map(makeAssignment), csrf)

    AssignmentsRequestCache
      .save(request)
      .map { cacheId =>
        PermanentRedirect(
          location = client.oauthUrl(csrf),
          cookies  = Seq(Cookie(Constants.RequestIdHeader, cacheId)),
          headers  = Map("Access-Control-Allow-Origin" -> "*")
        )
      }
  }

  def oauthCallback(oauthCode: String,
                    csrf: String,
                    requestId: String): Future[ActionResult] = {
    AssignmentsRequestCache
      .retrieve(requestId)
      .flatMap {
        case Some(AssignmentsRequest(subjectName, assignments, csrfCode))
          if csrfCode == csrf && assignments.nonEmpty =>
          new AssignmentsServiceClient(client, oauthCode)
            .createAssignments(assignments, subjectName)
            .map {
              case true  => PermanentRedirect("https://todoist.com/")
              case false => ServiceUnavailable("Something went wrong.")
            }
        case None =>
          val response =
            BadRequest("Invalid request sequence/Cache expired/Bad CSRF/No assignments to add.")
          Future.successful(response)
      }
  }

  private def makeAssignment(s: String): Assignment = {
    val components = s.split('|')
    Assignment(components(0), DateTime.parse(components(1)))
  }
}

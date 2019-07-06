package assignments

import assignments.todoist.TodoistClient
import assignments.todoist.api.Resources
import assignments.todoist.commands.{AddItemCommand, AddProjectCommand, Command, NopCommand}
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.HttpResponse

trait AssignmentsService {
  def createAssignments(assignments: Seq[Assignment],
                        subjectName: String): Future[Boolean]
}

class AssignmentsServiceClient(todoistClient: TodoistClient,
                               oauthCode: String)
                              (implicit ec: ExecutionContext) extends AssignmentsService {

  def createAssignments(assignments: Seq[Assignment],
                        subjectName: String): Future[Boolean] =
    for {
      tokenRequestResponse    <- todoistClient.accessToken(oauthCode)
      token                   = extractToken(tokenRequestResponse)
      projectsRequestResponse <- fetchProjects(token)
      projects                = extractProjects(projectsRequestResponse)
      projectId               = findProjectId(projects, subjectName)
      todoist                 = todoistClient.submitCommands(token) _
      createAssignmentsInProjectCommands =
        makeCreateAssignmentsInProjectCommands(
          projectId,
          subjectName,
          assignments
        )
      createAssignmentsInProjectRequestResponse <- todoist(createAssignmentsInProjectCommands)
    } yield createAssignmentsInProjectRequestResponse.isSuccess

  private def makeCreateAssignmentsInProjectCommands(projectId: Option[String],
                                                     subjectName: String,
                                                     assignments: Seq[Assignment]): Seq[Command] =
    projectId match {
      case Some(id) =>
        val assignmentsToCreateCommands = makeCreateAssignmentsCommands(assignments, id)
        NopCommand +: assignmentsToCreateCommands
      case None =>
        val projectToCreateCommand = AddProjectCommand(subjectName, color = Some(4)) // 4 is blue
        val projectId = projectToCreateCommand.tempId.toString
        val assignmentsToCreate = makeCreateAssignmentsCommands(assignments, projectId)
        projectToCreateCommand +: assignmentsToCreate
    }

  private def fetchProjects(token: String): Future[HttpResponse[String]] =
    todoistClient.fetchResourceDetails(token)(None, Resources.Projects)

  private def findProjectId(projects: Seq[Project], name: String): Option[String] =
    projects
      .find(_.name == name)
      .map(_.id.toString)

  private def extractProjects(response: HttpResponse[String]): Seq[Project] =
    (Json.parse(response.body) \ "projects").as[Seq[JsValue]]
      .map { project =>
        val name = (project \ "name").as[String]
        val id   = (project \ "id").as[Long]
        Project(name, id)
      }

  private def extractToken(response: HttpResponse[String]): String =
    (Json.parse(response.body) \ "access_token").as[String]

  private def makeCreateAssignmentsCommands(assignments: Seq[Assignment],
                                      projectId: String): Seq[Command] =
    assignments.map { case Assignment(name, dueDate) =>
      AddItemCommand(name, Some(dueDate), Some(projectId))
    }

  private case class Project(name: String, id: Long)
}

case class Assignment(name: String, dueDate: DateTime)

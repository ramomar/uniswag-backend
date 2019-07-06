package assignments

import common.RequestsCache
import org.joda.time.DateTime

case class AssignmentsRequest(subjectName: String,
                              assignments: Seq[Assignment],
                              csrf: String)

object AssignmentsRequestCache extends RequestsCache[AssignmentsRequest]

package folders

import common.RequestsCache

case class FoldersRequest(parentFolderName: String,
                          folders: Seq[String],
                          csrf: String)

object FoldersRequestCache extends RequestsCache[FoldersRequest]

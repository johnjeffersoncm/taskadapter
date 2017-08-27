package com.taskadapter.connector.definition

case class SaveResult(targetFileAbsolutePath: String, updatedTasksNumber: Int, createdTasksNumber: Int,
                      keyToRemoteKeyList: Seq[(TaskId, TaskId)],
                      generalErrors: Seq[Throwable],
                      taskErrors: Seq[TaskError[Throwable]]) {

  def getRemoteKeys: Seq[TaskId] = keyToRemoteKeyList.map(_._2)

  def hasErrors: Boolean = generalErrors.nonEmpty || taskErrors.nonEmpty
}

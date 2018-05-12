package com.taskadapter.connector.redmine

import java.util

import com.taskadapter.connector.{FieldRow, NewConnector}
import com.taskadapter.connector.common.TaskSavingUtils
import com.taskadapter.connector.definition._
import com.taskadapter.core.PreviouslyCreatedTasksResolver
import com.taskadapter.model.GTask
import com.taskadapter.redmineapi.{Include, RedmineException, RedmineManager}
import com.taskadapter.redmineapi.bean._

import scala.collection.JavaConverters._

object RedmineConnector {
  /**
    * Keep it the same to enable backward compatibility for previously created config files.
    */
  val ID = "Redmine"

  @throws[RedmineException]
  private def loadPriorities(rows: java.lang.Iterable[FieldRow], mgr: RedmineManager): util.Map[String, Integer] = {
    if (FieldRowFinder.containsTargetField(rows.asScala.toSeq, RedmineField.priority.name))
      loadPriorities(mgr)
    else
      new util.HashMap[String, Integer]
  }

  @throws[RedmineException]
  private def loadPriorities(mgr: RedmineManager): util.Map[String, Integer] = {
    mgr.getIssueManager.getIssuePriorities.asScala.map(p => (p.getName, p.getId)).toMap.asJava
  }
}

class RedmineConnector(config: RedmineConfig, setup: WebConnectorSetup) extends NewConnector {
  override def loadTaskByKey(id: TaskId, rows: Iterable[FieldRow]): GTask = {
    val httpClient = RedmineManagerFactory.createRedmineHttpClient()
    try {
      val mgr = RedmineManagerFactory.createRedmineManager(setup, httpClient)
      val intKey = id.id.toInt
      val issue = mgr.getIssueManager.getIssueById(intKey, Include.relations)
      val userCache = loadUsersIfAllowed(mgr)
      val converter = new RedmineToGTask(config, userCache)
      converter.convertToGenericTask(issue)
    } catch {
      case e: RedmineException =>
        throw new RuntimeException(e)
    } finally httpClient.getConnectionManager.shutdown()
  }

  override def loadData(): util.List[GTask] = {
    val httpClient = RedmineManagerFactory.createRedmineHttpClient()
    try {
      val mgr = RedmineManagerFactory.createRedmineManager(setup, httpClient)
      val usersCache = loadUsersIfAllowed(mgr)
      val issues = mgr.getIssueManager.getIssues(config.getProjectKey, config.getQueryId, Include.relations)
      addFullUsers(issues, usersCache)
      convertToGenericTasks(config, issues, usersCache)
    } catch {
      case e: RedmineException =>
        throw new RuntimeException(e)
    } finally httpClient.getConnectionManager.shutdown()
  }

  @throws[RedmineException]
  private def addFullUsers(issues: util.List[Issue], usersCache: RedmineUserCache): Unit = {
    issues.asScala.foreach { issue =>
      issue.setAssigneeName(patchUserDisplayName(issue.getAssigneeId, usersCache).getOrElse(""))
      issue.setAuthorName(patchUserDisplayName(issue.getAuthorId, usersCache).getOrElse(""))
    }
  }

  @throws[RedmineException]
  private def patchUserDisplayName(userId: Int, usersCache: RedmineUserCache): Option[String] = {
    usersCache.findRedmineUserInCache(userId).map(_.getFullName)
  }

  private def convertToGenericTasks(config: RedmineConfig, issues: util.List[Issue], usersCache: RedmineUserCache) = {
    val converter = new RedmineToGTask(config, usersCache)
    issues.asScala.map(i => converter.convertToGenericTask(i)).asJava
  }

  override def saveData(previouslyCreatedTasks: PreviouslyCreatedTasksResolver, tasks: util.List[GTask],
                        monitor: ProgressMonitor,
                        fieldRows: Iterable[FieldRow]): SaveResult = try {
    val httpClient = RedmineManagerFactory.createRedmineHttpClient()
    val mgr = RedmineManagerFactory.createRedmineManager(setup, httpClient)
    try {
      val rmProject = mgr.getProjectManager.getProjectByKey(config.getProjectKey)
      val priorities = RedmineConnector.loadPriorities(fieldRows.asJava, mgr)
      val statusList = mgr.getIssueManager.getStatuses
      val versions = mgr.getProjectManager.getVersions(rmProject.getId)
      val categories = mgr.getIssueManager.getCategories(rmProject.getId)
      val customFieldDefinitions = mgr.getCustomFieldManager.getCustomFieldDefinitions
      val userCache = loadUsersIfAllowed(mgr)
      val converter = new GTaskToRedmine(config, priorities, rmProject, userCache, customFieldDefinitions, statusList,
        versions, categories)
      val saver = new RedmineTaskSaver(mgr.getIssueManager, config)
      val tsrb = TaskSavingUtils.saveTasks(previouslyCreatedTasks, tasks, converter, saver, monitor, fieldRows,
        setup.host)
      TaskSavingUtils.saveRemappedRelations(config, tasks, saver, tsrb)
      tsrb.getResult
    } finally httpClient.getConnectionManager.shutdown()
  } catch {
    case e: RedmineException => throw RedmineExceptions.convertException(e)
  }

  private def loadUsersIfAllowed(mgr: RedmineManager): RedmineUserCache = {
    if (!config.isFindUserByName) {
      new RedmineUserCache(Seq())
    } else {
      new RedmineUserCache(mgr.getUserManager.getUsers.asScala)
    }
  }
}

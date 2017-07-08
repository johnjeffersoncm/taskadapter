package com.taskadapter.core

import java.util

import com.taskadapter.connector.FieldRow
import com.taskadapter.connector.definition.{ProgressMonitor, TaskSaveResult}
import com.taskadapter.model.GTask

trait NewConnector {
  def saveData(tasks: util.List[GTask], monitor: ProgressMonitor, rows: util.List[FieldRow]): TaskSaveResult

  /**
    * Load list of tasks. Order of loaded tasks is not specified and may depend on implementation.
    * To get tasks in a specific order, use [[com.taskadapter.connector.common.ConnectorUtils]] methods.
    *
    * @param monitor can't be null. See
    *                [[com.taskadapter.connector.common.ProgressMonitorUtils.DUMMY_MONITOR]]} if you don't
    *                want any monitoring.
    */
  def loadData(rows: util.List[FieldRow], monitor: ProgressMonitor): util.List[GTask]

  def loadData(): util.List[GTask]

  /**
    * Loads one task by its key.
    */
  def loadTaskByKey(key: String, rows: util.List[FieldRow]): GTask

  /**
    * is called after data was exported from this connector and we got some new "remote IDs", which need to
    * be saved in this connector
    *
    * @param monitor ProgressMonitor, can be NULL
    */
  def updateRemoteIDs(remoteIds: util.Map[Integer, String], monitor: ProgressMonitor, rows: util.List[FieldRow]): Unit
}

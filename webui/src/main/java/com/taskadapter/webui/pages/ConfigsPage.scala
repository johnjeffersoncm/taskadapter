package com.taskadapter.webui.pages

import java.util
import java.util.{Collections, Comparator}

import com.taskadapter.web.uiapi.{ConfigId, UISyncConfig}
import com.taskadapter.webui.{ConfigOperations, Page, Sizes, Tracker}
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.ui._

import scala.collection.JavaConverters._

object ConfigsPage {

  /**
    * Callback for config list page.
    */
  trait Callback {
    /**
      * User requested to edit config.
      *
      * @param config selected config.
      */
    def edit(config: UISyncConfig): Unit

    /**
      * User requested synchronization in "forward" directions (from left to
      * right).
      *
      * @param config config for the operation.
      */
    def forwardSync(config: UISyncConfig): Unit

    /**
      * User requested synchronization in "reverse" direction (from right to
      * left).
      *
      * @param config config for the operation.
      */
    def backwardSync(config: UISyncConfig): Unit

    /**
      * Performs a forward drop-in.
      *
      * @param config config to use.
      * @param file   file to receive.
      */
    def forwardDropIn(config: UISyncConfig, file: Html5File): Unit

    /**
      * Performs a backward drop-in.
      *
      * @param config config to use.
      * @param file   file to receive.
      */
    def backwardDropIn(config: UISyncConfig, file: Html5File): Unit

    /**
      * User requested creation of a new config.
      */
    def newConfig(): Unit

    def showAllPreviousResults(configId: ConfigId): Unit
    def showLastExportResult(configId: ConfigId): Unit
  }

  /**
    * Comparator for configuration files.
    */
  class ConfigComparator(
                          /**
                            * Name of the current user.
                            */
                          val userName: String) extends Comparator[UISyncConfig] {
    override def compare(o1: UISyncConfig, o2: UISyncConfig): Int = {
      val isMyConfig1 = userName == o1.getOwnerName
      val isMyConfig2 = userName == o2.getOwnerName
      if (isMyConfig1 != isMyConfig2) return if (isMyConfig1) -1
      else 1
      val ucomp = o1.getOwnerName.compareTo(o2.getOwnerName)
      if (ucomp != 0) return ucomp
      o1.getLabel.compareTo(o2.getLabel)
    }
  }

}

class ConfigsPage(tracker: Tracker, showAll: Boolean, callback: ConfigsPage.Callback, configOperations: ConfigOperations) {
  val displayMode = if (showAll) DisplayMode.ALL_CONFIGS
  else DisplayMode.OWNED_CONFIGS

  private var configs = Seq[UISyncConfig]()

  val layout = new VerticalLayout
  layout.setSpacing(true)
  val actionPanel = new HorizontalLayout
  actionPanel.setWidth(Sizes.configsListWidth)
  val addButton = new Button(Page.message("configsPage.buttonNewConfig"))
  addButton.addClickListener(_ => callback.newConfig())
  actionPanel.addComponent(addButton)
  actionPanel.setComponentAlignment(addButton, Alignment.MIDDLE_LEFT)

  val filterPanel = new HorizontalLayout
  val filterField = new TextField
  filterField.addTextChangeListener(e => filterFields(e.getText))
  filterPanel.addComponent(new Label(Page.message("configsPage.filter")))
  filterPanel.addComponent(filterField)
  filterPanel.setSpacing(true)
  actionPanel.addComponent(filterPanel)
  actionPanel.setComponentAlignment(filterPanel, Alignment.MIDDLE_RIGHT)
  val configsTopLevelLayout = new HorizontalLayout()
  configsTopLevelLayout.setSpacing(true)
  configsTopLevelLayout.setSizeFull()

  val configsLayout = new VerticalLayout()
  configsLayout.setWidth(Sizes.configsListWidth)

  configsTopLevelLayout.addComponent(configsLayout)
  configsTopLevelLayout.setComponentAlignment(configsLayout, Alignment.TOP_CENTER)

  layout.addComponent(actionPanel)
  layout.addComponent(configsTopLevelLayout)
  layout.setComponentAlignment(actionPanel, Alignment.TOP_LEFT)
  refreshConfigs()

  private def refreshConfigs() = {
    val loadedConfigs = if (showAll) configOperations.getManageableConfigs
    else configOperations.getOwnedConfigs
    val configsCopy = new util.ArrayList[UISyncConfig](loadedConfigs)
    Collections.sort(configsCopy, new ConfigsPage.ConfigComparator(configOperations.userName))
    configs = loadedConfigs.asScala
    setDisplayedConfigs(configs)
    filterFields(filterField.getValue)
  }

  private def setDisplayedConfigs(dispConfigs: Seq[UISyncConfig]): Unit = {
    configsLayout.removeAllComponents()
    dispConfigs.foreach(config =>
      configsLayout.addComponent(ConfigActionsPanel.render(config, displayMode, callback, configOperations,
        () => refreshConfigs,
        () => callback.showAllPreviousResults(config.id),
        () => callback.showLastExportResult(config.id), tracker))
    )
  }

  private def filterFields(filterStr: String): Unit = {
    val words = if (filterStr == null) new Array[String](0)
    else filterStr.toLowerCase.split(" +")
    val res = configs.filter(config => matches(config, words))
    setDisplayedConfigs(res)
  }

  private def matches(config: UISyncConfig, filters: Array[String]): Boolean = {
    if (filters.length == 0) return true
    for (name <- filters) {
      val confName = displayMode.nameOf(config)
      if (!confName.toLowerCase.contains(name) && !config.getConnector1.getLabel.toLowerCase.contains(name) && !config.getConnector2.getLabel.toLowerCase.contains(name)) return false
    }
    true
  }

  def ui = layout
}
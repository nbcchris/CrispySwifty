
package Application

import Database.CustomerOrderSQL
import Database.EmployeeSQL
import Entities.CustomerOrder
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import scalafx.Includes._
import scalafx.Includes.handle
import scalafx.Includes.jfxRectangle2sfx
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Orientation
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.control.ComboBox
import scalafx.scene.control.Separator
import scalafx.scene.control.TableColumn
import scalafx.scene.control.TableColumn.sfxTableColumn2jfx
import scalafx.scene.control.TableView
import scalafx.scene.control.ToggleGroup
import scalafx.scene.control.ToolBar
import scalafx.scene.control.Tooltip
//import scalafx.scene.image.Image.sfxImage2jfx
import scalafx.scene.layout.HBox
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color.PaleGreen
import scalafx.scene.paint.Color.SeaGreen
import scalafx.scene.paint.LinearGradient
import scalafx.scene.paint.Stops
import scalafx.event.ActionEvent
import scalafx.scene.Node


/**
 * @author ChrisPoole
 * 
 * Customer Orders is the page containing a list of all Customer Orders where Employees
 * are able to claim orders to work on in other areas of the system
 */
class CustomerOrders(user : String) extends JFXApp {
  
  val empdb = new EmployeeSQL()
  val db = new CustomerOrderSQL()
  /*
  def createRect(): ImagePane ={
    val image = new Image("file:src/images/logo.png")
    val rect = new Rectangle(0,0,80,80)
    rect setFill(new ImagePattern(image))
    rect
  }*/
  
  def logo: ImageView = {
    val image = new Image("file:src/images/logo.png", 80, 80, true, true)
    val imgview = new ImageView(image)
    imgview
  }

  
  /**
  * method used to replace the PrimaryStage of the application and display new content
  * Returns a new PrimaryStage
  */
  def build : PrimaryStage={
    stage = new PrimaryStage {
      title = "Customer Orders"
      resizable = false
      
      //Connect to the database
      val db = new CustomerOrderSQL()
      val orders : ObservableBuffer[CustomerOrder] = db.getOrders

      val table = buildTable(orders)

      val toolBar = buildTools(table)
      
      //Begin Scene construction
      scene = new Scene {
        fill = new LinearGradient(endX = 0,stops = Stops(SeaGreen, PaleGreen))
        
        //Arrange content in a Horizontal Box
        content = new VBox{ 
          padding = Insets(3)
          children = Seq(
            toolBar,
            new  VBox {
              children = Seq(
                  table
                  ,new Button {
                    text = "Show my claimed orders "
                    prefWidth = 490
                    onAction = handle{
                      filterTable(table, orders) 
                    }
                  }
                //Table Creation
               //Table finished
              )
            }
          )
        }
      }  
    }
   stage
  }
  
  /**
   * filterTable(table : TableView[CustomerOrder], orders: ObservableBuffer[CustomerOrder])
   * Uses the filter function to update the table with only the Orders that belong to the current user
   */
  def filterTable(table : TableView[CustomerOrder], orders: ObservableBuffer[CustomerOrder]) : Unit = {
     val newOrders:ObservableBuffer[CustomerOrder] = filter(orders)
     table.items.update(newOrders)
  }
  
  /**
   * filter(orders : ObservableBuffer[CustomerOrder])
   * Filters all orders by current users order id and returns them as an ObservableBuffer of CustomerOrders
   */
  def filter(orders : ObservableBuffer[CustomerOrder]): ObservableBuffer[CustomerOrder] = {
    val empdb = new EmployeeSQL()
    val newOrders = (x : CustomerOrder) =>  x.getId % empdb.getId(user) == 0
    for(x <- orders; if(newOrders(x))) yield x
  }
  
  def buildTable(orders : ObservableBuffer[CustomerOrder]): TableView[CustomerOrder]={
    val table =  new TableView[CustomerOrder](orders){
      columns ++= List(
        new TableColumn[CustomerOrder, Int] {
          text = "Order ID" 
          cellValueFactory = { _.value.customerOrderId }
          prefWidth = 163
        },
        new TableColumn[CustomerOrder, Int]() {
          text = "Employee ID"
          cellValueFactory = { _.value.employeeId }
          prefWidth = 163
          },
        new TableColumn[CustomerOrder, String] {
          text = "Status"
          cellValueFactory = { _.value.status }
          prefWidth = 163
        }
      )
    }
    table
  }
  
  def updateTable(table : TableView[CustomerOrder], orders: ObservableBuffer[CustomerOrder]){
    table.items.update(orders)
  }
  
  def buildCombo : ComboBox[String] = {
     val combo : ComboBox[String] = new ComboBox()
      val options : ObservableBuffer[String] = ObservableBuffer[String]()
        options += "Picked"
        options += "Packed"
        options += "Dispatched"
        options += "Complete"
      combo.promptText = "Pick a Status"
      combo.items = options
      
      combo
  }
  
  def buildTools( table : TableView[CustomerOrder]) : ToolBar = {
    val combo = buildCombo
    val toolBar = new ToolBar {
        content = List(
          new Button {
            id = "newButton"
            graphic = logo
            tooltip = Tooltip("Back to Index")
            onAction = handle {
              val a = new Index(user)
              stage = a build
            }
          },
          new Button {
            id = "viewOrder"
            text = "View Order"
            onMouseClicked = handle{
              val a = new Order(user, getCoid(table))
              stage = a build
            }
          },
          new Button {
            id = "claim"
            text = "Claim Order"
            onMouseClicked = handle {
              val empdb = new EmployeeSQL()
              val db = new CustomerOrderSQL()
              val userid = empdb getId(user)
              println(getCoid(table))
              db claim(getCoid(table), userid)
              updateTable(table, db getOrders)
            }
          },
          new Separator {
            orientation = Orientation.VERTICAL
          },
          combo,
          new Button {
            id = "changeStatus"
            text = "Change Status"
            onMouseClicked = handle {
              val selected = combo.value.value
              if(selected != null){
                db updateStatus(getCoid(table),selected)
                val a = new CustomerOrders(user)
                stage = a build
              }
            }
          }
        )
      }
      
    toolBar
  }
  
  /**
   * getCoid(table : TableView[CustomerOrder])
   * gets the CustomerOrderId for the currently selected tuple of the CustomerOrder Table
   */
  def getCoid(table : TableView[CustomerOrder]) : Int = {
    val coid = table.getSelectionModel.selectedItemProperty.get.customerOrderId.value
    println(coid)
    coid
  }

}
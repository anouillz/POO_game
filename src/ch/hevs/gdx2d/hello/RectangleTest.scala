package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer, TmxMapLoader}

class RectangleTest(var width: Int, var height: Int) extends PortableApplication(width, height) {

  println("GameScreenTest constructor called")

  var map: TiledMap = _
  var tiledMapRenderer: OrthogonalTiledMapRenderer = _
  var tiledLayer: TiledMapTileLayer = _
  var zoom = 1f
  var camera: OrthographicCamera = _

  // Width and height of one tile in pixels
  val tileWidth: Int = 32
  val tileHeight: Int = 32

  def onInit(): Unit = {
    println("onInit called")
    setTitle("Game")

    camera = new OrthographicCamera()
    camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
    println("Camera initialized.")

    try {
      val exampleMap: TiledMap = new TmxMapLoader().load("data/maps/mapTest1.tmx")
      println("Original map loaded.")

      map = createRectangle(exampleMap, 5, 5)
      println("New map with rectangle created.")

      tiledMapRenderer = new OrthogonalTiledMapRenderer(map)
      tiledLayer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
      println("Renderer initialized.")
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def onGraphicRender(g: GdxGraphics): Unit = {
    println("onGraphicRender called")
    g.clear()
    camera.update()
    tiledMapRenderer.setView(camera)

    println("Rendering map...")
    tiledMapRenderer.render()

    g.drawFPS()
    println("Printing school logo")
    g.drawSchoolLogo()
  }

  def changeTile(map1: TiledMap, layer: TiledMapTileLayer, x: Int, y: Int, newID: Int): Unit = {
    require(layer != null)
    println(s"Changing tile at ($x, $y) to ID $newID")

    var cell = layer.getCell(x, y)
    if (cell == null) {
      cell = new Cell()
      layer.setCell(x, y, cell)
      println(s"Initialized new cell at ($x, $y)")
    }

    val tileSet = map1.getTileSets.getTileSet(0)
    val newTile = tileSet.getTile(newID)
    if (newTile != null) {
      cell.setTile(new StaticTiledMapTile(newTile.getTextureRegion))
      println(s"Tile set at ($x, $y) to ID $newID")
    } else {
      println(s"Tile ID $newID does not exist in the tileset.")
    }
  }

  // coordinates start bottom left
  def createRectangle(originalMap: TiledMap, width: Int, height: Int): TiledMap = {
    println("createRectangle called")
    // Create a new TiledMap
    val newMap = new TiledMap()

    // Get the tileset from the original map and add it to the new map
    val originalTileSet = originalMap.getTileSets.getTileSet(0)
    newMap.getTileSets.addTileSet(originalTileSet)
    println("Tileset copied to new map.")

    // Create a new layer with the same dimensions and tile size as the original
    val originalLayer = originalMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
    val tileWidth = originalLayer.getTileWidth
    val tileHeight = originalLayer.getTileHeight
    val newLayer = new TiledMapTileLayer(originalLayer.getWidth, originalLayer.getHeight, tileWidth.toInt, tileHeight.toInt)

    newMap.getLayers.add(newLayer)
    println("New layer created and added to new map.")

    val groundID: Int = 65
    val offsetX: Int = 0
    val offsetY: Int = 0

    for (i <- offsetX until width + offsetX) {
      for (j <- offsetY until height + offsetY) {
        changeTile(newMap, newLayer, i, j, groundID)
      }
    }

    newMap
  }

  // Utilisez une méthode spécifique pour libérer les ressources
  def cleanup(): Unit = {
    println("Cleaning up resources...")
    if (tiledMapRenderer != null) {
      tiledMapRenderer.dispose()
    }
    if (map != null) {
      map.dispose()
    }
  }
}

object RectangleTest {
  def main(args: Array[String]): Unit = {
    println("Starting GameScreenTest...")
    new RectangleTest(800, 800)
  }
}

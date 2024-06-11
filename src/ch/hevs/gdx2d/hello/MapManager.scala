package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Pixmap, Texture}
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTile, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.math.Vector2

class MapManager(var width: Int, var height: Int) extends PortableApplication(width, height){

  var map: TiledMap = _
  var tiledMapRenderer: OrthogonalTiledMapRenderer = _
  var tiledLayer: TiledMapTileLayer = _
  var zoom = 1


  //width and height of one tile in pixels
  val tileWidth: Int = 32
  val tileHeight: Int = 32



  def onInit(): Unit = {
    setTitle("Game")

    map = new TmxMapLoader().load("data/maps/map.tmx")
    tiledMapRenderer = new OrthogonalTiledMapRenderer(map)
    tiledLayer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
  }

  def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    g.zoom(zoom)

    // Render the tilemap
    tiledMapRenderer.setView(g.getCamera)

    tiledMapRenderer.render()

    g.drawFPS()
    g.drawSchoolLogo()
  }


  def isWalkable(tile: TiledMapTile): Boolean = {

    if (tile == null) return false

    val test = tile.getProperties.get("isWalkable")

    return test.toString.toBoolean
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


}

object Game extends App {
  new MapManager(1000, 1000)
}



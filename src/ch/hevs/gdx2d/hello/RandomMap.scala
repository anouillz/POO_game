package ch.hevs.gdx2d.hello

import ch.hevs.gdx2d.desktop._
import ch.hevs.gdx2d.lib._
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile

import scala.collection.mutable.ArrayBuffer

class RandomMap(var width: Int, var height: Int) extends PortableApplication(width, height){

  var map: TiledMap = _
  var tiledMapRenderer: OrthogonalTiledMapRenderer = _
  var tiledLayer: TiledMapTileLayer = _
  val ZOOM: Int = 1


  private val tileWidth: Int = 32
  private val tileHeight: Int = 32



  override def onInit(): Unit = {
    setTitle("RandomMapTest")
    map = new TmxMapLoader().load("data/maps/map.tmx")
    tiledMapRenderer = new OrthogonalTiledMapRenderer(map)
    tiledLayer = map.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    g.zoom(ZOOM)

    // Render the tilemap
    tiledMapRenderer.setView(g.getCamera)

    tiledMapRenderer.render()

    g.drawFPS()
    g.drawSchoolLogo()
  }

  def changeTile(map1: TiledMap, layer: TiledMapTileLayer, x: Int, y: Int, newID: Int): Unit = {
    require(layer != null)

    var cell = layer.getCell(x, y)
    if (cell == null) {
      cell = new Cell()
      layer.setCell(x, y, cell)
    }

    val tileSet = map1.getTileSets.getTileSet(0)
    val newTile = tileSet.getTile(newID)
    if (newTile != null) {
      cell.setTile(new StaticTiledMapTile(newTile.getTextureRegion))
    } else {
      println(s"Tile ID $newID does not exist in the tileset.")
    }
  }

}

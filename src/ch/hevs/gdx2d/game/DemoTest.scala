package ch.hevs.gdx2d.game

import ch.hevs.gdx2d.desktop.PortableApplication
import ch.hevs.gdx2d.lib.GdxGraphics
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.maps.tiled._
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable

class DemoTest extends PortableApplication {

  // key management
  private val keyStatus: mutable.Map[Int, Boolean] = mutable.TreeMap[Int, Boolean]()

  // character
  private var hero: Hero = _

  // tiles management
  private var tiledMap: TiledMap = _
  private var tiledMapRenderer: TiledMapRenderer = _
  private var tiledLayer: TiledMapTileLayer = _
  private var zoom: Float = _

  override def onInit(): Unit = {

    // Create hero
    hero = new Hero(10, 20)

    // Set initial zoom
    zoom = 1

    // init keys status
    keyStatus.put(Input.Keys.UP, false)
    keyStatus.put(Input.Keys.DOWN, false)
    keyStatus.put(Input.Keys.LEFT, false)
    keyStatus.put(Input.Keys.RIGHT, false)

    // create map
    tiledMap = new TmxMapLoader().load("data/maps/mapTest1.tmx")
    tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap)
    tiledLayer = tiledMap.getLayers.get(0).asInstanceOf[TiledMapTileLayer]
  }

  override def onGraphicRender(g: GdxGraphics): Unit = {
    g.clear()

    // Hero activity
    manageHero()

    // Camera follows the hero
    g.zoom(zoom)
    g.moveCamera(hero.getPosition.x, hero.getPosition.y, tiledLayer.getWidth * tiledLayer.getTileWidth, tiledLayer.getHeight * tiledLayer.getTileHeight)

    // Render the tilemap
    tiledMapRenderer.setView(g.getCamera)
    tiledMapRenderer.render()

    // Draw the hero
    hero.animate(Gdx.graphics.getDeltaTime)
    hero.draw(g)

    g.drawFPS()
    g.drawSchoolLogo()
  }

  /**
   * exemple : getTile(myPosition,0,1) get the tile over myPosition
   *
   * @param position
   *            The position on map (not on screen)
   * @param offsetX
   *            The number of cells at right of the given position.
   * @param offsetY
   *            The number of cells over the given position.
   * @return The tile around the given position | null
   */
  private def getTile(position: Vector2, offsetX: Int, offsetY: Int): TiledMapTile = {
    try {
      val x = (position.x / tiledLayer.getTileWidth).toInt + offsetX
      val y = (position.y / tiledLayer.getTileHeight).toInt + offsetY

      tiledLayer.getCell(x, y).getTile
    } catch {
      case _: Exception =>
        null
    }
  }

  /**
   * Get the "walkable" property of the given tile.
   *
   * @param tile
   *            The tile to know the property
   * @return true if the property is set to "true", false otherwise
   */
  private def isWalkable(tile: TiledMapTile): Boolean = {
    if (tile == null){
      return false
    }
    val walkable = tile.getProperties.get("isWalkable")

    if(walkable == true) {
      return true
    }

    return false

  }

  /**
   * Get the "speed" property of the given tile.
   *
   * @param tile
   *            The tile to know the property
   * @return The value of the property
   */
  private def getSpeed(tile: TiledMapTile): Float = {
    val test = tile.getProperties.get("speed")
    java.lang.Float.parseFloat(test.toString)
  }

  /**
   * Manage the movements of the hero using the keyboard.
   */
  private def manageHero(): Unit = {
    // Do nothing if hero is already moving
    if (!hero.isMoving) {

      // Compute direction and next cell
      var nextCell: TiledMapTile = null
      var goalDirection: Hero.Direction.Value = Hero.Direction.NULL

      if (keyStatus(Input.Keys.RIGHT)) {
        goalDirection = Hero.Direction.RIGHT
        nextCell = getTile(hero.getPosition, 1, 0)
      } else if (keyStatus(Input.Keys.LEFT)) {
        goalDirection = Hero.Direction.LEFT
        nextCell = getTile(hero.getPosition, -1, 0)
      } else if (keyStatus(Input.Keys.UP)) {
        goalDirection = Hero.Direction.UP
        nextCell = getTile(hero.getPosition, 0, 1)
      } else if (keyStatus(Input.Keys.DOWN)) {
        goalDirection = Hero.Direction.DOWN
        nextCell = getTile(hero.getPosition, 0, -1)
      }

      // Is the move valid ?
      if (isWalkable(nextCell)) {
        // Go
        hero.setSpeed(getSpeed(nextCell))
        hero.go(goalDirection)
      } else {
        // Face the wall
        hero.turn(goalDirection)
      }
    }
  }

  // Manage keyboard events
  override def onKeyUp(keycode: Int): Unit = {
    super.onKeyUp(keycode)
    keyStatus.put(keycode, false)
  }

  override def onKeyDown(keycode: Int): Unit = {
    super.onKeyDown(keycode)

    keycode match {
      case Input.Keys.Z =>
        zoom match {
          case 1.0f => zoom = 0.5f
          case 0.5f => zoom = 2.0f
          case _ => zoom = 1.0f
        }
      case _ =>
    }
    keyStatus.put(keycode, true)
  }
}

object DemoTest {
  def main(args: Array[String]): Unit = {
    new DemoTest()
  }
}

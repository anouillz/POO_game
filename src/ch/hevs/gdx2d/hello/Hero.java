package ch.hevs.gdx2d.hello;

import ch.hevs.gdx2d.components.bitmaps.BitmapImage;
import ch.hevs.gdx2d.components.bitmaps.Spritesheet;
import ch.hevs.gdx2d.lib.GdxGraphics;
import ch.hevs.gdx2d.lib.interfaces.DrawableObject;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

/**
 * Character for the demo.
 *
 * @author Alain Woeffray (woa)
 * @author Pierre-André Mudry (mui)
 */
public class Hero implements DrawableObject {

    public enum Direction{
        UP_RIGHT,
        UP_LEFT,
        DOWN_RIGHT,
        DOWN_LEFT,
        UP,
        DOWN,
        RIGHT,
        LEFT,
        NULL
    }

    /**
     * The currently selected sprite for animation
     */
    int textureX = 0;
    int textureY = 1;
    float speed = 3;

    int currentFrame = 0;
    int nFrames = 4;

    private final static int SPRITE_WIDTH = 32;
    private final static int SPRITE_HEIGHT = 32;

    Spritesheet ss;

    Vector2 position;

    /**
     * Create the hero at the start position (0,0)
     */
    public Hero(){this(new Vector2(0,0));
    }

    /**
     * Create the hero at the given start tile.
     * @param x Column
     * @param y Line
     */
    public Hero(int x, int y){this(new Vector2(SPRITE_WIDTH * x, SPRITE_HEIGHT * y));
    }

    /**
     * Create the hero at the start position
     * @param initialPosition Start position [px] on the map.
     */
    public Hero(Vector2 initialPosition) {
        position = new Vector2(initialPosition);
        ss = new Spritesheet("data/images/hero8direction.png", SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    /**
     * @return the current position of the hero on the map.
     */
    public Vector2 getPosition(){
        return this.position;
    }


    /**
     * @param speed The new speed of the hero.
     */
    public void setSpeed(float speed){
        this.speed = speed;
    }

    /**
     * Do a step on the given direction
     * @param direction The direction to go.
     */
    public void go(Direction direction){
        switch(direction){
            case UP_RIGHT:
                position.add(SPRITE_WIDTH/16, SPRITE_HEIGHT/16);
                break;
            case UP_LEFT:
                position.add(-SPRITE_WIDTH/16, SPRITE_HEIGHT/16);
                break;
            case DOWN_RIGHT:
                position.add(SPRITE_WIDTH/16, -SPRITE_HEIGHT/16);
                break;
            case DOWN_LEFT:
                position.add(-SPRITE_WIDTH/16, -SPRITE_HEIGHT/16);
                break;
            case RIGHT:
                position.add(SPRITE_WIDTH/16, 0);
                break;
            case LEFT:
                position.add(-SPRITE_WIDTH/16, 0);
                break;
            case UP:
                position.add(0, SPRITE_HEIGHT/16);
                break;
            case DOWN:
                position.add(0, -SPRITE_HEIGHT/16);
                break;
            default:
                break;
        }

        turn(direction);
    }

    /**
     * Turn the hero on the given direction without do any step.
     * @param direction The direction to turn.
     */
    public void turn(Direction direction){
        switch(direction){
            case UP_RIGHT:
                textureY = 0;
                textureX = 0;
                break;
            case UP_LEFT:
                textureY = 0;
                textureX = 2;
                break;
            case DOWN_RIGHT:
                textureY = 1;
                textureX = 2;
                break;
            case DOWN_LEFT:
                textureY = 1;
                textureX = 0;
                break;
            case RIGHT:
                textureY = 1;
                textureX = 3;
                break;
            case LEFT:
                textureY = 0;
                textureX = 3;
                break;
            case UP:
                textureY = 0;
                textureX = 1;
                break;
            case DOWN:
                textureY = 1;
                textureX = 1;
                break;
            default:
                break;
        }
    }

    /**
     * Draw the character on the graphic object.
     * @param g Graphic object.
     */
    public void draw(GdxGraphics g) {
        g.draw(ss.sprites[textureY][textureX], position.x, position.y);
    }
}
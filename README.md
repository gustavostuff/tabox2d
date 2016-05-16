Inspired by [Lope2D](https://bitbucket.org/erlimoen/lope2d-v2/wiki/Home) and [FlxBox2D](https://github.com/flixel-gdx/flixel-gdx-box2d) Tabox2D was born, a wrapper class to ease body-texture management in Box2D (LibGDX).

[![License](http://img.shields.io/:license-MIT-blue.svg)](http://doge.mit-license.org)

Here's an example of an application class using Tabox2D:

```java
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.tavuntu.example.Tabox2D.Tabody;

public class Box2DTest extends ApplicationAdapter {
    Tabox2D t;
    float w ,h;
    float rad;
    @Override
    public void create () {
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        t = Tabox2D.getInstance();
        t.setFilter("linear", "linear");// Soft textures.
        //t.debug();// This uses Box2DDebugRenderer, shows AABB and mass centres.
        t.setMeterSize(64);// Default = 100.
        t.setRawForces(false);// If false (default value) forces are multiplied by the mass.

        // Walls:
        t.newBox("s", 0, 0, w, 30).texture("dot.png");// Down.
        t.newBox("s", 0, h - 30, w, 30).texture("dot.png");// Up.
        t.newBox("s", 0, 30, 30, h - 60).texture("dot.png");// Left.
        t.newBox("s", w - 30, 30, 30, h - 60).texture("dot.png");// Right.

        rad = w / 16;
        // Regular bodies:
        t.newSquare("d", 100, 100, rad).texture("square.png").rotate(45);// 1/8 turn.
        t.newPentagon("d", 100, 200, rad).texture("pentagon.png").density(0.1f);// Light.
        t.newHexagon("d", 100, 300, rad).texture("hexagon.png").friction(0);// Like ice.
        t.newHeptagon("d", 100, 400, rad).texture("heptagon.png").restitution(0.9f);// Bouncy.
        t.newOctagon("d", 200, 200, rad).texture("octagon.png");
        t.newBall("d", 200, 300, rad).texture("ball.png");
        t.newTriangle("d", 200, 400, rad).texture("triangle.png").forceY(-10);// Likes the floor.

        // Irregular:
        float[] pts1 = {160, 50, 220, 50, 240, 70, 220, 90, 160, 90, 140, 70},
                pts2 = {340, 60, 360, 60, 400, 90, 370, 120, 330, 130, 320, 70};
        t.newPoly("d", pts1).texture("irr1.png");
        t.newPoly("d", pts2).texture("irr2.png");

        // Compound bodies:
        Tabody bucket = t.combine("d",
            t.newBox("d", 300, 200, 30, 150),
            t.newBox("d", 330, 200, 90, 30),
            t.newBox("d", 420, 200, 30, 150)
        ).texture("bucket.png");

        Tabody cross = t.combine("d",
            t.newBox("d", 500, 130, 40, 80),
            t.newBox("d", 480, 150, 20, 40),
            t.newBox("d", 540, 150, 20, 40)
        ).texture("cross.png");

        //t.destroy(bucket);// Destroys a Tabody.
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        t.update();
        t.draw();

        // Move bodies:
        if(Gdx.input.justTouched()) {
            for(Tabox2D.Tabody b : t.getTabodies()) {
                b.impulseY(5);
            }
        }
    }

    @Override
    public void dispose() {
        t.dispose();
    }

}

```

The result would be something like:

![Tabox2D example](http://s27.postimg.org/gatt0as8z/ss1.png)

***

Tabox2D does this:

* Creates multi-shaped bodies in one line!
* Makes regular/irregluar polygons in a simple way
* Attaches textures to bodies in terms of position, scale and rotation
* Uses the Tabody entity, easier to manage than a normal Body
* Applies linear impulses through Tabody class
* Tabox2D.newBox() takes X and Y as the left-bottom corner, then full W and H

it doesn't:

* Cover the joints part
* Cover other Box2D API

This is still a work in progress, thanks for your feedback!

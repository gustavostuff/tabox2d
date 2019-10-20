[![log-tabox2d.png](https://i.postimg.cc/PfZxPBXs/log-tabox2d.png)](https://postimg.cc/HjYHNv8B)

Inspired by [Lope2D](https://love2d.org/wiki/Lope2D) and [FlxBox2D](https://github.com/flixel-gdx/flixel-gdx-box2d) Tabox2D was born, a simple layer class to ease body-texture management in Box2D (LibGDX).

[![License](http://img.shields.io/:license-MIT-blue.svg)](http://doge.mit-license.org)

Here's an example of an application using Tabox2D:

```java
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
        t.newPentagon("d", 100, 200, rad).texture("pentagon_hi_res.png").density(0.1f);// Light.
        t.newHexagon("d", 100, 300, rad).texture("hexagon.png").friction(0);// Like ice.
        t.newHeptagon("d", 100, 400, rad).texture("heptagon.png").restitution(0.9f);// Bouncy.
        t.newOctagon("d", 200, 200, rad).texture("octagon.png");
        t.newBall("d", 200, 300, rad).texture("ball_low_res.png");
        t.newTriangle("d", 200, 400, rad).texture("triangle.png").forceY(-10);// Likes the floor.

        // Irregular:
        float[] pts1 = {160, 50, 220, 50, 240, 70, 220, 90, 160, 90, 140, 70},
                pts2 = {340, 60, 360, 60, 400, 90, 370, 120, 330, 130, 320, 70};
        t.newPoly("d", pts1).texture("irr1.png");
        t.newPoly("d", pts2).texture("irr2.png");

        // Compound bodies (Tabox2D.Tabody)

        Tabody bucket = t.combine("d",
                t.newBox("d", 250, 200, 30, 150),
                t.newBox("d", 280, 200, 90, 30),
                t.newBox("d", 370, 200, 30, 150)
        ).texture("bucket.png");

        Tabody siameseBucket = t.combine("d",
                t.newBox("d", 420, 200, 30, 150),
                t.newBox("d", 450, 200, 90, 30),
                t.newBox("d", 540, 200, 30, 150),
                t.newBall("d", 570, 300, 20)
        ).texture("siameseBucket.png");

        Tabody cross = t.combine("d",
                t.newBox("d", 510, 20, 20, 40),
                t.newBox("d", 530, 0,  40, 80),
                t.newBox("d", 570, 20, 20, 40)
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

[![Captura-de-pantalla-2019-10-20-11-43-02.png](https://i.postimg.cc/vmLCFNPF/Captura-de-pantalla-2019-10-20-11-43-02.png)](https://postimg.cc/Lq5xjNKN)

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
* Cover other Box2D API (yet)

## Notes

* This has only been tested on Desktop libGDX projets, not Android, iOS, or anything else 
* No Maven/Gradle artifact for this yet (help needed, I'm not a Java Dev)

This is still a work in progress, thanks for your feedback!

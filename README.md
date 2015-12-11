![Tabox2D logo](http://s22.postimg.org/qm3cr4ma5/logo_Tabox2_D.png)

Tabox2D is a wrapper class to ease body-texture creation in Box2D (LibGDX). Here's an example of a simple application class using Tabox2D:

```java
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;

public class MyGdxGame extends ApplicationAdapter {

	Tabox2D t;
    float w ,h;
    float rad;
	@Override
	public void create () {
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        t = Tabox2D.getInstance();
        t.setFilter("linear", "linear");// Soft textures.
        t.debug();// This uses Box2DDebugRenderer, shows AABB centers and centroids.

        // Bodies, "i" means Gdx.files.internal:
        rad = 40;
        t.newBall    ("d", 100, 200, rad).setTexture("marble.png", "i");
        t.newTriangle("d", new Vector2(w / 2, h / 2), rad).setTexture("triangle.png", "i");
        t.newSquare  ("d", new Vector2(w / 2, h / 2), rad).setTexture("square.png", "i");
        t.newPentagon("d", new Vector2(w / 2, h / 2), rad).setTexture("pentagon.png", "i");
        t.newHexagon ("d", new Vector2(w / 2, h / 2), rad).setTexture("hexagon.png", "i");
        t.newHeptagon("d", new Vector2(w / 2, h / 2), rad).setTexture("heptagon.png", "i");
        t.newOctagon ("d", new Vector2(w / 2, h / 2), rad).setTexture("octagon.png", "i");

        // Irregular:
        float[] pts  = {40, 60, 60, 60, 100, 90, 70, 120, 30, 130, 20, 70},
                pts2 = {30, 50, 90, 50, 110, 70, 90, 90, 30, 90, 10, 70};
        t.newPoly("d", pts).setTexture("irr.png", "i");
        t.newPoly("d", pts2);// No texture.

        // Walls:
        t.newBox("s", 0, 0, w, 30);// Down.
        t.newBox("s", 0, h - 30, w, 30);// Up.
        t.newBox("s", 0, 30, 30, h - 60);// Left.
        t.newBox("s", w - 30, 30, 30, h - 60);// Right.
	}

	@Override
	public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        t.update();
        t.draw();

        // Move tabodies:
        if(Gdx.input.justTouched()) {
            for(Tabox2D.Tabody b : t.getTabodies()) {
                b.impulseY(3);
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

![Tabox2D example](http://s2.postimg.org/rnq3zmovt/ss_tabox2d.png)

***

Tabox2D is just a simple wrapper and doesn't cover the whole Box2D API (directly). Currently, it only does:

* Adjusts textures to bodies in terms of position, scale and rotation
* Apply linear impulses through Tabody class
* Make regular polygons in a simple way
* Tabox2D.newBox() takes X and Y as the left-bottom corner, then full W and H
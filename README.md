![Tabox2D logo](http://s22.postimg.org/qm3cr4ma5/logo_Tabox2_D.png)

Tabox2D is currently in development.

Using Tabox2D class, you'll be able to create bodies and attach them textures in an easier way.

Using the code below we create some bodies with some textures attached:
```java
w = Gdx.graphics.getWidth();
h = Gdx.graphics.getHeight();

t = Tabox2D.getInstance();
t.debug = true;
rad = 35;
force = 500;

Texture ballTexture = new Texture(Gdx.files.internal("ball.png"));
Texture triangleTexture = new Texture(Gdx.files.internal("triangle.png"));
Texture pentagonTexture = new Texture(Gdx.files.internal("pentagon.png"));
Texture octagonTexture = new Texture(Gdx.files.internal("octagon.png"));
Texture bricksTexture = new Texture(Gdx.files.internal("bricks.png"));


// Ball:
ball = t.newBall("d", w / 2, h / 2, rad).setTexture(ballTexture);

// Walls, ceiling and floor:
t.newBox("s", 10, 60, 20, h - 40);
t.newBox("s", 10, 10, w - 20, 50).setTexture(bricksTexture);// Floor.
t.newBox("s", w - 30, 60, 20, h - 40);
t.newBox("s", 10, h - 50, w - 20, 20);

// Regular polygons:
t.newTriangle("d", new Vector2(100, 100), rad).setTexture(triangleTexture);
t.newPentagon("d", new Vector2(100, 200), rad).setTexture(pentagonTexture);
t.newOctagon("d", new Vector2(100, 300), rad).setTexture(octagonTexture);
```

The result would be something like:

![Tabox2D example](http://s2.postimg.org/4clfux8nt/sstbx.png)

NOTE: The API is currently in development, the example above shows the complete characteristics. Currently, polygons have issues with textures.
Red points indicate the center of mass, cyan circles the geometric center (failing with polygons).
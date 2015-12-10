/*

Copyright (c) 2015 Gustavo Alberto Lara Gómez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

*/
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tabox2D {

    public static boolean debug = false;

    private static HashMap<String, Float> polyInfo;

    private static Tabox2D instance;
    private Box2DDebugRenderer renderer;
    private OrthographicCamera camera;
    private World world;
    private SpriteBatch spriteBath;
    private List<Tabody> tabodies;
    private int width;
    private int height;
    private float meterSize;
    public ShapeRenderer sr = new ShapeRenderer();

    private Tabox2D(Vector2 gravity) {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        meterSize = 100;// 1 metter = 100px, default.

        pl("width: " + width + ", height: " + height);
        polyInfo = new HashMap<String, Float>();

        // Sides by polygon:
        polyInfo.put("triangle", 3f);
        polyInfo.put("square", 4f);
        polyInfo.put("pentagon", 5f);
        polyInfo.put("hexagon", 6f);
        polyInfo.put("heptagon", 7f);
        polyInfo.put("octagon", 8f);

        // Angle of the sides:
        polyInfo.put("triangle_angle", 120f);
        polyInfo.put("square_angle", 90f);
        polyInfo.put("pentagon_angle", 72f);
        polyInfo.put("hexagon_angle", 60f);
        polyInfo.put("heptagon_angle", 51.428f);
        polyInfo.put("octagon_angle", 45f);

        renderer = new Box2DDebugRenderer();
        spriteBath = new SpriteBatch();
        adjustCamera();
        tabodies = new ArrayList<Tabody>();
        world = new World(new Vector2(gravity.x, gravity.y), true);

        sr = new ShapeRenderer();
    }

    private void adjustCamera() {
        float cameraW = width / meterSize;
        float cameraH = height / meterSize;
        pl("cameraW: " + cameraW + ", cameraH: " + cameraH);
        camera = new OrthographicCamera(cameraW, cameraH);
        // Set at 0, 0, 0 in space:
        float camX = camera.viewportWidth / 2;
        float camY = camera.viewportHeight / 2;
        camera.position.set(camX, camY, 0f);
        camera.update();
    }

    public static Tabox2D getInstance() {
        return getInstance(new Vector2(0, -9.8f));
    }

    public List<Tabody> getTabodies() {
        return tabodies;
    }

    public static Tabox2D getInstance(Vector2 gravity) {
        if(instance == null) {
            instance = new Tabox2D(gravity);
        }
        return instance;
    }

    public void setMeterSize(float meterSize) {
        if(meterSize > 500) {
            perr("Max meterSize allowed: 500, " +
            "using default = 100");
            return;
        }
        this.meterSize = meterSize;
        adjustCamera();
    }







    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Ball creator:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public Tabody newBall(String type, float x, float y, float r) {
        // Scale proportions:
        x = x / meterSize;
        y = y / meterSize;
        r = r / meterSize;

        CircleShape circleShape;
        BodyDef defBall = new BodyDef();

        setType(defBall, type);

        defBall.position.set(x, y);

        Tabody ball = new Tabody();
        ball.body = world.createBody(defBall);
        circleShape = new CircleShape();
        circleShape.setRadius(r);

        FixtureDef fixtureBall = new FixtureDef();
        fixtureBall.shape = circleShape;
        fixtureBall.density = 1;
        fixtureBall.friction = 1;
        fixtureBall.restitution = 0.5f;//0.2f;

        ////////////////////////////////////////
        ball.w = r * 2 * meterSize;
        ball.h = r * 2 * meterSize;
        ball.fixture = fixtureBall;
        ball.bodyType = "ball";
        ////////////////////////////////////////

        ball.body.createFixture(fixtureBall);
        circleShape.dispose();
        tabodies.add(ball);
        return ball;
    }






    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Box creator:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public Tabody newBox(String type, float x, float y, float w, float h) {
        // Scale proportions:
        x = x / meterSize;
        y = y / meterSize;
        w = w / meterSize;
        h = h / meterSize;

        PolygonShape polygonShape;
        BodyDef defBox = new BodyDef();

        setType(defBox, type);

        defBox.position.set(x + w / 2, y + h / 2);

        Tabody box = new Tabody();
        box.body = world.createBody(defBox);
        polygonShape = new PolygonShape();
        polygonShape.setAsBox(w / 2, h / 2);

        FixtureDef fixtureBox = new FixtureDef();
        fixtureBox.shape = polygonShape;
        fixtureBox.density = 1;
        fixtureBox.friction = 1;
        fixtureBox.restitution = 0.2f;

        ////////////////////////////////////////
        box.w = w * meterSize;
        box.h = h * meterSize;
        box.fixture = fixtureBox;
        box.bodyType = "box";
        ////////////////////////////////////////

        box.body.createFixture(fixtureBox);
        polygonShape.dispose();
        tabodies.add(box);
        return box;
    }






    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Polygons creators:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public Tabody newTriangle(String type, Vector2 center, float radius) {
        return generateRegularPoly("triangle", type, center, radius);
    }

    public Tabody newSquare(String type, Vector2 center, float radius) {
        return generateRegularPoly("square", type, center, radius);
    }

    public Tabody newPentagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("pentagon", type, center, radius);
    }

    public Tabody newHexagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("hexagon", type, center, radius);
    }

    public Tabody newHeptagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("heptagon", type, center, radius);
    }

    public Tabody newOctagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("octagon", type, center, radius);
    }

    private Tabody generateRegularPoly(String polyName, String type, Vector2 center, float rad) {
        // Scale proportions:
        center.set(center.x / meterSize, center.y / meterSize);
        rad /= meterSize;

        PolygonShape polygonShape;
        BodyDef defPoly = new BodyDef();

        setType(defPoly, type);

        defPoly.position.set(0, 0);
        //defPoly.position.set(center.x, center.y);

        Tabody regularPoly = new Tabody();
        regularPoly.body = world.createBody(defPoly);
        //regularPoly.body.setFixedRotation(true);
        polygonShape = new PolygonShape();

        // Generate points:
        List<Vector2> pts = new ArrayList<Vector2>();
        Vector2 p0 = new Vector2(center.x, center.y + rad);

        float conv = MathUtils.degreesToRadians;
        float angleInDeg = polyInfo.get(polyName + "_angle");
        float cos = MathUtils.cos(conv * angleInDeg);
        float sin = MathUtils.sin(conv * angleInDeg);

        for(int i = 0; i < polyInfo.get(polyName); i++) {
            pts.add(new Vector2(p0.x, p0.y));
            p0.set(p0.x - center.x, p0.y - center.y);

            float newX = p0.x * cos - p0.y * sin;
            float newY = p0.x * sin + p0.y * cos;

            p0.x = newX + center.x;
            p0.y = newY + center.y;
        }

        // Get bounding box:

        float[] rawPoints = new float[pts.size() * 2];
        int pointIndex = 0;
        for(int i = 0; i < rawPoints.length - 1; i += 2) {
            rawPoints[i] = pts.get(pointIndex).x;
            rawPoints[i + 1] = pts.get(pointIndex).y;
            pointIndex++;
        }
        
        Polygon polyForBox = new Polygon();
        polyForBox.setVertices(rawPoints);
        Rectangle boundingRect = polyForBox.getBoundingRectangle();
        float widthOfBox = boundingRect.getWidth();
        float heightOfBox = boundingRect.getHeight();


        //polygonShape.setAsBox(w / 2, h / 2);
        polygonShape.set(rawPoints);

        FixtureDef fixtureBox = new FixtureDef();
        fixtureBox.shape = polygonShape;
        fixtureBox.density = 1;
        fixtureBox.friction = 1;
        fixtureBox.restitution = 0.1f;

        ////////////////////////////////////////
        regularPoly.w = widthOfBox * meterSize;//radius * 2 * meterSize;
        regularPoly.h = heightOfBox * meterSize;//radius * 2 * meterSize;
        regularPoly.fixture = fixtureBox;
        regularPoly.bodyType = "poly";
        ////////////////////////////////////////

        regularPoly.body.createFixture(fixtureBox);
        polygonShape.dispose();
        tabodies.add(regularPoly);
        return regularPoly;
    }



    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Other stuff:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public void update() {
        this.update(Gdx.graphics.getDeltaTime());
    }

    public void update(float delta) {
        world.step(delta, 6, 2);
        // Move sprites:
        for(Tabody t : tabodies) {
            if(t.sprite != null) {
                float xb = t.body.getPosition().x * meterSize;
                float yb = t.body.getPosition().y * meterSize;
                xb -= t.sprite.getWidth() / 2;
                yb -= t.sprite.getHeight() / 2;
                t.sprite.setPosition(xb, yb);
                t.sprite.setRotation(t.body.getAngle() * MathUtils.radiansToDegrees);
            }
        }
    }

    public void dispose() {
        world.dispose();
        renderer.dispose();
    }

    public void draw() {
        // Draw sprites:
        for(Tabody t : tabodies) {
            if(t.sprite != null) {
                spriteBath.begin();
                t.sprite.draw(spriteBath);
                spriteBath.end();
            }
            if(this.debug) {
                renderer.render(world, camera.combined);
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setAutoShapeType(true);
                //sr.setColor(Color.DARK_GRAY);
                if(t.bodyType.equals("poly")) {
                    /*
                    sr.setColor(Color.MAROON);
                    PolygonShape ps = (PolygonShape)t.body.getFixtureList().get(0).getShape();
                    int vc = ps.getVertexCount();
                    Polygon polyAux = new Polygon();
                    Vector2 tmp = new Vector2();
                    int vertIndex = 0;
                    float[] vertices = new float[vc * 2];
                    for(int i = 0; i < vertices.length - 1; i+= 2) {
                        ps.getVertex(vertIndex, tmp);
                        vertices[i] = tmp.x * meterSize;
                        vertices[i + 1] = tmp.y * meterSize;
                        vertIndex++;
                    }
                    //pl(vertices.length);
                    polyAux.setVertices(vertices);
                    Rectangle boxAux = polyAux.getBoundingRectangle();
                    polyAux.setOrigin(boxAux.x + boxAux.width / 2, boxAux.y + boxAux.height / 2);
                    polyAux.setRotation(t.body.getAngle() * MathUtils.radiansToDegrees);
                    polyAux.setPosition(
                            t.body.getPosition().x * meterSize,
                            t.body.getPosition().y * meterSize);
                    float[] trans = polyAux.getTransformedVertices();
                    sr.polygon(trans);
                    */
                }

                sr.setColor(Color.RED);
                // Center of mass:
                sr.circle(
                        t.body.getWorldCenter().x * meterSize,
                        t.body.getWorldCenter().y * meterSize, 3);
                // Geometric center:
                sr.setColor(Color.CYAN);
                sr.set(ShapeRenderer.ShapeType.Line);
                sr.circle(
                        t.body.getPosition().x * meterSize,
                        t.body.getPosition().y * meterSize, 3);
                sr.end();
            }
        }
    }

    private void setType(BodyDef def, String type) {
        type = type.toLowerCase();
        if(type.equals("d")) {
            def.type = BodyDef.BodyType.DynamicBody;
        } else if(type.equals("s")) {
            def.type = BodyDef.BodyType.StaticBody;
        } else if(type.equals("k")) {
            def.type = BodyDef.BodyType.KinematicBody;
        } else {
            perr("'d', 's' or 'k' expected");
        }
    }

    public class Tabody {
        Body body;
        String bodyType;// "circle", "rectangle" or "polygon".
        Sprite sprite;
        FixtureDef fixture;
        Vector2 geomCenter;
        float w, h;
        float[] vertices;

        public Tabody setTexture(Texture texture) {
            this.sprite = new Sprite(texture);
            float scaleX = this.w / this.sprite.getWidth();
            float scaleY = this.h / this.sprite.getHeight();
            float posX = this.body.getPosition().x * meterSize;
            float posY = this.body.getPosition().y * meterSize;
            sprite.setOrigin(texture.getWidth() / 2, texture.getHeight() / 2);
            sprite.setPosition(posX, posY);
            sprite.setScale(scaleX, scaleY);
            return this;
        }
    }

    @Override
    public Object clone() {
        return this;
    }

    private static void pl(Object o){
        System.out.println(o);
    }

    private static void perr(Object o){
        System.err.println(o);
    }

    private static String nl() {
        return System.getProperty("line.separator");
    }
}
